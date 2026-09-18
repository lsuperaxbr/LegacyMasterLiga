package com.example.legacymasterliga.domain.model

data class BootstrapResult(
    val administratorCreated: Boolean,
    val leagueCreated: Boolean,
    val leagueBankCreated: Boolean,
    val administratorId: Long,
    val leagueId: Long,
    val leagueBankId: Long,
)
