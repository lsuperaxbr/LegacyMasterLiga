package com.example.legacymasterliga.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionSummary
import com.example.legacymasterliga.feature.statistics.domain.StatisticsRepository
import com.example.legacymasterliga.feature.statistics.domain.StatisticsSnapshot
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


data class StatisticsUiState(
    val leagues: List<League> = emptyList(),
    val competitions: List<CompetitionSummary> = emptyList(),
    val selectedLeagueId: Long? = null,
    val selectedCompetitionId: Long? = null,
    val selectedSeasonId: Long? = null,
    val snapshot: StatisticsSnapshot = StatisticsSnapshot(),
)

private data class Selection(
    val leagues: List<League>,
    val leagueId: Long?,
    val competitionId: Long?,
    val seasonId: Long?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    leagueRepository: LeagueRepository,
    competitionRepository: CompetitionRepository,
    statisticsRepository: StatisticsRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedCompetitionId = MutableStateFlow<Long?>(null)
    private val selectedSeasonId = MutableStateFlow<Long?>(null)

    private val selection = combine(
        leagueRepository.observeAll(),
        selectedLeagueId,
        selectedCompetitionId,
        selectedSeasonId,
    ) { leagues, requestedLeague, requestedCompetition, requestedSeason ->
        val leagueId = requestedLeague?.takeIf { id -> leagues.any { it.id == id } } ?: leagues.firstOrNull()?.id
        Selection(leagues, leagueId, requestedCompetition, requestedSeason)
    }

    private val competitions = selection.flatMapLatest { current ->
        current.leagueId?.let(competitionRepository::observeByLeague) ?: flowOf(emptyList())
    }.map { list -> list.filter { it.type == CompetitionType.LEAGUE } }

    private val snapshot = combine(selection, competitions) { current, competitionList ->
        val competitionId = current.competitionId?.takeIf { id -> competitionList.any { it.id == id } }
        val seasonId = current.seasonId?.takeIf { id -> competitionList.flatMap { it.seasons }.any { it.id == id } }
        Triple(current, competitionId, seasonId)
    }.flatMapLatest { (current, competitionId, seasonId) ->
        current.leagueId?.let { statisticsRepository.observe(it, competitionId, seasonId) }
            ?: flowOf(StatisticsSnapshot())
    }

    val uiState: StateFlow<StatisticsUiState> = combine(selection, competitions, snapshot) { current, competitionList, data ->
        val competitionId = current.competitionId?.takeIf { id -> competitionList.any { it.id == id } }
        val seasonId = current.seasonId?.takeIf { id -> competitionList.flatMap { it.seasons }.any { it.id == id } }
        StatisticsUiState(
            leagues = current.leagues,
            competitions = competitionList,
            selectedLeagueId = current.leagueId,
            selectedCompetitionId = competitionId,
            selectedSeasonId = seasonId,
            snapshot = data,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    fun selectLeague(id: Long?) {
        selectedLeagueId.value = id
        selectedCompetitionId.value = null
        selectedSeasonId.value = null
    }

    fun selectCompetition(id: Long?) {
        selectedCompetitionId.value = id
        selectedSeasonId.value = null
    }

    fun selectSeason(id: Long?) {
        selectedSeasonId.value = id
    }
}
