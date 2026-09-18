package com.example.legacymasterliga.feature.participants.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.participants.domain.ParticipantClub
import com.example.legacymasterliga.feature.participants.domain.ParticipantRepository
import com.example.legacymasterliga.feature.participants.domain.SeasonOption
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ParticipantsViewModel @Inject constructor(
    private val leagueRepository: LeagueRepository,
    private val participantRepository: ParticipantRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedSeasonId = MutableStateFlow<Long?>(null)
    private val message = MutableStateFlow<String?>(null)

    private val leagues = leagueRepository.observeAll()
    private val seasons = selectedLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else participantRepository.observeSeasonOptions(id)
    }
    private val clubs = combine(selectedLeagueId, selectedSeasonId) { leagueId, seasonId -> leagueId to seasonId }
        .flatMapLatest { (leagueId, seasonId) ->
            if (leagueId == null || seasonId == null) flowOf(emptyList())
            else participantRepository.observeClubsForSeason(leagueId, seasonId)
        }

    private val selection = combine(leagues, selectedLeagueId, seasons) { leagueList, leagueId, seasonList -> Triple(leagueList, leagueId, seasonList) }
    private val content = combine(selectedSeasonId, clubs, message) { seasonId, clubList, currentMessage -> Triple(seasonId, clubList, currentMessage) }

    val uiState: StateFlow<ParticipantsUiState> = combine(selection, content) { selected, data ->
        val (leagueList, leagueId, seasonList) = selected
        val (seasonId, clubList, currentMessage) = data
        val effectiveLeagueId = leagueId ?: leagueList.firstOrNull()?.id
        if (leagueId == null && effectiveLeagueId != null) selectedLeagueId.value = effectiveLeagueId
        val effectiveSeasonId = seasonId?.takeIf { id -> seasonList.any { it.id == id } } ?: seasonList.firstOrNull()?.id
        if (effectiveSeasonId != seasonId) selectedSeasonId.value = effectiveSeasonId
        ParticipantsUiState(leagueList, effectiveLeagueId, seasonList, effectiveSeasonId, clubList, currentMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ParticipantsUiState())

    fun selectLeague(id: Long) { selectedLeagueId.value = id; selectedSeasonId.value = null }
    fun selectSeason(id: Long) { selectedSeasonId.value = id }
    fun toggleClub(club: ParticipantClub) {
        val seasonId = selectedSeasonId.value ?: return
        viewModelScope.launch {
            runCatching { participantRepository.setSelected(seasonId, club.id, !club.selected) }
                .onSuccess { message.value = if (club.selected) "Clube removido da temporada." else "Clube inscrito na temporada." }
                .onFailure { message.value = it.message ?: "Não foi possível atualizar a inscrição." }
        }
    }
    fun clearMessage() { message.value = null }
}

data class ParticipantsUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val seasons: List<SeasonOption> = emptyList(),
    val selectedSeasonId: Long? = null,
    val clubs: List<ParticipantClub> = emptyList(),
    val message: String? = null,
)
