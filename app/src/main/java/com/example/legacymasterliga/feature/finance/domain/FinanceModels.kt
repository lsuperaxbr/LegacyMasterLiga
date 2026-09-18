package com.example.legacymasterliga.feature.finance.domain

data class ClubBalance(val clubId: Long, val clubName: String, val isBank: Boolean, val balanceCr: Long)

data class StatementEntry(
    val id: Long,
    val clubId: Long,
    val clubName: String,
    val amountCr: Long,
    val description: String,
    val type: String,
    val counterpartyName: String?,
    val transferId: Long?,
    val createdAt: Long,
)

data class MarketTransfer(
    val id: Long,
    val playerName: String,
    val originClubId: Long,
    val originClubName: String,
    val destinationClubId: Long,
    val destinationClubName: String,
    val valueCr: Long,
    val type: String,
    val swapId: String?,
    val note: String?,
    val createdAt: Long,
)
