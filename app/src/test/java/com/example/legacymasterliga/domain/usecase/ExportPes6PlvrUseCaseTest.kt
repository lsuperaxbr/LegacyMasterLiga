package com.example.legacymasterliga.domain.usecase

import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.database.model.ClubProfileHeaderRow
import com.example.legacymasterliga.core.database.model.PlayerWithClub
import com.example.legacymasterliga.core.model.MarketStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportPes6PlvrUseCaseTest {

    private lateinit var useCase: ExportPes6PlvrUseCase

    @Before
    fun setUp() {
        val fakeClubDao = object : ClubDao {
            override fun observeById(id: Long): Flow<ClubEntity?> = flowOf(null)
            override fun observeProfileHeader(clubId: Long): Flow<ClubProfileHeaderRow?> = flowOf(null)
            override fun observeActiveByLeague(leagueId: Long): Flow<List<ClubEntity>> = flowOf(emptyList())
            override fun observeManagedByLeague(leagueId: Long): Flow<List<ClubEntity>> = flowOf(emptyList())
            override suspend fun findBankByLeague(leagueId: Long): ClubEntity? = null
            override suspend fun findAllByLeague(leagueId: Long): List<ClubEntity> = emptyList()
            override fun observeByPresident(userId: Long): Flow<ClubEntity?> = flowOf(null)
            override fun observeAllByPresident(userId: Long): Flow<List<ClubEntity>> = flowOf(emptyList())
            override suspend fun setActive(clubId: Long, isActive: Boolean, updatedAt: Long): Int = 0
            override suspend fun clearPresidentAssignments(userId: Long, updatedAt: Long) {}
            override suspend fun assignPresident(clubId: Long, userId: Long?, updatedAt: Long): Int = 0
            override fun observeActiveClubCount(): Flow<Int> = flowOf(0)
            override suspend fun countCrestUsages(uri: String): Int = 0
            override suspend fun deleteById(clubId: Long): Int = 0
            override suspend fun isUsedInCompetitions(clubId: Long): Boolean = false
            override suspend fun hasFinancialHistory(clubId: Long): Boolean = false
            override suspend fun findByLeagueAndName(leagueId: Long, name: String): ClubEntity? = null
            override suspend fun findById(id: Long): ClubEntity? = null
            override suspend fun insert(club: ClubEntity): Long = 0L
            override suspend fun update(club: ClubEntity) {}
        }

        val fakePlayerDao = object : PlayerDao {
            override suspend fun insert(player: PlayerEntity): Long = 0L
            override suspend fun upsert(player: PlayerEntity): Long = 0L
            override suspend fun update(player: PlayerEntity) {}
            override suspend fun findByIdWithClub(id: Long): PlayerWithClub? = null
            override suspend fun findById(id: Long): PlayerEntity? = null
            override suspend fun findActiveByClub(clubId: Long): List<PlayerEntity> = emptyList()
            override suspend fun findByNameAndClub(clubId: Long, name: String): PlayerEntity? = null
            override suspend fun findAllByNameAndClub(clubId: Long, name: String): List<PlayerEntity> = emptyList()
            override fun observeByClub(clubId: Long): Flow<List<PlayerWithClub>> = flowOf(emptyList())
            override fun observeMarket(leagueId: Long, marketStatus: MarketStatus?, clubId: Long?, query: String?): Flow<List<PlayerWithClub>> = flowOf(emptyList())
            override suspend fun updateMarketStatus(playerId: Long, status: MarketStatus, price: Long?, now: Long) {}
            override suspend fun updateClubPlayersMarket(clubId: Long, status: MarketStatus, price: Long?, now: Long) {}
            override suspend fun deleteById(playerId: Long) {}
        }

        useCase = ExportPes6PlvrUseCase(fakeClubDao, fakePlayerDao)
    }

    @Test
    fun `sanitizeName removes accents and special characters and converts spaces to underscores`() {
        assertEquals("Sao_Paulo", useCase.sanitizeName("São Paulo"))
        assertEquals("Manchester_United", useCase.sanitizeName("Manchester United"))
        assertEquals("Cristiano_Ronaldo", useCase.sanitizeName("Cristiano Ronaldo!"))
        assertEquals("Banco_da_Liga", useCase.sanitizeName("Banco da Liga"))
    }

    @Test
    fun `buildPlayerTxtContent produces valid PLVR format without PLAYER block header`() {
        val player = PlayerEntity(
            id = 1L,
            leagueId = 1L,
            clubId = 1L,
            name = "Cristiano Ronaldo",
            position = "ATA",
            overall = 91,
            heightCm = 187,
            preferredFoot = "Right",
            attributesRaw = "92,42,86,83,87,86,88,94,88,86,81,80,78,76,91,90,88,85,82,94,95,82,89,91,85,50"
        )

        val txt = useCase.buildPlayerTxtContent(player)

        // Confirmar que NAO contem o bloco [PLAYER]
        assertFalse("O arquivo nao deve conter [PLAYER]", txt.contains("[PLAYER]"))

        // Confirmar campos obrigatorios e 26 atributos
        assertTrue(txt.contains("Name: Cristiano Ronaldo"))
        assertTrue(txt.contains("Position: ATA"))
        assertTrue(txt.contains("OVR: 91"))
        assertTrue(txt.contains("Foot: R"))
        assertTrue(txt.contains("Height: 187"))
        assertTrue(txt.contains("Attack: 92"))
        assertTrue(txt.contains("Defence: 42"))
        assertTrue(txt.contains("GK_Skills: 50"))
    }

    @Test
    fun `buildPlayerTxtContent preserves Height and Foot keys when null or blank`() {
        val playerWithoutHeightAndFoot = PlayerEntity(
            id = 2L,
            leagueId = 1L,
            clubId = 1L,
            name = "Jogador Vazio",
            position = "MC",
            overall = 75,
            heightCm = null,
            preferredFoot = null
        )

        val txt = useCase.buildPlayerTxtContent(playerWithoutHeightAndFoot)

        // As chaves Height e Foot devem estar presentes, mas com o valor em branco apos os dois-pontos
        assertTrue(txt.contains("Foot:\n"))
        assertTrue(txt.contains("Height:\n"))
    }
}
