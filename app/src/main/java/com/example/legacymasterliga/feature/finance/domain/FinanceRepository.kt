package com.example.legacymasterliga.feature.finance.domain

import com.example.legacymasterliga.domain.model.Player
import com.example.legacymasterliga.domain.model.User
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun observeBalances(leagueId: Long): Flow<List<ClubBalance>>
    fun observeStatement(leagueId: Long, clubId: Long?): Flow<List<StatementEntry>>
    fun observeTransfers(leagueId: Long): Flow<List<MarketTransfer>>
    suspend fun adjustBalance(actor: User, clubId: Long, amountCr: Long, description: String)
    suspend fun registerTransfer(
        actor: User,
        leagueId: Long,
        playerName: String,
        originClubId: Long,
        destinationClubId: Long,
        valueCr: Long,
        note: String? = null,
        type: String = "TRANSFER",
    )

    suspend fun transferPlayerAtomic(
        actor: User,
        leagueId: Long,
        player: Player,
        destinationClubId: Long,
        valueCr: Long,
        note: String?,
    )

    suspend fun registerSwap(
        actor: User,
        leagueId: Long,
        playerA: String,
        clubAId: Long,
        playerB: String,
        clubBId: Long,
        compensationCr: Long,
        payerClubId: Long?,
        note: String? = null,
    )
    suspend fun injectInitialBalance(leagueId: Long, amount: Long)
    suspend fun revertTransfer(transferId: Long): RevertResult
    suspend fun dispensePlayer(actor: User, leagueId: Long, player: Player, dispensalFeeCr: Long = 5L)
}

data class RevertResult(val moneyReverted: Boolean, val playerMoved: Boolean, val warning: String? = null)
