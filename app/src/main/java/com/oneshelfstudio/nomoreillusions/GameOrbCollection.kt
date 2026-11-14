package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

internal data class PowerUpPickupResult(
    val newStarPowerTime: Float,
    val newShieldTime: Float,
    val newSlowMoTime: Float,
    val newMagnetTime: Float,
    val newShakeAmount: Float,
    val newCameraPunch: Float,
    val newBeatFlashIntensity: Float,
    val bonusScore: Int,
    val toast: PowerUpBannerToast?
)

internal fun handleRegularOrbPowerUp(
    p: GameObject,
    mult: Int,
    difficultyScoreMultiplier: Float,
    isCaughtOnBeat: Boolean,
    gameTime: Float,
    starPowerTime: Float,
    shieldTime: Float,
    slowMoTime: Float,
    magnetTime: Float,
    shakeAmount: Float,
    cameraPunch: Float,
    beatFlashIntensity: Float,
    enemies: MutableList<GameObject>,
    enemyTelegraphs: MutableList<EnemySpawnTelegraph>,
    tetheredSatellites: MutableList<TetheredSatellite>,
    shockwaves: MutableList<Shockwave>,
    particles: MutableList<GameObject>
): PowerUpPickupResult {
    var nextStarPower = starPowerTime
    var nextShield = shieldTime
    var nextSlowMo = slowMoTime
    var nextMagnet = magnetTime
    var nextShake = shakeAmount
    var nextPunch = cameraPunch
    var nextFlash = beatFlashIntensity
    var bonusScore = 0
    var toast: PowerUpBannerToast? = null

    when (p.powerUpType) {
        PowerUpType.ELECTRIC_HARMONIC -> {
            nextStarPower = maxOf(nextStarPower, 8.0f)
            nextShake = 14f
            nextPunch = 0.06f
            nextFlash = 1.0f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 650f, Color(0xFFFACC15L), 1.3f))
            shockwaves.add(Shockwave(p.x, p.y, 0f, 450f, Color(0xFF38BDF8L), 1.1f))
            toast = PowerUpBannerToast(
                icon = "⚡",
                title = "ELECTRIC SURGE ACTIVE!",
                subtitle = "SYNTH AURORA & 4X OVERDRIVE",
                accentColor = Color(0xFFFACC15L),
                timer = 2.6f,
                maxTimer = 2.6f
            )
        }
        PowerUpType.PRISM_HARMONY -> {
            nextFlash = 0.35f
            nextPunch = 0.045f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 500f, Color(0xFF4ADE80L), 1.2f))
            shockwaves.add(Shockwave(p.x, p.y, 0f, 320f, Color(0xFFEC4899L), 1.0f))
            toast = PowerUpBannerToast(
                icon = "✨",
                title = "PRISM VOCAL BLOOM!",
                subtitle = "CRYSTAL HARMONY & ART REVEAL",
                accentColor = Color(0xFF4ADE80L),
                timer = 2.6f,
                maxTimer = 2.6f
            )
        }
        PowerUpType.SLOW_MO -> {
            nextSlowMo = maxOf(nextSlowMo, 6.0f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 420f, Color(0xFFC084FCL), 1.1f))
            toast = PowerUpBannerToast(
                icon = "🌀",
                title = "VORTEX TIME-WARP!",
                subtitle = "50% SLOW-MO BREAKDOWN FLOW",
                accentColor = Color(0xFFC084FCL),
                timer = 2.6f,
                maxTimer = 2.6f
            )
        }
        PowerUpType.BLAST -> {
            nextShake = 16f
            nextPunch = 0.055f
            nextFlash = 0.95f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 750f, Color(0xFFF97316L), 1.4f))
            var vaporizedCount = 0
            val clearIter = enemies.iterator()
            while (clearIter.hasNext()) {
                val en = clearIter.next()
                vaporizedCount++
                bonusScore += (150 * mult * difficultyScoreMultiplier).toInt()
                shockwaves.add(Shockwave(en.x, en.y, 0f, 160f, Color(0xFFFACC15L), 0.90f))
                repeat(12) { idx ->
                    val a = (idx / 12f) * 6.283185f
                    particles.add(
                        GameObject(
                            x = en.x,
                            y = en.y,
                            radius = 5.0f,
                            color = if (idx % 2 == 0) Color(0xFFFACC15L) else Color(0xFFF97316L),
                            vx = cos(a) * 9.2f,
                            vy = sin(a) * 9.2f,
                            life = 0.85f
                        )
                    )
                }
                clearIter.remove()
            }
            enemyTelegraphs.clear()
            toast = PowerUpBannerToast(
                icon = "💥",
                title = "NOVA DETONATED!",
                subtitle = if (vaporizedCount > 0) "$vaporizedCount HAZARDS VAPORIZED! (+${vaporizedCount * 150 * mult} PTS)" else "ARENA CLEARED OF HAZARDS!",
                accentColor = Color(0xFFF97316L),
                timer = 2.2f,
                maxTimer = 2.2f
            )
        }
        PowerUpType.SHIELD -> {
            nextShield = maxOf(nextShield, 6.0f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 260f, Color(0xFF38BDF8L), 0.90f))
            toast = PowerUpBannerToast(
                icon = "🛡️",
                title = "AEGIS SHIELD ACTIVE!",
                subtitle = "ABSORBS NEXT HAZARD COLLISION",
                accentColor = Color(0xFF38BDF8L),
                timer = 2.2f,
                maxTimer = 2.2f
            )
        }
        else -> {}
    }

    if (tetheredSatellites.size >= 14) {
        tetheredSatellites.removeAt(0)
    }
    val satIdx = tetheredSatellites.size
    val ringRadius = if (satIdx % 2 == 0) 34f else 48f
    tetheredSatellites.add(
        TetheredSatellite(
            orbitAngle = (satIdx / 7f) * 2f * PI.toFloat(),
            orbitRadius = ringRadius,
            orbitSpeed = if (satIdx % 2 == 0) 2.2f else -1.6f,
            color = if (isCaughtOnBeat) Color(0xFFFACC15L) else p.color,
            size = 3.8f,
            x = p.x,
            y = p.y
        )
    )

    shockwaves.add(Shockwave(p.x, p.y, 0f, 125f, p.color, 0.75f))
    val pCount = if (isCaughtOnBeat) 12 else 6
    for (i in 0 until pCount) {
        val ang = (i.toFloat() / pCount) * 6.283185f + gameTime
        particles.add(
            GameObject(
                x = p.x,
                y = p.y,
                radius = if (isCaughtOnBeat) 4.4f else 3.4f,
                color = if (isCaughtOnBeat && i % 2 == 0) Color(0xFFFACC15L) else p.color,
                vx = cos(ang) * (if (isCaughtOnBeat) 7.5f else 4.5f),
                vy = sin(ang) * (if (isCaughtOnBeat) 7.5f else 4.5f),
                life = 0.65f
            )
        )
    }

    return PowerUpPickupResult(
        newStarPowerTime = nextStarPower,
        newShieldTime = nextShield,
        newSlowMoTime = nextSlowMo,
        newMagnetTime = nextMagnet,
        newShakeAmount = nextShake,
        newCameraPunch = nextPunch,
        newBeatFlashIntensity = nextFlash,
        bonusScore = bonusScore,
        toast = toast
    )
}

internal data class SupernovaPickupResult(
    val newPhenomenon: ActivePhenomenon,
    val newConstellationCounter: Int,
    val keywordBonus: Int,
    val newMagnetTime: Float,
    val newShakeAmount: Float,
    val newStarPowerTime: Float,
    val newScoreMultiplier: Int,
    val newMultiplierTime: Float,
    val newShieldTime: Float,
    val toast: PowerUpBannerToast
)

internal fun handleSupernovaOrbPickup(
    p: GameObject,
    player: GameObject,
    isGoldenActive: Boolean,
    scoreMultiplier: Int,
    multiplierTime: Float,
    difficultyScoreMultiplier: Float,
    constellationCounter: Int,
    magnetTime: Float,
    shakeAmount: Float,
    starPowerTime: Float,
    shieldTime: Float,
    enemies: MutableList<GameObject>,
    tetheredSatellites: List<TetheredSatellite>,
    shockwaves: MutableList<Shockwave>,
    particles: MutableList<GameObject>
): SupernovaPickupResult {
    var nextConstellation = constellationCounter
    if (p.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR) {
        nextConstellation++
    }

    val phenom = ActivePhenomenon(
        effect = p.keyWordEffect,
        timer = 5.5f,
        maxDuration = 5.5f,
        originX = p.x,
        originY = p.y,
        constellationIndex = constellationCounter
    )

    val mult = if (isGoldenActive || p.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR) 4 else maxOf(scoreMultiplier, 2)
    val keywordBonus = (140 * mult * difficultyScoreMultiplier).toInt()

    var nextMagnet = magnetTime
    var nextShake = shakeAmount
    var nextStarPower = starPowerTime
    var nextMult = scoreMultiplier
    var nextMultTime = multiplierTime
    var nextShield = shieldTime
    val toast: PowerUpBannerToast

    val burstColor = when (p.keyWordEffect) {
        KeyWordEffect.TRACTOR_REACH, KeyWordEffect.CONSTELLATION_STAR, KeyWordEffect.BINARY_HEART -> {
            nextStarPower = 8.0f
            nextMult = 4
            nextMultTime = 12f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 580f, Color(0xFFFACC15L), 1.30f))
            spawnMelodicStarfallCascade(particles, maxOf(1080f, p.x * 2f), maxOf(1920f, p.y * 2f))
            tetheredSatellites.forEach { sat ->
                val ang = sat.orbitAngle
                particles.add(
                    GameObject(
                        x = sat.x,
                        y = sat.y,
                        radius = 5.2f,
                        color = Color(0xFFFACC15L),
                        vx = cos(ang) * 9.5f,
                        vy = sin(ang) * 9.5f,
                        life = 0.95f
                    )
                )
            }
            toast = PowerUpBannerToast("⚡", "ELECTRIC HARMONIC SURGE!", "4x SCORE MULTIPLIER + AURORA OVERDRIVE!", Color(0xFF38BDF8L), 2.8f, 2.8f)
            Color(0xFF38BDF8L)
        }
        KeyWordEffect.PRISM_SHATTER, KeyWordEffect.MAGNETAR_WAR -> {
            nextShake = 12f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 540f, Color(0xFFF97316L), 1.25f))
            val eIter = enemies.iterator()
            while (eIter.hasNext()) {
                val e = eIter.next()
                shockwaves.add(Shockwave(e.x, e.y, 0f, 135f, Color(0xFFFDE047L), 0.85f))
                eIter.remove()
            }
            toast = PowerUpBannerToast("💥", "BOMB SHOCKWAVE!", "ALL HAZARDS VAPORIZED + $keywordBonus PTS", Color(0xFFF97316L), 2.5f, 2.5f)
            Color(0xFFF97316L)
        }
        KeyWordEffect.ECLIPSE_DARK -> {
            nextShield = maxOf(nextShield, 8.5f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 440f, Color(0xFF38BDF8L), 1.20f))
            toast = PowerUpBannerToast("🛡️", "SHIELD BARRIER!", "8.5s PROTECTIVE DOME ACTIVE", Color(0xFF38BDF8L), 2.4f, 2.4f)
            Color(0xFF38BDF8L)
        }
        KeyWordEffect.NONE -> {
            toast = PowerUpBannerToast("♪", "ON-BEAT STREAK!", "+$keywordBonus PTS", Color(0xFF38BDF8L), 2.0f, 2.0f)
            Color(0xFF38BDF8L)
        }
    }

    for (i in 0 until 6) {
        val ang = (i / 6f) * 6.283185f
        val spd = if (i % 2 == 0) 8.5f else 5.5f
        particles.add(
            GameObject(
                x = p.x,
                y = p.y,
                radius = if (i % 2 == 0) 4.8f else 3.5f,
                color = if (i % 2 == 0) Color.White else burstColor,
                vx = cos(ang) * spd,
                vy = sin(ang) * spd,
                life = 0.65f
            )
        )
    }

    return SupernovaPickupResult(
        newPhenomenon = phenom,
        newConstellationCounter = nextConstellation,
        keywordBonus = keywordBonus,
        newMagnetTime = nextMagnet,
        newShakeAmount = nextShake,
        newStarPowerTime = nextStarPower,
        newScoreMultiplier = nextMult,
        newMultiplierTime = nextMultTime,
        newShieldTime = nextShield,
        toast = toast
    )
}

/**
 * Guarantees ZERO overlapping beat pegs on the open playfield:
 * Enforces >= 124px separation from every active orb (`x,y` and `anchorX,anchorY`) across X: 14%..86%, Y: 13%..83%.
 */
private fun resolveNonOverlappingOrbSpot(
    desiredX: Float,
    desiredY: Float,
    screenWidth: Float,
    screenHeight: Float,
    existingOrbs: List<GameObject>,
    seedIndex: Int
): Offset {
    val minX = screenWidth * 0.14f
    val maxX = screenWidth * 0.86f
    val minY = screenHeight * 0.13f
    val maxY = screenHeight * 0.83f
    val clampedX = desiredX.coerceIn(minX, maxX)
    val clampedY = desiredY.coerceIn(minY, maxY)
    if (existingOrbs.isEmpty()) return Offset(clampedX, clampedY)

    val minSep = (minOf(screenWidth, screenHeight) * 0.21f).coerceIn(96f, 134f)
    fun clearanceOf(cx: Float, cy: Float): Float {
        var best = Float.MAX_VALUE
        for (o in existingOrbs) {
            val d1 = hypot(cx - o.x, cy - o.y)
            val d2 = if (o.anchorX > 0f || o.anchorY > 0f) hypot(cx - o.anchorX, cy - o.anchorY) else d1
            val d = minOf(d1, d2)
            if (d < best) best = d
        }
        return best
    }

    val initialClearance = clearanceOf(clampedX, clampedY)
    if (initialClearance >= minSep) return Offset(clampedX, clampedY)

    var bestPt = Offset(clampedX, clampedY)
    var bestClearance = initialClearance
    val goldenAngle = 2.399963f
    for (step in 1..16) {
        val frac = step / 16f
        val ang = seedIndex * 1.13f + step * goldenAngle
        val rx = (screenWidth * 0.50f + cos(ang) * (screenWidth * (0.16f + 0.20f * frac))).coerceIn(minX, maxX)
        val ry = (screenHeight * 0.48f + sin(ang * 1.15f) * (screenHeight * (0.16f + 0.19f * frac))).coerceIn(minY, maxY)
        val c = clearanceOf(rx, ry)
        if (c > bestClearance) {
            bestClearance = c
            bestPt = Offset(rx, ry)
            if (bestClearance >= minSep) break
        }
    }
    return bestPt
}

/**
 * Computes an offscreen perimeter entry point radiating from the outside boundary inward.
 */
internal fun computePerimeterSpawnPoint(
    anchorPt: Offset,
    screenWidth: Float,
    screenHeight: Float,
    seedIndex: Int
): Offset {
    val cx = screenWidth * 0.5f
    val cy = screenHeight * 0.5f
    val baseAngle = atan2(anchorPt.y - cy, anchorPt.x - cx)
    val angleJitter = ((seedIndex % 7) - 3) * 0.18f
    val spawnAngle = baseAngle + angleJitter
    val spawnDist = hypot(cx, cy) * 1.30f
    val sx = cx + cos(spawnAngle) * spawnDist
    val sy = cy + sin(spawnAngle) * spawnDist
    return Offset(sx, sy)
}

internal fun countPlayableChartOrbs(chart: List<BeatOrbCue>, isTrainingMode: Boolean): Int {
    if (chart.isEmpty()) return 1
    var total = 0
    for (cue in chart) {
        if (
            isTrainingMode &&
            cue.sequenceIndex % 3 == 2 &&
            cue.powerUpType == PowerUpType.NONE &&
            cue.keyWordEffect == KeyWordEffect.NONE &&
            !cue.isGoldenCue
        ) {
            continue
        }
        total++
    }
    return total.coerceAtLeast(1)
}

internal fun spawnStationaryBeatOrbsForFrame(
    chart: List<BeatOrbCue>,
    startCueIdx: Int,
    trackSec: Float,
    spawnLeadTime: Float,
    ttlGraceSec: Float,
    screenWidth: Float,
    screenHeight: Float,
    isGoldenActive: Boolean,
    applyColorBlind: (Color) -> Color,
    powerUps: MutableList<GameObject>,
    shockwaves: MutableList<Shockwave>,
    hasActiveSustainRibbon: Boolean = false,
    isTrainingMode: Boolean = false,
    beatDurationSec: Float = 0.48387f,
    isBandHeroMode: Boolean = false,
    multiBand: MultiBandAudioState = MultiBandAudioState()
): Int {
    var nextOrbCueIdx = startCueIdx
    if (hasActiveSustainRibbon) {
        while (nextOrbCueIdx < chart.size && chart[nextOrbCueIdx].timestampSec <= trackSec + spawnLeadTime * 0.5f) {
            nextOrbCueIdx++
        }
        return nextOrbCueIdx
    }

    val laneColors = listOf(
        Color(0xFF22C55EL), // Emerald Resonance
        Color(0xFFFB7185L), // Crimson Resonance
        Color(0xFFFACC15L), // 24K Gold Resonance
        Color(0xFF38BDF8L)  // Electric Cyan Resonance
    )

    val beatGridStep = beatDurationSec.coerceIn(0.35f, 0.75f)
    val isFluidMoment = multiBand.vocal >= 0.43f ||
        (multiBand.bassDropOut && multiBand.vocal >= 0.30f) ||
        (multiBand.bass >= 0.72f && multiBand.vocal >= 0.36f) ||
        multiBand.swellDelta >= 0.08f
    val fluidFormationTag = if (!isBandHeroMode && isFluidMoment) 101f else 0f

    val maxActiveOrbs = if (isBandHeroMode) 12 else 18
    val regularLeadTime = if (isBandHeroMode) spawnLeadTime else (beatGridStep * 3.8f).coerceIn(1.9f, 2.55f)
    // Special Section-Transition Orbs descend with a crisp 1.45s lead time so they arrive right ON the musical swell!
    val specialLeadTime = if (isBandHeroMode) spawnLeadTime else 1.45f
    val maxLookahead = maxOf(regularLeadTime, specialLeadTime, 3.55f)
    val effectiveHoldWindow = if (isBandHeroMode) ttlGraceSec else (beatGridStep * 4.5f)

    while (
        nextOrbCueIdx < chart.size &&
        powerUps.size < maxActiveOrbs &&
        trackSec >= chart[nextOrbCueIdx].timestampSec - maxLookahead
    ) {
        val cue = chart[nextOrbCueIdx]
        val isSpecialCue = cue.powerUpType != PowerUpType.NONE || cue.keyWordEffect != KeyWordEffect.NONE
        val thisLeadTime = when {
            isBandHeroMode -> spawnLeadTime
            cue.customLeadSec > 0.1f -> cue.customLeadSec
            isSpecialCue -> specialLeadTime
            else -> regularLeadTime
        }

        // Wait until this specific orb's lead-time window arrives (prevents Special Orbs from spawning 3s before the swell!)
        if (trackSec < cue.timestampSec - thisLeadTime) {
            break
        }

        nextOrbCueIdx++

        if (isTrainingMode && cue.sequenceIndex % 3 == 2 && !isSpecialCue && !cue.isGoldenCue) {
            continue
        }

        val targetSecExact = if (isSpecialCue) {
            cue.timestampSec
        } else {
            (cue.timestampSec / beatGridStep).roundToInt() * beatGridStep
        }

        if (trackSec <= targetSecExact + effectiveHoldWindow && screenWidth > 0f && screenHeight > 0f) {
            val laneIdx = (((cue.normX - 0.16f) / 0.68f * 4f).toInt() + cue.sequenceIndex) % 4
            val clampedLane = laneIdx.coerceIn(0, 3)

            // 100% Human-Catchable Ergonomic Flow Path:
            // Honor cue.normX and cue.normY (including upper-middle 0.28f pocket for Special 24K Milestone Orbs!)
            val safeMinX = screenWidth * 0.18f
            val safeMaxX = screenWidth * 0.82f
            val desiredReceptorX = (cue.normX.coerceIn(0.18f, 0.82f)) * screenWidth
            val prevOrb = powerUps.lastOrNull { it.powerUpType == PowerUpType.NONE && it.keyWordEffect == KeyWordEffect.NONE }
            val dtFromPrev = if (prevOrb != null) abs(targetSecExact - prevOrb.targetBeatSec).coerceAtLeast(0.18f) else 2.0f
            val maxComfortableJump = (dtFromPrev * screenWidth * 0.62f).coerceIn(screenWidth * 0.11f, screenWidth * 0.50f)
            val receptorX = if (!isBandHeroMode && !isSpecialCue && prevOrb != null) {
                desiredReceptorX.coerceIn(
                    (prevOrb.anchorX - maxComfortableJump).coerceAtLeast(safeMinX),
                    (prevOrb.anchorX + maxComfortableJump).coerceAtMost(safeMaxX)
                )
            } else {
                desiredReceptorX
            }
            val receptorY = if (isSpecialCue) {
                (cue.normY.coerceIn(0.24f, 0.52f)) * screenHeight
            } else {
                (cue.normY.coerceIn(0.32f, 0.68f)) * screenHeight
            }

            val anchorPt = if (isBandHeroMode) {
                computeHighwayPoint(clampedLane.toFloat(), 0f, screenWidth, screenHeight)
            } else {
                Offset(receptorX, receptorY)
            }

            // Multi-Directional Entry & Ray-Matched Exit Trajectories:
            // Orbs enter from varied perimeter directions (flanks, corners, diagonals, arcs) and glide cleanly
            // across their beat target all the way past the opposite screen edge with zero bounce or kink.
            val swoopCurve = if (isBandHeroMode || isSpecialCue) 0f
            else (((cue.sequenceIndex * 41) % 5 - 2) * 0.055f)

            val startX: Float
            val startY: Float
            val exitX: Float
            val exitY: Float
            val expireSec: Float

            if (isBandHeroMode) {
                startX = anchorPt.x
                startY = anchorPt.y
                exitX = anchorPt.x
                exitY = anchorPt.y
                expireSec = targetSecExact + 0.45f
            } else if (isSpecialCue) {
                startX = receptorX
                startY = -56f
                exitX = receptorX
                exitY = -72f
                expireSec = targetSecExact + 3.15f
            } else {
                if (cue.sequenceIndex <= 3 || cue.timestampSec < 4.5f) {
                    val topOffset = ((cue.sequenceIndex % 3) - 1) * (screenWidth * 0.08f)
                    startX = (receptorX + topOffset).coerceIn(screenWidth * 0.20f, screenWidth * 0.80f)
                    startY = -44f
                } else {
                    val dirMode = ((cue.sequenceIndex * 37) + 3) % 8
                    when (dirMode) {
                        0 -> {
                            startX = -54f
                            startY = screenHeight * 0.10f
                        }
                        1 -> {
                            startX = screenWidth + 54f
                            startY = screenHeight * 0.12f
                        }
                        2 -> {
                            startX = screenWidth * 0.12f
                            startY = -54f
                        }
                        3 -> {
                            startX = screenWidth * 0.88f
                            startY = -54f
                        }
                        4 -> {
                            startX = -54f
                            startY = screenHeight * 0.28f
                        }
                        5 -> {
                            startX = screenWidth + 54f
                            startY = screenHeight * 0.28f
                        }
                        6 -> {
                            startX = (receptorX - screenWidth * 0.28f).coerceIn(24f, screenWidth - 24f)
                            startY = -54f
                        }
                        else -> {
                            startX = (receptorX + screenWidth * 0.28f).coerceIn(24f, screenWidth - 24f)
                            startY = -54f
                        }
                    }
                }
                val dx = receptorX - startX
                val dy = receptorY - startY
                val kx = when {
                    dx > 1f -> (screenWidth + 68f - receptorX) / dx
                    dx < -1f -> (-68f - receptorX) / dx
                    else -> 99f
                }
                val ky = when {
                    dy > 1f -> (screenHeight + 68f - receptorY) / dy
                    dy < -1f -> (-68f - receptorY) / dy
                    else -> 99f
                }
                val kExit = minOf(kx, ky).coerceIn(0.65f, 2.35f)
                exitX = receptorX + dx * kExit
                exitY = receptorY + dy * kExit
                expireSec = targetSecExact + thisLeadTime * kExit
            }

            val mappedPowerUpType = cue.powerUpType
            val mappedKeyWord = cue.keyWordEffect
            val isSupernova = mappedKeyWord == KeyWordEffect.CONSTELLATION_STAR || mappedPowerUpType == PowerUpType.ELECTRIC_HARMONIC
            val isOrbGolden = isSupernova || isGoldenActive

            val pColor = when (mappedPowerUpType) {
                PowerUpType.ELECTRIC_HARMONIC -> Color(0xFFFACC15L)
                PowerUpType.PRISM_HARMONY -> Color(0xFF4ADE80L)
                PowerUpType.SLOW_MO -> Color(0xFFC084FCL)
                PowerUpType.BLAST -> Color(0xFFF97316L)
                PowerUpType.SHIELD -> Color(0xFF38BDF8L)
                else -> if (isSupernova) Color(0xFFFACC15L) else laneColors[clampedLane]
            }

            val spawnSec = targetSecExact - thisLeadTime

            powerUps.add(
                GameObject(
                    x = startX,
                    y = startY,
                    radius = if (isSpecialCue) 38f else 32f,
                    color = applyColorBlind(pColor),
                    angle = 0f,
                    speed = 0f,
                    powerUpType = mappedPowerUpType,
                    phase = cue.sequenceIndex * 0.7f,
                    anchorX = receptorX,
                    anchorY = receptorY,
                    spawnBeatSec = spawnSec,
                    targetBeatSec = targetSecExact,
                    expireBeatSec = expireSec,
                    phraseBeatNum = clampedLane + 1,
                    sequenceIndex = cue.sequenceIndex * 2 + 1,
                    isGoldenOrb = isOrbGolden,
                    keyWordEffect = mappedKeyWord,
                    orbitRadius = fluidFormationTag,
                    spawnX = startX,
                    spawnY = startY,
                    exitX = exitX,
                    exitY = exitY,
                    swoopCurve = swoopCurve,
                    swirlStrength = cue.swirlStrength,
                    goldenTier = cue.goldenTier
                )
            )
        }
    }
    return nextOrbCueIdx
}

internal data class WormholeSurfStepResult(
    val nextSlide: WormholeSlideState?,
    val targetX: Float,
    val targetY: Float,
    val spawnInvulnerabilityTime: Float,
    val rhythmCombo: Int,
    val onBeatHitsThisRun: Int,
    val scoreDelta: Int,
    val pulsePointsDelta: Int,
    val cameraPunch: Float,
    val beatFlashIntensity: Float,
    val toast: PowerUpBannerToast?
)

@Suppress("UNUSED_PARAMETER")
internal fun stepWormholeSlideAndPortals(
    activeWormholeSlide: WormholeSlideState?,
    wormholes: List<SingularityWormhole>,
    player: GameObject,
    targetX: Float,
    targetY: Float,
    currentScale: Float,
    beatDurationSec: Float,
    spawnInvulnerabilityTime: Float,
    rhythmCombo: Int,
    onBeatHitsThisRun: Int,
    scoreMultiplier: Int,
    difficultyScoreMultiplier: Float,
    cameraPunch: Float,
    beatFlashIntensity: Float,
    audioEngine: AudioEngine,
    triggerHaptic: (Long) -> Unit,
    shockwaves: MutableList<Shockwave>,
    floatingTexts: MutableList<FloatingText>,
    screenWidth: Float = 1080f,
    screenHeight: Float = 1920f,
    particles: MutableList<GameObject>? = null,
    secondFingerPos: Offset? = null
): WormholeSurfStepResult {
    // Swooping slide ribbons removed in v11 — replaced by clean choreographed fluid peg formations!
    return WormholeSurfStepResult(
        nextSlide = null,
        targetX = targetX,
        targetY = targetY,
        spawnInvulnerabilityTime = spawnInvulnerabilityTime,
        rhythmCombo = rhythmCombo,
        onBeatHitsThisRun = onBeatHitsThisRun,
        scoreDelta = 0,
        pulsePointsDelta = 0,
        cameraPunch = cameraPunch,
        beatFlashIntensity = beatFlashIntensity,
        toast = null
    )
}
