package com.example.legacymasterliga.feature.online.domain

import com.example.legacymasterliga.core.model.UserRole

data class CloudLeague(
    val cloudLeagueId: String = "",
    val name: String = "",
    val ownerUid: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE",
    val season: Int = 1,
    val version: Int = 1,
    val visibility: String = "PUBLIC",
    val inviteMode: String = "CODE",
    val maxMembers: Int = 32,
    val cloudId: String = ""
)

data class CloudMember(
    val firebaseUid: String = "",
    val username: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.VISITOR,
    val clubCloudId: String? = null,
    val joinedAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE",
    val permissions: List<String> = emptyList(),
    val lastSeen: Long = System.currentTimeMillis(),
    val deviceCount: Int = 1,
    val inviteCode: String? = null,
    val cloudId: String = ""
)

data class CloudInvite(
    val inviteCode: String = "",
    val leagueId: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val uses: Int = 0,
    val maxUses: Int = 1,
    val status: String = "ACTIVE"
)
