package com.example.legacymasterliga.core.database.model

data class NewsFeedRow(
    val id: Long,
    val leagueId: Long,
    val leagueName: String,
    val competitionId: Long?,
    val competitionName: String?,
    val seasonId: Long?,
    val seasonName: String?,
    val title: String,
    val body: String,
    val category: String,
    val eventType: String,
    val sourceId: Long?,
    val dedupKey: String,
    val imageUri: String?,
    val authorUserId: Long?,
    val publishedAt: Long,
)
