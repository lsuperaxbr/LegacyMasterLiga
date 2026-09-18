package com.example.legacymasterliga.core.navigation

import com.example.legacymasterliga.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyNavigationMenuTest {
    @Test
    fun `administrator dashboard contains exactly the eight approved entries`() {
        val items = LegacyNavigationMenu.dashboard(UserRole.ADMINISTRATOR)
        assertEquals(
            listOf(
                "Liga",
                "Copa",
                "Mercado",
                "Clubes",
                "Financeiro",
                "Central",
                "História",
                "Mais",
            ),
            items.map { it.title },
        )
        assertEquals(LegacyDestination.CupHub.
        route, items.single { it.title == "Copa" }.route)
    }

    @Test
    fun `president and visitor never see inaccessible dashboard entries`() {
        assertEquals(
            listOf("Liga", "Copa", "Mercado", "Clubes", "Financeiro", "Central", "História"),
            LegacyNavigationMenu.dashboard(UserRole.PRESIDENT).map { it.title },
        )
        assertEquals(
            listOf("Liga", "Copa", "Clubes", "Central", "História"),
            LegacyNavigationMenu.dashboard(UserRole.VISITOR).map { it.title },
        )
    }

    @Test
    fun `league keeps restricted actions hidden and classification official`() {
        val administratorItems = LegacyNavigationMenu.league(UserRole.ADMINISTRATOR, LegacyNavigationContext(), false).items()
        val presidentItems = LegacyNavigationMenu.league(UserRole.PRESIDENT, LegacyNavigationContext(), false).items()

        assertEquals(
            listOf("Nova Temporada", "Rodadas", "Classificação", "Estatísticas", "Premiação", "Encerrar Temporada", "Configurações da Liga"),
            administratorItems.map { it.title },
        )
        assertEquals(listOf("Rodadas", "Classificação", "Estatísticas"), presidentItems.map { it.title })
    }

    @Test
    fun `cup exposes no active creation schedule standings or closure routes`() {
        val context = LegacyNavigationContext(leagueId = 7, competitionId = 21, seasonId = 42)
        assertTrue(LegacyNavigationMenu.cup(UserRole.ADMINISTRATOR, context).isEmpty())
        assertTrue(LegacyNavigationMenu.cup(UserRole.PRESIDENT, context).isEmpty())
        assertTrue(LegacyNavigationMenu.cup(UserRole.VISITOR, context).isEmpty())
    }

    @Test
    fun `league classification remains the single active standings entry`() {
        val leagueItems = LegacyNavigationMenu.league(UserRole.ADMINISTRATOR, LegacyNavigationContext(), false).items()
        val cupItems = LegacyNavigationMenu.cup(UserRole.ADMINISTRATOR, LegacyNavigationContext()).items()

        assertEquals(1, leagueItems.count { it.route == LegacyDestination.Standings.route })
        assertFalse(cupItems.any { it.route == LegacyDestination.Standings.route })
    }

    @Test
    fun `central history and more map only to existing screens`() {
        assertEquals(
            listOf(LegacyDestination.News.route, LegacyDestination.Notifications.route),
            LegacyNavigationMenu.central().items().map { it.route },
        )
        assertEquals(
            listOf(LegacyDestination.History.route, LegacyDestination.HallOfFame.route),
            LegacyNavigationMenu.history().items().map { it.route },
        )
        assertEquals(
            listOf("Sistema", "Gestão"),
            LegacyNavigationMenu.more(UserRole.ADMINISTRATOR).map { it.title },
        )
        assertTrue(LegacyNavigationMenu.more(UserRole.PRESIDENT).isEmpty())
        assertTrue(LegacyNavigationMenu.more(UserRole.VISITOR).isEmpty())
    }

    private fun List<LegacyMenuSection>.items(): List<LegacyMenuItem> = flatMap { it.items }
}
