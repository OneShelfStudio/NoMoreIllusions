package com.oneshelfstudio.nomoreillusions

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*


@Composable
internal fun ArtistVaultShowcaseModal(
    vaultId: String,
    animTime: Float,
    onLaunchTrack: () -> Unit,
    onClose: () -> Unit
) {
    var selectedSubTab by remember(vaultId) { mutableIntStateOf(0) }

    val title = when (vaultId) {
        "VAULT_LYRICS" -> "OFFICIAL TRACK CREDITS"
        "VAULT_POLAROIDS", "VAULT_ERA" -> "ARTIST EXCLUSIVES & RELEASES"
        else -> "COLLECTOR'S VINYL PRESSING VAULT"
    }
    val badge = when (vaultId) {
        "VAULT_LYRICS" -> "RELEASE ARCHITECTURE & CREDITS"
        "VAULT_POLAROIDS", "VAULT_ERA" -> "OFFICIAL SINGLES • FUTURE ARTIST EXCLUSIVES"
        else -> "COLLECTOR'S EDITION • OFFICIAL ARTWORK"
    }

    StudioDialog(
        title = title,
        onDismiss = onClose,
        accentColor = Color(0xFFFACC15)
    ) {
        Text(
            text = badge,
            color = Color(0xFF38BDF8L),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

            when (vaultId) {
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
                            "TITLE: CONFIDE",
                            "ARTIST: ETHAN CORREA",
                            "TEMPO: 124 BPM • LOSSLESS AUDIO MASTER",
                            "DRUM ENGINEERING & PERCUSSION: DAMIEN",
                            "ALBUM: NO MORĒ ILLUSĪONS"
                        )
                    } else {
                        listOf(
                            "TITLE: NUMBER ONE",
                            "ARTIST: ETHAN CORREA",
                            "TEMPO: 88 BPM • LOSSLESS AUDIO MASTER",
                            "DRUM ENGINEERING & PERCUSSION: DAMIEN",
                            "ALBUM: NO MORĒ ILLUSĪONS"
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

                "VAULT_POLAROIDS", "VAULT_ERA" -> {
                    val cards = listOf(
                        Triple("OFFICIAL RELEASE • TRACK 01", "Confide — Official audio release by Ethan Correa (Emerald Obsidian Pressing).", Color(0xFF4ADE80L)),
                        Triple("OFFICIAL RELEASE • TRACK 02", "Number One — Official audio release by Ethan Correa (24K Solar Gold Pressing).", Color(0xFFFACC15L)),
                        Triple("UPCOMING MUSIC & EXCLUSIVES", "More Ethan Correa singles and future artist exclusives will unlock here as new tracks release!", Color(0xFF38BDF8L))
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
            StudioPillButton(
                text = "CLOSE",
                palette = StudioPillPalette.SLATE_OBSIDIAN,
                onClick = onClose,
                modifier = Modifier.weight(1f).height(42.dp),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            StudioPillButton(
                text = "LAUNCH STAGE ▶",
                palette = StudioPillPalette.EMERALD_STAGE,
                onClick = onLaunchTrack,
                modifier = Modifier.weight(1.3f).height(42.dp),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ThemeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isPreviewed: Boolean = false,
    isUnlocked: Boolean = true,
    costNotes: Int = 0,
    accentColor: Color,
    secondaryAccent: Color = accentColor,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptics = rememberStudioHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isSelected || isPreviewed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHighlighted) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ThemeCardScale"
    )
    val cardBrush = when {
        isSelected && isUnlocked -> Brush.verticalGradient(
            listOf(
                accentColor.copy(alpha = 0.28f),
                secondaryAccent.copy(alpha = 0.10f),
                Color(0xFF060B18L)
            )
        )
        isPreviewed -> Brush.verticalGradient(
            listOf(
                accentColor.copy(alpha = 0.22f),
                Color(0xFF0A1428L),
                Color(0xFF050814L)
            )
        )
        !isUnlocked -> Brush.verticalGradient(
            listOf(
                Color(0xFF091020L),
                Color(0xFF040710L)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color(0xFF0D172DL),
                Color(0xFF050914L)
            )
        )
    }

    val statusText = when {
        isSelected && isUnlocked -> "EQUIPPED"
        isPreviewed && !isUnlocked -> "PREVIEW • $costNotes ♪"
        isPreviewed -> "PREVIEWING"
        isUnlocked -> "OWNED"
        else -> "UNLOCK $costNotes ♪"
    }
    val statusColor = when {
        isSelected && isUnlocked -> Color(0xFF4ADE80L)
        isPreviewed && !isUnlocked -> Color(0xFFFACC15L)
        isPreviewed -> Color(0xFF38BDF8L)
        isUnlocked -> Color(0xFF94A3B8L)
        else -> Color(0xFFFDE047L)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBrush)
            .border(
                width = if (isHighlighted) 1.8.dp else 1.0.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        when {
                            isSelected && isUnlocked -> accentColor
                            isPreviewed -> Color(0xFF38BDF8L)
                            !isUnlocked -> Color(0xFFFACC15L).copy(alpha = 0.45f)
                            else -> Color.White.copy(alpha = 0.22f)
                        },
                        secondaryAccent.copy(alpha = if (isHighlighted) 0.70f else 0.16f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.tick()
                onClick()
            }
            .padding(vertical = 11.dp, horizontal = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Live Miniature Glass Emblem Swatch
            Canvas(modifier = Modifier.size(30.dp)) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.5f
                val r = size.minDimension * 0.44f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accentColor.copy(alpha = 0.55f), secondaryAccent.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r * 1.15f
                    ),
                    radius = r * 1.15f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    brush = Brush.linearGradient(listOf(accentColor, secondaryAccent)),
                    radius = r * 0.62f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = if (isHighlighted) Color.White else accentColor.copy(alpha = 0.75f),
                    radius = r * 0.90f,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (isHighlighted) 2.2f else 1.4f, cap = StrokeCap.Round)
                )
                val sr = r * 0.36f
                val spath = Path().apply {
                    moveTo(cx, cy - sr)
                    quadraticTo(cx, cy, cx + sr, cy)
                    quadraticTo(cx, cy, cx, cy + sr)
                    quadraticTo(cx, cy, cx - sr, cy)
                    quadraticTo(cx, cy, cx, cy - sr)
                    close()
                }
                drawPath(spath, color = Color.White.copy(alpha = 0.95f))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isUnlocked || isPreviewed) Color.White else Color.White.copy(alpha = 0.85f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(0.8.dp, statusColor.copy(alpha = 0.65f), CircleShape)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 7.8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.3.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Unique Collector's Gatefold "Album & Artist Specials" Hub showcasing Ethan's personal track-by-track
 * Liner Notes, Behind-the-Scenes Vocal Secrets, Master Audio Reels, and Era Storyboards.
 */
@Composable
fun ArtistExclusivesSpecialsHub(
    prefs: android.content.SharedPreferences,
    highScore: Int,
    pulsePoints: Int,
    isVipUnlocked: Boolean,
    onPlayTrack: (Int) -> Unit,
    onActivateVipPasscode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberStudioHaptics()
    var activeModalId by remember { mutableStateOf<String?>(null) }
    var showVipDialog by remember { mutableStateOf(false) }
    var vipCodeInput by remember { mutableStateOf("") }
    var vipError by remember { mutableStateOf(false) }

    val unlockedCount = ARTIST_EXCLUSIVES_CATALOG.count {
        isVipUnlocked || it.isUnlocked(prefs, highScore, pulsePoints)
    }

    var expandedItemId by remember { mutableStateOf<String?>("excl_confide_master") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 14.dp)
        ) {
            // 1. Breathable Collector's Progress Header & 3 Authentic Archive Launchers (Starbloom Glass Standard)
            StudioGlassCard(
                accentColor = Color(0xFFFACC15),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "COLLECTION PROGRESS",
                            color = Color(0xFFFACC15L),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80L).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFF4ADE80L), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$unlockedCount / ${ARTIST_EXCLUSIVES_CATALOG.size} UNLOCKED",
                                color = Color(0xFF4ADE80L),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val pct = (unlockedCount.toFloat() / ARTIST_EXCLUSIVES_CATALOG.size.toFloat()).coerceIn(0.08f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF040812L))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFACC15L), Color(0xFF4ADE80L), Color(0xFF38BDF8L))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StudioPillButton(
                            text = "CREDITS",
                            palette = StudioPillPalette.CYAN_SYNTH,
                            onClick = { activeModalId = "VAULT_LYRICS" },
                            modifier = Modifier.weight(1f).height(44.dp),
                            fontSize = 10.5.sp
                        )
                        StudioPillButton(
                            text = "EXCLUSIVES",
                            palette = StudioPillPalette.VELVET_AMETHYST,
                            onClick = { activeModalId = "VAULT_ERA" },
                            modifier = Modifier.weight(1f).height(44.dp),
                            fontSize = 10.5.sp
                        )
                        StudioPillButton(
                            text = "VINYLS",
                            palette = StudioPillPalette.EMERALD_STAGE,
                            onClick = { activeModalId = "VAULT_WALLPAPER" },
                            modifier = Modifier.weight(1f).height(44.dp),
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "OFFICIAL RELEASES & FUTURE EXCLUSIVES",
                color = Color(0xFF38BDF8L),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.9.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            ARTIST_EXCLUSIVES_CATALOG.forEachIndexed { idx, item ->
                val unlocked = isVipUnlocked || item.isUnlocked(prefs, highScore, pulsePoints)
                val isExpanded = expandedItemId == item.id
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (unlocked) Brush.verticalGradient(listOf(Color(0xFF0A1428L), Color(0xFF050914L)))
                            else Brush.verticalGradient(listOf(Color(0xFF060A15L), Color(0xFF03050BL)))
                        )
                        .border(
                            width = if (isExpanded) 1.5.dp else 1.dp,
                            color = if (unlocked) item.accentColor.copy(alpha = if (isExpanded) 0.9f else 0.55f)
                            else Color.White.copy(alpha = if (isExpanded) 0.35f else 0.15f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            haptics.tick()
                            expandedItemId = if (isExpanded) null else item.id
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.category,
                                color = item.accentColor,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.6.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (unlocked) Color(0xFF16A34AL).copy(alpha = 0.22f)
                                    else Color(0xFFFACC15L).copy(alpha = 0.14f)
                                )
                                .border(
                                    1.dp,
                                    if (unlocked) Color(0xFF4ADE80L) else Color(0xFFFACC15L).copy(alpha = 0.55f),
                                    CircleShape
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (unlocked) (if (isExpanded) "✓ OPEN ▴" else "✓ READ ▾")
                                else (if (isExpanded) "LOCKED ▴" else "PEEK ▾"),
                                color = if (unlocked) Color(0xFF4ADE80L) else Color(0xFFFACC15L),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.subtitle,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (unlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF040812L))
                                    .border(1.dp, item.accentColor.copy(alpha = 0.42f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = item.artistNoteQuote,
                                        color = Color(0xFFF8FAFCL),
                                        fontSize = 11.sp,
                                        lineHeight = 15.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "★ PERK: ${item.exclusiveDetail}",
                                        color = item.accentColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(item.accentColor.copy(alpha = 0.20f))
                                        .border(1.dp, item.accentColor, RoundedCornerShape(50))
                                        .clickable {
                                            when (idx) {
                                                0, 1 -> activeModalId = "VAULT_LYRICS"
                                                2 -> activeModalId = "VAULT_WALLPAPER"
                                                3, 4 -> activeModalId = "VAULT_POLAROIDS"
                                                else -> activeModalId = "VAULT_POLAROIDS"
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "OPEN VIEW",
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(listOf(Color(0xFF22C55EL), Color(0xFF0D9488L)))
                                        )
                                        .clickable { onPlayTrack(if (idx == 1) 2 else 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (idx == 2 || idx == 4) "PLAY TRACK 2 ▶" else "PLAY TRACK 1 ▶",
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF080D1AL))
                                    .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "HOW TO UNLOCK:",
                                            color = Color(0xFFFACC15L),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.unlockRequirementText,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFF22C55EL))
                                            .clickable { onPlayTrack(if (idx == 1) 2 else 1) }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = "PLAY ▶",
                                            color = Color(0xFF020617L),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. VIP Fan / Artist Passcode Card at bottom
            StudioGlassCard(
                accentColor = Color(0xFFC084FC),
                shape = RoundedCornerShape(16.dp),
                onClick = { showVipDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isVipUnlocked) "★ VIP BACKSTAGE PASS ACTIVE" else "★ ARTIST VIP PASSCODE",
                            color = Color(0xFFC084FCL),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isVipUnlocked) "All Artist Secrets & +50,000 ♪ Notes Unlocked!"
                            else "Have Ethan's backstage code? Tap to unlock everything.",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 9.5.sp
                        )
                    }
                    Text(
                        text = if (isVipUnlocked) "✓ ACTIVE" else "ENTER →",
                        color = Color(0xFFFACC15L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (activeModalId != null) {
            ArtistVaultShowcaseModal(
                vaultId = activeModalId ?: "VAULT_LYRICS",
                animTime = 1.2f,
                onLaunchTrack = {
                    activeModalId = null
                    onPlayTrack(1)
                },
                onClose = { activeModalId = null }
            )
        }

        if (showVipDialog) {
            StudioDialog(
                title = "VIP BACKSTAGE PASSCODE",
                onDismiss = { showVipDialog = false },
                accentColor = Color(0xFFFACC15)
            ) {
                Text(
                    text = "Enter Ethan's backstage code to unlock all Artist Secrets and +50,000 ♪ Notes instantly.",
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
                        .border(1.dp, if (vipError) Color(0xFFFF1E42L) else Color(0xFF38BDF8L), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (vipCodeInput.isEmpty()) {
                        Text(
                            text = if (BuildConfig.DEBUG) "ENTER CODE (e.g. ETHAN / CONFIDE)" else "ENTER ARTIST VIP CODE",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    BasicTextField(
                        value = vipCodeInput,
                        onValueChange = {
                            vipCodeInput = it.uppercase()
                            vipError = false
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
                if (vipError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (BuildConfig.DEBUG) "Invalid code. Try ETHAN or CONFIDE" else "Invalid code. Please check and try again.",
                        color = Color(0xFFFF1E42L),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StudioPillButton(
                        text = "CANCEL",
                        palette = StudioPillPalette.SLATE_OBSIDIAN,
                        onClick = { showVipDialog = false },
                        modifier = Modifier.weight(1f).height(42.dp),
                        fontSize = 11.sp
                    )
                    StudioPillButton(
                        text = "UNLOCK ALL",
                        palette = StudioPillPalette.SOLAR_GOLD,
                        onClick = {
                            val clean = vipCodeInput.trim().uppercase()
                            if (clean in listOf("ETHAN", "CONFIDE", "ILLUSIONS2026", "NMI2026")) {
                                onActivateVipPasscode()
                                showVipDialog = false
                            } else {
                                vipError = true
                            }
                        },
                        modifier = Modifier.weight(1.2f).height(42.dp),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
