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
    currentScale: Float = 1f
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return

    // 1. During Zero-Bass Vocal Breakdowns, pause enemy telegraphs so the screen breathes with Ethan's voice!
    if (multiBand.bassDropOut) {
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

    // 3. Only initiate new telegraphs on the beat when total active + telegraphed threats are below cap
    val maxActiveEnemies = ((5.5f + difficulty * 3.8f) * spawnRateScale).roundToInt().coerceIn(5, 12)
    if (enemies.size + telegraphs.size >= maxActiveEnemies) return

    val isBeatOnset = beatPhase in 0f..0.32f
    if (!isBeatOnset) return

    val rampFactor = (levelPlayTime / 14f).coerceIn(0.48f, 1.0f)
    val energyScale = (0.65f + multiBand.bass * 0.50f + multiBand.rms * 0.35f).coerceIn(0.60f, 1.40f)
    val effectiveSpawnChance = (0.085f + difficulty * 0.080f) * spawnRateScale * rampFactor * energyScale

    if (Random.nextFloat() < effectiveSpawnChance) {
        val spawnCount = if ((multiBand.bass > 0.62f || difficulty > 0.48f) &&
            enemies.size + telegraphs.size + 1 < maxActiveEnemies &&
            Random.nextFloat() < 0.42f
        ) 2 else 1
        val baseSide = Random.nextInt(4)
        val warnSec = (0.86f - difficulty * 0.16f).coerceIn(0.56f, 0.86f)

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
                (stageMode == StageMode.SPEED_PORTAL || stageMode == StageMode.OVERDRIVE_SHATTER || difficulty > 0.55f) &&
                multiBand.high > 0.56f &&
                enemies.none { it.type == EnemyType.LASER_SWEEPER } &&
                telegraphs.none { it.spawnData.type == EnemyType.LASER_SWEEPER } &&
                Random.nextFloat() < 0.18f

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
            val finalSpeed = (spawnData.speed + difficulty * 1.45f) * speedScale * (0.80f + 0.20f * rampFactor)

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
    multiBand: MultiBandAudioState = MultiBandAudioState()
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

    // Keep an 82px buffer around the active target orb so enemies don't sit directly on top of collectibles
    if (activeTargetOrb != null) {
        val odx = e.x - activeTargetOrb.x
        val ody = e.y - activeTargetOrb.y
        val oDist = hypot(odx, ody)
        if (oDist in 1f..82f) {
            val push = ((82f - oDist) / 82f) * 3.0f * currentScale
            e.x += (odx / oDist) * push
            e.y += (ody / oDist) * push
        }
    }

    val beatSpeedMult = when (e.type) {
        EnemyType.SEEKER -> if (beatKick > 0.5f) 1.32f else 0.98f
        EnemyType.PULSE -> if (beatKick > 0.5f) 1.28f else 0.98f
        else -> 1.04f
    }

    val baseVx = cos(e.angle) * e.speed * beatSpeedMult
    val baseVy = sin(e.angle) * e.speed * beatSpeedMult
    val perpAngle = e.angle + (PI.toFloat() / 2f)
    val oscFactor = cos(gameTime * 3.8f + e.phase) * (e.orbitRadius * 0.09f)
    val oscVx = cos(perpAngle) * oscFactor
    val oscVy = sin(perpAngle) * oscFactor

    e.x += (baseVx + oscVx) * currentScale
    e.y += (baseVy + oscVy) * currentScale
}

/**
 * V5 TRAVELING HARMONIC COMET ORBS (ZERO GRAVITATIONAL PULL!):
 * - Collectibles NEVER yank or pull toward the player (`magnetRadius = 0`).
 * - Instead, they enter from the perimeter and travel smoothly across the playfield along curved
 *   harmonic trajectories (similar to enemy sprites), pulsing on every quarter-note beat!
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
    beatPhase: Float = 0f
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return
    val iter = powerUps.iterator()
    val beatKick = if (beatPhase in 0f..0.22f) 1.18f else 0.96f

    while (iter.hasNext()) {
        val orb = iter.next()
        // Remove once it has lived past its travel window or exited the arena after passing its target beat
        if (trackSec - orb.targetBeatSec > 4.2f) {
            iter.remove()
            continue
        }

        val perpAngle = orb.angle + (PI.toFloat() / 2f)
        val wave = sin(gameTime * 2.8f + orb.phase) * 0.65f
        val moveSpd = orb.speed * beatKick * currentScale

        orb.x += (cos(orb.angle) * moveSpd + cos(perpAngle) * wave)
        orb.y += (sin(orb.angle) * moveSpd + sin(perpAngle) * wave)

        // Gentle boundary reflection so traveling orbs stay inside the reachable arena during their beat window
        val marginX = 56f
        val marginYTop = 148f
        val marginYBottom = screenHeight - 96f
        if (orb.x < marginX && cos(orb.angle) < 0f) {
            orb.angle = PI.toFloat() - orb.angle
            orb.x = marginX
        } else if (orb.x > screenWidth - marginX && cos(orb.angle) > 0f) {
            orb.angle = PI.toFloat() - orb.angle
            orb.x = screenWidth - marginX
        }
        if (orb.y < marginYTop && sin(orb.angle) < 0f) {
            orb.angle = -orb.angle
            orb.y = marginYTop
        } else if (orb.y > marginYBottom && sin(orb.angle) > 0f) {
            orb.angle = -orb.angle
            orb.y = marginYBottom
        }
    }
}

/**
 * V5 FADING SINGULARITY WORMHOLES & HARMONIC PHASE BARRIERS:
 * - Spawns occasional swirling wormholes or rotating laser phase barriers that smoothly fade in (1.45s warning),
 *   stay active for 4.6s (warping enemy trajectories & creating spatial obstacles), and fade out (1.15s).
 */
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
    remixMode: TrackRemixMode = TrackRemixMode.ORIGINAL
) {
    if (screenWidth <= 0f || screenHeight <= 0f) return

    val wIter = wormholes.iterator()
    while (wIter.hasNext()) {
        val w = wIter.next()
        w.ageSec += 0.016f * currentScale
        if (w.kind == AnomalyKind.PHASE_BARRIER) {
            w.angle += 0.012f * currentScale
        } else if (w.isHazardActive) {
            // Active Singularity Wormhole curves nearby enemy trajectories around its event horizon!
            for (e in enemies) {
                val dx = w.x - e.x
                val dy = w.y - e.y
                val d = hypot(dx, dy)
                if (d in 18f..195f) {
                    val tangentAngle = atan2(dy, dx) + (PI.toFloat() / 2.6f)
                    val diff = normalizeAngle(tangentAngle - e.angle)
                    e.angle += diff.coerceIn(-0.032f, 0.032f) * currentScale
                }
            }
        }
        if (w.ageSec >= w.totalDuration) {
            wIter.remove()
        }
    }

    val maxAnomalies = if (remixMode == TrackRemixMode.SLOWED || remixMode == TrackRemixMode.ENDLESS) 2 else 1
    if (wormholes.size >= maxAnomalies || multiBand.bassDropOut) return

    val isBeatOnset = beatPhase in 0f..0.20f
    if (!isBeatOnset) return

    val swellTrigger = multiBand.rms > 0.52f || multiBand.swellDelta > 0.08f
    val spawnChance = if (swellTrigger) 0.065f else 0.022f
    if (Random.nextFloat() < spawnChance) {
        // Pick a safe coordinate at least 165px away from the player
        var wx = screenWidth * 0.5f
        var wy = screenHeight * 0.45f
        for (attempt in 0 until 8) {
            val candX = 95f + Random.nextFloat() * (screenWidth - 190f).coerceAtLeast(80f)
            val candY = 185f + Random.nextFloat() * (screenHeight - 330f).coerceAtLeast(100f)
            if (hypot(candX - player.x, candY - player.y) > 165f) {
                wx = candX
                wy = candY
                break
            }
        }
        val isBarrier = Random.nextFloat() < 0.38f
        wormholes.add(
            SingularityWormhole(
                x = wx,
                y = wy,
                radius = if (isBarrier) 22f else 48f,
                kind = if (isBarrier) AnomalyKind.PHASE_BARRIER else AnomalyKind.WORMHOLE_VORTEX,
                angle = Random.nextFloat() * PI.toFloat(),
                length = 128f,
                color = if (isBarrier) Color(0xFFF43F5EL) else Color(0xFFA855F7L)
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
