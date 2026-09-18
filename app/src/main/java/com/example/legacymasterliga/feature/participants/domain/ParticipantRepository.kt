package com.example.legacymasterliga.feature.participants.domain

import kotlinx.coroutines.flow.Flow

interface ParticipantRepository {
    fun observeSeasonOptions(leagueId: Long): Flow<List<SeasonOption>>
    fun observeClubsForSeason(leagueId: Long, seasonId: Long): Flow<List<ParticipantClub>>
    suspend fun setSelected(seasonId: Long, clubId: Long, selected: Boolean)
}
