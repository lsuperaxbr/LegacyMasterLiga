package com.example.legacymasterliga.feature.online.domain

import com.example.legacymasterliga.core.model.UserRole

data class CloudUserProfile(
    val firebaseUid: String = "",
    val localUserId: Long = 0,
    val username: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.VISITOR,
    val status: String = "ACTIVE",
    val cloudLeagueId: String? = null,
    val clubCloudId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
