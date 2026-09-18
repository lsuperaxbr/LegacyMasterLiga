package com.example.legacymasterliga.feature.news.domain

import kotlinx.coroutines.flow.Flow

data class NewsArticle(
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
    val imageUri: String? = null,
    val authorUserId: Long? = null,
    val publishedAt: Long,
)

interface NewsRepository {
    fun observeByLeague(leagueId: Long): Flow<List<NewsArticle>>
}
