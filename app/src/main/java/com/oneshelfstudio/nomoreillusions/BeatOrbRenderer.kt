package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.*

fun DrawScope.drawPowerUps(
    powerUps: List<GameObject>,
    player: GameObject,
    time: Float,
    trackSec: Float = 0f,
    beatPhase: Float = 0f,
    goldenOverdriveIntensity: Float = 0f,
    multiBand: MultiBandAudioState = MultiBandAudioState()
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return
    val beatKick = if (beatPhase >= 0f) exp(-beatPhase * 5.2f).coerceIn(0f, 1f) else 0.25f
    val isGoldStage = goldenOverdriveIntensity > 0.35f

    val laneColors = listOf(
        Color(0xFF22C55EL), // Lane 1: Emerald Fret
        Color(0xFFFB7185L), // Lane 2: Crimson Fret
        Color(0xFFFACC15L), // Lane 3: Gold Fret
        Color(0xFF38BDF8L)  // Lane 4: Cyan Fret
    )

    // =========================================================================
    // 1. 3D PERSPECTIVE BAND-HERO / GUITAR-HERO RHYTHM HIGHWAY & BEAT STRIKE LINE
    // =========================================================================
    val topL = computeHighwayPoint(-0.45f, 0f, w, h)
    val topR = computeHighwayPoint(3.45f, 0f, w, h)
    val strikeL = computeHighwayPoint(-0.45f, 1.0f, w, h)
    val strikeR = computeHighwayPoint(3.45f, 1.0f, w, h)
    val botL = computeHighwayPoint(-0.45f, 1.18f, w, h)
    val botR = computeHighwayPoint(3.45f, 1.18f, w, h)

    val deckPath = Path().apply {
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

    // Highway outer side rails
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

    // Scrolling horizontal beat-measure bars synced to the quarter-note beatPhase (scrolling downward toward the Strike Line)!
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

    // Ethereal "Ooh / Aaah" Vocal Aurora Borealis Curtains & Harmonic Halos during long flowy vocal swells!
    val vocalGlow = if (multiBand.bassDropOut && multiBand.vocal >= 0.34f) {
        ((multiBand.vocal - 0.28f) * 1.6f).coerceIn(0.25f, 1f)
    } else {
        ((multiBand.vocal - 0.50f) * 2.2f).coerceIn(0f, 1f)
    }
    if (vocalGlow > 0.04f) {
        for (curtain in 0..3) {
            val auroraPath = Path()
            val baseY = h * (0.11f + curtain * 0.045f)
            auroraPath.moveTo(0f, baseY)
            for (step in 1..12) {
                val xPt = w * (step / 12f)
                val yWave = baseY + sin(time * (1.8f + curtain * 0.35f) + step * 0.65f + curtain) * (18f + 16f * vocalGlow)
                auroraPath.lineTo(xPt, yWave)
            }
            val cCol = when (curtain % 3) {
                0 -> Color(0xFF38BDF8L)
                1 -> Color(0xFFC084FCL)
                else -> Color(0xFFFDE047L)
            }
            drawPath(
                path = auroraPath,
                color = cCol.copy(alpha = (0.18f + 0.28f * vocalGlow) * (1f - curtain * 0.16f)),
                style = Stroke(width = 6f + (3 - curtain) * 4f, cap = StrokeCap.Round),
                blendMode = BlendMode.Screen
            )
        }
        drawCircle(
            color = Color(0xFFC084FCL).copy(alpha = 0.22f * vocalGlow),
            radius = w * (0.24f + 0.06f * sin(time * 2.4f)),
            center = Offset(w * 0.5f, h * 0.20f),
            style = Stroke(width = 2.6f),
            blendMode = BlendMode.Screen
        )
    }

    // 4 Perspective Fret Lane Strings
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

    // GLOWING BEAT STRIKE LINE at u = 1.0 (Raised to Y = 65% of screen height for Thumb Steering!)
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

    // 4 Guitar-Hero Fret Receptors along the Beat Strike Line
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

    drawIntoCanvas { canvas ->
        cachedSubPaint.textSize = 15.0f
        cachedSubPaint.color = android.graphics.Color.argb(185, 250, 204, 21)
        canvas.nativeCanvas.drawText(
            "★ BEAT STRIKE LINE • STEER THUMB BELOW ★",
            w * 0.5f,
            strikeL.y + 38f,
            cachedSubPaint
        )
    }

    if (powerUps.isEmpty()) return
    val sorted = if (powerUps.size >= 2) powerUps.sortedBy { it.sequenceIndex } else powerUps

    // 2. Render each Scrolling Highway Beat Note & Labeled Power-Up (BOMB / SHIELD / 4X STAR)
    sorted.forEach { p ->
        val leadDur = (p.targetBeatSec - p.spawnBeatSec).coerceAtLeast(0.45f)
        val u = ((trackSec - p.spawnBeatSec) / leadDur).coerceIn(0f, 1.25f)
        val perspScale = computeHighwayScale(u)
        val center = Offset(p.x, p.y)
        val isSupernova = p.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR
        val isBomb = p.powerUpType == PowerUpType.BLAST
        val isShield = p.powerUpType == PowerUpType.SHIELD

        val expireSec = if (p.expireBeatSec > 0f) p.expireBeatSec else (p.targetBeatSec + 0.45f)
        val ttlRemaining = (expireSec - trackSec).coerceAtLeast(0f)
        val ttlAlpha = (ttlRemaining / 0.22f).coerceIn(0.20f, 1.0f)

        val isOnStrikeLine = u in 0.86f..1.14f
        val beatPulseScale = 1.0f + (if (isOnStrikeLine) 0.28f else 0.16f) * beatKick
        val coreRad = p.radius * perspScale * beatPulseScale
        // True 3D physical vertical bounce on the beat so orbs leap toward the camera in 3D space!
        val bounceOffsetY = -beatKick * (if (isOnStrikeLine) 13f else 7.5f) * perspScale
        val center3D = Offset(center.x, center.y + bounceOffsetY)
        val lightOrigin = Offset(center3D.x - coreRad * 0.34f, center3D.y - coreRad * 0.36f)

        val laneIdx = (p.phraseBeatNum - 1).coerceIn(0, 3)
        val effectColor = when {
            isBomb -> Color(0xFFF97316L)
            isShield -> Color(0xFF38BDF8L)
            isSupernova -> Color(0xFFFACC15L)
            else -> laneColors[laneIdx]
        }

        // 0. 3D Perspective Ground Drop-Shadow on the Highway Deck underneath the floating orb
        drawOval(
            color = Color.Black.copy(alpha = 0.52f * ttlAlpha),
            topLeft = Offset(center.x - coreRad * 0.92f, center.y + coreRad * 0.42f),
            size = Size(coreRad * 1.84f, coreRad * 0.56f)
        )
        drawOval(
            color = effectColor.copy(alpha = (0.28f + 0.24f * beatKick) * ttlAlpha),
            topLeft = Offset(center.x - coreRad * 1.10f, center.y + coreRad * 0.36f),
            size = Size(coreRad * 2.20f, coreRad * 0.68f),
            style = Stroke(width = 2.2f * perspScale)
        )

        // Vertical Fret Flame Column when a note is crossing the Beat Strike Line!
        if (isOnStrikeLine) {
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.88f * ttlAlpha),
                radius = coreRad * 1.45f,
                center = center3D,
                style = Stroke(width = 3.8f)
            )
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        effectColor.copy(alpha = 0.68f * ttlAlpha),
                        Color.White.copy(alpha = 0.92f * ttlAlpha)
                    ),
                    startY = center3D.y - coreRad * 2.9f,
                    endY = center3D.y
                ),
                start = Offset(center3D.x, center3D.y - coreRad * 2.9f),
                end = center3D,
                strokeWidth = coreRad * 0.88f,
                cap = StrokeCap.Round
            )
        }

        if (isBomb) {
            // =========================================================================
            // 💥 3D VOLUMETRIC BOMB SPHERE (UNMISTAKABLE LABELED ENEMY-DESTROYER!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFACC15L).copy(alpha = 0.85f * ttlAlpha),
                        Color(0xFFF97316L).copy(alpha = 0.55f * ttlAlpha),
                        Color(0xFFEF4444L).copy(alpha = 0.22f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center3D,
                    radius = coreRad * 2.3f
                ),
                radius = coreRad * 2.3f,
                center = center3D,
                blendMode = BlendMode.Screen
            )
            for (nub in 0 until 8) {
                val ang = nub * (PI.toFloat() / 4f) + time * 2.2f
                val p1 = Offset(center3D.x + cos(ang) * (coreRad * 0.82f), center3D.y + sin(ang) * (coreRad * 0.82f))
                val p2 = Offset(center3D.x + cos(ang) * (coreRad * 1.30f), center3D.y + sin(ang) * (coreRad * 1.30f))
                drawLine(
                    color = Color(0xFFFACC15L).copy(alpha = 0.95f * ttlAlpha),
                    start = p1,
                    end = p2,
                    strokeWidth = 4.6f * perspScale,
                    cap = StrokeCap.Round
                )
            }
            // 3D Shaded Ordnance Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFEF08AL).copy(alpha = 0.98f * ttlAlpha),
                        Color(0xFFF97316L).copy(alpha = 0.96f * ttlAlpha),
                        Color(0xFF7C2D12L).copy(alpha = 0.98f * ttlAlpha),
                        Color(0xFF1F0909L).copy(alpha = 0.98f * ttlAlpha)
                    ),
                    center = lightOrigin,
                    radius = coreRad * 1.35f
                ),
                radius = coreRad,
                center = center3D
            )
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.98f * ttlAlpha),
                radius = coreRad,
                center = center3D,
                style = Stroke(width = 3.4f)
            )
            // 3D Specular Highlight Arc
            drawArc(
                color = Color.White.copy(alpha = 0.88f * ttlAlpha),
                startAngle = 195f,
                sweepAngle = 68f,
                useCenter = false,
                topLeft = Offset(center3D.x - coreRad * 0.76f, center3D.y - coreRad * 0.76f),
                size = Size(coreRad * 1.52f, coreRad * 1.52f),
                style = Stroke(width = 3.0f * perspScale, cap = StrokeCap.Round)
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (18f * perspScale).coerceAtLeast(12.5f)
                cachedSubPaint.color = android.graphics.Color.WHITE
                canvas.nativeCanvas.drawText("💥 BOMB", center3D.x, center3D.y - coreRad * 1.35f, cachedSubPaint)
            }
        } else if (isShield) {
            // =========================================================================
            // 🛡️ 3D VOLUMETRIC SHIELD ORB (PROTECTIVE BARRIER DOME!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8L).copy(alpha = 0.76f * ttlAlpha),
                        Color(0xFF0284C7L).copy(alpha = 0.28f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center3D,
                    radius = coreRad * 2.15f
                ),
                radius = coreRad * 2.15f,
                center = center3D,
                blendMode = BlendMode.Screen
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.98f * ttlAlpha),
                        Color(0xFF38BDF8L).copy(alpha = 0.95f * ttlAlpha),
                        Color(0xFF0369A1L).copy(alpha = 0.96f * ttlAlpha),
                        Color(0xFF082F49L).copy(alpha = 0.98f * ttlAlpha)
                    ),
                    center = lightOrigin,
                    radius = coreRad * 1.35f
                ),
                radius = coreRad,
                center = center3D
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.92f * ttlAlpha),
                radius = coreRad,
                center = center3D,
                style = Stroke(width = 3.2f)
            )
            drawArc(
                color = Color.White.copy(alpha = 0.92f * ttlAlpha),
                startAngle = 195f,
                sweepAngle = 72f,
                useCenter = false,
                topLeft = Offset(center3D.x - coreRad * 0.76f, center3D.y - coreRad * 0.76f),
                size = Size(coreRad * 1.52f, coreRad * 1.52f),
                style = Stroke(width = 3.0f * perspScale, cap = StrokeCap.Round)
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (18f * perspScale).coerceAtLeast(12.5f)
                cachedSubPaint.color = android.graphics.Color.rgb(186, 230, 253)
                canvas.nativeCanvas.drawText("🛡️ SHIELD", center3D.x, center3D.y - coreRad * 1.32f, cachedSubPaint)
            }
        } else if (isSupernova) {
            // =========================================================================
            // ★ 3D VOLUMETRIC 24K STAR POWER SPHERE (4X MULTIPLIER & OVERDRIVE!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.98f * ttlAlpha),
                        Color(0xFFFACC15L).copy(alpha = 0.85f * ttlAlpha),
                        Color(0xFFF97316L).copy(alpha = 0.35f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center3D,
                    radius = coreRad * 2.35f
                ),
                radius = coreRad * 2.35f,
                center = center3D,
                blendMode = BlendMode.Screen
            )
            // Orbiting 3D Gyro Ring
            withTransform({
                rotate(degrees = (time * 95f) % 360f, pivot = center3D)
            }) {
                drawOval(
                    color = Color(0xFFFEF08AL).copy(alpha = 0.88f * ttlAlpha),
                    topLeft = Offset(center3D.x - coreRad * 1.32f, center3D.y - coreRad * 0.44f),
                    size = Size(coreRad * 2.64f, coreRad * 0.88f),
                    style = Stroke(width = 2.8f * perspScale)
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.99f * ttlAlpha),
                        Color(0xFFFDE047L).copy(alpha = 0.96f * ttlAlpha),
                        Color(0xFFD97706L).copy(alpha = 0.96f * ttlAlpha),
                        Color(0xFF451A03L).copy(alpha = 0.98f * ttlAlpha)
                    ),
                    center = lightOrigin,
                    radius = coreRad * 1.35f
                ),
                radius = coreRad,
                center = center3D
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.95f * ttlAlpha),
                radius = coreRad,
                center = center3D,
                style = Stroke(width = 3.5f)
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (17.5f * perspScale).coerceAtLeast(12f)
                cachedSubPaint.color = android.graphics.Color.rgb(254, 240, 138)
                canvas.nativeCanvas.drawText("★ 4X STAR", center3D.x, center3D.y - coreRad * 1.32f, cachedSubPaint)
            }
        } else {
            // =========================================================================
            // 🎵 TRUE 3D VOLUMETRIC PULSING BEAT ORB (GLOSSY SPHERE + GYRO RING + EMBLEM)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        effectColor.copy(alpha = (0.72f + 0.22f * beatKick) * ttlAlpha),
                        effectColor.copy(alpha = 0.24f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center3D,
                    radius = coreRad * (1.85f + 0.35f * beatKick)
                ),
                radius = coreRad * (1.85f + 0.35f * beatKick),
                center = center3D,
                blendMode = BlendMode.Screen
            )

            // 3D Tilted Equatorial Beat Ring that expands & rotates with the music!
            withTransform({
                rotate(degrees = -18f + sin(time * 3.2f + p.sequenceIndex) * 14f, pivot = center3D)
            }) {
                drawOval(
                    color = (if (isOnStrikeLine) Color.White else effectColor).copy(alpha = (0.62f + 0.34f * beatKick) * ttlAlpha),
                    topLeft = Offset(center3D.x - coreRad * (1.22f + 0.14f * beatKick), center3D.y - coreRad * 0.42f),
                    size = Size(coreRad * (2.44f + 0.28f * beatKick), coreRad * 0.84f),
                    style = Stroke(width = (2.2f + beatKick * 1.6f) * perspScale)
                )
            }

            // 3D Volumetric Shaded Sphere Body (Top-Left Specular -> Mid Tone -> Deep Rim Shadow)
            val darkRim = Color(
                red = (effectColor.red * 0.18f).coerceIn(0f, 1f),
                green = (effectColor.green * 0.18f).coerceIn(0f, 1f),
                blue = (effectColor.blue * 0.22f).coerceIn(0f, 1f),
                alpha = 0.98f * ttlAlpha
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.98f * ttlAlpha),
                        effectColor.copy(alpha = 0.96f * ttlAlpha),
                        darkRim
                    ),
                    center = lightOrigin,
                    radius = coreRad * 1.38f
                ),
                radius = coreRad,
                center = center3D
            )

            // Outer 3D Chrome/Neon Rim
            drawCircle(
                color = (if (isOnStrikeLine) Color.White else effectColor).copy(alpha = 0.98f * ttlAlpha),
                radius = coreRad,
                center = center3D,
                style = Stroke(width = if (isOnStrikeLine) 4.0f else 2.8f)
            )

            // 3D Glass Specular Crescent Glint (Top-Left Arc)
            drawArc(
                color = Color.White.copy(alpha = 0.90f * ttlAlpha),
                startAngle = 195f,
                sweepAngle = 72f,
                useCenter = false,
                topLeft = Offset(center3D.x - coreRad * 0.76f, center3D.y - coreRad * 0.76f),
                size = Size(coreRad * 1.52f, coreRad * 1.52f),
                style = Stroke(width = 2.8f * perspScale, cap = StrokeCap.Round)
            )

            // Inner Pulsing Harmonic Core + Embossed Music Note Symbol
            drawCircle(
                color = Color.White.copy(alpha = (0.32f + 0.45f * beatKick) * ttlAlpha),
                radius = coreRad * (0.28f + 0.14f * beatKick),
                center = center3D
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (coreRad * 0.82f).coerceAtLeast(13f)
                cachedSubPaint.color = android.graphics.Color.argb((245 * ttlAlpha).toInt().coerceIn(0, 255), 255, 255, 255)
                canvas.nativeCanvas.drawText("♪", center3D.x, center3D.y + coreRad * 0.28f, cachedSubPaint)
            }
        }
    }
}

/**
 * V9 2-FINGER STATIONARY SUSTAIN HOLD ANCHOR PEGS & "OOH / AAAH" VOCAL AURORA SPECTACLE:
 * - Paired 3D Sustain Hold Pegs (Lane 1 & Lane 4) scroll down to the Beat Strike Line (`u = 1.0` in 1.8s)
 *   and LOCK stationary on the Beat Strike Line!
 * - Linked by a crackling 3-strand Electric/Liquid Plasma Bridge + 0% -> 100% Radial Hold Progress Ring (`w.ctrlY`).
 * - While held, concentric liquid-plasma vortex rings erupt around BOTH pegs and "Ooh / Aaah" Vocal Aurora
 *   curtains bloom across the upper stage!
 */
fun DrawScope.drawWormholes(
    wormholes: List<SingularityWormhole>,
    time: Float,
    beatPhase: Float
) {
    if (wormholes.isEmpty()) return
    val wScreen = size.width
    val hScreen = size.height
    if (wScreen <= 0f || hScreen <= 0f) return
    val beatKick = if (beatPhase >= 0f) exp(-beatPhase * 5f).coerceIn(0f, 1f) else 0.25f

    wormholes.forEach { w ->
        if (w.angle >= 90f) return@forEach
        val uAnchor = (w.ageSec / 1.8f).coerceIn(0f, 1.0f)
        if (uAnchor <= 0.02f) return@forEach

        val startLane = w.x.coerceIn(0f, 3f)
        val endLane = w.exitX.coerceIn(0f, 3f)
        val pt1 = computeHighwayPoint(startLane, uAnchor, wScreen, hScreen)
        val pt2 = computeHighwayPoint(endLane, uAnchor, wScreen, hScreen)
        val midPt = Offset((pt1.x + pt2.x) * 0.5f, (pt1.y + pt2.y) * 0.5f)
        val perspScale = computeHighwayScale(uAnchor)
        val holdProg = w.ctrlY.coerceIn(0f, 1f)
        val isBeingHeld = holdProg > 0.02f

        // 0. "OOH / AAAH" VOCAL AURORA BOREALIS CURTAINS across the upper stage while holding!
        if (isBeingHeld) {
            for (cIdx in 0..5) {
                val xFrac = 0.14f + (cIdx / 5f) * 0.72f + sin(time * 2.2f + cIdx) * 0.03f
                val cx = wScreen * xFrac
                val curtainH = hScreen * (0.26f + 0.18f * holdProg + 0.06f * sin(time * 4f + cIdx))
                val cCol = when (cIdx % 3) {
                    0 -> Color(0xFF38BDF8L)
                    1 -> Color(0xFFE879F9L)
                    else -> Color(0xFF4ADE80L)
                }
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            cCol.copy(alpha = (0.28f + 0.42f * holdProg)),
                            Color(0xFFFACC15L).copy(alpha = 0.22f * holdProg),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = curtainH
                    ),
                    start = Offset(cx, 0f),
                    end = Offset(cx, curtainH),
                    strokeWidth = 42f + 24f * holdProg,
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Screen
                )
            }
        }

        // 1. 3-Strand Intertwined Electric/Liquid Plasma Bridge connecting the two Sustain Hold Pegs!
        val strandColors = listOf(
            Color(0xFF38BDF8L), // Electric Cyan
            Color(0xFFE879F9L), // Liquid Magenta
            Color(0xFFFACC15L)  // 24K Gold Core
        )
        for (sIdx in 0..2) {
            val plasmaPath = Path()
            val segs = 22
            val freq = 3.0f + sIdx * 1.2f
            val speed = 8.5f + sIdx * 3.0f
            val amp = (12f + 16f * holdProg + 8f * beatKick) * perspScale * (if (sIdx % 2 == 0) 1f else -1f)
            for (i in 0..segs) {
                val t = i / segs.toFloat()
                val bx = pt1.x + (pt2.x - pt1.x) * t
                val env = sin(t * PI.toFloat())
                val waveY = sin(t * freq * PI.toFloat() + time * speed + sIdx * 2.1f) * amp * env
                val by = pt1.y + (pt2.y - pt1.y) * t + waveY
                if (i == 0) plasmaPath.moveTo(bx, by) else plasmaPath.lineTo(bx, by)
            }
            drawPath(
                path = plasmaPath,
                color = strandColors[sIdx].copy(alpha = 0.65f + 0.30f * holdProg),
                style = Stroke(
                    width = (if (sIdx == 2) 6.5f else 11f) * perspScale * (1f + 0.35f * holdProg),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                blendMode = BlendMode.Screen
            )
        }

        // Crackling Plasma Nodes flowing between the two pegs
        for (nIdx in 0..6) {
            val tNode = ((nIdx / 7f) + time * 0.85f) % 1f
            val nx = pt1.x + (pt2.x - pt1.x) * tNode
            val ny = pt1.y + (pt2.y - pt1.y) * tNode + sin(tNode * PI.toFloat() * 4f + time * 10f) * 8f * perspScale
            drawCircle(
                color = Color.White.copy(alpha = 0.90f),
                radius = (4.5f + 4.0f * holdProg) * perspScale,
                center = Offset(nx, ny)
            )
        }

        // 2. Render Both 3D Sustain Hold Pegs (Lane 1 & Lane 4) + Special Vortex Effect when Held!
        val pegRadius = 35f * perspScale * (1f + 0.16f * beatKick + 0.18f * holdProg)
        listOf(Pair(pt1, Color(0xFF38BDF8L)), Pair(pt2, Color(0xFFE879F9L))).forEachIndexed { idx, (pt, pegCol) ->
            // Special Concentric Liquid-Plasma Vortex Rings when held!
            if (isBeingHeld) {
                for (ring in 0..2) {
                    val vr = pegRadius * (1.35f + ring * 0.36f + 0.12f * sin(time * 9f + ring))
                    val startDeg = ((time * (if (ring % 2 == 0) 210f else -180f)) + ring * 90f) % 360f
                    drawArc(
                        color = (if (ring % 2 == 0) Color(0xFFFACC15L) else pegCol).copy(alpha = 0.85f),
                        startAngle = startDeg,
                        sweepAngle = 125f + holdProg * 95f,
                        useCenter = false,
                        topLeft = Offset(pt.x - vr, pt.y - vr),
                        size = Size(vr * 2f, vr * 2f),
                        style = Stroke(width = 4.2f * perspScale, cap = StrokeCap.Round)
                    )
                }
            }

            // Outer Volumetric Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        pegCol.copy(alpha = 0.72f),
                        Color(0xFFFACC15L).copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = pt,
                    radius = pegRadius * 2.1f
                ),
                radius = pegRadius * 2.1f,
                center = pt,
                blendMode = BlendMode.Screen
            )

            // 3D Lit Sphere Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        pegCol,
                        Color(0xFF0F172AL)
                    ),
                    center = Offset(pt.x - pegRadius * 0.32f, pt.y - pegRadius * 0.32f),
                    radius = pegRadius * 1.35f
                ),
                radius = pegRadius,
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = pegRadius,
                center = pt,
                style = Stroke(width = 3.5f * perspScale)
            )

            // 360-Degree Radial Hold Progress Gauge around each peg!
            if (holdProg > 0.01f) {
                val gaugeR = pegRadius * 1.24f
                drawArc(
                    color = Color(0xFF4ADE80L),
                    startAngle = -90f,
                    sweepAngle = holdProg * 360f,
                    useCenter = false,
                    topLeft = Offset(pt.x - gaugeR, pt.y - gaugeR),
                    size = Size(gaugeR * 2f, gaugeR * 2f),
                    style = Stroke(width = 6.0f * perspScale, cap = StrokeCap.Round)
                )
            }

            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (pegRadius * 0.72f).coerceAtLeast(12f)
                cachedSubPaint.color = android.graphics.Color.WHITE
                canvas.nativeCanvas.drawText(if (idx == 0) "L ♪" else "R ♫", pt.x, pt.y + pegRadius * 0.26f, cachedSubPaint)
            }
        }

        // 3. Clear Center Callout Banner (`⚡ HOLD 2 FINGERS • X% ⚡`)
        drawIntoCanvas { canvas ->
            cachedSubPaint.textSize = (18.5f * perspScale).coerceAtLeast(13.5f)
            cachedSubPaint.color = android.graphics.Color.rgb(254, 240, 138)
            val pct = (holdProg * 100f).toInt().coerceIn(0, 100)
            val label = if (isBeingHeld) "⚡ PLASMA HARMONY HOLD • $pct% ⚡" else "⚡ HOLD 2 FINGERS ON BEAT LINE ♪"
            canvas.nativeCanvas.drawText(label, midPt.x, midPt.y - pegRadius * 1.45f, cachedSubPaint)
        }
    }
}
