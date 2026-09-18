package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.MatchEntity
import com.example.legacymasterliga.core.database.model.ScheduledMatchRow
import com.example.legacymasterliga.core.database.model.ClubRecentMatchRow
import com.example.legacymasterliga.core.model.MatchStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Update
    suspend fun update(match: MatchEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(matches: List<MatchEntity>): List<Long>

    @Query("SELECT * FROM matches WHERE seasonId = :seasonId ORDER BY roundId, id")
    suspend fun findBySeason(seasonId: Long): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun findById(matchId: Long): MatchEntity?

    @Query("SELECT * FROM matches WHERE seasonId = :seasonId AND stage = :stage")
    suspend fun findBySeasonAndStage(seasonId: Long, stage: String): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE seasonId = :seasonId AND pairingKey = :key AND leg = :leg LIMIT 1")
    suspend fun findByPairingAndLeg(seasonId: Long, key: String, leg: Int): MatchEntity?

    @Query("SELECT * FROM matches WHERE roundId = :roundId AND bracketPosition = :position AND leg = :leg LIMIT 1")
    suspend fun findByRoundBracketAndLeg(roundId: Long, position: Int, leg: Int): MatchEntity?

    @Query("SELECT * FROM matches WHERE seasonId = :seasonId AND status = 'FINISHED' ORDER BY roundId, id")
    suspend fun findFinishedBySeason(seasonId: Long): List<MatchEntity>

    @Query(
        """
        UPDATE matches
        SET homeScore = :homeScore,
            awayScore = :awayScore,
            status = :status,
            updatedAt = :updatedAt
        WHERE id = :matchId
        """,
    )
    suspend fun updateResult(
        matchId: Long,
        homeScore: Int,
        awayScore: Int,
        status: MatchStatus = MatchStatus.FINISHED,
        updatedAt: Long = System.currentTimeMillis(),
    ): Int

    @Query(
        """
        UPDATE matches
        SET homeScore = :h,
            awayScore = :a,
            penaltiesHome = :ph,
            penaltiesAway = :pa,
            winnerClubId = :winner,
            advanceReason = :reason,
            status = :status,
            updatedAt = :now
        WHERE id = :id
        """,
    )
    suspend fun updateMatchFull(
        id: Long,
        h: Int,
        a: Int,
        ph: Int?,
        pa: Int?,
        winner: Long?,
        reason: String?,
        status: MatchStatus = MatchStatus.FINISHED,
        now: Long = System.currentTimeMillis(),
    ): Int

    @Query("""
        UPDATE matches SET homeYellowCards = :hy, homeRedCards = :hr,
        awayYellowCards = :ay, awayRedCards = :ar WHERE id = :matchId
    """)
    suspend fun updateCards(matchId: Long, hy: Int, hr: Int, ay: Int, ar: Int)

    @Query("""
        SELECT clubId, SUM(yellow) AS yellowCount, SUM(red) AS redCount FROM (
            SELECT homeClubId AS clubId, homeYellowCards AS yellow, homeRedCards AS red
            FROM matches WHERE seasonId = :seasonId AND status = 'FINISHED'
            UNION ALL
            SELECT awayClubId AS clubId, awayYellowCards AS yellow, awayRedCards AS red
            FROM matches WHERE seasonId = :seasonId AND status = 'FINISHED'
        ) GROUP BY clubId HAVING yellowCount > 0 OR redCount > 0
    """)
    suspend fun findCardCountsBySeason(seasonId: Long): List<ClubCardCount>

    @Query("SELECT COUNT(*) FROM matches WHERE roundId = :roundId")
    suspend fun countByRound(roundId: Long): Int

    @Query("SELECT COUNT(*) FROM matches WHERE roundId = :roundId AND status = 'FINISHED'")
    suspend fun countFinishedByRound(roundId: Long): Int

    @Query("SELECT COUNT(*) FROM matches WHERE seasonId = :seasonId")
    suspend fun countBySeason(seasonId: Long): Int

    @Query("SELECT COUNT(*) FROM matches WHERE seasonId = :seasonId AND status = 'FINISHED'")
    suspend fun countFinishedBySeason(seasonId: Long): Int

    @Query("DELETE FROM matches WHERE seasonId = :seasonId")
    suspend fun deleteBySeason(seasonId: Long): Int

    @Query("DELETE FROM matches WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("SELECT COUNT(*) FROM matches WHERE seasonId = :seasonId AND stage != 'PLACEHOLDER'")
    suspend fun countValidMatchesBySeason(seasonId: Long): Int

    @Query("SELECT COUNT(*) FROM matches WHERE seasonId = :seasonId AND stage != 'PLACEHOLDER' AND status = 'FINISHED'")
    suspend fun countFinishedValidMatchesBySeason(seasonId: Long): Int

    @Query(
        """
        SELECT m1.id FROM matches m1
        JOIN rounds r1 ON r1.id = m1.roundId
        WHERE m1.seasonId = :seasonId 
        AND m1.id > (
            SELECT MIN(m2.id) FROM matches m2
            JOIN rounds r2 ON r2.id = m2.roundId
            WHERE m2.seasonId = :seasonId 
            AND r2.stageLabel = r1.stageLabel 
            AND m2.pairingKey = m1.pairingKey 
            AND m2.leg = m1.leg
        )
        """,
    )
    suspend fun findDuplicateMatchIds(seasonId: Long): List<Long>

    @Query(
        """
        SELECT
            matches.id AS matchId,
            rounds.id AS roundId,
            rounds.number AS roundNumber,
            rounds.name AS roundName,
            rounds.status AS roundStatus,
            rounds.stage AS stage,
            rounds.stageLabel AS stageLabel,
            home.id AS homeClubId,
            home.name AS homeClubName,
            home.crestUri AS homeShieldUri,
            away.id AS awayClubId,
            away.name AS awayClubName,
            away.crestUri AS awayShieldUri,
            matches.leg AS leg,
            matches.homeScore AS homeScore,
            matches.awayScore AS awayScore,
            matches.status AS matchStatus,
            matches.penaltiesHome AS penaltiesHome,
            matches.penaltiesAway AS penaltiesAway,
            matches.winnerClubId AS winnerClubId
        FROM matches
        INNER JOIN rounds ON rounds.id = matches.roundId
        INNER JOIN clubs AS home ON home.id = matches.homeClubId
        INNER JOIN clubs AS away ON away.id = matches.awayClubId
        WHERE matches.seasonId = :seasonId
        ORDER BY rounds.number, matches.id
        """,
    )
    fun observeSchedule(seasonId: Long): Flow<List<ScheduledMatchRow>>

    @Query(
        """
        SELECT
            matches.id AS matchId,
            rounds.id AS roundId,
            rounds.number AS roundNumber,
            rounds.name AS roundName,
            rounds.status AS roundStatus,
            rounds.stage AS stage,
            rounds.stageLabel AS stageLabel,
            home.id AS homeClubId,
            home.name AS homeClubName,
            home.crestUri AS homeShieldUri,
            away.id AS awayClubId,
            away.name AS awayClubName,
            away.crestUri AS awayShieldUri,
            matches.leg AS leg,
            matches.homeScore AS homeScore,
            matches.awayScore AS awayScore,
            matches.status AS matchStatus,
            matches.penaltiesHome AS penaltiesHome,
            matches.penaltiesAway AS penaltiesAway,
            matches.winnerClubId AS winnerClubId
        FROM matches
        INNER JOIN rounds ON rounds.id = matches.roundId
        INNER JOIN clubs AS home ON home.id = matches.homeClubId
        INNER JOIN clubs AS away ON away.id = matches.awayClubId
        WHERE matches.seasonId = :seasonId
        ORDER BY rounds.number, matches.id
        """,
    )
    suspend fun findScheduledRowsBySeason(seasonId: Long): List<ScheduledMatchRow>

    @Query(
        """
        SELECT
            matches.id AS matchId,
               matches.seasonId AS seasonId,
               rounds.number AS roundNumber,
               home.id AS homeClubId,
               home.name AS homeClubName,
               away.id AS awayClubId,
               away.name AS awayClubName,
               COALESCE(matches.homeScore, 0) AS homeScore,
               COALESCE(matches.awayScore, 0) AS awayScore
        FROM matches
        INNER JOIN rounds ON rounds.id = matches.roundId
        INNER JOIN clubs AS home ON home.id = matches.homeClubId
        INNER JOIN clubs AS away ON away.id = matches.awayClubId
        WHERE matches.seasonId = :seasonId
          AND matches.status = 'FINISHED'
          AND (matches.homeClubId = :clubId OR matches.awayClubId = :clubId)
        ORDER BY rounds.number DESC, matches.id DESC
        LIMIT :limit
        """,
    )
    fun observeRecentByClub(
        seasonId: Long,
        clubId: Long,
        limit: Int = 5,
    ): Flow<List<ClubRecentMatchRow>>
}

data class ClubCardCount(val clubId: Long, val yellowCount: Int, val redCount: Int)
