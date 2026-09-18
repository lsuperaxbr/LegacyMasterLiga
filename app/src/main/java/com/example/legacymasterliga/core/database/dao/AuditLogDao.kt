package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.legacymasterliga.core.database.entity.AuditLogEntity
import com.example.legacymasterliga.core.database.model.AuditLogRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insert(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AuditLogEntity?

    @Query(
        """
        SELECT a.id, a.leagueId, l.name AS leagueName, a.actorUserId, a.actorName, a.actorRole,
               a.category, a.action, a.entityType, a.entityId, a.summary, a.details, a.createdAt
        FROM audit_logs a
        LEFT JOIN leagues l ON l.id = a.leagueId
        WHERE (:leagueId IS NULL OR a.leagueId = :leagueId)
          AND (:category IS NULL OR a.category = :category)
          AND (:action IS NULL OR a.action = :action)
          AND (:query = '' OR a.summary LIKE '%' || :query || '%' COLLATE NOCASE
               OR IFNULL(a.details, '') LIKE '%' || :query || '%' COLLATE NOCASE
               OR a.actorName LIKE '%' || :query || '%' COLLATE NOCASE
               OR a.entityType LIKE '%' || :query || '%' COLLATE NOCASE)
        ORDER BY a.createdAt DESC, a.id DESC
        """,
    )
    fun observeFiltered(
        leagueId: Long?,
        category: String?,
        action: String?,
        query: String,
    ): Flow<List<AuditLogRow>>

    @Query("SELECT DISTINCT category FROM audit_logs ORDER BY category")
    fun observeCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT action FROM audit_logs ORDER BY action")
    fun observeActions(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM audit_logs")
    fun observeCount(): Flow<Int>
}
