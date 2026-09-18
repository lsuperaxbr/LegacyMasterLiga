package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile

object CloudUserProfileFactory {
    fun create(
        uid: String,
        email: String?,
        firebaseDisplayName: String?,
        localUser: UserEntity?,
        customDisplayName: String? = null,
        customUsername: String? = null,
        now: Long = System.currentTimeMillis(),
    ): CloudUserProfile {
        require(uid.isNotBlank()) { "Não é possível criar user_profile sem Firebase UID." }

        val emailName = email.orEmpty().substringBefore('@')
        val readableName = customDisplayName?.trim()?.takeUnless { it.isBlank() }
            ?: firebaseDisplayName?.trim()?.takeUnless { it.isBlank() }
            ?: localUser?.displayName?.trim()?.takeUnless { it.isBlank() }
            ?: emailName.takeUnless { it.isBlank() }
            ?: "Presidente"
        val username = customUsername?.trim()?.takeUnless { it.isBlank() }
            ?: localUser?.username?.trim()?.takeUnless { it.isBlank() }
            ?: cloudUsername(emailName, uid)

        return CloudUserProfile(
            firebaseUid = uid,
            localUserId = localUser?.id ?: 0L,
            username = username,
            displayName = readableName,
            // Client bootstrap always starts with the least-privileged online role.
            // Administrative assignment remains a server/console responsibility.
            role = UserRole.PRESIDENT,
            status = ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun cloudUsername(emailName: String, uid: String): String {
        val base = emailName
            .lowercase()
            .replace(NON_USERNAME_CHARACTER, "_")
            .trim('_')
            .take(24)
            .ifBlank { "presidente" }
        return "${base}_${uid.take(8)}"
    }

    private val NON_USERNAME_CHARACTER = Regex("[^a-z0-9_]")
    private const val ACTIVE = "ACTIVE"
}
