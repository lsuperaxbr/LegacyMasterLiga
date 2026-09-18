package com.example.legacymasterliga.feature.history.data

import com.example.legacymasterliga.core.database.dao.HistoryDao
import com.example.legacymasterliga.feature.history.domain.HistoricalSeason
import com.example.legacymasterliga.feature.history.domain.HistoricalStanding
import com.example.legacymasterliga.feature.history.domain.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomHistoryRepository @Inject constructor(
    private val historyDao: HistoryDao,
) : HistoryRepository {
    override fun observeSeasonsByLeague(leagueId: Long): Flow<List<HistoricalSeason>> =
        historyDao.observeSeasonsByLeague(leagueId).map { rows ->
            rows.map { row ->
                HistoricalSeason(
                    leagueId = row.leagueId,
                    leagueName = row.leagueName,
                    competitionId = row.competitionId,
                    competitionName = row.competitionName,
                    competitionType = row.competitionType,
                    seasonId = row.seasonId,
                    seasonNumber = row.seasonNumber,
                    seasonName = row.seasonName,
                    status = row.seasonStatus,
                    startedAt = row.startedAt,
                    finishedAt = row.finishedAt,
                    championClubId = row.championClubId,
                    championClubName = row.championClubName,
                    runnerUpClubId = row.runnerUpClubId,
                    runnerUpClubName = row.runnerUpClubName,
                    participantCount = row.participantCount,
                    completedMatchCount = row.completedMatchCount,
                    totalGoals = row.totalGoals,
                )
            }
        }

    override fun observeFinalTable(seasonId: Long): Flow<List<HistoricalStanding>> =
        historyDao.observeFinalTable(seasonId).map { rows ->
            rows.mapIndexed { index, row ->
                HistoricalStanding(
                    position = index + 1,
                    clubId = row.clubId,
                    clubName = row.clubName,
                    shieldUri = row.shieldUri,
                    played = row.played,
                    wins = row.wins,
                    draws = row.draws,
                    losses = row.losses,
                    goalsFor = row.goalsFor,
                    goalsAgainst = row.goalsAgainst,
                    goalDifference = row.goalDifference,
                    points = row.points,
                    performance = if (row.played == 0) 0.0 else row.points * 100.0 / (row.played * 3.0),
                )
            }
        }
}
