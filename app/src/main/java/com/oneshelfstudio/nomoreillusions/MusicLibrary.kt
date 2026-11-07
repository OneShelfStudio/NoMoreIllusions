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
    val isMusicVideoOrb: Boolean = false,
    val customLeadSec: Float = 0f,
    val swirlStrength: Float = 0f,
    val goldenTier: Int = 0
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

        // Exact 5 Video Relics prior to Montage 1 (1:00) and exact 5 prior to Montage 2 (2:36):
        val videoRelicTimestamps = listOf(
            33.39f, 39.68f, 46.45f, 53.23f, 59.52f,
            116.61f, 126.29f, 135.48f, 146.13f, 155.81f
        )
        val specialBeatIndices = specialMoments.map { (it.timestampSec / beatSec).roundToInt() }
        val videoBeatIndices = videoRelicTimestamps.map { (it / beatSec).roundToInt() }

        val usedAnchors = HashSet<Int>()
        val usedVideoRelics = HashSet<Int>()
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
            val inPreMontage1 = t in 49.4f..59.8f
            val inMontage1 = t in 59.8f..68.4f
            val inDrumRiser = t in 68.4f..77.2f
            val inChorus2 = t in 78.2f..93.0f
            val inVerse2 = t in 93.0f..108.0f
            val inSpokenFloat = t in 108.0f..115.2f
            val inBuildUp2 = t in 115.2f..138.2f
            val inChorus3 = t in 138.2f..155.9f
            val inMontage2 = t in 155.9f..170.0f
            val inPostMontageCadence = t in 170.0f..184.8f
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

            var matchedVideoIdx = -1
            if (anchor == null) {
                for (vi in videoBeatIndices.indices) {
                    if (vi !in usedVideoRelics && abs(videoBeatIndices[vi] - beatIdx) <= 1) {
                        matchedVideoIdx = vi
                        usedVideoRelics.add(vi)
                        break
                    }
                }
            }
            val isVideoCue = matchedVideoIdx != -1

            val phraseIdx = (seq - 1) / 4
            val stepInPhrase = (seq - 1) % 4
            val (rawX, rawY) = when {
                anchor != null -> 0.50f to anchor.normY
                inSurpriseEnding -> {
                    val sneakPositions = arrayOf(0.25f to 0.44f, 0.75f to 0.56f, 0.30f to 0.60f, 0.72f to 0.42f)
                    sneakPositions[(seq) % sneakPositions.size]
                }
                inMontage1 || inMontage2 -> {
                    val edgeX = if (seq % 2 == 0) 0.76f else 0.24f
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
                inVerse1 || inPreMontage1 || inVerse2 || inBuildUp2 || inPostMontageCadence -> {
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
                inMontage1 || inMontage2 -> 2.75f
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
                inMontage1 || inMontage2 -> 0.35f
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
                    isMusicVideoOrb = isVideoCue,
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
                    inMontage1 || inMontage2 -> 3
                    inOutroSlowdown -> 4
                    inSurpriseEnding -> 3
                    inChorus1 || inChorus2 || inChorus3 || inClimaxRiff -> if (seq % 3 == 0) 1 else 2
                    else -> if (seq % 2 == 0) 1 else 2
                }
                var nextBeat = beatIdx + rawStep
                // Never step over an un-emitted Special Milestone or Video Relic beat!
                for (i in specialBeatIndices.indices) {
                    val sb = specialBeatIndices[i]
                    if (i !in usedAnchors && sb > beatIdx && sb < nextBeat) {
                        nextBeat = sb
                    }
                }
                for (vi in videoBeatIndices.indices) {
                    val vb = videoBeatIndices[vi]
                    if (vi !in usedVideoRelics && vb > beatIdx && vb < nextBeat) {
                        nextBeat = vb
                    }
                }
                beatIdx = nextBeat
            }
        }
        return cues
    }

    /**
     * Builds the Beat-Quantized Orb Chart for Track 02: Number One (88.0 BPM, beat = 0.6818s, 166s total).
     * Mirrors the exact dramatic choreography of Track 01:
     * - -1.0s..0.0s: Pre-music screen movement + slow-falling initial orbs
     * - 0:00..0:09.5: Music starts, orbs pulse to beat
     * - 0:09.5..0:20.5: Transition 1 + clear quarter-note drum groove
     * - 0:21.1: Top-center Special 24K Milestone Orb (Tier 1)
     * - 0:32..0:54: Music shift + 5 Video Relics + 0:41.5-0:46.5 flowy/whispy slow-mo swirl
     * - 0:54.5..1:03: Slow-Mo Video Montage 1
     * - 1:03..1:13: Clear drum beat stream
     * - 1:13.6: Top-center Special 24K Milestone Orb (Tier 2 Hyper-Spectacle)
     * - 1:32: Intimate spoken/rap drift
     * - 1:38..1:58: Cadence + 1:49 Visual Wow + 2nd set of 5 Video Relics
     * - 1:58.5..2:11.5: Slow-Mo Crossfade Video Montage 2
     * - 2:21: Riff + 24K Climax
     * - 2:29..2:43.6: Slowdown + deceptive lull + 4 surprise ending pegs before 2:46!
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

        // Exact 5 Video Relics prior to Montage 1 (0:54.5) and exact 5 prior to Montage 2 (1:58.5):
        val videoRelicTimestamps = listOf(
            33.41f, 38.86f, 43.64f, 49.09f, 53.86f,
            96.14f, 101.59f, 106.36f, 112.50f, 117.95f
        )
        val specialBeatIndices = specialMoments.map { (it.timestampSec / beatSec).roundToInt() }
        val videoBeatIndices = videoRelicTimestamps.map { (it / beatSec).roundToInt() }

        val usedAnchors = HashSet<Int>()
        val usedVideoRelics = HashSet<Int>()
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
            val inMontage1 = t in 54.4f..63.0f
            val inDrumRiser = t in 63.0f..73.0f
            val inSpokenFloat = t in 91.5f..97.0f
            val inMontage2 = t in 118.4f..131.5f
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

            var matchedVideoIdx = -1
            if (anchor == null) {
                for (vi in videoBeatIndices.indices) {
                    if (vi !in usedVideoRelics && abs(videoBeatIndices[vi] - beatIdx) <= 1) {
                        matchedVideoIdx = vi
                        usedVideoRelics.add(vi)
                        break
                    }
                }
            }
            val isVideoCue = matchedVideoIdx != -1

            val phraseIdx = (seq - 1) / 4
            val stepInPhrase = (seq - 1) % 4
            val (rawX, rawY) = when {
                anchor != null -> 0.50f to anchor.normY
                inSurpriseEnding -> {
                    val sneakPositions = arrayOf(0.26f to 0.45f, 0.74f to 0.55f, 0.30f to 0.58f, 0.72f to 0.42f)
                    sneakPositions[seq % sneakPositions.size]
                }
                inMontage1 || inMontage2 -> {
                    val edgeX = if (seq % 2 == 0) 0.76f else 0.24f
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
                inMontage1 || inMontage2 -> 2.75f
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
                inMontage1 || inMontage2 -> 0.35f
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
                    isMusicVideoOrb = isVideoCue,
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
                    inMontage1 || inMontage2 -> 2
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
                for (vi in videoBeatIndices.indices) {
                    val vb = videoBeatIndices[vi]
                    if (vi !in usedVideoRelics && vb > beatIdx && vb < nextBeat) {
                        nextBeat = vb
                    }
                }
                beatIdx = nextBeat
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
                TrackSection(0f, "INTRO DRIFT", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(7.0f, "FIRST TRANSITION • DRUM CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.65f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(15.0f, "CHORUS I • COLLECT 24K ORB", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(31.0f, "VERSE ONE • VIDEO RELIC BUILD", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.70f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(45.0f, "FLOWY WHISPY BREAKDOWN", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(60.0f, "VIDEO MONTAGE I • SLOW-MO", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(68.5f, "DRUM RISER • CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(77.9f, "CHORUS II • 24K HYPER-SPECTACLE", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(93.0f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(108.0f, "SPOKEN VOCAL FLOAT", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(115.5f, "BUILD-UP • VIDEO RELIC BUILD II", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(139.0f, "PRISM VISUAL WOW", JuiceTier.EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(156.0f, "VIDEO MONTAGE II • VOCAL OOOHS", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
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
                TrackSection(32.0f, "PRE-CHORUS • VIDEO RELIC BUILD", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.75f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(41.5f, "FLOWY WHISPY BREAKDOWN", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(54.5f, "VIDEO MONTAGE I • SLOW-MO", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(63.0f, "DRUM RE-ENTRY • CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(73.6f, "CHORUS II • 24K HYPER-SPECTACLE", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(91.5f, "VOCAL DRIFT • VIDEO RELIC BUILD II", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(109.0f, "PRISM VISUAL WOW", JuiceTier.EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(118.5f, "VIDEO MONTAGE II • SLOW-MO", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(131.5f, "RESOLVING CADENCE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.75f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(141.0f, "ENDING RIFF CLIMAX", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.92f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(149.0f, "SLOWDOWN • SURPRISE OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
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
