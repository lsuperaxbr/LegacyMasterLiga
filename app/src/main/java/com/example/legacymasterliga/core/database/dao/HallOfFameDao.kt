package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.legacymasterliga.core.database.model.AggregateClubRecordRow
import com.example.legacymasterliga.core.database.model.BiggestWinRow
import com.example.legacymasterliga.core.database.model.ChampionRecordRow
import com.example.legacymasterliga.core.database.model.FinishedMatchTimelineRow
import com.example.legacymasterliga.core.database.model.SeasonClubRecordRow
import kotlinx.coroutines.flow.Flow

@Dao
interface HallOfFameDao {
    @Query(
        """
        SELECT clubs.id AS clubId, clubs.name AS clubName, clubs.crestUri AS crestUri,
               COUNT(*) AS titles
        FROM seasons
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        INNER JOIN standings winner ON winner.seasonId = seasons.id
        INNER JOIN clubs ON clubs.id = winner.clubId
        WHERE competitions.leagueId = :leagueId
          AND seasons.status IN ('FINISHED', 'ARCHIVED')
          AND winner.id = (
              SELECT candidate.id
              FROM standings candidate
              INNER JOIN clubs candidateClub ON candidateClub.id = candidate.clubId
              WHERE candidate.seasonId = seasons.id
              ORDER BY candidate.points DESC, candidate.wins DESC,
                       candidate.goalDifference DESC, candidate.goalsFor DESC,
                       candidateClub.name COLLATE NOCASE ASC
              LIMIT 1
          )
        GROUP BY clubs.id, clubs.name, clubs.crestUri
        ORDER BY titles DESC, clubs.name COLLATE NOCASE ASC
        LIMIT 1
        """,
    )
    fun observeTopChampion(leagueId: Long): Flow<ChampionRecordRow?>

    @Query(
        """
        SELECT clubs.id AS clubId, clubs.name AS clubName, clubs.crestUri AS crestUri,
               COALESCE(SUM(standings.wins), 0) AS value
        FROM clubs
        INNER JOIN competitions ON competitions.leagueId = clubs.leagueId
        INNER JOIN seasons ON seasons.competitionId = competitions.id
        INNER JOIN standings ON standings.seasonId = seasons.id AND standings.clubId = clubs.id
        WHERE clubs.leagueId = :leagueId AND clubs.isBank = 0
        GROUP BY clubs.id, clubs.name, clubs.crestUri
        ORDER BY value DESC, clubs.name COLLATE NOCASE ASC
        LIMIT 1
        """,
    )
    fun observeMostWins(leagueId: Long): Flow<AggregateClubRecordRow?>

    @Query(
        """
        SELECT clubs.id AS clubId, clubs.name AS clubName, clubs.crestUri AS crestUri,
               competitions.name AS competitionName, seasons.id AS seasonId,
               seasons.name AS seasonName, standings.goalsFor AS value
        FROM standings
        INNER JOIN clubs ON clubs.id = standings.clubId
        INNER JOIN seasons ON seasons.id = standings.seasonId
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        WHERE competitions.leagueId = :leagueId AND standings.played > 0
        ORDER BY standings.goalsFor DESC, standings.goalDifference DESC,
                 clubs.name COLLATE NOCASE ASC
        LIMIT 1
        """,
    )
    fun observeBestAttack(leagueId: Long): Flow<SeasonClubRecordRow?>

    @Query(
        """
        SELECT clubs.id AS clubId, clubs.name AS clubName, clubs.crestUri AS crestUri,
               competitions.name AS competitionName, seasons.id AS seasonId,
               seasons.name AS seasonName, standings.goalsAgainst AS value
        FROM standings
        INNER JOIN clubs ON clubs.id = standings.clubId
        INNER JOIN seasons ON seasons.id = standings.seasonId
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        WHERE competitions.leagueId = :leagueId
          AND seasons.status IN ('FINISHED', 'ARCHIVED')
          AND standings.played > 0
        ORDER BY standings.goalsAgainst ASC, standings.played DESC,
                 clubs.name COLLATE NOCASE ASC
        LIMIT 1
        """,
    )
    fun observeBestDefense(leagueId: Long): Flow<SeasonClubRecordRow?>

    @Query(
        """
        SELECT matches.id AS matchId, matches.seasonId AS seasonId,
               competitions.name AS competitionName, seasons.name AS seasonName,
               rounds.number AS roundNumber,
               matches.homeClubId AS homeClubId, home.name AS homeClubName,
               matches.awayClubId AS awayClubId, away.name AS awayClubName,
               matches.homeScore AS homeScore, matches.awayScore AS awayScore,
               ABS(matches.homeScore - matches.awayScore) AS goalDifference
        FROM matches
        INNER JOIN seasons ON seasons.id = matches.seasonId
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        INNER JOIN rounds ON rounds.id = matches.roundId
        INNER JOIN clubs home ON home.id = matches.homeClubId
        INNER JOIN clubs away ON away.id = matches.awayClubId
        WHERE competitions.leagueId = :leagueId
          AND matches.status = 'FINISHED'
          AND matches.homeScore IS NOT NULL
          AND matches.awayScore IS NOT NULL
          AND matches.homeScore != matches.awayScore
        ORDER BY goalDifference DESC,
                 (matches.homeScore + matches.awayScore) DESC,
                 matches.updatedAt ASC
        LIMIT 1
        """,
    )
    fun observeBiggestWin(leagueId: Long): Flow<BiggestWinRow?>

    @Query(
        """
        SELECT matches.id AS matchId, matches.seasonId AS seasonId,
               competitions.name AS competitionName, seasons.name AS seasonName,
               rounds.number AS roundNumber, matches.leg AS leg,
               matches.homeClubId AS homeClubId, home.name AS homeClubName,
               home.crestUri AS homeCrestUri,
               matches.awayClubId AS awayClubId, away.name AS awayClubName,
               away.crestUri AS awayCrestUri,
               matches.homeScore AS homeScore, matches.awayScore AS awayScore,
               COALESCE(matches.scheduledAt, matches.updatedAt, matches.createdAt) AS eventOrder
        FROM matches
        INNER JOIN seasons ON seasons.id = matches.seasonId
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        INNER JOIN rounds ON rounds.id = matches.roundId
        INNER JOIN clubs home ON home.id = matches.homeClubId
        INNER JOIN clubs away ON away.id = matches.awayClubId
        WHERE competitions.leagueId = :leagueId
          AND matches.status = 'FINISHED'
          AND matches.homeScore IS NOT NULL
          AND matches.awayScore IS NOT NULL
        ORDER BY seasons.id ASC, rounds.number ASC, matches.leg ASC,
                 eventOrder ASC, matches.id ASC
        """,
    )
    fun observeFinishedMatches(leagueId: Long): Flow<List<FinishedMatchTimelineRow>>

    @Query(
        """
        SELECT clubs.id AS clubId, clubs.name AS clubName, clubs.crestUri AS crestUri,
               COALESCE(SUM(transfers.valueCr), 0) AS value
        FROM clubs
        LEFT JOIN transfers ON transfers.leagueId = :leagueId
          AND (transfers.originClubId = clubs.id OR transfers.destinationClubId = clubs.id)
        WHERE clubs.leagueId = :leagueId AND clubs.isBank = 0
        GROUP BY clubs.id, clubs.name, clubs.crestUri
        ORDER BY value DESC, clubs.name COLLATE NOCASE ASC
        LIMIT 1
        """,
    )
    fun observeMostCrMovement(leagueId: Long): Flow<AggregateClubRecordRow?>
}
