package com.example.legacymasterliga.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.database.entity.OnlineSyncQueueEntity
import com.example.legacymasterliga.core.database.entity.OnlineSyncRecordEntity
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnlineSyncDaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() = database.close()

    @Test
    fun `only sports from online leagues become sync candidates`() = runBlocking {
        val onlineId = database.leagueDao().insert(LeagueEntity(name = "Online", cloudLeagueId = "cloud-1", isOnline = true))
        val offlineId = database.leagueDao().insert(LeagueEntity(name = "Offline"))
        database.competitionDao().insert(CompetitionEntity(leagueId = onlineId, name = "Copa", type = CompetitionType.CUP, format = CompetitionFormat.KNOCKOUT))
        database.competitionDao().insert(CompetitionEntity(leagueId = offlineId, name = "Liga", type = CompetitionType.LEAGUE, format = CompetitionFormat.SINGLE_ROUND))

        val candidates = database.onlineSyncDao().observeCandidates().first()

        assertEquals(1, candidates.size)
        assertEquals("COMPETITION", candidates.single().entityType)
        assertEquals("cloud-1", candidates.single().cloudLeagueId)
    }

    @Test
    fun `global mapping is stable and offline operation stays queued`() = runBlocking {
        val dao = database.onlineSyncDao()
        val record = OnlineSyncRecordEntity("MATCH", 10, "league", UUID.randomUUID().toString(), revision = 2)
        dao.insertRecord(record)
        dao.insertRecord(record.copy(cloudId = UUID.randomUUID().toString()))
        dao.enqueue(OnlineSyncQueueEntity("op-1", "MATCH", 10, "league", baseRevision = 2))

        assertEquals(record.cloudId, dao.findRecord("MATCH", 10)?.cloudId)
        assertNotNull(dao.findPending("MATCH", 10))
        assertEquals(1, dao.observePendingCount().first())
    }
}
