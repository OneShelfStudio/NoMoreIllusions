package com.oneshelfstudio.nomoreillusions

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("UNUSED_PARAMETER")
@Composable
fun PausedUI(
    uiDialogPanel: ImageBitmap,
    btnPillGreen: ImageBitmap,
    btnPillCyan: ImageBitmap,
    btnPillGold: ImageBitmap,
    btnPillPink: ImageBitmap,
    onResume: () -> Unit,
    onReplay: () -> Unit,
    onSettings: () -> Unit,
    onMenu: () -> Unit
) {
    StudioDialog(
        title = "STAGE PAUSED",
        onDismiss = onResume,
        accentColor = Color(0xFF38BDF8),
        modifier = Modifier
            .widthIn(max = 356.dp)
            .fillMaxWidth(0.90f)
            .wrapContentHeight()
    ) {
        Text(
            text = "AUDIO DECK ON STANDBY",
            color = Color(0xFF7DD3FC),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        StudioPillButton(
            text = "RESUME TRACK ▶",
            palette = StudioPillPalette.EMERALD_STAGE,
            onClick = onResume,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            fontSize = 13.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        StudioPillButton(
            text = "REPLAY FROM START",
            palette = StudioPillPalette.CYAN_SYNTH,
            onClick = onReplay,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            fontSize = 12.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        StudioPillButton(
            text = "STUDIO SETTINGS",
            palette = StudioPillPalette.SOLAR_GOLD,
            onClick = onSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            fontSize = 12.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        StudioPillButton(
            text = "MAIN MENU",
            palette = StudioPillPalette.SLATE_OBSIDIAN,
            onClick = onMenu,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            fontSize = 12.sp
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun GameOverUI(
    pulsePoints: Int,
    score: Int,
    highScore: Int,
    orbsCollected: Int = 0,
    totalTrackOrbs: Int = 1,
    trackLabel: String,
    canContinue: Boolean,
    hasAd: Boolean,
    btnPillGreen: ImageBitmap,
    btnPillCyan: ImageBitmap,
    btnPillGold: ImageBitmap,
    btnIconRestart: ImageBitmap,
    uiDialogPanel: ImageBitmap,
    onContinue: () -> Unit,
    onWatchAd: () -> Unit,
    onReplayTrack: () -> Unit,
    onBackToAlbum: () -> Unit,
    onBuyPoints: () -> Unit
) {
    val haptics = rememberStudioHaptics()
    val context = LocalContext.current
    val orbPct = ((orbsCollected * 100f) / totalTrackOrbs.coerceAtLeast(1)).toInt().coerceIn(0, 100)

    StudioDialog(
        title = "TRACK INTERRUPTED",
        onDismiss = {},
        accentColor = Color(0xFFFB7185),
        modifier = Modifier
            .widthIn(max = 368.dp)
            .fillMaxWidth(0.92f)
            .wrapContentHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF4ADE80).copy(alpha = 0.14f))
                    .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.65f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = trackLabel.uppercase(),
                    color = Color(0xFF4ADE80),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.9.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Starbloom Telemetry Glass Card
            StudioGlassCard(
                accentColor = Color(0xFF38BDF8),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "RUN SCORE",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "%,d".format(score),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (highScore > 0) {
                        Text(
                            text = "PERSONAL BEST • %,d".format(highScore),
                            color = Color(0xFF7DD3FC),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VictoryStatChip(
                            label = "ORB HARMONY",
                            value = "$orbPct% ($orbsCollected/$totalTrackOrbs)",
                            accent = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        VictoryStatChip(
                            label = "NOTES BANK",
                            value = "%,d ♪".format(pulsePoints),
                            accent = Color(0xFFFACC15),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Revive Using 250 Notes
            StudioPillButton(
                text = if (canContinue) "REVIVE FOR 250 ♪ NOTES" else "NEED 250 ♪ NOTES TO REVIVE",
                palette = StudioPillPalette.CYAN_SYNTH,
                onClick = { if (canContinue) onContinue() else onBuyPoints() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(9.dp))

            // 2. Google-Compliant Uniquely Identifiable Rewarded Ad Revive Button
            if (hasAd || BuildConfig.DEBUG) {
                ArcadeAdPillButton(
                    text = "WATCH AD TO REVIVE",
                    onClick = {
                        haptics.confirm()
                        onWatchAd()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )
                Spacer(modifier = Modifier.height(9.dp))
            }

            // 3. Prominent Replay Button
            StudioPillButton(
                text = "REPLAY TRACK ▶",
                palette = StudioPillPalette.EMERALD_STAGE,
                onClick = onReplayTrack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                fontSize = 13.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Social Sharing Card
            StudioGlassCard(
                accentColor = Color(0xFFFACC15),
                shape = RoundedCornerShape(14.dp),
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Just scored %,d in No More Illusions! Tagging @EthanCorrea #NoMoreIllusions Check out the gamified album: https://oneshelfstudio.github.io/NoMoreIllusions/".format(score)
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Run"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "SHARE YOUR RUN • TAG @ETHANCORREA",
                        color = Color(0xFFFACC15),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap to post your score with #NoMoreIllusions on TikTok / IG",
                        color = Color(0xFFCBD5E1),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            // 4. Back to Album & Shop
            StudioPillButton(
                text = "ALBUM & STUDIO SHOP",
                palette = StudioPillPalette.SOLAR_GOLD,
                onClick = onBackToAlbum,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                fontSize = 12.sp
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun LevelCompleteUI(
    trackIndex: Int,
    score: Int = 0,
    maxCombo: Int = 1,
    notesEarned: Int = 500,
    orbsCollected: Int = 0,
    totalTrackOrbs: Int = 1,
    collectionBonusNotes: Int = 0,
    unlockedVaultCount: Int = 4,
    uiDialogPanel: ImageBitmap,
    btnPillGreen: ImageBitmap,
    btnPillGold: ImageBitmap,
    onNextTrack: () -> Unit,
    onBackToAlbum: () -> Unit,
    onOpenArtistVault: () -> Unit = onBackToAlbum,
    onPlayBandHeroStyle: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentTrack = MusicLibrary.getTrack(trackIndex)
    val nextTrack = if (trackIndex < MusicLibrary.TOTAL_TRACKS) MusicLibrary.getTrack(trackIndex + 1) else null

    val victoryZoom = remember { Animatable(0.35f) }
    LaunchedEffect(trackIndex) {
        victoryZoom.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val infinite = rememberInfiniteTransition(label = "victoryAnim")
    val spinDeg by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4600, easing = LinearEasing)),
        label = "spin"
    )

    var showExclusivePopup by remember { mutableStateOf(false) }
    val haptics = rememberStudioHaptics()

    val featuredNote = when (trackIndex) {
        1 -> ARTIST_EXCLUSIVES_CATALOG[1]
        2 -> ARTIST_EXCLUSIVES_CATALOG[2]
        else -> ARTIST_EXCLUSIVES_CATALOG.first()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StudioDialog(
            title = "STAGE MASTERED",
            onDismiss = {},
            accentColor = Color(0xFFFACC15),
            modifier = Modifier
                .widthIn(max = 376.dp)
                .fillMaxWidth(0.93f)
                .wrapContentHeight()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80).copy(alpha = 0.16f))
                        .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.70f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TRACK 0$trackIndex UNVEILED • \"${currentTrack.title.uppercase()}\"",
                        color = Color(0xFF4ADE80),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Compact Celebratory Zoom on Full Unveiled Album Artwork
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .graphicsLayer {
                            scaleX = victoryZoom.value
                            scaleY = victoryZoom.value
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width * 0.5f
                        val cy = size.height * 0.5f
                        val maxR = size.minDimension * 0.49f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFACC15).copy(alpha = 0.40f),
                                    Color(0xFF38BDF8).copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = maxR * 1.14f
                            ),
                            radius = maxR * 1.14f,
                            center = Offset(cx, cy)
                        )
                        for (ring in 0..2) {
                            val r = maxR * (0.83f + ring * 0.07f)
                            val startA = (spinDeg * (if (ring % 2 == 0) 1f else -1f) + ring * 60f) % 360f
                            drawArc(
                                color = if (ring % 2 == 0) Color(0xFFFACC15) else Color(0xFF4ADE80),
                                startAngle = startA,
                                sweepAngle = 115f,
                                useCenter = false,
                                topLeft = Offset(cx - r, cy - r),
                                size = Size(r * 2f, r * 2f),
                                style = Stroke(width = 3.0f - ring * 0.5f, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(106.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.0.dp, Color(0xFFFACC15), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        TrackRevealOverlay(
                            trackIndex = trackIndex,
                            progress = 1.0f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Harmony Mastery Glass Card
                val safeTotalOrbs = totalTrackOrbs.coerceAtLeast(1)
                val orbPct = ((orbsCollected * 100f) / safeTotalOrbs).toInt().coerceIn(0, 100)
                val tierColor = when {
                    orbPct >= 100 -> Color(0xFFFACC15)
                    orbPct >= 90 -> Color(0xFF4ADE80)
                    orbPct >= 75 -> Color(0xFF38BDF8)
                    else -> Color(0xFFA5B4FC)
                }
                val tierLabel = when {
                    orbPct >= 100 -> "100% PERFECT HARMONY • +1,000 BONUS ♪"
                    orbPct >= 90 -> "GOLD ORB SYNC • +$collectionBonusNotes BONUS ♪"
                    orbPct >= 75 -> "SILVER ORB SYNC • +$collectionBonusNotes BONUS ♪"
                    else -> "HIT 75% / 90% / 100% ORBS FOR BONUS ♪"
                }

                StudioGlassCard(
                    accentColor = tierColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ORB HARMONY • $orbPct%",
                                color = tierColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "$orbsCollected / $safeTotalOrbs",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((orbPct / 100f).coerceIn(0.05f, 1f))
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF38BDF8), Color(0xFF4ADE80), Color(0xFFFACC15))
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = tierLabel,
                            color = tierColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VictoryStatChip(
                                label = "SCORE",
                                value = "%,d".format(score),
                                accent = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            VictoryStatChip(
                                label = "STREAK",
                                value = "x$maxCombo",
                                accent = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            VictoryStatChip(
                                label = "EARNED",
                                value = "+%,d ♪".format(notesEarned + collectionBonusNotes),
                                accent = Color(0xFFFACC15),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Streamlined 2-Pill Bonus Actions Row (Relic + Share)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StudioGlassCard(
                        accentColor = Color(0xFFFACC15),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            haptics.tick()
                            showExclusivePopup = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "✦ VAULT RELIC",
                                color = Color(0xFFFACC15),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Inspect ($unlockedVaultCount/${ARTIST_EXCLUSIVES_CATALOG.size})",
                                color = Color(0xFFE2E8F0),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                    StudioGlassCard(
                        accentColor = Color(0xFFE879F9),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            haptics.tick()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Just mastered Track 0%d in No More Illusions with %,d pts! Tagging @EthanCorrea #NoMoreIllusions Stream the gamified album: https://oneshelfstudio.github.io/NoMoreIllusions/".format(trackIndex, score)
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Run"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "↗ SHARE WIN",
                                color = Color(0xFFE879F9),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Tag @EthanCorrea",
                                color = Color(0xFFE2E8F0),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                StudioPillButton(
                    text = if (nextTrack != null) "PLAY TRACK 0${trackIndex + 1} ▶" else "REPLAY TRACK ▶",
                    palette = StudioPillPalette.EMERALD_STAGE,
                    onClick = onNextTrack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StudioPillButton(
                        text = "ARTIST VAULT",
                        palette = StudioPillPalette.SOLAR_GOLD,
                        onClick = onOpenArtistVault,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(44.dp),
                        fontSize = 10.5.sp
                    )
                    StudioPillButton(
                        text = "MAIN MENU",
                        palette = StudioPillPalette.SLATE_OBSIDIAN,
                        onClick = onBackToAlbum,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Tap-to-Open Vault Exclusive Pop-Up Modal
        if (showExclusivePopup) {
            StudioDialog(
                title = "VAULT ARCHIVE UNLOCKED",
                onDismiss = { showExclusivePopup = false },
                accentColor = Color(0xFFFACC15)
            ) {
                Text(
                    text = featuredNote.title,
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                StudioGlassCard(
                    accentColor = Color(0xFF38BDF8),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = featuredNote.artistNoteQuote,
                            color = Color(0xFFF8FAFC),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ ${featuredNote.exclusiveDetail}",
                            color = Color(0xFF4ADE80),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StudioPillButton(
                        text = "CLOSE",
                        palette = StudioPillPalette.SLATE_OBSIDIAN,
                        onClick = { showExclusivePopup = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        fontSize = 11.sp
                    )
                    StudioPillButton(
                        text = "OPEN VAULT ▶",
                        palette = StudioPillPalette.SOLAR_GOLD,
                        onClick = {
                            showExclusivePopup = false
                            onOpenArtistVault()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VictoryStatChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF060C1A))
            .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(10.dp))
            .padding(vertical = 7.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = accent,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
