package com.example.legacymasterliga.feature.hubs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.feature.dashboard.domain.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

data class LeagueHubUiState(
    val leagueName: String = "Liga",
    val hasActiveSeason: Boolean = false,
    val context: com.example.legacymasterliga.core.navigation.LegacyNavigationContext = com.example.legacymasterliga.core.navigation.LegacyNavigationContext(),
    val role: com.example.legacymasterliga.core.model.UserRole? = null,
    val isLoading: Boolean = true
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class LeagueHubViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val dashboardRepository: DashboardRepository,
    private val onlineSportsSyncManager: com.example.legacymasterliga.feature.online.sync.OnlineSportsSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueHubUiState())
    val uiState: StateFlow<LeagueHubUiState> = _uiState.asStateFlow()

    init {
        authRepository.currentUser
            .flatMapLatest { user ->
                dashboardRepository.observeSummary(user?.id)
                    .map { summary ->
                        LeagueHubUiState(
                            leagueName = summary.leagueName,
                            hasActiveSeason = summary.seasonId != null,
                            context = com.example.legacymasterliga.core.navigation.LegacyNavigationContext(
                                leagueId = summary.leagueId ?: 0L,
                                competitionId = summary.competitionId ?: 0L,
                                seasonId = summary.seasonId ?: 0L
                            ),
                            role = user?.role,
                            isLoading = false
                        )
                    }
            }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    fun forceRefresh() {
        onlineSportsSyncManager.triggerFullSync()
    }
}
