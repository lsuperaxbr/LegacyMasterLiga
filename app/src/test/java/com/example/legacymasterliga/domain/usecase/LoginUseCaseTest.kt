package com.example.legacymasterliga.domain.usecase

import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {
    private val user = User(1, "admin", "Administrador", UserRole.ADMINISTRATOR, AccountStatus.ACTIVE)

    @Test fun blank_credentials_are_rejected_without_calling_repository() = runBlocking {
        val repository = FakeAuthRepository(Result.success(user))
        val result = LoginUseCase(repository)(" ", "")
        assertTrue(result.isFailure)
        assertEquals(0, repository.loginCalls)
    }

    @Test fun valid_credentials_are_forwarded_to_repository() = runBlocking {
        val repository = FakeAuthRepository(Result.success(user))
        val result = LoginUseCase(repository)("admin", "admin123")
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        assertEquals(1, repository.loginCalls)
    }

    private class FakeAuthRepository(private val result: Result<User>) : AuthRepository {
        var loginCalls = 0
        override val currentUser: Flow<User?> = flowOf(null)
        override suspend fun login(username: String, password: CharArray): Result<User> {
            loginCalls++
            password.fill('\u0000')
            return result
        }
        override suspend fun logout() = Unit
    }
}
