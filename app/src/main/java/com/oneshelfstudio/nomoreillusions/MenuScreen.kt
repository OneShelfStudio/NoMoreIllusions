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
            .navigationBarsPadding()
            .padding(top = 10.dp, bottom = 6.dp, start = 18.dp, end = 18.dp)
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
                    .clickable { onMusicDemo(1) }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$pulsePoints NOTES",
                    color = Color(0xFFFACC15L),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isVipUnlocked) "• VIP ACTIVE" else "• VIP PASS",
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
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 34.dp, bottom = 0.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172AL).copy(alpha = 0.85f))
                        .border(1.6.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                        .clickable {
                            val prev = if (selectedTrackIndex <= 1) MusicLibrary.TOTAL_TRACKS else selectedTrackIndex - 1
                            onSelectQuickTrack(prev)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", color = Color(0xFFFAF8F5L), fontSize = 26.sp, fontWeight = FontWeight.Light)
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172AL).copy(alpha = 0.85f))
                        .border(1.6.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                        .clickable {
                            val next = if (selectedTrackIndex >= MusicLibrary.TOTAL_TRACKS) 1 else selectedTrackIndex + 1
                            onSelectQuickTrack(next)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("›", color = Color(0xFFFAF8F5L), fontSize = 26.sp, fontWeight = FontWeight.Light)
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
                            .size(width = if (isSel) 22.dp else 8.dp, height = 8.dp)
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isActive) gemCol.copy(alpha = 0.20f) else Color(0xFF070C18L))
                                .border(
                                    width = if (isActive) 1.8.dp else 1.dp,
                                    color = if (isActive) gemCol else Color.White.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectRemixMode(mode) }
                                .padding(vertical = 9.dp, horizontal = 6.dp)
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

            // 3. GRAND PULSING NEON JEWEL LAUNCH BUTTON
            val isCurrentLaunchUnlocked = selectedTrackIndex == 1 || unlockedTrackCount >= selectedTrackIndex || isVipUnlocked || prefs.getBoolean("art_unlocked_$selectedTrackIndex", false) || prefs.getBoolean("track_unlocked_$selectedTrackIndex", false)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF10B981L).copy(alpha = 0.90f),
                                Color(0xFF0284C7L).copy(alpha = 0.90f),
                                Color(0xFFFACC15L).copy(alpha = 0.80f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable { onStart() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (!isCurrentLaunchUnlocked) "UNLOCK TRACK 0$selectedTrackIndex (${selectedTrack.unlockCost} NOTES)"
                        else if (isBandHeroMode) "PLAY 4-LANE HIGHWAY"
                        else "PLAY ${selectedTrack.title.uppercase()}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = if (!isCurrentLaunchUnlocked) "TAP TO UNLOCK WITH COLLECTED NOTES"
                        else "${selectedRemixMode.displayName} • STUDIO MASTER",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    )
                }
            }

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
    val navItems = listOf(
        Triple("HOME", "HOME", onNavigateHome),
        Triple("ALBUM", "ALBUM", onNavigateAlbum),
        Triple("VAULT", "VAULT", onNavigateVault),
        Triple("SKINS", "SKINS", onNavigateSkins),
        Triple("SETTINGS", "SETTINGS", onNavigateSettings)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xF5080D1CL))
            .border(1.2.dp, Color(0x55FACC15L), RoundedCornerShape(22.dp))
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { (key, label, action) ->
            val isSel = currentDestination == key
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { action() }
                    .padding(vertical = 3.dp)
            ) {
                BottomNavGlyph(type = key, isSelected = isSel)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = label,
                    color = if (isSel) Color(0xFFFACC15L) else Color(0xFF94A3B8L),
                    fontSize = 8.5.sp,
                    fontWeight = if (isSel) FontWeight.Black else FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun BottomNavGlyph(type: String, isSelected: Boolean) {
    val tint = if (isSelected) Color(0xFFFACC15L) else Color(0xFF94A3B8L)
    Canvas(modifier = Modifier.size(19.dp)) {
        val w = size.width
        val h = size.height
        when (type) {
            "HOME" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.45f)
                    lineTo(w * 0.72f, h * 0.45f)
                    lineTo(w * 0.72f, h * 0.85f)
                    lineTo(w * 0.28f, h * 0.85f)
                    lineTo(w * 0.28f, h * 0.45f)
                    lineTo(w * 0.15f, h * 0.45f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
            }
            "SKINS" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.10f)
                    lineTo(w * 0.60f, h * 0.40f)
                    lineTo(w * 0.90f, h * 0.50f)
                    lineTo(w * 0.60f, h * 0.60f)
                    lineTo(w * 0.5f, h * 0.90f)
                    lineTo(w * 0.40f, h * 0.60f)
                    lineTo(w * 0.10f, h * 0.50f)
                    lineTo(w * 0.40f, h * 0.40f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
            }
            "ALBUM" -> {
                drawCircle(color = tint, radius = w * 0.42f, style = Stroke(width = 1.8.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.16f, style = Stroke(width = 1.5.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.05f)
            }
            "VAULT" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    lineTo(w * 0.88f, h * 0.50f)
                    lineTo(w * 0.5f, h * 0.88f)
                    lineTo(w * 0.12f, h * 0.50f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.12f)
            }
            "SETTINGS" -> {
                drawCircle(color = tint, radius = w * 0.40f, style = Stroke(width = 1.8.dp.toPx()))
                drawCircle(color = tint, radius = w * 0.16f, style = Stroke(width = 1.4.dp.toPx()))
            }
            "EXCLUSIVES" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.10f)
                    lineTo(w * 0.62f, h * 0.35f)
                    lineTo(w * 0.90f, h * 0.38f)
                    lineTo(w * 0.70f, h * 0.58f)
                    lineTo(w * 0.76f, h * 0.85f)
                    lineTo(w * 0.5f, h * 0.72f)
                    lineTo(w * 0.24f, h * 0.85f)
                    lineTo(w * 0.30f, h * 0.58f)
                    lineTo(w * 0.10f, h * 0.38f)
                    lineTo(w * 0.38f, h * 0.35f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
            }
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
                text = "HOW TO PLAY • MUSIC GUIDE",
                color = Color(0xFFEAB308L),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fluid Glide Rhythm • Unlock Tracks • Classic Mode (Guitar Hero Style)",
                color = Color(0xFFCBD5E1L),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            InstructionItem(
                "1. GATHER NOTES",
                "Glide into notes along the steady rhythm of the music. Catch them on beat to build your combo and unlock new songs!"
            )
            InstructionItem(
                "2. AVOID ENEMIES",
                "Dodge red enemy hazards to protect your streak and multiplier."
            )
            InstructionItem(
                "3. HIT SPECIAL PEGS",
                "Hit electric harmonic pegs and bomb pegs to clear enemies and transform the stage with spectacular visual surges!"
            )
            InstructionItem(
                "4. CLASSIC MODE (Guitar Hero Style)",
                "Toggle Classic Mode on the main menu anytime to play vertical highway rhythm lanes!"
            )
            Spacer(modifier = Modifier.height(20.dp))
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
                    ToggleItem("MUSIC VIDEO B-ROLL", isBrollVideo, onBrollVideoToggle)
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
                                text = "AMBIENT",
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
                        text = if (vipSuccess || isVipUnlocked) "ARTIST VIP PASSCODE ACTIVE (ALL UNLOCKED!)" else "ARTIST VIP PASSCODE (UNLOCK ALL OPTIONS)",
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

            Spacer(modifier = Modifier.height(12.dp))
            StudioBottomNavDock(
                currentDestination = "SETTINGS",
                onNavigateHome = onBack,
                onNavigateSkins = onBack,
                onNavigateVault = onBack,
                onNavigateSettings = { /* Already in settings */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ArcadePillButton(
                text = "SAVE & BACK",
                pillBitmap = btnPillGold,
                onClick = onBack,
                modifier = Modifier.width(240.dp).height(48.dp)
            )
        }
    }
}
