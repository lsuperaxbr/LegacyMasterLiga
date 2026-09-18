package com.example.legacymasterliga.feature.league.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.TieBreakCriterion
import com.example.legacymasterliga.feature.settings.domain.CompetitionOption
import com.example.legacymasterliga.feature.settings.domain.CompetitionRules
import com.example.legacymasterliga.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LeagueSettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedCompetitionId = MutableStateFlow<Long?>(null)
    private val message = MutableStateFlow<String?>(null)

    private val leagues = repository.observeLeagues()
    private val competitions = selectedLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeCompetitions(id)
    }
    private val rules = selectedCompetitionId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeCompetitionRules(id)
    }

    val uiState: StateFlow<LeagueSettingsUiState> = combine(
        leagues, selectedLeagueId, competitions, selectedCompetitionId, rules, message
    ) { params ->
        val leagueList = params[0] as List<com.example.legacymasterliga.domain.model.League>
        val selLeagueId = params[1] as Long?
        val compList = params[2] as List<CompetitionOption>
        val selCompId = params[3] as Long?
        val currentRules = params[4] as CompetitionRules?
        val currentMessage = params[5] as String?

        val effectiveLeagueId = selLeagueId ?: leagueList.firstOrNull()?.id
        val effectiveCompetitionId = selCompId 
            ?.takeIf { id -> compList.any { it.id == id } }
            ?: compList.firstOrNull()?.id
            
        if (selectedLeagueId.value != effectiveLeagueId) selectedLeagueId.value = effectiveLeagueId
        if (selectedCompetitionId.value != effectiveCompetitionId) selectedCompetitionId.value = effectiveCompetitionId

        LeagueSettingsUiState(
            leagues = leagueList,
            selectedLeagueId = effectiveLeagueId,
            competitions = compList,
            selectedCompetitionId = effectiveCompetitionId,
            rules = currentRules,
            message = currentMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeagueSettingsUiState())

    fun selectLeague(id: Long) { selectedLeagueId.value = id; selectedCompetitionId.value = null }
    fun selectCompetition(id: Long) { selectedCompetitionId.value = id }

    fun saveRules(win: Int, draw: Int, loss: Int, yellowFine: Long, redFine: Long, criteria: List<TieBreakCriterion>, highlightLeader: Boolean) = 
        launchAction("Regras da competição atualizadas.") {
            repository.updateCompetitionRules(
                CompetitionRules(
                    competitionId = requireNotNull(selectedCompetitionId.value),
                    pointsForWin = win,
                    pointsForDraw = draw,
                    pointsForLoss = loss,
                    yellowCardFineCr = yellowFine,
                    redCardFineCr = redFine,
                    tieBreakCriteria = criteria,
                    highlightLeader = highlightLeader
                ),
            )
        }

    fun clearMessage() { message.value = null }

    private fun launchAction(success: String, block: suspend () -> Unit) = viewModelScope.launch {
        runCatching { block() }
            .onSuccess { message.value = success }
            .onFailure { message.value = it.message ?: "Não foi possível salvar as configurações." }
    }
}

data class LeagueSettingsUiState(
    val leagues: List<com.example.legacymasterliga.domain.model.League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val competitions: List<CompetitionOption> = emptyList(),
    val selectedCompetitionId: Long? = null,
    val rules: CompetitionRules? = null,
    val message: String? = null,
)
