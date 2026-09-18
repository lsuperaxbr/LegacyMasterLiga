package com.example.legacymasterliga.core.navigation

sealed interface LegacyDestination {
    val route: String

    data object Login : LegacyDestination { override val route = "login" }
    data object Dashboard : LegacyDestination { override val route = "dashboard" }
    data object LeagueHub : LegacyDestination {
        override val route = "league?leagueId={leagueId}&competitionId={competitionId}&seasonId={seasonId}"

        fun createRoute(
            leagueId: Long = 0L,
            competitionId: Long = 0L,
            seasonId: Long = 0L,
        ): String = "league?leagueId=$leagueId&competitionId=$competitionId&seasonId=$seasonId"
    }
    data object CentralHub : LegacyDestination { override val route = "central" }
    data object HistoryHub : LegacyDestination { override val route = "history_hub" }
    data object MoreHub : LegacyDestination { override val route = "more" }
    data object Competitions : LegacyDestination { override val route = "competitions" }
    data object CupHub : LegacyDestination { override val route = "cup_hub" }
    data object CupSetup : LegacyDestination { override val route = "cup_setup" }
    data object CupBracket : LegacyDestination { 
        override val route = "cup_bracket/{seasonId}"
        fun createRoute(seasonId: Long) = "cup_bracket/$seasonId"
    }
    data object CupPrizeConfig : LegacyDestination {
        override val route = "cup_prize_config/{competitionId}"
        fun createRoute(competitionId: Long) = "cup_prize_config/$competitionId"
    }
    data object Clubs : LegacyDestination { override val route = "clubs" }
    data object ClubProfile : LegacyDestination {
        override val route = "club_profile/{clubId}?seasonId={seasonId}"
        fun createRoute(clubId: Long, seasonId: Long? = null): String =
            "club_profile/$clubId?seasonId=${seasonId ?: 0L}"
    }
    data object PlayerDetail : LegacyDestination {
        override val route = "player_detail/{playerId}"
        fun createRoute(playerId: Long): String = "player_detail/$playerId"
    }
    data object Participants : LegacyDestination { override val route = "participants" }
    data object Schedule : LegacyDestination {
        override val route = "schedule?leagueId={leagueId}&competitionId={competitionId}&seasonId={seasonId}&tab={tab}"
        fun createRoute(leagueId: Long, competitionId: Long, seasonId: Long, openBracket: Boolean): String =
            "schedule?leagueId=$leagueId&competitionId=$competitionId&seasonId=$seasonId&tab=${if (openBracket) "bracket" else "matches"}"
    }
    data object Standings : LegacyDestination { override val route = "standings" }
    data object Finance : LegacyDestination { override val route = "finance" }
    data object Market : LegacyDestination { override val route = "market" }
    data object TopScorers : LegacyDestination { override val route = "top_scorers" }
    data object Arena : LegacyDestination { override val route = "arena" }
    data object AuctionList : LegacyDestination { override val route = "auction_list" }
    data object AuctionCreate : LegacyDestination { override val route = "auction_create" }
    data object AuctionDetail : LegacyDestination {
        override val route = "auction_detail/{lotId}"
        fun createRoute(lotId: Long): String = "auction_detail/$lotId"
    }
    data object News : LegacyDestination { override val route = "news" }
    data object Users : LegacyDestination { override val route = "users" }
    data object Audit : LegacyDestination { override val route = "audit" }
    data object Backup : LegacyDestination { override val route = "backup" }
    data object Settings : LegacyDestination { override val route = "settings" }
    data object LeagueSettings : LegacyDestination { override val route = "league_settings" }
    data object History : LegacyDestination { override val route = "history" }
    data object HallOfFame : LegacyDestination { override val route = "hall_of_fame" }
    data object SeasonClosure : LegacyDestination { override val route = "season_closure" }
    data object Statistics : LegacyDestination { override val route = "statistics" }
    data object Notifications : LegacyDestination { override val route = "notifications" }
    data object Reports : LegacyDestination { override val route = "reports" }
    data object Performance : LegacyDestination { override val route = "performance" }
    data object BetaTestGuide : LegacyDestination { override val route = "beta_test_guide" }
    data object PresidentProfile : LegacyDestination { override val route = "president_profile" }
}
