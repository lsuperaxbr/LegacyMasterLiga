package com.example.legacymasterliga.feature.login.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.usecase.LoginUseCase
import com.example.legacymasterliga.feature.online.domain.OnlineLoginService
import com.example.legacymasterliga.feature.online.sync.OnlineSportsSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val onlineLoginService: OnlineLoginService,
    private val onlineSportsSyncManager: OnlineSportsSyncManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LoginUiState().withFirebaseAccount(onlineLoginService.hasAuthenticatedFirebaseAccount()),
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginModeChanged(mode: LoginMode) = _uiState.update { it.copy(loginMode = mode, errorMessage = null) }
    fun onOnlineSubModeChanged(mode: OnlineSubMode) = _uiState.update { it.copy(onlineSubMode = mode, errorMessage = null) }
    fun onUsernameChanged(value: String) = _uiState.update { it.copy(username = value, errorMessage = null) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onSignUpNameChanged(value: String) = _uiState.update { it.copy(signUpName = value, errorMessage = null) }
    fun onSignUpNicknameChanged(value: String) = _uiState.update { it.copy(signUpNickname = value, errorMessage = null) }
    fun onSignUpConfirmPasswordChanged(value: String) = _uiState.update { it.copy(signUpConfirmPassword = value, errorMessage = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        
        if (state.loginMode == LoginMode.LOCAL) {
            loginLocal(state)
        } else {
            when (state.onlineSubMode) {
                OnlineSubMode.SIGN_IN -> loginOnline(state)
                OnlineSubMode.SIGN_UP -> registerOnline(state)
                OnlineSubMode.COMPLETE_PROFILE -> retryOnlineProfile()
            }
        }
    }

    private fun loginLocal(state: LoginUiState) {
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha o usuário e a senha.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(state.username, state.password)
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = failure.message ?: "Não foi possível entrar.",
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { current -> current.copy(isLoading = false, password = "") }
                }
        }
    }

    private fun loginOnline(state: LoginUiState) {
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Informe um e-mail válido.") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe sua senha online.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            handleOnlineResult(onlineLoginService.login(state.email, state.password))
        }
    }

    private fun registerOnline(state: LoginUiState) {
        if (state.signUpName.trim().length < 3) {
            _uiState.update { it.copy(errorMessage = "O nome deve ter no mínimo 3 caracteres.") }
            return
        }
        if (state.signUpNickname.trim().length < 2) {
            _uiState.update { it.copy(errorMessage = "O apelido deve ter no mínimo 2 caracteres.") }
            return
        }
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Informe um e-mail válido.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "A senha deve ter no mínimo 6 caracteres.") }
            return
        }
        if (state.password != state.signUpConfirmPassword) {
            _uiState.update { it.copy(errorMessage = "A confirmação de senha é diferente da senha.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            handleOnlineResult(
                onlineLoginService.register(
                    email = state.email.trim(),
                    password = state.password,
                    displayName = state.signUpName.trim(),
                    username = state.signUpNickname.trim()
                )
            )
        }
    }

    fun retryOnlineProfile() {
        val state = _uiState.value
        if (state.isLoading) return
        
        // Se estiver em COMPLETE_PROFILE mas campos vazios, forçar preenchimento
        if (state.onlineSubMode == OnlineSubMode.COMPLETE_PROFILE && 
            (state.signUpName.isBlank() || state.signUpNickname.isBlank())) {
            _uiState.update { it.copy(errorMessage = "Informe seu nome e apelido para concluir seu perfil.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, showLocalFallback = false) }
            handleOnlineResult(
                onlineLoginService.completeAuthenticatedProfile(
                    displayName = state.signUpName.trim().takeIf { it.isNotBlank() },
                    username = state.signUpNickname.trim().takeIf { it.isNotBlank() }
                )
            )
        }
    }

    fun enterInLocalMode() {
        val state = _uiState.value
        _uiState.update { it.copy(showLocalFallback = false, errorMessage = null) }
        loginLocal(state.copy(username = state.username.ifBlank { "admin" }, password = "password"))
    }

    fun logoutOnlineAccount() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, showLocalFallback = false) }
            onlineLoginService.logout()
            _uiState.update { it.afterOnlineLogout() }
        }
    }

    private fun handleOnlineResult(result: Result<com.example.legacymasterliga.feature.online.domain.CloudUserProfile>) {
        result
            .onFailure(::showOnlineFailure)
            .onSuccess { profile ->
                onlineSportsSyncManager.startAuthenticatedSession(profile)
                _uiState.update { it.copy(isLoading = false, password = "", errorMessage = null, showLocalFallback = false) }
            }
    }

    private fun showOnlineFailure(error: Throwable) {
        val failure = com.example.legacymasterliga.core.network.FirebaseErrorMapper.classify(error)
        Log.e("OnlineLogin", "category=${failure.kind}; detail=${failure.technicalMessage}", error)
        
        val isFirestoreError = failure.kind in listOf(
            com.example.legacymasterliga.core.network.OnlineFailureKind.PERMISSION_DENIED,
            com.example.legacymasterliga.core.network.OnlineFailureKind.FIRESTORE_UNAVAILABLE,
            com.example.legacymasterliga.core.network.OnlineFailureKind.PROFILE_NOT_FOUND,
            com.example.legacymasterliga.core.network.OnlineFailureKind.UNEXPECTED
        )

        _uiState.update {
            it.copy(
                isLoading = false, 
                errorMessage = failure.userMessage,
                showLocalFallback = isFirestoreError && onlineLoginService.hasAuthenticatedFirebaseAccount()
            ).withFirebaseAccount(onlineLoginService.hasAuthenticatedFirebaseAccount())
        }
    }
}
