package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auction_items",
    indices = [
        Index("lotId"),
        Index("playerId")
    ]
)
data class AuctionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lotId: Long,
    val playerId: Long,
    val startingPriceCr: Long = 5,
    val currentBidCr: Long? = null,
    val leadingClubId: Long? = null,
    val status: String, // "OPEN", "SOLD", "UNSOLD"
    val createdAt: Long,
)
