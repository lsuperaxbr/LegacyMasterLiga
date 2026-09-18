package com.example.legacymasterliga.feature.audit.domain

data class AuditLog(
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

data class AuditQuery(
    val leagueId: Long? = null,
    val category: String? = null,
    val action: String? = null,
    val text: String = "",
)
