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
    val isMusicVideoOrb: Boolean = false
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
        // Master Choreographed Milestone Drops per Ethan's musical timeline:
        val specialMoments = listOf(
            KeyWordAnchor(15.00f, KeyWordEffect.CONSTELLATION_STAR), // 0:15.0 24K Milestone 1 near top-center!
            KeyWordAnchor(45.50f, KeyWordEffect.PRISM_SHATTER),      // 0:45.5 Whispy flow breakdown ("losing your battles")
            KeyWordAnchor(77.42f, KeyWordEffect.CONSTELLATION_STAR), // 1:17.4 24K Milestone 2 with enhanced spectacle!
            KeyWordAnchor(108.0f, KeyWordEffect.ECLIPSE_DARK),       // 1:48.0 Spoken vocal float (intimate slow-mo)
            KeyWordAnchor(137.42f, KeyWordEffect.CONSTELLATION_STAR),// 2:17.5 Chorus 3 Drop (⚡ Electric Overdrive)
            KeyWordAnchor(185.00f, KeyWordEffect.CONSTELLATION_STAR) // 3:05.0 Ending Climax Riff (⚡ Electric Overdrive)
        )

        // Exact Video Relic timestamps (5 relics per montage trigger):
        // Set 1 (triggers 1:00 montage): ~33.8s (beat 70), ~39.7s (beat 82), ~45.5s (beat 94), ~51.3s (beat 106), ~57.6s (beat 119)
        // Set 2 (triggers 2:36 montage): ~112.3s (beat 232), ~122.4s (beat 253), ~132.6s (beat 274), ~142.7s (beat 295), ~153.9s (beat 318)
        val videoRelicBeats = setOf(70, 82, 94, 106, 119, 232, 253, 274, 295, 318)

        val cues = ArrayList<BeatOrbCue>(200)
        val usedAnchors = HashSet<Int>()
        var beatIdx = 2 // Start on beat 2 (0.96s) so initial notes are already visible descending during pre-music drift!
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.52f
        while (beatIdx * beatSec <= 212.0f) {
            val t = beatIdx * beatSec
            val inIntroDrift = t < 7.0f
            val inDrumCadence = t in 7.0f..15.0f
            val inChorus1 = t in 15.0f..31.0f
            val inVerse1 = t in 31.0f..44.5f
            val inWhispyBreakdown = t in 44.5f..49.5f
            val inPreMontage1 = t in 49.5f..60.0f
            val inMontage1 = t in 60.0f..68.5f
            val inDrumRiser = t in 68.5f..77.0f
            val inChorus2 = t in 77.0f..93.0f
            val inVerse2 = t in 93.0f..108.0f
            val inSpokenFloat = t in 108.0f..124.0f
            val inBuildUp2 = t in 124.0f..137.5f
            val inChorus3 = t in 137.5f..155.0f
            val inMontage2 = t in 155.0f..170.0f // 2:35 - 2:50
            val inPostMontageCadence = t in 170.0f..184.0f
            val inClimaxRiff = t in 184.0f..202.0f
            val inOutroSlowdown = t >= 202.0f

            var matchedIdx = -1
            var bestDist = beatSec * 0.9f
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

            // Sequential 4-Beat Phrase Choreography (clean parallel streams, reachable by single thumb):
            val phraseIdx = (seq - 1) / 4
            val stepInPhrase = (seq - 1) % 4
            val (rawX, rawY) = when {
                // 15.0s milestone orb MUST arrive near top-center!
                matchedIdx == 0 -> 0.50f to 0.44f
                // 1:17.4 second 24K milestone also centered
                matchedIdx == 2 -> 0.50f to 0.46f
                inDrumCadence || inDrumRiser -> {
                    // Crisp, alternating thumb-friendly rhythmic staircase
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val lx = 0.50f + dir * ((stepInPhrase - 1.5f) / 3f) * 0.36f
                    lx.coerceIn(0.24f, 0.76f) to 0.52f
                }
                inWhispyBreakdown || inSpokenFloat -> {
                    // Centered gentle drifting arc
                    val a = seq * 0.55f
                    (0.50f + 0.16f * sin(a)) to (0.48f + 0.08f * cos(a))
                }
                inOutroSlowdown -> {
                    // Surprise false-ending notes on alternate sides
                    val side = if (seq % 2 == 0) 0.68f else 0.32f
                    side to 0.50f
                }
                inChorus1 || inChorus2 || inChorus3 || inClimaxRiff -> {
                    // Sweeping wide arcs across chorus drops
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val arcT = (stepInPhrase / 3f) - 0.5f
                    val cx = 0.50f + dir * arcT * 0.42f
                    val cy = 0.48f + sin(phraseIdx * 1.1f + stepInPhrase * 0.65f) * 0.14f
                    cx.coerceIn(0.22f, 0.78f) to cy.coerceIn(0.32f, 0.68f)
                }
                else -> {
                    // Gentle figure-8 verse pocket
                    val a = seq * 0.46f
                    (0.50f + 0.22f * sin(a)).coerceIn(0.24f, 0.76f) to
                        (0.50f + 0.14f * cos(a * 0.75f)).coerceIn(0.32f, 0.68f)
                }
            }

            // Clamp maximum step distance from previous beat so consecutive beats are always smoothly reachable
            val dx = (rawX - prevX).coerceIn(-0.24f, 0.24f)
            val dy = (rawY - prevY).coerceIn(-0.20f, 0.20f)
            val nx = if (matchedIdx == 0 || matchedIdx == 2) 0.50f else (prevX + dx).coerceIn(0.22f, 0.78f)
            val ny = if (matchedIdx == 0 || matchedIdx == 2) 0.46f else (prevY + dy).coerceIn(0.30f, 0.70f)
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

            val isVideoCue = (effect == KeyWordEffect.NONE && !isGolden) && (beatIdx in videoRelicBeats)

            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq,
                    timestampSec = t,
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = isGolden,
                    keyWordEffect = effect,
                    isMusicVideoOrb = isVideoCue
                )
            )
            seq++
            beatIdx += when {
                inDrumCadence || inDrumRiser || inWhispyBreakdown -> 1 // Clear drum stream and flowing celestial cluster
                inIntroDrift || inSpokenFloat -> 2
                inChorus1 || inChorus2 || inChorus3 || inClimaxRiff -> if (seq % 3 == 0) 1 else 2
                inOutroSlowdown -> 4 // Spaced out false ending notes!
                else -> if (seq % 2 == 0) 1 else 2
            }
        }
        return cues
    }

    /**
     * Builds the Beat-Quantized Orb Chart for Track 02: Number One (88.0 BPM, beat = 0.6818s).
     * Enhanced with Ethan Correa vocal cadence tracking and open hi-hat pocket groove!
     */
    private fun buildNumberOneOrbChart(): List<BeatOrbCue> {
        val beatSec = 60f / 88f // 0.6818s
        // Master Choreographed Milestone Drops per Ethan's musical timeline:
        val specialMoments = listOf(
            KeyWordAnchor(21.14f, KeyWordEffect.CONSTELLATION_STAR), // 0:21.1 24K Milestone 1 near top-center!
            KeyWordAnchor(44.32f, KeyWordEffect.MAGNETAR_WAR),       // 0:44.3 Pre-Chorus I Swell (💥 Solar Nova Blast)
            KeyWordAnchor(54.55f, KeyWordEffect.CONSTELLATION_STAR), // 0:54.5 Chorus I Drop (⚡ Electric Overdrive)
            KeyWordAnchor(92.05f, KeyWordEffect.CONSTELLATION_STAR), // 1:32.0 24K Milestone 2 on Verse Two Rap drop
            KeyWordAnchor(109.09f, KeyWordEffect.ECLIPSE_DARK),      // 1:49.1 Chorus II Slow-Mo Pocket (🌀 Vortex Slow-Mo)
            KeyWordAnchor(129.55f, KeyWordEffect.CONSTELLATION_STAR) // 2:09.5 Ending Breakdown Climax (⚡ Electric Overdrive)
        )

        // Exact Video Relic beats for Number One (5 relics per montage trigger):
        // Set 1 (triggers 0:54 montage): ~24.5s (beat 36), ~30.0s (beat 44), ~35.5s (beat 52), ~42.3s (beat 62), ~50.5s (beat 74)
        // Set 2 (triggers 1:49 montage): ~70.9s (beat 104), ~80.5s (beat 118), ~90.0s (beat 132), ~98.2s (beat 144), ~106.4s (beat 156)
        val videoRelicBeats = setOf(36, 44, 52, 62, 74, 104, 118, 132, 144, 156)

        val cues = ArrayList<BeatOrbCue>(260)
        val usedAnchors = HashSet<Int>()
        var beatTime = 2.0f * beatSec
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.52f

        var verseCadenceIdx = 0
        val verseStepPatterns = listOf(
            floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f),
            floatArrayOf(1.0f, 1.0f, 0.5f, 1.5f),
            floatArrayOf(1.0f, 0.5f, 0.5f, 1.0f, 1.0f),
            floatArrayOf(1.5f, 0.5f, 1.0f, 1.0f)
        )
        val chorusStepPattern = floatArrayOf(0.75f, 0.75f, 0.5f, 1.0f, 1.0f)
        var chorusStepToggle = 0

        while (beatTime <= 161.5f) {
            val t = beatTime
            val inIntroOrOutro = t < 9.8f || t >= 148.0f
            val isVerse = (t in 22.0f..44.0f) || (t in 65.0f..91.5f)
            val isPreChorus = (t in 44.0f..51.0f) || (t in 98.5f..105.0f)
            val inHush = (t in 51.0f..54.3f) || (t in 105.0f..108.8f)
            val isRap = t in 91.5f..98.5f
            val isChorus = (t in 54.5f..65.0f) || (t in 109.0f..129.0f)
            val isClimax = t in 129.0f..148.0f

            var matchedIdx = -1
            var bestDist = beatSec * 0.9f
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
                matchedIdx == 0 || matchedIdx == 3 -> 0.50f to 0.44f // Centered 24K Milestones
                isRap -> {
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val lx = 0.50f + dir * ((stepInPhrase - 1.5f) * 0.14f)
                    val ly = 0.36f + (stepInPhrase * 0.08f)
                    lx.coerceIn(0.24f, 0.76f) to ly.coerceIn(0.30f, 0.70f)
                }
                inIntroOrOutro || inHush -> {
                    val side = if (seq % 2 == 0) 0.65f else 0.35f
                    side to 0.50f
                }
                isChorus -> {
                    val chorusPositions = floatArrayOf(0.40f, 0.70f, 0.25f, 0.60f, 0.80f, 0.30f)
                    val cx = chorusPositions[(seq - 1) % chorusPositions.size]
                    val cy = 0.44f + 0.15f * sin(seq * 0.8f)
                    cx.coerceIn(0.22f, 0.78f) to cy.coerceIn(0.32f, 0.68f)
                }
                isVerse -> {
                    val barProg = (seq % 8) / 8.0f
                    val vx = 0.30f + 0.40f * (0.5f + 0.5f * sin(barProg * 6.28318f))
                    val vy = 0.42f + 0.12f * cos(barProg * 3.14159f)
                    vx.coerceIn(0.24f, 0.76f) to vy.coerceIn(0.32f, 0.68f)
                }
                isPreChorus || isClimax -> {
                    val prog = (seq % 6) / 6.0f
                    val px = if (phraseIdx % 2 == 0) (0.28f + prog * 0.44f) else (0.72f - prog * 0.44f)
                    val py = 0.40f + 0.18f * prog
                    px.coerceIn(0.22f, 0.78f) to py.coerceIn(0.32f, 0.68f)
                }
                else -> {
                    val ribbonPhase = seq * 0.46f
                    (0.50f + 0.20f * cos(ribbonPhase)).coerceIn(0.24f, 0.76f) to
                        (0.50f + 0.14f * sin(ribbonPhase * 0.72f)).coerceIn(0.32f, 0.68f)
                }
            }

            val dx = (rawX - prevX).coerceIn(-0.24f, 0.24f)
            val dy = (rawY - prevY).coerceIn(-0.20f, 0.20f)
            val nx = if (matchedIdx == 0 || matchedIdx == 3) 0.50f else (prevX + dx).coerceIn(0.22f, 0.78f)
            val ny = if (matchedIdx == 0 || matchedIdx == 3) 0.46f else (prevY + dy).coerceIn(0.30f, 0.70f)
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

            val beatNumber = (t / beatSec).roundToInt()
            val isVideoCue = (effect == KeyWordEffect.NONE && !isGolden) && (beatNumber in videoRelicBeats)

            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq,
                    timestampSec = quantizeToBeat(anchor?.timestampSec ?: t, beatSec),
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = isGolden,
                    keyWordEffect = effect,
                    isMusicVideoOrb = isVideoCue
                )
            )
            seq++

            val stepBeats = when {
                inIntroOrOutro -> 2.0f // Spaced out false-ending pegs
                inHush -> 1.0f
                isChorus -> {
                    val sb = chorusStepPattern[chorusStepToggle % chorusStepPattern.size]
                    chorusStepToggle++
                    sb
                }
                isRap -> 0.5f
                isVerse -> {
                    val activeBar = verseStepPatterns[verseCadenceIdx % verseStepPatterns.size]
                    val sb = activeBar[(seq - 1) % activeBar.size]
                    if ((seq - 1) % activeBar.size == 0) verseCadenceIdx++
                    sb
                }
                isClimax -> 0.75f
                else -> 1.0f
            }
            beatTime += stepBeats * beatSec
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
                TrackSection(7.0f, "FIRST TRANSITION • DRUM CADENCE", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.65f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(15.0f, "CHORUS I • 24K GOLD OVERDRIVE", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(31.0f, "VERSE ONE • VIDEO RELIC BUILD", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.70f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(45.0f, "FLOWY WHISPY BREAKDOWN", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(60.0f, "VIDEO MONTAGE I • SLOW-MO", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(68.5f, "DRUM RISER • CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(77.42f, "CHORUS II • 24K SPECTACLE", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(93.0f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(108.0f, "SPOKEN VOCAL FLOAT", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(124.0f, "BUILD-UP • VIDEO RELIC BUILD II", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.76f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(137.5f, "CHORUS III • HIGH VELOCITY", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.95f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(156.0f, "VIDEO MONTAGE II • VOCAL OOOHS", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(170.0f, "RESOLVING CADENCE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(185.0f, "ENDING RIFF CLIMAX • 24K", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.95f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(202.0f, "SLOWDOWN • SURPRISE OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
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
                TrackSection(9.8f, "INTRO GROOVE • DRUM CADENCE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.65f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(21.0f, "CHORUS I • 24K GOLD OVERDRIVE", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(43.5f, "PRE-CHORUS • VIDEO RELIC BUILD", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.80f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(54.5f, "VIDEO MONTAGE I • SLOW-MO", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(65.0f, "DRUM RE-ENTRY • VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(91.5f, "VERSE II CADENCE • 24K DROP", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(105.0f, "VOCAL FLOAT • VIDEO RELIC BUILD II", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(129.0f, "VIDEO MONTAGE II • CLIMAX", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.95f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(148.0f, "OUTRO SLOWDOWN • SURPRISE PEGS", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
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
