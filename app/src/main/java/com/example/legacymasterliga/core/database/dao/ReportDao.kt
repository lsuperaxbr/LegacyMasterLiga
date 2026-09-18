package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.legacymasterliga.core.database.model.*

@Dao
interface ReportDao {
    @Query("SELECT id, name FROM leagues ORDER BY name COLLATE NOCASE") suspend fun leagues(): List<ReportLeagueRow>
    @Query("SELECT id, leagueId, name FROM competitions WHERE leagueId = :leagueId ORDER BY name COLLATE NOCASE") suspend fun competitions(leagueId: Long): List<ReportCompetitionRow>
    @Query("SELECT id, competitionId, name FROM seasons WHERE competitionId = :competitionId ORDER BY number DESC") suspend fun seasons(competitionId: Long): List<ReportSeasonRow>

    @Query("""
        SELECT c.name AS clubName, s.played, s.wins, s.draws, s.losses, s.goalsFor, s.goalsAgainst, s.goalDifference, s.points
        FROM standings s JOIN clubs c ON c.id = s.clubId
        WHERE s.seasonId = :seasonId
        ORDER BY s.points DESC, s.wins DESC, s.goalDifference DESC, s.goalsFor DESC, c.name COLLATE NOCASE
    """) suspend fun standings(seasonId: Long): List<ReportStandingRow>

    @Query("""
        SELECT c.name AS clubName, f.amountCr, f.description, f.type, f.createdAt
        FROM financial_transactions f JOIN clubs c ON c.id = f.clubId
        WHERE c.leagueId = :leagueId ORDER BY f.createdAt DESC
    """) suspend fun finance(leagueId: Long): List<ReportFinanceRow>

    @Query("""
        SELECT t.playerName, o.name AS originClubName, d.name AS destinationClubName, t.valueCr, t.createdAt
        FROM transfers t JOIN clubs o ON o.id = t.originClubId JOIN clubs d ON d.id = t.destinationClubId
        WHERE t.leagueId = :leagueId ORDER BY t.createdAt DESC
    """) suspend fun transfers(leagueId: Long): List<ReportTransferRow>

    @Query("""
        SELECT r.number AS roundNumber, h.name AS homeClubName, a.name AS awayClubName,
               COALESCE(m.homeScore,0) AS homeScore, COALESCE(m.awayScore,0) AS awayScore
        FROM matches m JOIN rounds r ON r.id = m.roundId JOIN clubs h ON h.id = m.homeClubId JOIN clubs a ON a.id = m.awayClubId
        JOIN seasons s ON s.id = m.seasonId JOIN competitions c ON c.id = s.competitionId
        WHERE c.leagueId = :leagueId AND m.status = 'FINISHED'
          AND (:competitionId IS NULL OR c.id = :competitionId)
          AND (:seasonId IS NULL OR s.id = :seasonId)
        ORDER BY r.number DESC, m.id DESC
    """) suspend fun matches(leagueId: Long, competitionId: Long?, seasonId: Long?): List<ReportMatchRow>

    @Query("""
        SELECT c.name AS competitionName, s.name AS seasonName,
          (SELECT cl.name FROM final_standings fs JOIN clubs cl ON cl.id=fs.clubId WHERE fs.seasonId=s.id ORDER BY fs.position LIMIT 1) AS championName,
          (SELECT cl.name FROM final_standings fs JOIN clubs cl ON cl.id=fs.clubId WHERE fs.seasonId=s.id ORDER BY fs.position LIMIT 1 OFFSET 1) AS runnerUpName,
          COALESCE((SELECT SUM(m.homeScore+m.awayScore) FROM matches m WHERE m.seasonId=s.id AND m.status='FINISHED'),0) AS totalGoals,
          (SELECT COUNT(*) FROM matches m WHERE m.seasonId=s.id AND m.status='FINISHED') AS completedMatches
        FROM seasons s JOIN competitions c ON c.id=s.competitionId
        WHERE c.leagueId=:leagueId AND (:competitionId IS NULL OR c.id=:competitionId) AND (:seasonId IS NULL OR s.id=:seasonId)
        ORDER BY c.name, s.number DESC
    """) suspend fun history(leagueId: Long, competitionId: Long?, seasonId: Long?): List<ReportHistoryRow>

    @Query("""
        SELECT
          (SELECT COUNT(*) FROM matches m JOIN seasons s ON s.id=m.seasonId JOIN competitions c ON c.id=s.competitionId WHERE c.leagueId=:leagueId AND m.status='FINISHED' AND (:competitionId IS NULL OR c.id=:competitionId) AND (:seasonId IS NULL OR s.id=:seasonId)) AS finishedMatches,
          (SELECT COALESCE(SUM(m.homeScore+m.awayScore),0) FROM matches m JOIN seasons s ON s.id=m.seasonId JOIN competitions c ON c.id=s.competitionId WHERE c.leagueId=:leagueId AND m.status='FINISHED' AND (:competitionId IS NULL OR c.id=:competitionId) AND (:seasonId IS NULL OR s.id=:seasonId)) AS totalGoals,
          (SELECT COALESCE(AVG(CAST(m.homeScore+m.awayScore AS REAL)),0.0) FROM matches m JOIN seasons s ON s.id=m.seasonId JOIN competitions c ON c.id=s.competitionId WHERE c.leagueId=:leagueId AND m.status='FINISHED' AND (:competitionId IS NULL OR c.id=:competitionId) AND (:seasonId IS NULL OR s.id=:seasonId)) AS averageGoals,
          (SELECT COALESCE(SUM(valueCr),0) FROM transfers WHERE leagueId=:leagueId) AS totalCrMoved,
          (SELECT COUNT(*) FROM clubs WHERE leagueId=:leagueId AND isActive=1 AND isBank=0) AS activeClubs
    """) suspend fun statistics(leagueId: Long, competitionId: Long?, seasonId: Long?): ReportStatisticsRow
}
