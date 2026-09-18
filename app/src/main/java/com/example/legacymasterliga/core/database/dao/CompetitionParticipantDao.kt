package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.CompetitionParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionParticipantDao {
    @Update
    suspend fun update(participant: CompetitionParticipantEntity)

    @Query("SELECT * FROM competition_participants WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): CompetitionParticipantEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(participant: CompetitionParticipantEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(participants: List<CompetitionParticipantEntity>): List<Long>

    @Query("DELETE FROM competition_participants WHERE seasonId = :seasonId AND clubId = :clubId")
    suspend fun deleteBySeasonAndClub(seasonId: Long, clubId: Long): Int

    @Query("SELECT * FROM competition_participants WHERE seasonId = :seasonId ORDER BY seed IS NULL, seed, createdAt")
    fun observeBySeason(seasonId: Long): Flow<List<CompetitionParticipantEntity>>

    @Query("SELECT * FROM competition_participants WHERE seasonId = :seasonId AND clubId = :clubId LIMIT 1")
    suspend fun find(seasonId: Long, clubId: Long): CompetitionParticipantEntity?

    @Query("SELECT COUNT(*) FROM competition_participants WHERE seasonId = :seasonId AND isActive = 1")
    fun observeActiveCount(seasonId: Long): Flow<Int>

    @Query("SELECT * FROM competition_participants WHERE seasonId = :seasonId AND isActive = 1 ORDER BY seed IS NULL, seed, createdAt")
    suspend fun findActiveBySeason(seasonId: Long): List<CompetitionParticipantEntity>
}
