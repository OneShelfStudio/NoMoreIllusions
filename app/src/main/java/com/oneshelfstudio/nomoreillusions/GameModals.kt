package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
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
                    Text("PULSE PTS: %,d".format(pulsePoints), color = Color(0xFF4ADE80L), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Distinct Rewarded Ad Continue Button (Primary Revive Action)
            if (hasAd || BuildConfig.DEBUG) {
                ArcadeAdPillButton(
                    text = "WATCH AD TO REVIVE",
                    onClick = onWatchAd,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 2. Prominent Replay Button
            ArcadePillButton(
                text = "REPLAY TRACK",
                pillBitmap = btnPillGreen,
                onClick = onReplayTrack,
                icon = btnIconRestart,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth().height(46.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Continue with Pulse Points (1,000 PTS) or Get Points Store
            if (canContinue) {
                ArcadePillButton(
                    text = "CONTINUE (1,000 PTS)",
                    pillBitmap = btnPillCyan,
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                ArcadePillButton(
                    text = "GET PULSE POINTS",
                    pillBitmap = btnPillCyan,
                    onClick = onBuyPoints,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 4. Back to Album
            ArcadePillButton(
                text = "OPEN ALBUM",
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
    uiDialogPanel: ImageBitmap,
    btnPillGreen: ImageBitmap,
    btnPillGold: ImageBitmap,
    onNextTrack: () -> Unit,
    onBackToAlbum: () -> Unit
) {
    val currentTrack = MusicLibrary.getTrack(trackIndex)
    val nextTrack = if (trackIndex < MusicLibrary.TOTAL_TRACKS) MusicLibrary.getTrack(trackIndex + 1) else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        ArcadeDialog(
            title = "ILLUSION BROKEN!",
            panelBitmap = uiDialogPanel,
            modifier = Modifier.width(320.dp).wrapContentHeight()
        ) {
            Text(
                text = "${currentTrack.title.uppercase()} • COMPLETE",
                color = Color(0xFF4ADE80L),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Unlocked Album Artwork Showcase Card (100% revealed!)
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF22C55EL), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                TrackRevealOverlay(
                    trackIndex = trackIndex,
                    progress = 1.0f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Unlocked celebration badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF16A34AL).copy(alpha = 0.22f))
                    .border(1.dp, Color(0xFF4ADE80L).copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "MASTER TRACK & ART UNLOCKED IN ALBUM",
                    color = Color(0xFF4ADE80L),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ArcadePillButton(
                text = if (nextTrack != null) "PLAY ${nextTrack.title.uppercase()}" else "ALBUM COMPLETE",
                pillBitmap = btnPillGreen,
                onClick = onNextTrack,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ArcadePillButton(
                text = "VIEW IN ALBUM",
                pillBitmap = btnPillGold,
                onClick = onBackToAlbum,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                fontSize = 13.sp
            )
        }
    }
}
