package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("UNUSED_PARAMETER")
@Composable
fun HUD(
    score: Int,
    highScore: Int,
    pulsePoints: Int,
    unlockedTrackCount: Int,
    trackIndex: Int,
    trackProgress: Float,
    focus: Float,
    multiplier: Int,
    multiplierTime: Float,
    btnIconPause: ImageBitmap,
    trackArt: ImageBitmap? = null,
    isCurrentTrackUnlocked: Boolean = false,
    difficulty: GameDifficulty = GameDifficulty.REGULAR,
    currentSectionLabel: String = "INTRO",
    isExtraJuiceActive: Boolean = false,
    activePowerUpToast: PowerUpBannerToast? = null,
    toastTimeLeft: Float = 0f,
    starPowerTime: Float = 0f,
    shieldTime: Float = 0f,
    slowMoTime: Float = 0f,
    magnetTime: Float = 0f,
    endlessLoopCount: Int = 0,
    onPause: () -> Unit
) {
    // Ultra-Clean Open-Stage HUD (< 6% screen footprint — zero stacked boxes, zero popups!)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 6.dp, start = 14.dp, end = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Hairline Track Progress Bar along the very top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(trackProgress.coerceIn(0f, 1f))
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF38BDF8L), Color(0xFFFACC15L))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Frameless Top Row: Score + Multiplier on Left, Compact Pause Icon on Right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%,d".format(score),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.85f),
                                offset = Offset(0f, 2f),
                                blurRadius = 6f
                            )
                        )
                    )
                    if (multiplier > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "x$multiplier",
                            color = Color(0xFFFACC15L),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.85f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 5f
                                )
                            )
                        )
                    }
                    if (shieldTime > 0f) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🛡️",
                            fontSize = 13.sp
                        )
                    }
                    if (starPowerTime > 0f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "★",
                            color = Color(0xFFFACC15L),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                ArcadeIconButton(
                    iconBitmap = btnIconPause,
                    onClick = onPause,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 3. Live Song Unlock & Discovery Progress Capsule
            val pctUnlocked = (trackProgress * 100f).toInt().coerceIn(0, 100)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                    .background(Color(0xD9060B18L))
                    .border(
                        1.dp,
                        if (isCurrentTrackUnlocked) Color(0xFF4ADE80L).copy(alpha = 0.65f) else Color(0xFFFACC15L).copy(alpha = 0.65f),
                        CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                if (trackArt != null) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.dp, if (isCurrentTrackUnlocked) Color(0xFF4ADE80L) else Color(0xFFFACC15L), CircleShape)
                    ) {
                        Image(
                            bitmap = trackArt,
                            contentDescription = "Active Track Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (isCurrentTrackUnlocked) "★ TRACK 0$trackIndex UNLOCKED • $pctUnlocked%"
                    else "🔒 UNLOCKING TRACK 0$trackIndex • $pctUnlocked% • $pulsePoints ♪",
                    color = if (isCurrentTrackUnlocked) Color(0xFF86EFACL) else Color(0xFFFDE047L),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
