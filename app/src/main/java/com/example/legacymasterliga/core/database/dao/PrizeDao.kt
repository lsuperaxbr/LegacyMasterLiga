package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.legacymasterliga.core.database.entity.CompetitionPrizeEntity
import com.example.legacymasterliga.core.database.entity.PrizeHistoryEntity
import com.example.legacymasterliga.core.database.model.PrizeHistoryRow
import kotlinx.coroutines.flow.Flow

@Dao
interface PrizeDao {
    @Upsert
    suspend fun upsertConfiguration(configuration: CompetitionPrizeEntity)

    @Query("SELECT * FROM competition_prizes WHERE competitionId = :competitionId LIMIT 1")
    suspend fun findConfiguration(competitionId: Long): CompetitionPrizeEntity?

    @Query("SELECT * FROM competition_prizes WHERE competitionId = :competitionId LIMIT 1")
    fun observeConfiguration(competitionId: Long): Flow<CompetitionPrizeEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHistory(history: PrizeHistoryEntity): Long

    @Query(
        """
        SELECT ph.id, ph.leagueId, ph.competitionId, c.name AS competitionName,
               ph.seasonId, s.name AS seasonName, ph.clubId, cl.name AS clubName,
               ph.prizeType, ph.amountCr, ph.description, ph.awardedAt
        FROM prize_history ph
        JOIN competitions c ON c.id = ph.competitionId
        JOIN seasons s ON s.id = ph.seasonId
        JOIN clubs cl ON cl.id = ph.clubId
        WHERE ph.leagueId = :leagueId
        ORDER BY ph.awardedAt DESC, ph.id DESC
        """,
    )
    fun observeHistoryByLeague(leagueId: Long): Flow<List<PrizeHistoryRow>>

    @Query("SELECT EXISTS(SELECT 1 FROM prize_history WHERE seasonId = :seasonId AND clubId = :clubId AND prizeType = :prizeType)")
    suspend fun isPrizeAwarded(seasonId: Long, clubId: Long, prizeType: String): Boolean
}
