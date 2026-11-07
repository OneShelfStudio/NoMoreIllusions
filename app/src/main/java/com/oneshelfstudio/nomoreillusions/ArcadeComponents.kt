package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.text.style.TextAlign
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
    val haptics = rememberStudioHaptics()
    val resolvedPalette = remember(text, pillBitmap, palette) {
        palette ?: detectPalette(text, pillBitmap)
    }
    val colors = remember(resolvedPalette) { paletteColors(resolvedPalette) }
    val cardShape = RoundedCornerShape(14.dp)
    val contentAlpha = if (enabled) 1f else 0.45f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ArcadePillScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptics.confirm()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp)
                .width(3.dp)
                .fillMaxHeight(0.52f)
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
    val haptics = rememberStudioHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "AdPillScale"
    )
    val cardShape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .scale(scale)
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptics.confirm()
                onClick()
            }
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
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFACC15))
                        .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(6.dp))
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
    val haptics = rememberStudioHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.90f else 1f,
        label = "ArcadeIconScale"
    )
    val resolvedIcon = remember(iconBitmap, iconType) {
        iconType ?: detectIconType(iconBitmap)
    }
    val shape = RoundedCornerShape(12.dp)
    val accentColor = when (resolvedIcon) {
        ArcadeIconType.CLOSE -> Color(0xFFA855F7)
        ArcadeIconType.RESTART -> Color(0xFF22C55E)
        ArcadeIconType.PLAY -> Color(0xFF22C55E)
        ArcadeIconType.PAUSE -> Color(0xFF38BDF8)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(4.dp, shape, ambientColor = accentColor, spotColor = accentColor)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF20D182C),
                        Color(0xF2040812)
                    )
                )
            )
            .border(
                width = 1.3.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.85f),
                        accentColor.copy(alpha = 0.35f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptics.tick()
                onClick()
            },
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
                    val pad = w * 0.16f
                    val cross = Path().apply {
                        moveTo(pad, pad)
                        lineTo(w - pad, h - pad)
                        moveTo(w - pad, pad)
                        lineTo(pad, h - pad)
                    }
                    drawPath(
                        path = cross,
                        color = Color.White,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun TouchControlHintBanner(
    btnPillCyan: ImageBitmap,
    btnPillGreen: ImageBitmap,
    onDismissPermanently: () -> Unit,
    onDismissOnce: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val popScale = remember { Animatable(0.68f) }
    val popAlpha = remember { Animatable(0f) }
    var isClosing by remember { mutableStateOf(false) }

    val infinite = rememberInfiniteTransition(label = "hintEmblemAnim")
    val animPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "phase"
    )

    LaunchedEffect(Unit) {
        launch {
            popAlpha.animateTo(1f, animationSpec = tween(180))
        }
        popScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    fun triggerZoomOutAndDismiss(permanent: Boolean) {
        if (isClosing) return
        isClosing = true
        scope.launch {
            launch {
                popAlpha.animateTo(0f, animationSpec = tween(150))
            }
            popScale.animateTo(0.75f, animationSpec = tween(155, easing = FastOutSlowInEasing))
            if (permanent) onDismissPermanently() else onDismissOnce()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE802050C).copy(alpha = popAlpha.value * 0.90f))
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        val shape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = popScale.value,
                    scaleY = popScale.value,
                    alpha = popAlpha.value
                )
                .shadow(
                    elevation = 20.dp,
                    shape = shape,
                    ambientColor = Color(0xFFFACC15),
                    spotColor = Color(0xFF38BDF8)
                )
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFA0E1C33),
                            Color(0xFA081122),
                            Color(0xFA03070E)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFFACC15).copy(alpha = 0.85f),
                            Color(0xFF38BDF8).copy(alpha = 0.42f),
                            Color(0xFF1E293B).copy(alpha = 0.65f)
                        )
                    ),
                    shape = shape
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFFACC15).copy(alpha = 0.16f))
                    .border(1.dp, Color(0xFFFACC15).copy(alpha = 0.65f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STAGE BRIEFING",
                    color = Color(0xFFFACC15),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "GLIDE TO THE RHYTHM",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "Catch orbs on the beat • Unlock two slow-mo video montages",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickHintRow(
                    emblemKind = 0,
                    animPhase = animPhase,
                    badgeColor = Color(0xFF4ADE80),
                    title = "FLUID GLIDE & DUAL TOUCH",
                    desc = "Slide anywhere (or use 2 fingers) to catch incoming beat orbs & dodge red hazards."
                )
                QuickHintRow(
                    emblemKind = 2,
                    animPhase = animPhase,
                    badgeColor = Color(0xFFFACC15),
                    title = "24K GOLD MILESTONE ORBS",
                    desc = "Catch upper-center 24K Orbs at 0:15 & 1:18 to trigger golden ripples & overdrive."
                )
                QuickHintRow(
                    emblemKind = 3,
                    animPhase = animPhase,
                    badgeColor = Color(0xFFE879F9),
                    title = "5/5 VHS MONTAGE RELICS",
                    desc = "Collect 5 chromatic VHS Relics per window to launch slow-mo music video clips!"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudioPillButton(
                    text = "DROP IN ▶",
                    palette = StudioPillPalette.EMERALD_STAGE,
                    onClick = { triggerZoomOutAndDismiss(permanent = false) },
                    modifier = Modifier
                        .weight(1.22f)
                        .height(46.dp),
                    fontSize = 12.sp
                )
                StudioPillButton(
                    text = "DON'T SHOW",
                    palette = StudioPillPalette.SLATE_OBSIDIAN,
                    onClick = { triggerZoomOutAndDismiss(permanent = true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

@Composable
private fun QuickHintRow(
    emblemKind: Int,
    animPhase: Float,
    badgeColor: Color,
    title: String,
    desc: String
) {
    StudioGlassCard(
        accentColor = badgeColor,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFF040915))
                    .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                LiveGuideEmblemCanvas(
                    emblemKind = emblemKind,
                    animPhase = animPhase,
                    accentColor = badgeColor,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = badgeColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = desc,
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    lineHeight = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
