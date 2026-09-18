package com.example.legacymasterliga.feature.presidentprofile.domain

data class PresidentProfileSnapshot(
    val displayName: String = "",
    val username: String = "",
    val clubs: List<String> = emptyList(),
    val titles: Int = 0,
    val cupTitles: Int = 0,
    val seasonsPlayed: Int = 0,
    val played: Long = 0,
    val wins: Long = 0,
    val draws: Long = 0,
    val losses: Long = 0,
    val goalsFor: Long = 0,
    val goalsAgainst: Long = 0,
    val winRate: Double = 0.0,
    val currentBalanceCr: Long = 0,
    val marketMovementCr: Long = 0,
    val isLoading: Boolean = true,
)
