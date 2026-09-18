package com.example.legacymasterliga.data.repository

import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.data.mapper.toDomain
import com.example.legacymasterliga.domain.model.Player
import com.example.legacymasterliga.domain.repository.PlayerRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomPlayerRepository @Inject constructor(
    private val playerDao: PlayerDao
) : PlayerRepository {
    override fun observeByClub(clubId: Long): Flow<List<Player>> =
        playerDao.observeByClub(clubId).map { entities -> entities.map { it.toDomain() } }

    override fun observeMarket(
        leagueId: Long,
        status: MarketStatus?,
        clubId: Long?,
        query: String?
    ): Flow<List<Player>> =
        playerDao.observeMarket(leagueId, status, clubId, query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun findById(id: Long): Player? = playerDao.findByIdWithClub(id)?.toDomain()

    override suspend fun savePlayer(player: Player): Long {
        val entity = PlayerEntity(
            id = player.id,
            leagueId = player.leagueId,
            clubId = player.clubId,
            name = player.name,
            position = player.position,
            marketStatus = player.marketStatus,
            askingPriceCr = player.askingPriceCr,
            skillImageUri = player.skillImageUri,
            notes = player.notes,
            externalPlayerId = player.externalPlayerId,
            attributesRaw = player.attributesRaw,
            overall = player.overall,
            updatedAt = System.currentTimeMillis()
        )
        return if (entity.id == 0L) {
            playerDao.insert(entity)
        } else {
            playerDao.update(entity)
            entity.id
        }
    }

    override suspend fun updateMarketStatus(playerId: Long, status: MarketStatus, price: Long?) {
        playerDao.updateMarketStatus(playerId, status, price)
    }

    override suspend fun updateClubPlayersMarket(clubId: Long, status: MarketStatus, price: Long?) {
        playerDao.updateClubPlayersMarket(clubId, status, price)
    }

    override suspend fun deletePlayer(playerId: Long) {
        playerDao.deleteById(playerId)
    }
}
