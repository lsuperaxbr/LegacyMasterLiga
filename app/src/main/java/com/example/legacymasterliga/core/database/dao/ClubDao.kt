package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.model.ClubProfileHeaderRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(club: ClubEntity): Long

    @Update
    suspend fun update(club: ClubEntity)

    @Query("SELECT * FROM clubs WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ClubEntity?>

    @Query("SELECT * FROM clubs WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ClubEntity?

    @Query("SELECT * FROM clubs WHERE leagueId = :leagueId AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByLeagueAndName(leagueId: Long, name: String): ClubEntity?

    @Query(
        """
        SELECT clubs.id AS clubId,
               clubs.leagueId AS leagueId,
               clubs.name AS clubName,
               clubs.crestUri AS crestUri,
               clubs.presidentUserId AS presidentUserId,
               users.displayName AS presidentName,
               clubs.isActive AS isActive
        FROM clubs
        LEFT JOIN users ON users.id = clubs.presidentUserId
        WHERE clubs.id = :clubId
        LIMIT 1
        """,
    )
    fun observeProfileHeader(clubId: Long): Flow<ClubProfileHeaderRow?>

    @Query("SELECT * FROM clubs WHERE leagueId = :leagueId AND isActive = 1 ORDER BY isBank DESC, name COLLATE NOCASE")
    fun observeActiveByLeague(leagueId: Long): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs WHERE leagueId = :leagueId ORDER BY isBank DESC, isActive DESC, name COLLATE NOCASE")
    fun observeManagedByLeague(leagueId: Long): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs WHERE leagueId = :leagueId AND isBank = 1 LIMIT 1")
    suspend fun findBankByLeague(leagueId: Long): ClubEntity?

    @Query("SELECT * FROM clubs WHERE presidentUserId = :userId AND isBank = 0 LIMIT 1")
    fun observeByPresident(userId: Long): Flow<ClubEntity?>

    @Query("SELECT * FROM clubs WHERE presidentUserId = :userId AND isBank = 0")
    fun observeAllByPresident(userId: Long): Flow<List<ClubEntity>>

    @Query("UPDATE clubs SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :clubId AND isBank = 0")
    suspend fun setActive(clubId: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("UPDATE clubs SET presidentUserId = NULL, updatedAt = :updatedAt WHERE presidentUserId = :userId")
    suspend fun clearPresidentAssignments(userId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE clubs SET presidentUserId = :userId, updatedAt = :updatedAt WHERE id = :clubId AND isBank = 0")
    suspend fun assignPresident(clubId: Long, userId: Long?, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("SELECT COUNT(*) FROM clubs WHERE isActive = 1 AND isBank = 0")
    fun observeActiveClubCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM clubs WHERE crestUri = :uri")
    suspend fun countCrestUsages(uri: String): Int

    @Query("DELETE FROM clubs WHERE id = :clubId AND isBank = 0")
    suspend fun deleteById(clubId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM competition_participants WHERE clubId = :clubId)")
    suspend fun isUsedInCompetitions(clubId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM financial_transactions WHERE clubId = :clubId OR counterpartyClubId = :clubId)")
    suspend fun hasFinancialHistory(clubId: Long): Boolean
}
