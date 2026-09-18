package com.example.legacymasterliga.feature.results

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import com.example.legacymasterliga.core.database.entity.CompetitionParticipantEntity
import com.example.legacymasterliga.core.database.entity.CompetitionPrizeEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.database.entity.SeasonEntity
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.MatchStatus
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.news.domain.NewsEvent
import com.example.legacymasterliga.feature.news.domain.NewsEventPublisher
import com.example.legacymasterliga.feature.results.data.RoomResultsRepository
import com.example.legacymasterliga.feature.schedule.data.RoomScheduleRepository
import com.example.legacymasterliga.feature.closure.data.RoomSeasonClosureRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CupFlowIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var results: RoomResultsRepository
    private lateinit var schedule: RoomScheduleRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        val closure = RoomSeasonClosureRepository(
            database, database.seasonDao(), database.competitionDao(), database.competitionParticipantDao(),
            database.matchDao(), database.standingDao(), database.clubDao(), database.financialDao(),
            database.seasonClosureDao(), database.prizeDao(), NoOpNewsPublisher, NoOpAuditLogger,
        )
        results = RoomResultsRepository(
            database, database.matchDao(), database.roundDao(), database.standingDao(),
            database.seasonDao(), database.competitionDao(), database.competitionParticipantDao(),
            database.clubDao(), NoOpNewsPublisher, NoOpAuditLogger, closure, database.goalEventDao()
        )
        schedule = RoomScheduleRepository(
            database, database.competitionDao(), database.seasonDao(), database.competitionParticipantDao(),
            database.roundDao(), database.matchDao(), results,
        )
    }

    @After fun tearDown() = database.close()

    @Test
    fun `four club single match cup creates one final and preserves semifinal results`() = runBlocking {
        val seasonId = createCup(knockoutLegs = 1)
        schedule.generateSchedule(seasonId)
        val semifinalIds = rows(seasonId).filter { it.stageLabel == "Semifinais" }.map { it.matchId }
        assertEquals(2, semifinalIds.size)

        semifinalIds.forEach { results.saveResult(it, 2, 0) }

        val afterSemis = rows(seasonId)
        val finals = afterSemis.filter { it.stageLabel == "Final" }
        assertEquals(1, finals.size)
        semifinalIds.forEach { id -> assertEquals(2, database.matchDao().findById(id)?.homeScore) }

        // Reprocessing a finished phase reuses the existing Final.
        results.saveResult(semifinalIds.first(), 2, 0)
        assertEquals(1, rows(seasonId).count { it.stageLabel == "Final" })

        results.saveResult(finals.single().matchId, 1, 0)
        val podium = results.observePodium(seasonId).first()
        assertNotNull(podium)
        assertNotNull(podium?.championName)
        assertNotNull(podium?.runnerUpName)

        assertEquals(SeasonStatus.FINISHED, database.seasonDao().findById(seasonId)?.status)
        assertEquals(1, database.seasonClosureDao().countBySeason(seasonId))
    }

    @Test
    fun `four club two leg cup creates exactly two final legs and crowns champion`() = runBlocking {
        val seasonId = createCup(knockoutLegs = 2)
        schedule.generateSchedule(seasonId)
        val semifinals = rows(seasonId).filter { it.stageLabel == "Semifinais" }
        assertEquals(4, semifinals.size)

        semifinals.filter { it.leg == 1 }.forEach { results.saveResult(it.matchId, 1, 0) }
        semifinals.filter { it.leg == 2 }.forEach { results.saveResult(it.matchId, 0, 0) }

        val finals = rows(seasonId).filter { it.stageLabel == "Final" }
        assertEquals(2, finals.size)
        assertEquals(setOf(1, 2), finals.map { it.leg }.toSet())
        results.saveResult(finals.first { it.leg == 1 }.matchId, 1, 0)
        results.saveResult(finals.first { it.leg == 2 }.matchId, 0, 0)

        val podium = results.observePodium(seasonId).first()
        assertNotNull(podium?.championName)
        assertNotNull(podium?.runnerUpName)
        assertEquals(2, rows(seasonId).count { it.stageLabel == "Final" })
        assertEquals(SeasonStatus.FINISHED, database.seasonDao().findById(seasonId)?.status)
        assertEquals(1, database.seasonClosureDao().countBySeason(seasonId))
    }

    @Test
    fun `winner selected on tied return leg advances automatically`() = runBlocking {
        val seasonId = createCup(knockoutLegs = 2)
        schedule.generateSchedule(seasonId)
        val semifinals = rows(seasonId).filter { it.stageLabel == "Semifinais" }
        semifinals.filter { it.leg == 1 }.forEach { results.saveResult(it.matchId, 1, 0) }
        semifinals.filter { it.leg == 2 }.forEach { match ->
            results.saveResult(match.matchId, 1, 0, winnerClubId = match.homeClubId)
        }

        val finals = rows(seasonId).filter { it.stageLabel == "Final" }
        assertEquals(2, finals.size)
        assertEquals(setOf(1, 2), finals.map { it.leg }.toSet())
    }

    @Test
    fun `all supported cup sizes reach one final in single and two leg formats`() = runBlocking {
        for (clubCount in listOf(2, 4, 8, 10, 16)) {
            for (knockoutLegs in listOf(1, 2)) {
                val seasonId = createCup(knockoutLegs = knockoutLegs, clubCount = clubCount)
                val firstGeneration = schedule.generateSchedule(seasonId)
                val repeatedGeneration = schedule.generateSchedule(seasonId)
                assertTrue(firstGeneration.matchesCreated > 0)
                assertEquals(0, repeatedGeneration.roundsCreated)
                assertEquals(0, repeatedGeneration.matchesCreated)

                var guard = 0
                while (database.seasonDao().findById(seasonId)?.status == SeasonStatus.ACTIVE) {
                    check(guard++ < 20) { "A Copa com $clubCount clubes e $knockoutLegs jogo(s) não chegou à Final." }
                    val pending = rows(seasonId).filter { it.matchStatus == MatchStatus.SCHEDULED }
                    assertTrue("A Copa ativa precisa possuir partidas pendentes.", pending.isNotEmpty())
                    val nextRoundNumber = pending.minOf { it.roundNumber }
                    pending.filter { it.roundNumber == nextRoundNumber }.forEach { match ->
                        if (match.leg == 1) results.saveResult(match.matchId, 2, 0)
                        else results.saveResult(match.matchId, 0, 0)
                    }
                }

                val finalMatches = rows(seasonId).filter { it.stageLabel == "Final" }
                assertEquals(knockoutLegs, finalMatches.size)
                assertEquals(1, database.seasonClosureDao().countBySeason(seasonId))
                assertArchivedPodium(seasonId)
                val rounds = database.roundDao().observeBySeason(seasonId).first()
                assertEquals(rounds.size, rounds.map { it.number }.distinct().size)

                val season = requireNotNull(database.seasonDao().findById(seasonId))
                val competition = requireNotNull(database.competitionDao().findById(season.competitionId))
                val prizeHistory = database.prizeDao().observeHistoryByLeague(competition.leagueId).first()
                assertEquals(2, prizeHistory.size)
                assertEquals(setOf("CHAMPION", "RUNNER_UP"), prizeHistory.map { it.prizeType }.toSet())
            }
        }
    }

    private suspend fun createCup(knockoutLegs: Int, clubCount: Int = 4): Long {
        val suffix = "${clubCount}c-${knockoutLegs}p-${System.nanoTime()}"
        val leagueId = database.leagueDao().insert(LeagueEntity(name = "Liga da Copa $suffix"))
        val bankId = database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Banco da Liga", isBank = true))
        database.financialDao().insert(
            FinancialTransactionEntity(
                clubId = bankId,
                amountCr = 10_000L,
                description = "Fundo de premiação",
                type = "INITIAL_BALANCE",
            ),
        )
        val clubs = (1..clubCount).map { index ->
            database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Clube $index"))
        }
        val competitionId = database.competitionDao().insert(
            CompetitionEntity(
                leagueId = leagueId, name = "Copa $suffix", type = CompetitionType.CUP,
                format = CompetitionFormat.KNOCKOUT, knockoutLegs = knockoutLegs,
            ),
        )
        database.prizeDao().upsertConfiguration(
            CompetitionPrizeEntity(competitionId = competitionId, championPrizeCr = 10L, runnerUpPrizeCr = 5L),
        )
        val seasonId = database.seasonDao().insert(
            SeasonEntity(competitionId = competitionId, number = 1, name = "Temporada 1", status = SeasonStatus.ACTIVE),
        )
        database.competitionParticipantDao().insertAll(
            clubs.mapIndexed { index, clubId -> CompetitionParticipantEntity(seasonId = seasonId, clubId = clubId, seed = index + 1) },
        )
        return seasonId
    }

    private suspend fun rows(seasonId: Long) = database.matchDao().findScheduledRowsBySeason(seasonId)

    private fun assertArchivedPodium(seasonId: Long) {
        val closure = database.openHelper.readableDatabase.query(
            "SELECT championClubId, runnerUpClubId FROM season_closures WHERE seasonId = ?",
            arrayOf(seasonId.toString()),
        ).use { cursor ->
            check(cursor.moveToFirst())
            cursor.getLong(0) to cursor.getLong(1)
        }
        val podium = database.openHelper.readableDatabase.query(
            "SELECT clubId, position FROM final_standings WHERE seasonId = ? ORDER BY position LIMIT 2",
            arrayOf(seasonId.toString()),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getLong(0) to cursor.getInt(1))
            }
        }
        assertEquals(listOf(closure.first to 1, closure.second to 2), podium)
    }

    private object NoOpNewsPublisher : NewsEventPublisher {
        override suspend fun publish(event: NewsEvent) = Unit
        override suspend fun remove(dedupKey: String) = Unit
    }

    private object NoOpAuditLogger : AuditLogger {
        override suspend fun log(
            category: String, action: String, entityType: String, entityId: Long?, leagueId: Long?, summary: String, details: String?,
        ) = Unit
    }
}
