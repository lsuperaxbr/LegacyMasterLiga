package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transfers",
    foreignKeys = [
        ForeignKey(entity = LeagueEntity::class, parentColumns = ["id"], childColumns = ["leagueId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["originClubId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["destinationClubId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("leagueId"), Index("originClubId"), Index("destinationClubId"), Index("createdAt")],
)
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long,
    val playerName: String,
    val originClubId: Long,
    val destinationClubId: Long,
    val valueCr: Long,
    val type: String = "TRANSFER",
    val swapId: String? = null,
    val seasonId: Long? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
