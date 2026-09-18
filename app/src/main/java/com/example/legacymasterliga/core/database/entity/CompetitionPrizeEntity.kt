package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "competition_prizes",
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
data class CompetitionPrizeEntity(
    @PrimaryKey val competitionId: Long,
    val championPrizeCr: Long = 0,
    val runnerUpPrizeCr: Long = 0,
    val participationPrizeCr: Long = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)
