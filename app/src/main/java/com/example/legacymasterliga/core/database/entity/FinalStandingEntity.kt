package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "final_standings",
    foreignKeys = [
        ForeignKey(entity = SeasonEntity::class, parentColumns = ["id"], childColumns = ["seasonId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["clubId"], onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE),
    ],
    indices = [Index("seasonId"), Index("clubId"), Index(value = ["seasonId", "clubId"], unique = true), Index(value = ["seasonId", "position"], unique = true)],
)
data class FinalStandingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seasonId: Long,
    val clubId: Long,
    val position: Int,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val archivedAt: Long = System.currentTimeMillis(),
)
