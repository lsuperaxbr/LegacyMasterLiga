package com.example.legacymasterliga.feature.schedule.presentation

import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.LeagueStatus
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.PlayerRepository
import com.example.legacymasterliga.feature.results.domain.GoalRecord
import com.example.legacymasterliga.feature.results.domain.PodiumSummary
import com.example.legacymasterliga.feature.results.domain.ResultsRepository
import com.example.legacymasterliga.feature.results.domain.Standing
import com.example.legacymasterliga.feature.schedule.domain.ScheduleGenerationResult
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRound
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ScheduleViewModelCupIsolationTest {
    @Test
    fun `legacy cup route neither observes nor generates rounds`() {
        val scheduleRepository = RecordingScheduleRepository()
        val viewModel = ScheduleViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("leagueId" to 1L, "competitionId" to 200L, "seasonId" to 20L, "tab" to "bracket"),
            ),
            leagueRepository = FakeLeagueRepository(),
            authRepository = FakeAuthRepository(),
            playerRepository = FakePlayerRepository(),
            scheduleRepository = scheduleRepository,
            resultsRepository = FakeResultsRepository(),
        )
        val scope = CoroutineScope(Dispatchers.Main.immediate)
        var latest = ScheduleUiState()
        viewModel.uiState.onEach { latest = it }.launchIn(scope)

        shadowOf(Looper.getMainLooper()).idle()
        viewModel.generate()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(latest.isCupRouteBlocked)
        assertTrue(latest.seasons.all { it.type == CompetitionType.LEAGUE })
        assertEquals(0, scheduleRepository.observeScheduleCalls)
        assertEquals(0, scheduleRepository.generateCalls)
        scope.cancel()
    }

    private class FakeLeagueRepository : LeagueRepository {
        override fun observeAll(): Flow<List<League>> = MutableStateFlow(
            listOf(League(1L, "Liga Principal", "CR", LeagueStatus.ACTIVE)),
        )

        override suspend fun findByName(name: String): League? = null
        override suspend fun create(name: String, currencyCode: String): Long = error("Not used")
    }

    private class FakeAuthRepository : AuthRepository {
        override val currentUser = flowOf(null)
        override suspend fun login(username: String, password: CharArray): Result<User> = Result.failure(Exception("Not used"))
        override suspend fun logout() = Unit
    }

    private class FakePlayerRepository : PlayerRepository {
        override fun observeByClub(clubId: Long): Flow<List<com.example.legacymasterliga.domain.model.Player>> = flowOf(emptyList())
        override fun observeMarket(leagueId: Long, status: com.example.legacymasterliga.core.model.MarketStatus?, clubId: Long?, query: String?): Flow<List<com.example.legacymasterliga.domain.model.Player>> = flowOf(emptyList())
        override suspend fun findById(id: Long) = null
        override suspend fun savePlayer(player: com.example.legacymasterliga.domain.model.Player): Long = 0L
        override suspend fun updateMarketStatus(playerId: Long, status: com.example.legacymasterliga.core.model.MarketStatus, price: Long?) = Unit
        override suspend fun updateClubPlayersMarket(clubId: Long, status: com.example.legacymasterliga.core.model.MarketStatus, price: Long?) = Unit
        override suspend fun deletePlayer(playerId: Long) = Unit
    }

    private class RecordingScheduleRepository : ScheduleRepository {
        var observeScheduleCalls = 0
        var generateCalls = 0

        override fun observeSeasonOptions(leagueId: Long): Flow<List<ScheduleSeasonOption>> = MutableStateFlow(
            listOf(
                ScheduleSeasonOption(
                    10L, 100L, "Liga Principal", "Temporada 1",
                    CompetitionFormat.HOME_AND_AWAY, CompetitionType.LEAGUE,
                ),
                ScheduleSeasonOption(
                    20L, 200L, "Copa Histórica", "Edição 1",
                    CompetitionFormat.KNOCKOUT, CompetitionType.CUP,
                ),
            ),
        )

        override fun observeSchedule(seasonId: Long): Flow<List<ScheduleRound>> {
            observeScheduleCalls++
            return MutableStateFlow(emptyList())
        }

        override suspend fun generateSchedule(seasonId: Long): ScheduleGenerationResult {
            generateCalls++
            return ScheduleGenerationResult(0, 0)
        }
    }

    private class FakeResultsRepository : ResultsRepository {
        override fun observeStandings(seasonId: Long): Flow<List<Standing>> = MutableStateFlow(emptyList())
        override fun observePodium(seasonId: Long): Flow<PodiumSummary?> = MutableStateFlow(null)
        override suspend fun saveResult(
            matchId: Long,
            homeScore: Int,
            awayScore: Int,
            penaltiesHome: Int?,
            penaltiesAway: Int?,
            winnerClubId: Long?,
            goals: List<GoalRecord>,
            homeYellowCards: Int,
            homeRedCards: Int,
            awayYellowCards: Int,
            awayRedCards: Int
        ) = Unit

        override suspend fun rebuildStandings(seasonId: Long) = Unit
    }
}
