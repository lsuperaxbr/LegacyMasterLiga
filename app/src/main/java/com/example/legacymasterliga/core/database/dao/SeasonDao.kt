package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.SeasonEntity
import com.example.legacymasterliga.core.model.SeasonStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(season: SeasonEntity): Long

    @Update
    suspend fun update(season: SeasonEntity)

    @Query("SELECT * FROM seasons WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<SeasonEntity?>

    @Query("SELECT * FROM seasons WHERE competitionId = :competitionId ORDER BY number DESC")
    fun observeByCompetition(competitionId: Long): Flow<List<SeasonEntity>>

    @Query(
        """
        SELECT seasons.* FROM seasons
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        WHERE competitions.leagueId = :leagueId
        ORDER BY seasons.competitionId, seasons.number DESC
        """,
    )
    fun observeByLeague(leagueId: Long): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): SeasonEntity?

    @Query("SELECT * FROM seasons WHERE competitionId = :competitionId AND number = :number LIMIT 1")
    suspend fun findByCompetitionAndNumber(competitionId: Long, number: Int): SeasonEntity?

    @Query("SELECT * FROM seasons WHERE competitionId = :competitionId AND status = :status LIMIT 1")
    suspend fun findByStatus(competitionId: Long, status: SeasonStatus): SeasonEntity?

    @Query("SELECT MAX(number) FROM seasons WHERE competitionId = :competitionId")
    suspend fun findHighestNumber(competitionId: Long): Int?
}
