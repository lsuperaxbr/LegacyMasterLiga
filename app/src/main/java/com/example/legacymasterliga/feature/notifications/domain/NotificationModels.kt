package com.example.legacymasterliga.feature.notifications.domain

data class InternalNotification(
    val id: Long,
    val leagueId: Long?,
    val leagueName: String?,
    val category: String,
    val title: String,
    val message: String,
    val priority: String,
    val destinationRoute: String?,
    val createdAt: Long,
    val isRead: Boolean,
)

enum class NotificationReadFilter { ALL, UNREAD, READ }

data class NotificationQuery(
    val leagueId: Long? = null,
    val category: String? = null,
    val readFilter: NotificationReadFilter = NotificationReadFilter.ALL,
)

interface NotificationRepository {
    fun observeNotifications(userId: Long, role: String, query: NotificationQuery): kotlinx.coroutines.flow.Flow<List<InternalNotification>>
    fun observeUnreadCount(userId: Long, role: String): kotlinx.coroutines.flow.Flow<Int>
    fun observeCategories(): kotlinx.coroutines.flow.Flow<List<String>>
    suspend fun synchronize()
    suspend fun markRead(notificationId: Long, userId: Long)
    suspend fun markUnread(notificationId: Long, userId: Long)
    suspend fun markAllRead(userId: Long, role: String)
}
