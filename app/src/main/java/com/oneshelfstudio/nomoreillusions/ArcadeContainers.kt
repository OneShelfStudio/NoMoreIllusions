package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("UNUSED_PARAMETER")
@Composable
fun ArcadeDialog(
    title: String,
    panelBitmap: ImageBitmap,
    modifier: Modifier = Modifier.width(340.dp).wrapContentHeight(),
    onClose: (() -> Unit)? = null,
    closeIcon: ImageBitmap? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dialogShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .padding(12.dp)
            .shadow(
                elevation = 18.dp,
                shape = dialogShape,
                ambientColor = Color(0xFF38BDF8),
                spotColor = Color(0xFFFACC15)
            )
            .clip(dialogShape)
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
                        Color(0xFF38BDF8).copy(alpha = 0.45f),
                        Color(0xFF1E293B).copy(alpha = 0.7f)
                    )
                ),
                shape = dialogShape
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFACC15).copy(alpha = 0.16f),
                        Color(0xFF38BDF8).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
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
                .padding(top = 20.dp, bottom = 22.dp, start = 20.dp, end = 20.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
            content()
        }

        if (onClose != null && closeIcon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                ArcadeIconButton(
                    iconBitmap = closeIcon,
                    onClick = onClose,
                    iconType = ArcadeIconType.CLOSE,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun ArcadeStatCapsule(
    capsuleBitmap: ImageBitmap,
    label: String,
    value: String,
    modifier: Modifier = Modifier.size(width = 170.dp, height = 48.dp),
    valueColor: Color = Color.White
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xE60B182C), Color(0xE6040812))
                )
            )
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.55f), shape)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFF7DD3FC),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun GameButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.width(260.dp).height(54.dp)
) {
    val haptics = rememberStudioHaptics()
    Button(
        onClick = {
            haptics.confirm()
            onClick()
        },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = if (enabled) 0.88f else 0.25f),
            disabledContainerColor = color.copy(alpha = 0.2f)
        ),
        modifier = modifier.border(1.5.dp, color.copy(alpha = 0.7f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) Color.Black else Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ToggleItem(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    subtitle: String? = null
) {
    val haptics = rememberStudioHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val thumbProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ToggleThumb"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF060E1E).copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = if (checked) Color(0xFF4ADE80).copy(alpha = 0.38f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.tick()
                onToggle(!checked)
            }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Custom Glowing Pill Switch
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(26.dp)
                .clip(CircleShape)
                .background(
                    if (checked) {
                        Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
                    }
                )
                .border(
                    width = 1.2.dp,
                    color = if (checked) Color(0xFF6EE7B7) else Color(0xFF475569),
                    shape = CircleShape
                )
                .padding(horizontal = 3.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = (20f * thumbProgress).dp)
                    .size(20.dp)
                    .shadow(if (checked) 4.dp else 1.dp, CircleShape, ambientColor = Color(0xFF4ADE80))
                    .clip(CircleShape)
                    .background(
                        if (checked) Color.White else Color(0xFF94A3B8)
                    )
            )
        }
    }
}

@Composable
fun DifficultyOption(
    difficulty: GameDifficulty,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (GameDifficulty) -> Unit
) {
    val haptics = rememberStudioHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "DiffScale"
    )

    val (label, desc, accent) = when (difficulty) {
        GameDifficulty.EASY -> Triple("CHILL", "0.5x PTS", Color(0xFF38BDF8))
        GameDifficulty.REGULAR -> Triple("REGULAR", "1.0x PTS", Color(0xFF4ADE80))
        GameDifficulty.HARD -> Triple("INTENSE", "1.5x PTS", Color(0xFFFACC15))
    }
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 8.dp else 0.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.45f),
                spotColor = accent.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.28f),
                            Color(0xFF0A192F),
                            Color(0xFF040914)
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF0B1426), Color(0xFF050912)))
                }
            )
            .border(
                width = if (isSelected) 1.6.dp else 1.dp,
                color = if (isSelected) accent else Color.White.copy(alpha = 0.14f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.tick()
                onClick(difficulty)
            }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.72f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = if (isSelected) accent else Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InstructionItem(title: String, desc: String) {
    StudioGlassCard(
        accentColor = Color(0xFF38BDF8),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF4ADE80),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = desc,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ArcadeFocusButton(
    focusPower: Float,
    isActive: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canActivate = focusPower > 0.15f
    val pulseAlpha = if (isActive) 0.98f else if (canActivate) 0.88f else 0.40f
    val borderColor = if (isActive) Color(0xFF4ADE80) else if (canActivate) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .size(68.dp)
            .shadow(
                elevation = if (isActive) 12.dp else 5.dp,
                shape = CircleShape,
                ambientColor = borderColor,
                spotColor = borderColor
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (isActive) {
                        listOf(Color(0xEE15803D), Color(0xEE0F172A))
                    } else {
                        listOf(Color(0xD90E1C33), Color(0xD904060A))
                    }
                )
            )
            .border(2.dp, borderColor, CircleShape)
            .pointerInput(canActivate) {
                if (canActivate) {
                    awaitEachGesture {
                        awaitFirstDown()
                        onPressStart()
                        waitForUpOrCancellation()
                        onPressEnd()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.5f
                val s = size.minDimension * 0.45f
                val star = Path().apply {
                    moveTo(cx, cy - s)
                    quadraticTo(cx, cy, cx + s, cy)
                    quadraticTo(cx, cy, cx, cy + s)
                    quadraticTo(cx, cy, cx - s, cy)
                    quadraticTo(cx, cy, cx, cy - s)
                    close()
                }
                drawPath(star, color = if (isActive) Color(0xFF4ADE80) else Color(0xFF7DD3FC))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isActive) "ILLUSION" else "FOCUS",
                color = Color.White.copy(alpha = pulseAlpha),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            )
            Text(
                text = "${(focusPower * 100).toInt()}%",
                color = borderColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
