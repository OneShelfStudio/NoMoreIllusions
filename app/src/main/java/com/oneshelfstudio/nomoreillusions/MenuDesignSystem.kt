package com.oneshelfstudio.nomoreillusions

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random

val LocalStudioHapticsEnabled = compositionLocalOf { true }
val LocalStudioAudioEngine = compositionLocalOf<AudioEngine?> { null }

class StudioHaptics(
    private val vibrator: Vibrator?,
    private val enabled: Boolean,
    private val audioEngine: AudioEngine? = null
) {
    fun tick() {
        audioEngine?.playButtonSwoosh(isPrimary = false)
        val vib = vibrator ?: return
        if (!enabled) return
        runCatching {
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(12L, 110))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(12L)
            }
        }
    }

    fun confirm() {
        audioEngine?.playButtonSwoosh(isPrimary = true)
        val vib = vibrator ?: return
        if (!enabled) return
        runCatching {
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(22L, 180))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(22L)
            }
        }
    }
}

@Composable
fun rememberStudioHaptics(): StudioHaptics {
    val context = LocalContext.current
    val enabled = LocalStudioHapticsEnabled.current
    val audioEngine = LocalStudioAudioEngine.current
    val vibrator = remember(context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }.getOrNull()
    }
    return remember(vibrator, enabled, audioEngine) { StudioHaptics(vibrator, enabled, audioEngine) }
}

// ---------------------------------------------------------------------------
// 1. Color Palettes & Tokens (Starbloom-Grade 3D Shaders)
// ---------------------------------------------------------------------------

enum class StudioPillPalette {
    EMERALD_STAGE,    // High-energy Play / Launch
    SOLAR_GOLD,       // 24K Gold Starlight / VIP / Shop
    CYAN_SYNTH,       // Electric Cyan / Modes / Ambient
    VELVET_AMETHYST,  // Artist Vault / Director's Cut
    COSMIC_ROSE,      // Overclock / Fast Tempo / Danger
    SLATE_OBSIDIAN    // Secondary / Back / Subtle
}

private data class StudioButtonColors(
    val top: Color,
    val mid: Color,
    val bottom: Color,
    val rimLight: Color,
    val rimDark: Color,
    val glow: Color
)

private fun resolveStudioColors(palette: StudioPillPalette): StudioButtonColors {
    return when (palette) {
        StudioPillPalette.EMERALD_STAGE -> StudioButtonColors(
            top = Color(0xFF10B981),
            mid = Color(0xFF059669),
            bottom = Color(0xFF064E3B),
            rimLight = Color(0xFF6EE7B7),
            rimDark = Color(0xFF047857),
            glow = Color(0xFF10B981)
        )
        StudioPillPalette.SOLAR_GOLD -> StudioButtonColors(
            top = Color(0xFFF59E0B),
            mid = Color(0xFFD97706),
            bottom = Color(0xFF78350F),
            rimLight = Color(0xFFFDE68A),
            rimDark = Color(0xFFB45309),
            glow = Color(0xFFF59E0B)
        )
        StudioPillPalette.CYAN_SYNTH -> StudioButtonColors(
            top = Color(0xFF06B6D4),
            mid = Color(0xFF0284C7),
            bottom = Color(0xFF1E3A8A),
            rimLight = Color(0xFF7DD3FC),
            rimDark = Color(0xFF0369A1),
            glow = Color(0xFF06B6D4)
        )
        StudioPillPalette.VELVET_AMETHYST -> StudioButtonColors(
            top = Color(0xFFA855F7),
            mid = Color(0xFF7E22CE),
            bottom = Color(0xFF4C1D95),
            rimLight = Color(0xFFE9D5FF),
            rimDark = Color(0xFF6B21A8),
            glow = Color(0xFFA855F7)
        )
        StudioPillPalette.COSMIC_ROSE -> StudioButtonColors(
            top = Color(0xFFF43F5E),
            mid = Color(0xFFE11D48),
            bottom = Color(0xFF881337),
            rimLight = Color(0xFFFDA4AF),
            rimDark = Color(0xFF9F1239),
            glow = Color(0xFFF43F5E)
        )
        StudioPillPalette.SLATE_OBSIDIAN -> StudioButtonColors(
            top = Color(0xFF334155),
            mid = Color(0xFF1E293B),
            bottom = Color(0xFF0F172A),
            rimLight = Color(0xFF94A3B8),
            rimDark = Color(0xFF1E293B),
            glow = Color(0xFF38BDF8)
        )
    }
}

// ---------------------------------------------------------------------------
// 2. Atmospheric Musical Motes & Starlight Background
// ---------------------------------------------------------------------------

private data class MusicalMote(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun FloatingMotesBackground(
    stageAccent: Color = Color(0xFF38BDF8),
    modifier: Modifier = Modifier
) {
    var motes by remember { mutableStateOf<List<MusicalMote>>(emptyList()) }
    var tick by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        motes = List(34) {
            MusicalMote(
                x = Random.nextFloat() * 1080f,
                y = Random.nextFloat() * 2400f,
                speed = Random.nextFloat() * 1.3f + 0.4f,
                size = Random.nextFloat() * 5.5f + 2.5f,
                color = listOf(
                    Color(0xFFFDE68A), // Gold starlight
                    Color(0xFF38BDF8), // Neon cyan
                    Color(0xFF4ADE80), // Emerald pulse
                    Color(0xFFC084FC)  // Velvet amethyst
                ).random()
            )
        }
        while (true) {
            kotlinx.coroutines.delay(16)
            tick += 0.03f
            motes = motes.map {
                val nextY = (it.y - it.speed + 2400f) % 2400f
                it.copy(y = nextY)
            }
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.5f, size.height * 0.28f)

        // 1. Deep Obsidian-to-Indigo Stage Gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0E1A33),
                    Color(0xFF060D1D),
                    Color(0xFF020409)
                ),
                center = center,
                radius = size.maxDimension * 0.85f
            )
        )

        // 2. Ambient Stage Halo Glow keyed to stage accent
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    stageAccent.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = center,
                radius = size.width * 0.72f
            ),
            radius = size.width * 0.72f,
            center = center
        )

        // 3. Subtle Stage Turntable Concentric Rings
        val ringRadii = listOf(size.width * 0.32f, size.width * 0.54f, size.width * 0.78f)
        ringRadii.forEachIndexed { idx, r ->
            drawCircle(
                color = stageAccent.copy(alpha = 0.05f - idx * 0.012f),
                radius = r,
                center = center,
                style = Stroke(width = 1.2f)
            )
        }

        // 4. Drifting Motes (Twinkling Starlight Diamonds + Soft Glow Circles)
        motes.forEachIndexed { index, mote ->
            val twinkle = (sin(tick + index * 0.7f) * 0.35f + 0.65f).coerceIn(0.2f, 1f)
            val px = mote.x
            val py = mote.y

            drawCircle(
                color = mote.color.copy(alpha = 0.22f * twinkle),
                radius = mote.size * 1.8f,
                center = Offset(px, py)
            )

            if (index % 2 == 0) {
                // 4-Point Diamond Star
                val s = mote.size * 1.3f * twinkle
                val starPath = Path().apply {
                    moveTo(px, py - s)
                    quadraticTo(px, py, px + s, py)
                    quadraticTo(px, py, px, py + s)
                    quadraticTo(px, py, px - s, py)
                    quadraticTo(px, py, px, py - s)
                    close()
                }
                drawPath(starPath, color = Color.White.copy(alpha = 0.85f * twinkle))
            } else {
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f * twinkle),
                    radius = mote.size * 0.45f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Bespoke WebP Hardware Action Plaque Button (Zero Pill Shapes)
// ---------------------------------------------------------------------------

@Composable
fun rememberStudioPlaqueBitmap(palette: StudioPillPalette): ImageBitmap {
    val resId = when (palette) {
        StudioPillPalette.EMERALD_STAGE -> R.drawable.ui_plaque_emerald
        StudioPillPalette.SOLAR_GOLD -> R.drawable.ui_plaque_gold
        StudioPillPalette.CYAN_SYNTH -> R.drawable.ui_plaque_cyan
        StudioPillPalette.VELVET_AMETHYST -> R.drawable.ui_plaque_amethyst
        StudioPillPalette.COSMIC_ROSE -> R.drawable.ui_plaque_rose
        StudioPillPalette.SLATE_OBSIDIAN -> R.drawable.ui_plaque_obsidian
    }
    return ImageBitmap.imageResource(id = resId)
}

@Composable
fun StudioPillButton(
    text: String,
    palette: StudioPillPalette = StudioPillPalette.CYAN_SYNTH,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    height: Dp = 50.dp,
    textColor: Color = Color.White,
    leadingIcon: ImageVector? = null,
    badgeText: String? = null,
    subText: String? = null,
    fontSize: TextUnit = 14.5.sp,
    enabled: Boolean = true
) {
    val colors = remember(palette) { resolveStudioColors(palette) }
    val plaqueBitmap = rememberStudioPlaqueBitmap(palette)
    val haptics = rememberStudioHaptics()
    val plaqueShape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp, topEnd = 6.dp, bottomStart = 6.dp)
    val badgeShape = CutCornerShape(6.dp)
    val contentAlpha = if (enabled) 1f else 0.45f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "StudioPlaqueScale"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .offset(y = if (isPressed && enabled) 1.5.dp else 0.dp)
            .shadow(
                elevation = if (isPressed) 3.dp else 8.dp,
                shape = plaqueShape,
                ambientColor = colors.glow.copy(alpha = 0.55f),
                spotColor = colors.glow.copy(alpha = 0.55f)
            )
            .clip(plaqueShape)
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
        Image(
            bitmap = plaqueBitmap,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = contentAlpha
        )

        if (isPressed && enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.18f))
            )
        }

        // Crisp Chamfered Corner Bracket & Dynamic Diagonal Specular Gleam
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cMaj = 12.dp.toPx().coerceAtMost(h * 0.42f)
            val bLen = 14.dp.toPx().coerceAtMost(w * 0.14f)

            val gleamPath = Path().apply {
                moveTo(w * 0.22f, 0f)
                lineTo(w * 0.29f, 0f)
                lineTo(w * 0.20f, h)
                lineTo(w * 0.13f, h)
                close()
            }
            drawPath(
                path = gleamPath,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.14f * contentAlpha),
                        Color.White.copy(alpha = 0.02f * contentAlpha)
                    )
                )
            )

            val cornerCol = colors.rimLight.copy(alpha = 0.85f * contentAlpha)
            val strokePx = 1.6.dp.toPx()
            val topLeftBracket = Path().apply {
                moveTo(1.5f, cMaj + bLen * 0.65f)
                lineTo(1.5f, cMaj)
                lineTo(cMaj, 1.5f)
                lineTo(cMaj + bLen, 1.5f)
            }
            val botRightBracket = Path().apply {
                moveTo(w - 1.5f, h - cMaj - bLen * 0.65f)
                lineTo(w - 1.5f, h - cMaj)
                lineTo(w - cMaj, h - 1.5f)
                lineTo(w - cMaj - bLen, h - 1.5f)
            }
            drawPath(topLeftBracket, color = cornerCol, style = Stroke(width = strokePx, cap = StrokeCap.Square))
            drawPath(botRightBracket, color = cornerCol, style = Stroke(width = strokePx, cap = StrokeCap.Square))
        }

        // Button Content Row with Dual-Layered Faux Emboss Text
        val dynamicLetterSpacing = if (fontSize.value <= 11f) 0.4.sp else 0.9.sp
        val horizontalPad = if (fontSize.value <= 11f) 10.dp else 16.dp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPad, vertical = 2.dp)
        ) {
            if (leadingIcon != null) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(badgeShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(1.dp, colors.rimLight.copy(alpha = 0.65f * contentAlpha), badgeShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = textColor.copy(alpha = contentAlpha),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = text.uppercase(),
                        color = Color.Black.copy(alpha = 0.68f * contentAlpha),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Black,
                        letterSpacing = dynamicLetterSpacing,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.offset(x = 0.8.dp, y = 1.4.dp)
                    )
                    Text(
                        text = text.uppercase(),
                        color = textColor.copy(alpha = contentAlpha),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Black,
                        letterSpacing = dynamicLetterSpacing,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (subText != null) {
                    Text(
                        text = subText.uppercase(),
                        color = Color.White.copy(alpha = 0.88f * contentAlpha),
                        fontSize = (fontSize.value * 0.62f).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (badgeText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(badgeShape)
                        .background(Color.Black.copy(alpha = 0.42f))
                        .border(1.dp, colors.rimLight.copy(alpha = 0.80f), badgeShape)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        color = colors.rimLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. Tactile Chamfered WebP Keycap Icon Button & Reusable Badges / Switchers
// ---------------------------------------------------------------------------

@Composable
fun StudioIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    accentColor: Color = Color(0xFF38BDF8),
    tint: Color = Color.White,
    contentDescription: String? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.62f, h * 0.20f)
                lineTo(w * 0.36f, h * 0.50f)
                lineTo(w * 0.62f, h * 0.80f)
            }
            drawPath(
                path = path,
                color = tint,
                style = Stroke(
                    width = 2.4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
) {
    val shape = CutCornerShape(10.dp)
    val iconFrame = ImageBitmap.imageResource(id = R.drawable.ui_icon_frame)
    val haptics = rememberStudioHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.90f else 1f,
        label = "StudioIconScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .offset(y = if (isPressed && enabled) 1.dp else 0.dp)
            .shadow(6.dp, shape, ambientColor = accentColor, spotColor = accentColor)
            .clip(shape)
            .border(1.2.dp, accentColor.copy(alpha = 0.78f), shape)
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
        Image(
            bitmap = iconFrame,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = if (enabled) 1f else 0.45f
        )
        content()
    }
}

@Composable
fun StudioHeaderBadge(
    accentColor: Color = Color(0xFFFACC15),
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    content: @Composable RowScope.() -> Unit
) {
    val haptics = rememberStudioHaptics()
    val badgeShape = CutCornerShape(8.dp)
    val badgeBitmap = ImageBitmap.imageResource(id = R.drawable.ui_header_badge)
    val interactionSource = remember(onClick) { if (onClick != null) MutableInteractionSource() else null }
    val isPressed = interactionSource?.collectIsPressedAsState()?.value ?: false
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "HeaderBadgeScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(badgeShape)
            .border(1.dp, accentColor.copy(alpha = 0.75f), badgeShape)
            .then(
                if (onClick != null && interactionSource != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptics.tick()
                        onClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = badgeBitmap,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun StudioSegmentedSwitcher(
    options: List<Triple<String, StudioPillPalette, Color>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 42.dp,
    fontSize: TextUnit = 10.sp
) {
    val haptics = rememberStudioHaptics()
    val deckShape = CutCornerShape(12.dp)
    val tabShape = CutCornerShape(8.dp)
    val deckBitmap = ImageBitmap.imageResource(id = R.drawable.ui_switcher_deck)

    Box(
        modifier = modifier
            .height(height)
            .clip(deckShape)
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.38f), deckShape)
    ) {
        Image(
            bitmap = deckBitmap,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            options.forEachIndexed { idx, (label, palette, activeColor) ->
                val isSel = selectedIndex == idx
                val activePlaque = rememberStudioPlaqueBitmap(palette)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(tabShape)
                        .then(
                            if (isSel) Modifier.border(1.1.dp, activeColor.copy(alpha = 0.85f), tabShape)
                            else Modifier
                        )
                        .clickable {
                            haptics.tick()
                            onSelect(idx)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSel) {
                        Image(
                            bitmap = activePlaque,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Text(
                        text = label.uppercase(),
                        color = if (isSel) Color.White else Color(0xFF94A3B8),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. Unified Screen Header (Starbloom Standard with Golden Filament Line)
// ---------------------------------------------------------------------------

@Composable
fun StudioScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = Color(0xFFFACC15),
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        StudioIconButton(
            onClick = onBack,
            accentColor = accentColor,
            contentDescription = "Back"
        )

        // Centered Title + Golden Tapered Filament Line + Subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.0.sp,
                    shadow = Shadow(
                        color = accentColor.copy(alpha = 0.75f),
                        offset = Offset(0f, 1f),
                        blurRadius = 14f
                    )
                ),
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            // Starbloom signature tapered diamond light-filament with center starlight jewel
            Canvas(
                modifier = Modifier
                    .width(136.dp)
                    .height(8.dp)
            ) {
                val midY = size.height / 2f
                val cx = size.width / 2f
                val taperPath = Path().apply {
                    moveTo(0f, midY)
                    quadraticTo(cx, midY - 1.6.dp.toPx(), size.width, midY)
                    quadraticTo(cx, midY + 1.6.dp.toPx(), 0f, midY)
                    close()
                }
                drawPath(
                    path = taperPath,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0.85f),
                            Color(0xFFFDE68A),
                            accentColor.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
                val d = 3.2.dp.toPx()
                val starPath = Path().apply {
                    moveTo(cx, midY - d)
                    quadraticTo(cx, midY, cx + d, midY)
                    quadraticTo(cx, midY, cx, midY + d)
                    quadraticTo(cx, midY, cx - d, midY)
                    quadraticTo(cx, midY, cx, midY - d)
                    close()
                }
                drawPath(starPath, color = Color.White)
            }

            if (subtitle != null) {
                Text(
                    text = subtitle.uppercase(),
                    color = accentColor.copy(alpha = 0.90f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Trailing Content or Balancer Spacer
        if (trailingContent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = trailingContent)
        } else {
            Spacer(modifier = Modifier.size(42.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// 6. Bespoke WebP Obsidian-Gold Deck Card
// ---------------------------------------------------------------------------

@Composable
fun StudioGlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = CutCornerShape(16.dp),
    accentColor: Color = Color(0xFFFACC15),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptics = rememberStudioHaptics()
    val cardFrameBitmap = ImageBitmap.imageResource(id = R.drawable.ui_card_frame_ornate)
    val interactionSource = remember(onClick) { if (onClick != null) MutableInteractionSource() else null }
    val isPressed = interactionSource?.collectIsPressedAsState()?.value ?: false
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "StudioGlassScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .offset(y = if (isPressed) 1.dp else 0.dp)
            .then(
                if (onClick != null && interactionSource != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            haptics.tick()
                            onClick()
                        }
                    )
                } else Modifier
            ),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                listOf(
                    accentColor.copy(alpha = 0.85f),
                    Color(0xFF38BDF8).copy(alpha = 0.35f),
                    Color(0xFF0F172A).copy(alpha = 0.70f)
                )
            )
        ),
        shadowElevation = 10.dp
    ) {
        Box {
            Image(
                bitmap = cardFrameBitmap,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                alpha = 0.94f
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.5f, 0f)
                )
            }
            content()
        }
    }
}

@Composable
fun BottomNavGlyph(type: String, isSelected: Boolean) {
    val tint = if (isSelected) Color(0xFFFACC15) else Color(0xFF94A3B8)
    Canvas(modifier = Modifier.size(19.dp)) {
        val w = size.width
        val h = size.height
        when (type) {
            "HOME" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.45f)
                    lineTo(w * 0.72f, h * 0.45f)
                    lineTo(w * 0.72f, h * 0.85f)
                    lineTo(w * 0.28f, h * 0.85f)
                    lineTo(w * 0.28f, h * 0.45f)
                    lineTo(w * 0.15f, h * 0.45f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
            }
            "SKINS", "SHOP" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.10f)
                    lineTo(w * 0.60f, h * 0.40f)
                    lineTo(w * 0.90f, h * 0.50f)
                    lineTo(w * 0.60f, h * 0.60f)
                    lineTo(w * 0.5f, h * 0.90f)
                    lineTo(w * 0.40f, h * 0.60f)
                    lineTo(w * 0.10f, h * 0.50f)
                    lineTo(w * 0.40f, h * 0.40f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
            }
            "ALBUM" -> {
                drawCircle(color = tint, radius = w * 0.42f, style = Stroke(width = 1.8.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.16f, style = Stroke(width = 1.5.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.05f)
            }
            "VAULT" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    lineTo(w * 0.88f, h * 0.50f)
                    lineTo(w * 0.5f, h * 0.88f)
                    lineTo(w * 0.12f, h * 0.50f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.12f)
            }
            "SETTINGS" -> {
                drawCircle(color = tint, radius = w * 0.40f, style = Stroke(width = 1.8.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.16f, style = Stroke(width = 1.4.dp.toPx()))
            }
            else -> {
                drawCircle(color = tint, radius = w * 0.35f, style = Stroke(width = 1.8.dp.toPx()))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 7. Bespoke WebP Obsidian-Gold Navigation Console Dock
// ---------------------------------------------------------------------------

@Composable
fun StudioNavDock(
    currentDestination: String,
    onNavigateHome: () -> Unit,
    onNavigateAlbum: () -> Unit,
    onNavigateVault: () -> Unit,
    onNavigateSkins: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberStudioHaptics()
    val dockPlate = ImageBitmap.imageResource(id = R.drawable.ui_nav_dock_plate)
    val iconFrame = ImageBitmap.imageResource(id = R.drawable.ui_icon_frame)
    val navItems = listOf(
        Triple("HOME", "HOME", onNavigateHome),
        Triple("ALBUM", "ALBUM", onNavigateAlbum),
        Triple("VAULT", "VAULT", onNavigateVault),
        Triple("SHOP", "SHOP", onNavigateSkins),
        Triple("SETTINGS", "SETTINGS", onNavigateSettings)
    )

    val dockShape = CutCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    val keycapShape = CutCornerShape(9.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, dockShape, ambientColor = Color(0xFFFACC15).copy(alpha = 0.28f))
            .clip(dockShape)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFACC15).copy(alpha = 0.65f),
                        Color(0xFF38BDF8).copy(alpha = 0.28f),
                        Color(0xFF0F172A).copy(alpha = 0.85f)
                    )
                ),
                shape = dockShape
            )
    ) {
        Image(
            bitmap = dockPlate,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (key, label, action) ->
                val isSel = currentDestination == key || ((currentDestination == "SKINS" || currentDestination == "SHOP") && key == "SHOP")
                val accent = when (key) {
                    "HOME" -> Color(0xFF4ADE80)
                    "ALBUM" -> Color(0xFF38BDF8)
                    "VAULT" -> Color(0xFFFACC15)
                    "SKINS", "SHOP" -> Color(0xFFA855F7)
                    else -> Color(0xFFFDE68A)
                }

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else if (isSel) 1.05f else 1f,
                    label = "NavNodeScale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptics.tick()
                            action()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(keycapShape)
                            .then(
                                if (isSel) Modifier.border(1.3.dp, accent.copy(alpha = 0.85f), keycapShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSel) {
                            Image(
                                bitmap = iconFrame,
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                        BottomNavGlyph(type = key, isSelected = isSel)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = label,
                        color = if (isSel) accent else Color(0xFF94A3B8),
                        fontSize = 8.5.sp,
                        fontWeight = if (isSel) FontWeight.Black else FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 8. Spring Pop Dialog with Ornate WebP Hardware Frame
// ---------------------------------------------------------------------------

@Composable
fun StudioDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth(0.92f).wrapContentHeight(),
    accentColor: Color = Color(0xFFFACC15),
    content: @Composable ColumnScope.() -> Unit
) {
    val audioEngine = LocalStudioAudioEngine.current
    val dialogFrameBitmap = ImageBitmap.imageResource(id = R.drawable.ui_card_frame_ornate)
    val dialogShape = CutCornerShape(18.dp)
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appeared = true
        audioEngine?.playWhooshChime("MODAL")
    }

    val popScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.78f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 440f),
        label = "StudioDialogPop"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .systemBarsPadding()
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier
                .heightIn(max = 650.dp)
                .scale(popScale)
                .clickable(enabled = false) {}
                .padding(10.dp)
                .shadow(16.dp, dialogShape, ambientColor = accentColor, spotColor = accentColor)
                .clip(dialogShape)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.88f),
                            Color(0xFF38BDF8).copy(alpha = 0.35f),
                            Color(0xFF1E293B).copy(alpha = 0.65f)
                        )
                    ),
                    shape = dialogShape
                )
        ) {
            Image(
                bitmap = dialogFrameBitmap,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                alpha = 0.96f
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.width * 0.75f
                    ),
                    radius = size.width * 0.75f,
                    center = Offset(size.width / 2f, 0f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 14.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.0.sp,
                        shadow = Shadow(accentColor, Offset.Zero, 14f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tapered diamond filament divider
                Canvas(modifier = Modifier.width(128.dp).height(8.dp)) {
                    val midY = size.height / 2f
                    val cx = size.width / 2f
                    val taperPath = Path().apply {
                        moveTo(0f, midY)
                        quadraticTo(cx, midY - 1.6.dp.toPx(), size.width, midY)
                        quadraticTo(cx, midY + 1.6.dp.toPx(), 0f, midY)
                        close()
                    }
                    drawPath(
                        path = taperPath,
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, accentColor, Color(0xFFFDE68A), accentColor, Color.Transparent)
                        )
                    )
                    val d = 3.0.dp.toPx()
                    val starPath = Path().apply {
                        moveTo(cx, midY - d)
                        quadraticTo(cx, midY, cx + d, midY)
                        quadraticTo(cx, midY, cx, midY + d)
                        quadraticTo(cx, midY, cx - d, midY)
                        quadraticTo(cx, midY, cx, midY - d)
                        close()
                    }
                    drawPath(starPath, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}
