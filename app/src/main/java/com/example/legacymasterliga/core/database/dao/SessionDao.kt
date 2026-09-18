package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY lastAccessAt DESC LIMIT 1")
    fun observeCurrent(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions ORDER BY lastAccessAt DESC LIMIT 1")
    suspend fun findCurrent(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE tokenHash = :tokenHash LIMIT 1")
    suspend fun findByTokenHash(tokenHash: String): SessionEntity?

    @Query("DELETE FROM sessions WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Long)

    @Query("DELETE FROM sessions WHERE expiresAt <= :now")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM sessions")
    suspend fun clear()
}
