package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
    unlockedStageFx: Set<StageFxBackdrop> = setOf(StageFxBackdrop.AURORA_SILK, StageFxBackdrop.KALEIDOSCOPE_MANDALA),
    activeStageFx: StageFxBackdrop? = null,
    stageFxRemainingRatio: Float = 0f,
    fxDropCooldownRatio: Float = 0f,
    comboBanner: ComboZoomBannerData? = null,
    onDropStageFxOrb: (StageFxBackdrop) -> Unit = {},
    onActivate24K: () -> Unit = {},
    onPause: () -> Unit
) {
    var isFxBoardExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp, start = 14.dp, end = 14.dp)
    ) {
        // Upper-Middle on-screen Combo & Streak Zoom Popup Banner
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

        // Compact Frosted Milestone / Stage FX Drop Toast Pill
        if (activePowerUpToast != null && toastTimeLeft > 0f && (comboBanner == null || comboBanner.timer <= 0f)) {
            val maxT = activePowerUpToast.maxTimer.coerceAtLeast(0.5f)
            val elapsed = (maxT - toastTimeLeft).coerceAtLeast(0f)
            val popScale = (1f + 0.16f * exp(-elapsed * 14f) * cos(elapsed * 20f)).coerceIn(0.92f, 1.18f)
            val fadeAlpha = (toastTimeLeft / 0.28f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 132.dp)
                    .graphicsLayer {
                        scaleX = popScale
                        scaleY = popScale
                        alpha = fadeAlpha
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xEB0B1528L), Color(0xF2040812L))
                        )
                    )
                    .border(
                        width = 1.4.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                activePowerUpToast.accentColor.copy(alpha = 0.90f),
                                Color.White.copy(alpha = 0.45f),
                                activePowerUpToast.accentColor.copy(alpha = 0.90f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = activePowerUpToast.icon,
                        color = activePowerUpToast.accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = activePowerUpToast.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = activePowerUpToast.subtitle,
                            color = activePowerUpToast.accentColor.copy(alpha = 0.92f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Milestone Header (Thick 8dp Neon Groove)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentAlignment = Alignment.CenterStart
            ) {
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3338BDF8L))
                                .border(1.dp, Color(0xFF38BDF8L), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "SHIELD", color = Color(0xFF38BDF8L), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (starPowerTime > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33FACC15L))
                                .border(1.dp, Color(0xFFFACC15L), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "24K 4X", color = Color(0xFFFACC15L), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (slowMoTime > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33C084FCL))
                                .border(1.dp, Color(0xFFC084FCL), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "FLOW", color = Color(0xFFC084FCL), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (activeStageFx != null && stageFxRemainingRatio > 0.02f) {
                        val fxAccent = Color(activeStageFx.accentHex)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(fxAccent.copy(alpha = 0.20f))
                                .border(1.dp, fxAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✦ ${activeStageFx.shortLabel}",
                                color = fxAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                ArcadeIconButton(
                    iconBitmap = btnIconPause,
                    onClick = onPause,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 3. Center Score & Angled Judgment Badges
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 46.dp)
        ) {
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
                        text = "LOOP ${endlessLoopCount + 1} • +${(endlessLoopCount * 8)}% SPEED",
                        color = Color(0xFFFACC15L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 3b. Left-Edge Interactive "STAGE FX DROP BOARD"
        // Tap the left FX capsule to expand the Stage FX Board, pick any unlocked FX, and drop a Special FX Orb!
        val fxSpinInf = rememberInfiniteTransition(label = "fxBoardAnim")
        val fxSpinDeg by fxSpinInf.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (activeStageFx != null) 2800 else 6200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "fxSpin"
        )
        val fxPulse by fxSpinInf.animateFloat(
            initialValue = 1.0f,
            targetValue = if (fxDropCooldownRatio <= 0.01f && !isFxBoardExpanded) 1.06f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fxPulse"
        )

        val leadAccent = Color(activeStageFx?.accentHex ?: 0xFF38BDF8L)
        val leadSecondary = Color(activeStageFx?.secondaryHex ?: 0xFFA855F7L)
        val isDropReady = fxDropCooldownRatio <= 0.01f

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp)
        ) {
            // Compact FX Board Toggle Capsule
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = fxPulse
                        scaleY = fxPulse
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xE6050914L))
                    .border(
                        width = if (isFxBoardExpanded || activeStageFx != null) 1.8.dp else 1.3.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                leadAccent.copy(alpha = if (isDropReady) 0.95f else 0.45f),
                                leadSecondary.copy(alpha = if (isDropReady) 0.85f else 0.35f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { isFxBoardExpanded = !isFxBoardExpanded }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        val cx = size.width * 0.5f
                        val cy = size.height * 0.5f
                        val r = size.minDimension * 0.44f
                        drawCircle(
                            color = leadAccent.copy(alpha = 0.22f),
                            radius = r
                        )
                        // Recharge or Active FX ring
                        val sweep = when {
                            activeStageFx != null && stageFxRemainingRatio > 0.01f -> 360f * stageFxRemainingRatio.coerceIn(0f, 1f)
                            !isDropReady -> 360f * (1f - fxDropCooldownRatio.coerceIn(0f, 1f))
                            else -> 300f
                        }
                        drawArc(
                            brush = Brush.sweepGradient(listOf(leadAccent, leadSecondary, leadAccent)),
                            startAngle = if (isDropReady && activeStageFx == null) fxSpinDeg else -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(cx - r, cy - r),
                            size = Size(r * 2f, r * 2f),
                            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                        )
                        // 4-point starlight FX emblem
                        val rStar = r * 0.54f
                        val rIn = r * 0.16f
                        val star = Path().apply {
                            moveTo(cx, cy - rStar)
                            quadraticTo(cx + rIn, cy - rIn, cx + rStar, cy)
                            quadraticTo(cx + rIn, cy + rIn, cx, cy + rStar)
                            quadraticTo(cx - rIn, cy + rIn, cx - rStar, cy)
                            quadraticTo(cx - rIn, cy - rIn, cx, cy - rStar)
                            close()
                        }
                        drawPath(
                            path = star,
                            color = if (isDropReady) Color.White else Color.White.copy(alpha = 0.45f)
                        )
                    }
                    Text(
                        text = if (isFxBoardExpanded) "CLOSE" else if (isDropReady) "FX DROP" else "WAIT",
                        color = if (isDropReady) Color.White else Color.White.copy(alpha = 0.55f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            // Expandable Stage FX Selection Board
            AnimatedVisibility(
                visible = isFxBoardExpanded,
                enter = fadeIn(tween(160)) + expandVertically(tween(180)),
                exit = fadeOut(tween(130)) + shrinkVertically(tween(150))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xF2070D1EL), Color(0xF5030610L))
                            )
                        )
                        .border(
                            1.3.dp,
                            Brush.verticalGradient(
                                listOf(Color(0xFF38BDF8L).copy(alpha = 0.72f), Color(0xFFA855F7L).copy(alpha = 0.62f))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isDropReady) "DROP FX ORB" else "RECHARGING",
                        color = if (isDropReady) Color(0xFF7DD3FCL) else Color(0xFF94A3B8L),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                    StageFxBackdrop.entries.forEach { fx ->
                        val isUnlocked = fx in unlockedStageFx
                        val isCurrent = activeStageFx == fx
                        val fxPrimary = Color(fx.accentHex)
                        val fxSecondary = Color(fx.secondaryHex)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .width(92.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isCurrent -> fxPrimary.copy(alpha = 0.24f)
                                        isUnlocked && isDropReady -> Color(0xFF0E172AL)
                                        else -> Color(0x99080D18L)
                                    }
                                )
                                .border(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = when {
                                        isCurrent -> fxPrimary
                                        isUnlocked && isDropReady -> fxPrimary.copy(alpha = 0.65f)
                                        isUnlocked -> fxPrimary.copy(alpha = 0.28f)
                                        else -> Color.White.copy(alpha = 0.12f)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = isUnlocked && isDropReady) {
                                    onDropStageFxOrb(fx)
                                    isFxBoardExpanded = false
                                }
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isUnlocked) {
                                            Brush.linearGradient(listOf(fxPrimary, fxSecondary))
                                        } else {
                                            Brush.linearGradient(listOf(Color(0xFF334155L), Color(0xFF1E293BL)))
                                        }
                                    )
                            )
                            Text(
                                text = if (isUnlocked) fx.shortLabel else "LOCKED",
                                color = when {
                                    isUnlocked && isDropReady -> Color.White
                                    isUnlocked -> Color.White.copy(alpha = 0.55f)
                                    else -> Color(0xFF64748BL)
                                },
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Floating Right-Edge 24K Liquid Energy Capsule
        val isReady = overdriveProgress >= 0.75f || is24KOverdrive
        val isFullMax = overdriveProgress >= 0.99f || is24KOverdrive
        val infiniteTransition = rememberInfiniteTransition(label = "capsulePulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isFullMax) 1.14f else if (isReady) 1.07f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (isFullMax) 420 else 650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        val pulseGlow by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = if (isFullMax) 1.0f else if (isReady) 0.85f else 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (isFullMax) 420 else 650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
        val beaconRayAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "beaconRays"
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isReady) {
                Canvas(modifier = Modifier.size(62.dp, 135.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFFFACC15L).copy(alpha = if (isFullMax) 0.52f * pulseGlow else 0.28f * pulseGlow),
                                Color(0xFFFF8C00L).copy(alpha = if (isFullMax) 0.28f * pulseGlow else 0.14f * pulseGlow),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = size.width * (if (isFullMax) 0.95f else 0.70f)
                        ),
                        radius = size.width * (if (isFullMax) 0.95f else 0.70f),
                        center = Offset(cx, cy)
                    )
                    if (isFullMax) {
                        val haloR = size.width * 0.44f
                        for (arc in 0 until 3) {
                            drawArc(
                                color = Color(0xFFFEF08AL).copy(alpha = 0.72f * pulseGlow),
                                startAngle = (beaconRayAngle + arc * 120f) % 360f,
                                sweepAngle = 68f,
                                useCenter = false,
                                topLeft = Offset(cx - haloR, cy - haloR),
                                size = Size(haloR * 2f, haloR * 2f),
                                style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .width(36.dp)
                    .height(115.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xD9060A16L))
                    .border(
                        width = if (isFullMax) 2.5.dp else if (isReady) 1.8.dp else 1.2.dp,
                        brush = if (isReady) {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = pulseGlow),
                                    Color(0xFFFACC15L),
                                    Color(0xFFFB923CL)
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
                val fillHeightFraction = if (is24KOverdrive) (starPowerTime / 15.5f).coerceIn(0f, 1f) else overdriveProgress.coerceIn(0f, 1f)
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

                // Sculpted 4-Point Golden Starlight Emblem
                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                ) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    val rOut = size.minDimension * 0.48f
                    val rCtrl = size.minDimension * 0.14f
                    val star = Path().apply {
                        moveTo(cx, cy - rOut)
                        quadraticTo(cx + rCtrl, cy - rCtrl, cx + rOut, cy)
                        quadraticTo(cx + rCtrl, cy + rCtrl, cx, cy + rOut)
                        quadraticTo(cx - rCtrl, cy + rCtrl, cx - rOut, cy)
                        quadraticTo(cx - rCtrl, cy - rCtrl, cx, cy - rOut)
                        close()
                    }
                    drawPath(
                        path = star,
                        color = if (isReady) Color(0xFF0F172AL) else Color(0xFFFACC15L).copy(alpha = 0.75f)
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
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rOut = size.width * 0.48f
            val rCtrl = size.width * 0.16f
            val path = Path().apply {
                moveTo(cx, cy - rOut)
                quadraticTo(cx + rCtrl, cy - rCtrl, cx + rOut, cy)
                quadraticTo(cx + rCtrl, cy + rCtrl, cx, cy + rOut)
                quadraticTo(cx - rCtrl, cy + rCtrl, cx - rOut, cy)
                quadraticTo(cx - rCtrl, cy - rCtrl, cx, cy - rOut)
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
