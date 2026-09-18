package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.session.SessionManager
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile
import com.example.legacymasterliga.feature.online.domain.OnlineLocalSessionGateway
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomOnlineLocalSessionGateway @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
) : OnlineLocalSessionGateway {

    override suspend fun open(profile: CloudUserProfile): Long {
        require(profile.firebaseUid.isNotBlank()) { "Perfil online sem Firebase UID." }

        var localUser = userDao.findByFirebaseUid(profile.firebaseUid)
        if (localUser == null) {
            val sameInstallationUser = if (profile.localUserId > 0L) {
                userDao.findById(profile.localUserId)
                    ?.takeIf { 
                        it.firebaseUid == null && 
                        it.username.equals(profile.username, ignoreCase = true) &&
                        it.role == profile.role // Segurança: impede escalada de privilégio por vínculo de ID
                    }
            } else {
                null
            }

            localUser = if (sameInstallationUser != null) {
                userDao.linkFirebase(sameInstallationUser.id, profile.firebaseUid)
                // Atualiza o nome/apelido no Room se o perfil Firestore tiver dados mais frescos
                userDao.updateProfile(
                    userId = sameInstallationUser.id,
                    displayName = profile.displayName,
                    role = profile.role,
                    status = sameInstallationUser.status
                )
                userDao.findById(sameInstallationUser.id)
            } else {
                val safeUsername = availableUsername(profile.username, profile.firebaseUid)
                val id = userDao.insert(
                    UserEntity(
                        username = safeUsername,
                        displayName = profile.displayName.ifBlank { safeUsername },
                        passwordHash = "",
                        passwordSalt = "",
                        role = profile.role,
                        status = AccountStatus.ACTIVE,
                        firebaseUid = profile.firebaseUid,
                    ),
                )
                userDao.findById(id)
            }
        }

        val user = checkNotNull(localUser) { "Não foi possível criar o usuário local da sessão online." }
        check(user.status == AccountStatus.ACTIVE) { "A conta local vinculada está desativada." }
        sessionManager.createSession(user.id, ONLINE)
        return user.id
    }

    override suspend fun clear() {
        sessionManager.clearSession()
    }

    private suspend fun availableUsername(requested: String, uid: String): String {
        val base = requested.ifBlank { "presidente_${uid.take(8)}" }
        val existing = userDao.findByUsername(base)
        if (existing == null || existing.firebaseUid == uid) return base

        val suffix = uid.take(8)
        var candidate = "${base.take(38)}_$suffix"
        var sequence = 2
        while (userDao.findByUsername(candidate) != null) {
            candidate = "${base.take(34)}_${suffix}_$sequence"
            sequence++
        }
        return candidate
    }

    private companion object {
        const val ONLINE = "ONLINE"
    }
}
