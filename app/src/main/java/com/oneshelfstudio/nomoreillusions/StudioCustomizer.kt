package com.oneshelfstudio.nomoreillusions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun StudioCustomizerSuite(
    customBgTheme: String,
    customHeroSkin: String,
    customEnemyStyle: String,
    pulsePoints: Int = 0,
    unlockedItemsCsv: String = "",
    isVipUnlocked: Boolean = false,
    onUnlockShopItem: (itemId: String, cost: Int) -> Unit = { _, _ -> },
    onWatchAdForNotes: () -> Unit = {},
    onActivateVipPasscode: () -> Unit = {},
    onUpdateCustomization: (bg: String, hero: String, enemy: String) -> Unit,
    onLaunchTestGame: () -> Unit,
    nebulaBitmap: ImageBitmap,
    heroSolarMandala: ImageBitmap,
    heroCyberMandala: ImageBitmap,
    heroPrismMandala: ImageBitmap,
    enemyPulsarMandala: ImageBitmap,
    enemySeekerMandala: ImageBitmap,
    enemyWandererMandala: ImageBitmap,
    btnPillGreen: ImageBitmap,
    modifier: Modifier = Modifier
) {
    val haptics = rememberStudioHaptics()
    var bgTheme by remember(customBgTheme) { mutableStateOf(customBgTheme) }
    var heroSkin by remember(customHeroSkin) { mutableStateOf(customHeroSkin) }
    var enemyStyle by remember(customEnemyStyle) { mutableStateOf(customEnemyStyle) }

    val unlockedSet = remember(unlockedItemsCsv, isVipUnlocked) {
        val base = unlockedItemsCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        // Default starter loadout is always unlocked
        base.add("BG_OBSIDIAN")
        base.add("HERO_SOLAR")
        base.add("ENEMY_BALANCED")
        if (isVipUnlocked) {
            base.addAll(
                listOf(
                    "BG_CYBER", "BG_SUNSET", "BG_PRISM", "BG_GOLD_VINYL", "BG_BLOOD_MOON",
                    "HERO_CYBER", "HERO_PRISM", "HERO_VINYL", "HERO_ECLIPSE", "HERO_SERAPH",
                    "ENEMY_PULSARS", "ENEMY_SEEKERS", "ENEMY_WANDERERS",
                    "VAULT_DEMO", "VAULT_LYRICS", "VAULT_WALLPAPER", "VAULT_POLAROIDS"
                )
            )
        }
        base
    }

    var showPasscodeModal by remember { mutableStateOf(false) }
    var activeVaultModal by remember { mutableStateOf<String?>(null) }
    var passcodeText by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    fun applyOrUnlock(
        itemId: String,
        cost: Int,
        newBg: String = bgTheme,
        newHero: String = heroSkin,
        newEnemy: String = enemyStyle
    ) {
        if (itemId in unlockedSet || isVipUnlocked) {
            bgTheme = newBg
            heroSkin = newHero
            enemyStyle = newEnemy
            onUpdateCustomization(newBg, newHero, newEnemy)
            statusBanner = null
        } else if (pulsePoints >= cost) {
            onUnlockShopItem(itemId, cost)
            bgTheme = newBg
            heroSkin = newHero
            enemyStyle = newEnemy
            onUpdateCustomization(newBg, newHero, newEnemy)
            statusBanner = "UNLOCKED FOR $cost ♪ NOTES!"
        } else {
            statusBanner = "NEED $cost ♪ NOTES — PLAY OR WATCH AN AD (+150 ♪) TO UNLOCK!"
        }
    }

    var simHeroX by remember { mutableFloatStateOf(160f) }
    var simHeroY by remember { mutableFloatStateOf(95f) }
    val simEnemies = remember { mutableStateListOf<GameObject>() }

    LaunchedEffect(enemyStyle) {
        simEnemies.clear()
        val eType = when (enemyStyle) {
            "PULSARS" -> EnemyType.PULSE
            "SEEKERS" -> EnemyType.SEEKER
            "WANDERERS" -> EnemyType.WANDERER
            else -> EnemyType.PULSE
        }
        for (i in 0 until 4) {
            val ang = (i * 1.55f)
            simEnemies.add(
                GameObject(
                    x = 65f + i * 60f,
                    y = 40f + (i % 2) * 35f,
                    radius = 15f,
                    color = when (i % 3) {
                        0 -> Color(0xFFF43F5EL)
                        1 -> Color(0xFFFF1E42L)
                        else -> Color(0xFFFB7185L)
                    },
                    type = if (enemyStyle == "BALANCED") {
                        when (i % 3) { 0 -> EnemyType.PULSE; 1 -> EnemyType.SEEKER; else -> EnemyType.WANDERER }
                    } else eType,
                    angle = ang,
                    phase = i.toFloat(),
                    orbitRadius = 12f,
                    speed = 2.0f
                )
            )
        }
    }

    var animTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                animTime += 0.016f
                simEnemies.forEach { e ->
                    when (e.type) {
                        EnemyType.SEEKER -> {
                            val targetAngle = atan2(simHeroY - e.y, simHeroX - e.x)
                            val diff = normalizeAngle(targetAngle - e.angle)
                            e.angle += diff.coerceIn(-0.03f, 0.03f)
                        }
                        EnemyType.PULSE -> {
                            e.angle += sin(animTime * 3f + e.phase) * 0.03f
                        }
                        EnemyType.WANDERER -> {
                            e.angle += 0.025f + 0.01f * sin(animTime * 2f + e.phase)
                        }
                        else -> {}
                    }
                    e.x += cos(e.angle) * 1.5f
                    e.y += sin(e.angle) * 1.5f
                    if (e.x < 15f) { e.x = 305f } else if (e.x > 305f) { e.x = 15f }
                    if (e.y < 15f) { e.y = 175f } else if (e.y > 175f) { e.y = 15f }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Fan Shop Bar: Watch Ad (+150 ♪ Notes) + Artist VIP Testing Passcode Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.25f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF15803DL), Color(0xFF059669L))
                            )
                        )
                        .border(1.2.dp, Color(0xFF4ADE80L), RoundedCornerShape(10.dp))
                        .clickable {
                            haptics.confirm()
                            onWatchAdForNotes()
                            statusBanner = "+150 ♪ MUSIC NOTES EARNED!"
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ WATCH AD (+150 ♪ NOTES)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(0.95f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isVipUnlocked) {
                                Brush.horizontalGradient(listOf(Color(0xFFB45309L), Color(0xFFD97706L)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF1E1B4BL), Color(0xFF312E81L)))
                            }
                        )
                        .border(
                            1.2.dp,
                            if (isVipUnlocked) Color(0xFFFACC15L) else Color(0xFF818CF8L),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            haptics.tick()
                            showPasscodeModal = true
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVipUnlocked) "★ VIP ACTIVE" else "★ VIP PASSCODE",
                        color = if (isVipUnlocked) Color(0xFFFEF08AL) else Color(0xFFC7D2FEL),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (statusBanner != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1B4BL).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(vertical = 5.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusBanner ?: "",
                        color = Color(0xFFFDE047L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var selectedCategoryIndex by remember { mutableIntStateOf(0) }

            // Live Interactive Stage & Core Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(0xFF6366F1L).copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            simHeroX = (simHeroX + dragAmount.x).coerceIn(24f, size.width.toFloat() - 24f)
                            simHeroY = (simHeroY + dragAmount.y).coerceIn(24f, size.height.toFloat() - 24f)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawNebulaBackground(
                        nebula = nebulaBitmap,
                        starsBack = nebulaBitmap,
                        starsFront = nebulaBitmap,
                        time = animTime,
                        isFocus = false,
                        bgTheme = bgTheme,
                        juiceIntensity = 0.25f
                    )

                    simEnemies.forEach { e ->
                        drawEnemySprite(
                            e = e,
                            alpha = 1f,
                            chromatic = 0f,
                            pulsarMandala = enemyPulsarMandala,
                            seekerMandala = enemySeekerMandala,
                            wandererMandala = enemyWandererMandala,
                            time = animTime,
                            beatPhase = (animTime * 2f) % 1f
                        )
                    }

                    val previewHero = GameObject(simHeroX, simHeroY, 18f, Color(0xFF22C55EL))
                    drawPlayerSprite(
                        p = previewHero,
                        isFocus = false,
                        time = animTime,
                        shield = 0f,
                        applyCB = { it },
                        isDead = false,
                        heroSolarMandala = heroSolarMandala,
                        heroCyberMandala = heroCyberMandala,
                        heroPrismMandala = heroPrismMandala,
                        skin = heroSkin,
                        invulnTime = 0f,
                        beatPhase = (animTime * 2f) % 1f
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .border(1.dp, Color(0xFF22C55EL).copy(alpha = 0.55f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "DRAG CORE TO PREVIEW",
                        color = Color(0xFF4ADE80L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direct 2-Segment Switcher (Stage Palettes vs Hero Cores — Zero Clunky Dropdown, Zero Redundant Reels!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF070D1CL))
                    .border(1.2.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                val palettesSel = selectedCategoryIndex == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (palettesSel) Brush.horizontalGradient(listOf(Color(0xFF0284C7L), Color(0xFF4338CAL)))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            haptics.tick()
                            selectedCategoryIndex = 0
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "STAGE PALETTES",
                        color = if (palettesSel) Color.White else Color(0xFF94A3B8L),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp
                    )
                }

                val coresSel = selectedCategoryIndex == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (coresSel) Brush.horizontalGradient(listOf(Color(0xFF16A34AL), Color(0xFF0D9488L)))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            haptics.tick()
                            selectedCategoryIndex = 1
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HERO CORES",
                        color = if (coresSel) Color.White else Color(0xFF94A3B8L),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedCategoryIndex == 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ThemeCard(
                        title = "ILLUSION",
                        subtitle = "Indigo & Forest Green",
                        isSelected = bgTheme == "OBSIDIAN",
                        isUnlocked = "BG_OBSIDIAN" in unlockedSet,
                        costNotes = 0,
                        accentColor = Color(0xFF6366F1L),
                        modifier = Modifier.weight(1f)
                    ) { applyOrUnlock("BG_OBSIDIAN", 0, newBg = "OBSIDIAN") }

                    ThemeCard(
                        title = "24K VINYL",
                        subtitle = "Gold Collector Stage",
                        isSelected = bgTheme == "GOLD_VINYL",
                        isUnlocked = "BG_GOLD_VINYL" in unlockedSet,
                        costNotes = 450,
                        accentColor = Color(0xFFFACC15L),
                        modifier = Modifier.weight(1f)
                    ) { applyOrUnlock("BG_GOLD_VINYL", 450, newBg = "GOLD_VINYL") }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ThemeCard(
                    title = "MORE STUDIO PALETTES COMING SOON",
                    subtitle = "Master existing tracks to unlock upcoming releases",
                    isSelected = false,
                    isUnlocked = true,
                    costNotes = 0,
                    accentColor = Color(0xFF38BDF8L),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    statusBanner = "Ethan Correa is recording upcoming singles & stage themes!"
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ThemeCard(
                        title = "EMERALD",
                        subtitle = "Starter Core",
                        isSelected = heroSkin == "SOLAR",
                        isUnlocked = "HERO_SOLAR" in unlockedSet,
                        costNotes = 0,
                        accentColor = Color(0xFF22C55EL),
                        modifier = Modifier.weight(1f)
                    ) { applyOrUnlock("HERO_SOLAR", 0, newHero = "SOLAR") }

                    ThemeCard(
                        title = "24K ECLIPSE",
                        subtitle = "Solar Corona",
                        isSelected = heroSkin == "ECLIPSE",
                        isUnlocked = "HERO_ECLIPSE" in unlockedSet,
                        costNotes = 450,
                        accentColor = Color(0xFFFACC15L),
                        modifier = Modifier.weight(1f)
                    ) { applyOrUnlock("HERO_ECLIPSE", 450, newHero = "ECLIPSE") }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ThemeCard(
                    title = "MORE HERO CORES COMING SOON",
                    subtitle = "Master existing tracks to unlock upcoming releases",
                    isSelected = false,
                    isUnlocked = true,
                    costNotes = 0,
                    accentColor = Color(0xFF38BDF8L),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    statusBanner = "Ethan Correa is recording upcoming singles & core designs!"
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            StudioPillButton(
                text = "LAUNCH SELECTED TRACK ▶",
                palette = StudioPillPalette.EMERALD_STAGE,
                onClick = onLaunchTestGame,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Artist VIP Passcode Modal Overlay
        if (showPasscodeModal) {
            StudioDialog(
                title = "ARTIST VIP TESTING PASSCODE",
                onDismiss = { showPasscodeModal = false },
                accentColor = Color(0xFFFACC15)
            ) {
                Text(
                    text = "Enter your artist passcode to unlock all Customizer skins, Wave Palettes, Vault Exclusives, and +50,000 ♪ Notes for testing.",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF03060CL))
                        .border(
                            1.2.dp,
                            if (passcodeError) Color(0xFFFF1E42L) else Color(0xFF6366F1L),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (passcodeText.isEmpty()) {
                        Text(
                            text = if (BuildConfig.DEBUG) "ENTER PASSCODE (e.g. ILLUSIONS2026)" else "ENTER SECRET PASSCODE",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    BasicTextField(
                        value = passcodeText,
                        onValueChange = {
                            passcodeText = it.uppercase()
                            passcodeError = false
                        },
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFFFACC15L)),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (passcodeError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (BuildConfig.DEBUG) "Invalid passcode. Try ILLUSIONS2026 or CONFIDE" else "Invalid passcode. Please check and try again.",
                        color = Color(0xFFFF1E42L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    StudioPillButton(
                        text = "CANCEL",
                        palette = StudioPillPalette.SLATE_OBSIDIAN,
                        onClick = { showPasscodeModal = false },
                        modifier = Modifier.weight(1f).height(42.dp),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    StudioPillButton(
                        text = "UNLOCK ALL",
                        palette = StudioPillPalette.SOLAR_GOLD,
                        onClick = {
                            val clean = passcodeText.trim().uppercase()
                            if (clean == "ILLUSIONS2026" || clean == "CONFIDE" || clean == "NMI2026" || clean == "ETHAN") {
                                onActivateVipPasscode()
                                showPasscodeModal = false
                                statusBanner = "★ ARTIST VIP MODE ACTIVE: ALL ITEMS + 50,000 ♪ UNLOCKED!"
                            } else {
                                passcodeError = true
                            }
                        },
                        modifier = Modifier.weight(1.3f).height(42.dp),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Interactive Artist Vault Exclusive Showcase Modal
        if (activeVaultModal != null) {
            ArtistVaultShowcaseModal(
                vaultId = activeVaultModal ?: "VAULT_DEMO",
                animTime = animTime,
                onLaunchTrack = {
                    activeVaultModal = null
                    onLaunchTestGame()
                },
                onClose = { activeVaultModal = null }
            )
        }
    }
}

