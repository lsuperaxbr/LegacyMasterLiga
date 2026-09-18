package com.example.legacymasterliga.feature.news.data

import com.example.legacymasterliga.core.database.dao.NewsDao
import com.example.legacymasterliga.feature.news.domain.NewsArticle
import com.example.legacymasterliga.feature.news.domain.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomNewsRepository @Inject constructor(
    private val newsDao: NewsDao,
) : NewsRepository {
    override fun observeByLeague(leagueId: Long): Flow<List<NewsArticle>> =
        newsDao.observeByLeague(leagueId).map { rows ->
            rows.map { row ->
                NewsArticle(
                    id = row.id,
                    leagueId = row.leagueId,
                    leagueName = row.leagueName,
                    competitionId = row.competitionId,
                    competitionName = row.competitionName,
                    seasonId = row.seasonId,
                    seasonName = row.seasonName,
                    title = row.title,
                    body = row.body,
                    category = row.category,
                    imageUri = row.imageUri,
                    authorUserId = row.authorUserId,
                    publishedAt = row.publishedAt,
                )
            }
        }
}
