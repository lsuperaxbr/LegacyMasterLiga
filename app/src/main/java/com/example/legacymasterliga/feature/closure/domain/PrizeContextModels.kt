package com.example.legacymasterliga.feature.closure.domain

data class PrizeCompetitionOption(
    val id: Long,
    val name: String
)

data class PrizeSeasonOption(
    val id: Long,
    val competitionId: Long,
    val name: String
)

data class PrizeClubOption(
    val id: Long,
    val name: String
)
