package com.example.legacymasterliga.feature.settings.domain

import com.example.legacymasterliga.domain.model.League
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeAppPreferences(): Flow<AppPreferences>
    fun observeLeagues(): Flow<List<League>>
    fun observeCompetitions(leagueId: Long): Flow<List<CompetitionOption>>
    fun observeCompetitionRules(competitionId: Long): Flow<CompetitionRules>

    suspend fun updateAppPreferences(preferences: AppPreferences)
    suspend fun renameLeague(leagueId: Long, name: String)
    suspend fun updateCompetitionRules(rules: CompetitionRules)
}
