package com.example.legacymasterliga.feature.results

import org.junit.Test
import org.junit.Assert.*

class KnockoutIdempotencyTest {

    private data class Round(
        val id: Long,
        val seasonId: Long,
        val number: Int,
        val stageLabel: String
    )

    private data class Match(
        val id: Long,
        val seasonId: Long,
        val roundId: Long,
        val pairingKey: String,
        val leg: Int
    )

    @Test
    fun verifyKnockoutGenerationIdempotency() {
        val rounds = mutableListOf<Round>()
        val matches = mutableListOf<Match>()
        
        val seasonId = 1L
        val stageLabel = "Final"
        val winners = listOf(10L, 20L) // Club A vs Club B

        // Simulating the generateKnockoutStageInternal logic
        fun generate(currentRounds: MutableList<Round>, currentMatches: MutableList<Match>) {
            val existing = currentRounds.find { it.seasonId == seasonId && it.stageLabel == stageLabel }
            val roundId: Long
            val roundNum: Int
            
            if (existing != null) {
                roundId = existing.id
                roundNum = existing.number
            } else {
                roundNum = (currentRounds.maxOfOrNull { it.number } ?: 0) + 1
                roundId = roundNum.toLong()
                currentRounds.add(Round(roundId, seasonId, roundNum, stageLabel))
            }

            val pairingKey = "K-$roundNum-0"
            val existingMatch = currentMatches.find { it.seasonId == seasonId && it.pairingKey == pairingKey && it.leg == 1 }
            if (existingMatch == null) {
                currentMatches.add(Match(currentMatches.size.toLong() + 1, seasonId, roundId, pairingKey, 1))
            }
        }

        // 1. First run
        generate(rounds, matches)
        assertEquals(1, rounds.size)
        assertEquals(1, matches.size)
        val firstMatchId = matches.first().id

        // 2. Second run (simulating multiple clicks or triggers)
        generate(rounds, matches)
        assertEquals(1, rounds.size) // Should not add new round
        assertEquals(1, matches.size) // Should not add new match
        assertEquals(firstMatchId, matches.first().id) // Should be the same match
        
        // 3. Verify labels and numbers
        assertEquals("Final", rounds.first().stageLabel)
        assertEquals(1, rounds.first().number)
    }
}
