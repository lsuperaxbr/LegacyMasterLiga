package com.example.legacymasterliga.feature.auction.domain

import com.example.legacymasterliga.core.database.dao.AuctionItemRow
import com.example.legacymasterliga.core.database.dao.AuctionLotSummary
import kotlinx.coroutines.flow.Flow

interface AuctionRepository {
    fun observeLots(leagueId: Long): Flow<List<AuctionLotSummary>>
    fun observeItems(lotId: Long): Flow<List<AuctionItemRow>>
    
    suspend fun createLot(
        leagueId: Long, 
        name: String, 
        startAt: Long, 
        endAt: Long, 
        playerIds: List<Long>, 
        createdByUserId: Long
    )
    
    suspend fun placeBid(
        itemId: Long, 
        clubId: Long, 
        amountCr: Long, 
        bidByUserId: Long
    ): Result<Unit>
    
    suspend fun closeExpiredLots(leagueId: Long)
}
