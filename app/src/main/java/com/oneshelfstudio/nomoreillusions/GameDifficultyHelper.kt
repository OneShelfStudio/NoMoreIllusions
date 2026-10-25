package com.oneshelfstudio.nomoreillusions

fun getTrackDifficultyBoost(selectedTrackIndex: Int): Float {
    return 1.0f + (selectedTrackIndex - 1).coerceAtLeast(0) * 0.14f
}

fun getDifficultyScoreMultiplier(gameDifficulty: GameDifficulty, selectedRemixMode: TrackRemixMode): Float {
    return (when (gameDifficulty) {
        GameDifficulty.EASY -> 0.5f
        GameDifficulty.REGULAR -> 1.0f
        GameDifficulty.HARD -> 1.5f
    }) * selectedRemixMode.scoreMultiplier
}

fun getSpeedScale(gameDifficulty: GameDifficulty, trackDifficultyBoost: Float): Float {
    return (when (gameDifficulty) {
        GameDifficulty.EASY -> 0.68f
        GameDifficulty.REGULAR -> 0.85f
        GameDifficulty.HARD -> 1.18f
    }) * trackDifficultyBoost
}

fun getSpawnRateScale(gameDifficulty: GameDifficulty, trackDifficultyBoost: Float, selectedRemixMode: TrackRemixMode): Float {
    return (when (gameDifficulty) {
        GameDifficulty.EASY -> 0.22f
        GameDifficulty.REGULAR -> 0.42f
        GameDifficulty.HARD -> 1.15f
    }) * trackDifficultyBoost * selectedRemixMode.enemyScale
}
