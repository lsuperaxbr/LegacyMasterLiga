package com.example.legacymasterliga.feature.news.domain

sealed interface NewsEvent {
    val leagueId: Long
    val competitionId: Long?
    val seasonId: Long?
    val sourceId: Long
    val dedupKey: String
    val occurredAt: Long
}

data class TransferCompletedEvent(
    override val leagueId: Long,
    override val sourceId: Long,
    val playerName: String,
    val originClubName: String,
    val destinationClubName: String,
    val valueCr: Long,
    override val occurredAt: Long,
    val type: String = "TRANSFER",
) : NewsEvent {
    override val competitionId: Long? = null
    override val seasonId: Long? = null
    override val dedupKey: String = "TRANSFER:$sourceId"
}

data class SwapCompletedEvent(
    override val leagueId: Long,
    override val sourceId: Long, // Use the swapId's hash or first transfer ID
    val playerA: String,
    val clubAName: String,
    val playerB: String,
    val clubBName: String,
    val compensationCr: Long,
    val payerClubName: String?,
    override val occurredAt: Long,
) : NewsEvent {
    override val competitionId: Long? = null
    override val seasonId: Long? = null
    override val dedupKey: String = "SWAP:$sourceId"
}

data class MatchFinishedEvent(
    override val leagueId: Long,
    override val competitionId: Long,
    override val seasonId: Long,
    override val sourceId: Long,
    val competitionName: String,
    val seasonName: String,
    val homeClubName: String,
    val awayClubName: String,
    val homeScore: Int,
    val awayScore: Int,
    override val occurredAt: Long,
) : NewsEvent {
    override val dedupKey: String = "MATCH:$sourceId"
}

data class LeadershipChangedEvent(
    override val leagueId: Long,
    override val competitionId: Long,
    override val seasonId: Long,
    override val sourceId: Long,
    val competitionName: String,
    val seasonName: String,
    val previousLeaderName: String,
    val newLeaderName: String,
    override val occurredAt: Long,
) : NewsEvent {
    override val dedupKey: String = "LEADERSHIP:$sourceId"
}

data class ChampionCrownedEvent(
    override val leagueId: Long,
    override val competitionId: Long,
    override val seasonId: Long,
    override val sourceId: Long = seasonId,
    val competitionName: String,
    val seasonName: String,
    val championClubName: String,
    val points: Int,
    override val occurredAt: Long,
) : NewsEvent {
    override val dedupKey: String = "CHAMPION:$seasonId"
}

interface NewsEventPublisher {
    suspend fun publish(event: NewsEvent)
    suspend fun remove(dedupKey: String)
}
