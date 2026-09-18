package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.OnlineSyncQueueEntity
import com.example.legacymasterliga.core.database.entity.OnlineSyncRecordEntity
import com.example.legacymasterliga.core.database.model.OnlineSyncCandidate
import kotlinx.coroutines.flow.Flow

@Dao
interface OnlineSyncDao {
    @Query(
        """
        SELECT 'COMPETITION' AS entityType, c.id AS localId, l.cloudLeagueId AS cloudLeagueId, c.updatedAt AS updatedAt
        FROM competitions c JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'CLUB', cl.id, l.cloudLeagueId, cl.updatedAt
        FROM clubs cl JOIN leagues l ON l.id = cl.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'SEASON', s.id, l.cloudLeagueId, s.updatedAt
        FROM seasons s JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'PARTICIPANT', p.id, l.cloudLeagueId, p.createdAt
        FROM competition_participants p JOIN seasons s ON s.id = p.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'ROUND', r.id, l.cloudLeagueId, r.updatedAt
        FROM rounds r JOIN seasons s ON s.id = r.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'MATCH', m.id, l.cloudLeagueId, m.updatedAt
        FROM matches m JOIN seasons s ON s.id = m.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'FINANCE', ft.id, l.cloudLeagueId, ft.createdAt
        FROM financial_transactions ft JOIN clubs cl ON cl.id = ft.clubId JOIN leagues l ON l.id = cl.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'TRANSFER', t.id, l.cloudLeagueId, t.createdAt
        FROM transfers t JOIN leagues l ON l.id = t.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'PLAYER', p.id, l.cloudLeagueId, p.updatedAt
        FROM players p JOIN leagues l ON l.id = p.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'NEWS', n.id, l.cloudLeagueId, n.publishedAt
        FROM news n JOIN leagues l ON l.id = n.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'ARENA', d.id, l.cloudLeagueId, d.createdAt
        FROM arena_duels d JOIN leagues l ON l.id = d.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'GOAL_EVENT', g.id, l.cloudLeagueId, g.createdAt
        FROM goal_events g JOIN matches m ON m.id = g.matchId JOIN seasons s ON s.id = m.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'AUCTION_LOT', al.id, l.cloudLeagueId, al.createdAt
        FROM auction_lots al JOIN leagues l ON l.id = al.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'AUCTION_ITEM', ai.id, l.cloudLeagueId, ai.createdAt
        FROM auction_items ai JOIN auction_lots al ON al.id = ai.lotId JOIN leagues l ON l.id = al.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'AUCTION_BID', ab.id, l.cloudLeagueId, ab.createdAt
        FROM auction_bids ab JOIN auction_items ai ON ai.id = ab.itemId JOIN auction_lots al ON al.id = ai.lotId JOIN leagues l ON l.id = al.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
        UNION ALL
        SELECT 'USER', u.id, 'GLOBAL', u.updatedAt
        FROM users u
        WHERE u.firebaseUid IS NOT NULL
        """,
    )
    fun observeCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("SELECT * FROM online_sync_records WHERE entityType = :type AND localId = :localId LIMIT 1")
    suspend fun findRecord(type: String, localId: Long): OnlineSyncRecordEntity?

    @Query("SELECT * FROM online_sync_records WHERE cloudLeagueId = :leagueId AND entityType = :type AND cloudId = :cloudId LIMIT 1")
    suspend fun findRecordByCloudId(leagueId: String, type: String, cloudId: String): OnlineSyncRecordEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: OnlineSyncRecordEntity): Long

    @Update
    suspend fun updateRecord(record: OnlineSyncRecordEntity)

    @Query("UPDATE online_sync_records SET lastLocalUpdatedAt = :updatedAt WHERE entityType = :type AND localId = :localId")
    suspend fun markLocalObserved(type: String, localId: Long, updatedAt: Long)

    @Query("SELECT * FROM online_sync_queue WHERE entityType = :type AND localId = :localId AND status IN ('PENDING', 'UPLOADING') LIMIT 1")
    suspend fun findPending(type: String, localId: Long): OnlineSyncQueueEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(operation: OnlineSyncQueueEntity): Long

    @Update
    suspend fun updateQueue(operation: OnlineSyncQueueEntity)

    @Query("SELECT * FROM online_sync_queue WHERE status = 'PENDING' ORDER BY createdAt LIMIT :limit")
    suspend fun findPendingBatch(limit: Int = 50): List<OnlineSyncQueueEntity>

    @Query("DELETE FROM online_sync_queue WHERE operationId = :operationId")
    suspend fun deleteQueue(operationId: String)

    @Query("SELECT COUNT(*) FROM online_sync_queue WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM online_sync_records WHERE status = 'CONFLICT'")
    fun observeConflictCount(): Flow<Int>

    @Query("SELECT * FROM online_sync_records WHERE status = 'CONFLICT'")
    suspend fun findConflicts(): List<OnlineSyncRecordEntity>

    @Query("DELETE FROM online_sync_queue WHERE entityType = :type AND localId = :localId AND status = 'CONFLICT'")
    suspend fun deleteConflictQueue(type: String, localId: Long)
}
