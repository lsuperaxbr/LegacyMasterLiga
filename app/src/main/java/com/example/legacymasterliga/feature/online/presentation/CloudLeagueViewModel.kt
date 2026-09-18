package com.example.legacymasterliga.feature.online.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.network.FirebaseErrorMapper
import com.example.legacymasterliga.core.session.SessionManager
import com.example.legacymasterliga.feature.online.domain.CloudAuthRepository
import com.example.legacymasterliga.feature.online.domain.CloudInvite
import com.example.legacymasterliga.feature.online.domain.CloudLeague
import com.example.legacymasterliga.feature.online.domain.CloudLeagueRepository
import com.example.legacymasterliga.feature.online.domain.CloudMember
import com.example.legacymasterliga.feature.online.sync.OnlineSportsSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloudLeagueUiState(
    val cloudLeague: CloudLeague? = null,
    val members: List<CloudMember> = emptyList(),
    val activeInvite: CloudInvite? = null,
    val isOnline: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingSyncCount: Int = 0,
    val conflictCount: Int = 0,
)

@HiltViewModel
class CloudLeagueViewModel @Inject constructor(
    private val cloudLeagueRepository: CloudLeagueRepository,
    private val cloudAuthRepository: CloudAuthRepository,
    private val sessionManager: SessionManager,
    private val onlineSportsSyncManager: OnlineSportsSyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudLeagueUiState())
    val uiState: StateFlow<CloudLeagueUiState> = _uiState.asStateFlow()

    init {
        onlineSportsSyncManager.pendingCount
            .onEach { count -> _uiState.update { it.copy(pendingSyncCount = count) } }
            .launchIn(viewModelScope)
        onlineSportsSyncManager.conflictCount
            .onEach { count -> _uiState.update { it.copy(conflictCount = count) } }
            .launchIn(viewModelScope)
            
        cloudAuthRepository.observeOnlineProfile()
            .onEach { profile -> 
                val isOnline = profile != null
                _uiState.update { it.copy(isOnline = isOnline) }
                
                if (profile?.cloudLeagueId != null) {
                    // Carga automática se o perfil tem uma liga vinculada
                    startObserving(profile.cloudLeagueId)
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadExistingLeague(localId: Long) {
        viewModelScope.launch {
            // Aqui precisariamos de um repositório que busque a LeagueEntity
            // Para simplificar o hotfix e não mudar o domínio agora, vamos 
            // focar na barreira de conexão.
        }
    }

    fun acceptRemoteConflicts() = onlineSportsSyncManager.acceptRemoteConflicts()

    fun promote(localLeagueId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = cloudLeagueRepository.promoteToCloud(localLeagueId)
            result.onSuccess { cloudId ->
                startObserving(cloudId)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = FirebaseErrorMapper.map(e)) }
            }
        }
    }

    fun generateInvite(cloudId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = cloudLeagueRepository.generateInvite(cloudId)
            result.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = FirebaseErrorMapper.map(e)) }
            }
        }
    }

    fun join(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = cloudLeagueRepository.joinByInvite(code)
            result.onSuccess { cloudId ->
                startObserving(cloudId)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = FirebaseErrorMapper.map(e)) }
            }
        }
    }

    private fun startObserving(cloudId: String) {
        cloudLeagueRepository.observeLeague(cloudId)
            .onEach { league -> _uiState.update { it.copy(cloudLeague = league, isLoading = false) } }
            .launchIn(viewModelScope)

        cloudLeagueRepository.observeMembers(cloudId)
            .onEach { list -> _uiState.update { it.copy(members = list) } }
            .launchIn(viewModelScope)

        cloudLeagueRepository.observeActiveInvite(cloudId)
            .onEach { invite -> _uiState.update { it.copy(activeInvite = invite) } }
            .launchIn(viewModelScope)
    }
}
