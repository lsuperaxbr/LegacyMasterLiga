package com.example.legacymasterliga.feature.competitions.domain

import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionStatus
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.SeasonStatus

data class CompetitionSummary(
    val id: Long,
    val leagueId: Long,
    val name: String,
    val type: CompetitionType,
    val format: CompetitionFormat,
    val status: CompetitionStatus,
    val seasons: List<SeasonSummary>,
)

data class SeasonSummary(
    val id: Long,
    val competitionId: Long,
    val number: Int,
    val name: String,
    val status: SeasonStatus,
)

data class CreateCompetitionRequest(
    val leagueId: Long,
    val name: String,
    val type: CompetitionType,
    val format: CompetitionFormat,
    val firstSeasonName: String,
    val initialParticipantIds: List<Long> = emptyList(),
    val groupCount: Int? = null,
    val qualifiedPerGroup: Int? = null,
    val knockoutLegs: Int = 1,
)
