package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Path
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
    overdriveProgress: Float = 0f,
    is24KOverdrive: Boolean = false,
    comboBanner: ComboZoomBannerData? = null,
    onActivate24K: () -> Unit = {},
    onPause: () -> Unit
) {
    // Ultra-Clean Open-Stage HUD (< 4% screen footprint — clean score, combo streak, zero cluttered popups!)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp, start = 14.dp, end = 14.dp)
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
            // 1. Magic Tiles Milestone Header (★ ★ ★ 👑 👑 👑 — Thick 8dp Neon Groove)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Track Line (8dp High)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
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
                                    listOf(Color(0xFF38BDF8L), Color(0xFF818CF8L), Color(0xFFFACC15L), Color(0xFFEC4899L))
                                )
                            )
                    )
                }

                // Milestones: 3 Stars (16.6%, 33.3%, 50%) and 3 Crowns (66.6%, 83.3%, 98%)
                val milestones = listOf(
                    Pair(0.166f, false),
                    Pair(0.333f, false),
                    Pair(0.500f, false),
                    Pair(0.666f, true),
                    Pair(0.833f, true),
                    Pair(0.980f, true)
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val trackW = maxWidth
                    for ((pct, isCrown) in milestones) {
                        val isUnlocked = trackProgress >= pct
                        val iconX = trackW * pct - 7.dp
                        MilestoneGlyph(
                            isCrown = isCrown,
                            isUnlocked = isUnlocked,
                            modifier = Modifier
                                .offset(x = iconX)
                                .graphicsLayer {
                                    if (isUnlocked) {
                                        scaleX = 1.35f
                                        scaleY = 1.35f
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3338BDF8L))
                                .border(1.dp, Color(0xFF38BDF8L), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(text = "SHIELD", color = Color(0xFF38BDF8L), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (starPowerTime > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33FACC15L))
                                .border(1.dp, Color(0xFFFACC15L), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(text = "4X MULT", color = Color(0xFFFACC15L), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (slowMoTime > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33C084FCL))
                                .border(1.dp, Color(0xFFC084FCL), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(text = "SLOW-MO", color = Color(0xFFC084FCL), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
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

            // 5-Star Band Hero Overdrive Gauge & 24K Overdrive Trigger
            Spacer(modifier = Modifier.height(4.dp))
            val filledStars = (overdriveProgress * 5).toInt().coerceIn(0, 5)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xD90A0816L))
                    .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.55f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (st in 1..5) {
                        val isCharged = st <= filledStars || is24KOverdrive
                        Text(
                            text = "★",
                            color = if (isCharged) Color(0xFFFACC15L) else Color.White.copy(alpha = 0.25f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.graphicsLayer {
                                if (isCharged) {
                                    scaleX = 1.2f
                                    scaleY = 1.2f
                                }
                            }
                        )
                    }
                }
            }

            // Pulsing 24K Overdrive Activation Trigger
            if (is24KOverdrive || overdriveProgress >= 0.60f) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (is24KOverdrive) Brush.horizontalGradient(listOf(Color(0xFFFFFFFFL), Color(0xFFFACC15L)))
                            else Brush.horizontalGradient(listOf(Color(0xFFFACC15L), Color(0xFFEC4899L)))
                        )
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { onActivate24K() }
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (is24KOverdrive) "24K ACTIVE • %.1fs".format(starPowerTime) else "24K OVERDRIVE",
                        color = Color(0xFF0F172AL),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Endless Mode Loop Counter (Magic Tiles Accelerating Style)
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
                        text = "LOOP ${endlessLoopCount + 1} • +${(endlessLoopCount * 6)}% SPEED",
                        color = Color(0xFFFACC15L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneGlyph(isCrown: Boolean, isUnlocked: Boolean, modifier: Modifier = Modifier) {
    val tint = if (isUnlocked) Color(0xFFFACC15L) else Color.White.copy(alpha = 0.32f)
    Canvas(modifier = modifier.size(13.dp)) {
        if (!isCrown) {
            val path = Path().apply {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerR = size.width / 2f
                val innerR = outerR * 0.45f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = (i * Math.PI / 5.0 - Math.PI / 2.0).toFloat()
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, color = tint)
        } else {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                moveTo(w * 0.15f, h * 0.85f)
                lineTo(w * 0.85f, h * 0.85f)
                lineTo(w * 0.95f, h * 0.25f)
                lineTo(w * 0.65f, h * 0.55f)
                lineTo(w * 0.50f, h * 0.15f)
                lineTo(w * 0.35f, h * 0.55f)
                lineTo(w * 0.05f, h * 0.25f)
                close()
            }
            drawPath(path, color = tint)
        }
    }
}
