package com.example.legacymasterliga.feature.closure.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.session.SessionManager
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.closure.domain.*
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SeasonClosureViewModel @Inject constructor(
    leagueRepository: LeagueRepository,
    private val competitionRepository: CompetitionRepository,
    private val closureRepository: SeasonClosureRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedSeasonId = MutableStateFlow<Long?>(null)
    private val selectedCompetitionId = MutableStateFlow<Long?>(null)
    private val preview = MutableStateFlow<ClosurePreview?>(null)
    private val feedback = MutableStateFlow<String?>(null)
    private val user = sessionManager.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    // Suporte ao diálogo de premiação manual
    private val selectedPrizeCompetitionId = MutableStateFlow<Long?>(null)
    private val selectedPrizeSeasonId = MutableStateFlow<Long?>(null)
    private val prizeParticipants = MutableStateFlow<List<PrizeClubOption>>(emptyList())

    private val leagues = leagueRepository.observeAll()
    private val competitions: StateFlow<List<CompetitionSummary>> = selectedLeagueId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else competitionRepository.observeByLeague(id) }
        .map { list -> list.filter { it.type == CompetitionType.LEAGUE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val prizeConfig = selectedCompetitionId.flatMapLatest { id ->
        if (id == null) flowOf(null) else closureRepository.observePrizeConfiguration(id).map { it as PrizeConfiguration? }
    }
    private val prizeHistory = selectedLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else closureRepository.observePrizeHistory(id)
    }
    
    // Fluxos para o seletor de premiação
    private val allPrizeCompetitions = selectedLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else closureRepository.observeCompetitions(id)
    }
    private val prizeCompetitions = combine(allPrizeCompetitions, competitions) { options, leagueCompetitions ->
        val activeLeagueIds = leagueCompetitions.mapTo(mutableSetOf()) { it.id }
        options.filter { it.id in activeLeagueIds }
    }
    private val prizeSeasons = selectedPrizeCompetitionId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else closureRepository.observeSeasons(id)
    }

    private val context = combine(leagues, selectedLeagueId, competitions, selectedSeasonId) { l, lid, c, sid ->
        val effectiveLeague = lid ?: l.firstOrNull()?.id
        if (lid == null && effectiveLeague != null) selectedLeagueId.value = effectiveLeague
        ClosureContext(l, effectiveLeague, c, sid)
    }

    val uiState: StateFlow<SeasonClosureUiState> = combine(
        context, preview, prizeConfig, prizeHistory, feedback, 
        prizeCompetitions, prizeSeasons, prizeParticipants
    ) { params ->
        val ctx = params[0] as ClosureContext
        val p = params[1] as ClosurePreview?
        val config = params[2] as PrizeConfiguration?
        val history = params[3] as List<PrizeHistoryItem>
        val f = params[4] as String?
        val pComps = params[5] as List<PrizeCompetitionOption>
        val pSeasons = params[6] as List<PrizeSeasonOption>
        val pClubs = params[7] as List<PrizeClubOption>

        SeasonClosureUiState(
            leagues = ctx.leagues,
            selectedLeagueId = ctx.selectedLeagueId,
            competitions = ctx.competitions,
            selectedSeasonId = ctx.selectedSeasonId,
            selectedCompetitionId = selectedCompetitionId.value,
            preview = p,
            prizeConfiguration = config,
            prizeHistory = history,
            feedback = f,
            prizeCompetitions = pComps,
            prizeSeasons = pSeasons,
            prizeParticipants = pClubs
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeasonClosureUiState())

    fun selectLeague(id: Long) {
        selectedLeagueId.value = id
        selectedSeasonId.value = null
        selectedCompetitionId.value = null
        preview.value = null
        selectedPrizeCompetitionId.value = null
        selectedPrizeSeasonId.value = null
        prizeParticipants.value = emptyList()
    }

    fun selectSeason(id: Long) {
        selectedSeasonId.value = id
        selectedCompetitionId.value = competitions.value.firstNotNullOfOrNull { competition ->
            competition.seasons.firstOrNull { it.id == id }?.let { competition.id }
        }
        viewModelScope.launch { preview.value = runCatching { closureRepository.preview(id) }.getOrNull() }
    }
    
    fun selectPrizeCompetition(id: Long) {
        selectedPrizeCompetitionId.value = id
        selectedPrizeSeasonId.value = null
        prizeParticipants.value = emptyList()
    }
    
    fun selectPrizeSeason(id: Long) {
        selectedPrizeSeasonId.value = id
        viewModelScope.launch {
            prizeParticipants.value = closureRepository.findParticipants(id)
        }
    }

    fun savePrizeConfiguration(champion: String, runnerUp: String, participation: String) {
        val actor = user.value ?: return setFeedback("Sessão inválida.")
        val competitionId = selectedCompetitionId.value ?: return setFeedback("Selecione uma temporada ativa.")
        viewModelScope.launch {
            runCatching {
                closureRepository.savePrizeConfiguration(
                    actor,
                    PrizeConfiguration(
                        competitionId = competitionId,
                        championPrizeCr = champion.toLongOrNull() ?: 0L,
                        runnerUpPrizeCr = runnerUp.toLongOrNull() ?: 0L,
                        participationPrizeCr = participation.toLongOrNull() ?: 0L,
                    ),
                )
            }.onSuccess { feedback.value = "Premiações salvas para a competição." }
                .onFailure { feedback.value = it.message ?: "Não foi possível salvar as premiações." }
        }
    }

    fun closeSeason(finishCompetition: Boolean) {
        val seasonId = selectedSeasonId.value ?: return setFeedback("Selecione uma temporada ativa.")
        val actor = user.value ?: return setFeedback("Sessão inválida.")
        viewModelScope.launch {
            runCatching { closureRepository.close(actor, CloseSeasonRequest(seasonId, finishCompetition)) }
                .onSuccess { feedback.value = "Temporada encerrada oficialmente."; preview.value = closureRepository.preview(seasonId) }
                .onFailure { feedback.value = it.message ?: "Não foi possível encerrar a temporada." }
        }
    }

    fun awardPrize(competitionId: Long, seasonId: Long, clubId: Long, type: String, amount: Long, description: String) {
        val actor = user.value ?: return setFeedback("Sessão inválida.")
        val leagueId = selectedLeagueId.value ?: return setFeedback("Selecione uma liga.")
        viewModelScope.launch {
            runCatching {
                closureRepository.awardPrize(actor, leagueId, competitionId, seasonId, clubId, type, amount, description)
            }.onSuccess { feedback.value = "Premiação distribuída com sucesso." }
                .onFailure { feedback.value = it.message ?: "Não foi possível distribuir a premiação." }
        }
    }

    fun clearFeedback() { feedback.value = null }
    private fun setFeedback(message: String) { feedback.value = message }
}

private data class ClosureContext(
    val leagues: List<League>,
    val selectedLeagueId: Long?,
    val competitions: List<CompetitionSummary>,
    val selectedSeasonId: Long?,
)

data class SeasonClosureUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val competitions: List<CompetitionSummary> = emptyList(),
    val selectedSeasonId: Long? = null,
    val selectedCompetitionId: Long? = null,
    val preview: ClosurePreview? = null,
    val prizeConfiguration: PrizeConfiguration? = null,
    val prizeHistory: List<PrizeHistoryItem> = emptyList(),
    val feedback: String? = null,
    val prizeCompetitions: List<PrizeCompetitionOption> = emptyList(),
    val prizeSeasons: List<PrizeSeasonOption> = emptyList(),
    val prizeParticipants: List<PrizeClubOption> = emptyList(),
)
