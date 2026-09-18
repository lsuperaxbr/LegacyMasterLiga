package com.example.legacymasterliga.feature.results

import org.junit.Test
import org.junit.Assert.*

class StandingsLogicTest {

    private data class Match(
        val homeClubId: Long,
        val awayClubId: Long,
        val homeScore: Int,
        val awayScore: Int,
        val stage: String = "REGULAR"
    )

    private class MutableStanding(
        var played: Int = 0,
        var wins: Int = 0,
        var draws: Int = 0,
        var losses: Int = 0,
        var goalsFor: Int = 0,
        var goalsAgainst: Int = 0,
        var points: Int = 0,
    )

    @Test
    fun verifyStandingsCalculation() {
        // Scenario A, B, C, D
        val clubIds = listOf(1L, 2L, 3L, 4L) // A=1, B=2, C=3, D=4
        val matches = listOf(
            Match(1, 2, 2, 0), // A 2x0 B
            Match(3, 4, 1, 1), // C 1x1 D
            Match(1, 3, 1, 3)  // A 1x3 C
        )

        val accumulators = clubIds.associateWith { MutableStanding() }
        val pointsForWin = 3
        val pointsForDraw = 1
        val pointsForLoss = 0

        matches.forEach { match ->
            val home = accumulators[match.homeClubId]!!
            val away = accumulators[match.awayClubId]!!

            home.played++
            away.played++
            home.goalsFor += match.homeScore
            home.goalsAgainst += match.awayScore
            away.goalsFor += match.awayScore
            away.goalsAgainst += match.homeScore

            when {
                match.homeScore > match.awayScore -> {
                    home.wins++
                    away.losses++
                    home.points += pointsForWin
                    away.points += pointsForLoss
                }
                match.homeScore < match.awayScore -> {
                    away.wins++
                    home.losses++
                    away.points += pointsForWin
                    home.points += pointsForLoss
                }
                else -> {
                    home.draws++
                    away.draws++
                    home.points += pointsForDraw
                    away.points += pointsForDraw
                }
            }
        }

        // Expected A: J 2, V 1, D 1, GP 3, GC 3, PTS 3
        val a = accumulators[1L]!!
        assertEquals(2, a.played)
        assertEquals(1, a.wins)
        assertEquals(1, a.losses)
        assertEquals(3, a.goalsFor)
        assertEquals(3, a.goalsAgainst)
        assertEquals(3, a.points)

        // Expected B: J 1, D 1, GP 0, GC 2, PTS 0
        val b = accumulators[2L]!!
        assertEquals(1, b.played)
        assertEquals(1, b.losses)
        assertEquals(0, b.goalsFor)
        assertEquals(2, b.goalsAgainst)
        assertEquals(0, b.points)

        // Expected C: J 2, V 1, E 1, GP 4, GC 2, PTS 4
        val c = accumulators[3L]!!
        assertEquals(2, c.played)
        assertEquals(1, c.wins)
        assertEquals(1, c.draws)
        assertEquals(4, c.goalsFor)
        assertEquals(2, c.goalsAgainst)
        assertEquals(4, c.points)

        // Expected D: J 1, E 1, GP 1, GC 1, PTS 1
        val d = accumulators[4L]!!
        assertEquals(1, d.played)
        assertEquals(1, d.draws)
        assertEquals(1, d.goalsFor)
        assertEquals(1, d.goalsAgainst)
        assertEquals(1, d.points)
    }
}
