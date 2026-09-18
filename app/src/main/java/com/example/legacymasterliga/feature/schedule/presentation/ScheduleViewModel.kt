package com.example.legacymasterliga.feature.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.core.navigation.CupIsolationPolicy
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.results.domain.ResultsRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRound
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import kotlinx.coroutines.flow.firstOrNull
import com.example.legacymasterliga.domain.repository.PlayerRepository
import com.example.legacymasterliga.feature.results.domain.GoalRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    leagueRepository: LeagueRepository,
    authRepository: AuthRepository,
    private val playerRepository: PlayerRepository,
    private val scheduleRepository: ScheduleRepository,
    private val resultsRepository: ResultsRepository,
) : ViewModel() {
    private val requestedCompetitionId = savedStateHandle.get<Long>("competitionId")?.takeIf { it > 0L }
    private val selectedLeagueId = MutableStateFlow(savedStateHandle.get<Long>("leagueId")?.takeIf { it > 0L })
    private val selectedSeasonId = MutableStateFlow(savedStateHandle.get<Long>("seasonId")?.takeIf { it > 0L })
    val initialTabIndex: Int = if (savedStateHandle.get<String>("tab") == "bracket") 1 else 0
    private val isGenerating = MutableStateFlow(false)
    private val feedback = MutableStateFlow<String?>(null)

    private val homeRoster = MutableStateFlow<List<com.example.legacymasterliga.domain.model.Player>>(emptyList())
    private val awayRoster = MutableStateFlow<List<com.example.legacymasterliga.domain.model.Player>>(emptyList())

    private val leagues = leagueRepository.observeAll()
    private val allSeasons = selectedLeagueId.flatMapLatest { leagueId ->
        if (leagueId == null) flowOf(emptyList()) else scheduleRepository.observeSeasonOptions(leagueId)
    }
    private val seasons = allSeasons.map(CupIsolationPolicy::leagueSeasons)
    private val cupRouteBlocked = allSeasons.map { options ->
        CupIsolationPolicy.blocksLegacySchedule(requestedCompetitionId, options)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    private val rounds = combine(selectedSeasonId, seasons, cupRouteBlocked) { seasonId, leagueSeasons, blocked ->
        seasonId?.takeIf { id -> !blocked && leagueSeasons.any { it.seasonId == id } }
    }.flatMapLatest { seasonId ->
        if (seasonId == null) flowOf(emptyList()) else scheduleRepository.observeSchedule(seasonId)
    }

    private val selection = combine(leagues, selectedLeagueId, seasons, selectedSeasonId, cupRouteBlocked) { leagueList, leagueId, seasonList, seasonId, blocked ->
        SelectionState(leagueList, leagueId, seasonList, seasonId, blocked)
    }
    private val activity = combine(rounds, isGenerating, feedback, homeRoster, awayRoster) { schedule, generating, message, home, away ->
        DataActivity(schedule, generating, message, home, away)
    }

    val uiState: StateFlow<ScheduleUiState> = combine(
        selection, activity, authRepository.currentUser
    ) { selected, act, user ->
        val (leagueList, leagueId, seasonList, seasonId) = selected
        val (schedule, generating, message, home, away) = act
        val effectiveLeagueId = leagueId ?: leagueList.firstOrNull()?.id
        if (leagueId == null && effectiveLeagueId != null) selectedLeagueId.value = effectiveLeagueId
        val effectiveSeasonId = seasonId?.takeIf { id ->
            seasonList.any { it.seasonId == id && (requestedCompetitionId == null || it.competitionId == requestedCompetitionId) }
        }
            ?: seasonList.firstOrNull()?.seasonId
        if (effectiveSeasonId != seasonId) selectedSeasonId.value = effectiveSeasonId
        ScheduleUiState(
            leagues = leagueList,
            selectedLeagueId = effectiveLeagueId,
            seasons = seasonList,
            selectedSeasonId = effectiveSeasonId,
            rounds = schedule,
            isGenerating = generating,
            feedback = message,
            isCupRouteBlocked = selected.cupRouteBlocked,
            role = user?.role,
            homeRoster = home,
            awayRoster = away
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    // Filtro por clube — null significa sem filtro ativo
    val filterClubA = MutableStateFlow<Long?>(null)
    val filterClubB = MutableStateFlow<Long?>(null)

    // Lista de clubes disponíveis para o filtro (extraída das próprias rodadas)
    val filterableClubs: StateFlow<List<Pair<Long, String>>> = uiState
        .map { state ->
            state.rounds
                .flatMap { it.matches }
                .flatMap { match ->
                    listOf(
                        match.homeClubId to match.homeClubName,
                        match.awayClubId to match.awayClubName,
                    )
                }
                .distinctBy { it.first }
                .sortedBy { it.second }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Rodadas filtradas — aplica o filtro sobre as rodadas já carregadas
    val filteredRounds: StateFlow<List<ScheduleRound>> = combine(
        uiState.map { it.rounds },
        filterClubA,
        filterClubB,
    ) { rounds, clubA, clubB ->
        if (clubA == null && clubB == null) return@combine rounds
        rounds.mapNotNull { round ->
            val matchesFiltradas = round.matches.filter { match ->
                val envolvidos = setOf(match.homeClubId, match.awayClubId)
                when {
                    clubA != null && clubB != null ->
                        envolvidos.containsAll(setOf(clubA, clubB))
                    clubA != null -> envolvidos.contains(clubA)
                    clubB != null -> envolvidos.contains(clubB)
                    else -> true
                }
            }
            if (matchesFiltradas.isEmpty()) null
            else round.copy(matches = matchesFiltradas)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterClubA(clubId: Long?) { filterClubA.value = clubId }
    fun setFilterClubB(clubId: Long?) { filterClubB.value = clubId }
    fun clearFilter() { filterClubA.value = null; filterClubB.value = null }

    fun selectLeague(id: Long) {
        selectedLeagueId.value = id
        selectedSeasonId.value = null
    }

    fun selectSeason(id: Long) {
        selectedSeasonId.value = id
    }

    fun loadRosters(homeClubId: Long, awayClubId: Long) {
        viewModelScope.launch {
            playerRepository.observeByClub(homeClubId).firstOrNull()?.let { homeRoster.value = it }
            playerRepository.observeByClub(awayClubId).firstOrNull()?.let { awayRoster.value = it }
        }
    }

    fun generate() {
        if (cupRouteBlocked.value) return
        val seasonId = selectedSeasonId.value ?: return
        viewModelScope.launch {
            isGenerating.value = true
            runCatching { scheduleRepository.generateSchedule(seasonId) }
                .onSuccess { result ->
                    feedback.value = "${result.roundsCreated} rodadas e ${result.matchesCreated} partidas criadas."
                }
                .onFailure { error -> feedback.value = error.message ?: "Não foi possível gerar os jogos." }
            isGenerating.value = false
        }
    }

    fun saveResult(
        matchId: Long,
        homeScore: Int,
        awayScore: Int,
        penaltiesHome: Int? = null,
        penaltiesAway: Int? = null,
        winnerId: Long? = null,
        goals: List<GoalRecord> = emptyList(),
        hy: Int = 0,
        hr: Int = 0,
        ay: Int = 0,
        ar: Int = 0
    ) {
        if (cupRouteBlocked.value) return
        viewModelScope.launch {
            runCatching {
                resultsRepository.saveResult(
                    matchId, homeScore, awayScore, penaltiesHome, penaltiesAway, winnerId, goals,
                    hy, hr, ay, ar
                )
            }
                .onSuccess { feedback.value = "Resultado salvo e classificação atualizada." }
                .onFailure { error -> feedback.value = error.message ?: "Não foi possível salvar o resultado." }
        }
    }

    fun clearFeedback() {
        feedback.value = null
    }
}

private data class SelectionState(
    val leagues: List<League>,
    val leagueId: Long?,
    val seasons: List<ScheduleSeasonOption>,
    val seasonId: Long?,
    val cupRouteBlocked: Boolean,
)

private data class DataActivity(
    val rounds: List<ScheduleRound>,
    val isGenerating: Boolean,
    val feedback: String?,
    val homeRoster: List<com.example.legacymasterliga.domain.model.Player>,
    val awayRoster: List<com.example.legacymasterliga.domain.model.Player>
)

data class ScheduleUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val seasons: List<ScheduleSeasonOption> = emptyList(),
    val selectedSeasonId: Long? = null,
    val rounds: List<ScheduleRound> = emptyList(),
    val isGenerating: Boolean = false,
    val feedback: String? = null,
    val isCupRouteBlocked: Boolean = false,
    val role: com.example.legacymasterliga.core.model.UserRole? = null,
    val homeRoster: List<com.example.legacymasterliga.domain.model.Player> = emptyList(),
    val awayRoster: List<com.example.legacymasterliga.domain.model.Player> = emptyList(),
)
