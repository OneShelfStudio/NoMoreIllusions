package com.oneshelfstudio.nomoreillusions

import android.content.Context
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class JuiceTier {
    NONE,
    SECTION_SHIFT,
    SLIGHT_EXTRA_JUICE,
    EXTRA_JUICE
}

data class TrackSection(
    val timestampSec: Float,
    val label: String,
    val juiceTier: JuiceTier = JuiceTier.SECTION_SHIFT,
    val stageMode: StageMode = StageMode.ABYSSAL_WAVE,
    val isGoldenOverdrive: Boolean = false,
    val maxEnemies: Int = 4,
    val enemySpeedMult: Float = 1.0f,
    val clearEnemiesOnEnter: Boolean = false,
    val allowAnomalies: Boolean = false
)

/**
 * Sample-accurate beat-quantized orb cue.
 * - Regular Beat Orbs have [keyWordEffect] == KeyWordEffect.NONE (Cyan/Emerald harmonic spheres).
 * - Special Supernova Beat-Drop Orbs have [keyWordEffect] != NONE (2x larger Multi-Ringed Golden/Prismatic
 *   Gyroscope cores with 8 solar flare spikes — zero text on screen!).
 */
data class BeatOrbCue(
    val sequenceIndex: Int,
    val timestampSec: Float,
    val normX: Float,
    val normY: Float,
    val powerUpType: PowerUpType,
    val lyricOrBeatCallout: String = "",
    val isGoldenCue: Boolean = false,
    val keyWordEffect: KeyWordEffect = KeyWordEffect.NONE
)

private data class KeyWordAnchor(
    val timestampSec: Float,
    val effect: KeyWordEffect
)

data class ArtistSocialLink(
    val label: String,
    val badge: String,
    val url: String,
    val accentHex: Long
)

data class DemoAlbum(
    val albumId: String,
    val artistName: String,
    val albumTitle: String,
    val tracks: List<DemoTrack>,
    val socialLinks: List<ArtistSocialLink>
)

data class DemoTrack(
    val index: Int,
    val title: String,
    val bpm: Float,
    val durationSec: Int,
    val unlockCost: Int,
    val audioResName: String,
    val sections: List<TrackSection>,
    val orbChart: List<BeatOrbCue>
)

object MusicLibrary {

    const val TOTAL_TRACKS = 2

    val officialSocialLinks = listOf(
        ArtistSocialLink(
            label = "SPOTIFY",
            badge = "♫",
            url = "https://open.spotify.com/artist/2Cy1rpHuiZvU5ymbOQfD37?si=4z4vjdReTGCpYG-RhEn2sA",
            accentHex = 0xFF1DB954L
        ),
        ArtistSocialLink(
            label = "APPLE MUSIC",
            badge = "♫",
            url = "https://music.apple.com/us/artist/no-more-illusions/396440626",
            accentHex = 0xFFFA243CL
        ),
        ArtistSocialLink(
            label = "YOUTUBE",
            badge = "▶",
            url = "https://www.youtube.com/@nomoreillusionsofficial",
            accentHex = 0xFFFF0033L
        ),
        ArtistSocialLink(
            label = "INSTAGRAM",
            badge = "◎",
            url = "https://www.instagram.com/nomoreillusions_official",
            accentHex = 0xFF8B5CF6L
        ),
        ArtistSocialLink(
            label = "TIKTOK",
            badge = "♪",
            url = "https://www.tiktok.com/@nomoreillusions_official?_r=1&_t=zp-99wh3hbpxj1",
            accentHex = 0xFF38BDF8L
        )
    )

    private fun sample4Hz(arr: FloatArray, positionSec: Float): Float {
        if (arr.isEmpty()) return 0.4f
        val exactIdx = (positionSec.coerceAtLeast(0f) / MultiBandEnvelopes.STEP_SEC)
        val i0 = exactIdx.toInt().coerceIn(0, arr.lastIndex)
        val i1 = (i0 + 1).coerceIn(0, arr.lastIndex)
        val frac = (exactIdx - i0).coerceIn(0f, 1f)
        return (arr[i0] * (1f - frac) + arr[i1] * frac).coerceIn(0f, 1f)
    }

    /**
     * Returns the real-time 4Hz (250ms) Multi-Band Spectral State (RMS, Sub-Bass, Vocal/Mid, High Shimmer)
     * plus automatic sub-bass drop-out detection and swell velocity.
     */
    fun getMultiBandStateAt(trackIndex: Int, positionSec: Float): MultiBandAudioState {
        val isTrack1 = trackIndex <= 1
        val rmsArr = if (isTrack1) MultiBandEnvelopes.confideRms4Hz else MultiBandEnvelopes.numberOneRms4Hz
        val bassArr = if (isTrack1) MultiBandEnvelopes.confideBass4Hz else MultiBandEnvelopes.numberOneBass4Hz
        val vocalArr = if (isTrack1) MultiBandEnvelopes.confideVocal4Hz else MultiBandEnvelopes.numberOneVocal4Hz
        val highArr = if (isTrack1) MultiBandEnvelopes.confideHigh4Hz else MultiBandEnvelopes.numberOneHigh4Hz

        val rms = sample4Hz(rmsArr, positionSec)
        val bass = sample4Hz(bassArr, positionSec)
        val vocal = sample4Hz(vocalArr, positionSec)
        val high = sample4Hz(highArr, positionSec)

        val prevRms = sample4Hz(rmsArr, (positionSec - 0.75f).coerceAtLeast(0f))
        val prevVocal = sample4Hz(vocalArr, (positionSec - 0.75f).coerceAtLeast(0f))
        val prevHigh = sample4Hz(highArr, (positionSec - 0.75f).coerceAtLeast(0f))
        val swellDelta = maxOf(rms - prevRms, (vocal - prevVocal) * 0.85f, (high - prevHigh) * 0.85f).coerceIn(-0.5f, 0.7f)

        // True when the bass/kick drum intentionally drops out for vocal intimacy, pre-chorus hushes, or intros/outros
        val isConfideLull = isTrack1 && (positionSec < 7.0f || positionSec in 62.0f..68.9f || positionSec in 124.0f..130.9f || positionSec >= 207.8f)
        val isNumberOneLull = !isTrack1 && (positionSec < 9.5f || positionSec in 51.0f..53.2f || positionSec in 105.0f..107.6f || positionSec >= 152.5f)
        val bassDropOut = isConfideLull || isNumberOneLull || (positionSec > 8.0f && bass < 0.12f)

        return MultiBandAudioState(
            rms = rms,
            bass = bass,
            vocal = vocal,
            high = high,
            bassDropOut = bassDropOut,
            swellDelta = swellDelta
        )
    }

    fun getAudioEnergyAt(trackIndex: Int, positionSec: Float): Float {
        return getMultiBandStateAt(trackIndex, positionSec).rms
    }

    fun getAudioSwellDelta(trackIndex: Int, positionSec: Float): Float {
        return getMultiBandStateAt(trackIndex, positionSec).swellDelta
    }

    /**
     * Quantizes any timestamp to the nearest exact quarter-note beat of [bpm].
     */
    private fun quantizeToBeat(sec: Float, beatSec: Float): Float {
        val beatIndex = (sec / beatSec).roundToInt().coerceAtLeast(1)
        return beatIndex * beatSec
    }

    /**
     * Builds the Beat-Quantized Orb Chart for Track 01: Confide (124.0 BPM, beat = 0.48387s).
     * Every single orb lands on an exact quarter-note beat!
     * Special Supernova Orbs land on the exact measured bass drops, vocal hooks, and chorus swells.
     */
    private fun buildConfideOrbChart(): List<BeatOrbCue> {
        val beatSec = 60f / 124f // 0.48387s
        // ONLY 6 true milestone Supernova moments across the entire 3:39 track (matched to Ethan's exact notes!)
        // so that when a Supernova Orb appears, it feels unmistakably special!
        val specialMoments = listOf(
            KeyWordAnchor(14.52f, KeyWordEffect.CONSTELLATION_STAR), // 0:14.5 Intro Drop (EXTRA JUICE)
            KeyWordAnchor(45.97f, KeyWordEffect.PRISM_SHATTER),      // 0:46.0 Pre-Chorus Surge
            KeyWordAnchor(75.48f, KeyWordEffect.CONSTELLATION_STAR), // 1:15.5 Hook Instrumental Drop I (EXTRA JUICE)
            KeyWordAnchor(137.42f, KeyWordEffect.CONSTELLATION_STAR),// 2:17.5 Hook Instrumental Drop II (EXTRA JUICE)
            KeyWordAnchor(169.35f, KeyWordEffect.MAGNETAR_WAR),      // 2:49.5 Bridge Vocal Battle Swell
            KeyWordAnchor(183.87f, KeyWordEffect.CONSTELLATION_STAR) // 3:03.9 Ending Breakdown Climax (EXTRA JUICE)
        )

        val cues = ArrayList<BeatOrbCue>(190)
        val usedAnchors = HashSet<Int>()
        var beatIdx = 4
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.52f
        while (beatIdx * beatSec <= 213.0f) {
            val t = beatIdx * beatSec
            val inIntroLull = t < 7.2f
            val inBreakdown = (t in 62.0f..68.8f) || (t in 124.0f..130.8f)
            val inRiser = (t in 69.0f..75.4f) || (t in 131.0f..137.3f)
            val inChorusOrFinale = (t in 14.5f..30.8f) || (t in 75.5f..92.8f) || (t in 137.5f..154.8f) || (t in 183.5f..201.0f)
            val inOutro = t >= 207.8f

            var matchedIdx = -1
            var bestDist = beatSec * 1.2f
            for (i in specialMoments.indices) {
                if (i !in usedAnchors) {
                    val d = abs(specialMoments[i].timestampSec - t)
                    if (d <= bestDist) {
                        bestDist = d
                        matchedIdx = i
                    }
                }
            }

            val anchor = if (matchedIdx != -1) {
                usedAnchors.add(matchedIdx)
                specialMoments[matchedIdx]
            } else null

            val effect = anchor?.effect ?: KeyWordEffect.NONE
            val isGolden = effect == KeyWordEffect.CONSTELLATION_STAR

            // Sequential 4-Beat Phrase Choreography (Osu! / Piano Tiles / Guitar Hero style):
            // Consecutive beats (1 -> 2 -> 3 -> 4) form smooth, connected paths so players can
            // glide in rhythm from beat to beat without chaotic cross-screen jumps.
            val phraseIdx = (seq - 1) / 4
            val stepInPhrase = (seq - 1) % 4 // 0, 1, 2, 3
            val (rawX, rawY) = when {
                inRiser -> {
                    // Ascending rhythmic staircase climbing toward the Chorus Drop!
                    val riserProgress = if (t < 100f) ((t - 69f) / 6.4f).coerceIn(0f, 1f) else ((t - 131f) / 6.3f).coerceIn(0f, 1f)
                    val laneX = 0.30f + (stepInPhrase / 3f) * 0.40f * (if (phraseIdx % 2 == 0) 1f else -1f) + (if (phraseIdx % 2 == 0) 0f else 0.40f)
                    laneX.coerceIn(0.24f, 0.76f) to (0.72f - riserProgress * 0.40f).coerceIn(0.28f, 0.74f)
                }
                inIntroLull || inBreakdown || inOutro -> {
                    // Gentle centered crescent arc
                    val a = seq * 0.62f
                    (0.50f + 0.18f * sin(a)) to (0.50f + 0.10f * cos(a))
                }
                inChorusOrFinale -> {
                    // Sweeping 4-beat constellation arcs across the arena
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val arcT = (stepInPhrase / 3f) - 0.5f // -0.5 .. +0.5
                    val cx = 0.50f + dir * arcT * 0.46f
                    val cy = 0.48f + sin(phraseIdx * 1.1f + stepInPhrase * 0.65f) * 0.18f
                    cx.coerceIn(0.20f, 0.80f) to cy.coerceIn(0.28f, 0.72f)
                }
                else -> {
                    // Smooth flowing S-curve figure-8 pocket for Verses
                    val a = seq * 0.48f
                    (0.50f + 0.24f * sin(a)).coerceIn(0.22f, 0.78f) to
                        (0.50f + 0.16f * cos(a * 0.75f)).coerceIn(0.30f, 0.70f)
                }
            }

            // Clamp maximum step distance from previous beat so consecutive beats are always smoothly reachable
            val dx = (rawX - prevX).coerceIn(-0.24f, 0.24f)
            val dy = (rawY - prevY).coerceIn(-0.20f, 0.20f)
            val nx = (prevX + dx).coerceIn(0.20f, 0.80f)
            val ny = (prevY + dy).coerceIn(0.28f, 0.74f)
            prevX = nx
            prevY = ny

            val pType = when (effect) {
                KeyWordEffect.MAGNETAR_WAR, KeyWordEffect.PRISM_SHATTER, KeyWordEffect.CONSTELLATION_STAR -> PowerUpType.BLAST
                KeyWordEffect.ECLIPSE_DARK, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                KeyWordEffect.TRACTOR_REACH -> PowerUpType.MAGNET
                else -> when {
                    seq % 16 == 4 -> PowerUpType.BLAST    // 💥 Nova Blast: Vaporizes all enemies on screen + 5s Overdrive!
                    seq % 16 == 8 -> PowerUpType.SHIELD   // 🛡️ Aegis Shield: Absorbs next hit!
                    seq % 16 == 12 -> PowerUpType.SLOW_MO // ⏳ Chrono Shift: Slows enemies 0.45x!
                    seq % 16 == 0 -> PowerUpType.MAGNET   // 🧲 Harmonic Magnet: Pulls nearby orbs in!
                    else -> PowerUpType.MULTIPLIER        // ♪ Musical Beat Note Orb
                }
            }

            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq,
                    timestampSec = quantizeToBeat(anchor?.timestampSec ?: t, beatSec),
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = isGolden,
                    keyWordEffect = effect
                )
            )
            seq++
            beatIdx += when {
                inIntroLull || inBreakdown || inOutro -> 3 // Relaxed 3-beat breathing cadence
                inRiser -> 1                               // Every single quarter-note beat on drum risers!
                inChorusOrFinale -> 2                      // Energetic 2-beat half-note chorus flow
                else -> 2                                  // Linear 2-beat verse rhythm chain
            }
        }
        return cues
    }

    /**
     * Builds the Beat-Quantized Orb Chart for Track 02: Number One (88.0 BPM, beat = 0.6818s).
     * Restricted to 5 milestone Supernova moments matched to Ethan's exact notes!
     */
    private fun buildNumberOneOrbChart(): List<BeatOrbCue> {
        val beatSec = 60f / 88f // 0.6818s
        val specialMoments = listOf(
            KeyWordAnchor(44.32f, KeyWordEffect.TRACTOR_REACH),      // 0:44.3 Pre-Chorus I Swell
            KeyWordAnchor(54.55f, KeyWordEffect.CONSTELLATION_STAR), // 0:54.5 Chorus I Drop (EXTRA JUICE)
            KeyWordAnchor(92.05f, KeyWordEffect.PRISM_SHATTER),      // 1:32.0 Verse Two "Rap" (SLIGHT EXTRA JUICE)
            KeyWordAnchor(109.09f, KeyWordEffect.CONSTELLATION_STAR),// 1:49.1 Chorus II Drop (EXTRA JUICE)
            KeyWordAnchor(129.55f, KeyWordEffect.CONSTELLATION_STAR) // 2:09.5 Ending Breakdown Climax (EXTRA JUICE)
        )

        val cues = ArrayList<BeatOrbCue>(160)
        val usedAnchors = HashSet<Int>()
        var beatIdx = 3
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.52f
        while (beatIdx * beatSec <= 161.0f) {
            val t = beatIdx * beatSec
            val inIntroOrOutro = t < 9.8f || t >= 152.5f
            val inHush = (t in 51.0f..53.5f) || (t in 105.0f..107.8f)
            val isRap = t in 91.5f..98.5f
            val isChorus = (t in 54.5f..75.5f) || (t in 109.0f..129.0f)
            val isClimax = t in 129.0f..152.4f

            var matchedIdx = -1
            var bestDist = beatSec * 1.1f
            for (i in specialMoments.indices) {
                if (i !in usedAnchors) {
                    val d = abs(specialMoments[i].timestampSec - t)
                    if (d <= bestDist) {
                        bestDist = d
                        matchedIdx = i
                    }
                }
            }

            val anchor = if (matchedIdx != -1) {
                usedAnchors.add(matchedIdx)
                specialMoments[matchedIdx]
            } else null

            val effect = anchor?.effect ?: KeyWordEffect.NONE
            val isGolden = effect == KeyWordEffect.CONSTELLATION_STAR

            val phraseIdx = (seq - 1) / 4
            val stepInPhrase = (seq - 1) % 4
            val (rawX, rawY) = when {
                isRap -> {
                    // Tight sequential rhythm wave across the center during Ethan's 1:32 Rap!
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val lx = 0.50f + dir * ((stepInPhrase - 1.5f) * 0.14f)
                    val ly = 0.36f + (stepInPhrase * 0.08f)
                    lx.coerceIn(0.24f, 0.76f) to ly.coerceIn(0.30f, 0.70f)
                }
                inIntroOrOutro || inHush -> {
                    0.50f + 0.15f * cos(seq * 0.65f) to 0.50f + 0.09f * sin(seq * 0.65f)
                }
                isClimax || isChorus -> {
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val arcT = (stepInPhrase / 3f) - 0.5f
                    val cx = 0.50f + dir * arcT * 0.44f
                    val cy = 0.48f + cos(phraseIdx * 1.05f + stepInPhrase * 0.65f) * 0.17f
                    cx.coerceIn(0.20f, 0.80f) to cy.coerceIn(0.28f, 0.72f)
                }
                else -> {
                    val ribbonPhase = seq * 0.46f
                    (0.50f + 0.23f * cos(ribbonPhase)).coerceIn(0.22f, 0.78f) to
                        (0.50f + 0.16f * sin(ribbonPhase * 0.72f)).coerceIn(0.30f, 0.72f)
                }
            }

            val dx = (rawX - prevX).coerceIn(-0.24f, 0.24f)
            val dy = (rawY - prevY).coerceIn(-0.20f, 0.20f)
            val nx = (prevX + dx).coerceIn(0.20f, 0.80f)
            val ny = (prevY + dy).coerceIn(0.28f, 0.74f)
            prevX = nx
            prevY = ny

            val pType = when (effect) {
                KeyWordEffect.MAGNETAR_WAR, KeyWordEffect.PRISM_SHATTER, KeyWordEffect.CONSTELLATION_STAR -> PowerUpType.BLAST
                KeyWordEffect.ECLIPSE_DARK, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                KeyWordEffect.TRACTOR_REACH -> PowerUpType.MAGNET
                else -> when {
                    seq % 16 == 4 -> PowerUpType.BLAST    // 💥 Nova Blast: Vaporizes all enemies on screen + 5s Overdrive!
                    seq % 16 == 8 -> PowerUpType.SHIELD   // 🛡️ Aegis Shield: Absorbs next hit!
                    seq % 16 == 12 -> PowerUpType.SLOW_MO // ⏳ Chrono Shift: Slows enemies 0.45x!
                    seq % 16 == 0 -> PowerUpType.MAGNET   // 🧲 Harmonic Magnet: Pulls nearby orbs in!
                    else -> PowerUpType.MULTIPLIER        // ♪ Musical Beat Note Orb
                }
            }

            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq,
                    timestampSec = quantizeToBeat(anchor?.timestampSec ?: t, beatSec),
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = isGolden,
                    keyWordEffect = effect
                )
            )
            seq++
            beatIdx += when {
                inIntroOrOutro || inHush -> 3 // Calm, spacious breathing room
                isRap -> 1                    // Every quarter-note beat during Rap flow at 1:32!
                isClimax -> 1                 // Every quarter-note beat during Ending Breakdown at 2:09!
                isChorus -> 1                 // Every quarter-note beat (0.68s) during Chorus groove!
                else -> 2                     // Smooth 2-beat (1.36s) 88 BPM hip-hop pocket
            }
        }
        return cues
    }

    private val officialTracks = listOf(
        DemoTrack(
            index = 1,
            title = "Confide",
            bpm = 124f,
            durationSec = 219,
            unlockCost = 0,
            audioResName = "track_01",
            sections = listOf(
                // 0:00 Ambient Vocal Intro (0 enemies, pure floating calm)
                TrackSection(0f, "INTRO • VOCAL FLOAT", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                // 0:07.2 Bass & Guitar Swell (1 gentle ambient enemy)
                TrackSection(7.25f, "BASS & GUITAR SWELL", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.68f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 0:14.5 Intro Drop (EXTRA JUICE — Ethan's Note: 0:14 Intro Drop + Slide Wormhole!)
                TrackSection(14.5f, "INTRO DROP", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 0:31.0 Verse One (Ethan's Note: 0:31 Verse One — Rhythm First! Only 1 slow enemy)
                TrackSection(31.0f, "VERSE ONE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 0:45.5 Pre-Chorus Surge (2 enemies + Slide Wormhole!)
                TrackSection(45.5f, "PRE-CHORUS SURGE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 1:02.0 Hook Part 1: Zero-Bass Vocal Breakdown (Ethan's Note: 1:03 Hook — 0 enemies, complete lull!)
                TrackSection(62.0f, "HOOK • VOCAL FLOAT", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                // 1:09.2 Pre-Drop Drum Riser (Ascending beat staircase, 1 enemy)
                TrackSection(69.25f, "DRUM RISER BUILD", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.78f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 1:15.5 Hook Instrumental Drop (Ethan's Note: 1:17 Instrumental — 3 rhythmic enemies + Slide Wormholes!)
                TrackSection(75.5f, "INSTRUMENTAL DROP", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.92f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 1:33.0 Verse Two (Ethan's Note: 1:33 Verse Two — 1 flowing enemy)
                TrackSection(93.0f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.74f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 1:48.0 Mid-Verse Bass Surge (2 enemies + Slide Wormhole)
                TrackSection(108.0f, "VERSE BASS SURGE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 2:04.0 Hook II Part 1: Second Zero-Bass Vocal Breakdown (Ethan's Note: 2:05 Hook — 0 enemies, complete lull!)
                TrackSection(124.0f, "HOOK II • VOCAL FLOAT", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                // 2:11.2 Second Drum Riser
                TrackSection(131.25f, "DRUM RISER II", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.80f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 2:17.5 Second Instrumental Drop (Ethan's Note: 2:18 Instrumental — 3 enemies + Slide Wormholes!)
                TrackSection(137.5f, "INSTRUMENTAL DROP II", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.95f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 2:35.0 Pre-Bridge Breather (Calm down to 1 enemy)
                TrackSection(155.0f, "BRIDGE BREATHER", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 2:49.5 Bridge Vocal Battle Swell (Ethan's Note: 2:50 Bridge — 2 enemies + Slide Wormholes)
                TrackSection(169.5f, "BRIDGE VOCAL SWELL", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.86f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 3:03.5 Ending Breakdown Climax (Ethan's Note: 3:03 Ending Breakdown EXTRA JUICE — 4 enemies max in Normal!)
                TrackSection(183.5f, "ENDING BREAKDOWN", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 4, enemySpeedMult = 0.98f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 3:21.0 Isolated 808 Sub-Bass Heartbeat (Bass = 0.99! Heavy gravity shockwaves)
                TrackSection(201.0f, "808 HEARTBEAT PULSE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = true, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 3:27.5 Velvet Outro Lull (0 enemies — peaceful victory glide!)
                TrackSection(207.8f, "OUTRO • VICTORY GLIDE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
            ),
            orbChart = buildConfideOrbChart()
        ),
        DemoTrack(
            index = 2,
            title = "Number One",
            bpm = 88f,
            durationSec = 166,
            unlockCost = 1200,
            audioResName = "track_02",
            sections = listOf(
                // 0:00 Warm Chord Intro (Ethan's Note: 0:00 Intro — 0 enemies for first 9.8s)
                TrackSection(0f, "INTRO • WARM CHORDS", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                // 0:09.8 Percussion Shimmer Opening (1 gentle enemy)
                TrackSection(9.8f, "BEAT OPENING", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.68f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 0:21.0 Verse One (Ethan's Note: 0:21 Verse One — smooth 88 BPM pocket, max 1 enemy)
                TrackSection(21.0f, "VERSE ONE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 0:43.5 Pre-Chorus (Ethan's Note: 0:44 Pre-Chorus — 2 enemies + Slide Wormhole)
                TrackSection(43.5f, "PRE-CHORUS SWELL", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 0:51.0 Pre-Drop Hush (2-second vocal breath before Chorus I)
                TrackSection(51.0f, "PRE-DROP HUSH", JuiceTier.NONE, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                // 0:54.5 Chorus I (Ethan's Note: 0:54 Chorus — 3 rhythmic enemies + Golden bloom + Slide Wormholes!)
                TrackSection(54.5f, "CHORUS • NUMBER ONE", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 1:15.5 Verse Two Pullback (Ethan's Note: 1:15 Verse Two — cool stripped-back groove, 1 enemy)
                TrackSection(75.5f, "VERSE TWO • COOL GROOVE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 1:31.5 Verse Two "Rap" (Ethan's Note: 1:32 Verse Two "Rap" SLIGHT EXTRA JUICE — focus on rapid beat chain, 2 enemies!)
                TrackSection(91.5f, "DOUBLE-TIME RAP FLOW", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 1:38.5 Pre-Chorus II (Ethan's Note: 1:38 Pre-Chorus)
                TrackSection(98.5f, "PRE-CHORUS II", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = false),
                // 1:45.0 Pre-Chorus II Hush (2.5s breather before Chorus II)
                TrackSection(105.0f, "VOCAL BREATHER", JuiceTier.NONE, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                // 1:49.0 Chorus II (Ethan's Note: 1:49 Chorus — 3 enemies + Slide Wormholes!)
                TrackSection(109.0f, "CHORUS II DROP", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.94f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 2:09.0 Ending Breakdown Climax (Ethan's Note: 2:09 Ending Breakdown EXTRA JUICE — 4 enemies max!)
                TrackSection(129.0f, "ENDING BREAKDOWN CLIMAX", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 4, enemySpeedMult = 0.98f, clearEnemiesOnEnter = false, allowAnomalies = true),
                // 2:32.5 Velvet Twilight Outro (0 enemies — peaceful victory glide!)
                TrackSection(152.5f, "OUTRO • VICTORY GLIDE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
            ),
            orbChart = buildNumberOneOrbChart()
        )
    )

    fun placeholderAlbum(): DemoAlbum {
        return DemoAlbum(
            albumId = "no_more_illusions_01",
            artistName = "No Morē Illusīons",
            albumTitle = "No More Illusions",
            tracks = officialTracks,
            socialLinks = officialSocialLinks
        )
    }

    fun getTrack(trackIndex: Int): DemoTrack {
        return officialTracks.getOrElse((trackIndex - 1).coerceIn(0, officialTracks.lastIndex)) {
            officialTracks.first()
        }
    }

    fun currentSectionAt(trackIndex: Int, positionSec: Float): TrackSection {
        val track = getTrack(trackIndex)
        return track.sections.lastOrNull { positionSec >= it.timestampSec } ?: track.sections.first()
    }

    fun unlockCostForTrack(trackIndex: Int): Int {
        return getTrack(trackIndex).unlockCost
    }

    fun resolveRawAudioId(context: Context, audioResName: String): Int {
        return context.resources.getIdentifier(audioResName, "raw", context.packageName)
    }
}
