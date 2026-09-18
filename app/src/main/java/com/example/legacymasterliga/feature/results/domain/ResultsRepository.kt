package com.example.legacymasterliga.feature.results.domain

import kotlinx.coroutines.flow.Flow

interface ResultsRepository {
    fun observeStandings(seasonId: Long): Flow<List<Standing>>
    suspend fun saveResult(
        matchId: Long, 
        homeScore: Int, 
        awayScore: Int, 
        penaltiesHome: Int? = null, 
        penaltiesAway: Int? = null,
        winnerClubId: Long? = null,
        goals: List<GoalRecord> = emptyList(),
        homeYellowCards: Int = 0,
        homeRedCards: Int = 0,
        awayYellowCards: Int = 0,
        awayRedCards: Int = 0
    )
    suspend fun rebuildStandings(seasonId: Long)
    fun observePodium(seasonId: Long): Flow<PodiumSummary?>
}

data class GoalRecord(
    val playerId: Long,
    val scoringClubId: Long,
    val isOwnGoal: Boolean
)

data class PodiumSummary(
    val championName: String?,
    val championShield: String?,
    val runnerUpName: String?,
    val runnerUpShield: String?,
    val semifinalists: List<String> = emptyList()
)
