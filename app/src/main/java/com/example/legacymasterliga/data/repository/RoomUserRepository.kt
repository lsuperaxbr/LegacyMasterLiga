package com.example.legacymasterliga.data.repository

import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.data.mapper.toDomain
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomUserRepository @Inject constructor(
    private val userDao: UserDao,
    private val auditLogger: AuditLogger,
) : UserRepository {
    override fun observeAll(): Flow<List<User>> =
        userDao.observeAll().map { users -> users.map { it.toDomain() } }

    override suspend fun findById(id: Long): User? = userDao.findById(id)?.toDomain()

    override suspend fun findByUsername(username: String): User? =
        userDao.findByUsername(username.trim())?.toDomain()

    override suspend fun create(
        username: String,
        displayName: String,
        passwordHash: String,
        passwordSalt: String,
        role: UserRole,
    ): Long {
        val id = userDao.insert(
            UserEntity(
                username = username.trim(),
                displayName = displayName.trim(),
                passwordHash = passwordHash,
                passwordSalt = passwordSalt,
                role = role,
            ),
        )
        auditLogger.log("USERS", "USER_CREATED", "USER", id, null, "Usuário ${displayName.trim()} criado", "Perfil: $role")
        return id
    }

    override suspend fun updateProfile(
        userId: Long,
        displayName: String,
        role: UserRole,
        status: AccountStatus,
    ) {
        check(userDao.updateProfile(userId, displayName.trim(), role, status) == 1) {
            "Usuário não encontrado."
        }
        auditLogger.log("USERS", "USER_UPDATED", "USER", userId, null, "Usuário ${displayName.trim()} atualizado", "Perfil: $role; status: $status")
    }

    override suspend fun updatePassword(userId: Long, passwordHash: String, passwordSalt: String) {
        check(userDao.updatePassword(userId, passwordHash, passwordSalt) == 1) {
            "Usuário não encontrado."
        }
        auditLogger.log("USERS", "PASSWORD_RESET", "USER", userId, null, "Senha de usuário redefinida", null)
    }
}
