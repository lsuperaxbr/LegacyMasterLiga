package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.legacymasterliga.core.database.entity.GoalEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: GoalEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<GoalEventEntity>)

    @Query("SELECT * FROM goal_events WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): GoalEventEntity?

    @Query("DELETE FROM goal_events WHERE matchId = :matchId")
    suspend fun deleteByMatch(matchId: Long)

    @Query("SELECT * FROM goal_events WHERE matchId = :matchId")
    suspend fun findByMatch(matchId: Long): List<GoalEventEntity>

    @Query("SELECT COUNT(*) FROM goal_events WHERE playerId = :playerId AND isOwnGoal = 0")
    fun observeGoalsByPlayer(playerId: Long): Flow<Int>

    @Query("""
        SELECT p.id AS playerId, p.name AS playerName, c.name AS clubName, c.id AS clubId,
               COUNT(*) AS goals
        FROM goal_events ge
        JOIN players p ON p.id = ge.playerId
        JOIN clubs c ON c.id = p.clubId
        JOIN matches m ON m.id = ge.matchId
        JOIN rounds r ON r.id = m.roundId
        JOIN seasons s ON s.id = r.seasonId
        WHERE ge.isOwnGoal = 0 AND s.competitionId = :competitionId
        GROUP BY p.id
        ORDER BY goals DESC
        LIMIT 50
    """)
    fun observeTopScorers(competitionId: Long): Flow<List<TopScorerRow>>
}

data class TopScorerRow(
    val playerId: Long,
    val playerName: String,
    val clubId: Long,
    val clubName: String,
    val goals: Int
)
