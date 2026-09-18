package com.example.legacymasterliga.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.database.entity.SessionEntity
import com.example.legacymasterliga.core.database.entity.TransferEntity
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseIntegrationTest {
    private lateinit var database: AppDatabase

    @Before fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After @Throws(IOException::class) fun closeDatabase() = database.close()

    @Test fun user_session_league_club_and_finance_work_together() = runBlocking {
        val userId = database.userDao().insert(
            UserEntity(username = "admin", displayName = "Administrador", passwordHash = "hash", passwordSalt = "salt", role = UserRole.ADMINISTRATOR, status = AccountStatus.ACTIVE),
        )
        database.sessionDao().insert(SessionEntity(userId = userId, tokenHash = "token", expiresAt = System.currentTimeMillis() + 60_000))
        val leagueId = database.leagueDao().insert(LeagueEntity(name = "Liga M L Amigos"))
        val clubId = database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Torino", presidentUserId = userId))
        database.financialDao().insert(FinancialTransactionEntity(clubId = clubId, amountCr = 500, description = "Saldo inicial", type = "INITIAL_BALANCE"))
        database.financialDao().insert(FinancialTransactionEntity(clubId = clubId, amountCr = -120, description = "Compra", type = "TRANSFER_PURCHASE"))

        assertNotNull(database.userDao().findByUsername("ADMIN"))
        assertEquals(userId, database.sessionDao().findCurrent()?.userId)
        assertEquals(380L, database.financialDao().balanceOf(clubId))
        assertEquals("Torino", database.clubDao().observeManagedByLeague(leagueId).first().single().name)
    }

    @Test fun transfer_and_financial_entries_preserve_market_history() = runBlocking {
        val leagueId = database.leagueDao().insert(LeagueEntity(name = "Liga Mercado"))
        val bankId = database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Banco da Liga", isBank = true))
        val clubId = database.clubDao().insert(ClubEntity(leagueId = leagueId, name = "Fluminense"))
        database.financialDao().insert(FinancialTransactionEntity(clubId = clubId, amountCr = 500, description = "Saldo inicial", type = "INITIAL_BALANCE"))
        val transferId = database.transferDao().insert(TransferEntity(leagueId = leagueId, playerName = "Jogador Teste", originClubId = bankId, destinationClubId = clubId, valueCr = 80))
        database.financialDao().insert(FinancialTransactionEntity(clubId = clubId, amountCr = -80, description = "Compra", type = "TRANSFER_PURCHASE", counterpartyClubId = bankId, transferId = transferId))
        database.financialDao().insert(FinancialTransactionEntity(clubId = bankId, amountCr = 80, description = "Venda", type = "TRANSFER_SALE", counterpartyClubId = clubId, transferId = transferId))

        assertEquals(420L, database.financialDao().balanceOf(clubId))
        assertEquals(80L, database.financialDao().balanceOf(bankId))
        val transfer = database.transferDao().observeByLeague(leagueId).first().single()
        assertEquals("Jogador Teste", transfer.playerName)
        assertEquals(80L, transfer.valueCr)
    }

    @Test fun foreign_keys_reject_orphan_club() = runBlocking {
        val failure = runCatching { database.clubDao().insert(ClubEntity(leagueId = 999, name = "Órfão")) }
        assertTrue(failure.isFailure)
    }
}
