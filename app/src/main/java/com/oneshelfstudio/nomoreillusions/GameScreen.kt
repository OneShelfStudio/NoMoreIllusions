package com.oneshelfstudio.nomoreillusions

import android.app.Activity
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.ui.graphics.graphicsLayer
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
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
    val track01Art = ImageBitmap.imageResource(id = R.drawable.track_01)
    val track02Art = ImageBitmap.imageResource(id = R.drawable.track_02)

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

    // Dismissable control hint preference
    var showDragHint by remember { mutableStateOf(prefs.getBoolean("show_drag_hint", true)) }
    var isRevivingRun by remember { mutableStateOf(false) }

    var gameState by remember { mutableStateOf(GameState.MENU) }
    var settingsReturnState by remember { mutableStateOf(GameState.MENU) }

    // Intercept hardware/system back gestures so pressing back navigates menus naturally instead of killing the app
    BackHandler(enabled = gameState != GameState.MENU) {
        when (gameState) {
            GameState.PLAYING -> {
                audioEngine.pauseTrack()
                gameState = GameState.PAUSED
            }
            GameState.PAUSED,
            GameState.COUNTDOWN -> {
                audioEngine.stopTrack()
                audioEngine.startThemeMusic()
                gameState = GameState.MENU
            }
            GameState.SETTINGS -> {
                gameState = settingsReturnState
                if (settingsReturnState == GameState.MENU) {
                    audioEngine.startThemeMusic()
                }
            }
            GameState.MUSIC_DEMO,
            GameState.HOW_TO_PLAY,
            GameState.LEVEL_COMPLETE,
            GameState.GAME_OVER -> {
                audioEngine.stopTrack()
                audioEngine.startThemeMusic()
                gameState = GameState.MENU
            }
            GameState.MENU -> { /* System back exits app */ }
        }
    }

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
                    audioEngine.playVictory()
                }
                productId == "full_album_unlock" -> {
                    audioEngine.playVictory()
                }
                productId.startsWith("track_") -> {
                    val trackNum = productId.removePrefix("track_").toIntOrNull()
                    if (trackNum != null) {
                        selectedTrackIndex = trackNum.coerceIn(1, totalTracks)
                        prefs.edit()
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
    var orbsCollectedThisRun by remember { mutableIntStateOf(0) }
    var totalTrackOrbsThisRun by remember { mutableIntStateOf(40) }
    var lastCompletionBonusNotes by remember { mutableIntStateOf(0) }
    var maxComboThisRun by remember { mutableIntStateOf(1) }
    var initialDemoTab by remember { mutableIntStateOf(1) }
    var activeDiscovery by remember { mutableStateOf<FirstTimeDiscovery?>(null) }
    var isPreMusicDrift by remember { mutableStateOf(false) }
    var preMusicElapsed by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(gameState, selectedTrackIndex, isPreMusicDrift) {
        if (gameState == GameState.COUNTDOWN) {
            audioEngine.pauseThemeMusic()
        }
        if (gameState == GameState.PLAYING && !isPreMusicDrift) {
            audioEngine.resumeTrack()
            if (!audioEngine.isTrackPlaying()) {
                audioEngine.startTrack(selectedTrackIndex, remixMode = selectedRemixMode)
            }
        }
        if (gameState == GameState.MENU) {
            isPreMusicDrift = false
            audioEngine.stopTrack()
            audioEngine.startThemeMusic()
        }
        if (gameState == GameState.MUSIC_DEMO) {
            isPreMusicDrift = false
            audioEngine.stopTrack()
        }
        if (gameState == GameState.GAME_OVER || gameState == GameState.PAUSED || gameState == GameState.LEVEL_COMPLETE) audioEngine.pauseTrack()
    }

    // Periodic off-frame-loop clock synchronization (every 120ms)
    LaunchedEffect(gameState) {
        while (gameState == GameState.COUNTDOWN || gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            trackRevealMusicProgress = audioEngine.trackProgressFraction()
            if (trackRevealMusicProgress >= 0.99f && gameState == GameState.PLAYING && selectedRemixMode != TrackRemixMode.ENDLESS) {
                val counts = parseCompletionsCsv(trackCompletionsCsv)
                val trackIdx = (selectedTrackIndex - 1).coerceIn(0, totalTracks - 1)
                val isEasyOrChill = selectedRemixMode == TrackRemixMode.SLOWED || gameDifficulty == GameDifficulty.EASY
                counts[trackIdx] += if (isEasyOrChill) 1 else 2
                trackCompletionsCsv = toCompletionsCsv(counts)

                // Easy/Chill requires 2 completions (counts >= 2) to unlock the full Album Art & Song; Normal/Fast unlocks in 1 completion!
                val isFullyUnlockedNow = counts[trackIdx] >= 2

                // Update 3-Star Track Mastery (★1 Full Unlock, ★2 8+ On-Beat Streak, ★3 Fast Overclock Clear)
                val masteryArr = parseCompletionsCsv(trackMasteryStarsCsv)
                var earnedStars = masteryArr[trackIdx]
                if (isFullyUnlockedNow) earnedStars = maxOf(earnedStars, 1)
                if (isFullyUnlockedNow && onBeatHitsThisRun >= 8) earnedStars = maxOf(earnedStars, 2)
                if (selectedRemixMode == TrackRemixMode.OVERCLOCK) {
                    earnedStars = 3
                }
                masteryArr[trackIdx] = earnedStars
                trackMasteryStarsCsv = toCompletionsCsv(masteryArr)

                val orbCompletionPct = ((orbsCollectedThisRun * 100f) / totalTrackOrbsThisRun.coerceAtLeast(1)).toInt().coerceIn(0, 100)
                if (orbCompletionPct >= 100) {
                    earnedStars = 3
                    masteryArr[trackIdx] = earnedStars
                    trackMasteryStarsCsv = toCompletionsCsv(masteryArr)
                }
                val pctBonusNotes = when {
                    orbCompletionPct >= 100 -> 1000
                    orbCompletionPct >= 90 -> 350
                    orbCompletionPct >= 75 -> 150
                    else -> 0
                }
                lastCompletionBonusNotes = pctBonusNotes

                if (isFullyUnlockedNow && selectedTrackIndex == unlockedTrackCount && unlockedTrackCount < totalTracks) {
                    unlockedTrackCount++
                }
                pulsePoints += (75 * selectedRemixMode.noteMultiplier * (if (gameDifficulty == GameDifficulty.EASY) 0.5f else 1.0f)).toInt().coerceAtLeast(15) + pctBonusNotes

                val editor = prefs.edit()
                    .putString("track_completions", trackCompletionsCsv)
                    .putString("track_mastery_stars", trackMasteryStarsCsv)
                    .putInt("unlocked_tracks", unlockedTrackCount)
                    .putInt("pulse_points", pulsePoints)
                    .putInt("best_combo", maxOf(prefs.getInt("best_combo", 0), maxComboThisRun))
                if (isFullyUnlockedNow) {
                    editor.putBoolean("art_unlocked_$selectedTrackIndex", true)
                }
                editor.apply()

                audioEngine.playVictory()
                gameState = GameState.LEVEL_COMPLETE
                voiceOverManager.playVoiceOver("vo_track_decoded")
            } else if (trackRevealMusicProgress >= 0.99f && gameState == GameState.PLAYING && selectedRemixMode == TrackRemixMode.ENDLESS) {
                val nextBestLoop = prefs.getInt("endless_best_loop", 0) + 1
                prefs.edit().putInt("endless_best_loop", nextBestLoop).apply()
            }
            delay(120)
        }
    }

    // Entities & Physics State
    var player by remember { mutableStateOf(GameObject(0f, 0f, 18f, Color(0xFF22C55EL))) }
    var targetX by remember { mutableFloatStateOf(0f) }
    var targetY by remember { mutableFloatStateOf(0f) }
    var secondTouchPos by remember { mutableStateOf<Offset?>(null) }
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
    var overdriveProgress by remember { mutableFloatStateOf(0f) }
    var beatFlashIntensity by remember { mutableFloatStateOf(0f) }
    var gameTime by remember { mutableFloatStateOf(0f) }
    var levelPlayTime by remember { mutableFloatStateOf(0f) }
    var shieldTime by remember { mutableFloatStateOf(0f) }
    var slowMoTime by remember { mutableFloatStateOf(0f) }
    var magnetTime by remember { mutableFloatStateOf(0f) }
    var lastLoopCycleSeen by remember { mutableIntStateOf(0) }
    var activePowerUpToast by remember { mutableStateOf<PowerUpBannerToast?>(null) }
    var primedSectionEffect by remember { mutableStateOf<PrimedSectionEffect?>(null) }
    var toastTimeLeft by remember { mutableFloatStateOf(0f) }
    var comboZoomBanner by remember { mutableStateOf<ComboZoomBannerData?>(null) }
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
    var isBandHeroMode by remember { mutableStateOf(false) }
    var vocalStreamContrailTimer by remember { mutableFloatStateOf(0f) }
    var isBrollEnabled by remember { mutableStateOf(prefs.getBoolean("enable_broll_video", true)) }
    var brollActive by remember { mutableStateOf(false) }
    var brollTimer by remember { mutableFloatStateOf(0f) }
    var brollDurationTotal by remember { mutableFloatStateOf(11.0f) }
    var brollClipIndex by remember { mutableIntStateOf(0) }
    var brollBaseClip by remember { mutableIntStateOf(0) }
    var brollClipFadeAlpha by remember { mutableFloatStateOf(0f) }
    var brollCooldown by remember { mutableFloatStateOf(0f) }
    var cipherMeterProgress by remember { mutableFloatStateOf(0f) }
    var isCipherArmed by remember { mutableStateOf(false) }
    var cipherArmedAtSec by remember { mutableFloatStateOf(0f) }
    var videoWindowIndex by remember { mutableIntStateOf(0) }
    var videoWindowFailed by remember { mutableStateOf(false) }
    var activeGoldenTier by remember { mutableIntStateOf(0) }

    val difficultyScoreMultiplier = getDifficultyScoreMultiplier(gameDifficulty, selectedRemixMode)
    val trackDifficultyBoost = getTrackDifficultyBoost(selectedTrackIndex)
    val speedScale = getSpeedScale(gameDifficulty, trackDifficultyBoost)
    val spawnRateScale = getSpawnRateScale(gameDifficulty, trackDifficultyBoost, selectedRemixMode)

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

    val triggerErrorHaptic = {
        if (isHapticEnabled) {
            runCatching {
                if (vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 55L, 65L, 85L), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(140L)
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
        orbsCollectedThisRun = 0
        val isTrainRun = (gameDifficulty == GameDifficulty.EASY || selectedRemixMode == TrackRemixMode.SLOWED)
        totalTrackOrbsThisRun = countPlayableChartOrbs(MusicLibrary.getTrack(selectedTrackIndex).orbChart, isTrainRun)
        lastCompletionBonusNotes = 0
        cipherMeterProgress = 0f
        isCipherArmed = false
        cipherArmedAtSec = 0f
        videoWindowIndex = 0
        videoWindowFailed = false
        brollActive = false
        brollTimer = 0f
        brollClipFadeAlpha = 0f
        activeGoldenTier = 0
        isPreMusicDrift = false
        preMusicElapsed = 0f
        level = 1
        focusPower = 1f
        isFocusActive = false
        shieldTime = 0f
        slowMoTime = 0f
        magnetTime = 0f
        lastLoopCycleSeen = 0
        activePowerUpToast = null
        comboZoomBanner = null
        scoreMultiplier = 1
        multiplierTime = 0f
        shakeAmount = 0f
        chromaticAmount = 0f
        cameraPunch = 0f
        goldenOverdriveIntensity = 0f
        starPowerTime = 0f
        overdriveProgress = 0f
        beatFlashIntensity = 0f
        lastTriggeredSectionSec = -1f
        activeSectionLabel = "INTRO"
        activeStageMode = StageMode.ABYSSAL_WAVE
        juiceIntensity = 0f
        nextOrbCueIdx = 0
        vocalStreamContrailTimer = 0f
        activePhenomenon = null
        activeWormholeSlide = null
        primedSectionEffect = null
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
        liveTrackSec = -1.0f
        player = GameObject(screenWidth / 2f, screenHeight * 0.65f, 18f, applyColorBlind(Color(0xFF22C55EL)))
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
        primedSectionEffect = null
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
        player = GameObject(screenWidth / 2f, screenHeight * 0.65f, 18f, applyColorBlind(Color(0xFF22C55EL)))
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
            delay(150)
            levelPlayTime = 0f
            preMusicElapsed = 0f
            spawnInvulnerabilityTime = 3.0f
            if (isRevivingRun) {
                isPreMusicDrift = false
                gameState = GameState.PLAYING
                audioEngine.resumeTrack()
            } else {
                // 1-second pre-music movement buffer: player has full glide control, motes move,
                // and initial orbs slowly start drifting in WITHOUT pulsing before music begins!
                isPreMusicDrift = true
                gameState = GameState.PLAYING
                delay(1000)
                if (gameState == GameState.PLAYING) {
                    audioEngine.startTrack(selectedTrackIndex, forceRestart = true, remixMode = selectedRemixMode)
                    isPreMusicDrift = false
                }
            }
        }
    }

    // Zero-Stutter 60/120Hz Physics & 4Hz Multi-Band Musical Choreography Loop
    LaunchedEffect(gameState) {
        if (gameState != GameState.PLAYING && gameState != GameState.MENU) return@LaunchedEffect
        var lastFrameNanos = 0L
        while (isActive && (gameState == GameState.PLAYING || gameState == GameState.MENU)) {
            withFrameNanos { frameTimeNanos ->
                val dt = if (lastFrameNanos == 0L) 0.016f else ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastFrameNanos = frameTimeNanos
                val frameFactor = dt / 0.016f

                val isAudioRunning = audioEngine.isTrackPlaying()
                liveBeatPhase = if (isAudioRunning) audioEngine.getBeatPhase() else -1f
                if (gameState == GameState.MENU) {
                    gameTime += dt
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

                val currentScale = (when {
                    isFocusActive -> 0.32f
                    slowMoTime > 0f -> 0.48f
                    else -> timeScale
                }) * frameFactor
                gameTime += dt * currentScale
                levelPlayTime += dt * currentScale
                survivalPointAccumulator += dt * currentScale

                if (spawnInvulnerabilityTime > 0f) {
                    spawnInvulnerabilityTime = (spawnInvulnerabilityTime - dt * currentScale).coerceAtLeast(0f)
                }
                if (slowMoTime > 0f) {
                    slowMoTime = (slowMoTime - dt).coerceAtLeast(0f)
                }
                if (magnetTime > 0f) {
                    magnetTime = (magnetTime - dt).coerceAtLeast(0f)
                }
                if (vocalStreamContrailTimer > 0f) {
                    vocalStreamContrailTimer = (vocalStreamContrailTimer - dt * currentScale).coerceAtLeast(0f)
                }
                val curToast = activePowerUpToast
                if (curToast != null) {
                    val nextToastTime = curToast.timer - dt
                    if (nextToastTime <= 0f) {
                        activePowerUpToast = null
                        toastTimeLeft = 0f
                    } else {
                        curToast.timer = nextToastTime
                        toastTimeLeft = nextToastTime
                    }
                }

                val curBanner = comboZoomBanner
                if (curBanner != null) {
                    val nextBannerTime = curBanner.timer - dt
                    if (nextBannerTime <= 0f) {
                        comboZoomBanner = null
                    } else {
                        comboZoomBanner = curBanner.copy(timer = nextBannerTime)
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
                if (isPreMusicDrift && !isAudioRunning) {
                    preMusicElapsed = (preMusicElapsed + dt).coerceAtMost(1.0f)
                }
                val rawTrackSec = audioEngine.trackPositionSeconds()
                // During pre-music movement buffer, trackSec advances smoothly from -1.0s to 0.0s with real wall-clock dt
                val trackSec = if (isAudioRunning) rawTrackSec else (preMusicElapsed - 1.0f).coerceIn(-1.0f, 0.0f)
                liveTrackSec = trackSec

                // Manage Video Relic Window 1 vs Window 2 reset cleanly
                val curWindowIdx = if (selectedTrackIndex == 1) {
                    when {
                        trackSec in 30.5f..60.8f -> 1
                        trackSec in 114.5f..156.8f -> 2
                        else -> 0
                    }
                } else {
                    when {
                        trackSec in 31.5f..55.2f -> 1
                        trackSec in 94.5f..119.2f -> 2
                        else -> 0
                    }
                }
                if (curWindowIdx != 0 && curWindowIdx != videoWindowIndex) {
                    videoWindowIndex = curWindowIdx
                    videoWindowFailed = false
                    if (!brollActive) {
                        cipherMeterProgress = 0f
                    }
                }

                val activeTrack = MusicLibrary.getTrack(selectedTrackIndex)
                val mb = MusicLibrary.getMultiBandStateAt(selectedTrackIndex, trackSec.coerceAtLeast(0f))
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
                    val nextTimer = curPhenom.timer - dt * currentScale
                    if (nextTimer <= 0f) {
                        activePhenomenon = null
                    } else {
                        curPhenom.timer = nextTimer
                    }
                }

                // Check if a Primed Milestone Section Effect should ignite on this exact downbeat!
                val primed = primedSectionEffect
                if (primed != null && trackSec >= primed.targetSec) {
                    if (primed.goldenTier > 0) {
                        activeGoldenTier = maxOf(activeGoldenTier, primed.goldenTier)
                        starPowerTime = maxOf(starPowerTime, 15.5f)
                        goldenOverdriveIntensity = 1.0f
                    }
                    when (primed.type) {
                        PowerUpType.ELECTRIC_HARMONIC -> {
                            starPowerTime = maxOf(starPowerTime, 15.5f)
                            shakeAmount = 14f
                            cameraPunch = 0.06f
                            beatFlashIntensity = 1.0f
                            audioEngine.playElectricSurgeStinger()
                            shockwaves.add(Shockwave(player.x, player.y, 0f, 650f, Color(0xFFFACC15L), 1.3f))
                            shockwaves.add(Shockwave(player.x, player.y, 0f, 450f, Color(0xFF38BDF8L), 1.1f))
                            activePowerUpToast = PowerUpBannerToast(
                                icon = "⚡",
                                title = if (primed.goldenTier >= 2) "24K HYPER-SPECTACLE!" else "24K GOLD MODE!",
                                subtitle = if (primed.goldenTier >= 2) "SOLAR MANDALA + TWIN-RING PULSE" else "SYNTH AURORA & 4X OVERDRIVE",
                                accentColor = Color(0xFFFACC15L),
                                timer = 2.6f,
                                maxTimer = 2.6f
                            )
                            toastTimeLeft = 2.6f
                        }
                        PowerUpType.PRISM_HARMONY -> {
                            beatFlashIntensity = 0.90f
                            cameraPunch = 0.045f
                            audioEngine.playPrismBloomStinger()
                            shockwaves.add(Shockwave(player.x, player.y, 0f, 500f, Color(0xFF4ADE80L), 1.2f))
                            shockwaves.add(Shockwave(player.x, player.y, 0f, 320f, Color(0xFFEC4899L), 1.0f))
                            activePowerUpToast = PowerUpBannerToast(
                                icon = "✨",
                                title = "PRISM VOCAL BLOOM!",
                                subtitle = "CRYSTAL HARMONY & ART REVEAL",
                                accentColor = Color(0xFF4ADE80L),
                                timer = 2.6f,
                                maxTimer = 2.6f
                            )
                            toastTimeLeft = 2.6f
                        }
                        PowerUpType.SLOW_MO -> {
                            slowMoTime = maxOf(slowMoTime, 6.0f)
                            audioEngine.playSlowMoStinger()
                            shockwaves.add(Shockwave(player.x, player.y, 0f, 420f, Color(0xFFC084FCL), 1.1f))
                            activePowerUpToast = PowerUpBannerToast(
                                icon = "🌀",
                                title = "VORTEX TIME-WARP!",
                                subtitle = "50% SLOW-MO BREAKDOWN FLOW",
                                accentColor = Color(0xFFC084FCL),
                                timer = 2.6f,
                                maxTimer = 2.6f
                            )
                            toastTimeLeft = 2.6f
                        }
                        PowerUpType.BLAST -> {
                            shakeAmount = 16f
                            cameraPunch = 0.055f
                            beatFlashIntensity = 0.95f
                            audioEngine.playSolarNovaStinger()
                            shockwaves.add(Shockwave(player.x, player.y, 0f, 750f, Color(0xFFF97316L), 1.4f))
                            enemies.clear()
                            enemyTelegraphs.clear()
                            for (s in 0 until 28) {
                                val sAngle = Random.nextFloat() * 2f * PI.toFloat()
                                val sSpd = 260f + Random.nextFloat() * 320f
                                particles.add(
                                    GameObject(
                                        x = player.x,
                                        y = player.y,
                                        vx = cos(sAngle) * sSpd,
                                        vy = sin(sAngle) * sSpd,
                                        radius = 3.5f + Random.nextFloat() * 3f,
                                        color = if (s % 3 == 0) Color(0xFFFDE047L) else if (s % 3 == 1) Color(0xFF38BDF8L) else Color(0xFFF97316L),
                                        life = 1.2f
                                    )
                                )
                            }
                            activePowerUpToast = PowerUpBannerToast(
                                icon = "💥",
                                title = "SOLAR NOVA DETONATION!",
                                subtitle = "ALL FOES VAPORIZED",
                                accentColor = Color(0xFFF97316L),
                                timer = 2.6f,
                                maxTimer = 2.6f
                            )
                            toastTimeLeft = 2.6f
                        }
                        PowerUpType.SHIELD -> {
                            shieldTime = maxOf(shieldTime, 6.0f)
                            audioEngine.playAegisStinger()
                            shockwaves.add(Shockwave(player.x, player.y, 0f, 380f, Color(0xFF38BDF8L), 1.0f))
                            activePowerUpToast = PowerUpBannerToast(
                                icon = "🛡️",
                                title = "AEGIS SHIELD ENGAGED!",
                                subtitle = "6.0s DAMAGE IMMUNITY",
                                accentColor = Color(0xFF38BDF8L),
                                timer = 2.6f,
                                maxTimer = 2.6f
                            )
                            toastTimeLeft = 2.6f
                        }
                        else -> {}
                    }
                    if (primed.keyWordEffect != KeyWordEffect.NONE) {
                        val dummyOrb = GameObject(
                            x = player.x,
                            y = player.y,
                            radius = 38f,
                            color = Color(0xFFFACC15L),
                            keyWordEffect = primed.keyWordEffect
                        )
                        val supRes = handleSupernovaOrbPickup(
                            p = dummyOrb,
                            player = player,
                            isGoldenActive = goldenOverdriveIntensity > 0.25f,
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
                    triggerHaptic(55L)
                    primedSectionEffect = null
                }

                // 2. Live Section & Musical Swell Transitions (Zero text popups on orbs; clean section shift pulse)
                val currentSecObj = MusicLibrary.currentSectionAt(selectedTrackIndex, trackSec)
                // Video playback is strictly unlocked by collecting the designated Video Relic notes (no random unexpected activations)
                if (currentSecObj.timestampSec != lastTriggeredSectionSec) {
                    lastTriggeredSectionSec = currentSecObj.timestampSec
                    activeSectionLabel = currentSecObj.label
                    activeStageMode = currentSecObj.stageMode

                    // If entering a Vocal Float, Pre-Drop Hush, or Victory Outro, dissolve all active enemies into stardust!
                    // Never clear `wormholes` here so active 2-Finger Vocal Sustain Holds remain intact during vocal floats!
                    if (currentSecObj.clearEnemiesOnEnter) {
                        enemyTelegraphs.clear()
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
                if (selectedTrackIndex == 1 && trackSec in 201.0f..207.5f && liveBeatPhase in 0f..0.14f && shockwaves.size < 6) {
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

                // Update 24K Golden Overdrive intensity smoothly from earned 24K Milestone Orbs or Supernova bar
                if (starPowerTime > 0f) {
                    starPowerTime = (starPowerTime - dt).coerceAtLeast(0f)
                    if (starPowerTime <= 0f) {
                        activeGoldenTier = 0
                    }
                }
                val isGoldenActive = currentSecObj.isGoldenOverdrive ||
                    activeStageMode == StageMode.GOLDEN_OVERDRIVE ||
                    starPowerTime > 0f ||
                    (activePhenomenon?.effect == KeyWordEffect.CONSTELLATION_STAR && (activePhenomenon?.timer ?: 0f) > 0f)
                val targetGoldIntensity = if (isGoldenActive) 1.0f else (mb.high - 0.70f).coerceAtLeast(0f) * 2.0f
                goldenOverdriveIntensity += (targetGoldIntensity.coerceIn(0f, 1f) - goldenOverdriveIntensity) * 0.12f

                // 3. BAND HERO / GUITAR HERO SCROLLING RHYTHM HIGHWAY NOTE SPAWNING
                val beatDurationSec = 60f / activeTrack.bpm.coerceAtLeast(60f)
                val spawnLeadTime = (beatDurationSec * 3.8f).coerceIn(1.75f, 2.35f)
                val ttlGraceSec = (beatDurationSec * 0.72f).coerceIn(0.32f, 0.52f)
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
                    shockwaves = shockwaves,
                    hasActiveSustainRibbon = isBandHeroMode && wormholes.isNotEmpty(),
                    isTrainingMode = (gameDifficulty == GameDifficulty.EASY || selectedRemixMode == TrackRemixMode.SLOWED),
                    beatDurationSec = beatDurationSec,
                    isBandHeroMode = isBandHeroMode,
                    multiBand = mb
                )

                // Step Beat Orbs + Expiration Check (Miss resets combo only, never health!)
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
                    magnetActive = magnetTime > 0f && !isBandHeroMode,
                    isBandHeroMode = isBandHeroMode,
                    onBeatMissed = { missedOrb ->
                        if (missedOrb.isMusicVideoOrb && !brollActive) {
                            // Missing ANY of the 5 video relics resets the VHS meter with double-pulse haptic & glitch feedback!
                            cipherMeterProgress = 0f
                            videoWindowFailed = true
                            triggerErrorHaptic()
                            shakeAmount = maxOf(shakeAmount, 8.5f)
                            chromaticAmount = maxOf(chromaticAmount, 3.2f)
                            val missY = missedOrb.y.coerceIn(screenHeight * 0.25f, screenHeight * 0.85f)
                            val missX = missedOrb.x.coerceIn(screenWidth * 0.15f, screenWidth * 0.85f)
                            shockwaves.add(Shockwave(missX, missY, 0f, 280f, Color(0xFFFF2A6DL), 0.85f))
                            shockwaves.add(Shockwave(missX, missY, 0f, 180f, Color(0xFF05D9E8L), 0.65f))
                            activePowerUpToast = PowerUpBannerToast(
                                icon = "📼",
                                title = "VIDEO REEL MISSED!",
                                subtitle = "SIGNAL LOST • VHS METER RESET",
                                accentColor = Color(0xFFFF2A6DL),
                                timer = 2.4f,
                                maxTimer = 2.4f
                            )
                            toastTimeLeft = 2.4f
                        }
                        if (isGoldenActive) {
                            // protected during 24K Overdrive rush!
                        } else if (rhythmCombo > 5) {
                            rhythmCombo = maxOf(1, rhythmCombo - 2)
                            shakeAmount = maxOf(shakeAmount, 2f)
                        } else if (rhythmCombo > 0) {
                            rhythmCombo = maxOf(0, rhythmCombo - 1)
                            shakeAmount = maxOf(shakeAmount, 3.5f)
                        }
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
                    allowAnomalies = currentSecObj.allowAnomalies,
                    powerUps = powerUps,
                    isBandHeroMode = isBandHeroMode
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

                // 5. Differentiated Beat Orb Collection + Unmistakable ON-BEAT STRIKE LINE SPECTACLE!
                val pIter = powerUps.iterator()
                while (pIter.hasNext()) {
                    val p = pIter.next()
                    val distPrimary = hypot(p.x - player.x, p.y - player.y)
                    val secPos = secondTouchPos
                    val distSecondary = if (secPos != null) hypot(p.x - secPos.x, p.y - secPos.y) else 9999f
                    val distTetherMid = if (secPos != null) hypot(p.x - (player.x + secPos.x) * 0.5f, p.y - (player.y + secPos.y) * 0.5f) else 9999f
                    val dist = minOf(distPrimary, minOf(distSecondary, distTetherMid))
                    val catchRadius = p.radius + player.radius + (if (isGoldenActive) 22f else 18f)
                    if (dist < catchRadius) {
                        orbsCollectedThisRun++
                        rhythmCombo++
                        if (rhythmCombo > maxComboThisRun) maxComboThisRun = rhythmCombo

                        val isSupernovaOrb = p.keyWordEffect != KeyWordEffect.NONE
                        val hasSpecialPower = p.powerUpType != null && p.powerUpType != PowerUpType.NONE
                        val isSpecialTransitionOrb = isSupernovaOrb || hasSpecialPower
                        val timeToSwell = p.targetBeatSec - trackSec

                        vocalStreamContrailTimer = if (p.orbitRadius > 50f || mb.vocal >= 0.42f) {
                            4.0f
                        } else {
                            maxOf(vocalStreamContrailTimer, 2.0f)
                        }

                        val leadDur = (p.targetBeatSec - p.spawnBeatSec).coerceAtLeast(0.45f)
                        val uHit = (trackSec - p.spawnBeatSec) / leadDur

                        //Special Transition Orbs: ignite immediately if within 0.45s of swell OR during post-swell linger;
                        // if caught early while approaching (>0.45s ahead), prime to detonate right on targetBeatSec!
                        val isImmediateSwell = timeToSwell <= 0.45f
                        val isSwellLockWindow = timeToSwell > 0.45f
                        val isRegularOnBeat = if (isBandHeroMode) {
                            uHit in 0.84f..1.16f || abs(p.y - screenHeight * 0.65f) <= 54f
                        } else {
                            abs(timeToSwell) <= 0.28f || (liveBeatPhase >= 0f && (liveBeatPhase <= 0.26f || liveBeatPhase >= 0.78f))
                        }
                        val isCaughtOnBeat = if (isSpecialTransitionOrb) true else isRegularOnBeat

                        if (isCaughtOnBeat) {
                            onBeatHitsThisRun++
                        }

                        // Enigmatic Cipher / VHS Spool Orb: 5 video orbs fill the bar (+20% each), 5th triggers Slow-Mo Montage!
                        if (p.isMusicVideoOrb && !isSpecialTransitionOrb && isBrollEnabled) {
                            if (!brollActive && !videoWindowFailed) {
                                cipherMeterProgress = (cipherMeterProgress + 0.20f).coerceAtMost(1.0f)
                                val reelNum = (cipherMeterProgress * 5f + 0.05f).toInt().coerceIn(1, 5)
                                shockwaves.add(Shockwave(p.x, p.y, 0f, 150f, Color(0xFF05D9E8L), 0.60f))
                                shockwaves.add(Shockwave(p.x, p.y, 0f, 105f, Color(0xFFFF2A6DL), 0.48f))
                                if (cipherMeterProgress >= 0.99f) {
                                    cipherMeterProgress = 1.0f
                                    isCipherArmed = false
                                    brollBaseClip = (brollBaseClip + 1) % 4
                                    brollClipIndex = brollBaseClip
                                    // End Montage 1 right as drum beat returns (1:08.5 on Confide) and Montage 2 right at 2:50 ("I")!
                                    val endTargetSec = if (selectedTrackIndex == 1) {
                                        if (trackSec < 100f) 68.5f else 170.0f
                                    } else {
                                        if (trackSec < 85f) 63.0f else 131.5f
                                    }
                                    val montageDuration = (endTargetSec - trackSec).coerceIn(8.0f, 16.0f)
                                    brollDurationTotal = montageDuration
                                    brollTimer = montageDuration
                                    brollActive = true
                                    triggerHaptic(55L)
                                    shockwaves.add(Shockwave(screenWidth * 0.5f, screenHeight * 0.45f, 0f, maxOf(screenWidth, screenHeight) * 0.78f, Color(0xFF05D9E8L), 0.95f))
                                    activePowerUpToast = PowerUpBannerToast(
                                        icon = "🎬",
                                        title = "VHS REEL UNLOCKED (5/5)!",
                                        subtitle = "SLOW-MO VIDEO MONTAGE ACTIVE",
                                        accentColor = Color(0xFF05D9E8L),
                                        timer = 2.5f,
                                        maxTimer = 2.5f
                                    )
                                    toastTimeLeft = 2.5f
                                } else {
                                    activePowerUpToast = PowerUpBannerToast(
                                        icon = "📼",
                                        title = "VIDEO REEL $reelNum / 5",
                                        subtitle = "COLLECT ALL 5 TO UNLOCK MONTAGE",
                                        accentColor = Color(0xFF05D9E8L),
                                        timer = 1.5f,
                                        maxTimer = 1.5f
                                    )
                                    toastTimeLeft = 1.5f
                                }
                            }
                        }

                        val noteGain = ((when {
                            isSpecialTransitionOrb && isImmediateSwell -> 15
                            isSpecialTransitionOrb -> 10
                            isCaughtOnBeat -> 5
                            else -> 3
                        }) * selectedRemixMode.noteMultiplier).toInt().coerceAtLeast(1)
                        pulsePoints += noteGain

                        audioEngine.playOrbBeatHit(
                            combo = rhythmCombo,
                            isKeyWordPeg = isCaughtOnBeat
                        )

                        if (isSpecialTransitionOrb) {
                            val mult = if (isGoldenActive) maxOf(scoreMultiplier, 4) else scoreMultiplier
                            if (isImmediateSwell) {
                                // PERFECT / HOVER SWELL TIMING: Unleash full stage transformation (24K Tier 1 / Tier 2 / Prism / Eclipse)!
                                triggerHaptic(54L)
                                val swellBonus = (750 * difficultyScoreMultiplier).toInt()
                                score += swellBonus
                                cameraPunch = maxOf(cameraPunch, 0.048f)
                                beatFlashIntensity = maxOf(beatFlashIntensity, 0.48f)
                                if (p.goldenTier > 0 || p.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR) {
                                    activeGoldenTier = maxOf(activeGoldenTier, if (p.goldenTier > 0) p.goldenTier else 1)
                                    starPowerTime = maxOf(starPowerTime, 15.5f)
                                    goldenOverdriveIntensity = 1.0f
                                } else {
                                    goldenOverdriveIntensity = maxOf(goldenOverdriveIntensity, 0.45f)
                                }
                                scoreMultiplier = maxOf(scoreMultiplier, 4)
                                multiplierTime = maxOf(multiplierTime, 12.0f)
                                shockwaves.add(Shockwave(p.x, p.y, 0f, 540f, Color(0xFFFACC15L), 1.15f))
                                shockwaves.add(Shockwave(p.x, p.y, 0f, 360f, Color(0xFF38BDF8L), 0.92f))

                                if (hasSpecialPower) {
                                    val pickupRes = handleRegularOrbPowerUp(
                                        p = p,
                                        mult = mult,
                                        difficultyScoreMultiplier = difficultyScoreMultiplier,
                                        isCaughtOnBeat = true,
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
                                    starPowerTime = maxOf(starPowerTime, pickupRes.newStarPowerTime)
                                    shieldTime = pickupRes.newShieldTime
                                    slowMoTime = pickupRes.newSlowMoTime
                                    magnetTime = pickupRes.newMagnetTime
                                    shakeAmount = pickupRes.newShakeAmount
                                    cameraPunch = pickupRes.newCameraPunch
                                    beatFlashIntensity = pickupRes.newBeatFlashIntensity
                                    score += pickupRes.bonusScore
                                    when (p.powerUpType) {
                                        PowerUpType.ELECTRIC_HARMONIC -> audioEngine.playElectricSurgeStinger()
                                        PowerUpType.PRISM_HARMONY -> audioEngine.playPrismBloomStinger()
                                        PowerUpType.SLOW_MO -> audioEngine.playSlowMoStinger()
                                        PowerUpType.BLAST -> audioEngine.playSolarNovaStinger()
                                        PowerUpType.SHIELD -> audioEngine.playAegisStinger()
                                        else -> {}
                                    }
                                }
                                if (isSupernovaOrb) {
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
                                    starPowerTime = maxOf(starPowerTime, supRes.newStarPowerTime)
                                    scoreMultiplier = supRes.newScoreMultiplier
                                    multiplierTime = maxOf(multiplierTime, supRes.newMultiplierTime)
                                    shieldTime = supRes.newShieldTime
                                    activePowerUpToast = if (p.goldenTier >= 2) {
                                        PowerUpBannerToast(
                                            icon = "👑",
                                            title = "24K HYPER-SPECTACLE!",
                                            subtitle = "SOLAR MANDALA + TWIN-RING PULSE",
                                            accentColor = Color(0xFFFACC15L),
                                            timer = 2.8f,
                                            maxTimer = 2.8f
                                        )
                                    } else {
                                        supRes.toast
                                    }
                                }
                            } else if (isSwellLockWindow) {
                                // PRIMED FOR EXACT SWELL: Caught while approaching — locks in and ignites right on targetBeatSec!
                                triggerHaptic(34L)
                                score += (450 * difficultyScoreMultiplier).toInt()
                                shockwaves.add(Shockwave(p.x, p.y, 0f, 200f, Color(0xFFFACC15L), 0.70f))
                                primedSectionEffect = PrimedSectionEffect(
                                    type = p.powerUpType ?: PowerUpType.NONE,
                                    targetSec = p.targetBeatSec,
                                    title = "SWELL LOCKED",
                                    keyWordEffect = p.keyWordEffect,
                                    goldenTier = if (p.goldenTier > 0) p.goldenTier else if (p.keyWordEffect == KeyWordEffect.CONSTELLATION_STAR) 1 else 0
                                )
                            }
                        } else {
                            // Regular & Cipher Orbs
                            triggerHaptic(if (isCaughtOnBeat) 34L else 15L)
                            if (isCaughtOnBeat) {
                                beatFlashIntensity = maxOf(beatFlashIntensity, 0.22f)
                                cameraPunch = maxOf(cameraPunch, 0.015f)
                                scoreMultiplier = maxOf(scoreMultiplier, if (rhythmCombo >= 12) 4 else 2)
                                multiplierTime = maxOf(multiplierTime, 7.5f)
                                shockwaves.add(Shockwave(p.x, p.y, 0f, 125f, Color(0xFFFACC15L), 0.55f))
                            } else {
                                shockwaves.add(Shockwave(p.x, p.y, 0f, 45f, Color(0x6694A3B8L), 0.25f))
                            }
                            if (!isGoldenActive) {
                                overdriveProgress = (overdriveProgress + (if (isCaughtOnBeat) 0.055f else 0.04f)).coerceAtMost(1f)
                                // SPECIAL 24K METER 100% PAYOFF: Filling the 24K bar unleashes a Golden Supernova Cascade!
                                if (overdriveProgress >= 0.999f) {
                                    overdriveProgress = 0f
                                    activeGoldenTier = 2
                                    starPowerTime = maxOf(starPowerTime, 10.0f)
                                    magnetTime = maxOf(magnetTime, 5.0f)
                                    goldenOverdriveIntensity = 1.0f
                                    scoreMultiplier = 4
                                    multiplierTime = maxOf(multiplierTime, 10.0f)
                                    score += (1000 * difficultyScoreMultiplier).toInt()
                                    shakeAmount = maxOf(shakeAmount, 16f)
                                    cameraPunch = maxOf(cameraPunch, 0.055f)
                                    beatFlashIntensity = 0.95f
                                    triggerHaptic(65L)
                                    audioEngine.playElectricSurgeStinger()
                                    powerUps.forEach { activeOrb ->
                                        if (!activeOrb.isMusicVideoOrb) {
                                            activeOrb.isGoldenOrb = true
                                            activeOrb.color = Color(0xFFFACC15L)
                                        }
                                    }
                                    shockwaves.add(Shockwave(player.x, player.y, 0f, 680f, Color(0xFFFACC15L), 1.25f))
                                    shockwaves.add(Shockwave(player.x, player.y, 0f, 460f, Color(0xFF38BDF8L), 1.05f))
                                    repeat(22) { sp ->
                                        val ang = (sp / 22f) * 6.283185f
                                        particles.add(
                                            GameObject(
                                                x = player.x,
                                                y = player.y,
                                                radius = 4.2f,
                                                color = if (sp % 2 == 0) Color(0xFFFDE047L) else Color(0xFF38BDF8L),
                                                vx = cos(ang) * 9.5f,
                                                vy = sin(ang) * 9.5f,
                                                life = 1.05f
                                            )
                                        )
                                    }
                                    activePowerUpToast = PowerUpBannerToast(
                                        icon = "👑",
                                        title = "24K BAR FULL • SUPERNOVA!",
                                        subtitle = "MAGNET + GOLD ORB CONVERSION + 4X RUSH",
                                        accentColor = Color(0xFFFACC15L),
                                        timer = 3.0f,
                                        maxTimer = 3.0f
                                    )
                                    toastTimeLeft = 3.0f
                                }
                            }
                            val mult = if (isGoldenActive) maxOf(scoreMultiplier, 4) else scoreMultiplier
                            val gainedPts = ((if (isCaughtOnBeat) 100 else 45) * mult * (1f + (rhythmCombo.coerceAtMost(12) * 0.10f)) * difficultyScoreMultiplier).toInt()
                            score += gainedPts
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

                // 6b. Band-Hero Sustain Slide Ribbon Tracking
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
                    floatingTexts = floatingTexts,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    particles = particles,
                    secondFingerPos = secondTouchPos
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
                if (surfRes.toast != null || wormholes.any { it.slideCooldownSec >= 0.5f }) {
                    starPowerTime = maxOf(starPowerTime, 6.5f)
                    goldenOverdriveIntensity = maxOf(goldenOverdriveIntensity, 1.0f)
                    vocalStreamContrailTimer = maxOf(vocalStreamContrailTimer, 6.5f)
                    scoreMultiplier = 4
                    multiplierTime = maxOf(multiplierTime, 6.5f)
                }

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
                    screenHeight = screenHeight,
                    isContrailBoosted = (vocalStreamContrailTimer > 0f || mb.vocal >= 0.44f || (mb.bassDropOut && mb.vocal >= 0.32f))
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
                if (brollCooldown > 0f) brollCooldown -= dt

                // Flowy whispy breakdown slow-mo during "losing your battles" (0:45 - 0:49.5) & spoken vocal drift
                val isFlowyBreakdownTime = (selectedTrackIndex == 1 && (trackSec in 44.4f..49.5f || trackSec in 108.0f..115.2f)) ||
                    (selectedTrackIndex == 2 && (trackSec in 41.5f..47.0f || trackSec in 91.5f..97.0f))
                if (isFlowyBreakdownTime) {
                    slowMoTime = maxOf(slowMoTime, 0.45f)
                }

                // If montage timestamp arrived but player did not gather all 5 relics, reset the meter with haptic & glitch
                val isMontageCutoff = (selectedTrackIndex == 1 && ((trackSec in 60.5f..61.8f) || (trackSec in 156.5f..157.8f))) ||
                    (selectedTrackIndex == 2 && ((trackSec in 54.5f..55.8f) || (trackSec in 118.5f..119.8f)))
                if (isMontageCutoff && cipherMeterProgress > 0f && cipherMeterProgress < 0.99f && !brollActive) {
                    cipherMeterProgress = 0f
                    videoWindowFailed = true
                    triggerErrorHaptic()
                    chromaticAmount = maxOf(chromaticAmount, 3.0f)
                    shakeAmount = maxOf(shakeAmount, 7.5f)
                    shockwaves.add(Shockwave(screenWidth * 0.5f, screenHeight * 0.45f, 0f, 260f, Color(0xFFFF2A6DL), 0.80f))
                    activePowerUpToast = PowerUpBannerToast(
                        icon = "📼",
                        title = "REEL INCOMPLETE",
                        subtitle = "WINDOW MISSED • VHS METER RESET",
                        accentColor = Color(0xFFFF2A6DL),
                        timer = 2.2f,
                        maxTimer = 2.2f
                    )
                    toastTimeLeft = 2.2f
                }

                if (brollActive) {
                    brollTimer = (brollTimer - dt).coerceAtLeast(0f)
                    // Smooth ambient slow-mo during video montages
                    slowMoTime = maxOf(slowMoTime, 0.38f)

                    // Smooth multi-clip crossfade envelope (2 clips for Montage 1, 3 clips during "oooohs" in Montage 2)
                    val totalDur = brollDurationTotal.coerceAtLeast(4.0f)
                    val numClips = if (totalDur >= 11.5f) 3 else 2
                    val elapsed = (totalDur - brollTimer).coerceIn(0f, totalDur)
                    val segLen = (totalDur / numClips.toFloat()).coerceAtLeast(2.5f)
                    val segIdx = (elapsed / segLen).toInt().coerceIn(0, numClips - 1)
                    val targetClip = (brollBaseClip + segIdx) % 4
                    if (brollClipIndex != targetClip) {
                        brollClipIndex = targetClip
                    }
                    val segLocal = (elapsed - segIdx * segLen).coerceIn(0f, segLen)
                    val fadeSec = minOf(0.82f, segLen * 0.25f)
                    val rawIn = (segLocal / fadeSec).coerceIn(0f, 1f)
                    val rawOut = ((segLen - segLocal) / fadeSec).coerceIn(0f, 1f)
                    val smoothIn = rawIn * rawIn * (3f - 2f * rawIn)
                    val smoothOut = rawOut * rawOut * (3f - 2f * rawOut)
                    brollClipFadeAlpha = minOf(smoothIn, smoothOut) * 0.88f

                    if (brollTimer <= 0f) {
                        brollActive = false
                        brollClipFadeAlpha = 0f
                        cipherMeterProgress = 0f
                    }
                } else {
                    brollClipFadeAlpha = 0f
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        screenWidth = constraints.maxWidth.toFloat()
        screenHeight = constraints.maxHeight.toFloat()
        if (player.x == 0f) {
            player.x = screenWidth / 2f
            player.y = screenHeight * 0.65f
            targetX = player.x
            targetY = player.y
        }

        // Smooth session envelope hides center album art throughout the entire montage (without flickering on clip switches)
        val brollAlbumHideAlpha by animateFloatAsState(
            targetValue = if ((gameState == GameState.PLAYING || gameState == GameState.PAUSED) && isBrollEnabled && brollActive) 0.92f else 0f,
            animationSpec = tween(durationMillis = 750),
            label = "brollAlbumHideAlpha"
        )
        val effectiveBrollClipAlpha = if ((gameState == GameState.PLAYING || gameState == GameState.PAUSED) && isBrollEnabled && brollActive) {
            brollClipFadeAlpha
        } else 0f
        val isFlowyStageVisual = (selectedTrackIndex == 1 && (liveTrackSec in 44.4f..49.5f || liveTrackSec in 108.0f..115.2f)) ||
            (selectedTrackIndex == 2 && (liveTrackSec in 41.5f..47.0f || liveTrackSec in 91.5f..97.0f))

        // Translucent Slow-Mo Crossfade Music Video B-Roll Overlay
        if ((gameState == GameState.PLAYING || gameState == GameState.PAUSED) && isBrollEnabled) {
            BrollVideoOverlay(
                isActive = brollActive,
                clipIndex = brollClipIndex,
                visualAlpha = effectiveBrollClipAlpha
            )
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
            isBandHeroMode = isBandHeroMode,
            brollOverlayAlpha = brollAlbumHideAlpha,
            goldenTier = activeGoldenTier,
            isFlowySwirlStage = isFlowyStageVisual,
            player = player,
            secondTouchPos = secondTouchPos,
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
            },
            onSecondFingerChange = { secOffset ->
                secondTouchPos = secOffset
            }
        )

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
                rhythmCombo = rhythmCombo,
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
                overdriveProgress = overdriveProgress,
                is24KOverdrive = starPowerTime > 0f || activeStageMode == StageMode.GOLDEN_OVERDRIVE,
                cipherMeterProgress = cipherMeterProgress,
                isCipherArmed = isCipherArmed,
                isBrollPlaying = brollActive,
                brollPlayProgress = if (brollDurationTotal > 0f) (brollTimer / brollDurationTotal).coerceIn(0f, 1f) else 0f,
                comboBanner = null,
                onActivate24K = {
                    activeGoldenTier = 2
                    starPowerTime = 10.0f
                    magnetTime = maxOf(magnetTime, 5.0f)
                    overdriveProgress = 0f
                    shakeAmount = maxOf(shakeAmount, 18f)
                    powerUps.forEach {
                        if (!it.isMusicVideoOrb) {
                            it.isGoldenOrb = true
                            it.color = Color(0xFFFACC15L)
                        }
                    }
                    audioEngine.playElectricSurgeStinger()
                },
                onPause = { if (gameState == GameState.PLAYING) gameState = GameState.PAUSED }
            )
        }



        if (gameState != GameState.MUSIC_DEMO && gameState != GameState.PLAYING && gameState != GameState.COUNTDOWN && gameState != GameState.PAUSED) {
            Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()) {
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = AdManager.bannerAdUnitId
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    onRelease = { adView ->
                        adView.destroy()
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
                isBandHeroMode = isBandHeroMode,
                onToggleBandHeroMode = { enabled ->
                    audioEngine.playMenuNote()
                    isBandHeroMode = enabled
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
                    val isTrackUnlocked = selectedTrackIndex == 1 || unlockedTrackCount >= selectedTrackIndex || isVipUnlocked || prefs.getBoolean("art_unlocked_$selectedTrackIndex", false) || prefs.getBoolean("track_unlocked_$selectedTrackIndex", false)
                    if (isTrackUnlocked) {
                        audioEngine.playMenuNote()
                        startFreshLevelRun(selectedTrackIndex)
                    } else {
                        val cost = MusicLibrary.unlockCostForTrack(selectedTrackIndex)
                        if (pulsePoints >= cost) {
                            pulsePoints -= cost
                            prefs.edit().putInt("pulse_points", pulsePoints).putBoolean("track_unlocked_$selectedTrackIndex", true).apply()
                            unlockedTrackCount = maxOf(unlockedTrackCount, selectedTrackIndex)
                            audioEngine.playPowerUp()
                            startFreshLevelRun(selectedTrackIndex)
                        } else {
                            audioEngine.playMenuNote()
                            initialDemoTab = 1
                            gameState = GameState.MUSIC_DEMO
                        }
                    }
                },
                onMusicDemo = { tabIdx ->
                    audioEngine.playMenuNote()
                    initialDemoTab = tabIdx
                    gameState = GameState.MUSIC_DEMO
                },
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
                isBrollVideo = isBrollEnabled,
                onBrollVideoToggle = {
                    isBrollEnabled = it
                    prefs.edit().putBoolean("enable_broll_video", it).apply()
                },
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
                orbsCollected = orbsCollectedThisRun,
                totalTrackOrbs = totalTrackOrbsThisRun,
                trackLabel = "TRACK %02d • ${MusicLibrary.getTrack(selectedTrackIndex).title}".format(selectedTrackIndex),
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
                onBackToAlbum = { audioEngine.playMenuNote(); initialDemoTab = 1; gameState = GameState.MUSIC_DEMO },
                onBuyPoints = { audioEngine.playMenuNote(); billingManager.purchasePoints(context as Activity) }
            )

            GameState.LEVEL_COMPLETE -> LevelCompleteUI(
                trackIndex = selectedTrackIndex,
                score = score,
                maxCombo = maxComboThisRun,
                notesEarned = (75 * selectedRemixMode.noteMultiplier).toInt() + onBeatHitsThisRun * 5,
                orbsCollected = orbsCollectedThisRun,
                totalTrackOrbs = totalTrackOrbsThisRun,
                collectionBonusNotes = lastCompletionBonusNotes,
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
                        initialDemoTab = 0
                        gameState = GameState.MUSIC_DEMO
                    }
                },
                onBackToAlbum = {
                    audioEngine.playMenuNote()
                    adManager.showInterstitialIfReady(context as Activity) {
                        gameState = GameState.MENU
                    }
                },
                onPlayBandHeroStyle = {
                    audioEngine.playMenuNote()
                    isBandHeroMode = true
                    adManager.showInterstitialIfReady(context as Activity) {
                        startFreshLevelRun(selectedTrackIndex)
                    }
                }
            )

            GameState.MUSIC_DEMO -> MusicDemoScreen(
                initialTab = initialDemoTab,
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
                btnIconClose = btnIconClose,
                onOpenSettings = {
                    audioEngine.playMenuNote()
                    settingsReturnState = GameState.MUSIC_DEMO
                    gameState = GameState.SETTINGS
                }
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


@Composable
fun BrollVideoOverlay(
    isActive: Boolean,
    clipIndex: Int,
    visualAlpha: Float,
    modifier: Modifier = Modifier
) {
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    val loadedClipHolder = remember { intArrayOf(-1) }
    val surfaceHolder = remember { arrayOfNulls<Surface>(1) }

    fun applyAspectFitTransform(tv: TextureView, videoW: Int, videoH: Int) {
        val viewW = tv.width.toFloat()
        val viewH = tv.height.toFloat()
        if (viewW <= 0f || viewH <= 0f || videoW <= 0 || videoH <= 0) return
        // True Aspect-Fit (fitCenter): Scales video uniformly to fit inside screen bounds with zero stretching
        val fitScale = minOf(viewW / videoW.toFloat(), viewH / videoH.toFloat())
        val targetW = videoW * fitScale
        val targetH = videoH * fitScale
        val matrix = android.graphics.Matrix()
        matrix.setScale(targetW / viewW, targetH / viewH, viewW * 0.5f, viewH * 0.5f)
        tv.setTransform(matrix)
    }

    fun loadClipIntoPlayer(ctx: android.content.Context, tv: TextureView, targetClipIdx: Int, playNow: Boolean) {
        val surf = surfaceHolder[0] ?: return
        val clipNum = ((targetClipIdx % 4) + 4) % 4 + 1
        try {
            val existing = mediaPlayerRef
            val player = if (existing != null) {
                existing.reset()
                existing
            } else {
                MediaPlayer().also { mediaPlayerRef = it }
            }
            val afd = ctx.assets.openFd("broll/broll_$clipNum.mp4")
            player.setSurface(surf)
            player.isLooping = true
            player.setVolume(0f, 0f)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            loadedClipHolder[0] = targetClipIdx
            player.setOnVideoSizeChangedListener { _, vw, vh ->
                applyAspectFitTransform(tv, vw, vh)
            }
            player.setOnPreparedListener { mp ->
                applyAspectFitTransform(tv, mp.videoWidth, mp.videoHeight)
                if (playNow) {
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mp.playbackParams = android.media.PlaybackParams().setSpeed(0.55f)
                        }
                        mp.start()
                    }.onFailure {
                        runCatching { mp.start() }
                    }
                }
            }
            player.prepareAsync()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = visualAlpha.coerceIn(0f, 1f) }
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                            surfaceHolder[0] = Surface(st)
                            loadClipIntoPlayer(ctx, this@apply, clipIndex, isActive)
                        }

                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {
                            mediaPlayerRef?.let { mp ->
                                runCatching { applyAspectFitTransform(this@apply, mp.videoWidth, mp.videoHeight) }
                            }
                        }

                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            runCatching {
                                mediaPlayerRef?.stop()
                                mediaPlayerRef?.release()
                            }
                            mediaPlayerRef = null
                            surfaceHolder[0]?.release()
                            surfaceHolder[0] = null
                            loadedClipHolder[0] = -1
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
            },
            update = { tv ->
                if (surfaceHolder[0] != null && loadedClipHolder[0] != clipIndex) {
                    loadClipIntoPlayer(tv.context, tv, clipIndex, isActive)
                } else {
                    val mp = mediaPlayerRef
                    if (mp != null) {
                        if (isActive) {
                            runCatching {
                                if (!mp.isPlaying) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        runCatching { mp.playbackParams = android.media.PlaybackParams().setSpeed(0.55f) }
                                    }
                                    mp.start()
                                }
                            }
                        } else if (visualAlpha <= 0.02f) {
                            runCatching { if (mp.isPlaying) mp.pause() }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
