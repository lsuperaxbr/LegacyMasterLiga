package com.example.legacymasterliga.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.history.domain.HistoricalOverview
import com.example.legacymasterliga.feature.history.domain.HistoricalSeason
import com.example.legacymasterliga.feature.history.domain.HistoricalStanding
import com.example.legacymasterliga.feature.history.domain.HistoryRepository
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


data class HistoryUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val selectedCompetitionId: Long? = null,
    val selectedSeasonId: Long? = null,
    val seasons: List<HistoricalSeason> = emptyList(),
    val visibleSeasons: List<HistoricalSeason> = emptyList(),
    val selectedSeason: HistoricalSeason? = null,
    val finalTable: List<HistoricalStanding> = emptyList(),
    val overview: HistoricalOverview = HistoricalOverview(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    leagueRepository: LeagueRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedCompetitionId = MutableStateFlow<Long?>(null)
    private val selectedSeasonId = MutableStateFlow<Long?>(null)

    private val leagueContext = combine(leagueRepository.observeAll(), selectedLeagueId) { leagues, selected ->
        leagues to (selected ?: leagues.firstOrNull()?.id)
    }

    private val seasons = leagueContext.flatMapLatest { (_, leagueId) ->
        if (leagueId == null) flowOf(emptyList()) else historyRepository.observeSeasonsByLeague(leagueId)
    }

    private val selection = combine(seasons, selectedCompetitionId, selectedSeasonId) { allSeasons, competitionId, seasonId ->
        val effectiveCompetitionId = competitionId?.takeIf { id -> allSeasons.any { it.competitionId == id } }
            ?: allSeasons.firstOrNull()?.competitionId
        val visible = if (effectiveCompetitionId == null) emptyList() else allSeasons.filter { it.competitionId == effectiveCompetitionId }
        val effectiveSeasonId = seasonId?.takeIf { id -> visible.any { it.seasonId == id } }
            ?: visible.firstOrNull()?.seasonId
        Triple(effectiveCompetitionId, effectiveSeasonId, visible)
    }

    private val table = selection.flatMapLatest { (_, seasonId, _) ->
        if (seasonId == null) flowOf(emptyList()) else historyRepository.observeFinalTable(seasonId)
    }

    val uiState: StateFlow<HistoryUiState> = combine(leagueContext, seasons, selection, table) { leagueData, allSeasons, selected, finalTable ->
        val overview = HistoricalOverview(
            seasonCount = allSeasons.size,
            finishedSeasonCount = allSeasons.count { it.status.name == "FINISHED" || it.status.name == "ARCHIVED" },
            competitionCount = allSeasons.map { it.competitionId }.distinct().size,
            completedMatchCount = allSeasons.sumOf { it.completedMatchCount },
            totalGoals = allSeasons.sumOf { it.totalGoals },
        )
        HistoryUiState(
            leagues = leagueData.first,
            selectedLeagueId = leagueData.second,
            selectedCompetitionId = selected.first,
            selectedSeasonId = selected.second,
            seasons = allSeasons,
            visibleSeasons = selected.third,
            selectedSeason = allSeasons.firstOrNull { it.seasonId == selected.second },
            finalTable = finalTable,
            overview = overview,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun selectLeague(id: Long) {
        selectedLeagueId.value = id
        selectedCompetitionId.value = null
        selectedSeasonId.value = null
    }

    fun selectCompetition(id: Long) {
        selectedCompetitionId.value = id
        selectedSeasonId.value = null
    }

    fun selectSeason(id: Long) {
        selectedSeasonId.value = id
    }
}
