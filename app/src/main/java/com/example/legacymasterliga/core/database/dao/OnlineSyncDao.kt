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
import kotlinx.coroutines.flow.combine

@Dao
interface OnlineSyncDao {

    @Query("""
        SELECT 'COMPETITION' AS entityType, c.id AS localId, l.cloudLeagueId AS cloudLeagueId, c.updatedAt AS updatedAt
        FROM competitions c JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeCompetitionCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'CLUB' AS entityType, cl.id AS localId, l.cloudLeagueId AS cloudLeagueId, cl.updatedAt AS updatedAt
        FROM clubs cl JOIN leagues l ON l.id = cl.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeClubCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'SEASON' AS entityType, s.id AS localId, l.cloudLeagueId AS cloudLeagueId, s.updatedAt AS updatedAt
        FROM seasons s JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeSeasonCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'PARTICIPANT' AS entityType, p.id AS localId, l.cloudLeagueId AS cloudLeagueId, p.createdAt AS updatedAt
        FROM competition_participants p JOIN seasons s ON s.id = p.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeParticipantCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'ROUND' AS entityType, r.id AS localId, l.cloudLeagueId AS cloudLeagueId, r.updatedAt AS updatedAt
        FROM rounds r JOIN seasons s ON s.id = r.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeRoundCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'MATCH' AS entityType, m.id AS localId, l.cloudLeagueId AS cloudLeagueId, m.updatedAt AS updatedAt
        FROM matches m JOIN seasons s ON s.id = m.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeMatchCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'FINANCE' AS entityType, ft.id AS localId, l.cloudLeagueId AS cloudLeagueId, ft.createdAt AS updatedAt
        FROM financial_transactions ft JOIN clubs cl ON cl.id = ft.clubId JOIN leagues l ON l.id = cl.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeFinanceCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'TRANSFER' AS entityType, t.id AS localId, l.cloudLeagueId AS cloudLeagueId, t.createdAt AS updatedAt
        FROM transfers t JOIN leagues l ON l.id = t.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeTransferCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'PLAYER' AS entityType, p.id AS localId, l.cloudLeagueId AS cloudLeagueId, p.updatedAt AS updatedAt
        FROM players p JOIN leagues l ON l.id = p.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observePlayerCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'NEWS' AS entityType, n.id AS localId, l.cloudLeagueId AS cloudLeagueId, n.publishedAt AS updatedAt
        FROM news n JOIN leagues l ON l.id = n.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeNewsCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'ARENA' AS entityType, d.id AS localId, l.cloudLeagueId AS cloudLeagueId, d.createdAt AS updatedAt
        FROM arena_duels d JOIN leagues l ON l.id = d.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeArenaCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'GOAL_EVENT' AS entityType, g.id AS localId, l.cloudLeagueId AS cloudLeagueId, g.createdAt AS updatedAt
        FROM goal_events g JOIN matches m ON m.id = g.matchId JOIN seasons s ON s.id = m.seasonId JOIN competitions c ON c.id = s.competitionId JOIN leagues l ON l.id = c.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeGoalEventCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'AUCTION_LOT' AS entityType, al.id AS localId, l.cloudLeagueId AS cloudLeagueId, al.createdAt AS updatedAt
        FROM auction_lots al JOIN leagues l ON l.id = al.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeAuctionLotCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'AUCTION_ITEM' AS entityType, ai.id AS localId, l.cloudLeagueId AS cloudLeagueId, ai.createdAt AS updatedAt
        FROM auction_items ai JOIN auction_lots al ON al.id = ai.lotId JOIN leagues l ON l.id = al.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeAuctionItemCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'AUCTION_BID' AS entityType, ab.id AS localId, l.cloudLeagueId AS cloudLeagueId, ab.createdAt AS updatedAt
        FROM auction_bids ab JOIN auction_items ai ON ai.id = ab.itemId JOIN auction_lots al ON al.id = ai.lotId JOIN leagues l ON l.id = al.leagueId
        WHERE l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL
    """)
    fun observeAuctionBidCandidates(): Flow<List<OnlineSyncCandidate>>

    @Query("""
        SELECT 'USER' AS entityType, u.id AS localId, 'GLOBAL' AS cloudLeagueId, u.updatedAt AS updatedAt
        FROM users u
        WHERE u.firebaseUid IS NOT NULL
    """)
    fun observeUserCandidates(): Flow<List<OnlineSyncCandidate>>

    /**
     * Performance optimization: Instead of a single giant UNION ALL query that re-runs whenever
     * any of the 16 tables change, we observe each table independently and combine the results.
     */
    fun observeCandidates(): Flow<List<OnlineSyncCandidate>> = combine(
        observeCompetitionCandidates(), observeClubCandidates(), observeSeasonCandidates(),
        observeParticipantCandidates(), observeRoundCandidates(), observeMatchCandidates(),
        observeFinanceCandidates(), observeTransferCandidates(), observePlayerCandidates(),
        observeNewsCandidates(), observeArenaCandidates(), observeGoalEventCandidates(),
        observeAuctionLotCandidates(), observeAuctionItemCandidates(), observeAuctionBidCandidates(),
        observeUserCandidates(),
    ) { arrays -> arrays.toList().flatten() }

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

    @Query("SELECT * FROM online_sync_queue WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun observePendingQueue(): Flow<List<OnlineSyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM online_sync_records WHERE status = 'CONFLICT'")
    fun observeConflictCount(): Flow<Int>

    @Query("SELECT * FROM online_sync_records WHERE status = 'CONFLICT'")
    suspend fun findConflicts(): List<OnlineSyncRecordEntity>

    @Query("DELETE FROM online_sync_queue WHERE entityType = :type AND localId = :localId AND status = 'CONFLICT'")
    suspend fun deleteConflictQueue(type: String, localId: Long)

    @Query("SELECT COUNT(*) FROM online_sync_queue WHERE status = 'QUARANTINED'")
    fun observeQuarantinedCount(): Flow<Int>

    @Query("SELECT * FROM online_sync_queue WHERE status = 'QUARANTINED' ORDER BY updatedAt DESC")
    fun observeQuarantinedQueue(): Flow<List<OnlineSyncQueueEntity>>

    @Query("UPDATE online_sync_queue SET status = 'PENDING', attempts = 0, lastError = NULL, updatedAt = :updatedAt WHERE operationId = :operationId")
    suspend fun retryQuarantined(operationId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE online_sync_queue SET status = 'PENDING', attempts = 0, lastError = NULL, updatedAt = :updatedAt WHERE status = 'QUARANTINED'")
    suspend fun retryAllQuarantined(updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM online_sync_queue WHERE operationId = :operationId AND status = 'QUARANTINED'")
    suspend fun discardQuarantined(operationId: String)

    @Query("DELETE FROM online_sync_queue WHERE status = 'QUARANTINED'")
    suspend fun discardAllQuarantined()
}
