package com.example.legacymasterliga.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.feature.reports.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsUiState(
    val leagues: List<ReportOption> = emptyList(), val competitions: List<ReportOption> = emptyList(), val seasons: List<ReportOption> = emptyList(),
    val leagueId: Long? = null, val competitionId: Long? = null, val seasonId: Long? = null,
    val isLoading: Boolean = false, val generated: GeneratedReport? = null, val message: String? = null,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(private val repository: ReportRepository) : ViewModel() {
    private val _state = MutableStateFlow(ReportsUiState()); val state: StateFlow<ReportsUiState> = _state.asStateFlow()
    init { viewModelScope.launch { val l=repository.leagues(); _state.value=_state.value.copy(leagues=l, leagueId=l.firstOrNull()?.id); l.firstOrNull()?.id?.let(::loadCompetitions) } }
    fun selectLeague(id: Long) { _state.value=_state.value.copy(leagueId=id, competitionId=null, seasonId=null, seasons=emptyList()); loadCompetitions(id) }
    private fun loadCompetitions(id: Long) { viewModelScope.launch { _state.value=_state.value.copy(competitions=repository.competitions(id)) } }
    fun selectCompetition(id: Long?) { _state.value=_state.value.copy(competitionId=id,seasonId=null,seasons=emptyList()); if(id!=null)viewModelScope.launch{_state.value=_state.value.copy(seasons=repository.seasons(id))} }
    fun selectSeason(id: Long?) { _state.value=_state.value.copy(seasonId=id) }
    fun generate(format: ReportFormat) { val leagueId=_state.value.leagueId?:return; viewModelScope.launch { runCatching { _state.value=_state.value.copy(isLoading=true,message=null); repository.generate(ReportFilters(leagueId,_state.value.competitionId,_state.value.seasonId),format) }.onSuccess { _state.value=_state.value.copy(isLoading=false,generated=it,message="Relatório pronto.") }.onFailure { _state.value=_state.value.copy(isLoading=false,message=it.message?:"Erro ao gerar relatório.") } } }
    fun clearGenerated() { _state.value=_state.value.copy(generated=null) }
    fun copyTo(uri: android.net.Uri) { val report=_state.value.generated?:return; viewModelScope.launch { runCatching { repository.copyTo(uri,report) }.onSuccess { _state.value=_state.value.copy(message="Arquivo salvo com sucesso.") }.onFailure { _state.value=_state.value.copy(message=it.message) } } }
}
