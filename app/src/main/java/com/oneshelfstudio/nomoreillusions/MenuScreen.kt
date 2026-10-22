package com.oneshelfstudio.nomoreillusions

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
                    text = "♪ $pulsePoints",
                    color = Color(0xFFFACC15L),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                if (isVipUnlocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• VIP SAFE ADS",
                        color = Color(0xFF4ADE80L),
                        fontSize = 8.5.sp,
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
                    text = "LOBBY: ${activeLobbyTrack.title.uppercase()} ↻",
                    color = Color(0xFFC7D2FEL),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 26.dp)
        ) {
            // BESPOKE HOLOGRAPHIC VINYL TURNTABLE PLATTER + LASER TONE-ARM + RADIAL SPECTRUM
            Box(
                modifier = Modifier
                    .size(138.dp)
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = center.x
                    val cy = center.y
                    val spinSpeed = selectedRemixMode.audioSpeed
                    // 1. Outer 36-Bar Radial Audio Spectrum Ring
                    val barCount = 36
                    for (i in 0 until barCount) {
                        val ang = (i.toFloat() / barCount) * 6.283185f + menuTime * 0.32f * spinSpeed
                        val wave = 0.30f + 0.70f * kotlin.math.abs(sin(menuTime * 4.2f * spinSpeed + i * 0.45f))
                        val rInner = 58f
                        val rOuter = rInner + 8f + wave * 18f
                        val col = if (i % 2 == 0) modeAccent else Color(0xFF818CF8L)
                        drawLine(
                            color = col.copy(alpha = 0.48f + wave * 0.45f),
                            start = Offset(cx + cos(ang) * rInner, cy + sin(ang) * rInner),
                            end = Offset(cx + cos(ang) * rOuter, cy + sin(ang) * rOuter),
                            strokeWidth = 3.0f,
                            cap = StrokeCap.Round
                        )
                    }

                    // 2. Obsidian Vinyl Platter + Spinning Iridescent Grooves
                    drawCircle(
                        color = Color(0xFF050811L),
                        radius = 55f,
                        center = center
                    )
                    for (g in 1..4) {
                        val gr = 30f + g * 5.5f
                        drawCircle(
                            color = modeAccent.copy(alpha = 0.16f + (g % 2) * 0.10f),
                            radius = gr,
                            center = center,
                            style = Stroke(width = 1.1f)
                        )
                    }
                    drawCircle(
                        color = modeAccent.copy(alpha = glowAlpha * 0.65f),
                        radius = 56f,
                        center = center,
                        style = Stroke(width = 2.2f)
                    )

                    // 3. Holographic Laser Stylus / Tone-Arm reading the vinyl grooves
                    val armPivot = Offset(cx + 58f, cy - 48f)
                    val stylusTip = Offset(cx + 28f, cy + 12f)
                    drawLine(
                        color = Color(0xFFE2E8F0L).copy(alpha = 0.78f),
                        start = armPivot,
                        end = stylusTip,
                        strokeWidth = 2.6f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = modeAccent, radius = 4.5f, center = armPivot)
                    drawCircle(color = Color.White, radius = 3.2f, center = stylusTip)
                }
                Image(
                    bitmap = nmiLogo,
                    contentDescription = "No More Illusions Vinyl Emblem",
                    modifier = Modifier
                        .size(78.dp)
                        .graphicsLayer(rotationZ = (menuTime * 32f * selectedRemixMode.audioSpeed) % 360f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Artist Signature Header + Interactive Album Badge
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "No Morē Illusīons",
                    color = Color(0xFF4F46E5L).copy(alpha = 0.85f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.offset(x = 2.5.dp, y = 3.dp)
                )
                Text(
                    text = "No Morē Illusīons",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
            Text(
                text = "INTERACTIVE VISUAL ALBUM • BEST SCORE: $highScore",
                color = modeAccent,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Flagship Artist Stage Console (Track Deck + 4 Remix Modes + 3-Star Mastery)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(18.dp), ambientColor = modeAccent, spotColor = Color(0xFF6366F1L))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF2081322L),
                                Color(0xF8040811L)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(modeAccent.copy(alpha = 0.85f), Color(0xFF6366F1L).copy(alpha = 0.72f))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Track Vinyl Selector Cards with 3-Star Mastery Badges (01 CONFIDE | 02 NUMBER ONE)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        if (isSel) Brush.horizontalGradient(listOf(Color(0xFF0E291EL), Color(0xFF15193CL)))
                                        else Brush.horizontalGradient(listOf(Color(0xFF050A12L), Color(0xFF050A12L)))
                                    )
                                    .border(
                                        width = if (isSel) 1.6.dp else 1.dp,
                                        color = if (isSel) modeAccent else Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                    .clickable {
                                        if (isUnlocked) onSelectQuickTrack(idx) else onMusicDemo()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isUnlocked) "0$idx • ${(t.bpm * selectedRemixMode.audioSpeed).toInt()} BPM" else "0$idx • LOCKED",
                                            color = if (isSel) modeAccent else Color(0xFFA5B4FCL),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.6.sp
                                        )
                                        Text(
                                            text = starText,
                                            color = Color(0xFFFACC15L),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Text(
                                        text = t.title.uppercase(),
                                        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.45f),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(9.dp))

                    // 2. 4-Mode Replayability Selector (1.0x ORIGINAL | 1.2x NIGHTCORE | 0.85x SLOWED | ∞ ENDLESS)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "REMIX / GAMEPLAY MODE",
                            color = Color(0xFF94A3B8L),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.9.sp
                        )
                        Text(
                            text = selectedRemixMode.subtitle.uppercase(),
                            color = modeAccent,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TrackRemixMode.entries.forEach { mode ->
                            val isModeSel = mode == selectedRemixMode
                            val mCol = when (mode) {
                                TrackRemixMode.ORIGINAL -> Color(0xFF4ADE80L)
                                TrackRemixMode.OVERCLOCK -> Color(0xFFF43F5EL)
                                TrackRemixMode.SLOWED -> Color(0xFF38BDF8L)
                                TrackRemixMode.ENDLESS -> Color(0xFFFACC15L)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isModeSel) mCol.copy(alpha = 0.18f) else Color(0xFF050911L))
                                    .border(
                                        width = if (isModeSel) 1.4.dp else 1.dp,
                                        color = if (isModeSel) mCol else Color.White.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectRemixMode(mode) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = mode.shortTag,
                                        color = if (isModeSel) mCol else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = mode.id.take(7),
                                        color = Color.White.copy(alpha = if (isModeSel) 0.90f else 0.50f),
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. 3-Star Mastery Checklist Banner
                    Text(
                        text = "MASTERY: ★1 Clear Song  •  ★2 8x Beat Streak  •  ★3 Beat Nightcore/Slowed",
                        color = Color(0xFFC7D2FEL).copy(alpha = 0.85f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. High-Dopamine Launch Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF16A34AL),
                                        Color(0xFF0D9488L),
                                        Color(0xFF4F46E5L)
                                    )
                                )
                            )
                            .border(1.5.dp, modeAccent, RoundedCornerShape(12.dp))
                            .clickable { onStart() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶  DROP INTO ${selectedTrack.title.uppercase()} (${selectedRemixMode.shortTag})",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clean 3-Tile Bento Navigation Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MenuBentoTile(
                    title = "VAULT & SHOP",
                    subtitle = "Skins & Exclusives",
                    accent = Color(0xFFFACC15L),
                    onClick = onMusicDemo,
                    modifier = Modifier.weight(1.2f)
                )
                MenuBentoTile(
                    title = "HOW TO PLAY",
                    subtitle = "Beat Comets & Wormholes",
                    accent = Color(0xFF22C55EL),
                    onClick = onHowToPlay,
                    modifier = Modifier.weight(1.1f)
                )
                MenuBentoTile(
                    title = "SETTINGS",
                    subtitle = "Audio & Speed",
                    accent = Color(0xFFA855F7L),
                    onClick = onSettings,
                    modifier = Modifier.weight(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Authentic Vector Brand Icon Dock (Spotify, Apple Music, YouTube, Instagram, TikTok)
            Text(
                text = "STREAM & FOLLOW NO MORE ILLUSIONS",
                color = Color(0xFFA5B4FCL).copy(alpha = 0.78f),
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
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A111EL), Color(0xFF04070DL))
                )
            )
            .border(1.2.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(13.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
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
fun CountdownUI(value: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val label = if (value > 0) value.toString() else "DROP!"
        val color = if (value > 0) Color(0xFF818CF8L) else Color(0xFF4ADE80L)
        Text(
            text = label,
            color = color,
            fontSize = 104.sp,
            fontWeight = FontWeight.Black
        )
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
            .background(Color(0xF5020403L))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "RHYTHM & ORB GUIDE",
                color = Color(0xFF818CF8L),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(22.dp))
            InstructionItem("BEAT & LYRIC ORBS", "Follow the glowing constellation tether and catch Nebulous Orbs as their shrinking approach ring locks in on the beat and lyrics!")
            InstructionItem("STAGE TRANSFORMATIONS", "Drops, Hooks, and Breakdowns shift the arena into Speed Portals, Magic Rhythm Lanes, and Overdrive Shatter modes.")
            InstructionItem("RHYTHM HAZARDS", "Dodge Beat-Dashers (telegraphed by pink laser sights), sweeping Laser Gates, and Bass Nova Mines.")
            InstructionItem("ILLUSION FOCUS", "Hold FOCUS in the bottom-right corner to dilate time through dense breakdown patterns.")
            InstructionItem("LOBBY & ALBUM VAULT", "Switch studio lobby loops anytime on the menu, and beat tracks to unlock full playback & artwork.")
            Spacer(modifier = Modifier.height(28.dp))
            ArcadePillButton(
                text = "BACK TO MENU",
                pillBitmap = btnPillCyan,
                onClick = onBack,
                modifier = Modifier.width(240.dp).height(48.dp)
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
    onBack: () -> Unit
) {
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

            Spacer(modifier = Modifier.height(28.dp))
            ArcadePillButton(
                text = "SAVE & BACK",
                pillBitmap = btnPillGold,
                onClick = onBack,
                modifier = Modifier.width(240.dp).height(48.dp)
            )
        }
    }
}
