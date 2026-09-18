package com.example.legacymasterliga.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.feature.audit.presentation.AuditRoute
import com.example.legacymasterliga.feature.backup.presentation.BackupRoute
import com.example.legacymasterliga.feature.betaguide.presentation.BetaTestGuideScreen
import com.example.legacymasterliga.feature.clubprofile.presentation.ClubProfileRoute
import com.example.legacymasterliga.feature.clubs.presentation.ClubsRoute
import com.example.legacymasterliga.feature.common.presentation.AccessDeniedScreen
import com.example.legacymasterliga.feature.competitions.presentation.CompetitionsRoute
import com.example.legacymasterliga.feature.dashboard.presentation.DashboardRoute
import com.example.legacymasterliga.feature.auction.presentation.AuctionListRoute
import com.example.legacymasterliga.feature.auction.presentation.AuctionCreateRoute
import com.example.legacymasterliga.feature.auction.presentation.AuctionDetailRoute
import com.example.legacymasterliga.feature.cup.presentation.CupSetupRoute
import com.example.legacymasterliga.feature.cup.presentation.CupBracketRoute
import com.example.legacymasterliga.feature.cup.presentation.CupListRoute
import com.example.legacymasterliga.feature.cup.presentation.CupPrizeConfigRoute
import com.example.legacymasterliga.feature.cup.presentation.CupComingSoonScreen
import com.example.legacymasterliga.feature.login.presentation.LoginScreen
import com.example.legacymasterliga.feature.login.presentation.LoginViewModel
import com.example.legacymasterliga.feature.results.presentation.StandingsRoute
import com.example.legacymasterliga.feature.schedule.presentation.ScheduleRoute
import com.example.legacymasterliga.feature.users.presentation.UsersRoute
import com.example.legacymasterliga.feature.finance.presentation.FinanceRoute
import com.example.legacymasterliga.feature.market.presentation.MarketRoute
import com.example.legacymasterliga.feature.topscorers.presentation.TopScorersRoute
import com.example.legacymasterliga.feature.news.presentation.NewsRoute
import com.example.legacymasterliga.feature.notifications.presentation.NotificationsRoute
import com.example.legacymasterliga.feature.settings.presentation.SettingsRoute
import com.example.legacymasterliga.feature.history.presentation.HistoryRoute
import com.example.legacymasterliga.feature.hubs.presentation.CentralHubScreen
import com.example.legacymasterliga.feature.hubs.presentation.HistoryHubScreen
import com.example.legacymasterliga.feature.hubs.presentation.LeagueHubScreen
import com.example.legacymasterliga.feature.hubs.presentation.MoreHubScreen
import com.example.legacymasterliga.feature.halloffame.presentation.HallOfFameRoute
import com.example.legacymasterliga.feature.statistics.presentation.StatisticsRoute
import com.example.legacymasterliga.feature.closure.presentation.SeasonClosureRoute
import com.example.legacymasterliga.feature.reports.presentation.ReportsRoute
import com.example.legacymasterliga.feature.performance.presentation.PerformanceRoute
import com.example.legacymasterliga.feature.presidentprofile.presentation.PresidentProfileRoute
import com.example.legacymasterliga.ui.theme.LegacyMotion

@Composable
fun LegacyNavGraph(rootViewModel: RootViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val currentUser by rootViewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser?.id) {
        val destination = if (currentUser == null) LegacyDestination.Login.route else LegacyDestination.Dashboard.route
        navController.navigate(destination) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = LegacyDestination.Login.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(LegacyMotion.normal))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(LegacyMotion.fast))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(LegacyMotion.normal))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(LegacyMotion.fast))
        },
    ) {
        composable(LegacyDestination.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LoginScreen(
                state = state,
                onLoginModeChanged = viewModel::onLoginModeChanged,
                onOnlineSubModeChanged = viewModel::onOnlineSubModeChanged,
                onUsernameChanged = viewModel::onUsernameChanged,
                onEmailChanged = viewModel::onEmailChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onSignUpNameChanged = viewModel::onSignUpNameChanged,
                onSignUpNicknameChanged = viewModel::onSignUpNicknameChanged,
                onSignUpConfirmPasswordChanged = viewModel::onSignUpConfirmPasswordChanged,
                onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                onLogin = viewModel::login,
                onLogoutOnlineAccount = viewModel::logoutOnlineAccount,
            )
        }
        composable(LegacyDestination.Dashboard.route) {
            DashboardRoute(onOpen = navController::navigate)
        }
        composable(
            route = LegacyDestination.LeagueHub.route,
            arguments = hubContextArguments(),
        ) { backStackEntry ->
            val leagueHubViewModel: com.example.legacymasterliga.feature.hubs.presentation.LeagueHubViewModel = hiltViewModel()
            val state by leagueHubViewModel.uiState.collectAsStateWithLifecycle()
            
            com.example.legacymasterliga.feature.hubs.presentation.LeagueHubScreen(
                state = state,
                context = backStackEntry.hubContext(),
                onBack = navController::navigateUp,
                onOpen = navController::navigate,
            )
        }
        composable(LegacyDestination.CupHub.route) {
            CupComingSoonScreen(onBack = navController::navigateUp)
        }
        composable("cup_list") {
            CupListRoute(
                onBack = navController::navigateUp,
                onOpenCup = { seasonId -> navController.navigate(LegacyDestination.CupBracket.createRoute(seasonId)) },
                onOpenPrizeConfig = { competitionId -> navController.navigate(LegacyDestination.CupPrizeConfig.createRoute(competitionId)) },
                onNavigateToSetup = { navController.navigate(LegacyDestination.CupSetup.route) }
            )
        }
        composable(LegacyDestination.CupSetup.route) {
            CupSetupRoute(
                onBack = navController::navigateUp,
                onCreated = { seasonId -> 
                    navController.navigate(LegacyDestination.CupBracket.createRoute(seasonId)) {
                        popUpTo(LegacyDestination.CupSetup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = LegacyDestination.CupBracket.route,
            arguments = listOf(navArgument("seasonId") { type = NavType.LongType }),
        ) {
            CupBracketRoute(onBack = navController::navigateUp)
        }
        composable(
            route = LegacyDestination.CupPrizeConfig.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType }),
        ) {
            CupPrizeConfigRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.CentralHub.route) {
            CentralHubScreen(onBack = navController::navigateUp, onOpen = navController::navigate)
        }
        composable(LegacyDestination.HistoryHub.route) {
            HistoryHubScreen(onBack = navController::navigateUp, onOpen = navController::navigate)
        }
        composable(LegacyDestination.MoreHub.route) {
            MoreHubScreen(
                role = currentUser?.role,
                onBack = navController::navigateUp,
                onOpen = navController::navigate,
            )
        }
        composable(LegacyDestination.Competitions.route) {
            CompetitionsRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.Clubs.route) {
            ClubsRoute(
                onBack = navController::navigateUp,
                onOpenProfile = { clubId -> navController.navigate(LegacyDestination.ClubProfile.createRoute(clubId)) },
            )
        }
        composable(
            route = LegacyDestination.ClubProfile.route,
            arguments = listOf(
                navArgument("clubId") { type = NavType.LongType },
                navArgument("seasonId") { type = NavType.LongType; defaultValue = 0L },
            ),
        ) { ClubProfileRoute(onBack = navController::navigateUp, onOpenPlayer = { id -> navController.navigate(LegacyDestination.PlayerDetail.createRoute(id)) }) }
        composable(
            route = LegacyDestination.PlayerDetail.route,
            arguments = listOf(navArgument("playerId") { type = NavType.LongType }),
        ) { com.example.legacymasterliga.feature.player.presentation.PlayerDetailRoute(onBack = navController::navigateUp) }
        composable(LegacyDestination.Participants.route) {
            // SPRINT UX-002B: Redirecionamento para a Central da Liga (Fluxo integrado)
            LaunchedEffect(Unit) {
                navController.navigate(LegacyDestination.LeagueHub.createRoute()) {
                    popUpTo(LegacyDestination.Participants.route) { inclusive = true }
                }
            }
        }
        composable(
            route = LegacyDestination.Schedule.route,
            arguments = listOf(
                navArgument("leagueId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("competitionId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("seasonId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("tab") { type = NavType.StringType; defaultValue = "matches" },
            ),
        ) { backStackEntry ->
            ScheduleRoute(
                onBack = navController::navigateUp,
                onCupBlocked = {
                    val context = backStackEntry.hubContext()
                    navController.navigate(
                        LegacyDestination.CupHub.route
                    ) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(LegacyDestination.Standings.route) {
            StandingsRoute(
                onBack = navController::navigateUp,
                onOpenClub = { clubId, seasonId ->
                    navController.navigate(LegacyDestination.ClubProfile.createRoute(clubId, seasonId))
                },
                onOpenTopScorers = {
                    navController.navigate(LegacyDestination.TopScorers.route)
                }
            )
        }
        composable(LegacyDestination.Finance.route) {
            FinanceRoute(navController::navigateUp)
        }
        composable(LegacyDestination.Market.route) {
            MarketRoute(onBack = navController::navigateUp, onOpenPlayer = { id -> navController.navigate(LegacyDestination.PlayerDetail.createRoute(id)) })
        }
        composable(LegacyDestination.TopScorers.route) {
            TopScorersRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.AuctionList.route) {
            AuctionListRoute(
                onBack = navController::navigateUp,
                onCreateLot = { navController.navigate(LegacyDestination.AuctionCreate.route) },
                onOpenLot = { lotId -> navController.navigate(LegacyDestination.AuctionDetail.createRoute(lotId)) }
            )
        }
        composable(LegacyDestination.AuctionCreate.route) {
            AuctionCreateRoute(
                onBack = navController::navigateUp,
                onCreated = { navController.navigateUp() }
            )
        }
        composable(
            route = LegacyDestination.AuctionDetail.route,
            arguments = listOf(navArgument("lotId") { type = NavType.LongType })
        ) {
            AuctionDetailRoute(
                onBack = navController::navigateUp,
                onOpenPlayer = { id -> navController.navigate(LegacyDestination.PlayerDetail.createRoute(id)) }
            )
        }
        composable(LegacyDestination.Arena.route) {
            com.example.legacymasterliga.feature.arena.presentation.ArenaRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.News.route) { NewsRoute(navController::navigateUp) }
        composable(LegacyDestination.Notifications.route) {
            NotificationsRoute(onBack = navController::navigateUp, onOpenDestination = navController::navigate)
        }
        composable(LegacyDestination.Reports.route) { ReportsRoute(onBack = navController::navigateUp) }
        composable(LegacyDestination.Performance.route) {
            PerformanceRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.BetaTestGuide.route) {
            BetaTestGuideScreen(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.Users.route) {
            UsersRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.Audit.route) {
            AuditRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.Backup.route) {
            BackupRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.Settings.route) {
            SettingsRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.LeagueSettings.route) {
            com.example.legacymasterliga.feature.league.presentation.LeagueSettingsRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.SeasonClosure.route) {
            SeasonClosureRoute(onBack = navController::navigateUp)
        }
        composable(LegacyDestination.HallOfFame.route) {
            HallOfFameRoute(
                onBack = navController::navigateUp,
                onOpenClub = { clubId, seasonId ->
                    navController.navigate(LegacyDestination.ClubProfile.createRoute(clubId, seasonId))
                },
            )
        }
        composable(LegacyDestination.Statistics.route) {
            StatisticsRoute(
                onBack = navController::navigateUp,
                onOpenClub = { clubId, seasonId ->
                    navController.navigate(LegacyDestination.ClubProfile.createRoute(clubId, seasonId))
                },
            )
        }
        composable(LegacyDestination.History.route) {
            HistoryRoute(
                onBack = navController::navigateUp,
                onOpenClub = { clubId, seasonId -> navController.navigate(LegacyDestination.ClubProfile.createRoute(clubId, seasonId)) },
            )
        }
        composable(LegacyDestination.PresidentProfile.route) {
            PresidentProfileRoute(onBack = navController::navigateUp)
        }
    }
}

private fun hubContextArguments() = listOf(
    navArgument("leagueId") { type = NavType.LongType; defaultValue = 0L },
    navArgument("competitionId") { type = NavType.LongType; defaultValue = 0L },
    navArgument("seasonId") { type = NavType.LongType; defaultValue = 0L },
)

private fun androidx.navigation.NavBackStackEntry.hubContext() = LegacyNavigationContext(
    leagueId = arguments?.getLong("leagueId") ?: 0L,
    competitionId = arguments?.getLong("competitionId") ?: 0L,
    seasonId = arguments?.getLong("seasonId") ?: 0L,
)
