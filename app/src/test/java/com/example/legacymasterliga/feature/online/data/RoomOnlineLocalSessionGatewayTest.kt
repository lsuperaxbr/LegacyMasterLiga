package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.session.SessionManager
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomOnlineLocalSessionGatewayTest {

    @Test
    fun `first online login creates local mirror and persistent online session`() = runBlocking {
        val users = FakeUserDao()
        val sessions = FakeSessionManager()
        val gateway = RoomOnlineLocalSessionGateway(users, sessions)

        val userId = gateway.open(profile())

        assertEquals(userId, sessions.createdUserId)
        assertEquals("ONLINE", sessions.createdSource)
        assertEquals("uid-1", users.findById(userId)?.firebaseUid)
        assertEquals(UserRole.PRESIDENT, users.findById(userId)?.role)
    }

    @Test
    fun `second login reuses same local user without duplication`() = runBlocking {
        val users = FakeUserDao()
        val sessions = FakeSessionManager()
        val gateway = RoomOnlineLocalSessionGateway(users, sessions)

        val firstId = gateway.open(profile())
        val secondId = gateway.open(profile())

        assertEquals(firstId, secondId)
        assertEquals(1, users.count())
        assertEquals(2, sessions.createCalls)
    }

    @Test
    fun `profile created from this installation links original President`() = runBlocking {
        val users = FakeUserDao()
        val originalId = users.insert(user(username = "luiz"))
        val sessions = FakeSessionManager()
        val gateway = RoomOnlineLocalSessionGateway(users, sessions)

        val openedId = gateway.open(profile(username = "luiz", localUserId = originalId))

        assertEquals(originalId, openedId)
        assertEquals("uid-1", users.findById(originalId)?.firebaseUid)
        assertEquals(1, users.count())
    }

    @Test
    fun `unrelated username collision never hijacks existing local user`() = runBlocking {
        val users = FakeUserDao()
        val unrelatedId = users.insert(user(username = "presidente"))
        val sessions = FakeSessionManager()
        val gateway = RoomOnlineLocalSessionGateway(users, sessions)

        val onlineId = gateway.open(profile())

        assertNotEquals(unrelatedId, onlineId)
        assertEquals(null, users.findById(unrelatedId)?.firebaseUid)
        assertTrue(users.findById(onlineId)?.username?.startsWith("presidente_uid-1") == true)
    }

    @Test
    fun `online profile with President role never hijacks local Administrator account`() = runBlocking {
        val users = FakeUserDao()
        val adminId = users.insert(
            UserEntity(
                username = "admin",
                displayName = "Administrador",
                passwordHash = "hash",
                passwordSalt = "salt",
                role = UserRole.ADMINISTRATOR,
            )
        )
        val sessions = FakeSessionManager()
        val gateway = RoomOnlineLocalSessionGateway(users, sessions)

        // Perfil online diz ser 'admin' (ID 1), mas o cargo na nuvem é PRESIDENT.
        val openedId = gateway.open(profile(username = "admin", localUserId = adminId))

        assertNotEquals(adminId, openedId)
        assertEquals(null, users.findById(adminId)?.firebaseUid)
        assertEquals(UserRole.PRESIDENT, users.findById(openedId)?.role)
        assertEquals(2, users.count())
    }

    @Test
    fun `logout clears persistent session`() = runBlocking {
        val sessions = FakeSessionManager()
        val gateway = RoomOnlineLocalSessionGateway(FakeUserDao(), sessions)

        gateway.clear()

        assertTrue(sessions.cleared)
    }

    private fun profile(username: String = "presidente", localUserId: Long = 0L) = CloudUserProfile(
        firebaseUid = "uid-1",
        localUserId = localUserId,
        username = username,
        displayName = "Presidente",
        role = UserRole.PRESIDENT,
        status = "ACTIVE",
    )

    private fun user(username: String) = UserEntity(
        username = username,
        displayName = username,
        passwordHash = "hash",
        passwordSalt = "salt",
        role = UserRole.PRESIDENT,
    )
}

private class FakeSessionManager : SessionManager {
    override val currentUser: Flow<User?> = MutableStateFlow(null)
    var createdUserId: Long? = null
    var createdSource: String? = null
    var createCalls = 0
    var cleared = false

    override suspend fun createSession(userId: Long, source: String) {
        createdUserId = userId
        createdSource = source
        createCalls++
    }

    override suspend fun clearSession() {
        cleared = true
    }
}

private class FakeUserDao : UserDao {
    private val users = linkedMapOf<Long, UserEntity>()
    private var nextId = 1L

    override suspend fun insert(user: UserEntity): Long {
        check(users.values.none { it.username.equals(user.username, ignoreCase = true) })
        check(user.firebaseUid == null || users.values.none { it.firebaseUid == user.firebaseUid })
        val id = if (user.id == 0L) nextId++ else user.id
        users[id] = user.copy(id = id)
        return id
    }

    override suspend fun update(user: UserEntity) {
        users[user.id] = user
    }

    override suspend fun findById(id: Long): UserEntity? = users[id]

    override suspend fun findByUsername(username: String): UserEntity? =
        users.values.firstOrNull { it.username.equals(username, ignoreCase = true) }

    override fun observeAll(): Flow<List<UserEntity>> = flowOf(users.values.toList())

    override suspend fun count(): Int = users.size

    override suspend fun updateProfile(
        userId: Long,
        displayName: String,
        role: UserRole,
        status: AccountStatus,
        updatedAt: Long,
    ): Int {
        val user = users[userId] ?: return 0
        users[userId] = user.copy(displayName = displayName, role = role, status = status, updatedAt = updatedAt)
        return 1
    }

    override suspend fun updatePassword(
        userId: Long,
        passwordHash: String,
        passwordSalt: String,
        updatedAt: Long,
    ): Int {
        val user = users[userId] ?: return 0
        users[userId] = user.copy(passwordHash = passwordHash, passwordSalt = passwordSalt, updatedAt = updatedAt)
        return 1
    }

    override suspend fun linkFirebase(userId: Long, uid: String, now: Long): Int {
        val user = users[userId] ?: return 0
        users[userId] = user.copy(firebaseUid = uid, updatedAt = now)
        return 1
    }

    override suspend fun findByFirebaseUid(uid: String): UserEntity? =
        users.values.firstOrNull { it.firebaseUid == uid }
}
