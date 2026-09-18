package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auction_bids",
    indices = [
        Index("itemId"),
        Index("clubId")
    ]
)
data class AuctionBidEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val clubId: Long,
    val amountCr: Long,
    val bidByUserId: Long? = null,
    val status: String, // "ACTIVE", "OUTBID", "WON", "REFUNDED"
    val createdAt: Long,
)
