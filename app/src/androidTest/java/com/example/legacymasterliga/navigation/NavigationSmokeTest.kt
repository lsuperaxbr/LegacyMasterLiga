package com.example.legacymasterliga.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.legacymasterliga.core.navigation.LegacyDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {
    @Test fun destination_routes_are_unique_and_non_blank() {
        val destinations = listOf(
            LegacyDestination.Login, LegacyDestination.Dashboard, LegacyDestination.LeagueHub,
            LegacyDestination.CupHub, LegacyDestination.CentralHub, LegacyDestination.HistoryHub,
            LegacyDestination.MoreHub, LegacyDestination.Competitions,
            LegacyDestination.Clubs, LegacyDestination.ClubProfile, LegacyDestination.Participants,
            LegacyDestination.Schedule, LegacyDestination.Standings, LegacyDestination.Finance,
            LegacyDestination.Market, LegacyDestination.News, LegacyDestination.Users,
            LegacyDestination.Audit, LegacyDestination.Backup, LegacyDestination.Settings,
            LegacyDestination.History, LegacyDestination.HallOfFame, LegacyDestination.SeasonClosure,
            LegacyDestination.Statistics, LegacyDestination.Notifications, LegacyDestination.Reports,
            LegacyDestination.Performance, LegacyDestination.BetaTestGuide,
        )
        val routes = destinations.map { it.route }
        assertTrue(routes.all(String::isNotBlank))
        assertEquals(routes.size, routes.distinct().size)
    }

    @Test fun club_profile_route_contains_identifiers() {
        assertEquals("club_profile/7?seasonId=3", LegacyDestination.ClubProfile.createRoute(7, 3))
    }
}
