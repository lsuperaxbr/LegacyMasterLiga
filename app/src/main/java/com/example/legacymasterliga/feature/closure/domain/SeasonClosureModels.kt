package com.example.legacymasterliga.feature.closure.domain

data class ClosurePreview(
    val championClubName: String,
    val runnerUpClubName: String?,
    val championPoints: Int,
    val totalMatches: Int,
    val finishedMatches: Int,
    val totalDisciplinaryFineCr: Long = 0,
)

data class PrizeConfiguration(
    val competitionId: Long,
    val championPrizeCr: Long = 0,
    val runnerUpPrizeCr: Long = 0,
    val participationPrizeCr: Long = 0,
)

data class PrizeHistoryItem(
    val id: Long,
    val competitionName: String,
    val seasonName: String,
    val clubName: String,
    val prizeType: String,
    val amountCr: Long,
    val description: String,
    val awardedAt: Long,
)

data class CloseSeasonRequest(
    val seasonId: Long,
    val finishCompetition: Boolean,
)
