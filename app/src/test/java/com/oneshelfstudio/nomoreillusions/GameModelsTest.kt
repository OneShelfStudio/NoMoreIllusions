package com.oneshelfstudio.nomoreillusions

import org.junit.Assert.*
import org.junit.Test

class GameModelsTest {

    @Test
    fun testTrackRemixMode_properties_Original() {
        val mode = TrackRemixMode.ORIGINAL
        assertEquals(1.0f, mode.audioSpeed, 0.0f)
        assertEquals(1.0f, mode.audioPitch, 0.0f)
        assertEquals(1.0f, mode.noteMultiplier, 0.0f)
        assertEquals(1.0f, mode.scoreMultiplier, 0.0f)
        assertEquals(1.0f, mode.enemyScale, 0.0f)
    }

    @Test
    fun testTrackRemixMode_properties_Overclock() {
        val mode = TrackRemixMode.OVERCLOCK
        assertEquals(1.20f, mode.audioSpeed, 0.0f)
        assertEquals(1.12f, mode.audioPitch, 0.0f)
        assertEquals(1.5f, mode.noteMultiplier, 0.0f)
        assertEquals(1.5f, mode.scoreMultiplier, 0.0f)
        assertEquals(1.25f, mode.enemyScale, 0.0f)
    }

    @Test
    fun testTrackRemixMode_properties_Slowed() {
        val mode = TrackRemixMode.SLOWED
        assertEquals(0.85f, mode.audioSpeed, 0.0f)
        assertEquals(0.86f, mode.audioPitch, 0.0f)
        assertEquals(0.5f, mode.noteMultiplier, 0.0f)
        assertEquals(0.55f, mode.scoreMultiplier, 0.0f)
        assertEquals(0.65f, mode.enemyScale, 0.0f)
    }

    @Test
    fun testTrackRemixMode_properties_Endless() {
        val mode = TrackRemixMode.ENDLESS
        assertEquals(1.0f, mode.audioSpeed, 0.0f)
        assertEquals(1.0f, mode.audioPitch, 0.0f)
        assertEquals(2.0f, mode.noteMultiplier, 0.0f)
        assertEquals(2.0f, mode.scoreMultiplier, 0.0f)
        assertEquals(1.15f, mode.enemyScale, 0.0f)
    }

    @Test
    fun testGetTrackDifficultyBoost() {
        assertEquals(1.0f, getTrackDifficultyBoost(1), 0.01f) // First track
        assertEquals(1.14f, getTrackDifficultyBoost(2), 0.01f) // Second track (+0.14)
        assertEquals(1.28f, getTrackDifficultyBoost(3), 0.01f) // Third track (+0.28)
    }

    @Test
    fun testGetDifficultyScoreMultiplier() {
        // Test combination matrices
        assertEquals(0.5f * 1.0f, getDifficultyScoreMultiplier(GameDifficulty.EASY, TrackRemixMode.ORIGINAL), 0.01f)
        assertEquals(1.0f * 1.5f, getDifficultyScoreMultiplier(GameDifficulty.REGULAR, TrackRemixMode.OVERCLOCK), 0.01f)
        assertEquals(1.5f * 2.0f, getDifficultyScoreMultiplier(GameDifficulty.HARD, TrackRemixMode.ENDLESS), 0.01f)
    }

    @Test
    fun testGetSpeedScale() {
        // Boost is 1.0f for track 1
        assertEquals(0.68f * 1.0f, getSpeedScale(GameDifficulty.EASY, 1.0f), 0.01f)
        assertEquals(0.85f * 1.0f, getSpeedScale(GameDifficulty.REGULAR, 1.0f), 0.01f)
        assertEquals(1.18f * 1.0f, getSpeedScale(GameDifficulty.HARD, 1.0f), 0.01f)

        // Boost is 1.14f for track 2
        assertEquals(0.85f * 1.14f, getSpeedScale(GameDifficulty.REGULAR, 1.14f), 0.01f)
    }

    @Test
    fun testGetSpawnRateScale() {
        // Boost is 1.0f for track 1
        assertEquals(0.22f * 1.0f * 1.0f, getSpawnRateScale(GameDifficulty.EASY, 1.0f, TrackRemixMode.ORIGINAL), 0.01f)
        // Hard mode (1.15) * track 2 boost (1.14) * Overclock enemy scale (1.25)
        assertEquals(1.15f * 1.14f * 1.25f, getSpawnRateScale(GameDifficulty.HARD, 1.14f, TrackRemixMode.OVERCLOCK), 0.01f)
        // Regular mode (0.42) * track 1 boost (1.0) * Slowed enemy scale (0.65)
        assertEquals(0.42f * 1.0f * 0.65f, getSpawnRateScale(GameDifficulty.REGULAR, 1.0f, TrackRemixMode.SLOWED), 0.01f)
    }
}
