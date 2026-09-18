package com.example.legacymasterliga.feature.closure.domain

import com.example.legacymasterliga.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SeasonClosureRepository {
    suspend fun preview(seasonId: Long): ClosurePreview
    fun observePrizeConfiguration(competitionId: Long): Flow<PrizeConfiguration>
    fun observePrizeHistory(leagueId: Long): Flow<List<PrizeHistoryItem>>
    fun observeCompetitions(leagueId: Long): Flow<List<PrizeCompetitionOption>>
    fun observeSeasons(competitionId: Long): Flow<List<PrizeSeasonOption>>
    suspend fun findParticipants(seasonId: Long): List<PrizeClubOption>
    suspend fun savePrizeConfiguration(actor: User, configuration: PrizeConfiguration)
    suspend fun awardPrize(
        actor: User,
        leagueId: Long,
        competitionId: Long,
        seasonId: Long,
        clubId: Long,
        prizeType: String,
        amount: Long,
        description: String,
    )
    suspend fun close(actor: User, request: CloseSeasonRequest)
    suspend fun closeCupAutomatically(seasonId: Long)
}
