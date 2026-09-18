package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.RoundStatus

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["seasonId"]),
        Index(value = ["seasonId", "number"], unique = true),
        Index(value = ["status"]),
    ],
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seasonId: Long,
    val number: Int,
    val name: String = "Rodada $number",
    val stage: String = "REGULAR",
    val stageLabel: String? = null,
    val groupIndex: Int? = null,
    val status: RoundStatus = RoundStatus.SCHEDULED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
