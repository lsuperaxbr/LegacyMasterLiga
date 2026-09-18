package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.AuctionBidEntity
import com.example.legacymasterliga.core.database.entity.AuctionItemEntity
import com.example.legacymasterliga.core.database.entity.AuctionLotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuctionDao {
    @Insert suspend fun insertLot(lot: AuctionLotEntity): Long
    @Insert suspend fun insertItem(item: AuctionItemEntity): Long
    @Insert suspend fun insertBid(bid: AuctionBidEntity): Long
    @Update suspend fun updateLot(lot: AuctionLotEntity)
    @Update suspend fun updateItem(item: AuctionItemEntity)
    @Update suspend fun updateBid(bid: AuctionBidEntity)

    @Query("SELECT * FROM auction_lots WHERE id = :id")
    suspend fun findLotById(id: Long): AuctionLotEntity?

    @Query("SELECT * FROM auction_lots WHERE leagueId = :leagueId AND status != 'CLOSED' AND endAt <= :now")
    suspend fun findExpiredLots(leagueId: Long, now: Long): List<AuctionLotEntity>

    @Query("SELECT * FROM auction_items WHERE lotId = :lotId")
    suspend fun findItemsByLot(lotId: Long): List<AuctionItemEntity>

    @Query("SELECT * FROM auction_items WHERE id = :id")
    suspend fun findItemById(id: Long): AuctionItemEntity?

    @Query("SELECT * FROM auction_bids WHERE itemId = :itemId AND status = 'ACTIVE' ORDER BY amountCr DESC LIMIT 1")
    suspend fun findActiveBid(itemId: Long): AuctionBidEntity?

    @Query("SELECT * FROM auction_bids WHERE id = :id LIMIT 1")
    suspend fun findBidById(id: Long): AuctionBidEntity?

    @Query("SELECT COALESCE(SUM(amountCr), 0) FROM auction_bids WHERE clubId = :clubId AND status = 'ACTIVE'")
    suspend fun sumActiveBidsByClub(clubId: Long): Long

    @Query("""
        SELECT l.*, COUNT(i.id) as itemCount
        FROM auction_lots l
        LEFT JOIN auction_items i ON i.lotId = l.id
        WHERE l.leagueId = :leagueId
        GROUP BY l.id
        ORDER BY l.createdAt DESC
    """)
    fun observeLotsByLeague(leagueId: Long): Flow<List<AuctionLotSummary>>

    @Query("""
        SELECT i.*, p.name AS playerName, p.overall AS playerOverall, p.position AS playerPosition,
               c.name AS leadingClubName
        FROM auction_items i
        JOIN players p ON p.id = i.playerId
        LEFT JOIN clubs c ON c.id = i.leadingClubId
        WHERE i.lotId = :lotId
    """)
    fun observeItemsByLot(lotId: Long): Flow<List<AuctionItemRow>>
}

data class AuctionLotSummary(
    val id: Long, val leagueId: Long, val name: String, val startAt: Long, val endAt: Long,
    val status: String, val closedAt: Long?, val createdByUserId: Long?, val createdAt: Long,
    val itemCount: Int,
)

data class AuctionItemRow(
    val id: Long, val lotId: Long, val playerId: Long, val playerName: String,
    val playerOverall: Int?, val playerPosition: String?, val startingPriceCr: Long,
    val currentBidCr: Long?, val leadingClubId: Long?, val leadingClubName: String?, val status: String,
)
