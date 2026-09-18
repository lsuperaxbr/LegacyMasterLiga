package com.example.legacymasterliga.domain.repository

import com.example.legacymasterliga.domain.model.Club
import kotlinx.coroutines.flow.Flow

interface ClubRepository {
    fun observeActiveByLeague(leagueId: Long): Flow<List<Club>>
    fun observeManagedByLeague(leagueId: Long): Flow<List<Club>>
    suspend fun findById(clubId: Long): Club?
    suspend fun findLeagueBank(leagueId: Long): Club?
    suspend fun createLeagueBank(leagueId: Long, name: String): Long
    suspend fun create(
        leagueId: Long,
        name: String,
        crestUri: String?,
        presidentUserId: Long?,
        initialBalance: Long = 0L,
    ): Long
    suspend fun update(
        clubId: Long,
        leagueId: Long,
        name: String,
        crestUri: String?,
        presidentUserId: Long?,
    )
    suspend fun delete(clubId: Long)
    suspend fun setActive(clubId: Long, isActive: Boolean)
    suspend fun assignPresident(userId: Long, clubId: Long?)
}
