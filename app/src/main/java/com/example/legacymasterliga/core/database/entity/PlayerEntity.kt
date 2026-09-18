package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.MarketStatus

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = LeagueEntity::class,
            parentColumns = ["id"],
            childColumns = ["leagueId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("leagueId"),
        Index("clubId"),
        Index("name"),
        Index("marketStatus"),
        Index("externalPlayerId", unique = true)
    ]
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long,
    val clubId: Long,
    val name: String,
    val position: String? = null,
    val marketStatus: MarketStatus = MarketStatus.NOT_LISTED,
    val askingPriceCr: Long? = null,
    val skillImageUri: String? = null,
    val notes: String? = null,
    val externalPlayerId: String? = null,
    val attributesRaw: String? = null,
    val overall: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
