package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_events",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["scoringClubId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("matchId"),
        Index("playerId")
    ]
)
data class GoalEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val playerId: Long,
    val scoringClubId: Long, // clube que recebe o gol no placar
    val isOwnGoal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
