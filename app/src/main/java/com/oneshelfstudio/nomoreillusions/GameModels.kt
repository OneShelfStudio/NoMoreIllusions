package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.graphics.Color
import kotlin.math.PI

enum class GameState { MENU, COUNTDOWN, PLAYING, PAUSED, GAME_OVER, HOW_TO_PLAY, SETTINGS, MUSIC_DEMO, LEVEL_COMPLETE }
enum class EnemyType { PULSE, SEEKER, WANDERER, PHANTOM, LASER_SWEEPER }
enum class PowerUpType { SHIELD, BLAST, MULTIPLIER }
enum class ColorBlindMode { NONE, PROTANOPIA, DEUTERANOPIA, TRITANOPIA }
enum class GameDifficulty { EASY, REGULAR, HARD }

/**
 * Stage modes driven by real musical swells & song sections.
 */
enum class StageMode {
    ABYSSAL_WAVE,      // Calm bioluminescent aurora drift + gentle neural starfield
    SPEED_PORTAL,      // Rising musical swell: faster aurora currents + Kerr spiral galaxy vortex
    MAGIC_LANES,       // Vocal/rap cadence: shimmering synaptic harmonic web
    OVERDRIVE_SHATTER, // Climax breakdown: high-energy aurora + twin spiral galaxies
    GOLDEN_OVERDRIVE   // 24K Constellation Starlight Bloom during major chorus/drop swells
}

/**
 * Bespoke Orbital Drift Cosmic Phenomena triggered ONLY when collecting a Special Supernova Beat Orb!
 * Regular "Flow Beat Orbs" use [NONE] and quietly tether into orbit around the hero.
 */
enum class KeyWordEffect {
    NONE,               // Regular Flow Beat Orb: quiet collection, tethers a satellite into hero's orbit
    TRACTOR_REACH,      // Neural synaptic tractor filaments reel in all nearby pegs
    PRISM_SHATTER,      // Celestial Crystal Prism + 3 rainbow dispersion plumes deflect foes
    MAGNETAR_WAR,       // Magnetar 3D figure-8 dipole loops + polar jets vaporize foes
    CONSTELLATION_STAR, // 24K Golden Constellation locks in the sky + 4x multiplier
    BINARY_HEART,       // Twin ruby-and-gold binary stars + lub-dub harmonic pulse
    ECLIPSE_DARK        // Velvet obsidian eclipse + blazing Solar Corona diamond shield
}

/**
 * Real-time 4Hz (250ms) Multi-Band Spectral State extracted from Ethan's master audio stems:
 * - [bass] (25–160 Hz): Sub-bass & kick drum weight (drives gravity rings, camera thump, enemy hazard rings)
 * - [vocal] (220–3200 Hz): Ethan's vocal & harmonic presence (drives Aurora Borealis silk ribbons & hero bloom)
 * - [high] (3200–12000 Hz): Cymbal/synth shimmer & chorus brilliance (drives Kerr spiral galaxy & constellations)
 * - [bassDropOut]: True when sub-bass cuts out (< 0.14f) during vocal breakdowns or syncopated stops
 */
data class MultiBandAudioState(
    val rms: Float = 0.42f,
    val bass: Float = 0.35f,
    val vocal: Float = 0.40f,
    val high: Float = 0.25f,
    val bassDropOut: Boolean = false,
    val swellDelta: Float = 0f
)

/**
 * Selectable original studio lobby music tracks (FluidSynth + pedalboard mastered, non-artist audio).
 */
enum class LobbyTrack(val id: String, val title: String, val subtitle: String, val rawResName: String, val bpm: Float) {
    VELVET("VELVET", "Velvet Abyss", "Dark Synth Pad • 90 BPM", "lobby_velvet", 90f),
    CRYSTAL("CRYSTAL", "Crystal Mirage", "Glass Vibraphone • 100 BPM", "lobby_crystal", 100f),
    MIDNIGHT("MIDNIGHT", "Midnight Lo-Fi", "Warm Rhodes • 84 BPM", "lobby_midnight", 84f);

    companion object {
        fun fromId(id: String?): LobbyTrack = entries.firstOrNull { it.id == id } ?: VELVET
    }
}

/**
 * 4 High-Replayability Remix & Challenge Modes per Song (turning 2 songs into 8+ distinct arcade runs).
 */
enum class TrackRemixMode(
    val id: String,
    val displayName: String,
    val title: String,
    val shortTag: String,
    val subtitle: String,
    val audioSpeed: Float,
    val audioPitch: Float,
    val noteMultiplier: Float
) {
    ORIGINAL("ORIGINAL", "NORMAL", "Normal Speed", "1.0x", "Standard Tempo • 1x ♪ Notes", 1.0f, 1.0f, 1.0f),
    OVERCLOCK("OVERCLOCK", "FAST", "Fast Mode", "1.2x", "Faster Music & Gameplay • 2x ♪ Notes", 1.20f, 1.12f, 2.0f),
    SLOWED("SLOWED", "CHILL", "Chill Mode", "0.85x", "Slowed & Pitched Down • 1.5x ♪ Notes", 0.85f, 0.86f, 1.5f),
    ENDLESS("ENDLESS", "ENDLESS", "Endless Loop", "∞ LOOP", "Non-Stop Rising Challenge • 2.5x ♪ Notes", 1.0f, 1.0f, 2.5f);

    companion object {
        fun fromId(id: String?): TrackRemixMode = entries.firstOrNull { it.id == id } ?: ORIGINAL
    }
}

enum class AnomalyKind { WORMHOLE_VORTEX, PHASE_BARRIER }

/**
 * Fading-in Singularity Wormhole or Rhythmic Phase Barrier that materializes on musical swells,
 * warps enemy trajectories, and creates dynamic spatial obstacles to weave around.
 */
data class SingularityWormhole(
    var x: Float,
    var y: Float,
    val radius: Float = 52f,
    val kind: AnomalyKind = AnomalyKind.WORMHOLE_VORTEX,
    var angle: Float = 0f,
    val length: Float = 135f,
    var ageSec: Float = 0f,
    val fadeInSec: Float = 1.45f,
    val activeSec: Float = 4.6f,
    val fadeOutSec: Float = 1.15f,
    val color: Color = Color(0xFFA855F7L)
) {
    val totalDuration: Float get() = fadeInSec + activeSec + fadeOutSec
    val isHazardActive: Boolean get() = ageSec in fadeInSec..(fadeInSec + activeSec)
    val isWarningPhase: Boolean get() = ageSec < fadeInSec
    val alphaProgress: Float
        get() = when {
            ageSec < fadeInSec -> (ageSec / fadeInSec).coerceIn(0f, 1f)
            ageSec <= fadeInSec + activeSec -> 1f
            else -> (1f - (ageSec - fadeInSec - activeSec) / fadeOutSec).coerceIn(0f, 1f)
        }
}

data class GameObject(
    var x: Float,
    var y: Float,
    val radius: Float,
    var color: Color,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var life: Float = 1.0f,
    val type: EnemyType = EnemyType.PULSE,
    var angle: Float = 0f,
    val powerUpType: PowerUpType? = null,
    var phase: Float = 0f,
    var orbitRadius: Float = 12f,
    var speed: Float = 3.5f,
    // Beat & Supernova Orb properties
    var anchorX: Float = 0f,
    var anchorY: Float = 0f,
    var spawnBeatSec: Float = 0f,
    var targetBeatSec: Float = 0f,
    var lyricCallout: String = "",
    var sequenceIndex: Int = 0,
    var isGoldenOrb: Boolean = false,
    var keyWordEffect: KeyWordEffect = KeyWordEffect.NONE,
    var hasPassedPlayer: Boolean = false
)

/**
 * Beat-locked perimeter warning beacon that telegraphs an incoming enemy for 2 beats (~0.95s)
 * with a pulsing Crimson Hazard Diamond + Laser Trajectory Sightline before the enemy enters the arena!
 */
data class EnemySpawnTelegraph(
    val edgeX: Float,
    val edgeY: Float,
    val entryX: Float,
    val entryY: Float,
    val targetAngle: Float,
    var timer: Float,
    val maxTimer: Float = 0.92f,
    val spawnData: EnemySpawnData,
    val finalSpeed: Float
)

/**
 * Living starlight satellite orbiting the hero (inspired by Orbital Drift's tethered orb ring).
 */
data class TetheredSatellite(
    var orbitAngle: Float,
    var orbitRadius: Float,
    val orbitSpeed: Float,
    val color: Color,
    val size: Float = 4.8f,
    var x: Float = 0f,
    var y: Float = 0f
)

/**
 * Tracks the currently active Orbital Drift Phenomenon when a player hits a Special Supernova Beat Orb.
 */
data class ActivePhenomenon(
    val effect: KeyWordEffect,
    var timer: Float,
    val maxDuration: Float,
    val originX: Float,
    val originY: Float,
    val constellationIndex: Int = 0,
    val keywordLabel: String = ""
)

data class BeatFlamePillar(
    val x: Float,
    val y: Float,
    val width: Float = 96f,
    val color: Color = Color(0xFFFACC15L),
    val isGolden: Boolean = false,
    var life: Float = 1.0f,
    var starburstRadius: Float = 24f,
    val maxStarburstRadius: Float = 460f
)

data class EnemySpawnData(
    val type: EnemyType,
    val baseColor: Color,
    val speed: Float,
    val orbitRadius: Float
)

data class FloatingText(
    val x: Float,
    val y: Float,
    val text: String,
    var life: Float = 1f,
    val color: Color = Color.White,
    val isLarge: Boolean = false,
    val subtitle: String = ""
)

fun setCenterTopCallout(
    list: MutableList<FloatingText>,
    screenWidth: Float,
    screenHeight: Float,
    text: String,
    color: Color = Color.White,
    isLarge: Boolean = true,
    subtitle: String = ""
) {
    list.clear()
    list.add(
        FloatingText(
            x = screenWidth / 2f,
            y = screenHeight * 0.205f,
            text = text,
            life = 1.25f,
            color = color,
            isLarge = isLarge,
            subtitle = subtitle
        )
    )
}

data class Shockwave(
    val x: Float,
    val y: Float,
    var radius: Float = 0f,
    val maxRadius: Float = 150f,
    val color: Color = Color.White,
    var life: Float = 1f
)

data class ThemeColors(
    val c1: Color,
    val c2: Color,
    val c3: Color,
    val nebTint: Color,
    val starTint: Color
)

fun normalizeAngle(angle: Float): Float {
    var a = angle
    while (a <= -PI.toFloat()) a += 2 * PI.toFloat()
    while (a > PI.toFloat()) a -= 2 * PI.toFloat()
    return a
}

fun Color.toProtanopia(): Color {
    val r = red * 0.567f + green * 0.433f
    val g = red * 0.558f + green * 0.442f
    val b = green * 0.242f + blue * 0.758f
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), alpha)
}

fun Color.toDeuteranopia(): Color {
    val r = red * 0.625f + green * 0.375f
    val g = red * 0.7f + green * 0.3f
    val b = green * 0.3f + blue * 0.7f
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), alpha)
}

fun Color.toTritanopia(): Color {
    val r = red * 0.95f + green * 0.05f
    val g = green * 0.433f + blue * 0.567f
    val b = green * 0.475f + blue * 0.525f
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), alpha)
}

fun parseCompletionsCsv(csv: String): IntArray {
    val out = IntArray(24) { 0 }
    if (csv.isBlank()) return out
    val parts = csv.split(',')
    for (i in 0 until minOf(24, parts.size)) {
        out[i] = parts[i].trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
    }
    return out
}

fun toCompletionsCsv(arr: IntArray): String = arr.joinToString(",") { it.coerceAtLeast(0).toString() }
