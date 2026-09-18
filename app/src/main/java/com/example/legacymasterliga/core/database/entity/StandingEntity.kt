package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "standings",
    foreignKeys = [
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["seasonId"]),
        Index(value = ["clubId"]),
        Index(value = ["seasonId", "clubId"], unique = true),
        Index(value = ["seasonId", "points"]),
    ],
)
data class StandingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seasonId: Long,
    val clubId: Long,
    val groupIndex: Int? = null,
    val played: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)
