package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.84f)),
        contentAlignment = Alignment.Center
    ) {
        ArcadeDialog(
            title = "PAUSED",
            panelBitmap = uiDialogPanel,
            modifier = Modifier.width(320.dp).wrapContentHeight()
        ) {
            ArcadePillButton(
                text = "RESUME",
                pillBitmap = btnPillGreen,
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().height(46.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            ArcadePillButton(
                text = "REPLAY TRACK",
                pillBitmap = btnPillCyan,
                onClick = onReplay,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            ArcadePillButton(
                text = "SETTINGS",
                pillBitmap = btnPillGold,
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            ArcadePillButton(
                text = "MAIN MENU",
                pillBitmap = btnPillPink,
                onClick = onMenu,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            )
        }
    }
}

@Composable
fun GameOverUI(
    pulsePoints: Int,
    score: Int,
    highScore: Int,
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        ArcadeDialog(
            title = "TRACK INTERRUPTED",
            panelBitmap = uiDialogPanel,
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = trackLabel.uppercase(),
                color = Color(0xFF4ADE80L),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Score readout badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF050B16L))
                    .border(1.5.dp, Color(0xFF6366F1L).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINAL SCORE", color = Color(0xFFA5B4FCL), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("%,d".format(score), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    if (highScore > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("BEST SCORE: %,d".format(highScore), color = Color(0xFFA5B4FCL), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("♪ NOTES BALANCE: %,d ♪".format(pulsePoints), color = Color(0xFFFACC15L), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Revive Using 250 ♪ Notes (Primary Note Revive Option)
            ArcadePillButton(
                text = if (canContinue) "♪ USE 250 NOTES TO REVIVE" else "NEED 250 ♪ NOTES TO REVIVE",
                pillBitmap = btnPillCyan,
                onClick = { if (canContinue) onContinue() else onBuyPoints() },
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(46.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Google-Compliant Uniquely Identifiable Rewarded Ad Revive Button
            if (hasAd || BuildConfig.DEBUG) {
                ArcadeAdPillButton(
                    text = "WATCH AD TO REVIVE",
                    onClick = onWatchAd,
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Prominent Restart / Replay Button
            ArcadePillButton(
                text = "REPLAY TRACK",
                pillBitmap = btnPillGreen,
                onClick = onReplayTrack,
                icon = btnIconRestart,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth().height(46.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Back to Album & Shop
            ArcadePillButton(
                text = "ALBUM & SHOP",
                pillBitmap = btnPillGold,
                onClick = onBackToAlbum,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            )
        }
    }
}

@Composable
fun LevelCompleteUI(
    trackIndex: Int,
    score: Int = 0,
    maxCombo: Int = 1,
    notesEarned: Int = 500,
    unlockedVaultCount: Int = 4,
    uiDialogPanel: ImageBitmap,
    btnPillGreen: ImageBitmap,
    btnPillGold: ImageBitmap,
    onNextTrack: () -> Unit,
    onBackToAlbum: () -> Unit,
    onOpenArtistVault: () -> Unit = onBackToAlbum
) {
    val currentTrack = MusicLibrary.getTrack(trackIndex)
    val nextTrack = if (trackIndex < MusicLibrary.TOTAL_TRACKS) MusicLibrary.getTrack(trackIndex + 1) else null

    val infinite = rememberInfiniteTransition(label = "victoryAnim")
    val spinDeg by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "spin"
    )

    val grade = when {
        score >= 6000 || maxCombo >= 18 -> "S+ MASTER"
        score >= 3500 || maxCombo >= 10 -> "S RANK"
        score >= 1800 || maxCombo >= 5 -> "A RANK"
        else -> "TRACK CLEARED"
    }
    val featuredNote = when (trackIndex) {
        1 -> ARTIST_EXCLUSIVES_CATALOG[1]
        2 -> ARTIST_EXCLUSIVES_CATALOG[2]
        else -> ARTIST_EXCLUSIVES_CATALOG.first()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE02050DL)),
        contentAlignment = Alignment.Center
    ) {
        ArcadeDialog(
            title = "★ TRACK MASTERED! ★",
            panelBitmap = uiDialogPanel,
            modifier = Modifier
                .width(346.dp)
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Grade & Track Header Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFACC15L).copy(alpha = 0.20f))
                        .border(1.dp, Color(0xFFFACC15L), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = grade,
                        color = Color(0xFFFACC15L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentTrack.title.uppercase(),
                    color = Color(0xFF4ADE80L),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spinning 24K Gold Vinyl Halo + 100% Revealed Album Cover Centerpiece
            Box(
                modifier = Modifier.size(164.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    val maxR = size.minDimension * 0.48f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFACC15L).copy(alpha = 0.35f), Color(0xFF38BDF8L).copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = maxR * 1.1f
                        ),
                        radius = maxR * 1.1f,
                        center = Offset(cx, cy)
                    )
                    for (ring in 0..2) {
                        val r = maxR * (0.82f + ring * 0.08f)
                        val startA = (spinDeg * (if (ring % 2 == 0) 1f else -1f) + ring * 60f) % 360f
                        drawArc(
                            color = if (ring % 2 == 0) Color(0xFFFACC15L) else Color(0xFF4ADE80L),
                            startAngle = startA,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = Offset(cx - r, cy - r),
                            size = Size(r * 2f, r * 2f),
                            style = Stroke(width = 3f - ring * 0.6f, cap = StrokeCap.Round)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFFFACC15L), RoundedCornerShape(16.dp)),
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

            // 4-Stat Mastery Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VictoryStatChip(
                    label = "FINAL SCORE",
                    value = "%,d".format(score),
                    accent = Color(0xFF38BDF8L),
                    modifier = Modifier.weight(1f)
                )
                VictoryStatChip(
                    label = "BEST COMBO",
                    value = "x$maxCombo",
                    accent = Color(0xFF4ADE80L),
                    modifier = Modifier.weight(1f)
                )
                VictoryStatChip(
                    label = "NOTES EARNED",
                    value = "+%,d ♪".format(notesEarned),
                    accent = Color(0xFFFACC15L),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Newly Unlocked Artist Exclusive Spotlight Card (Direct Fan Reward!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0E1B38L), Color(0xFF060B18L))
                        )
                    )
                    .border(1.5.dp, Color(0xFFFACC15L).copy(alpha = 0.80f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "★ ARTIST VAULT EXCLUSIVE UNLOCKED",
                        color = Color(0xFFFACC15L),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp
                    )
                    Text(
                        text = "$unlockedVaultCount/${ARTIST_EXCLUSIVES_CATALOG.size} VAULT",
                        color = Color(0xFF38BDF8L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = featuredNote.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = featuredNote.artistNoteQuote,
                    color = Color(0xFFE2E8F0L),
                    fontSize = 10.5.sp,
                    lineHeight = 14.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓ ${featuredNote.exclusiveDetail}",
                    color = Color(0xFF4ADE80L),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ArcadePillButton(
                text = "★ OPEN ARTIST VAULT & SPECIALS",
                pillBitmap = btnPillGold,
                onClick = onOpenArtistVault,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                fontSize = 12.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ArcadePillButton(
                text = if (nextTrack != null) "PLAY ${nextTrack.title.uppercase()}" else "REPLAY ALBUM",
                pillBitmap = btnPillGreen,
                onClick = onNextTrack,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                fontSize = 12.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            ArcadePillButton(
                text = "BACK TO MAIN MENU",
                pillBitmap = btnPillGold,
                onClick = onBackToAlbum,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                fontSize = 11.5.sp
            )
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
            .background(Color(0xFF060C1AL))
            .border(1.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(10.dp))
            .padding(vertical = 7.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = accent,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}
