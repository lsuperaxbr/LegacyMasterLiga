package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.legacymasterliga.core.database.entity.NotificationReadEntity
import com.example.legacymasterliga.core.database.model.NotificationRow
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query(
        """
        SELECT n.id, n.leagueId, l.name AS leagueName, n.category, n.title, n.message,
               n.priority, n.destinationRoute, n.createdAt,
               CASE WHEN r.notificationId IS NULL THEN 0 ELSE 1 END AS isRead
        FROM notifications n
        LEFT JOIN leagues l ON l.id = n.leagueId
        LEFT JOIN notification_reads r ON r.notificationId = n.id AND r.userId = :userId
        WHERE (:leagueId IS NULL OR n.leagueId = :leagueId)
          AND (:category IS NULL OR n.category = :category)
          AND (:readState = 'ALL' OR (:readState = 'READ' AND r.notificationId IS NOT NULL)
               OR (:readState = 'UNREAD' AND r.notificationId IS NULL))
          AND (n.audience = 'ALL' OR n.audience = :role)
        ORDER BY CASE n.priority WHEN 'HIGH' THEN 0 WHEN 'NORMAL' THEN 1 ELSE 2 END,
                 n.createdAt DESC, n.id DESC
        """,
    )
    fun observeFiltered(
        userId: Long,
        role: String,
        leagueId: Long?,
        category: String?,
        readState: String,
    ): Flow<List<NotificationRow>>

    @Query(
        """
        SELECT COUNT(*) FROM notifications n
        LEFT JOIN notification_reads r ON r.notificationId = n.id AND r.userId = :userId
        WHERE r.notificationId IS NULL
          AND (n.audience = 'ALL' OR n.audience = :role)
        """,
    )
    fun observeUnreadCount(userId: Long, role: String): Flow<Int>

    @Query("SELECT DISTINCT category FROM notifications ORDER BY category")
    fun observeCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markRead(read: NotificationReadEntity)

    @Query("DELETE FROM notification_reads WHERE notificationId = :notificationId AND userId = :userId")
    suspend fun markUnread(notificationId: Long, userId: Long)

    @Query(
        """
        INSERT OR IGNORE INTO notification_reads(notificationId, userId, readAt)
        SELECT n.id, :userId, :now FROM notifications n
        WHERE (n.audience = 'ALL' OR n.audience = :role)
        """,
    )
    suspend fun markAllRead(userId: Long, role: String, now: Long)

    @Query(
        """
        DELETE FROM notifications
        WHERE sourceKey LIKE 'PENDING_ROUND:%'
          AND sourceKey NOT IN (
              SELECT 'PENDING_ROUND:' || r.id
              FROM rounds r
              JOIN seasons s ON s.id = r.seasonId
              JOIN matches m ON m.roundId = r.id AND m.status = 'SCHEDULED'
              WHERE s.status = 'ACTIVE'
              GROUP BY r.id
          )
        """,
    )
    suspend fun removeResolvedPendingResults()

    @Query(
        """
        INSERT OR IGNORE INTO notifications
            (leagueId, category, title, message, audience, priority, sourceKey, destinationRoute, createdAt)
        SELECT c.leagueId, 'RESULTADOS', 'Resultados pendentes',
               r.name || ' possui ' || COUNT(m.id) || ' jogo(s) aguardando placar.',
               'ADMINISTRATOR', 'HIGH', 'PENDING_ROUND:' || r.id, 'schedule', :now
        FROM rounds r
        JOIN seasons s ON s.id = r.seasonId
        JOIN competitions c ON c.id = s.competitionId
        JOIN matches m ON m.roundId = r.id AND m.status = 'SCHEDULED'
        WHERE s.status = 'ACTIVE'
        GROUP BY r.id, c.leagueId, r.name
        """,
    )
    suspend fun syncPendingResults(now: Long)

    @Query(
        """
        INSERT OR IGNORE INTO notifications
            (leagueId, category, title, message, audience, priority, sourceKey, destinationRoute, createdAt)
        SELECT t.leagueId, 'MERCADO', 'Transferência registrada',
               t.playerName || ': ' || origin.name || ' → ' || destination.name || ' por ' || t.valueCr || ' CR.',
               'ALL', 'NORMAL', 'TRANSFER:' || t.id, 'news', t.createdAt
        FROM transfers t
        JOIN clubs origin ON origin.id = t.originClubId
        JOIN clubs destination ON destination.id = t.destinationClubId
        """,
    )
    suspend fun syncTransfers()

    @Query(
        """
        INSERT OR IGNORE INTO notifications
            (leagueId, category, title, message, audience, priority, sourceKey, destinationRoute, createdAt)
        SELECT c.leagueId,
               'RESULTADOS',
               home.name || ' ' || m.homeScore || ' x ' || m.awayScore || ' ' || away.name,
               'Rodada ' || r.number || ' — ' || comp.name || ' · ' || s.name,
               'ALL',
               'NORMAL',
               'MATCH_RESULT:' || m.id,
               'schedule',
               m.updatedAt
        FROM matches m
        JOIN rounds r ON r.id = m.roundId
        JOIN seasons s ON s.id = r.seasonId
        JOIN competitions comp ON comp.id = s.competitionId
        JOIN clubs home ON home.id = m.homeClubId
        JOIN clubs away ON away.id = m.awayClubId
        JOIN competitions c ON c.id = s.competitionId
        WHERE m.status = 'FINISHED'
        """,
    )
    suspend fun syncMatchResults()

    @Query(
        """
        INSERT OR IGNORE INTO notifications
            (leagueId, category, title, message, audience, priority, sourceKey, destinationRoute, createdAt)
        SELECT leagueId,
               CASE eventType WHEN 'LEADERSHIP' THEN 'LIDERANÇA' WHEN 'CHAMPION' THEN 'ENCERRAMENTO' ELSE 'NOTÍCIAS' END,
               title, body, 'ALL',
               CASE eventType WHEN 'CHAMPION' THEN 'HIGH' WHEN 'LEADERSHIP' THEN 'HIGH' ELSE 'NORMAL' END,
               'NEWS:' || id, 'news', publishedAt
        FROM news
        WHERE eventType IN ('LEADERSHIP', 'CHAMPION')
        """,
    )
    suspend fun syncImportantNews()

    @Query(
        """
        INSERT OR IGNORE INTO notifications
            (leagueId, category, title, message, audience, priority, sourceKey, destinationRoute, createdAt)
        SELECT leagueId, 'BACKUP', summary, COALESCE(details, 'Ação de backup registrada.'),
               'ADMINISTRATOR', 'NORMAL', 'AUDIT:' || id, 'backup', createdAt
        FROM audit_logs WHERE category = 'BACKUP'
        """,
    )
    suspend fun syncBackups()

    @Query(
        """
        INSERT OR IGNORE INTO notifications
            (leagueId, category, title, message, audience, priority, sourceKey, destinationRoute, createdAt)
        SELECT leagueId, 'ADMINISTRAÇÃO', summary, COALESCE(details, actorName || ' executou ' || action || '.'),
               'ADMINISTRATOR', 'LOW', 'AUDIT:' || id, 'audit', createdAt
        FROM audit_logs
        WHERE category IN ('USERS', 'COMPETITIONS', 'SETTINGS', 'CLUBS', 'SECURITY')
        """,
    )
    suspend fun syncAdministrativeActions()

    @Transaction
    suspend fun synchronize(now: Long = System.currentTimeMillis()) {
        removeResolvedPendingResults()
        syncPendingResults(now)
        syncTransfers()
        syncMatchResults()
        syncImportantNews()
        syncBackups()
        syncAdministrativeActions()
    }
}
