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
    val isGoldenOverdrive: Boolean = false
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

        // True when the bass/kick drum intentionally drops out for vocal intimacy or stop-time
        val bassDropOut = positionSec > 8.0f && bass < 0.14f

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
        val specialMoments = listOf(
            // 0:07.5 Bass & Synth Swell Enters -> 0:14.5 Full Drum & High Ignition
            KeyWordAnchor(7.26f, KeyWordEffect.TRACTOR_REACH),
            KeyWordAnchor(14.52f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(21.29f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(28.55f, KeyWordEffect.PRISM_SHATTER),
            // Verse 1: Vocal Entry (31.68s), Syncopated Bass Cuts & Re-entrances (36.5s, 44.2s, 46.0s, 53.7s, 61.0s)
            KeyWordAnchor(31.94f, KeyWordEffect.TRACTOR_REACH),
            KeyWordAnchor(36.29f, KeyWordEffect.ECLIPSE_DARK),
            KeyWordAnchor(43.06f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(45.97f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(53.71f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(60.97f, KeyWordEffect.ECLIPSE_DARK),
            // 1:02 - 1:09 Zero-Bass Vocal Breakdown -> 1:09.2 Bass Re-Entry Crescendo -> 1:15.5 Chorus 1 Supernova Drop!
            KeyWordAnchor(65.81f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(69.68f, KeyWordEffect.TRACTOR_REACH),
            KeyWordAnchor(72.58f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(75.48f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(83.23f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(90.97f, KeyWordEffect.MAGNETAR_WAR),
            // Verse 2 Groove & Syncopated Swells (93.0s - 123.0s)
            KeyWordAnchor(97.26f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(102.10f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(107.90f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(113.71f, KeyWordEffect.TRACTOR_REACH),
            KeyWordAnchor(119.52f, KeyWordEffect.PRISM_SHATTER),
            // 2:04 - 2:11 Second Zero-Bass Breakdown -> 2:11.2 Pre-Chorus Crescendo -> 2:17.5 Chorus 2 Drop!
            KeyWordAnchor(127.26f, KeyWordEffect.ECLIPSE_DARK),
            KeyWordAnchor(131.61f, KeyWordEffect.TRACTOR_REACH),
            KeyWordAnchor(134.52f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(137.42f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(145.16f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(152.90f, KeyWordEffect.MAGNETAR_WAR),
            // Bridge -> 2:49.2 Vocal Battle Swell -> 3:04.8 Grand Finale Climax!
            KeyWordAnchor(160.65f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(169.35f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(176.13f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(184.84f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(192.58f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(201.29f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(208.06f, KeyWordEffect.ECLIPSE_DARK)
        )

        val cues = ArrayList<BeatOrbCue>(115)
        val usedAnchors = HashSet<Int>()
        // Beat-locked every 4 beats (1 bar = 1.935s) during standard sections, and every 2 beats (0.967s) during pre-chorus risers!
        var beatIdx = 4
        var seq = 1
        while (beatIdx * beatSec <= 214.0f) {
            val t = beatIdx * beatSec
            val inBreakdown = (t in 62.0f..68.8f) || (t in 124.0f..130.8f)
            val inRiser = (t in 69.0f..75.4f) || (t in 131.0f..137.3f)

            var matchedIdx = -1
            var bestDist = beatSec * 1.6f
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

            // Musical choreography of orb positions on screen:
            // - During Pre-Chorus Risers (69-75s, 131-137s), orbs climb upward in a dramatic rising staircase!
            // - During Breakdowns, orbs float calmly near the center.
            // - Otherwise, orbs weave a clean figure-8 / S-curve across the arena.
            val (nx, ny) = when {
                inRiser -> {
                    val riserProgress = if (t < 100f) ((t - 69f) / 6.5f).coerceIn(0f, 1f) else ((t - 131f) / 6.5f).coerceIn(0f, 1f)
                    val side = if (seq % 2 == 0) 0.32f else 0.68f
                    side to (0.72f - riserProgress * 0.40f).coerceIn(0.28f, 0.74f)
                }
                inBreakdown -> {
                    0.50f + 0.16f * sin(seq * 0.8f) to 0.52f + 0.10f * cos(seq * 0.8f)
                }
                else -> {
                    val ribbonPhase = seq * 0.58f
                    (0.50f + 0.28f * sin(ribbonPhase)).coerceIn(0.20f, 0.80f) to
                        (0.50f + 0.21f * cos(ribbonPhase * 0.70f)).coerceIn(0.28f, 0.74f)
                }
            }

            val pType = when (effect) {
                KeyWordEffect.MAGNETAR_WAR, KeyWordEffect.PRISM_SHATTER -> PowerUpType.BLAST
                KeyWordEffect.ECLIPSE_DARK, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                else -> PowerUpType.MULTIPLIER
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
                inRiser -> 3      // Tighter climbing pulse during pre-chorus drum risers
                inBreakdown -> 6  // Spacious, breathing cadence during zero-bass vocal breakdowns
                else -> 4         // Exact 1-bar (4-beat) downbeat lock!
            }
        }
        return cues
    }

    /**
     * Builds the Beat-Quantized Orb Chart for Track 02: Number One (88.0 BPM, beat = 0.6818s).
     * Every orb is locked to the 88 BPM beat grid.
     */
    private fun buildNumberOneOrbChart(): List<BeatOrbCue> {
        val beatSec = 60f / 88f // 0.6818s
        val specialMoments = listOf(
            // Intro Bass Entry (6.5s) & Shimmer Opening (9.75s)
            KeyWordAnchor(6.82f, KeyWordEffect.TRACTOR_REACH),
            KeyWordAnchor(10.23f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(17.73f, KeyWordEffect.TRACTOR_REACH),
            // Verse 1 Swells (24.5s, 31.25s, 39.5s)
            KeyWordAnchor(24.55f, KeyWordEffect.TRACTOR_REACH),
            KeyWordAnchor(31.36f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(39.55f, KeyWordEffect.MAGNETAR_WAR),
            // Pre-Chorus & Shimmer Riser (45.0s, 52.5s) -> Chorus 1 Swell (55.25s - 75.0s)
            KeyWordAnchor(45.00f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(52.50f, KeyWordEffect.ECLIPSE_DARK),
            KeyWordAnchor(55.23f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(58.64f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(64.09f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(70.23f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(75.00f, KeyWordEffect.CONSTELLATION_STAR),
            // Verse 2 Pullback (76.0s) -> Groove Re-entry (79.0s, 84.5s)
            KeyWordAnchor(79.09f, KeyWordEffect.ECLIPSE_DARK),
            KeyWordAnchor(84.55f, KeyWordEffect.TRACTOR_REACH),
            // 1:31.2 Rap Cadence & Bass Swells
            KeyWordAnchor(91.36f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(94.09f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(97.50f, KeyWordEffect.MAGNETAR_WAR),
            // Pre-Chorus 2 (104.5s) -> Chorus 2 Swell (109.75s - 125.0s)
            KeyWordAnchor(104.32f, KeyWordEffect.ECLIPSE_DARK),
            KeyWordAnchor(109.77f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(113.18f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(120.68f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(124.09f, KeyWordEffect.BINARY_HEART),
            // Grand Finale Climax ("Don't go out!" 129.5s - 152.5s)
            KeyWordAnchor(129.55f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(135.00f, KeyWordEffect.BINARY_HEART),
            KeyWordAnchor(139.09f, KeyWordEffect.PRISM_SHATTER),
            KeyWordAnchor(145.91f, KeyWordEffect.MAGNETAR_WAR),
            KeyWordAnchor(148.64f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(152.73f, KeyWordEffect.CONSTELLATION_STAR),
            KeyWordAnchor(159.55f, KeyWordEffect.ECLIPSE_DARK)
        )

        val cues = ArrayList<BeatOrbCue>(95)
        val usedAnchors = HashSet<Int>()
        var beatIdx = 3
        var seq = 1
        while (beatIdx * beatSec <= 162.0f) {
            val t = beatIdx * beatSec
            val isRap = t in 91.0f..98.5f
            val isClimax = t in 129.0f..152.5f

            var matchedIdx = -1
            var bestDist = beatSec * 1.4f
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

            val ribbonPhase = if (isRap) seq * 0.65f else seq * 0.52f
            val nx = (0.50f + 0.28f * cos(ribbonPhase)).coerceIn(0.20f, 0.80f)
            val ny = (0.50f + 0.21f * sin(ribbonPhase * 0.72f)).coerceIn(0.28f, 0.74f)

            val pType = when (effect) {
                KeyWordEffect.MAGNETAR_WAR, KeyWordEffect.PRISM_SHATTER -> PowerUpType.BLAST
                KeyWordEffect.ECLIPSE_DARK, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                else -> PowerUpType.MULTIPLIER
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
                isRap -> 2     // 2-beat cadence during the 1:32 rap flow
                isClimax -> 2  // 2-beat cadence during the 2:10 Grand Finale
                else -> 3      // Smooth 3-beat harmonic cadence at 88 BPM
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
                TrackSection(0f, "INTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(7.25f, "BASS & GUITAR SWELL", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(14.5f, "DRUM & SYNTH DROP", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true),
                TrackSection(31.2f, "VERSE ONE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(45.5f, "PRE-CHORUS SURGE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(62.0f, "VOCAL BREAKDOWN", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false),
                TrackSection(69.25f, "PRE-CHORUS RISER", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(75.5f, "CHORUS SUPERNOVA", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true),
                TrackSection(93.0f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(107.8f, "MID-VERSE BASS SURGE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(124.0f, "VOCAL BREAKDOWN II", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false),
                TrackSection(131.25f, "PRE-CHORUS RISER II", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(137.5f, "CHORUS II SUPERNOVA", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true),
                TrackSection(155.0f, "BRIDGE BREATHER", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(169.25f, "VOCAL BATTLE SWELL", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(184.75f, "GRAND FINALE CLIMAX", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true),
                TrackSection(201.25f, "SUB-BASS HEARTBEAT", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(208.0f, "OUTRO ECLIPSE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false)
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
                TrackSection(0f, "INTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(6.5f, "SUB-BASS WARMTH", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(9.75f, "PERCUSSION SHIMMER", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(22.0f, "VERSE ONE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(42.25f, "PRE-CHORUS SWELL", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false),
                TrackSection(55.25f, "CHORUS DROP", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true),
                TrackSection(76.0f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(91.25f, "RAP CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.MAGIC_LANES, isGoldenOverdrive = false),
                TrackSection(101.5f, "PRE-CHORUS II", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false),
                TrackSection(109.75f, "CHORUS II DROP", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true),
                TrackSection(129.5f, "GRAND FINALE CLIMAX", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true),
                TrackSection(152.5f, "VELVET TWILIGHT OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false)
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
