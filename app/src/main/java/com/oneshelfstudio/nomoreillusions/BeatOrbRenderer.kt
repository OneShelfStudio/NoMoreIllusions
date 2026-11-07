package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.*

private val sharedDeckPath = Path()
private val sharedPlasmaContourPath = Path()
private val sharedFilamentPath = Path()
private val sharedFacetPath = Path()
private val sharedPetalPath = Path()
private val sharedWakeRibbonPath = Path()
private val sharedWaveCorePath = Path()

@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawPowerUps(
    powerUps: List<GameObject>,
    player: GameObject,
    time: Float,
    trackSec: Float = 0f,
    beatPhase: Float = 0f,
    goldenOverdriveIntensity: Float = 0f,
    multiBand: MultiBandAudioState = MultiBandAudioState(),
    isBandHeroMode: Boolean = false,
    goldenTier: Int = 0
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return
    val isMusicStarted = trackSec >= 0f
    val beatKick = if (!isMusicStarted) 0f else if (beatPhase >= 0f) exp(-beatPhase * 5.2f).coerceIn(0f, 1f) else 0.25f
    val isGoldStage = goldenOverdriveIntensity > 0.15f

    val laneColors = listOf(
        Color(0xFF22C55EL), // Lane 1: Emerald Resonance
        Color(0xFFFB7185L), // Lane 2: Rose-Crimson Resonance
        Color(0xFFFACC15L), // Lane 3: Solar Gold Resonance
        Color(0xFF38BDF8L)  // Lane 4: Celestial Cyan Resonance
    )

    if (isBandHeroMode) {
        val topL = computeHighwayPoint(-0.45f, 0f, w, h)
        val topR = computeHighwayPoint(3.45f, 0f, w, h)
        val strikeL = computeHighwayPoint(-0.45f, 1.0f, w, h)
        val strikeR = computeHighwayPoint(3.45f, 1.0f, w, h)
        val botL = computeHighwayPoint(-0.45f, 1.18f, w, h)
        val botR = computeHighwayPoint(3.45f, 1.18f, w, h)

        val deckPath = sharedDeckPath.apply {
            reset()
            moveTo(topL.x, topL.y)
            lineTo(topR.x, topR.y)
            lineTo(botR.x, botR.y)
            lineTo(botL.x, botL.y)
            close()
        }
        drawPath(
            path = deckPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF070A16L).copy(alpha = 0.22f),
                    Color(0xFF0A1022L).copy(alpha = 0.62f),
                    Color(0xFF111832L).copy(alpha = 0.76f),
                    Color(0xFF070A16L).copy(alpha = 0.30f)
                ),
                startY = topL.y,
                endY = botL.y
            )
        )

        val railColor = if (isGoldStage) Color(0xFFFACC15L) else Color(0xFF38BDF8L)
        drawLine(
            color = railColor.copy(alpha = 0.42f + beatKick * 0.25f),
            start = topL,
            end = botL,
            strokeWidth = 3.2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = railColor.copy(alpha = 0.42f + beatKick * 0.25f),
            start = topR,
            end = botR,
            strokeWidth = 3.2f,
            cap = StrokeCap.Round
        )

        val phaseFrac = if (beatPhase >= 0f) beatPhase.coerceIn(0f, 1f) else ((time * 2f) % 1f)
        for (barIdx in 0..3) {
            val uBar = ((barIdx + phaseFrac) / 4f).coerceIn(0.02f, 1.12f)
            val bL = computeHighwayPoint(-0.42f, uBar, w, h)
            val bR = computeHighwayPoint(3.42f, uBar, w, h)
            val barAlpha = (0.10f + 0.26f * uBar) * (if (uBar in 0.92f..1.05f) 1.5f else 1.0f)
            drawLine(
                color = Color(0xFFE2E8F0L).copy(alpha = barAlpha.coerceIn(0.05f, 0.48f)),
                start = bL,
                end = bR,
                strokeWidth = 1.4f + uBar * 1.8f
            )
        }

        for (lane in 0..3) {
            val lTop = computeHighwayPoint(lane.toFloat(), 0f, w, h)
            val lBot = computeHighwayPoint(lane.toFloat(), 1.16f, w, h)
            val lCol = laneColors[lane]
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lCol.copy(alpha = 0.12f),
                        lCol.copy(alpha = 0.42f + beatKick * 0.18f),
                        lCol.copy(alpha = 0.72f)
                    ),
                    startY = lTop.y,
                    endY = lBot.y
                ),
                start = lTop,
                end = lBot,
                strokeWidth = 2.4f,
                cap = StrokeCap.Round
            )
        }

        val strikeGlowCol = if (isGoldStage) Color(0xFFFACC15L) else Color(0xFF38BDF8L)
        drawLine(
            color = strikeGlowCol.copy(alpha = 0.28f + beatKick * 0.24f),
            start = Offset(strikeL.x - 10f, strikeL.y),
            end = Offset(strikeR.x + 10f, strikeR.y),
            strokeWidth = 14f + beatKick * 6f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.82f + beatKick * 0.18f),
            start = strikeL,
            end = strikeR,
            strokeWidth = 3.2f,
            cap = StrokeCap.Round
        )

        for (lane in 0..3) {
            val recPt = computeHighwayPoint(lane.toFloat(), 1.0f, w, h)
            val lCol = laneColors[lane]
            val hasNoteOnLine = powerUps.any {
                val lead = (it.targetBeatSec - it.spawnBeatSec).coerceAtLeast(0.45f)
                val uNote = (trackSec - it.spawnBeatSec) / lead
                (it.phraseBeatNum - 1) == lane && uNote in 0.85f..1.14f
            }
            val recR = if (hasNoteOnLine) 28f + beatKick * 6f else 22f + beatKick * 2.5f
            drawCircle(
                color = lCol.copy(alpha = if (hasNoteOnLine) 0.52f else 0.20f),
                radius = recR * 1.35f,
                center = recPt
            )
            drawCircle(
                color = Color(0xFF090D1AL).copy(alpha = 0.88f),
                radius = recR,
                center = recPt
            )
            drawCircle(
                color = if (hasNoteOnLine) Color.White else lCol.copy(alpha = 0.85f),
                radius = recR,
                center = recPt,
                style = Stroke(width = if (hasNoteOnLine) 4.2f else 2.6f)
            )
            drawCircle(
                color = lCol.copy(alpha = if (hasNoteOnLine) 0.95f else 0.45f),
                radius = recR * 0.38f,
                center = recPt
            )
        }
    }

    if (powerUps.isEmpty()) return
    val sorted = if (powerUps.size >= 2) powerUps.sortedBy { it.sequenceIndex } else powerUps

    sorted.forEach { p ->
        val leadDur = (p.targetBeatSec - p.spawnBeatSec).coerceAtLeast(0.45f)
        val u = ((trackSec - p.spawnBeatSec) / leadDur).coerceIn(0f, 1.35f)
        val perspScale = if (isBandHeroMode) computeHighwayScale(u.coerceAtMost(1.25f)) else 1.0f
        val center = Offset(p.x, p.y)

        val isElectric = p.powerUpType == PowerUpType.ELECTRIC_HARMONIC || p.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR
        val isPrism = p.powerUpType == PowerUpType.PRISM_HARMONY || p.keyWordEffect == KeyWordEffect.PRISM_SHATTER
        val isVortex = p.powerUpType == PowerUpType.SLOW_MO || p.keyWordEffect == KeyWordEffect.ECLIPSE_DARK
        val isBomb = p.powerUpType == PowerUpType.BLAST || p.keyWordEffect == KeyWordEffect.MAGNETAR_WAR
        val isShield = p.powerUpType == PowerUpType.SHIELD || p.keyWordEffect == KeyWordEffect.TRACTOR_REACH
        val isSpecial = isElectric || isPrism || isVortex || isBomb || isShield
        val isCipherOrb = p.isMusicVideoOrb && !isSpecial

        val expireSec = if (p.expireBeatSec > 0f) p.expireBeatSec else (p.targetBeatSec + 0.45f)
        val ttlRemaining = (expireSec - trackSec).coerceAtLeast(0f)
        val ttlAlpha = (ttlRemaining / 0.35f).coerceIn(0.22f, 1.0f)

        val timeToSwell = p.targetBeatSec - trackSec
        val isSwellSweetSpot = abs(timeToSwell) <= 0.42f
        val isOnBeatNow = if (!isMusicStarted) {
            false
        } else if (isSpecial) {
            isSwellSweetSpot
        } else if (isBandHeroMode) {
            u in 0.86f..1.14f
        } else {
            beatPhase in 0f..0.24f || beatPhase >= 0.84f
        }

        val isTier2GoldPulse = isMusicStarted && isGoldStage && goldenTier >= 2
        val specialScaleMult = if (isSpecial) 1.24f else if (isCipherOrb) 1.15f else 1.0f
        val beatPulseScale = if (!isMusicStarted) {
            1.0f
        } else {
            val tierBoost = if (isTier2GoldPulse) 0.10f * beatKick else 0f
            1.0f + (if (isOnBeatNow) 0.18f else 0.07f) * beatKick + tierBoost +
                0.05f * sin((beatPhase.coerceIn(0f, 1f)) * 6.283185f)
        }
        val coreRad = p.radius * perspScale * specialScaleMult * beatPulseScale
        val bounceOffsetY = if (!isMusicStarted) 0f else -beatKick * (if (isOnBeatNow) 5.5f else 3f) * perspScale
        val center3D = Offset(center.x, center.y + bounceOffsetY)
        val lightOrigin = Offset(center3D.x - coreRad * 0.34f, center3D.y - coreRad * 0.36f)

        val laneIdx = (p.phraseBeatNum - 1).coerceIn(0, 3)
        val effectColor = when {
            isCipherOrb -> Color(0xFF05D9E8L)
            isGoldStage || p.isGoldenOrb -> Color(0xFFFACC15L)
            isElectric -> Color(0xFFFACC15L)
            isPrism -> Color(0xFF4ADE80L)
            isVortex -> Color(0xFFC084FCL)
            isBomb -> Color(0xFFF97316L)
            isShield -> Color(0xFF38BDF8L)
            else -> laneColors[laneIdx]
        }

        // =========================================================================
        // SWELL-LOCK TIMING HALO (Special Orbs) OR SUBTLE BEAT RING (Regular Orbs)
        // =========================================================================
        if (isSpecial) {
            val approachFrac = (1f - u.coerceIn(0f, 1f))
            val swellRingR = coreRad * (1.08f + approachFrac * 1.48f)
            val haloColor = if (isSwellSweetSpot) Color.White else effectColor
            val haloAlpha = (if (isSwellSweetSpot) 0.95f else (0.35f + (1f - approachFrac) * 0.50f)) * ttlAlpha

            // Outer converging timing lock ring
            drawCircle(
                color = haloColor.copy(alpha = haloAlpha),
                radius = swellRingR,
                center = center3D,
                style = Stroke(width = if (isSwellSweetSpot) 4.2f else 2.4f)
            )
            // Secondary chromatic echo ring
            drawCircle(
                color = effectColor.copy(alpha = haloAlpha * 0.45f),
                radius = swellRingR * 1.08f,
                center = center3D,
                style = Stroke(width = 1.4f)
            )
            // 4 Cardinal Convergence Diamond Ticks locking onto the rim right at the musical swell
            for (d in 0 until 4) {
                val tickAng = d * (PI.toFloat() * 0.5f) + approachFrac * 1.2f
                val tx = center3D.x + cos(tickAng) * swellRingR
                val ty = center3D.y + sin(tickAng) * swellRingR
                drawCircle(
                    color = if (isSwellSweetSpot) Color(0xFFFACC15L) else Color.White.copy(alpha = haloAlpha),
                    radius = (if (isSwellSweetSpot) 4.6f else 3.0f) * perspScale,
                    center = Offset(tx, ty)
                )
            }
            if (isSwellSweetSpot) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f * ttlAlpha),
                            effectColor.copy(alpha = 0.25f * ttlAlpha),
                            Color.Transparent
                        ),
                        center = center3D,
                        radius = coreRad * 2.5f
                    ),
                    radius = coreRad * 2.5f,
                    center = center3D,
                    blendMode = BlendMode.Screen
                )
            }
        } else if (!isBandHeroMode) {
            if (isMusicStarted) {
                val phaseClamped = beatPhase.coerceIn(0f, 1f)
                val beatRingFrac = if (beatPhase >= 0f) (1f - phaseClamped) else 0.5f
                val beatRingR = coreRad * (1.06f + beatRingFrac * 0.42f)
                drawCircle(
                    color = (if (isOnBeatNow) Color.White else effectColor).copy(alpha = (0.22f + 0.34f * beatKick) * ttlAlpha),
                    radius = beatRingR,
                    center = center3D,
                    style = Stroke(width = if (isOnBeatNow) 2.3f else 1.3f)
                )
                // 1:18+ Tier-2 24K Special Harmonic Pulse: Expanding Twin Gold/Diamond Shockwave Rings + 4 Starflare Glints!
                if (isTier2GoldPulse) {
                    val expandWaveR = coreRad * (1.18f + phaseClamped * 0.92f)
                    val expandFade = (1f - phaseClamped) * ttlAlpha
                    drawCircle(
                        color = Color(0xFFFACC15L).copy(alpha = 0.72f * expandFade),
                        radius = expandWaveR,
                        center = center3D,
                        style = Stroke(width = 3.0f * perspScale)
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8L).copy(alpha = 0.58f * expandFade),
                        radius = coreRad * (1.35f + (1f - phaseClamped) * 0.55f),
                        center = center3D,
                        style = Stroke(width = 1.8f * perspScale)
                    )
                    for (sp in 0 until 4) {
                        val spAng = sp * (PI.toFloat() * 0.5f) + time * 1.8f
                        val flareDist = coreRad * (1.28f + 0.22f * beatKick)
                        val fx = center3D.x + cos(spAng) * flareDist
                        val fy = center3D.y + sin(spAng) * flareDist
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = (0.85f + 0.15f * beatKick) * ttlAlpha),
                                    Color(0xFFFEF08AL).copy(alpha = 0.52f * ttlAlpha),
                                    Color.Transparent
                                ),
                                center = Offset(fx, fy),
                                radius = 7.5f * perspScale
                            ),
                            radius = 7.5f * perspScale,
                            center = Offset(fx, fy),
                            blendMode = BlendMode.Screen
                        )
                    }
                }
            }
        } else if (isOnBeatNow) {
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.82f * ttlAlpha),
                radius = coreRad * 1.38f,
                center = center3D,
                style = Stroke(width = 3.2f)
            )
        }

        // Soft volumetric ground shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.36f * ttlAlpha),
            topLeft = Offset(center.x - coreRad * 0.82f, center.y + coreRad * 0.48f),
            size = Size(coreRad * 1.64f, coreRad * 0.42f)
        )

        when {
            isElectric -> drawElectricSurgeRelic(center3D, lightOrigin, coreRad, perspScale, ttlAlpha, time, beatKick)
            isPrism -> drawPrismLotusRelic(center3D, lightOrigin, coreRad, perspScale, ttlAlpha, time, beatKick)
            isVortex -> drawVortexSingularityRelic(center3D, coreRad, perspScale, ttlAlpha, time, beatKick)
            isBomb -> drawSolarNovaRelic(center3D, lightOrigin, coreRad, perspScale, ttlAlpha, time, beatKick)
            isShield -> drawAegisGeodesicRelic(center3D, lightOrigin, coreRad, perspScale, ttlAlpha, time, beatKick)
            isCipherOrb -> drawEnigmaticCipherRelic(center3D, coreRad, perspScale, ttlAlpha, time, beatKick, p.sequenceIndex)
            else -> drawLivingResonanceSphere(
                p = p,
                center3D = center3D,
                lightOrigin = lightOrigin,
                coreRad = coreRad,
                perspScale = perspScale,
                ttlAlpha = ttlAlpha,
                effectColor = effectColor,
                isGoldStage = isGoldStage || p.isGoldenOrb,
                isOnBeatNow = isOnBeatNow,
                time = time,
                beatKick = beatKick,
                vocalEnergy = multiBand.vocal
            )
        }
    }
}

/**
 * ELECTRIC SURGE RELIC (Zero Emojis):
 * Triple counter-rotating 3D gyroscope rings + 6 crackling procedural plasma filaments arcing from an incandescent core.
 */
private fun DrawScope.drawElectricSurgeRelic(
    center3D: Offset,
    lightOrigin: Offset,
    coreRad: Float,
    perspScale: Float,
    ttlAlpha: Float,
    time: Float,
    beatKick: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f * ttlAlpha),
                Color(0xFFFACC15L).copy(alpha = 0.78f * ttlAlpha),
                Color(0xFF38BDF8L).copy(alpha = 0.42f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * 2.45f
        ),
        radius = coreRad * 2.45f,
        center = center3D,
        blendMode = BlendMode.Screen
    )

    // 3 Counter-Rotating 3D Gyroscope Containment Rings
    val ringAngles = floatArrayOf((time * 115f) % 360f, (-time * 142f + 60f) % 360f, (time * 88f + 125f) % 360f)
    val ringCols = arrayOf(Color(0xFFFEF08AL), Color(0xFF38BDF8L), Color(0xFFFACC15L))
    for (r in 0..2) {
        withTransform({
            rotate(degrees = ringAngles[r], pivot = center3D)
        }) {
            val rx = coreRad * (1.36f - r * 0.08f)
            val ry = coreRad * (0.44f + r * 0.08f)
            drawOval(
                color = ringCols[r].copy(alpha = 0.88f * ttlAlpha),
                topLeft = Offset(center3D.x - rx, center3D.y - ry),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(width = (2.6f - r * 0.3f) * perspScale)
            )
            val nodeAng = time * (3.2f + r)
            drawCircle(
                color = Color.White.copy(alpha = 0.96f * ttlAlpha),
                radius = 3.2f * perspScale,
                center = Offset(center3D.x + cos(nodeAng) * rx, center3D.y + sin(nodeAng) * ry)
            )
        }
    }

    // Deep Ionized Sphere Shell
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.99f * ttlAlpha),
                Color(0xFFFDE047L).copy(alpha = 0.94f * ttlAlpha),
                Color(0xFF0284C7L).copy(alpha = 0.95f * ttlAlpha),
                Color(0xFF061126L).copy(alpha = 0.98f * ttlAlpha)
            ),
            center = lightOrigin,
            radius = coreRad * 1.32f
        ),
        radius = coreRad,
        center = center3D
    )

    // Smooth Counter-Rotating Plasma Crescent Arcs & Expanding Ripple Halos (zero jagged stick-lightning!)
    for (arc in 0 until 4) {
        val startDeg = (time * 165f + arc * 90f) % 360f
        val arcR = coreRad * (0.66f + (arc % 2) * 0.22f + beatKick * 0.08f)
        drawArc(
            color = (if (arc % 2 == 0) Color.White else Color(0xFF7DD3FCL)).copy(alpha = 0.88f * ttlAlpha),
            startAngle = startDeg,
            sweepAngle = 56f,
            useCenter = false,
            topLeft = Offset(center3D.x - arcR, center3D.y - arcR),
            size = Size(arcR * 2f, arcR * 2f),
            style = Stroke(width = 2.8f * perspScale, cap = StrokeCap.Round)
        )
    }
    val ripplePhase = (time * 1.35f) % 1f
    drawCircle(
        color = Color(0xFFFDE047L).copy(alpha = (1f - ripplePhase) * 0.72f * ttlAlpha),
        radius = coreRad * (1.05f + ripplePhase * 0.75f),
        center = center3D,
        style = Stroke(width = 2.4f * (1f - ripplePhase * 0.5f) * perspScale)
    )

    // Incandescent White-Gold Plasma Singularity Core
    drawCircle(
        color = Color.White.copy(alpha = 0.98f * ttlAlpha),
        radius = coreRad * (0.30f + beatKick * 0.08f),
        center = center3D
    )
    drawCircle(
        color = Color(0xFFFEF08AL).copy(alpha = 0.95f * ttlAlpha),
        radius = coreRad,
        center = center3D,
        style = Stroke(width = 3.2f * perspScale)
    )
}

/**
 * PRISM VOCAL LOTUS RELIC:
 * Counter-rotating 8-point crystalline sacred star-lotus with rainbow spectral dispersion facets.
 */
private fun DrawScope.drawPrismLotusRelic(
    center3D: Offset,
    lightOrigin: Offset,
    coreRad: Float,
    perspScale: Float,
    ttlAlpha: Float,
    time: Float,
    beatKick: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.96f * ttlAlpha),
                Color(0xFF4ADE80L).copy(alpha = 0.76f * ttlAlpha),
                Color(0xFFEC4899L).copy(alpha = 0.45f * ttlAlpha),
                Color(0xFF38BDF8L).copy(alpha = 0.24f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * 2.4f
        ),
        radius = coreRad * 2.4f,
        center = center3D,
        blendMode = BlendMode.Screen
    )

    val spectrumCols = arrayOf(
        Color(0xFF4ADE80L), Color(0xFF38BDF8L), Color(0xFFC084FCL), Color(0xFFEC4899L),
        Color(0xFFFACC15L), Color(0xFF2DD4BFL), Color(0xFF818CF8L), Color(0xFFF472B6L)
    )

    // Outer 8-Petal Curved Bezier Lotus (smooth organic petals, zero sharp polygon corners)
    for (pIdx in 0 until 8) {
        val ang = time * 0.95f + pIdx * (PI.toFloat() * 0.25f)
        val tipR = coreRad * (1.22f + (if (pIdx % 2 == 0) 0.12f else 0.0f) + beatKick * 0.10f)
        val ctrlR = coreRad * 0.84f
        val spread = 0.36f

        val tip = Offset(center3D.x + cos(ang) * tipR, center3D.y + sin(ang) * tipR)
        val leftCtrl = Offset(center3D.x + cos(ang - spread) * ctrlR, center3D.y + sin(ang - spread) * ctrlR)
        val rightCtrl = Offset(center3D.x + cos(ang + spread) * ctrlR, center3D.y + sin(ang + spread) * ctrlR)

        val petal = sharedPetalPath.apply {
            reset()
            moveTo(center3D.x, center3D.y)
            quadraticTo(leftCtrl.x, leftCtrl.y, tip.x, tip.y)
            quadraticTo(rightCtrl.x, rightCtrl.y, center3D.x, center3D.y)
            close()
        }
        drawPath(
            path = petal,
            color = spectrumCols[pIdx].copy(alpha = 0.80f * ttlAlpha)
        )
        drawPath(
            path = petal,
            color = Color.White.copy(alpha = 0.65f * ttlAlpha),
            style = Stroke(width = 1.6f * perspScale, cap = StrokeCap.Round)
        )
    }

    // Inner Luminous Pearl Sphere Core (replaces flat straight-sided hexagon)
    val crownR = coreRad * 0.56f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.99f * ttlAlpha),
                Color(0xFFA7F3D0L).copy(alpha = 0.94f * ttlAlpha),
                Color(0xFF38BDF8L).copy(alpha = 0.92f * ttlAlpha)
            ),
            center = lightOrigin,
            radius = crownR * 1.3f
        ),
        radius = crownR,
        center = center3D
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.95f * ttlAlpha),
        radius = crownR,
        center = center3D,
        style = Stroke(width = 2.2f * perspScale)
    )

    // 6 Orbiting Spectral Prism Motes
    for (m in 0 until 6) {
        val mAng = -time * 2.1f + m * (PI.toFloat() / 3f)
        val mDist = coreRad * 1.48f
        drawCircle(
            color = spectrumCols[m].copy(alpha = 0.92f * ttlAlpha),
            radius = 3.2f * perspScale,
            center = Offset(center3D.x + cos(mAng) * mDist, center3D.y + sin(mAng) * mDist)
        )
    }
}

/**
 * VORTEX TIME-WARP SINGULARITY RELIC:
 * Gravitational lens Einstein ring, doppler accretion disk, and 4 inward-falling stardust spirals.
 */
private fun DrawScope.drawVortexSingularityRelic(
    center3D: Offset,
    coreRad: Float,
    perspScale: Float,
    ttlAlpha: Float,
    time: Float,
    beatKick: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE9D5FFL).copy(alpha = 0.80f * ttlAlpha),
                Color(0xFFA855F7L).copy(alpha = 0.52f * ttlAlpha),
                Color(0xFF38BDF8L).copy(alpha = 0.24f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * 2.45f
        ),
        radius = coreRad * 2.45f,
        center = center3D,
        blendMode = BlendMode.Screen
    )

    // Tilted Doppler-Shifted Accretion Disk
    withTransform({
        rotate(degrees = -22f + sin(time * 1.8f) * 8f, pivot = center3D)
    }) {
        drawOval(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF38BDF8L).copy(alpha = 0.92f * ttlAlpha),
                    Color(0xFFE9D5FFL).copy(alpha = 0.95f * ttlAlpha),
                    Color(0xFF9333EAL).copy(alpha = 0.78f * ttlAlpha)
                ),
                startX = center3D.x - coreRad * 1.48f,
                endX = center3D.x + coreRad * 1.48f
            ),
            topLeft = Offset(center3D.x - coreRad * 1.48f, center3D.y - coreRad * 0.44f),
            size = Size(coreRad * 2.96f, coreRad * 0.88f),
            style = Stroke(width = (4.2f + beatKick * 1.8f) * perspScale)
        )
    }

    // 4 Smooth Bezier Logarithmic Accretion Spirals
    withTransform({
        rotate(degrees = (time * 148f) % 360f, pivot = center3D)
    }) {
        for (arm in 0 until 4) {
            val baseAng = arm * (PI.toFloat() * 0.5f)
            val spiral = sharedFilamentPath.apply {
                reset()
                var prevX = center3D.x
                var prevY = center3D.y
                for (st in 0..18) {
                    val tNorm = st / 18f
                    val r = coreRad * (0.32f + tNorm * 1.08f)
                    val theta = baseAng + tNorm * 2.35f
                    val ax = center3D.x + cos(theta) * r
                    val ay = center3D.y + sin(theta) * r
                    if (st == 0) {
                        moveTo(ax, ay)
                    } else {
                        quadraticTo(prevX, prevY, (prevX + ax) * 0.5f, (prevY + ay) * 0.5f)
                        if (st == 18) lineTo(ax, ay)
                    }
                    prevX = ax
                    prevY = ay
                }
            }
            drawPath(
                path = spiral,
                color = (if (arm % 2 == 0) Color(0xFFF3E8FFL) else Color(0xFF38BDF8L)).copy(alpha = 0.86f * ttlAlpha),
                style = Stroke(width = 2.8f * perspScale, cap = StrokeCap.Round)
            )
        }
    }

    // Pitch-Black Event Horizon + Luminous Einstein Lensing Ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF020006L).copy(alpha = 0.99f * ttlAlpha),
                Color(0xFF1E0942L).copy(alpha = 0.98f * ttlAlpha),
                Color(0xFF7E22CEL).copy(alpha = 0.95f * ttlAlpha)
            ),
            center = center3D,
            radius = coreRad * 0.72f
        ),
        radius = coreRad * 0.72f,
        center = center3D
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.95f * ttlAlpha),
        radius = coreRad * (0.72f + beatKick * 0.05f),
        center = center3D,
        style = Stroke(width = 2.8f * perspScale)
    )
}

/**
 * SOLAR NOVA RELIC:
 * 12-spike undulating solar corona starburst + magnetic prominence loops + boiling molten core.
 */
private fun DrawScope.drawSolarNovaRelic(
    center3D: Offset,
    lightOrigin: Offset,
    coreRad: Float,
    perspScale: Float,
    ttlAlpha: Float,
    time: Float,
    beatKick: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFEF08AL).copy(alpha = 0.90f * ttlAlpha),
                Color(0xFFF97316L).copy(alpha = 0.60f * ttlAlpha),
                Color(0xFFEF4444L).copy(alpha = 0.25f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * 2.5f
        ),
        radius = coreRad * 2.5f,
        center = center3D,
        blendMode = BlendMode.Screen
    )

    // Smooth Scalloped Bezier Solar Corona (replaces jagged 24-vertex sawtooth polygon)
    val corona = sharedPlasmaContourPath.apply {
        reset()
        val pts = 48
        var prevX = 0f
        var prevY = 0f
        var firstX = 0f
        var firstY = 0f
        for (i in 0..pts) {
            val theta = time * 0.85f + (i.toFloat() / pts) * 2f * PI.toFloat()
            val petalMod = 1.10f + (0.24f + beatKick * 0.12f) * sin(theta * 8f + time * 4f)
            val r = coreRad * petalMod
            val x = center3D.x + cos(theta) * r
            val y = center3D.y + sin(theta) * r
            if (i == 0) {
                firstX = x
                firstY = y
                moveTo(x, y)
            } else {
                quadraticTo(prevX, prevY, (prevX + x) * 0.5f, (prevY + y) * 0.5f)
            }
            prevX = x
            prevY = y
        }
        quadraticTo(prevX, prevY, firstX, firstY)
        close()
    }
    drawPath(
        path = corona,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.96f * ttlAlpha),
                Color(0xFFFACC15L).copy(alpha = 0.90f * ttlAlpha),
                Color(0xFFEA580CL).copy(alpha = 0.82f * ttlAlpha)
            ),
            center = center3D,
            radius = coreRad * 1.48f
        )
    )

    // Molten Photosphere Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.99f * ttlAlpha),
                Color(0xFFFEF08AL).copy(alpha = 0.97f * ttlAlpha),
                Color(0xFFF97316L).copy(alpha = 0.96f * ttlAlpha),
                Color(0xFF7C2D12L).copy(alpha = 0.98f * ttlAlpha)
            ),
            center = lightOrigin,
            radius = coreRad * 1.25f
        ),
        radius = coreRad * 0.86f,
        center = center3D
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.94f * ttlAlpha),
        radius = coreRad * 0.86f,
        center = center3D,
        style = Stroke(width = 2.6f * perspScale)
    )
}

/**
 * AEGIS SHIELD GEODESIC RELIC:
 * Crystalline sapphire dome with rotating hexagonal energy lattice.
 */
private fun DrawScope.drawAegisGeodesicRelic(
    center3D: Offset,
    lightOrigin: Offset,
    coreRad: Float,
    perspScale: Float,
    ttlAlpha: Float,
    time: Float,
    beatKick: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF7DD3FCL).copy(alpha = 0.82f * ttlAlpha),
                Color(0xFF0284C7L).copy(alpha = 0.34f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * 2.25f
        ),
        radius = coreRad * 2.25f,
        center = center3D,
        blendMode = BlendMode.Screen
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f * ttlAlpha),
                Color(0xFF38BDF8L).copy(alpha = 0.94f * ttlAlpha),
                Color(0xFF0369A1L).copy(alpha = 0.96f * ttlAlpha),
                Color(0xFF082F49L).copy(alpha = 0.98f * ttlAlpha)
            ),
            center = lightOrigin,
            radius = coreRad * 1.32f
        ),
        radius = coreRad,
        center = center3D
    )
    // Twin Counter-Rotating Sapphire Crescent Shield Arcs (replaces rigid hexagon wireframe)
    for (arc in 0..2) {
        val deg = (time * 95f + arc * 120f) % 360f
        val rArc = coreRad * 0.72f
        drawArc(
            color = Color.White.copy(alpha = 0.86f * ttlAlpha),
            startAngle = deg,
            sweepAngle = 72f,
            useCenter = false,
            topLeft = Offset(center3D.x - rArc, center3D.y - rArc),
            size = Size(rArc * 2f, rArc * 2f),
            style = Stroke(width = 2.4f * perspScale, cap = StrokeCap.Round)
        )
    }
    drawCircle(
        color = Color.White.copy(alpha = 0.95f * ttlAlpha),
        radius = coreRad * (1.0f + beatKick * 0.04f),
        center = center3D,
        style = Stroke(width = 3.0f * perspScale)
    )
}

/**
 * ENIGMATIC ANALOG CIPHER / VHS RELIC ORB (Replaces Video Camera Emoji — Zero Words, Zero Emojis!):
 * RGB VHS phosphor channel-split aura + obsidian casing + 6-blade breathing mechanical iris + twin spinning micro-spools!
 */
private fun DrawScope.drawEnigmaticCipherRelic(
    center3D: Offset,
    coreRad: Float,
    perspScale: Float,
    ttlAlpha: Float,
    time: Float,
    beatKick: Float,
    seed: Int
) {
    val splitOffset = (2.8f + sin(time * 9f + seed) * 1.4f) * perspScale

    // 1. RGB VHS Chromatic Split Halo (Magenta Right / Cyan Left)
    drawCircle(
        color = Color(0xFFFF2A6DL).copy(alpha = 0.72f * ttlAlpha),
        radius = coreRad * 1.16f,
        center = Offset(center3D.x + splitOffset, center3D.y),
        style = Stroke(width = 2.6f * perspScale)
    )
    drawCircle(
        color = Color(0xFF05D9E8L).copy(alpha = 0.78f * ttlAlpha),
        radius = coreRad * 1.16f,
        center = Offset(center3D.x - splitOffset, center3D.y),
        style = Stroke(width = 2.6f * perspScale)
    )

    // Volumetric Analog Phosphor Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF05D9E8L).copy(alpha = 0.55f * ttlAlpha),
                Color(0xFFFF2A6DL).copy(alpha = 0.32f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * 2.15f
        ),
        radius = coreRad * 2.15f,
        center = center3D,
        blendMode = BlendMode.Screen
    )

    // 2. Deep Obsidian Optical Glass Housing
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF191032L).copy(alpha = 0.98f * ttlAlpha),
                Color(0xFF090D1EL).copy(alpha = 0.99f * ttlAlpha),
                Color(0xFF03050BL).copy(alpha = 0.99f * ttlAlpha)
            ),
            center = center3D,
            radius = coreRad
        ),
        radius = coreRad,
        center = center3D
    )

    // 3. Holographic Chromatic Crescent Shutter Rings (smooth curved arcs, zero thin wire spokes or triangles!)
    for (arc in 0 until 3) {
        val rotDeg = (time * 125f + seed * 37f + arc * 120f) % 360f
        val rIris = coreRad * (0.78f + beatKick * 0.06f)
        val arcCol = when (arc) {
            0 -> Color(0xFF05D9E8L)
            1 -> Color(0xFFFF2A6DL)
            else -> Color(0xFFFACC15L)
        }
        drawArc(
            color = arcCol.copy(alpha = 0.88f * ttlAlpha),
            startAngle = rotDeg,
            sweepAngle = 76f,
            useCenter = false,
            topLeft = Offset(center3D.x - rIris, center3D.y - rIris),
            size = Size(rIris * 2f, rIris * 2f),
            style = Stroke(width = 2.8f * perspScale, cap = StrokeCap.Round)
        )
    }

    // 4. Twin Luminous Studio Reel Pearls + Smooth Curved Ribbon Bridge
    val spoolDist = coreRad * 0.40f
    val spoolR = coreRad * 0.25f
    val leftSpool = Offset(center3D.x - spoolDist, center3D.y)
    val rightSpool = Offset(center3D.x + spoolDist, center3D.y)

    val bridgePath = sharedFilamentPath.apply {
        reset()
        moveTo(leftSpool.x, leftSpool.y - spoolR * 0.85f)
        quadraticTo(center3D.x, center3D.y - spoolR * 1.25f, rightSpool.x, rightSpool.y - spoolR * 0.85f)
    }
    drawPath(
        path = bridgePath,
        color = Color(0xFFFACC15L).copy(alpha = 0.86f * ttlAlpha),
        style = Stroke(width = 2.2f * perspScale, cap = StrokeCap.Round)
    )

    for (sIdx in 0..1) {
        val sc = if (sIdx == 0) leftSpool else rightSpool
        val spinDir = if (sIdx == 0) 1f else -1f
        val reelCol = if (sIdx == 0) Color(0xFF05D9E8L) else Color(0xFFFF2A6DL)
        drawCircle(
            color = Color(0xFF060914L).copy(alpha = 0.95f * ttlAlpha),
            radius = spoolR,
            center = sc
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.92f * ttlAlpha),
            radius = spoolR,
            center = sc,
            style = Stroke(width = 2.0f * perspScale)
        )
        val orbAng = time * 4.2f * spinDir
        drawCircle(
            color = Color(0xFFFACC15L).copy(alpha = 0.95f * ttlAlpha),
            radius = spoolR * 0.26f,
            center = Offset(sc.x + cos(orbAng) * spoolR * 0.56f, sc.y + sin(orbAng) * spoolR * 0.56f)
        )
        drawCircle(
            color = reelCol.copy(alpha = 0.96f * ttlAlpha),
            radius = spoolR * 0.34f,
            center = sc
        )
    }

    // 5. Incandescent Enigmatic Center Lens Bloom + Precision Rim
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.90f + 0.10f * beatKick) * ttlAlpha),
                Color(0xFF05D9E8L).copy(alpha = 0.45f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * 0.42f
        ),
        radius = coreRad * 0.42f,
        center = center3D,
        blendMode = BlendMode.Screen
    )
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF05D9E8L).copy(alpha = 0.96f * ttlAlpha),
                Color(0xFFFACC15L).copy(alpha = 0.96f * ttlAlpha),
                Color(0xFFFF2A6DL).copy(alpha = 0.96f * ttlAlpha),
                Color(0xFF05D9E8L).copy(alpha = 0.96f * ttlAlpha)
            ),
            center = center3D
        ),
        radius = coreRad,
        center = center3D,
        style = Stroke(width = 3.0f * perspScale)
    )
}

/**
 * REGULAR BEAT ORB -> LIVING PLASMA RESONANCE SPHERE (Zero Text Font Stamp!):
 * Velocity-aligned silk comet ribbon + organic morphing ferrofluid contour + internal harmonic soundwave caustic!
 */
private fun DrawScope.drawLivingResonanceSphere(
    p: GameObject,
    center3D: Offset,
    lightOrigin: Offset,
    coreRad: Float,
    perspScale: Float,
    ttlAlpha: Float,
    effectColor: Color,
    isGoldStage: Boolean,
    isOnBeatNow: Boolean,
    time: Float,
    beatKick: Float,
    vocalEnergy: Float
) {
    // 1. Soft Bioluminescent Bloom Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                effectColor.copy(alpha = (0.64f + 0.24f * beatKick) * ttlAlpha),
                effectColor.copy(alpha = 0.20f * ttlAlpha),
                Color.Transparent
            ),
            center = center3D,
            radius = coreRad * (1.82f + 0.30f * beatKick)
        ),
        radius = coreRad * (1.82f + 0.30f * beatKick),
        center = center3D,
        blendMode = BlendMode.Screen
    )

    // 2. Velocity-Aligned Tapered Silk Comet Tail
    val vx = p.trajVx
    val vy = if (abs(p.trajVy) < 10f) 180f else p.trajVy
    val vMag = hypot(vx, vy).coerceAtLeast(1f)
    val tailDirX = -vx / vMag
    val tailDirY = -vy / vMag
    val perpX = -tailDirY
    val perpY = tailDirX
    val tailLen = coreRad * (2.35f + beatKick * 0.55f)
    val tailTip = Offset(
        center3D.x + tailDirX * tailLen + perpX * sin(time * 6f + p.sequenceIndex) * (coreRad * 0.16f),
        center3D.y + tailDirY * tailLen
    )
    val wakeRibbon = sharedWakeRibbonPath.apply {
        reset()
        moveTo(center3D.x + perpX * coreRad * 0.56f, center3D.y + perpY * coreRad * 0.56f)
        quadraticTo(
            center3D.x + tailDirX * tailLen * 0.48f + perpX * coreRad * 0.22f,
            center3D.y + tailDirY * tailLen * 0.48f + perpY * coreRad * 0.22f,
            tailTip.x,
            tailTip.y
        )
        quadraticTo(
            center3D.x + tailDirX * tailLen * 0.48f - perpX * coreRad * 0.22f,
            center3D.y + tailDirY * tailLen * 0.48f - perpY * coreRad * 0.22f,
            center3D.x - perpX * coreRad * 0.56f,
            center3D.y - perpY * coreRad * 0.56f
        )
        close()
    }
    drawPath(
        path = wakeRibbon,
        brush = Brush.linearGradient(
            colors = listOf(
                effectColor.copy(alpha = 0.46f * ttlAlpha),
                effectColor.copy(alpha = 0.14f * ttlAlpha),
                Color.Transparent
            ),
            start = center3D,
            end = tailTip
        )
    )

    // 3. Organic Morphing Ferrofluid / Plasma Core Contour
    val liquidPhase = time * 4.6f + p.sequenceIndex * 0.85f
    val animLight = Offset(
        lightOrigin.x + cos(liquidPhase) * (coreRad * 0.15f),
        lightOrigin.y + sin(liquidPhase) * (coreRad * 0.15f)
    )
    val darkRim = if (isGoldStage) {
        Color(0xFF422006L).copy(alpha = 0.98f * ttlAlpha)
    } else {
        Color(
            red = (effectColor.red * 0.16f).coerceIn(0f, 1f),
            green = (effectColor.green * 0.16f).coerceIn(0f, 1f),
            blue = (effectColor.blue * 0.22f).coerceIn(0f, 1f),
            alpha = 0.98f * ttlAlpha
        )
    }

    val morphContour = sharedPlasmaContourPath.apply {
        reset()
        val segs = 24
        for (s in 0 until segs) {
            val theta = s * (2f * PI.toFloat() / segs)
            val wobble = 1.0f +
                0.048f * sin(3f * theta + liquidPhase) +
                0.032f * cos(5f * theta - liquidPhase * 0.8f) * (0.5f + beatKick * 0.5f)
            val r = coreRad * wobble
            val px = center3D.x + cos(theta) * r
            val py = center3D.y + sin(theta) * r
            if (s == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    drawPath(
        path = morphContour,
        brush = Brush.radialGradient(
            colors = if (isGoldStage) listOf(
                Color.White.copy(alpha = 0.99f * ttlAlpha),
                Color(0xFFFEF08AL).copy(alpha = 0.98f * ttlAlpha),
                Color(0xFFFACC15L).copy(alpha = 0.96f * ttlAlpha),
                darkRim
            ) else listOf(
                Color.White.copy(alpha = 0.96f * ttlAlpha),
                effectColor.copy(alpha = 0.95f * ttlAlpha),
                darkRim
            ),
            center = animLight,
            radius = coreRad * 1.38f
        )
    )

    // 4. Internal Living Harmonic Soundwave Caustic (Pure Visual Music — Zero Text Font Clutter!)
    val waveSpan = coreRad * 0.64f
    val waveAmp = coreRad * (0.16f + beatKick * 0.18f + vocalEnergy * 0.12f)
    val waveCore = sharedWaveCorePath.apply {
        reset()
        val steps = 12
        for (i in 0..steps) {
            val frac = i / steps.toFloat()
            val wx = center3D.x - waveSpan + frac * (waveSpan * 2f)
            val env = sin(frac * PI.toFloat())
            val wy = center3D.y + sin(frac * 9.42f + time * 9.5f + p.sequenceIndex) * waveAmp * env
            if (i == 0) moveTo(wx, wy) else lineTo(wx, wy)
        }
    }
    drawPath(
        path = waveCore,
        color = Color.White.copy(alpha = (0.78f + 0.20f * beatKick) * ttlAlpha),
        style = Stroke(width = 2.4f * perspScale, cap = StrokeCap.Round)
    )

    // 5. Tilted Orbital Ring + Orbiting Spark + Specular Glass Glint
    withTransform({
        rotate(degrees = -18f + sin(time * 3.0f + p.sequenceIndex) * 14f, pivot = center3D)
    }) {
        drawOval(
            color = (if (isOnBeatNow) Color.White else effectColor).copy(alpha = (0.58f + 0.34f * beatKick) * ttlAlpha),
            topLeft = Offset(center3D.x - coreRad * 1.24f, center3D.y - coreRad * 0.38f),
            size = Size(coreRad * 2.48f, coreRad * 0.76f),
            style = Stroke(width = (2.0f + beatKick * 1.4f) * perspScale)
        )
    }

    drawPath(
        path = morphContour,
        color = (if (isOnBeatNow) Color.White else effectColor).copy(alpha = 0.96f * ttlAlpha),
        style = Stroke(width = (if (isOnBeatNow) 3.4f else 2.4f) * perspScale)
    )

    drawArc(
        color = Color.White.copy(alpha = 0.88f * ttlAlpha),
        startAngle = 198f,
        sweepAngle = 66f,
        useCenter = false,
        topLeft = Offset(center3D.x - coreRad * 0.74f, center3D.y - coreRad * 0.74f),
        size = Size(coreRad * 1.48f, coreRad * 1.48f),
        style = Stroke(width = 2.5f * perspScale, cap = StrokeCap.Round)
    )
}

@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawWormholes(
    wormholes: List<SingularityWormhole>,
    time: Float,
    beatPhase: Float,
    isBandHeroMode: Boolean = false
) {
    // Swooping slide ribbons removed in v11 — replaced by clean choreographed fluid peg formations!
}
