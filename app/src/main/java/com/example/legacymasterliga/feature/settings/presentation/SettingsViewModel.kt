package com.example.legacymasterliga.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.model.DensityPreference
import com.example.legacymasterliga.core.model.ThemePreference
import com.example.legacymasterliga.core.model.TieBreakCriterion
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.CsvImportProgress
import com.example.legacymasterliga.domain.model.CsvImportSummary
import com.example.legacymasterliga.domain.model.CsvParseResult
import com.example.legacymasterliga.domain.model.CsvTeamMapping
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.parser.CsvRosterParser
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.usecase.DeleteLeagueResult
import com.example.legacymasterliga.domain.usecase.DeleteLeagueUseCase
import com.example.legacymasterliga.domain.usecase.ImportCsvRostersUseCase
import com.example.legacymasterliga.domain.usecase.PurgeGhostResult
import com.example.legacymasterliga.domain.usecase.ResetLeagueResult
import com.example.legacymasterliga.domain.usecase.ResetLeagueUseCase
import com.example.legacymasterliga.feature.finance.domain.FinanceRepository
import com.example.legacymasterliga.feature.settings.domain.AppPreferences
import com.example.legacymasterliga.feature.settings.domain.CompetitionOption
import com.example.legacymasterliga.feature.settings.domain.CompetitionRules
import com.example.legacymasterliga.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val database: AppDatabase,
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val clubRepository: ClubRepository,
    private val financeRepository: FinanceRepository,
    private val resetLeagueUseCase: ResetLeagueUseCase,
    private val deleteLeagueUseCase: DeleteLeagueUseCase,
    private val importCsvRostersUseCase: ImportCsvRostersUseCase,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedCompetitionId = MutableStateFlow<Long?>(null)
    private val message = MutableStateFlow<String?>(null)

    val csvParseResult = MutableStateFlow<CsvParseResult?>(null)
    val csvExistingPlayerCount = MutableStateFlow(0)
    val csvImportProgress = MutableStateFlow<CsvImportProgress?>(null)
    val csvImportSummary = MutableStateFlow<CsvImportSummary?>(null)
    val purgeGhostResult = MutableStateFlow<PurgeGhostResult?>(null)

    private val leagues = repository.observeLeagues()
    private val competitions = selectedLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeCompetitions(id)
    }
    private val rules = selectedCompetitionId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeCompetitionRules(id)
    }
    private val clubs = selectedLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else clubRepository.observeManagedByLeague(id)
    }

    private val base = combine(
        repository.observeAppPreferences(), leagues, competitions, rules, selectedLeagueId,
    ) { preferences, leagueList, competitionList, currentRules, selectedLeague ->
        SettingsBase(preferences, leagueList, competitionList, currentRules, selectedLeague)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        base, clubs, authRepository.currentUser, selectedCompetitionId, message,
    ) { current, clubList, user, selectedCompetition, currentMessage ->
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
            clubs = clubList,
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
            val compId = selectedCompetitionId.value
            if (compId == null) {
                message.value = "Crie ou selecione uma competição primeiro."
                return@launchAction
            }
            repository.updateCompetitionRules(
                CompetitionRules(
                    competitionId = compId,
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

    fun resetLeague() = viewModelScope.launch {
        val leagueId = selectedLeagueId.value ?: uiState.value.leagues.firstOrNull()?.id
        if (leagueId == null) {
            message.value = "Nenhuma liga selecionada."
            return@launch
        }
        when (val result = resetLeagueUseCase(leagueId)) {
            is ResetLeagueResult.Success -> {
                message.value = "Reset da Liga executado com sucesso! Jogadores e histórico zerados e saldos restaurados."
            }
            is ResetLeagueResult.Error -> {
                message.value = result.message
            }
        }
    }

    fun deleteLeague(targetLeagueId: Long) = viewModelScope.launch {
        val activeId = selectedLeagueId.value ?: uiState.value.leagues.firstOrNull()?.id
        when (val result = deleteLeagueUseCase(targetLeagueId, activeId)) {
            is DeleteLeagueResult.Success -> {
                message.value = "Liga excluída com sucesso! Todos os dados associados foram removidos."
            }
            is DeleteLeagueResult.Error -> {
                message.value = result.message
            }
        }
    }

    fun purgeGhostLeague() = viewModelScope.launch {
        runCatching { deleteLeagueUseCase.purgeGhostLeague("Liga M L Amigos") }
            .onSuccess { result ->
                purgeGhostResult.value = result
                message.value = "Limpeza de liga fantasma concluída. Perfis desvinculados: ${result.profilesUpdatedCount}."
            }
            .onFailure { error ->
                message.value = "Falha na purga da liga fantasma: ${error.message}"
            }
    }

    fun parseCsvUri(contentResolver: android.content.ContentResolver, uri: android.net.Uri) = viewModelScope.launch(Dispatchers.IO) {
        val leagueId = selectedLeagueId.value ?: uiState.value.leagues.firstOrNull()?.id
        if (leagueId == null) {
            withContext(Dispatchers.Main) {
                message.value = "Selecione uma liga antes de importar."
            }
            return@launch
        }

        try {
            val result = contentResolver.openInputStream(uri)?.use { stream ->
                CsvRosterParser.parse(stream)
            } ?: CsvParseResult(emptyList(), listOf("Não foi possível abrir o arquivo selecionado."))

            var count = 0
            database.query("SELECT COUNT(*) FROM players WHERE leagueId = ?", arrayOf(leagueId.toString())).use { cursor ->
                if (cursor.moveToFirst()) count = cursor.getInt(0)
            }

            val teams = result.players.map { it.csvTeam }.distinct()
            android.util.Log.d("CsvRosterParser", "Atletas extraídos: ${result.players.size}, Times identificados (${teams.size}): $teams")

            withContext(Dispatchers.Main) {
                csvExistingPlayerCount.value = count
                csvParseResult.value = result
            }
        } catch (e: Exception) {
            android.util.Log.e("CsvRosterParser", "Erro de leitura do arquivo CSV", e)
            withContext(Dispatchers.Main) {
                csvParseResult.value = CsvParseResult(emptyList(), listOf("Erro de leitura do arquivo CSV: ${e.message}"))
            }
        }
    }

    fun confirmCsvImport(mappings: List<CsvTeamMapping> = emptyList()) = viewModelScope.launch {
        val leagueId = selectedLeagueId.value ?: uiState.value.leagues.firstOrNull()?.id ?: return@launch
        val parsed = csvParseResult.value ?: return@launch

        val summary = if (mappings.isEmpty()) {
            importCsvRostersUseCase(
                leagueId = leagueId,
                players = parsed.players,
                parseErrors = parsed.errors,
                onProgress = { p -> csvImportProgress.value = p }
            )
        } else {
            importCsvRostersUseCase(
                leagueId = leagueId,
                mappings = mappings,
                players = parsed.players,
                parseErrors = parsed.errors,
                onProgress = { p -> csvImportProgress.value = p }
            )
        }

        csvImportSummary.value = summary
    }

    fun dismissCsvImport() {
        csvParseResult.value = null
        csvExistingPlayerCount.value = 0
        csvImportProgress.value = null
        csvImportSummary.value = null
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
    val clubs: List<Club> = emptyList(),
    val message: String? = null,
)
