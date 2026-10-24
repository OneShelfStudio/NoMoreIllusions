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
    val maxActiveEnemies = (sectionMaxEnemies * spawnRateScale).roundToInt().coerceIn(1, 12)
    if (enemies.size + telegraphs.size >= maxActiveEnemies) return

    val isBeatOnset = beatPhase in 0f..0.32f
    if (!isBeatOnset) return

    val rampFactor = (levelPlayTime / 18f).coerceIn(0.55f, 1.0f)
    val energyScale = (0.58f + multiBand.bass * 0.45f + multiBand.rms * 0.32f).coerceIn(0.50f, 1.25f)
    val sectionUrgency = if (sectionMaxEnemies >= 4) 1.15f else if (sectionMaxEnemies >= 2) 0.88f else 0.62f
    val effectiveSpawnChance = (0.062f + difficulty * 0.055f) * spawnRateScale * rampFactor * energyScale * sectionUrgency

    if (Random.nextFloat() < effectiveSpawnChance) {
        val spawnCount = if (sectionMaxEnemies >= 4 &&
            enemies.size + telegraphs.size + 1 < maxActiveEnemies &&
            Random.nextFloat() < 0.35f
        ) 2 else 1
        val baseSide = Random.nextInt(4)
        val warnSec = (1.02f - difficulty * 0.14f).coerceIn(0.72f, 1.05f)

        for (sIdx in 0 until spawnCount) {
            val side = (baseSide + sIdx * 2) % 4
            val margin = 24f
            val (edgeX, edgeY, entryX, entryY) = when (side) {
                0 -> {
                    val rx = (Random.nextFloat() * (screenWidth - 140f)) + 70f
                    floatArrayOf(rx, margin, rx, -28f)
                }
                1 -> {
                    val ry = (Random.nextFloat() * (screenHeight - 260f)) + 140f
                    floatArrayOf(screenWidth - margin, ry, screenWidth + 28f, ry)
                }
                2 -> {
                    val rx = (Random.nextFloat() * (screenWidth - 140f)) + 70f
                    floatArrayOf(rx, screenHeight - margin, rx, screenHeight + 28f)
                }
                else -> {
                    val ry = (Random.nextFloat() * (screenHeight - 260f)) + 140f
                    floatArrayOf(margin, ry, -28f, ry)
                }
            }

            val allowLaserSweeper = sIdx == 0 &&
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
                    "PULSARS" -> EnemySpawnData(EnemyType.PULSE, Color(0xFFF43F5EL), 2.95f, 16f)
                    "SEEKERS" -> EnemySpawnData(EnemyType.SEEKER, Color(0xFFFF1E42L), 2.90f, 10f)
                    "WANDERERS" -> EnemySpawnData(EnemyType.WANDERER, Color(0xFFFB7185L), 2.75f, 22f)
                    else -> {
                        val roll = Random.nextFloat()
                        when {
                            roll < 0.36f -> EnemySpawnData(EnemyType.PULSE, Color(0xFFF43F5EL), 2.95f, 16f)
                            roll < 0.74f -> EnemySpawnData(EnemyType.SEEKER, Color(0xFFFF1E42L), 2.90f, 10f)
                            else -> EnemySpawnData(EnemyType.WANDERER, Color(0xFFFB7185L), 2.75f, 22f)
                        }
                    }
                }
            }

            val targetAngle = atan2(player.y - edgeY, player.x - edgeX) + (Random.nextFloat() - 0.5f) * 0.18f
            val finalSpeed = (spawnData.speed + difficulty * 1.45f) * speedScale * sectionSpeedMult * (0.82f + 0.18f * rampFactor)

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
    if (distToPlayer < 68f) {
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
    val baseVy = sin(e.angle) * e.speed * beatSpeedMult * drumCutScale
    val perpAngle = e.angle + (PI.toFloat() / 2f)
    val oscFactor = cos(gameTime * 3.8f + e.phase) * (e.orbitRadius * 0.09f) * drumCutScale
    val oscVx = cos(perpAngle) * oscFactor
    val oscVy = sin(perpAngle) * oscFactor

    e.x += (baseVx + oscVx) * currentScale
    e.y += (baseVy + oscVy) * currentScale
}

internal fun computeHighwayPoint(
    laneFloat: Float,
    u: Float,
    screenWidth: Float,
    screenHeight: Float
): Offset {
    val horizonY = screenHeight * 0.16f
    val strikeY = screenHeight * 0.74f
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
 * V6 BAND HERO / GUITAR HERO SCROLLING RHYTHM HIGHWAY NOTES (ZERO MAGNET PULL!):
 * - Notes spawn at the top horizon (`u = 0`, `y = 16%`) in 1 of 4 perspective fret lanes and scroll down
 *   toward the Glowing Beat Strike Line (`u = 1.0`, `y = 74%`), crossing the line at the exact millisecond of `targetBeatSec`!
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
        if (trackSec > expireSec || u > 1.24f) {
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
    allowAnomalies: Boolean = false
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return

    val wIter = wormholes.iterator()
    while (wIter.hasNext()) {
        val w = wIter.next()
        w.ageSec += 0.016f * currentScale
        if (w.slideCooldownSec > 0f) {
            w.slideCooldownSec = (w.slideCooldownSec - 0.016f * currentScale).coerceAtLeast(0f)
        }
        if (w.ageSec >= 3.2f) {
            wIter.remove()
        }
    }

    if (!allowAnomalies || multiBand.bassDropOut) return
    if (wormholes.isNotEmpty()) return

    val isBeatOnset = beatPhase in 0f..0.22f
    if (!isBeatOnset) return

    val swellTrigger = multiBand.rms > 0.42f || multiBand.swellDelta > 0.05f
    val spawnChance = if (swellTrigger) 0.16f else 0.065f
    if (Random.nextFloat() < spawnChance) {
        val startLane = Random.nextInt(4)
        val delta = if (Random.nextBoolean()) 1 else -1
        val endLane = (startLane + delta).coerceIn(0, 3).let { if (it == startLane) (startLane + 1) % 4 else it }

        wormholes.add(
            SingularityWormhole(
                x = startLane.toFloat(),
                y = 0f,
                radius = 34f,
                kind = AnomalyKind.WORMHOLE_VORTEX,
                angle = 0f,
                length = 1f,
                color = Color(0xFFFACC15L),
                exitX = endLane.toFloat(),
                exitY = 1f,
                ctrlX = (startLane + endLane) * 0.5f,
                ctrlY = 0.5f
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
        pt.x += pt.vx * currentScale
        pt.y += pt.vy * currentScale
        pt.life -= 0.025f * currentScale
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
