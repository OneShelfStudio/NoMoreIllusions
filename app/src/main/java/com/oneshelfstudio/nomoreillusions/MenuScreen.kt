package com.oneshelfstudio.nomoreillusions

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Suppress("UNUSED_PARAMETER")
@Composable
fun MenuUI(
    heroSolarMandala: ImageBitmap,
    heroCyberMandala: ImageBitmap,
    heroPrismMandala: ImageBitmap,
    btnPillGreen: ImageBitmap,
    btnPillGold: ImageBitmap,
    btnPillCyan: ImageBitmap,
    btnPillPink: ImageBitmap,
    customHeroSkin: String = "SOLAR",
    selectedTrackIndex: Int = 1,
    unlockedTrackCount: Int = 1,
    activeLobbyTrack: LobbyTrack = LobbyTrack.VELVET,
    selectedRemixMode: TrackRemixMode = TrackRemixMode.ORIGINAL,
    onSelectRemixMode: (TrackRemixMode) -> Unit = {},
    pulsePoints: Int = 0,
    highScore: Int = 0,
    trackMasteryStars: IntArray = intArrayOf(0, 0),
    isVipUnlocked: Boolean = false,
    onCycleLobbyTrack: () -> Unit = {},
    onSelectQuickTrack: (Int) -> Unit = {},
    onStart: () -> Unit,
    onMusicDemo: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pulse_prefs", android.content.Context.MODE_PRIVATE) }
    val unlockedExclusivesCount = remember(highScore, pulsePoints, isVipUnlocked) {
        if (isVipUnlocked) ARTIST_EXCLUSIVES_CATALOG.size
        else countUnlockedExclusives(prefs, highScore, pulsePoints)
    }
    val nmiLogo = ImageBitmap.imageResource(id = R.drawable.nmi_logo)
    val socialLinks = remember { MusicLibrary.officialSocialLinks }
    val selectedTrack = remember(selectedTrackIndex) { MusicLibrary.getTrack(selectedTrackIndex) }

    val infiniteTransition = rememberInfiniteTransition(label = "menu")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1150, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(tween(1150, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val menuTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 62.83185f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
        label = "menuTime"
    )

    val modeAccent = when (selectedRemixMode) {
        TrackRemixMode.ORIGINAL -> Color(0xFF4ADE80L)
        TrackRemixMode.OVERCLOCK -> Color(0xFFF43F5EL)
        TrackRemixMode.SLOWED -> Color(0xFF38BDF8L)
        TrackRemixMode.ENDLESS -> Color(0xFFFACC15L)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        modeAccent.copy(alpha = 0.16f),
                        Color(0xCC040812L),
                        Color(0xF8010205L)
                    )
                )
            )
            .statusBarsPadding()
            .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
    ) {
        // Top Frameless Stage Bar: ♪ Music Notes Balance + Lobby Synth Switcher (Zero Boxy Pills!)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onMusicDemo() }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "♪ $pulsePoints NOTES",
                    color = Color(0xFFFACC15L),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isVipUnlocked) "• ★ VIP" else "• 🔑 VIP",
                    color = if (isVipUnlocked) Color(0xFF4ADE80L) else Color(0xFFC084FCL),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onCycleLobbyTrack() }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(modeAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STAGE SYNTH: ${activeLobbyTrack.title.uppercase()} ↻",
                    color = Color(0xFFC7D2FEL),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 34.dp, bottom = 36.dp)
        ) {
            // BESPOKE HOLOGRAPHIC VINYL TURNTABLE PLATTER + LASER TONE-ARM + RADIAL SPECTRUM
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = center.x
                    val cy = center.y
                    val spinSpeed = selectedRemixMode.audioSpeed
                    for (rIdx in 0..2) {
                        val waveR = 54f + rIdx * 5.2f
                        val startDeg = ((menuTime * (if (rIdx % 2 == 0) 42f else -36f) * spinSpeed) + rIdx * 60f) % 360f
                        val ringCol = if (rIdx % 2 == 0) modeAccent else Color(0xFFEAB308L)
                        for (seg in 0..2) {
                            drawArc(
                                color = ringCol.copy(alpha = 0.68f - rIdx * 0.16f),
                                startAngle = startDeg + seg * 120f,
                                sweepAngle = 74f,
                                useCenter = false,
                                topLeft = Offset(cx - waveR, cy - waveR),
                                size = androidx.compose.ui.geometry.Size(waveR * 2f, waveR * 2f),
                                style = Stroke(width = 3.0f - rIdx * 0.5f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Obsidian Vinyl Platter + Spinning Grooves
                    drawCircle(color = Color(0xFF06080FL), radius = 51f, center = center)
                    for (g in 1..4) {
                        val gr = 28f + g * 5.2f
                        drawCircle(
                            color = modeAccent.copy(alpha = 0.16f + (g % 2) * 0.10f),
                            radius = gr,
                            center = center,
                            style = Stroke(width = 1.1f)
                        )
                    }
                    drawCircle(
                        color = modeAccent.copy(alpha = glowAlpha * 0.65f),
                        radius = 52f,
                        center = center,
                        style = Stroke(width = 2.2f)
                    )

                    // Smooth S-Curved Studio Tone-Arm
                    val armPivot = Offset(cx + 52f, cy - 42f)
                    val stylusTip = Offset(cx + 26f, cy + 10f)
                    val armPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(armPivot.x, armPivot.y)
                        quadraticTo(cx + 50f, cy - 8f, stylusTip.x, stylusTip.y)
                    }
                    drawPath(
                        path = armPath,
                        color = Color(0xFFFAF8F5L).copy(alpha = 0.82f),
                        style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                    )
                    drawCircle(color = modeAccent, radius = 4.5f, center = armPivot)
                    drawCircle(color = Color.White, radius = 3.2f, center = stylusTip)
                }
                Image(
                    bitmap = nmiLogo,
                    contentDescription = "No More Illusions Vinyl Emblem",
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer(rotationZ = (menuTime * 32f * selectedRemixMode.audioSpeed) % 360f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Artist Signature Header + High-Score Record Banner
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "No Morē Illusīons",
                    color = Color(0xFFEAB308L).copy(alpha = 0.45f),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.2.sp,
                    modifier = Modifier.offset(x = 2.dp, y = 2.5.dp)
                )
                Text(
                    text = "No Morē Illusīons",
                    color = Color(0xFFFAF8F5L),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.2.sp
                )
            }
            Text(
                text = "GAMIFIED STUDIO ALBUM • PERSONAL RECORD: $highScore PTS",
                color = Color(0xFFEAB308L),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            var showRewardsPeek by remember { mutableStateOf(false) }
            var showTrackDropdown by remember { mutableStateOf(false) }
            var showModeDropdown by remember { mutableStateOf(false) }

            // 1. Frameless Curiosity Reward Peek Line (Zero Boxy AI Pill Containers!)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRewardsPeek = !showRewardsPeek }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = if (showRewardsPeek) "🎁 FAN REWARDS ($unlockedExclusivesCount/${ARTIST_EXCLUSIVES_CATALOG.size} UNLOCKED) ▴"
                    else "🎁 WHAT CAN I WIN? ($unlockedExclusivesCount/${ARTIST_EXCLUSIVES_CATALOG.size} SECRETS) • TAP TO PEEK ▾",
                    color = Color(0xFFFACC15L),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.0.sp
                )

                if (showRewardsPeek) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "💿 Beat Any Track → Unlock Full Song & Cover Art in Album",
                            color = Color(0xFF4ADE80L),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "⚡ 2-Finger Plasma Hold → Master Vocal Swells for +500 Bonus",
                            color = Color(0xFF38BDF8L),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🏆 15,000+ on ∞ Endless → Artist Secret Message Slot Unlocked!",
                            color = Color(0xFFFACC15L),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "★ OPEN ARTIST VAULT & FAN NOTES →",
                            color = Color(0xFFFAF8F5L),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .clickable { onMusicDemo() }
                                .padding(top = 4.dp)
                        )
                    }
                }
            }

            // Subtle Laser Horizon Hairline Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, modeAccent.copy(alpha = 0.65f), Color(0xFFFACC15L).copy(alpha = 0.65f), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Frameless Open-Canvas Vinyl Stage Selector (Zero Boxy Console Cards or Squares!)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                val selStars = trackMasteryStars.getOrElse(selectedTrackIndex - 1) { 0 }.coerceIn(0, 3)
                val selStarStr = buildString { for (s in 1..3) append(if (s <= selStars) "★" else "☆") }
                val effBpm = (selectedTrack.bpm * selectedRemixMode.audioSpeed).toInt()
                val isSelArtUnlocked = prefs.getBoolean("art_unlocked_$selectedTrackIndex", false) || isVipUnlocked

                // A. Frameless Track Selector Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showTrackDropdown = !showTrackDropdown
                            if (showTrackDropdown) showModeDropdown = false
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "SELECTED TRACK • TAP TO PEEK PRIZE & SWITCH",
                        color = Color(0xFF94A3B8L),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TRACK 0$selectedTrackIndex",
                            color = Color(0xFFFAF8F5L),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.8.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSelArtUnlocked) "• ${selectedTrack.title.uppercase()}" else "• 🔒 MYSTERY PRIZE",
                            color = modeAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$selStarStr ${if (showTrackDropdown) "▴" else "▾"}",
                            color = Color(0xFFFACC15L),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "$effBpm BPM • ${if (isSelArtUnlocked) "SONG & COVER ART UNLOCKED" else "BEAT TRACK TO UNLOCK SONG & FAN NOTE"}",
                        color = Color(0xFFCBD5E1L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (showTrackDropdown) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        for (idx in 1..MusicLibrary.TOTAL_TRACKS) {
                            val t = MusicLibrary.getTrack(idx)
                            val isSel = idx == selectedTrackIndex
                            val isArtWon = prefs.getBoolean("art_unlocked_$idx", false) || isVipUnlocked
                            val st = trackMasteryStars.getOrElse(idx - 1) { 0 }.coerceIn(0, 3)
                            val stTxt = buildString { for (s in 1..3) append(if (s <= st) "★" else "☆") }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectQuickTrack(idx)
                                        showTrackDropdown = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                // Circular Vinyl Sleeve Prize Preview
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isArtWon) Color(0xFF4ADE80L) else Color(0xFFFACC15L), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TrackRevealOverlay(
                                        trackIndex = idx,
                                        progress = if (isArtWon) 1.0f else 0.18f,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (!isArtWon) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.45f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🔒", fontSize = 15.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "TRACK 0$idx • \"${t.title.uppercase()}\"",
                                            color = if (isSel) modeAccent else Color.White,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = stTxt,
                                            color = Color(0xFFFACC15L),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Text(
                                        text = if (isArtWon) "✓ UNLOCKED: Full Song + Cover Art + Artist Fan Note Slot"
                                        else "🎁 WIN: Full Song in Album + Cover Art + Artist Fan Note Slot",
                                        color = if (isArtWon) Color(0xFF4ADE80L) else Color(0xFFFACC15L),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.14f))
                )
                Spacer(modifier = Modifier.height(6.dp))

                // B. Frameless Speed & Mode Selector Line
                val modeHook = when (selectedRemixMode) {
                    TrackRemixMode.ORIGINAL -> "1.0x Pts • Unlocks Album Track in 1 run"
                    TrackRemixMode.OVERCLOCK -> "1.5x Pts • Faster highway + ★3 Gold Medal"
                    TrackRemixMode.SLOWED -> "0.5x Pts • Relaxed training (2 runs to unlock)"
                    TrackRemixMode.ENDLESS -> "🏆 Speeds up +8%/loop • 15,000+ unlocks Artist Secret Message!"
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showModeDropdown = !showModeDropdown
                            if (showModeDropdown) showTrackDropdown = false
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ SPEED & MODE: ${selectedRemixMode.shortTag} ${selectedRemixMode.displayName} ${if (showModeDropdown) "▴" else "▾"}",
                        color = modeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = modeHook,
                        color = Color(0xFFFAF8F5L).copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (showModeDropdown) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        TrackRemixMode.entries.forEach { mode ->
                            val isModeSel = mode == selectedRemixMode
                            val mCol = when (mode) {
                                TrackRemixMode.ORIGINAL -> Color(0xFF4ADE80L)
                                TrackRemixMode.OVERCLOCK -> Color(0xFFF43F5EL)
                                TrackRemixMode.SLOWED -> Color(0xFF38BDF8L)
                                TrackRemixMode.ENDLESS -> Color(0xFFEAB308L)
                            }
                            val hookText = when (mode) {
                                TrackRemixMode.ORIGINAL -> "1.0x Pts • Unlocks song in 1 run"
                                TrackRemixMode.OVERCLOCK -> "1.5x Pts • Faster notes + ★3 Gold Medal"
                                TrackRemixMode.SLOWED -> "0.5x Pts • Relaxed speed (2 runs to unlock)"
                                TrackRemixMode.ENDLESS -> "🏆 Speeds up every loop! 15,000+ pts unlocks Artist Secret Message!"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectRemixMode(mode)
                                        showModeDropdown = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${mode.shortTag} ${mode.displayName}",
                                        color = mCol,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = hookText,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (isModeSel) {
                                    Text(
                                        text = "● ACTIVE",
                                        color = mCol,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // C. Custom Vinyl-Groove Stage Launch Emblem (Frameless Glowing Stage Igniter!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(54.dp)
                        .clickable { onStart() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        // Glowing laser horizon bars top & bottom + vinyl arc caps
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, modeAccent, Color(0xFFFACC15L), modeAccent, Color.Transparent)
                            ),
                            start = Offset(0f, 2f),
                            end = Offset(w, 2f),
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, modeAccent, Color(0xFFFACC15L), modeAccent, Color.Transparent)
                            ),
                            start = Offset(0f, h - 2f),
                            end = Offset(w, h - 2f),
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round
                        )
                    }
                    Text(
                        text = "▶  PLAY TRACK $selectedTrackIndex  ◀",
                        color = Color(0xFFFAF8F5L),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Frameless Floating Stage Icons (Zero Boxy AI Tiles!)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                MenuBentoTile(
                    title = "💿 ALBUM & VAULT",
                    subtitle = "Songs & Fan Notes",
                    accent = Color(0xFFEAB308L),
                    onClick = onMusicDemo
                )
                MenuBentoTile(
                    title = "🎬 MINI VIDEO",
                    subtitle = "How To Play",
                    accent = Color(0xFF4ADE80L),
                    onClick = onHowToPlay
                )
                MenuBentoTile(
                    title = "🎚️ STUDIO",
                    subtitle = "Audio & VIP",
                    accent = Color(0xFF38BDF8L),
                    onClick = onSettings
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Authentic Vector Brand Icon Dock (Spotify, Apple Music, YouTube, Instagram, TikTok)
            Text(
                text = "STREAM & FOLLOW NO MORE ILLUSIONS",
                color = Color(0xFFEAB308L).copy(alpha = 0.82f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            SocialBrandIconRow(
                links = socialLinks,
                showLabels = false,
                onOpenUrl = { url ->
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            )
        }
    }
}

@Composable
private fun MenuBentoTile(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFFFAF8F5L),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = accent,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(2.dp)
                .background(accent.copy(alpha = 0.75f))
        )
    }
}

@Composable
fun CountdownUI(value: Int, visible: Boolean = true) {
    if (!visible) return
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val label = if (value > 0) value.toString() else "DROP!"
        val color = if (value > 0) Color(0xFF38BDF8L) else Color(0xFF4ADE80L)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (value > 0) "GET READY" else "MUSIC STARTING",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp
            )
            Text(
                text = label,
                color = color,
                fontSize = 104.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun HowToPlayUI(
    btnPillCyan: ImageBitmap,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF504060CL))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "HOW TO PLAY • VIDEO GUIDE",
                color = Color(0xFFEAB308L),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Band Hero Rhythm Highway • Labeled Pickups • High-Score Chases",
                color = Color(0xFFCBD5E1L),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Live 12-Second Animated Gameplay Mini-Video Reel
            MiniGameplayVideoReel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            InstructionItem(
                "1. CATCH NOTES ON THE BEAT STRIKE LINE",
                "Notes scroll down the 4-lane perspective highway. Catch them right as they cross the glowing lower Beat Strike Line for ★ PERFECT ON-BEAT (+200) and Fret Flames!"
            )
            InstructionItem(
                "2. TWO-FINGER TWIN HERO & VOCAL SUSTAIN HOLDS",
                "Touch with a 2nd finger anytime to spawn a Twin Hero linked by a live Electric/Liquid Plasma Tether! On flowy vocal swells, hold 2 fingers on the Beat Strike Line to charge 100% Vocal Harmony (+500)!"
            )
            InstructionItem(
                "3. GRAB 💥 BOMB TO VAPORIZE ALL ENEMIES",
                "Every item is clearly labeled! Grabbing 💥 BOMB triggers a shockwave that instantly destroys every red enemy on screen (+150 pts each)."
            )
            InstructionItem(
                "4. GRAB 🛡️ SHIELD & CHASE ∞ ENDLESS HIGH SCORES",
                "🛡️ SHIELD blocks 1 hazard hit. Play ∞ ENDLESS LOOP mode on the main menu to loop songs at escalating speeds for massive high-score records!"
            )
            Spacer(modifier = Modifier.height(16.dp))
            ArcadePillButton(
                text = "BACK TO STUDIO MENU",
                pillBitmap = btnPillCyan,
                onClick = onBack,
                modifier = Modifier.width(240.dp).height(46.dp)
            )
        }
    }
}

@Composable
fun SettingsUI(
    isMusic: Boolean,
    isSound: Boolean,
    isHaptic: Boolean,
    difficulty: GameDifficulty,
    activeLobbyTrack: LobbyTrack = LobbyTrack.VELVET,
    onLobbyTrackChange: (LobbyTrack) -> Unit = {},
    btnPillGold: ImageBitmap,
    onMusicToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onDifficultyChange: (GameDifficulty) -> Unit,
    isVipUnlocked: Boolean = false,
    onActivateVipPasscode: () -> Unit = {},
    onBack: () -> Unit
) {
    var vipCodeInput by remember { mutableStateOf("") }
    var vipError by remember { mutableStateOf(false) }
    var vipSuccess by remember { mutableStateOf(isVipUnlocked) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2030605L))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "SETTINGS",
                color = Color(0xFF4ADE80L),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070D0AL))
                    .border(1.dp, Color(0xFF6366F1L).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    ToggleItem("MUSIC", isMusic, onMusicToggle)
                    ToggleItem("SOUND FX", isSound, onSoundToggle)
                    ToggleItem("HAPTICS", isHaptic, onHapticToggle)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "LOBBY BACKGROUND MUSIC (STUDIO SYNTH)",
                color = Color(0xFFA5B4FCL),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LobbyTrack.entries.forEach { lt ->
                    val sel = lt == activeLobbyTrack
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) Color(0xFF0E291AL) else Color(0xFF060B12L))
                            .border(1.2.dp, if (sel) Color(0xFF4ADE80L) else Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                            .clickable { onLobbyTrackChange(lt) }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = lt.title.uppercase(),
                                color = if (sel) Color(0xFF4ADE80L) else Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            Text(
                                text = "${lt.bpm.toInt()} BPM",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "GAME DIFFICULTY",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.padding(top = 8.dp)) {
                DifficultyOption(GameDifficulty.EASY, difficulty == GameDifficulty.EASY) { onDifficultyChange(it) }
                DifficultyOption(GameDifficulty.REGULAR, difficulty == GameDifficulty.REGULAR) { onDifficultyChange(it) }
                DifficultyOption(GameDifficulty.HARD, difficulty == GameDifficulty.HARD) { onDifficultyChange(it) }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0D18L))
                    .border(
                        1.2.dp,
                        if (vipSuccess || isVipUnlocked) Color(0xFF4ADE80L) else Color(0xFFFACC15L).copy(alpha = 0.65f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (vipSuccess || isVipUnlocked) "★ ARTIST VIP PASSCODE ACTIVE (ALL UNLOCKED!)" else "🔑 ARTIST VIP PASSCODE (UNLOCK ALL OPTIONS)",
                        color = if (vipSuccess || isVipUnlocked) Color(0xFF4ADE80L) else Color(0xFFFACC15L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    if (!vipSuccess && !isVipUnlocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF04070FL))
                                    .border(
                                        1.dp,
                                        if (vipError) Color(0xFFF43F5EL) else Color(0xFF6366F1L).copy(alpha = 0.6f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (vipCodeInput.isEmpty()) {
                                    Text(
                                        text = "ENTER PASSCODE...",
                                        color = Color(0xFF64748BL),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                BasicTextField(
                                    value = vipCodeInput,
                                    onValueChange = {
                                        vipCodeInput = it.uppercase()
                                        vipError = false
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFACC15L), Color(0xFFD97706L))
                                        )
                                    )
                                    .clickable {
                                        val clean = vipCodeInput.trim().uppercase()
                                        if (clean == "ETHAN" || clean == "CONFIDE" || clean == "ILLUSIONS2026" || clean == "NMI2026") {
                                            vipSuccess = true
                                            vipError = false
                                            onActivateVipPasscode()
                                        } else {
                                            vipError = true
                                        }
                                    }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "UNLOCK",
                                    color = Color(0xFF050811L),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            ArcadePillButton(
                text = "SAVE & BACK",
                pillBitmap = btnPillGold,
                onClick = onBack,
                modifier = Modifier.width(240.dp).height(48.dp)
            )
        }
    }
}
