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
    multiBand: MultiBandAudioState = MultiBandAudioState(),
    isBandHeroMode: Boolean = false
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

    if (isBandHeroMode) {
        // =========================================================================
        // 1A. BAND HERO ALTERNATE MODE: 3D PERSPECTIVE 4-LANE HIGHWAY & STRIKE LINE
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

        drawIntoCanvas { canvas ->
            cachedSubPaint.textSize = 15.0f
            cachedSubPaint.color = android.graphics.Color.argb(185, 250, 204, 21)
            canvas.nativeCanvas.drawText(
                "🎸 BAND HERO HIGHWAY • STEER THUMB BELOW ★",
                w * 0.5f,
                strikeL.y + 38f,
                cachedSubPaint
            )
        }
    }

    if (powerUps.isEmpty()) return
    val sorted = if (powerUps.size >= 2) powerUps.sortedBy { it.sequenceIndex } else powerUps

    // 2. Render each standalone 3D Volumetric Pulsing Beat Orb & Labeled Power-Up (Zero clutter!)
    sorted.forEach { p ->
        val leadDur = (p.targetBeatSec - p.spawnBeatSec).coerceAtLeast(0.45f)
        val u = ((trackSec - p.spawnBeatSec) / leadDur).coerceIn(0f, 1.25f)
        val perspScale = if (isBandHeroMode) computeHighwayScale(u) else 1.0f
        val center = Offset(p.x, p.y)
        val isSupernova = p.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR
        val isBomb = p.powerUpType == PowerUpType.BLAST
        val isShield = p.powerUpType == PowerUpType.SHIELD

        val expireSec = if (p.expireBeatSec > 0f) p.expireBeatSec else (p.targetBeatSec + 0.45f)
        val ttlRemaining = (expireSec - trackSec).coerceAtLeast(0f)
        val ttlAlpha = (ttlRemaining / 0.35f).coerceIn(0.20f, 1.0f)

        val isOnBeatNow = if (isBandHeroMode) (u in 0.86f..1.14f) else (beatPhase in 0f..0.24f || beatPhase >= 0.84f)
        val beatPulseScale = 1.0f + (if (isOnBeatNow) 0.18f else 0.10f) * beatKick
        val coreRad = p.radius * perspScale * beatPulseScale
        val bounceOffsetY = -beatKick * (if (isOnBeatNow) 7f else 4f) * perspScale
        val center3D = Offset(center.x, center.y + bounceOffsetY)
        val lightOrigin = Offset(center3D.x - coreRad * 0.34f, center3D.y - coreRad * 0.36f)

        val laneIdx = (p.phraseBeatNum - 1).coerceIn(0, 3)
        val effectColor = when {
            isBomb -> Color(0xFFF97316L)
            isShield -> Color(0xFF38BDF8L)
            isSupernova -> Color(0xFFFACC15L)
            else -> laneColors[laneIdx]
        }

        // Subtle, clean contracting beat ring in Original mode (zero giant vertical flame columns!)
        if (!isBandHeroMode && !isBomb && !isShield) {
            val beatRingFrac = if (beatPhase >= 0f) (1f - beatPhase.coerceIn(0f, 1f)) else 0.5f
            val beatRingR = coreRad * (1.06f + beatRingFrac * 0.50f)
            drawCircle(
                color = (if (isOnBeatNow) Color.White else effectColor).copy(alpha = (0.28f + 0.38f * beatKick) * ttlAlpha),
                radius = beatRingR,
                center = center3D,
                style = Stroke(width = if (isOnBeatNow) 2.6f else 1.5f)
            )
        } else if (isBandHeroMode && isOnBeatNow) {
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.82f * ttlAlpha),
                radius = coreRad * 1.38f,
                center = center3D,
                style = Stroke(width = 3.2f)
            )
        }

        // Subtle soft ground shadow oval
        drawOval(
            color = Color.Black.copy(alpha = 0.42f * ttlAlpha),
            topLeft = Offset(center.x - coreRad * 0.84f, center.y + coreRad * 0.42f),
            size = Size(coreRad * 1.68f, coreRad * 0.48f)
        )

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
                    color = (if (isOnBeatNow) Color.White else effectColor).copy(alpha = (0.62f + 0.34f * beatKick) * ttlAlpha),
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
                color = (if (isOnBeatNow) Color.White else effectColor).copy(alpha = 0.98f * ttlAlpha),
                radius = coreRad,
                center = center3D,
                style = Stroke(width = if (isOnBeatNow) 4.0f else 2.8f)
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
@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawWormholes(
    wormholes: List<SingularityWormhole>,
    time: Float,
    beatPhase: Float,
    isBandHeroMode: Boolean = false
) {
    // Swooping slide ribbons removed in v11 — replaced by clean choreographed fluid peg formations!
}
