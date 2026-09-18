package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionStatus
import com.example.legacymasterliga.core.model.CompetitionType

@Entity(
    tableName = "competitions",
    foreignKeys = [
        ForeignKey(
            entity = LeagueEntity::class,
            parentColumns = ["id"],
            childColumns = ["leagueId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["leagueId"]),
        Index(value = ["leagueId", "name"], unique = true),
        Index(value = ["type"]),
        Index(value = ["status"]),
    ],
)
data class CompetitionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val leagueId: Long,
    val name: String,
    val type: CompetitionType,
    val format: CompetitionFormat,
    val status: CompetitionStatus = CompetitionStatus.DRAFT,
    val pointsForWin: Int = 3,
    val pointsForDraw: Int = 1,
    val pointsForLoss: Int = 0,
    val yellowCardFineCr: Long = 0,
    val redCardFineCr: Long = 0,
    val groupCount: Int? = null,
    val qualifiedPerGroup: Int? = null,
    val knockoutLegs: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
