package com.example.legacymasterliga.domain.model

import com.example.legacymasterliga.core.model.MarketStatus

data class Player(
    val id: Long,
    val leagueId: Long,
    val clubId: Long,
    val name: String,
    val position: String?,
    val marketStatus: MarketStatus,
    val askingPriceCr: Long?,
    val skillImageUri: String?,
    val notes: String?,
    val externalPlayerId: String?,
    val attributesRaw: String? = null,
    val overall: Int? = null,
    val clubName: String? = null
)
