package com.example.legacymasterliga.feature.cup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.competitions.domain.CreateCompetitionRequest
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CupSetupUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val availableClubs: List<Club> = emptyList(),
    val selectedClubIds: Set<Long> = emptySet(),
    val orderedSelectedClubs: List<Long> = emptyList(),
    val cupName: String = "Copa Legacy",
    val legs: Int = 2,
    val manualOrderMode: Boolean = false,
    val isCreating: Boolean = false,
    val feedback: String? = null,
    val createdSeasonId: Long? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CupSetupViewModel @Inject constructor(
    private val leagueRepository: LeagueRepository,
    private val clubRepository: ClubRepository,
    private val competitionRepository: CompetitionRepository,
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {

    private val _selectedLeagueId = MutableStateFlow<Long?>(null)
    private val _selectedClubIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _orderedClubs = MutableStateFlow<List<Long>>(emptyList())
    private val _cupName = MutableStateFlow("Copa Legacy")
    private val _legs = MutableStateFlow(2)
    private val _manualOrderMode = MutableStateFlow(false)
    private val _isCreating = MutableStateFlow(false)
    private val _feedback = MutableStateFlow<String?>(null)
    private val _createdSeasonId = MutableStateFlow<Long?>(null)

    private val leagues = leagueRepository.observeAll()
    private val clubs = _selectedLeagueId.flatMapLatest { leagueId ->
        if (leagueId == null) flowOf(emptyList())
        else clubRepository.observeManagedByLeague(leagueId).map { list -> list.filter { !it.isBank } }
    }

    val uiState: StateFlow<CupSetupUiState> = combine(
        leagues, _selectedLeagueId, clubs, _selectedClubIds, _orderedClubs, _cupName, _legs, _manualOrderMode, _isCreating, _feedback, _createdSeasonId
    ) { params ->
        val leagueList = params[0] as List<League>
        val leagueId = params[1] as Long?
        val clubList = params[2] as List<Club>
        val selectedIds = params[3] as Set<Long>
        val ordered = params[4] as List<Long>
        val name = params[5] as String
        val legsCount = params[6] as Int
        val manualMode = params[7] as Boolean
        val creating = params[8] as Boolean
        val message = params[9] as String?
        val seasonId = params[10] as Long?

        val effectiveLeagueId = leagueId ?: leagueList.firstOrNull { it.isOnline && it.cloudLeagueId != null }?.id
            ?: leagueList.firstOrNull()?.id
        if (leagueId == null && effectiveLeagueId != null) _selectedLeagueId.value = effectiveLeagueId

        CupSetupUiState(
            leagues = leagueList,
            selectedLeagueId = effectiveLeagueId,
            availableClubs = clubList,
            selectedClubIds = selectedIds,
            orderedSelectedClubs = ordered,
            cupName = name,
            legs = legsCount,
            manualOrderMode = manualMode,
            isCreating = creating,
            feedback = message,
            createdSeasonId = seasonId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CupSetupUiState())

    fun selectLeague(id: Long) {
        _selectedLeagueId.value = id
        _selectedClubIds.value = emptySet()
        _orderedClubs.value = emptyList()
    }

    fun setCupName(name: String) {
        _cupName.value = name
    }

    fun setLegs(count: Int) {
        _legs.value = count
    }

    fun toggleClub(id: Long) {
        _selectedClubIds.update { current ->
            if (current.contains(id)) {
                _orderedClubs.update { list -> list.filter { it != id } }
                current - id
            } else {
                _orderedClubs.update { list -> list + id }
                current + id
            }
        }
    }

    fun toggleManualOrder() { _manualOrderMode.update { !it } }

    fun moveClubUp(clubId: Long) {
        _orderedClubs.update { list ->
            val mutable = list.toMutableList()
            val idx = mutable.indexOf(clubId)
            if (idx > 0) {
                val tmp = mutable[idx]
                mutable[idx] = mutable[idx - 1]
                mutable[idx - 1] = tmp
            }
            mutable
        }
    }

    fun moveClubDown(clubId: Long) {
        _orderedClubs.update { list ->
            val mutable = list.toMutableList()
            val idx = mutable.indexOf(clubId)
            if (idx != -1 && idx < mutable.size - 1) {
                val tmp = mutable[idx]
                mutable[idx] = mutable[idx + 1]
                mutable[idx + 1] = tmp
            }
            mutable
        }
    }

    fun selectAll() {
        val allIds = uiState.value.availableClubs.map { it.id }
        _selectedClubIds.value = allIds.toSet()
        _orderedClubs.value = allIds
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    fun createCup() {
        val state = uiState.value
        val leagueId = state.selectedLeagueId ?: return
        if (state.selectedClubIds.size < 2) {
            _feedback.value = "Selecione pelo menos 2 clubes."
            return
        }

        viewModelScope.launch {
            _isCreating.value = true
            runCatching {
                val finalOrder = if (state.manualOrderMode) {
                    state.orderedSelectedClubs
                } else {
                    state.selectedClubIds.toList().shuffled()
                }
                val request = CreateCompetitionRequest(
                    leagueId = leagueId,
                    name = state.cupName,
                    type = CompetitionType.CUP,
                    format = CompetitionFormat.KNOCKOUT,
                    firstSeasonName = "Temporada 1",
                    initialParticipantIds = finalOrder,
                    knockoutLegs = state.legs
                )
                val competitionId = competitionRepository.createCompetition(request)
                val competitionSummary = competitionRepository.observeByLeague(leagueId).first()
                    .find { it.id == competitionId }
                val seasonId = competitionSummary?.seasons?.firstOrNull()?.id 
                    ?: throw IllegalStateException("Falha ao localizar a temporada da Copa.")

                scheduleRepository.generateSchedule(seasonId)
                _createdSeasonId.value = seasonId
            }.onFailure { e ->
                _feedback.value = e.message ?: "Erro ao criar Copa."
            }
            _isCreating.value = false
        }
    }
}
