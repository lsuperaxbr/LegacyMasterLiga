package com.example.legacymasterliga.core.database.model

import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.SeasonStatus

data class SeasonHistoryRow(
    val leagueId: Long,
    val leagueName: String,
    val competitionId: Long,
    val competitionName: String,
    val competitionType: CompetitionType,
    val seasonId: Long,
    val seasonNumber: Int,
    val seasonName: String,
    val seasonStatus: SeasonStatus,
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
