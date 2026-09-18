package com.example.legacymasterliga.domain.usecase

import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Preencha usuário e senha."))
        }
        return authRepository.login(username, password.toCharArray())
    }
}
