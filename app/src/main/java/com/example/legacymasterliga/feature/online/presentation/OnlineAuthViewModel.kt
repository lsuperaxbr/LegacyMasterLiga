package com.example.legacymasterliga.feature.online.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.network.FirebaseErrorMapper
import com.example.legacymasterliga.core.network.OnlineFailureKind
import com.example.legacymasterliga.core.network.OnlineProfileInactiveException
import com.example.legacymasterliga.core.session.SessionManager
import com.example.legacymasterliga.feature.online.domain.CloudAuthRepository
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OnlineAuthUiState(
    val email: String = "",
    val isConnecting: Boolean = false,
    val error: String? = null,
    val profile: CloudUserProfile? = null,
    val isOnline: Boolean = false,
)

@HiltViewModel
class OnlineAuthViewModel @Inject constructor(
    private val cloudAuthRepository: CloudAuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _email = MutableStateFlow("")
    private val _isConnecting = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OnlineAuthUiState> = combine(
        _email,
        _isConnecting,
        _error,
        cloudAuthRepository.observeOnlineProfile(),
    ) { email, connecting, error, profile ->
        OnlineAuthUiState(
            email = email,
            isConnecting = connecting,
            error = error,
            profile = profile,
            isOnline = profile != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnlineAuthUiState())

    fun updateEmail(value: String) {
        _email.value = value
        _error.value = null
    }

    fun connect(password: String) {
        viewModelScope.launch {
            val currentUser = sessionManager.currentUser.first()
            val localUserId = currentUser?.id ?: return@launch
            
            _isConnecting.value = true
            _error.value = null

            val loginResult = cloudAuthRepository.signInOnline(_email.value, password)
            if (loginResult.isSuccess) {
                completeConnection(loginResult.getOrThrow(), localUserId)
            } else {
                val loginError = loginResult.exceptionOrNull() ?: IllegalStateException("Falha desconhecida no Firebase Auth.")
                val classified = FirebaseErrorMapper.classify(loginError)
                if (classified.kind != OnlineFailureKind.INVALID_CREDENTIAL) {
                    showFailure(loginError)
                } else {
                    val registration = cloudAuthRepository.registerOnlineAccount(
                        email = _email.value, 
                        password = password, 
                        displayName = _email.value.substringBefore('@'),
                        username = _email.value.substringBefore('@'),
                        localUserId = localUserId
                    )
                    registration.onSuccess { uid ->
                        completeConnection(uid, localUserId)
                    }.onFailure { registrationError ->
                        val registrationFailure = FirebaseErrorMapper.classify(registrationError)
                        _error.value = if (registrationFailure.kind == OnlineFailureKind.INVALID_CREDENTIAL) {
                            classified.userMessage
                        } else {
                            registrationFailure.userMessage
                        }
                    }
                }
            }
            _isConnecting.value = false
        }
    }

    private suspend fun completeConnection(uid: String, localUserId: Long) {
        cloudAuthRepository.getOrCreateProfile(
            uid = uid, 
            preferredLocalUserId = localUserId
        )
            .onFailure { error ->
                cloudAuthRepository.signOutOnline()
                showFailure(error)
            }
            .onSuccess { profile ->
                if (profile.status != ACTIVE) {
                    cloudAuthRepository.signOutOnline()
                    showFailure(OnlineProfileInactiveException())
                    return@onSuccess
                }

                cloudAuthRepository.linkLocalUser(localUserId, uid)
                    .onFailure { error ->
                        cloudAuthRepository.signOutOnline()
                        showFailure(error)
                    }
            }
    }

    private fun showFailure(error: Throwable) {
        _error.value = FirebaseErrorMapper.classify(error).userMessage
    }

    fun disconnect() {
        viewModelScope.launch {
            cloudAuthRepository.signOutOnline()
        }
    }

    private companion object {
        const val ACTIVE = "ACTIVE"
    }
}
