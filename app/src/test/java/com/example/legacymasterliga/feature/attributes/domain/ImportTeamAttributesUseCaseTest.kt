package com.example.legacymasterliga.feature.attributes.domain

import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.database.model.PlayerWithClub
import com.example.legacymasterliga.core.model.MarketStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportTeamAttributesUseCaseTest {

    private val playerDatabase = mutableMapOf<Pair<Long, String>, PlayerEntity>()
    private lateinit var useCase: ImportTeamAttributesUseCase

    @Before
    fun setUp() {
        playerDatabase.clear()

        // Seed 2 jogadores no clube 10L
        playerDatabase[10L to "Bruno Fernandes"] = PlayerEntity(
            id = 1L,
            leagueId = 1L,
            clubId = 10L,
            name = "Bruno Fernandes",
            position = "MC",
            overall = 80,
            heightCm = 179,
            preferredFoot = "Right",
            attributesRaw = "80,60,70,85,75,75,80,75,80,75,85,85,85,85,80,80,80,80,80,60,70,80,80,75,80,50"
        )

        playerDatabase[10L to "Marcus Rashford"] = PlayerEntity(
            id = 2L,
            leagueId = 1L,
            clubId = 10L,
            name = "Marcus Rashford",
            position = "PE",
            overall = 82,
            heightCm = 185,
            preferredFoot = "Right",
            attributesRaw = "82,50,78,82,90,90,82,85,84,88,78,78,75,75,82,86,82,75,75,72,78,75,82,80,80,50"
        )

        val fakePlayerDao = object : PlayerDao {
            override suspend fun findByNameAndClub(clubId: Long, name: String): PlayerEntity? {
                return playerDatabase.entries.find { 
                    it.key.first == clubId && it.key.second.equals(name, ignoreCase = true) 
                }?.value
            }

            override suspend fun update(player: PlayerEntity) {
                playerDatabase[player.clubId to player.name] = player
            }

            override suspend fun insert(player: PlayerEntity): Long = 0L
            override suspend fun upsert(player: PlayerEntity): Long = 0L
            override suspend fun findByIdWithClub(id: Long): PlayerWithClub? = null
            override suspend fun findById(id: Long): PlayerEntity? = null
            override suspend fun findActiveByClub(clubId: Long): List<PlayerEntity> = emptyList()
            override suspend fun findAllByNameAndClub(clubId: Long, name: String): List<PlayerEntity> = emptyList()
            override fun observeByClub(clubId: Long): Flow<List<PlayerWithClub>> = flowOf(emptyList())
            override fun observeMarket(leagueId: Long, marketStatus: MarketStatus?, clubId: Long?, query: String?): Flow<List<PlayerWithClub>> = flowOf(emptyList())
            override suspend fun updateMarketStatus(playerId: Long, status: MarketStatus, price: Long?, now: Long) {}
            override suspend fun updateClubPlayersMarket(clubId: Long, status: MarketStatus, price: Long?, now: Long) {}
            override suspend fun deleteById(playerId: Long) {}
        }

        useCase = ImportTeamAttributesUseCase(fakePlayerDao)
    }

    @Test
    fun `importation with 2 valid players updates both successfully`() = runBlocking {
        val txtContent = """
            Name: Bruno Fernandes
            Position: MEI
            OVR: 86
            Attack: 84
            Defence: 68

            Name: Marcus Rashford
            Position: ATA
            OVR: 84
            Attack: 85
            Speed: 92
        """.trimIndent()

        val result = useCase(10L, txtContent)

        assertEquals(2, result.totalParsed)
        assertEquals(2, result.totalUpdated)
        assertTrue(result.unmappedPlayerNames.isEmpty())

        val updatedBruno = playerDatabase[10L to "Bruno Fernandes"]!!
        assertEquals("MEI", updatedBruno.position)
        assertEquals(86, updatedBruno.overall)

        val updatedRashford = playerDatabase[10L to "Marcus Rashford"]!!
        assertEquals("ATA", updatedRashford.position)
        assertEquals(84, updatedRashford.overall)
    }

    @Test
    fun `importation with unmapped player records error and updates valid player`() = runBlocking {
        val txtContent = """
            Name: Bruno Fernandes
            OVR: 88

            Name: Cristiano Ronaldo
            OVR: 91
        """.trimIndent()

        val result = useCase(10L, txtContent)

        assertEquals(2, result.totalParsed)
        assertEquals(1, result.totalUpdated)
        assertEquals(listOf("Cristiano Ronaldo"), result.unmappedPlayerNames)
    }

    @Test
    fun `importation with blank Foot and Height saves them as empty or null`() = runBlocking {
        val txtContent = """
            Name: Bruno Fernandes
            Foot:
            Height:
            OVR: 87
        """.trimIndent()

        val result = useCase(10L, txtContent)

        assertEquals(1, result.totalUpdated)
        val updated = playerDatabase[10L to "Bruno Fernandes"]!!
        assertEquals("", updated.preferredFoot)
        assertEquals(null, updated.heightCm)
    }

    @Test
    fun `importation with unknown keys ignores unknown keys and updates player attributes`() = runBlocking {
        val txtContent = """
            Name: Bruno Fernandes
            Unknown_Custom_Key: 999
            Favorite_Color: Blue
            OVR: 89
            Attack: 90
        """.trimIndent()

        val result = useCase(10L, txtContent)

        assertEquals(1, result.totalUpdated)
        val updated = playerDatabase[10L to "Bruno Fernandes"]!!
        assertEquals(89, updated.overall)
    }
}
