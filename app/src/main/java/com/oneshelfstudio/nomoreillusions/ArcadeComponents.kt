package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
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
    modifier: Modifier = Modifier.fillMaxWidth().height(46.dp),
    fontSize: TextUnit = 13.sp,
    enabled: Boolean = true
) {
    val cardShape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = Color(0xFF22C55E),
                spotColor = Color(0xFF6366F1)
            )
            .clip(cardShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF092214),
                        Color(0xFF101432)
                    )
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF4ADE80), Color(0xFF818CF8))
                ),
                shape = cardShape
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.18f))
                    .border(1.dp, Color(0xFF4ADE80), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AD ▶",
                    color = Color(0xFF4ADE80),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.9.sp,
                maxLines = 1
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

    // Elastic Zoom-In Bounce on appear
    LaunchedEffect(Unit) {
        launch {
            popAlpha.animateTo(1f, animationSpec = tween(180))
        }
        popScale.animateTo(
            targetValue = 1.06f,
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        )
        popScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Gentle idle zoom-in/out breathing bounce while open
    val infiniteTransition = rememberInfiniteTransition(label = "popupBounce")
    val idleBreath by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleBreath"
    )

    fun triggerZoomOutAndDismiss(permanent: Boolean) {
        if (isClosing) return
        isClosing = true
        scope.launch {
            // Zoom-in anticipation pop before zooming out
            popScale.animateTo(1.08f, animationSpec = tween(95, easing = FastOutSlowInEasing))
            launch {
                popAlpha.animateTo(0f, animationSpec = tween(160))
            }
            popScale.animateTo(0.25f, animationSpec = tween(165, easing = FastOutSlowInEasing))
            if (permanent) onDismissPermanently() else onDismissOnce()
        }
    }

    val combinedScale = if (isClosing) popScale.value else (popScale.value * idleBreath)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD902050B).copy(alpha = popAlpha.value * 0.86f))
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = combinedScale,
                    scaleY = combinedScale,
                    alpha = popAlpha.value
                )
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color(0xFF38BDF8),
                    spotColor = Color(0xFF22C55E)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFA091326),
                            Color(0xFA040812)
                        )
                    )
                )
                .border(
                    width = 1.8.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF4ADE80), Color(0xFF38BDF8), Color(0xFF818CF8))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF0E291E))
                    .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.7f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = Color(0xFF4ADE80),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "GLIDE TO THE BEAT",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Drag your finger anywhere on screen to steer your core. The countdown will start as soon as you're ready!",
                color = Color(0xFFCBD5E1),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Quick Visual Guide Pills
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickHintRow(
                    badge = "●",
                    badgeColor = Color(0xFF22D3EE),
                    title = "CATCH CYAN & GOLD ORBS",
                    desc = "Collect smooth glowing orbs on the beat to score & power up."
                )
                QuickHintRow(
                    badge = "◆",
                    badgeColor = Color(0xFFFF1E42),
                    title = "DODGE RED SPIKED ENEMIES",
                    desc = "Watch the screen edges for red warning ripples before foes lunge."
                )
                QuickHintRow(
                    badge = "⚡",
                    badgeColor = Color(0xFFA855F7),
                    title = "HOLD FOCUS TO SLOW TIME",
                    desc = "Press & hold the bottom-right Focus button in tight spots."
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ArcadePillButton(
                    text = "START COUNTDOWN",
                    pillBitmap = btnPillGreen,
                    onClick = { triggerZoomOutAndDismiss(permanent = false) },
                    modifier = Modifier
                        .weight(1.25f)
                        .height(44.dp),
                    fontSize = 12.sp,
                    palette = ArcadeButtonPalette.EMERALD_PULSE
                )
                ArcadePillButton(
                    text = "DON'T SHOW AGAIN",
                    pillBitmap = btnPillCyan,
                    onClick = { triggerZoomOutAndDismiss(permanent = true) },
                    modifier = Modifier
                        .weight(1.1f)
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

