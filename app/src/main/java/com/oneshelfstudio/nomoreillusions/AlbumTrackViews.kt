package com.oneshelfstudio.nomoreillusions

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TrackListMode { LIST, GRID }

data class TrackAsset(
    val rawAudioId: Int,
    val drawableId: Int
)

fun resolveTrackAsset(context: Context, trackIndex: Int): TrackAsset {
    val safeIdx = trackIndex.coerceIn(1, MusicLibrary.TOTAL_TRACKS)
    val name = "track_" + safeIdx.toString().padStart(2, '0')
    var rawId = context.resources.getIdentifier(name, "raw", context.packageName)
    if (rawId == 0) {
        rawId = context.resources.getIdentifier("track_01", "raw", context.packageName)
    }
    var drawId = context.resources.getIdentifier(name, "drawable", context.packageName)
    if (drawId == 0) {
        drawId = R.drawable.album_art
    }
    return TrackAsset(rawAudioId = rawId, drawableId = drawId)
}

@Composable
fun TrackListList(
    modifier: Modifier,
    tracks: List<DemoTrack>,
    selectedTrackIndex: Int,
    isPlayable: (DemoTrack) -> Boolean,
    isArtworkUnlocked: (DemoTrack) -> Boolean,
    onSelect: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xD9050B16L), Color(0xE602050AL))
                )
            )
            .border(1.dp, Color(0xFF6366F1L).copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tracks) { idx, t ->
            val selectedRow = (idx + 1) == selectedTrackIndex
            val playable = isPlayable(t)
            val artUnlocked = isArtworkUnlocked(t)

            val rowBrush = when {
                selectedRow -> Brush.horizontalGradient(
                    listOf(Color(0xFF0E1D3AL), Color(0xFF061614L))
                )
                artUnlocked -> Brush.horizontalGradient(
                    listOf(Color(0xFF081B14L), Color(0xFF081022L))
                )
                playable -> Brush.horizontalGradient(
                    listOf(Color(0xFF071020L), Color(0xFF040914L))
                )
                else -> Brush.horizontalGradient(
                    listOf(Color(0xFF040810L), Color(0xFF020408L))
                )
            }

            val borderColor = when {
                selectedRow -> Color(0xFF22C55EL)
                artUnlocked -> Color(0xFF4ADE80L).copy(alpha = 0.50f)
                playable -> Color(0xFF6366F1L).copy(alpha = 0.35f)
                else -> Color.White.copy(alpha = 0.08f)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(rowBrush)
                    .border(if (selectedRow) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { onSelect(idx + 1) }
                    .padding(vertical = 11.dp, horizontal = 12.dp)
            ) {
                // Track Number Pill Badge
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 26.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (selectedRow) Color(0xFF22C55EL).copy(alpha = 0.25f)
                            else if (playable) Color(0xFF6366F1L).copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.04f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.index.toString().padStart(2, '0'),
                        color = if (selectedRow) Color(0xFF4ADE80L) else if (playable) Color(0xFFA5B4FCL) else Color.White.copy(alpha = 0.35f),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t.title,
                        color = if (playable) Color.White else Color.White.copy(alpha = 0.45f),
                        fontWeight = if (selectedRow) FontWeight.Black else FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${t.bpm.toInt()} BPM • ${formatTrackTime(t.durationSec)} • ${t.sections.size} SECTIONS",
                        color = if (selectedRow) Color(0xFFA5B4FCL) else Color.White.copy(alpha = 0.48f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Capsule
                when {
                    artUnlocked -> {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF16A34AL).copy(alpha = 0.22f))
                                .border(1.dp, Color(0xFF4ADE80L).copy(alpha = 0.7f), CircleShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✓ UNLOCKED",
                                color = Color(0xFF4ADE80L),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    playable -> {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1L).copy(alpha = 0.20f))
                                .border(1.dp, Color(0xFF818CF8L).copy(alpha = 0.65f), CircleShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PLAYABLE",
                                color = Color(0xFFA5B4FCL),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${t.unlockCost} PTS",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackListGrid(
    modifier: Modifier,
    tracks: List<DemoTrack>,
    selectedTrackIndex: Int,
    isPlayable: (DemoTrack) -> Boolean,
    isArtworkUnlocked: (DemoTrack) -> Boolean,
    onSelect: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xD9050B16L), Color(0xE602050AL))
                )
            )
            .border(1.dp, Color(0xFF6366F1L).copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tracks) { t ->
            val selected = t.index == selectedTrackIndex
            val playable = isPlayable(t)
            val artUnlocked = isArtworkUnlocked(t)

            val tileBrush = when {
                selected -> Brush.verticalGradient(
                    listOf(Color(0xFF0E1D3AL), Color(0xFF061614L))
                )
                artUnlocked -> Brush.verticalGradient(
                    listOf(Color(0xFF081B14L), Color(0xFF070F1EL))
                )
                playable -> Brush.verticalGradient(
                    listOf(Color(0xFF081226L), Color(0xFF040914L))
                )
                else -> Brush.verticalGradient(
                    listOf(Color(0xFF040810L), Color(0xFF020408L))
                )
            }
            val borderCol = when {
                selected -> Color(0xFF22C55EL)
                artUnlocked -> Color(0xFF4ADE80L).copy(alpha = 0.65f)
                playable -> Color(0xFF6366F1L).copy(alpha = 0.38f)
                else -> Color.White.copy(alpha = 0.08f)
            }

            Box(
                modifier = Modifier
                    .height(92.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tileBrush)
                    .border(if (selected) 1.8.dp else 1.dp, borderCol, RoundedCornerShape(14.dp))
                    .clickable { onSelect(t.index) }
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "TRACK %02d • ${t.bpm.toInt()} BPM".format(t.index),
                        color = if (selected) Color(0xFF4ADE80L) else Color(0xFFA5B4FCL),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = t.title,
                        color = if (playable) Color.White else Color.White.copy(alpha = 0.45f),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTrackTime(t.durationSec),
                        color = Color.White.copy(alpha = 0.50f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }

                val badgeText = when {
                    artUnlocked -> "✓ UNLOCKED"
                    playable -> "PLAYABLE"
                    else -> "${t.unlockCost} PTS"
                }
                val badgeColor = when {
                    artUnlocked -> Color(0xFF4ADE80L)
                    playable -> Color(0xFFA5B4FCL)
                    else -> Color.White.copy(alpha = 0.45f)
                }
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(20.dp)) {
        val barCount = 32
        val spacing = size.width / barCount
        val barWidth = spacing * 0.55f

        for (i in 0 until barCount) {
            val normI = i.toFloat() / barCount.toFloat()
            val amp = if (isPlaying) {
                val s1 = kotlin.math.sin(wavePhase * 2f + normI * 10f) * 0.4f
                val s2 = kotlin.math.cos(wavePhase * 3f + normI * 16f) * 0.3f
                val s3 = kotlin.math.sin(wavePhase * 5f + normI * 4f) * 0.2f
                (0.25f + kotlin.math.abs(s1 + s2 + s3)).coerceIn(0.15f, 1f)
            } else {
                (0.14f + 0.06f * kotlin.math.sin(normI * 12f + wavePhase * 0.5f)).coerceIn(0.10f, 0.26f)
            }
            val barHeight = size.height * amp
            val x = i * spacing + (spacing - barWidth) / 2f
            val y = size.height - barHeight

            val isPlayed = normI <= progress.coerceIn(0f, 1f)
            val barColor = when {
                isPlayed -> Color(0xFF22C55EL)
                normI < 0.5f -> Color(0xFF6366F1L)
                else -> Color(0xFFA855F7L)
            }
            drawRoundRect(
                color = barColor.copy(alpha = if (isPlaying) (if (isPlayed) 0.95f else 0.72f) else 0.40f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

fun formatTrackTime(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    val m = s / 60
    val r = s % 60
    return "$m:${r.toString().padStart(2, '0')}"
}
