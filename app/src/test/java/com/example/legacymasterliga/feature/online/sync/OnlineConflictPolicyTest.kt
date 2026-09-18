package com.example.legacymasterliga.feature.online.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class OnlineConflictPolicyTest {
    @Test
    fun `matching revision permits idempotent upload`() {
        assertEquals(
            OnlineConflictDecision.UPLOAD,
            OnlineConflictPolicy.decide("MATCH", 4, 4, mapOf("homeScore" to 2), mapOf("homeScore" to 1)),
        )
    }

    @Test
    fun `concurrent score is never silently overwritten`() {
        assertEquals(
            OnlineConflictDecision.SCORE_CONFLICT,
            OnlineConflictPolicy.decide(
                "MATCH", 3, 4,
                mapOf("homeScore" to 2, "awayScore" to 0, "penaltiesHome" to null, "penaltiesAway" to null),
                mapOf("homeScore" to 1, "awayScore" to 0, "penaltiesHome" to null, "penaltiesAway" to null),
            ),
        )
    }

    @Test
    fun `newer remote metadata wins`() {
        assertEquals(
            OnlineConflictDecision.REMOTE_WINS,
            OnlineConflictPolicy.decide("SEASON", 1, 2, emptyMap(), emptyMap()),
        )
    }
}
