package com.example.legacymasterliga.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.DensityPreference
import com.example.legacymasterliga.core.model.ThemePreference
import com.example.legacymasterliga.core.model.TieBreakCriterion
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.feature.settings.domain.AppPreferences
import com.example.legacymasterliga.feature.settings.domain.CompetitionOption
import com.example.legacymasterliga.feature.settings.domain.CompetitionRules
import com.example.legacymasterliga.feature.settings.domain.SettingsRepository
import com.example.legacymasterliga.feature.finance.domain.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class SettingsBase(
    val preferences: AppPreferences,
    val leagues: List<League>,
    val competitions: List<CompetitionOption>,
    val rules: CompetitionRules?,
    val selectedLeagueId: Long?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val financeRepository: FinanceRepository,
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

    private val base = combine(
        repository.observeAppPreferences(), leagues, competitions, rules, selectedLeagueId,
    ) { preferences, leagueList, competitionList, currentRules, selectedLeague ->
        SettingsBase(preferences, leagueList, competitionList, currentRules, selectedLeague)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        base, authRepository.currentUser, selectedCompetitionId, message,
    ) { current, user, selectedCompetition, currentMessage ->
        val effectiveLeagueId = current.selectedLeagueId ?: current.leagues.firstOrNull()?.id
        val effectiveCompetitionId = selectedCompetition
            ?.takeIf { id -> current.competitions.any { it.id == id } }
            ?: current.competitions.firstOrNull()?.id
        if (selectedLeagueId.value != effectiveLeagueId) selectedLeagueId.value = effectiveLeagueId
        if (selectedCompetitionId.value != effectiveCompetitionId) selectedCompetitionId.value = effectiveCompetitionId
        SettingsUiState(
            user = user,
            preferences = current.preferences,
            leagues = current.leagues,
            selectedLeagueId = effectiveLeagueId,
            competitions = current.competitions,
            selectedCompetitionId = effectiveCompetitionId,
            rules = current.rules,
            message = currentMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun selectLeague(id: Long) { selectedLeagueId.value = id; selectedCompetitionId.value = null }
    fun selectCompetition(id: Long) { selectedCompetitionId.value = id }

    fun saveLeagueName(name: String) = launchAction("Nome da liga atualizado.") {
        repository.renameLeague(requireNotNull(selectedLeagueId.value), name)
    }

    fun savePreferences(theme: ThemePreference, density: DensityPreference, animations: Boolean) =
        launchAction("Preferências visuais atualizadas.") {
            repository.updateAppPreferences(AppPreferences(theme, density, animations))
        }

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

    fun injectBankBalance(amount: Long) = launchAction("Saldo do Banco da Liga injetado com sucesso.") {
        val leagueId = selectedLeagueId.value ?: uiState.value.leagues.firstOrNull()?.id ?: error("Nenhuma liga selecionada.")
        financeRepository.injectInitialBalance(leagueId, amount)
    }

    fun clearMessage() { message.value = null }

    private fun launchAction(success: String, block: suspend () -> Unit) = viewModelScope.launch {
        runCatching { block() }
            .onSuccess { message.value = success }
            .onFailure { message.value = it.message ?: "Não foi possível salvar as configurações." }
    }
}

data class SettingsUiState(
    val user: User? = null,
    val preferences: AppPreferences = AppPreferences(),
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val competitions: List<CompetitionOption> = emptyList(),
    val selectedCompetitionId: Long? = null,
    val rules: CompetitionRules? = null,
    val message: String? = null,
)
