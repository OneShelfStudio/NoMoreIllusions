package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HUD(
    score: Int,
    highScore: Int,
    pulsePoints: Int,
    unlockedTrackCount: Int,
    trackIndex: Int,
    trackProgress: Float,
    focus: Float,
    multiplier: Int,
    multiplierTime: Float,
    btnIconPause: ImageBitmap,
    difficulty: GameDifficulty = GameDifficulty.REGULAR,
    currentSectionLabel: String = "INTRO",
    isExtraJuiceActive: Boolean = false,
    onPause: () -> Unit
) {
    val activeTrack = remember(trackIndex) { MusicLibrary.getTrack(trackIndex) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 56.dp, start = 12.dp, end = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optical track decoding progress line at the top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(trackProgress.coerceIn(0f, 1f))
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF16A34AL), Color(0xFF6366F1L), Color(0xFFF8FAFCL))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Column: Track & Score, Live Song Section Chip, Points/Unlocked, Focus
                Column(modifier = Modifier.weight(1f)) {
                    // Track Title & Score Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xE6040906L))
                            .border(
                                1.2.dp,
                                if (isExtraJuiceActive) Color(0xFF4ADE80L) else Color(0xFF6366F1L).copy(alpha = 0.65f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeTrack.title.uppercase(),
                                color = Color(0xFF86EFACL),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "%,d".format(score),
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Live Timestamped Song Section & BPM Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isExtraJuiceActive) {
                                    Brush.horizontalGradient(listOf(Color(0xFF15803DL), Color(0xFF4F46E5L)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xD90E1326L), Color(0xD906140DL)))
                                }
                            )
                            .border(
                                1.dp,
                                if (isExtraJuiceActive) Color.White else Color(0xFF818CF8L).copy(alpha = 0.45f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 3.5.dp)
                    ) {
                        Text(
                            text = if (isExtraJuiceActive) "⚡ $currentSectionLabel • EXTRA JUICE!" else "$currentSectionLabel • ${activeTrack.bpm.toInt()} BPM",
                            color = if (isExtraJuiceActive) Color.White else Color(0xFFC7D2FEL),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Points & Unlocked Tracks Chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xD906120BL))
                                .border(1.dp, Color(0xFF22C55EL).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "%,d PTS".format(pulsePoints),
                                color = Color(0xFF4ADE80L),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xD9080B1AL))
                                .border(1.dp, Color(0xFF6366F1L).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "UNLOCKED ${unlockedTrackCount.coerceIn(1, MusicLibrary.TOTAL_TRACKS)}/${MusicLibrary.TOTAL_TRACKS}",
                                color = Color(0xFFA5B4FCL),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (difficulty != GameDifficulty.REGULAR) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[$difficulty]",
                                color = if (difficulty == GameDifficulty.EASY) Color(0xFF4ADE80L) else Color(0xFFC084FCL),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Multiplier & Best Score
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (multiplier > 1) {
                            Text(
                                text = "x$multiplier (${multiplierTime.toInt()}s)",
                                color = Color(0xFF4ADE80L),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "BEST: %,d".format(highScore),
                            color = Color.White.copy(alpha = 0.60f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Focus Energy Bar
                    Box(
                        modifier = Modifier
                            .width(104.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(focus.coerceIn(0f, 1f))
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF16A34AL), Color(0xFF6366F1L), Color.White)
                                    )
                                )
                        )
                    }
                }

                // Right Column: Artwork Decoder Card + Pause Button
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TrackRevealOverlay(
                            trackIndex = trackIndex,
                            progress = trackProgress,
                            modifier = Modifier.size(76.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "ART REVEAL",
                            color = Color(0xFFA5B4FCL),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    ArcadeIconButton(
                        iconBitmap = btnIconPause,
                        onClick = onPause,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }
    }
}
