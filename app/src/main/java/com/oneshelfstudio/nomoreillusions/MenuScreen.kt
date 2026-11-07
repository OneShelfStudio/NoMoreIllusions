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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.graphics.Path
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
                        text = "LOCKED",
                        color = Color(0xFFE2E8F0L),
                        fontSize = (sizeDp * 0.12f).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    if (unlockCost > 0) {
                        Text(
                            text = "$unlockCost NOTES",
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
    onMusicDemo: (Int) -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pulse_prefs", android.content.Context.MODE_PRIVATE) }
    val nmiLogo = ImageBitmap.imageResource(id = R.drawable.nmi_logo)
    val track01Art = ImageBitmap.imageResource(id = R.drawable.track_01)
    val track02Art = ImageBitmap.imageResource(id = R.drawable.track_02)
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
        modifier = Modifier.fillMaxSize()
    ) {
        // Starbloom-Grade Atmospheric Floating Motes & Starlight Background
        FloatingMotesBackground(stageAccent = modeAccent)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp, start = 18.dp, end = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Frameless Stage Bar: ♪ Music Notes Balance + Lobby Synth Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Notes Balance Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xF510233D), Color(0xF5060E1A))
                            )
                        )
                        .border(1.2.dp, Color(0xFFFACC15L).copy(alpha = 0.75f), RoundedCornerShape(50))
                        .clickable { onMusicDemo(1) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%,d ♪".format(pulsePoints),
                            color = Color(0xFFFACC15L),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isVipUnlocked) "• VIP ACTIVE" else "• VIP PASS",
                            color = if (isVipUnlocked) Color(0xFF4ADE80L) else Color(0xFFC084FCL),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Stage Synth Switcher Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xF50B1B2F), Color(0xF5040913))
                            )
                        )
                        .border(1.2.dp, modeAccent.copy(alpha = 0.65f), RoundedCornerShape(50))
                        .clickable { onCycleLobbyTrack() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(modeAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STAGE: ${activeLobbyTrack.title.uppercase()} ↻",
                            color = Color(0xFFC7D2FEL),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Centerpiece Content Column with Proportional Weight
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
            // BESPOKE HOLOGRAPHIC VINYL TURNTABLE CAROUSEL SHOWCASE
            val isTrack02AltCover = prefs.getBoolean("track02_alt_cover", false)
            val selectedTrackArt = if (selectedTrackIndex == 1) track01Art else if (isTrack02AltCover) nmiLogo else track02Art
            val isCurrentTrackUnlocked = selectedTrackIndex == 1 || unlockedTrackCount >= selectedTrackIndex || isVipUnlocked || prefs.getBoolean("art_unlocked_$selectedTrackIndex", false) || prefs.getBoolean("track_unlocked_$selectedTrackIndex", false)
            val platterSpinSpeed = selectedRemixMode.audioSpeed
            val currentTrackObj = MusicLibrary.getTrack(selectedTrackIndex)
            val st = trackMasteryStars.getOrElse(selectedTrackIndex - 1) { 0 }.coerceIn(0, 3)
            val stTxt = buildString { for (s in 1..3) append(if (s <= st) "★" else "☆") }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Carousel Prev Track Button
                StudioIconButton(
                    onClick = {
                        val prev = if (selectedTrackIndex <= 1) MusicLibrary.TOTAL_TRACKS else selectedTrackIndex - 1
                        onSelectQuickTrack(prev)
                    },
                    size = 40.dp,
                    accentColor = modeAccent,
                    contentDescription = "Previous Track"
                ) {
                    Text("‹", color = Color(0xFFFAF8F5L), fontSize = 24.sp, fontWeight = FontWeight.Light)
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Grand 165.dp Turntable Platter
                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = center.x
                        val cy = center.y
                        for (rIdx in 0..2) {
                            val waveR = 66f + rIdx * 6.5f
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
                                    style = Stroke(width = 3.2f - rIdx * 0.5f, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Obsidian Vinyl Platter + Grooves
                        drawCircle(color = if (isCurrentTrackUnlocked) Color(0xFF06080FL) else Color(0xFF0C1019L), radius = 84f, center = center)
                        for (g in 1..4) {
                            val gr = 46f + g * 8.5f
                            drawCircle(
                                color = if (isCurrentTrackUnlocked) modeAccent.copy(alpha = 0.16f + (g % 2) * 0.10f) else Color.White.copy(alpha = 0.05f),
                                radius = gr,
                                center = center,
                                style = Stroke(width = 1.1f)
                            )
                        }
                        drawCircle(
                            color = if (isCurrentTrackUnlocked) modeAccent.copy(alpha = glowAlpha * 0.65f) else Color(0xFF475569L).copy(alpha = 0.40f),
                            radius = 85f,
                            center = center,
                            style = Stroke(width = 2.4f)
                        )

                        // Smooth S-Curved Studio Tone-Arm
                        val armPivot = Offset(cx + 84f, cy - 68f)
                        val stylusTip = if (isCurrentTrackUnlocked) Offset(cx + 42f, cy + 16f) else Offset(cx + 72f, cy - 32f)
                        val armPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(armPivot.x, armPivot.y)
                            quadraticTo(cx + 78f, cy - 12f, stylusTip.x, stylusTip.y)
                        }
                        drawPath(
                            path = armPath,
                            color = Color(0xFFFAF8F5L).copy(alpha = if (isCurrentTrackUnlocked) 0.85f else 0.45f),
                            style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                        )
                        drawCircle(color = if (isCurrentTrackUnlocked) modeAccent else Color(0xFF64748BL), radius = 5f, center = armPivot)
                        drawCircle(color = if (isCurrentTrackUnlocked) Color.White else Color(0xFF94A3B8L), radius = 3.6f, center = stylusTip)
                    }

                    // Spinning Vinyl Center Label with Selected Track Artwork
                    val platterLabelSize = 120.dp
                    Box(
                        modifier = Modifier
                            .size(platterLabelSize)
                            .graphicsLayer(
                                rotationZ = (menuTime * 32f * platterSpinSpeed) % 360f
                            )
                            .clip(CircleShape)
                            .background(Color(0xFF060913L))
                            .border(
                                width = 2.dp,
                                color = if (isCurrentTrackUnlocked) modeAccent else Color(0xFF475569L),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = selectedTrackArt,
                            contentDescription = "${currentTrackObj.title} Artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = if (!isCurrentTrackUnlocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.0f) }) else null,
                            alpha = if (isCurrentTrackUnlocked) 1f else 0.45f
                        )
                        // Metallic Spindle Hole Core
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF030508L))
                                .border(1.4.dp, Color(0xFFFACC15L), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Carousel Next Track Button
                StudioIconButton(
                    onClick = {
                        val next = if (selectedTrackIndex >= MusicLibrary.TOTAL_TRACKS) 1 else selectedTrackIndex + 1
                        onSelectQuickTrack(next)
                    },
                    size = 40.dp,
                    accentColor = modeAccent,
                    contentDescription = "Next Track"
                ) {
                    Text("›", color = Color(0xFFFAF8F5L), fontSize = 24.sp, fontWeight = FontWeight.Light)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Track Pagination Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (idx in 1..MusicLibrary.TOTAL_TRACKS) {
                    val isSel = idx == selectedTrackIndex
                    Box(
                        modifier = Modifier
                            .size(width = if (isSel) 20.dp else 8.dp, height = 7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) Color(0xFFFACC15L) else Color.White.copy(alpha = 0.25f))
                            .clickable { onSelectQuickTrack(idx) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Editorial Track Title & Metadata Header
            Text(
                text = "TRACK 0$selectedTrackIndex: ${currentTrackObj.title.uppercase()}",
                color = if (isCurrentTrackUnlocked) Color(0xFFFAF8F5L) else Color(0xFF94A3B8L),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp
            )
            Text(
                text = if (isCurrentTrackUnlocked) "STUDIO MASTER • $stTxt READY" else "LOCKED • BEAT TRACK 01 OR UNLOCK (1,200 NOTES)",
                color = if (isCurrentTrackUnlocked) Color(0xFFFACC15L) else Color(0xFFF87171L),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )

            if (selectedTrackIndex == 2) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isTrack02AltCover) Color(0x33FACC15L) else Color(0x1AFFFFFFL))
                        .border(1.dp, if (isTrack02AltCover) Color(0xFFFACC15L) else Color.White.copy(alpha = 0.25f), CircleShape)
                        .clickable {
                            prefs.edit().putBoolean("track02_alt_cover", !isTrack02AltCover).apply()
                        }
                        .padding(horizontal = 10.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = if (isTrack02AltCover) "✓ ALT LOTUS COVER ACTIVE" else "ALT COVER",
                        color = if (isTrack02AltCover) Color(0xFFFACC15L) else Color(0xFFCBD5E1L),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                text = "PERSONAL RECORD: $highScore PTS",
                color = Color(0xFFEAB308L),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )

            // 2. Breathable 4-Gem Tempo Selector + Clean Playstyle Toggle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val modes = listOf(
                        Triple("CHILL", "0.85x", TrackRemixMode.SLOWED),
                        Triple("NORMAL", "1.0x", TrackRemixMode.ORIGINAL),
                        Triple("FAST", "1.2x", TrackRemixMode.OVERCLOCK),
                        Triple("ENDLESS", "Loop+", TrackRemixMode.ENDLESS)
                    )
                    modes.forEach { (label, sub, mode) ->
                        val isActive = selectedRemixMode == mode
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isActive) Brush.verticalGradient(
                                        listOf(gemCol.copy(alpha = 0.28f), Color(0xFF081224))
                                    ) else Brush.verticalGradient(
                                        listOf(Color(0xFF08101E), Color(0xFF040810))
                                    )
                                )
                                .border(
                                    width = if (isActive) 1.6.dp else 1.dp,
                                    color = if (isActive) gemCol else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectRemixMode(mode) }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isActive) gemCol else Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = sub,
                                color = Color(0xFFCBD5E1L),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Playstyle Mode Segmented Pill (Free-Glide Stage vs Classic 4-Lane Highway)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF070C18L))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
                        .padding(3.dp)
                ) {
                    val freeSelected = !isBandHeroMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(if (freeSelected) Color(0xFF22C55EL).copy(alpha = 0.24f) else Color.Transparent)
                            .clickable { onToggleBandHeroMode(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FREE-GLIDE FLOW",
                            color = if (freeSelected) Color(0xFF4ADE80L) else Color(0xFF94A3B8L),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(if (isBandHeroMode) Color(0xFFFACC15L).copy(alpha = 0.24f) else Color.Transparent)
                            .clickable { onToggleBandHeroMode(true) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "4-LANE HIGHWAY",
                            color = if (isBandHeroMode) Color(0xFFFACC15L) else Color(0xFF94A3B8L),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // 3. Tactile 3D Studio Pill Launch Button (Starbloom Standard - Sleek Proportion)
            val isCurrentLaunchUnlocked = selectedTrackIndex == 1 || unlockedTrackCount >= selectedTrackIndex || isVipUnlocked || prefs.getBoolean("art_unlocked_$selectedTrackIndex", false) || prefs.getBoolean("track_unlocked_$selectedTrackIndex", false)
            StudioPillButton(
                text = if (!isCurrentLaunchUnlocked) "UNLOCK TRACK 0$selectedTrackIndex"
                       else if (isBandHeroMode) "PLAY HIGHWAY"
                       else "PLAY ${selectedTrack.title.uppercase()}",
                subText = if (!isCurrentLaunchUnlocked) "TAP TO UNLOCK (${selectedTrack.unlockCost} ♪)"
                          else "${selectedRemixMode.displayName} • MASTER",
                palette = if (!isCurrentLaunchUnlocked) StudioPillPalette.SOLAR_GOLD else StudioPillPalette.EMERALD_STAGE,
                onClick = onStart,
                height = 46.dp,
                fontSize = 13.sp,
                modifier = Modifier.width(210.dp)
            )

            // Artist Social Links Dock (Official Ethan Correa / No More Illusions)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xB30F172A))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "FOLLOW",
                    color = Color(0xFF94A3B8L),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.9.sp
                )
                MusicLibrary.officialSocialLinks.forEach { link ->
                    val accent = Color(link.accentHex)
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, accent.copy(alpha = 0.65f), CircleShape)
                            .clickable {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                } catch (_: Exception) {}
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        BrandVectorGlyph(
                            brand = link.label,
                            tint = accent,
                            size = 13.dp
                        )
                    }
                }
            }
            } // end inner SpaceEvenly Column

            Spacer(modifier = Modifier.height(8.dp))

            StudioBottomNavDock(
                currentDestination = "HOME",
                onNavigateHome = { /* Already Home */ },
                onNavigateAlbum = { onMusicDemo(1) },
                onNavigateVault = { onMusicDemo(0) },
                onNavigateSkins = { onMusicDemo(2) },
                onNavigateSettings = { onSettings() }
            )
        }
    }
}

@Composable
fun StudioBottomNavDock(
    currentDestination: String,
    onNavigateHome: () -> Unit,
    onNavigateAlbum: () -> Unit = {},
    onNavigateVault: () -> Unit,
    onNavigateSkins: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    StudioNavDock(
        currentDestination = currentDestination,
        onNavigateHome = onNavigateHome,
        onNavigateAlbum = onNavigateAlbum,
        onNavigateVault = onNavigateVault,
        onNavigateSkins = onNavigateSkins,
        onNavigateSettings = onNavigateSettings,
        modifier = modifier
    )
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
    val pulseScale by animateFloatAsState(
        targetValue = if (value == 0) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CountdownScale"
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val label = if (value > 0) value.toString() else "DROP!"
        val color = if (value > 0) Color(0xFF38BDF8L) else Color(0xFFFACC15L)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
        ) {
            Canvas(modifier = Modifier.size(210.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.28f), Color.Transparent),
                        center = center,
                        radius = size.minDimension * 0.5f
                    ),
                    radius = size.minDimension * 0.5f,
                    center = center
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xCC060D1E))
                        .border(1.dp, color.copy(alpha = 0.65f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (value > 0) "STAGE STANDBY" else "NEEDLE DROPPING",
                        color = color,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = if (value > 0) 96.sp else 74.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = color,
                            offset = Offset.Zero,
                            blurRadius = 28f
                        )
                    )
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun HowToPlayUI(
    btnPillCyan: ImageBitmap,
    onNavigateHome: () -> Unit = {},
    onNavigateAlbum: () -> Unit = {},
    onNavigateVault: () -> Unit = {},
    onNavigateSkins: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onBack: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "guideEmblems")
    val animPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "guidePhase"
    )
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        FloatingMotesBackground(stageAccent = Color(0xFF38BDF8))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            StudioScreenHeader(
                title = "STUDIO GUIDE",
                subtitle = "RHYTHM, RELICS & VIDEO MONTAGES",
                accentColor = Color(0xFFFACC15),
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ExplainerShowcaseCard(
                    stepIndex = 0,
                    appeared = appeared,
                    emblemKind = 0,
                    animPhase = animPhase,
                    badge = "CONTROLS",
                    title = "FLUID GLIDE & DUAL TOUCH",
                    body = "Slide anywhere to steer smoothly across the stage—or touch with a second finger to weave a curved Dual-Hero plasma ribbon.",
                    accentColor = Color(0xFF4ADE80)
                )
                ExplainerShowcaseCard(
                    stepIndex = 1,
                    appeared = appeared,
                    emblemKind = 1,
                    animPhase = animPhase,
                    badge = "RHYTHM FLOW",
                    title = "MULTI-DIRECTIONAL BEAT ORBS",
                    body = "Luminous orbs glide in from every angle and pulse to the drum groove. Catch them on-beat to build combos and unveil album art.",
                    accentColor = Color(0xFF38BDF8)
                )
                ExplainerShowcaseCard(
                    stepIndex = 2,
                    appeared = appeared,
                    emblemKind = 2,
                    animPhase = animPhase,
                    badge = "0:15 & 1:18",
                    title = "24K GOLD SPECTACLE RELICS",
                    body = "Catch upper-center 24K Milestone Orbs (or tap the right HUD capsule at 100%) to unleash golden lotus ripples & vaporize hazards.",
                    accentColor = Color(0xFFFACC15)
                )
                ExplainerShowcaseCard(
                    stepIndex = 3,
                    appeared = appeared,
                    emblemKind = 3,
                    animPhase = animPhase,
                    badge = "5/5 VHS REEL",
                    title = "MUSIC VIDEO MONTAGES",
                    body = "Collect 5 chromatic VHS Relics in each song window to arm your left VHS Meter—launching two slow-mo music video montages per song!",
                    accentColor = Color(0xFFE879F9)
                )

                // Classic Highway Mode Bonus Strip
                StudioGlassCard(
                    accentColor = Color(0xFF7DD3FC),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.16f))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.65f), CircleShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BONUS MODE",
                                color = Color(0xFF7DD3FC),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Toggle CLASSIC HIGHWAY on the Home deck anytime for vertical 4-lane rhythm play.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                StudioPillButton(
                    text = "RETURN TO STAGE DECK ▶",
                    palette = StudioPillPalette.EMERALD_STAGE,
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    fontSize = 12.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            StudioBottomNavDock(
                currentDestination = "GUIDE",
                onNavigateHome = onBack,
                onNavigateAlbum = onNavigateAlbum,
                onNavigateVault = onNavigateVault,
                onNavigateSkins = onNavigateSkins,
                onNavigateSettings = onNavigateSettings
            )
        }
    }
}

@Composable
private fun ExplainerShowcaseCard(
    stepIndex: Int,
    appeared: Boolean,
    emblemKind: Int,
    animPhase: Float,
    badge: String,
    title: String,
    body: String,
    accentColor: Color
) {
    val cardScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = (380f - stepIndex * 45f).coerceAtLeast(180f)
        ),
        label = "ExplainerCardScale_$stepIndex"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 220 + stepIndex * 70),
        label = "ExplainerCardAlpha_$stepIndex"
    )

    StudioGlassCard(
        accentColor = accentColor,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF040915))
                    .border(1.2.dp, accentColor.copy(alpha = 0.52f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                LiveGuideEmblemCanvas(
                    emblemKind = emblemKind,
                    animPhase = animPhase + stepIndex * 0.6f,
                    accentColor = accentColor,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.16f))
                            .border(1.dp, accentColor.copy(alpha = 0.65f), CircleShape)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = accentColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.5.sp,
                    lineHeight = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun SettingsUI(
    isMusic: Boolean,
    isSound: Boolean,
    isHaptic: Boolean,
    isBrollVideo: Boolean = true,
    onBrollVideoToggle: (Boolean) -> Unit = {},
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
    onNavigateHome: () -> Unit = {},
    onNavigateAlbum: () -> Unit = {},
    onNavigateVault: () -> Unit = {},
    onNavigateSkins: () -> Unit = {},
    onBack: () -> Unit
) {
    val haptics = rememberStudioHaptics()
    var vipCodeInput by remember { mutableStateOf("") }
    var vipError by remember { mutableStateOf(false) }
    var vipSuccess by remember { mutableStateOf(isVipUnlocked) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        FloatingMotesBackground(stageAccent = Color(0xFFFACC15))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            StudioScreenHeader(
                title = "SETTINGS",
                subtitle = "AUDIO, PACE & BACKSTAGE PASS",
                accentColor = Color(0xFFFACC15),
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Audio & Visual Options
                StudioGlassCard(
                    accentColor = Color(0xFF4ADE80),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "STUDIO MIX & FEEDBACK",
                            color = Color(0xFF4ADE80),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp
                        )
                        ToggleItem(
                            label = "MASTER MUSIC",
                            checked = isMusic,
                            onToggle = onMusicToggle,
                            subtitle = "Album tracks & ambient studio synth"
                        )
                        ToggleItem(
                            label = "HARMONIC SOUND FX",
                            checked = isSound,
                            onToggle = onSoundToggle,
                            subtitle = "24K chimes, relic blooms & stingers"
                        )
                        ToggleItem(
                            label = "TACTILE HAPTICS",
                            checked = isHaptic,
                            onToggle = onHapticToggle,
                            subtitle = "Physical micro-pulse on orbs & buttons"
                        )
                        ToggleItem(
                            label = "MUSIC VIDEO MONTAGES",
                            checked = isBrollVideo,
                            onToggle = onBrollVideoToggle,
                            subtitle = "Slow-mo B-roll clips on 5/5 VHS meter"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lobby Ambient Synth & Game Difficulty
                StudioGlassCard(
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "LOBBY AMBIENT SYNTH",
                            color = Color(0xFF7DD3FC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LobbyTrack.entries.forEach { lt ->
                                val sel = lt == activeLobbyTrack
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (sel) {
                                                Brush.verticalGradient(listOf(Color(0xFF0E291A), Color(0xFF06131F)))
                                            } else {
                                                Brush.verticalGradient(listOf(Color(0xFF0B1426), Color(0xFF050912)))
                                            }
                                        )
                                        .border(
                                            width = if (sel) 1.5.dp else 1.dp,
                                            color = if (sel) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            haptics.tick()
                                            onLobbyTrackChange(lt)
                                        }
                                        .padding(vertical = 9.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = lt.title.uppercase(),
                                            color = if (sel) Color(0xFF4ADE80) else Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "SYNTH BED",
                                            color = if (sel) Color(0xFF86EFAC) else Color(0xFF94A3B8),
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "STAGE DIFFICULTY",
                            color = Color(0xFF7DD3FC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DifficultyOption(
                                difficulty = GameDifficulty.EASY,
                                isSelected = difficulty == GameDifficulty.EASY,
                                modifier = Modifier.weight(1f)
                            ) { onDifficultyChange(it) }
                            DifficultyOption(
                                difficulty = GameDifficulty.REGULAR,
                                isSelected = difficulty == GameDifficulty.REGULAR,
                                modifier = Modifier.weight(1f)
                            ) { onDifficultyChange(it) }
                            DifficultyOption(
                                difficulty = GameDifficulty.HARD,
                                isSelected = difficulty == GameDifficulty.HARD,
                                modifier = Modifier.weight(1f)
                            ) { onDifficultyChange(it) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Artist VIP Backstage Passcode
                StudioGlassCard(
                    accentColor = Color(0xFFFACC15),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (vipSuccess || isVipUnlocked) "★ ARTIST VIP PASSCODE ACTIVE (ALL UNLOCKED)" else "ARTIST VIP BACKSTAGE PASSCODE",
                            color = if (vipSuccess || isVipUnlocked) Color(0xFF4ADE80L) else Color(0xFFFACC15L),
                            fontSize = 10.5.sp,
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
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF04070FL))
                                        .border(
                                            1.dp,
                                            if (vipError) Color(0xFFF43F5EL) else Color(0xFFFACC15L).copy(alpha = 0.45f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 12.dp),
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
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.2.sp
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                StudioPillButton(
                                    text = "UNLOCK",
                                    palette = StudioPillPalette.SOLAR_GOLD,
                                    onClick = {
                                        val clean = vipCodeInput.trim().uppercase()
                                        if (clean == "ETHAN" || clean == "CONFIDE" || clean == "ILLUSIONS2026" || clean == "NMI2026") {
                                            vipSuccess = true
                                            vipError = false
                                            onActivateVipPasscode()
                                        } else {
                                            vipError = true
                                        }
                                    },
                                    modifier = Modifier
                                        .width(104.dp)
                                        .height(40.dp),
                                    fontSize = 11.sp
                                )
                            }
                            if (vipError) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Invalid backstage passcode. Check spelling and try again.",
                                    color = Color(0xFFFB7185),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                StudioPillButton(
                    text = "SAVE & RETURN ▶",
                    palette = StudioPillPalette.SOLAR_GOLD,
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    fontSize = 12.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            StudioBottomNavDock(
                currentDestination = "SETTINGS",
                onNavigateHome = onNavigateHome,
                onNavigateAlbum = onNavigateAlbum,
                onNavigateVault = onNavigateVault,
                onNavigateSkins = onNavigateSkins,
                onNavigateSettings = { /* Already in settings */ }
            )
        }
    }
}
