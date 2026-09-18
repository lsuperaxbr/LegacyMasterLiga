package com.example.legacymasterliga.feature.history.domain

import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeSeasonsByLeague(leagueId: Long): Flow<List<HistoricalSeason>>
    fun observeFinalTable(seasonId: Long): Flow<List<HistoricalStanding>>
}
