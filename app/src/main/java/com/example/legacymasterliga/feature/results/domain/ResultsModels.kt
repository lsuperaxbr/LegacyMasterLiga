package com.example.legacymasterliga.feature.results.domain

data class Standing(
    val position: Int,
    val clubId: Long,
    val clubName: String,
    val shieldUri: String?,
    val groupIndex: Int?,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val maximumPointsPerWin: Int = 3,
) {
    val performancePercentage: Double
        get() = if (played == 0) 0.0 else (points.toDouble() / (played * maximumPointsPerWin.toDouble().coerceAtLeast(1.0))) * 100.0
}
