package com.example.legacymasterliga.core.database.model

data class AuditLogRow(
    val id: Long,
    val leagueId: Long?,
    val leagueName: String?,
    val actorUserId: Long?,
    val actorName: String,
    val actorRole: String,
    val category: String,
    val action: String,
    val entityType: String,
    val entityId: Long?,
    val summary: String,
    val details: String?,
    val createdAt: Long,
)
