package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clubs",
    foreignKeys = [
        ForeignKey(
            entity = LeagueEntity::class,
            parentColumns = ["id"],
            childColumns = ["leagueId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["presidentUserId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["leagueId"]),
        Index(value = ["presidentUserId"]),
        Index(value = ["leagueId", "name"], unique = true),
        Index(value = ["isActive"]),
    ],
)
data class ClubEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val leagueId: Long,
    val name: String,
    val crestUri: String? = null,
    val presidentUserId: Long? = null,
    val isBank: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
