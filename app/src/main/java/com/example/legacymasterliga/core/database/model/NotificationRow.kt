package com.example.legacymasterliga.core.database.model

data class NotificationRow(
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
