package com.example.legacymasterliga.feature.audit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.audit.domain.AuditLog
import com.example.legacymasterliga.feature.audit.domain.AuditQuery
import com.example.legacymasterliga.feature.audit.domain.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AuditViewModel @Inject constructor(
    repository: AuditRepository,
    leagueRepository: LeagueRepository,
) : ViewModel() {
    private val query = MutableStateFlow(AuditQuery())
    private val logs = query.flatMapLatest(repository::observeLogs)

    private val filterOptions = combine(
        repository.observeCategories(),
        repository.observeActions(),
        repository.observeCount(),
        leagueRepository.observeAll(),
    ) { categories, actions, count, leagues ->
        FilterOptions(categories, actions, count, leagues)
    }

    val uiState: StateFlow<AuditUiState> = combine(logs, filterOptions, query) { logs, options, query ->
        AuditUiState(logs, options.categories, options.actions, options.totalCount, options.leagues, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuditUiState())

    fun setText(value: String) { query.value = query.value.copy(text = value) }
    fun setLeague(value: Long?) { query.value = query.value.copy(leagueId = value) }
    fun setCategory(value: String?) { query.value = query.value.copy(category = value) }
    fun setAction(value: String?) { query.value = query.value.copy(action = value) }
    fun clearFilters() { query.value = AuditQuery() }
}

data class AuditUiState(
    val logs: List<AuditLog> = emptyList(),
    val categories: List<String> = emptyList(),
    val actions: List<String> = emptyList(),
    val totalCount: Int = 0,
    val leagues: List<League> = emptyList(),
    val query: AuditQuery = AuditQuery(),
)

private data class FilterOptions(val categories: List<String>, val actions: List<String>, val totalCount: Int, val leagues: List<League>)
