package com.oneshelfstudio.nomoreillusions

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
            shockwaves.add(Shockwave(p.x, p.y, 0f, 520f, Color(0xFFF97316L), 1.20f))
            var vaporizedCount = 0
            val clearIter = enemies.iterator()
            while (clearIter.hasNext()) {
                val en = clearIter.next()
                vaporizedCount++
                bonusScore += (100 * mult * difficultyScoreMultiplier).toInt()
                shockwaves.add(Shockwave(en.x, en.y, 0f, 130f, Color(0xFFFACC15L), 0.85f))
                repeat(8) { idx ->
                    val a = (idx / 8f) * 6.283185f
                    particles.add(
                        GameObject(
                            x = en.x,
                            y = en.y,
                            radius = 4.6f,
                            color = if (idx % 2 == 0) Color(0xFFFACC15L) else Color(0xFFF97316L),
                            vx = cos(a) * 8.2f,
                            vy = sin(a) * 8.2f,
                            life = 0.80f
                        )
                    )
                }
                clearIter.remove()
            }
            enemyTelegraphs.clear()
            toast = PowerUpBannerToast(
                icon = "💥",
                title = "NOVA BLAST!",
                subtitle = if (vaporizedCount > 0) "$vaporizedCount ENEMIES VAPORIZED + 5.5s OVERDRIVE!" else "5.5s OVERDRIVE • RAM ENEMIES TO VAPORIZE!",
                accentColor = Color(0xFFF97316L),
                timer = 2.6f,
                maxTimer = 2.6f
            )
        }
        PowerUpType.SHIELD -> {
            nextShield = maxOf(nextShield, 7.0f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 220f, Color(0xFF38BDF8L), 0.90f))
            toast = PowerUpBannerToast(
                icon = "🛡️",
                title = "AEGIS SHIELD!",
                subtitle = "ABSORBS NEXT COLLISION • 7.0s ACTIVE",
                accentColor = Color(0xFF38BDF8L),
                timer = 2.4f,
                maxTimer = 2.4f
            )
        }
        PowerUpType.SLOW_MO -> {
            nextSlowMo = maxOf(nextSlowMo, 5.5f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 240f, Color(0xFFA855F7L), 0.95f))
            toast = PowerUpBannerToast(
                icon = "⏳",
                title = "CHRONO SHIFT!",
                subtitle = "ENEMIES SLOWED 50% • 5.5s ACTIVE",
                accentColor = Color(0xFFA855F7L),
                timer = 2.4f,
                maxTimer = 2.4f
            )
        }
        PowerUpType.MAGNET -> {
            nextMagnet = maxOf(nextMagnet, 7.0f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 220f, Color(0xFF10B981L), 0.90f))
            toast = PowerUpBannerToast(
                icon = "🧲",
                title = "ORB MAGNET!",
                subtitle = "PULLING NEARBY ORBS • 7.0s ACTIVE",
                accentColor = Color(0xFF10B981L),
                timer = 2.4f,
                maxTimer = 2.4f
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
        KeyWordEffect.TRACTOR_REACH -> {
            nextMagnet = maxOf(nextMagnet, 6.0f)
            toast = PowerUpBannerToast("🧲", "TRACTOR REACH!", "PULLING ORBS + $keywordBonus PTS", Color(0xFF22D3EEL), 2.5f, 2.5f)
            Color(0xFF22D3EEL)
        }
        KeyWordEffect.PRISM_SHATTER -> {
            nextShake = 9f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 400f, Color(0xFFE879F9L), 1.15f))
            toast = PowerUpBannerToast("💎", "PRISM SHATTER!", "DEFLECTING ENEMIES + $keywordBonus PTS", Color(0xFFE879F9L), 2.5f, 2.5f)
            Color(0xFFE879F9L)
        }
        KeyWordEffect.MAGNETAR_WAR -> {
            nextShake = 12f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 540f, Color(0xFFFB7185L), 1.25f))
            val eIter = enemies.iterator()
            while (eIter.hasNext()) {
                val e = eIter.next()
                if (hypot(e.x - player.x, e.y - player.y) < 420f) {
                    shockwaves.add(Shockwave(e.x, e.y, 0f, 125f, Color(0xFFFDE047L), 0.85f))
                    eIter.remove()
                }
            }
            toast = PowerUpBannerToast("💥", "MAGNETAR SHOCKWAVE!", "ENEMIES VAPORIZED + $keywordBonus PTS", Color(0xFFFB7185L), 2.6f, 2.6f)
            Color(0xFFFB7185L)
        }
        KeyWordEffect.CONSTELLATION_STAR -> {
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
            toast = PowerUpBannerToast("★", "24K OVERDRIVE STAR!", "8s ENEMY VAPORIZE + 4x SCORE", Color(0xFFFACC15L), 2.8f, 2.8f)
            Color(0xFFFACC15L)
        }
        KeyWordEffect.BINARY_HEART -> {
            nextMult = maxOf(nextMult, 2)
            nextMultTime = 12f
            shockwaves.add(Shockwave(p.x, p.y, 0f, 360f, Color(0xFF4ADE80L), 1.10f))
            toast = PowerUpBannerToast("💚", "HARMONIC PULSE!", "ENEMY REPULSION + 2x SCORE", Color(0xFF4ADE80L), 2.5f, 2.5f)
            Color(0xFF4ADE80L)
        }
        KeyWordEffect.ECLIPSE_DARK -> {
            nextShield = maxOf(nextShield, 8.5f)
            shockwaves.add(Shockwave(p.x, p.y, 0f, 440f, Color(0xFFFDE047L), 1.20f))
            toast = PowerUpBannerToast("🛡️", "SOLAR ECLIPSE SHIELD!", "8.5s INVINCIBILITY BARRIER", Color(0xFFFDE047L), 2.5f, 2.5f)
            Color(0xFFFDE047L)
        }
        KeyWordEffect.NONE -> {
            toast = PowerUpBannerToast("♪", "RESONANCE!", "+$keywordBonus PTS", Color(0xFF38BDF8L), 2.0f, 2.0f)
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
        powerUps.size < 4 &&
        (trackSec >= chart[nextOrbCueIdx].timestampSec - spawnLeadTime || (powerUps.isEmpty() && trackSec + spawnLeadTime * 1.5f >= chart[nextOrbCueIdx].timestampSec))
    ) {
        val cue = chart[nextOrbCueIdx]
        nextOrbCueIdx++
        if (trackSec <= cue.timestampSec + ttlGraceSec && screenWidth > 0f && screenHeight > 0f) {
            val destX = (cue.normX * screenWidth).coerceIn(72f, screenWidth - 72f)
            val destY = (cue.normY * screenHeight).coerceIn(168f, screenHeight - 115f)
            val isSupernova = cue.keyWordEffect != KeyWordEffect.NONE
            val isOrbGolden = cue.isGoldenCue || cue.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR
            val pColor = when (cue.keyWordEffect) {
                KeyWordEffect.TRACTOR_REACH -> Color(0xFF22D3EEL)
                KeyWordEffect.PRISM_SHATTER -> Color(0xFFE879F9L)
                KeyWordEffect.MAGNETAR_WAR -> Color(0xFFFB7185L)
                KeyWordEffect.CONSTELLATION_STAR -> Color(0xFFFACC15L)
                KeyWordEffect.BINARY_HEART -> Color(0xFF4ADE80L)
                KeyWordEffect.ECLIPSE_DARK -> Color(0xFFFDE047L)
                KeyWordEffect.NONE -> when (cue.powerUpType) {
                    PowerUpType.BLAST -> Color(0xFFF97316L)
                    PowerUpType.SHIELD -> Color(0xFF38BDF8L)
                    PowerUpType.SLOW_MO -> Color(0xFFA855F7L)
                    PowerUpType.MAGNET -> Color(0xFF10B981L)
                    else -> if (isGoldenActive) Color(0xFFFDE047L) else Color(0xFF06B6D4L)
                }
            }
            shockwaves.add(
                Shockwave(
                    x = destX,
                    y = destY,
                    radius = 0f,
                    maxRadius = if (isSupernova) 115f else 62f,
                    color = pColor,
                    life = 0.55f
                )
            )
            powerUps.add(
                GameObject(
                    x = destX,
                    y = destY,
                    radius = if (isSupernova) 36f else 23f,
                    color = applyColorBlind(pColor),
                    angle = 0f,
                    speed = 0f,
                    powerUpType = cue.powerUpType,
                    phase = cue.sequenceIndex * 0.7f,
                    anchorX = destX,
                    anchorY = destY,
                    spawnBeatSec = trackSec,
                    targetBeatSec = cue.timestampSec,
                    expireBeatSec = cue.timestampSec + ttlGraceSec,
                    phraseBeatNum = ((cue.sequenceIndex - 1) % 4) + 1,
                    sequenceIndex = cue.sequenceIndex,
                    isGoldenOrb = isOrbGolden,
                    keyWordEffect = cue.keyWordEffect
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
    floatingTexts: MutableList<FloatingText>
): WormholeSurfStepResult {
    var nextSlide = activeWormholeSlide
    var nextTargetX = targetX
    var nextTargetY = targetY
    var nextInvuln = spawnInvulnerabilityTime
    var nextCombo = rhythmCombo
    var nextHits = onBeatHitsThisRun
    var scoreDelta = 0
    var ptsDelta = 0
    var nextPunch = cameraPunch
    var nextFlash = beatFlashIntensity
    var toast: PowerUpBannerToast? = null

    if (nextSlide != null) {
        val nextProg = nextSlide.progress + (0.016f * currentScale) / nextSlide.durationSec.coerceAtLeast(0.45f)
        val clampedT = nextProg.coerceIn(0f, 1f)
        val inv = 1f - clampedT
        val sx = inv * inv * nextSlide.startX + 2f * inv * clampedT * nextSlide.ctrlX + clampedT * clampedT * nextSlide.endX
        val sy = inv * inv * nextSlide.startY + 2f * inv * clampedT * nextSlide.ctrlY + clampedT * clampedT * nextSlide.endY
        player.x = sx
        player.y = sy
        nextTargetX = sx
        nextTargetY = sy
        nextInvuln = maxOf(nextInvuln, 0.45f)

        val tickIdx = (clampedT * 5f).toInt()
        if (tickIdx > nextSlide.lastTickIndex && tickIdx <= 5) {
            nextSlide.lastTickIndex = tickIdx
            nextCombo++
            nextHits++
            val tickPts = (55 * maxOf(scoreMultiplier, 2) * difficultyScoreMultiplier).toInt()
            scoreDelta += tickPts
            ptsDelta += 2
            audioEngine.playOrbBeatHit(combo = nextCombo, isKeyWordPeg = tickIdx == 5)
            triggerHaptic(18L)
            shockwaves.add(Shockwave(sx, sy, 0f, 115f, Color(0xFF38BDF8L), 0.65f))
            floatingTexts.add(
                FloatingText(
                    x = sx,
                    y = sy - 18f,
                    text = "SLIDE ♪ +$tickPts",
                    life = 0.72f,
                    color = Color(0xFFFACC15L),
                    isLarge = false
                )
            )
        }
        if (nextProg >= 1f) {
            val dropPts = (180 * maxOf(scoreMultiplier, 2) * difficultyScoreMultiplier).toInt()
            scoreDelta += dropPts
            nextPunch = 0.036f
            nextFlash = 0.65f
            shockwaves.add(Shockwave(sx, sy, 0f, 260f, Color(0xFFFACC15L), 0.95f))
            floatingTexts.add(
                FloatingText(
                    x = sx,
                    y = sy - 24f,
                    text = "SLIDE DROP! +$dropPts",
                    life = 0.95f,
                    color = Color(0xFF4ADE80L),
                    isLarge = false
                )
            )
            nextSlide = null
        } else {
            nextSlide.progress = nextProg
        }
    } else {
        for (w in wormholes) {
            if (w.slideCooldownSec > 0f || w.alphaProgress < 0.35f) continue
            val bx = if (w.exitX > 0f) w.exitX else (w.x + 180f)
            val by = if (w.exitY > 0f) w.exitY else (w.y - 140f)
            val cx = if (w.ctrlX > 0f) w.ctrlX else ((w.x + bx) * 0.5f + 55f)
            val cy = if (w.ctrlY > 0f) w.ctrlY else ((w.y + by) * 0.5f - 45f)
            val distA = hypot(player.x - w.x, player.y - w.y)
            val distB = hypot(player.x - bx, player.y - by)
            if (distA < w.radius + player.radius * 0.85f) {
                w.slideCooldownSec = 2.4f
                nextInvuln = maxOf(nextInvuln, 1.35f)
                nextSlide = WormholeSlideState(
                    startX = w.x,
                    startY = w.y,
                    ctrlX = cx,
                    ctrlY = cy,
                    endX = bx,
                    endY = by,
                    durationSec = (beatDurationSec * 1.85f).coerceIn(0.75f, 1.15f)
                )
                toast = PowerUpBannerToast(
                    icon = "🌀",
                    title = "WORMHOLE SLIDE STREAM!",
                    subtitle = "SURFING THE BEAT RIBBON • INVINCIBLE!",
                    accentColor = Color(0xFF38BDF8L),
                    timer = 1.8f,
                    maxTimer = 1.8f
                )
                break
            } else if (distB < w.radius + player.radius * 0.85f) {
                w.slideCooldownSec = 2.4f
                nextInvuln = maxOf(nextInvuln, 1.35f)
                nextSlide = WormholeSlideState(
                    startX = bx,
                    startY = by,
                    ctrlX = cx,
                    ctrlY = cy,
                    endX = w.x,
                    endY = w.y,
                    durationSec = (beatDurationSec * 1.85f).coerceIn(0.75f, 1.15f)
                )
                toast = PowerUpBannerToast(
                    icon = "🌀",
                    title = "WORMHOLE SLIDE STREAM!",
                    subtitle = "SURFING THE BEAT RIBBON • INVINCIBLE!",
                    accentColor = Color(0xFFE879F9L),
                    timer = 1.8f,
                    maxTimer = 1.8f
                )
                break
            }
        }
    }

    return WormholeSurfStepResult(
        nextSlide = nextSlide,
        targetX = nextTargetX,
        targetY = nextTargetY,
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
