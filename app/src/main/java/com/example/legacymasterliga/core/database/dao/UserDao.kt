package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY displayName COLLATE NOCASE")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("UPDATE users SET displayName = :displayName, role = :role, status = :status, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateProfile(
        userId: Long,
        displayName: String,
        role: UserRole,
        status: AccountStatus,
        updatedAt: Long = System.currentTimeMillis(),
    ): Int

    @Query("UPDATE users SET passwordHash = :passwordHash, passwordSalt = :passwordSalt, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updatePassword(
        userId: Long,
        passwordHash: String,
        passwordSalt: String,
        updatedAt: Long = System.currentTimeMillis(),
    ): Int

    @Query("UPDATE users SET firebaseUid = :uid, updatedAt = :now WHERE id = :userId")
    suspend fun linkFirebase(
        userId: Long,
        uid: String,
        now: Long = System.currentTimeMillis()
    ): Int

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    suspend fun findByFirebaseUid(uid: String): UserEntity?
}
