package com.example.legacymasterliga.feature.clubprofile.domain

data class ClubProfileHeader(
    val clubId: Long,
    val leagueId: Long,
    val clubName: String,
    val crestUri: String?,
    val presidentUserId: Long?,
    val presidentName: String?,
    val isActive: Boolean,
)

data class ClubRecentResult(
    val matchId: Long,
    val roundNumber: Int,
    val opponentName: String,
    val goalsFor: Int,
    val goalsAgainst: Int,
) {
    val outcome: ClubMatchOutcome
        get() = when {
            goalsFor > goalsAgainst -> ClubMatchOutcome.WIN
            goalsFor < goalsAgainst -> ClubMatchOutcome.LOSS
            else -> ClubMatchOutcome.DRAW
        }
}

enum class ClubMatchOutcome { WIN, DRAW, LOSS }

data class ClubSeasonHistory(
    val seasonId: Long,
    val seasonName: String,
    val competitionName: String,
    val position: Int,
    val pointsForWin: Int,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
) {
    val performancePercentage: Double
        get() = if (played == 0) 0.0 else (points.toDouble() / (played * pointsForWin.toDouble())) * 100.0
}
