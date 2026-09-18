package com.example.legacymasterliga.core.database.model

data class LeagueStatisticsOverviewRow(
    val finishedMatches: Long,
    val totalGoals: Long,
    val averageGoals: Double,
    val totalCrMoved: Long,
    val activeClubs: Long,
    val seasonsCount: Long,
)

data class ClubStatisticsRankingRow(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val played: Long,
    val wins: Long,
    val draws: Long,
    val losses: Long,
    val goalsFor: Long,
    val goalsAgainst: Long,
    val goalDifference: Long,
    val points: Long,
    val averagePoints: Double,
    val crMoved: Long,
)
