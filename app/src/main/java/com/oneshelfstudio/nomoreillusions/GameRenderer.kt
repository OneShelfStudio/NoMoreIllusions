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

    // 1. Draw Tethered Orbital Satellites around the Hero
    if (satellites.isNotEmpty()) {
        for (i in satellites.indices) {
            val sat = satellites[i]
            drawLine(
                color = sat.color.copy(alpha = 0.24f),
                start = Offset(cx, cy),
                end = Offset(sat.x, sat.y),
                strokeWidth = 1.1f
            )
            if (satellites.size >= 2) {
                val next = satellites[(i + 1) % satellites.size]
                drawLine(
                    color = sat.color.copy(alpha = 0.18f),
                    start = Offset(sat.x, sat.y),
                    end = Offset(next.x, next.y),
                    strokeWidth = 1.0f
                )
            }
            drawCircle(
                color = sat.color.copy(alpha = 0.35f),
                radius = sat.size * 2.4f,
                center = Offset(sat.x, sat.y)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = sat.size * 0.65f,
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

    // 1. Direct Hero -> Primary Target Orb Harmonic Guidance Beam (so the player ALWAYS sees what to collect!)
    val primaryTarget = sorted.first()
    val isPrimarySupernova = primaryTarget.keyWordEffect != KeyWordEffect.NONE
    val guideColor = if (isPrimarySupernova) Color(0xFFFACC15L) else Color(0xFF22D3EEL)
    val gdx = primaryTarget.x - player.x
    val gdy = primaryTarget.y - player.y
    val gDist = hypot(gdx, gdy)
    if (gDist > 36f) {
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    guideColor.copy(alpha = 0.14f + beatKick * 0.16f),
                    guideColor.copy(alpha = if (isPrimarySupernova) 0.55f else 0.36f)
                ),
                start = Offset(player.x, player.y),
                end = Offset(primaryTarget.x, primaryTarget.y)
            ),
            start = Offset(player.x, player.y),
            end = Offset(primaryTarget.x, primaryTarget.y),
            strokeWidth = if (isPrimarySupernova) 2.8f else 1.8f,
            cap = StrokeCap.Round
        )
        // Beat-synced guide photon racing from Hero to the target orb
        val photonT = ((time * 2.2f) % 1f)
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = if (isPrimarySupernova) 3.8f else 2.6f,
            center = Offset(player.x + gdx * photonT, player.y + gdy * photonT)
        )
    }

    // 2. Preview tether from Primary Target Orb to the Next Upcoming Beat Orb (if 2 orbs on screen)
    if (sorted.size >= 2) {
        val nextOrb = sorted[1]
        drawLine(
            color = Color(0xFF38BDF8L).copy(alpha = 0.22f),
            start = Offset(primaryTarget.x, primaryTarget.y),
            end = Offset(nextOrb.x, nextOrb.y),
            strokeWidth = 1.4f,
            cap = StrokeCap.Round
        )
    }

    // 3. Render each Beat Orb (Regular Flow Orb vs. 2x Larger Multi-Ringed Supernova Beat-Drop Orb)
    sorted.forEachIndexed { idx, p ->
        val center = Offset(p.x, p.y)
        val isSupernova = p.keyWordEffect != KeyWordEffect.NONE
        val isPrimary = idx == 0

        // Materialization pop-in scale when spawning on the beat
        val spawnAge = (trackSec - p.spawnBeatSec).coerceAtLeast(0f)
        val popInScale = (spawnAge / 0.22f).coerceIn(0.35f, 1.0f)

        // Strong quarter-note beat pulse + sub-bass swell
        val beatPulseScale = 1.0f + (if (isSupernova) 0.38f else 0.28f) * beatKick + multiBand.bass * 0.14f
        val coreRad = p.radius * popInScale * beatPulseScale * (if (isPrimary) 1.05f else 0.86f)

        val effectColor = when (p.keyWordEffect) {
            KeyWordEffect.TRACTOR_REACH -> Color(0xFF22D3EEL)
            KeyWordEffect.PRISM_SHATTER -> Color(0xFFE879F9L)
            KeyWordEffect.MAGNETAR_WAR -> Color(0xFFFB7185L)
            KeyWordEffect.CONSTELLATION_STAR -> Color(0xFFFACC15L)
            KeyWordEffect.BINARY_HEART -> Color(0xFF4ADE80L)
            KeyWordEffect.ECLIPSE_DARK -> Color(0xFFFDE047L)
            KeyWordEffect.NONE -> Color(0xFF06B6D4L)
        }

        // Converging Beat Timing Ring (contracts onto the orb core on every quarter-note beat!)
        val beatCycle = (1f - beatPhase).coerceIn(0f, 1f)
        val isOnBeatWindow = beatPhase <= 0.36f || beatPhase >= 0.78f
        val ringRadius = coreRad * (1.02f + beatCycle * (if (isSupernova) 1.75f else 1.35f))

        // 0. Traveling Harmonic Comet Wake trailing behind the orb's flight vector (-cos(p.angle), -sin(p.angle))
        val tailDirX = -cos(p.angle)
        val tailDirY = -sin(p.angle)
        val perpX = -tailDirY
        val perpY = tailDirX
        for (seg in 1..6) {
            val t = seg / 6f
            val dist = t * (if (isSupernova) 68f else 44f)
            val sway = sin(time * 7f - seg * 0.7f + p.phase) * (t * 7f)
            val tx = p.x + tailDirX * dist + perpX * sway
            val ty = p.y + tailDirY * dist + perpY * sway
            drawCircle(
                color = (if (isOnBeatWindow) Color(0xFFFACC15L) else effectColor).copy(alpha = (1f - t) * 0.55f),
                radius = coreRad * (1f - t * 0.72f) * 0.48f,
                center = Offset(tx, ty)
            )
        }

        drawCircle(
            color = (if (isOnBeatWindow) Color(0xFFFACC15L) else effectColor).copy(
                alpha = if (isOnBeatWindow) 0.95f else (if (isSupernova) 0.85f else 0.58f)
            ),
            radius = ringRadius,
            center = center,
            style = Stroke(width = if (isOnBeatWindow) 3.8f + beatKick * 1.8f else 2.2f)
        )

        if (isSupernova) {
            // =========================================================================
            // SPECIAL BEAT-DROP SUPERNOVA ORB (ZERO TEXT — UNMISTAKABLE GYROSCOPE STAR!)
            // =========================================================================
            // Second outer golden harmonic countdown ring
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.52f + beatKick * 0.35f),
                radius = ringRadius * 1.18f,
                center = center,
                style = Stroke(width = 1.8f)
            )

            // Volumetric 24K Gold & Prismatic Supernova Halo
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

            // 8 Pulsing Solar Flare Spikes that pump outward on every beat!
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

            // Counter-Rotating Outer Sacred Geometry Gyroscope Ring #1
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

            // Counter-Rotating Inner Sacred Geometry Gyroscope Ring #2
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

            // Blazing White-Gold Singularity Core
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
            // REGULAR BEAT ORB: Clean Cyan-Emerald Beat-Pulsing Pearl
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.92f),
                        effectColor.copy(alpha = 0.62f),
                        Color(0xFF10B981L).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRad * 1.85f
                ),
                radius = coreRad * 1.85f,
                center = center,
                blendMode = BlendMode.Screen
            )
            drawCircle(
                color = effectColor.copy(alpha = 0.90f),
                radius = coreRad * 0.56f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = coreRad * 0.30f,
                center = center
            )
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

        // 1. Projected Crimson Trajectory Sightline showing where the enemy is about to charge
        val lineLen = 190f + beatKick * 35f
        val endX = tg.edgeX + cos(tg.targetAngle) * lineLen
        val endY = tg.edgeY + sin(tg.targetAngle) * lineLen

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    warnCol.copy(alpha = (0.68f + 0.25f * urgencyPulse)),
                    warnCol.copy(alpha = 0.22f),
                    Color.Transparent
                ),
                start = Offset(tg.edgeX, tg.edgeY),
                end = Offset(endX, endY)
            ),
            start = Offset(tg.edgeX, tg.edgeY),
            end = Offset(endX, endY),
            strokeWidth = 2.6f + beatKick * 1.4f,
            cap = StrokeCap.Round
        )

        // 2. Shrinking Lock-On Countdown Ring on the screen border
        val lockRadius = 42f * (1f - progress * 0.65f)
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
 * V5 FADING SINGULARITY WORMHOLES & HARMONIC PHASE BARRIERS:
 * - During the 1.45s fade-in warning (`w.isWarningPhase`), renders a pulsing holographic target reticle.
 * - During active phase (`w.isHazardActive`), renders a swirling 5-arm Kerr Singularity Vortex with an obsidian
 *   event horizon OR a Twin-Node Neon Laser Phase Barrier.
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
        val center = Offset(w.x, w.y)

        if (w.kind == AnomalyKind.PHASE_BARRIER) {
            val halfLen = w.length * 0.5f
            val cosA = cos(w.angle)
            val sinA = sin(w.angle)
            val p1 = Offset(w.x - cosA * halfLen, w.y - sinA * halfLen)
            val p2 = Offset(w.x + cosA * halfLen, w.y + sinA * halfLen)

            if (w.isWarningPhase) {
                drawLine(
                    color = w.color.copy(alpha = a * 0.45f),
                    start = p1,
                    end = p2,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = w.color.copy(alpha = a * 0.6f), radius = 14f, center = p1, style = Stroke(width = 2f))
                drawCircle(color = w.color.copy(alpha = a * 0.6f), radius = 14f, center = p2, style = Stroke(width = 2f))
            } else {
                // Active Neon Laser Phase Barrier + Anchor Pylons
                drawLine(
                    color = w.color.copy(alpha = a * (0.42f + 0.25f * beatKick)),
                    start = p1,
                    end = p2,
                    strokeWidth = 22f + beatKick * 8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = a * 0.92f),
                    start = p1,
                    end = p2,
                    strokeWidth = 5.5f,
                    cap = StrokeCap.Round
                )
                for (node in listOf(p1, p2)) {
                    drawCircle(color = w.color.copy(alpha = a * 0.85f), radius = 16f + beatKick * 4f, center = node)
                    drawCircle(color = Color.White.copy(alpha = a), radius = 7f, center = node)
                }
            }
        } else {
            // Singularity Wormhole Vortex
            val r = w.radius * (0.85f + 0.15f * a + 0.12f * beatKick)
            if (w.isWarningPhase) {
                // Shimmering fade-in warning rings (non-lethal while materializing)
                drawCircle(
                    color = w.color.copy(alpha = a * 0.55f),
                    radius = r * (1.35f - 0.35f * a),
                    center = center,
                    style = Stroke(width = 2.4f)
                )
                drawCircle(
                    color = Color(0xFF38BDF8L).copy(alpha = a * 0.38f),
                    radius = r * 0.65f,
                    center = center,
                    style = Stroke(width = 1.6f)
                )
            } else {
                // Active Swirling Kerr Accretion Disk + Obsidian Event Horizon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF020108L),
                            w.color.copy(alpha = a * 0.78f),
                            Color(0xFF38BDF8L).copy(alpha = a * 0.42f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = r * 2.1f
                    ),
                    radius = r * 2.1f,
                    center = center
                )
                val armCount = 5
                for (arm in 0 until armCount) {
                    val baseAng = (arm.toFloat() / armCount) * 6.283185f - time * 3.2f
                    for (s in 1..8) {
                        val t = s / 8f
                        val sr = r * (0.42f + t * 1.15f)
                        val sa = baseAng + t * 1.9f
                        val sx = w.x + cos(sa) * sr
                        val sy = w.y + sin(sa) * sr
                        val col = if (s % 2 == 0) Color(0xFFE879F9L) else Color(0xFF38BDF8L)
                        drawCircle(
                            color = col.copy(alpha = a * (1f - t * 0.7f)),
                            radius = (1f - t * 0.5f) * 4.5f,
                            center = Offset(sx, sy)
                        )
                    }
                }
                // Deep Obsidian Singularity Core + Photon Ring
                drawCircle(
                    color = Color(0xFF020206L).copy(alpha = a * 0.96f),
                    radius = r * 0.52f,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = a * (0.75f + 0.25f * beatKick)),
                    radius = r * 0.55f,
                    center = center,
                    style = Stroke(width = 2.4f + beatKick * 1.5f)
                )
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

    // 0. 6-Segment Articulated Biomechanical Spine / Plasma Tail snaking behind the enemy's heading
    val backX = -cos(e.angle)
    val backY = -sin(e.angle)
    val perpX = -backY
    val perpY = backX
    var prevSegX = e.x
    var prevSegY = e.y
    for (seg in 1..6) {
        val t = seg / 6f
        val dist = visualRad * 0.55f + seg * 9.5f
        val slither = sin(time * 9.5f - seg * 0.85f + e.phase) * (seg * 2.4f)
        val sx = e.x + backX * dist + perpX * slither
        val sy = e.y + backY * dist + perpY * slither
        val segRad = visualRad * (0.42f * (1f - t * 0.68f))
        drawLine(
            color = dangerRimColor.copy(alpha = safeAlpha * (1f - t * 0.75f) * 0.72f),
            start = Offset(prevSegX, prevSegY),
            end = Offset(sx, sy),
            strokeWidth = segRad * 1.35f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = secondaryTrimColor.copy(alpha = safeAlpha * (1f - t * 0.65f)),
            radius = segRad,
            center = Offset(sx, sy)
        )
        prevSegX = sx
        prevSegY = sy
    }

    // 1. High-Contrast Crimson Danger Halo behind every enemy
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                dangerRimColor.copy(alpha = safeAlpha * (0.55f + 0.25f * beatKick)),
                Color(0xFF881337L).copy(alpha = safeAlpha * 0.28f),
                Color.Transparent
            ),
            center = Offset(e.x, e.y),
            radius = visualRad * 1.95f
        ),
        radius = visualRad * 1.95f,
        center = Offset(e.x, e.y),
        blendMode = BlendMode.Screen
    )

    // 2. Archetype-Specific Articulated Armor & Mechanical Detail Layer
    when (e.type) {
        EnemyType.SEEKER -> {
            // "Fractured Seraph": Articulated Twin Scimitar Wing-Mandibles that snap on the beat
            val wingSpread = 0.58f + 0.22f * beatKick
            for (side in listOf(-1f, 1f)) {
                val wAng1 = e.angle + side * wingSpread
                val wAng2 = e.angle + side * (wingSpread + 0.55f)
                val tipX = e.x + cos(wAng1) * (visualRad * 1.55f)
                val tipY = e.y + sin(wAng1) * (visualRad * 1.55f)
                val midX = e.x + cos(wAng2) * (visualRad * 1.15f)
                val midY = e.y + sin(wAng2) * (visualRad * 1.15f)
                drawLine(
                    color = secondaryTrimColor.copy(alpha = safeAlpha * 0.92f),
                    start = Offset(e.x, e.y),
                    end = Offset(midX, midY),
                    strokeWidth = 3.2f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = dangerRimColor.copy(alpha = safeAlpha * 0.95f),
                    start = Offset(midX, midY),
                    end = Offset(tipX, tipY),
                    strokeWidth = 2.6f,
                    cap = StrokeCap.Round
                )
            }
        }
        EnemyType.PULSE -> {
            // "Chrono-Saw": 10-Tooth Counter-Rotating Cybernetic Sawblade Ring + Beat Lightning Arcs
            val teeth = 10
            for (tIdx in 0 until teeth) {
                val tAng = (tIdx.toFloat() / teeth) * 6.283185f - time * 3.8f
                val rIn = visualRad * 0.95f
                val rOut = visualRad * (1.42f + 0.22f * beatKick)
                val toothLead = tAng + 0.18f
                drawLine(
                    color = secondaryTrimColor.copy(alpha = safeAlpha * 0.88f),
                    start = Offset(e.x + cos(tAng) * rIn, e.y + sin(tAng) * rIn),
                    end = Offset(e.x + cos(toothLead) * rOut, e.y + sin(toothLead) * rOut),
                    strokeWidth = 2.6f,
                    cap = StrokeCap.Round
                )
            }
        }
        else -> {
            // "Void Leviathan": 4 Undulating Bioluminescent Pectoral Fins
            for (side in listOf(-1f, 1f)) {
                for (fin in 1..2) {
                    val flap = sin(time * 7.5f + fin * 1.2f + e.phase) * 0.28f
                    val fAng = e.angle + side * (1.35f + fin * 0.42f + flap)
                    val fLen = visualRad * (1.55f - fin * 0.22f)
                    drawLine(
                        color = secondaryTrimColor.copy(alpha = safeAlpha * 0.82f),
                        start = Offset(e.x, e.y),
                        end = Offset(e.x + cos(fAng) * fLen, e.y + sin(fAng) * fLen),
                        strokeWidth = 3.4f - fin * 0.7f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    // 3. Crisp Crimson/Amber Outer Armor Ring + Counter-Rotating Inner Hex Lattice
    drawCircle(
        color = dangerRimColor.copy(alpha = safeAlpha * (0.88f + 0.12f * beatKick)),
        radius = visualRad * 1.12f,
        center = Offset(e.x, e.y),
        style = Stroke(width = 2.4f + beatKick * 1.2f)
    )
    withTransform({
        rotate(degrees = -time * 95f, pivot = Offset(e.x, e.y))
    }) {
        val boxR = visualRad * 0.76f
        drawRoundRect(
            color = secondaryTrimColor.copy(alpha = safeAlpha * 0.78f),
            topLeft = Offset(e.x - boxR, e.y - boxR),
            size = Size(boxR * 2f, boxR * 2f),
            cornerRadius = CornerRadius(boxR * 0.22f, boxR * 0.22f),
            style = Stroke(width = 1.9f)
        )
    }

    // 4. Forward Trajectory Sightline & Directional Spearhead so the player sees the enemy's heading
    if (!e.hasPassedPlayer) {
        val sightLen = if (e.type == EnemyType.SEEKER) (115f + beatKick * 35f) else (65f + beatKick * 20f)
        val endX = e.x + cos(e.angle) * sightLen
        val endY = e.y + sin(e.angle) * sightLen
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(dangerRimColor.copy(alpha = safeAlpha * 0.62f), Color.Transparent),
                start = Offset(e.x, e.y),
                end = Offset(endX, endY)
            ),
            start = Offset(e.x, e.y),
            end = Offset(endX, endY),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        val tipDist = visualRad * 1.44f
        val sideAng1 = e.angle + 2.5f
        val sideAng2 = e.angle - 2.5f
        val chev = sharedEnemyChevron.apply {
            reset()
            moveTo(e.x + cos(e.angle) * tipDist, e.y + sin(e.angle) * tipDist)
            lineTo(e.x + cos(sideAng1) * (visualRad * 0.85f), e.y + sin(sideAng1) * (visualRad * 0.85f))
            lineTo(e.x + cos(sideAng2) * (visualRad * 0.85f), e.y + sin(sideAng2) * (visualRad * 0.85f))
            close()
        }
        drawPath(
            path = chev,
            color = secondaryTrimColor.copy(alpha = safeAlpha * 0.92f)
        )
    }

    // 5. Rotating Mandala Sprite Core
    val drawSize = (visualRad * 2.55f).toInt().coerceAtLeast(1)
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
            alpha = safeAlpha,
            colorFilter = if (isGoldenOverdrive) ColorFilter.tint(Color(0xFFFDE047L), BlendMode.Modulate) else null,
            blendMode = BlendMode.Screen
        )
    }

    // 6. High-Contrast Multi-Ring Reactor Eye
    drawCircle(
        color = Color(0xFF090206L).copy(alpha = safeAlpha * 0.90f),
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

    // Subtle Orbital Drift gravitational reach halo around the hero
    drawCircle(
        color = (if (isGoldenOverdrive) Color(0xFFFACC15L) else Color(0xFF38BDF8L)).copy(alpha = 0.24f + beatKick * 0.18f),
        radius = p.radius * (2.5f + 0.35f * beatKick),
        center = center,
        style = Stroke(width = 1.6f + beatKick)
    )

    if (invulnTime > 0f) {
        val invulnPulse = 1f + 0.1f * sin(time * 12f)
        drawCircle(
            color = Color(0xFF4ADE80L).copy(alpha = 0.55f),
            radius = p.radius * 2.5f * invulnPulse,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
    }

    if (shield > 0f) {
        val shieldPulse = 1f + 0.08f * sin(time * 8f)
        drawCircle(
            color = applyCB(Color(0xFF818CF8L)).copy(alpha = 0.48f),
            radius = p.radius * 2.3f * shieldPulse,
            center = center,
            style = Stroke(width = 3.2f)
        )
    }

    val heroBitmap = when (skin) {
        "CYBER", "VINYL" -> heroCyberMandala
        "PRISM", "SERAPH" -> heroPrismMandala
        else -> heroSolarMandala
    }

    val pulse = 1f + 0.05f * sin(time * 6f) + 0.10f * beatKick
    val drawSize = (p.radius * 3.8f * pulse).toInt().coerceAtLeast(1)
    val halfSize = drawSize / 2
    val rotSpeed = if (isFocus) 22f else 54f

    // Bespoke Artist Avatar Architecture Underlay & Crown Geometry
    when (skin) {
        "VINYL" -> {
            // 45-RPM Spinning Holographic Vinyl Turntable Avatar
            val platterR = p.radius * (2.05f + 0.14f * beatKick)
            drawCircle(
                color = Color(0xFF050811L).copy(alpha = 0.92f * spriteAlpha),
                radius = platterR,
                center = center
            )
            for (g in 1..4) {
                val gr = platterR * (0.42f + g * 0.13f)
                drawCircle(
                    color = Color(0xFF38BDF8L).copy(alpha = (0.28f + (g % 2) * 0.18f) * spriteAlpha),
                    radius = gr,
                    center = center,
                    style = Stroke(width = 1.4f)
                )
            }
            // Spinning 24K Gold Center Label + Spindle
            drawCircle(
                color = Color(0xFFFACC15L).copy(alpha = 0.90f * spriteAlpha),
                radius = platterR * 0.34f,
                center = center
            )
            drawCircle(
                color = Color(0xFF030712L),
                radius = platterR * 0.10f,
                center = center
            )
            // Neon Turntable Tonearm Laser
            val armAng = time * 1.8f
            drawLine(
                color = Color(0xFF4ADE80L).copy(alpha = 0.85f * spriteAlpha),
                start = Offset(
                    center.x + cos(armAng) * platterR * 1.15f,
                    center.y + sin(armAng) * platterR * 1.15f
                ),
                end = Offset(
                    center.x + cos(armAng + 0.55f) * platterR * 0.45f,
                    center.y + sin(armAng + 0.55f) * platterR * 0.45f
                ),
                strokeWidth = 2.6f,
                cap = StrokeCap.Round
            )
        }
        "ECLIPSE" -> {
            // 24K Solar Eclipse Crown (Pure Obsidian Core + 12 Blazing Solar Corona Spikes)
            val coronaR = p.radius * (2.2f + 0.25f * beatKick)
            for (i in 0 until 12) {
                val ang = i * (PI.toFloat() * 2f / 12f) - time * 1.4f
                val len = coronaR * (if (i % 2 == 0) 1.32f else 1.05f)
                drawLine(
                    color = Color(0xFFFACC15L).copy(alpha = 0.85f * spriteAlpha),
                    start = Offset(
                        center.x + cos(ang) * (p.radius * 1.15f),
                        center.y + sin(ang) * (p.radius * 1.15f)
                    ),
                    end = Offset(
                        center.x + cos(ang) * len,
                        center.y + sin(ang) * len
                    ),
                    strokeWidth = if (i % 2 == 0) 3.0f else 1.8f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(
                color = Color(0xFF020308L),
                radius = p.radius * 1.22f,
                center = center
            )
            drawCircle(
                color = Color(0xFFFDE047L).copy(alpha = 0.95f * spriteAlpha),
                radius = p.radius * 1.25f,
                center = center,
                style = Stroke(width = 2.8f)
            )
        }
        "SERAPH" -> {
            // Twin Binary Pulse (Two Counter-Orbiting Harmonic Satellite Cores + Sine Tether)
            val orbitR = p.radius * (2.15f + 0.18f * beatKick)
            val satAngle = time * 3.4f
            val s1 = Offset(center.x + cos(satAngle) * orbitR, center.y + sin(satAngle) * orbitR)
            val s2 = Offset(center.x - cos(satAngle) * orbitR, center.y - sin(satAngle) * orbitR)
            drawLine(
                color = Color(0xFFE879F9L).copy(alpha = 0.68f * spriteAlpha),
                start = s1,
                end = s2,
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFF4ADE80L).copy(alpha = spriteAlpha), radius = 6.5f, center = s1)
            drawCircle(color = Color.White.copy(alpha = spriteAlpha), radius = 3.2f, center = s1)
            drawCircle(color = Color(0xFF38BDF8L).copy(alpha = spriteAlpha), radius = 6.5f, center = s2)
            drawCircle(color = Color.White.copy(alpha = spriteAlpha), radius = 3.2f, center = s2)
        }
    }

    withTransform({
        rotate(degrees = (time * rotSpeed) % 360f, pivot = center)
    }) {
        drawImage(
            image = heroBitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(heroBitmap.width, heroBitmap.height),
            dstOffset = IntOffset(p.x.toInt() - halfSize, p.y.toInt() - halfSize),
            dstSize = IntSize(drawSize, drawSize),
            alpha = if (skin == "ECLIPSE") spriteAlpha * 0.55f else spriteAlpha,
            colorFilter = if (skin == "ECLIPSE" || isGoldenOverdrive) {
                ColorFilter.tint(Color(0xFFFDE047L), BlendMode.Modulate)
            } else null,
            blendMode = BlendMode.Screen
        )
    }
}

/**
 * Simple Beauty Center-Top Callout Renderer (used only for subtle song section headers if needed).
 */
fun DrawScope.drawFloatingTexts(floatingTexts: List<FloatingText>) {
    val active = floatingTexts.lastOrNull() ?: return
    val alpha = (active.life.coerceIn(0f, 1f) * 255).toInt()
    if (alpha <= 5) return

    val cx = size.width * 0.5f
    val cy = size.height * 0.205f
    val lifeEase = active.life.coerceIn(0f, 1f)

    val pillW = (active.text.length * (if (active.isLarge) 13.5f else 11f) + 48f).coerceIn(160f, size.width * 0.86f)
    val pillH = if (active.subtitle.isNotEmpty()) 54f else 38f
    drawRoundRect(
        color = Color(0xCC03070EL).copy(alpha = (lifeEase * 0.74f).coerceIn(0f, 0.74f)),
        topLeft = Offset(cx - pillW / 2f, cy - 26f),
        size = Size(pillW, pillH),
        cornerRadius = CornerRadius(50f, 50f)
    )
    drawRoundRect(
        color = active.color.copy(alpha = (lifeEase * 0.62f).coerceIn(0f, 0.62f)),
        topLeft = Offset(cx - pillW / 2f, cy - 26f),
        size = Size(pillW, pillH),
        cornerRadius = CornerRadius(50f, 50f),
        style = Stroke(width = 1.6f)
    )

    drawIntoCanvas { canvas ->
        cachedBannerPaint.textSize = if (active.isLarge) 34f else 26f
        cachedBannerPaint.color = android.graphics.Color.argb((alpha * 0.70f).toInt(), 79, 70, 229)
        canvas.nativeCanvas.drawText(active.text, cx + 2.5f, cy + 2.5f, cachedBannerPaint)

        cachedBannerPaint.color = android.graphics.Color.argb(
            alpha,
            (active.color.red * 255).toInt(),
            (active.color.green * 255).toInt(),
            (active.color.blue * 255).toInt()
        )
        canvas.nativeCanvas.drawText(active.text, cx, cy, cachedBannerPaint)

        if (active.subtitle.isNotEmpty()) {
            cachedSubPaint.textSize = 19f
            cachedSubPaint.color = android.graphics.Color.argb((alpha * 0.88f).toInt(), 226, 232, 240)
            canvas.nativeCanvas.drawText(active.subtitle, cx, cy + 20f, cachedSubPaint)
        }
    }
}
