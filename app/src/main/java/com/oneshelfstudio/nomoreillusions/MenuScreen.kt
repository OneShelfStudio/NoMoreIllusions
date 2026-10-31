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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic High-Fidelity Spinning Vinyl Record.
 * Features an obsidian vinyl disc, etched micro-grooves, dual specular light sheen reflections,
 * a circular album art picture center label, spindle core, and frosted greyed-out lock overlay when locked.
 */
@Composable
fun SpinningVinylRecord(
    trackIndex: Int,
    artwork: ImageBitmap,
    isUnlocked: Boolean,
    isSelected: Boolean,
    spinAngle: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
    sizeDp: Int = 70,
    showLockOverlay: Boolean = true,
    unlockCost: Int = 1200
) {
    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        // Continuous Spinning Disc Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    rotationZ = if (isUnlocked && isSelected) spinAngle
                    else if (isUnlocked) (spinAngle * 0.35f) % 360f
                    else 0f
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension * 0.49f

                // 1. Obsidian Vinyl Platter Base
                drawCircle(
                    color = if (isUnlocked) Color(0xFF06080FL) else Color(0xFF0C1019L),
                    radius = r,
                    center = Offset(cx, cy)
                )

                // 2. Etched Micro-Grooves (Light catch rings)
                val grooveAlpha = if (isUnlocked) 0.14f else 0.05f
                for (g in 1..4) {
                    val gr = r * (0.62f + g * 0.085f)
                    drawCircle(
                        color = Color.White.copy(alpha = grooveAlpha),
                        radius = gr,
                        center = Offset(cx, cy),
                        style = Stroke(width = 0.9f)
                    )
                }

                // 3. Specular Light Sheen Reflections (Dual rotating gloss arcs)
                val sheenAlpha = if (isUnlocked) 0.18f else 0.05f
                drawArc(
                    color = Color.White.copy(alpha = sheenAlpha),
                    startAngle = 25f,
                    sweepAngle = 48f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.88f, cy - r * 0.88f),
                    size = androidx.compose.ui.geometry.Size(r * 1.76f, r * 1.76f),
                    style = Stroke(width = r * 0.16f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White.copy(alpha = sheenAlpha * 0.8f),
                    startAngle = 205f,
                    sweepAngle = 48f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.88f, cy - r * 0.88f),
                    size = androidx.compose.ui.geometry.Size(r * 1.76f, r * 1.76f),
                    style = Stroke(width = r * 0.16f, cap = StrokeCap.Round)
                )

                // 4. Outer Vinyl Rim
                drawCircle(
                    color = if (isSelected && isUnlocked) accentColor.copy(alpha = 0.90f)
                    else if (isUnlocked) Color(0xFF475569L).copy(alpha = 0.50f)
                    else Color(0xFF334155L).copy(alpha = 0.40f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (isSelected && isUnlocked) 2.2f else 1.2f)
                )
            }

            // 5. Circular Center Album Art Picture Label
            val labelSizeDp = (sizeDp * 0.58f).dp
            Box(
                modifier = Modifier
                    .size(labelSizeDp)
                    .clip(CircleShape)
                    .border(
                        width = 1.4.dp,
                        color = if (isUnlocked) (if (isSelected) accentColor else Color(0xFFFACC15L)) else Color(0xFF475569L),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = artwork,
                    contentDescription = "Track $trackIndex Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = if (!isUnlocked) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.0f) })
                    } else null,
                    alpha = if (isUnlocked) 1.0f else 0.45f
                )

                // Center Spindle Hole (Solid Obsidian core + Metallic Brass Ring)
                Box(
                    modifier = Modifier
                        .size((sizeDp * 0.13f).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF030508L))
                        .border(1.2.dp, if (isUnlocked) Color(0xFFFACC15L) else Color(0xFF64748BL), CircleShape)
                )
            }
        }

        // 6. Frosted Greyed-Out Lock Overlay (Non-spinning for crystal-clear readability)
        if (!isUnlocked && showLockOverlay) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.74f).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD9050811L))
                    .border(1.2.dp, Color(0xFF64748BL).copy(alpha = 0.70f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "🔒",
                        fontSize = (sizeDp * 0.20f).sp
                    )
                    Text(
                        text = "LOCKED",
                        color = Color(0xFFE2E8F0L),
                        fontSize = (sizeDp * 0.10f).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    if (unlockCost > 0) {
                        Text(
                            text = "$unlockCost ♪",
                            color = Color(0xFFFACC15L),
                            fontSize = (sizeDp * 0.09f).sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

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
    isBandHeroMode: Boolean = false,
    onToggleBandHeroMode: (Boolean) -> Unit = {},
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
    val track01Art = ImageBitmap.imageResource(id = R.drawable.track_01)
    val track02Art = ImageBitmap.imageResource(id = R.drawable.track_02)
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
            val selectedTrackArt = if (selectedTrackIndex == 1) track01Art else track02Art
            val isCurrentTrackUnlocked = selectedTrackIndex == 1 || unlockedTrackCount >= selectedTrackIndex || isVipUnlocked || prefs.getBoolean("art_unlocked_$selectedTrackIndex", false) || prefs.getBoolean("track_unlocked_$selectedTrackIndex", false)
            val platterSpinSpeed = selectedRemixMode.audioSpeed

            Box(
                modifier = Modifier
                    .size(136.dp)
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = center.x
                    val cy = center.y
                    for (rIdx in 0..2) {
                        val waveR = 56f + rIdx * 5.4f
                        val startDeg = ((menuTime * (if (rIdx % 2 == 0) 42f else -36f) * platterSpinSpeed) + rIdx * 60f) % 360f
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

                    // Obsidian Vinyl Platter + Grooves
                    drawCircle(color = if (isCurrentTrackUnlocked) Color(0xFF06080FL) else Color(0xFF0C1019L), radius = 53f, center = center)
                    for (g in 1..4) {
                        val gr = 29f + g * 5.4f
                        drawCircle(
                            color = if (isCurrentTrackUnlocked) modeAccent.copy(alpha = 0.16f + (g % 2) * 0.10f) else Color.White.copy(alpha = 0.05f),
                            radius = gr,
                            center = center,
                            style = Stroke(width = 1.1f)
                        )
                    }
                    drawCircle(
                        color = if (isCurrentTrackUnlocked) modeAccent.copy(alpha = glowAlpha * 0.65f) else Color(0xFF475569L).copy(alpha = 0.40f),
                        radius = 54f,
                        center = center,
                        style = Stroke(width = 2.2f)
                    )

                    // Smooth S-Curved Studio Tone-Arm (rests on groove when unlocked, lifts away when locked)
                    val armPivot = Offset(cx + 54f, cy - 44f)
                    val stylusTip = if (isCurrentTrackUnlocked) Offset(cx + 26f, cy + 10f) else Offset(cx + 46f, cy - 20f)
                    val armPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(armPivot.x, armPivot.y)
                        quadraticTo(cx + 50f, cy - 8f, stylusTip.x, stylusTip.y)
                    }
                    drawPath(
                        path = armPath,
                        color = Color(0xFFFAF8F5L).copy(alpha = if (isCurrentTrackUnlocked) 0.85f else 0.45f),
                        style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                    )
                    drawCircle(color = if (isCurrentTrackUnlocked) modeAccent else Color(0xFF64748BL), radius = 4.5f, center = armPivot)
                    drawCircle(color = if (isCurrentTrackUnlocked) Color.White else Color(0xFF94A3B8L), radius = 3.2f, center = stylusTip)
                }

                // Spinning Vinyl Center Label with Real Track Picture
                val platterLabelSize = 74.dp
                Box(
                    modifier = Modifier
                        .size(platterLabelSize)
                        .graphicsLayer(
                            rotationZ = if (isCurrentTrackUnlocked) (menuTime * 32f * platterSpinSpeed) % 360f else 0f
                        )
                        .clip(CircleShape)
                        .border(
                            width = 1.8.dp,
                            color = if (isCurrentTrackUnlocked) modeAccent else Color(0xFF475569L),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = selectedTrackArt,
                        contentDescription = "No More Illusions Active Track Artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = if (!isCurrentTrackUnlocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.0f) }) else null,
                        alpha = if (isCurrentTrackUnlocked) 1.0f else 0.42f
                    )
                    // Metallic Spindle Hole
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF030508L))
                            .border(1.2.dp, if (isCurrentTrackUnlocked) Color(0xFFFACC15L) else Color(0xFF64748BL), CircleShape)
                    )
                }

                // Frosted Greyed-Out Lock Overlay on Turntable Platter
                if (!isCurrentTrackUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xD9050811L))
                            .border(1.2.dp, Color(0xFF64748BL).copy(alpha = 0.75f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🔒", fontSize = 16.sp)
                            Text(
                                text = "LOCKED",
                                color = Color(0xFFCBD5E1L),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                }
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

            var peekPrizeTrackIdx by remember { mutableStateOf<Int?>(null) }

            // 1. ZERO-READING "TOUCH HERE -> GET THIS" DUAL VINYL STAGE DECK (Track 01 & Track 02 Side-by-Side)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                for (idx in 1..MusicLibrary.TOTAL_TRACKS) {
                    val t = MusicLibrary.getTrack(idx)
                    val isSel = idx == selectedTrackIndex
                    val isTrkUnlocked = idx == 1 || unlockedTrackCount >= idx || isVipUnlocked || prefs.getBoolean("art_unlocked_$idx", false) || prefs.getBoolean("track_unlocked_$idx", false)
                    val st = trackMasteryStars.getOrElse(idx - 1) { 0 }.coerceIn(0, 3)
                    val stTxt = buildString { for (s in 1..3) append(if (s <= st) "★" else "☆") }
                    val discAccent = when {
                        !isTrkUnlocked -> Color(0xFF475569L)
                        isSel -> modeAccent
                        else -> Color(0xFF38BDF8L)
                    }
                    val trackArt = if (idx == 1) track01Art else track02Art

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
                            .background(
                                Brush.verticalGradient(
                                    when {
                                        !isTrkUnlocked -> listOf(Color(0xFF090D18L), Color(0xFF03050BL))
                                        isSel -> listOf(discAccent.copy(alpha = 0.24f), Color(0xFF070D1BL))
                                        else -> listOf(Color(0xFF080E1CL), Color(0xFF040710L))
                                    }
                                )
                            )
                            .border(
                                width = if (isSel && isTrkUnlocked) 2.2.dp else 1.2.dp,
                                color = when {
                                    !isTrkUnlocked -> Color(0xFF334155L)
                                    isSel -> discAccent
                                    else -> Color.White.copy(alpha = 0.18f)
                                },
                                shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
                            )
                            .clickable { onSelectQuickTrack(idx) }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Top Row: Track Number + Lock/Unlocked Status Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TRACK 0$idx",
                                    color = if (isTrkUnlocked) (if (isSel) discAccent else Color(0xFF94A3B8L)) else Color(0xFF64748BL),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp))
                                        .background(if (isTrkUnlocked) Color(0xFF064E3BL) else Color(0xFF1E293BL))
                                        .border(
                                            1.dp,
                                            if (isTrkUnlocked) Color(0xFF4ADE80L) else Color(0xFF64748BL),
                                            CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp)
                                        )
                                        .clickable { peekPrizeTrackIdx = if (peekPrizeTrackIdx == idx) null else idx }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isTrkUnlocked) "★ UNLOCKED" else "🔒 ${t.unlockCost} ♪",
                                        color = if (isTrkUnlocked) Color(0xFF4ADE80L) else Color(0xFFCBD5E1L),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive Spinning Vinyl Record Disc with Real Track Picture
                            SpinningVinylRecord(
                                trackIndex = idx,
                                artwork = trackArt,
                                isUnlocked = isTrkUnlocked,
                                isSelected = isSel,
                                spinAngle = (menuTime * 45f * selectedRemixMode.audioSpeed) % 360f,
                                accentColor = discAccent,
                                sizeDp = 70,
                                showLockOverlay = true,
                                unlockCost = t.unlockCost
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = t.title.uppercase(),
                                color = if (isTrkUnlocked) Color.White else Color(0xFF94A3B8L),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            Text(
                                text = if (isTrkUnlocked) "$stTxt • READY" else "🔒 BEAT STAGE 1 OR ${t.unlockCost} ♪",
                                color = if (isTrkUnlocked) Color(0xFF4ADE80L) else Color(0xFF64748BL),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Visual Tap-to-Peek Prize Card (Opens when tapping status badge on either Vinyl Disc)
            val peekIdx = peekPrizeTrackIdx
            if (peekIdx != null) {
                val peekTrack = MusicLibrary.getTrack(peekIdx)
                val peekWon = peekIdx == 1 || unlockedTrackCount >= peekIdx || isVipUnlocked || prefs.getBoolean("art_unlocked_$peekIdx", false) || prefs.getBoolean("track_unlocked_$peekIdx", false)
                val peekArt = if (peekIdx == 1) track01Art else track02Art
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
                        .background(if (peekWon) Color(0xFF0A1224L) else Color(0xFF0F172AL))
                        .border(1.5.dp, if (peekWon) Color(0xFFFACC15L) else Color(0xFF475569L), CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
                        .clickable { peekPrizeTrackIdx = null }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, if (peekWon) Color(0xFFFACC15L) else Color(0xFF64748BL), CircleShape)
                    ) {
                        Image(
                            bitmap = peekArt,
                            contentDescription = "Track $peekIdx Artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = if (!peekWon) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.0f) }) else null,
                            alpha = if (peekWon) 1f else 0.5f
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (peekWon) "★ TRACK 0$peekIdx UNLOCKED & READY!" else "🔒 BEAT TRACK 01 OR UNLOCK FOR ${peekTrack.unlockCost} ♪:",
                            color = if (peekWon) Color(0xFFFACC15L) else Color(0xFFCBD5E1L),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "💿 \"${peekTrack.title}\" Official Master Track (${peekTrack.bpm.toInt()} BPM • ${peekTrack.durationSec / 60}:${"%02d".format(peekTrack.durationSec % 60)})",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "✕",
                        color = Color(0xFF94A3B8L),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 4 SCULPTED CHAMFERED NEON MODE GEMS (Touch Here -> Get This: Chill, Normal, Fast, Band Hero!)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val modes = listOf(
                    Triple("🌙 CHILL", "0.5x • 2 Runs", TrackRemixMode.SLOWED),
                    Triple("⚡ NORMAL", "1.0x • 1 Run", TrackRemixMode.ORIGINAL),
                    Triple("🔥 FAST", "1.5x Score", TrackRemixMode.OVERCLOCK)
                )
                modes.forEach { (label, sub, mode) ->
                    val isActive = !isBandHeroMode && selectedRemixMode == mode
                    val gemCol = when (mode) {
                        TrackRemixMode.SLOWED -> Color(0xFF38BDF8L)
                        TrackRemixMode.ORIGINAL -> Color(0xFF4ADE80L)
                        TrackRemixMode.OVERCLOCK -> Color(0xFFF43F5EL)
                        TrackRemixMode.ENDLESS -> Color(0xFFFACC15L)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                            .background(if (isActive) gemCol.copy(alpha = 0.22f) else Color(0xFF070C18L))
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) gemCol else Color.White.copy(alpha = 0.16f),
                                shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)
                            )
                            .clickable {
                                onToggleBandHeroMode(false)
                                onSelectRemixMode(mode)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isActive) gemCol else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                        Text(
                            text = sub,
                            color = Color(0xFFCBD5E1L),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // 4th Sculpted Gem: 🎸 BAND HERO (4-Lane Highway Edition)
                val bhCol = Color(0xFFFACC15L)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                        .background(if (isBandHeroMode) bhCol.copy(alpha = 0.25f) else Color(0xFF160B24L))
                        .border(
                            width = if (isBandHeroMode) 2.dp else 1.dp,
                            color = if (isBandHeroMode) bhCol else Color(0xFFEC4899L).copy(alpha = 0.45f),
                            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)
                        )
                        .clickable { onToggleBandHeroMode(!isBandHeroMode) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = "🎸 BAND HERO",
                        color = if (isBandHeroMode) bhCol else Color(0xFFF472B6L),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Text(
                        text = if (isBandHeroMode) "4-Lane ON" else "4-Lane Mode",
                        color = Color(0xFFCBD5E1L),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. CUSTOM-SCULPTED CHAMFERED NEON LAUNCH BLADE
            val isCurrentLaunchUnlocked = selectedTrackIndex == 1 || unlockedTrackCount >= selectedTrackIndex || isVipUnlocked || prefs.getBoolean("art_unlocked_$selectedTrackIndex", false) || prefs.getBoolean("track_unlocked_$selectedTrackIndex", false)
            val launchAccent = when {
                !isCurrentLaunchUnlocked -> Color(0xFFF59E0BL)
                isBandHeroMode -> Color(0xFFFACC15L)
                else -> modeAccent
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .height(54.dp)
                    .clip(CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                launchAccent.copy(alpha = 0.28f),
                                Color(0xFF091326L),
                                launchAccent.copy(alpha = 0.28f)
                            )
                        )
                    )
                    .border(
                        width = 2.4.dp,
                        brush = Brush.horizontalGradient(
                            listOf(launchAccent, Color(0xFFFACC15L), launchAccent)
                        ),
                        shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp)
                    )
                    .clickable { onStart() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!isCurrentLaunchUnlocked) "🔒 UNLOCK TRACK 0$selectedTrackIndex (${selectedTrack.unlockCost} ♪ OR BEAT STAGE 1)"
                    else if (isBandHeroMode) "🎸 PLAY BAND HERO • TRACK 0$selectedTrackIndex"
                    else "▶ PLAY TRACK 0$selectedTrackIndex • ${selectedTrack.title.uppercase()}",
                    color = if (!isCurrentLaunchUnlocked) Color(0xFFFDE68AL) else Color(0xFFFAF8F5L),
                    fontSize = if (!isCurrentLaunchUnlocked) 12.sp else 14.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (!isCurrentLaunchUnlocked) 0.8.sp else 1.6.sp
                )
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
            .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
            .background(Color(0xFF070C18L))
            .border(1.2.dp, accent.copy(alpha = 0.65f), CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
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
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
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
