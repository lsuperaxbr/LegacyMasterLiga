package com.example.legacymasterliga.core.database.model

data class DashboardContextRow(
    val leagueId: Long,
    val leagueName: String,
    val competitionId: Long?,
    val competitionName: String?,
    val seasonId: Long?,
    val seasonName: String?,
)

data class DashboardLeaderRow(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val points: Int,
    val played: Int,
)

data class DashboardRoundRow(
    val seasonId: Long,
    val roundId: Long,
    val roundNumber: Int,
    val roundName: String,
    val pendingMatches: Int,
    val totalMatches: Int,
)

data class DashboardResultRow(
    val matchId: Long,
    val roundNumber: Int,
    val homeClubName: String,
    val awayClubName: String,
    val homeScore: Int,
    val awayScore: Int,
)

data class DashboardTransferRow(
    val transferId: Long,
    val playerName: String,
    val originClubName: String,
    val destinationClubName: String,
    val valueCr: Long,
    val createdAt: Long,
)

data class DashboardNewsRow(
    val newsId: Long,
    val title: String,
    val category: String,
    val publishedAt: Long,
)

data class DashboardClubBalanceRow(
    val clubId: Long,
    val clubName: String,
    val balanceCr: Long,
)
