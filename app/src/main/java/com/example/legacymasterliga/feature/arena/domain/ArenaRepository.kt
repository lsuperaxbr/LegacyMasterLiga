package com.example.legacymasterliga.feature.arena.domain

import com.example.legacymasterliga.core.database.dao.ArenaDuelRow
import kotlinx.coroutines.flow.Flow

interface ArenaRepository {
    fun observeByLeague(leagueId: Long): Flow<List<ArenaDuelRow>>
    suspend fun createDuel(leagueId: Long, clubAId: Long, clubBId: Long, stakeCr: Long, note: String?, createdByUserId: Long)
    suspend fun resolveDuel(duelId: Long, resultType: String)
}
