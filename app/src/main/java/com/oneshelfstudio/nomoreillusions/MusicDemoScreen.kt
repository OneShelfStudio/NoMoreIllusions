package com.oneshelfstudio.nomoreillusions

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun MusicDemoScreen(
    pulsePoints: Int,
    unlockedTrackCount: Int,
    trackListMode: String,
    trackCompletionsCsv: String,
    nebula: ImageBitmap,
    selectedTrackIndexState: Int,
    onSelectTrack: (Int) -> Unit,
    onPlayGame: (Int) -> Unit,
    onBack: () -> Unit,
    onBuyTrack: (Int) -> Unit,
    onBuyAlbum: () -> Unit,
    onUpdateMeta: (newPoints: Int, newUnlockedCount: Int, newMode: String, newCompletionsCsv: String) -> Unit,
    customBgTheme: String = "OBSIDIAN",
    customHeroSkin: String = "SOLAR",
    customEnemyStyle: String = "BALANCED",
    unlockedItemsCsv: String = "",
    isVipUnlocked: Boolean = false,
    onUnlockShopItem: (itemId: String, cost: Int) -> Unit = { _, _ -> },
    onWatchAdForNotes: () -> Unit = {},
    onActivateVipPasscode: () -> Unit = {},
    onUpdateCustomization: (bg: String, hero: String, enemy: String) -> Unit = { _, _, _ -> },
    heroSolarMandala: ImageBitmap,
    heroCyberMandala: ImageBitmap,
    heroPrismMandala: ImageBitmap,
    enemyPulsarMandala: ImageBitmap,
    enemySeekerMandala: ImageBitmap,
    enemyWandererMandala: ImageBitmap,
    btnPillGreen: ImageBitmap,
    btnPillCyan: ImageBitmap,
    btnPillGold: ImageBitmap,
    btnIconClose: ImageBitmap
) {
    val context = LocalContext.current
    val album = remember { MusicLibrary.placeholderAlbum() }
    val tracks = album.tracks
    val totalTracks = MusicLibrary.TOTAL_TRACKS

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Album, 1 = Shop & Customizer
    var selectedTrackIndexLocal by remember(selectedTrackIndexState) {
        mutableIntStateOf(selectedTrackIndexState.coerceIn(1, totalTracks))
    }
    var isPlaying by remember { mutableStateOf(false) }

    var localPoints by remember(pulsePoints) { mutableIntStateOf(pulsePoints) }
    var localUnlocked by remember(unlockedTrackCount) {
        mutableIntStateOf(unlockedTrackCount.coerceIn(1, totalTracks))
    }

    LaunchedEffect(pulsePoints) { localPoints = pulsePoints }
    LaunchedEffect(unlockedTrackCount) { localUnlocked = unlockedTrackCount.coerceIn(1, totalTracks) }

    var localMode by remember(trackListMode) {
        mutableStateOf(
            runCatching { TrackListMode.valueOf(trackListMode) }.getOrElse { TrackListMode.LIST }
        )
    }

    val completionCounts by remember(trackCompletionsCsv) { mutableStateOf(parseCompletionsCsv(trackCompletionsCsv)) }
    val completedTotal = remember(completionCounts) {
        (0 until totalTracks).count { completionCounts[it] > 0 }
    }

    var simPlayheadMs by remember { mutableIntStateOf(0) }
    var simDurationMs by remember { mutableIntStateOf(219_000) }

    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var realDurationMs by remember { mutableIntStateOf(0) }
    var realPlayheadMs by remember { mutableIntStateOf(0) }

    val selected = tracks[(selectedTrackIndexLocal - 1).coerceIn(0, tracks.lastIndex)]
    val selectedAsset = remember(selectedTrackIndexLocal) { resolveTrackAsset(context, selectedTrackIndexLocal) }
    val usingRealAudio = selectedAsset.rawAudioId != 0

    val durationMs = if (usingRealAudio && realDurationMs > 0) realDurationMs else (selected.durationSec * 1000).coerceAtLeast(1)
    val playheadMs = if (usingRealAudio) realPlayheadMs.coerceAtLeast(0) else simPlayheadMs.coerceAtLeast(0)

    val isSelectedCompleted = completionCounts[(selectedTrackIndexLocal - 1).coerceIn(0, totalTracks - 1)] > 0 || isVipUnlocked
    val targetReveal = if (isSelectedCompleted) 1.0f else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetReveal,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "reveal"
    )

    fun pushMeta() {
        onUpdateMeta(
            localPoints,
            localUnlocked,
            localMode.name,
            toCompletionsCsv(completionCounts)
        )
    }

    fun isPlayable(track: DemoTrack): Boolean = track.index <= localUnlocked || isVipUnlocked
    fun isArtworkUnlocked(track: DemoTrack): Boolean = completionCounts[(track.index - 1).coerceIn(0, totalTracks - 1)] > 0 || isVipUnlocked

    fun tryUnlock(track: DemoTrack) {
        if (isPlayable(track)) return
        if (track.index != localUnlocked + 1) return
        if (localPoints < track.unlockCost) return
        localPoints -= track.unlockCost
        localUnlocked = track.index
        pushMeta()
    }

    fun stopPlayback() {
        isPlaying = false
        player?.runCatching {
            setOnCompletionListener(null)
            stop()
            release()
        }
        player = null
        realPlayheadMs = 0
        realDurationMs = 0
        simPlayheadMs = 0
    }

    fun startPlayback() {
        if (!isSelectedCompleted) return

        if (usingRealAudio) {
            stopPlayback()
            val mp = MediaPlayer.create(context, selectedAsset.rawAudioId)
            if (mp == null) {
                simPlayheadMs = 0
                simDurationMs = selected.durationSec * 1000
                isPlaying = true
                return
            }
            player = mp
            realDurationMs = runCatching { mp.duration }.getOrDefault(selected.durationSec * 1000)
            realPlayheadMs = 0
            mp.setOnCompletionListener {
                stopPlayback()
            }
            mp.start()
            isPlaying = true
        } else {
            stopPlayback()
            simPlayheadMs = 0
            simDurationMs = selected.durationSec * 1000
            isPlaying = true
        }
    }

    LaunchedEffect(isPlaying, selectedTrackIndexLocal) {
        while (isPlaying) {
            delay(200)
            if (usingRealAudio) {
                val mp = player
                if (mp != null) {
                    realPlayheadMs = runCatching { mp.currentPosition }.getOrDefault(realPlayheadMs)
                    realDurationMs = runCatching { mp.duration }.getOrDefault(realDurationMs)
                }
            } else {
                simPlayheadMs += 200
                if (simPlayheadMs >= simDurationMs) {
                    simPlayheadMs = simDurationMs
                    isPlaying = false
                }
            }
        }
    }

    LaunchedEffect(selectedTrackIndexLocal) {
        onSelectTrack(selectedTrackIndexLocal)
        stopPlayback()
    }

    DisposableEffect(Unit) {
        onDispose { stopPlayback() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03060EL),
                        Color(0xFF050B18L),
                        Color(0xFF020408L)
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar: Pill Switcher (ALBUM | SHOP & VAULT) + Music Notes Pill + Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF060C18L))
                        .border(1.dp, Color(0xFF6366F1L).copy(alpha = 0.38f), RoundedCornerShape(50))
                        .padding(3.dp)
                ) {
                    val albumSelected = activeTab == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (albumSelected) {
                                    Brush.verticalGradient(listOf(Color(0xFF22C55EL), Color(0xFF15803DL)))
                                } else {
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { activeTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ALBUM",
                            color = if (albumSelected) Color.White else Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    val custSelected = activeTab == 1
                    Box(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (custSelected) {
                                    Brush.verticalGradient(listOf(Color(0xFF6366F1L), Color(0xFF4338CAL)))
                                } else {
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { activeTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SHOP & VAULT",
                            color = if (custSelected) Color.White else Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sleek Music Notes (♪) Balance Pill
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF061A10L), Color(0xFF030D08L))
                            )
                        )
                        .border(1.2.dp, Color(0xFF22C55EL).copy(alpha = 0.65f), RoundedCornerShape(50))
                        .padding(horizontal = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%,d ♪".format(localPoints),
                        color = Color(0xFF4ADE80L),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                ArcadeIconButton(
                    iconBitmap = btnIconClose,
                    onClick = onBack,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab 1: Locked Fan Shop, Customizer & Artist Vault
            if (activeTab == 1) {
                StudioCustomizerSuite(
                    customBgTheme = customBgTheme,
                    customHeroSkin = customHeroSkin,
                    customEnemyStyle = customEnemyStyle,
                    pulsePoints = localPoints,
                    unlockedItemsCsv = unlockedItemsCsv,
                    isVipUnlocked = isVipUnlocked,
                    onUnlockShopItem = onUnlockShopItem,
                    onWatchAdForNotes = onWatchAdForNotes,
                    onActivateVipPasscode = onActivateVipPasscode,
                    onUpdateCustomization = onUpdateCustomization,
                    onLaunchTestGame = {
                        stopPlayback()
                        onPlayGame(selectedTrackIndexLocal)
                    },
                    nebulaBitmap = nebula,
                    heroSolarMandala = heroSolarMandala,
                    heroCyberMandala = heroCyberMandala,
                    heroPrismMandala = heroPrismMandala,
                    enemyPulsarMandala = enemyPulsarMandala,
                    enemySeekerMandala = enemySeekerMandala,
                    enemyWandererMandala = enemyWandererMandala,
                    btnPillGreen = btnPillGreen,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Artist Socials & Streaming Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    SocialBrandIconRow(
                        links = MusicLibrary.officialSocialLinks,
                        showLabels = true,
                        onOpenUrl = { url ->
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Featured Track Deck (Glassmorphic Beveled Card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = Color(0xFF6366F1L), spotColor = Color(0xFF22C55EL))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xF2081226L),
                                    Color(0xF5050B18L),
                                    Color(0xFA02050BL)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4ADE80L).copy(alpha = 0.72f),
                                    Color(0xFF6366F1L).copy(alpha = 0.45f)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelectedCompleted) 2.dp else 1.2.dp,
                                        color = if (isSelectedCompleted) Color(0xFF22C55EL) else Color(0xFF6366F1L).copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                TrackRevealOverlay(
                                    trackIndex = selected.index,
                                    progress = animatedProgress,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF6366F1L).copy(alpha = 0.22f))
                                            .border(1.dp, Color(0xFF818CF8L).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "TRACK %02d • ${selected.bpm.toInt()} BPM".format(selected.index),
                                            color = Color(0xFFA5B4FCL),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "UNLOCKED $localUnlocked/$totalTracks",
                                        color = Color(0xFF4ADE80L),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = selected.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = album.artistName,
                                    color = Color(0xFFA5B4FCL),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                val compCount = completionCounts[(selected.index - 1).coerceIn(0, totalTracks - 1)]
                                val statusText = when {
                                    compCount > 0 || isVipUnlocked -> "MASTER ARTWORK UNLOCKED • READY TO LISTEN"
                                    isPlayable(selected) -> "PLAYABLE • BEAT TRACK TO REVEAL ART"
                                    else -> "LOCKED • ${selected.unlockCost} ♪ NOTES TO UNLOCK"
                                }
                                val statusColor = when {
                                    compCount > 0 || isVipUnlocked -> Color(0xFF4ADE80L)
                                    isPlayable(selected) -> Color(0xFFA5B4FCL)
                                    else -> Color.White.copy(alpha = 0.55f)
                                }
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${formatTrackTime(playheadMs / 1000)} / ${formatTrackTime(durationMs / 1000)} • ART REVEALED: $completedTotal/$totalTracks",
                                    color = Color.White.copy(alpha = 0.60f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Song Section & Extra Juice Timeline Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            selected.sections.forEach { sec ->
                                val isJuice = sec.juiceTier == JuiceTier.EXTRA_JUICE || sec.juiceTier == JuiceTier.SLIGHT_EXTRA_JUICE
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isJuice) Color(0xFF16A34AL).copy(alpha = 0.24f)
                                            else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isJuice) Color(0xFF4ADE80L).copy(alpha = 0.70f)
                                            else Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = (if (isJuice) "⚡ " else "") + "${formatTrackTime(sec.timestampSec.toInt())} ${sec.label}",
                                        color = if (isJuice) Color(0xFF4ADE80L) else Color.White.copy(alpha = 0.70f),
                                        fontSize = 8.sp,
                                        fontWeight = if (isJuice) FontWeight.Black else FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons Row
                        if (!isPlayable(selected)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (selected.index == localUnlocked + 1 && localPoints >= selected.unlockCost) {
                                    ArcadePillButton(
                                        text = "UNLOCK (${selected.unlockCost} ♪)",
                                        pillBitmap = btnPillGold,
                                        onClick = { tryUnlock(selected) },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        fontSize = 11.sp
                                    )
                                } else {
                                    ArcadePillButton(
                                        text = "BUY TRACK ($0.99)",
                                        pillBitmap = btnPillGold,
                                        onClick = { onBuyTrack(selected.index) },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                ArcadePillButton(
                                    text = "UNLOCK ALL ($1.99)",
                                    pillBitmap = btnPillCyan,
                                    onClick = { onBuyAlbum() },
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    fontSize = 11.sp
                                )
                            }
                        } else if (isSelectedCompleted) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ArcadePillButton(
                                    text = if (isPlaying) "PAUSE" else "LISTEN",
                                    pillBitmap = btnPillGreen,
                                    onClick = { if (isPlaying) stopPlayback() else startPlayback() },
                                    modifier = Modifier.weight(0.95f).height(42.dp),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ArcadePillButton(
                                    text = "PLAY ${selected.title.uppercase()}",
                                    pillBitmap = btnPillCyan,
                                    onClick = {
                                        stopPlayback()
                                        onPlayGame(selectedTrackIndexLocal)
                                    },
                                    modifier = Modifier.weight(1.25f).height(42.dp),
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ArcadePillButton(
                                    text = "PLAY TO UNLOCK ART",
                                    pillBitmap = btnPillGreen,
                                    onClick = {
                                        stopPlayback()
                                        onPlayGame(selectedTrackIndexLocal)
                                    },
                                    modifier = Modifier.weight(1.3f).height(42.dp),
                                    fontSize = 12.sp
                                )
                                if (localUnlocked < totalTracks) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ArcadePillButton(
                                        text = "UNLOCK ALL",
                                        pillBitmap = btnPillGold,
                                        onClick = { onBuyAlbum() },
                                        modifier = Modifier.weight(0.9f).height(42.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // FIXED Waveform & Un-crunched List/Grid Header Bar!
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "TRACKS ($localUnlocked/$totalTracks UNLOCKED)",
                        color = Color(0xFF4ADE80L),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        modifier = Modifier
                            .width(126.dp)
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF060C18L))
                            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .padding(2.dp)
                    ) {
                        val isList = localMode == TrackListMode.LIST
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    if (isList) Brush.verticalGradient(listOf(Color(0xFF22C55EL), Color(0xFF15803DL)))
                                    else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { localMode = TrackListMode.LIST; pushMeta() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LIST",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        val isGrid = localMode == TrackListMode.GRID
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    if (isGrid) Brush.verticalGradient(listOf(Color(0xFF22C55EL), Color(0xFF15803DL)))
                                    else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { localMode = TrackListMode.GRID; pushMeta() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "GRID",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                AudioWaveformVisualizer(
                    isPlaying = isPlaying,
                    progress = if (isPlaying) (playheadMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else animatedProgress
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Track Collection List/Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (localMode == TrackListMode.LIST) {
                        TrackListList(
                            modifier = Modifier.fillMaxSize(),
                            tracks = tracks,
                            selectedTrackIndex = selectedTrackIndexLocal,
                            isPlayable = { isPlayable(it) },
                            isArtworkUnlocked = { isArtworkUnlocked(it) },
                            onSelect = { selectedTrackIndexLocal = it }
                        )
                    } else {
                        TrackListGrid(
                            modifier = Modifier.fillMaxSize(),
                            tracks = tracks,
                            selectedTrackIndex = selectedTrackIndexLocal,
                            isPlayable = { isPlayable(it) },
                            isArtworkUnlocked = { isArtworkUnlocked(it) },
                            onSelect = { selectedTrackIndexLocal = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // UPCOMING RELEASE RADAR / PRESAVE BANNER (For Ethan to announce new singles, shows & presaves!)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E1B4BL), Color(0xFF061A10L))
                            )
                        )
                        .border(1.dp, Color(0xFFFACC15L).copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .clickable {
                            runCatching {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://open.spotify.com/artist/2Cy1rpHuiZvU5ymbOQfD37?si=4z4vjdReTGCpYG-RhEn2sA")
                                )
                                context.startActivity(intent)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "★ UPCOMING RELEASE RADAR • NEW MUSIC & SHOWS",
                            color = Color(0xFFFACC15L),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Tap to follow No Morē Illusīons on Spotify & unlock exclusive Vault drops in the Shop tab.",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFFACC15L))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FOLLOW",
                            color = Color(0xFF030712L),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
