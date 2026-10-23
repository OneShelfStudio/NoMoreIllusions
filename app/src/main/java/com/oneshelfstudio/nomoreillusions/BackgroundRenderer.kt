package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.*

private val sharedAuroraPath = Path()

fun getThemePalette(bgTheme: String, isFocus: Boolean): ThemeColors {
    return when (bgTheme) {
        "CYBER" -> ThemeColors(
            c1 = if (isFocus) Color(0xFF020705L) else Color(0xFF010403L),
            c2 = if (isFocus) Color(0xFF05180EL) else Color(0xFF031109L),
            c3 = if (isFocus) Color(0xFF071D12L) else Color(0xFF04120BL),
            nebTint = Color(0xFF22C55EL),
            starTint = Color(0xFF818CF8L)
        )
        "SUNSET" -> ThemeColors(
            c1 = if (isFocus) Color(0xFF07020AL) else Color(0xFF040106L),
            c2 = if (isFocus) Color(0xFF14051CL) else Color(0xFF0D0314L),
            c3 = if (isFocus) Color(0xFF090418L) else Color(0xFF05020EL),
            nebTint = Color(0xFFA855F7L),
            starTint = Color(0xFF22C55EL)
        )
        "PRISM" -> ThemeColors(
            c1 = if (isFocus) Color(0xFF03030CL) else Color(0xFF020208L),
            c2 = if (isFocus) Color(0xFF090820L) else Color(0xFF060516L),
            c3 = if (isFocus) Color(0xFF04140CL) else Color(0xFF020C07L),
            nebTint = Color(0xFF6366F1L),
            starTint = Color(0xFF4ADE80L)
        )
        "GOLD_VINYL" -> ThemeColors(
            c1 = if (isFocus) Color(0xFF080501L) else Color(0xFF050300L),
            c2 = if (isFocus) Color(0xFF1A1103L) else Color(0xFF120B02L),
            c3 = if (isFocus) Color(0xFF0E0902L) else Color(0xFF080501L),
            nebTint = Color(0xFFFACC15L),
            starTint = Color(0xFF38BDF8L)
        )
        "BLOOD_MOON" -> ThemeColors(
            c1 = if (isFocus) Color(0xFF090103L) else Color(0xFF050002L),
            c2 = if (isFocus) Color(0xFF1C040AL) else Color(0xFF120206L),
            c3 = if (isFocus) Color(0xFF0E0208L) else Color(0xFF070104L),
            nebTint = Color(0xFFF43F5EL),
            starTint = Color(0xFFFACC15L)
        )
        else -> ThemeColors(
            c1 = if (isFocus) Color(0xFF020504L) else Color(0xFF010302L),
            c2 = if (isFocus) Color(0xFF040D08L) else Color(0xFF030805L),
            c3 = if (isFocus) Color(0xFF04030EL) else Color(0xFF020208L),
            nebTint = Color(0xFF16A34AL),
            starTint = Color(0xFF6366F1L)
        )
    }
}

@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawNebulaBackground(
    nebula: ImageBitmap,
    starsBack: ImageBitmap,
    starsFront: ImageBitmap,
    time: Float,
    isFocus: Boolean,
    bgTheme: String = "OBSIDIAN",
    trackArtwork: ImageBitmap? = null,
    trackProgress: Float = 0f,
    juiceIntensity: Float = 0f,
    stageMode: StageMode = StageMode.ABYSSAL_WAVE,
    beatPhase: Float = 0f,
    goldenOverdriveIntensity: Float = 0f,
    beatFlashIntensity: Float = 0f,
    audioEnergy: Float = 0.45f,
    swellDelta: Float = 0f,
    activePhenomenon: ActivePhenomenon? = null,
    multiBand: MultiBandAudioState = MultiBandAudioState(rms = audioEnergy, swellDelta = swellDelta)
) {
    val basePalette = getThemePalette(bgTheme, isFocus)
    val isEclipseActive = (activePhenomenon?.effect == KeyWordEffect.ECLIPSE_DARK && activePhenomenon.timer > 0f) ||
        multiBand.bassDropOut
    val isConstellationActive = (activePhenomenon?.effect == KeyWordEffect.CONSTELLATION_STAR && activePhenomenon.timer > 0f) ||
        stageMode == StageMode.GOLDEN_OVERDRIVE ||
        goldenOverdriveIntensity > 0.25f

    val goldBlend = when {
        activePhenomenon?.effect == KeyWordEffect.CONSTELLATION_STAR -> (activePhenomenon.timer / 1.5f).coerceIn(0f, 1f)
        stageMode == StageMode.GOLDEN_OVERDRIVE -> 1f
        else -> goldenOverdriveIntensity.coerceIn(0f, 1f)
    }

    val palette = when {
        isEclipseActive -> ThemeColors(
            c1 = Color(0xFF010104L),
            c2 = Color(0xFF03020AL),
            c3 = Color(0xFF010105L),
            nebTint = Color(0xFF38BDF8L),
            starTint = Color(0xFFA855F7L)
        )
        isConstellationActive -> ThemeColors(
            c1 = Color(0xFF060401L),
            c2 = Color(0xFF140D02L),
            c3 = Color(0xFF0A0601L),
            nebTint = Color(0xFFFACC15L),
            starTint = Color(0xFF38BDF8L)
        )
        else -> basePalette
    }

    val energy = multiBand.rms.coerceIn(0.08f, 1.0f)
    val bass = multiBand.bass.coerceIn(0.0f, 1.0f)
    val vocal = multiBand.vocal.coerceIn(0.05f, 1.0f)
    val high = multiBand.high.coerceIn(0.0f, 1.0f)
    val swellBoost = (multiBand.swellDelta * 2.4f).coerceIn(0f, 0.70f)
    val beatPulse = if (beatPhase >= 0f) exp(-beatPhase * 4.5f).coerceIn(0f, 1f) else 0.2f

    // 1. Deep Obsidian Space Base
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(palette.c1, palette.c2, palette.c3)
        )
    )

    val centerX = size.width * 0.5f
    val centerY = size.height * 0.46f
    val maxDim = maxOf(size.width, size.height)

    // 2. Volumetric Radial Nebula Cloud (expands on Sub-Bass & Vocal swells; hushes during bassDropOut!)
    val nebulaAlpha = if (multiBand.bassDropOut) 0.06f else (0.05f + vocal * 0.16f + bass * 0.08f + swellBoost * 0.10f).coerceAtMost(0.32f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.nebTint.copy(alpha = nebulaAlpha),
                palette.starTint.copy(alpha = nebulaAlpha * 0.55f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = maxDim * (0.34f + vocal * 0.22f + bass * 0.12f + beatPulse * 0.05f)
        ),
        radius = maxDim * 0.72f,
        center = Offset(centerX, centerY),
        blendMode = BlendMode.Screen
    )

    // 3. SUB-BASS GRAVITY THUMP RINGS (driven directly by the 25–160Hz Sub-Bass Stem!)
    // When the kick & bass guitar pump, expanding gravity rings throb outward on the beat;
    // when the bass drops out (e.g. Confide 1:02–1:09 & 2:04–2:11), the rings vanish into zero-gravity silence!
    if (!multiBand.bassDropOut && bass > 0.22f) {
        val bassRingCount = 3
        for (bIdx in 0 until bassRingCount) {
            val bPhase = ((beatPhase + bIdx * 0.33f) % 1f)
            val bRad = minOf(size.width, size.height) * (0.14f + bPhase * (0.38f + bass * 0.22f))
            val bAlpha = (1f - bPhase) * (bass - 0.18f) * 0.36f
            val bCol = if (isConstellationActive) Color(0xFFFACC15L) else palette.nebTint
            drawCircle(
                color = bCol.copy(alpha = bAlpha.coerceIn(0f, 0.34f)),
                radius = bRad,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.6f + bass * 3.2f)
            )
        }
    }

    // 4. VOCAL-DRIVEN SOLAR AURORA BOREALIS SILK RIBBONS + NEURAL SYNAPSE NODES
    // Directly coupled to the 220–3200Hz Vocal/Midrange stem so the silk waves rise and bloom with Ethan's voice!
    val ribbonCount = 3
    for (rIdx in 0 until ribbonCount) {
        val baseY = size.height * (0.26f + rIdx * 0.24f)
        val phaseOffset = rIdx * 2.1f
        val waveAmp = size.height * (0.018f + vocal * 0.055f + swellBoost * 0.030f)
        val speed = 0.30f + vocal * 0.45f + high * 0.25f + rIdx * 0.08f

        val path = sharedAuroraPath.apply {
            reset()
            val segments = 24
            val stepX = size.width / segments
            var prevX = 0f
            var prevY = baseY
            for (s in 0..segments) {
                val x = s * stepX
                val nx = s.toFloat() / segments
                val y = baseY +
                    sin(nx * 4.2f + time * speed + phaseOffset) * waveAmp +
                    cos(nx * 2.5f - time * (speed * 0.65f) + phaseOffset) * (waveAmp * 0.55f)
                if (s == 0) {
                    moveTo(x, y)
                } else {
                    val midX = (prevX + x) * 0.5f
                    val midY = (prevY + y) * 0.5f
                    quadraticTo(prevX, prevY, midX, midY)
                    if (s == segments) lineTo(x, y)
                }
                prevX = x
                prevY = y
            }
        }

        val rCol1 = when {
            isConstellationActive -> Color(0xFFFACC15L)
            multiBand.bassDropOut -> Color(0xFF38BDF8L)
            else -> palette.nebTint
        }
        val rCol2 = when {
            isConstellationActive -> Color(0xFF38BDF8L)
            multiBand.bassDropOut -> Color(0xFFC084FCL)
            else -> palette.starTint
        }
        val silkAlpha = (0.05f + vocal * 0.18f + swellBoost * 0.12f).coerceAtMost(0.32f)

        // Outer soft volumetric silk aurora glow
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    rCol1.copy(alpha = silkAlpha * 0.42f),
                    rCol2.copy(alpha = silkAlpha * 0.38f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = (26f + vocal * 36f - rIdx * 5f), cap = StrokeCap.Round)
        )
        // Soft inner bioluminescent ribbon
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    rCol1.copy(alpha = (silkAlpha * 0.95f).coerceAtMost(0.38f)),
                    rCol2.copy(alpha = (silkAlpha * 0.85f).coerceAtMost(0.34f)),
                    Color.Transparent
                )
            ),
            style = Stroke(width = 4.5f + vocal * 5.0f, cap = StrokeCap.Round)
        )

        // Living Bioluminescent Bokeh Orbs drifting along the aurora currents (zero straight lines!)
        if (vocal > 0.34f || high > 0.32f) {
            val nodeCount = 7
            for (n in 0 until nodeCount) {
                val nx = ((n.toFloat() / nodeCount) + time * (0.045f + rIdx * 0.014f)) % 1f
                val px = nx * size.width
                val py = baseY +
                    sin(nx * 4.2f + time * speed + phaseOffset) * waveAmp +
                    cos(nx * 2.5f - time * (speed * 0.65f) + phaseOffset) * (waveAmp * 0.55f)
                val curPos = Offset(px, py)
                val nodeAlpha = ((maxOf(vocal, high) - 0.28f) * 0.80f).coerceIn(0.08f, 0.58f)
                val nCol = if (n % 2 == 0) rCol1 else rCol2

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = nodeAlpha * 0.85f),
                            nCol.copy(alpha = nodeAlpha * 0.42f),
                            Color.Transparent
                        ),
                        center = curPos,
                        radius = 12f + vocal * 10f
                    ),
                    radius = 12f + vocal * 10f,
                    center = curPos,
                    blendMode = BlendMode.Screen
                )
            }
        }
    }

    // 5. HIGH-SHIMMER KERR SINGULARITY SPIRAL GALAXY VORTEX (driven by High Shimmer 3.2–12kHz & Chorus Energy!)
    val galaxyDrive = maxOf(high * 1.15f, energy * 0.85f)
    if (galaxyDrive >= 0.46f && !multiBand.bassDropOut) {
        val vortexStrength = ((galaxyDrive - 0.44f) / 0.56f).coerceIn(0f, 1f)
        val armCount = 6
        val maxArmRadius = minOf(size.width, size.height) * (0.26f + vortexStrength * 0.26f)
        for (arm in 0 until armCount) {
            val startAngle = (arm.toFloat() / armCount) * 2f * PI.toFloat() + time * (0.38f + high * 0.35f)
            val armCol = when {
                isConstellationActive && arm % 2 == 0 -> Color(0xFFFDE047L)
                isConstellationActive -> Color(0xFF38BDF8L)
                arm % 2 == 0 -> palette.nebTint
                else -> palette.starTint
            }
            for (s in 1..14) {
                val t = s / 14f
                val r = 18f + t * maxArmRadius
                val a = startAngle - t * 2.4f
                val px = centerX + cos(a) * r
                val py = centerY + sin(a) * r
                val dotAlpha = (1f - t) * vortexStrength * (0.28f + beatPulse * 0.18f)
                drawCircle(
                    color = armCol.copy(alpha = dotAlpha.coerceIn(0f, 0.52f)),
                    radius = 2.2f + (1f - t) * 4.0f,
                    center = Offset(px, py)
                )
            }
        }
    }

    // 6. VOLUMETRIC CHORUS AURORA CROWNS & PERIMETER HARMONIC BLOOMS (Zero straight laser lines or boxy bars!)
    val overdriveFlash = maxOf(goldBlend, beatFlashIntensity.coerceIn(0f, 1f))
    if (overdriveFlash > 0.04f) {
        val bloomCount = 4
        for (b in 0 until bloomCount) {
            val angle = time * 0.65f + (b * PI.toFloat() / 2f)
            val orbitR = minOf(size.width, size.height) * (0.28f + 0.08f * sin(time * 1.8f + b))
            val bx = centerX + cos(angle) * orbitR
            val by = centerY + sin(angle) * orbitR
            val bloomCol = if (b % 2 == 0) Color(0xFFFACC15L) else Color(0xFF38BDF8L)
            val bloomRad = maxDim * (0.22f + 0.08f * beatPulse)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        bloomCol.copy(alpha = overdriveFlash * (0.18f + beatPulse * 0.14f)),
                        bloomCol.copy(alpha = overdriveFlash * 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(bx, by),
                    radius = bloomRad
                ),
                radius = bloomRad,
                center = Offset(bx, by),
                blendMode = BlendMode.Screen
            )
        }
    }

    // 7. Organic Concentric Harmonic Halo Waves (Replaces boxy rectangular squircle tunnel with smooth circular/arc ripples!)
    val ringCount = if (isEclipseActive) 4 else 6
    val ringSpeed = if (multiBand.bassDropOut) 0.035f else (0.06f + bass * 0.10f + high * 0.05f)
    for (i in 0 until ringCount) {
        val rawPhase = ((i.toFloat() / ringCount) + time * ringSpeed) % 1f
        val curvedPhase = rawPhase * rawPhase
        val waveRadius = curvedPhase * maxDim * (0.58f + bass * 0.06f)
        if (waveRadius < 18f) continue

        val isEven = i % 2 == 0
        val baseRingColor = when {
            isConstellationActive && isEven -> Color(0xFFFACC15L)
            isConstellationActive -> Color(0xFF38BDF8L)
            multiBand.bassDropOut -> Color(0xFF38BDF8L)
            isEven -> palette.nebTint
            else -> palette.starTint
        }

        val fadeAlpha = sin(rawPhase * PI.toFloat()).coerceIn(0f, 1f)
        val finalAlpha = (fadeAlpha * (0.04f + energy * 0.16f + swellBoost * 0.12f)).coerceIn(0f, 0.28f)
        val strokeW = 1.2f + rawPhase * (1.8f + bass * 2.4f)
        val rotDeg = (time * (18f + vocal * 16f) * (if (isEven) 1f else -1f) + i * 45f) % 360f

        for (petal in 0 until 3) {
            drawArc(
                color = baseRingColor.copy(alpha = finalAlpha),
                startAngle = rotDeg + petal * 120f,
                sweepAngle = 78f + vocal * 22f,
                useCenter = false,
                topLeft = Offset(centerX - waveRadius, centerY - waveRadius),
                size = Size(waveRadius * 2f, waveRadius * 2f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
    }

    // 8. Progressive Album Artwork Hologram Watermark Reveal in the Center
    if (trackArtwork != null && trackProgress > 0.01f) {
        val p = trackProgress.coerceIn(0f, 1f)
        val artBoxSize = minOf(size.width, size.height) * (0.46f + bass * 0.04f)
        val dstW = artBoxSize.toInt().coerceAtLeast(1)
        val dstH = artBoxSize.toInt().coerceAtLeast(1)
        val left = ((size.width - dstW) / 2f).toInt()
        val top = ((size.height - dstH) / 2f).toInt()

        drawImage(
            image = trackArtwork,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(trackArtwork.width, trackArtwork.height),
            dstOffset = IntOffset(left, top),
            dstSize = IntSize(dstW, dstH),
            alpha = (0.04f + p * 0.14f + vocal * 0.05f).coerceAtMost(0.24f),
            blendMode = BlendMode.Screen
        )
    }

    // 9. Focus-mode or Eclipse / Zero-Bass Breakdown deep edge vignette
    if (isFocus || isEclipseActive) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF010205L).copy(alpha = if (multiBand.bassDropOut) 0.62f else 0.76f)
                ),
                radius = maxDim * 0.52f
            )
        )
    }
}
