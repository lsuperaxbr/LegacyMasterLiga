package com.example.legacymasterliga.feature.statistics.domain

import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun observe(
        leagueId: Long,
        competitionId: Long?,
        seasonId: Long?,
    ): Flow<StatisticsSnapshot>
}
