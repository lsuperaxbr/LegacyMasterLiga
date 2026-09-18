package com.example.legacymasterliga.core.database.model

data class ClubSeasonHistoryRow(
    val seasonId: Long,
    val seasonName: String,
    val competitionName: String,
    val pointsForWin: Int,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val position: Int,
)
