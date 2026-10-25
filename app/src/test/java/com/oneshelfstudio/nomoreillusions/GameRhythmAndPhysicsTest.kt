package com.oneshelfstudio.nomoreillusions

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class GameRhythmAndPhysicsTest {

    @Test
    fun `spawnStationaryBeatOrbsForFrame clears and suppresses all pegs when sustain ribbon is active`() {
        val chart = listOf(
            BeatOrbCue(1, 1.0f, 0.25f, 0.65f, PowerUpType.NONE, isGoldenCue = false),
            BeatOrbCue(2, 1.5f, 0.50f, 0.65f, PowerUpType.BLAST, isGoldenCue = true),
            BeatOrbCue(3, 2.0f, 0.75f, 0.65f, PowerUpType.SHIELD, keyWordEffect = KeyWordEffect.CONSTELLATION_STAR)
        )
        val powerUps = mutableListOf(
            GameObject(x = 100f, y = 200f, radius = 32f, color = Color.Cyan, powerUpType = PowerUpType.NONE),
            GameObject(x = 150f, y = 220f, radius = 38f, color = Color.Yellow, keyWordEffect = KeyWordEffect.CONSTELLATION_STAR)
        )
        val shockwaves = mutableListOf<Shockwave>()

        val nextIdx = spawnStationaryBeatOrbsForFrame(
            chart = chart,
            startCueIdx = 0,
            trackSec = 1.2f,
            spawnLeadTime = 2.0f,
            ttlGraceSec = 0.5f,
            screenWidth = 1080f,
            screenHeight = 2400f,
            isGoldenActive = false,
            applyColorBlind = { it },
            powerUps = powerUps,
            shockwaves = shockwaves,
            hasActiveSustainRibbon = true,
            isTrainingMode = false
        )

        assertTrue("All active beat pegs must be cleared while sustain ribbon is active", powerUps.isEmpty())
        assertTrue("Cue index should advance past overlapping ribbon timestamps", nextIdx > 0)
    }

    @Test
    fun `spawnStationaryBeatOrbsForFrame spawns 3D volumetric orbs with 32f and 38f radii when no ribbon is active`() {
        val chart = listOf(
            BeatOrbCue(1, 1.0f, 0.25f, 0.65f, PowerUpType.NONE, isGoldenCue = false),
            BeatOrbCue(2, 2.2f, 0.75f, 0.65f, PowerUpType.BLAST, isGoldenCue = true)
        )
        val powerUps = mutableListOf<GameObject>()
        val shockwaves = mutableListOf<Shockwave>()

        spawnStationaryBeatOrbsForFrame(
            chart = chart,
            startCueIdx = 0,
            trackSec = 0.8f,
            spawnLeadTime = 2.0f,
            ttlGraceSec = 0.5f,
            screenWidth = 1080f,
            screenHeight = 2400f,
            isGoldenActive = false,
            applyColorBlind = { it },
            powerUps = powerUps,
            shockwaves = shockwaves,
            hasActiveSustainRibbon = false,
            isTrainingMode = false
        )

        assertTrue("Should spawn highway beat pegs plus intermediate gap filler peg", powerUps.size >= 2)
        val standardOrb = powerUps.first { it.powerUpType == PowerUpType.NONE && it.keyWordEffect == KeyWordEffect.NONE }
        val specialOrb = powerUps.first { it.powerUpType == PowerUpType.BLAST }
        assertEquals(32f, standardOrb.radius, 0.01f)
        assertEquals(38f, specialOrb.radius, 0.01f)
    }

    @Test
    fun `stepEnemyKinematicsAndFlocking enforces strictly downward movement and never flies upward from bottom`() {
        val player = GameObject(x = 540f, y = 1560f, radius = 26f, color = Color.Cyan)
        // Enemy starts below the player with an upward angle (-PI / 2)
        val enemy = GameObject(
            x = 540f,
            y = 1750f,
            radius = 16.5f,
            color = Color.Red,
            type = EnemyType.SEEKER,
            angle = -PI.toFloat() / 2f,
            speed = 4.0f
        )
        val startY = enemy.y

        stepEnemyKinematicsAndFlocking(
            e = enemy,
            enemies = listOf(enemy),
            player = player,
            gameTime = 10f,
            currentScale = 1.0f,
            beatPhase = 0.1f,
            multiBand = MultiBandAudioState(bass = 0.5f, bassDropOut = false)
        )

        assertTrue("Enemy at or below playerY must lock hasPassedPlayer = true", enemy.hasPassedPlayer)
        assertTrue("Enemy y must strictly increase (move downward) even if initial angle pointed upward", enemy.y > startY)
    }

    @Test
    fun `stepAndSpawnTelegraphedEnemies respects Easy training mode cap and top-26-percent spawn zone`() {
        val enemies = mutableListOf(
            GameObject(x = 500f, y = 300f, radius = 16.5f, color = Color.Red, type = EnemyType.PULSE)
        )
        val telegraphs = mutableListOf<EnemySpawnTelegraph>()
        val shockwaves = mutableListOf<Shockwave>()
        val player = GameObject(x = 540f, y = 1560f, radius = 26f, color = Color.Cyan)

        // In Easy / Chill Training Mode (spawnRateScale = 0.22f), maxActiveEnemies is 1
        stepAndSpawnTelegraphedEnemies(
            enemies = enemies,
            telegraphs = telegraphs,
            shockwaves = shockwaves,
            levelPlayTime = 30f,
            difficulty = 1.0f,
            spawnRateScale = 0.22f,
            speedScale = 0.72f,
            screenWidth = 1080f,
            screenHeight = 2400f,
            customEnemyStyle = "MIXED",
            player = player,
            applyColorBlind = { it },
            beatPhase = 0.05f,
            sectionMaxEnemies = 4
        )

        assertTrue("Easy training mode must not spawn additional telegraphs when 1 enemy is already active", telegraphs.isEmpty())
    }

    @Test
    fun `TrackRemixMode scales score, note rewards, and enemy multipliers across Chill, Normal, Fast, and Endless`() {
        assertTrue(TrackRemixMode.SLOWED.scoreMultiplier < TrackRemixMode.ORIGINAL.scoreMultiplier)
        assertTrue(TrackRemixMode.ORIGINAL.scoreMultiplier < TrackRemixMode.OVERCLOCK.scoreMultiplier)
        assertTrue(TrackRemixMode.OVERCLOCK.scoreMultiplier < TrackRemixMode.ENDLESS.scoreMultiplier)

        assertEquals(0.5f, TrackRemixMode.SLOWED.noteMultiplier, 0.001f)
        assertEquals(1.0f, TrackRemixMode.ORIGINAL.noteMultiplier, 0.001f)
        assertEquals(1.5f, TrackRemixMode.OVERCLOCK.noteMultiplier, 0.001f)
        assertEquals(2.0f, TrackRemixMode.ENDLESS.noteMultiplier, 0.001f)
    }
}
