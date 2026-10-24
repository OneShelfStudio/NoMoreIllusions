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
    if (powerUps.isEmpty()) return
    val sorted = if (powerUps.size >= 2) powerUps.sortedBy { it.sequenceIndex } else powerUps
    val beatKick = if (beatPhase >= 0f) exp(-beatPhase * 5.2f).coerceIn(0f, 1f) else 0.25f

    // 1. Organic Orbital Compass Arc + Curved Stardust Trail toward the #1 active target orb (Zero straight ruler lines!)
    val primaryTarget = sorted.first()
    val isPrimarySupernova = primaryTarget.keyWordEffect != KeyWordEffect.NONE
    val guideColor = if (isPrimarySupernova) Color(0xFFFACC15L) else Color(0xFF22D3EEL)
    val gdx = primaryTarget.x - player.x
    val gdy = primaryTarget.y - player.y
    val gDist = hypot(gdx, gdy)
    if (gDist > 42f) {
        val dirRad = atan2(gdy, gdx)
        val dirDeg = (dirRad * 180f / PI.toFloat())
        val compassR = player.radius * 2.95f
        // Glowing directional compass arc hugging the player's halo
        drawArc(
            color = guideColor.copy(alpha = 0.58f + beatKick * 0.28f),
            startAngle = dirDeg - 26f,
            sweepAngle = 52f,
            useCenter = false,
            topLeft = Offset(player.x - compassR, player.y - compassR),
            size = Size(compassR * 2f, compassR * 2f),
            style = Stroke(width = if (isPrimarySupernova) 3.4f else 2.4f, cap = StrokeCap.Round)
        )
        // Curved stardust wisps drifting along a sine arc toward the target orb
        val nx = gdx / gDist
        val ny = gdy / gDist
        val perpX = -ny
        val perpY = nx
        val wispCount = 5
        for (wIdx in 0 until wispCount) {
            val t = ((time * 1.35f + wIdx / wispCount.toFloat()) % 1f)
            val curveOffset = sin(t * PI.toFloat()) * 22f * sin(time * 2.4f + wIdx)
            val wx = player.x + gdx * t + perpX * curveOffset
            val wy = player.y + gdy * t + perpY * curveOffset
            val wAlpha = sin(t * PI.toFloat()).coerceIn(0f, 1f) * (if (isPrimarySupernova) 0.72f else 0.50f)
            drawCircle(
                color = guideColor.copy(alpha = wAlpha),
                radius = (if (isPrimarySupernova) 4.2f else 3.0f) * (0.6f + 0.4f * (1f - t)),
                center = Offset(wx, wy)
            )
        }
    }

    // 2. Sequential Rhythm Silk Stream (smooth curved quadratic Bezier ribbon + drifting pearls connecting 1 -> 2 -> 3 -> 4!)
    if (sorted.size >= 2) {
        for (cIdx in 0 until sorted.size - 1) {
            val pA = sorted[cIdx]
            val pB = sorted[cIdx + 1]
            val dx = pB.x - pA.x
            val dy = pB.y - pA.y
            val dist = hypot(dx, dy).coerceAtLeast(1f)
            val perpX = -dy / dist
            val perpY = dx / dist
            val bend = sin(time * 1.6f + cIdx * 1.4f) * (dist * 0.18f).coerceIn(14f, 44f)
            val ctrlX = (pA.x + pB.x) * 0.5f + perpX * bend
            val ctrlY = (pA.y + pB.y) * 0.5f + perpY * bend
            val linkAlpha = (0.48f - cIdx * 0.12f).coerceIn(0.16f, 0.50f)

            val streamPath = Path().apply {
                moveTo(pA.x, pA.y)
                quadraticTo(ctrlX, ctrlY, pB.x, pB.y)
            }
            drawPath(
                path = streamPath,
                color = Color(0xFF38BDF8L).copy(alpha = linkAlpha * 0.28f),
                style = Stroke(width = 8.5f, cap = StrokeCap.Round)
            )
            drawPath(
                path = streamPath,
                color = Color(0xFF38BDF8L).copy(alpha = linkAlpha * 0.65f),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )

            // Flowing beat-pulse pearls gliding along the curved quadratic Bezier stream from pA -> pB
            for (step in 1..3) {
                val mt = ((step / 4f) + time * 0.85f) % 1f
                val inv = 1f - mt
                val mx = inv * inv * pA.x + 2f * inv * mt * ctrlX + mt * mt * pB.x
                val my = inv * inv * pA.y + 2f * inv * mt * ctrlY + mt * mt * pB.y
                drawCircle(
                    color = Color(0xFFFACC15L).copy(alpha = linkAlpha * 0.90f),
                    radius = if (cIdx == 0) 3.4f else 2.4f,
                    center = Offset(mx, my)
                )
            }
        }
    }

    // 3. Render each Stationary Beat Node with Shrinking TTL Approach Ring & Phrase Badge (1 -> 2 -> 3 -> 4)
    sorted.forEachIndexed { idx, p ->
        val center = Offset(p.x, p.y)
        val isSupernova = p.keyWordEffect != KeyWordEffect.NONE
        val isPrimary = idx == 0

        // Materialization pop-in scale when spawning ahead of the beat
        val spawnAge = (trackSec - p.spawnBeatSec).coerceAtLeast(0f)
        val popInScale = (spawnAge / 0.22f).coerceIn(0.45f, 1.0f)

        // Expire fade-out when approaching TTL deadline
        val expireSec = if (p.expireBeatSec > 0f) p.expireBeatSec else (p.targetBeatSec + 0.55f)
        val ttlRemaining = (expireSec - trackSec).coerceAtLeast(0f)
        val ttlAlpha = (ttlRemaining / 0.28f).coerceIn(0.15f, 1.0f)

        // Quarter-note beat pulse + sub-bass swell
        val beatPulseScale = 1.0f + (if (isSupernova) 0.32f else 0.22f) * beatKick + multiBand.bass * 0.12f
        val coreRad = p.radius * popInScale * beatPulseScale * (if (isPrimary) 1.08f else 0.90f)

        val effectColor = when {
            p.keyWordEffect != KeyWordEffect.NONE -> when (p.keyWordEffect) {
                KeyWordEffect.TRACTOR_REACH -> Color(0xFF22D3EEL)
                KeyWordEffect.PRISM_SHATTER -> Color(0xFFE879F9L)
                KeyWordEffect.MAGNETAR_WAR -> Color(0xFFFB7185L)
                KeyWordEffect.CONSTELLATION_STAR -> Color(0xFFFACC15L)
                KeyWordEffect.BINARY_HEART -> Color(0xFF4ADE80L)
                KeyWordEffect.ECLIPSE_DARK -> Color(0xFFFDE047L)
                KeyWordEffect.NONE -> Color(0xFF06B6D4L)
            }
            p.powerUpType == PowerUpType.BLAST -> Color(0xFFF97316L)
            p.powerUpType == PowerUpType.SHIELD -> Color(0xFF38BDF8L)
            p.powerUpType == PowerUpType.SLOW_MO -> Color(0xFFA855F7L)
            p.powerUpType == PowerUpType.MAGNET -> Color(0xFF10B981L)
            else -> Color(0xFF06B6D4L)
        }

        // Osu!-Style Shrinking Time-To-Live (TTL) Approach Ring:
        // Contracts smoothly from 2.45x radius down to 1.02x radius right as trackSec hits p.targetBeatSec!
        val leadWindow = (p.targetBeatSec - p.spawnBeatSec).coerceAtLeast(0.45f)
        val timeToBeat = p.targetBeatSec - trackSec
        val approachRatio = (timeToBeat / leadWindow).coerceIn(0f, 1f)
        val isOnBeatWindow = abs(timeToBeat) <= 0.24f
        val isLateGrace = timeToBeat < -0.24f
        val ringRadius = coreRad * (1.03f + approachRatio * (if (isSupernova) 1.65f else 1.42f))
        val ringCol = when {
            isOnBeatWindow -> Color(0xFF4ADE80L) // Blazing Emerald-Gold when on-beat!
            isLateGrace -> Color(0xFFFB7185L)    // Coral warning when TTL is about to expire
            else -> effectColor
        }

        drawCircle(
            color = ringCol.copy(
                alpha = (if (isOnBeatWindow) 0.98f else (if (isSupernova) 0.85f else 0.68f)) * ttlAlpha
            ),
            radius = ringRadius,
            center = center,
            style = Stroke(width = if (isOnBeatWindow) 4.2f + beatKick * 1.8f else 2.5f)
        )
        if (isOnBeatWindow) {
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.72f * ttlAlpha),
                radius = coreRad * 1.18f,
                center = center,
                style = Stroke(width = 2.0f)
            )
        }

        if (isSupernova) {
            // =========================================================================
            // SPECIAL BEAT-DROP SUPERNOVA ORB (24K GOLD & PRISMATIC GYROSCOPE STAR!)
            // =========================================================================
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.52f + beatKick * 0.35f),
                radius = ringRadius * 1.18f,
                center = center,
                style = Stroke(width = 1.8f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.98f),
                        effectColor.copy(alpha = 0.82f),
                        Color(0xFFFACC15L).copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 2.5f
                ),
                radius = coreRad * 2.5f,
                center = center,
                blendMode = BlendMode.Screen
            )

            // Smooth Counter-Rotating Celestial Crescent Halo Arcs & Orbiting Gold Pearls (Zero straight spikes or boxy squares!)
            for (h in 0..1) {
                val hRad = coreRad * (if (h == 0) 1.05f else 0.76f)
                val hCol = if (h == 0) Color(0xFFFACC15L) else effectColor
                val startDeg = ((time * (if (h == 0) 130f else -155f)) % 360f)
                for (a in 0..1) {
                    drawArc(
                        color = hCol.copy(alpha = 0.92f * ttlAlpha),
                        startAngle = startDeg + a * 180f,
                        sweepAngle = 112f,
                        useCenter = false,
                        topLeft = Offset(p.x - hRad, p.y - hRad),
                        size = Size(hRad * 2f, hRad * 2f),
                        style = Stroke(width = if (h == 0) 3.0f else 2.4f, cap = StrokeCap.Round)
                    )
                }
            }
            for (orbIdx in 0 until 6) {
                val ang = (orbIdx * PI.toFloat() / 3f) + time * 1.4f
                val orbitR = coreRad * (1.36f + 0.18f * beatKick)
                val ox = p.x + cos(ang) * orbitR
                val oy = p.y + sin(ang) * orbitR
                drawCircle(
                    color = (if (orbIdx % 2 == 0) Color(0xFFFFFBEBL) else effectColor).copy(alpha = 0.88f * ttlAlpha),
                    radius = if (orbIdx % 2 == 0) 3.4f else 2.4f,
                    center = Offset(ox, oy)
                )
            }

            drawCircle(
                color = Color(0xFFFACC15L),
                radius = coreRad * 0.48f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = coreRad * 0.28f,
                center = center
            )
        } else {
            // =========================================================================
            // SMOOTH SPHERICAL LUMINOUS GLASS / PLASMA ORB (ZERO STRAIGHT POLYGON LINES!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        effectColor.copy(alpha = 0.56f * ttlAlpha),
                        effectColor.copy(alpha = 0.20f * ttlAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 1.95f
                ),
                radius = coreRad * 1.95f,
                center = center,
                blendMode = BlendMode.Screen
            )

            // Smooth Spherical Glass Body with Volumetric Off-Center Radial Shading
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        effectColor.copy(alpha = 0.42f * ttlAlpha),
                        Color(0xFF0A1428L).copy(alpha = 0.94f * ttlAlpha),
                        Color(0xFF040812L).copy(alpha = 0.96f * ttlAlpha)
                    ),
                    center = Offset(p.x - coreRad * 0.25f, p.y - coreRad * 0.25f),
                    radius = coreRad * 1.15f
                ),
                radius = coreRad * 0.96f,
                center = center
            )
            drawCircle(
                color = (if (isOnBeatWindow) Color(0xFFFACC15L) else effectColor).copy(alpha = 0.94f * ttlAlpha),
                radius = coreRad * 0.96f,
                center = center,
                style = Stroke(width = 2.6f)
            )

            // Soft Curved Upper-Left Specular Glass Crescent Highlight
            val specRad = coreRad * 0.74f
            drawArc(
                color = Color.White.copy(alpha = 0.68f * ttlAlpha),
                startAngle = 200f,
                sweepAngle = 72f,
                useCenter = false,
                topLeft = Offset(p.x - specRad, p.y - specRad),
                size = Size(specRad * 2f, specRad * 2f),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )

            // Inner Luminous Ring
            drawCircle(
                color = effectColor.copy(alpha = 0.42f * ttlAlpha),
                radius = coreRad * 0.60f,
                center = center,
                style = Stroke(width = 1.4f)
            )

            // Smooth Curved Core Emblem (Nova Rings, Shield Halo, Chrono Rings, Magnet Arc, or Phrase Number 1..4)
            when (p.powerUpType) {
                PowerUpType.BLAST -> {
                    drawCircle(
                        color = Color(0xFFFDE047L).copy(alpha = 0.85f * ttlAlpha),
                        radius = coreRad * 0.44f,
                        center = center,
                        style = Stroke(width = 2.2f)
                    )
                    drawCircle(color = Color.White, radius = coreRad * 0.24f, center = center)
                }
                PowerUpType.SHIELD -> {
                    val sh = coreRad * 0.42f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.92f * ttlAlpha),
                        radius = sh,
                        center = center,
                        style = Stroke(width = 2.4f)
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8L).copy(alpha = 0.65f * ttlAlpha),
                        radius = sh * 0.55f,
                        center = center
                    )
                }
                PowerUpType.SLOW_MO -> {
                    val hg = coreRad * 0.38f
                    drawArc(
                        color = Color.White,
                        startAngle = (time * 90f) % 360f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = Offset(p.x - hg, p.y - hg),
                        size = Size(hg * 2f, hg * 2f),
                        style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                    )
                    drawCircle(color = Color.White, radius = coreRad * 0.14f, center = center)
                }
                PowerUpType.MAGNET -> {
                    val mr = coreRad * 0.38f
                    drawArc(
                        color = Color.White,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(p.x - mr, p.y - mr * 0.6f),
                        size = Size(mr * 2f, mr * 2f),
                        style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                    )
                }
                else -> {
                    val phraseNum = ((p.sequenceIndex - 1).coerceAtLeast(0) % 4) + 1
                    drawIntoCanvas { canvas ->
                        cachedSubPaint.textSize = coreRad * 0.92f
                        cachedSubPaint.color = android.graphics.Color.WHITE
                        canvas.nativeCanvas.drawText(
                            phraseNum.toString(),
                            p.x,
                            p.y + coreRad * 0.32f,
                            cachedSubPaint
                        )
                    }
                }
            }
        }
    }
}

/**
 * V4 BEAT-LOCKED PERIMETER ENEMY WARNING TELEGRAPHS:
 * Renders a high-contrast Crimson Warning Diamond (◆) on the screen border + a Crimson Laser
 * Trajectory Sightline 2 beats (~0.92s) before an enemy enters the playfield, eliminating random/unfair spawns!
 */

fun DrawScope.drawWormholes(
    wormholes: List<SingularityWormhole>,
    time: Float,
    beatPhase: Float
) {
    if (wormholes.isEmpty()) return
    val beatKick = if (beatPhase >= 0f) exp(-beatPhase * 5f).coerceIn(0f, 1f) else 0.25f

    wormholes.forEach { w ->
        val a = w.alphaProgress
        if (a <= 0.02f) return@forEach

        val ax = w.x
        val ay = w.y
        val bx = if (w.exitX > 0f) w.exitX else (w.x + 180f)
        val by = if (w.exitY > 0f) w.exitY else (w.y - 140f)
        val cx = if (w.ctrlX > 0f) w.ctrlX else ((ax + bx) * 0.5f + 55f)
        val cy = if (w.ctrlY > 0f) w.ctrlY else ((ay + by) * 0.5f - 45f)

        // 1. Curved Bezier Musical Light-Ribbon connecting Entry Portal A -> Exit Portal B
        val ribbonPath = Path().apply {
            moveTo(ax, ay)
            quadraticTo(cx, cy, bx, by)
        }
        drawPath(
            path = ribbonPath,
            color = Color(0xFF38BDF8L).copy(alpha = a * (0.28f + 0.18f * beatKick)),
            style = Stroke(width = 22f + beatKick * 6f, cap = StrokeCap.Round)
        )
        drawPath(
            path = ribbonPath,
            color = Color(0xFFE879F9L).copy(alpha = a * 0.55f),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
        drawPath(
            path = ribbonPath,
            color = Color.White.copy(alpha = a * 0.85f),
            style = Stroke(width = 2.6f, cap = StrokeCap.Round)
        )

        // 2. 5 Pulsing Mini Rhythm Pearls + Flowing Wave Particles along the Slide Stream
        for (i in 1..5) {
            val t = i / 6f
            val inv = 1f - t
            val px = inv * inv * ax + 2f * inv * t * cx + t * t * bx
            val py = inv * inv * ay + 2f * inv * t * cy + t * t * by
            val pearlR = 6.5f + 2.5f * beatKick
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = a * 0.88f),
                radius = pearlR,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color.White.copy(alpha = a * 0.96f),
                radius = pearlR * 0.48f,
                center = Offset(px, py)
            )
        }
        for (m in 0 until 4) {
            val mt = ((time * 1.15f + m * 0.25f) % 1f)
            val inv = 1f - mt
            val mx = inv * inv * ax + 2f * inv * mt * cx + mt * mt * bx
            val my = inv * inv * ay + 2f * inv * mt * cy + mt * mt * by
            drawCircle(
                color = Color(0xFF38BDF8L).copy(alpha = a * sin(mt * PI.toFloat()).coerceIn(0f, 1f) * 0.85f),
                radius = 5.2f,
                center = Offset(mx, my)
            )
        }

        // 3. Render Entry Portal A (Cyan/Gold "SLIDE") & Exit Portal B (Magenta/Gold "EXIT")
        val portals = listOf(
            Triple(Offset(ax, ay), Color(0xFF38BDF8L), "SLIDE ♪"),
            Triple(Offset(bx, by), Color(0xFFE879F9L), "EXIT ★")
        )
        portals.forEachIndexed { pIdx, (pCenter, pCol, label) ->
            val r = w.radius * (0.88f + 0.14f * beatKick)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF040210L).copy(alpha = a * 0.94f),
                        pCol.copy(alpha = a * 0.68f),
                        Color(0xFFFACC15L).copy(alpha = a * 0.35f),
                        Color.Transparent
                    ),
                    center = pCenter,
                    radius = r * 1.95f
                ),
                radius = r * 1.95f,
                center = pCenter
            )
            for (ring in 0..2) {
                val rr = r * (0.55f + ring * 0.30f)
                val startDeg = ((time * (180f - ring * 50f) * (if (ring % 2 == 0) 1f else -1f)) % 360f)
                drawArc(
                    color = (if (ring % 2 == 0) pCol else Color(0xFFFACC15L)).copy(alpha = a * (0.88f - ring * 0.18f)),
                    startAngle = startDeg,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(pCenter.x - rr, pCenter.y - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(width = 3.5f - ring * 0.6f, cap = StrokeCap.Round)
                )
            }
            drawCircle(
                color = Color.White.copy(alpha = a * 0.92f),
                radius = r * 0.36f,
                center = pCenter,
                style = Stroke(width = 2.2f)
            )
            drawIntoCanvas { canvas ->
                cachedSubPaint.textSize = 18f
                cachedSubPaint.color = android.graphics.Color.argb(
                    (a * 240).toInt().coerceIn(0, 255),
                    if (pIdx == 0) 250 else 255,
                    if (pIdx == 0) 204 else 255,
                    if (pIdx == 0) 21 else 255
                )
                canvas.nativeCanvas.drawText(label, pCenter.x, pCenter.y - r * 1.15f, cachedSubPaint)
            }
        }
    }
}
