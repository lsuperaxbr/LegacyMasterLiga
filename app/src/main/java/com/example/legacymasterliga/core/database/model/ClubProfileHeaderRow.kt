package com.example.legacymasterliga.core.database.model

data class ClubProfileHeaderRow(
    val clubId: Long,
    val leagueId: Long,
    val clubName: String,
    val crestUri: String?,
    val presidentUserId: Long?,
    val presidentName: String?,
    val isActive: Boolean,
)
