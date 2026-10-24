package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import kotlin.math.cos
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class ArcadeButtonPalette {
    EMERALD_PULSE,    // Forest Green (#22C55E)
    SOLAR_GOLD,       // Electric Blueish-Purple (#6366F1)
    ASTRAL_CYAN,      // Crisp Indigo-Slate (#818CF8)
    VELVET_AMETHYST   // Amethyst Violet (#A855F7)
}

enum class ArcadeIconType {
    PAUSE,
    CLOSE,
    RESTART,
    PLAY
}

private data class ButtonStyleColors(
    val surfaceLeft: Color,
    val surfaceRight: Color,
    val accent: Color,
    val borderLight: Color,
    val borderDark: Color
)

private fun paletteColors(palette: ArcadeButtonPalette): ButtonStyleColors {
    return when (palette) {
        ArcadeButtonPalette.EMERALD_PULSE -> ButtonStyleColors(
            surfaceLeft = Color(0xFF071F12),
            surfaceRight = Color(0xFF040B08),
            accent = Color(0xFF22C55E),
            borderLight = Color(0xFF4ADE80),
            borderDark = Color(0xFF15803D)
        )
        ArcadeButtonPalette.SOLAR_GOLD -> ButtonStyleColors(
            surfaceLeft = Color(0xFF101334),
            surfaceRight = Color(0xFF050714),
            accent = Color(0xFF6366F1),
            borderLight = Color(0xFFA5B4FC),
            borderDark = Color(0xFF4338CA)
        )
        ArcadeButtonPalette.ASTRAL_CYAN -> ButtonStyleColors(
            surfaceLeft = Color(0xFF0A1024),
            surfaceRight = Color(0xFF040710),
            accent = Color(0xFF818CF8),
            borderLight = Color(0xFFC7D2FE),
            borderDark = Color(0xFF3730A3)
        )
        ArcadeButtonPalette.VELVET_AMETHYST -> ButtonStyleColors(
            surfaceLeft = Color(0xFF190B2E),
            surfaceRight = Color(0xFF070410),
            accent = Color(0xFFA855F7),
            borderLight = Color(0xFFD8B4FE),
            borderDark = Color(0xFF6B21A8)
        )
    }
}

private fun detectPalette(text: String, bitmap: ImageBitmap): ArcadeButtonPalette {
    val upper = text.uppercase()
    when {
        upper.contains("PLAY") || upper.contains("RESUME") || upper.contains("LAUNCH") ||
            upper.contains("NEXT") || upper.contains("GOT IT") || upper.contains("PREVIEW") ||
            upper.contains("PAUSE") || upper.contains("LISTEN") || upper.contains("DROP") -> return ArcadeButtonPalette.EMERALD_PULSE
        upper.contains("SHOWCASE") || upper.contains("ALBUM") || upper.contains("SAVE") ||
            upper.contains("UNLOCK (") || upper.contains("BUY") -> return ArcadeButtonPalette.SOLAR_GOLD
        upper.contains("SETTINGS") || upper.contains("MAIN MENU") -> return ArcadeButtonPalette.VELVET_AMETHYST
        upper.contains("HOW TO") || upper.contains("CONTINUE") || upper.contains("POINTS") ||
            upper.contains("BACK") || upper.contains("DON'T SHOW") || upper.contains("UNLOCK ALL") -> return ArcadeButtonPalette.ASTRAL_CYAN
    }
    return runCatching {
        val pm = bitmap.toPixelMap(
            startX = bitmap.width / 2,
            startY = bitmap.height / 2,
            width = 1,
            height = 1
        )
        val c = pm[0, 0]
        when {
            c.red > 0.7f && c.green > 0.45f -> ArcadeButtonPalette.SOLAR_GOLD
            c.red > 0.6f && c.blue > 0.35f -> ArcadeButtonPalette.VELVET_AMETHYST
            c.green > 0.55f && c.blue < 0.55f -> ArcadeButtonPalette.EMERALD_PULSE
            else -> ArcadeButtonPalette.ASTRAL_CYAN
        }
    }.getOrDefault(ArcadeButtonPalette.ASTRAL_CYAN)
}

private fun detectIconType(bitmap: ImageBitmap): ArcadeIconType {
    return runCatching {
        val pm = bitmap.toPixelMap()
        val c = pm[bitmap.width / 2, (bitmap.height * 0.75f).toInt().coerceAtMost(bitmap.height - 1)]
        when {
            c.red > 0.65f -> ArcadeIconType.CLOSE
            c.green > 0.65f && c.blue < 0.45f -> ArcadeIconType.RESTART
            else -> ArcadeIconType.PAUSE
        }
    }.getOrDefault(ArcadeIconType.PAUSE)
}

/**
 * Architectural Dark-Glass Bento Action Button (replaces bubble/AI-style pills with sharp geometric glass).
 */
@Composable
fun ArcadePillButton(
    text: String,
    pillBitmap: ImageBitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().height(46.dp),
    textColor: Color = Color.White,
    icon: ImageBitmap? = null,
    fontSize: TextUnit = 13.5.sp,
    enabled: Boolean = true,
    palette: ArcadeButtonPalette? = null
) {
    val resolvedPalette = remember(text, pillBitmap, palette) {
        palette ?: detectPalette(text, pillBitmap)
    }
    val colors = remember(resolvedPalette) { paletteColors(resolvedPalette) }
    val cardShape = RoundedCornerShape(12.dp)
    val contentAlpha = if (enabled) 1f else 0.45f

    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = cardShape,
                ambientColor = colors.accent.copy(alpha = 0.45f),
                spotColor = colors.accent.copy(alpha = 0.45f)
            )
            .clip(cardShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.surfaceLeft.copy(alpha = contentAlpha),
                        colors.surfaceRight.copy(alpha = contentAlpha)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        colors.borderLight.copy(alpha = 0.82f * contentAlpha),
                        colors.borderDark.copy(alpha = 0.48f * contentAlpha)
                    )
                ),
                shape = cardShape
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Subtle left architectural laser bar accent
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 5.dp)
                .width(3.dp)
                .fillMaxHeight(0.54f)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.accent.copy(alpha = 0.90f * contentAlpha))
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            if (icon != null) {
                Canvas(
                    modifier = Modifier
                        .size(15.dp)
                        .padding(end = 2.dp)
                ) {
                    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    val radius = size.minDimension * 0.38f
                    drawArc(
                        color = colors.borderLight.copy(alpha = contentAlpha),
                        startAngle = -45f,
                        sweepAngle = 285f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = stroke
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor.copy(alpha = contentAlpha),
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ArcadeAdPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().height(54.dp),
    fontSize: TextUnit = 13.sp,
    enabled: Boolean = true
) {
    // Google Play / AdMob Compliant Rewarded Ad Button:
    // Visually distinct from standard menu pills via a warm Amber-Gold Broadcast frame,
    // high-contrast solid yellow [AD] disclosure badge, and explicit "SPONSORED VIDEO" sublabel.
    val cardShape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = Color(0xFFF59E0B),
                spotColor = Color(0xFFFACC15)
            )
            .clip(cardShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF291804),
                        Color(0xFF1F1203),
                        Color(0xFF2D1B05)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFACC15), Color(0xFFF59E0B), Color(0xFFFDE047))
                ),
                shape = cardShape
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFFACC15))
                        .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(5.dp))
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AD",
                        color = Color(0xFF090D16),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = text,
                        color = Color(0xFFFEF08A),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "SPONSORED VIDEO • INSTANT REVIVE",
                        color = Color(0xFFFBBF24).copy(alpha = 0.90f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = "▶",
                color = Color(0xFFFACC15),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ArcadeIconButton(
    iconBitmap: ImageBitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(42.dp),
    enabled: Boolean = true,
    iconType: ArcadeIconType? = null
) {
    val resolvedIcon = remember(iconBitmap, iconType) {
        iconType ?: detectIconType(iconBitmap)
    }
    val shape = RoundedCornerShape(11.dp)
    val accentColor = when (resolvedIcon) {
        ArcadeIconType.CLOSE -> Color(0xFFA855F7)
        ArcadeIconType.RESTART -> Color(0xFF22C55E)
        ArcadeIconType.PLAY -> Color(0xFF22C55E)
        ArcadeIconType.PAUSE -> Color(0xFF6366F1)
    }

    Box(
        modifier = modifier
            .shadow(4.dp, shape, ambientColor = accentColor, spotColor = accentColor)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF20B101D),
                        Color(0xF2030509)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.80f),
                        accentColor.copy(alpha = 0.32f)
                    )
                ),
                shape = shape
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.44f)) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.3.dp.toPx()

            when (resolvedIcon) {
                ArcadeIconType.PAUSE -> {
                    val barW = w * 0.24f
                    val barH = h * 0.82f
                    val topY = (h - barH) / 2f
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.16f, topY),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.60f, topY),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                }
                ArcadeIconType.CLOSE -> {
                    val pad = w * 0.15f
                    drawLine(
                        color = Color.White,
                        start = Offset(pad, pad),
                        end = Offset(w - pad, h - pad),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w - pad, pad),
                        end = Offset(pad, h - pad),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
                ArcadeIconType.RESTART -> {
                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    val r = size.minDimension * 0.42f
                    drawArc(
                        color = Color.White,
                        startAngle = -40f,
                        sweepAngle = 280f,
                        useCenter = false,
                        topLeft = Offset(center.x - r, center.y - r),
                        size = Size(r * 2f, r * 2f),
                        style = stroke
                    )
                }
                ArcadeIconType.PLAY -> {
                    val path = Path().apply {
                        moveTo(w * 0.22f, h * 0.14f)
                        lineTo(w * 0.86f, h * 0.50f)
                        lineTo(w * 0.22f, h * 0.86f)
                        close()
                    }
                    drawPath(path, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MiniGameplayVideoReel(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(176.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "miniVideoReel")
    val reelSec by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reelSec"
    )

    val chapterIdx = (reelSec / 4f).toInt().coerceIn(0, 2)
    val chapterProgress = (reelSec % 4f) / 4f
    val chapterTitle = when (chapterIdx) {
        0 -> "SCENE 1/3 • CATCH NOTES ON THE BEAT STRIKE LINE"
        1 -> "SCENE 2/3 • HOLD & FOLLOW SUSTAIN SLIDE RIBBONS"
        else -> "SCENE 3/3 • GRAB 💥 BOMBS TO VAPORIZE RED HAZARDS"
    }
    val chapterSub = when (chapterIdx) {
        0 -> "Wait for notes to cross the glowing lower Beat Line for ★ PERFECT ON-BEAT!"
        1 -> "Steer onto the note head and stay inside the scrolling golden ribbon tail!"
        else -> "💥 BOMB destroys all red enemies! 🛡️ SHIELD blocks 1 collision!"
    }
    val chapterAccent = when (chapterIdx) {
        0 -> Color(0xFF4ADE80)
        1 -> Color(0xFFFACC15)
        else -> Color(0xFFF97316)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF050811))
            .border(1.5.dp, chapterAccent.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
    ) {
        // Top Video Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1120))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(chapterAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = chapterTitle,
                    color = Color(0xFFFAF8F5),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
            }
            Text(
                text = "▶ MINI-VIDEO",
                color = chapterAccent,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Animated Gameplay Stage Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val horizonY = h * 0.14f
                val strikeY = h * 0.76f
                val topHalfW = w * 0.15f
                val strikeHalfW = w * 0.42f

                fun hwyPt(lane: Float, u: Float): Offset {
                    val p = u.coerceIn(0f, 1.2f)
                    val hw = topHalfW + (strikeHalfW - topHalfW) * p
                    val ln = ((lane - 1.5f) / 1.5f) * 0.78f
                    return Offset(w * 0.5f + ln * hw, horizonY + (strikeY - horizonY) * p)
                }

                // Draw perspective highway deck
                val tl = hwyPt(-0.4f, 0f)
                val tr = hwyPt(3.4f, 0f)
                val bl = hwyPt(-0.4f, 1.15f)
                val br = hwyPt(3.4f, 1.15f)
                val deck = Path().apply {
                    moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bl.x, bl.y); close()
                }
                drawPath(deck, color = Color(0xFF0A1224))
                val laneCols = listOf(Color(0xFF22C55E), Color(0xFFFB7185), Color(0xFFFACC15), Color(0xFF38BDF8))
                for (l in 0..3) {
                    drawLine(
                        color = laneCols[l].copy(alpha = 0.45f),
                        start = hwyPt(l.toFloat(), 0f),
                        end = hwyPt(l.toFloat(), 1.15f),
                        strokeWidth = 2f
                    )
                }
                // Strike Line
                val sl = hwyPt(-0.4f, 1.0f)
                val sr = hwyPt(3.4f, 1.0f)
                drawLine(color = Color(0xFF38BDF8).copy(alpha = 0.4f), start = sl, end = sr, strokeWidth = 9f, cap = StrokeCap.Round)
                drawLine(color = Color.White, start = sl, end = sr, strokeWidth = 2.5f, cap = StrokeCap.Round)
                for (l in 0..3) {
                    val rp = hwyPt(l.toFloat(), 1.0f)
                    drawCircle(color = laneCols[l], radius = 9f, center = rp, style = Stroke(width = 2.2f))
                }

                if (chapterIdx == 0) {
                    // Chapter 1: Notes scrolling down to Beat Strike Line + PERFECT ON-BEAT flame!
                    val u1 = (chapterProgress * 1.45f).coerceIn(0f, 1.15f)
                    val notePt = hwyPt(1f, u1)
                    val shipX = w * 0.5f + (notePt.x - w * 0.5f) * (chapterProgress * 1.4f).coerceIn(0f, 1f)
                    val shipPt = Offset(shipX, strikeY)

                    if (u1 < 1.02f) {
                        drawCircle(color = Color(0xFFFB7185), radius = 11f, center = notePt)
                        drawCircle(color = Color.White, radius = 5f, center = notePt)
                    } else {
                        // Hit explosion at Strike Line!
                        val ringR = 14f + (u1 - 1.0f) * 190f
                        drawCircle(color = Color(0xFFFACC15).copy(alpha = 0.85f), radius = ringR, center = shipPt, style = Stroke(width = 3.5f))
                        drawLine(
                            color = Color(0xFF4ADE80),
                            start = Offset(shipPt.x, shipPt.y - 52f),
                            end = shipPt,
                            strokeWidth = 10f,
                            cap = StrokeCap.Round
                        )
                    }
                    // Player core
                    drawCircle(color = Color(0xFF22C55E), radius = 12f, center = shipPt)
                    drawCircle(color = Color.White, radius = 5f, center = shipPt)
                } else if (chapterIdx == 1) {
                    // Chapter 2: Hold & Slide Sustain Ribbon!
                    val uHead = (0.25f + chapterProgress * 0.95f)
                    val uTail = (uHead - 0.55f).coerceAtLeast(0.05f)
                    val rPath = Path()
                    var shipPt = Offset(w * 0.5f, strikeY)
                    for (i in 0..10) {
                        val s = i / 10f
                        val uS = uTail + (uHead - uTail) * s
                        val ln = 0.6f + 1.8f * (0.5f - 0.5f * cos(s * 3.14159f))
                        val pt = hwyPt(ln, uS)
                        if (i == 0) rPath.moveTo(pt.x, pt.y) else rPath.lineTo(pt.x, pt.y)
                        if (i == 7) shipPt = pt
                    }
                    drawPath(rPath, color = Color(0xFFFACC15).copy(alpha = 0.45f), style = Stroke(width = 18f, cap = StrokeCap.Round))
                    drawPath(rPath, color = Color(0xFF38BDF8), style = Stroke(width = 7f, cap = StrokeCap.Round))
                    drawCircle(color = Color(0xFF22C55E), radius = 12f, center = shipPt)
                    drawCircle(color = Color.White, radius = 6f, center = shipPt)
                    drawCircle(color = Color(0xFFFACC15), radius = 19f, center = shipPt, style = Stroke(width = 2.5f))
                } else {
                    // Chapter 3: Grabbing 💥 BOMB vaporizes Red Spiked Hazards!
                    val uBomb = (chapterProgress * 1.35f).coerceIn(0f, 1.0f)
                    val bombPt = hwyPt(2f, uBomb)
                    val shipPt = Offset(bombPt.x, strikeY)
                    val detonated = chapterProgress >= 0.72f

                    if (!detonated) {
                        drawCircle(color = Color(0xFFF97316), radius = 13f, center = bombPt)
                        drawCircle(color = Color(0xFFFACC15), radius = 6f, center = bombPt)
                        // 2 incoming red hazards
                        val e1 = Offset(w * 0.25f + chapterProgress * 45f, h * 0.42f + chapterProgress * 35f)
                        val e2 = Offset(w * 0.76f - chapterProgress * 45f, h * 0.48f + chapterProgress * 30f)
                        drawCircle(color = Color(0xFFFF1E42), radius = 11f, center = e1)
                        drawCircle(color = Color(0xFFFF1E42), radius = 11f, center = e2)
                    } else {
                        val blastT = (chapterProgress - 0.72f) / 0.28f
                        drawCircle(
                            color = Color(0xFFF97316).copy(alpha = (1f - blastT).coerceIn(0f, 1f)),
                            radius = 20f + blastT * 160f,
                            center = shipPt,
                            style = Stroke(width = 4.5f)
                        )
                    }
                    drawCircle(color = Color(0xFF22C55E), radius = 12f, center = shipPt)
                    drawCircle(color = Color.White, radius = 5f, center = shipPt)
                }
            }
        }

        // Caption & Scrubber Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090E1A))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = chapterSub,
                color = Color(0xFFCBD5E1),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (c in 0..2) {
                    val fillFrac = when {
                        c < chapterIdx -> 1f
                        c == chapterIdx -> chapterProgress
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fillFrac)
                                .background(chapterAccent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TouchControlHintBanner(
    btnPillCyan: ImageBitmap,
    btnPillGreen: ImageBitmap,
    onDismissPermanently: () -> Unit,
    onDismissOnce: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val popScale = remember { Animatable(0.32f) }
    val popAlpha = remember { Animatable(0f) }
    var isClosing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            popAlpha.animateTo(1f, animationSpec = tween(180))
        }
        popScale.animateTo(
            targetValue = 1.03f,
            animationSpec = tween(210, easing = FastOutSlowInEasing)
        )
        popScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    fun triggerZoomOutAndDismiss(permanent: Boolean) {
        if (isClosing) return
        isClosing = true
        scope.launch {
            popScale.animateTo(1.05f, animationSpec = tween(90, easing = FastOutSlowInEasing))
            launch {
                popAlpha.animateTo(0f, animationSpec = tween(150))
            }
            popScale.animateTo(0.25f, animationSpec = tween(155, easing = FastOutSlowInEasing))
            if (permanent) onDismissPermanently() else onDismissOnce()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE604060C).copy(alpha = popAlpha.value * 0.90f))
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = popScale.value,
                    scaleY = popScale.value,
                    alpha = popAlpha.value
                )
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = Color(0xFFEAB308),
                    spotColor = Color(0xFF22C55E)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFA0C101D),
                            Color(0xFA05070E)
                        )
                    )
                )
                .border(
                    width = 1.6.dp,
                    color = Color(0xFFEAB308).copy(alpha = 0.78f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "STAGE BRIEFING • BAND HERO RHYTHM HIGHWAY",
                color = Color(0xFFEAB308),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "HIT THE BEAT STRIKE LINE",
                color = Color(0xFFFAF8F5),
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.0.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 12-Second Looping Mini-Video Gameplay Reel
            MiniGameplayVideoReel()

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Unmistakable Item & Timing Rules
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickHintRow(
                    badge = "♪",
                    badgeColor = Color(0xFF4ADE80),
                    title = "BEAT LINE = ★ PERFECT ON-BEAT (+200)",
                    desc = "Catch scrolling fret notes right as they cross the lower Beat Strike Line."
                )
                QuickHintRow(
                    badge = "💥",
                    badgeColor = Color(0xFFF97316),
                    title = "💥 BOMB = VAPORIZES ALL ENEMIES",
                    desc = "Grab any pickup labeled 💥 BOMB to blow up every red hazard on screen!"
                )
                QuickHintRow(
                    badge = "🛡️",
                    badgeColor = Color(0xFF38BDF8),
                    title = "🛡️ SHIELD & SUSTAIN RIBBONS",
                    desc = "🛡️ SHIELD blocks 1 hit. Follow HOLD & SLIDE ♪ ribbons for rapid bonus points!"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ArcadePillButton(
                    text = "DROP THE BEAT ▶",
                    pillBitmap = btnPillGreen,
                    onClick = { triggerZoomOutAndDismiss(permanent = false) },
                    modifier = Modifier
                        .weight(1.25f)
                        .height(44.dp),
                    fontSize = 12.sp,
                    palette = ArcadeButtonPalette.EMERALD_PULSE
                )
                ArcadePillButton(
                    text = "GOT IT (HIDE)",
                    pillBitmap = btnPillCyan,
                    onClick = { triggerZoomOutAndDismiss(permanent = true) },
                    modifier = Modifier
                        .weight(1.05f)
                        .height(44.dp),
                    fontSize = 10.5.sp,
                    palette = ArcadeButtonPalette.ASTRAL_CYAN
                )
            }
        }
    }
}

@Composable
private fun QuickHintRow(
    badge: String,
    badgeColor: Color,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF060B14))
            .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.16f))
                .border(1.dp, badgeColor.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                color = badgeColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = desc,
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

