package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "competition_participants",
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
        Index(value = ["isActive"]),
    ],
)
data class CompetitionParticipantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seasonId: Long,
    val clubId: Long,
    val seed: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)
