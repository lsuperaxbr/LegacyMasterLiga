package com.example.legacymasterliga.core.session

import com.example.legacymasterliga.core.database.dao.SessionDao
import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.entity.SessionEntity
import com.example.legacymasterliga.data.mapper.toDomain
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.domain.model.User
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SessionManager {
    val currentUser: Flow<User?>
    suspend fun createSession(userId: Long, source: String = "LOCAL")
    suspend fun clearSession()
}

@Singleton
class RoomSessionManager @Inject constructor(
    private val sessionDao: SessionDao,
    private val userDao: UserDao,
    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth,
) : SessionManager {
    override val currentUser: Flow<User?> = sessionDao.observeCurrent().map { session ->
        if (session == null || session.expiresAt <= System.currentTimeMillis()) {
            null
        } else {
            val user = userDao.findById(session.userId)
            if (user == null || user.status != AccountStatus.ACTIVE) {
                null
            } else {
                User(
                    id = user.id,
                    username = user.username,
                    displayName = user.displayName,
                    role = user.role,
                    status = user.status,
                    isOnlineSession = session.loginSource == "ONLINE"
                )
            }
        }
    }

    override suspend fun createSession(userId: Long, source: String) {
        sessionDao.clear()
        val now = System.currentTimeMillis()
        val rawToken = ByteArray(32).also(SecureRandom()::nextBytes)
        val tokenHash = MessageDigest.getInstance("SHA-256")
            .digest(rawToken)
            .let { Base64.encodeToString(it, Base64.NO_WRAP) }
        sessionDao.insert(
            SessionEntity(
                userId = userId,
                tokenHash = tokenHash,
                loginSource = source,
                createdAt = now,
                expiresAt = now + SESSION_DURATION_MS,
                lastAccessAt = now,
            ),
        )
    }

    override suspend fun clearSession() {
        sessionDao.clear()
        firebaseAuth.signOut()
    }

    private companion object {
        const val SESSION_DURATION_MS = 30L * 24L * 60L * 60L * 1000L
    }
}
