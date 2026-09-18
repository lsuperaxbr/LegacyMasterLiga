package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(league: LeagueEntity): Long

    @Update
    suspend fun update(league: LeagueEntity)

    @Query("SELECT * FROM leagues WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<LeagueEntity?>

    @Query("SELECT * FROM leagues WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): LeagueEntity?

    @Query("SELECT * FROM leagues WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): LeagueEntity?

    @Query("SELECT * FROM leagues WHERE cloudLeagueId = :cloudId LIMIT 1")
    suspend fun findByCloudId(cloudId: String): LeagueEntity?

    @Query("SELECT * FROM leagues ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<LeagueEntity>>

    @Query("SELECT * FROM leagues WHERE isOnline = 1 AND cloudLeagueId IS NOT NULL ORDER BY name COLLATE NOCASE")
    fun observeOnline(): Flow<List<LeagueEntity>>

    @Query("UPDATE leagues SET cloudLeagueId = :cloudId, isOnline = 1, updatedAt = :now WHERE id = :localId")
    suspend fun linkCloudId(localId: Long, cloudId: String, now: Long = System.currentTimeMillis()): Int
}
