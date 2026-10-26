package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

fun updateMenuMotes(
    motes: MutableList<GameObject>,
    screenWidth: Float,
    screenHeight: Float,
    beatPhase: Float
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return
    if (motes.size < 34) {
        val mColor = when (Random.nextInt(3)) {
            0 -> Color(0xFF22C55EL)
            1 -> Color(0xFF6366F1L)
            else -> Color(0xFFF8FAFCL)
        }
        motes.add(
            GameObject(
                x = Random.nextFloat() * screenWidth,
                y = Random.nextFloat() * screenHeight,
                radius = Random.nextFloat() * 3.5f + 1.2f,
                color = mColor,
                vx = (Random.nextFloat() - 0.5f) * 0.45f,
                vy = (Random.nextFloat() - 0.5f) * 0.45f
            )
        )
    }
    val beatBoost = if (beatPhase in 0f..0.2f) 2.2f else 1f
    motes.forEach { m ->
        m.x = (m.x + m.vx * beatBoost + screenWidth) % screenWidth
        m.y = (m.y + m.vy * beatBoost + screenHeight) % screenHeight
    }
}

/**
 * V4 BEAT-LOCKED & TELEGRAPHED ENEMY WAVE CHOREOGRAPHY:
 * - Never spawns enemies randomly without warning.
 * - During Zero-Bass Vocal Breakdowns (`multiBand.bassDropOut`), clears pending telegraphs and pauses spawns.
 * - On downbeats (`beatPhase in 0f..0.24f`), locks a 2-beat (`0.92s`) Crimson Perimeter Telegraph (`EnemySpawnTelegraph`)
 *   onto the screen border showing the exact entry point and laser trajectory sightline before the enemy launches!
 */
fun stepAndSpawnTelegraphedEnemies(
    enemies: MutableList<GameObject>,
    telegraphs: MutableList<EnemySpawnTelegraph>,
    shockwaves: MutableList<Shockwave>,
    levelPlayTime: Float,
    difficulty: Float,
    spawnRateScale: Float,
    speedScale: Float,
    screenWidth: Float,
    screenHeight: Float,
    customEnemyStyle: String,
    player: GameObject,
    applyColorBlind: (Color) -> Color,
    stageMode: StageMode = StageMode.ABYSSAL_WAVE,
    multiBand: MultiBandAudioState = MultiBandAudioState(),
    beatPhase: Float = 0f,
    currentScale: Float = 1f,
    sectionMaxEnemies: Int = 5,
    sectionSpeedMult: Float = 1.0f
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return

    // 1. During Zero-Bass Vocal Breakdowns, Intros, Hushes, or Outros, pause enemy telegraphs so the screen breathes with Ethan's voice!
    if (multiBand.bassDropOut || sectionMaxEnemies <= 0) {
        telegraphs.clear()
        return
    }

    // 2. Step active perimeter telegraphs and launch enemies when their 2-beat warning completes
    val tgIter = telegraphs.iterator()
    while (tgIter.hasNext()) {
        val tg = tgIter.next()
        tg.timer -= 0.016f * currentScale
        if (tg.timer <= 0f) {
            enemies.add(
                GameObject(
                    x = tg.entryX,
                    y = tg.entryY,
                    radius = if (tg.spawnData.type == EnemyType.LASER_SWEEPER) 11f else 16.5f,
                    color = applyColorBlind(tg.spawnData.baseColor),
                    type = tg.spawnData.type,
                    angle = tg.targetAngle,
                    phase = Random.nextFloat() * 6.28f,
                    orbitRadius = tg.spawnData.orbitRadius,
                    speed = tg.finalSpeed
                )
            )
            shockwaves.add(
                Shockwave(
                    x = tg.edgeX,
                    y = tg.edgeY,
                    radius = 0f,
                    maxRadius = 95f,
                    color = Color(0xFFFF1E42L),
                    life = 0.65f
                )
            )
            tgIter.remove()
        }
    }

    // 3. Only initiate new telegraphs on the beat when total active + telegraphed threats are below the section's musical cap
    val maxActiveEnemies = when {
        spawnRateScale <= 0.30f -> 1 // Easy / Chill Training Mode: at most 1 enemy at a time!
        spawnRateScale <= 0.58f -> minOf(2, (sectionMaxEnemies * spawnRateScale).roundToInt().coerceAtLeast(1)) // Normal Mode: 1-2 enemies max!
        else -> (sectionMaxEnemies * spawnRateScale).roundToInt().coerceIn(2, 6) // Hard / Fast / Endless
    }
    if (enemies.size + telegraphs.size >= maxActiveEnemies) return

    val isBeatOnset = beatPhase in 0f..0.28f
    if (!isBeatOnset) return

    val rampFactor = (levelPlayTime / 22f).coerceIn(0.45f, 1.0f)
    val energyScale = (0.52f + multiBand.bass * 0.38f + multiBand.rms * 0.25f).coerceIn(0.45f, 1.15f)
    val sectionUrgency = if (sectionMaxEnemies >= 4) 1.0f else if (sectionMaxEnemies >= 2) 0.75f else 0.50f
    val effectiveSpawnChance = (0.042f + difficulty * 0.040f) * spawnRateScale * rampFactor * energyScale * sectionUrgency

    if (Random.nextFloat() < effectiveSpawnChance) {
        val spawnCount = if (spawnRateScale > 0.85f &&
            sectionMaxEnemies >= 4 &&
            enemies.size + telegraphs.size + 1 < maxActiveEnemies &&
            Random.nextFloat() < 0.30f
        ) 2 else 1
        val baseSide = Random.nextInt(3)
        val warnSec = (1.05f - difficulty * 0.12f).coerceIn(0.75f, 1.08f)
        // Strict Upper-Screen Spawn Zone (top 26% of screen height only, NEVER near bottom!)
        val maxUpperSideY = (screenHeight * 0.26f).coerceAtLeast(130f)

        for (sIdx in 0 until spawnCount) {
            // ONLY spawn from Top Edge (0), Upper-Right (1), or Upper-Left (2) — NEVER from the bottom!
            val side = (baseSide + sIdx) % 3
            val margin = 24f
            val (edgeX, edgeY, entryX, entryY) = when (side) {
                0 -> {
                    val rx = (Random.nextFloat() * (screenWidth - 140f)) + 70f
                    floatArrayOf(rx, margin, rx, -28f)
                }
                1 -> {
                    val ry = 60f + Random.nextFloat() * (maxUpperSideY - 60f)
                    floatArrayOf(screenWidth - margin, ry, screenWidth + 28f, ry)
                }
                else -> {
                    val ry = 60f + Random.nextFloat() * (maxUpperSideY - 60f)
                    floatArrayOf(margin, ry, -28f, ry)
                }
            }

            val allowLaserSweeper = sIdx == 0 &&
                spawnRateScale > 0.85f &&
                sectionMaxEnemies >= 6 &&
                (stageMode == StageMode.SPEED_PORTAL || stageMode == StageMode.OVERDRIVE_SHATTER || stageMode == StageMode.GOLDEN_OVERDRIVE) &&
                multiBand.high > 0.54f &&
                enemies.none { it.type == EnemyType.LASER_SWEEPER } &&
                telegraphs.none { it.spawnData.type == EnemyType.LASER_SWEEPER } &&
                Random.nextFloat() < 0.22f

            val spawnData = if (allowLaserSweeper) {
                EnemySpawnData(EnemyType.LASER_SWEEPER, Color(0xFFFF1E42L), 2.85f, 62f)
            } else {
                when (customEnemyStyle) {
                    "PULSARS" -> EnemySpawnData(EnemyType.PULSE, Color(0xFFF43F5EL), 2.75f, 16f)
                    "SEEKERS" -> EnemySpawnData(EnemyType.SEEKER, Color(0xFFFF1E42L), 2.70f, 10f)
                    "WANDERERS" -> EnemySpawnData(EnemyType.WANDERER, Color(0xFFFB7185L), 2.55f, 22f)
                    else -> {
                        val roll = Random.nextFloat()
                        when {
                            roll < 0.36f -> EnemySpawnData(EnemyType.PULSE, Color(0xFFF43F5EL), 2.75f, 16f)
                            roll < 0.74f -> EnemySpawnData(EnemyType.SEEKER, Color(0xFFFF1E42L), 2.70f, 10f)
                            else -> EnemySpawnData(EnemyType.WANDERER, Color(0xFFFB7185L), 2.55f, 22f)
                        }
                    }
                }
            }

            // Strictly downward trajectory angle so enemies always travel from top toward bottom
            val rawAngle = atan2((player.y.coerceAtLeast(edgeY + 140f)) - edgeY, player.x - edgeX) + (Random.nextFloat() - 0.5f) * 0.16f
            val targetAngle = rawAngle.coerceIn(PI.toFloat() * 0.16f, PI.toFloat() * 0.84f)
            val finalSpeed = (spawnData.speed + difficulty * 1.25f) * speedScale * sectionSpeedMult * (0.82f + 0.18f * rampFactor)

            telegraphs.add(
                EnemySpawnTelegraph(
                    edgeX = edgeX,
                    edgeY = edgeY,
                    entryX = entryX,
                    entryY = entryY,
                    targetAngle = targetAngle,
                    timer = warnSec,
                    maxTimer = warnSec,
                    spawnData = spawnData,
                    finalSpeed = finalSpeed
                )
            )
        }
    }
}

fun stepEnemyKinematicsAndFlocking(
    e: GameObject,
    enemies: List<GameObject>,
    player: GameObject,
    gameTime: Float,
    currentScale: Float,
    beatPhase: Float = 0f,
    activeTargetOrb: GameObject? = null,
    activePhenomenon: ActivePhenomenon? = null,
    multiBand: MultiBandAudioState = MultiBandAudioState(),
    allBeatOrbs: List<GameObject> = emptyList()
) {
    val beatKick = if (beatPhase in 0f..0.22f) 1.0f else 0.0f

    // During Zero-Bass Vocal Breakdowns, existing enemies smoothly drift outward away from the center
    if (multiBand.bassDropOut) {
        e.hasPassedPlayer = true
        e.x += cos(e.angle) * e.speed * 1.35f * currentScale
        e.y += sin(e.angle) * e.speed * 1.35f * currentScale
        return
    }

    if (e.type == EnemyType.LASER_SWEEPER) {
        val moveSpd = e.speed * (1.0f + beatKick * 0.25f) * currentScale
        e.x += cos(e.angle) * moveSpd
        e.y += sin(e.angle) * moveSpd
        return
    }

    val distToPlayer = hypot(player.x - e.x, player.y - e.y)
    // Once an enemy approaches the player's vertical level (or lower half of screen), lock it into downward exit so it NEVER loops up from the bottom!
    if (distToPlayer < 85f || e.y >= player.y - 24f) {
        e.hasPassedPlayer = true
    }

    // Active Supernova Phenomenon deflection (Crystal Prism, Magnetar & Binary Heart push enemies outward!)
    if (activePhenomenon != null && activePhenomenon.timer > 0f) {
        when (activePhenomenon.effect) {
            KeyWordEffect.PRISM_SHATTER, KeyWordEffect.MAGNETAR_WAR, KeyWordEffect.BINARY_HEART -> {
                if (distToPlayer in 1f..230f) {
                    val pushX = (e.x - player.x) / distToPlayer
                    val pushY = (e.y - player.y) / distToPlayer
                    val force = ((230f - distToPlayer) / 230f) * 6.8f * currentScale
                    e.x += pushX * force
                    e.y += pushY * force
                    e.hasPassedPlayer = true
                }
            }
            else -> {}
        }
    }

    var sepX = 0f
    var sepY = 0f
    var alignAngleSin = 0f
    var alignAngleCos = 0f
    var neighborCount = 0

    for (other in enemies) {
        if (other !== e && other.type != EnemyType.LASER_SWEEPER) {
            val dx = e.x - other.x
            val dy = e.y - other.y
            val distSq = dx * dx + dy * dy
            if (distSq > 0.001f && distSq < 6400f) {
                val d = sqrt(distSq)
                sepX += (dx / d) / d
                sepY += (dy / d) / d
                alignAngleSin += sin(other.angle)
                alignAngleCos += cos(other.angle)
                neighborCount++
            }
        }
    }

    when (e.type) {
        EnemyType.SEEKER -> {
            if (!e.hasPassedPlayer) {
                val targetAngle = atan2(player.y - e.y, player.x - e.x)
                val diff = normalizeAngle(targetAngle - e.angle)
                val turnRate = if (beatKick > 0.5f) 0.036f else 0.028f
                e.angle += diff.coerceIn(-turnRate, turnRate) * currentScale
            }
        }
        EnemyType.PULSE -> {
            val wave = sin(gameTime * 3.4f + e.phase) * 0.028f
            e.angle += wave * currentScale
        }
        EnemyType.WANDERER -> {
            val curve = 0.015f * sin(gameTime * 1.6f + e.phase) + 0.009f * cos(gameTime * 2.8f)
            e.angle += curve * currentScale
        }
        EnemyType.PHANTOM -> {
            if (!e.hasPassedPlayer) {
                val targetAngle = atan2(player.y - e.y, player.x - e.x)
                val diff = normalizeAngle(targetAngle - e.angle)
                e.angle += (diff.coerceIn(-0.016f, 0.016f) + sin(gameTime * 4.5f + e.phase) * 0.025f) * currentScale
            }
            e.life = (sin(gameTime * 4.2f + e.phase) + 1f) / 2f
        }
        EnemyType.LASER_SWEEPER -> {}
    }

    if (neighborCount > 0) {
        val avgAlign = atan2(alignAngleSin, alignAngleCos)
        val alignDiff = normalizeAngle(avgAlign - e.angle)
        e.angle += alignDiff.coerceIn(-0.010f, 0.010f) * currentScale
        e.x += sepX * 12f * currentScale
        e.y += sepY * 12f * currentScale
    }

    // Keep a generous 135px safe buffer around active stationary beat nodes so enemies NEVER camp on top of notes!
    val orbsToGuard = if (allBeatOrbs.isNotEmpty()) allBeatOrbs else listOfNotNull(activeTargetOrb)
    for (orb in orbsToGuard) {
        val odx = e.x - orb.x
        val ody = e.y - orb.y
        val oDist = hypot(odx, ody)
        if (oDist in 1f..135f) {
            val push = ((135f - oDist) / 135f) * 4.8f * currentScale
            e.x += (odx / oDist) * push
            e.y += (ody / oDist) * push
        }
    }

    val beatSpeedMult = when (e.type) {
        EnemyType.SEEKER -> if (beatKick > 0.5f) 1.32f else 0.98f
        EnemyType.PULSE -> if (beatKick > 0.5f) 1.28f else 0.98f
        else -> 1.04f
    }
    // Syncopated drum-cut breathing (e.g., 0:51 in Confide): when bass dips below 0.16, enemies briefly suspend/slow
    val drumCutScale = if (multiBand.bass < 0.16f) 0.42f else 1.0f

    val baseVx = cos(e.angle) * e.speed * beatSpeedMult * drumCutScale
    // Enforce strictly downward vertical movement (minDownwardVy) so enemies NEVER fly upward from the bottom!
    val minDownwardVy = e.speed * 0.42f * drumCutScale
    val baseVy = maxOf(minDownwardVy, sin(e.angle) * e.speed * beatSpeedMult * drumCutScale)
    val perpAngle = e.angle + (PI.toFloat() / 2f)
    val oscFactor = cos(gameTime * 3.8f + e.phase) * (e.orbitRadius * 0.09f) * drumCutScale
    val oscVx = cos(perpAngle) * oscFactor
    val oscVy = maxOf(0f, sin(perpAngle) * oscFactor)

    e.x += (baseVx + oscVx) * currentScale
    e.y += (baseVy + oscVy) * currentScale
}

internal fun computeHighwayPoint(
    laneFloat: Float,
    u: Float,
    screenWidth: Float,
    screenHeight: Float
): Offset {
    val horizonY = screenHeight * 0.15f
    val strikeY = screenHeight * 0.65f
    val topHalfW = screenWidth * 0.14f
    val strikeHalfW = screenWidth * 0.42f
    val clampedU = u.coerceIn(-0.05f, 1.32f)
    val p = if (clampedU >= 0f) clampedU.pow(1.25f) else clampedU
    val halfW = topHalfW + (strikeHalfW - topHalfW) * p
    val laneNorm = ((laneFloat - 1.5f) / 1.5f) * 0.78f
    val x = screenWidth * 0.5f + laneNorm * halfW
    val y = horizonY + (strikeY - horizonY) * p
    return Offset(x, y)
}

internal fun computeHighwayScale(u: Float): Float {
    val clampedU = u.coerceIn(0f, 1.25f)
    return (0.44f + 0.56f * clampedU.pow(1.15f)).coerceIn(0.42f, 1.15f)
}

/**
 * Spawns a breathtaking Melodic Starfall Curtain — 32 golden & aurora stardust petals that
 * gently sway side-to-side (`sin(phase)`) while raining down from the top of the stage on major musical hits!
 */
fun spawnMelodicStarfallCascade(
    particles: MutableList<GameObject>,
    screenWidth: Float,
    screenHeight: Float
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return
    val palette = listOf(
        Color(0xFFFACC15L), // 24K Gold
        Color(0xFF38BDF8L), // Electric Cyan
        Color(0xFF4ADE80L), // Bioluminescent Emerald
        Color(0xFFF472B6L)  // Aurora Rose
    )
    for (i in 0 until 32) {
        val xPos = (0.08f + (i / 31f) * 0.84f + (Random.nextFloat() - 0.5f) * 0.04f) * screenWidth
        val yPos = -12f - Random.nextFloat() * (screenHeight * 0.28f)
        particles.add(
            GameObject(
                x = xPos,
                y = yPos,
                radius = 4.2f + Random.nextFloat() * 3.8f,
                color = palette[i % palette.size],
                vx = (Random.nextFloat() - 0.5f) * 0.8f,
                vy = 2.6f + Random.nextFloat() * 2.2f,
                life = 1.35f + Random.nextFloat() * 0.45f,
                phase = Random.nextFloat() * 6.28f,
                orbitRadius = 99f // Flag for swaying Melodic Starfall Petal
            )
        )
    }
}

/**
 * V6 BAND HERO / GUITAR HERO SCROLLING RHYTHM HIGHWAY NOTES (ZERO MAGNET PULL!):
 * - Notes spawn at the top horizon (`u = 0`, `y = 15%`) in 1 of 4 perspective fret lanes and scroll down
 *   toward the Glowing Beat Strike Line (`u = 1.0`, `y = 65%`), crossing the line at the exact millisecond of `targetBeatSec`!
 */
@Suppress("UNUSED_PARAMETER")
fun stepNebulousBeatOrbs(
    powerUps: MutableList<GameObject>,
    player: GameObject,
    gameTime: Float,
    trackSec: Float,
    screenWidth: Float,
    screenHeight: Float,
    isGoldenOverdrive: Boolean = false,
    activePhenomenon: ActivePhenomenon? = null,
    currentScale: Float = 1f,
    beatPhase: Float = 0f,
    magnetActive: Boolean = false,
    onBeatMissed: (GameObject) -> Unit = {}
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return
    val iter = powerUps.iterator()

    while (iter.hasNext()) {
        val orb = iter.next()
        val leadDur = (orb.targetBeatSec - orb.spawnBeatSec).coerceAtLeast(0.45f)
        val u = (trackSec - orb.spawnBeatSec) / leadDur
        val expireSec = if (orb.expireBeatSec > 0f) orb.expireBeatSec else (orb.targetBeatSec + 0.45f)
        if (trackSec > expireSec || u > 1.22f) {
            onBeatMissed(orb)
            iter.remove()
            continue
        }

        val laneIdx = (orb.phraseBeatNum - 1).coerceIn(0, 3).toFloat()
        val pt = computeHighwayPoint(laneIdx, u, screenWidth, screenHeight)
        orb.anchorX = pt.x
        orb.anchorY = pt.y
        orb.x = pt.x
        orb.y = pt.y
    }
}

@Suppress("UNUSED_PARAMETER")
fun stepAndSpawnWormholes(
    wormholes: MutableList<SingularityWormhole>,
    enemies: List<GameObject>,
    player: GameObject,
    gameTime: Float,
    currentScale: Float,
    screenWidth: Float,
    screenHeight: Float,
    multiBand: MultiBandAudioState,
    beatPhase: Float,
    remixMode: TrackRemixMode = TrackRemixMode.ORIGINAL,
    allowAnomalies: Boolean = false,
    powerUps: MutableList<GameObject>? = null
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return

    val wIter = wormholes.iterator()
    while (wIter.hasNext()) {
        val w = wIter.next()
        w.ageSec += 0.016f * currentScale
        if (w.slideCooldownSec > 0f) {
            w.slideCooldownSec = (w.slideCooldownSec - 0.016f * currentScale).coerceAtLeast(0f)
        }
        if (w.ageSec >= 4.4f) {
            wIter.remove()
        }
    }

    // Never clear active mid-flight beat pegs (`powerUps`) when a sustain hold spawns or is active!
    // Allow 2-Finger Vocal Sustain Holds during both `allowAnomalies` sections AND ethereal vocal-float breakdowns (`bassDropOut && vocal >= 0.42f`)!
    val canSpawnVocalHold = allowAnomalies || (multiBand.bassDropOut && multiBand.vocal >= 0.42f)
    if (!canSpawnVocalHold) return
    if (wormholes.isNotEmpty()) return

    val isBeatOnset = beatPhase in 0f..0.22f
    if (!isBeatOnset) return

    // Trigger 2-Finger Vocal Sustain Holds during long, flowy vocal/harmonic swells ("ooh / aaah" moments)!
    val vocalSwellTrigger = (multiBand.vocal >= 0.46f && (multiBand.rms > 0.40f || multiBand.swellDelta > 0.03f)) ||
        (multiBand.bassDropOut && multiBand.vocal >= 0.42f)
    if (!vocalSwellTrigger) return

    if (Random.nextFloat() < 0.16f) {
        wormholes.add(
            SingularityWormhole(
                x = 0f, // Lane 1 Left Sustain Anchor Peg
                y = 0f,
                radius = 36f,
                kind = AnomalyKind.WORMHOLE_VORTEX,
                angle = 0f,
                length = 1f,
                color = Color(0xFF38BDF8L),
                exitX = 3f, // Lane 4 Right Sustain Anchor Peg
                exitY = 1f,
                ctrlX = 1.5f,
                ctrlY = 0f // 0.0 -> 1.0 Hold Progress Gauge!
            )
        )
    }
}

fun stepTetheredSatellites(
    satellites: MutableList<TetheredSatellite>,
    player: GameObject,
    currentScale: Float
) {
    satellites.forEach { sat ->
        sat.orbitAngle += sat.orbitSpeed * 0.022f * currentScale
        val targetX = player.x + cos(sat.orbitAngle) * sat.orbitRadius
        val targetY = player.y + sin(sat.orbitAngle) * sat.orbitRadius
        if (sat.x == 0f && sat.y == 0f) {
            sat.x = targetX
            sat.y = targetY
        } else {
            sat.x += (targetX - sat.x) * 0.28f
            sat.y += (targetY - sat.y) * 0.28f
        }
    }
}

fun updateAmbientEntities(
    motes: MutableList<GameObject>,
    particles: MutableList<GameObject>,
    floatingTexts: MutableList<FloatingText>,
    shockwaves: MutableList<Shockwave>,
    playerTrail: MutableList<Offset>,
    player: GameObject,
    targetX: Float,
    targetY: Float,
    isPlaying: Boolean,
    currentScale: Float,
    screenWidth: Float,
    screenHeight: Float
) {
    if (motes.size < 34 && screenWidth > 0f && screenHeight > 0f) {
        val mColor = when (Random.nextInt(3)) {
            0 -> Color(0xFF22C55EL)
            1 -> Color(0xFF6366F1L)
            else -> Color(0xFFF8FAFCL)
        }
        motes.add(
            GameObject(
                x = Random.nextFloat() * screenWidth,
                y = Random.nextFloat() * screenHeight,
                radius = Random.nextFloat() * 3.5f + 1.2f,
                color = mColor,
                vx = (Random.nextFloat() - 0.5f) * 0.45f,
                vy = (Random.nextFloat() - 0.5f) * 0.45f
            )
        )
    }
    if (screenWidth > 0f && screenHeight > 0f) {
        motes.forEach { m ->
            m.x = (m.x + m.vx * currentScale + screenWidth) % screenWidth
            m.y = (m.y + m.vy * currentScale + screenHeight) % screenHeight
        }
    }

    val ptIter = particles.iterator()
    while (ptIter.hasNext()) {
        val pt = ptIter.next()
        if (pt.orbitRadius > 50f) {
            // Melodic Starfall Petal: gentle horizontal harmonic sway while cascading down
            pt.phase += 0.085f * currentScale
            pt.x += (pt.vx + sin(pt.phase) * 2.4f) * currentScale
            pt.y += pt.vy * currentScale
            pt.life -= 0.014f * currentScale
        } else {
            pt.x += pt.vx * currentScale
            pt.y += pt.vy * currentScale
            pt.life -= 0.025f * currentScale
        }
        if (pt.life <= 0f) ptIter.remove()
    }

    val ftIter = floatingTexts.iterator()
    while (ftIter.hasNext()) {
        val ft = ftIter.next()
        ft.life -= 0.022f * currentScale
        if (ft.life <= 0f) ftIter.remove()
    }

    val swIter = shockwaves.iterator()
    while (swIter.hasNext()) {
        val sw = swIter.next()
        sw.radius += (sw.maxRadius - sw.radius) * 0.14f * currentScale
        sw.life -= 0.032f * currentScale
        if (sw.life <= 0f) swIter.remove()
    }

    player.x += (targetX - player.x) * 0.32f
    player.y += (targetY - player.y) * 0.32f

    if (isPlaying) {
        playerTrail.add(Offset(player.x, player.y))
        if (playerTrail.size > 20) playerTrail.removeAt(0)
    }
}
