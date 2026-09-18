package com.example.legacymasterliga.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.feature.online.domain.OnlineLoginService
import com.example.legacymasterliga.feature.online.sync.OnlineSportsSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository,
    onlineLoginService: OnlineLoginService,
    onlineSportsSyncManager: OnlineSportsSyncManager,
) : ViewModel() {
    val currentUser: StateFlow<User?> = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    init {
        viewModelScope.launch {
            onlineLoginService.restoreAuthenticatedSession()
                .onSuccess { profile ->
                    if (profile != null) {
                        onlineSportsSyncManager.startAuthenticatedSession(profile)
                    }
                }
        }
    }
}
