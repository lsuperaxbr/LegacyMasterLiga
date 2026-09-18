package com.example.legacymasterliga.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.database.entity.RoundEntity
import com.example.legacymasterliga.core.database.entity.SeasonEntity
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoundDaoRegressionTest {
    private lateinit var database: AppDatabase
    private var seasonId: Long = 0

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        val leagueId = database.leagueDao().insert(LeagueEntity(name = "Liga teste"))
        val competitionId = database.competitionDao().insert(
            CompetitionEntity(
                leagueId = leagueId,
                name = "Copa teste",
                type = CompetitionType.CUP,
                format = CompetitionFormat.KNOCKOUT,
            ),
        )
        seasonId = database.seasonDao().insert(
            SeasonEntity(competitionId = competitionId, number = 1, name = "Temporada 1"),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `legacy return round is recovered by exact name`() = runBlocking {
        val dao = database.roundDao()
        dao.insertAll(
            listOf(
                RoundEntity(seasonId = seasonId, number = 1, name = "Final", stageLabel = "Final"),
                // Shape written by the old bug: the return leg reused the first-leg label.
                RoundEntity(seasonId = seasonId, number = 2, name = "Final - Volta", stageLabel = "Final"),
            ),
        )

        val recovered = dao.findBySeasonAndName(seasonId, "Final - Volta")

        assertNotNull(recovered)
        assertEquals(2, recovered?.number)
        assertEquals(2, dao.countBySeason(seasonId))
    }

    @Test
    fun `conflicting round number is ignored without changing existing data`() = runBlocking {
        val dao = database.roundDao()
        dao.insertAll(listOf(RoundEntity(seasonId = seasonId, number = 1, name = "Semifinais")))

        val result = dao.insertIfAbsent(
            RoundEntity(seasonId = seasonId, number = 1, name = "Final"),
        )

        assertEquals(-1L, result)
        assertEquals("Semifinais", dao.findBySeasonAndNumber(seasonId, 1)?.name)
        assertEquals(1, dao.countBySeason(seasonId))
    }
}
