package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auction_lots",
    indices = [Index("leagueId")]
)
data class AuctionLotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long,
    val name: String,
    val startAt: Long,
    val endAt: Long,
    val status: String, // "SCHEDULED", "OPEN", "CLOSED"
    val closedAt: Long? = null,
    val createdByUserId: Long? = null,
    val createdAt: Long,
)
