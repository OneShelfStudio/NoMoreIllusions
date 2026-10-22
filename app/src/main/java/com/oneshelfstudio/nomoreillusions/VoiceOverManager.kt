package com.oneshelfstudio.nomoreillusions

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.*

class VoiceOverManager(private val context: Context, private val audioEngine: AudioEngine) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var duckJob: Job? = null

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build())
        .build()

    private val voiceMap = mutableMapOf<String, Int>()
    
    init {
        loadVoice("vo_boot")
        loadVoice("vo_countdown")
        loadVoice("vo_focus_enter")
        loadVoice("vo_focus_depleted")
        loadVoice("vo_near_miss_streak")
        loadVoice("vo_shield_break")
        loadVoice("vo_emp_blast")
        loadVoice("vo_track_decoded")
        loadVoice("vo_new_record")
    }

    private fun loadVoice(name: String) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) {
            voiceMap[name] = soundPool.load(context, resId, 1)
        }
    }

    fun playVoiceOver(event: String) {
        if (!audioEngine.isSoundEnabled) return
        val soundId = voiceMap[event] ?: return
        
        duckJob?.cancel()
        duckJob = scope.launch {
            audioEngine.setMusicDucked(true)
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            delay(1500) // Default ducking duration
            audioEngine.setMusicDucked(false)
        }
    }

    fun release() {
        duckJob?.cancel()
        scope.cancel()
        audioEngine.setMusicDucked(false)
        soundPool.release()
    }
}
