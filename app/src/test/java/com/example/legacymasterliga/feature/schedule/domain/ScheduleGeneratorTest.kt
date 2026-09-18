package com.example.legacymasterliga.feature.schedule.domain

import com.example.legacymasterliga.core.model.CompetitionFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleGeneratorTest {
    @Test fun single_round_generates_every_pair_once() {
        val fixtures = ScheduleGenerator.generate(listOf(1, 2, 3, 4), CompetitionFormat.SINGLE_ROUND)
        assertEquals(6, fixtures.size)
        assertEquals(3, fixtures.maxOf { it.roundNumber })
        assertEquals(6, fixtures.map { setOf(it.homeClubId, it.awayClubId) }.toSet().size)
        assertTrue(fixtures.all { it.leg == 1 && it.homeClubId != it.awayClubId })
    }

    @Test fun home_and_away_inverts_every_fixture() {
        val fixtures = ScheduleGenerator.generate(listOf(1, 2, 3, 4), CompetitionFormat.HOME_AND_AWAY)
        assertEquals(12, fixtures.size)
        val firstLeg = fixtures.filter { it.leg == 1 }
        val secondLeg = fixtures.filter { it.leg == 2 }
        assertEquals(firstLeg.size, secondLeg.size)
        firstLeg.forEach { first ->
            assertTrue(secondLeg.any { it.homeClubId == first.awayClubId && it.awayClubId == first.homeClubId })
        }
    }

    @Test fun odd_number_of_clubs_creates_byes_without_self_matches() {
        val fixtures = ScheduleGenerator.generate(listOf(1, 2, 3, 4, 5), CompetitionFormat.SINGLE_ROUND)
        assertEquals(10, fixtures.size)
        assertTrue(fixtures.all { it.homeClubId != it.awayClubId })
    }

    @Test(expected = IllegalArgumentException::class)
    fun less_than_two_distinct_clubs_is_rejected() {
        ScheduleGenerator.generate(listOf(1, 1), CompetitionFormat.SINGLE_ROUND)
    }
}
