package com.oneshelfstudio.nomoreillusions

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.SoundPool

class AudioEngine(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(12)
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

    fun playVictory() {
        if (!isSoundEnabled) return
        if (victorySound != 0) {
            soundPool.play(victorySound, 0.95f, 0.95f, 1, 0, 1f)
        } else if (levelUpSound != 0) {
            soundPool.play(levelUpSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playMenuNote() {
        if (!isSoundEnabled || focusSound == 0) return
        val pitch = pentatonicScale[menuNoteIndex % pentatonicScale.size]
        soundPool.play(focusSound, 0.45f, 0.45f, 0, 0, pitch)
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
        trackPlayer = MediaPlayer.create(context, rawId)?.apply {
            isLooping = (remixMode == TrackRemixMode.ENDLESS)
            setVolume(vol, vol)
            syncedDurationMs = runCatching { duration.toFloat().coerceAtLeast(1000f) }.getOrDefault(219_000f)
            syncedPosMs = 0f
            syncNanoTimestamp = System.nanoTime()
            clockRunning = true
            setOnCompletionListener {
                if (remixMode == TrackRemixMode.ENDLESS) {
                    loopCycleCount++
                    syncedPosMs = 0f
                    syncNanoTimestamp = System.nanoTime()
                    start()
                } else {
                    clockRunning = false
                    isTrackFinished = true
                }
            }
            start()
            runCatching {
                playbackParams = PlaybackParams()
                    .setSpeed(remixMode.audioSpeed)
                    .setPitch(remixMode.audioPitch)
            }
        }
    }

    fun pauseTrack() {
        syncClockWithMediaPlayer()
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
                syncNanoTimestamp = System.nanoTime()
                clockRunning = true
            }
        }
    }

    fun stopTrack() {
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
            syncedPosMs = pos
            syncNanoTimestamp = System.nanoTime()
            clockRunning = playing
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
