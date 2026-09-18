package com.example.legacymasterliga.feature.finance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.finance.domain.ClubBalance
import com.example.legacymasterliga.feature.finance.domain.FinanceRepository
import com.example.legacymasterliga.feature.finance.domain.StatementEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinanceUiState(
    val leagues: List<League> = emptyList(), val selectedLeagueId: Long? = null,
    val selectedClubId: Long? = null, val balances: List<ClubBalance> = emptyList(),
    val statement: List<StatementEntry> = emptyList(), val isAdmin: Boolean = false,
    val feedback: String? = null,
)

private data class FinanceContext(val leagues: List<League>, val leagueId: Long?, val clubId: Long?, val isAdmin: Boolean)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository,
    leagueRepository: LeagueRepository,
    authRepository: AuthRepository,
    clubDao: ClubDao,
) : ViewModel() {
    private val selectedLeague = MutableStateFlow<Long?>(null)
    private val selectedClub = MutableStateFlow<Long?>(null)
    private val feedback = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val user = authRepository.currentUser
    private val presidentClubs = user.flatMapLatest { current ->
        if (current?.role == UserRole.PRESIDENT) clubDao.observeAllByPresident(current.id) else flowOf(emptyList())
    }
    private val context = combine(user, leagueRepository.observeAll(), presidentClubs, selectedLeague, selectedClub) { current, leagues, ownClubs, leagueChoice, clubChoice ->
        val admin = current?.role == UserRole.ADMINISTRATOR
        val leagueId = if (admin) leagueChoice ?: leagues.firstOrNull()?.id else ownClubs.firstOrNull()?.leagueId ?: leagues.firstOrNull()?.id
        val finalClubId = when {
            admin -> clubChoice
            clubChoice != null && ownClubs.any { it.id == clubChoice } -> clubChoice
            else -> ownClubs.firstOrNull()?.id
        }
        FinanceContext(leagues, leagueId, finalClubId, admin)
    }
    private val data = context.flatMapLatest { ctx ->
        val leagueId = ctx.leagueId ?: return@flatMapLatest flowOf(emptyList<ClubBalance>() to emptyList<StatementEntry>())
        combine(repository.observeBalances(leagueId), repository.observeStatement(leagueId, ctx.clubId)) { balances, statement -> balances to statement }
    }
    val uiState: StateFlow<FinanceUiState> = combine(context, data, feedback) { ctx, content, message ->
        FinanceUiState(ctx.leagues, ctx.leagueId, ctx.clubId, content.first, content.second, ctx.isAdmin, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState())

    fun selectLeague(id: Long) { selectedLeague.value = id; selectedClub.value = null }
    fun selectClub(id: Long?) { selectedClub.value = id }
    fun clearFeedback() { feedback.value = null }
    fun adjust(clubId: Long, amountCr: Long, description: String) = viewModelScope.launch {
        val actor = user.firstOrNull()
        if (actor == null) { feedback.value = "Sessão inválida."; return@launch }
        runCatching { repository.adjustBalance(actor, clubId, amountCr, description) }
            .onSuccess { feedback.value = "Lançamento concluído e saldos atualizados." }
            .onFailure { feedback.value = it.message ?: "Não foi possível lançar." }
    }
}
