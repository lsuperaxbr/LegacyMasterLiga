package com.example.legacymasterliga.core.database.model

data class ChampionRecordRow(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val titles: Int,
)

data class AggregateClubRecordRow(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val value: Long,
)

data class SeasonClubRecordRow(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val competitionName: String,
    val seasonId: Long,
    val seasonName: String,
    val value: Int,
)

data class BiggestWinRow(
    val matchId: Long,
    val seasonId: Long,
    val competitionName: String,
    val seasonName: String,
    val roundNumber: Int,
    val homeClubId: Long,
    val homeClubName: String,
    val awayClubId: Long,
    val awayClubName: String,
    val homeScore: Int,
    val awayScore: Int,
    val goalDifference: Int,
)

data class FinishedMatchTimelineRow(
    val matchId: Long,
    val seasonId: Long,
    val competitionName: String,
    val seasonName: String,
    val roundNumber: Int,
    val leg: Int,
    val homeClubId: Long,
    val homeClubName: String,
    val homeCrestUri: String?,
    val awayClubId: Long,
    val awayClubName: String,
    val awayCrestUri: String?,
    val homeScore: Int,
    val awayScore: Int,
    val eventOrder: Long,
)
