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

            var selectedCategoryIndex by remember { mutableIntStateOf(0) }
            var showCategoryDropdown by remember { mutableStateOf(false) }
            val categories = listOf(
                Triple(0, "🌌 STAGE PALETTES (6 THEMES)", Color(0xFF38BDF8L)),
                Triple(1, "🛸 HERO AVATARS (6 CORES)", Color(0xFF4ADE80L)),
                Triple(2, "👾 ENEMY SWARMS (4 STYLES)", Color(0xFFF43F5EL)),
                Triple(3, "★ VAULT REELS & BOOKLETS", Color(0xFFFACC15L))
            )

            // Live Interactive Optical Illusion Simulation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            // CATEGORY DROPDOWN SELECTOR (Eliminates vertical scroll wall!)
            val activeCat = categories[selectedCategoryIndex]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A1328L))
                    .border(1.5.dp, activeCat.third.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDropdown = !showCategoryDropdown }
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CUSTOMIZE CATEGORY (TAP TO SWITCH)",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeCat.second,
                            color = activeCat.third,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(activeCat.third.copy(alpha = 0.18f))
                            .border(1.dp, activeCat.third.copy(alpha = 0.65f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showCategoryDropdown) "CLOSE ▴" else "SWITCH ▾",
                            color = activeCat.third,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (showCategoryDropdown) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF050914L))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { (idx, label, col) ->
                            val isSel = idx == selectedCategoryIndex
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) col.copy(alpha = 0.18f) else Color(0xFF0B1324L))
                                    .border(1.dp, if (isSel) col else Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedCategoryIndex = idx
                                        showCategoryDropdown = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) col else Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (isSel) "✓ SELECTED" else "SELECT →",
                                    color = if (isSel) col else Color.White.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedCategoryIndex == 0) {
                // SECTION 1: BACKGROUND THEME
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
            } else if (selectedCategoryIndex == 1) {
                // SECTION 2: HERO CORE SKIN
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
            } else if (selectedCategoryIndex == 2) {
                // SECTION 3: ENEMY HAZARD SWARM
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
            } else {
                // SECTION 4: INTERACTIVE ARTIST VAULT EXCLUSIVES
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LAUNCH TEST GAME BUTTON
            ArcadePillButton(
                text = "LAUNCH SELECTED TRACK",
                pillBitmap = btnPillGreen,
                onClick = onLaunchTestGame,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))
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

