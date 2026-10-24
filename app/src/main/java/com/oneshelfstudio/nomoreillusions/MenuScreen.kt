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
        // Top Studio Control Bar: ♪ Music Notes Balance + Lobby Synth Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // Music Notes Wallet Pill (tappable to open Customizer & Vault)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xD9050B14L))
                    .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .clickable { onMusicDemo() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "♪ $pulsePoints NOTES",
                    color = Color(0xFFFACC15L),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                if (isVipUnlocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ★ VIP UNLOCKED",
                        color = Color(0xFF4ADE80L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• 🔑 VIP",
                        color = Color(0xFFC084FCL),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Selectable Studio Lobby Music Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xD9050B14L))
                    .border(1.dp, Color(0xFF6366F1L).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .clickable { onCycleLobbyTrack() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(modeAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MENU MUSIC: ${activeLobbyTrack.title.uppercase()} ↻",
                    color = Color(0xFFC7D2FEL),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
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

            // Compact 1-Line Artist Vault Pill (Zero Wordy Paragraphs!)
            val sleeveShape = CutCornerShape(topStart = 9.dp, bottomEnd = 9.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(sleeveShape)
                    .background(Color(0xFF100E07L))
                    .border(1.3.dp, Color(0xFFEAB308L).copy(alpha = 0.85f), sleeveShape)
                    .clickable { onMusicDemo() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "★ ETHAN'S VAULT & LINER NOTES",
                        color = Color(0xFFEAB308L),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp
                    )
                    Text(
                        text = "$unlockedExclusivesCount/${ARTIST_EXCLUSIVES_CATALOG.size} UNLOCKED →",
                        color = Color(0xFFFAF8F5L),
                        fontSize = 8.8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Pop & Zoom Studio Console (Clean, Visual-First, Minimal Text)
            val consoleShape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, consoleShape, ambientColor = modeAccent, spotColor = Color(0xFFEAB308L))
                    .clip(consoleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF50A0E17L),
                                Color(0xFA05070CL)
                            )
                        )
                    )
                    .border(
                        width = 1.6.dp,
                        brush = Brush.horizontalGradient(
                            listOf(modeAccent.copy(alpha = 0.90f), Color(0xFFEAB308L).copy(alpha = 0.78f))
                        ),
                        shape = consoleShape
                    )
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Tactile Pop & Zoom Vinyl Track Cards (SIDE A: CONFIDE | SIDE B: NUMBER ONE)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (idx in 1..MusicLibrary.TOTAL_TRACKS) {
                            val t = MusicLibrary.getTrack(idx)
                            val isSel = idx == selectedTrackIndex
                            val isUnlocked = idx <= unlockedTrackCount
                            val stars = trackMasteryStars.getOrElse(idx - 1) { 0 }.coerceIn(0, 3)
                            val starText = buildString {
                                for (s in 1..3) append(if (s <= stars) "★" else "☆")
                            }
                            val cardScale by animateFloatAsState(
                                targetValue = if (isSel) 1.05f else 0.96f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                label = "trackScale$idx"
                            )
                            val cardCut = CutCornerShape(topEnd = 10.dp, bottomStart = 10.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        scaleX = cardScale
                                        scaleY = cardScale
                                    }
                                    .shadow(if (isSel) 10.dp else 2.dp, cardCut, ambientColor = modeAccent)
                                    .clip(cardCut)
                                    .background(
                                        if (isSel) Brush.horizontalGradient(listOf(Color(0xFF14211BL), Color(0xFF1A1508L)))
                                        else Brush.horizontalGradient(listOf(Color(0xFF070A10L), Color(0xFF070A10L)))
                                    )
                                    .border(
                                        width = if (isSel) 2.2.dp else 1.dp,
                                        color = if (isSel) modeAccent else Color.White.copy(alpha = 0.16f),
                                        shape = cardCut
                                    )
                                    .clickable {
                                        if (isUnlocked) onSelectQuickTrack(idx) else onMusicDemo()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 10.dp)
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isUnlocked) "SIDE 0$idx • ${(t.bpm * selectedRemixMode.audioSpeed).toInt()} BPM" else "SIDE 0$idx • LOCKED",
                                            color = if (isSel) modeAccent else Color(0xFF94A3B8L),
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = starText,
                                            color = Color(0xFFEAB308L),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = t.title.uppercase(),
                                        color = if (isUnlocked) Color(0xFFFAF8F5L) else Color.White.copy(alpha = 0.45f),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Tactile Pop & Zoom Speed Switches (Pop-Out Card Reveals Crisp 1-Line Spec!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TrackRemixMode.entries.forEach { mode ->
                            val isModeSel = mode == selectedRemixMode
                            val mCol = when (mode) {
                                TrackRemixMode.ORIGINAL -> Color(0xFF4ADE80L)
                                TrackRemixMode.OVERCLOCK -> Color(0xFFF43F5EL)
                                TrackRemixMode.SLOWED -> Color(0xFF38BDF8L)
                                TrackRemixMode.ENDLESS -> Color(0xFFEAB308L)
                            }
                            val modeScale by animateFloatAsState(
                                targetValue = if (isModeSel) 1.08f else 0.94f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                label = "modeScale_${mode.name}"
                            )
                            val switchShape = RoundedCornerShape(6.dp)
                            Box(
                                modifier = Modifier
                                    .weight(if (isModeSel) 1.25f else 0.92f)
                                    .graphicsLayer {
                                        scaleX = modeScale
                                        scaleY = modeScale
                                    }
                                    .shadow(if (isModeSel) 8.dp else 0.dp, switchShape, ambientColor = mCol)
                                    .clip(switchShape)
                                    .background(if (isModeSel) mCol.copy(alpha = 0.24f) else Color(0xFF06080EL))
                                    .border(
                                        width = if (isModeSel) 2.0.dp else 1.dp,
                                        color = if (isModeSel) mCol else Color.White.copy(alpha = 0.16f),
                                        shape = switchShape
                                    )
                                    .clickable { onSelectRemixMode(mode) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${mode.shortTag} ${mode.displayName}",
                                        color = if (isModeSel) mCol else Color(0xFFFAF8F5L).copy(alpha = 0.78f),
                                        fontSize = if (isModeSel) 10.sp else 8.8.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1
                                    )
                                    if (isModeSel) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = mode.subtitle,
                                            color = Color(0xFFFAF8F5L),
                                            fontSize = 7.4.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Chamfered Studio Master Launch Button
                    val launchShape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(12.dp, launchShape, ambientColor = modeAccent)
                            .clip(launchShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF15803DL),
                                        Color(0xFF0F766EL),
                                        Color(0xFFB45309L)
                                    )
                                )
                            )
                            .border(2.0.dp, modeAccent, launchShape)
                            .clickable { onStart() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶  PLAY ${selectedTrack.title.uppercase()} (${selectedRemixMode.displayName})",
                            color = Color(0xFFFAF8F5L),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Tile Studio Console Navigation Row (Always visible, crisp 1-word titles!)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MenuBentoTile(
                    title = "★ VAULT",
                    subtitle = "Album & Notes",
                    accent = Color(0xFFEAB308L),
                    onClick = onMusicDemo,
                    modifier = Modifier.weight(1.1f)
                )
                MenuBentoTile(
                    title = "▶ REEL",
                    subtitle = "Mini Video",
                    accent = Color(0xFF4ADE80L),
                    onClick = onHowToPlay,
                    modifier = Modifier.weight(1.0f)
                )
                MenuBentoTile(
                    title = "⚙ OPTIONS",
                    subtitle = "Audio & Setup",
                    accent = Color(0xFF38BDF8L),
                    onClick = onSettings,
                    modifier = Modifier.weight(1.0f)
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
    val tileShape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(tileShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D111CL), Color(0xFF06080EL))
                )
            )
            .border(1.3.dp, accent.copy(alpha = 0.72f), tileShape)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color(0xFFFAF8F5L),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
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
        }
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
                "2. HOLD & SLIDE SUSTAIN RIBBONS",
                "When a golden HOLD & SLIDE ♪ ribbon scrolls down the highway, steer onto the note head and follow the glowing ribbon tail for rapid combo points!"
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
