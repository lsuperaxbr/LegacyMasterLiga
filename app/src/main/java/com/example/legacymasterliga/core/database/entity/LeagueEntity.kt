package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.LeagueStatus

@Entity(
    tableName = "leagues",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["cloudLeagueId"], unique = true),
        Index(value = ["status"]),
    ],
)
data class LeagueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val currencyCode: String = "CR",
    val status: LeagueStatus = LeagueStatus.ACTIVE,
    val cloudLeagueId: String? = null,
    val isOnline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
