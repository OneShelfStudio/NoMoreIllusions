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
    shockwaves: MutableList<Shockwave>
): Int {
    var nextOrbCueIdx = startCueIdx
    while (
        nextOrbCueIdx < chart.size &&
        powerUps.size < 10 &&
        (trackSec >= chart[nextOrbCueIdx].timestampSec - spawnLeadTime || (powerUps.isEmpty() && trackSec + spawnLeadTime * 1.5f >= chart[nextOrbCueIdx].timestampSec))
    ) {
        val cue = chart[nextOrbCueIdx]
        nextOrbCueIdx++
        if (trackSec <= cue.timestampSec + ttlGraceSec && screenWidth > 0f && screenHeight > 0f) {
            // Quantize into 4 Guitar-Hero / Band-Hero perspective lanes (0, 1, 2, 3)
            val laneIdx = (((cue.normX - 0.16f) / 0.68f * 4f).toInt() + cue.sequenceIndex) % 4
            val clampedLane = laneIdx.coerceIn(0, 3)
            val startPt = computeHighwayPoint(clampedLane.toFloat(), 0f, screenWidth, screenHeight)

            // Simplify pickups strictly to:
            // 1. Standard Beat Notes (PowerUpType.SCORE_ORB, KeyWordEffect.NONE)
            // 2. 24K Star Notes (KeyWordEffect.CONSTELLATION_STAR)
            // 3. 💥 BOMB (PowerUpType.BLAST) - vaporizes all enemies on screen
            // 4. 🛡️ SHIELD (PowerUpType.SHIELD) - protective barrier dome
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

            val laneColors = listOf(
                Color(0xFF22C55EL), // Lane 1: Emerald Fret
                Color(0xFFFB7185L), // Lane 2: Crimson Fret
                Color(0xFFFACC15L), // Lane 3: Gold Fret
                Color(0xFF38BDF8L)  // Lane 4: Cyan Fret
            )
            val pColor = when {
                mappedPowerUpType == PowerUpType.BLAST -> Color(0xFFF97316L)
                mappedPowerUpType == PowerUpType.SHIELD -> Color(0xFF38BDF8L)
                isSupernova -> Color(0xFFFACC15L)
                else -> laneColors[clampedLane]
            }

            val spawnSec = minOf(trackSec, cue.timestampSec - spawnLeadTime * 0.85f)
            powerUps.add(
                GameObject(
                    x = startPt.x,
                    y = startPt.y,
                    radius = if (isSupernova || mappedPowerUpType != PowerUpType.NONE) 31f else 24f,
                    color = applyColorBlind(pColor),
                    angle = 0f,
                    speed = 0f,
                    powerUpType = mappedPowerUpType,
                    phase = cue.sequenceIndex * 0.7f,
                    anchorX = startPt.x,
                    anchorY = startPt.y,
                    spawnBeatSec = spawnSec,
                    targetBeatSec = cue.timestampSec,
                    expireBeatSec = cue.timestampSec + ttlGraceSec,
                    phraseBeatNum = clampedLane + 1,
                    sequenceIndex = cue.sequenceIndex,
                    isGoldenOrb = isOrbGolden,
                    keyWordEffect = mappedKeyWord
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
    screenHeight: Float = 1920f
): WormholeSurfStepResult {
    var nextInvuln = spawnInvulnerabilityTime
    var nextCombo = rhythmCombo
    var nextHits = onBeatHitsThisRun
    var scoreDelta = 0
    var ptsDelta = 0
    var nextPunch = cameraPunch
    var nextFlash = beatFlashIntensity
    val toast: PowerUpBannerToast? = null

    // Guitar-Hero / Beatstar Sustain Slide Ribbon Tracking:
    // Player steers freely! Holding the ship inside the scrolling sustain ribbon awards rapid SLIDE ♪ sparks.
    for (w in wormholes) {
        val uHead = (w.ageSec / 2.2f)
        val uTail = (uHead - 0.44f).coerceAtLeast(0f)
        if (uHead < 0.25f || uTail > 1.18f) continue

        val startLane = w.x.coerceIn(0f, 3f)
        val endLane = w.exitX.coerceIn(0f, 3f)
        var minRibbonDist = 9999f
        var hitPt = Offset(player.x, player.y)

        for (step in 0..8) {
            val s = step / 8f
            val uSample = uTail + (uHead - uTail) * s
            if (uSample < 0f || uSample > 1.22f) continue
            val laneAtS = startLane + (endLane - startLane) * (0.5f - 0.5f * cos(s * PI.toFloat()))
            val pt = computeHighwayPoint(laneAtS, uSample, screenWidth, screenHeight)
            val d = hypot(player.x - pt.x, player.y - pt.y)
            if (d < minRibbonDist) {
                minRibbonDist = d
                hitPt = pt
            }
        }

        if (minRibbonDist <= 66f) {
            nextInvuln = maxOf(nextInvuln, 0.25f)
            if (w.slideCooldownSec <= 0f) {
                w.slideCooldownSec = 0.18f
                nextCombo++
                nextHits++
                val tickPts = (60 * maxOf(scoreMultiplier, 2) * difficultyScoreMultiplier).toInt()
                scoreDelta += tickPts
                ptsDelta += 2
                nextPunch = maxOf(nextPunch, 0.018f)
                nextFlash = maxOf(nextFlash, 0.35f)
                audioEngine.playOrbBeatHit(combo = nextCombo, isKeyWordPeg = true)
                triggerHaptic(16L)
                shockwaves.add(Shockwave(hitPt.x, hitPt.y, 0f, 110f, Color(0xFFFACC15L), 0.55f))
                floatingTexts.add(
                    FloatingText(
                        x = hitPt.x,
                        y = hitPt.y - 22f,
                        text = "SLIDE ♪ +$tickPts",
                        life = 0.65f,
                        color = Color(0xFFFACC15L),
                        isLarge = false
                    )
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
