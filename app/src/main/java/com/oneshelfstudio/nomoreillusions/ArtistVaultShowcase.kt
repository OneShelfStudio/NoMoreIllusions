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
internal fun ArtistVaultShowcaseModal(
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
                "VAULT_LYRICS" -> "DIRECTOR'S CUT LYRIC BOOKLET"
                "VAULT_POLAROIDS" -> "NO MORĒ ILLUSĪONS ERA ARCHIVE"
                else -> "4K HOLOGRAPHIC ART & VINYL VAULT"
            }
            val badge = when (vaultId) {
                "VAULT_LYRICS" -> "SONG ARCHITECTURE & SECTION TIMELINE"
                "VAULT_POLAROIDS" -> "BEHIND THE SCENES • STUDIO NOTES"
                else -> "COLLECTOR'S PRESSING • HIGH-RES COVER ART"
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

                "VAULT_POLAROIDS" -> {
                    val cards = listOf(
                        Triple("STUDIO ARCHIVE • TRACK 01", "Confide — Official master recording and stems engineered for No Morē Illusīons.", Color(0xFF4ADE80L)),
                        Triple("STUDIO ARCHIVE • TRACK 02", "Number One — Official master recording and stems engineered for No Morē Illusīons.", Color(0xFF818CF8L)),
                        Triple("OFFICIAL RELEASES", "No Morē Illusīons — Connect with @EthanCorrea and share your highest scores using #NoMoreIllusions.", Color(0xFFFACC15L))
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
            .padding(vertical = 14.dp, horizontal = 12.dp),
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
            Spacer(modifier = Modifier.height(4.dp))
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
            // 1. Breathable Collector's Progress Header & 3 Authentic Archive Launchers (No nonsensical Reels sub-menu!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF101D3BL), Color(0xFF060B18L))
                        )
                    )
                    .border(1.5.dp, Color(0xFFFACC15L).copy(alpha = 0.75f), RoundedCornerShape(18.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Triple("VAULT_LYRICS", "LYRIC BOOK", Color(0xFF38BDF8L)),
                        Triple("VAULT_POLAROIDS", "STUDIO NOTES", Color(0xFFE879F9L)),
                        Triple("VAULT_WALLPAPER", "4K ARTWORK", Color(0xFF4ADE80L))
                    ).forEach { (id, label, col) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(col.copy(alpha = 0.14f))
                                .border(1.dp, col.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                .clickable { activeModalId = id }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "STUDIO ARCHIVE ENVELOPES",
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
                        .clickable { expandedItemId = if (isExpanded) null else item.id }
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
                                else (if (isExpanded) "🔒 HOW ▴" else "🔒 PEEK ▾"),
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF070D1CL))
                    .border(1.dp, Color(0xFFC084FCL).copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .clickable { showVipDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isVipUnlocked) "★ VIP BACKSTAGE PASS ACTIVE" else "🔑 ARTIST VIP PASSCODE",
                            color = Color(0xFFC084FCL),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isVipUnlocked) "All Artist Secrets & +50,000 ♪ Notes Unlocked!"
                            else "Have Ethan's backstage code? Tap to unlock everything.",
                            color = Color.White.copy(alpha = 0.75f),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = true) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A1228L))
                        .border(1.5.dp, Color(0xFFFACC15L), RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "VIP BACKSTAGE PASSCODE",
                        color = Color(0xFFFACC15L),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
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
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { showVipDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("CANCEL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFFACC15L))
                                .clickable {
                                    val clean = vipCodeInput.trim().uppercase()
                                    if (clean in listOf("ETHAN", "CONFIDE", "ILLUSIONS2026", "NMI2026")) {
                                        onActivateVipPasscode()
                                        showVipDialog = false
                                    } else {
                                        vipError = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("UNLOCK ALL", color = Color(0xFF020617L), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
