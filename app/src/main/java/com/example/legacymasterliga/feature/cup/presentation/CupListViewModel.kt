package com.example.legacymasterliga.feature.cup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CupListUiState(
    val cups: List<CompetitionSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
)

@HiltViewModel
class CupListViewModel @Inject constructor(
    private val competitionRepository: CompetitionRepository,
    private val leagueRepository: LeagueRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val leagueId = leagueRepository.observeAll().map { leagues ->
        leagues.firstOrNull { it.isOnline && it.cloudLeagueId != null }?.id
            ?: leagues.firstOrNull()?.id
    }

    val uiState: StateFlow<CupListUiState> = combine(
        leagueId.flatMapLatest { id -> 
            if (id == null) flowOf(emptyList()) 
            else competitionRepository.observeByLeague(id) 
        },
        authRepository.currentUser
    ) { competitions, user ->
        CupListUiState(
            cups = competitions.filter { it.type == CompetitionType.CUP },
            isLoading = false,
            isAdmin = user?.role == UserRole.ADMINISTRATOR
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CupListUiState())

    val feedback = MutableStateFlow<String?>(null)

    fun deleteCup(competitionId: Long) {
        viewModelScope.launch {
            runCatching { competitionRepository.delete(competitionId) }
                .onSuccess { feedback.value = "Copa excluída." }
                .onFailure { feedback.value = "Erro ao excluir Copa." }
        }
    }

    fun clearFeedback() {
        feedback.value = null
    }
}
