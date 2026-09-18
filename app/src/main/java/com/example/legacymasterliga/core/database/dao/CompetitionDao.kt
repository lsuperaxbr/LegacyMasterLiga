package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(competition: CompetitionEntity): Long

    @Update
    suspend fun update(competition: CompetitionEntity)

    @Query("SELECT * FROM competitions WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<CompetitionEntity?>

    @Query("SELECT * FROM competitions WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): CompetitionEntity?

    @Query("SELECT * FROM competitions WHERE leagueId = :leagueId AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByLeagueAndName(leagueId: Long, name: String): CompetitionEntity?

    @Query("SELECT * FROM competitions WHERE leagueId = :leagueId AND type = :type LIMIT 1")
    suspend fun findByLeagueAndType(leagueId: Long, type: com.example.legacymasterliga.core.model.CompetitionType): CompetitionEntity?

    @Query("SELECT * FROM competitions WHERE leagueId = :leagueId ORDER BY status, name COLLATE NOCASE")
    fun observeByLeague(leagueId: Long): Flow<List<CompetitionEntity>>

    @Query("SELECT COUNT(*) FROM competitions")
    fun observeCount(): Flow<Int>

    @Query("UPDATE competitions SET status = :status, updatedAt = :updatedAt WHERE id = :competitionId")
    suspend fun updateStatus(competitionId: Long, status: com.example.legacymasterliga.core.model.CompetitionStatus, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM competitions WHERE id = :competitionId")
    suspend fun deleteById(competitionId: Long)
}
