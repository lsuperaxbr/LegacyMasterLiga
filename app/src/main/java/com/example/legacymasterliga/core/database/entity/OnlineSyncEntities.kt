package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "online_sync_records",
    primaryKeys = ["entityType", "localId"],
    indices = [
        Index(value = ["cloudLeagueId", "entityType", "cloudId"], unique = true),
        Index(value = ["status"]),
    ],
)
data class OnlineSyncRecordEntity(
    val entityType: String,
    val localId: Long,
    val cloudLeagueId: String,
    val cloudId: String,
    val revision: Long = 0,
    val lastLocalUpdatedAt: Long = 0,
    val lastRemoteUpdatedAt: Long = 0,
    val status: String = "SYNCED",
    val conflictPayload: String? = null,
)

@Entity(
    tableName = "online_sync_queue",
    indices = [
        Index(value = ["entityType", "localId", "status"]),
        Index(value = ["cloudLeagueId", "status", "createdAt"]),
    ],
)
data class OnlineSyncQueueEntity(
    @androidx.room.PrimaryKey
    val operationId: String,
    val entityType: String,
    val localId: Long,
    val cloudLeagueId: String,
    val baseRevision: Long,
    val status: String = "PENDING",
    val attempts: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
