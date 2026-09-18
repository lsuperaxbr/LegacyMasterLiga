package com.example.legacymasterliga.feature.audit.domain

import kotlinx.coroutines.flow.Flow

interface AuditRepository {
    fun observeLogs(query: AuditQuery): Flow<List<AuditLog>>
    fun observeCategories(): Flow<List<String>>
    fun observeActions(): Flow<List<String>>
    fun observeCount(): Flow<Int>
}

interface AuditLogger {
    suspend fun log(
        category: String,
        action: String,
        entityType: String,
        entityId: Long? = null,
        leagueId: Long? = null,
        summary: String,
        details: String? = null,
    )
}
