package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.TransferEntity
import com.example.legacymasterliga.core.database.model.TransferHistoryRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transfer: TransferEntity): Long

    @Update
    suspend fun update(transfer: TransferEntity)

    @Query("SELECT * FROM transfers WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TransferEntity?

    @Query("""
        SELECT t.id, t.leagueId, t.playerName, t.originClubId, origin.name AS originClubName,
               t.destinationClubId, destination.name AS destinationClubName, t.valueCr,
               t.type, t.swapId, t.seasonId, t.note, t.createdAt
        FROM transfers t
        JOIN clubs origin ON origin.id = t.originClubId
        JOIN clubs destination ON destination.id = t.destinationClubId
        WHERE t.leagueId = :leagueId
        ORDER BY t.createdAt DESC, t.id DESC
    """)
    fun observeByLeague(leagueId: Long): Flow<List<TransferHistoryRow>>

    @Query("""
        SELECT t.id, t.leagueId, t.playerName, t.originClubId, origin.name AS originClubName,
               t.destinationClubId, destination.name AS destinationClubName, t.valueCr,
               t.type, t.swapId, t.seasonId, t.note, t.createdAt
        FROM transfers t
        JOIN clubs origin ON origin.id = t.originClubId
        JOIN clubs destination ON destination.id = t.destinationClubId
        WHERE t.leagueId = :leagueId
          AND (t.originClubId = :clubId OR t.destinationClubId = :clubId)
        ORDER BY t.createdAt DESC, t.id DESC
    """)
    fun observeByClub(leagueId: Long, clubId: Long): Flow<List<TransferHistoryRow>>

    @Query("SELECT COUNT(*) FROM transfers")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(valueCr), 0) FROM transfers")
    fun observeTotalMoved(): Flow<Long>

    @Query("""
        SELECT sc.seasonId, s.name AS seasonName, c.name AS competitionName, c.type AS competitionType, sc.closedAt
        FROM season_closures sc
        JOIN seasons s ON s.id = sc.seasonId
        JOIN competitions c ON c.id = s.competitionId
        WHERE sc.championClubId = :clubId
        ORDER BY sc.closedAt DESC
    """)
    fun observeTrophiesByClub(clubId: Long): Flow<List<ClubTrophyRow>>
}

data class ClubTrophyRow(
    val seasonId: Long,
    val seasonName: String,
    val competitionName: String,
    val competitionType: String,
    val closedAt: Long
)
