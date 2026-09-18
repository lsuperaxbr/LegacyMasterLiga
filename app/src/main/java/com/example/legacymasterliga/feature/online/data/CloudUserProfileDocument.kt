package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.network.OnlineProfileCorruptException
import com.example.legacymasterliga.core.network.OnlineProfileIncompleteException
import com.example.legacymasterliga.core.network.OnlineProfileIdentityMismatchException
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile

internal object CloudUserProfileDocument {
    const val COLLECTION = "user_profiles"

    fun payload(profile: CloudUserProfile): Map<String, Any?> = mapOf(
        "firebaseUid" to profile.firebaseUid,
        "localUserId" to profile.localUserId,
        "username" to profile.username,
        "displayName" to profile.displayName,
        "role" to profile.role.name,
        "status" to profile.status,
        "cloudLeagueId" to profile.cloudLeagueId,
        "clubCloudId" to profile.clubCloudId,
        "createdAt" to profile.createdAt,
        "updatedAt" to profile.updatedAt,
    )

    fun decode(uid: String, data: Map<String, Any?>): CloudUserProfile {
        val firebaseUid = data["firebaseUid"] as? String ?: uid
        if (firebaseUid != uid) throw OnlineProfileIdentityMismatchException(uid, firebaseUid)

        val username = data["username"] as? String ?: "user_${uid.take(5)}"
        val displayName = data["displayName"] as? String ?: "Membro"

        val roleStr = data["role"] as? String ?: "PRESIDENT"
        val role = UserRole.values().firstOrNull { it.name == roleStr } ?: UserRole.PRESIDENT
        
        val statusStr = data["status"] as? String ?: "ACTIVE"
        val status = AccountStatus.values().firstOrNull { it.name == statusStr }?.name ?: "ACTIVE"

        return CloudUserProfile(
            firebaseUid = firebaseUid,
            localUserId = (data["localUserId"] as? Number)?.toLong() ?: 0L,
            username = username,
            displayName = displayName,
            role = role,
            status = status,
            cloudLeagueId = data["cloudLeagueId"] as? String,
            clubCloudId = data["clubCloudId"] as? String,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        )
    }

    private fun Map<String, Any?>.string(uid: String, field: String): String =
        this[field] as? String ?: throw OnlineProfileCorruptException(uid, "$field deve ser String")

    private fun Map<String, Any?>.optionalString(field: String): String? =
        this[field] as? String

    private fun Map<String, Any?>.long(uid: String, field: String): Long =
        (this[field] as? Number)?.toLong()
            ?: throw OnlineProfileCorruptException(uid, "$field deve ser inteiro")

    private inline fun <reified T : Enum<T>> Map<String, Any?>.enumValue(uid: String, field: String): T {
        val value = string(uid, field)
        return enumValues<T>().firstOrNull { it.name == value }
            ?: throw OnlineProfileCorruptException(uid, "$field possui valor inválido: $value")
    }

    private val REQUIRED_FIELDS = setOf(
        "firebaseUid",
        "localUserId",
        "username",
        "displayName",
        "role",
        "status",
        "createdAt",
        "updatedAt",
    )
}
