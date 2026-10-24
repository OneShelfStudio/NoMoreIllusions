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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
            val cx = size.width * 0.5f
            val cy = size.height * 0.5f
            val maxR = kotlin.math.hypot(cx, cy)
            val irisR = (maxR * p).coerceAtLeast(2f)

            // Dimmed silhouette base + soft concentric vinyl grooves (Zero grid lines!)
            drawRect(color = Color(0xFF040812L))
            drawImage(
                image = img,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                alpha = 0.22f
            )
            for (g in 1..3) {
                drawCircle(
                    color = Color(0xFF38BDF8L).copy(alpha = 0.12f),
                    radius = (size.minDimension * 0.16f * g),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2f)
                )
            }

            // Smooth Radial Iris Reveal expanding outward from the center
            if (p > 0.01f) {
                val irisPath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            left = cx - irisR,
                            top = cy - irisR,
                            right = cx + irisR,
                            bottom = cy + irisR
                        )
                    )
                }
                clipPath(irisPath) {
                    drawImage(
                        image = img,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    )
                    val bandWidth = size.width * 0.45f
                    val x = (size.width * shimmerX.value) - (bandWidth / 2f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.0f)
                            ),
                            start = Offset(x, 0f),
                            end = Offset(x + bandWidth, size.height)
                        ),
                        topLeft = Offset.Zero,
                        size = size
                    )
                }
                if (p < 0.99f) {
                    drawCircle(
                        color = Color(0xFF4ADE80L).copy(alpha = 0.85f),
                        radius = irisR,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.4f)
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
