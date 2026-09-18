package com.example.legacymasterliga.feature.online.domain

import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.network.OnlineProfileInactiveException
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OnlineLoginServiceTest {

    @Test
    fun `valid credentials load profile and open online session`() = runBlocking {
        val repository = FakeCloudAuthRepository(profileResult = Result.success(activeProfile()))
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.login("presidente@liga.com", "senha-correta")

        assertTrue(result.isSuccess)
        assertEquals("uid-1", repository.profileUidRequested)
        assertEquals("uid-1", sessions.openedProfile?.firebaseUid)
        assertFalse(repository.signedOut)
    }

    @Test
    fun `missing profile is provisioned through get or create before session`() = runBlocking {
        val createdProfile = activeProfile(username = "novo_presidente")
        val repository = FakeCloudAuthRepository(profileResult = Result.success(createdProfile))
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.login("novo@liga.com", "senha-correta")

        assertTrue(result.isSuccess)
        assertEquals(1, repository.getOrCreateCalls)
        assertSame(createdProfile, sessions.openedProfile)
    }

    @Test
    fun `wrong password never reads Firestore or creates session`() = runBlocking {
        val invalidCredential = IllegalArgumentException("E-mail ou senha inválidos.")
        val repository = FakeCloudAuthRepository(signInResult = Result.failure(invalidCredential))
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.login("presidente@liga.com", "senha-errada")

        assertSame(invalidCredential, result.exceptionOrNull())
        assertEquals(0, repository.getOrCreateCalls)
        assertNull(sessions.openedProfile)
    }

    @Test
    fun `Firestore failure is preserved and authenticated account remains available for retry`() = runBlocking {
        val firestoreFailure = IOException("PERMISSION_DENIED user_profiles/uid-1")
        val repository = FakeCloudAuthRepository(profileResult = Result.failure(firestoreFailure))
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.login("presidente@liga.com", "senha-correta")

        assertSame(firestoreFailure, result.exceptionOrNull())
        assertFalse(repository.signedOut)
        assertNull(sessions.openedProfile)
    }

    @Test
    fun `inactive profile is rejected before local session`() = runBlocking {
        val repository = FakeCloudAuthRepository(
            profileResult = Result.success(activeProfile().copy(status = "INACTIVE")),
        )
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.login("presidente@liga.com", "senha-correta")

        assertTrue(result.exceptionOrNull() is OnlineProfileInactiveException)
        assertFalse(repository.signedOut)
        assertNull(sessions.openedProfile)
    }

    @Test
    fun `new account completes the same idempotent profile bootstrap and opens session`() = runBlocking {
        val repository = FakeCloudAuthRepository(
            createAccountResult = Result.success("uid-created"),
            profileResult = Result.success(activeProfile().copy(firebaseUid = "uid-created")),
        )
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.register("novo@liga.com", "senha-segura", "Nome", "Apelido")

        assertTrue(result.isSuccess)
        assertEquals(1, repository.createAccountCalls)
        assertEquals("uid-created", repository.profileUidRequested)
        assertEquals("uid-created", sessions.openedProfile?.firebaseUid)
    }

    @Test
    fun `authenticated account without profile can retry without a second Auth login`() = runBlocking {
        val repository = FakeCloudAuthRepository(
            currentUid = "uid-1",
            profileResult = Result.success(activeProfile()),
        )
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.completeAuthenticatedProfile("Nome", "Apelido")

        assertTrue(result.isSuccess)
        assertEquals(0, repository.signInCalls)
        assertEquals(1, repository.getOrCreateCalls)
        assertEquals("uid-1", sessions.openedProfile?.firebaseUid)
    }

    @Test
    fun `second login reuses one profile identity without creating another Auth account`() = runBlocking {
        val repository = FakeCloudAuthRepository(profileResult = Result.success(activeProfile()))
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        assertTrue(service.login("presidente@liga.com", "senha-correta").isSuccess)
        assertTrue(service.login("presidente@liga.com", "senha-correta").isSuccess)

        assertEquals(2, repository.getOrCreateCalls)
        assertEquals(0, repository.createAccountCalls)
        assertEquals("uid-1", repository.profileUidRequested)
    }

    @Test
    fun `cached Firebase user restores profile and session without password`() = runBlocking {
        val repository = FakeCloudAuthRepository(
            currentUid = "uid-1",
            profileResult = Result.success(activeProfile()),
        )
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.restoreAuthenticatedSession()

        assertEquals("uid-1", result.getOrThrow()?.firebaseUid)
        assertEquals("uid-1", sessions.openedProfile?.firebaseUid)
        assertEquals(0, repository.signInCalls)
    }

    @Test
    fun `app without cached Firebase user remains logged out`() = runBlocking {
        val repository = FakeCloudAuthRepository(currentUid = null)
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        val result = service.restoreAuthenticatedSession()

        assertNull(result.getOrThrow())
        assertNull(sessions.openedProfile)
        assertEquals(0, repository.getOrCreateCalls)
    }

    @Test
    fun `logout clears Room session and Firebase authentication`() = runBlocking {
        val repository = FakeCloudAuthRepository()
        val sessions = FakeOnlineLocalSessionGateway()
        val service = OnlineLoginService(repository, sessions)

        service.logout()

        assertTrue(sessions.cleared)
        assertTrue(repository.signedOut)
    }

    private fun activeProfile(username: String = "presidente") = CloudUserProfile(
        firebaseUid = "uid-1",
        username = username,
        displayName = "Presidente",
        role = UserRole.PRESIDENT,
        status = "ACTIVE",
    )
}

private class FakeOnlineLocalSessionGateway : OnlineLocalSessionGateway {
    var openedProfile: CloudUserProfile? = null
    var cleared = false

    override suspend fun open(profile: CloudUserProfile): Long {
        openedProfile = profile
        return 1L
    }

    override suspend fun clear() {
        cleared = true
    }
}

private class FakeCloudAuthRepository(
    private val signInResult: Result<String> = Result.success("uid-1"),
    private val createAccountResult: Result<String> = Result.success("uid-1"),
    private val profileResult: Result<CloudUserProfile> = Result.success(
        CloudUserProfile(firebaseUid = "uid-1", username = "presidente"),
    ),
    private val currentUid: String? = null,
) : CloudAuthRepository {
    var signInCalls = 0
    var createAccountCalls = 0
    var getOrCreateCalls = 0
    var profileUidRequested: String? = null
    var signedOut = false

    override suspend fun createOnlineAccount(email: String, password: String): Result<String> {
        createAccountCalls++
        return createAccountResult
    }

    override suspend fun registerOnlineAccount(
        email: String,
        password: String,
        displayName: String,
        username: String,
        localUserId: Long
    ) = Result.success("uid-1")

    override suspend fun signInOnline(email: String, password: String): Result<String> {
        signInCalls++
        return signInResult
    }

    override suspend fun fetchProfile(uid: String): Result<CloudUserProfile?> =
        profileResult.map { it }

    override suspend fun getOrCreateProfile(
        uid: String,
        customDisplayName: String?,
        customUsername: String?,
        preferredLocalUserId: Long?,
    ): Result<CloudUserProfile> {
        getOrCreateCalls++
        profileUidRequested = uid
        return profileResult
    }

    override suspend fun signOutOnline() {
        signedOut = true
    }

    override fun currentFirebaseUid(): String? = currentUid

    override fun observeOnlineProfile(): Flow<CloudUserProfile?> = flowOf(profileResult.getOrNull())

    override suspend fun linkLocalUser(localUserId: Long, firebaseUid: String): Result<Unit> =
        Result.success(Unit)
}
