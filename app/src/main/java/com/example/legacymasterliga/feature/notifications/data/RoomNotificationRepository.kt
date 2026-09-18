package com.example.legacymasterliga.feature.notifications.data

import com.example.legacymasterliga.core.database.dao.NotificationDao
import com.example.legacymasterliga.core.database.entity.NotificationReadEntity
import com.example.legacymasterliga.feature.notifications.domain.InternalNotification
import com.example.legacymasterliga.feature.notifications.domain.NotificationQuery
import com.example.legacymasterliga.feature.notifications.domain.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomNotificationRepository @Inject constructor(
    private val dao: NotificationDao,
) : NotificationRepository {
    override fun observeNotifications(userId: Long, role: String, query: NotificationQuery): Flow<List<InternalNotification>> =
        dao.observeFiltered(userId, role, query.leagueId, query.category, query.readFilter.name).map { rows ->
            rows.map { row ->
                InternalNotification(row.id, row.leagueId, row.leagueName, row.category, row.title, row.message, row.priority, row.destinationRoute, row.createdAt, row.isRead)
            }
        }

    override fun observeUnreadCount(userId: Long, role: String): Flow<Int> = dao.observeUnreadCount(userId, role)
    override fun observeCategories(): Flow<List<String>> = dao.observeCategories()
    override suspend fun synchronize() = dao.synchronize()
    override suspend fun markRead(notificationId: Long, userId: Long) = dao.markRead(NotificationReadEntity(notificationId, userId))
    override suspend fun markUnread(notificationId: Long, userId: Long) = dao.markUnread(notificationId, userId)
    override suspend fun markAllRead(userId: Long, role: String) = dao.markAllRead(userId, role, System.currentTimeMillis())
}
