package com.example.legacymasterliga.core.database.model

import com.example.legacymasterliga.core.model.MatchStatus
import com.example.legacymasterliga.core.model.RoundStatus

data class ScheduledMatchRow(
    val matchId: Long,
    val roundId: Long,
    val roundNumber: Int,
    val roundName: String,
    val roundStatus: RoundStatus,
    val stage: String,
    val stageLabel: String?,
    val homeClubId: Long,
    val homeClubName: String,
    val homeShieldUri: String?,
    val awayClubId: Long,
    val awayClubName: String,
    val awayShieldUri: String?,
    val leg: Int,
    val homeScore: Int?,
    val awayScore: Int?,
    val matchStatus: MatchStatus,
    val penaltiesHome: Int? = null,
    val penaltiesAway: Int? = null,
    val winnerClubId: Long? = null,
)
