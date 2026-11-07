package com.oneshelfstudio.nomoreillusions

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.SoundPool

class AudioEngine(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(16)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var hitSound: Int = 0
    private var nearMissSound: Int = 0
    private var focusSound: Int = 0
    private var powerUpSound: Int = 0
    private var levelUpSound: Int = 0
    private var deathSound: Int = 0
    private var victorySound: Int = 0
    private var special24kChimeSound: Int = 0
    private var hyper24kChimeSound: Int = 0
    private var videoRelicChimeSound: Int = 0
    private var videoMontageUnlockSound: Int = 0

    private var themePlayer: MediaPlayer? = null
    private var trackPlayer: MediaPlayer? = null
    private var currentTrackIndex: Int? = null
    private var currentLobbyTrack: LobbyTrack = LobbyTrack.VELVET
    private var currentLobbyRemixMode: TrackRemixMode = TrackRemixMode.ORIGINAL
    private var isTrackFinished: Boolean = false
    private var isDucked: Boolean = false

    // High-Precision Monotonic Clock (eliminates Binder IPC stalls inside 60/120Hz withFrameNanos)
    @Volatile private var syncedPosMs: Float = 0f
    @Volatile private var syncedDurationMs: Float = 219_000f
    @Volatile private var syncNanoTimestamp: Long = System.nanoTime()
    @Volatile private var startGraceUntilNano: Long = 0L
    @Volatile private var clockRunning: Boolean = false
    @Volatile private var activeBpmCached: Float = 124f
    @Volatile private var currentPlaybackSpeed: Float = 1.0f
    @Volatile var loopCycleCount: Int = 0
        private set

    var isMusicEnabled = true
        set(value) {
            field = value
            updateVolumes()
            if (!value) {
                themePlayer?.let { if (it.isPlaying) it.pause() }
            }
        }
    var isSoundEnabled = true

    private var menuNoteIndex = 0
    private val pentatonicScale = listOf(1.0f, 1.122f, 1.259f, 1.498f, 1.681f, 2.0f)

    init {
        loadSounds()
    }

    private fun currentMusicVolume(baseVol: Float = 0.48f): Float {
        if (!isMusicEnabled) return 0f
        return if (isDucked) baseVol * 0.6f else baseVol
    }

    private fun updateVolumes() {
        val themeVol = currentMusicVolume(0.36f)
        val trackVol = currentMusicVolume(0.90f)
        themePlayer?.runCatching { setVolume(themeVol, themeVol) }
        trackPlayer?.runCatching { setVolume(trackVol, trackVol) }
    }

    private fun loadSounds() {
        val hitId = context.resources.getIdentifier("pulse_hit", "raw", context.packageName)
        if (hitId != 0) hitSound = soundPool.load(context, hitId, 1)

        val missId = context.resources.getIdentifier("near_miss_woosh", "raw", context.packageName)
        if (missId != 0) nearMissSound = soundPool.load(context, missId, 1)

        val focusId = context.resources.getIdentifier("focus_activate", "raw", context.packageName)
        if (focusId != 0) focusSound = soundPool.load(context, focusId, 1)

        val powerId = context.resources.getIdentifier("powerup", "raw", context.packageName)
        if (powerId != 0) powerUpSound = soundPool.load(context, powerId, 1)

        val levelId = context.resources.getIdentifier("level_up", "raw", context.packageName)
        if (levelId != 0) levelUpSound = soundPool.load(context, levelId, 1)

        val deathId = context.resources.getIdentifier("death", "raw", context.packageName)
        if (deathId != 0) deathSound = soundPool.load(context, deathId, 1)

        val victoryId = context.resources.getIdentifier("victory_fanfare", "raw", context.packageName)
        if (victoryId != 0) victorySound = soundPool.load(context, victoryId, 1)

        val chime24kId = context.resources.getIdentifier("sfx_special_24k_chime", "raw", context.packageName)
        if (chime24kId != 0) special24kChimeSound = soundPool.load(context, chime24kId, 1)

        val hyper24kId = context.resources.getIdentifier("sfx_hyper_24k_chime", "raw", context.packageName)
        if (hyper24kId != 0) hyper24kChimeSound = soundPool.load(context, hyper24kId, 1)

        val videoRelicId = context.resources.getIdentifier("sfx_video_relic_chime", "raw", context.packageName)
        if (videoRelicId != 0) videoRelicChimeSound = soundPool.load(context, videoRelicId, 1)

        val videoUnlockId = context.resources.getIdentifier("sfx_video_montage_unlock", "raw", context.packageName)
        if (videoUnlockId != 0) videoMontageUnlockSound = soundPool.load(context, videoUnlockId, 1)
    }

    fun playHit() { if (isSoundEnabled && hitSound != 0) soundPool.play(hitSound, 0.35f, 0.35f, 0, 0, 1f) }
    fun playNearMiss() { if (isSoundEnabled && nearMissSound != 0) soundPool.play(nearMissSound, 0.08f, 0.08f, 0, 0, 1.2f) }
    fun playFocus() { if (isSoundEnabled && focusSound != 0) soundPool.play(focusSound, 0.25f, 0.25f, 0, 0, 1f) }
    fun playPowerUp() { if (isSoundEnabled && powerUpSound != 0) soundPool.play(powerUpSound, 0.16f, 0.16f, 0, 0, 1f) }
    fun playLevelUp() { if (isSoundEnabled && levelUpSound != 0) soundPool.play(levelUpSound, 0.22f, 0.22f, 0, 0, 1f) }
    fun playDeath() { if (isSoundEnabled && deathSound != 0) soundPool.play(deathSound, 0.55f, 0.55f, 0, 0, 1f) }

    /**
     * Whisper-quiet velvet feedback so Ethan's music is NEVER drowned out by collection sounds.
     * Regular Flow Pegs play at 0.05f volume; Key-Word Lyric Pegs play a gentle 0.15f harmonic shimmer.
     */
    fun playOrbBeatHit(combo: Int, isKeyWordPeg: Boolean = false) {
        if (!isSoundEnabled) return
        val basePitch = pentatonicScale[combo.coerceAtLeast(0) % pentatonicScale.size]
        if (isKeyWordPeg) {
            if (focusSound != 0) {
                soundPool.play(focusSound, 0.15f, 0.15f, 1, 0, basePitch)
            }
        } else {
            if (powerUpSound != 0) {
                soundPool.play(powerUpSound, 0.05f, 0.05f, 0, 0, basePitch)
            }
        }
    }

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    /**
     * Mastered Celesta + Bowed Glass + Harp Fmaj9 ascending bloom for Tier-1 24K Milestone Orbs.
     */
    fun playSpecial24kChime(pitch: Float = 1.0f) {
        if (!isSoundEnabled) return
        if (special24kChimeSound != 0) {
            soundPool.play(special24kChimeSound, 0.48f, 0.48f, 2, 0, pitch.coerceIn(0.75f, 1.45f))
        } else if (focusSound != 0) {
            soundPool.play(focusSound, 0.36f, 0.36f, 1, 0, 1.25f)
        }
    }

    /**
     * Mastered Harp Glissando + Crystal + Halo Pad shimmer for Tier-2 Hyper-24K & 100% Supernova.
     */
    fun playHyper24kChime() {
        if (!isSoundEnabled) return
        if (hyper24kChimeSound != 0) {
            soundPool.play(hyper24kChimeSound, 0.55f, 0.55f, 2, 0, 1.0f)
        } else {
            playSpecial24kChime(1.12f)
        }
    }

    /**
     * Pitch-stepped Vibraphone + Crystal Bell fifth interval as each VHS Video Relic fills 1/5 -> 4/5.
     */
    fun playVideoRelicChime(step: Int) {
        if (!isSoundEnabled) return
        val stepPitches = floatArrayOf(0.92f, 1.00f, 1.08f, 1.17f, 1.26f)
        val rate = stepPitches[(step - 1).coerceIn(0, stepPitches.lastIndex)]
        if (videoRelicChimeSound != 0) {
            soundPool.play(videoRelicChimeSound, 0.44f, 0.44f, 2, 0, rate)
        } else if (focusSound != 0) {
            soundPool.play(focusSound, 0.32f, 0.32f, 1, 0, rate)
        }
    }

    /**
     * Warm Chorused Rhodes + Harp + Atmosphere resolving bloom when the 5/5 Video Montage unlocks.
     */
    fun playVideoMontageUnlockChime() {
        if (!isSoundEnabled) return
        if (videoMontageUnlockSound != 0) {
            soundPool.play(videoMontageUnlockSound, 0.56f, 0.56f, 2, 0, 1.0f)
        } else {
            playSpecial24kChime(1.08f)
        }
    }

    fun playElectricSurgeStinger() {
        if (!isSoundEnabled) return
        if (special24kChimeSound != 0) {
            soundPool.play(special24kChimeSound, 0.48f, 0.48f, 2, 0, 1.0f)
            return
        }
        val arps = listOf(1.0f, 1.259f, 1.498f, 1.887f)
        arps.forEachIndexed { i, rate ->
            mainHandler.postDelayed({
                if (isSoundEnabled && focusSound != 0) soundPool.play(focusSound, 0.40f, 0.40f, 1, 0, rate * 1.5f)
            }, (i * 35).toLong())
        }
    }

    fun playPrismBloomStinger() {
        if (!isSoundEnabled) return
        if (videoMontageUnlockSound != 0) {
            soundPool.play(videoMontageUnlockSound, 0.48f, 0.48f, 2, 0, 1.08f)
            return
        }
        val chord = listOf(1.0f, 1.259f, 1.498f, 1.887f, 2.245f)
        chord.forEach { rate ->
            if (focusSound != 0) soundPool.play(focusSound, 0.22f, 0.22f, 1, 0, rate)
        }
    }

    fun playSlowMoStinger() {
        if (!isSoundEnabled) return
        if (special24kChimeSound != 0) {
            soundPool.play(special24kChimeSound, 0.44f, 0.44f, 2, 0, 0.84f)
        } else if (nearMissSound != 0) {
            soundPool.play(nearMissSound, 0.45f, 0.45f, 1, 0, 0.65f)
        }
    }

    fun playSolarNovaStinger() {
        if (!isSoundEnabled) return
        if (hyper24kChimeSound != 0) {
            soundPool.play(hyper24kChimeSound, 0.54f, 0.54f, 2, 0, 0.94f)
        } else if (powerUpSound != 0) {
            soundPool.play(powerUpSound, 0.45f, 0.45f, 1, 0, 1.8f)
        }
    }

    fun playAegisStinger() {
        if (!isSoundEnabled) return
        if (special24kChimeSound != 0) {
            soundPool.play(special24kChimeSound, 0.44f, 0.44f, 2, 0, 1.12f)
            return
        }
        if (powerUpSound != 0) soundPool.play(powerUpSound, 0.38f, 0.38f, 1, 0, 1.0f)
        mainHandler.postDelayed({
            if (isSoundEnabled && powerUpSound != 0) soundPool.play(powerUpSound, 0.32f, 0.32f, 1, 0, 2.0f)
        }, 80L)
    }

    /**
     * Signature "Whoosh -> Musical Chime/Beep" transition sound for menus, first-time discovery cards,
     * level completion, and game-over transitions.
     */
    fun playWhooshChime(kind: String = "MODAL") {
        if (!isSoundEnabled) return
        when (kind) {
            "VICTORY" -> {
                if (nearMissSound != 0) soundPool.play(nearMissSound, 0.34f, 0.34f, 1, 0, 1.12f)
                if (hyper24kChimeSound != 0) {
                    soundPool.play(hyper24kChimeSound, 0.52f, 0.52f, 2, 0, 1.0f)
                }
                if (victorySound != 0) {
                    mainHandler.postDelayed({
                        if (isSoundEnabled) soundPool.play(victorySound, 0.78f, 0.78f, 1, 0, 1f)
                    }, 160L)
                }
            }
            "GAME_OVER" -> {
                if (nearMissSound != 0) soundPool.play(nearMissSound, 0.30f, 0.30f, 1, 0, 0.78f)
                mainHandler.postDelayed({
                    if (isSoundEnabled && focusSound != 0) soundPool.play(focusSound, 0.38f, 0.38f, 1, 0, 1.122f)
                }, 75L)
                mainHandler.postDelayed({
                    if (isSoundEnabled && focusSound != 0) soundPool.play(focusSound, 0.34f, 0.34f, 1, 0, 0.84f)
                }, 185L)
            }
            else -> {
                // "MODAL" / "TUTORIAL" / "MENU": Soft air whoosh into a bright E5 -> B5 musical fifth beep
                if (nearMissSound != 0) soundPool.play(nearMissSound, 0.26f, 0.26f, 1, 0, 0.98f)
                mainHandler.postDelayed({
                    if (isSoundEnabled && focusSound != 0) soundPool.play(focusSound, 0.36f, 0.36f, 1, 0, 1.259f)
                }, 65L)
                mainHandler.postDelayed({
                    if (isSoundEnabled && powerUpSound != 0) soundPool.play(powerUpSound, 0.32f, 0.32f, 1, 0, 1.681f)
                }, 145L)
            }
        }
    }

    fun playVictory() {
        playWhooshChime("VICTORY")
    }

    fun playMenuNote() {
        if (!isSoundEnabled || focusSound == 0) return
        if (nearMissSound != 0) soundPool.play(nearMissSound, 0.18f, 0.18f, 0, 0, 1.05f)
        val pitch = pentatonicScale[menuNoteIndex % pentatonicScale.size]
        mainHandler.postDelayed({
            if (isSoundEnabled && focusSound != 0) {
                soundPool.play(focusSound, 0.40f, 0.40f, 0, 0, pitch)
            }
        }, 50L)
        menuNoteIndex++
    }

    fun getLobbyTrack(): LobbyTrack = currentLobbyTrack

    fun setLobbyRemixMode(remixMode: TrackRemixMode, playFeedback: Boolean = false) {
        currentLobbyRemixMode = remixMode
        if (isMusicEnabled) {
            if (themePlayer == null && trackPlayer == null) {
                startThemeMusic()
            } else {
                themePlayer?.runCatching {
                    playbackParams = PlaybackParams()
                        .setSpeed(remixMode.audioSpeed)
                        .setPitch(remixMode.audioPitch)
                }
            }
        }
        if (playFeedback && isSoundEnabled) {
            val p1 = when (remixMode) {
                TrackRemixMode.OVERCLOCK -> 1.32f
                TrackRemixMode.SLOWED -> 0.78f
                TrackRemixMode.ENDLESS -> 1.18f
                TrackRemixMode.ORIGINAL -> 1.0f
            }
            if (focusSound != 0) soundPool.play(focusSound, 0.55f, 0.55f, 1, 0, p1)
            if (powerUpSound != 0) soundPool.play(powerUpSound, 0.40f, 0.40f, 1, 0, (p1 * 1.25f).coerceIn(0.5f, 2.0f))
        }
    }

    fun setLobbyTrack(lobbyTrack: LobbyTrack, restartIfPlaying: Boolean = true) {
        val changed = currentLobbyTrack != lobbyTrack
        currentLobbyTrack = lobbyTrack
        if (changed && restartIfPlaying) {
            themePlayer?.runCatching {
                stop()
                release()
            }
            themePlayer = null
            if (isMusicEnabled && trackPlayer == null) {
                startThemeMusic()
            }
        }
    }

    fun startThemeMusic() {
        if (!isMusicEnabled) return
        if (themePlayer == null) {
            var resId = context.resources.getIdentifier(currentLobbyTrack.rawResName, "raw", context.packageName)
            if (resId == 0) {
                resId = context.resources.getIdentifier("pulse_theme", "raw", context.packageName)
            }
            if (resId != 0) {
                val vol = currentMusicVolume(0.36f)
                themePlayer = MediaPlayer.create(context, resId)?.apply {
                    isLooping = true
                    setVolume(vol, vol)
                    start()
                    runCatching {
                        playbackParams = PlaybackParams()
                            .setSpeed(currentLobbyRemixMode.audioSpeed)
                            .setPitch(currentLobbyRemixMode.audioPitch)
                    }
                }
            }
        } else {
            val vol = currentMusicVolume(0.36f)
            themePlayer?.runCatching {
                setVolume(vol, vol)
                if (!isPlaying) start()
                playbackParams = PlaybackParams()
                    .setSpeed(currentLobbyRemixMode.audioSpeed)
                    .setPitch(currentLobbyRemixMode.audioPitch)
            }
        }
    }

    fun pauseThemeMusic() {
        themePlayer?.runCatching { if (isPlaying) pause() }
    }

    fun setMusicDucked(ducked: Boolean) {
        isDucked = ducked
        updateVolumes()
    }

    fun resolveTrackRawId(trackIndex: Int): Int {
        val safeIndex = trackIndex.coerceIn(1, MusicLibrary.TOTAL_TRACKS)
        val exactName = "track_%02d".format(safeIndex)
        val exactId = context.resources.getIdentifier(exactName, "raw", context.packageName)
        if (exactId != 0) return exactId
        return context.resources.getIdentifier("track_01", "raw", context.packageName)
    }

    fun startTrack(
        trackIndex: Int,
        forceRestart: Boolean = false,
        remixMode: TrackRemixMode = TrackRemixMode.ORIGINAL
    ) {
        pauseThemeMusic()
        val safeIndex = trackIndex.coerceIn(1, MusicLibrary.TOTAL_TRACKS)
        if (!forceRestart && currentTrackIndex == safeIndex && trackPlayer?.isPlaying == true) return

        stopTrack()

        val rawId = resolveTrackRawId(safeIndex)
        if (rawId == 0) return

        currentTrackIndex = safeIndex
        activeBpmCached = MusicLibrary.getTrack(safeIndex).bpm
        currentPlaybackSpeed = remixMode.audioSpeed
        loopCycleCount = 0
        isTrackFinished = false
        val vol = currentMusicVolume(0.90f)
        val createdPlayer = MediaPlayer.create(context, rawId)?.apply {
            isLooping = false
            setVolume(vol, vol)
            syncedDurationMs = runCatching { duration.toFloat().coerceAtLeast(1000f) }.getOrDefault(219_000f)
            syncedPosMs = 0f
            val startNowNano = System.nanoTime()
            syncNanoTimestamp = startNowNano
            startGraceUntilNano = startNowNano + 500_000_000L
            clockRunning = true
            setOnCompletionListener {
                if (remixMode == TrackRemixMode.ENDLESS) {
                    loopCycleCount++
                    val nextSpeed = (1.0f + loopCycleCount * 0.08f).coerceAtMost(1.65f)
                    val nextPitch = (1.0f + loopCycleCount * 0.04f).coerceAtMost(1.32f)
                    currentPlaybackSpeed = nextSpeed
                    syncedPosMs = 0f
                    val loopNowNano = System.nanoTime()
                    syncNanoTimestamp = loopNowNano
                    startGraceUntilNano = loopNowNano + 500_000_000L
                    seekTo(0)
                    start()
                    runCatching {
                        playbackParams = PlaybackParams()
                            .setSpeed(nextSpeed)
                            .setPitch(nextPitch)
                    }
                } else {
                    startGraceUntilNano = 0L
                    clockRunning = false
                    isTrackFinished = true
                }
            }
            start()
            if (remixMode != TrackRemixMode.ORIGINAL) {
                runCatching {
                    playbackParams = PlaybackParams()
                        .setSpeed(remixMode.audioSpeed)
                        .setPitch(remixMode.audioPitch)
                }
            }
        }
        trackPlayer = createdPlayer
        if (createdPlayer == null) {
            syncedDurationMs = (MusicLibrary.getTrack(safeIndex).durationSec * 1000f).coerceAtLeast(1000f)
            syncedPosMs = 0f
            val fallbackNano = System.nanoTime()
            syncNanoTimestamp = fallbackNano
            startGraceUntilNano = fallbackNano + 500_000_000L
            clockRunning = true
        }
    }

    fun pauseTrack() {
        syncClockWithMediaPlayer()
        startGraceUntilNano = 0L
        clockRunning = false
        trackPlayer?.runCatching { if (isPlaying) pause() }
    }

    fun resumeTrack() {
        val vol = currentMusicVolume(0.90f)
        trackPlayer?.runCatching {
            setVolume(vol, vol)
            if (!isPlaying && !isTrackFinished) {
                start()
                syncedPosMs = currentPosition.toFloat()
                val resumeNano = System.nanoTime()
                syncNanoTimestamp = resumeNano
                startGraceUntilNano = resumeNano + 500_000_000L
                clockRunning = true
            }
        }
    }

    fun stopTrack() {
        startGraceUntilNano = 0L
        clockRunning = false
        syncedPosMs = 0f
        trackPlayer?.runCatching {
            setOnCompletionListener(null)
            stop()
            release()
        }
        trackPlayer = null
        currentTrackIndex = null
        isTrackFinished = false
    }

    fun isTrackPlaying(): Boolean = clockRunning || (trackPlayer?.isPlaying == true)

    /**
     * Called periodically (e.g. every 150-250ms) off the render hot-path to keep the monotonic clock anchored.
     */
    fun syncClockWithMediaPlayer(): Float {
        val p = trackPlayer ?: return 0f
        runCatching {
            val playing = p.isPlaying
            val pos = p.currentPosition.toFloat()
            val dur = p.duration.toFloat()
            if (dur > 0f) syncedDurationMs = dur
            if (pos < syncedPosMs - 15000f) {
                loopCycleCount++
            }
            val nowNano = System.nanoTime()
            val inStartupGrace = !isTrackFinished && nowNano < startGraceUntilNano
            if (playing || !inStartupGrace) {
                syncedPosMs = pos
                syncNanoTimestamp = nowNano
                clockRunning = playing
            }
        }
        return (syncedPosMs / syncedDurationMs).coerceIn(0f, 1f)
    }

    /**
     * Zero-IPC sub-millisecond track position in seconds for 60/120Hz frame loops.
     */
    fun trackPositionSeconds(): Float {
        if (!clockRunning) return syncedPosMs / 1000f
        val elapsedMs = ((System.nanoTime() - syncNanoTimestamp) / 1_000_000f) * currentPlaybackSpeed
        return ((syncedPosMs + elapsedMs).coerceAtMost(syncedDurationMs)) / 1000f
    }

    fun trackProgressFraction(): Float {
        if (isTrackFinished) return 1.0f
        if (trackPlayer == null && clockRunning) {
            return ((trackPositionSeconds() * 1000f) / syncedDurationMs).coerceIn(0f, 1f)
        }
        val posSec = syncClockWithMediaPlayer()
        return posSec.coerceIn(0f, 1f)
    }

    /**
     * Zero-IPC beat phase [0.0 .. 1.0) for 60/120Hz frame loops.
     */
    fun getBeatPhase(bpmOverride: Float? = null): Float {
        val activeBpm = bpmOverride ?: if (clockRunning) activeBpmCached else currentLobbyTrack.bpm
        val beatDurationSec = 60f / activeBpm
        val posSec = if (clockRunning) {
            trackPositionSeconds()
        } else {
            ((System.nanoTime() / 1_000_000_000.0) % 3600.0).toFloat()
        }
        return ((posSec % beatDurationSec) / beatDurationSec).coerceIn(0f, 1f)
    }

    fun release() {
        clockRunning = false
        themePlayer?.runCatching { release() }
        themePlayer = null
        stopTrack()
        soundPool.release()
    }
}
