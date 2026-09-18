package com.example.legacymasterliga.feature.halloffame.domain

data class ClubRecord(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val value: Long,
    val detail: String,
    val seasonId: Long? = null,
)

data class BiggestWinRecord(
    val matchId: Long,
    val seasonId: Long,
    val competitionName: String,
    val seasonName: String,
    val roundNumber: Int,
    val winnerClubId: Long,
    val winnerClubName: String,
    val loserClubId: Long,
    val loserClubName: String,
    val score: String,
    val goalDifference: Int,
)

data class UnbeatenStreakRecord(
    val clubId: Long,
    val clubName: String,
    val crestUri: String?,
    val matches: Int,
    val competitionName: String,
    val seasonId: Long,
    val seasonName: String,
)

data class HallOfFameSnapshot(
    val topChampion: ClubRecord? = null,
    val mostWins: ClubRecord? = null,
    val bestAttack: ClubRecord? = null,
    val bestDefense: ClubRecord? = null,
    val biggestWin: BiggestWinRecord? = null,
    val longestUnbeaten: UnbeatenStreakRecord? = null,
    val mostCrMovement: ClubRecord? = null,
)
