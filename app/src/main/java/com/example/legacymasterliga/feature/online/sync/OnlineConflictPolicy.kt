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
        if (entityType == "MATCH") {
            val scoreKeys = listOf("homeScore", "awayScore", "penaltiesHome", "penaltiesAway")
            return if (scoreKeys.any { local[it] != remote[it] }) {
                OnlineConflictDecision.SCORE_CONFLICT
            } else {
                OnlineConflictDecision.REMOTE_WINS
            }
        }
        if (entityType == "ARENA") {
            val remoteStatus = remote["status"] as? String
            val localStatus = local["status"] as? String
            // Remoto ainda pendente e a mudança local é diferente (ex: resolvendo
            // o duelo): é progresso real, sempre sobe.
            if (remoteStatus == "PENDING" && localStatus != remoteStatus) {
                return OnlineConflictDecision.UPLOAD
            }
            // Remoto já resolvido: não sobrescreve (outro dispositivo já
            // resolveu primeiro) — mantém o padrão de segurança já usado.
            return OnlineConflictDecision.REMOTE_WINS
        }
        return OnlineConflictDecision.REMOTE_WINS
    }
}
