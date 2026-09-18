package com.example.legacymasterliga.feature.topscorers.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.navigation.CupIsolationPolicy
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import com.example.legacymasterliga.core.database.dao.GoalEventDao
import com.example.legacymasterliga.core.database.dao.TopScorerRow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TopScorersViewModel @Inject constructor(
    leagueRepository: LeagueRepository,
    private val scheduleRepository: ScheduleRepository,
    private val goalEventDao: GoalEventDao,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedCompetitionId = MutableStateFlow<Long?>(null)

    private val leagues = leagueRepository.observeAll().onEach { list ->
        if (selectedLeagueId.value == null && list.isNotEmpty()) {
            selectedLeagueId.value = list.first().id
        }
    }
    
    private val seasons = selectedLeagueId.flatMapLatest { leagueId ->
        if (leagueId == null) flowOf(emptyList()) else scheduleRepository.observeSeasonOptions(leagueId)
    }.map(CupIsolationPolicy::leagueSeasons).onEach { list ->
        if (selectedCompetitionId.value == null && list.isNotEmpty()) {
            selectedCompetitionId.value = list.first().competitionId
        }
    }
    
    private val topScorers = selectedCompetitionId.flatMapLatest { compId ->
        if (compId == null) flowOf(emptyList()) else goalEventDao.observeTopScorers(compId)
    }

    val uiState: StateFlow<TopScorersUiState> = combine(
        leagues, selectedLeagueId, seasons, selectedCompetitionId, topScorers
    ) { leagueList, leagueId, seasonList, compId, scorers ->
        TopScorersUiState(
            leagues = leagueList,
            selectedLeagueId = leagueId,
            competitions = seasonList.distinctBy { it.competitionId },
            selectedCompetitionId = compId,
            topScorers = scorers
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TopScorersUiState())

    fun selectLeague(id: Long) {
        if (selectedLeagueId.value == id) return
        selectedLeagueId.value = id
        selectedCompetitionId.value = null
    }
    
    fun selectCompetition(id: Long) {
        selectedCompetitionId.value = id
    }
}

data class TopScorersUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val competitions: List<ScheduleSeasonOption> = emptyList(),
    val selectedCompetitionId: Long? = null,
    val topScorers: List<TopScorerRow> = emptyList(),
    val isLoading: Boolean = false
)
