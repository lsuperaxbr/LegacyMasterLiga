package com.example.legacymasterliga.domain.model

import com.example.legacymasterliga.core.model.LeagueStatus

data class League(
    val id: Long,
    val name: String,
    val currencyCode: String,
    val status: LeagueStatus,
    val cloudLeagueId: String? = null,
    val isOnline: Boolean = false,
)
