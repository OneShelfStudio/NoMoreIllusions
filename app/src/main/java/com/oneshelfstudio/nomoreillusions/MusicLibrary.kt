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
        // ONLY Rare Milestone Orbs placed at major musical transitions & drops across the entire 3:39 track
        val specialMoments = listOf(
            KeyWordAnchor(14.52f, KeyWordEffect.CONSTELLATION_STAR), // 0:14.5 Intro Drop into Chorus 1 (⚡ Electric Overdrive)
            KeyWordAnchor(45.97f, KeyWordEffect.PRISM_SHATTER),      // 0:46.0 Pre-Chorus Vocal Surge (✨ Prism Vocal Bloom)
            KeyWordAnchor(75.48f, KeyWordEffect.MAGNETAR_WAR),       // 1:15.5 Chorus 2 Drop (💥 Solar Nova Blast)
            KeyWordAnchor(124.0f, KeyWordEffect.ECLIPSE_DARK),       // 2:04.0 Hook Vocal Float (🌀 Vortex Slow-Mo)
            KeyWordAnchor(137.42f, KeyWordEffect.CONSTELLATION_STAR),// 2:17.5 Chorus 3 Drop (⚡ Electric Overdrive)
            KeyWordAnchor(183.87f, KeyWordEffect.CONSTELLATION_STAR) // 3:03.9 Ending Breakdown Climax (⚡ Electric Overdrive)
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
                KeyWordEffect.CONSTELLATION_STAR -> PowerUpType.ELECTRIC_HARMONIC
                KeyWordEffect.PRISM_SHATTER -> PowerUpType.PRISM_HARMONY
                KeyWordEffect.ECLIPSE_DARK -> PowerUpType.SLOW_MO
                KeyWordEffect.MAGNETAR_WAR -> PowerUpType.BLAST
                KeyWordEffect.TRACTOR_REACH, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                else -> PowerUpType.NONE
            }

            cues.add(
                BeatOrbCue(
                    sequenceIndex = seq,
                    timestampSec = t,
                    normX = nx,
                    normY = ny,
                    powerUpType = pType,
                    isGoldenCue = isGolden,
                    keyWordEffect = effect
                )
            )
            seq++
            beatIdx += when {
                inIntroLull || inBreakdown || inOutro -> 2 // Active 2-beat pocket (no empty dead pauses)
                inRiser -> 1                               // Every single quarter-note beat on drum risers!
                inChorusOrFinale -> if (seq % 3 == 0) 1 else 2 // High-energy chorus drive
                else -> if (seq % 2 == 0) 1 else 2         // Syncopated hi-hat pocket groove
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
        // ONLY Rare Milestone Orbs placed at major musical transitions & drops across the entire 2:46 track
        val specialMoments = listOf(
            KeyWordAnchor(44.32f, KeyWordEffect.MAGNETAR_WAR),       // 0:44.3 Pre-Chorus I Swell (💥 Solar Nova Blast)
            KeyWordAnchor(54.55f, KeyWordEffect.CONSTELLATION_STAR), // 0:54.5 Chorus I Drop (⚡ Electric Overdrive)
            KeyWordAnchor(92.05f, KeyWordEffect.PRISM_SHATTER),      // 1:32.0 Verse Two "Rap" (✨ Prism Vocal Bloom)
            KeyWordAnchor(109.09f, KeyWordEffect.ECLIPSE_DARK),      // 1:49.1 Chorus II Slow-Mo Pocket (🌀 Vortex Slow-Mo)
            KeyWordAnchor(129.55f, KeyWordEffect.CONSTELLATION_STAR) // 2:09.5 Ending Breakdown Climax (⚡ Electric Overdrive)
        )

        val cues = ArrayList<BeatOrbCue>(260)
        val usedAnchors = HashSet<Int>()
        var beatTime = 2.0f * beatSec
        var seq = 1
        var prevX = 0.50f
        var prevY = 0.52f

        var verseCadenceIdx = 0
        val verseStepPatterns = listOf(
            floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f),      // Bar 1: Steady 4-beat pulse
            floatArrayOf(1.0f, 1.0f, 0.5f, 1.5f),      // Bar 2: Vocal syncopation on 3.5
            floatArrayOf(1.0f, 0.5f, 0.5f, 1.0f, 1.0f), // Bar 3: Quick vocal rhyme bounce
            floatArrayOf(1.5f, 0.5f, 1.0f, 1.0f)       // Bar 4: Vocal hold with delayed entry
        )
        val chorusStepPattern = floatArrayOf(0.75f, 0.75f, 0.5f, 1.0f, 1.0f)
        var chorusStepToggle = 0

        while (beatTime <= 161.5f) {
            val t = beatTime
            val inIntroOrOutro = t < 9.8f || t >= 152.5f
            val isVerse = (t in 22.0f..44.0f) || (t in 76.0f..91.5f)
            val isPreChorus = (t in 44.0f..51.0f) || (t in 101.0f..105.0f)
            val inHush = (t in 51.0f..54.3f) || (t in 105.0f..108.8f)
            val isRap = t in 91.5f..98.5f
            val isChorus = (t in 54.5f..76.0f) || (t in 109.0f..129.0f)
            val isClimax = t in 129.0f..152.0f

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
                isRap -> {
                    val dir = if (phraseIdx % 2 == 0) 1f else -1f
                    val lx = 0.50f + dir * ((stepInPhrase - 1.5f) * 0.14f)
                    val ly = 0.36f + (stepInPhrase * 0.08f)
                    lx.coerceIn(0.24f, 0.76f) to ly.coerceIn(0.30f, 0.70f)
                }
                inIntroOrOutro || inHush -> {
                    0.50f + 0.15f * cos(seq * 0.65f) to 0.50f + 0.09f * sin(seq * 0.65f)
                }
                isChorus -> {
                    val chorusPositions = floatArrayOf(0.40f, 0.70f, 0.25f, 0.60f, 0.80f, 0.30f)
                    val cx = chorusPositions[(seq - 1) % chorusPositions.size]
                    val cy = 0.44f + 0.15f * sin(seq * 0.8f)
                    cx.coerceIn(0.20f, 0.80f) to cy.coerceIn(0.28f, 0.72f)
                }
                isVerse -> {
                    val barProg = (seq % 8) / 8.0f
                    val vx = 0.30f + 0.40f * (0.5f + 0.5f * sin(barProg * 6.28318f))
                    val vy = 0.42f + 0.12f * cos(barProg * 3.14159f)
                    vx.coerceIn(0.22f, 0.78f) to vy.coerceIn(0.30f, 0.72f)
                }
                isPreChorus || isClimax -> {
                    val prog = (seq % 6) / 6.0f
                    val px = if (phraseIdx % 2 == 0) (0.25f + prog * 0.50f) else (0.75f - prog * 0.50f)
                    val py = 0.38f + 0.22f * prog
                    px.coerceIn(0.20f, 0.80f) to py.coerceIn(0.28f, 0.72f)
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
                KeyWordEffect.CONSTELLATION_STAR -> PowerUpType.ELECTRIC_HARMONIC
                KeyWordEffect.PRISM_SHATTER -> PowerUpType.PRISM_HARMONY
                KeyWordEffect.ECLIPSE_DARK -> PowerUpType.SLOW_MO
                KeyWordEffect.MAGNETAR_WAR -> PowerUpType.BLAST
                KeyWordEffect.TRACTOR_REACH, KeyWordEffect.BINARY_HEART -> PowerUpType.SHIELD
                else -> PowerUpType.NONE
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

            val stepBeats = when {
                inIntroOrOutro -> 1.5f
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
                TrackSection(0f, "INTRO", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(7.25f, "INTRO BUILD", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.68f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(14.5f, "CHORUS I", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 2, enemySpeedMult = 0.88f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(31.0f, "VERSE ONE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(45.5f, "PRE-CHORUS", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(62.0f, "BREAKDOWN", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(69.25f, "DRUM RISER", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.78f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(75.5f, "CHORUS II", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.92f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(93.0f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.74f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(108.0f, "VERSE SURGE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(124.0f, "BREAKDOWN II", JuiceTier.SECTION_SHIFT, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(131.25f, "DRUM RISER II", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.80f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(137.5f, "CHORUS III", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.95f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(155.0f, "BRIDGE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(169.5f, "BRIDGE BUILD", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.86f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(183.5f, "ENDING BREAKDOWN", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 4, enemySpeedMult = 0.98f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(201.0f, "SUB-BASS PULSE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.OVERDRIVE_SHATTER, isGoldenOverdrive = true, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(207.8f, "OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
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
                TrackSection(0f, "INTRO", JuiceTier.NONE, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(9.8f, "INTRO GROOVE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.68f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(21.0f, "VERSE ONE", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(43.5f, "PRE-CHORUS", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(51.0f, "BUILD-UP", JuiceTier.NONE, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(54.5f, "CHORUS I", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.90f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(75.5f, "VERSE TWO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 1, enemySpeedMult = 0.72f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(91.5f, "VERSE II CADENCE", JuiceTier.SLIGHT_EXTRA_JUICE, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.85f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(98.5f, "PRE-CHORUS II", JuiceTier.SECTION_SHIFT, StageMode.SPEED_PORTAL, isGoldenOverdrive = false, maxEnemies = 2, enemySpeedMult = 0.82f, clearEnemiesOnEnter = false, allowAnomalies = false),
                TrackSection(105.0f, "BUILD-UP II", JuiceTier.NONE, StageMode.MAGIC_LANES, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false),
                TrackSection(109.0f, "CHORUS II", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 3, enemySpeedMult = 0.94f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(129.0f, "ENDING BREAKDOWN", JuiceTier.EXTRA_JUICE, StageMode.GOLDEN_OVERDRIVE, isGoldenOverdrive = true, maxEnemies = 4, enemySpeedMult = 0.98f, clearEnemiesOnEnter = false, allowAnomalies = true),
                TrackSection(152.5f, "OUTRO", JuiceTier.SECTION_SHIFT, StageMode.ABYSSAL_WAVE, isGoldenOverdrive = false, maxEnemies = 0, enemySpeedMult = 0.0f, clearEnemiesOnEnter = true, allowAnomalies = false)
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
