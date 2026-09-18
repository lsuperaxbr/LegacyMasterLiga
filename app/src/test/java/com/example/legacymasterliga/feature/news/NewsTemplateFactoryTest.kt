package com.example.legacymasterliga.feature.news

import com.example.legacymasterliga.feature.news.domain.MatchFinishedEvent
import com.example.legacymasterliga.feature.news.domain.NewsTemplateFactory
import com.example.legacymasterliga.feature.news.domain.TransferCompletedEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsTemplateFactoryTest {
    private val factory = NewsTemplateFactory()

    @Test
    fun `same event always generates same article`() {
        val event = transfer(42)
        assertEquals(factory.render(event), factory.render(event))
        assertEquals("TRANSFER:42", event.dedupKey)
    }

    @Test
    fun `templates vary deterministically between events`() {
        val titles = (1L..20L).map { factory.render(transfer(it)).title }.toSet()
        assertTrue(titles.size > 1)
    }

    @Test
    fun `three goal margin is classified as goleada`() {
        val article = factory.render(match(homeScore = 4, awayScore = 1))
        assertEquals("GOLEADA", article.category)
        assertEquals("MATCH", article.eventType)
    }

    @Test
    fun `ordinary score is classified as resultado`() {
        assertEquals("RESULTADO", factory.render(match(homeScore = 2, awayScore = 1)).category)
        assertEquals("RESULTADO", factory.render(match(homeScore = 2, awayScore = 2)).category)
    }

    private fun transfer(id: Long) = TransferCompletedEvent(
        leagueId = 1,
        sourceId = id,
        playerName = "Jogador $id",
        originClubName = "Origem",
        destinationClubName = "Destino",
        valueCr = 100,
        occurredAt = 1_000,
    )

    private fun match(homeScore: Int, awayScore: Int) = MatchFinishedEvent(
        leagueId = 1,
        competitionId = 2,
        seasonId = 3,
        sourceId = 4,
        competitionName = "Liga Principal",
        seasonName = "Temporada 1",
        homeClubName = "Mandante",
        awayClubName = "Visitante",
        homeScore = homeScore,
        awayScore = awayScore,
        occurredAt = 1_000,
    )
}
