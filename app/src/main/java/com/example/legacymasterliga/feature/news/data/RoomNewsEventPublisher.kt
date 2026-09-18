package com.example.legacymasterliga.feature.news.data

import com.example.legacymasterliga.core.database.dao.NewsDao
import com.example.legacymasterliga.core.database.entity.NewsEntity
import com.example.legacymasterliga.feature.news.domain.NewsEvent
import com.example.legacymasterliga.feature.news.domain.NewsEventPublisher
import com.example.legacymasterliga.feature.news.domain.NewsTemplateFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomNewsEventPublisher @Inject constructor(
    private val newsDao: NewsDao,
    private val templates: NewsTemplateFactory,
) : NewsEventPublisher {
    override suspend fun publish(event: NewsEvent) {
        val generated = templates.render(event)
        newsDao.upsert(
            NewsEntity(
                leagueId = event.leagueId,
                competitionId = event.competitionId,
                seasonId = event.seasonId,
                title = generated.title,
                body = generated.body,
                category = generated.category,
                eventType = generated.eventType,
                sourceId = event.sourceId,
                dedupKey = event.dedupKey,
                publishedAt = event.occurredAt,
            ),
        )
    }

    override suspend fun remove(dedupKey: String) {
        newsDao.deleteByDedupKey(dedupKey)
    }
}
