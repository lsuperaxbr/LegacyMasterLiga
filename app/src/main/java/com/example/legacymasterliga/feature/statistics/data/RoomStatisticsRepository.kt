package com.example.legacymasterliga.feature.statistics.data

import com.example.legacymasterliga.core.database.dao.StatisticsDao
import com.example.legacymasterliga.feature.statistics.domain.ClubStatisticsRanking
import com.example.legacymasterliga.feature.statistics.domain.LeagueStatisticsOverview
import com.example.legacymasterliga.feature.statistics.domain.StatisticsRepository
import com.example.legacymasterliga.feature.statistics.domain.StatisticsSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class RoomStatisticsRepository @Inject constructor(
    private val dao: StatisticsDao,
) : StatisticsRepository {
    override fun observe(
        leagueId: Long,
        competitionId: Long?,
        seasonId: Long?,
    ): Flow<StatisticsSnapshot> = combine(
        dao.observeOverview(leagueId, competitionId, seasonId),
        dao.observeClubRanking(leagueId, competitionId, seasonId),
    ) { overview, clubs ->
        StatisticsSnapshot(
            overview = LeagueStatisticsOverview(
                finishedMatches = overview.finishedMatches,
                totalGoals = overview.totalGoals,
                averageGoals = overview.averageGoals,
                totalCrMoved = overview.totalCrMoved,
                activeClubs = overview.activeClubs,
                seasonsCount = overview.seasonsCount,
            ),
            clubs = clubs.map {
                ClubStatisticsRanking(
                    clubId = it.clubId,
                    clubName = it.clubName,
                    crestUri = it.crestUri,
                    played = it.played,
                    wins = it.wins,
                    draws = it.draws,
                    losses = it.losses,
                    goalsFor = it.goalsFor,
                    goalsAgainst = it.goalsAgainst,
                    goalDifference = it.goalDifference,
                    points = it.points,
                    averagePoints = it.averagePoints,
                    crMoved = it.crMoved,
                )
            },
        )
    }
}
