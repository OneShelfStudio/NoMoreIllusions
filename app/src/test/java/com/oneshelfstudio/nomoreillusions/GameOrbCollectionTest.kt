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
        // Existing mid-flight powerUps must be preserved while new spawns are suppressed
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
}
