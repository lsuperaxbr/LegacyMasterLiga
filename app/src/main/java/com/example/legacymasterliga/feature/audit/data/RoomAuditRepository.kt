package com.example.legacymasterliga.feature.audit.data

import com.example.legacymasterliga.core.database.dao.AuditLogDao
import com.example.legacymasterliga.core.database.dao.SessionDao
import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.entity.AuditLogEntity
import com.example.legacymasterliga.feature.audit.domain.AuditLog
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.audit.domain.AuditQuery
import com.example.legacymasterliga.feature.audit.domain.AuditRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomAuditRepository @Inject constructor(
    private val auditLogDao: AuditLogDao,
    private val sessionDao: SessionDao,
    private val userDao: UserDao,
) : AuditRepository, AuditLogger {
    override fun observeLogs(query: AuditQuery): Flow<List<AuditLog>> =
        auditLogDao.observeFiltered(query.leagueId, query.category, query.action, query.text.trim()).map { rows ->
            rows.map { row ->
                AuditLog(row.id, row.leagueId, row.leagueName, row.actorUserId, row.actorName, row.actorRole,
                    row.category, row.action, row.entityType, row.entityId, row.summary, row.details, row.createdAt)
            }
        }

    override fun observeCategories(): Flow<List<String>> = auditLogDao.observeCategories()
    override fun observeActions(): Flow<List<String>> = auditLogDao.observeActions()
    override fun observeCount(): Flow<Int> = auditLogDao.observeCount()

    override suspend fun log(
        category: String,
        action: String,
        entityType: String,
        entityId: Long?,
        leagueId: Long?,
        summary: String,
        details: String?,
    ) {
        val session = sessionDao.findCurrent()
        val actor = session?.let { userDao.findById(it.userId) }
        auditLogDao.insert(
            AuditLogEntity(
                leagueId = leagueId,
                actorUserId = actor?.id,
                actorName = actor?.displayName ?: "Sistema",
                actorRole = actor?.role?.name ?: "SYSTEM",
                category = category,
                action = action,
                entityType = entityType,
                entityId = entityId,
                summary = summary,
                details = details,
            ),
        )
    }
}
