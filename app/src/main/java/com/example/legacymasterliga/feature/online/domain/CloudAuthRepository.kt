package com.example.legacymasterliga.feature.online.domain

import kotlinx.coroutines.flow.Flow

interface CloudAuthRepository {
    suspend fun createOnlineAccount(email: String, password: String): Result<String>
    suspend fun registerOnlineAccount(
        email: String,
        password: String,
        displayName: String,
        username: String,
        localUserId: Long
    ): Result<String>
    suspend fun signInOnline(email: String, password: String): Result<String>
    suspend fun fetchProfile(uid: String): Result<CloudUserProfile?>
    suspend fun getOrCreateProfile(
        uid: String,
        customDisplayName: String? = null,
        customUsername: String? = null,
        preferredLocalUserId: Long? = null
    ): Result<CloudUserProfile>
    suspend fun signOutOnline()
    fun currentFirebaseUid(): String?
    fun observeOnlineProfile(): Flow<CloudUserProfile?>
    suspend fun linkLocalUser(localUserId: Long, firebaseUid: String): Result<Unit>
}
