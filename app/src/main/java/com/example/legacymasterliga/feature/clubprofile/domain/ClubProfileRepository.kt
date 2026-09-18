package com.example.legacymasterliga.feature.clubprofile.domain

import kotlinx.coroutines.flow.Flow

interface ClubProfileRepository {
    fun observeHeader(clubId: Long): Flow<ClubProfileHeader?>
    fun observeRecentResults(clubId: Long, seasonId: Long, limit: Int = 5): Flow<List<ClubRecentResult>>
    fun observeHistory(clubId: Long): Flow<List<ClubSeasonHistory>>
}
