package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.random.Random

@Suppress("UNUSED_PARAMETER")
@Composable
fun GamePlayfieldCanvas(
    gameState: GameState,
    screenWidth: Float,
    screenHeight: Float,
    shakeAmount: Float,
    chromaticAmount: Float,
    focusWarp: Float,
    gameTime: Float,
    lastLevelUpTime: Float,
    isFocusActive: Boolean,
    shieldTime: Float,
    spawnInvulnerabilityTime: Float,
    customBgTheme: String,
    customHeroSkin: String,
    trackRevealMusicProgress: Float,
    juiceIntensity: Float = 0f,
    stageMode: StageMode = StageMode.ABYSSAL_WAVE,
    beatPhase: Float = 0f,
    trackSec: Float = 0f,
    cameraPunch: Float = 0f,
    goldenOverdriveIntensity: Float = 0f,
    beatFlashIntensity: Float = 0f,
    audioEnergy: Float = 0.45f,
    swellDelta: Float = 0f,
    multiBand: MultiBandAudioState = MultiBandAudioState(rms = audioEnergy, swellDelta = swellDelta),
    activePhenomenon: ActivePhenomenon? = null,
    tetheredSatellites: List<TetheredSatellite> = emptyList(),
    enemyTelegraphs: List<EnemySpawnTelegraph> = emptyList(),
    wormholes: List<SingularityWormhole> = emptyList(),
    player: GameObject,
    secondTouchPos: Offset? = null,
    motes: List<GameObject>,
    playerTrail: List<Offset>,
    shockwaves: List<Shockwave>,
    particles: List<GameObject>,
    powerUps: List<GameObject>,
    enemies: List<GameObject>,
    floatingTexts: List<FloatingText>,
    nebulaBitmap: ImageBitmap,
    starsBack: ImageBitmap,
    starsFront: ImageBitmap,
    activeTrackArtwork: ImageBitmap,
    milestoneIcon: ImageBitmap,
    heroSolarMandala: ImageBitmap,
    heroCyberMandala: ImageBitmap,
    heroPrismMandala: ImageBitmap,
    enemyPulsarMandala: ImageBitmap,
    enemySeekerMandala: ImageBitmap,
    enemyWandererMandala: ImageBitmap,
    applyColorBlind: (Color) -> Color,
    onDragPlayer: (dx: Float, dy: Float) -> Unit,
    onSecondFingerChange: (Offset?) -> Unit = {}
) {
    val isGoldenOverdrive = stageMode == StageMode.GOLDEN_OVERDRIVE ||
        goldenOverdriveIntensity > 0.25f ||
        (activePhenomenon?.effect == KeyWordEffect.CONSTELLATION_STAR && activePhenomenon.timer > 0f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(gameState) {
                if (gameState == GameState.PLAYING || gameState == GameState.COUNTDOWN) {
                    awaitEachGesture {
                        do {
                            val event = awaitPointerEvent()
                            val activeChanges = event.changes.filter { it.pressed }
                            if (activeChanges.isNotEmpty()) {
                                val primary = activeChanges.first()
                                if (primary.positionChanged()) {
                                    val delta = primary.position - primary.previousPosition
                                    onDragPlayer(delta.x, delta.y)
                                }
                                primary.consume()

                                if (activeChanges.size >= 2) {
                                    val secondary = activeChanges[1]
                                    onSecondFingerChange(secondary.position)
                                    secondary.consume()
                                } else {
                                    onSecondFingerChange(null)
                                }
                            } else {
                                onSecondFingerChange(null)
                            }
                        } while (event.changes.any { it.pressed })
                        onSecondFingerChange(null)
                    }
                }
            }
    ) {
        val sx = if (shakeAmount > 0.5f) (Random.nextFloat() - 0.5f) * shakeAmount else 0f
        val sy = if (shakeAmount > 0.5f) (Random.nextFloat() - 0.5f) * shakeAmount else 0f

        withTransform({
            translate(sx, sy)
            val scale = 1f - (focusWarp * 0.04f) + (juiceIntensity * 0.012f) + cameraPunch
            scale(scale, scale, Offset(screenWidth / 2, screenHeight / 2))
        }) {
            drawNebulaBackground(
                nebula = nebulaBitmap,
                starsBack = starsBack,
                starsFront = starsFront,
                time = gameTime,
                isFocus = isFocusActive,
                bgTheme = customBgTheme,
                trackArtwork = if (gameState == GameState.PLAYING || gameState == GameState.PAUSED || gameState == GameState.COUNTDOWN) activeTrackArtwork else null,
                trackProgress = trackRevealMusicProgress,
                juiceIntensity = juiceIntensity,
                stageMode = stageMode,
                beatPhase = beatPhase,
                goldenOverdriveIntensity = goldenOverdriveIntensity,
                beatFlashIntensity = beatFlashIntensity,
                audioEnergy = audioEnergy,
                swellDelta = swellDelta,
                activePhenomenon = activePhenomenon,
                multiBand = multiBand
            )
            drawMotes(motes, isGoldenOverdrive = isGoldenOverdrive)
            drawPlayerTrail(playerTrail, player.color, isFocusActive, applyColorBlind, isGoldenOverdrive = isGoldenOverdrive)
            drawShockwaves(shockwaves, applyColorBlind)
            if (gameState != GameState.MENU) {
                drawWormholes(
                    wormholes = wormholes,
                    time = gameTime,
                    beatPhase = beatPhase
                )
                drawOrbitalPhenomenonAndSatellites(
                    player = player,
                    satellites = tetheredSatellites,
                    powerUps = powerUps,
                    activePhenomenon = activePhenomenon,
                    time = gameTime
                )
            }
            drawParticles(particles)
            if (gameState != GameState.MENU) {
                drawPowerUps(
                    powerUps = powerUps,
                    player = player,
                    time = gameTime,
                    trackSec = trackSec,
                    beatPhase = beatPhase,
                    goldenOverdriveIntensity = goldenOverdriveIntensity,
                    multiBand = multiBand
                )
            }
            // High-contrast Perimeter Enemy Spawn Telegraphs (2 beats before enemy enters!)
            drawEnemyTelegraphs(
                telegraphs = enemyTelegraphs,
                time = gameTime,
                beatPhase = beatPhase
            )
            enemies.forEach { e ->
                val eAlpha = if (e.type == EnemyType.PHANTOM) e.life else 1f
                drawEnemySprite(
                    e = e,
                    alpha = eAlpha,
                    chromatic = chromaticAmount,
                    pulsarMandala = enemyPulsarMandala,
                    seekerMandala = enemySeekerMandala,
                    wandererMandala = enemyWandererMandala,
                    time = gameTime,
                    beatPhase = beatPhase,
                    isGoldenOverdrive = isGoldenOverdrive,
                    bassEnergy = multiBand.bass
                )
            }
            if (gameState != GameState.MENU) {
                if (secondTouchPos != null) {
                    drawDualHeroPlasmaTether(
                        p1 = Offset(player.x, player.y),
                        p2 = secondTouchPos,
                        time = gameTime,
                        beatPhase = beatPhase
                    )
                    val twinHero = player.copy(x = secondTouchPos.x, y = secondTouchPos.y, color = Color(0xFF38BDF8L))
                    drawPlayerSprite(
                        p = twinHero,
                        isFocus = isFocusActive,
                        time = gameTime + 1.57f,
                        shield = shieldTime,
                        applyCB = applyColorBlind,
                        isDead = gameState == GameState.GAME_OVER,
                        heroSolarMandala = heroSolarMandala,
                        heroCyberMandala = heroCyberMandala,
                        heroPrismMandala = heroPrismMandala,
                        skin = customHeroSkin,
                        invulnTime = spawnInvulnerabilityTime,
                        beatPhase = beatPhase,
                        isGoldenOverdrive = isGoldenOverdrive
                    )
                }
                drawPlayerSprite(
                    p = player,
                    isFocus = isFocusActive,
                    time = gameTime,
                    shield = shieldTime,
                    applyCB = applyColorBlind,
                    isDead = gameState == GameState.GAME_OVER,
                    heroSolarMandala = heroSolarMandala,
                    heroCyberMandala = heroCyberMandala,
                    heroPrismMandala = heroPrismMandala,
                    skin = customHeroSkin,
                    invulnTime = spawnInvulnerabilityTime,
                    beatPhase = beatPhase,
                    isGoldenOverdrive = isGoldenOverdrive
                )
            }
            drawFloatingTexts(floatingTexts)
        }
    }
}
