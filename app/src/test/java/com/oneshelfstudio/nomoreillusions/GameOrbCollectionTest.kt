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
            BeatOrbCue(sequenceIndex = 4, timestampSec = 4.0f, normX = 0.50f, normY = 0.62f, powerUpType = PowerUpType.NONE, isMusicVideoOrb = true)
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
        assertFalse("Regular notes without isMusicVideoOrb must not be marked video orbs", spawnedAll[0].isMusicVideoOrb)
        assertTrue("Explicitly charted video relic (index 4) must be marked isMusicVideoOrb", spawnedAll[4].isMusicVideoOrb)
    }

    @Test
    fun testConfideAndNumberOne_spawnSixVideoRelicsPerWindowWithCleanSeparationAndWideWindow2Stagger() {
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
                var t = -1.2f
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

                val cutoffMid = if (trackIdx == 1) 80f else 70f
                val montage1Cutoff = if (trackIdx == 1) 58.0f else 53.0f
                val montage1End = if (trackIdx == 1) 68.5f else 63.0f
                val montage2Cutoff = if (trackIdx == 1) 155.0f else 117.5f

                val set1Relics = spawnedAll.filter { it.isMusicVideoOrb && it.targetBeatSec < cutoffMid }.sortedBy { it.targetBeatSec }
                val set2Relics = spawnedAll.filter { it.isMusicVideoOrb && it.targetBeatSec >= cutoffMid }.sortedBy { it.targetBeatSec }

                assertEquals("Track $trackIdx (training=$training) must spawn 6 Video Relics in Window 1 (5 needed + 1 buffer)", 6, set1Relics.size)
                assertTrue("All Window 1 Video Relics must arrive well before Montage 1 ($montage1Cutoff s)", set1Relics.all { it.targetBeatSec <= montage1Cutoff })

                assertEquals("Track $trackIdx (training=$training) must spawn 6 Video Relics in Window 2 (5 needed + 1 buffer)", 6, set2Relics.size)
                assertTrue("All Window 2 Video Relics must arrive before Montage 2 ($montage2Cutoff s)", set2Relics.all { it.targetBeatSec <= montage2Cutoff })

                val breatherGap = set2Relics.first().targetBeatSec - montage1End
                assertTrue("Clean breather separation between Montage 1 end and Window 2 start must be >= 16.0s, was $breatherGap", breatherGap >= 16.0f)

                val avgWindow2Spacing = (set2Relics.last().targetBeatSec - set2Relics.first().targetBeatSec) / (set2Relics.size - 1).toFloat()
                val minExpectedAvg = if (trackIdx == 1) 11.5f else 6.8f
                assertTrue("Window 2 Video Relics must be widely staggered (avg >= $minExpectedAvg s), was $avgWindow2Spacing", avgWindow2Spacing >= minExpectedAvg)
            }
        }

        // Verify all 5 RippleWaveStyle shockwave archetypes construct cleanly
        assertEquals(5, RippleWaveStyle.entries.size)
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
        var t = -1.0f
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

        // Verify multi-directional entry: left flank, right flank, and top entries all present!
        assertTrue("Must spawn orbs entering from left flank", spawnedSnapshots.any { it.spawnX < 0f })
        assertTrue("Must spawn orbs entering from right flank", spawnedSnapshots.any { it.spawnX > 1080f })
        assertTrue("Must spawn orbs entering from top perimeter", spawnedSnapshots.any { it.spawnY < 0f })

        // Verify 0:15 (Tier 1) and 1:18 (Tier 2) Special 24K Orbs arrive near top-center!
        val milestone15 = spawnedSnapshots.firstOrNull { abs(it.targetBeatSec - 15.0f) <= 0.6f && it.goldenTier == 1 }
        assertNotNull("0:15 Special 24K Milestone Orb (Tier 1) must exist", milestone15)
        assertEquals(540f, milestone15!!.anchorX, 2f)
        assertTrue("0:15 Special 24K Orb must sit near top-middle (anchorY <= 34% height), was ${milestone15.anchorY}", milestone15.anchorY <= 1920f * 0.34f)

        val milestone78 = spawnedSnapshots.firstOrNull { abs(it.targetBeatSec - 77.9f) <= 0.8f && it.goldenTier == 2 }
        assertNotNull("1:18 Special 24K Milestone Orb (Tier 2) must exist", milestone78)
        assertEquals(540f, milestone78!!.anchorX, 2f)
        assertTrue("1:18 Special 24K Orb must sit near top-middle (anchorY <= 34% height)", milestone78.anchorY <= 1920f * 0.34f)

        // Verify flowy/whispy slow-mo swirl orbs around 0:45-0:48 ("losing your battles")
        val flowyOrbs = spawnedSnapshots.filter { it.targetBeatSec in 44.5f..49.2f }
        assertTrue("Flowy breakdown at 0:45-0:48 must have swirling slow-mo orbs", flowyOrbs.isNotEmpty() && flowyOrbs.all { it.swirlStrength >= 0.9f && (it.targetBeatSec - it.spawnBeatSec) >= 2.5f })

        // Verify surprise false-ending pegs after 3:32 (212s) in Confide and after 2:38 (158.5s) in Number One!
        val lateSurpriseConfide = track.orbChart.filter { it.timestampSec >= 212.0f }
        assertTrue("Confide must include >= 3 surprise late pegs after 212s, found ${lateSurpriseConfide.size}", lateSurpriseConfide.size >= 3)
        val lateSurpriseNumberOne = MusicLibrary.getTrack(2).orbChart.filter { it.timestampSec >= 158.5f }
        assertTrue("Number One must include >= 3 surprise late pegs after 158.5s, found ${lateSurpriseNumberOne.size}", lateSurpriseNumberOne.size >= 3)
    }
}
