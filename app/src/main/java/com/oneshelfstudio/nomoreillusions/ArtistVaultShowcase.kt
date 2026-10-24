package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*


@Composable
internal fun ArtistVaultShowcaseModal(
    vaultId: String,
    animTime: Float,
    onLaunchTrack: () -> Unit,
    onClose: () -> Unit
) {
    var selectedSubTab by remember(vaultId) { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(enabled = true) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A1228L), Color(0xFF040711L))
                    )
                )
                .border(1.5.dp, Color(0xFFFACC15L).copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            val title = when (vaultId) {
                "VAULT_DEMO" -> "★ STUDIO VAULT TAPE DECK"
                "VAULT_LYRICS" -> "★ DIRECTOR'S CUT LYRIC BOOKLET"
                "VAULT_POLAROIDS" -> "★ NO MORĒ ILLUSĪONS ERA STORYBOARD"
                else -> "★ 4K HOLOGRAPHIC ART & VINYL VAULT"
            }
            val badge = when (vaultId) {
                "VAULT_DEMO" -> "MASTER REEL • SLOWED 0.85x & NIGHTCORE 1.20x"
                "VAULT_LYRICS" -> "SYNCHRONIZED SONG ARCHITECTURE & CHOREOGRAPHY"
                "VAULT_POLAROIDS" -> "BEHIND THE SCENES • COLLECTOR CARDS"
                else -> "COLLECTOR'S PRESSING • HOLOGRAPHIC FOIL"
            }

            Text(
                text = title,
                color = Color(0xFFFACC15L),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = badge,
                color = Color(0xFF38BDF8L),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (vaultId) {
                "VAULT_DEMO" -> {
                    // Spinning Dual-Reel Studio Tape Deck Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF04070FL))
                            .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cy = size.height * 0.5f
                            val leftX = size.width * 0.30f
                            val rightX = size.width * 0.70f
                            val reelR = size.height * 0.34f
                            // Tape path between reels
                            drawLine(
                                color = Color(0xFFFACC15L).copy(alpha = 0.55f),
                                start = Offset(leftX, cy + reelR * 0.85f),
                                end = Offset(rightX, cy + reelR * 0.85f),
                                strokeWidth = 3f
                            )
                            listOf(leftX, rightX).forEach { rx ->
                                drawCircle(
                                    color = Color(0xFF0F172AL),
                                    radius = reelR,
                                    center = Offset(rx, cy)
                                )
                                drawCircle(
                                    color = Color(0xFFFACC15L),
                                    radius = reelR,
                                    center = Offset(rx, cy),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                                )
                                for (s in 0 until 3) {
                                    val ang = animTime * 2.6f + s * (2f * PI.toFloat() / 3f)
                                    drawLine(
                                        color = Color(0xFFFDE047L),
                                        start = Offset(rx, cy),
                                        end = Offset(rx + cos(ang) * reelR * 0.85f, cy + sin(ang) * reelR * 0.85f),
                                        strokeWidth = 3f
                                    )
                                }
                                drawCircle(
                                    color = Color(0xFF38BDF8L),
                                    radius = reelR * 0.24f,
                                    center = Offset(rx, cy)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Unlocked in your Turntable Console: switch between ORIGINAL (1.0x), NIGHTCORE OVERCLOCK (1.20x), SLOWED + REVERB (0.85x), and ENDLESS LOOP (∞) directly on the Main Menu!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                "VAULT_LYRICS" -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("01. CONFIDE", "02. NUMBER ONE").forEachIndexed { idx, tabLabel ->
                            val sel = selectedSubTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (sel) Color(0xFF38BDF8L) else Color(0xFF0F172AL))
                                    .clickable { selectedSubTab = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabLabel,
                                    color = if (sel) Color(0xFF030712L) else Color.White.copy(alpha = 0.75f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val notes = if (selectedSubTab == 0) {
                        listOf(
                            "0:00 • ATMOSPHERIC INTRO — \"Shadow in the mirror...\" (Obsidian Nebula Drift)",
                            "0:54 • CHORUS DROP I — \"Confide in me / No more illusions\" (24K Golden Overdrive + Concert Lasers)",
                            "1:22 • VERSE II (B3NJI FEATURE) — \"Caught inside the undertow\" (Singularity Wormholes Materialize)",
                            "2:15 • BRIDGE BREAKDOWN — \"Strip away the disguise\" (Solar Eclipse Blackout & Sub-Bass Swell)",
                            "2:42 • FINAL CLIMAX — Full 24K Gold Stage Overdrive + Supernova Gyroscope Orbs"
                        )
                    } else {
                        listOf(
                            "0:00 • PULSE INTRO — \"Dialed into the frequency...\" (138 BPM High-Velocity Lock)",
                            "0:26 • CHORUS HOOK — \"You're my number one\" (24K Gold Concert Laser Fan + Twin Pulse)",
                            "0:55 • RAPID VERSE — \"Racing through the neon haze\" (Chrono-Saw & Seraph Swarm Surge)",
                            "1:28 • BASS DROP — \"Zero gravity\" (Kerr Accretion Wormholes & Neon Phase Gates)",
                            "1:52 • FINAL OVERDRIVE — Maximum BPM Star-Power Finale"
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        notes.forEach { line ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF060C1AL))
                                    .border(1.dp, Color(0xFF38BDF8L).copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = line,
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                "VAULT_POLAROIDS" -> {
                    val cards = listOf(
                        Triple("SESSION 01 • THE MIRROR ERA", "Indigo & Emerald visual identity born from late-night vocal layering.", Color(0xFF4ADE80L)),
                        Triple("SESSION 02 • CONFIDE (FEAT. B3NJI)", "124 BPM emotional anthem fusing melodic hooks with raw honesty.", Color(0xFF818CF8L)),
                        Triple("SESSION 03 • NUMBER ONE OVERDRIVE", "138 BPM high-octane rush engineered for peak replayability.", Color(0xFFFACC15L)),
                        Triple("SESSION 04 • THE NEXT CHAPTER", "Stay locked to the Release Radar for upcoming singles & live dates.", Color(0xFFE879F9L))
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cards.forEach { (head, body, col) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF060B16L))
                                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = head,
                                        color = col,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = body,
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    // 4K Holographic Cover Art & Gold Vinyl Showcase
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFFFACC15L), RoundedCornerShape(16.dp))
                    ) {
                        TrackRevealOverlay(
                            trackIndex = if (selectedSubTab == 0) 1 else 2,
                            progress = 1.0f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("CONFIDE 4K ART", "NUMBER ONE 4K ART").forEachIndexed { idx, label ->
                            val sel = selectedSubTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (sel) Color(0xFF4ADE80L) else Color(0xFF0F172AL))
                                    .clickable { selectedSubTab = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (sel) Color(0xFF030712L) else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLOSE VAULT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1.25f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF22C55EL), Color(0xFF15803DL))
                            )
                        )
                        .clickable { onLaunchTrack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LAUNCH STAGE ▶",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isUnlocked: Boolean = true,
    costNotes: Int = 0,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardBrush = when {
        isSelected && isUnlocked -> Brush.verticalGradient(
            listOf(
                accentColor.copy(alpha = 0.30f),
                Color(0xFF070E1AL),
                Color(0xFF03060CL)
            )
        )
        !isUnlocked -> Brush.verticalGradient(
            listOf(
                Color(0xFF05070DL),
                Color(0xFF020307L)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color(0xFF070C16L),
                Color(0xFF03060CL)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBrush)
            .border(
                width = if (isSelected && isUnlocked) 1.8.dp else 1.dp,
                color = when {
                    isSelected && isUnlocked -> accentColor
                    !isUnlocked -> Color(0xFFFACC15L).copy(alpha = 0.32f)
                    else -> Color.White.copy(alpha = 0.14f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !isUnlocked -> Color(0xFFFACC15L)
                                isSelected -> accentColor
                                else -> accentColor.copy(alpha = 0.45f)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isUnlocked) title else "🔒 $title",
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isUnlocked) subtitle else "UNLOCK • $costNotes ♪ NOTES",
                color = when {
                    !isUnlocked -> Color(0xFFFDE047L)
                    isSelected -> accentColor
                    else -> Color.White.copy(alpha = 0.48f)
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
