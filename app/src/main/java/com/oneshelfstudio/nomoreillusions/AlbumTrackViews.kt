package com.oneshelfstudio.nomoreillusions

import android.content.Context
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
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
    val haptics = rememberStudioHaptics()
    val cardFrameBitmap = ImageBitmap.imageResource(id = R.drawable.ui_card_frame_ornate)
    val tileFrameBitmap = ImageBitmap.imageResource(id = R.drawable.ui_shop_tile_frame)
    val tileSelectedBitmap = ImageBitmap.imageResource(id = R.drawable.ui_shop_tile_selected)
    val deckShape = CutCornerShape(14.dp)
    val rowShape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp, topEnd = 5.dp, bottomStart = 5.dp)
    val tagShape = CutCornerShape(5.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(deckShape)
            .border(1.2.dp, Color(0xFF38BDF8L).copy(alpha = 0.38f), deckShape)
    ) {
        Image(
            bitmap = cardFrameBitmap,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        itemsIndexed(tracks) { idx, t ->
            val selectedRow = (idx + 1) == selectedTrackIndex
            val playable = isPlayable(t)
            val artUnlocked = isArtworkUnlocked(t)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "TrackRowScale_${t.index}"
            )

            val borderColor = when {
                selectedRow -> Color(0xFF4ADE80L)
                artUnlocked -> Color(0xFF4ADE80L).copy(alpha = 0.55f)
                playable -> Color(0xFF38BDF8L).copy(alpha = 0.42f)
                else -> Color.White.copy(alpha = 0.14f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clip(rowShape)
                    .border(if (selectedRow) 1.6.dp else 1.dp, borderColor, rowShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptics.tick()
                        onSelect(idx + 1)
                    }
            ) {
                Image(
                    bitmap = if (selectedRow) tileSelectedBitmap else tileFrameBitmap,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds,
                    alpha = if (playable) 1f else 0.72f
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 14.dp)
                ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 28.dp)
                        .clip(CutCornerShape(6.dp))
                        .background(
                            if (selectedRow) Color(0xFF22C55EL).copy(alpha = 0.25f)
                            else if (playable) Color(0xFF38BDF8L).copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.index.toString().padStart(2, '0'),
                        color = if (selectedRow) Color(0xFF4ADE80L) else if (playable) Color(0xFF7DD3FCL) else Color.White.copy(alpha = 0.35f),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t.title,
                        color = if (playable) Color.White else Color.White.copy(alpha = 0.45f),
                        fontWeight = if (selectedRow) FontWeight.Black else FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val pressing = MusicLibrary.pressingEditionForTrack(t.index)
                    Text(
                        text = "${formatTrackTime(t.durationSec)} • ${pressing.editionTitle}",
                        color = if (selectedRow) Color(0xFF7DD3FCL) else Color(pressing.primaryHex).copy(alpha = 0.78f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                when {
                    artUnlocked -> {
                        Box(
                            modifier = Modifier
                                .clip(tagShape)
                                .background(Color(0xFF16A34AL).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFF4ADE80L).copy(alpha = 0.78f), tagShape)
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
                                .clip(tagShape)
                                .background(Color(0xFF0284C7L).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFF38BDF8L).copy(alpha = 0.78f), tagShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PLAYABLE",
                                color = Color(0xFF7DD3FCL),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(tagShape)
                                .background(Color(0xFFFACC15L).copy(alpha = 0.14f))
                                .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.50f), tagShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${t.unlockCost} ♪",
                                color = Color(0xFFFDE047L),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                }
            }
        }

        // Reserved Upcoming Song Slots for Ethan Correa's Next Official Singles
        items(MusicLibrary.upcomingReleaseSlots.size) { slotIdx ->
            val slot = MusicLibrary.upcomingReleaseSlots[slotIdx]
            val slotAccent = Color(slot.accentHex)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(rowShape)
                    .border(1.dp, slotAccent.copy(alpha = 0.34f), rowShape)
            ) {
                Image(
                    bitmap = tileFrameBitmap,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds,
                    alpha = 0.75f
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 14.dp)
                ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 28.dp)
                        .clip(CutCornerShape(6.dp))
                        .background(slotAccent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slot.index.toString().padStart(2, '0'),
                        color = slotAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.workingTitle,
                        color = Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ETHAN CORREA • ${slot.editionName.uppercase()}",
                        color = slotAccent.copy(alpha = 0.78f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(tagShape)
                        .background(slotAccent.copy(alpha = 0.16f))
                        .border(1.dp, slotAccent.copy(alpha = 0.55f), tagShape)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = slot.statusBadge,
                        color = slotAccent,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
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
    val haptics = rememberStudioHaptics()
    val cardFrameBitmap = ImageBitmap.imageResource(id = R.drawable.ui_card_frame_ornate)
    val tileFrameBitmap = ImageBitmap.imageResource(id = R.drawable.ui_shop_tile_frame)
    val tileSelectedBitmap = ImageBitmap.imageResource(id = R.drawable.ui_shop_tile_selected)
    val deckShape = CutCornerShape(14.dp)
    val tileShape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp, topEnd = 5.dp, bottomStart = 5.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(deckShape)
            .border(1.2.dp, Color(0xFF38BDF8L).copy(alpha = 0.38f), deckShape)
    ) {
        Image(
            bitmap = cardFrameBitmap,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        items(tracks) { t ->
            val selected = t.index == selectedTrackIndex
            val playable = isPlayable(t)
            val artUnlocked = isArtworkUnlocked(t)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "TrackTileScale_${t.index}"
            )

            val borderCol = when {
                selected -> Color(0xFF4ADE80L)
                artUnlocked -> Color(0xFF4ADE80L).copy(alpha = 0.65f)
                playable -> Color(0xFF38BDF8L).copy(alpha = 0.42f)
                else -> Color.White.copy(alpha = 0.14f)
            }

            Box(
                modifier = Modifier
                    .height(106.dp)
                    .scale(scale)
                    .clip(tileShape)
                    .border(if (selected) 1.8.dp else 1.dp, borderCol, tileShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptics.tick()
                        onSelect(t.index)
                    }
            ) {
                Image(
                    bitmap = if (selected) tileSelectedBitmap else tileFrameBitmap,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds,
                    alpha = if (playable) 1f else 0.72f
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TRACK %02d • STUDIO MASTER".format(t.index),
                        color = if (selected) Color(0xFF4ADE80L) else Color(0xFF7DD3FCL),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.5.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
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
                        color = Color.White.copy(alpha = 0.55f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }

                val badgeText = when {
                    artUnlocked -> "✓ UNLOCKED"
                    playable -> "PLAYABLE"
                    else -> "${t.unlockCost} ♪"
                }
                val badgeColor = when {
                    artUnlocked -> Color(0xFF4ADE80L)
                    playable -> Color(0xFF7DD3FCL)
                    else -> Color(0xFFFDE047L)
                }
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                )
            }
        }

        items(MusicLibrary.upcomingReleaseSlots) { slot ->
            val slotAccent = Color(slot.accentHex)
            Box(
                modifier = Modifier
                    .height(106.dp)
                    .clip(tileShape)
                    .border(1.dp, slotAccent.copy(alpha = 0.34f), tileShape)
            ) {
                Image(
                    bitmap = tileFrameBitmap,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds,
                    alpha = 0.75f
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TRACK %02d • %s".format(slot.index, slot.statusBadge),
                        color = slotAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = slot.workingTitle,
                        color = Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = slot.editionName,
                        color = slotAccent.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "SOON",
                    color = slotAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                )
            }
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
                isPlayed -> Color(0xFF4ADE80L)
                normI < 0.5f -> Color(0xFF38BDF8L)
                else -> Color(0xFFFACC15L)
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
