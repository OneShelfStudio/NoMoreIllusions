package com.oneshelfstudio.nomoreillusions

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

@Composable
fun GameScreen(
    audioEngine: AudioEngine,
    voiceOverManager: VoiceOverManager,
    billingManager: BillingManager,
    adManager: AdManager
) {
    val context = LocalContext.current
    val totalTracks = MusicLibrary.TOTAL_TRACKS

    // Pre-cached Vibrator (zero getSystemService lookups on the render thread)
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // High-Resolution Backgrounds & UI Bitmaps
    val nebulaBitmap = ImageBitmap.imageResource(id = R.drawable.nebula)
    val milestoneIcon = ImageBitmap.imageResource(id = R.drawable.album_art)
    val starsBack = ImageBitmap.imageResource(id = R.drawable.stars)
    val starsFront = ImageBitmap.imageResource(id = R.drawable.stars)

    // No More Illusions Sacred / Optical Geometry Sprites
    val heroSolarMandala = ImageBitmap.imageResource(id = R.drawable.mandala_hero_solar)
    val heroCyberMandala = ImageBitmap.imageResource(id = R.drawable.mandala_hero_cyber)
    val heroPrismMandala = ImageBitmap.imageResource(id = R.drawable.mandala_hero_prism)
    val enemyPulsarMandala = ImageBitmap.imageResource(id = R.drawable.mandala_enemy_pulsar)
    val enemySeekerMandala = ImageBitmap.imageResource(id = R.drawable.mandala_enemy_seeker)
    val enemyWandererMandala = ImageBitmap.imageResource(id = R.drawable.mandala_enemy_wanderer)

    // UI Bitmaps
    val btnPillGreen = ImageBitmap.imageResource(id = R.drawable.btn_pill_green)
    val btnPillPink = ImageBitmap.imageResource(id = R.drawable.btn_pill_pink)
    val btnPillGold = ImageBitmap.imageResource(id = R.drawable.btn_pill_gold)
    val btnPillCyan = ImageBitmap.imageResource(id = R.drawable.btn_pill_cyan)
    val btnIconRestart = ImageBitmap.imageResource(id = R.drawable.btn_icon_restart)
    val btnIconPause = ImageBitmap.imageResource(id = R.drawable.btn_icon_pause)
    val btnIconClose = ImageBitmap.imageResource(id = R.drawable.btn_icon_close)
    val uiDialogPanel = ImageBitmap.imageResource(id = R.drawable.ui_dialog_panel)

    val prefs = remember {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "dodge_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var isMusicEnabled by remember { mutableStateOf(prefs.getBoolean("music_enabled", true)) }
    var isSoundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    var isHapticEnabled by remember { mutableStateOf(prefs.getBoolean("haptic_enabled", true)) }
    val colorBlindMode by remember { mutableStateOf(ColorBlindMode.valueOf(prefs.getString("color_blind", "NONE") ?: "NONE")) }
    val isAdminMode by remember { mutableStateOf(prefs.getBoolean("admin_mode", false)) }
    var gameDifficulty by remember { mutableStateOf(GameDifficulty.valueOf(prefs.getString("game_difficulty", "REGULAR") ?: "REGULAR")) }
    var activeLobbyTrack by remember { mutableStateOf(LobbyTrack.fromId(prefs.getString("lobby_track", "VELVET"))) }

    // Artist Customization & Locked Shop State
    var customBgTheme by remember { mutableStateOf(prefs.getString("custom_bg_theme", "OBSIDIAN") ?: "OBSIDIAN") }
    var customHeroSkin by remember { mutableStateOf(prefs.getString("custom_hero_skin", "SOLAR") ?: "SOLAR") }
    var customEnemyStyle by remember { mutableStateOf(prefs.getString("custom_enemy_style", "BALANCED") ?: "BALANCED") }
    var unlockedShopItemsCsv by remember { mutableStateOf(prefs.getString("unlocked_shop_items", "BG_OBSIDIAN,HERO_SOLAR,ENEMY_BALANCED") ?: "BG_OBSIDIAN,HERO_SOLAR,ENEMY_BALANCED") }
    var isVipUnlocked by remember { mutableStateOf(prefs.getBoolean("vip_unlocked", false)) }
    var selectedRemixMode by remember { mutableStateOf(TrackRemixMode.fromId(prefs.getString("remix_mode", "ORIGINAL"))) }
    var trackMasteryStarsCsv by remember { mutableStateOf(prefs.getString("track_mastery_stars", "0,0") ?: "0,0") }
    var gameOverCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(isVipUnlocked) {
        // Keep safe Google Test Ad Unit IDs active for all builds during artist testing
        AdManager.forceSafeTestAds = true
    }

    // Dismissable control hint preference
    var showDragHint by remember { mutableStateOf(prefs.getBoolean("show_drag_hint", true)) }
    var isRevivingRun by remember { mutableStateOf(false) }

    var gameState by remember { mutableStateOf(GameState.MENU) }
    var settingsReturnState by remember { mutableStateOf(GameState.MENU) }

    LaunchedEffect(isMusicEnabled, isSoundEnabled, activeLobbyTrack, selectedRemixMode) {
        audioEngine.isMusicEnabled = isMusicEnabled
        audioEngine.isSoundEnabled = isSoundEnabled
        audioEngine.setLobbyRemixMode(selectedRemixMode, playFeedback = false)
        audioEngine.setLobbyTrack(activeLobbyTrack, restartIfPlaying = (gameState == GameState.MENU || (gameState == GameState.SETTINGS && settingsReturnState == GameState.MENU)))
        if (isMusicEnabled && (gameState == GameState.MENU || (gameState == GameState.SETTINGS && settingsReturnState == GameState.MENU))) {
            audioEngine.startThemeMusic()
        }
    }

    var score by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var countdownValue by remember { mutableIntStateOf(3) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("high_score", 0)) }

    var pulsePoints by remember { mutableIntStateOf(prefs.getInt("pulse_points", 0)) }
    var unlockedTrackCount by remember { mutableIntStateOf(prefs.getInt("unlocked_tracks", 1).coerceIn(1, totalTracks)) }
    var trackListMode by remember { mutableStateOf(prefs.getString("track_list_mode", "LIST") ?: "LIST") }
    var trackCompletionsCsv by remember { mutableStateOf(prefs.getString("track_completions", "") ?: "") }
    var selectedTrackIndex by remember { mutableIntStateOf(prefs.getInt("selected_track", 1).coerceIn(1, totalTracks)) }

    // Active track artwork for decode reveal
    val trackArtId = remember(selectedTrackIndex) {
        val safeIdx = selectedTrackIndex.coerceIn(1, totalTracks)
        val id = context.resources.getIdentifier("track_%02d".format(safeIdx), "drawable", context.packageName)
        if (id != 0) id else R.drawable.album_art
    }
    val activeTrackArtwork = remember(trackArtId) {
        ImageBitmap.imageResource(context.resources, trackArtId)
    }

    LaunchedEffect(isAdminMode) { if (isAdminMode) pulsePoints = 100000 }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, prefs, billingManager) {
        val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "pulse_points" -> pulsePoints = sharedPrefs.getInt("pulse_points", pulsePoints)
                "unlocked_tracks" -> unlockedTrackCount = sharedPrefs.getInt("unlocked_tracks", unlockedTrackCount).coerceIn(1, totalTracks)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        billingManager.onUiPurchaseListener = { productId ->
            when {
                productId == "points_10000" -> {
                    pulsePoints += 1000
                    prefs.edit().putInt("pulse_points", pulsePoints).apply()
                    audioEngine.playVictory()
                }
                productId == "full_album_unlock" -> {
                    unlockedTrackCount = totalTracks
                    prefs.edit().putInt("unlocked_tracks", totalTracks).apply()
                    audioEngine.playVictory()
                }
                productId.startsWith("track_") -> {
                    val trackNum = productId.removePrefix("track_").toIntOrNull()
                    if (trackNum != null) {
                        if (trackNum > unlockedTrackCount) {
                            unlockedTrackCount = trackNum.coerceIn(1, totalTracks)
                        }
                        selectedTrackIndex = trackNum.coerceIn(1, totalTracks)
                        prefs.edit()
                            .putInt("unlocked_tracks", unlockedTrackCount)
                            .putInt("selected_track", selectedTrackIndex)
                            .apply()
                        audioEngine.playVictory()
                    }
                }
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (gameState == GameState.PLAYING) {
                    gameState = GameState.PAUSED
                }
                audioEngine.pauseTrack()
                audioEngine.pauseThemeMusic()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            billingManager.onUiPurchaseListener = null
            prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(
        gameState,
        isMusicEnabled,
        isSoundEnabled,
        isHapticEnabled,
        activeLobbyTrack,
        trackListMode,
        trackCompletionsCsv,
        selectedTrackIndex,
        isAdminMode,
        gameDifficulty,
        customBgTheme,
        customHeroSkin,
        customEnemyStyle,
        unlockedShopItemsCsv,
        isVipUnlocked,
        selectedRemixMode,
        trackMasteryStarsCsv,
        highScore,
        unlockedTrackCount,
        pulsePoints
    ) {
        if (gameState != GameState.PLAYING && gameState != GameState.COUNTDOWN) {
            prefs.edit()
                .putBoolean("music_enabled", isMusicEnabled)
                .putBoolean("sound_enabled", isSoundEnabled)
                .putBoolean("haptic_enabled", isHapticEnabled)
                .putString("lobby_track", activeLobbyTrack.id)
                .putInt("high_score", highScore)
                .putInt("pulse_points", pulsePoints)
                .putInt("unlocked_tracks", unlockedTrackCount)
                .putString("track_list_mode", trackListMode)
                .putString("track_completions", trackCompletionsCsv)
                .putInt("selected_track", selectedTrackIndex)
                .putBoolean("admin_mode", isAdminMode)
                .putString("game_difficulty", gameDifficulty.name)
                .putString("custom_bg_theme", customBgTheme)
                .putString("custom_hero_skin", customHeroSkin)
                .putString("custom_enemy_style", customEnemyStyle)
                .putString("unlocked_shop_items", unlockedShopItemsCsv)
                .putBoolean("vip_unlocked", isVipUnlocked)
                .putString("remix_mode", selectedRemixMode.id)
                .putString("track_mastery_stars", trackMasteryStarsCsv)
                .apply()
        }
    }

    var trackRevealMusicProgress by remember { mutableFloatStateOf(0f) }
    var onBeatHitsThisRun by remember { mutableIntStateOf(0) }
    var maxComboThisRun by remember { mutableIntStateOf(1) }
    var activeDiscovery by remember {
        mutableStateOf<FirstTimeDiscovery?>(
            if (!hasSeenDiscovery(prefs, FirstTimeDiscovery.FAN_WELCOME)) FirstTimeDiscovery.FAN_WELCOME else null
        )
    }

    LaunchedEffect(gameState, selectedTrackIndex) {
        if (gameState == GameState.COUNTDOWN) {
            audioEngine.pauseThemeMusic()
        }
        if (gameState == GameState.PLAYING) {
            audioEngine.resumeTrack()
            if (!audioEngine.isTrackPlaying()) {
                audioEngine.startTrack(selectedTrackIndex, remixMode = selectedRemixMode)
            }
        }
        if (gameState == GameState.MENU) {
            audioEngine.stopTrack()
            audioEngine.startThemeMusic()
        }
        if (gameState == GameState.MUSIC_DEMO) audioEngine.stopTrack()
        if (gameState == GameState.GAME_OVER || gameState == GameState.PAUSED || gameState == GameState.LEVEL_COMPLETE) audioEngine.pauseTrack()
    }

    // Periodic off-frame-loop clock synchronization (every 120ms)
    LaunchedEffect(gameState) {
        while (gameState == GameState.COUNTDOWN || gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            trackRevealMusicProgress = audioEngine.trackProgressFraction()
            if (trackRevealMusicProgress >= 0.99f && gameState == GameState.PLAYING && selectedRemixMode != TrackRemixMode.ENDLESS) {
                val counts = parseCompletionsCsv(trackCompletionsCsv)
                val trackIdx = (selectedTrackIndex - 1).coerceIn(0, totalTracks - 1)
                counts[trackIdx] += 1
                trackCompletionsCsv = toCompletionsCsv(counts)

                // Update 3-Star Track Mastery (★1 Clear, ★2 8+ On-Beat Comets, ★3 Nightcore/Slowed Clear)
                val masteryArr = parseCompletionsCsv(trackMasteryStarsCsv)
                var earnedStars = maxOf(masteryArr[trackIdx], 1)
                if (onBeatHitsThisRun >= 8) earnedStars = maxOf(earnedStars, 2)
                if (selectedRemixMode == TrackRemixMode.OVERCLOCK || selectedRemixMode == TrackRemixMode.SLOWED) {
                    earnedStars = 3
                }
                masteryArr[trackIdx] = earnedStars
                trackMasteryStarsCsv = toCompletionsCsv(masteryArr)

                if (selectedTrackIndex == unlockedTrackCount && unlockedTrackCount < totalTracks) {
                    unlockedTrackCount++
                }
                pulsePoints += (75 * selectedRemixMode.noteMultiplier).toInt()

                prefs.edit()
                    .putString("track_completions", trackCompletionsCsv)
                    .putString("track_mastery_stars", trackMasteryStarsCsv)
                    .putInt("unlocked_tracks", unlockedTrackCount)
                    .putInt("pulse_points", pulsePoints)
                    .putBoolean("art_unlocked_$selectedTrackIndex", true)
                    .putInt("best_combo", maxOf(prefs.getInt("best_combo", 0), maxComboThisRun))
                    .apply()

                audioEngine.playVictory()
                gameState = GameState.LEVEL_COMPLETE
                voiceOverManager.playVoiceOver("vo_track_decoded")
            }
            delay(120)
        }
    }

    // Entities & Physics State
    var player by remember { mutableStateOf(GameObject(0f, 0f, 18f, Color(0xFF22C55EL))) }
    var targetX by remember { mutableFloatStateOf(0f) }
    var targetY by remember { mutableFloatStateOf(0f) }
    var spawnInvulnerabilityTime by remember { mutableFloatStateOf(2.5f) }

    val enemies = remember { mutableStateListOf<GameObject>() }
    val enemyTelegraphs = remember { mutableStateListOf<EnemySpawnTelegraph>() }
    val wormholes = remember { mutableStateListOf<SingularityWormhole>() }
    val powerUps = remember { mutableStateListOf<GameObject>() }
    val particles = remember { mutableStateListOf<GameObject>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }
    val shockwaves = remember { mutableStateListOf<Shockwave>() }
    val tetheredSatellites = remember { mutableStateListOf<TetheredSatellite>() }
    val playerTrail = remember { mutableStateListOf<Offset>() }
    val motes = remember { mutableStateListOf<GameObject>() }

    var activePhenomenon by remember { mutableStateOf<ActivePhenomenon?>(null) }
    var activeWormholeSlide by remember { mutableStateOf<WormholeSlideState?>(null) }
    var constellationCounter by remember { mutableIntStateOf(0) }
    var liveMultiBand by remember { mutableStateOf(MultiBandAudioState()) }
    var liveAudioEnergy by remember { mutableFloatStateOf(0.42f) }
    var liveSwellDelta by remember { mutableFloatStateOf(0f) }

    var timeScale by remember { mutableFloatStateOf(1f) }
    var focusPower by remember { mutableFloatStateOf(1f) }
    var isFocusActive by remember { mutableStateOf(false) }
    val focusWarp by animateFloatAsState(
        targetValue = if (isFocusActive) 1f else 0f,
        animationSpec = tween(400, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)),
        label = "warp"
    )
    var shakeAmount by remember { mutableFloatStateOf(0f) }
    var chromaticAmount by remember { mutableFloatStateOf(0f) }
    var cameraPunch by remember { mutableFloatStateOf(0f) }
    var goldenOverdriveIntensity by remember { mutableFloatStateOf(0f) }
    var starPowerTime by remember { mutableFloatStateOf(0f) }
    var beatFlashIntensity by remember { mutableFloatStateOf(0f) }
    var gameTime by remember { mutableFloatStateOf(0f) }
    var levelPlayTime by remember { mutableFloatStateOf(0f) }
    var shieldTime by remember { mutableFloatStateOf(0f) }
    var slowMoTime by remember { mutableFloatStateOf(0f) }
    var magnetTime by remember { mutableFloatStateOf(0f) }
    var lastLoopCycleSeen by remember { mutableIntStateOf(0) }
    var activePowerUpToast by remember { mutableStateOf<PowerUpBannerToast?>(null) }
    var toastTimeLeft by remember { mutableFloatStateOf(0f) }
    var scoreMultiplier by remember { mutableIntStateOf(1) }
    var rhythmCombo by remember { mutableIntStateOf(0) }
    var multiplierTime by remember { mutableFloatStateOf(0f) }
    var lastLevelUpTime by remember { mutableFloatStateOf(-10f) }
    var survivalPointAccumulator by remember { mutableFloatStateOf(0f) }
    var nearMissPointCooldown by remember { mutableFloatStateOf(0f) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

    // Live Song Section, Stage Mode & Beat Chart State
    var lastTriggeredSectionSec by remember { mutableFloatStateOf(-1f) }
    var activeSectionLabel by remember { mutableStateOf("INTRO") }
    var activeStageMode by remember { mutableStateOf(StageMode.ABYSSAL_WAVE) }
    var juiceIntensity by remember { mutableFloatStateOf(0f) }
    var nextOrbCueIdx by remember { mutableIntStateOf(0) }
    var liveTrackSec by remember { mutableFloatStateOf(0f) }
    var liveBeatPhase by remember { mutableFloatStateOf(0f) }

    val difficultyScoreMultiplier = when (gameDifficulty) {
        GameDifficulty.EASY -> 0.5f
        GameDifficulty.REGULAR -> 1.0f
        GameDifficulty.HARD -> 1.5f
    }
    val trackDifficultyBoost = 1.0f + (selectedTrackIndex - 1).coerceAtLeast(0) * 0.14f
    val speedScale = when (gameDifficulty) {
        GameDifficulty.EASY -> 0.68f
        GameDifficulty.REGULAR -> 0.85f
        GameDifficulty.HARD -> 1.18f
    } * trackDifficultyBoost
    val spawnRateScale = when (gameDifficulty) {
        GameDifficulty.EASY -> 0.50f
        GameDifficulty.REGULAR -> 0.72f
        GameDifficulty.HARD -> 1.25f
    } * trackDifficultyBoost

    val applyColorBlind: (Color) -> Color = { color ->
        when (colorBlindMode) {
            ColorBlindMode.NONE -> color
            ColorBlindMode.PROTANOPIA -> color.toProtanopia()
            ColorBlindMode.DEUTERANOPIA -> color.toDeuteranopia()
            ColorBlindMode.TRITANOPIA -> color.toTritanopia()
        }
    }

    val triggerHaptic = { strength: Long ->
        if (isHapticEnabled) {
            runCatching {
                if (vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(strength, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(strength)
                    }
                }
            }
        }
    }

    fun startFreshLevelRun(trackIdx: Int) {
        selectedTrackIndex = trackIdx.coerceIn(1, totalTracks)
        score = 0
        rhythmCombo = 0
        maxComboThisRun = 1
        onBeatHitsThisRun = 0
        level = 1
        focusPower = 1f
        isFocusActive = false
        shieldTime = 0f
        slowMoTime = 0f
        magnetTime = 0f
        lastLoopCycleSeen = 0
        activePowerUpToast = null
        scoreMultiplier = 1
        multiplierTime = 0f
        shakeAmount = 0f
        chromaticAmount = 0f
        cameraPunch = 0f
        goldenOverdriveIntensity = 0f
        starPowerTime = 0f
        beatFlashIntensity = 0f
        lastTriggeredSectionSec = -1f
        activeSectionLabel = "INTRO"
        activeStageMode = StageMode.ABYSSAL_WAVE
        juiceIntensity = 0f
        nextOrbCueIdx = 0
        activePhenomenon = null
        activeWormholeSlide = null
        liveMultiBand = MultiBandAudioState()
        liveAudioEnergy = 0.42f
        liveSwellDelta = 0f
        enemies.clear()
        enemyTelegraphs.clear()
        wormholes.clear()
        powerUps.clear()
        particles.clear()
        floatingTexts.clear()
        shockwaves.clear()
        tetheredSatellites.clear()
        playerTrail.clear()
        isRevivingRun = false
        audioEngine.pauseThemeMusic()
        audioEngine.stopTrack()
        trackRevealMusicProgress = 0f
        liveTrackSec = 0f
        player = GameObject(screenWidth / 2f, screenHeight * 0.72f, 18f, applyColorBlind(Color(0xFF22C55EL)))
        targetX = player.x
        targetY = player.y
        spawnInvulnerabilityTime = 2.0f
        gameState = GameState.COUNTDOWN
    }

    fun reviveCurrentRun() {
        isFocusActive = false
        shakeAmount = 0f
        chromaticAmount = 0f
        cameraPunch = 0f
        beatFlashIntensity = 0f
        activePhenomenon = null
        activeWormholeSlide = null
        activePowerUpToast = PowerUpBannerToast(
            icon = "🛡️",
            title = "REVIVED!",
            subtitle = "3.0s INVINCIBILITY SHIELD ACTIVE",
            accentColor = Color(0xFF4ADE80L),
            timer = 2.6f,
            maxTimer = 2.6f
        )
        enemies.clear()
        enemyTelegraphs.clear()
        wormholes.clear()
        particles.clear()
        floatingTexts.clear()
        shockwaves.clear()
        playerTrail.clear()
        isRevivingRun = true
        player = GameObject(screenWidth / 2f, screenHeight * 0.72f, 18f, applyColorBlind(Color(0xFF22C55EL)))
        targetX = player.x
        targetY = player.y
        spawnInvulnerabilityTime = 2.6f
        shieldTime = maxOf(shieldTime, 3.0f)
        gameState = GameState.COUNTDOWN
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.COUNTDOWN) {
            levelPlayTime = 0f
            spawnInvulnerabilityTime = 2.0f
            countdownValue = 3
            // Wait until the centered How-To-Play popup is dismissed before starting the countdown or music!
            while (showDragHint) {
                delay(50)
            }
            voiceOverManager.playVoiceOver("vo_countdown")
            while (countdownValue > 0) {
                delay(950)
                countdownValue--
            }
            delay(220)
            levelPlayTime = 0f
            spawnInvulnerabilityTime = 2.0f
            if (isRevivingRun) {
                audioEngine.resumeTrack()
            } else {
                audioEngine.startTrack(selectedTrackIndex, forceRestart = true, remixMode = selectedRemixMode)
            }
            gameState = GameState.PLAYING
        }
    }

    // Zero-Stutter 60/120Hz Physics & 4Hz Multi-Band Musical Choreography Loop
    LaunchedEffect(gameState) {
        if (gameState != GameState.PLAYING && gameState != GameState.MENU) return@LaunchedEffect
        while (isActive && (gameState == GameState.PLAYING || gameState == GameState.MENU)) {
            withFrameNanos { _ ->
                liveBeatPhase = audioEngine.getBeatPhase()
                if (gameState == GameState.MENU) {
                    gameTime += 0.016f
                    liveAudioEnergy = 0.42f + 0.08f * sin(gameTime * 0.8f)
                    liveSwellDelta = 0f
                    liveMultiBand = MultiBandAudioState(rms = liveAudioEnergy, bass = 0.38f, vocal = 0.45f, high = 0.30f)
                    updateMenuMotes(motes, screenWidth, screenHeight, liveBeatPhase)
                    return@withFrameNanos
                }

                // Pause physics while a First-Time Contextual Discovery Card is open
                if (activeDiscovery != null) {
                    return@withFrameNanos
                }

                val currentScale = when {
                    isFocusActive -> 0.32f
                    slowMoTime > 0f -> 0.48f
                    else -> timeScale
                }
                gameTime += 0.016f * currentScale
                levelPlayTime += 0.016f * currentScale
                survivalPointAccumulator += 0.016f * currentScale

                if (spawnInvulnerabilityTime > 0f) {
                    spawnInvulnerabilityTime -= 0.016f * currentScale
                }
                if (slowMoTime > 0f) {
                    slowMoTime = (slowMoTime - 0.016f).coerceAtLeast(0f)
                }
                if (magnetTime > 0f) {
                    magnetTime = (magnetTime - 0.016f).coerceAtLeast(0f)
                }
                val curToast = activePowerUpToast
                if (curToast != null) {
                    val nextToastTime = curToast.timer - 0.016f
                    if (nextToastTime <= 0f) {
                        activePowerUpToast = null
                        toastTimeLeft = 0f
                    } else {
                        curToast.timer = nextToastTime
                        toastTimeLeft = nextToastTime
                    }
                }

                // Detect Endless Mode Loop Speed Escalation (+8% faster each cycle!)
                if (selectedRemixMode == TrackRemixMode.ENDLESS && audioEngine.loopCycleCount > lastLoopCycleSeen) {
                    lastLoopCycleSeen = audioEngine.loopCycleCount
                    nextOrbCueIdx = 0
                    val newSpd = (1.0f + lastLoopCycleSeen * 0.08f).coerceAtMost(1.65f)
                    score += 500 * lastLoopCycleSeen
                    pulsePoints += 25
                    cameraPunch = 0.042f
                    beatFlashIntensity = 0.75f
                    shockwaves.add(
                        Shockwave(
                            screenWidth * 0.5f,
                            screenHeight * 0.45f,
                            0f,
                            maxOf(screenWidth, screenHeight) * 0.85f,
                            Color(0xFFFACC15L),
                            1.25f
                        )
                    )
                    activePowerUpToast = PowerUpBannerToast(
                        icon = "∞",
                        title = "ENDLESS LOOP #${lastLoopCycleSeen + 1}!",
                        subtitle = "SONG SPEED ESCALATED TO ${"%.2f".format(newSpd)}x • +${500 * lastLoopCycleSeen} PTS",
                        accentColor = Color(0xFFFACC15L),
                        timer = 3.2f,
                        maxTimer = 3.2f
                    )
                }

                // 1. Sub-millisecond monotonic song clock & 4Hz (250ms) Multi-Band Spectral State
                val trackSec = audioEngine.trackPositionSeconds()
                liveTrackSec = trackSec
                val activeTrack = MusicLibrary.getTrack(selectedTrackIndex)
                val mb = MusicLibrary.getMultiBandStateAt(selectedTrackIndex, trackSec)
                liveMultiBand = mb
                liveAudioEnergy += (mb.rms - liveAudioEnergy) * 0.18f
                liveSwellDelta = mb.swellDelta

                // Subtle camera bass-thump when the kick/sub-bass hits on the beat
                if (!mb.bassDropOut && mb.bass > 0.62f && liveBeatPhase in 0f..0.15f) {
                    cameraPunch = maxOf(cameraPunch, 0.016f * mb.bass)
                }

                // Step active Supernova Phenomenon timer
                val curPhenom = activePhenomenon
                if (curPhenom != null) {
                    val nextTimer = curPhenom.timer - 0.016f * currentScale
                    if (nextTimer <= 0f) {
                        activePhenomenon = null
                    } else {
                        curPhenom.timer = nextTimer
                    }
                }

                // 2. Live Section & Musical Swell Transitions (Zero text popups on orbs; clean section shift pulse)
                val currentSecObj = MusicLibrary.currentSectionAt(selectedTrackIndex, trackSec)
                if (currentSecObj.timestampSec != lastTriggeredSectionSec) {
                    lastTriggeredSectionSec = currentSecObj.timestampSec
                    activeSectionLabel = currentSecObj.label
                    activeStageMode = currentSecObj.stageMode

                    // If entering a Vocal Float, Pre-Drop Hush, or Victory Outro, dissolve all active enemies into stardust!
                    if (currentSecObj.clearEnemiesOnEnter) {
                        enemyTelegraphs.clear()
                        wormholes.clear()
                        for (e in enemies) {
                            shockwaves.add(
                                Shockwave(
                                    x = e.x,
                                    y = e.y,
                                    radius = 0f,
                                    maxRadius = 90f,
                                    color = Color(0xFF38BDF8L),
                                    life = 0.75f
                                )
                            )
                            repeat(8) {
                                val a = Random.nextFloat() * 6.283f
                                val sp = 1.4f + Random.nextFloat() * 3.2f
                                particles.add(
                                    GameObject(
                                        x = e.x,
                                        y = e.y,
                                        radius = 2.6f + Random.nextFloat() * 2.2f,
                                        color = if (it % 2 == 0) Color(0xFFFDE047L) else Color(0xFF38BDF8L),
                                        vx = cos(a) * sp,
                                        vy = sin(a) * sp,
                                        life = 1.0f
                                    )
                                )
                            }
                        }
                        enemies.clear()
                    }

                    if (currentSecObj.isGoldenOverdrive || currentSecObj.stageMode == StageMode.GOLDEN_OVERDRIVE) {
                        goldenOverdriveIntensity = 1.0f
                        beatFlashIntensity = 0.70f
                        scoreMultiplier = 4
                        multiplierTime = 16f
                    }

                    when (currentSecObj.juiceTier) {
                        JuiceTier.EXTRA_JUICE -> {
                            juiceIntensity = 1.20f
                            shakeAmount = 10f
                            chromaticAmount = 1.8f
                            cameraPunch = 0.038f
                            lastLevelUpTime = gameTime
                            score += (150 * scoreMultiplier * difficultyScoreMultiplier).toInt()
                            triggerHaptic(60L)
                            shockwaves.add(
                                Shockwave(
                                    screenWidth / 2f,
                                    screenHeight * 0.45f,
                                    0f,
                                    maxOf(screenWidth, screenHeight) * 0.85f,
                                    Color(0xFFFACC15L),
                                    1.20f
                                )
                            )
                        }

                        JuiceTier.SLIGHT_EXTRA_JUICE -> {
                            juiceIntensity = 0.75f
                            shakeAmount = 7f
                            cameraPunch = 0.026f
                            lastLevelUpTime = gameTime
                            score += (80 * scoreMultiplier * difficultyScoreMultiplier).toInt()
                            triggerHaptic(35L)
                            shockwaves.add(Shockwave(screenWidth / 2f, screenHeight * 0.45f, 0f, 480f, Color(0xFF38BDF8L), 1.0f))
                        }

                        JuiceTier.SECTION_SHIFT, JuiceTier.NONE -> {
                            if (currentSecObj.timestampSec > 0f) {
                                juiceIntensity = maxOf(juiceIntensity, 0.22f)
                                cameraPunch = 0.015f
                                shockwaves.add(Shockwave(screenWidth / 2f, screenHeight * 0.45f, 0f, 260f, Color(0xFF38BDF8L), 0.8f))
                            }
                        }
                    }
                }

                // 808 Sub-Bass Heartbeat Pulse during Confide's 3:21–3:27.5 Breakdown
                if (selectedTrackIndex == 0 && trackSec in 201.0f..207.5f && liveBeatPhase in 0f..0.14f && shockwaves.size < 6) {
                    cameraPunch = maxOf(cameraPunch, 0.030f)
                    shockwaves.add(
                        Shockwave(
                            x = screenWidth * 0.5f,
                            y = screenHeight * 0.45f,
                            radius = 12f,
                            maxRadius = maxOf(screenWidth, screenHeight) * 0.62f,
                            color = Color(0xFFFACC15L),
                            life = 0.65f
                        )
                    )
                }

                // Update 24K Golden Overdrive intensity smoothly from section swells or Supernova Constellation orbs
                if (starPowerTime > 0f) {
                    starPowerTime = (starPowerTime - 0.016f * currentScale).coerceAtLeast(0f)
                }
                val isGoldenActive = currentSecObj.isGoldenOverdrive ||
                    activeStageMode == StageMode.GOLDEN_OVERDRIVE ||
                    starPowerTime > 0f ||
                    (activePhenomenon?.effect == KeyWordEffect.CONSTELLATION_STAR && (activePhenomenon?.timer ?: 0f) > 0f)
                val targetGoldIntensity = if (isGoldenActive) 1.0f else (mb.high - 0.70f).coerceAtLeast(0f) * 2.0f
                goldenOverdriveIntensity += (targetGoldIntensity.coerceIn(0f, 1f) - goldenOverdriveIntensity) * 0.12f

                // 3. STATIONARY ANCHORED SEQUENTIAL BEAT NODE SPAWNING + TTL APPROACH RINGS (Osu! / Guitar Hero Flow!)
                val beatDurationSec = 60f / activeTrack.bpm.coerceAtLeast(60f)
                val spawnLeadTime = beatDurationSec * 2.5f
                val ttlGraceSec = beatDurationSec * 0.92f
                val chart = activeTrack.orbChart
                if (selectedRemixMode == TrackRemixMode.ENDLESS && nextOrbCueIdx >= chart.size && trackSec < 5f) {
                    nextOrbCueIdx = 0
                }
                nextOrbCueIdx = spawnStationaryBeatOrbsForFrame(
                    chart = chart,
                    startCueIdx = nextOrbCueIdx,
                    trackSec = trackSec,
                    spawnLeadTime = spawnLeadTime,
                    ttlGraceSec = ttlGraceSec,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    isGoldenActive = isGoldenActive,
                    applyColorBlind = applyColorBlind,
                    powerUps = powerUps,
                    shockwaves = shockwaves
                )

                // Step Stationary Beat Nodes + TTL Expiration Check (Miss resets combo only, never health!)
                stepNebulousBeatOrbs(
                    powerUps = powerUps,
                    player = player,
                    gameTime = gameTime,
                    trackSec = trackSec,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    isGoldenOverdrive = isGoldenActive,
                    activePhenomenon = activePhenomenon,
                    currentScale = currentScale,
                    beatPhase = liveBeatPhase,
                    magnetActive = magnetTime > 0f || activePhenomenon?.effect == KeyWordEffect.TRACTOR_REACH,
                    onBeatMissed = { missedOrb ->
                        if (rhythmCombo > 0) {
                            rhythmCombo = 0
                            floatingTexts.add(
                                FloatingText(
                                    x = missedOrb.x,
                                    y = missedOrb.y - 14f,
                                    text = "MISS • COMBO RESET",
                                    life = 0.85f,
                                    color = Color(0xFFFB7185L),
                                    isLarge = false
                                )
                            )
                        } else {
                            floatingTexts.add(
                                FloatingText(
                                    x = missedOrb.x,
                                    y = missedOrb.y - 14f,
                                    text = "MISS",
                                    life = 0.75f,
                                    color = Color(0xFF94A3B8L),
                                    isLarge = false
                                )
                            )
                        }
                        shockwaves.add(
                            Shockwave(
                                x = missedOrb.x,
                                y = missedOrb.y,
                                radius = missedOrb.radius * 0.8f,
                                maxRadius = missedOrb.radius * 2.1f,
                                color = Color(0xFFFB7185L),
                                life = 0.45f
                            )
                        )
                    }
                )
                stepTetheredSatellites(
                    satellites = tetheredSatellites,
                    player = player,
                    currentScale = currentScale
                )
                stepAndSpawnWormholes(
                    wormholes = wormholes,
                    enemies = enemies,
                    player = player,
                    gameTime = gameTime,
                    currentScale = currentScale,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    multiBand = mb,
                    beatPhase = liveBeatPhase,
                    remixMode = selectedRemixMode,
                    allowAnomalies = currentSecObj.allowAnomalies
                )

                // Smoothly decay juiceIntensity, cameraPunch & beatFlashIntensity
                if (juiceIntensity > 0f) {
                    juiceIntensity = (juiceIntensity - 0.0024f * currentScale).coerceAtLeast(0f)
                }
                if (beatFlashIntensity > 0f) {
                    beatFlashIntensity = (beatFlashIntensity - 0.032f * currentScale).coerceAtLeast(0f)
                }
                cameraPunch *= 0.86f

                if (survivalPointAccumulator >= 4.5f) {
                    survivalPointAccumulator -= 4.5f
                    pulsePoints += (2 * selectedRemixMode.noteMultiplier).toInt().coerceAtLeast(2)
                }
                if (nearMissPointCooldown > 0f) nearMissPointCooldown -= 0.016f * currentScale

                if (shieldTime > 0f) shieldTime -= 0.016f * currentScale
                if (multiplierTime > 0f) {
                    multiplierTime -= 0.016f * currentScale
                    if (multiplierTime <= 0f) {
                        scoreMultiplier = if (isGoldenActive) 4 else 1
                    }
                }
                if (isFocusActive) {
                    focusPower -= 0.005f
                    if (focusPower <= 0f) {
                        isFocusActive = false
                        voiceOverManager.playVoiceOver("vo_focus_depleted")
                    }
                } else {
                    focusPower = (focusPower + 0.0018f).coerceAtMost(1f)
                }

                val endlessEscalation = 1.0f + (audioEngine.loopCycleCount * 0.18f)
                val songProgressBoost = trackRevealMusicProgress.coerceIn(0f, 1f) * 0.65f
                val difficulty = (((score / 4200f) + songProgressBoost) * endlessEscalation).coerceAtMost(1.75f)

                // 4. Step & Spawn Beat-Locked Telegraphed Enemies (Zero random/invisible spawns, choreographed to TrackSection!)
                stepAndSpawnTelegraphedEnemies(
                    enemies = enemies,
                    telegraphs = enemyTelegraphs,
                    shockwaves = shockwaves,
                    levelPlayTime = levelPlayTime,
                    difficulty = difficulty,
                    spawnRateScale = spawnRateScale * (if (selectedRemixMode == TrackRemixMode.OVERCLOCK) 1.22f else 1.0f),
                    speedScale = speedScale * selectedRemixMode.audioSpeed * endlessEscalation.coerceAtMost(1.45f),
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    customEnemyStyle = customEnemyStyle,
                    player = player,
                    applyColorBlind = applyColorBlind,
                    stageMode = if (isGoldenActive) StageMode.GOLDEN_OVERDRIVE else activeStageMode,
                    multiBand = mb,
                    beatPhase = liveBeatPhase,
                    currentScale = currentScale,
                    sectionMaxEnemies = currentSecObj.maxEnemies,
                    sectionSpeedMult = currentSecObj.enemySpeedMult
                )

                // First-Time Contextual Discovery Check: Pauses gameplay the VERY FIRST TIME a new mechanic appears!
                if (activeDiscovery == null && levelPlayTime > 0.65f) {
                    val triggerDiscovery: FirstTimeDiscovery? = when {
                        powerUps.isNotEmpty() && !hasSeenDiscovery(prefs, FirstTimeDiscovery.BEAT_CHAIN) -> FirstTimeDiscovery.BEAT_CHAIN
                        powerUps.any { it.keyWordEffect != KeyWordEffect.NONE } && !hasSeenDiscovery(prefs, FirstTimeDiscovery.SUPERNOVA_ORB) -> FirstTimeDiscovery.SUPERNOVA_ORB
                        wormholes.isNotEmpty() && !hasSeenDiscovery(prefs, FirstTimeDiscovery.SLIDE_STREAM) -> FirstTimeDiscovery.SLIDE_STREAM
                        (enemyTelegraphs.isNotEmpty() || enemies.isNotEmpty()) && !hasSeenDiscovery(prefs, FirstTimeDiscovery.HAZARD_SHIELD) -> FirstTimeDiscovery.HAZARD_SHIELD
                        else -> null
                    }
                    if (triggerDiscovery != null) {
                        markDiscoverySeen(prefs, triggerDiscovery)
                        activeDiscovery = triggerDiscovery
                        audioEngine.pauseTrack()
                        audioEngine.playWhooshChime("MODAL")
                        return@withFrameNanos
                    }
                }

                // 5. Differentiated Beat Orb Collection + Unmistakable ON-BEAT HARMONIC SPECTACLE!
                val pIter = powerUps.iterator()
                while (pIter.hasNext()) {
                    val p = pIter.next()
                    val dist = hypot(p.x - player.x, p.y - player.y)
                    val catchRadius = p.radius + player.radius + (if (isGoldenActive) 20f else 15f)
                    if (dist < catchRadius) {
                        rhythmCombo++
                        if (rhythmCombo > maxComboThisRun) maxComboThisRun = rhythmCombo
                        val isSupernovaOrb = p.keyWordEffect != KeyWordEffect.NONE
                        val beatDelta = abs(trackSec - p.targetBeatSec)
                        val isCaughtOnBeat = (beatDelta <= beatDurationSec * 0.42f) || (liveBeatPhase <= 0.38f || liveBeatPhase >= 0.76f) || isSupernovaOrb
                        if (isCaughtOnBeat) {
                            onBeatHitsThisRun++
                        }
                        val noteGain = ((if (isSupernovaOrb) 10 else if (isCaughtOnBeat) 5 else 3) * selectedRemixMode.noteMultiplier).toInt()
                        pulsePoints += noteGain

                        // Whisper-quiet audio feedback so Ethan's music always leads
                        audioEngine.playOrbBeatHit(
                            combo = rhythmCombo,
                            isKeyWordPeg = isSupernovaOrb || isCaughtOnBeat
                        )

                        // Guaranteed ON-BEAT visual transformation (fires Concert Lasers + Equalizer Columns + Golden Shockwave!)
                        if (isCaughtOnBeat) {
                            beatFlashIntensity = maxOf(beatFlashIntensity, if (isSupernovaOrb) 0.85f else 0.55f)
                            goldenOverdriveIntensity = maxOf(goldenOverdriveIntensity, if (isSupernovaOrb) 1.0f else 0.65f)
                            cameraPunch = maxOf(cameraPunch, if (isSupernovaOrb) 0.042f else 0.026f)
                            scoreMultiplier = maxOf(scoreMultiplier, if (isSupernovaOrb) 4 else 2)
                            multiplierTime = maxOf(multiplierTime, 6.5f)
                            shockwaves.add(Shockwave(p.x, p.y, 0f, 280f, Color(0xFFFACC15L), 0.95f))
                        }

                        if (!isSupernovaOrb) {
                            triggerHaptic(if (isCaughtOnBeat) 28L else 14L)
                            val mult = if (isGoldenActive) maxOf(scoreMultiplier, 4) else scoreMultiplier
                            val gainedPts = ((if (isCaughtOnBeat) 80 else 45) * mult * (1f + (rhythmCombo.coerceAtMost(12) * 0.10f)) * difficultyScoreMultiplier).toInt()
                            score += gainedPts
                            floatingTexts.add(
                                FloatingText(
                                    x = p.x,
                                    y = p.y - 18f,
                                    text = if (isCaughtOnBeat) "PERFECT! +$gainedPts (x$rhythmCombo)" else "GOOD! +$gainedPts",
                                    life = 0.90f,
                                    color = if (isCaughtOnBeat) Color(0xFF4ADE80L) else Color(0xFFFACC15L),
                                    isLarge = false
                                )
                            )

                            val pickupRes = handleRegularOrbPowerUp(
                                p = p,
                                mult = mult,
                                difficultyScoreMultiplier = difficultyScoreMultiplier,
                                isCaughtOnBeat = isCaughtOnBeat,
                                gameTime = gameTime,
                                starPowerTime = starPowerTime,
                                shieldTime = shieldTime,
                                slowMoTime = slowMoTime,
                                magnetTime = magnetTime,
                                shakeAmount = shakeAmount,
                                cameraPunch = cameraPunch,
                                beatFlashIntensity = beatFlashIntensity,
                                enemies = enemies,
                                enemyTelegraphs = enemyTelegraphs,
                                tetheredSatellites = tetheredSatellites,
                                shockwaves = shockwaves,
                                particles = particles
                            )
                            starPowerTime = pickupRes.newStarPowerTime
                            shieldTime = pickupRes.newShieldTime
                            slowMoTime = pickupRes.newSlowMoTime
                            magnetTime = pickupRes.newMagnetTime
                            shakeAmount = pickupRes.newShakeAmount
                            cameraPunch = pickupRes.newCameraPunch
                            beatFlashIntensity = pickupRes.newBeatFlashIntensity
                            score += pickupRes.bonusScore
                            if (pickupRes.toast != null) activePowerUpToast = pickupRes.toast
                        } else {
                            triggerHaptic(48L)
                            cameraPunch = 0.038f
                            beatFlashIntensity = 0.60f
                            val supRes = handleSupernovaOrbPickup(
                                p = p,
                                player = player,
                                isGoldenActive = isGoldenActive,
                                scoreMultiplier = scoreMultiplier,
                                multiplierTime = multiplierTime,
                                difficultyScoreMultiplier = difficultyScoreMultiplier,
                                constellationCounter = constellationCounter,
                                magnetTime = magnetTime,
                                shakeAmount = shakeAmount,
                                starPowerTime = starPowerTime,
                                shieldTime = shieldTime,
                                enemies = enemies,
                                tetheredSatellites = tetheredSatellites,
                                shockwaves = shockwaves,
                                particles = particles
                            )
                            activePhenomenon = supRes.newPhenomenon
                            constellationCounter = supRes.newConstellationCounter
                            score += supRes.keywordBonus
                            magnetTime = supRes.newMagnetTime
                            shakeAmount = supRes.newShakeAmount
                            starPowerTime = supRes.newStarPowerTime
                            scoreMultiplier = supRes.newScoreMultiplier
                            multiplierTime = supRes.newMultiplierTime
                            shieldTime = supRes.newShieldTime
                            activePowerUpToast = supRes.toast
                        }

                        pIter.remove()
                    }
                }

                // 6. High-Contrast Enemy Kinematics & Fair Collision Logic
                val nextTargetOrb = powerUps.minByOrNull { it.sequenceIndex }
                val eIter = enemies.iterator()
                var nearMissCount = 0
                while (eIter.hasNext()) {
                    val e = eIter.next()
                    stepEnemyKinematicsAndFlocking(
                        e = e,
                        enemies = enemies,
                        player = player,
                        gameTime = gameTime,
                        currentScale = currentScale,
                        beatPhase = liveBeatPhase,
                        activeTargetOrb = nextTargetOrb,
                        activePhenomenon = activePhenomenon,
                        multiBand = mb,
                        allBeatOrbs = powerUps
                    )

                    val hitDist = if (e.type == EnemyType.LASER_SWEEPER) {
                        val halfLen = e.orbitRadius.coerceAtLeast(58f)
                        val cosA = cos(e.angle)
                        val sinA = sin(e.angle)
                        val relX = player.x - e.x
                        val relY = player.y - e.y
                        val proj = (relX * cosA + relY * sinA).coerceIn(-halfLen, halfLen)
                        val closestX = e.x + cosA * proj
                        val closestY = e.y + sinA * proj
                        hypot(player.x - closestX, player.y - closestY)
                    } else {
                        hypot(e.x - player.x, e.y - player.y)
                    }

                    if (hitDist < e.radius + player.radius && !isAdminMode && spawnInvulnerabilityTime <= 0f) {
                        if (e.type != EnemyType.PHANTOM || e.life > 0.3f) {
                            if (starPowerTime > 0f) {
                                // OVERDRIVE VAPORIZATION: Ramming enemies destroys them on contact without ending Overdrive!
                                val vapBonus = (150 * maxOf(scoreMultiplier, 2) * difficultyScoreMultiplier).toInt()
                                score += vapBonus
                                pulsePoints += 4
                                audioEngine.playOrbBeatHit(combo = rhythmCombo + 2, isKeyWordPeg = true)
                                triggerHaptic(40L)
                                shakeAmount = 9f
                                cameraPunch = 0.032f
                                shockwaves.add(
                                    Shockwave(
                                        x = e.x,
                                        y = e.y,
                                        radius = 0f,
                                        maxRadius = 190f,
                                        color = Color(0xFFFACC15L),
                                        life = 0.90f
                                    )
                                )
                                repeat(10) { idx ->
                                    val a = (idx / 10f) * 6.283185f
                                    particles.add(
                                        GameObject(
                                            x = e.x,
                                            y = e.y,
                                            radius = 4.5f,
                                            color = if (idx % 2 == 0) Color(0xFFFACC15L) else Color(0xFFF97316L),
                                            vx = cos(a) * 8.5f,
                                            vy = sin(a) * 8.5f,
                                            life = 0.75f
                                        )
                                    )
                                }
                                activePowerUpToast = PowerUpBannerToast(
                                    icon = "💥",
                                    title = "ENEMY VAPORIZED! +$vapBonus",
                                    subtitle = "OVERDRIVE RAMMING ACTIVE (${"%.1f".format(starPowerTime)}s)",
                                    accentColor = Color(0xFFFACC15L),
                                    timer = 1.6f,
                                    maxTimer = 1.6f
                                )
                                eIter.remove()
                            } else if (shieldTime > 0f) {
                                shieldTime = 0f
                                spawnInvulnerabilityTime = 1.25f
                                audioEngine.playHit()
                                triggerHaptic(55L)
                                shakeAmount = 10f
                                cameraPunch = 0.032f
                                shockwaves.add(
                                    Shockwave(
                                        x = player.x,
                                        y = player.y,
                                        radius = 0f,
                                        maxRadius = 260f,
                                        color = Color(0xFF38BDF8L),
                                        life = 1f
                                    )
                                )
                                activePowerUpToast = PowerUpBannerToast(
                                    icon = "🛡️",
                                    title = "SHIELD ABSORBED HIT!",
                                    subtitle = "PROTECTIVE BARRIER SAVED YOUR RUN",
                                    accentColor = Color(0xFF38BDF8L),
                                    timer = 2.0f,
                                    maxTimer = 2.0f
                                )
                                eIter.remove()
                            } else {
                                pulsePoints += ((score / 70).coerceIn(5, 45) * difficultyScoreMultiplier).toInt().coerceAtLeast(5)
                                gameOverCounter++
                                gameState = GameState.GAME_OVER
                                shockwaves.add(Shockwave(player.x, player.y, 0f, 500f, Color(0xFFEC4899L), 1f))
                                audioEngine.playDeath()
                                audioEngine.playWhooshChime("GAME_OVER")
                                triggerHaptic(120L)
                                shakeAmount = 28f
                                chromaticAmount = 4.5f
                                if (score > highScore) {
                                    highScore = score
                                    prefs.edit().putInt("high_score", highScore).apply()
                                    voiceOverManager.playVoiceOver("vo_new_record")
                                }
                            }
                        }
                    } else if (hitDist < 115f) {
                        nearMissCount++
                    }

                    if (e.x < -110f || e.x > screenWidth + 110f || e.y < -110f || e.y > screenHeight + 110f) {
                        eIter.remove()
                        score += (4 * scoreMultiplier * difficultyScoreMultiplier).toInt()
                    }
                }

                // 6b. Musical Slide-Note Wormhole Surf (Entry Portal A -> Bezier Light Stream -> Exit Portal B!)
                val surfRes = stepWormholeSlideAndPortals(
                    activeWormholeSlide = activeWormholeSlide,
                    wormholes = wormholes,
                    player = player,
                    targetX = targetX,
                    targetY = targetY,
                    currentScale = currentScale,
                    beatDurationSec = beatDurationSec,
                    spawnInvulnerabilityTime = spawnInvulnerabilityTime,
                    rhythmCombo = rhythmCombo,
                    onBeatHitsThisRun = onBeatHitsThisRun,
                    scoreMultiplier = scoreMultiplier,
                    difficultyScoreMultiplier = difficultyScoreMultiplier,
                    cameraPunch = cameraPunch,
                    beatFlashIntensity = beatFlashIntensity,
                    audioEngine = audioEngine,
                    triggerHaptic = triggerHaptic,
                    shockwaves = shockwaves,
                    floatingTexts = floatingTexts
                )
                activeWormholeSlide = surfRes.nextSlide
                targetX = surfRes.targetX
                targetY = surfRes.targetY
                spawnInvulnerabilityTime = surfRes.spawnInvulnerabilityTime
                rhythmCombo = surfRes.rhythmCombo
                onBeatHitsThisRun = surfRes.onBeatHitsThisRun
                score += surfRes.scoreDelta
                pulsePoints += surfRes.pulsePointsDelta
                cameraPunch = surfRes.cameraPunch
                beatFlashIntensity = surfRes.beatFlashIntensity
                if (surfRes.toast != null) activePowerUpToast = surfRes.toast

                updateAmbientEntities(
                    motes = motes,
                    particles = particles,
                    floatingTexts = floatingTexts,
                    shockwaves = shockwaves,
                    playerTrail = playerTrail,
                    player = player,
                    targetX = targetX,
                    targetY = targetY,
                    isPlaying = gameState == GameState.PLAYING,
                    currentScale = currentScale,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )

                if (nearMissCount > 0) {
                    timeScale = (timeScale - 0.03f).coerceAtLeast(0.45f)
                    if (Random.nextFloat() < 0.06f) { audioEngine.playNearMiss(); triggerHaptic(10L) }
                    if (nearMissPointCooldown <= 0f) {
                        val grazeScore = (nearMissCount * 5 * scoreMultiplier * difficultyScoreMultiplier).toInt().coerceIn(5, 25)
                        score += grazeScore
                        pulsePoints += 2
                        nearMissPointCooldown = 2.0f
                        if (nearMissCount >= 3) voiceOverManager.playVoiceOver("vo_near_miss_streak")
                    }
                } else {
                    timeScale = (timeScale + 0.02f).coerceAtMost(1f)
                }
                shakeAmount *= 0.88f
                chromaticAmount *= 0.88f
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        screenWidth = constraints.maxWidth.toFloat()
        screenHeight = constraints.maxHeight.toFloat()
        if (player.x == 0f) {
            player.x = screenWidth / 2f
            player.y = screenHeight * 0.72f
            targetX = player.x
            targetY = player.y
        }

        GamePlayfieldCanvas(
            gameState = gameState,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            shakeAmount = shakeAmount,
            chromaticAmount = chromaticAmount,
            focusWarp = focusWarp,
            gameTime = gameTime,
            lastLevelUpTime = lastLevelUpTime,
            isFocusActive = isFocusActive,
            shieldTime = shieldTime,
            spawnInvulnerabilityTime = spawnInvulnerabilityTime,
            customBgTheme = customBgTheme,
            customHeroSkin = customHeroSkin,
            trackRevealMusicProgress = trackRevealMusicProgress,
            juiceIntensity = juiceIntensity,
            stageMode = activeStageMode,
            beatPhase = liveBeatPhase,
            trackSec = liveTrackSec,
            cameraPunch = cameraPunch,
            goldenOverdriveIntensity = goldenOverdriveIntensity,
            beatFlashIntensity = beatFlashIntensity,
            audioEnergy = liveAudioEnergy,
            swellDelta = liveSwellDelta,
            multiBand = liveMultiBand,
            activePhenomenon = activePhenomenon,
            tetheredSatellites = tetheredSatellites,
            enemyTelegraphs = enemyTelegraphs,
            wormholes = wormholes,
            player = player,
            motes = motes,
            playerTrail = playerTrail,
            shockwaves = shockwaves,
            particles = particles,
            powerUps = powerUps,
            enemies = enemies,
            floatingTexts = floatingTexts,
            nebulaBitmap = nebulaBitmap,
            starsBack = starsBack,
            starsFront = starsFront,
            activeTrackArtwork = activeTrackArtwork,
            milestoneIcon = milestoneIcon,
            heroSolarMandala = heroSolarMandala,
            heroCyberMandala = heroCyberMandala,
            heroPrismMandala = heroPrismMandala,
            enemyPulsarMandala = enemyPulsarMandala,
            enemySeekerMandala = enemySeekerMandala,
            enemyWandererMandala = enemyWandererMandala,
            applyColorBlind = applyColorBlind,
            onDragPlayer = { dx, dy ->
                targetX = (targetX + dx).coerceIn(player.radius, screenWidth - player.radius)
                targetY = (targetY + dy).coerceIn(player.radius, screenHeight - player.radius)
            }
        )

        val currentTrackMeta = MusicLibrary.getTrack(selectedTrackIndex)
        if (gameState == GameState.PLAYING || gameState == GameState.COUNTDOWN || gameState == GameState.PAUSED) {
            HUD(
                score = score,
                highScore = highScore,
                pulsePoints = pulsePoints,
                unlockedTrackCount = unlockedTrackCount,
                trackIndex = selectedTrackIndex,
                trackProgress = trackRevealMusicProgress,
                focus = focusPower,
                multiplier = scoreMultiplier,
                multiplierTime = multiplierTime,
                btnIconPause = btnIconPause,
                difficulty = gameDifficulty,
                currentSectionLabel = activeSectionLabel,
                isExtraJuiceActive = juiceIntensity > 0.45f || liveAudioEnergy > 0.72f,
                activePowerUpToast = activePowerUpToast,
                toastTimeLeft = toastTimeLeft,
                starPowerTime = starPowerTime,
                shieldTime = shieldTime,
                slowMoTime = slowMoTime,
                magnetTime = magnetTime,
                endlessLoopCount = if (selectedRemixMode == TrackRemixMode.ENDLESS) lastLoopCycleSeen else 0,
                onPause = { if (gameState == GameState.PLAYING) gameState = GameState.PAUSED }
            )
        }

        if (gameState == GameState.PLAYING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 36.dp, end = 20.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                ArcadeFocusButton(
                    focusPower = focusPower,
                    isActive = isFocusActive,
                    onPressStart = {
                        if (focusPower > 0.15f) {
                            isFocusActive = true
                            audioEngine.playFocus()
                            voiceOverManager.playVoiceOver("vo_focus_enter")
                        }
                    },
                    onPressEnd = {
                        isFocusActive = false
                    }
                )
            }
        }

        if (gameState != GameState.MUSIC_DEMO) {
            Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()) {
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = AdManager.bannerAdUnitId
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }

        when (gameState) {
            GameState.MENU -> MenuUI(
                heroSolarMandala = heroSolarMandala,
                heroCyberMandala = heroCyberMandala,
                heroPrismMandala = heroPrismMandala,
                btnPillGreen = btnPillGreen,
                btnPillGold = btnPillGold,
                btnPillCyan = btnPillCyan,
                btnPillPink = btnPillPink,
                customHeroSkin = customHeroSkin,
                selectedTrackIndex = selectedTrackIndex,
                unlockedTrackCount = unlockedTrackCount,
                activeLobbyTrack = activeLobbyTrack,
                selectedRemixMode = selectedRemixMode,
                onSelectRemixMode = { mode ->
                    selectedRemixMode = mode
                    audioEngine.setLobbyRemixMode(mode, playFeedback = true)
                },
                pulsePoints = pulsePoints,
                highScore = highScore,
                trackMasteryStars = parseCompletionsCsv(trackMasteryStarsCsv),
                isVipUnlocked = isVipUnlocked,
                onCycleLobbyTrack = {
                    val entries = LobbyTrack.entries
                    val next = entries[(entries.indexOf(activeLobbyTrack) + 1) % entries.size]
                    activeLobbyTrack = next
                    audioEngine.playMenuNote()
                },
                onSelectQuickTrack = { idx ->
                    audioEngine.playMenuNote()
                    selectedTrackIndex = idx.coerceIn(1, totalTracks)
                },
                onStart = {
                    audioEngine.playMenuNote()
                    startFreshLevelRun(selectedTrackIndex)
                },
                onMusicDemo = { audioEngine.playMenuNote(); gameState = GameState.MUSIC_DEMO },
                onHowToPlay = { audioEngine.playMenuNote(); gameState = GameState.HOW_TO_PLAY },
                onSettings = {
                    audioEngine.playMenuNote()
                    settingsReturnState = GameState.MENU
                    gameState = GameState.SETTINGS
                }
            )

            GameState.COUNTDOWN -> {
                CountdownUI(countdownValue, visible = !showDragHint)
                if (showDragHint) {
                    TouchControlHintBanner(
                        btnPillCyan = btnPillCyan,
                        btnPillGreen = btnPillGreen,
                        onDismissPermanently = {
                            audioEngine.playMenuNote()
                            prefs.edit().putBoolean("show_drag_hint", false).apply()
                            showDragHint = false
                        },
                        onDismissOnce = {
                            audioEngine.playMenuNote()
                            showDragHint = false
                        }
                    )
                }
            }

            GameState.PAUSED -> PausedUI(
                uiDialogPanel = uiDialogPanel,
                btnPillGreen = btnPillGreen,
                btnPillCyan = btnPillCyan,
                btnPillGold = btnPillGold,
                btnPillPink = btnPillPink,
                onResume = { audioEngine.playMenuNote(); gameState = GameState.PLAYING },
                onReplay = {
                    audioEngine.playMenuNote()
                    startFreshLevelRun(selectedTrackIndex)
                },
                onMenu = { audioEngine.playMenuNote(); gameState = GameState.MENU },
                onSettings = {
                    audioEngine.playMenuNote()
                    settingsReturnState = GameState.PAUSED
                    gameState = GameState.SETTINGS
                }
            )

            GameState.HOW_TO_PLAY -> HowToPlayUI(btnPillCyan = btnPillCyan) {
                audioEngine.playMenuNote()
                gameState = GameState.MENU
            }

            GameState.SETTINGS -> SettingsUI(
                isMusic = isMusicEnabled,
                isSound = isSoundEnabled,
                isHaptic = isHapticEnabled,
                difficulty = gameDifficulty,
                activeLobbyTrack = activeLobbyTrack,
                onLobbyTrackChange = {
                    activeLobbyTrack = it
                    audioEngine.playMenuNote()
                },
                btnPillGold = btnPillGold,
                onMusicToggle = { isMusicEnabled = it },
                onSoundToggle = { isSoundEnabled = it },
                onHapticToggle = { isHapticEnabled = it },
                onDifficultyChange = { gameDifficulty = it },
                isVipUnlocked = isVipUnlocked,
                onActivateVipPasscode = {
                    isVipUnlocked = true
                    unlockedTrackCount = totalTracks
                    pulsePoints += 50000
                    audioEngine.playVictory()
                },
                onBack = { audioEngine.playMenuNote(); gameState = settingsReturnState }
            )

            GameState.GAME_OVER -> GameOverUI(
                pulsePoints = pulsePoints,
                score = score,
                highScore = highScore,
                trackLabel = "TRACK %02d • ${currentTrackMeta.title}".format(selectedTrackIndex),
                canContinue = pulsePoints >= 250,
                hasAd = adManager.isAdAvailable(),
                btnPillGreen = btnPillGreen,
                btnPillCyan = btnPillCyan,
                btnPillGold = btnPillGold,
                btnIconRestart = btnIconRestart,
                uiDialogPanel = uiDialogPanel,
                onContinue = {
                    audioEngine.playMenuNote()
                    pulsePoints = (pulsePoints - 250).coerceAtLeast(0)
                    reviveCurrentRun()
                },
                onWatchAd = {
                    audioEngine.playMenuNote()
                    adManager.showRewardedAd(
                        context as Activity,
                        onRewardEarned = { reviveCurrentRun() },
                        onAdDismissed = {}
                    )
                },
                onReplayTrack = {
                    audioEngine.playMenuNote()
                    if (gameOverCounter > 0 && gameOverCounter % 3 == 0) {
                        adManager.showInterstitialIfReady(context as Activity) {
                            startFreshLevelRun(selectedTrackIndex)
                        }
                    } else {
                        startFreshLevelRun(selectedTrackIndex)
                    }
                },
                onBackToAlbum = { audioEngine.playMenuNote(); gameState = GameState.MUSIC_DEMO },
                onBuyPoints = { audioEngine.playMenuNote(); billingManager.purchasePoints(context as Activity) }
            )

            GameState.LEVEL_COMPLETE -> LevelCompleteUI(
                trackIndex = selectedTrackIndex,
                score = score,
                maxCombo = maxComboThisRun,
                notesEarned = (75 * selectedRemixMode.noteMultiplier).toInt() + onBeatHitsThisRun * 5,
                unlockedVaultCount = if (isVipUnlocked) ARTIST_EXCLUSIVES_CATALOG.size else countUnlockedExclusives(prefs, highScore, pulsePoints),
                uiDialogPanel = uiDialogPanel,
                btnPillGreen = btnPillGreen,
                btnPillGold = btnPillGold,
                onNextTrack = {
                    audioEngine.playMenuNote()
                    adManager.showInterstitialIfReady(context as Activity) {
                        if (selectedTrackIndex < totalTracks) {
                            startFreshLevelRun(selectedTrackIndex + 1)
                        } else {
                            gameState = GameState.MUSIC_DEMO
                        }
                    }
                },
                onOpenArtistVault = {
                    audioEngine.playWhooshChime("MODAL")
                    adManager.showInterstitialIfReady(context as Activity) {
                        gameState = GameState.MUSIC_DEMO
                    }
                },
                onBackToAlbum = {
                    audioEngine.playMenuNote()
                    adManager.showInterstitialIfReady(context as Activity) {
                        gameState = GameState.MENU
                    }
                }
            )

            GameState.MUSIC_DEMO -> MusicDemoScreen(
                pulsePoints = pulsePoints,
                unlockedTrackCount = unlockedTrackCount,
                nebula = nebulaBitmap,
                onBack = { audioEngine.playMenuNote(); gameState = GameState.MENU },
                trackListMode = trackListMode,
                trackCompletionsCsv = trackCompletionsCsv,
                onUpdateMeta = { newPoints, newUnlockedCount, newMode, newCompletionsCsv ->
                    pulsePoints = newPoints
                    unlockedTrackCount = newUnlockedCount
                    trackListMode = newMode
                    trackCompletionsCsv = newCompletionsCsv
                },
                selectedTrackIndexState = selectedTrackIndex,
                onSelectTrack = { idx -> audioEngine.playMenuNote(); selectedTrackIndex = idx },
                onPlayGame = { idx ->
                    audioEngine.playMenuNote()
                    startFreshLevelRun(idx)
                },
                onBuyTrack = { idx -> audioEngine.playMenuNote(); billingManager.purchaseProduct(context as Activity, "track_${idx.toString().padStart(2, '0')}") },
                onBuyAlbum = { audioEngine.playMenuNote(); billingManager.purchaseProduct(context as Activity, "full_album_unlock") },
                customBgTheme = customBgTheme,
                customHeroSkin = customHeroSkin,
                customEnemyStyle = customEnemyStyle,
                unlockedItemsCsv = unlockedShopItemsCsv,
                isVipUnlocked = isVipUnlocked,
                onUnlockShopItem = { itemId, cost ->
                    if (pulsePoints >= cost) {
                        pulsePoints -= cost
                        val items = unlockedShopItemsCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                        items.add(itemId)
                        unlockedShopItemsCsv = items.joinToString(",")
                        audioEngine.playVictory()
                    }
                },
                onWatchAdForNotes = {
                    audioEngine.playMenuNote()
                    adManager.showRewardedAd(
                        context as Activity,
                        onRewardEarned = {
                            pulsePoints += 150
                            audioEngine.playVictory()
                        },
                        onAdDismissed = {}
                    )
                },
                onActivateVipPasscode = {
                    isVipUnlocked = true
                    unlockedTrackCount = totalTracks
                    pulsePoints += 50000
                    audioEngine.playVictory()
                },
                onUpdateCustomization = { bg, hero, enemy ->
                    customBgTheme = bg
                    customHeroSkin = hero
                    customEnemyStyle = enemy
                },
                heroSolarMandala = heroSolarMandala,
                heroCyberMandala = heroCyberMandala,
                heroPrismMandala = heroPrismMandala,
                enemyPulsarMandala = enemyPulsarMandala,
                enemySeekerMandala = enemySeekerMandala,
                enemyWandererMandala = enemyWandererMandala,
                btnPillGreen = btnPillGreen,
                btnPillCyan = btnPillCyan,
                btnPillGold = btnPillGold,
                btnIconClose = btnIconClose
            )
            else -> {}
        }

        // First-Time Contextual Discovery Overlay (Fan Welcome on First Launch + Auto-Pause In-Game Tutorials)
        val curDiscovery = activeDiscovery
        if (curDiscovery != null) {
            FirstTimeDiscoveryModal(
                discovery = curDiscovery,
                onDismiss = {
                    markDiscoverySeen(prefs, curDiscovery)
                    audioEngine.playWhooshChime("MODAL")
                    val resumeGame = (gameState == GameState.PLAYING)
                    activeDiscovery = null
                    if (resumeGame) {
                        spawnInvulnerabilityTime = maxOf(spawnInvulnerabilityTime, 1.8f)
                        audioEngine.resumeTrack()
                    }
                }
            )
        }
    }
}
