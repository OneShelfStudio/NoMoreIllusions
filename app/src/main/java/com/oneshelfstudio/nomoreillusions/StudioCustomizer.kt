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
    var previewBgTheme by remember(customBgTheme) { mutableStateOf(customBgTheme) }
    var previewHeroSkin by remember(customHeroSkin) { mutableStateOf(customHeroSkin) }
    var previewStageFx by remember { mutableStateOf<StageFxBackdrop?>(StageFxBackdrop.AURORA_SILK) }
    var lockedPreviewId by remember { mutableStateOf<String?>(null) }
    var lockedPreviewLabel by remember { mutableStateOf<String?>(null) }
    var lockedPreviewCost by remember { mutableIntStateOf(0) }
    var lockedPreviewApply by remember { mutableStateOf<(() -> Unit)?>(null) }

    val unlockedSet = remember(unlockedItemsCsv, isVipUnlocked) {
        val base = unlockedItemsCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        // Default starter loadout + 2 starter Stage FX Orbs are always unlocked
        base.add("BG_OBSIDIAN")
        base.add("HERO_SOLAR")
        base.add("ENEMY_BALANCED")
        base.add("FX_AURORA")
        base.add("FX_MANDALA")
        if (isVipUnlocked) {
            base.addAll(
                listOf(
                    "BG_CYBER", "BG_SUNSET", "BG_PRISM", "BG_GOLD_VINYL", "BG_BLOOD_MOON",
                    "HERO_CYBER", "HERO_PRISM", "HERO_VINYL", "HERO_ECLIPSE", "HERO_SERAPH",
                    "ENEMY_PULSARS", "ENEMY_SEEKERS", "ENEMY_WANDERERS",
                    "FX_STARFALL", "FX_PRISM", "FX_ECLIPSE", "FX_VORTEX",
                    "VAULT_LYRICS", "VAULT_WALLPAPER", "VAULT_ERA"
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

    fun previewOrApply(
        itemId: String,
        label: String,
        cost: Int,
        newBg: String = bgTheme,
        newHero: String = heroSkin,
        newEnemy: String = enemyStyle
    ) {
        previewBgTheme = newBg
        previewHeroSkin = newHero
        if (cost == 0 || itemId in unlockedSet || isVipUnlocked) {
            bgTheme = newBg
            heroSkin = newHero
            enemyStyle = newEnemy
            onUpdateCustomization(newBg, newHero, newEnemy)
            lockedPreviewId = null
            lockedPreviewApply = null
            statusBanner = "✦ EQUIPPED: $label"
        } else {
            lockedPreviewId = itemId
            lockedPreviewLabel = label
            lockedPreviewCost = cost
            lockedPreviewApply = {
                if (pulsePoints >= cost) {
                    onUnlockShopItem(itemId, cost)
                    bgTheme = newBg
                    heroSkin = newHero
                    enemyStyle = newEnemy
                    onUpdateCustomization(newBg, newHero, newEnemy)
                    lockedPreviewId = null
                    lockedPreviewApply = null
                    statusBanner = "UNLOCKED & EQUIPPED $label FOR $cost ♪ NOTES!"
                } else {
                    statusBanner = "NEED $cost ♪ NOTES — PLAY OR WATCH AN AD (+150 ♪) TO UNLOCK!"
                }
            }
            statusBanner = if (pulsePoints >= cost) {
                "PREVIEWING $label — TAP UNLOCK BELOW FOR $cost ♪ NOTES!"
            } else {
                "PREVIEWING $label — EARN OR WATCH AD FOR $cost ♪ NOTES TO UNLOCK!"
            }
        }
    }

    var canvasWidthPx by remember { mutableFloatStateOf(0f) }
    var canvasHeightPx by remember { mutableFloatStateOf(0f) }
    var simHeroX by remember { mutableFloatStateOf(0f) }
    var simHeroY by remember { mutableFloatStateOf(0f) }
    val simEnemies = remember { mutableStateListOf<GameObject>() }

    LaunchedEffect(enemyStyle, canvasWidthPx, canvasHeightPx) {
        val w = if (canvasWidthPx > 60f) canvasWidthPx else 720f
        val h = if (canvasHeightPx > 60f) canvasHeightPx else 320f
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
                    x = w * (0.16f + i * 0.22f),
                    y = h * (if (i % 2 == 0) 0.28f else 0.72f),
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
                val maxW = if (canvasWidthPx > 100f) canvasWidthPx - 20f else 700f
                val maxH = if (canvasHeightPx > 80f) canvasHeightPx - 20f else 300f
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
                    if (e.x < 20f) { e.x = maxW } else if (e.x > maxW) { e.x = 20f }
                    if (e.y < 20f) { e.y = maxH } else if (e.y > maxH) { e.y = 20f }
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1B4BL).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(vertical = 6.dp, horizontal = 10.dp),
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

            if (lockedPreviewId != null && lockedPreviewApply != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val canAfford = pulsePoints >= lockedPreviewCost
                StudioPillButton(
                    text = if (canAfford) "UNLOCK ${lockedPreviewLabel ?: "ITEM"} • $lockedPreviewCost ♪" else "NEED $lockedPreviewCost ♪ TO UNLOCK",
                    palette = if (canAfford) StudioPillPalette.SOLAR_GOLD else StudioPillPalette.SLATE_OBSIDIAN,
                    onClick = { lockedPreviewApply?.invoke() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            var selectedCategoryIndex by remember { mutableIntStateOf(0) }

            // Live Interactive Stage & Core Preview Box (Centered Dynamically)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(0xFF6366F1L).copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            simHeroX = (simHeroX + dragAmount.x).coerceIn(28f, size.width.toFloat() - 28f)
                            simHeroY = (simHeroY + dragAmount.y).coerceIn(28f, size.height.toFloat() - 28f)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (canvasWidthPx == 0f || abs(canvasWidthPx - size.width) > 20f || abs(canvasHeightPx - size.height) > 20f) {
                        canvasWidthPx = size.width
                        canvasHeightPx = size.height
                        simHeroX = size.width * 0.5f
                        simHeroY = size.height * 0.56f
                    }
                    val heroX = if (simHeroX <= 1f) size.width * 0.5f else simHeroX
                    val heroY = if (simHeroY <= 1f) size.height * 0.56f else simHeroY

                    drawNebulaBackground(
                        nebula = nebulaBitmap,
                        starsBack = nebulaBitmap,
                        starsFront = nebulaBitmap,
                        time = animTime,
                        isFocus = false,
                        bgTheme = previewBgTheme,
                        juiceIntensity = 0.25f,
                        activeStageFx = previewStageFx,
                        stageFxIntensity = if (previewStageFx != null) 0.95f else 0f
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

                    val previewHero = GameObject(heroX, heroY, 18f, Color(0xFF22C55EL))
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
                        skin = previewHeroSkin,
                        invulnTime = 0f,
                        beatPhase = (animTime * 2f) % 1f
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.74f))
                        .border(1.dp, Color(previewStageFx?.accentHex ?: 0xFF22C55EL).copy(alpha = 0.65f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    val badgeText = when {
                        previewStageFx != null -> "✦ LIVE FX: ${previewStageFx!!.title.uppercase()}"
                        lockedPreviewLabel != null -> "✦ PREVIEWING: $lockedPreviewLabel"
                        else -> "DRAG CORE TO TEST PREVIEW"
                    }
                    Text(
                        text = badgeText,
                        color = Color(previewStageFx?.accentHex ?: 0xFF4ADE80L),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Segment Switcher: Stage FX Orbs | Stage Palettes | Hero Cores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070D1CL))
                    .border(1.2.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                val fxSel = selectedCategoryIndex == 0
                Box(
                    modifier = Modifier
                        .weight(1.08f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (fxSel) Brush.horizontalGradient(listOf(Color(0xFF7C3AEDL), Color(0xFF0284C7L)))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            haptics.tick()
                            selectedCategoryIndex = 0
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦ STAGE FX",
                        color = if (fxSel) Color.White else Color(0xFF94A3B8L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp
                    )
                }

                val palettesSel = selectedCategoryIndex == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (palettesSel) Brush.horizontalGradient(listOf(Color(0xFF0284C7L), Color(0xFF4338CAL)))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            haptics.tick()
                            selectedCategoryIndex = 1
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PALETTES",
                        color = if (palettesSel) Color.White else Color(0xFF94A3B8L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp
                    )
                }

                val coresSel = selectedCategoryIndex == 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (coresSel) Brush.horizontalGradient(listOf(Color(0xFF16A34AL), Color(0xFF0D9488L)))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            haptics.tick()
                            selectedCategoryIndex = 2
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CORES",
                        color = if (coresSel) Color.White else Color(0xFF94A3B8L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedCategoryIndex == 0) {
                Text(
                    text = "UNLOCK SPECIAL FX ORBS — RANDOM FLUID FX ORBS DROP IN-GAME!",
                    color = Color(0xFF7DD3FCL),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
                StageFxBackdrop.entries.chunked(2).forEachIndexed { rowIdx, pair ->
                    if (rowIdx > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pair.forEach { fx ->
                            val isFxUnlocked = fx.costNotes == 0 || fx.shopId in unlockedSet || isVipUnlocked
                            ThemeCard(
                                title = fx.title.uppercase(),
                                subtitle = fx.subtitle,
                                isSelected = isFxUnlocked && previewStageFx == fx,
                                isPreviewed = previewStageFx == fx,
                                isUnlocked = isFxUnlocked,
                                costNotes = fx.costNotes,
                                accentColor = Color(fx.accentHex),
                                secondaryAccent = Color(fx.secondaryHex),
                                modifier = Modifier.weight(1f)
                            ) {
                                previewStageFx = fx
                                if (isFxUnlocked) {
                                    lockedPreviewId = null
                                    lockedPreviewApply = null
                                    statusBanner = "✦ ${fx.title.uppercase()} IS IN YOUR RANDOM FX DROP POOL!"
                                } else {
                                    lockedPreviewId = fx.shopId
                                    lockedPreviewLabel = fx.title.uppercase()
                                    lockedPreviewCost = fx.costNotes
                                    lockedPreviewApply = {
                                        if (pulsePoints >= fx.costNotes) {
                                            onUnlockShopItem(fx.shopId, fx.costNotes)
                                            lockedPreviewId = null
                                            lockedPreviewApply = null
                                            statusBanner = "UNLOCKED ${fx.title.uppercase()} FX ORB FOR ${fx.costNotes} ♪!"
                                        } else {
                                            statusBanner = "NEED ${fx.costNotes} ♪ NOTES — PLAY OR WATCH AD (+150 ♪) TO UNLOCK!"
                                        }
                                    }
                                    statusBanner = "PREVIEWING ${fx.title.uppercase()} — TAP UNLOCK ABOVE (${fx.costNotes} ♪)"
                                }
                            }
                        }
                    }
                }
            } else if (selectedCategoryIndex == 1) {
                val palettes = listOf(
                    Triple("BG_OBSIDIAN", "ILLUSION", Triple("Indigo & Forest Green", 0, "OBSIDIAN" to Color(0xFF6366F1L))),
                    Triple("BG_GOLD_VINYL", "24K VINYL", Triple("Gold Collector Stage", 450, "GOLD_VINYL" to Color(0xFFFACC15L))),
                    Triple("BG_CYBER", "EMERALD PULSE", Triple("Neon Matrix Canopy", 550, "CYBER" to Color(0xFF22C55EL))),
                    Triple("BG_PRISM", "CRYSTAL PRISM", Triple("Sapphire & Aurora", 700, "PRISM" to Color(0xFF38BDF8L))),
                    Triple("BG_SUNSET", "VELVET DUSK", Triple("Amethyst Horizon", 850, "SUNSET" to Color(0xFFA855F7L))),
                    Triple("BG_BLOOD_MOON", "SUPERNOVA", Triple("Ruby Eclipse Stage", 1000, "BLOOD_MOON" to Color(0xFFF43F5EL)))
                )
                palettes.chunked(2).forEachIndexed { rowIdx, pair ->
                    if (rowIdx > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pair.forEach { (shopKey, title, meta) ->
                            val (sub, cost, codeAndColor) = meta
                            val (themeCode, col) = codeAndColor
                            ThemeCard(
                                title = title,
                                subtitle = sub,
                                isSelected = bgTheme == themeCode,
                                isPreviewed = previewBgTheme == themeCode,
                                isUnlocked = cost == 0 || shopKey in unlockedSet || isVipUnlocked,
                                costNotes = cost,
                                accentColor = col,
                                modifier = Modifier.weight(1f)
                            ) {
                                previewStageFx = null
                                previewOrApply(shopKey, title, cost, newBg = themeCode)
                            }
                        }
                    }
                }
            } else {
                val cores = listOf(
                    Triple("HERO_SOLAR", "EMERALD", Triple("Starter Core", 0, "SOLAR" to Color(0xFF22C55EL))),
                    Triple("HERO_ECLIPSE", "24K ECLIPSE", Triple("Solar Corona", 450, "ECLIPSE" to Color(0xFFFACC15L))),
                    Triple("HERO_CYBER", "CYBERLOTUS", Triple("Electric Cyan Halo", 600, "CYBER" to Color(0xFF38BDF8L))),
                    Triple("HERO_PRISM", "PRISM STAR", Triple("Iridescent Orchid", 750, "PRISM" to Color(0xFFE879F9L))),
                    Triple("HERO_VINYL", "PLATINUM", Triple("Turntable Crest", 950, "VINYL" to Color(0xFF7DD3FCL))),
                    Triple("HERO_SERAPH", "NOVA SERAPH", Triple("Celestial Rose Core", 1200, "SERAPH" to Color(0xFFF472B6L)))
                )
                cores.chunked(2).forEachIndexed { rowIdx, pair ->
                    if (rowIdx > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pair.forEach { (shopKey, title, meta) ->
                            val (sub, cost, codeAndColor) = meta
                            val (skinCode, col) = codeAndColor
                            ThemeCard(
                                title = title,
                                subtitle = sub,
                                isSelected = heroSkin == skinCode,
                                isPreviewed = previewHeroSkin == skinCode,
                                isUnlocked = cost == 0 || shopKey in unlockedSet || isVipUnlocked,
                                costNotes = cost,
                                accentColor = col,
                                modifier = Modifier.weight(1f)
                            ) {
                                previewStageFx = null
                                previewOrApply(shopKey, title, cost, newHero = skinCode)
                            }
                        }
                    }
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
                vaultId = activeVaultModal ?: "VAULT_LYRICS",
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

