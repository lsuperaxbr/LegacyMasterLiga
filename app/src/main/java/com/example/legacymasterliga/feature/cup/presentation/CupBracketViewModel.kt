package com.example.legacymasterliga.feature.cup.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.feature.results.domain.ResultsRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRound
import com.example.legacymasterliga.feature.schedule.domain.ScheduleMatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CupBracketUiState(
    val rounds: List<ScheduleRound> = emptyList(),
    val competitionName: String = "",
    val isLoading: Boolean = true,
    val feedback: String? = null,
    val canManage: Boolean = false,
    val ownClubIds: List<Long> = emptyList()
)

@HiltViewModel
class CupBracketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scheduleRepository: ScheduleRepository,
    private val resultsRepository: ResultsRepository,
    private val authRepository: AuthRepository,
    private val clubDao: com.example.legacymasterliga.core.database.dao.ClubDao,
) : ViewModel() {

    private val seasonId: Long = checkNotNull(savedStateHandle["seasonId"])
    private val _feedback = MutableStateFlow<String?>(null)

    private val user = authRepository.currentUser
    private val presidentClubs = user.flatMapLatest { current -> 
        if (current?.role == UserRole.PRESIDENT) clubDao.observeAllByPresident(current.id) else flowOf(emptyList()) 
    }

    val uiState: StateFlow<CupBracketUiState> = combine(
        scheduleRepository.observeSchedule(seasonId),
        user,
        presidentClubs,
        _feedback
    ) { rounds, currentUser, ownClubs, message ->
        val knockoutRounds = rounds.filter { it.stage == "KNOCKOUT" }
        val ownIds = ownClubs.map { it.id }
        
        CupBracketUiState(
            rounds = knockoutRounds,
            competitionName = knockoutRounds.firstOrNull()?.name?.substringBefore(" -") ?: "Copa",
            isLoading = false,
            feedback = message,
            canManage = currentUser?.role == UserRole.ADMINISTRATOR || currentUser?.role == UserRole.PRESIDENT,
            ownClubIds = ownIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CupBracketUiState())

    fun saveResult(matchId: Long, homeScore: Int, awayScore: Int, winnerId: Long?) {
        viewModelScope.launch {
            runCatching {
                resultsRepository.saveResult(
                    matchId = matchId,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    penaltiesHome = null,
                    penaltiesAway = null,
                    winnerClubId = winnerId
                )
            }.onSuccess {
                _feedback.value = "Resultado salvo!"
            }.onFailure { e ->
                _feedback.value = e.message ?: "Erro ao salvar resultado."
            }
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }
}
