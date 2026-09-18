package com.example.legacymasterliga.feature.results.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.TieBreakCriterion
import com.example.legacymasterliga.core.navigation.CupIsolationPolicy
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.results.domain.ResultsRepository
import com.example.legacymasterliga.feature.results.domain.Standing
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import com.example.legacymasterliga.feature.settings.domain.CompetitionRules
import com.example.legacymasterliga.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private data class ConfiguredStandings(
    val table: List<Standing> = emptyList(),
    val highlightLeader: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StandingsViewModel @Inject constructor(
    leagueRepository: LeagueRepository,
    scheduleRepository: ScheduleRepository,
    resultsRepository: ResultsRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedSeasonId = MutableStateFlow<Long?>(null)

    private val leagues = leagueRepository.observeAll().onEach { list ->
        if (selectedLeagueId.value == null && list.isNotEmpty()) {
            selectedLeagueId.value = list.first().id
        }
    }
    
    private val seasons = selectedLeagueId.flatMapLatest { leagueId ->
        if (leagueId == null) flowOf(emptyList()) else scheduleRepository.observeSeasonOptions(leagueId)
    }.map(CupIsolationPolicy::leagueSeasons).onEach { list ->
        if (selectedSeasonId.value == null && list.isNotEmpty()) {
            selectedSeasonId.value = list.first().seasonId
        }
    }
    
    private val standings = selectedSeasonId.flatMapLatest { seasonId ->
        if (seasonId == null) flowOf(emptyList()) else resultsRepository.observeStandings(seasonId)
    }

    private val competitionRules = combine(selectedSeasonId, seasons) { seasonId, options ->
        options.firstOrNull { it.seasonId == seasonId }?.competitionId
    }.flatMapLatest { competitionId ->
        if (competitionId == null) flowOf(null) else settingsRepository.observeCompetitionRules(competitionId)
    }

    private val configuredStandings = combine(standings, competitionRules) { table, rules ->
        val currentRules = rules ?: CompetitionRules(competitionId = 0)
        ConfiguredStandings(
            table = table.sortedWith(standingComparator(currentRules.tieBreakCriteria))
                .mapIndexed { index, item ->
                    item.copy(position = index + 1, maximumPointsPerWin = currentRules.pointsForWin.coerceAtLeast(1))
                },
            highlightLeader = currentRules.highlightLeader,
        )
    }

    val uiState: StateFlow<StandingsUiState> = combine(
        leagues, selectedLeagueId, seasons, selectedSeasonId, configuredStandings,
    ) { leagueList, leagueId, seasonList, seasonId, configured ->
        StandingsUiState(
            leagues = leagueList,
            selectedLeagueId = leagueId,
            seasons = seasonList,
            selectedSeasonId = seasonId,
            standings = configured.table,
            highlightLeader = configured.highlightLeader,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StandingsUiState())

    fun selectLeague(id: Long) { 
        if (selectedLeagueId.value == id) return
        selectedLeagueId.value = id
        selectedSeasonId.value = null 
    }
    
    fun selectSeason(id: Long) { 
        selectedSeasonId.value = id 
    }

    private fun standingComparator(criteria: List<TieBreakCriterion>): Comparator<Standing> = Comparator { a, b ->
        for (criterion in criteria) {
            val result = when (criterion) {
                TieBreakCriterion.POINTS -> b.points.compareTo(a.points)
                TieBreakCriterion.WINS -> b.wins.compareTo(a.wins)
                TieBreakCriterion.GOAL_DIFFERENCE -> b.goalDifference.compareTo(a.goalDifference)
                TieBreakCriterion.GOALS_FOR -> b.goalsFor.compareTo(a.goalsFor)
                TieBreakCriterion.HEAD_TO_HEAD -> 0
            }
            if (result != 0) return@Comparator result
        }
        a.clubName.lowercase().compareTo(b.clubName.lowercase())
    }
}

data class StandingsUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val seasons: List<ScheduleSeasonOption> = emptyList(),
    val selectedSeasonId: Long? = null,
    val standings: List<Standing> = emptyList(),
    val highlightLeader: Boolean = true,
)
