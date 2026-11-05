package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    cipherMeterProgress: Float = 0f,
    isCipherArmed: Boolean = false,
    isBrollPlaying: Boolean = false,
    brollPlayProgress: Float = 0f,
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
            val hasRecentHit = (rhythmCombo > 1 || (comboBanner != null && comboBanner.timer > 0f)) && !is24KOverdrive
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

        // 3b. Mysterious Left-Edge Analog Cipher Gauge (Fills via Enigmatic Spool/Iris Orbs -> Triggers VHS Video on Musical Transition!)
        val cipherSpinInf = rememberInfiniteTransition(label = "cipherGauge")
        val cipherSpinDeg by cipherSpinInf.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (isCipherArmed || isBrollPlaying) 2400 else 6800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "cipherSpin"
        )
        val cipherPulse by cipherSpinInf.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isCipherArmed || isBrollPlaying) 1.09f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 580, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cipherPulse"
        )
        val cipherDisplayFill = if (isBrollPlaying) brollPlayProgress.coerceIn(0.05f, 1f) else cipherMeterProgress.coerceIn(0f, 1f)
        if (cipherDisplayFill > 0.01f || isCipherArmed || isBrollPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .graphicsLayer {
                        scaleX = cipherPulse
                        scaleY = cipherPulse
                    }
                    .width(34.dp)
                    .height(115.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xD9050814L))
                    .border(
                        width = if (isCipherArmed || isBrollPlaying) 2.dp else 1.2.dp,
                        brush = if (isCipherArmed || isBrollPlaying) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF05D9E8L),
                                    Color(0xFFFACC15L),
                                    Color(0xFFFF2A6DL)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x8805D9E8L),
                                    Color(0x66FF2A6DL)
                                )
                            )
                        },
                        shape = RoundedCornerShape(17.dp)
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Chromatic VHS Liquid Fill (4 Discrete Segment Notches + Smooth Phosphor Column)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(cipherDisplayFill)
                        .clip(RoundedCornerShape(bottomStart = 17.dp, bottomEnd = 17.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF05D9E8L).copy(alpha = 0.92f),
                                    Color(0xFF818CF8L).copy(alpha = 0.88f),
                                    Color(0xFFFF2A6DL).copy(alpha = 0.92f)
                                )
                            )
                        )
                )

                // Bespoke Enigmatic Iris & Twin-Spool Sigil (Zero Words, Zero Emojis!)
                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(22.dp)
                ) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    val r = size.minDimension * 0.44f
                    val rotRad = Math.toRadians(cipherSpinDeg.toDouble()).toFloat()

                    // Outer Chromatic Split Ring
                    drawCircle(
                        color = Color(0xFFFF2A6DL).copy(alpha = 0.85f),
                        radius = r,
                        center = Offset(cx + 1.1f, cy),
                        style = Stroke(width = 1.6f)
                    )
                    drawCircle(
                        color = Color(0xFF05D9E8L).copy(alpha = 0.90f),
                        radius = r,
                        center = Offset(cx - 1.1f, cy),
                        style = Stroke(width = 1.6f)
                    )
                    drawCircle(
                        color = Color(0xFF050814L).copy(alpha = 0.86f),
                        radius = r * 0.84f,
                        center = Offset(cx, cy)
                    )

                    // 6 Rotating Aperture Spokes
                    for (b in 0 until 6) {
                        val a = rotRad + b * (Math.PI.toFloat() / 3f)
                        drawLine(
                            color = if (b % 2 == 0) Color(0xFF05D9E8L) else Color(0xFFFACC15L),
                            start = Offset(cx + cos(a) * r * 0.28f, cy + sin(a) * r * 0.28f),
                            end = Offset(cx + cos(a + 0.42f) * r * 0.82f, cy + sin(a + 0.42f) * r * 0.82f),
                            strokeWidth = 1.6f,
                            cap = StrokeCap.Round
                        )
                    }
                    // Center Glowing Aperture Pupil
                    drawCircle(
                        color = if (isCipherArmed || isBrollPlaying) Color.White else Color(0xFFFACC15L),
                        radius = r * 0.22f,
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        // 4. Floating Right-Edge 24K Liquid Energy Capsule (Off to side, never blocking falling notes!)
        val isReady = overdriveProgress >= 0.50f || is24KOverdrive
        val infiniteTransition = rememberInfiniteTransition(label = "capsulePulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isReady) 1.08f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        val pulseGlow by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = if (isReady) 1.0f else 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .width(36.dp)
                .height(115.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xD9060A16L))
                .border(
                    width = if (isReady) 2.dp else 1.2.dp,
                    brush = if (isReady) {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = pulseGlow),
                                Color(0xFFFACC15L),
                                Color(0xFFEC4899L)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color(0x33FACC15L)
                            )
                        )
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable {
                    if (isReady) onActivate24K()
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Liquid fill rising from bottom
            val fillHeightFraction = if (is24KOverdrive) (starPowerTime / 9.0f).coerceIn(0f, 1f) else overdriveProgress.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillHeightFraction)
                    .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFFFFFL),
                                Color(0xFFFEF08AL),
                                Color(0xFFFACC15L),
                                Color(0xFFD97706L)
                            )
                        )
                    )
            )

            // Central Lightning Bolt Icon (Pure Visual, Zero Words)
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp)
            ) {
                val w = size.width
                val h = size.height
                val boltPath = Path().apply {
                    moveTo(w * 0.58f, 0f)
                    lineTo(w * 0.18f, h * 0.54f)
                    lineTo(w * 0.48f, h * 0.54f)
                    lineTo(w * 0.38f, h)
                    lineTo(w * 0.82f, h * 0.42f)
                    lineTo(w * 0.52f, h * 0.42f)
                    close()
                }
                drawPath(
                    path = boltPath,
                    color = if (isReady) Color(0xFF0F172AL) else Color(0xFFFACC15L).copy(alpha = 0.65f)
                )
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
