package com.example.legacymasterliga.feature.competitions.domain

import kotlinx.coroutines.flow.Flow

interface CompetitionRepository {
    fun observeByLeague(leagueId: Long): Flow<List<CompetitionSummary>>
    suspend fun createCompetition(request: CreateCompetitionRequest): Long
    suspend fun createNextSeason(competitionId: Long, name: String, participantIds: List<Long>): Long
    suspend fun delete(competitionId: Long)
}
