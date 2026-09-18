package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["tokenHash"], unique = true),
        Index(value = ["expiresAt"]),
    ],
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val tokenHash: String,
    val loginSource: String = "LOCAL",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val lastAccessAt: Long = System.currentTimeMillis(),
)
