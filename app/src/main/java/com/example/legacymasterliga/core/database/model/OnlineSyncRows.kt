package com.example.legacymasterliga.core.database.model

data class OnlineSyncCandidate(
    val entityType: String,
    val localId: Long,
    val cloudLeagueId: String,
    val updatedAt: Long,
)
