package com.example.legacymasterliga.feature.history.domain

import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.SeasonStatus

data class HistoricalSeason(
    val leagueId: Long,
    val leagueName: String,
    val competitionId: Long,
    val competitionName: String,
    val competitionType: CompetitionType,
    val seasonId: Long,
    val seasonNumber: Int,
    val seasonName: String,
    val status: SeasonStatus,
    val startedAt: Long?,
    val finishedAt: Long?,
    val championClubId: Long?,
    val championClubName: String?,
    val runnerUpClubId: Long?,
    val runnerUpClubName: String?,
    val participantCount: Int,
    val completedMatchCount: Int,
    val totalGoals: Int,
)

data class HistoricalStanding(
    val position: Int,
    val clubId: Long,
    val clubName: String,
    val shieldUri: String?,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val performance: Double,
)

data class HistoricalOverview(
    val seasonCount: Int = 0,
    val finishedSeasonCount: Int = 0,
    val competitionCount: Int = 0,
    val completedMatchCount: Int = 0,
    val totalGoals: Int = 0,
)
