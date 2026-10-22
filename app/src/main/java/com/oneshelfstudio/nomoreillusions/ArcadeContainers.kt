package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
                ambientColor = Color(0xFF6366F1),
                spotColor = Color(0xFF16A34A)
            )
            .clip(dialogShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFA060B09),
                        Color(0xFA04060D),
                        Color(0xFA020306)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF6366F1).copy(alpha = 0.85f),
                        Color(0xFF22C55E).copy(alpha = 0.55f),
                        Color(0xFF1E1B4B).copy(alpha = 0.7f)
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
                        Color(0xFF6366F1).copy(alpha = 0.18f),
                        Color(0xFF16A34A).copy(alpha = 0.08f),
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF0A2E18),
                                Color(0xFF1E1B4B),
                                Color(0xFF0A2E18)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF22C55E).copy(alpha = 0.65f),
                                Color(0xFFA5B4FC).copy(alpha = 0.90f),
                                Color(0xFF22C55E).copy(alpha = 0.65f)
                            )
                        ),
                        shape = RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
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
                    listOf(Color(0xE6091410), Color(0xE604060E))
                )
            )
            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.55f), shape)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFF86EFAC),
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
    Button(
        onClick = onClick,
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
fun ToggleItem(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF4ADE80),
                checkedTrackColor = Color(0xFF4F46E5).copy(alpha = 0.65f),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF0F172A)
            )
        )
    }
}

@Composable
fun DifficultyOption(
    difficulty: GameDifficulty,
    isSelected: Boolean,
    onClick: (GameDifficulty) -> Unit
) {
    val (label, desc) = when (difficulty) {
        GameDifficulty.EASY -> "EASY" to "0.5x PTS"
        GameDifficulty.REGULAR -> "REGULAR" to "1.0x PTS"
        GameDifficulty.HARD -> "HARD" to "1.5x PTS"
    }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .padding(horizontal = 5.dp, vertical = 4.dp)
            .clip(shape)
            .background(
                if (isSelected) {
                    Brush.verticalGradient(listOf(Color(0xFF4F46E5), Color(0xFF15803D)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF05080F)))
                }
            )
            .border(
                width = 1.2.dp,
                color = if (isSelected) Color(0xFF86EFAC) else Color.White.copy(alpha = 0.15f),
                shape = shape
            )
            .clickable { onClick(difficulty) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Text(
                text = desc,
                color = if (isSelected) Color(0xFF86EFAC) else Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InstructionItem(title: String, desc: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF060B09).copy(alpha = 0.85f))
            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF4ADE80),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = desc,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 3.dp)
        )
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
    val borderColor = if (isActive) Color(0xFF4ADE80) else if (canActivate) Color(0xFF818CF8) else Color.White.copy(alpha = 0.22f)

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
                        listOf(Color(0xEE15803D), Color(0xEE1E1B4B))
                    } else {
                        listOf(Color(0xD91E1B4B), Color(0xD904060A))
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
            Text(
                text = "⚡",
                color = if (isActive) Color(0xFF4ADE80) else Color(0xFFA5B4FC),
                fontSize = 16.sp
            )
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
