package com.example.legacymasterliga.feature.halloffame.data

import com.example.legacymasterliga.core.database.dao.HallOfFameDao
import com.example.legacymasterliga.core.database.model.AggregateClubRecordRow
import com.example.legacymasterliga.core.database.model.BiggestWinRow
import com.example.legacymasterliga.core.database.model.ChampionRecordRow
import com.example.legacymasterliga.core.database.model.FinishedMatchTimelineRow
import com.example.legacymasterliga.core.database.model.SeasonClubRecordRow
import com.example.legacymasterliga.feature.halloffame.domain.BiggestWinRecord
import com.example.legacymasterliga.feature.halloffame.domain.ClubRecord
import com.example.legacymasterliga.feature.halloffame.domain.HallOfFameRepository
import com.example.legacymasterliga.feature.halloffame.domain.HallOfFameSnapshot
import com.example.legacymasterliga.feature.halloffame.domain.UnbeatenStreakRecord
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private data class StandingsRecords(
    val champion: ChampionRecordRow?,
    val wins: AggregateClubRecordRow?,
    val attack: SeasonClubRecordRow?,
    val defense: SeasonClubRecordRow?,
)

private data class EventRecords(
    val biggestWin: BiggestWinRow?,
    val matches: List<FinishedMatchTimelineRow>,
    val crMovement: AggregateClubRecordRow?,
)

private data class StreakKey(val seasonId: Long, val clubId: Long)

private data class RunningStreak(
    var count: Int = 0,
    var clubName: String = "",
    var crestUri: String? = null,
    var competitionName: String = "",
    var seasonName: String = "",
)

@Singleton
class RoomHallOfFameRepository @Inject constructor(
    private val dao: HallOfFameDao,
) : HallOfFameRepository {
    override fun observeByLeague(leagueId: Long): Flow<HallOfFameSnapshot> {
        val standingsRecords = combine(
            dao.observeTopChampion(leagueId),
            dao.observeMostWins(leagueId),
            dao.observeBestAttack(leagueId),
            dao.observeBestDefense(leagueId),
        ) { champion, wins, attack, defense ->
            StandingsRecords(champion, wins, attack, defense)
        }
        val eventRecords = combine(
            dao.observeBiggestWin(leagueId),
            dao.observeFinishedMatches(leagueId),
            dao.observeMostCrMovement(leagueId),
        ) { biggestWin, matches, crMovement ->
            EventRecords(biggestWin, matches, crMovement)
        }

        return combine(standingsRecords, eventRecords) { standing, events ->
            HallOfFameSnapshot(
                topChampion = standing.champion?.let {
                    ClubRecord(it.clubId, it.clubName, it.crestUri, it.titles.toLong(), "${it.titles} título(s)")
                },
                mostWins = standing.wins?.let {
                    ClubRecord(it.clubId, it.clubName, it.crestUri, it.value, "${it.value} vitórias")
                },
                bestAttack = standing.attack?.let {
                    ClubRecord(
                        clubId = it.clubId,
                        clubName = it.clubName,
                        crestUri = it.crestUri,
                        value = it.value.toLong(),
                        detail = "${it.value} gols • ${it.competitionName} • ${it.seasonName}",
                        seasonId = it.seasonId,
                    )
                },
                bestDefense = standing.defense?.let {
                    ClubRecord(
                        clubId = it.clubId,
                        clubName = it.clubName,
                        crestUri = it.crestUri,
                        value = it.value.toLong(),
                        detail = "${it.value} gols sofridos • ${it.competitionName} • ${it.seasonName}",
                        seasonId = it.seasonId,
                    )
                },
                biggestWin = events.biggestWin?.let {
                    val homeWon = it.homeScore > it.awayScore
                    BiggestWinRecord(
                        matchId = it.matchId,
                        seasonId = it.seasonId,
                        competitionName = it.competitionName,
                        seasonName = it.seasonName,
                        roundNumber = it.roundNumber,
                        winnerClubId = if (homeWon) it.homeClubId else it.awayClubId,
                        winnerClubName = if (homeWon) it.homeClubName else it.awayClubName,
                        loserClubId = if (homeWon) it.awayClubId else it.homeClubId,
                        loserClubName = if (homeWon) it.awayClubName else it.homeClubName,
                        score = "${it.homeClubName} ${it.homeScore} x ${it.awayScore} ${it.awayClubName}",
                        goalDifference = it.goalDifference,
                    )
                },
                longestUnbeaten = calculateLongestUnbeaten(events.matches),
                mostCrMovement = events.crMovement?.takeIf { it.value > 0 }?.let {
                    ClubRecord(it.clubId, it.clubName, it.crestUri, it.value, "${it.value} CR movimentados")
                },
            )
        }
    }

    private fun calculateLongestUnbeaten(matches: List<FinishedMatchTimelineRow>): UnbeatenStreakRecord? {
        val running = mutableMapOf<StreakKey, RunningStreak>()
        var best: UnbeatenStreakRecord? = null

        matches.forEach { match ->
            val homeLost = match.homeScore < match.awayScore
            val awayLost = match.awayScore < match.homeScore

            fun update(
                clubId: Long,
                clubName: String,
                crestUri: String?,
                lost: Boolean,
            ) {
                val key = StreakKey(match.seasonId, clubId)
                val current = running.getOrPut(key) { RunningStreak() }
                current.clubName = clubName
                current.crestUri = crestUri
                current.competitionName = match.competitionName
                current.seasonName = match.seasonName
                current.count = if (lost) 0 else current.count + 1

                if (current.count > (best?.matches ?: 0)) {
                    best = UnbeatenStreakRecord(
                        clubId = clubId,
                        clubName = clubName,
                        crestUri = crestUri,
                        matches = current.count,
                        competitionName = match.competitionName,
                        seasonId = match.seasonId,
                        seasonName = match.seasonName,
                    )
                }
            }

            update(match.homeClubId, match.homeClubName, match.homeCrestUri, homeLost)
            update(match.awayClubId, match.awayClubName, match.awayCrestUri, awayLost)
        }
        return best
    }
}
