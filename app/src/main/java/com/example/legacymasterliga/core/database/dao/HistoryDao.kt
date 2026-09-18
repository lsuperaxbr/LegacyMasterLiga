package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.legacymasterliga.core.database.model.SeasonHistoryRow
import com.example.legacymasterliga.core.database.model.StandingRow
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query(
        """
        SELECT
            leagues.id AS leagueId,
            leagues.name AS leagueName,
            competitions.id AS competitionId,
            competitions.name AS competitionName,
            competitions.type AS competitionType,
            seasons.id AS seasonId,
            seasons.number AS seasonNumber,
            seasons.name AS seasonName,
            seasons.status AS seasonStatus,
            seasons.startedAt AS startedAt,
            seasons.finishedAt AS finishedAt,
            (
                SELECT s1.clubId
                FROM standings s1
                INNER JOIN clubs c1 ON c1.id = s1.clubId
                WHERE s1.seasonId = seasons.id
                ORDER BY s1.points DESC, s1.wins DESC, s1.goalDifference DESC,
                         s1.goalsFor DESC, c1.name COLLATE NOCASE ASC
                LIMIT 1
            ) AS championClubId,
            (
                SELECT c1.name
                FROM standings s1
                INNER JOIN clubs c1 ON c1.id = s1.clubId
                WHERE s1.seasonId = seasons.id
                ORDER BY s1.points DESC, s1.wins DESC, s1.goalDifference DESC,
                         s1.goalsFor DESC, c1.name COLLATE NOCASE ASC
                LIMIT 1
            ) AS championClubName,
            (
                SELECT s2.clubId
                FROM standings s2
                INNER JOIN clubs c2 ON c2.id = s2.clubId
                WHERE s2.seasonId = seasons.id
                ORDER BY s2.points DESC, s2.wins DESC, s2.goalDifference DESC,
                         s2.goalsFor DESC, c2.name COLLATE NOCASE ASC
                LIMIT 1 OFFSET 1
            ) AS runnerUpClubId,
            (
                SELECT c2.name
                FROM standings s2
                INNER JOIN clubs c2 ON c2.id = s2.clubId
                WHERE s2.seasonId = seasons.id
                ORDER BY s2.points DESC, s2.wins DESC, s2.goalDifference DESC,
                         s2.goalsFor DESC, c2.name COLLATE NOCASE ASC
                LIMIT 1 OFFSET 1
            ) AS runnerUpClubName,
            (SELECT COUNT(*) FROM competition_participants cp WHERE cp.seasonId = seasons.id) AS participantCount,
            (SELECT COUNT(*) FROM matches m WHERE m.seasonId = seasons.id AND m.status = 'FINISHED') AS completedMatchCount,
            COALESCE((SELECT SUM(m.homeScore + m.awayScore) FROM matches m WHERE m.seasonId = seasons.id AND m.status = 'FINISHED'), 0) AS totalGoals
        FROM seasons
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        INNER JOIN leagues ON leagues.id = competitions.leagueId
        WHERE leagues.id = :leagueId
        ORDER BY competitions.name COLLATE NOCASE ASC, seasons.number DESC
        """,
    )
    fun observeSeasonsByLeague(leagueId: Long): Flow<List<SeasonHistoryRow>>

    @Query(
        """
        SELECT
            standings.id AS standingId,
            standings.seasonId AS seasonId,
            standings.clubId AS clubId,
            clubs.name AS clubName,
            clubs.crestUri AS shieldUri,
            standings.played AS played,
            standings.wins AS wins,
            standings.draws AS draws,
            standings.losses AS losses,
            standings.goalsFor AS goalsFor,
            standings.goalsAgainst AS goalsAgainst,
            standings.goalDifference AS goalDifference,
            standings.points AS points
        FROM standings
        INNER JOIN clubs ON clubs.id = standings.clubId
        WHERE standings.seasonId = :seasonId
        ORDER BY standings.points DESC, standings.wins DESC,
                 standings.goalDifference DESC, standings.goalsFor DESC,
                 clubs.name COLLATE NOCASE ASC
        """,
    )
    fun observeFinalTable(seasonId: Long): Flow<List<StandingRow>>
}
