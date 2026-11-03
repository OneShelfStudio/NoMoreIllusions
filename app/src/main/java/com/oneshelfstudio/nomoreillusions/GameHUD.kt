package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

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
    rhythmCombo: Int = 0,
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
    comboBanner: ComboZoomBannerData? = null,
    onPause: () -> Unit
) {
    // Ultra-Clean Open-Stage HUD (< 4% screen footprint — clean score, combo streak, zero cluttered popups!)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 6.dp, start = 14.dp, end = 14.dp)
    ) {
        // 3. Upper-Middle on-screen Combo & Streak Zoom Popup Banner
        if (comboBanner != null && comboBanner.timer > 0f) {
            val elapsed = comboBanner.maxTimer - comboBanner.timer
            val progress = (elapsed / comboBanner.maxTimer).coerceIn(0f, 1f)
            val fadeOut = (comboBanner.timer / 0.22f).coerceIn(0f, 1f)

            val springPunch = if (!comboBanner.isBroken) {
                0.42f * exp(-elapsed * 12.0f) * cos(elapsed * 22.0f)
            } else 0f
            val scale = (1.0f + springPunch).coerceIn(0.85f, 1.45f)
            val shake = if (comboBanner.isBroken) {
                sin(progress * 55f) * (1f - progress * 1.5f).coerceAtLeast(0f) * 9f
            } else 0f

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 58.dp)
                    .offset(x = shake.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = fadeOut
                    }
                    .clip(CircleShape)
                    .background(
                        if (comboBanner.isBroken) Color(0xEB1E080EL) else Color(0xDE080C18L)
                    )
                    .border(
                        width = 2.dp,
                        color = comboBanner.color.copy(alpha = 0.95f * fadeOut),
                        shape = CircleShape
                    )
                    .padding(horizontal = 20.dp, vertical = 7.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = comboBanner.text,
                        color = if (comboBanner.isBroken) Color.White else comboBanner.color,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = Shadow(
                                color = comboBanner.color.copy(alpha = 0.85f),
                                blurRadius = 14f
                            )
                        )
                    )
                    Text(
                        text = comboBanner.subText,
                        color = if (comboBanner.isBroken) Color(0xFFFCA5A5L) else Color.White.copy(alpha = 0.88f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Magic Tiles Milestone Header (★ ★ ★ 👑 👑 👑)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Track Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(trackProgress.coerceIn(0f, 1f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF38BDF8L), Color(0xFFFACC15L), Color(0xFFEC4899L))
                                )
                            )
                    )
                }

                // Milestones: 3 Stars (16.6%, 33.3%, 50%) and 3 Crowns (66.6%, 83.3%, 98%)
                val milestones = listOf(
                    Pair(0.166f, "★"),
                    Pair(0.333f, "★"),
                    Pair(0.500f, "★"),
                    Pair(0.666f, "👑"),
                    Pair(0.833f, "👑"),
                    Pair(0.980f, "👑")
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val trackW = maxWidth
                    for ((pct, icon) in milestones) {
                        val isUnlocked = trackProgress >= pct
                        val iconX = trackW * pct - 9.dp
                        Text(
                            text = icon,
                            color = if (isUnlocked) Color(0xFFFACC15L) else Color.White.copy(alpha = 0.32f),
                            fontSize = if (icon == "★") 12.sp else 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .offset(x = iconX)
                                .graphicsLayer {
                                    if (isUnlocked) {
                                        scaleX = 1.25f
                                        scaleY = 1.25f
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Upper Row: Power-up badges & Pause Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (shieldTime > 0f) {
                        Text(text = "🛡️", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (starPowerTime > 0f) {
                        Text(text = "⚡ 4X", color = Color(0xFFFACC15L), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (slowMoTime > 0f) {
                        Text(text = "🌀 50%", color = Color(0xFFC084FCL), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                ArcadeIconButton(
                    iconBitmap = btnIconPause,
                    onClick = onPause,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // 3. Center Magic Tiles Score & Angled Judgment Badges
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 46.dp)
        ) {
            // Big Score Digits
            Text(
                text = "%,d".format(score),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 3f),
                        blurRadius = 8f
                    )
                )
            )

            // Angled Rating Badge (e.g. PERFECT / GREAT)
            val hasRecentHit = rhythmCombo > 1 || (comboBanner != null && comboBanner.timer > 0f)
            if (hasRecentHit) {
                Spacer(modifier = Modifier.height(2.dp))
                val isBroken = comboBanner?.isBroken == true
                Box(
                    modifier = Modifier
                        .graphicsLayer(rotationZ = -3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isBroken) Color(0xEB1E080EL)
                            else Color(0x33EC4899L)
                        )
                        .border(
                            1.2.dp,
                            if (isBroken) Color(0xFFF43F5EL) else Color(0xFFEC4899L),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isBroken) "MISS" else (comboBanner?.text ?: "PERFECT"),
                        color = if (isBroken) Color(0xFFF43F5EL) else Color(0xFFF472B6L),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Soft Multiplier Row (× 3)
            val displayMult = if (multiplier > 1) multiplier else if (rhythmCombo > 1) (rhythmCombo / 4 + 1).coerceIn(1, 8) else 1
            if (displayMult > 1) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "×",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$displayMult",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Endless Mode Loop Counter
            if (endlessLoopCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x33FACC15L))
                        .border(1.dp, Color(0xFFFACC15L), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "👑 LOOP ${endlessLoopCount + 1}",
                        color = Color(0xFFFACC15L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
