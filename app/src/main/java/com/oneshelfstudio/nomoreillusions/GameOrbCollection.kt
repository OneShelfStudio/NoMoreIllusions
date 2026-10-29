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

    for (i in 0 until 16) {
        val ang = (i / 16f) * 6.283185f
        val spd = if (i % 2 == 0) 10.5f else 6.5f
        particles.add(
            GameObject(
                x = p.x,
                y = p.y,
                radius = if (i % 2 == 0) 5.8f else 4.0f,
                color = if (i % 2 == 0) Color.White else burstColor,
                vx = cos(ang) * spd,
                vy = sin(ang) * spd,
                life = 0.90f
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
    // While a 2-Finger Vocal Sustain Hold is active, preserve mid-flight pegs and pause new spawns at the horizon
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

    val beatGridStep = beatDurationSec.coerceIn(0.35f, 0.75f) // Exact quarter-note downbeat grid
    // Clean, readable stage count (max 7 standalone orbs in Original mode with a generous 8-beat window — zero clutter!)
    val maxActiveOrbs = if (isBandHeroMode) 10 else 7
    val effectiveLeadTime = if (isBandHeroMode) spawnLeadTime else maxOf(spawnLeadTime, beatGridStep * 2.5f)
    val effectiveHoldWindow = if (isBandHeroMode) ttlGraceSec else (beatGridStep * 6.0f) // 2 beats lead + 6 beats hold = 8 full beats on screen!
    val isVocalFlow = multiBand.vocal >= 0.44f || (multiBand.bassDropOut && multiBand.vocal >= 0.32f)

    while (
        nextOrbCueIdx < chart.size &&
        powerUps.size < maxActiveOrbs &&
        (trackSec >= chart[nextOrbCueIdx].timestampSec - effectiveLeadTime || (powerUps.isEmpty() && trackSec + effectiveLeadTime * 1.45f >= chart[nextOrbCueIdx].timestampSec))
    ) {
        val prevCueSec = if (nextOrbCueIdx > 0) chart[nextOrbCueIdx - 1].timestampSec else 0f
        val cue = chart[nextOrbCueIdx]
        nextOrbCueIdx++

        // In Training/Easy/Chill mode, skip every 3rd dense subdivision peg so beats feel relaxed and easy to learn
        if (isTrainingMode && cue.sequenceIndex % 3 == 2 && cue.powerUpType == PowerUpType.NONE && !cue.isGoldenCue) {
            continue
        }

        val quantizedBeatSec = (cue.timestampSec / beatGridStep).roundToInt() * beatGridStep
        if (trackSec <= quantizedBeatSec + effectiveHoldWindow && screenWidth > 0f && screenHeight > 0f) {
            val laneIdx = (((cue.normX - 0.16f) / 0.68f * 4f).toInt() + cue.sequenceIndex) % 4
            val clampedLane = laneIdx.coerceIn(0, 3)

            // Only inject intermediate highway notes in Band Hero mode (or when powerUps is initially empty) so Original mode stays clean!
            val gapSec = quantizedBeatSec - prevCueSec
            if ((isBandHeroMode || powerUps.isEmpty()) && !isTrainingMode && prevCueSec > 0.5f && gapSec in (beatGridStep * 1.85f)..3.20f && powerUps.size < maxActiveOrbs - 2) {
                val rawMidSec = prevCueSec + gapSec * 0.5f
                val midBeatSec = (rawMidSec / beatGridStep).roundToInt() * beatGridStep
                if (midBeatSec > trackSec + 0.25f && abs(midBeatSec - quantizedBeatSec) >= beatGridStep * 0.75f && abs(midBeatSec - prevCueSec) >= beatGridStep * 0.75f) {
                    val midLane = (clampedLane + 2) % 4
                    val midStartPt = if (isBandHeroMode) {
                        computeHighwayPoint(midLane.toFloat(), 0f, screenWidth, screenHeight)
                    } else {
                        val ox = (1f - cue.normX).coerceIn(0.16f, 0.84f) * screenWidth
                        val oy = cue.normY.coerceIn(0.18f, 0.74f) * screenHeight
                        Offset(ox, oy)
                    }
                    val midSpawnSec = midBeatSec - effectiveLeadTime
                    powerUps.add(
                        GameObject(
                            x = midStartPt.x,
                            y = midStartPt.y,
                            radius = 32f,
                            color = applyColorBlind(laneColors[midLane]),
                            angle = 0f,
                            speed = 0f,
                            powerUpType = PowerUpType.NONE,
                            phase = (cue.sequenceIndex + 0.5f) * 0.7f,
                            anchorX = midStartPt.x,
                            anchorY = midStartPt.y,
                            spawnBeatSec = midSpawnSec,
                            targetBeatSec = midBeatSec,
                            expireBeatSec = midBeatSec + effectiveHoldWindow,
                            phraseBeatNum = midLane + 1,
                            sequenceIndex = cue.sequenceIndex * 2,
                            isGoldenOrb = isGoldenActive,
                            keyWordEffect = KeyWordEffect.NONE,
                            orbitRadius = if (!isBandHeroMode && isVocalFlow) 77f else 0f
                        )
                    )
                }
            }

            val startPt = if (isBandHeroMode) {
                computeHighwayPoint(clampedLane.toFloat(), 0f, screenWidth, screenHeight)
            } else {
                val ox = cue.normX.coerceIn(0.15f, 0.85f) * screenWidth
                val oy = cue.normY.coerceIn(0.18f, 0.75f) * screenHeight
                Offset(ox, oy)
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
                    x = startPt.x,
                    y = startPt.y,
                    radius = if (isSupernova || mappedPowerUpType != PowerUpType.NONE) 38f else 32f,
                    color = applyColorBlind(pColor),
                    angle = 0f,
                    speed = 0f,
                    powerUpType = mappedPowerUpType,
                    phase = cue.sequenceIndex * 0.7f,
                    anchorX = startPt.x,
                    anchorY = startPt.y,
                    spawnBeatSec = spawnSec,
                    targetBeatSec = quantizedBeatSec,
                    expireBeatSec = quantizedBeatSec + effectiveHoldWindow,
                    phraseBeatNum = clampedLane + 1,
                    sequenceIndex = cue.sequenceIndex * 2 + 1,
                    isGoldenOrb = isOrbGolden,
                    keyWordEffect = mappedKeyWord,
                    orbitRadius = if (!isBandHeroMode && isVocalFlow) 77f else 0f
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
    var nextInvuln = spawnInvulnerabilityTime
    var nextCombo = rhythmCombo
    var nextHits = onBeatHitsThisRun
    var scoreDelta = 0
    var ptsDelta = 0
    var nextPunch = cameraPunch
    var nextFlash = beatFlashIntensity
    var toast: PowerUpBannerToast? = null

    // V10.1 ONE SINGLE BAND-HERO 5-STAR VOCAL POWER LONG NOTE ACTIVATION (ON "OOOOOH / AAAAH" VOCAL SWELLS):
    // - Zero rapid repeated buzzes!
    // - Touching the Star-Power Note (or using 2 fingers) triggers ONE single clean activation chime & haptic,
    //   latching the sweeping Golden-Cyan Star-Power Long Note Wave (`w.slideCooldownSec = 1.0f`) so it smoothly
    //   glides from 0% -> 100% while unleashing 4X Star Power Overdrive + thick Neon Contrail!
    for (w in wormholes) {
        if (w.angle >= 90f) continue
        val uAnchor = (w.ageSec / 1.5f).coerceIn(0f, 1.0f)
        if (uAnchor < 0.25f) continue

        val startLane = w.x.coerceIn(0f, 3f)
        val endLane = w.exitX.coerceIn(0f, 3f)
        val hwPt1 = computeHighwayPoint(startLane, uAnchor, screenWidth, screenHeight)
        val hwPt2 = computeHighwayPoint(endLane, uAnchor, screenWidth, screenHeight)
        val stagePt1 = Offset(screenWidth * 0.20f, screenHeight * 0.50f)
        val stagePt2 = Offset(screenWidth * 0.80f, screenHeight * 0.42f)
        val stageMid = Offset(screenWidth * 0.50f, screenHeight * 0.44f)
        val midPt = Offset((stagePt1.x + stagePt2.x) * 0.5f, (stagePt1.y + stagePt2.y) * 0.5f)

        val dHighway = minOf(hypot(player.x - hwPt1.x, player.y - hwPt1.y), hypot(player.x - hwPt2.x, player.y - hwPt2.y))
        val dStage = minOf(
            hypot(player.x - stagePt1.x, player.y - stagePt1.y),
            minOf(hypot(player.x - stageMid.x, player.y - stageMid.y), hypot(player.x - stagePt2.x, player.y - stagePt2.y))
        )
        val dPrimary = minOf(dHighway, dStage)
        val isTwoFingerActive = secondFingerPos != null
        val dSecondary = if (secondFingerPos != null) {
            minOf(
                hypot(secondFingerPos.x - hwPt1.x, secondFingerPos.y - hwPt1.y),
                hypot(secondFingerPos.x - stagePt1.x, secondFingerPos.y - stagePt1.y)
            )
        } else 9999f

        val isTouchingStarNote = isTwoFingerActive || dPrimary <= 125f || dSecondary <= 125f

        // 1. Single One-Shot Star Power Activation on Touch (Zero repeated buzzing!)
        if (w.slideCooldownSec < 0.5f && isTouchingStarNote) {
            w.slideCooldownSec = 1.0f // Latched Star-Power Active!
            nextInvuln = maxOf(nextInvuln, 0.65f)
            nextCombo++
            nextHits++
            val actPts = (250 * maxOf(scoreMultiplier, 2) * difficultyScoreMultiplier).toInt()
            scoreDelta += actPts
            ptsDelta += 6
            nextPunch = maxOf(nextPunch, 0.036f)
            nextFlash = maxOf(nextFlash, 0.72f)
            audioEngine.playOrbBeatHit(combo = nextCombo + 2, isKeyWordPeg = true)
            triggerHaptic(28L) // ONE single clean activation feel!
            shockwaves.add(Shockwave(player.x, player.y, 0f, 340f, Color(0xFFFACC15L), 0.95f))
            floatingTexts.add(
                FloatingText(
                    x = midPt.x,
                    y = midPt.y - 40f,
                    text = "★ 5-STAR VOCAL POWER ACTIVATED! +$actPts ★",
                    life = 1.15f,
                    color = Color(0xFFFACC15L),
                    isLarge = true
                )
            )
            toast = PowerUpBannerToast(
                icon = "★",
                title = "5-STAR VOCAL POWER!",
                subtitle = "STAR POWER OVERDRIVE + NEON CONTRAIL ACTIVE",
                accentColor = Color(0xFFFACC15L),
                timer = 2.4f,
                maxTimer = 2.4f
            )
        }

        // 2. Smooth Continuous Long-Note Wave Progression (Zero repeated buzzes!)
        if (w.slideCooldownSec >= 0.5f || isTouchingStarNote) {
            w.slideCooldownSec = 1.0f
            nextInvuln = maxOf(nextInvuln, 0.35f)
            val chargeRate = if (isTwoFingerActive) 0.55f else 0.45f
            w.ctrlY = (w.ctrlY + 0.016f * currentScale * chargeRate).coerceAtMost(1.0f)

            if (w.ctrlY >= 1.0f) {
                w.angle = 99f // Completed!
                w.ageSec = 99f
                nextCombo += 2
                nextHits += 2
                val bonusPts = (500 * maxOf(scoreMultiplier, 2) * difficultyScoreMultiplier).toInt()
                scoreDelta += bonusPts
                ptsDelta += 12
                nextPunch = maxOf(nextPunch, 0.048f)
                nextFlash = maxOf(nextFlash, 0.88f)
                audioEngine.playOrbBeatHit(combo = nextCombo + 3, isKeyWordPeg = true)
                triggerHaptic(40L) // ONE single finale celebration feel!
                shockwaves.add(Shockwave(stagePt1.x, stagePt1.y, 0f, 320f, Color(0xFF38BDF8L), 1.10f))
                shockwaves.add(Shockwave(stagePt2.x, stagePt2.y, 0f, 320f, Color(0xFFFACC15L), 1.10f))
                shockwaves.add(Shockwave(midPt.x, midPt.y, 0f, 460f, Color(0xFF4ADE80L), 1.25f))
                if (particles != null) {
                    spawnMelodicStarfallCascade(particles, screenWidth, screenHeight)
                }
                floatingTexts.add(
                    FloatingText(
                        x = midPt.x,
                        y = midPt.y - 42f,
                        text = "★ 5-STAR VOCAL POWER MASTERED! +$bonusPts ★",
                        life = 1.25f,
                        color = Color(0xFFFACC15L),
                        isLarge = true
                    )
                )
                toast = PowerUpBannerToast(
                    icon = "★",
                    title = "VOCAL STAR POWER COMPLETE!",
                    subtitle = "4X OVERDRIVE SURGE • +$bonusPts PTS",
                    accentColor = Color(0xFFFACC15L),
                    timer = 2.2f,
                    maxTimer = 2.2f
                )
            }
        }
    }

    return WormholeSurfStepResult(
        nextSlide = null,
        targetX = targetX,
        targetY = targetY,
        spawnInvulnerabilityTime = nextInvuln,
        rhythmCombo = nextCombo,
        onBeatHitsThisRun = nextHits,
        scoreDelta = scoreDelta,
        pulsePointsDelta = ptsDelta,
        cameraPunch = nextPunch,
        beatFlashIntensity = nextFlash,
        toast = toast
    )
}
