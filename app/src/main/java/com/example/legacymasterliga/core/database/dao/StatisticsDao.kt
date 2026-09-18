package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.legacymasterliga.core.database.model.ClubStatisticsRankingRow
import com.example.legacymasterliga.core.database.model.LeagueStatisticsOverviewRow
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM matches m
                JOIN seasons s ON s.id = m.seasonId
                JOIN competitions c ON c.id = s.competitionId
                WHERE c.leagueId = :leagueId AND m.status = 'FINISHED'
                  AND (:competitionId IS NULL OR c.id = :competitionId)
                  AND (:seasonId IS NULL OR s.id = :seasonId)) AS finishedMatches,
            (SELECT COALESCE(SUM(m.homeScore + m.awayScore), 0) FROM matches m
                JOIN seasons s ON s.id = m.seasonId
                JOIN competitions c ON c.id = s.competitionId
                WHERE c.leagueId = :leagueId AND m.status = 'FINISHED'
                  AND (:competitionId IS NULL OR c.id = :competitionId)
                  AND (:seasonId IS NULL OR s.id = :seasonId)) AS totalGoals,
            (SELECT COALESCE(AVG(CAST(m.homeScore + m.awayScore AS REAL)), 0.0) FROM matches m
                JOIN seasons s ON s.id = m.seasonId
                JOIN competitions c ON c.id = s.competitionId
                WHERE c.leagueId = :leagueId AND m.status = 'FINISHED'
                  AND (:competitionId IS NULL OR c.id = :competitionId)
                  AND (:seasonId IS NULL OR s.id = :seasonId)) AS averageGoals,
            (SELECT COALESCE(SUM(t.valueCr), 0) FROM transfers t WHERE t.leagueId = :leagueId) AS totalCrMoved,
            (SELECT COUNT(*) FROM clubs cl WHERE cl.leagueId = :leagueId AND cl.isActive = 1 AND cl.isBank = 0) AS activeClubs,
            (SELECT COUNT(*) FROM seasons s JOIN competitions c ON c.id = s.competitionId
                WHERE c.leagueId = :leagueId
                  AND (:competitionId IS NULL OR c.id = :competitionId)
                  AND (:seasonId IS NULL OR s.id = :seasonId)) AS seasonsCount
        """,
    )
    fun observeOverview(
        leagueId: Long,
        competitionId: Long?,
        seasonId: Long?,
    ): Flow<LeagueStatisticsOverviewRow>

    @Query(
        """
        SELECT
            cl.id AS clubId,
            cl.name AS clubName,
            cl.crestUri AS crestUri,
            COALESCE(SUM(st.played), 0) AS played,
            COALESCE(SUM(st.wins), 0) AS wins,
            COALESCE(SUM(st.draws), 0) AS draws,
            COALESCE(SUM(st.losses), 0) AS losses,
            COALESCE(SUM(st.goalsFor), 0) AS goalsFor,
            COALESCE(SUM(st.goalsAgainst), 0) AS goalsAgainst,
            COALESCE(SUM(st.goalDifference), 0) AS goalDifference,
            COALESCE(SUM(st.points), 0) AS points,
            CASE WHEN COALESCE(SUM(st.played), 0) = 0 THEN 0.0
                 ELSE CAST(COALESCE(SUM(st.points), 0) AS REAL) / CAST(SUM(st.played) AS REAL) END AS averagePoints,
            COALESCE((SELECT SUM(t.valueCr) FROM transfers t
                WHERE t.leagueId = :leagueId AND (t.originClubId = cl.id OR t.destinationClubId = cl.id)), 0) AS crMoved
        FROM clubs cl
        LEFT JOIN standings st ON st.clubId = cl.id
        LEFT JOIN seasons s ON s.id = st.seasonId
        LEFT JOIN competitions c ON c.id = s.competitionId
        WHERE cl.leagueId = :leagueId AND cl.isBank = 0
          AND (:competitionId IS NULL OR c.id = :competitionId)
          AND (:seasonId IS NULL OR s.id = :seasonId)
        GROUP BY cl.id
        ORDER BY points DESC, wins DESC, goalDifference DESC, goalsFor DESC, cl.name COLLATE NOCASE
        """,
    )
    fun observeClubRanking(
        leagueId: Long,
        competitionId: Long?,
        seasonId: Long?,
    ): Flow<List<ClubStatisticsRankingRow>>
}
