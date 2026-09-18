package com.example.legacymasterliga.feature.schedule.domain

import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeSeasonOptions(leagueId: Long): Flow<List<ScheduleSeasonOption>>
    fun observeSchedule(seasonId: Long): Flow<List<ScheduleRound>>
    suspend fun generateSchedule(seasonId: Long): ScheduleGenerationResult
}
