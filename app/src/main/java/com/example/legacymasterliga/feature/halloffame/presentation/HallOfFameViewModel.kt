package com.example.legacymasterliga.feature.halloffame.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.halloffame.domain.HallOfFameRepository
import com.example.legacymasterliga.feature.halloffame.domain.HallOfFameSnapshot
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

data class HallOfFameUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val records: HallOfFameSnapshot = HallOfFameSnapshot(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HallOfFameViewModel @Inject constructor(
    leagueRepository: LeagueRepository,
    hallOfFameRepository: HallOfFameRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)

    private val leagueContext = combine(leagueRepository.observeAll(), selectedLeagueId) { leagues, selected ->
        leagues to (selected?.takeIf { id -> leagues.any { it.id == id } } ?: leagues.firstOrNull()?.id)
    }

    private val records = leagueContext.flatMapLatest { (_, leagueId) ->
        if (leagueId == null) flowOf(HallOfFameSnapshot()) else hallOfFameRepository.observeByLeague(leagueId)
    }

    val uiState: StateFlow<HallOfFameUiState> = combine(leagueContext, records) { leagueData, snapshot ->
        HallOfFameUiState(
            leagues = leagueData.first,
            selectedLeagueId = leagueData.second,
            records = snapshot,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HallOfFameUiState())

    fun selectLeague(leagueId: Long) {
        selectedLeagueId.value = leagueId
    }
}
