package com.example.legacymasterliga.feature.schedule.domain

import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.MatchStatus

data class ScheduleSeasonOption(
    val seasonId: Long,
    val competitionId: Long,
    val competitionName: String,
    val seasonName: String,
    val format: CompetitionFormat,
    val type: CompetitionType,
)

data class ScheduleMatch(
    val id: Long,
    val homeClubId: Long,
    val homeClubName: String,
    val homeShieldUri: String?,
    val awayClubId: Long,
    val awayClubName: String,
    val awayShieldUri: String?,
    val leg: Int,
    val stage: String = "REGULAR",
    val homeScore: Int?,
    val awayScore: Int?,
    val status: MatchStatus,
    val penaltiesHome: Int? = null,
    val penaltiesAway: Int? = null,
    val winnerClubId: Long? = null,
)

data class ScheduleRound(
    val id: Long,
    val number: Int,
    val name: String,
    val stage: String = "REGULAR",
    val stageLabel: String? = null,
    val matches: List<ScheduleMatch>,
)

data class GeneratedFixture(
    val roundNumber: Int,
    val roundName: String? = null,
    val stageLabel: String? = null,
    val homeClubId: Long,
    val awayClubId: Long,
    val leg: Int = 1,
    val stage: String = "REGULAR",
    val groupIndex: Int? = null,
    val bracketPosition: Int? = null,
)

data class ScheduleGenerationResult(
    val roundsCreated: Int,
    val matchesCreated: Int,
)
