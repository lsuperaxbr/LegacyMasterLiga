package com.example.legacymasterliga.domain.repository

import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeAll(): Flow<List<User>>
    suspend fun findById(id: Long): User?
    suspend fun findByUsername(username: String): User?
    suspend fun create(
        username: String,
        displayName: String,
        passwordHash: String,
        passwordSalt: String,
        role: UserRole,
    ): Long
    suspend fun updateProfile(userId: Long, displayName: String, role: UserRole, status: AccountStatus)
    suspend fun updatePassword(userId: Long, passwordHash: String, passwordSalt: String)
}
