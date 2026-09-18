package com.example.legacymasterliga.core.navigation

import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CupIsolationPolicyTest {
    private val leagueSeason = ScheduleSeasonOption(
        seasonId = 10,
        competitionId = 100,
        competitionName = "Liga Principal",
        seasonName = "Temporada 1",
        format = CompetitionFormat.HOME_AND_AWAY,
        type = CompetitionType.LEAGUE,
    )
    private val historicalCup = ScheduleSeasonOption(
        seasonId = 20,
        competitionId = 200,
        competitionName = "Copa Histórica",
        seasonName = "Edição 1",
        format = CompetitionFormat.KNOCKOUT,
        type = CompetitionType.CUP,
    )

    @Test
    fun `classification and generic schedule expose league seasons only`() {
        val source = listOf(leagueSeason, historicalCup)

        assertEquals(listOf(leagueSeason), CupIsolationPolicy.leagueSeasons(source))
        assertEquals(listOf(leagueSeason, historicalCup), source)
    }

    @Test
    fun `legacy cup schedule is blocked while league schedule remains active`() {
        val options = listOf(leagueSeason, historicalCup)

        assertTrue(CupIsolationPolicy.blocksLegacySchedule(200, options))
        assertFalse(CupIsolationPolicy.blocksLegacySchedule(100, options))
        assertFalse(CupIsolationPolicy.blocksLegacySchedule(null, options))
    }

    @Test
    fun `cup entry never targets standings or schedule`() {
        val route = LegacyNavigationMenu.dashboard(com.example.legacymasterliga.core.model.UserRole.ADMINISTRATOR)
            .single { it.title == "Copa" }
            .route

        assertEquals("cup?leagueId=0&competitionId=0&seasonId=0", route)
        assertFalse(route.startsWith("standings"))
        assertFalse(route.startsWith("schedule"))
    }
}
