package com.example.legacymasterliga.core.database.model

data class ClubBalanceRow(
    val id: Long,
    val name: String,
    val isBank: Boolean,
    val balanceCr: Long,
)

data class FinancialStatementRow(
    val id: Long,
    val clubId: Long,
    val clubName: String,
    val amountCr: Long,
    val description: String,
    val type: String,
    val counterpartyClubId: Long?,
    val counterpartyName: String?,
    val transferId: Long?,
    val createdAt: Long,
)

data class TransferHistoryRow(
    val id: Long,
    val leagueId: Long,
    val playerName: String,
    val originClubId: Long,
    val originClubName: String,
    val destinationClubId: Long,
    val destinationClubName: String,
    val valueCr: Long,
    val type: String,
    val swapId: String?,
    val seasonId: Long?,
    val note: String?,
    val createdAt: Long,
)
