package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.database.model.PlayerWithClub
import com.example.legacymasterliga.core.model.MarketStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(player: PlayerEntity): Long

    @Upsert
    suspend fun upsert(player: PlayerEntity): Long

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("""
        SELECT p.*, c.name AS clubName 
        FROM players p 
        JOIN clubs c ON c.id = p.clubId 
        WHERE p.id = :id LIMIT 1
    """)
    suspend fun findByIdWithClub(id: Long): PlayerWithClub?

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE clubId = :clubId AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByNameAndClub(clubId: Long, name: String): PlayerEntity?

    @Query("SELECT * FROM players WHERE clubId = :clubId AND name = :name COLLATE NOCASE")
    suspend fun findAllByNameAndClub(clubId: Long, name: String): List<PlayerEntity>

    @Query("""
        SELECT p.*, c.name AS clubName 
        FROM players p 
        JOIN clubs c ON c.id = p.clubId 
        WHERE p.clubId = :clubId AND p.isActive = 1 
        ORDER BY p.name COLLATE NOCASE
    """)
    fun observeByClub(clubId: Long): Flow<List<PlayerWithClub>>

    @Query("""
        SELECT p.*, c.name AS clubName 
        FROM players p 
        JOIN clubs c ON c.id = p.clubId 
        WHERE p.leagueId = :leagueId 
        AND p.isActive = 1 
        AND (:marketStatus IS NULL OR p.marketStatus = :marketStatus)
        AND (:clubId IS NULL OR p.clubId = :clubId)
        AND (:query IS NULL OR p.name LIKE '%' || :query || '%')
        ORDER BY p.name COLLATE NOCASE
    """)
    fun observeMarket(
        leagueId: Long,
        marketStatus: MarketStatus? = null,
        clubId: Long? = null,
        query: String? = null
    ): Flow<List<PlayerWithClub>>

    @Query("UPDATE players SET marketStatus = :status, askingPriceCr = :price, updatedAt = :now WHERE id = :playerId")
    suspend fun updateMarketStatus(playerId: Long, status: MarketStatus, price: Long?, now: Long = System.currentTimeMillis())

    @Query("UPDATE players SET marketStatus = :status, askingPriceCr = :price, updatedAt = :now WHERE clubId = :clubId")
    suspend fun updateClubPlayersMarket(clubId: Long, status: MarketStatus, price: Long?, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deleteById(playerId: Long)
}
