package com.example.legacymasterliga.feature.online.domain

import kotlinx.coroutines.flow.Flow

interface CloudLeagueRepository {
    suspend fun promoteToCloud(localLeagueId: Long): Result<String>
    suspend fun generateInvite(cloudLeagueId: String, maxUses: Int = 1): Result<String>
    suspend fun joinByInvite(inviteCode: String): Result<String>
    fun observeLeague(cloudLeagueId: String): Flow<CloudLeague?>
    fun observeMembers(cloudLeagueId: String): Flow<List<CloudMember>>
    fun observeActiveInvite(cloudLeagueId: String): Flow<CloudInvite?>
}
