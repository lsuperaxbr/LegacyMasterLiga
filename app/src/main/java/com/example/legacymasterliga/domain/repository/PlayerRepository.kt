package com.example.legacymasterliga.domain.repository

import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun observeByClub(clubId: Long): Flow<List<Player>>
    fun observeMarket(
        leagueId: Long,
        status: MarketStatus? = null,
        clubId: Long? = null,
        query: String? = null
    ): Flow<List<Player>>
    suspend fun findById(id: Long): Player?
    suspend fun savePlayer(player: Player): Long
    suspend fun updateMarketStatus(playerId: Long, status: MarketStatus, price: Long?)
    suspend fun updateClubPlayersMarket(clubId: Long, status: MarketStatus, price: Long?)
    suspend fun deletePlayer(playerId: Long)
}
