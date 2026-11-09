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
    val keyWordEffect: KeyWordEffect = KeyWordEffect.NONE,
    val customLeadSec: Float = 0f,
    val swirlStrength: Float = 0f,
    val goldenTier: Int = 0
)

/**
 * Upcoming song slot displayed in the Album & Vault catalog to reserve space for Ethan's next releases.
 */
data class UpcomingReleaseSlot(
    val index: Int,
    val workingTitle: String,
    val editionName: String,
    val statusBadge: String,
    val accentHex: Long
)

/**
 * Collector's Vinyl Pressing Edition palette per track index (eliminates the need for separate custom PNG cover art per song).
 */
data class VinylPressingEdition(
    val editionTitle: String,
    val primaryHex: Long,
    val secondaryHex: Long
)

private data class KeyWordAnchor(
    val timestampSec: Float,
    val effect: KeyWordEffect,
    val goldenTier: Int = 0,
    val normY: Float = 0.28f
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

    val TOTAL_TRACKS: Int
        get() = officialTracks.size

    /**
     * Reserved upcoming slots for Ethan's next singles — drop a new `track_03.mp3` into `res/raw/`
     * and call `createOfficialReleaseTrack(index = 3, title = "...", bpm = ..., durationSec = ...)` in `officialTracks`!
     */
    val upcomingReleaseSlots: List<UpcomingReleaseSlot> = listOf(
        UpcomingReleaseSlot(
            index = 3,
            workingTitle = "Upcoming Single 03",
            editionName = "Amethyst Aurora Pressing",
            statusBadge = "IN THE STUDIO",
            accentHex = 0xFFC084FCL
        ),
        UpcomingReleaseSlot(
            index = 4,
            workingTitle = "Upcoming Single 04",
            editionName = "Crimson Supernova Pressing",
            statusBadge = "UPCOMING DROP",
            accentHex = 0xFFFB7185L
        ),
        UpcomingReleaseSlot(
            index = 5,
            workingTitle = "Upcoming Single 05",
            editionName = "Diamond Prism Pressing",
            statusBadge = "UPCOMING DROP",
            accentHex = 0xFF38BDF8L
        )
    )

    fun pressingEditionForTrack(trackIndex: Int): VinylPressingEdition {
        return when ((trackIndex - 1).coerceAtLeast(0) % 6) {
            0 -> VinylPressingEdition("EMERALD OBSIDIAN EDITION", 0xFF22C55EL, 0xFF38BDF8L)
            1 -> VinylPressingEdition("24K SOLAR GOLD EDITION", 0xFFFACC15L, 0xFF818CF8L)
            2 -> VinylPressingEdition("AMETHYST AURORA EDITION", 0xFFC084FCL, 0xFF38BDF8L)
            3 -> VinylPressingEdition("CRIMSON SUPERNOVA EDITION", 0xFFFB7185L, 0xFFFACC15L)
            4 -> VinylPressingEdition("DIAMOND PRISM EDITION", 0xFF38BDF8L, 0xFF4ADE80L)
            else -> VinylPressingEdition("VELVET ECLIPSE EDITION", 0xFFF59E0BL, 0xFFE879F9L)
        }
    }

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
     * Automatically supports Track 03+ via beat-locked procedural harmonic envelopes when pre-extracted arrays aren't present!
     */
    fun getMultiBandStateAt(trackIndex: Int, positionSec: Float): MultiBandAudioState {
        if (trackIndex > 2) {
            val trk = getTrack(trackIndex)
            val beatSec = 60f / trk.bpm.coerceAtLeast(60f)
            val beatPhase = ((positionSec.coerceAtLeast(0f) % beatSec) / beatSec)
            val kick = kotlin.math.exp(-beatPhase * 4.2f).coerceIn(0f, 1f)
            val phraseWave = 0.5f + 0.5f * sin(positionSec * 0.22f + trackIndex)
            val isLull = positionSec < 8.0f || positionSec > (trk.durationSec - 12f)
            val bassVal = if (isLull) 0.08f else (0.34f + kick * 0.46f * (0.65f + 0.35f * phraseWave)).coerceIn(0f, 1f)
            val vocalVal = (0.36f + 0.38f * phraseWave + 0.12f * sin(positionSec * 0.9f)).coerceIn(0.1f, 1f)
            val highVal = (0.24f + 0.34f * phraseWave + kick * 0.18f).coerceIn(0.05f, 1f)
            val rmsVal = (bassVal * 0.42f + vocalVal * 0.38f + highVal * 0.20f).coerceIn(0.12f, 1f)
            val swell = (0.14f * cos(positionSec * 0.22f + trackIndex)).coerceIn(-0.3f, 0.4f)
            return MultiBandAudioState(
                rms = rmsVal,
                bass = bassVal,
                vocal = vocalVal,
                high = highVal,
                bassDropOut = isLull,
                swellDelta = swell
            )
        }
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
     * Builds the Beat-Quantized Orb Chart for Track 01: Confide (124.0 BPM, beat = 0.48387s, 219s total).
     * Choreographed beat-for-beat to Ethan's arrangement:
     * - -1.0s..0.0s: Pre-music drift (initial orbs slowly enter without pulsing)
     * - 0:00..0:07: Music starts, orbs pulse to beat
     * - 0:07..0:15: Transition 1 + clear quarter-note drum beat stream
     * - 0:15: Top-center Special 24K Milestone Orb (Tier 1)
     * - 0:31..1:00: Music shift + 5 Video Relics + 0:45-0:48 flowy/whispy slow-mo swirl ("losing your battles")
     * - 1:00..1:08.5: Slow-Mo Video Montage 1
     * - 1:08.5..1:18: Clear quarter-note drum beat stream
     * - 1:18: Top-center Special 24K Milestone Orb (Tier 2 Hyper-Spectacle + special orb pulse)
     * - 1:18..1:48: Verse 2 steady cadence
     * - 1:48..1:55: Spoken-ish vocal drift
     * - 1:55..2:36: Cadence + 2:19 Visual Wow + 2nd set of 5 Video Relics
     * - 2:36..2:50: Slow-Mo Crossfade Video Montage 2 during vocal "oooohs" until "I" at 2:50
     * - 2:50..3:05: Regular note collections
     * - 3:05..3:20: Riff + 24K Climax
     * - 3:20..3:37: Slowdown + deceptive quiet lull + 4 unexpected surprise ending pegs before 3:39!
     */
    private fun buildConfideOrbChart(): List<BeatOrbCue> {
        val beatSec = 60f / 124f // 0.483871s
        val specialMoments = listOf(
            KeyWordAnchor(15.00f, KeyWordEffect.CONSTELLATION_STAR, goldenTier = 1, normY = 0.28f),  // 0:15.0 24K Milestone 1 near top-center!
            KeyWordAnchor(77.90f, KeyWordEffect.CONSTELLATION_STAR, goldenTier = 2, normY = 0.28f),  // 1:17.9 24K Milestone 2 (Tier 2 Spectacle!)
            KeyWordAnchor(108.39f, KeyWordEffect.ECLIPSE_DARK, goldenTier = 0, normY = 0.34f),       // 1:48.4 Spoken-ish vocal drift
            KeyWordAnchor(138.87f, KeyWordEffect.PRISM_SHATTER, goldenTier = 0, normY = 0.30f),      // 2:18.9 Visual Wow transition
            KeyWordAnchor(185.32f, KeyWordEffect.CONSTELLATION_STAR, goldenTier = 2, normY = 0.28f)  // 3:05.3 Riff 24K Climax
        )

        val specialBeatIndices = specialMoments.map { (it.timestampSec / beatSec).roundToInt() }
        val usedAnchors = HashSet<Int>()
        val cues = ArrayList<BeatOrbCue>(240)

        var beatIdx = 3 // 1.45s: spawns at -1.10s with 2.55s lead so orbs slowly drift in during the 1s pre-music movement!
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.48f
        var prevSpawnSec = -5.0f

        while (beatIdx * beatSec <= 217.0f) {
            val t = beatIdx * beatSec
            val inIntroDrift = t < 7.0f
            val inDrumCadence = t in 7.0f..14.3f
            val inChorus1 = t in 15.5f..31.0f
            val inVerse1 = t in 31.0f..44.4f
            val inWhispyBreakdown = t in 44.4f..49.4f
            val inPreInterlude1 = t in 49.4f..59.8f
            val inInterlude1 = t in 59.8f..68.4f
            val inDrumRiser = t in 68.4f..77.2f
            val inChorus2 = t in 78.2f..93.0f
            val inVerse2 = t in 93.0f..108.0f
            val inSpokenFloat = t in 108.0f..115.2f
            val inBuildUp2 = t in 115.2f..138.2f
            val inChorus3 = t in 138.2f..155.9f
            val inInterlude2 = t in 155.9f..170.0f
            val inPostInterludeCadence = t in 170.0f..184.8f
            val inClimaxRiff = t in 184.8f..200.0f
            val inOutroSlowdown = t in 200.0f..209.2f
            val inSurpriseEnding = t >= 212.0f

            var matchedIdx = -1
            for (i in specialBeatIndices.indices) {
                if (i !in usedAnchors && abs(specialBeatIndices[i] - beatIdx) <= 1) {
                    matchedIdx = i
                    usedAnchors.add(i)
                    break
                }
            }
            val anchor = if (matchedIdx != -1) specialMoments[matchedIdx] else null
            val effect = anchor?.effect ?: KeyWordEffect.NONE
            val isGolden = effect == KeyWordEffect.CONSTELLATION_STAR
            val gTier = anchor?.goldenTier ?: 0

            val phraseIdx = (seq - 1) / 4
            val stepInPhrase = (seq - 1) % 4
            val (rawX, rawY) = when {
                anchor != null -> 0.50f to anchor.normY
                inSurpriseEnding -> {
                    val sneakPositions = arrayOf(0.25f to 0.44f, 0.75f to 0.56f, 0.30f to 0.60f, 0.72f to 0.42f)
                    sneakPositions[(seq) % sneakPositions.size]
                }
                inInterlude1 || inInterlude2 -> {
                    val edgeX = if (seq % 2 == 0) 0.72f else 0.28f
                    edgeX to (0.48f + 0.12f * sin(seq * 0.9f))
                }
                inDrumCadence || inDrumRiser -> {
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val lx = 0.50f + dir * ((stepInPhrase - 1.5f) / 3f) * 0.38f
                    val ly = 0.42f + ((stepInPhrase % 3) * 0.08f)
                    lx.coerceIn(0.22f, 0.78f) to ly.coerceIn(0.34f, 0.64f)
                }
                inWhispyBreakdown || inSpokenFloat -> {
                    val a = seq * 0.62f
                    (0.50f + 0.18f * sin(a)) to (0.46f + 0.10f * cos(a))
                }
                inOutroSlowdown -> {
                    val side = if (seq % 2 == 0) 0.66f else 0.34f
                    side to (0.48f + 0.08f * sin(seq.toFloat()))
                }
                inChorus1 || inChorus2 || inChorus3 || inClimaxRiff -> {
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val arcT = (stepInPhrase / 3f) - 0.5f
                    val cx = 0.50f + dir * arcT * 0.44f
                    val cy = 0.46f + sin(phraseIdx * 1.1f + stepInPhrase * 0.65f) * 0.15f
                    cx.coerceIn(0.22f, 0.78f) to cy.coerceIn(0.32f, 0.66f)
                }
                inVerse1 || inPreInterlude1 || inVerse2 || inBuildUp2 || inPostInterludeCadence -> {
                    val a = seq * 0.48f
                    (0.50f + 0.24f * sin(a)).coerceIn(0.22f, 0.78f) to
                        (0.48f + 0.14f * cos(a * 0.75f)).coerceIn(0.34f, 0.66f)
                }
                else -> {
                    val a = seq * 0.50f
                    (0.50f + 0.20f * sin(a)).coerceIn(0.26f, 0.74f) to
                        (0.46f + 0.10f * cos(a)).coerceIn(0.34f, 0.60f)
                }
            }

            val nx: Float
            val ny: Float
            if (anchor != null) {
                nx = 0.50f
                ny = anchor.normY
            } else if (inSurpriseEnding) {
                nx = rawX.coerceIn(0.22f, 0.78f)
                ny = rawY.coerceIn(0.34f, 0.66f)
            } else {
                val dx = (rawX - prevX).coerceIn(-0.26f, 0.26f)
                val dy = (rawY - prevY).coerceIn(-0.22f, 0.22f)
                nx = (prevX + dx).coerceIn(0.22f, 0.78f)
                ny = (prevY + dy).coerceIn(0.30f, 0.68f)
            }
            prevX = nx
            prevY = ny

            val pType = when (effect) {
                KeyWordEffect.CONSTELLATION_STAR -> PowerUpType.ELECTRIC_HARMONIC
                KeyWordEffect.PRISM_SHATTER -> PowerUpType.PRISM_HARMONY
                KeyWordEffect.ECLIPSE_DARK -> PowerUpType.SLOW_MO
                KeyWordEffect.MAGNETAR_WAR -> PowerUpType.BLAST
                KeyWordEffect.TRACTOR_REACH, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                else -> PowerUpType.NONE
            }

            val desiredLead = when {
                anchor != null -> 1.45f
                inIntroDrift -> 2.55f
                inWhispyBreakdown -> 3.30f
                inSpokenFloat -> 2.85f
                inInterlude1 || inInterlude2 -> 2.75f
                inOutroSlowdown -> 3.20f
                inSurpriseEnding -> 1.40f
                else -> 1.95f
            }
            // Guarantee monotonic spawn order so variable lead times never block or stall subsequent cues
            val maxAllowedLead = (t - (prevSpawnSec + 0.03f)).coerceAtLeast(1.15f)
            val effectiveLead = minOf(desiredLead, maxAllowedLead)
            prevSpawnSec = t - effectiveLead

            val swirl = when {
                anchor != null -> 0f
                inWhispyBreakdown -> 1.0f
                inSpokenFloat -> 0.65f
                inInterlude1 || inInterlude2 -> 0.45f
                else -> 0f
            }

            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq,
                    timestampSec = t,
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = isGolden,
                    keyWordEffect = effect,
                    customLeadSec = effectiveLead,
                    swirlStrength = swirl,
                    goldenTier = gTier
                )
            )
            seq++

            if (inOutroSlowdown && (beatIdx + 4) * beatSec > 209.2f) {
                // Deceptive 3.4-second quiet lull before the 4 surprise ending pegs at 3:32.4 - 3:36.8!
                beatIdx = 439 // 439 * 0.483871 = 212.42s
            } else {
                val rawStep = when {
                    anchor != null -> 2 // Give 2 beats of comfortable breathing room after top-center milestone orb
                    inDrumCadence || inDrumRiser -> 1 // Every single clear drum beat!
                    inIntroDrift || inWhispyBreakdown || inSpokenFloat -> 2
                    inInterlude1 || inInterlude2 -> 3
                    inOutroSlowdown -> 4
                    inSurpriseEnding -> 3
                    inChorus1 || inChorus2 || inChorus3 || inClimaxRiff -> if (seq % 3 == 0) 1 else 2
                    else -> if (seq % 2 == 0) 1 else 2
                }
                var nextBeat = beatIdx + rawStep
                for (i in specialBeatIndices.indices) {
                    val sb = specialBeatIndices[i]
                    if (i !in usedAnchors && sb > beatIdx && sb < nextBeat) {
                        nextBeat = sb
                    }
                }
                beatIdx = nextBeat
            }
        }
        return cues
    }

    /**
     * Builds the Beat-Quantized Orb Chart for Track 02: Number One (88.0 BPM, beat = 0.6818s, 166s total).
     */
    private fun buildNumberOneOrbChart(): List<BeatOrbCue> {
        val beatSec = 60f / 88f // 0.681818s
        val specialMoments = listOf(
            KeyWordAnchor(21.14f, KeyWordEffect.CONSTELLATION_STAR, goldenTier = 1, normY = 0.28f),  // 0:21.1 24K Milestone 1 near top-center
            KeyWordAnchor(73.64f, KeyWordEffect.CONSTELLATION_STAR, goldenTier = 2, normY = 0.28f),  // 1:13.6 24K Milestone 2 (Tier 2 Spectacle)
            KeyWordAnchor(92.05f, KeyWordEffect.ECLIPSE_DARK, goldenTier = 0, normY = 0.34f),        // 1:32.0 Intimate vocal/rap shift
            KeyWordAnchor(109.09f, KeyWordEffect.PRISM_SHATTER, goldenTier = 0, normY = 0.30f),      // 1:49.1 Visual Wow crystal prism
            KeyWordAnchor(141.14f, KeyWordEffect.CONSTELLATION_STAR, goldenTier = 2, normY = 0.28f)  // 2:21.1 Ending Riff 24K Climax
        )

        val specialBeatIndices = specialMoments.map { (it.timestampSec / beatSec).roundToInt() }
        val usedAnchors = HashSet<Int>()
        val cues = ArrayList<BeatOrbCue>(220)

        var beatIdx = 2 // 1.36s: spawns at -1.19s during pre-music drift!
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.48f
        var prevSpawnSec = -5.0f

        while (beatIdx * beatSec <= 164.0f) {
            val t = beatIdx * beatSec
            val inIntroDrift = t < 9.5f
            val inDrumCadence = t in 9.5f..20.5f
            val inChorus1 = t in 21.5f..32.0f
            val inWhispyBreakdown = t in 41.5f..46.8f
            val inInterlude1 = t in 54.4f..63.0f
            val inDrumRiser = t in 63.0f..73.0f
            val inSpokenFloat = t in 91.5f..97.0f
            val inInterlude2 = t in 118.4f..131.5f
            val inClimaxRiff = t in 140.5f..149.0f
            val inOutroSlowdown = t in 149.0f..156.2f
            val inSurpriseEnding = t >= 158.8f

            var matchedIdx = -1
            for (i in specialBeatIndices.indices) {
                if (i !in usedAnchors && abs(specialBeatIndices[i] - beatIdx) <= 1) {
                    matchedIdx = i
                    usedAnchors.add(i)
                    break
                }
            }
            val anchor = if (matchedIdx != -1) specialMoments[matchedIdx] else null
            val effect = anchor?.effect ?: KeyWordEffect.NONE
            val isGolden = effect == KeyWordEffect.CONSTELLATION_STAR
            val gTier = anchor?.goldenTier ?: 0

            val phraseIdx = (seq - 1) / 4
            val stepInPhrase = (seq - 1) % 4
            val (rawX, rawY) = when {
                anchor != null -> 0.50f to anchor.normY
                inSurpriseEnding -> {
                    val sneakPositions = arrayOf(0.26f to 0.45f, 0.74f to 0.55f, 0.30f to 0.58f, 0.72f to 0.42f)
                    sneakPositions[seq % sneakPositions.size]
                }
                inInterlude1 || inInterlude2 -> {
                    val edgeX = if (seq % 2 == 0) 0.72f else 0.28f
                    edgeX to (0.48f + 0.12f * sin(seq * 0.9f))
                }
                inDrumCadence || inDrumRiser -> {
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val lx = 0.50f + dir * ((stepInPhrase - 1.5f) / 3f) * 0.38f
                    val ly = 0.42f + ((stepInPhrase % 3) * 0.08f)
                    lx.coerceIn(0.22f, 0.78f) to ly.coerceIn(0.34f, 0.64f)
                }
                inWhispyBreakdown || inSpokenFloat -> {
                    val a = seq * 0.62f
                    (0.50f + 0.18f * sin(a)) to (0.46f + 0.10f * cos(a))
                }
                inOutroSlowdown -> {
                    val side = if (seq % 2 == 0) 0.66f else 0.34f
                    side to (0.48f + 0.08f * sin(seq.toFloat()))
                }
                inChorus1 || inClimaxRiff -> {
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val arcT = (stepInPhrase / 3f) - 0.5f
                    val cx = 0.50f + dir * arcT * 0.44f
                    val cy = 0.46f + sin(phraseIdx * 1.1f + stepInPhrase * 0.65f) * 0.15f
                    cx.coerceIn(0.22f, 0.78f) to cy.coerceIn(0.32f, 0.66f)
                }
                else -> {
                    val a = seq * 0.48f
                    (0.50f + 0.24f * sin(a)).coerceIn(0.22f, 0.78f) to
                        (0.48f + 0.14f * cos(a * 0.75f)).coerceIn(0.34f, 0.66f)
                }
            }

            val nx: Float
            val ny: Float
            if (anchor != null) {
                nx = 0.50f
                ny = anchor.normY
            } else if (inSurpriseEnding) {
                nx = rawX.coerceIn(0.22f, 0.78f)
                ny = rawY.coerceIn(0.34f, 0.66f)
            } else {
                val dx = (rawX - prevX).coerceIn(-0.26f, 0.26f)
                val dy = (rawY - prevY).coerceIn(-0.22f, 0.22f)
                nx = (prevX + dx).coerceIn(0.22f, 0.78f)
                ny = (prevY + dy).coerceIn(0.30f, 0.68f)
            }
            prevX = nx
            prevY = ny

            val pType = when (effect) {
                KeyWordEffect.CONSTELLATION_STAR -> PowerUpType.ELECTRIC_HARMONIC
                KeyWordEffect.PRISM_SHATTER -> PowerUpType.PRISM_HARMONY
                KeyWordEffect.ECLIPSE_DARK -> PowerUpType.SLOW_MO
                KeyWordEffect.MAGNETAR_WAR -> PowerUpType.BLAST
                KeyWordEffect.TRACTOR_REACH, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                else -> PowerUpType.NONE
            }

            val desiredLead = when {
                anchor != null -> 1.45f
                inIntroDrift -> 2.55f
                inWhispyBreakdown -> 3.30f
                inSpokenFloat -> 2.85f
                inInterlude1 || inInterlude2 -> 2.75f
                inOutroSlowdown -> 3.20f
                inSurpriseEnding -> 1.40f
                else -> 1.95f
            }
            val maxAllowedLead = (t - (prevSpawnSec + 0.03f)).coerceAtLeast(1.15f)
            val effectiveLead = minOf(desiredLead, maxAllowedLead)
            prevSpawnSec = t - effectiveLead

            val swirl = when {
                anchor != null -> 0f
                inWhispyBreakdown -> 1.0f
                inSpokenFloat -> 0.65f
                inInterlude1 || inInterlude2 -> 0.45f
                else -> 0f
            }

            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq,
                    timestampSec = t,
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = isGolden,
                    keyWordEffect = effect,
                    customLeadSec = effectiveLead,
                    swirlStrength = swirl,
                    goldenTier = gTier
                )
            )
            seq++

            if (inOutroSlowdown && (beatIdx + 3) * beatSec > 156.2f) {
                // Deceptive lull before 4 unexpected surprise pegs at 158.86s - 163.64s (track ends at 166s)!
                beatIdx = 233 // 233 * 0.681818 = 158.86s
            } else {
                val rawStep = when {
                    anchor != null -> 2
                    inDrumCadence || inDrumRiser -> 1
                    inIntroDrift || inWhispyBreakdown || inSpokenFloat -> 2
                    inInterlude1 || inInterlude2 -> 2
                    inOutroSlowdown -> 3
                    inSurpriseEnding -> 2
                    else -> if (seq % 2 == 0) 1 else 2
                }
                var nextBeat = beatIdx + rawStep
                for (i in specialBeatIndices.indices) {
                    val sb = specialBeatIndices[i]
                    if (i !in usedAnchors && sb > beatIdx && sb < nextBeat) {
                        nextBeat = sb
                    }
                }
                beatIdx = nextBeat
            }
        }
        return cues
    }

    /**
     * One-line plug-and-play track builder for any new `.mp3` single Ethan provides (`track_03`, `track_04`, etc.).
     * Automatically builds beat-quantized multi-directional orbs, 24K Milestone Orbs, flowy breakdowns, and surprise ending pegs!
     */
    fun createOfficialReleaseTrack(
        index: Int,
        title: String,
        bpm: Float,
        durationSec: Int,
        unlockCost: Int = 1500,
        audioResName: String = "track_%02d".format(index)
    ): DemoTrack {
        val dur = durationSec.coerceAtLeast(60).toFloat()
        val sections = listOf(
            TrackSection(0f, "INTRO DRIFT", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
            TrackSection(dur * 0.05f, "FIRST TRANSITION • DRUM GROOVE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.65f, clearEnemiesOnEnter = false, allowAnomalies = false),
            TrackSection(dur * 0.11f, "CHORUS I • COLLECT 24K ORB", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
            TrackSection(dur * 0.26f, "FLOWY WHISPY BREAKDOWN", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
            TrackSection(dur * 0.42f, "CHORUS II • 24K HYPER-SPECTACLE", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
            TrackSection(dur * 0.58f, "VOCAL INTERLUDE • DRIFT", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
            TrackSection(dur * 0.72f, "PRISM VISUAL WOW", JuiceTier.EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
            TrackSection(dur * 0.86f, "ENDING RIFF CLIMAX", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.92f, clearEnemiesOnEnter = false, allowAnomalies = true),
            TrackSection(dur * 0.92f, "SLOWDOWN • SURPRISE OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
        )

        val beatSec = 60f / bpm.coerceIn(65f, 180f)
        val specialMoments = listOf(
            KeyWordAnchor(quantizeToBeat(dur * 0.11f, beatSec), KeyWordEffect.CONSTELLATION_STAR, goldenTier = 1, normY = 0.28f),
            KeyWordAnchor(quantizeToBeat(dur * 0.42f, beatSec), KeyWordEffect.CONSTELLATION_STAR, goldenTier = 2, normY = 0.28f),
            KeyWordAnchor(quantizeToBeat(dur * 0.58f, beatSec), KeyWordEffect.ECLIPSE_DARK, goldenTier = 0, normY = 0.34f),
            KeyWordAnchor(quantizeToBeat(dur * 0.72f, beatSec), KeyWordEffect.PRISM_SHATTER, goldenTier = 0, normY = 0.30f),
            KeyWordAnchor(quantizeToBeat(dur * 0.86f, beatSec), KeyWordEffect.CONSTELLATION_STAR, goldenTier = 2, normY = 0.28f)
        )
        val specialBeats = specialMoments.map { (it.timestampSec / beatSec).roundToInt() }
        val used = HashSet<Int>()
        val cues = ArrayList<BeatOrbCue>(220)
        var beatIdx = (1.35f / beatSec).roundToInt().coerceAtLeast(2)
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.48f
        var prevSpawnSec = -5.0f

        while (beatIdx * beatSec <= dur - 2.0f) {
            val t = beatIdx * beatSec
            val inFlowy = t in (dur * 0.26f)..(dur * 0.33f) || t in (dur * 0.58f)..(dur * 0.64f)
            val inSurprise = t >= dur - 6.5f
            val inLull = t in (dur - 10.0f)..(dur - 6.6f)
            if (inLull) {
                beatIdx++
                continue
            }
            var mIdx = -1
            for (i in specialBeats.indices) {
                if (i !in used && abs(specialBeats[i] - beatIdx) <= 1) {
                    mIdx = i
                    used.add(i)
                    break
                }
            }
            val anchor = if (mIdx != -1) specialMoments[mIdx] else null
            val eff = anchor?.effect ?: KeyWordEffect.NONE
            val nx = if (anchor != null) 0.50f else (prevX + (0.50f + 0.26f * sin(seq * 0.54f + index) - prevX).coerceIn(-0.24f, 0.24f)).coerceIn(0.22f, 0.78f)
            val ny = if (anchor != null) anchor.normY else (prevY + (0.48f + 0.14f * cos(seq * 0.41f + index) - prevY).coerceIn(-0.20f, 0.20f)).coerceIn(0.32f, 0.66f)
            prevX = nx
            prevY = ny
            val pType = when (eff) {
                KeyWordEffect.CONSTELLATION_STAR -> PowerUpType.ELECTRIC_HARMONIC
                KeyWordEffect.PRISM_SHATTER -> PowerUpType.PRISM_HARMONY
                KeyWordEffect.ECLIPSE_DARK -> PowerUpType.SLOW_MO
                else -> PowerUpType.NONE
            }
            val desiredLead = when {
                anchor != null -> 1.45f
                inFlowy -> 3.05f
                inSurprise -> 1.40f
                t < 8.0f -> 2.50f
                else -> 1.95f
            }
            val effectiveLead = minOf(desiredLead, (t - (prevSpawnSec + 0.03f)).coerceAtLeast(1.15f))
            prevSpawnSec = t - effectiveLead
            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq++,
                    timestampSec = t,
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = eff == KeyWordEffect.CONSTELLATION_STAR,
                    keyWordEffect = eff,
                    customLeadSec = effectiveLead,
                    swirlStrength = if (anchor == null && inFlowy) 0.9f else 0f,
                    goldenTier = anchor?.goldenTier ?: 0
                )
            )
            var nextBeat = beatIdx + if (anchor != null || inFlowy || inSurprise) 2 else if (seq % 2 == 0) 1 else 2
            for (i in specialBeats.indices) {
                val sb = specialBeats[i]
                if (i !in used && sb > beatIdx && sb < nextBeat) nextBeat = sb
            }
            beatIdx = nextBeat
        }

        return DemoTrack(
            index = index,
            title = title,
            bpm = bpm,
            durationSec = durationSec,
            unlockCost = unlockCost,
            audioResName = audioResName,
            sections = sections,
            orbChart = cues
        )
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
                TrackSection(0f, "INTRO DRIFT", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(7.0f, "FIRST TRANSITION • DRUM CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.65f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(15.0f, "CHORUS I • COLLECT 24K ORB", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(31.0f, "VERSE ONE • GROOVE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.70f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(45.0f, "FLOWY WHISPY BREAKDOWN", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(60.0f, "ETHEREAL INTERLUDE I", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(68.5f, "DRUM RISER • CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(77.9f, "CHORUS II • 24K HYPER-SPECTACLE", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(93.0f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(108.0f, "SPOKEN VOCAL FLOAT", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(115.5f, "BUILD-UP • CADENCE", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(139.0f, "PRISM VISUAL WOW", JuiceTier.EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(156.0f, "VOCAL OOOHS • INTERLUDE II", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(170.0f, "RESOLVING CADENCE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(185.0f, "ENDING RIFF CLIMAX", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.92f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(200.0f, "SLOWDOWN • SURPRISE OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
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
                TrackSection(0f, "INTRO DRIFT", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(9.5f, "FIRST TRANSITION • DRUM GROOVE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.65f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(21.0f, "CHORUS I • COLLECT 24K ORB", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(32.0f, "PRE-CHORUS • GROOVE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.75f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(41.5f, "FLOWY WHISPY BREAKDOWN", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(54.5f, "ETHEREAL INTERLUDE I", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(63.0f, "DRUM RE-ENTRY • CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(73.6f, "CHORUS II • 24K HYPER-SPECTACLE", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(91.5f, "VOCAL DRIFT • INTERLUDE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(109.0f, "PRISM VISUAL WOW", JuiceTier.EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(118.5f, "VOCAL HARMONY • INTERLUDE II", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(131.5f, "RESOLVING CADENCE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.75f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(141.0f, "ENDING RIFF CLIMAX", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.92f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(149.0f, "SLOWDOWN • SURPRISE OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
            ),
            orbChart = buildNumberOneOrbChart()
        )
        // Add future Ethan Correa singles in 1 line as `.mp3` masters arrive:
        // createOfficialReleaseTrack(index = 3, title = "...", bpm = 110f, durationSec = 195, unlockCost = 1500)
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

    fun nextSectionAfter(trackIndex: Int, positionSec: Float): TrackSection? {
        val track = getTrack(trackIndex)
        return track.sections.firstOrNull { it.timestampSec > positionSec }
    }

    fun unlockCostForTrack(trackIndex: Int): Int {
        return getTrack(trackIndex).unlockCost
    }

    fun resolveRawAudioId(context: Context, audioResName: String): Int {
        return context.resources.getIdentifier(audioResName, "raw", context.packageName)
    }
}
