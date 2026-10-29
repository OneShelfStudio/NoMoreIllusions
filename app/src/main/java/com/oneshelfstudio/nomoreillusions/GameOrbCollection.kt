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
        PowerUpType.BLAST -> {
            nextStarPower = maxOf(nextStarPower, 5.5f)
            nextShake = 14f
            nextPunch = 0.045f
            nextFlash = 0.85f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 560f, Color(0xFFF97316L), 1.25f))
            var vaporizedCount = 0
            val clearIter = enemies.iterator()
            while (clearIter.hasNext()) {
                val en = clearIter.next()
                vaporizedCount++
                bonusScore += (150 * mult * difficultyScoreMultiplier).toInt()
                shockwaves.add(Shockwave(en.x, en.y, 0f, 145f, Color(0xFFFACC15L), 0.90f))
                repeat(10) { idx ->
                    val a = (idx / 10f) * 6.283185f
                    particles.add(
                        GameObject(
                            x = en.x,
                            y = en.y,
                            radius = 4.8f,
                            color = if (idx % 2 == 0) Color(0xFFFACC15L) else Color(0xFFF97316L),
                            vx = cos(a) * 8.6f,
                            vy = sin(a) * 8.6f,
                            life = 0.85f
                        )
                    )
                }
                clearIter.remove()
            }
            enemyTelegraphs.clear()
            toast = PowerUpBannerToast(
                icon = "💥",
                title = "BOMB DETONATED!",
                subtitle = if (vaporizedCount > 0) "$vaporizedCount HAZARDS VAPORIZED! (+${vaporizedCount * 150 * mult} PTS)" else "ALL HAZARDS CLEARED + 5.5s OVERDRIVE!",
                accentColor = Color(0xFFF97316L),
                timer = 2.4f,
                maxTimer = 2.4f
            )
        }
        PowerUpType.SHIELD -> {
            nextShield = maxOf(nextShield, 8.0f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 240f, Color(0xFF38BDF8L), 0.90f))
            toast = PowerUpBannerToast(
                icon = "🛡️",
                title = "SHIELD ACTIVE!",
                subtitle = "BLOCKS NEXT HAZARD COLLISION • 8.0s",
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
            toast = PowerUpBannerToast("★", "STAR POWER OVERDRIVE!", "8s 4x SCORE MULTIPLIER + RAM HAZARDS!", Color(0xFFFACC15L), 2.6f, 2.6f)
            Color(0xFFFACC15L)
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
    // Special musical moments ("ooooh" vocal swells, harmonic floats, or chorus swells) trigger fluid group peg motion!
    val isFluidMoment = multiBand.vocal >= 0.43f ||
        (multiBand.bassDropOut && multiBand.vocal >= 0.30f) ||
        (multiBand.bass >= 0.72f && multiBand.vocal >= 0.36f) ||
        multiBand.swellDelta >= 0.08f
    // Rotate cleanly among 3 fluid formations: 101f = Outward-Inward Sweep, 102f = Slow Galactic Spiral, 103f = Flowing Sine Wave
    val fluidFormationTag = if (!isBandHeroMode && isFluidMoment) {
        when (((trackSec / 16f).toInt()) % 3) {
            0 -> 101f
            1 -> 102f
            else -> 103f
        }
    } else 0f

    // Calm, uncluttered stage count: max 5 pegs in normal groove, 6 during fluid musical formations!
    val maxActiveOrbs = if (isBandHeroMode) 9 else if (isFluidMoment) 6 else 5
    val effectiveLeadTime = if (isBandHeroMode) spawnLeadTime else maxOf(spawnLeadTime, beatGridStep * 2.5f)
    val effectiveHoldWindow = if (isBandHeroMode) ttlGraceSec else (beatGridStep * 6.0f)

    while (
        nextOrbCueIdx < chart.size &&
        powerUps.size < maxActiveOrbs &&
        (trackSec >= chart[nextOrbCueIdx].timestampSec - effectiveLeadTime || (powerUps.isEmpty() && trackSec + effectiveLeadTime * 1.45f >= chart[nextOrbCueIdx].timestampSec))
    ) {
        val prevCueSec = if (nextOrbCueIdx > 0) chart[nextOrbCueIdx - 1].timestampSec else 0f
        val cue = chart[nextOrbCueIdx]
        nextOrbCueIdx++

        if (isTrainingMode && cue.sequenceIndex % 3 == 2 && cue.powerUpType == PowerUpType.NONE && !cue.isGoldenCue) {
            continue
        }

        val quantizedBeatSec = (cue.timestampSec / beatGridStep).roundToInt() * beatGridStep
        if (trackSec <= quantizedBeatSec + effectiveHoldWindow && screenWidth > 0f && screenHeight > 0f) {
            val laneIdx = (((cue.normX - 0.16f) / 0.68f * 4f).toInt() + cue.sequenceIndex) % 4
            val clampedLane = laneIdx.coerceIn(0, 3)

            // Only inject intermediate highway notes in Band Hero mode (or when powerUps is initially empty)
            val gapSec = quantizedBeatSec - prevCueSec
            if ((isBandHeroMode || powerUps.isEmpty()) && !isTrainingMode && prevCueSec > 0.5f && gapSec in (beatGridStep * 1.85f)..3.20f && powerUps.size < maxActiveOrbs - 2) {
                val rawMidSec = prevCueSec + gapSec * 0.5f
                val midBeatSec = (rawMidSec / beatGridStep).roundToInt() * beatGridStep
                if (midBeatSec > trackSec + 0.25f && abs(midBeatSec - quantizedBeatSec) >= beatGridStep * 0.75f && abs(midBeatSec - prevCueSec) >= beatGridStep * 0.75f) {
                    val midLane = (clampedLane + 2) % 4
                    val midAnchor = if (isBandHeroMode) {
                        computeHighwayPoint(midLane.toFloat(), 0f, screenWidth, screenHeight)
                    } else {
                        resolveNonOverlappingOrbSpot(
                            desiredX = (1f - cue.normX) * screenWidth,
                            desiredY = cue.normY * screenHeight,
                            screenWidth = screenWidth,
                            screenHeight = screenHeight,
                            existingOrbs = powerUps,
                            seedIndex = cue.sequenceIndex * 2
                        )
                    }
                    val midSpawnSec = midBeatSec - effectiveLeadTime
                    powerUps.add(
                        GameObject(
                            x = midAnchor.x,
                            y = midAnchor.y,
                            radius = 32f,
                            color = applyColorBlind(laneColors[midLane]),
                            angle = 0f,
                            speed = 0f,
                            powerUpType = PowerUpType.NONE,
                            phase = (cue.sequenceIndex + 0.5f) * 0.7f,
                            anchorX = midAnchor.x,
                            anchorY = midAnchor.y,
                            spawnBeatSec = midSpawnSec,
                            targetBeatSec = midBeatSec,
                            expireBeatSec = midBeatSec + effectiveHoldWindow,
                            phraseBeatNum = midLane + 1,
                            sequenceIndex = cue.sequenceIndex * 2,
                            isGoldenOrb = isGoldenActive,
                            keyWordEffect = KeyWordEffect.NONE,
                            orbitRadius = fluidFormationTag
                        )
                    )
                }
            }

            val anchorPt = if (isBandHeroMode) {
                computeHighwayPoint(clampedLane.toFloat(), 0f, screenWidth, screenHeight)
            } else {
                resolveNonOverlappingOrbSpot(
                    desiredX = cue.normX * screenWidth,
                    desiredY = cue.normY * screenHeight,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    existingOrbs = powerUps,
                    seedIndex = cue.sequenceIndex * 2 + 1
                )
            }
            // For Outward-Inward Sweeping Glide (101f), start near the outer stage edge so notes smoothly sweep inward onto the stage!
            val spawnEntryPt = if (!isBandHeroMode && fluidFormationTag == 101f) {
                val fromLeft = anchorPt.x < screenWidth * 0.5f
                Offset(
                    x = if (fromLeft) -28f else (screenWidth + 28f),
                    y = (anchorPt.y + (if (cue.sequenceIndex % 2 == 0) -65f else 65f)).coerceIn(screenHeight * 0.12f, screenHeight * 0.84f)
                )
            } else {
                anchorPt
            }
            val mappedPowerUpType = when (cue.powerUpType) {
                PowerUpType.BLAST, PowerUpType.MAGNET -> PowerUpType.BLAST
                PowerUpType.SHIELD, PowerUpType.SLOW_MO -> PowerUpType.SHIELD
                else -> when (cue.keyWordEffect) {
                    KeyWordEffect.MAGNETAR_WAR, KeyWordEffect.PRISM_SHATTER -> PowerUpType.BLAST
                    KeyWordEffect.ECLIPSE_DARK -> PowerUpType.SHIELD
                    else -> PowerUpType.NONE
                }
            }
            val mappedKeyWord = when {
                mappedPowerUpType == PowerUpType.BLAST || mappedPowerUpType == PowerUpType.SHIELD -> KeyWordEffect.NONE
                cue.keyWordEffect != KeyWordEffect.NONE || cue.isGoldenCue -> KeyWordEffect.CONSTELLATION_STAR
                else -> KeyWordEffect.NONE
            }

            val isSupernova = mappedKeyWord == KeyWordEffect.CONSTELLATION_STAR
            val isOrbGolden = isSupernova || isGoldenActive

            val pColor = when {
                mappedPowerUpType == PowerUpType.BLAST -> Color(0xFFF97316L)
                mappedPowerUpType == PowerUpType.SHIELD -> Color(0xFF38BDF8L)
                isSupernova -> Color(0xFFFACC15L)
                else -> laneColors[clampedLane]
            }

            val spawnSec = quantizedBeatSec - effectiveLeadTime
            powerUps.add(
                GameObject(
                    x = spawnEntryPt.x,
                    y = spawnEntryPt.y,
                    radius = if (isSupernova || mappedPowerUpType != PowerUpType.NONE) 38f else 32f,
                    color = applyColorBlind(pColor),
                    angle = 0f,
                    speed = 0f,
                    powerUpType = mappedPowerUpType,
                    phase = cue.sequenceIndex * 0.7f,
                    anchorX = anchorPt.x,
                    anchorY = anchorPt.y,
                    spawnBeatSec = spawnSec,
                    targetBeatSec = quantizedBeatSec,
                    expireBeatSec = quantizedBeatSec + effectiveHoldWindow,
                    phraseBeatNum = clampedLane + 1,
                    sequenceIndex = cue.sequenceIndex * 2 + 1,
                    isGoldenOrb = isOrbGolden,
                    keyWordEffect = mappedKeyWord,
                    orbitRadius = fluidFormationTag
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
