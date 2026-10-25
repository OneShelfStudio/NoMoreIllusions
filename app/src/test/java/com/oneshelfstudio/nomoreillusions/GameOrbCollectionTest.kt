package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class GameOrbCollectionTest {

    @Test
    fun testSpawnStationaryBeatOrbsForFrame_clearsAndSuppressesWhenSustainRibbonActive() {
        val chart = listOf(
            BeatOrbCue(sequenceIndex = 0, timestampSec = 0.5f, normX = 0.5f, normY = 0.5f, powerUpType = PowerUpType.NONE),
            BeatOrbCue(sequenceIndex = 1, timestampSec = 1.0f, normX = 0.5f, normY = 0.5f, powerUpType = PowerUpType.NONE),
            BeatOrbCue(sequenceIndex = 2, timestampSec = 1.5f, normX = 0.5f, normY = 0.5f, powerUpType = PowerUpType.NONE)
        )
        // Add existing powerUps to verify they get cleared
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
            hasActiveSustainRibbon = true, // The condition we are testing
            isTrainingMode = false
        )

        assertTrue("powerUps list should be cleared when hasActiveSustainRibbon is true", powerUps.isEmpty())
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
            trackSec = 0.2f, // trackSec is well before timestampSec, but trackSec + spawnLeadTime > timestampSec (0.2 + 1.5 > 1.0)
            spawnLeadTime = 1.0f,
            ttlGraceSec = 0.5f,
            screenWidth = 1080f,
            screenHeight = 1920f,
            isGoldenActive = false,
            applyColorBlind = { it },
            powerUps = powerUps,
            shockwaves = shockwaves,
            hasActiveSustainRibbon = false, // Not active, should spawn
            isTrainingMode = false
        )

        assertTrue("powerUps list should not be empty, it should have spawned the cue", powerUps.isNotEmpty())
        assertEquals("nextCueIdx should advance past the spawned cue", 1, nextCueIdx)
    }
}
