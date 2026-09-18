package com.example.legacymasterliga.feature.online.domain

import com.example.legacymasterliga.core.network.OnlineProfileInactiveException
import javax.inject.Inject
import javax.inject.Singleton

interface OnlineLocalSessionGateway {
    suspend fun open(profile: CloudUserProfile): Long
    suspend fun clear()
}

@Singleton
class OnlineLoginService @Inject constructor(
    private val cloudAuthRepository: CloudAuthRepository,
    private val localSessionGateway: OnlineLocalSessionGateway,
) {
    suspend fun login(email: String, password: String): Result<CloudUserProfile> {
        val uid = cloudAuthRepository.signInOnline(email, password).getOrElse {
            return Result.failure(it)
        }
        return complete(uid)
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result<CloudUserProfile> {
        val uid = cloudAuthRepository.createOnlineAccount(email, password).getOrElse {
            return Result.failure(it)
        }
        // Atômico: criou no Auth, agora cria no Firestore
        return complete(uid, displayName, username)
    }

    suspend fun completeAuthenticatedProfile(
        displayName: String? = null,
        username: String? = null
    ): Result<CloudUserProfile> {
        val uid = cloudAuthRepository.currentFirebaseUid()
            ?: return Result.failure(IllegalStateException("Sessão online não encontrada. Entre novamente."))
        return complete(uid, displayName, username)
    }

    suspend fun restoreAuthenticatedSession(): Result<CloudUserProfile?> {
        val uid = cloudAuthRepository.currentFirebaseUid() ?: return Result.success(null)
        return complete(uid).map { it }
    }

    suspend fun logout() {
        localSessionGateway.clear()
        cloudAuthRepository.signOutOnline()
    }

    fun hasAuthenticatedFirebaseAccount(): Boolean = cloudAuthRepository.currentFirebaseUid() != null

    private suspend fun complete(
        uid: String,
        customDisplayName: String? = null,
        customUsername: String? = null
    ): Result<CloudUserProfile> = runCatching {
        val profile = cloudAuthRepository.getOrCreateProfile(
            uid = uid,
            customDisplayName = customDisplayName,
            customUsername = customUsername
        ).getOrThrow()
        requireActive(profile)
        // A sessão local só é aberta se o perfil Firestore for válido e estiver ativo.
        localSessionGateway.open(profile)
        profile
    }

    private fun requireActive(profile: CloudUserProfile) {
        if (profile.status != ACTIVE) throw OnlineProfileInactiveException()
    }

    private companion object {
        const val ACTIVE = "ACTIVE"
    }
}
