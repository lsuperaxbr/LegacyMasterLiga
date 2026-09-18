package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "arena_duels",
    indices = [Index("leagueId")],
)
data class ArenaDuelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long,
    val clubAId: Long,
    val clubBId: Long,
    val stakeCr: Long,
    val status: String, // "PENDING" ou "RESOLVED"
    val resultType: String? = null, // "CLUB_A_WIN", "CLUB_B_WIN", "DRAW"
    val note: String? = null,
    val createdByUserId: Long? = null,
    val createdAt: Long,
    val resolvedAt: Long? = null,
)
