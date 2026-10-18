package com.example.legacymasterliga.domain.model

data class Club(
    val id: Long,
    val leagueId: Long,
    val name: String,
    val crestUri: String?,
    val badgeConfigJson: String? = null,
    val presidentUserId: Long?,
    val isBank: Boolean,
    val isActive: Boolean,
)
