package com.example.legacymasterliga.core.database.model

data class ClubRecentMatchRow(
    val matchId: Long,
    val seasonId: Long,
    val roundNumber: Int,
    val homeClubId: Long,
    val homeClubName: String,
    val awayClubId: Long,
    val awayClubName: String,
    val homeScore: Int,
    val awayScore: Int,
)
