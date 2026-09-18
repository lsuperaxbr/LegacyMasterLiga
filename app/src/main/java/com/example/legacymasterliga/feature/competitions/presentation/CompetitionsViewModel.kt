package com.example.legacymasterliga.feature.competitions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionSummary
import com.example.legacymasterliga.feature.competitions.domain.CreateCompetitionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CompetitionsViewModel @Inject constructor(
    private val leagueRepository: LeagueRepository,
    private val clubRepository: ClubRepository,
    private val competitionRepository: CompetitionRepository,
    authRepository: AuthRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val feedback = MutableStateFlow<CompetitionFeedback?>(null)

    private val leagues = leagueRepository.observeAll()
    private val currentUser = authRepository.currentUser
    private val clubs = selectedLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else clubRepository.observeManagedByLeague(id)
    }
    private val competitions = selectedLeagueId.flatMapLatest { leagueId ->
        if (leagueId == null) flowOf(emptyList()) else competitionRepository.observeByLeague(leagueId)
    }.map { list -> list.filter { it.type == CompetitionType.LEAGUE } }

    val uiState: StateFlow<CompetitionsUiState> = combine(
        leagues,
        selectedLeagueId,
        competitions,
        clubs,
        feedback,
        currentUser
    ) { params ->
        val leagueList = params[0] as List<League>
        val selectedId = params[1] as Long?
        val competitionList = params[2] as List<CompetitionSummary>
        val clubList = params[3] as List<Club>
        val currentFeedback = params[4] as CompetitionFeedback?
        val user = params[5] as com.example.legacymasterliga.domain.model.User?

        val effectiveId = selectedId ?: leagueList.firstOrNull()?.id
        if (selectedId == null && effectiveId != null) selectedLeagueId.value = effectiveId
        CompetitionsUiState(
            leagues = leagueList,
            selectedLeagueId = effectiveId,
            competitions = competitionList,
            availableClubs = clubList.filter { it.isActive },
            feedback = currentFeedback,
            role = user?.role
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompetitionsUiState())

    fun selectLeague(leagueId: Long) {
        selectedLeagueId.value = leagueId
        feedback.value = null
    }

    fun createLeague(name: String) {
        viewModelScope.launch {
            runCatching { leagueRepository.create(name, "CR") }
                .onSuccess { id ->
                    selectedLeagueId.value = id
                    feedback.value = CompetitionFeedback.Success("Liga criada com sucesso.")
                }
                .onFailure { error -> feedback.value = CompetitionFeedback.Error(error.message ?: "Não foi possível criar a liga.") }
        }
    }

    fun createCompetition(
        name: String,
        type: CompetitionType,
        format: CompetitionFormat,
        firstSeasonName: String,
        participantIds: List<Long>,
        groupCount: Int? = null,
        qualifiedPerGroup: Int? = null,
        knockoutLegs: Int = 1,
    ) {
        if (type != CompetitionType.LEAGUE) {
            feedback.value = CompetitionFeedback.Error("O modo Copa está temporariamente indisponível.")
            return
        }
        val leagueId = selectedLeagueId.value
        if (leagueId == null) {
            feedback.value = CompetitionFeedback.Error("Crie ou selecione uma liga primeiro.")
            return
        }
        val minimumParticipants = 3
        if (participantIds.size < minimumParticipants) {
            feedback.value = CompetitionFeedback.Error("A competição deve possuir pelo menos $minimumParticipants clubes.")
            return
        }
        
        viewModelScope.launch {
            runCatching {
                competitionRepository.createCompetition(
                    CreateCompetitionRequest(
                        leagueId = leagueId, 
                        name = name, 
                        type = type, 
                        format = format, 
                        firstSeasonName = firstSeasonName, 
                        initialParticipantIds = participantIds,
                        groupCount = groupCount,
                        qualifiedPerGroup = qualifiedPerGroup,
                        knockoutLegs = knockoutLegs
                    ),
                )
            }.onSuccess {
                feedback.value = CompetitionFeedback.Success("Competição e primeira temporada criadas com ${participantIds.size} participantes.")
            }.onFailure { error ->
                feedback.value = CompetitionFeedback.Error(error.message ?: "Não foi possível criar a competição.")
            }
        }
    }

    fun createNextSeason(competitionId: Long, name: String, participantIds: List<Long>) {
        viewModelScope.launch {
            runCatching { competitionRepository.createNextSeason(competitionId, name, participantIds) }
                .onSuccess { feedback.value = CompetitionFeedback.Success("Nova temporada criada com sucesso.") }
                .onFailure { error -> feedback.value = CompetitionFeedback.Error(error.message ?: "Não foi possível criar a temporada.") }
        }
    }

    fun clearFeedback() {
        feedback.value = null
    }
}

data class CompetitionsUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val competitions: List<CompetitionSummary> = emptyList(),
    val availableClubs: List<Club> = emptyList(),
    val feedback: CompetitionFeedback? = null,
    val role: com.example.legacymasterliga.core.model.UserRole? = null,
)

sealed interface CompetitionFeedback {
    data class Success(val message: String) : CompetitionFeedback
    data class Error(val message: String) : CompetitionFeedback
}
