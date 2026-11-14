package com.oneshelfstudio.nomoreillusions

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
        prefKey = "seen_discovery_fan_welcome_v3",
        badge = "STUDIO STAGE BRIEFING",
        title = "RHYTHM & STAGE FX",
        subtitle = "FLUID GLIDE • 24K SPECTACLES • LIVE FX DROP BOARD",
        body = "Glide freely across the stage to catch beat-synced orbs, dodge red void hazards, and tap the left Stage FX Board to drop custom visual FX orbs!",
        tip = "Touch with a second finger anytime to weave a curved Dual-Hero plasma ribbon!",
        buttonText = "DROP THE NEEDLE ▶",
        accentColor = Color(0xFFFACC15L)
    ),
    BEAT_CHAIN(
        prefKey = "seen_discovery_beat_chain_v2",
        badge = "RHYTHM FLOW • BEAT ORBS",
        title = "MULTI-DIRECTIONAL BEAT ORBS",
        subtitle = "CATCH ORBS ON THE DRUM GROOVE • UNVEIL THE ALBUM",
        body = "Luminous Beat Orbs drift in from every angle and pulse to the live drum beat. Glide smoothly through them to build your combo multiplier and progressively unveil the official Collector's Edition vinyl.",
        tip = "FLOW STATE: Orbs phase smoothly past one another—focus on riding the musical groove!",
        buttonText = "RESUME TRACK ▶",
        accentColor = Color(0xFF38BDF8L)
    ),
    SUPERNOVA_ORB(
        prefKey = "seen_discovery_supernova_v2",
        badge = "STAGE PHENOMENON • 24K RELIC",
        title = "24K GOLD SPECTACLE ORBS",
        subtitle = "TOP-CENTER MILESTONES & 24K SUPERNOVA METER",
        body = "At signature musical transitions (0:15, 1:18, and key riffs), a radiant 24K Gold Milestone Orb floats into the upper-center stage. Catching it unleashes golden solar ripples, vaporizes nearby hazards, and ignites 24K Overdrive!",
        tip = "TAP THE TOP-CENTER ★ ENABLE 24K MODE ★ BUTTON when charged for a 24K Overdrive burst!",
        buttonText = "RESUME TRACK ▶",
        accentColor = Color(0xFFFACC15L)
    ),
    SLIDE_STREAM(
        prefKey = "seen_discovery_stage_fx_board_v3",
        badge = "LIVE STAGE FX • FLUID DROP",
        title = "FLUID STAGE FX ORBS",
        subtitle = "ORGANIC DROPS • CATCH THE ORB • TRANSFORM THE SKY",
        body = "Luminous fluid Stage FX Orbs drift organically onto the stage from your unlocked collection! Catch one with your cursor to ripple waves across the arena and transform the background visualizer.",
        tip = "UNLOCK 6 STAGE FX ORBS in the Studio Shop using ♪ Notes or track milestones!",
        buttonText = "RESUME TRACK ▶",
        accentColor = Color(0xFFE879F9L)
    ),
    HAZARD_SHIELD(
        prefKey = "seen_discovery_hazard_shield_v2",
        badge = "ARENA AWARENESS • PROTECTION",
        title = "VOID HAZARDS & AEGIS RELICS",
        subtitle = "DODGE CRIMSON PULSARS • CATCH SHIELD & SLOW-MO ORBS",
        body = "Soft perimeter beacons warn you before crimson Void Hazards enter the stage. Weave around hazards while catching Prism, Aegis Shield, and Vortex Slow-Mo relics to protect your combo streak.",
        tip = "CUSTOM PACE: Adjust Chill, Regular, or Hard difficulty anytime in Studio Settings.",
        buttonText = "RESUME TRACK ▶",
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
        category = "OFFICIAL RELEASE • TRACK 01",
        title = "ETHAN CORREA • 'CONFIDE'",
        subtitle = "Emerald Obsidian Collector's Pressing",
        unlockRequirementText = "Unlocked Free for Fan Community",
        artistNoteQuote = "\"Confide\" — Official studio single from No Morē Illusīons by Ethan Correa. Master the rhythm stage to unlock full-track listening and Collector's Edition vinyl styling.",
        exclusiveDetail = "Unlocks unlimited full-track listening + Starfall Warp Stage FX Orb upon clearing Track 01.",
        accentColor = Color(0xFF38BDF8L),
        isUnlocked = { _, _, _ -> true }
    ),
    ArtistExclusiveItem(
        id = "excl_number_one_master",
        category = "OFFICIAL RELEASE • TRACK 02",
        title = "ETHAN CORREA • 'NUMBER ONE'",
        subtitle = "24K Solar Gold Collector's Pressing",
        unlockRequirementText = "Clear Track 02 or reach 2,500+ High Score",
        artistNoteQuote = "\"Number One\" — Official studio single from No Morē Illusīons by Ethan Correa. Connect with @EthanCorrea and flex your high scores with #NoMoreIllusions.",
        exclusiveDetail = "Unlocks unlimited full-track listening + 24K Eclipse Stage FX Orb upon clearing Track 02.",
        accentColor = Color(0xFF4ADE80L),
        isUnlocked = { prefs, highScore, _ ->
            prefs.getBoolean("art_unlocked_2", false) || highScore >= 2500 || prefs.getBoolean("vip_all_unlocked", false)
        }
    ),
    ArtistExclusiveItem(
        id = "excl_upcoming_teaser",
        category = "FUTURE ARTIST EXCLUSIVES",
        title = "UPCOMING RELEASES & EXCLUSIVES",
        subtitle = "New Ethan Correa Music & Fan Perks Coming Soon",
        unlockRequirementText = "Build your ♪ Notes bank & high score ahead of future drops",
        artistNoteQuote = "New Ethan Correa releases and special artist exclusives are on the horizon. Keep stacking ♪ Notes and unlocking Stage FX Orbs so you're ready on day one!",
        exclusiveDetail = "Early-supporter status & instant readiness as Ethan drops new singles into the Album deck.",
        accentColor = Color(0xFFFACC15L),
        isUnlocked = { prefs, highScore, _ ->
            prefs.getBoolean("vip_all_unlocked", false) || highScore >= 4000
        }
    )
)

fun countUnlockedExclusives(prefs: SharedPreferences, highScore: Int, points: Int): Int {
    return ARTIST_EXCLUSIVES_CATALOG.count { it.isUnlocked(prefs, highScore, points) }
}

@Composable
fun LiveGuideEmblemCanvas(
    emblemKind: Int,
    animPhase: Float,
    accentColor: Color,
    modifier: Modifier = Modifier.size(48.dp)
) {
    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.42f
        val pulse = 0.5f + 0.5f * sin(animPhase * 2f)

        // Ambient radial bloom
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.34f), Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 1.35f
            ),
            radius = r * 1.35f,
            center = Offset(cx, cy)
        )

        when (emblemKind) {
            0 -> {
                // Touch & Dual-Glide Silk Orbit
                val orbitX = cx + cos(animPhase) * (r * 0.46f)
                val orbitY = cy + sin(animPhase * 2f) * (r * 0.28f)
                val trailPath = Path().apply {
                    moveTo(cx - r * 0.55f, cy + r * 0.18f)
                    quadraticTo(cx, cy - r * 0.48f, orbitX, orbitY)
                }
                drawPath(
                    path = trailPath,
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0xFF38BDF8), Color(0xFF4ADE80))
                    ),
                    style = Stroke(width = 4.2f, cap = StrokeCap.Round)
                )
                drawCircle(
                    color = Color(0xFF4ADE80).copy(alpha = 0.38f),
                    radius = r * (0.42f + 0.10f * pulse),
                    center = Offset(orbitX, orbitY)
                )
                drawCircle(
                    color = Color.White,
                    radius = r * 0.22f,
                    center = Offset(orbitX, orbitY)
                )
            }
            1 -> {
                // Beat Orb + Velvet Water-Drop Ripple
                val rippleFrac = (animPhase / 6.283185f)
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = (1f - rippleFrac) * 0.65f),
                    radius = r * (0.45f + rippleFrac * 0.65f),
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.4f * (1f - rippleFrac * 0.5f))
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFF4ADE80), Color(0xFF0284C7)),
                        center = Offset(cx - r * 0.12f, cy - r * 0.12f),
                        radius = r * 0.55f
                    ),
                    radius = r * (0.46f + 0.08f * pulse),
                    center = Offset(cx, cy)
                )
            }
            2 -> {
                // 24K Solar Lotus Relic
                val petalCount = 6
                val rot = animPhase * 0.5f
                val lotus = Path()
                for (i in 0 until petalCount) {
                    val a0 = rot + i * (6.283185f / petalCount)
                    val aMid = a0 + (3.14159f / petalCount)
                    val a1 = a0 + (6.283185f / petalCount)
                    val p0 = Offset(cx + cos(a0) * r * 0.42f, cy + sin(a0) * r * 0.42f)
                    val ctrl = Offset(cx + cos(aMid) * r * 0.95f, cy + sin(aMid) * r * 0.95f)
                    val p1 = Offset(cx + cos(a1) * r * 0.42f, cy + sin(a1) * r * 0.42f)
                    if (i == 0) lotus.moveTo(p0.x, p0.y)
                    lotus.quadraticTo(ctrl.x, ctrl.y, p1.x, p1.y)
                }
                lotus.close()
                drawPath(
                    path = lotus,
                    color = Color(0xFFFDE68A).copy(alpha = 0.85f),
                    style = Stroke(width = 2.2f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFFACC15), Color(0xFFD97706)),
                        center = Offset(cx, cy),
                        radius = r * 0.48f
                    ),
                    radius = r * (0.42f + 0.07f * pulse),
                    center = Offset(cx, cy)
                )
            }
            else -> {
                // VHS Chromatic Montage Lens (5/5)
                val shift = 2.2f + 1.4f * pulse
                drawCircle(
                    color = Color(0xFF22D3EE).copy(alpha = 0.78f),
                    radius = r * 0.72f,
                    center = Offset(cx - shift, cy),
                    style = Stroke(width = 2.2f)
                )
                drawCircle(
                    color = Color(0xFFE879F9).copy(alpha = 0.78f),
                    radius = r * 0.72f,
                    center = Offset(cx + shift, cy),
                    style = Stroke(width = 2.2f)
                )
                for (i in 0 until 5) {
                    val a = animPhase * 0.8f + i * (6.283185f / 5f)
                    val hx = cx + cos(a) * (r * 0.40f)
                    val hy = cy + sin(a) * (r * 0.40f)
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFFDE68A) else Color.White,
                        radius = r * 0.11f,
                        center = Offset(hx, hy)
                    )
                }
            }
        }
    }
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

    StudioDialog(
        title = discovery.title,
        onDismiss = onDismiss,
        accentColor = discovery.accentColor
    ) {
        // Top Pill Badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(discovery.accentColor.copy(alpha = 0.16f))
                .border(1.dp, discovery.accentColor.copy(alpha = 0.70f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = discovery.badge,
                color = discovery.accentColor,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.0.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = discovery.subtitle,
            color = Color(0xFFCBD5E1L),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.7.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (discovery == FirstTimeDiscovery.FAN_WELCOME) {
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DiscoveryFeatureCard(
                    emblemKind = 1,
                    animPhase = animPhase,
                    accent = Color(0xFF4ADE80L),
                    title = "BEAT-SYNCED ORBS",
                    desc = "Glide freely to catch multi-directional notes on the drum beat & unveil album art."
                )
                DiscoveryFeatureCard(
                    emblemKind = 2,
                    animPhase = animPhase,
                    accent = Color(0xFFFACC15L),
                    title = "24K GOLD MILESTONES",
                    desc = "Catch upper-center 24K Orbs (0:15 & 1:18) to ignite solar ripples & wipe hazards."
                )
                DiscoveryFeatureCard(
                    emblemKind = 3,
                    animPhase = animPhase,
                    accent = Color(0xFFE879F9L),
                    title = "LIVE STAGE FX DROP BOARD",
                    desc = "Tap the left FX Board in-game to drop Special FX Orbs that transform the sky!"
                )
            }
        } else {
            val emblemKind = when (discovery) {
                FirstTimeDiscovery.BEAT_CHAIN -> 1
                FirstTimeDiscovery.SUPERNOVA_ORB -> 2
                FirstTimeDiscovery.SLIDE_STREAM -> 3
                FirstTimeDiscovery.HAZARD_SHIELD -> 0
                else -> 1
            }

            StudioGlassCard(
                accentColor = discovery.accentColor,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    LiveGuideEmblemCanvas(
                        emblemKind = emblemKind,
                        animPhase = animPhase,
                        accentColor = discovery.accentColor,
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = discovery.body,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Studio Tip Ribbon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF071124L))
                .border(1.dp, discovery.accentColor.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = discovery.tip,
                color = discovery.accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val pillPalette = when (discovery) {
            FirstTimeDiscovery.FAN_WELCOME, FirstTimeDiscovery.SUPERNOVA_ORB -> StudioPillPalette.SOLAR_GOLD
            FirstTimeDiscovery.SLIDE_STREAM -> StudioPillPalette.VELVET_AMETHYST
            FirstTimeDiscovery.HAZARD_SHIELD -> StudioPillPalette.COSMIC_ROSE
            else -> StudioPillPalette.EMERALD_STAGE
        }

        StudioPillButton(
            text = discovery.buttonText,
            palette = pillPalette,
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun DiscoveryFeatureCard(
    emblemKind: Int,
    animPhase: Float,
    accent: Color,
    title: String,
    desc: String
) {
    StudioGlassCard(
        accentColor = accent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF040915L))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                LiveGuideEmblemCanvas(
                    emblemKind = emblemKind,
                    animPhase = animPhase,
                    accentColor = accent,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.7.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = Color(0xFFE2E8F0L),
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
