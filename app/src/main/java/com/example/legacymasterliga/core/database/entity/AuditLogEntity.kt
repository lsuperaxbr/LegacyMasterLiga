package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    indices = [
        Index("leagueId"),
        Index("actorUserId"),
        Index("category"),
        Index("action"),
        Index("entityType"),
        Index("entityId"),
        Index("createdAt"),
    ],
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long? = null,
    val actorUserId: Long? = null,
    val actorName: String,
    val actorRole: String,
    val category: String,
    val action: String,
    val entityType: String,
    val entityId: Long? = null,
    val summary: String,
    val details: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
