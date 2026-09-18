package com.example.legacymasterliga.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.notifications.domain.InternalNotification
import com.example.legacymasterliga.feature.notifications.domain.NotificationQuery
import com.example.legacymasterliga.feature.notifications.domain.NotificationReadFilter
import com.example.legacymasterliga.feature.notifications.domain.NotificationRepository
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
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    authRepository: AuthRepository,
    leagueRepository: LeagueRepository,
) : ViewModel() {
    private val query = MutableStateFlow(NotificationQuery())
    private val currentUser = authRepository.currentUser

    private val notifications = combine(currentUser, query) { user, filter -> user to filter }
        .flatMapLatest { (user, filter) ->
            if (user == null) flowOf(emptyList())
            else repository.observeNotifications(user.id, user.role.name, filter)
        }

    private val unreadCount = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(0) else repository.observeUnreadCount(user.id, user.role.name)
    }

    private val filterOptions = combine(
        repository.observeCategories(),
        leagueRepository.observeAll(),
    ) { categories, leagues -> categories to leagues }

    val uiState: StateFlow<NotificationsUiState> = combine(
        currentUser,
        notifications,
        unreadCount,
        filterOptions,
        query,
    ) { user, items, unread, options, filter ->
        NotificationsUiState(user?.id, user?.role?.name, items, unread, options.first, options.second, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    init { refresh() }

    fun refresh() { viewModelScope.launch { repository.synchronize() } }
    fun setLeague(value: Long?) { query.value = query.value.copy(leagueId = value) }
    fun setCategory(value: String?) { query.value = query.value.copy(category = value) }
    fun setReadFilter(value: NotificationReadFilter) { query.value = query.value.copy(readFilter = value) }
    fun toggleRead(notification: InternalNotification) {
        val userId = uiState.value.userId ?: return
        viewModelScope.launch {
            if (notification.isRead) repository.markUnread(notification.id, userId)
            else repository.markRead(notification.id, userId)
        }
    }
    fun markAllRead() {
        val state = uiState.value
        val userId = state.userId ?: return
        val role = state.role ?: return
        viewModelScope.launch { repository.markAllRead(userId, role) }
    }
}

data class NotificationsUiState(
    val userId: Long? = null,
    val role: String? = null,
    val notifications: List<InternalNotification> = emptyList(),
    val unreadCount: Int = 0,
    val categories: List<String> = emptyList(),
    val leagues: List<League> = emptyList(),
    val query: NotificationQuery = NotificationQuery(),
)
