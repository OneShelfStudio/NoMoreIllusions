package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class GameOrbCollectionTest {

    @Test
    fun testSpawnStationaryBeatOrbsForFrame_preservesMidFlightPegsAndSuppressesNewSpawnsWhenSustainRibbonActive() {
        val chart = listOf(
            BeatOrbCue(sequenceIndex = 0, timestampSec = 0.5f, normX = 0.5f, normY = 0.5f, powerUpType = PowerUpType.NONE),
            BeatOrbCue(sequenceIndex = 1, timestampSec = 1.0f, normX = 0.5f, normY = 0.5f, powerUpType = PowerUpType.NONE),
            BeatOrbCue(sequenceIndex = 2, timestampSec = 1.5f, normX = 0.5f, normY = 0.5f, powerUpType = PowerUpType.NONE)
        )
        val powerUps = mutableListOf<GameObject>(
            GameObject(x = 0f, y = 0f, radius = 32f, color = Color.Red),
            GameObject(x = 10f, y = 10f, radius = 32f, color = Color.Blue)
        )
        val shockwaves = mutableListOf<Shockwave>()

        val nextCueIdx = spawnStationaryBeatOrbsForFrame(
            chart = chart,
            startCueIdx = 0,
            trackSec = 0.8f,
            spawnLeadTime = 1.0f,
            ttlGraceSec = 0.5f,
            screenWidth = 1080f,
            screenHeight = 1920f,
            isGoldenActive = false,
            applyColorBlind = { it },
            powerUps = powerUps,
            shockwaves = shockwaves,
            hasActiveSustainRibbon = true,
            isTrainingMode = false
        )

        assertEquals("Mid-flight powerUps must be preserved when hasActiveSustainRibbon is true", 2, powerUps.size)
        assertTrue("nextCueIdx should advance past cues that would have spawned", nextCueIdx > 0)
    }

    @Test
    fun testSpawnStationaryBeatOrbsForFrame_spawnsNormallyWhenNoSustainRibbon() {
        val chart = listOf(
            BeatOrbCue(timestampSec = 1.0f, normX = 0.5f, normY = 0.5f, powerUpType = PowerUpType.NONE, sequenceIndex = 0)
        )
        val powerUps = mutableListOf<GameObject>()
        val shockwaves = mutableListOf<Shockwave>()

        val nextCueIdx = spawnStationaryBeatOrbsForFrame(
            chart = chart,
            startCueIdx = 0,
            trackSec = 0.2f,
            spawnLeadTime = 1.0f,
            ttlGraceSec = 0.5f,
            screenWidth = 1080f,
            screenHeight = 1920f,
            isGoldenActive = false,
            applyColorBlind = { it },
            powerUps = powerUps,
            shockwaves = shockwaves,
            hasActiveSustainRibbon = false,
            isTrainingMode = false
        )

        assertTrue("powerUps list should not be empty, it should have spawned the cue", powerUps.isNotEmpty())
        assertEquals("nextCueIdx should advance past the spawned cue", 1, nextCueIdx)
    }

    @Test
    fun testStepNebulousBeatOrbs_overlappingOrbsPassThroughCleanlyWithoutSwingOrFling() {
        val orbA = GameObject(
            x = 540f, y = 800f, radius = 32f, color = Color.Cyan,
            anchorX = 540f, anchorY = 1200f,
            spawnX = 540f, spawnY = -48f, exitX = 540f, exitY = 1985f,
            spawnBeatSec = 0f, targetBeatSec = 2.0f, expireBeatSec = 3.8f
        )
        val orbB = GameObject(
            x = 545f, y = 805f, radius = 32f, color = Color.Magenta,
            anchorX = 545f, anchorY = 1200f,
            spawnX = 545f, spawnY = -48f, exitX = 545f, exitY = 1985f,
            spawnBeatSec = 0.05f, targetBeatSec = 2.05f, expireBeatSec = 3.85f
        )
        val orbs = mutableListOf(orbA, orbB)
        val player = GameObject(x = 100f, y = 1600f, radius = 26f, color = Color.White)

        stepNebulousBeatOrbs(
            powerUps = orbs,
            trackSec = 1.0f,
            gameTime = 1.0f,
            currentScale = 1.0f,
            screenWidth = 1080f,
            screenHeight = 1920f,
            player = player,
            magnetActive = false
        )

        assertEquals("Orb A bounceOffsetX must remain zero (pass-through)", 0f, orbA.bounceOffsetX, 0.001f)
        assertEquals("Orb A bounceOffsetY must remain zero (pass-through)", 0f, orbA.bounceOffsetY, 0.001f)
        assertEquals("Orb B bounceOffsetX must remain zero (pass-through)", 0f, orbB.bounceOffsetX, 0.001f)
        assertEquals("Orb B bounceOffsetY must remain zero (pass-through)", 0f, orbB.bounceOffsetY, 0.001f)
    }

    @Test
    fun testCountPlayableChartOrbs_matchesExactSpawnedOrbsAndEnforcesCatchableHorizontalReach() {
        val chart = listOf(
            BeatOrbCue(sequenceIndex = 0, timestampSec = 1.0f, normX = 0.10f, normY = 0.62f, powerUpType = PowerUpType.NONE),
            BeatOrbCue(sequenceIndex = 1, timestampSec = 1.3f, normX = 0.95f, normY = 0.62f, powerUpType = PowerUpType.NONE),
            BeatOrbCue(sequenceIndex = 2, timestampSec = 1.8f, normX = 0.20f, normY = 0.62f, powerUpType = PowerUpType.NONE),
            BeatOrbCue(sequenceIndex = 3, timestampSec = 3.0f, normX = 0.60f, normY = 0.62f, powerUpType = PowerUpType.ELECTRIC_HARMONIC),
            BeatOrbCue(sequenceIndex = 4, timestampSec = 4.0f, normX = 0.50f, normY = 0.62f, powerUpType = PowerUpType.NONE)
        )
        val expectedCount = countPlayableChartOrbs(chart, isTrainingMode = false)
        assertEquals(5, expectedCount)

        val spawnedAll = mutableListOf<GameObject>()
        val active = mutableListOf<GameObject>()
        val shockwaves = mutableListOf<Shockwave>()
        var cueIdx = 0
        var t = 0f
        while (t <= 5.0f) {
            if (t < 1.50f) {
                assertFalse(
                    "Special swell orb at 3.0s should not spawn prematurely before 1.55s",
                    active.any { it.powerUpType == PowerUpType.ELECTRIC_HARMONIC }
                )
            }
            val beforeSize = active.size
            cueIdx = spawnStationaryBeatOrbsForFrame(
                chart = chart,
                startCueIdx = cueIdx,
                trackSec = t,
                spawnLeadTime = 2.0f,
                ttlGraceSec = 0.5f,
                screenWidth = 1080f,
                screenHeight = 1920f,
                isGoldenActive = false,
                applyColorBlind = { it },
                powerUps = active,
                shockwaves = shockwaves,
                hasActiveSustainRibbon = false,
                isTrainingMode = false
            )
            if (active.size > beforeSize) {
                spawnedAll.addAll(active.subList(beforeSize, active.size))
            }
            t += 0.1f
        }

        assertEquals("Total spawned orbs must match countPlayableChartOrbs for 100% completion accuracy", expectedCount, spawnedAll.size)
        val jump01 = abs(spawnedAll[1].anchorX - spawnedAll[0].anchorX)
        assertTrue("Rapid consecutive horizontal jump should be ergonomically bounded (< 540px), was $jump01", jump01 <= 541f)
        assertNull("Charted regular notes must not have stageFxDrop set", spawnedAll[0].stageFxDrop)
    }

    @Test
    fun testMultiSongExpansionAndStageFxDropOrbs_supportUpcomingTracksAndSmoothStageFxFlight() {
        for (trackIdx in listOf(1, 2)) {
            val track = MusicLibrary.getTrack(trackIdx)
            val beatDur = 60f / track.bpm
            for (training in listOf(false, true)) {
                val expectedTotal = countPlayableChartOrbs(track.orbChart, isTrainingMode = training)
                val spawnedAll = mutableListOf<GameObject>()
                val active = mutableListOf<GameObject>()
                val shockwaves = mutableListOf<Shockwave>()
                val player = GameObject(x = 540f, y = 1200f, radius = 20f, color = Color.White)
                var cueIdx = 0
                var t = -1.8f
                val endT = track.durationSec + 4f
                while (t <= endT) {
                    val beforeIds = active.map { it.sequenceIndex }.toSet()
                    cueIdx = spawnStationaryBeatOrbsForFrame(
                        chart = track.orbChart,
                        startCueIdx = cueIdx,
                        trackSec = t,
                        spawnLeadTime = 2.0f,
                        ttlGraceSec = 0.4f,
                        screenWidth = 1080f,
                        screenHeight = 1920f,
                        isGoldenActive = false,
                        applyColorBlind = { it },
                        powerUps = active,
                        shockwaves = shockwaves,
                        hasActiveSustainRibbon = false,
                        isTrainingMode = training,
                        beatDurationSec = beatDur
                    )
                    for (orb in active) {
                        if (orb.sequenceIndex !in beforeIds) {
                            spawnedAll.add(orb.copy())
                        }
                    }
                    stepNebulousBeatOrbs(
                        powerUps = active,
                        player = player,
                        gameTime = (t + 2f).coerceAtLeast(0f),
                        trackSec = t,
                        screenWidth = 1080f,
                        screenHeight = 1920f
                    )
                    t += (1f / 30f)
                }

                assertEquals(
                    "Track $trackIdx (training=$training): spawned count must match countPlayableChartOrbs",
                    expectedTotal,
                    spawnedAll.size
                )
            }
        }

        // Verify plug-and-play multi-song builder generates a complete Track 03 with sections, orbs & unique pressing edition
        val futureTrack3 = MusicLibrary.createOfficialReleaseTrack(
            index = 3,
            title = "Illusions Unbound",
            bpm = 112f,
            durationSec = 185,
            unlockCost = 1800
        )
        assertTrue("Generated Track 03 must have >= 4 choreographed sections", futureTrack3.sections.size >= 4)
        assertTrue("Generated Track 03 must have >= 80 beat orbs", futureTrack3.orbChart.size >= 80)
        assertTrue("Upcoming release slots must advertise Track 03+", MusicLibrary.upcomingReleaseSlots.isNotEmpty())
        val uniqueEditions = (1..6).map { MusicLibrary.pressingEditionForTrack(it).editionTitle }.toSet()
        assertEquals("Tracks 1..6 must each get a distinct Collector's Vinyl Pressing Edition", 6, uniqueEditions.size)

        // Verify StageFxBackdrop has 6 distinct visual effects & player-dropped FX orb glides smoothly into hover pocket
        assertEquals(6, StageFxBackdrop.entries.size)
        assertEquals(5, RippleWaveStyle.entries.size)
        val fxOrb = GameObject(
            x = 540f,
            y = -56f,
            radius = 36f,
            color = Color(StageFxBackdrop.KALEIDOSCOPE_MANDALA.accentHex),
            anchorX = 540f,
            anchorY = 768f,
            spawnBeatSec = 10.0f,
            targetBeatSec = 11.45f,
            expireBeatSec = 14.30f,
            phraseBeatNum = 2,
            sequenceIndex = 903,
            spawnX = 540f,
            spawnY = -56f,
            exitX = 540f,
            exitY = 1996f,
            stageFxDrop = StageFxBackdrop.KALEIDOSCOPE_MANDALA
        )
        val fxList = mutableListOf(fxOrb)
        val dummyPlayer = GameObject(x = 540f, y = 1248f, radius = 18f, color = Color.Green)
        stepNebulousBeatOrbs(fxList, dummyPlayer, gameTime = 11.45f, trackSec = 11.45f, screenWidth = 1080f, screenHeight = 1920f)
        assertEquals("Stage FX Drop Orb must ease right into its hover pocket (768f) at targetBeatSec", 768f, fxOrb.y, 12f)
    }

    @Test
    fun testConfideFullTimeline_zeroTeleportDiscontinuityAndMultiDirectionalAndSpecialMilestones() {
        val track = MusicLibrary.getTrack(1)
        val beatDur = 60f / track.bpm
        val active = mutableListOf<GameObject>()
        val shockwaves = mutableListOf<Shockwave>()
        val player = GameObject(x = 540f, y = 1200f, radius = 20f, color = Color.White)
        val prevPositions = HashMap<Int, Pair<Float, Float>>()
        val spawnedSnapshots = mutableListOf<GameObject>()

        var cueIdx = 0
        var t = -1.8f
        val dt = 1f / 60f
        var maxStepJump = 0f
        var maxJumpInfo = ""

        while (t <= 220.0f) {
            val existingIds = active.map { it.sequenceIndex }.toSet()
            cueIdx = spawnStationaryBeatOrbsForFrame(
                chart = track.orbChart,
                startCueIdx = cueIdx,
                trackSec = t,
                spawnLeadTime = 2.0f,
                ttlGraceSec = 0.4f,
                screenWidth = 1080f,
                screenHeight = 1920f,
                isGoldenActive = false,
                applyColorBlind = { it },
                powerUps = active,
                shockwaves = shockwaves,
                hasActiveSustainRibbon = false,
                isTrainingMode = false,
                beatDurationSec = beatDur
            )
            stepNebulousBeatOrbs(
                powerUps = active,
                player = player,
                gameTime = (t + 1.5f).coerceAtLeast(0f),
                trackSec = t,
                screenWidth = 1080f,
                screenHeight = 1920f
            )

            for (orb in active) {
                if (orb.sequenceIndex !in existingIds) {
                    spawnedSnapshots.add(orb.copy())
                } else {
                    val prev = prevPositions[orb.sequenceIndex]
                    if (prev != null) {
                        val jump = kotlin.math.hypot(orb.x - prev.first, orb.y - prev.second)
                        if (jump > maxStepJump) {
                            maxStepJump = jump
                            maxJumpInfo = "seq=${orb.sequenceIndex} at t=$t jump=$jump"
                        }
                    }
                }
                prevPositions[orb.sequenceIndex] = orb.x to orb.y
            }
            t += dt
        }

        // At 60 FPS on a 1080x1920 screen, smooth movement is ~10-20px/frame; any teleport glitch would be >45px/frame!
        assertTrue("Zero orb teleportation allowed across entire 219s song, max frame step was $maxJumpInfo", maxStepJump <= 34f)

        // Verify multi-directional entry: top-down intro orbs (first 3 spawn from top edge) + left/right flanks later!
        val introOrbs = spawnedSnapshots.take(3)
        assertEquals(3, introOrbs.size)
        assertTrue(
            "First 3 intro orbs must spawn from the top edge inside [180f, 900f], were ${introOrbs.map { it.spawnX to it.spawnY }}",
            introOrbs.all { it.spawnY < 0f && it.spawnX in 180f..900f }
        )
        assertTrue("Very first intro orb must spawn before -1.5s for 1.8s pre-music drift, was ${introOrbs.first().spawnBeatSec}", introOrbs.first().spawnBeatSec < -1.5f)
        assertTrue("Must spawn orbs entering from left flank", spawnedSnapshots.any { it.spawnX < 0f })
        assertTrue("Must spawn orbs entering from right flank", spawnedSnapshots.any { it.spawnX > 1080f })
        assertTrue("Must spawn orbs entering from top perimeter", spawnedSnapshots.any { it.spawnY < 0f })

        // Verify 0:15 (Tier 1) and 1:17.4 (~1:18, Tier 2) Special 24K Orbs arrive near top-center!
        val milestone15 = spawnedSnapshots.firstOrNull { abs(it.targetBeatSec - 15.0f) <= 0.6f && it.goldenTier == 1 }
        assertNotNull("0:15 Special 24K Milestone Orb (Tier 1) must exist", milestone15)
        assertEquals(540f, milestone15!!.anchorX, 2f)
        assertTrue("0:15 Special 24K Orb must sit near top-middle (anchorY <= 34% height), was ${milestone15.anchorY}", milestone15.anchorY <= 1920f * 0.34f)

        val milestone78 = spawnedSnapshots.firstOrNull { abs(it.targetBeatSec - 77.42f) <= 0.8f && it.goldenTier == 2 }
        assertNotNull("1:17.4 (~1:18) Special 24K Milestone Orb (Tier 2) must exist", milestone78)
        assertEquals(540f, milestone78!!.anchorX, 2f)
        assertTrue("1:18 Special 24K Orb must sit near top-middle (anchorY <= 34% height)", milestone78.anchorY <= 1920f * 0.34f)

        // Verify flowy/whispy slow-mo swirl orbs around 0:45-0:49.8 ("losing your battles")
        val flowyOrbs = spawnedSnapshots.filter { it.targetBeatSec in 44.9f..49.85f }
        assertTrue("Flowy breakdown at 0:45-0:49.8 must have swirling slow-mo orbs", flowyOrbs.isNotEmpty() && flowyOrbs.all { it.swirlStrength >= 0.9f && (it.targetBeatSec - it.spawnBeatSec) >= 2.5f })

        // Verify surprise false-ending pegs after 212.4s in Confide and after 159.5s in Number One!
        val lateSurpriseConfide = track.orbChart.filter { it.timestampSec >= 212.4f }
        assertTrue("Confide must include >= 3 surprise late pegs after 212.4s, found ${lateSurpriseConfide.size}", lateSurpriseConfide.size >= 3)
        val lateSurpriseNumberOne = MusicLibrary.getTrack(2).orbChart.filter { it.timestampSec >= 159.5f }
        assertTrue("Number One must include >= 3 surprise late pegs after 159.5s, found ${lateSurpriseNumberOne.size}", lateSurpriseNumberOne.size >= 3)

        // Verify automatic Interlude Stage FX crossfades for Confide & Number One
        val interlude1A = resolveChoreographedInterludeStageFx(1, 61.0f, 219)
        val interlude1B = resolveChoreographedInterludeStageFx(1, 67.0f, 219)
        assertEquals(StageFxBackdrop.AURORA_SILK, interlude1A?.backdrop)
        assertEquals(StageFxBackdrop.KALEIDOSCOPE_MANDALA, interlude1B?.backdrop)
        assertNull("Interlude 1 must snap back cleanly at 69.75s when drums re-enter", resolveChoreographedInterludeStageFx(1, 69.75f, 219))

        val interlude2A = resolveChoreographedInterludeStageFx(1, 158.0f, 219)
        val interlude2B = resolveChoreographedInterludeStageFx(1, 163.0f, 219)
        val interlude2C = resolveChoreographedInterludeStageFx(1, 168.0f, 219)
        assertEquals(StageFxBackdrop.PRISM_CAUSTICS, interlude2A?.backdrop)
        assertEquals(StageFxBackdrop.SOLAR_ECLIPSE, interlude2B?.backdrop)
        assertEquals(StageFxBackdrop.NEBULA_VORTEX, interlude2C?.backdrop)
        assertNull("Interlude 2 must snap back cleanly at 170.40s when Ethan sings 'I'", resolveChoreographedInterludeStageFx(1, 170.40f, 219))

        assertNotNull(resolveChoreographedInterludeStageFx(2, 56.0f, 166))
        assertNull(resolveChoreographedInterludeStageFx(2, 63.50f, 166))
        assertNotNull(resolveChoreographedInterludeStageFx(2, 122.0f, 166))
        assertNull(resolveChoreographedInterludeStageFx(2, 130.30f, 166))
    }

    @Test
    fun testPreMusicDriftToLiveAudioHandoff_startsTrackAtZeroSecAndNeverFreezesFirstOrb() {
        val track = MusicLibrary.getTrack(1)
        val beatDur = 60f / track.bpm
        val active = mutableListOf<GameObject>()
        val shockwaves = mutableListOf<Shockwave>()
        val player = GameObject(x = 540f, y = 1248f, radius = 18f, color = Color.Green)

        var isPreMusicDrift = true
        var preMusicElapsed = 0f
        var isAudioRunning = false
        var simulatedAudioSec = 0f
        var startTrackTriggeredCount = 0
        var cueIdx = 0
        val dt = 1f / 60f

        val firstSpawnedSeq = track.orbChart.first().sequenceIndex * 2 + 1
        var firstOrbPosAtMinus12Sec: Pair<Float, Float>? = null
        var firstOrbPosAtZeroSec: Pair<Float, Float>? = null
        var firstOrbPosAtPlusHalfSec: Pair<Float, Float>? = null
        var firstOrbSpawnedSnapshot: GameObject? = null

        // Simulate 240 frames (4.0 seconds: -1.8s pre-music drift -> 0.0s handoff -> +2.2s live music)
        repeat(240) {
            if (isAudioRunning) {
                simulatedAudioSec += dt
            }
            val step = stepPreMusicAndTrackClock(
                isPreMusicDrift = isPreMusicDrift,
                preMusicElapsed = preMusicElapsed,
                dt = dt,
                isAudioRunning = isAudioRunning,
                rawTrackSec = simulatedAudioSec
            )
            preMusicElapsed = step.preMusicElapsed
            isPreMusicDrift = step.isPreMusicDrift
            if (step.shouldStartTrackNow) {
                startTrackTriggeredCount++
                isAudioRunning = true
            }
            val trackSec = if (isAudioRunning) simulatedAudioSec else step.trackSec

            cueIdx = spawnStationaryBeatOrbsForFrame(
                chart = track.orbChart,
                startCueIdx = cueIdx,
                trackSec = trackSec,
                spawnLeadTime = 2.0f,
                ttlGraceSec = 0.4f,
                screenWidth = 1080f,
                screenHeight = 1920f,
                isGoldenActive = false,
                applyColorBlind = { it },
                powerUps = active,
                shockwaves = shockwaves,
                hasActiveSustainRibbon = false,
                isTrainingMode = false,
                beatDurationSec = beatDur
            )
            stepNebulousBeatOrbs(
                powerUps = active,
                player = player,
                gameTime = (trackSec + 1.8f).coerceAtLeast(0f),
                trackSec = trackSec,
                screenWidth = 1080f,
                screenHeight = 1920f
            )

            val firstOrb = active.firstOrNull { it.sequenceIndex == firstSpawnedSeq }
            if (firstOrb != null) {
                if (firstOrbSpawnedSnapshot == null) {
                    firstOrbSpawnedSnapshot = firstOrb.copy()
                }
                if (firstOrbPosAtMinus12Sec == null && trackSec >= -1.2f) {
                    firstOrbPosAtMinus12Sec = firstOrb.x to firstOrb.y
                }
                if (firstOrbPosAtZeroSec == null && trackSec >= 0.0f) {
                    firstOrbPosAtZeroSec = firstOrb.x to firstOrb.y
                }
                if (firstOrbPosAtPlusHalfSec == null && trackSec >= 0.5f) {
                    firstOrbPosAtPlusHalfSec = firstOrb.x to firstOrb.y
                }
            }
        }

        assertEquals("startTrack must trigger exactly once when preMusicElapsed reaches 1.8s", 1, startTrackTriggeredCount)
        assertFalse("isPreMusicDrift must clear to false after 1.8s handoff", isPreMusicDrift)
        assertTrue("Simulated audio clock must advance smoothly past 2.0s", simulatedAudioSec >= 2.0f)
        assertNotNull("First orb must spawn during early pre-music drift", firstOrbSpawnedSnapshot)
        assertTrue("First orb must spawn from top edge (spawnY < 0f)", firstOrbSpawnedSnapshot!!.spawnY < 0f)
        assertNotNull("First orb must already be falling on screen at -1.2s", firstOrbPosAtMinus12Sec)
        assertNotNull("First orb must reach 0.0s handoff without freezing", firstOrbPosAtZeroSec)
        assertNotNull("First orb must keep moving past +0.5s after music starts", firstOrbPosAtPlusHalfSec)
        val distPreToZero = kotlin.math.hypot(
            firstOrbPosAtZeroSec!!.first - firstOrbPosAtMinus12Sec!!.first,
            firstOrbPosAtZeroSec!!.second - firstOrbPosAtMinus12Sec!!.second
        )
        val distZeroToPost = kotlin.math.hypot(
            firstOrbPosAtPlusHalfSec!!.first - firstOrbPosAtZeroSec!!.first,
            firstOrbPosAtPlusHalfSec!!.second - firstOrbPosAtZeroSec!!.second
        )
        assertTrue(
            "First orb must continuously fall and advance across 0.0s handoff (distPreToZero=$distPreToZero, distZeroToPost=$distZeroToPost)",
            distPreToZero > 80f && distZeroToPost > 25f
        )
    }
}
