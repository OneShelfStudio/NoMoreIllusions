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
 * Displays the current track's Collector's Vinyl Pressing Edition framing Ethan's official artwork,
 * revealing radially from the center as gameplay progresses with zero custom PNGs required per future song.
 */
@Composable
fun TrackRevealOverlay(
    trackIndex: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val safeIndex = trackIndex.coerceAtLeast(1)
    val pressing = remember(safeIndex) { MusicLibrary.pressingEditionForTrack(safeIndex) }
    val primaryAccent = Color(pressing.primaryHex)
    val secondaryAccent = Color(pressing.secondaryHex)

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
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(primaryAccent.copy(alpha = 0.88f), secondaryAccent.copy(alpha = 0.78f))
                ),
                shape
            )
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

            // Dimmed silhouette base + Collector's Pressing concentric vinyl grooves
            drawRect(color = Color(0xFF040812L))
            drawImage(
                image = img,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                alpha = 0.22f
            )
            for (g in 1..4) {
                val ringTint = if (g % 2 == 0) secondaryAccent else primaryAccent
                drawCircle(
                    color = ringTint.copy(alpha = 0.20f),
                    radius = (size.minDimension * 0.12f * g),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4f)
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
                    // Subtle Collector's Edition foil rim sheen around the cover
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                primaryAccent.copy(alpha = 0.14f),
                                secondaryAccent.copy(alpha = 0.28f)
                            ),
                            center = Offset(cx, cy),
                            radius = size.minDimension * 0.62f
                        ),
                        radius = size.minDimension * 0.62f,
                        center = Offset(cx, cy)
                    )
                    val bandWidth = size.width * 0.45f
                    val x = (size.width * shimmerX.value) - (bandWidth / 2f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                primaryAccent.copy(alpha = 0.22f),
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
                        color = primaryAccent.copy(alpha = 0.90f),
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
                .background(Color.Black.copy(alpha = 0.82f))
                .border(1.dp, primaryAccent.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${(p * 100f).toInt()}%",
                color = if (p >= 1f) primaryAccent else Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
