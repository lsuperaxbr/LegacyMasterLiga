package com.example.legacymasterliga.core.database.model

data class ReportLeagueRow(val id: Long, val name: String)
data class ReportCompetitionRow(val id: Long, val leagueId: Long, val name: String)
data class ReportSeasonRow(val id: Long, val competitionId: Long, val name: String)
data class ReportStandingRow(
    val clubName: String, val played: Int, val wins: Int, val draws: Int, val losses: Int,
    val goalsFor: Int, val goalsAgainst: Int, val goalDifference: Int, val points: Int,
)
data class ReportFinanceRow(val clubName: String, val amountCr: Long, val description: String, val type: String, val createdAt: Long)
data class ReportTransferRow(val playerName: String, val originClubName: String, val destinationClubName: String, val valueCr: Long, val createdAt: Long)
data class ReportMatchRow(val roundNumber: Int, val homeClubName: String, val awayClubName: String, val homeScore: Int, val awayScore: Int)
data class ReportHistoryRow(val competitionName: String, val seasonName: String, val championName: String?, val runnerUpName: String?, val totalGoals: Int, val completedMatches: Int)
data class ReportStatisticsRow(val finishedMatches: Long, val totalGoals: Long, val averageGoals: Double, val totalCrMoved: Long, val activeClubs: Long)
