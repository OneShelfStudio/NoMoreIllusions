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
        val beatPulseScale = 1.0f + (if (isOnStrikeLine) 0.26f else 0.12f) * beatKick
        val coreRad = p.radius * perspScale * beatPulseScale

        val laneIdx = (p.phraseBeatNum - 1).coerceIn(0, 3)
        val effectColor = when {
            isBomb -> Color(0xFFF97316L)
            isShield -> Color(0xFF38BDF8L)
            isSupernova -> Color(0xFFFACC15L)
            else -> laneColors[laneIdx]
        }

        // Vertical Fret Flame Column when a note is crossing the Beat Strike Line!
        if (isOnStrikeLine) {
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.85f * ttlAlpha),
                radius = coreRad * 1.42f,
                center = center,
                style = Stroke(width = 3.5f)
            )
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        effectColor.copy(alpha = 0.65f * ttlAlpha),
                        Color.White.copy(alpha = 0.90f * ttlAlpha)
                    ),
                    startY = center.y - coreRad * 2.8f,
                    endY = center.y
                ),
                start = Offset(center.x, center.y - coreRad * 2.8f),
                end = center,
                strokeWidth = coreRad * 0.85f,
                cap = StrokeCap.Round
            )
        }

        if (isBomb) {
            // =========================================================================
            // 💥 BOMB PICKUP (UNMISTAKABLE LABELED ENEMY-DESTROYER ORDNANCE!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFACC15L).copy(alpha = 0.85f * ttlAlpha),
                        Color(0xFFF97316L).copy(alpha = 0.55f * ttlAlpha),
                        Color(0xFFEF4444L).copy(alpha = 0.22f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 2.3f
                ),
                radius = coreRad * 2.3f,
                center = center,
                blendMode = BlendMode.Screen
            )
            // 8 Ordnance Detonator Nubs around the sphere
            for (nub in 0 until 8) {
                val ang = nub * (PI.toFloat() / 4f) + time * 1.8f
                val p1 = Offset(center.x + cos(ang) * (coreRad * 0.85f), center.y + sin(ang) * (coreRad * 0.85f))
                val p2 = Offset(center.x + cos(ang) * (coreRad * 1.28f), center.y + sin(ang) * (coreRad * 1.28f))
                drawLine(
                    color = Color(0xFFFACC15L).copy(alpha = 0.95f * ttlAlpha),
                    start = p1,
                    end = p2,
                    strokeWidth = 4.2f * perspScale,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(
                color = Color(0xFF180808L).copy(alpha = 0.96f * ttlAlpha),
                radius = coreRad,
                center = center
            )
            drawCircle(
                color = Color(0xFFF97316L).copy(alpha = 0.98f * ttlAlpha),
                radius = coreRad,
                center = center,
                style = Stroke(width = 3.4f)
            )
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.92f * ttlAlpha),
                radius = coreRad * 0.45f,
                center = center
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (17f * perspScale).coerceAtLeast(12f)
                cachedSubPaint.color = android.graphics.Color.WHITE
                canvas.nativeCanvas.drawText("💥 BOMB", center.x, center.y - coreRad * 1.38f, cachedSubPaint)
            }
        } else if (isShield) {
            // =========================================================================
            // 🛡️ SHIELD PICKUP (UNMISTAKABLE LABELED PROTECTIVE DOME!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8L).copy(alpha = 0.72f * ttlAlpha),
                        Color(0xFF0284C7L).copy(alpha = 0.28f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 2.1f
                ),
                radius = coreRad * 2.1f,
                center = center,
                blendMode = BlendMode.Screen
            )
            drawCircle(
                color = Color(0xFF061528L).copy(alpha = 0.94f * ttlAlpha),
                radius = coreRad,
                center = center
            )
            drawCircle(
                color = Color(0xFF38BDF8L).copy(alpha = 0.98f * ttlAlpha),
                radius = coreRad,
                center = center,
                style = Stroke(width = 3.4f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.90f * ttlAlpha),
                radius = coreRad * 0.62f,
                center = center,
                style = Stroke(width = 2.2f)
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (17f * perspScale).coerceAtLeast(12f)
                cachedSubPaint.color = android.graphics.Color.rgb(186, 230, 253)
                canvas.nativeCanvas.drawText("🛡️ SHIELD", center.x, center.y - coreRad * 1.32f, cachedSubPaint)
            }
        } else if (isSupernova) {
            // =========================================================================
            // ★ 24K STAR POWER NOTE (4X MULTIPLIER & OVERDRIVE!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.98f * ttlAlpha),
                        Color(0xFFFACC15L).copy(alpha = 0.82f * ttlAlpha),
                        Color(0xFFF97316L).copy(alpha = 0.35f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 2.3f
                ),
                radius = coreRad * 2.3f,
                center = center,
                blendMode = BlendMode.Screen
            )
            drawCircle(
                color = Color(0xFF1C1404L).copy(alpha = 0.95f * ttlAlpha),
                radius = coreRad,
                center = center
            )
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.98f * ttlAlpha),
                radius = coreRad,
                center = center,
                style = Stroke(width = 3.6f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.95f * ttlAlpha),
                radius = coreRad * 0.46f,
                center = center
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = (16.5f * perspScale).coerceAtLeast(11.5f)
                cachedSubPaint.color = android.graphics.Color.rgb(254, 240, 138)
                canvas.nativeCanvas.drawText("★ 4X STAR", center.x, center.y - coreRad * 1.30f, cachedSubPaint)
            }
        } else {
            // =========================================================================
            // GUITAR-HERO / BAND-HERO FRET GEM BEAT NOTE
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        effectColor.copy(alpha = 0.65f * ttlAlpha),
                        effectColor.copy(alpha = 0.22f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 1.85f
                ),
                radius = coreRad * 1.85f,
                center = center,
                blendMode = BlendMode.Screen
            )
            // Outer Fret Gem Body
            drawCircle(
                color = Color(0xFF090F20L).copy(alpha = 0.95f * ttlAlpha),
                radius = coreRad,
                center = center
            )
            drawCircle(
                color = (if (isOnStrikeLine) Color.White else effectColor).copy(alpha = 0.98f * ttlAlpha),
                radius = coreRad,
                center = center,
                style = Stroke(width = if (isOnStrikeLine) 3.8f else 2.8f)
            )
            // Bright Guitar-Hero Center Cap
            drawCircle(
                color = effectColor.copy(alpha = 0.88f * ttlAlpha),
                radius = coreRad * 0.62f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.95f * ttlAlpha),
                radius = coreRad * 0.30f,
                center = center
            )
        }
    }
}

/**
 * V6 BAND-HERO / BEATSTAR SCROLLING SUSTAIN SLIDE RIBBONS:
 * Renders a glowing note head + long snaking sustain ribbon scrolling down the 4-lane perspective highway!
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
        val uHead = (w.ageSec / 2.2f)
        val uTail = (uHead - 0.44f).coerceAtLeast(0f)
        if (uHead <= 0.02f || uTail >= 1.22f) return@forEach

        val startLane = w.x.coerceIn(0f, 3f)
        val endLane = w.exitX.coerceIn(0f, 3f)
        val ribbonPath = Path()
        val steps = 14
        var headPt = Offset.Zero
        for (i in 0..steps) {
            val s = i / steps.toFloat()
            val uSample = (uTail + (uHead - uTail) * s).coerceIn(0f, 1.24f)
            val laneAtS = startLane + (endLane - startLane) * (0.5f - 0.5f * cos(s * PI.toFloat()))
            val pt = computeHighwayPoint(laneAtS, uSample, wScreen, hScreen)
            if (i == 0) ribbonPath.moveTo(pt.x, pt.y) else ribbonPath.lineTo(pt.x, pt.y)
            if (i == steps) headPt = pt
        }

        // Glowing Sustain Ribbon Tail
        drawPath(
            path = ribbonPath,
            color = Color(0xFFFACC15L).copy(alpha = 0.35f + 0.20f * beatKick),
            style = Stroke(width = 28f + beatKick * 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = ribbonPath,
            color = Color(0xFF38BDF8L).copy(alpha = 0.72f),
            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = ribbonPath,
            color = Color.White.copy(alpha = 0.92f),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Sustain Note Head
        val headScale = computeHighwayScale(uHead.coerceIn(0f, 1.2f))
        val headR = 26f * headScale * (1f + 0.15f * beatKick)
        drawCircle(
            color = Color(0xFFFACC15L).copy(alpha = 0.95f),
            radius = headR,
            center = headPt
        )
        drawCircle(
            color = Color.White,
            radius = headR * 0.52f,
            center = headPt
        )
        drawIntoCanvas { canvas ->
            cachedSubPaint.textSize = (16f * headScale).coerceAtLeast(12f)
            cachedSubPaint.color = android.graphics.Color.rgb(254, 240, 138)
            canvas.nativeCanvas.drawText("HOLD & SLIDE ♪", headPt.x, headPt.y - headR * 1.35f, cachedSubPaint)
        }
    }
}
