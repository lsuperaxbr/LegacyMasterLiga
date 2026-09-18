package com.example.legacymasterliga.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.usecase.LogoutUseCase
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.dashboard.domain.DashboardRepository
import com.example.legacymasterliga.feature.dashboard.domain.DashboardSummary
import com.example.legacymasterliga.feature.news.domain.NewsArticle
import com.example.legacymasterliga.feature.news.domain.NewsRepository
import com.example.legacymasterliga.feature.notifications.domain.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val user: User? = null,
    val summary: DashboardSummary = DashboardSummary(),
    val unreadNotifications: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dashboardRepository: DashboardRepository,
    authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val notificationRepository: NotificationRepository,
    private val newsRepository: NewsRepository,
    private val competitionRepository: CompetitionRepository,
) : ViewModel() {
    private val summary = authRepository.currentUser
        .distinctUntilChanged()
        .flatMapLatest { user ->
        if (user == null) flowOf(DashboardSummary())
        else dashboardRepository.observeSummary(user.id)
    }

    val latestNews: StateFlow<List<NewsArticle>> = summary
        .flatMapLatest { s ->
            val id = s.leagueId
            if (id != null && id > 0L) newsRepository.observeByLeague(id)
            else flowOf(emptyList())
        }
        .map { it.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifications: StateFlow<Int> = authRepository.currentUser
        .distinctUntilChanged()
        .flatMapLatest { user ->
        if (user == null) flowOf(0)
        else notificationRepository.observeUnreadCount(user.id, user.role.name)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val latestCupSeasonId: StateFlow<Long?> = summary
        .map { it.leagueId }
        .filterNotNull()
        .flatMapLatest { leagueId -> competitionRepository.observeByLeague(leagueId) }
        .map { competitions ->
            competitions
                .filter { it.type == CompetitionType.CUP }
                .flatMap { it.seasons }
                .maxByOrNull { it.id }
                ?.id
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch { notificationRepository.synchronize() }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        authRepository.currentUser,
        summary,
        unreadNotifications,
    ) { user, dashboard, unread -> DashboardUiState(user, dashboard, unread) }
        .distinctUntilChanged()
        .conflate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}
