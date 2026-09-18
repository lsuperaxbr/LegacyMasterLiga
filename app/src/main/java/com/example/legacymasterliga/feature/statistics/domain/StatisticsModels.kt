package com.example.legacymasterliga.feature.statistics.domain

data class LeagueStatisticsOverview(
    val finishedMatches: Long = 0,
    val totalGoals: Long = 0,
    val averageGoals: Double = 0.0,
    val totalCrMoved: Long = 0,
    val activeClubs: Long = 0,
    val seasonsCount: Long = 0,
)

data class ClubStatisticsRanking(
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

data class StatisticsSnapshot(
    val overview: LeagueStatisticsOverview = LeagueStatisticsOverview(),
    val clubs: List<ClubStatisticsRanking> = emptyList(),
)
