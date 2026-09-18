package com.example.legacymasterliga.feature.dashboard.domain

data class DashboardLeader(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val points: Int,
    val played: Int,
)

data class DashboardRound(
    val seasonId: Long,
    val roundId: Long,
    val number: Int,
    val name: String,
    val pendingMatches: Int,
    val totalMatches: Int,
)

data class DashboardResult(
    val matchId: Long,
    val roundNumber: Int,
    val homeClubName: String,
    val awayClubName: String,
    val homeScore: Int,
    val awayScore: Int,
)

data class DashboardTransfer(
    val transferId: Long,
    val playerName: String,
    val originClubName: String,
    val destinationClubName: String,
    val valueCr: Long,
    val createdAt: Long,
)

data class DashboardNews(
    val newsId: Long,
    val title: String,
    val category: String,
    val publishedAt: Long,
)

data class DashboardClubBalance(
    val clubId: Long,
    val clubName: String,
    val balanceCr: Long,
)

data class DashboardSummary(
    val leagueId: Long? = null,
    val leagueName: String = "Liga M L Amigos",
    val competitionId: Long? = null,
    val competitionName: String? = null,
    val seasonId: Long? = null,
    val seasonName: String? = null,
    val competitionCount: Int = 0,
    val clubCount: Int = 0,
    val totalCr: Long = 0,
    val transferCount: Int = 0,
    val newsCount: Int = 0,
    val leader: DashboardLeader? = null,
    val nextRound: DashboardRound? = null,
    val recentResults: List<DashboardResult> = emptyList(),
    val latestTransfers: List<DashboardTransfer> = emptyList(),
    val latestNews: List<DashboardNews> = emptyList(),
    val presidentClubBalances: List<DashboardClubBalance> = emptyList(),
)
