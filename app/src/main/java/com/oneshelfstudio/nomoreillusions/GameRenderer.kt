package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.*

internal val cachedBannerPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
}

internal val cachedSubPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
}

private val sharedTelegraphDiamond = Path()
private val sharedEnemyChevron = Path()

fun DrawScope.drawMotes(motes: List<GameObject>, isGoldenOverdrive: Boolean = false) {
    motes.forEachIndexed { idx, m ->
        val c = if (isGoldenOverdrive) Color(0xFFFACC15L) else m.color
        if (idx % 4 == 0) {
            drawCircle(
                color = c.copy(alpha = if (isGoldenOverdrive) 0.18f else 0.12f),
                radius = m.radius * 2.4f,
                center = Offset(m.x, m.y)
            )
        }
        drawCircle(
            color = c.copy(alpha = if (isGoldenOverdrive) 0.45f else 0.28f),
            radius = m.radius,
            center = Offset(m.x, m.y)
        )
    }
}

private val sharedTrailPath = Path()

fun DrawScope.drawPlayerTrail(
    trail: List<Offset>,
    color: Color,
    isFocus: Boolean,
    applyCB: (Color) -> Color,
    isGoldenOverdrive: Boolean = false
) {
    if (trail.size < 2) return
    val baseColor = when {
        isGoldenOverdrive -> Color(0xFFFACC15L)
        isFocus -> applyCB(Color(0xFF4ADE80L))
        else -> applyCB(color)
    }
    for (i in 0 until trail.size - 1) {
        val progress = i.toFloat() / trail.size
        val p0 = trail[i]
        val p1 = trail[i + 1]
        val pNext = trail[(i + 2).coerceAtMost(trail.lastIndex)]
        val mid1 = Offset((p0.x + p1.x) * 0.5f, (p0.y + p1.y) * 0.5f)
        val mid2 = Offset((p1.x + pNext.x) * 0.5f, (p1.y + pNext.y) * 0.5f)
        val segPath = sharedTrailPath.apply {
            reset()
            if (i == 0) {
                moveTo(p0.x, p0.y)
                quadraticTo(p1.x, p1.y, mid2.x, mid2.y)
            } else {
                moveTo(mid1.x, mid1.y)
                quadraticTo(p1.x, p1.y, mid2.x, mid2.y)
            }
        }
        drawPath(
            path = segPath,
            color = baseColor.copy(alpha = progress * 0.42f),
            style = Stroke(width = (progress * 18f).coerceAtLeast(1.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        if (i % 3 == 0) {
            drawCircle(
                color = Color.White.copy(alpha = progress * 0.36f),
                radius = (progress * 4.5f).coerceAtLeast(1.2f),
                center = p1
            )
        }
    }
}

fun DrawScope.drawShockwaves(shockwaves: List<Shockwave>, applyCB: (Color) -> Color) {
    shockwaves.forEach { sw ->
        drawCircle(
            color = applyCB(sw.color).copy(alpha = (sw.life * 0.58f).coerceIn(0f, 1f)),
            radius = sw.radius,
            center = Offset(sw.x, sw.y),
            style = Stroke(width = 3.6f * sw.life)
        )
    }
}

/**
 * Renders Orbital Drift's Tethered Satellite Ring around the hero + the active Supernova Phenomenon!
 */
fun DrawScope.drawOrbitalPhenomenonAndSatellites(
    player: GameObject,
    satellites: List<TetheredSatellite>,
    powerUps: List<GameObject>,
    activePhenomenon: ActivePhenomenon?,
    time: Float
) {
    val cx = player.x
    val cy = player.y

    // 1. Draw Tethered Orbital Satellites around the Hero (soft orbital ring + glowing pearls, zero rigid spokes!)
    if (satellites.isNotEmpty()) {
        drawCircle(
            color = Color(0xFF38BDF8L).copy(alpha = 0.14f),
            radius = 36f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f)
        )
        for (i in satellites.indices) {
            val sat = satellites[i]
            drawCircle(
                color = sat.color.copy(alpha = 0.36f),
                radius = sat.size * 2.3f,
                center = Offset(sat.x, sat.y)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = sat.size * 0.68f,
                center = Offset(sat.x, sat.y)
            )
        }
    }

    // 2. Render Active Supernova Phenomenon
    val phenom = activePhenomenon ?: return
    if (phenom.timer <= 0f || phenom.effect == KeyWordEffect.NONE) return
    val lifeAlpha = (phenom.timer / phenom.maxDuration).coerceIn(0f, 1f)

    when (phenom.effect) {
        KeyWordEffect.TRACTOR_REACH -> {
            val reachRad = 320f
            drawCircle(
                color = Color(0xFF22D3EEL).copy(alpha = lifeAlpha * 0.42f),
                radius = reachRad,
                center = Offset(cx, cy),
                style = Stroke(width = 1.8f)
            )
            val inwardR = ((phenom.timer * 190f) % reachRad)
            drawCircle(
                color = Color(0xFFA855F7L).copy(alpha = lifeAlpha * 0.35f),
                radius = inwardR,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
            powerUps.forEachIndexed { idx, orb ->
                for (step in 0..2) {
                    val synT = ((time * 1.6f + idx * 0.23f + step * 0.33f) % 1f)
                    val curve = sin(synT * PI.toFloat()) * 16f
                    val sx = orb.x + (cx - orb.x) * synT
                    val sy = orb.y + (cy - orb.y) * synT - curve
                    drawCircle(
                        color = Color(0xFF38BDF8L).copy(alpha = lifeAlpha * 0.78f),
                        radius = 3.4f,
                        center = Offset(sx, sy)
                    )
                }
            }
        }

        KeyWordEffect.PRISM_SHATTER -> {
            val petals = 12
            val baseR = 54f + (1f - lifeAlpha) * 24f
            val path = Path()
            for (s in 0..72) {
                val theta = (s / 72f) * 2f * PI.toFloat()
                val r = baseR * (0.68f + 0.32f * sin(theta * (petals / 2f) + time * 4f))
                val px = cx + cos(theta) * r
                val py = cy + sin(theta) * r
                if (s == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(
                path = path,
                color = Color(0xFFE879F9L).copy(alpha = lifeAlpha * 0.72f),
                style = Stroke(width = 2.2f)
            )
            val beamColors = listOf(Color(0xFF22D3EEL), Color(0xFFE879F9L), Color(0xFFFDE047L))
            for (arm in 0..2) {
                val col = beamColors[arm]
                for (step in 1..12) {
                    val t = ((step / 12f) + time * 0.7f) % 1f
                    val dist = 24f + t * 220f
                    val ang = time * 1.4f + (arm * 2f * PI.toFloat() / 3f) + t * 0.65f
                    val px = cx + cos(ang) * dist
                    val py = cy + sin(ang) * dist
                    drawCircle(
                        color = col.copy(alpha = (1f - t) * lifeAlpha * 0.78f),
                        radius = 4.2f,
                        center = Offset(px, py)
                    )
                }
            }
        }

        KeyWordEffect.MAGNETAR_WAR -> {
            val jetAngle = time * 1.8f
            for (side in listOf(-1f, 1f)) {
                val loopR = 72f
                val perp = jetAngle + side * (PI.toFloat() / 2f)
                val lcx = cx + cos(perp) * loopR * 0.6f
                val lcy = cy + sin(perp) * loopR * 0.6f
                drawCircle(
                    color = Color(0xFFFB7185L).copy(alpha = lifeAlpha * 0.52f),
                    radius = loopR * 0.6f,
                    center = Offset(lcx, lcy),
                    style = Stroke(width = 2.2f)
                )
            }
            for (pole in listOf(0f, PI.toFloat())) {
                val ja = jetAngle + pole
                for (s in 1..10) {
                    val jp = ((s / 10f) + time * 1.2f) % 1f
                    val dist = 16f + jp * 210f
                    val wobble = sin(jp * 10f - time * 6f) * (6f + jp * 16f)
                    val perp = ja + PI.toFloat() / 2f
                    val px = cx + cos(ja) * dist + cos(perp) * wobble
                    val py = cy + sin(ja) * dist + sin(perp) * wobble
                    drawCircle(
                        color = (if (s % 2 == 0) Color(0xFFFDE047L) else Color(0xFFF43F5EL)).copy(alpha = (1f - jp) * lifeAlpha * 0.85f),
                        radius = 4.5f,
                        center = Offset(px, py)
                    )
                }
            }
        }

        KeyWordEffect.CONSTELLATION_STAR -> {
            val outerR = 68f
            for (i in 0 until 6) {
                val a = time * 1.4f + (i * PI.toFloat() / 3f)
                val px = cx + cos(a) * outerR
                val py = cy + sin(a) * outerR
                drawCircle(
                    color = Color(0xFFFDE047L).copy(alpha = lifeAlpha * 0.85f),
                    radius = if (i % 2 == 0) 4.2f else 2.8f,
                    center = Offset(px, py)
                )
            }
            for (arcIdx in 0..2) {
                val startDeg = ((time * 95f) + arcIdx * 120f) % 360f
                drawArc(
                    color = Color(0xFFFACC15L).copy(alpha = lifeAlpha * 0.58f),
                    startAngle = startDeg,
                    sweepAngle = 78f,
                    useCenter = false,
                    topLeft = Offset(cx - 52f, cy - 52f),
                    size = Size(104f, 104f),
                    style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                )
            }
        }

        KeyWordEffect.BINARY_HEART -> {
            val sep = 42f + 8f * sin(time * 8f)
            val a1 = time * 3.2f
            val a2 = a1 + PI.toFloat()
            val p1 = Offset(cx + cos(a1) * sep, cy + sin(a1) * sep)
            val p2 = Offset(cx + cos(a2) * sep, cy + sin(a2) * sep)

            drawCircle(
                color = Color(0xFFF43F5EL).copy(alpha = lifeAlpha * 0.35f),
                radius = sep,
                center = Offset(cx, cy),
                style = Stroke(width = 2.0f)
            )
            drawCircle(color = Color(0xFFF43F5EL).copy(alpha = lifeAlpha * 0.42f), radius = 16f, center = p1)
            drawCircle(color = Color.White.copy(alpha = lifeAlpha), radius = 5.5f, center = p1)
            drawCircle(color = Color(0xFFFACC15L).copy(alpha = lifeAlpha * 0.42f), radius = 16f, center = p2)
            drawCircle(color = Color.White.copy(alpha = lifeAlpha), radius = 5.5f, center = p2)
        }

        KeyWordEffect.ECLIPSE_DARK -> {
            val eclipseR = 38f
            drawCircle(
                color = Color(0xFFFBBF24L).copy(alpha = lifeAlpha * 0.42f),
                radius = eclipseR * 1.85f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFF030408L).copy(alpha = 0.95f),
                radius = eclipseR,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFFFEF08AL).copy(alpha = lifeAlpha * 0.95f),
                radius = eclipseR + 2f,
                center = Offset(cx, cy),
                style = Stroke(width = 3.0f)
            )
        }

        KeyWordEffect.NONE -> {}
    }
}

fun DrawScope.drawParticles(particles: List<GameObject>) {
    particles.forEach { pt ->
        drawCircle(
            color = pt.color.copy(alpha = pt.life.coerceIn(0f, 1f)),
            radius = pt.radius * pt.life.coerceIn(0.2f, 1f),
            center = Offset(pt.x, pt.y)
        )
    }
}

/**
 * V4 BEAT-SPAWNED, BEAT-PULSING ORBS (ZERO ON-SCREEN WORDS!):
 * 1. Draws a direct Luminous Guidance Tether from the Hero to the #1 active target orb so the player
 *    always knows what to collect next.
 * 2. Every orb physically pumps in size and brightness on every single beat (`beatKick` + `bass`).
 * 3. Regular Beat Orbs (`KeyWordEffect.NONE`) are compact Cyan-Emerald Harmonic Pearls (`22f` base radius).
 * 4. Special Supernova Beat-Drop Orbs (`KeyWordEffect != NONE`) are 2x LARGER (`38f` base radius)
 *    Multi-Ringed 24K Gold & Prismatic Gyroscope Cores with 3 counter-rotating sacred geometry rings
 *    and 8 pulsing solar flare spikes — 100% unmistakable without a single word of text!
 */
@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawEnemyTelegraphs(
    telegraphs: List<EnemySpawnTelegraph>,
    time: Float,
    beatPhase: Float
) {
    if (telegraphs.isEmpty()) return
    val beatKick = if (beatPhase >= 0f) exp(-beatPhase * 5.5f).coerceIn(0f, 1f) else 0.3f

    telegraphs.forEach { tg ->
        val progress = (1f - (tg.timer / tg.maxTimer)).coerceIn(0f, 1f)
        val urgencyPulse = 0.65f + 0.35f * sin(time * 24f)
        val warnCol = Color(0xFFFF1E42L)
        val coreCol = Color(0xFFFFB703L)
        val deg = (tg.targetAngle * 180f / PI.toFloat())

        // 1. Expanding Directional Warning Ripples (3 curved sonar arcs sweeping inward from the border—zero straight laser lines!)
        for (w in 1..3) {
            val waveR = 22f + w * 22f + ((time * 65f) % 22f)
            val waveAlpha = (1f - w * 0.25f) * (0.55f + 0.30f * urgencyPulse)
            drawArc(
                color = warnCol.copy(alpha = waveAlpha.coerceIn(0f, 0.85f)),
                startAngle = deg - 36f,
                sweepAngle = 72f,
                useCenter = false,
                topLeft = Offset(tg.edgeX - waveR, tg.edgeY - waveR),
                size = Size(waveR * 2f, waveR * 2f),
                style = Stroke(width = 2.6f + beatKick * 1.2f, cap = StrokeCap.Round)
            )
        }

        // 2. Shrinking Lock-On Countdown Ring on the screen border
        val lockRadius = 40f * (1f - progress * 0.62f)
        drawCircle(
            color = warnCol.copy(alpha = 0.78f),
            radius = lockRadius,
            center = Offset(tg.edgeX, tg.edgeY),
            style = Stroke(width = 2.4f)
        )

        // 3. Smooth Crimson/Amber Radial Warning Beacon Orb on the perimeter (Zero sharp polygon lines!)
        val bSize = 13f + beatKick * 4f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, coreCol, warnCol.copy(alpha = 0.88f), Color.Transparent),
                center = Offset(tg.edgeX, tg.edgeY),
                radius = bSize * 1.65f
            ),
            radius = bSize * 1.65f,
            center = Offset(tg.edgeX, tg.edgeY)
        )
        drawCircle(
            color = coreCol,
            radius = bSize * 0.72f,
            center = Offset(tg.edgeX, tg.edgeY),
            style = Stroke(width = 2.0f)
        )
        drawCircle(
            color = Color.White,
            radius = 3.8f,
            center = Offset(tg.edgeX, tg.edgeY)
        )
    }
}

/**
 * V6 MUSICAL SLIDE-NOTE WORMHOLE STREAMS:
 * - Renders Entry Portal A (`w.x, w.y`), Exit Portal B (`w.exitX, w.exitY`), and a glowing curved Bezier
 *   Musical Light-Ribbon lined with 5 pulsing Rhythm Pearls so players can surf the musical slide on the beat!
 */
fun DrawScope.drawEnemySprite(
    e: GameObject,
    alpha: Float,
    chromatic: Float,
    pulsarMandala: ImageBitmap,
    seekerMandala: ImageBitmap,
    wandererMandala: ImageBitmap,
    time: Float = 0f,
    beatPhase: Float = 0f,
    isGoldenOverdrive: Boolean = false,
    bassEnergy: Float = 0.45f
) {
    val safeAlpha = alpha.coerceIn(0f, 1f)
    val beatKick = if (beatPhase >= 0f) exp(-beatPhase * 5f).coerceIn(0f, 1f) else 0.2f

    val dangerRimColor = when {
        isGoldenOverdrive -> Color(0xFFF59E0BL)
        e.type == EnemyType.SEEKER -> Color(0xFFFF1E42L)
        e.type == EnemyType.PULSE -> Color(0xFFF43F5EL)
        else -> Color(0xFFFB7185L)
    }
    val secondaryTrimColor = when (e.type) {
        EnemyType.SEEKER -> Color(0xFFFFB703L)
        EnemyType.PULSE -> Color(0xFFE879F9L)
        else -> Color(0xFFC084FCL)
    }
    val visualRad = e.radius * (1.48f + 0.18f * beatKick + 0.12f * bassEnergy)

    if (e.type == EnemyType.LASER_SWEEPER) {
        val barHalfW = e.orbitRadius.coerceAtLeast(68f)
        val barHalfH = e.radius.coerceAtLeast(11f)
        withTransform({
            rotate(degrees = (e.angle * 180f / PI.toFloat()), pivot = Offset(e.x, e.y))
        }) {
            drawRoundRect(
                color = Color(0xFFFF1E42L).copy(alpha = safeAlpha * (0.48f + beatKick * 0.28f)),
                topLeft = Offset(e.x - barHalfW - 8f, e.y - barHalfH - 8f),
                size = Size((barHalfW + 8f) * 2f, (barHalfH + 8f) * 2f),
                cornerRadius = CornerRadius(barHalfH + 8f, barHalfH + 8f)
            )
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFF1E42L), Color.White, Color(0xFFFFB703L), Color.White, Color(0xFFFF1E42L))
                ),
                topLeft = Offset(e.x - barHalfW, e.y - barHalfH * 0.60f),
                size = Size(barHalfW * 2f, barHalfH * 1.2f),
                cornerRadius = CornerRadius(barHalfH, barHalfH),
                alpha = safeAlpha
            )
            drawCircle(color = Color.White, radius = barHalfH, center = Offset(e.x - barHalfW, e.y))
            drawCircle(color = Color.White, radius = barHalfH, center = Offset(e.x + barHalfW, e.y))
        }
        return
    }

    val mandalaBitmap = when (e.type) {
        EnemyType.PULSE -> pulsarMandala
        EnemyType.SEEKER -> seekerMandala
        EnemyType.WANDERER -> wandererMandala
        EnemyType.PHANTOM -> wandererMandala
        EnemyType.LASER_SWEEPER -> pulsarMandala
    }

    // 1. High-Contrast Crimson Danger Halo behind every enemy (ZERO wiggling tail!)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                dangerRimColor.copy(alpha = safeAlpha * (0.58f + 0.25f * beatKick)),
                Color(0xFF881337L).copy(alpha = safeAlpha * 0.30f),
                Color.Transparent
            ),
            center = Offset(e.x, e.y),
            radius = visualRad * 1.95f
        ),
        radius = visualRad * 1.95f,
        center = Offset(e.x, e.y),
        blendMode = BlendMode.Screen
    )

    // 2. Smooth Curved Energy Crescents & Orbital Threat Rings by Archetype (Zero sharp straight polygon wireframes!)
    when (e.type) {
        EnemyType.SEEKER -> {
            // Smooth Swept Crescent Interceptor Arcs locked onto flight vector
            val aimDeg = (e.angle * 180f / PI.toFloat())
            val arcR = visualRad * 1.18f
            drawCircle(
                color = Color(0xFF1E050DL).copy(alpha = safeAlpha * 0.92f),
                radius = visualRad * 0.95f,
                center = Offset(e.x, e.y)
            )
            drawArc(
                color = dangerRimColor.copy(alpha = safeAlpha),
                startAngle = aimDeg - 74f,
                sweepAngle = 148f,
                useCenter = false,
                topLeft = Offset(e.x - arcR, e.y - arcR),
                size = Size(arcR * 2f, arcR * 2f),
                style = Stroke(width = 3.6f + beatKick, cap = StrokeCap.Round)
            )
            for (side in listOf(-2.15f, 2.15f)) {
                val wx = e.x + cos(e.angle + side) * (visualRad * 1.18f)
                val wy = e.y + sin(e.angle + side) * (visualRad * 1.18f)
                drawCircle(color = secondaryTrimColor.copy(alpha = safeAlpha), radius = 4.2f, center = Offset(wx, wy))
            }
        }
        EnemyType.PULSE -> {
            // Smooth Spinning Multi-Crescent Plasma Vortex
            drawCircle(
                color = Color(0xFF1F040EL).copy(alpha = safeAlpha * 0.92f),
                radius = visualRad * 0.98f,
                center = Offset(e.x, e.y)
            )
            for (blade in 0 until 4) {
                val startDeg = ((time * -210f) + blade * 90f) % 360f
                val bRad = visualRad * (1.12f + 0.14f * beatKick)
                drawArc(
                    color = dangerRimColor.copy(alpha = safeAlpha),
                    startAngle = startDeg,
                    sweepAngle = 54f,
                    useCenter = false,
                    topLeft = Offset(e.x - bRad, e.y - bRad),
                    size = Size(bRad * 2f, bRad * 2f),
                    style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                )
            }
        }
        else -> {
            // Smooth Counter-Rotating Eclipse Rings + 3 Orbiting Hazard Pearls
            drawCircle(
                color = Color(0xFF16041EL).copy(alpha = safeAlpha * 0.88f),
                radius = visualRad * 0.95f,
                center = Offset(e.x, e.y)
            )
            for (ring in 0..1) {
                val rRad = visualRad * (if (ring == 0) 1.08f else 0.82f)
                val startDeg = ((if (ring == 0) 1f else -1f) * time * 110f) % 360f
                for (seg in 0..1) {
                    drawArc(
                        color = (if (ring == 0) dangerRimColor else secondaryTrimColor).copy(alpha = safeAlpha * 0.95f),
                        startAngle = startDeg + seg * 180f,
                        sweepAngle = 108f,
                        useCenter = false,
                        topLeft = Offset(e.x - rRad, e.y - rRad),
                        size = Size(rRad * 2f, rRad * 2f),
                        style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                    )
                }
            }
            for (n in 0..2) {
                val na = time * 3.2f + n * (2f * PI.toFloat() / 3f)
                val nx = e.x + cos(na) * (visualRad * 1.36f)
                val ny = e.y + sin(na) * (visualRad * 1.36f)
                drawCircle(color = secondaryTrimColor.copy(alpha = safeAlpha), radius = 4.0f, center = Offset(nx, ny))
            }
        }
    }

    // 3. Rotating Mandala Core Overlay
    val drawSize = (visualRad * 2.35f).toInt().coerceAtLeast(1)
    val halfSize = drawSize / 2
    val rotDegrees = (e.angle * 180f / PI.toFloat()) + 90f

    withTransform({
        rotate(degrees = rotDegrees, pivot = Offset(e.x, e.y))
    }) {
        drawImage(
            image = mandalaBitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(mandalaBitmap.width, mandalaBitmap.height),
            dstOffset = IntOffset(e.x.toInt() - halfSize, e.y.toInt() - halfSize),
            dstSize = IntSize(drawSize, drawSize),
            alpha = safeAlpha * 0.85f,
            colorFilter = if (isGoldenOverdrive) ColorFilter.tint(Color(0xFFFDE047L), BlendMode.Modulate) else null,
            blendMode = BlendMode.Screen
        )
    }

    // 4. High-Contrast Multi-Ring Reactor Eye
    drawCircle(
        color = Color(0xFF090206L).copy(alpha = safeAlpha * 0.94f),
        radius = e.radius * 0.56f,
        center = Offset(e.x, e.y)
    )
    drawCircle(
        color = dangerRimColor.copy(alpha = safeAlpha),
        radius = e.radius * 0.42f,
        center = Offset(e.x, e.y)
    )
    drawCircle(
        color = Color.White.copy(alpha = safeAlpha * 0.95f),
        radius = e.radius * 0.20f,
        center = Offset(e.x, e.y)
    )
}

fun DrawScope.drawPlayerSprite(
    p: GameObject,
    isFocus: Boolean,
    time: Float,
    shield: Float,
    applyCB: (Color) -> Color,
    isDead: Boolean,
    heroSolarMandala: ImageBitmap,
    heroCyberMandala: ImageBitmap,
    heroPrismMandala: ImageBitmap,
    skin: String = "SOLAR",
    invulnTime: Float = 0f,
    beatPhase: Float = 0f,
    isGoldenOverdrive: Boolean = false
) {
    if (isDead) return
    val center = Offset(p.x, p.y)
    val beatKick = if (beatPhase >= 0f) exp(-beatPhase * 5f).coerceIn(0f, 1f) else 0.2f

    val isBlinking = invulnTime > 0f && (sin(time * 28f) < 0f)
    val spriteAlpha = if (isBlinking) 0.42f else 1.0f

    val heroPrimaryCol = when {
        isGoldenOverdrive -> Color(0xFFFACC15L)
        skin == "CYBER" || skin == "VINYL" -> Color(0xFF38BDF8L)
        skin == "PRISM" || skin == "SERAPH" -> Color(0xFFE879F9L)
        else -> Color(0xFF4ADE80L)
    }

    // 1. Volumetric Hero Aura + Crisp Equalizer Ring (unmistakably identifies the Player!)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                heroPrimaryCol.copy(alpha = 0.48f * spriteAlpha),
                Color(0xFF6366F1L).copy(alpha = 0.22f * spriteAlpha),
                Color.Transparent
            ),
            center = center,
            radius = p.radius * 2.8f
        ),
        radius = p.radius * 2.8f,
        center = center,
        blendMode = BlendMode.Screen
    )

    drawCircle(
        color = heroPrimaryCol.copy(alpha = (0.55f + beatKick * 0.30f) * spriteAlpha),
        radius = p.radius * (2.25f + 0.25f * beatKick),
        center = center,
        style = Stroke(width = 2.4f + beatKick)
    )

    // Smooth Rotating Harmonic Crescent Waves + Orbiting Gold Pearls around the Hero (Replaces 12 straight stick lines!)
    val haloArcR = p.radius * (1.72f + 0.18f * beatKick)
    for (arcIdx in 0 until 3) {
        val startDeg = ((time * 95f) + arcIdx * 120f) % 360f
        drawArc(
            color = (if (arcIdx % 2 == 0) Color(0xFFFACC15L) else heroPrimaryCol).copy(alpha = 0.82f * spriteAlpha),
            startAngle = startDeg,
            sweepAngle = 68f + beatKick * 18f,
            useCenter = false,
            topLeft = Offset(center.x - haloArcR, center.y - haloArcR),
            size = Size(haloArcR * 2f, haloArcR * 2f),
            style = Stroke(width = 2.8f, cap = StrokeCap.Round)
        )
    }
    for (orbIdx in 0 until 6) {
        val ang = (orbIdx / 6f) * 6.283185f - time * 1.6f
        val orbR = p.radius * (1.92f + (if (orbIdx % 2 == 0) 0.22f * beatKick else 0.08f))
        drawCircle(
            color = (if (orbIdx % 2 == 0) Color(0xFFFACC15L) else heroPrimaryCol).copy(alpha = 0.88f * spriteAlpha),
            radius = if (orbIdx % 2 == 0) 3.2f else 2.2f,
            center = Offset(center.x + cos(ang) * orbR, center.y + sin(ang) * orbR)
        )
    }

    // 2. Solid Obsidian-and-Gold Vinyl Turntable Platter Base (gives the Hero a rock-solid silhouette on Android!)
    val platterR = p.radius * (1.48f + 0.10f * beatKick)
    drawCircle(
        color = Color(0xFF040812L).copy(alpha = 0.94f * spriteAlpha),
        radius = platterR,
        center = center
    )
    drawCircle(
        color = heroPrimaryCol.copy(alpha = 0.92f * spriteAlpha),
        radius = platterR,
        center = center,
        style = Stroke(width = 2.5f)
    )

    if (invulnTime > 0f) {
        val invulnPulse = 1f + 0.1f * sin(time * 12f)
        drawCircle(
            color = Color(0xFF4ADE80L).copy(alpha = 0.72f),
            radius = p.radius * 2.5f * invulnPulse,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
    }

    if (shield > 0f) {
        val shieldPulse = 1f + 0.08f * sin(time * 8f)
        drawCircle(
            color = applyCB(Color(0xFF38BDF8L)).copy(alpha = 0.75f),
            radius = p.radius * 2.45f * shieldPulse,
            center = center,
            style = Stroke(width = 3.6f)
        )
    }

    val heroBitmap = when (skin) {
        "CYBER", "VINYL" -> heroCyberMandala
        "PRISM", "SERAPH" -> heroPrismMandala
        else -> heroSolarMandala
    }

    val pulse = 1f + 0.05f * sin(time * 6f) + 0.10f * beatKick
    val drawSize = (p.radius * 3.6f * pulse).toInt().coerceAtLeast(1)
    val halfSize = drawSize / 2
    val rotSpeed = if (isFocus) 22f else 54f

    withTransform({
        rotate(degrees = (time * rotSpeed) % 360f, pivot = center)
    }) {
        drawImage(
            image = heroBitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(heroBitmap.width, heroBitmap.height),
            dstOffset = IntOffset(p.x.toInt() - halfSize, p.y.toInt() - halfSize),
            dstSize = IntSize(drawSize, drawSize),
            alpha = spriteAlpha,
            colorFilter = if (skin == "ECLIPSE" || isGoldenOverdrive) {
                ColorFilter.tint(Color(0xFFFDE047L), BlendMode.Modulate)
            } else null,
            blendMode = BlendMode.Screen
        )
    }

    // 3. Blazing 24K Gold & White Center Spindle Core
    drawCircle(
        color = Color(0xFFFACC15L).copy(alpha = spriteAlpha),
        radius = p.radius * 0.42f,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = spriteAlpha),
        radius = p.radius * 0.22f,
        center = center
    )
}

/**
 * Renders both World-Space Rhythm Judgments (`PERFECT!`, `GOOD!`, `MISS`, `SLIDE!`) right above each
 * beat node AND Center-Top Song Section Callout Pills when `isLarge` is set.
 */
fun DrawScope.drawFloatingTexts(floatingTexts: List<FloatingText>) {
    if (floatingTexts.isEmpty()) return

    drawIntoCanvas { canvas ->
        floatingTexts.forEach { ft ->
            val lifeEase = ft.life.coerceIn(0f, 1f)
            val alpha = (lifeEase * 255).toInt()
            if (alpha <= 5) return@forEach

            if (ft.isLarge && ft.subtitle.isNotEmpty()) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.205f
                cachedBannerPaint.textSize = 32f
                cachedBannerPaint.color = android.graphics.Color.argb((alpha * 0.70f).toInt(), 79, 70, 229)
                canvas.nativeCanvas.drawText(ft.text, cx + 2.5f, cy + 2.5f, cachedBannerPaint)

                cachedBannerPaint.color = android.graphics.Color.argb(
                    alpha,
                    (ft.color.red * 255).toInt(),
                    (ft.color.green * 255).toInt(),
                    (ft.color.blue * 255).toInt()
                )
                canvas.nativeCanvas.drawText(ft.text, cx, cy, cachedBannerPaint)

                cachedSubPaint.textSize = 19f
                cachedSubPaint.color = android.graphics.Color.argb((alpha * 0.88f).toInt(), 226, 232, 240)
                canvas.nativeCanvas.drawText(ft.subtitle, cx, cy + 20f, cachedSubPaint)
            } else {
                val riseY = ft.y - (1f - lifeEase) * 36f
                cachedSubPaint.textSize = if (ft.isLarge) 28f else 22f
                cachedSubPaint.color = android.graphics.Color.argb((alpha * 0.82f).toInt(), 4, 8, 18)
                canvas.nativeCanvas.drawText(ft.text, ft.x + 2f, riseY + 2f, cachedSubPaint)

                cachedSubPaint.color = android.graphics.Color.argb(
                    alpha,
                    (ft.color.red * 255).toInt(),
                    (ft.color.green * 255).toInt(),
                    (ft.color.blue * 255).toInt()
                )
                canvas.nativeCanvas.drawText(ft.text, ft.x, riseY, cachedSubPaint)
            }
        }
    }
}
