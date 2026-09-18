package com.example.legacymasterliga.data.repository

import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.security.PasswordHasher
import com.example.legacymasterliga.core.session.SessionManager
import com.example.legacymasterliga.data.mapper.toDomain
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomAuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val sessionManager: SessionManager,
    private val auditLogger: AuditLogger,
) : AuthRepository {
    override val currentUser: Flow<User?> = sessionManager.currentUser

    override suspend fun login(username: String, password: CharArray): Result<User> = runCatching {
        val user = userDao.findByUsername(username.trim())
            ?: error("Usuário ou senha inválidos.")
        check(user.status == AccountStatus.ACTIVE) { "Esta conta não está ativa." }
        check(passwordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
            "Usuário ou senha inválidos."
        }
        sessionManager.createSession(user.id)
        auditLogger.log("SECURITY", "LOGIN", "USER", user.id, null, "Login realizado por ${user.displayName}", null)
        user.toDomain()
    }

    override suspend fun logout() {
        auditLogger.log("SECURITY", "LOGOUT", "SESSION", null, null, "Sessão encerrada", null)
        sessionManager.clearSession()
    }
}
