package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "season_closures",
    foreignKeys = [
        ForeignKey(entity = SeasonEntity::class, parentColumns = ["id"], childColumns = ["seasonId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["championClubId"], onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["runnerUpClubId"], onDelete = ForeignKey.SET_NULL, onUpdate = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["seasonId"], unique = true), Index("championClubId"), Index("runnerUpClubId")],
)
data class SeasonClosureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seasonId: Long,
    val championClubId: Long,
    val runnerUpClubId: Long? = null,
    val championPrizeCr: Long = 0,
    val runnerUpPrizeCr: Long = 0,
    val participationPrizeCr: Long = 0,
    val championPresidentUserId: Long? = null,
    val closedAt: Long = System.currentTimeMillis(),
)
