package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders crisp, authentic vector brand logos for Spotify, Apple Music, YouTube, Instagram, and TikTok
 * inside sleek dark-glass circular badges or compact pills (no generic AI bitmap pills).
 */
@Composable
fun SocialBrandIconRow(
    links: List<ArtistSocialLink>,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        links.forEach { link ->
            val accent = Color(link.accentHex)
            if (showLabels) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0A121EL), Color(0xFF04080FL))
                            )
                        )
                        .border(1.2.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(50))
                        .clickable { onOpenUrl(link.url) }
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    BrandVectorGlyph(
                        brand = link.label,
                        tint = accent,
                        size = 15.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = link.label,
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    accent.copy(alpha = 0.18f),
                                    Color(0xFF060B14L),
                                    Color(0xFF030509L)
                                )
                            )
                        )
                        .border(1.3.dp, accent.copy(alpha = 0.72f), CircleShape)
                        .clickable { onOpenUrl(link.url) },
                    contentAlignment = Alignment.Center
                ) {
                    BrandVectorGlyph(
                        brand = link.label,
                        tint = accent,
                        size = 19.dp
                    )
                }
            }
        }
    }
}

@Composable
fun BrandVectorGlyph(
    brand: String,
    tint: Color,
    size: Dp = 18.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        when (brand.uppercase()) {
            "SPOTIFY" -> {
                // Spotify circle + 3 curved soundwave arcs
                drawCircle(
                    color = tint.copy(alpha = 0.22f),
                    radius = w * 0.48f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = tint,
                    radius = w * 0.46f,
                    center = Offset(cx, cy),
                    style = Stroke(width = w * 0.08f)
                )
                val arcOffsets = listOf(
                    Triple(0.28f, 0.72f, 0.34f),
                    Triple(0.31f, 0.69f, 0.50f),
                    Triple(0.35f, 0.65f, 0.66f)
                )
                arcOffsets.forEachIndexed { idx, (xStart, xEnd, yNorm) ->
                    val path = Path().apply {
                        moveTo(w * xStart, h * (yNorm + 0.02f))
                        quadraticTo(
                            w * 0.50f,
                            h * (yNorm - 0.08f),
                            w * xEnd,
                            h * (yNorm + 0.04f)
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(
                            width = w * (0.095f - idx * 0.012f),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            "APPLE MUSIC" -> {
                // Sleek beamed eighth notes (Apple Music emblem)
                val stemLeft = w * 0.36f
                val stemRight = w * 0.76f
                val topLeft = h * 0.22f
                val topRight = h * 0.15f
                val botLeft = h * 0.72f
                val botRight = h * 0.65f

                // Top slant beam
                val beam = Path().apply {
                    moveTo(stemLeft, topLeft)
                    lineTo(stemRight, topRight)
                    lineTo(stemRight, topRight + h * 0.13f)
                    lineTo(stemLeft, topLeft + h * 0.13f)
                    close()
                }
                drawPath(beam, color = tint)

                // Left & right stems
                drawLine(
                    color = tint,
                    start = Offset(stemLeft + w * 0.03f, topLeft + h * 0.06f),
                    end = Offset(stemLeft + w * 0.03f, botLeft),
                    strokeWidth = w * 0.09f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(stemRight - w * 0.03f, topRight + h * 0.06f),
                    end = Offset(stemRight - w * 0.03f, botRight),
                    strokeWidth = w * 0.09f,
                    cap = StrokeCap.Round
                )

                // Note heads
                drawOval(
                    color = Color.White,
                    topLeft = Offset(w * 0.16f, botLeft - h * 0.11f),
                    size = Size(w * 0.26f, h * 0.20f)
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(w * 0.56f, botRight - h * 0.11f),
                    size = Size(w * 0.26f, h * 0.20f)
                )
            }

            "YOUTUBE" -> {
                // Rounded play screen + crisp center play triangle
                drawRoundRect(
                    color = tint.copy(alpha = 0.25f),
                    topLeft = Offset(w * 0.08f, h * 0.20f),
                    size = Size(w * 0.84f, h * 0.60f),
                    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.08f, h * 0.20f),
                    size = Size(w * 0.84f, h * 0.60f),
                    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
                    style = Stroke(width = w * 0.08f)
                )
                val tri = Path().apply {
                    moveTo(w * 0.41f, h * 0.35f)
                    lineTo(w * 0.65f, h * 0.50f)
                    lineTo(w * 0.41f, h * 0.65f)
                    close()
                }
                drawPath(tri, color = Color.White)
            }

            "INSTAGRAM" -> {
                // Camera squircle + lens circle + flash dot
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.12f, h * 0.12f),
                    size = Size(w * 0.76f, h * 0.76f),
                    cornerRadius = CornerRadius(w * 0.22f, w * 0.22f),
                    style = Stroke(width = w * 0.085f)
                )
                drawCircle(
                    color = Color.White,
                    radius = w * 0.19f,
                    center = Offset(cx, cy),
                    style = Stroke(width = w * 0.08f)
                )
                drawCircle(
                    color = tint,
                    radius = w * 0.055f,
                    center = Offset(w * 0.70f, h * 0.30f)
                )
            }

            "TIKTOK" -> {
                // Stylized TikTok 'd' note with dual cyan/pink chromatic offset
                val stemX = w * 0.54f
                // Offset shadow
                drawLine(
                    color = Color(0xFFEC4899L),
                    start = Offset(stemX + w * 0.04f, h * 0.18f),
                    end = Offset(stemX + w * 0.04f, h * 0.68f),
                    strokeWidth = w * 0.10f,
                    cap = StrokeCap.Round
                )
                // Main stem
                drawLine(
                    color = Color.White,
                    start = Offset(stemX, h * 0.16f),
                    end = Offset(stemX, h * 0.66f),
                    strokeWidth = w * 0.10f,
                    cap = StrokeCap.Round
                )
                // Top flag curve
                val flag = Path().apply {
                    moveTo(stemX, h * 0.18f)
                    quadraticTo(w * 0.62f, h * 0.36f, w * 0.80f, h * 0.38f)
                }
                drawPath(
                    path = flag,
                    color = tint,
                    style = Stroke(width = w * 0.10f, cap = StrokeCap.Round)
                )
                // Bottom loop
                drawCircle(
                    color = Color.White,
                    radius = w * 0.16f,
                    center = Offset(w * 0.38f, h * 0.66f),
                    style = Stroke(width = w * 0.095f)
                )
            }
        }
    }
}
