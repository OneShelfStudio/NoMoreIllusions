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
        drawCircle(
            color = c.copy(alpha = if (isGoldenOverdrive) 0.42f else 0.26f),
            radius = m.radius,
            center = Offset(m.x, m.y)
        )
        if (idx % 5 == 0) {
            val ray = m.radius * 2.4f
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(m.x - ray, m.y),
                end = Offset(m.x + ray, m.y),
                strokeWidth = 1.0f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(m.x, m.y - ray),
                end = Offset(m.x, m.y + ray),
                strokeWidth = 1.0f
            )
        }
    }
}

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
        drawLine(
            color = baseColor.copy(alpha = progress * 0.44f),
            start = trail[i],
            end = trail[i + 1],
            strokeWidth = progress * 18f,
            cap = StrokeCap.Round
        )
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
                drawLine(
                    color = Color(0xFF38BDF8L).copy(alpha = lifeAlpha * 0.55f),
                    start = Offset(orb.x, orb.y),
                    end = Offset(cx, cy),
                    strokeWidth = 1.8f
                )
                val synT = ((time * 1.8f + idx * 0.23f) % 1f)
                val sx = orb.x + (cx - orb.x) * synT
                val sy = orb.y + (cy - orb.y) * synT
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 3.2f,
                    center = Offset(sx, sy)
                )
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
            val outerR = 74f
            for (i in 0 until 8) {
                val a = time * 0.9f + (i * PI.toFloat() / 4f)
                val len = if (i % 2 == 0) outerR else outerR * 0.60f
                drawLine(
                    color = Color(0xFFFDE047L).copy(alpha = lifeAlpha * 0.78f),
                    start = Offset(cx, cy),
                    end = Offset(cx + cos(a) * len, cy + sin(a) * len),
                    strokeWidth = if (i % 2 == 0) 3.2f else 1.8f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = lifeAlpha * 0.45f),
                radius = 52f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.4f)
            )
        }

        KeyWordEffect.BINARY_HEART -> {
            val sep = 42f + 8f * sin(time * 8f)
            val a1 = time * 3.2f
            val a2 = a1 + PI.toFloat()
            val p1 = Offset(cx + cos(a1) * sep, cy + sin(a1) * sep)
            val p2 = Offset(cx + cos(a2) * sep, cy + sin(a2) * sep)

            drawLine(
                color = Color(0xFFF43F5EL).copy(alpha = lifeAlpha * 0.65f),
                start = p1,
                end = p2,
                strokeWidth = 2.0f
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

        // 3. High-Contrast Crimson/Amber Warning Diamond Beacon (◆)
        val dSize = 14f + beatKick * 4f
        val diamond = sharedTelegraphDiamond.apply {
            reset()
            moveTo(tg.edgeX, tg.edgeY - dSize)
            lineTo(tg.edgeX + dSize, tg.edgeY)
            lineTo(tg.edgeX, tg.edgeY + dSize)
            lineTo(tg.edgeX - dSize, tg.edgeY)
            close()
        }
        drawPath(
            path = diamond,
            color = warnCol.copy(alpha = 0.90f)
        )
        drawPath(
            path = diamond,
            color = coreCol,
            style = Stroke(width = 2.2f)
        )
        drawCircle(
            color = Color.White,
            radius = 3.5f,
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

    // 2. Crisp Geometric Threat Chassis by Archetype (Sawblade, Delta Wing, or Void Octahedron)
    when (e.type) {
        EnemyType.SEEKER -> {
            // "DELTA INTERCEPTOR": Angular Twin-Blade Stealth Fighter Chassis locked onto flight vector
            val fighterPath = Path().apply {
                val noseX = e.x + cos(e.angle) * (visualRad * 1.65f)
                val noseY = e.y + sin(e.angle) * (visualRad * 1.65f)
                val leftWingX = e.x + cos(e.angle + 2.38f) * (visualRad * 1.48f)
                val leftWingY = e.y + sin(e.angle + 2.38f) * (visualRad * 1.48f)
                val notchX = e.x - cos(e.angle) * (visualRad * 0.65f)
                val notchY = e.y - sin(e.angle) * (visualRad * 0.65f)
                val rightWingX = e.x + cos(e.angle - 2.38f) * (visualRad * 1.48f)
                val rightWingY = e.y + sin(e.angle - 2.38f) * (visualRad * 1.48f)
                moveTo(noseX, noseY)
                lineTo(leftWingX, leftWingY)
                lineTo(notchX, notchY)
                lineTo(rightWingX, rightWingY)
                close()
            }
            drawPath(
                path = fighterPath,
                color = Color(0xFF1E050DL).copy(alpha = safeAlpha * 0.92f)
            )
            drawPath(
                path = fighterPath,
                color = dangerRimColor.copy(alpha = safeAlpha),
                style = Stroke(width = 2.8f + beatKick)
            )
            // Twin amber wingtip beacons
            for (side in listOf(-2.38f, 2.38f)) {
                val wx = e.x + cos(e.angle + side) * (visualRad * 1.48f)
                val wy = e.y + sin(e.angle + side) * (visualRad * 1.48f)
                drawCircle(color = secondaryTrimColor.copy(alpha = safeAlpha), radius = 4.2f, center = Offset(wx, wy))
            }
        }
        EnemyType.PULSE -> {
            // "CRIMSON SAWBLADE": 8-Point Spinning Cybernetic Buzzsaw Polygon
            val sawPath = Path().apply {
                val pts = 16
                for (i in 0 until pts) {
                    val a = (i.toFloat() / pts) * 6.283185f - time * 3.8f
                    val r = if (i % 2 == 0) visualRad * (1.45f + 0.18f * beatKick) else visualRad * 0.86f
                    val px = e.x + cos(a) * r
                    val py = e.y + sin(a) * r
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = sawPath,
                color = Color(0xFF1F040EL).copy(alpha = safeAlpha * 0.92f)
            )
            drawPath(
                path = sawPath,
                color = dangerRimColor.copy(alpha = safeAlpha),
                style = Stroke(width = 2.6f)
            )
        }
        else -> {
            // "VOID OCTAHEDRON": Counter-Rotating Double-Diamond Crystalline Prism + 3 Orbiting Hazard Nodes
            for (sq in 0..1) {
                withTransform({
                    rotate(degrees = (if (sq == 0) 1f else -1f) * time * 72f + sq * 45f, pivot = Offset(e.x, e.y))
                }) {
                    val boxR = visualRad * 0.96f
                    drawRoundRect(
                        color = Color(0xFF16041EL).copy(alpha = safeAlpha * 0.85f),
                        topLeft = Offset(e.x - boxR, e.y - boxR),
                        size = Size(boxR * 2f, boxR * 2f),
                        cornerRadius = CornerRadius(boxR * 0.16f, boxR * 0.16f)
                    )
                    drawRoundRect(
                        color = (if (sq == 0) dangerRimColor else secondaryTrimColor).copy(alpha = safeAlpha * 0.95f),
                        topLeft = Offset(e.x - boxR, e.y - boxR),
                        size = Size(boxR * 2f, boxR * 2f),
                        cornerRadius = CornerRadius(boxR * 0.16f, boxR * 0.16f),
                        style = Stroke(width = 2.4f)
                    )
                }
            }
            for (n in 0..2) {
                val na = time * 3.2f + n * (2f * PI.toFloat() / 3f)
                val nx = e.x + cos(na) * (visualRad * 1.42f)
                val ny = e.y + sin(na) * (visualRad * 1.42f)
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

    // 12-Bar Radial Turntable Equalizer Crown around the Hero
    for (bar in 0 until 12) {
        val ang = (bar / 12f) * 6.283185f + time * 1.4f
        val rIn = p.radius * 1.55f
        val rOut = p.radius * (1.88f + (if (bar % 2 == 0) 0.35f * beatKick else 0.15f))
        drawLine(
            color = (if (bar % 2 == 0) Color(0xFFFACC15L) else heroPrimaryCol).copy(alpha = 0.85f * spriteAlpha),
            start = Offset(center.x + cos(ang) * rIn, center.y + sin(ang) * rIn),
            end = Offset(center.x + cos(ang) * rOut, center.y + sin(ang) * rOut),
            strokeWidth = 2.6f,
            cap = StrokeCap.Round
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
