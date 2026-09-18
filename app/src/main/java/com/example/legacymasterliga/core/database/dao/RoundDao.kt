package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.model.RoundStatus
import com.example.legacymasterliga.core.database.entity.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Update
    suspend fun update(round: RoundEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(rounds: List<RoundEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(round: RoundEntity): Long

    @Query("SELECT * FROM rounds WHERE seasonId = :seasonId ORDER BY number")
    fun observeBySeason(seasonId: Long): Flow<List<RoundEntity>>

    @Query("SELECT COUNT(*) FROM rounds WHERE seasonId = :seasonId")
    suspend fun countBySeason(seasonId: Long): Int

    @Query("UPDATE rounds SET status = :status, updatedAt = :updatedAt WHERE id = :roundId")
    suspend fun updateStatus(
        roundId: Long,
        status: RoundStatus,
        updatedAt: Long = System.currentTimeMillis(),
    ): Int

    @Query("SELECT * FROM rounds WHERE id = :roundId LIMIT 1")
    suspend fun findById(roundId: Long): RoundEntity?

    @Query("SELECT * FROM rounds WHERE seasonId = :seasonId AND number = :number LIMIT 1")
    suspend fun findBySeasonAndNumber(seasonId: Long, number: Int): RoundEntity?

    @Query("SELECT * FROM rounds WHERE seasonId = :seasonId AND stageLabel = :label LIMIT 1")
    suspend fun findBySeasonAndLabel(seasonId: Long, label: String): RoundEntity?

    @Query("SELECT * FROM rounds WHERE seasonId = :seasonId AND stageLabel = :label ORDER BY number")
    suspend fun findAllBySeasonAndLabel(seasonId: Long, label: String): List<RoundEntity>

    @Query("SELECT * FROM rounds WHERE seasonId = :seasonId AND name = :name LIMIT 1")
    suspend fun findBySeasonAndName(seasonId: Long, name: String): RoundEntity?

    @Query("SELECT MAX(number) FROM rounds WHERE seasonId = :seasonId")
    suspend fun findMaxNumber(seasonId: Long): Int?

    @Query("DELETE FROM rounds WHERE seasonId = :seasonId")
    suspend fun deleteBySeason(seasonId: Long): Int

    @Query("DELETE FROM rounds WHERE seasonId = :seasonId AND number > :roundNumber")
    suspend fun deleteAfterNumber(seasonId: Long, roundNumber: Int): Int

    @Query("SELECT EXISTS(SELECT 1 FROM rounds WHERE seasonId = :seasonId AND stageLabel = :label)")
    suspend fun existsBySeasonAndLabel(seasonId: Long, label: String): Boolean
}
