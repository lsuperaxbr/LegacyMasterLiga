package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.legacymasterliga.core.database.entity.StandingEntity
import com.example.legacymasterliga.core.database.model.StandingRow
import com.example.legacymasterliga.core.database.model.ClubSeasonHistoryRow
import kotlinx.coroutines.flow.Flow

@Dao
interface StandingDao {
    @Query("SELECT * FROM standings WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): StandingEntity?

    @Query("SELECT * FROM standings WHERE seasonId = :seasonId AND clubId = :clubId LIMIT 1")
    suspend fun findBySeasonAndClub(seasonId: Long, clubId: Long): StandingEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(standings: List<StandingEntity>)

    @Query("DELETE FROM standings WHERE seasonId = :seasonId")
    suspend fun deleteBySeason(seasonId: Long): Int

    @Query(
        """
        SELECT standings.id AS standingId,
               standings.seasonId AS seasonId,
               standings.clubId AS clubId,
               clubs.name AS clubName,
               clubs.crestUri AS shieldUri,
               standings.groupIndex AS groupIndex,
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
        LIMIT 1
        """,
    )
    suspend fun findLeader(seasonId: Long): StandingRow?

    @Query(
        """
        SELECT
            standings.id AS standingId,
            standings.seasonId AS seasonId,
            standings.clubId AS clubId,
            clubs.name AS clubName,
            clubs.crestUri AS shieldUri,
            standings.groupIndex AS groupIndex,
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
        ORDER BY standings.groupIndex ASC, 
                 standings.points DESC,
                 standings.wins DESC,
                 standings.goalDifference DESC,
                 standings.goalsFor DESC,
                 clubs.name COLLATE NOCASE ASC
        """,
    )
    fun observeTable(seasonId: Long): Flow<List<StandingRow>>

    @Query(
        """
        SELECT s.seasonId AS seasonId,
               seasons.name AS seasonName,
               competitions.name AS competitionName,
               competitions.pointsForWin AS pointsForWin,
               s.played AS played,
               s.wins AS wins,
               s.draws AS draws,
               s.losses AS losses,
               s.goalsFor AS goalsFor,
               s.goalsAgainst AS goalsAgainst,
               s.goalDifference AS goalDifference,
               s.points AS points,
               1 + (
                   SELECT COUNT(*)
                   FROM standings AS better
                   INNER JOIN clubs AS betterClub ON betterClub.id = better.clubId
                   INNER JOIN clubs AS currentClub ON currentClub.id = s.clubId
                   WHERE better.seasonId = s.seasonId
                     AND IFNULL(better.groupIndex, -1) = IFNULL(s.groupIndex, -1)
                     AND (
                         better.points > s.points OR
                         (better.points = s.points AND better.wins > s.wins) OR
                         (better.points = s.points AND better.wins = s.wins AND better.goalDifference > s.goalDifference) OR
                         (better.points = s.points AND better.wins = s.wins AND better.goalDifference = s.goalDifference AND better.goalsFor > s.goalsFor) OR
                         (better.points = s.points AND better.wins = s.wins AND better.goalDifference = s.goalDifference AND better.goalsFor = s.goalsFor AND LOWER(betterClub.name) < LOWER(currentClub.name))
                     )
               ) AS position
        FROM standings AS s
        INNER JOIN seasons ON seasons.id = s.seasonId
        INNER JOIN competitions ON competitions.id = seasons.competitionId
        WHERE s.clubId = :clubId
        ORDER BY seasons.createdAt DESC, competitions.name COLLATE NOCASE
        """,
    )
    fun observeHistoryByClub(clubId: Long): Flow<List<ClubSeasonHistoryRow>>
    @Query(
        """
        SELECT standings.id AS standingId, standings.seasonId AS seasonId, standings.clubId AS clubId,
               clubs.name AS clubName, clubs.crestUri AS shieldUri, standings.groupIndex AS groupIndex,
               standings.played AS played, standings.wins AS wins, standings.draws AS draws, standings.losses AS losses,
               standings.goalsFor AS goalsFor, standings.goalsAgainst AS goalsAgainst,
               standings.goalDifference AS goalDifference, standings.points AS points
        FROM standings INNER JOIN clubs ON clubs.id = standings.clubId
        WHERE standings.seasonId = :seasonId
        ORDER BY standings.groupIndex ASC, standings.points DESC, standings.wins DESC, 
                 standings.goalDifference DESC, standings.goalsFor DESC, clubs.name COLLATE NOCASE ASC
        """,
    )
    suspend fun findTableSnapshot(seasonId: Long): List<StandingRow>

}
