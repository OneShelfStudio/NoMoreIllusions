package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
    onOpenArtistVault: () -> Unit = onBackToAlbum,
    onPlayBandHeroStyle: () -> Unit = {}
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
    val starPulse by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(720, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "starPulse"
    )

    var showExclusivePopup by remember { mutableStateOf(false) }

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
            title = "★ CONGRATS! YOU WON! ★",
            panelBitmap = uiDialogPanel,
            modifier = Modifier
                .width(348.dp)
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Clear, Immediate Prize Banner
            Text(
                text = "UNLOCKED SONG ADDED TO YOUR ALBUM!",
                color = Color(0xFF4ADE80L),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "TRACK 0$trackIndex • \"${currentTrack.title.uppercase()}\"",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // HUGE 216dp Centerpiece: Spinning 24K Gold Vinyl Halo + Full Unveiled Album Cover!
            Box(
                modifier = Modifier.size(216.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    val maxR = size.minDimension * 0.49f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFACC15L).copy(alpha = 0.42f),
                                Color(0xFF38BDF8L).copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = maxR * 1.12f
                        ),
                        radius = maxR * 1.12f,
                        center = Offset(cx, cy)
                    )
                    for (ring in 0..2) {
                        val r = maxR * (0.84f + ring * 0.07f)
                        val startA = (spinDeg * (if (ring % 2 == 0) 1f else -1f) + ring * 60f) % 360f
                        drawArc(
                            color = if (ring % 2 == 0) Color(0xFFFACC15L) else Color(0xFF4ADE80L),
                            startAngle = startA,
                            sweepAngle = 115f,
                            useCenter = false,
                            topLeft = Offset(cx - r, cy - r),
                            size = Size(r * 2f, r * 2f),
                            style = Stroke(width = 3.5f - ring * 0.6f, cap = StrokeCap.Round)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.5.dp, Color(0xFFFACC15L), RoundedCornerShape(20.dp)),
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

            // Compact 1-Line Run Summary Pill (Clean, Zero Clutter)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF060C1AL))
                    .border(1.dp, Color(0xFF38BDF8L).copy(alpha = 0.55f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SCORE: %,d  •  STREAK: x%d  •  +%,d ♪".format(score, maxCombo, notesEarned),
                    color = Color(0xFFE2E8F0L),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Spinning Star / Music Note Curiosity Emblem (Tap to Pop Open Vault Exclusive!)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E1505L), Color(0xFF0D182EL), Color(0xFF1E1505L))
                        )
                    )
                    .border(1.8.dp, Color(0xFFFACC15L), RoundedCornerShape(16.dp))
                    .clickable { showExclusivePopup = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Glowing Spinning Star & Music Note Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .graphicsLayer(
                            scaleX = starPulse,
                            scaleY = starPulse
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFACC15L), Color(0xFFD97706L), Color(0xFF1E1B4BL))
                            )
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★♪",
                        color = Color(0xFF020617L),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.graphicsLayer(rotationZ = (spinDeg * 0.35f) % 360f)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✨ NEW VAULT SECRET UNLOCKED!",
                        color = Color(0xFFFACC15L),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap the glowing star to see what you won ($unlockedVaultCount/${ARTIST_EXCLUSIVES_CATALOG.size})",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Alternate Mode Button: "PLAY MY MUSIC: BAND HERO STYLE" (4-Lane Highway Edition)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2E1065L), Color(0xFF0F172AL), Color(0xFF4C0519L))
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFEC4899L), Color(0xFFFACC15L), Color(0xFF38BDF8L))
                        ),
                        shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
                    )
                    .clickable { onPlayBandHeroStyle() }
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎸 PLAY MY MUSIC: BAND HERO STYLE",
                        color = Color(0xFFFACC15L),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "4-LANE RHYTHM HIGHWAY EDITION • TAP TO LAUNCH",
                        color = Color(0xFF38BDF8L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ArcadePillButton(
                text = if (nextTrack != null) "▶ PLAY TRACK ${trackIndex + 1}" else "★ LISTEN IN ALBUM DECK",
                pillBitmap = btnPillGreen,
                onClick = onNextTrack,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                fontSize = 13.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ArcadePillButton(
                text = "BACK TO MAIN MENU",
                pillBitmap = btnPillGold,
                onClick = onBackToAlbum,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                fontSize = 12.sp
            )
        }

        // Tap-to-Open Vault Exclusive Pop-Up Modal
        if (showExclusivePopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { showExclusivePopup = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(332.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF101D3AL), Color(0xFF060B16L))
                            )
                        )
                        .border(2.dp, Color(0xFFFACC15L), RoundedCornerShape(20.dp))
                        .clickable(enabled = false) {}
                        .padding(18.dp)
                ) {
                    Text(
                        text = "★ VAULT EXCLUSIVE UNLOCKED ★",
                        color = Color(0xFFFACC15L),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = featuredNote.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF040812L))
                            .border(1.dp, Color(0xFF38BDF8L).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = featuredNote.artistNoteQuote,
                                color = Color(0xFFF8FAFCL),
                                fontSize = 11.sp,
                                lineHeight = 15.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✓ ${featuredNote.exclusiveDetail}",
                                color = Color(0xFF4ADE80L),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ArcadePillButton(
                        text = "★ OPEN ARTIST VAULT",
                        pillBitmap = btnPillGold,
                        onClick = {
                            showExclusivePopup = false
                            onOpenArtistVault()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { showExclusivePopup = false }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "CLOSE",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
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
