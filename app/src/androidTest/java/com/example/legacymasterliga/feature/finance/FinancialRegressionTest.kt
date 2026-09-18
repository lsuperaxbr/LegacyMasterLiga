package com.example.legacymasterliga.feature.finance

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.entity.*
import com.example.legacymasterliga.core.model.*
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.closure.data.RoomSeasonClosureRepository
import com.example.legacymasterliga.feature.closure.domain.CloseSeasonRequest
import com.example.legacymasterliga.feature.finance.data.RoomFinanceRepository
import com.example.legacymasterliga.feature.news.domain.NewsEvent
import com.example.legacymasterliga.feature.news.domain.NewsEventPublisher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinancialRegressionTest {
    private lateinit var database: AppDatabase
    private lateinit var financeRepository: RoomFinanceRepository
    private lateinit var closureRepository: RoomSeasonClosureRepository
    
    private var leagueId: Long = 0
    private var bankId: Long = 0
    private var clubAId: Long = 0
    private var clubBId: Long = 0
    
    private val adminUser = User(1, "admin", "Admin", UserRole.ADMINISTRATOR, AccountStatus.ACTIVE)

    private val auditLogger = object : AuditLogger {
        override suspend fun log(category: String, action: String, entityType: String, entityId: Long?, leagueId: Long?, summary: String, details: String?) {}
    }

    private val newsPublisher = object : NewsEventPublisher {
        override suspend fun publish(event: NewsEvent) {}
        override suspend fun remove(dedupKey: String) {}
    }

    @Before
    fun setup() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        financeRepository = RoomFinanceRepository(
            database,
            database.clubDao(),
            database.financialDao(),
            database.transferDao(),
            newsPublisher,
            auditLogger
        )

        closureRepository = RoomSeasonClosureRepository(
            database,
            database.seasonDao(),
            database.competitionDao(),
            database.competitionParticipantDao(),
            database.matchDao(),
            database.standingDao(),
            database.clubDao(),
            database.financialDao(),
            database.seasonClosureDao(),
            database.prizeDao(),
            newsPublisher,
            auditLogger
        )

        leagueId = database.leagueDao().insert(LeagueEntity(name = "Liga Teste"))
        bankId = database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Banco da Liga", isBank = true))
        clubAId = database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Clube A"))
        clubBId = database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Clube B"))

        database.financialDao().insert(FinancialTransactionEntity(clubId = bankId, amountCr = 10000, description = "Carga inicial do sistema", type = "SYSTEM_LOAD"))
        
        financeRepository.adjustBalance(adminUser, clubAId, 1000L, "Capital Inicial A")
        financeRepository.adjustBalance(adminUser, clubBId, 1000L, "Capital Inicial B")
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun cr_conservation_sum_is_always_invariant() = runBlocking {
        val initialTotal = calculateLeagueTotalCr()
        assertEquals(10000L, initialTotal)

        financeRepository.registerTransfer(adminUser, leagueId, "Jogador 1", clubAId, clubBId, 200L)
        assertEquals("Total CR deve ser invariante após transferência", initialTotal, calculateLeagueTotalCr())

        financeRepository.adjustBalance(adminUser, clubAId, 500L, "Ajuste Positivo")
        assertEquals("Total CR deve ser invariante após ajuste positivo", initialTotal, calculateLeagueTotalCr())

        financeRepository.adjustBalance(adminUser, clubBId, -300L, "Ajuste Negativo")
        assertEquals("Total CR deve ser invariante após ajuste negativo", initialTotal, calculateLeagueTotalCr())
    }

    @Test
    fun club_to_club_transfer_integrity() = runBlocking {
        val amount = 100L
        val balanceA = database.financialDao().balanceOf(clubAId)
        val balanceB = database.financialDao().balanceOf(clubBId)

        financeRepository.registerTransfer(adminUser, leagueId, "Craque", clubAId, clubBId, amount)

        assertEquals(balanceB - amount, database.financialDao().balanceOf(clubBId))
        assertEquals(balanceA + amount, database.financialDao().balanceOf(clubAId))

        val transactions = database.financialDao().observeStatement(leagueId, null).first()
        val transferEntries = transactions.filter { it.transferId != null }
        assertEquals("Deve gerar exatamente 2 lançamentos financeiros para a transferência", 2, transferEntries.size)
        
        val debit = transferEntries.find { it.clubId == clubBId }!!
        val credit = transferEntries.find { it.clubId == clubAId }!!
        
        assertEquals(-amount, debit.amountCr)
        assertEquals(amount, credit.amountCr)

        val transfers = database.transferDao().observeByLeague(leagueId).first()
        assertEquals(1, transfers.size)
        assertEquals("Craque", transfers[0].playerName)
    }

    @Test
    fun prizes_generate_correct_flow_from_bank_to_club() = runBlocking {
        val compId = database.competitionDao().insert(CompetitionEntity(leagueId = leagueId, name = "Copa", type = CompetitionType.LEAGUE, format = CompetitionFormat.HOME_AND_AWAY, status = CompetitionStatus.ACTIVE))
        database.prizeDao().upsertConfiguration(CompetitionPrizeEntity(compId, championPrizeCr = 500, runnerUpPrizeCr = 200, participationPrizeCr = 50))
        
        val seasonId = database.seasonDao().insert(SeasonEntity(competitionId = compId, number = 1, name = "T1", status = SeasonStatus.ACTIVE))
        database.competitionParticipantDao().insertAll(listOf(
            CompetitionParticipantEntity(seasonId = seasonId, clubId = clubAId, isActive = true),
            CompetitionParticipantEntity(seasonId = seasonId, clubId = clubBId, isActive = true)
        ))
        
        val roundId = database.roundDao().insertAll(listOf(RoundEntity(seasonId = seasonId, number = 1, name = "R1"))).first()
        database.matchDao().insertAll(listOf(MatchEntity(seasonId = seasonId, roundId = roundId, homeClubId = clubAId, awayClubId = clubBId, pairingKey = "K", leg = 1, homeScore = 2, awayScore = 0, status = MatchStatus.FINISHED)))
        
        database.standingDao().upsertAll(listOf(
            StandingEntity(seasonId = seasonId, clubId = clubAId, played = 1, wins = 1, points = 3),
            StandingEntity(seasonId = seasonId, clubId = clubBId, played = 1, losses = 1, points = 0)
        ))

        val bankBefore = database.financialDao().balanceOf(bankId)
        val clubABefore = database.financialDao().balanceOf(clubAId)

        closureRepository.close(adminUser, CloseSeasonRequest(seasonId = seasonId, finishCompetition = true))

        assertEquals(clubABefore + 550, database.financialDao().balanceOf(clubAId))
        assertEquals(bankBefore - 800, database.financialDao().balanceOf(bankId))

        val history = database.prizeDao().observeHistoryByLeague(leagueId).first()
        assertEquals(4, history.size) 
    }

    @Test
    fun insufficient_balance_prevents_partial_records() = runBlocking {
        val balanceB = database.financialDao().balanceOf(clubBId)
        val initialTransactions = database.financialDao().observeTransactionCount().first()
        
        val result = runCatching {
            financeRepository.registerTransfer(adminUser, leagueId, "Caro", clubAId, clubBId, balanceB + 1L)
        }

        assertTrue("Transferência deve falhar por saldo insuficiente", result.isFailure)
        assertEquals("Saldo do comprador não deve mudar", balanceB, database.financialDao().balanceOf(clubBId))
        assertEquals("Nenhum registro financeiro deve ser criado", initialTransactions, database.financialDao().observeTransactionCount().first())
    }

    @Test
    fun atomic_rollback_on_database_error() = runBlocking {
        val initialTotal = calculateLeagueTotalCr()
        val initialCount = database.financialDao().observeTransactionCount().first()

        val failingFinancialDao = object : FinancialDao by database.financialDao() {
            override suspend fun insert(transaction: FinancialTransactionEntity): Long {
                if (transaction.description == "Rollback Force") throw RuntimeException("Simulated crash")
                return database.financialDao().insert(transaction)
            }
        }

        val failingRepo = RoomFinanceRepository(
            database, database.clubDao(), failingFinancialDao, database.transferDao(), newsPublisher, auditLogger
        )

        runCatching { failingRepo.adjustBalance(adminUser, clubAId, 100L, "Rollback Force") }

        assertEquals("Contagem de transações não deve mudar após rollback", initialCount, database.financialDao().observeTransactionCount().first())
        assertEquals("Total CR da liga deve permanecer íntegro", initialTotal, calculateLeagueTotalCr())
    }

    @Test
    fun statement_sum_matches_system_balance() = runBlocking {
        financeRepository.adjustBalance(adminUser, clubAId, 300L, "Bonus")
        financeRepository.registerTransfer(adminUser, leagueId, "P1", clubBId, clubAId, 150L)
        financeRepository.adjustBalance(adminUser, clubAId, -50L, "Taxa")

        val reported = database.financialDao().balanceOf(clubAId)
        val calculated = database.financialDao().observeStatement(leagueId, clubAId).first().sumOf { it.amountCr }
        
        assertEquals("O saldo reportado deve ser a soma exata dos lançamentos do extrato", calculated, reported)
    }

    @Test
    fun no_cr_creation_without_counterparty_in_adjustments() = runBlocking {
        financeRepository.adjustBalance(adminUser, clubAId, 400L, "Ajuste")
        
        val lastTransactions = database.financialDao().observeStatement(leagueId, null).first().take(2)
        assertEquals(2, lastTransactions.size)
        
        val entry1 = lastTransactions[0]
        val entry2 = lastTransactions[1]
        
        assertEquals("Lançamentos de ajuste devem ser espelhados", -entry1.amountCr, entry2.amountCr)
        assertEquals("Lançamentos de ajuste devem ter contrapartes definidas", entry1.clubId, entry2.counterpartyClubId)
        assertEquals("Lançamentos de ajuste devem ter contrapartes definidas", entry2.clubId, entry1.counterpartyClubId)
    }

    private suspend fun calculateLeagueTotalCr(): Long {
        return database.financialDao().observeClubBalances(leagueId).first().sumOf { it.balanceCr }
    }
}
