package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresidentProfileDao {

    // Clubes comandados pelo presidente (máx 2, excluindo banco da liga)
    @Query("""
        SELECT id, name, crestUri, leagueId
        FROM clubs
        WHERE presidentUserId = :userId AND isBank = 0
    """)
    fun observeClubs(userId: Long): Flow<List<PresidentClubRow>>

    // Títulos de Liga conquistados (temporadas encerradas oficialmente)
    @Query("""
        SELECT COUNT(*) FROM season_closures sc
        JOIN seasons s ON s.id = sc.seasonId
        JOIN competitions c ON c.id = s.competitionId
        WHERE sc.championPresidentUserId = :userId AND c.type = 'LEAGUE'
    """)
    fun observeTitles(userId: Long): Flow<Int>

    // Títulos de Copa conquistados (temporadas encerradas oficialmente)
    @Query("""
        SELECT COUNT(*) FROM season_closures sc
        JOIN seasons s ON s.id = sc.seasonId
        JOIN competitions c ON c.id = s.competitionId
        WHERE sc.championPresidentUserId = :userId AND c.type = 'CUP'
    """)
    fun observeCupTitles(userId: Long): Flow<Int>

    // Temporadas disputadas (seasons onde o clube do presidente tem standings)
    @Query("""
        SELECT COUNT(DISTINCT s.id) FROM seasons s
        JOIN standings st ON st.seasonId = s.id
        JOIN club_presidencies cp ON cp.clubId = st.clubId
        WHERE cp.userId = :userId
          AND s.createdAt >= cp.startAt
          AND (cp.endAt IS NULL OR s.createdAt <= cp.endAt)
    """)
    fun observeSeasonsPlayed(userId: Long): Flow<Int>

    // Totais históricos de partidas, vitórias, empates, derrotas e gols (somando todos os clubes do presidente)
    @Query("""
        SELECT
            COALESCE(SUM(st.played), 0) AS played,
            COALESCE(SUM(st.wins), 0) AS wins,
            COALESCE(SUM(st.draws), 0) AS draws,
            COALESCE(SUM(st.losses), 0) AS losses,
            COALESCE(SUM(st.goalsFor), 0) AS goalsFor,
            COALESCE(SUM(st.goalsAgainst), 0) AS goalsAgainst,
            COALESCE(SUM(st.points), 0) AS points
        FROM standings st
        JOIN seasons s ON s.id = st.seasonId
        JOIN club_presidencies cp ON cp.clubId = st.clubId
        WHERE cp.userId = :userId
          AND s.createdAt >= cp.startAt
          AND (cp.endAt IS NULL OR s.createdAt <= cp.endAt)
    """)
    fun observeCareerTotals(userId: Long): Flow<PresidentCareerRow>

    // Saldo atual em CR (somando todos os clubes do presidente)
    @Query("""
        SELECT COALESCE(SUM(ft.amountCr), 0)
        FROM financial_transactions ft
        JOIN clubs cl ON cl.id = ft.clubId
        WHERE cl.presidentUserId = :userId
    """)
    fun observeCurrentBalance(userId: Long): Flow<Long>

    // Total movimentado no mercado em CR
    @Query("""
        SELECT COALESCE(SUM(ABS(t.valueCr)), 0)
        FROM transfers t
        JOIN clubs cl ON cl.id = t.originClubId OR cl.id = t.destinationClubId
        WHERE cl.presidentUserId = :userId
    """)
    fun observeMarketMovement(userId: Long): Flow<Long>
}

data class PresidentClubRow(val id: Long, val name: String, val crestUri: String?, val leagueId: Long)
data class PresidentCareerRow(val played: Long, val wins: Long, val draws: Long, val losses: Long, val goalsFor: Long, val goalsAgainst: Long, val points: Long)
