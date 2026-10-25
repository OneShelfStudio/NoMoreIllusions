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

    // Scrolling horizontal beat-measure bars synced to the quarter-note beatPhase!
    val phaseFrac = if (beatPhase >= 0f) (1f - beatPhase.coerceIn(0f, 1f)) else ((time * 2f) % 1f)
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
 * V8 BAND-HERO / BEATSTAR SCROLLING SUSTAIN SLIDE RIBBONS:
 * Slower (`3.6s` travel), ultra-vibrant neon path with animated directional pearls (`»`)
 * and clear `SLIDE HERE ♪` -> `FINISH ★` endpoints!
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
        val uHead = (w.ageSec / 3.6f)
        val uTail = (uHead - 0.52f).coerceAtLeast(0f)
        if (uHead <= 0.02f || uTail >= 1.22f) return@forEach

        val startLane = w.x.coerceIn(0f, 3f)
        val endLane = w.exitX.coerceIn(0f, 3f)
        val ribbonPath = Path()
        val steps = 18
        var headPt = Offset.Zero
        var tailPt = Offset.Zero
        val samplePts = ArrayList<Offset>(steps + 1)

        for (i in 0..steps) {
            val s = i / steps.toFloat()
            val uSample = (uTail + (uHead - uTail) * s).coerceIn(0f, 1.24f)
            val laneAtS = startLane + (endLane - startLane) * (0.5f - 0.5f * cos(s * PI.toFloat()))
            val pt = computeHighwayPoint(laneAtS, uSample, wScreen, hScreen)
            samplePts.add(pt)
            if (i == 0) {
                tailPt = pt
                ribbonPath.moveTo(pt.x, pt.y)
            } else {
                ribbonPath.lineTo(pt.x, pt.y)
            }
            if (i == steps) headPt = pt
        }

        // 1. Ultra-Vibrant Multi-Layered Neon Slide Highway Ribbon
        drawPath(
            path = ribbonPath,
            color = Color(0xFFF43F5EL).copy(alpha = 0.45f + 0.25f * beatKick),
            style = Stroke(width = 44f + beatKick * 10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = ribbonPath,
            color = Color(0xFFFACC15L).copy(alpha = 0.82f),
            style = Stroke(width = 24f + beatKick * 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = ribbonPath,
            color = Color(0xFF22D3EE).copy(alpha = 0.92f),
            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = ribbonPath,
            color = Color.White,
            style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Animated Directional Guide Pearls flowing along the ribbon so the player sees the exact swipe path!
        val flowShift = (time * 1.8f) % 1f
        for (pIdx in 0..5) {
            val frac = ((pIdx / 6f) + flowShift * 0.16f).coerceIn(0.08f, 0.92f)
            val sFloat = (1f - frac) * steps // Flow from Head (s=1) toward Tail (s=0)
            val idx0 = sFloat.toInt().coerceIn(0, steps - 1)
            val idx1 = (idx0 + 1).coerceAtMost(steps)
            val localT = sFloat - idx0
            val p0 = samplePts[idx0]
            val p1 = samplePts[idx1]
            val pearlPt = Offset(p0.x + (p1.x - p0.x) * localT, p0.y + (p1.y - p0.y) * localT)
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.92f),
                radius = 9.5f,
                center = pearlPt
            )
            drawCircle(
                color = Color.White,
                radius = 5.2f,
                center = pearlPt
            )
        }

        // 3. Tail Finish Ring (`FINISH ★`)
        val tailScale = computeHighwayScale(uTail.coerceIn(0f, 1.2f))
        val tailR = 26f * tailScale
        drawCircle(
            color = Color(0xFF4ADE80L).copy(alpha = 0.88f),
            radius = tailR,
            center = tailPt
        )
        drawCircle(
            color = Color.White,
            radius = tailR,
            center = tailPt,
            style = Stroke(width = 3.2f)
        )

        // 4. Giant 3D Sustain Note Head (`SLIDE HERE ♪`)
        val headScale = computeHighwayScale(uHead.coerceIn(0f, 1.2f))
        val headR = 34f * headScale * (1f + 0.18f * beatKick)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFFACC15L),
                    Color(0xFFF97316L)
                ),
                center = Offset(headPt.x - headR * 0.3f, headPt.y - headR * 0.3f),
                radius = headR * 1.3f
            ),
            radius = headR,
            center = headPt
        )
        drawCircle(
            color = Color.White,
            radius = headR,
            center = headPt,
            style = Stroke(width = 3.8f)
        )
        drawIntoCanvas { canvas ->
            cachedSubPaint.textSize = (18.5f * headScale).coerceAtLeast(13.5f)
            cachedSubPaint.color = android.graphics.Color.rgb(254, 240, 138)
            canvas.nativeCanvas.drawText("SLIDE & FOLLOW ♪ »", headPt.x, headPt.y - headR * 1.35f, cachedSubPaint)
        }
    }
}
