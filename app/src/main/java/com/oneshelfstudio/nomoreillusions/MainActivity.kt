package com.oneshelfstudio.nomoreillusions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    private lateinit var audioEngine: AudioEngine
    private lateinit var voiceOverManager: VoiceOverManager
    private lateinit var billingManager: BillingManager
    private lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        audioEngine = AudioEngine(this)
        voiceOverManager = VoiceOverManager(this, audioEngine)
        voiceOverManager.playVoiceOver("vo_boot")
        adManager = AdManager(this)
        billingManager = BillingManager(this) { productId ->
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val prefs = EncryptedSharedPreferences.create(
                this,
                "dodge_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            when {
                productId == "points_10000" -> {
                    val currentPoints = prefs.getInt("pulse_points", 0)
                    prefs.edit().putInt("pulse_points", currentPoints + 1000).apply()
                }
                productId == "full_album_unlock" -> {
                    prefs.edit().putInt("unlocked_tracks", MusicLibrary.TOTAL_TRACKS).apply()
                }
                productId.startsWith("track_") -> {
                    val trackNum = productId.removePrefix("track_").toIntOrNull() ?: return@BillingManager
                    val currentUnlocked = prefs.getInt("unlocked_tracks", 1)
                    if (trackNum > currentUnlocked) {
                        prefs.edit().putInt("unlocked_tracks", trackNum.coerceAtMost(MusicLibrary.TOTAL_TRACKS)).apply()
                    }
                }
            }
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF22C55EL),
                    secondary = Color(0xFF818CF8L),
                    surface = Color(0xFF050A14L),
                    background = Color.Black
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    GameScreen(audioEngine, voiceOverManager, billingManager, adManager)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioEngine.pauseThemeMusic()
        audioEngine.pauseTrack()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
        voiceOverManager.release()
        billingManager.release()
    }
}
