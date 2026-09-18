package com.example.legacymasterliga.feature.schedule.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CupEngineTest {
    @Test
    fun `opening round uses the official phase for supported bracket sizes`() {
        val expected = mapOf(
            2 to CupEngine.FINAL,
            4 to CupEngine.SEMI_FINALS,
            8 to CupEngine.QUARTER_FINALS,
            16 to CupEngine.ROUND_OF_16,
        )
        expected.forEach { (clubCount, label) ->
            val fixtures = CupEngine.openingRound((1L..clubCount.toLong()).toList(), legs = 1)
            assertEquals(clubCount / 2, fixtures.size)
            assertEquals(setOf(label), fixtures.map { it.stageLabel }.toSet())
        }
    }

    @Test
    fun `ten clubs create a preliminary round with six byes`() {
        val fixtures = CupEngine.openingRound((1L..10L).toList(), legs = 1)
        assertEquals(2, fixtures.size)
        assertEquals(setOf(7L, 8L, 9L, 10L), fixtures.flatMap { listOf(it.homeClubId, it.awayClubId) }.toSet())
        assertEquals(setOf(CupEngine.PRELIMINARY), fixtures.map { it.stageLabel }.toSet())
    }

    @Test
    fun `two leg opening creates mirrored fixtures without changing phase`() {
        val fixtures = CupEngine.openingRound((1L..8L).toList(), legs = 2)
        assertEquals(8, fixtures.size)
        assertEquals(setOf(1, 2), fixtures.map { it.leg }.toSet())
        assertEquals(setOf(CupEngine.QUARTER_FINALS), fixtures.map { it.stageLabel }.toSet())
        fixtures.filter { it.leg == 1 }.forEach { first ->
            val second = fixtures.single { it.bracketPosition == first.bracketPosition && it.leg == 2 }
            assertEquals(first.homeClubId, second.awayClubId)
            assertEquals(first.awayClubId, second.homeClubId)
        }
    }

    @Test
    fun `aggregate tie uses winner stored on return leg`() {
        val winner = CupEngine.winner(
            listOf(
                CupEngine.LegResult(1, 10L, 20L, 1, 0, null),
                CupEngine.LegResult(2, 20L, 10L, 1, 0, 20L),
            ),
        )
        assertEquals(20L, winner)
    }

    @Test
    fun `incomplete two leg tie has no winner`() {
        assertNull(
            CupEngine.winner(
                listOf(
                    CupEngine.LegResult(1, 10L, 20L, 1, 0, null),
                    CupEngine.LegResult(2, 20L, 10L, 1, 0, null),
                ),
            ),
        )
    }
}
