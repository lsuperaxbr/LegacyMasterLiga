package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.legacymasterliga.core.database.model.DashboardClubBalanceRow
import com.example.legacymasterliga.core.database.model.DashboardContextRow
import com.example.legacymasterliga.core.database.model.DashboardLeaderRow
import com.example.legacymasterliga.core.database.model.DashboardNewsRow
import com.example.legacymasterliga.core.database.model.DashboardResultRow
import com.example.legacymasterliga.core.database.model.DashboardRoundRow
import com.example.legacymasterliga.core.database.model.DashboardTransferRow
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query(
        """
        SELECT l.id AS leagueId,
               l.name AS leagueName,
               c.id AS competitionId,
               c.name AS competitionName,
               s.id AS seasonId,
               s.name AS seasonName
        FROM leagues l
        LEFT JOIN competitions c ON c.leagueId = l.id AND c.status = 'ACTIVE' AND c.type != 'CUP'
        LEFT JOIN seasons s ON s.competitionId = c.id AND s.status = 'ACTIVE'
        WHERE l.status = 'ACTIVE'
        ORDER BY (CASE WHEN l.isOnline = 1 AND l.cloudLeagueId IS NOT NULL THEN 0 ELSE 1 END), l.id, s.id DESC, s.number DESC
        LIMIT 1
        """,
    )
    fun observeContext(): Flow<DashboardContextRow?>


    @Query("SELECT COUNT(*) FROM competitions WHERE leagueId = :leagueId")
    fun observeCompetitionCount(leagueId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM clubs WHERE leagueId = :leagueId AND isActive = 1 AND isBank = 0")
    fun observeActiveClubCount(leagueId: Long): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(ft.amountCr), 0)
        FROM financial_transactions ft
        INNER JOIN clubs c ON c.id = ft.clubId
        WHERE c.leagueId = :leagueId AND c.isBank = 0
    """)
    fun observeTotalBalance(leagueId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM transfers WHERE leagueId = :leagueId")
    fun observeTransferCount(leagueId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM news WHERE leagueId = :leagueId")
    fun observeNewsCount(leagueId: Long): Flow<Int>

    @Query(
        """
        SELECT st.clubId AS clubId,
               c.name AS clubName,
               c.crestUri AS crestUri,
               st.points AS points,
               st.played AS played
        FROM standings st
        INNER JOIN clubs c ON c.id = st.clubId
        WHERE st.seasonId = :seasonId
        ORDER BY st.points DESC, st.wins DESC, st.goalDifference DESC,
                 st.goalsFor DESC, c.name COLLATE NOCASE ASC
        LIMIT 1
        """,
    )
    fun observeLeader(seasonId: Long): Flow<DashboardLeaderRow?>

    @Query(
        """
        SELECT r.seasonId AS seasonId,
               r.id AS roundId,
               r.number AS roundNumber,
               r.name AS roundName,
               SUM(CASE WHEN m.status != 'FINISHED' THEN 1 ELSE 0 END) AS pendingMatches,
               COUNT(m.id) AS totalMatches
        FROM rounds r
        INNER JOIN matches m ON m.roundId = r.id
        WHERE r.seasonId = :seasonId
          AND EXISTS (SELECT 1 FROM matches x WHERE x.roundId = r.id AND x.status != 'FINISHED')
        GROUP BY r.id
        ORDER BY r.number
        LIMIT 1
        """,
    )
    fun observeNextRound(seasonId: Long): Flow<DashboardRoundRow?>

    @Query(
        """
        SELECT m.id AS matchId,
               r.number AS roundNumber,
               home.name AS homeClubName,
               away.name AS awayClubName,
               COALESCE(m.homeScore, 0) AS homeScore,
               COALESCE(m.awayScore, 0) AS awayScore
        FROM matches m
        INNER JOIN rounds r ON r.id = m.roundId
        INNER JOIN clubs home ON home.id = m.homeClubId
        INNER JOIN clubs away ON away.id = m.awayClubId
        WHERE m.seasonId = :seasonId AND m.status = 'FINISHED'
        ORDER BY m.updatedAt DESC, m.id DESC
        LIMIT 3
        """,
    )
    fun observeRecentResults(seasonId: Long): Flow<List<DashboardResultRow>>

    @Query(
        """
        SELECT t.id AS transferId,
               t.playerName AS playerName,
               origin.name AS originClubName,
               destination.name AS destinationClubName,
               t.valueCr AS valueCr,
               t.createdAt AS createdAt
        FROM transfers t
        INNER JOIN clubs origin ON origin.id = t.originClubId
        INNER JOIN clubs destination ON destination.id = t.destinationClubId
        WHERE t.leagueId = :leagueId
        ORDER BY t.createdAt DESC, t.id DESC
        LIMIT 3
        """,
    )
    fun observeLatestTransfers(leagueId: Long): Flow<List<DashboardTransferRow>>

    @Query(
        """
        SELECT id AS newsId, title, category, publishedAt
        FROM news
        WHERE leagueId = :leagueId
        ORDER BY publishedAt DESC, id DESC
        LIMIT 3
        """,
    )
    fun observeLatestNews(leagueId: Long): Flow<List<DashboardNewsRow>>

    @Query(
        """
        SELECT c.id AS clubId,
               c.name AS clubName,
               COALESCE(SUM(ft.amountCr), 0) AS balanceCr
        FROM clubs c
        LEFT JOIN financial_transactions ft ON ft.clubId = c.id
        WHERE c.presidentUserId = :userId AND c.isBank = 0
        GROUP BY c.id
        """,
    )
    fun observePresidentClubBalances(userId: Long): Flow<List<DashboardClubBalanceRow>>
}
