package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.SeasonStatus

@Entity(
    tableName = "seasons",
    foreignKeys = [
        ForeignKey(
            entity = CompetitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["competitionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["competitionId"]),
        Index(value = ["competitionId", "number"], unique = true),
        Index(value = ["status"]),
    ],
)
data class SeasonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val competitionId: Long,
    val number: Int,
    val name: String,
    val status: SeasonStatus = SeasonStatus.DRAFT,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
