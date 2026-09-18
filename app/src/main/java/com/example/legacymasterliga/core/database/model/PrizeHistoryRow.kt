package com.example.legacymasterliga.core.database.model

data class PrizeHistoryRow(
    val id: Long,
    val leagueId: Long,
    val competitionId: Long,
    val competitionName: String,
    val seasonId: Long,
    val seasonName: String,
    val clubId: Long,
    val clubName: String,
    val prizeType: String,
    val amountCr: Long,
    val description: String,
    val awardedAt: Long,
)
