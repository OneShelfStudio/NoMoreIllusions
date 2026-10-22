package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays the current track artwork and reveals it from bottom to top as gameplay progresses.
 * Features a glowing Forest Green / Electric Indigo laser scanline and animated shimmer.
 */
@Composable
fun TrackRevealOverlay(
    trackIndex: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val safeIndex = trackIndex.coerceIn(1, MusicLibrary.TOTAL_TRACKS)
    val drawableId = remember(safeIndex) {
        val drawableName = "track_%02d".format(safeIndex)
        val id = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.album_art
    }
    val img: ImageBitmap = remember(drawableId) {
        ImageBitmap.imageResource(context.resources, drawableId)
    }

    val shape = RoundedCornerShape(14.dp)
    val p = progress.coerceIn(0f, 1f)

    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shimmerX = shimmer.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        ),
        label = "shimmerX"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.5.dp, Color(0xFF6366F1L).copy(alpha = 0.78f), shape)
            .background(Color(0xDD03070EL))
            .padding(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        ) {
            val revealHeight = size.height * p
            val top = size.height - revealHeight

            // Draw unrevealed optical-illusion matrix backdrop
            drawRect(color = Color(0xFF040912L))
            val gridStep = size.width / 6f
            for (i in 0..6) {
                drawLine(
                    color = Color(0xFF22C55EL).copy(alpha = 0.14f),
                    start = Offset(i * gridStep, 0f),
                    end = Offset(i * gridStep, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF6366F1L).copy(alpha = 0.14f),
                    start = Offset(0f, i * gridStep),
                    end = Offset(size.width, i * gridStep),
                    strokeWidth = 1f
                )
            }

            // Draw revealed portion of the artwork
            if (revealHeight > 0f) {
                clipRect(
                    left = 0f,
                    top = top,
                    right = size.width,
                    bottom = size.height
                ) {
                    drawImage(
                        image = img,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    )

                    // Shimmer band
                    val bandWidth = size.width * 0.4f
                    val x = (size.width * shimmerX.value) - (bandWidth / 2f)
                    val brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.0f),
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        start = Offset(x, top),
                        end = Offset(x + bandWidth, size.height)
                    )
                    drawRect(
                        brush = brush,
                        topLeft = Offset(0f, top),
                        size = Size(size.width, revealHeight)
                    )
                }

                // Glowing Forest Green Laser Scanline at the reveal boundary
                if (top > 1f && top < size.height - 1f) {
                    drawLine(
                        color = Color(0xFF22C55EL),
                        start = Offset(0f, top),
                        end = Offset(size.width, top),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, top),
                        end = Offset(size.width, top),
                        strokeWidth = 1.2f
                    )
                }
            }
        }

        // Percentage readout badge in bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.80f))
                .border(1.dp, Color(0xFF22C55EL).copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${(p * 100f).toInt()}%",
                color = if (p >= 1f) Color(0xFF4ADE80L) else Color(0xFFA5B4FCL),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
