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

                Spacer(modifier = Modifier.width(8.dp))

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
                        .clickable { showPasscodeModal = true }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVipUnlocked) "★ VIP UNLOCKED" else "🔑 VIP PASSCODE",
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

            Spacer(modifier = Modifier.height(8.dp))

            // Live Interactive Optical Illusion Simulation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0xFF6366F1L).copy(alpha = 0.65f), RoundedCornerShape(16.dp))
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
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .border(1.dp, Color(0xFF22C55EL).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE LOADOUT PREVIEW • DRAG CORE TO TEST",
                        color = Color(0xFF4ADE80L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 1: BACKGROUND THEME
            Text(
                text = "1. OPTICAL WAVE PALETTE (UNLOCK WITH ♪ NOTES)",
                color = Color(0xFF4ADE80L),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "ILLUSION",
                    subtitle = "Indigo & Forest Green",
                    isSelected = bgTheme == "OBSIDIAN",
                    isUnlocked = "BG_OBSIDIAN" in unlockedSet,
                    costNotes = 0,
                    accentColor = Color(0xFF6366F1L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("BG_OBSIDIAN", 0, newBg = "OBSIDIAN") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "FOREST ABYSS",
                    subtitle = "Deep Emerald Matrix",
                    isSelected = bgTheme == "CYBER",
                    isUnlocked = "BG_CYBER" in unlockedSet,
                    costNotes = 200,
                    accentColor = Color(0xFF22C55EL),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("BG_CYBER", 200, newBg = "CYBER") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "BREAKDOWN",
                    subtitle = "Crimson & Violet Surge",
                    isSelected = bgTheme == "SUNSET",
                    isUnlocked = "BG_SUNSET" in unlockedSet,
                    costNotes = 300,
                    accentColor = Color(0xFFEC4899L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("BG_SUNSET", 300, newBg = "SUNSET") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "MIRAGE",
                    subtitle = "Electric Purple Bloom",
                    isSelected = bgTheme == "PRISM",
                    isUnlocked = "BG_PRISM" in unlockedSet,
                    costNotes = 350,
                    accentColor = Color(0xFFA855F7L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("BG_PRISM", 350, newBg = "PRISM") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "24K VINYL",
                    subtitle = "Gold Collector Stage",
                    isSelected = bgTheme == "GOLD_VINYL",
                    isUnlocked = "BG_GOLD_VINYL" in unlockedSet,
                    costNotes = 450,
                    accentColor = Color(0xFFFACC15L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("BG_GOLD_VINYL", 450, newBg = "GOLD_VINYL") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "BLOOD MOON",
                    subtitle = "Crimson Solar Eclipse",
                    isSelected = bgTheme == "BLOOD_MOON",
                    isUnlocked = "BG_BLOOD_MOON" in unlockedSet,
                    costNotes = 500,
                    accentColor = Color(0xFFF43F5EL),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("BG_BLOOD_MOON", 500, newBg = "BLOOD_MOON") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: HERO CORE SKIN
            Text(
                text = "2. HERO ILLUSION CORE (6 SIGNATURE AVATARS)",
                color = Color(0xFF4ADE80L),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "EMERALD",
                    subtitle = "Starter Core",
                    isSelected = heroSkin == "SOLAR",
                    isUnlocked = "HERO_SOLAR" in unlockedSet,
                    costNotes = 0,
                    accentColor = Color(0xFF22C55EL),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("HERO_SOLAR", 0, newHero = "SOLAR") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "INDIGO",
                    subtitle = "Deep Nebula",
                    isSelected = heroSkin == "CYBER",
                    isUnlocked = "HERO_CYBER" in unlockedSet,
                    costNotes = 250,
                    accentColor = Color(0xFF6366F1L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("HERO_CYBER", 250, newHero = "CYBER") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "PRISM",
                    subtitle = "Dual Spectrum",
                    isSelected = heroSkin == "PRISM",
                    isUnlocked = "HERO_PRISM" in unlockedSet,
                    costNotes = 350,
                    accentColor = Color(0xFFA855F7L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("HERO_PRISM", 350, newHero = "PRISM") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "45-RPM VINYL",
                    subtitle = "Turntable Halo",
                    isSelected = heroSkin == "VINYL",
                    isUnlocked = "HERO_VINYL" in unlockedSet,
                    costNotes = 400,
                    accentColor = Color(0xFF38BDF8L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("HERO_VINYL", 400, newHero = "VINYL") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "24K ECLIPSE",
                    subtitle = "Solar Corona",
                    isSelected = heroSkin == "ECLIPSE",
                    isUnlocked = "HERO_ECLIPSE" in unlockedSet,
                    costNotes = 450,
                    accentColor = Color(0xFFFACC15L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("HERO_ECLIPSE", 450, newHero = "ECLIPSE") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "TWIN PULSE",
                    subtitle = "Binary Orbit",
                    isSelected = heroSkin == "SERAPH",
                    isUnlocked = "HERO_SERAPH" in unlockedSet,
                    costNotes = 500,
                    accentColor = Color(0xFFE879F9L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("HERO_SERAPH", 500, newHero = "SERAPH") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: ENEMY HAZARD SWARM
            Text(
                text = "3. OPTICAL HAZARD SWARM (UNLOCK WITH ♪ NOTES)",
                color = Color(0xFF4ADE80L),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "BALANCED",
                    subtitle = "Full Spectrum",
                    isSelected = enemyStyle == "BALANCED",
                    isUnlocked = "ENEMY_BALANCED" in unlockedSet,
                    costNotes = 0,
                    accentColor = Color(0xFF22C55EL),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("ENEMY_BALANCED", 0, newEnemy = "BALANCED") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "PULSARS",
                    subtitle = "Chrono-Saw Swarm",
                    isSelected = enemyStyle == "PULSARS",
                    isUnlocked = "ENEMY_PULSARS" in unlockedSet,
                    costNotes = 150,
                    accentColor = Color(0xFF8B5CF6L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("ENEMY_PULSARS", 150, newEnemy = "PULSARS") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "SEEKERS",
                    subtitle = "Fractured Seraphs",
                    isSelected = enemyStyle == "SEEKERS",
                    isUnlocked = "ENEMY_SEEKERS" in unlockedSet,
                    costNotes = 250,
                    accentColor = Color(0xFFEC4899L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("ENEMY_SEEKERS", 250, newEnemy = "SEEKERS") }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "WANDERERS",
                    subtitle = "Void Leviathans",
                    isSelected = enemyStyle == "WANDERERS",
                    isUnlocked = "ENEMY_WANDERERS" in unlockedSet,
                    costNotes = 300,
                    accentColor = Color(0xFF10B981L),
                    modifier = Modifier.weight(1f)
                ) { applyOrUnlock("ENEMY_WANDERERS", 300, newEnemy = "WANDERERS") }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 4: INTERACTIVE ARTIST VAULT EXCLUSIVES
            Text(
                text = "4. ARTIST VAULT EXCLUSIVES (TAP UNLOCKED CARD TO OPEN)",
                color = Color(0xFFFACC15L),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "VAULT TAPE DECK",
                    subtitle = "Slowed & Nightcore",
                    isSelected = "VAULT_DEMO" in unlockedSet,
                    isUnlocked = "VAULT_DEMO" in unlockedSet,
                    costNotes = 500,
                    accentColor = Color(0xFFFACC15L),
                    modifier = Modifier.weight(1f)
                ) {
                    if ("VAULT_DEMO" in unlockedSet) {
                        activeVaultModal = "VAULT_DEMO"
                    } else {
                        applyOrUnlock("VAULT_DEMO", 500)
                        if ("VAULT_DEMO" in unlockedSet || pulsePoints >= 500) {
                            activeVaultModal = "VAULT_DEMO"
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "LYRIC BOOKLET",
                    subtitle = "Director's Liner Notes",
                    isSelected = "VAULT_LYRICS" in unlockedSet,
                    isUnlocked = "VAULT_LYRICS" in unlockedSet,
                    costNotes = 500,
                    accentColor = Color(0xFF38BDF8L),
                    modifier = Modifier.weight(1f)
                ) {
                    if ("VAULT_LYRICS" in unlockedSet) {
                        activeVaultModal = "VAULT_LYRICS"
                    } else {
                        applyOrUnlock("VAULT_LYRICS", 500)
                        if ("VAULT_LYRICS" in unlockedSet || pulsePoints >= 500) {
                            activeVaultModal = "VAULT_LYRICS"
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeCard(
                    title = "ERA STORYBOARD",
                    subtitle = "Studio Session Cards",
                    isSelected = "VAULT_POLAROIDS" in unlockedSet,
                    isUnlocked = "VAULT_POLAROIDS" in unlockedSet,
                    costNotes = 600,
                    accentColor = Color(0xFFE879F9L),
                    modifier = Modifier.weight(1f)
                ) {
                    if ("VAULT_POLAROIDS" in unlockedSet) {
                        activeVaultModal = "VAULT_POLAROIDS"
                    } else {
                        applyOrUnlock("VAULT_POLAROIDS", 600)
                        if ("VAULT_POLAROIDS" in unlockedSet || pulsePoints >= 600) {
                            activeVaultModal = "VAULT_POLAROIDS"
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                ThemeCard(
                    title = "4K ART & VINYL",
                    subtitle = "Holographic Foil Showcase",
                    isSelected = "VAULT_WALLPAPER" in unlockedSet,
                    isUnlocked = "VAULT_WALLPAPER" in unlockedSet,
                    costNotes = 600,
                    accentColor = Color(0xFF4ADE80L),
                    modifier = Modifier.weight(1f)
                ) {
                    if ("VAULT_WALLPAPER" in unlockedSet) {
                        activeVaultModal = "VAULT_WALLPAPER"
                    } else {
                        applyOrUnlock("VAULT_WALLPAPER", 600)
                        if ("VAULT_WALLPAPER" in unlockedSet || pulsePoints >= 600) {
                            activeVaultModal = "VAULT_WALLPAPER"
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // LAUNCH TEST GAME BUTTON
            ArcadePillButton(
                text = "LAUNCH SELECTED TRACK",
                pillBitmap = btnPillGreen,
                onClick = onLaunchTestGame,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Artist VIP Passcode Modal Overlay
        if (showPasscodeModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.84f))
                    .clickable(enabled = true) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0B132BL), Color(0xFF050814L))
                            )
                        )
                        .border(1.5.dp, Color(0xFFFACC15L).copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "ARTIST VIP TESTING PASSCODE",
                        color = Color(0xFFFACC15L),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                text = "ENTER PASSCODE (e.g. ILLUSIONS2026)",
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
                            text = "Invalid passcode. Try ILLUSIONS2026 or CONFIDE",
                            color = Color(0xFFFF1E42L),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.10f))
                                .clickable { showPasscodeModal = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CANCEL",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFACC15L), Color(0xFFD97706L))
                                    )
                                )
                                .clickable {
                                    val clean = passcodeText.trim().uppercase()
                                    if (clean == "ILLUSIONS2026" || clean == "CONFIDE" || clean == "NMI2026" || clean == "ETHAN") {
                                        onActivateVipPasscode()
                                        showPasscodeModal = false
                                        statusBanner = "★ ARTIST VIP MODE ACTIVE: ALL ITEMS + 50,000 ♪ UNLOCKED!"
                                    } else {
                                        passcodeError = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "UNLOCK ALL",
                                color = Color(0xFF030712L),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
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

@Composable
private fun ArtistVaultShowcaseModal(
    vaultId: String,
    animTime: Float,
    onLaunchTrack: () -> Unit,
    onClose: () -> Unit
) {
    var selectedSubTab by remember(vaultId) { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(enabled = true) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A1228L), Color(0xFF040711L))
                    )
                )
                .border(1.5.dp, Color(0xFFFACC15L).copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            val title = when (vaultId) {
                "VAULT_DEMO" -> "★ STUDIO VAULT TAPE DECK"
                "VAULT_LYRICS" -> "★ DIRECTOR'S CUT LYRIC BOOKLET"
                "VAULT_POLAROIDS" -> "★ NO MORĒ ILLUSĪONS ERA STORYBOARD"
                else -> "★ 4K HOLOGRAPHIC ART & VINYL VAULT"
            }
            val badge = when (vaultId) {
                "VAULT_DEMO" -> "MASTER REEL • SLOWED 0.85x & NIGHTCORE 1.20x"
                "VAULT_LYRICS" -> "SYNCHRONIZED SONG ARCHITECTURE & CHOREOGRAPHY"
                "VAULT_POLAROIDS" -> "BEHIND THE SCENES • COLLECTOR CARDS"
                else -> "COLLECTOR'S PRESSING • HOLOGRAPHIC FOIL"
            }

            Text(
                text = title,
                color = Color(0xFFFACC15L),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = badge,
                color = Color(0xFF38BDF8L),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (vaultId) {
                "VAULT_DEMO" -> {
                    // Spinning Dual-Reel Studio Tape Deck Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF04070FL))
                            .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cy = size.height * 0.5f
                            val leftX = size.width * 0.30f
                            val rightX = size.width * 0.70f
                            val reelR = size.height * 0.34f
                            // Tape path between reels
                            drawLine(
                                color = Color(0xFFFACC15L).copy(alpha = 0.55f),
                                start = Offset(leftX, cy + reelR * 0.85f),
                                end = Offset(rightX, cy + reelR * 0.85f),
                                strokeWidth = 3f
                            )
                            listOf(leftX, rightX).forEach { rx ->
                                drawCircle(
                                    color = Color(0xFF0F172AL),
                                    radius = reelR,
                                    center = Offset(rx, cy)
                                )
                                drawCircle(
                                    color = Color(0xFFFACC15L),
                                    radius = reelR,
                                    center = Offset(rx, cy),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                                )
                                for (s in 0 until 3) {
                                    val ang = animTime * 2.6f + s * (2f * PI.toFloat() / 3f)
                                    drawLine(
                                        color = Color(0xFFFDE047L),
                                        start = Offset(rx, cy),
                                        end = Offset(rx + cos(ang) * reelR * 0.85f, cy + sin(ang) * reelR * 0.85f),
                                        strokeWidth = 3f
                                    )
                                }
                                drawCircle(
                                    color = Color(0xFF38BDF8L),
                                    radius = reelR * 0.24f,
                                    center = Offset(rx, cy)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Unlocked in your Turntable Console: switch between ORIGINAL (1.0x), NIGHTCORE OVERCLOCK (1.20x), SLOWED + REVERB (0.85x), and ENDLESS LOOP (∞) directly on the Main Menu!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                "VAULT_LYRICS" -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("01. CONFIDE", "02. NUMBER ONE").forEachIndexed { idx, tabLabel ->
                            val sel = selectedSubTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (sel) Color(0xFF38BDF8L) else Color(0xFF0F172AL))
                                    .clickable { selectedSubTab = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabLabel,
                                    color = if (sel) Color(0xFF030712L) else Color.White.copy(alpha = 0.75f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val notes = if (selectedSubTab == 0) {
                        listOf(
                            "0:00 • ATMOSPHERIC INTRO — \"Shadow in the mirror...\" (Obsidian Nebula Drift)",
                            "0:54 • CHORUS DROP I — \"Confide in me / No more illusions\" (24K Golden Overdrive + Concert Lasers)",
                            "1:22 • VERSE II (B3NJI FEATURE) — \"Caught inside the undertow\" (Singularity Wormholes Materialize)",
                            "2:15 • BRIDGE BREAKDOWN — \"Strip away the disguise\" (Solar Eclipse Blackout & Sub-Bass Swell)",
                            "2:42 • FINAL CLIMAX — Full 24K Gold Stage Overdrive + Supernova Gyroscope Orbs"
                        )
                    } else {
                        listOf(
                            "0:00 • PULSE INTRO — \"Dialed into the frequency...\" (138 BPM High-Velocity Lock)",
                            "0:26 • CHORUS HOOK — \"You're my number one\" (24K Gold Concert Laser Fan + Twin Pulse)",
                            "0:55 • RAPID VERSE — \"Racing through the neon haze\" (Chrono-Saw & Seraph Swarm Surge)",
                            "1:28 • BASS DROP — \"Zero gravity\" (Kerr Accretion Wormholes & Neon Phase Gates)",
                            "1:52 • FINAL OVERDRIVE — Maximum BPM Star-Power Finale"
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        notes.forEach { line ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF060C1AL))
                                    .border(1.dp, Color(0xFF38BDF8L).copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = line,
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                "VAULT_POLAROIDS" -> {
                    val cards = listOf(
                        Triple("SESSION 01 • THE MIRROR ERA", "Indigo & Emerald visual identity born from late-night vocal layering.", Color(0xFF4ADE80L)),
                        Triple("SESSION 02 • CONFIDE (FEAT. B3NJI)", "124 BPM emotional anthem fusing melodic hooks with raw honesty.", Color(0xFF818CF8L)),
                        Triple("SESSION 03 • NUMBER ONE OVERDRIVE", "138 BPM high-octane rush engineered for peak replayability.", Color(0xFFFACC15L)),
                        Triple("SESSION 04 • THE NEXT CHAPTER", "Stay locked to the Release Radar for upcoming singles & live dates.", Color(0xFFE879F9L))
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cards.forEach { (head, body, col) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF060B16L))
                                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = head,
                                        color = col,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = body,
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    // 4K Holographic Cover Art & Gold Vinyl Showcase
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFFFACC15L), RoundedCornerShape(16.dp))
                    ) {
                        TrackRevealOverlay(
                            trackIndex = if (selectedSubTab == 0) 1 else 2,
                            progress = 1.0f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("CONFIDE 4K ART", "NUMBER ONE 4K ART").forEachIndexed { idx, label ->
                            val sel = selectedSubTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (sel) Color(0xFF4ADE80L) else Color(0xFF0F172AL))
                                    .clickable { selectedSubTab = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (sel) Color(0xFF030712L) else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLOSE VAULT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1.25f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF22C55EL), Color(0xFF15803DL))
                            )
                        )
                        .clickable { onLaunchTrack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LAUNCH STAGE ▶",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isUnlocked: Boolean = true,
    costNotes: Int = 0,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardBrush = when {
        isSelected && isUnlocked -> Brush.verticalGradient(
            listOf(
                accentColor.copy(alpha = 0.30f),
                Color(0xFF070E1AL),
                Color(0xFF03060CL)
            )
        )
        !isUnlocked -> Brush.verticalGradient(
            listOf(
                Color(0xFF05070DL),
                Color(0xFF020307L)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color(0xFF070C16L),
                Color(0xFF03060CL)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBrush)
            .border(
                width = if (isSelected && isUnlocked) 1.8.dp else 1.dp,
                color = when {
                    isSelected && isUnlocked -> accentColor
                    !isUnlocked -> Color(0xFFFACC15L).copy(alpha = 0.32f)
                    else -> Color.White.copy(alpha = 0.14f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !isUnlocked -> Color(0xFFFACC15L)
                                isSelected -> accentColor
                                else -> accentColor.copy(alpha = 0.45f)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isUnlocked) title else "🔒 $title",
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isUnlocked) subtitle else "UNLOCK • $costNotes ♪ NOTES",
                color = when {
                    !isUnlocked -> Color(0xFFFDE047L)
                    isSelected -> accentColor
                    else -> Color.White.copy(alpha = 0.48f)
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
