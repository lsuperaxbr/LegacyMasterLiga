package com.example.legacymasterliga.domain.repository

import com.example.legacymasterliga.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(username: String, password: CharArray): Result<User>
    suspend fun logout()
}
