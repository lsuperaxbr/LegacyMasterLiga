package com.example.legacymasterliga.core.database.model

data class StandingRow(
    val standingId: Long,
    val seasonId: Long,
    val clubId: Long,
    val clubName: String,
    val shieldUri: String?,
    val groupIndex: Int?,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
)
