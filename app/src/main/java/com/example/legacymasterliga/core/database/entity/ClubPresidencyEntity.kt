package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "club_presidencies",
    indices = [
        Index("clubId"),
        Index("userId")
    ]
)
data class ClubPresidencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clubId: Long,
    val userId: Long,
    val startAt: Long,
    val endAt: Long? = null,
)
