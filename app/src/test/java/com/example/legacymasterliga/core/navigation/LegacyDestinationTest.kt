package com.example.legacymasterliga.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyDestinationTest {
    @Test
    fun `legacy routes remain unchanged`() {
        val routes = linkedMapOf(
            "Login" to LegacyDestination.Login.route,
            "Dashboard" to LegacyDestination.Dashboard.route,
            "Competitions" to LegacyDestination.Competitions.route,
            "Clubs" to LegacyDestination.Clubs.route,
            "ClubProfile" to LegacyDestination.ClubProfile.route,
            "Participants" to LegacyDestination.Participants.route,
            "Schedule" to LegacyDestination.Schedule.route,
            "Standings" to LegacyDestination.Standings.route,
            "Finance" to LegacyDestination.Finance.route,
            "Market" to LegacyDestination.Market.route,
            "News" to LegacyDestination.News.route,
            "Users" to LegacyDestination.Users.route,
            "Audit" to LegacyDestination.Audit.route,
            "Backup" to LegacyDestination.Backup.route,
            "Settings" to LegacyDestination.Settings.route,
            "History" to LegacyDestination.History.route,
            "HallOfFame" to LegacyDestination.HallOfFame.route,
            "SeasonClosure" to LegacyDestination.SeasonClosure.route,
            "Statistics" to LegacyDestination.Statistics.route,
            "Notifications" to LegacyDestination.Notifications.route,
            "Reports" to LegacyDestination.Reports.route,
            "Performance" to LegacyDestination.Performance.route,
            "BetaTestGuide" to LegacyDestination.BetaTestGuide.route,
        )

        assertEquals(
            linkedMapOf(
                "Login" to "login",
                "Dashboard" to "dashboard",
                "Competitions" to "competitions",
                "Clubs" to "clubs",
                "ClubProfile" to "club_profile/{clubId}?seasonId={seasonId}",
                "Participants" to "participants",
                "Schedule" to "schedule?leagueId={leagueId}&competitionId={competitionId}&seasonId={seasonId}&tab={tab}",
                "Standings" to "standings",
                "Finance" to "finance",
                "Market" to "market",
                "News" to "news",
                "Users" to "users",
                "Audit" to "audit",
                "Backup" to "backup",
                "Settings" to "settings",
                "History" to "history",
                "HallOfFame" to "hall_of_fame",
                "SeasonClosure" to "season_closure",
                "Statistics" to "statistics",
                "Notifications" to "notifications",
                "Reports" to "reports",
                "Performance" to "performance",
                "BetaTestGuide" to "beta_test_guide",
            ),
            routes,
        )
    }

    @Test
    fun `bracket route keeps league competition season and selected tab`() {
        assertEquals(
            "schedule?leagueId=7&competitionId=21&seasonId=42&tab=bracket",
            LegacyDestination.Schedule.createRoute(leagueId = 7, competitionId = 21, seasonId = 42, openBracket = true),
        )
    }

    @Test
    fun `hub routes keep explicit context`() {
        assertEquals(
            "league?leagueId=7&competitionId=21&seasonId=42",
            LegacyDestination.LeagueHub.createRoute(leagueId = 7, competitionId = 21, seasonId = 42),
        )
    }

    @Test
    fun `all route patterns are unique`() {
        val routes = listOf(
            LegacyDestination.Login.route,
            LegacyDestination.Dashboard.route,
            LegacyDestination.LeagueHub.route,
            LegacyDestination.CupHub.route,
            LegacyDestination.CentralHub.route,
            LegacyDestination.HistoryHub.route,
            LegacyDestination.MoreHub.route,
            LegacyDestination.Competitions.route,
            LegacyDestination.Clubs.route,
            LegacyDestination.ClubProfile.route,
            LegacyDestination.Participants.route,
            LegacyDestination.Schedule.route,
            LegacyDestination.Standings.route,
            LegacyDestination.Finance.route,
            LegacyDestination.Market.route,
            LegacyDestination.News.route,
            LegacyDestination.Users.route,
            LegacyDestination.Audit.route,
            LegacyDestination.Backup.route,
            LegacyDestination.Settings.route,
            LegacyDestination.History.route,
            LegacyDestination.HallOfFame.route,
            LegacyDestination.SeasonClosure.route,
            LegacyDestination.Statistics.route,
            LegacyDestination.Notifications.route,
            LegacyDestination.Reports.route,
            LegacyDestination.Performance.route,
            LegacyDestination.BetaTestGuide.route,
        )

        assertTrue(routes.size == routes.toSet().size)
    }
}
