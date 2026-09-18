package com.example.legacymasterliga.feature.online.sync

enum class OnlineConflictDecision { UPLOAD, REMOTE_WINS, SCORE_CONFLICT }

object OnlineConflictPolicy {
    fun decide(
        entityType: String,
        baseRevision: Long,
        remoteRevision: Long,
        local: Map<String, Any?>,
        remote: Map<String, Any?>,
    ): OnlineConflictDecision {
        if (baseRevision == remoteRevision) return OnlineConflictDecision.UPLOAD
        if (entityType != "MATCH") return OnlineConflictDecision.REMOTE_WINS
        val scoreKeys = listOf("homeScore", "awayScore", "penaltiesHome", "penaltiesAway")
        return if (scoreKeys.any { local[it] != remote[it] }) {
            OnlineConflictDecision.SCORE_CONFLICT
        } else {
            OnlineConflictDecision.REMOTE_WINS
        }
    }
}
