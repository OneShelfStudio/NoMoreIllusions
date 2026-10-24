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

private val cachedBannerPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
}

private val cachedSubPaint = android.graphics.Paint().apply {
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

    // 2. Sequential Rhythm Constellation Path (connects upcoming stationary beats 1 -> 2 -> 3 -> 4 in order!)
    if (sorted.size >= 2) {
        for (cIdx in 0 until sorted.size - 1) {
            val pA = sorted[cIdx]
            val pB = sorted[cIdx + 1]
            val linkAlpha = (0.48f - cIdx * 0.12f).coerceIn(0.16f, 0.50f)
            drawLine(
                color = Color(0xFF38BDF8L).copy(alpha = linkAlpha * 0.55f),
                start = Offset(pA.x, pA.y),
                end = Offset(pB.x, pB.y),
                strokeWidth = 2.0f,
                cap = StrokeCap.Round
            )
            // Flowing beat-pulse pearls gliding from pA -> pB
            for (step in 1..3) {
                val mt = ((step / 4f) + time * 0.85f) % 1f
                val mx = pA.x + (pB.x - pA.x) * mt
                val my = pA.y + (pB.y - pA.y) * mt - sin(mt * PI.toFloat()) * 12f
                drawCircle(
                    color = Color(0xFFFACC15L).copy(alpha = linkAlpha * 0.85f),
                    radius = if (cIdx == 0) 3.0f else 2.2f,
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

            val spikeCount = 8
            for (sIdx in 0 until spikeCount) {
                val ang = (sIdx * PI.toFloat() / 4f) + time * 0.65f
                val isMajorSpike = sIdx % 2 == 0
                val spikeLen = coreRad * (if (isMajorSpike) (2.15f + 0.55f * beatKick) else (1.45f + 0.30f * beatKick))
                drawLine(
                    color = if (isMajorSpike) Color(0xFFFFFBEBL) else effectColor,
                    start = Offset(p.x - cos(ang) * spikeLen, p.y - sin(ang) * spikeLen),
                    end = Offset(p.x + cos(ang) * spikeLen, p.y + sin(ang) * spikeLen),
                    strokeWidth = if (isMajorSpike) 3.0f else 1.8f,
                    cap = StrokeCap.Round
                )
            }

            withTransform({
                rotate(degrees = time * 115f, pivot = center)
            }) {
                val g1 = coreRad * 0.92f
                drawRoundRect(
                    color = Color(0xFFFACC15L).copy(alpha = 0.92f),
                    topLeft = Offset(p.x - g1, p.y - g1),
                    size = Size(g1 * 2f, g1 * 2f),
                    cornerRadius = CornerRadius(g1 * 0.22f, g1 * 0.22f),
                    style = Stroke(width = 2.6f)
                )
            }

            withTransform({
                rotate(degrees = -time * 140f + 45f, pivot = center)
            }) {
                val g2 = coreRad * 0.68f
                drawRoundRect(
                    color = effectColor.copy(alpha = 0.95f),
                    topLeft = Offset(p.x - g2, p.y - g2),
                    size = Size(g2 * 2f, g2 * 2f),
                    cornerRadius = CornerRadius(g2 * 0.18f, g2 * 0.18f),
                    style = Stroke(width = 2.4f)
                )
            }

            drawCircle(
                color = Color(0xFFFACC15L),
                radius = coreRad * 0.46f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = coreRad * 0.28f,
                center = center
            )
        } else {
            // =========================================================================
            // FACETED HEXAGONAL MUSICAL GEM & POWER-UP MEDALLION (ZERO TAILS!)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        effectColor.copy(alpha = 0.52f),
                        effectColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 1.95f
                ),
                radius = coreRad * 1.95f,
                center = center,
                blendMode = BlendMode.Screen
            )

            // 6-sided Faceted Gemstone Bezel rotating smoothly
            val hexPath = Path().apply {
                val hexRad = coreRad * 0.96f
                for (v in 0 until 6) {
                    val a = time * 0.85f + v * (PI.toFloat() / 3f)
                    val vx = p.x + cos(a) * hexRad
                    val vy = p.y + sin(a) * hexRad
                    if (v == 0) moveTo(vx, vy) else lineTo(vx, vy)
                }
                close()
            }
            drawPath(
                path = hexPath,
                color = Color(0xFF060B18L).copy(alpha = 0.92f)
            )
            drawPath(
                path = hexPath,
                color = if (isOnBeatWindow) Color(0xFFFACC15L) else effectColor,
                style = Stroke(width = 2.6f)
            )

            // Inner Facet Ring
            drawCircle(
                color = effectColor.copy(alpha = 0.45f),
                radius = coreRad * 0.64f,
                center = center,
                style = Stroke(width = 1.4f)
            )

            // Distinct Etched Core Emblem (Nova Starburst, Shield Crest, Hourglass, Magnet, or Musical Note ♪)
            when (p.powerUpType) {
                PowerUpType.BLAST -> {
                    // 💥 8-Point Nova Starburst Emblem (Enemy Destroyer!)
                    for (ray in 0 until 8) {
                        val a = ray * (PI.toFloat() / 4f) + time * 1.8f
                        val len = coreRad * (if (ray % 2 == 0) 0.58f else 0.36f)
                        drawLine(
                            color = if (ray % 2 == 0) Color(0xFFFDE047L) else Color.White,
                            start = Offset(p.x - cos(a) * len, p.y - sin(a) * len),
                            end = Offset(p.x + cos(a) * len, p.y + sin(a) * len),
                            strokeWidth = 2.6f,
                            cap = StrokeCap.Round
                        )
                    }
                    drawCircle(color = Color.White, radius = coreRad * 0.22f, center = center)
                }
                PowerUpType.SHIELD -> {
                    // 🛡️ Crisp Shield Diamond Emblem
                    val sh = coreRad * 0.46f
                    val shieldPath = Path().apply {
                        moveTo(p.x, p.y - sh)
                        lineTo(p.x + sh * 0.85f, p.y - sh * 0.35f)
                        lineTo(p.x + sh * 0.65f, p.y + sh * 0.45f)
                        lineTo(p.x, p.y + sh)
                        lineTo(p.x - sh * 0.65f, p.y + sh * 0.45f)
                        lineTo(p.x - sh * 0.85f, p.y - sh * 0.35f)
                        close()
                    }
                    drawPath(path = shieldPath, color = Color.White, style = Stroke(width = 2.4f))
                }
                PowerUpType.SLOW_MO -> {
                    // ⏳ Twin-Triangle Hourglass Emblem
                    val hg = coreRad * 0.42f
                    val hgPath = Path().apply {
                        moveTo(p.x - hg * 0.75f, p.y - hg)
                        lineTo(p.x + hg * 0.75f, p.y - hg)
                        lineTo(p.x - hg * 0.75f, p.y + hg)
                        lineTo(p.x + hg * 0.75f, p.y + hg)
                        close()
                    }
                    drawPath(path = hgPath, color = Color.White, style = Stroke(width = 2.2f))
                }
                PowerUpType.MAGNET -> {
                    // 🧲 U-Shaped Magnet Attractor Arc
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
                    // ♪ Phrase Sequence Number (1, 2, 3, 4) inside the Faceted Gem so players see the exact rhythm chain!
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

/**
 * V5 HIGH-DETAIL ARTICULATED BIOMECHANICAL / CELESTIAL ENEMY SPRITES:
 * - Multi-segmented articulated plasma tail/spine that snakes behind each enemy's velocity vector.
 * - Distinct articulated armor geometry per enemy archetype:
 *   * SEEKER ("Fractured Seraph"): Counter-rotating scimitar wing-mandibles + targeting laser + 6-segment spine.
 *   * PULSE ("Chrono-Saw"): Dual 10-tooth counter-rotating cybernetic sawblades + radial beat lightning arcs.
 *   * WANDERER ("Void Leviathan"): Undulating bioluminescent pectoral fins + layered crystalline carapace.
 */
@Suppress("UNUSED_PARAMETER")
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
