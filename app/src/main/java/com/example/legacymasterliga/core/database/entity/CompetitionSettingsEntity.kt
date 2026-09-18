package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "competition_settings",
    foreignKeys = [
        ForeignKey(
            entity = CompetitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["competitionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["competitionId"], unique = true)],
)
data class CompetitionSettingsEntity(
    @androidx.room.PrimaryKey
    val competitionId: Long,
    val tieBreakCriteriaCsv: String = "POINTS,WINS,GOAL_DIFFERENCE,GOALS_FOR",
    val highlightLeader: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
)
