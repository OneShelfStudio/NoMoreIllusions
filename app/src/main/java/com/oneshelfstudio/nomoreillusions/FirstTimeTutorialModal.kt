package com.oneshelfstudio.nomoreillusions

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

enum class FirstTimeDiscovery(
    val prefKey: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val tip: String,
    val buttonText: String,
    val accentColor: Color
) {
    FAN_WELCOME(
        prefKey = "seen_discovery_fan_welcome_v2",
        badge = "★ QUICK GUIDE ★",
        title = "HOW TO PLAY",
        subtitle = "GATHER NOTES • AVOID ENEMIES • HIT SPECIAL PEGS",
        body = "Glide smoothly with your finger to intercept rhythmic notes, dodge hazards, and trigger electrifying stage transformations!",
        tip = "Hit steady beat notes to unlock new songs and artist vault secrets!",
        buttonText = "LET'S PLAY ♪",
        accentColor = Color(0xFFFACC15L)
    ),
    BEAT_CHAIN(
        prefKey = "seen_discovery_beat_chain_v2",
        badge = "NEW MECHANIC • RHYTHM PHRASE",
        title = "NUMBERED BEAT CHAINS (1 → 2 → 3 → 4)",
        subtitle = "HIT BEATS IN ORDER AS THE OUTER RING LOCKS IN",
        body = "Stationary Beat Orbs spawn along a curved silk stream in musical 4-beat phrases (1 → 2 → 3 → 4). Glide into #1 right as its shrinking outer ring reaches the orb for PERFECT! (+250) timing!",
        tip = "CASUAL-FRIENDLY: Missing a beat's timer only resets your Combo multiplier—it NEVER damages your health or ends your run.",
        buttonText = "GOT IT — RESUME TRACK",
        accentColor = Color(0xFF38BDF8L)
    ),
    SUPERNOVA_ORB(
        prefKey = "seen_discovery_supernova_v2",
        badge = "NEW MECHANIC • VOCAL DROP",
        title = "24K GOLD LYRIC SUPERNOVA ORB",
        subtitle = "STAGE PHENOMENON & 2X RHYTHM RUSH",
        body = "On signature vocal drops, a blazing 24K Gold Supernova Orb appears! Collecting it triggers a full-stage visual phenomenon, vaporizes nearby hazards, and activates 2x Constellation Rush.",
        tip = "PRO TIP: Follow your hero's golden compass arc whenever a Supernova Beat materializes!",
        buttonText = "GOT IT — RESUME TRACK",
        accentColor = Color(0xFFFACC15L)
    ),
    SLIDE_STREAM(
        prefKey = "seen_discovery_slide_stream_v2",
        badge = "NEW MECHANIC • MUSICAL HOLD SLIDE",
        title = "SLIDE-STREAM WORMHOLE RIBBON",
        subtitle = "SLIDE ♪ ENTRY → 5 RHYTHM PEARLS → EXIT ★",
        body = "Fly into the glowing SLIDE ♪ portal to automatically surf the curved musical light-ribbon across space! You are 100% invulnerable while sliding and collect all 5 rhythm pearls (+500 bonus) on the beat.",
        tip = "RHYTHM FLOW: Slide streams carry you safely past hazards straight into the next musical phrase!",
        buttonText = "GOT IT — RESUME TRACK",
        accentColor = Color(0xFFE879F9L)
    ),
    HAZARD_SHIELD(
        prefKey = "seen_discovery_hazard_shield_v2",
        badge = "NEW MECHANIC • ARENA AWARENESS",
        title = "PERIMETER BEACONS & VOID HAZARDS",
        subtitle = "DODGE RED HAZARDS • CHARGE YOUR SHIELD",
        body = "Pulsing red beacons on the screen edge warn you 2 beats before a Void Hazard enters the arena. Dodge red hazards while hitting beats to spawn Shield, Slow-Mo, and Overdrive Blast orbs!",
        tip = "NEED MORE BREATHING ROOM? You can switch between Chill, Normal, and Intense anytime on the Main Menu.",
        buttonText = "GOT IT — RESUME TRACK",
        accentColor = Color(0xFFFB7185L)
    )
}

fun hasSeenDiscovery(prefs: SharedPreferences, item: FirstTimeDiscovery): Boolean {
    return prefs.getBoolean(item.prefKey, false)
}

fun markDiscoverySeen(prefs: SharedPreferences, item: FirstTimeDiscovery) {
    prefs.edit().putBoolean(item.prefKey, true).apply()
}

data class ArtistExclusiveItem(
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val unlockRequirementText: String,
    val artistNoteQuote: String,
    val exclusiveDetail: String,
    val accentColor: Color,
    val isUnlocked: (SharedPreferences, Int, Int) -> Boolean
)

val ARTIST_EXCLUSIVES_CATALOG = listOf(
    ArtistExclusiveItem(
        id = "excl_confide_master",
        category = "STUDIO LINER NOTES • NO MORE ILLUSIONS",
        title = "ETHAN CORREA • ALBUM REFLECTIONS",
        subtitle = "Behind The Music & Vocal Production",
        unlockRequirementText = "Unlocked Free for VIP Fan Community",
        artistNoteQuote = "\"No More Illusions is about cutting through digital static and finding authentic connection. 'Confide' was born during late-night studio sessions exploring vulnerability, while 'Number One' captures high-energy rhythmic syncopation. Thank you for building this interactive world with us.\"",
        exclusiveDetail = "Unlocks Ethan's full interactive gatefold sleeve notes & behind-the-scenes vocal reflections.",
        accentColor = Color(0xFF38BDF8L),
        isUnlocked = { _, _, _ -> true }
    ),
    ArtistExclusiveItem(
        id = "excl_number_one_master",
        category = "BACKSTAGE MASTER REEL • TRACK 02",
        title = "'NUMBER ONE' CADENCE & STEM REEL",
        subtitle = "Open Hi-Hat Groove & Electronic Energy",
        unlockRequirementText = "Play Track 02 or reach 2,500+ High Score",
        artistNoteQuote = "\"'Number One' was designed around syncopated open hi-hat patterns and layered vocal harmonies. As you hit each beat in the drop, you are feeling the raw rhythm track directly from the master desk.\"",
        exclusiveDetail = "Unlocks the 4K Holographic Vinyl star on Track 02 + 250 ♪ Bonus Notes.",
        accentColor = Color(0xFF4ADE80L),
        isUnlocked = { prefs, highScore, _ ->
            prefs.getBoolean("art_unlocked_2", false) || highScore >= 2500 || prefs.getBoolean("vip_all_unlocked", false)
        }
    ),
    ArtistExclusiveItem(
        id = "excl_upcoming_teaser",
        category = "UPCOMING STUDIO RELEASES",
        title = "MORE ARTIST EXCLUSIVES COMING SOON",
        subtitle = "Upcoming Singles & Studio Stem Packs",
        unlockRequirementText = "Master current tracks to qualify for upcoming release drops",
        artistNoteQuote = "\"Ethan Correa is currently in the studio recording upcoming singles, alternate acoustic versions, and exclusive stem packs. Level up your album mastery to unlock new tracks and VIP rewards upon release.\"",
        exclusiveDetail = "Upcoming singles and backstage rehearsal footage will unlock here in future studio updates.",
        accentColor = Color(0xFFFACC15L),
        isUnlocked = { prefs, highScore, _ ->
            prefs.getBoolean("vip_all_unlocked", false) || highScore >= 4500
        }
    )
)

fun countUnlockedExclusives(prefs: SharedPreferences, highScore: Int, points: Int): Int {
    return ARTIST_EXCLUSIVES_CATALOG.count { it.isUnlocked(prefs, highScore, points) }
}

@Composable
fun FirstTimeDiscoveryModal(
    discovery: FirstTimeDiscovery,
    onDismiss: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "discoveryAnim")
    val animPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing)
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE602050CL))
            .clickable(enabled = true) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 350.dp)
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0B152CL), Color(0xFF050914L))
                    )
                )
                .border(2.dp, discovery.accentColor.copy(alpha = 0.88f), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Top Pill Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(discovery.accentColor.copy(alpha = 0.16f))
                    .border(1.dp, discovery.accentColor.copy(alpha = 0.65f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = discovery.badge,
                    color = discovery.accentColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.9.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = discovery.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = discovery.subtitle,
                color = discovery.accentColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth Animated Visual Spotlight Canvas (Zero straight lines!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF040812L))
                    .border(1.dp, discovery.accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    val pulse = 0.5f + 0.5f * sin(animPhase * 2f)

                    // Soft radial aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(discovery.accentColor.copy(alpha = 0.28f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = size.height * 0.72f
                        ),
                        radius = size.height * 0.72f,
                        center = Offset(cx, cy)
                    )

                    when (discovery) {
                        FirstTimeDiscovery.BEAT_CHAIN -> {
                            val x1 = size.width * 0.24f
                            val x2 = size.width * 0.50f
                            val x3 = size.width * 0.76f
                            val y1 = cy + 10f
                            val y2 = cy - 14f
                            val y3 = cy + 8f
                            val stream = Path().apply {
                                moveTo(x1, y1)
                                quadraticTo((x1 + x2) * 0.5f, cy - 28f, x2, y2)
                                quadraticTo((x2 + x3) * 0.5f, cy + 22f, x3, y3)
                            }
                            drawPath(
                                path = stream,
                                color = Color(0xFF38BDF8L).copy(alpha = 0.45f),
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )
                            val shrinkR = 20f * (1.05f + (1f - (animPhase / 6.283185f)) * 0.9f)
                            drawCircle(
                                color = Color(0xFF4ADE80L),
                                radius = shrinkR,
                                center = Offset(x1, y1),
                                style = Stroke(width = 2.6f)
                            )
                            listOf(Offset(x1, y1), Offset(x2, y2), Offset(x3, y3)).forEachIndexed { idx, pt ->
                                drawCircle(
                                    color = if (idx == 0) Color(0xFF38BDF8L) else Color(0xFF0E1D3AL),
                                    radius = 16f,
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 16f,
                                    center = pt,
                                    style = Stroke(width = 2f)
                                )
                            }
                        }
                        FirstTimeDiscovery.SLIDE_STREAM -> {
                            val ax = size.width * 0.22f
                            val bx = size.width * 0.78f
                            val ribbon = Path().apply {
                                moveTo(ax, cy + 12f)
                                quadraticTo(cx, cy - 38f, bx, cy + 12f)
                            }
                            drawPath(
                                path = ribbon,
                                color = Color(0xFFE879F9L).copy(alpha = 0.35f),
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = ribbon,
                                color = Color.White.copy(alpha = 0.88f),
                                style = Stroke(width = 3f, cap = StrokeCap.Round)
                            )
                            for (i in 1..4) {
                                val t = i / 5f
                                val inv = 1f - t
                                val px = inv * inv * ax + 2f * inv * t * cx + t * t * bx
                                val py = inv * inv * (cy + 12f) + 2f * inv * t * (cy - 38f) + t * t * (cy + 12f)
                                drawCircle(color = Color(0xFFFACC15L), radius = 5.5f, center = Offset(px, py))
                            }
                            drawCircle(color = Color(0xFF38BDF8L), radius = 18f, center = Offset(ax, cy + 12f), style = Stroke(width = 3f))
                            drawCircle(color = Color(0xFFE879F9L), radius = 18f, center = Offset(bx, cy + 12f), style = Stroke(width = 3f))
                        }
                        else -> {
                            for (ring in 0..2) {
                                val r = 24f + ring * 13f
                                val startDeg = ((animPhase * 57.3f * (if (ring % 2 == 0) 1f else -1f)) + ring * 60f) % 360f
                                drawArc(
                                    color = discovery.accentColor.copy(alpha = 0.85f - ring * 0.2f),
                                    startAngle = startDeg,
                                    sweepAngle = 120f,
                                    useCenter = false,
                                    topLeft = Offset(cx - r, cy - r),
                                    size = Size(r * 2f, r * 2f),
                                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                                )
                            }
                            drawCircle(
                                color = discovery.accentColor,
                                radius = 14f + pulse * 3f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 6.5f,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (discovery == FirstTimeDiscovery.FAN_WELCOME) {
                // Visual-First 3-Card Guide (Zero Reading Overhead!)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Card 1: Gather Notes
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070E1DL))
                            .border(1.2.dp, Color(0xFFFACC15L).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "🎵", fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "GATHER",
                            color = Color(0xFFFACC15L),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Beat Notes",
                            color = Color(0xFF94A3B8L),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Card 2: Avoid Enemies
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070E1DL))
                            .border(1.2.dp, Color(0xFFF43F5EL).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "👾", fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "AVOID",
                            color = Color(0xFFF43F5EL),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Enemies",
                            color = Color(0xFF94A3B8L),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Card 3: Special Pegs
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070E1DL))
                            .border(1.2.dp, Color(0xFF38BDF8L).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "⚡", fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SPECIAL",
                            color = Color(0xFF38BDF8L),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Power Pegs",
                            color = Color(0xFF94A3B8L),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Unlock tracks & artist vault secrets as you play!",
                    color = Color(0xFFCBD5E1L),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = discovery.body,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF071124L))
                        .border(1.dp, discovery.accentColor.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = discovery.tip,
                        color = discovery.accentColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(discovery.accentColor, Color(0xFF38BDF8L))
                        )
                    )
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (discovery == FirstTimeDiscovery.FAN_WELCOME) "LET'S PLAY ♪" else discovery.buttonText,
                    color = Color(0xFF020617L),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SHOWN ONCE • AUTO-SAVED TO YOUR PROFILE",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}
