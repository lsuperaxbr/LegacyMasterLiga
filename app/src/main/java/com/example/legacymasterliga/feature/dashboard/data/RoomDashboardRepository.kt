package com.example.legacymasterliga.feature.dashboard.data

import android.os.SystemClock
import com.example.legacymasterliga.core.database.dao.DashboardDao
import com.example.legacymasterliga.core.error.AppErrorReporter
import com.example.legacymasterliga.core.performance.PerformanceMonitor
import com.example.legacymasterliga.feature.dashboard.domain.DashboardClubBalance
import com.example.legacymasterliga.feature.dashboard.domain.DashboardLeader
import com.example.legacymasterliga.feature.dashboard.domain.DashboardNews
import com.example.legacymasterliga.feature.dashboard.domain.DashboardRepository
import com.example.legacymasterliga.feature.dashboard.domain.DashboardResult
import com.example.legacymasterliga.feature.dashboard.domain.DashboardRound
import com.example.legacymasterliga.feature.dashboard.domain.DashboardSummary
import com.example.legacymasterliga.feature.dashboard.domain.DashboardTransfer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@Singleton
class RoomDashboardRepository @Inject constructor(
    private val dashboardDao: DashboardDao,
    private val performanceMonitor: PerformanceMonitor,
    private val errorReporter: AppErrorReporter,
) : DashboardRepository {
    override fun observeSummary(userId: Long?): Flow<DashboardSummary> =
        dashboardDao.observeContext()
            .distinctUntilChanged()
            .flatMapLatest { context ->
                if (context == null) return@flatMapLatest flowOf(DashboardSummary())

                val organization = combine(
                    dashboardDao.observeCompetitionCount(context.leagueId).distinctUntilChanged(),
                    dashboardDao.observeActiveClubCount(context.leagueId).distinctUntilChanged(),
                    dashboardDao.observeTotalBalance(context.leagueId).distinctUntilChanged(),
                ) { competitions, clubs, totalCr -> Triple(competitions, clubs, totalCr) }

                val activity = combine(
                    dashboardDao.observeTransferCount(context.leagueId).distinctUntilChanged(),
                    dashboardDao.observeNewsCount(context.leagueId).distinctUntilChanged(),
                    dashboardDao.observeLatestTransfers(context.leagueId).distinctUntilChanged(),
                    dashboardDao.observeLatestNews(context.leagueId).distinctUntilChanged(),
                ) { transferCount, newsCount, transfers, news ->
                    ActivityData(transferCount, newsCount, transfers, news)
                }

                val seasonData = context.seasonId?.let { seasonId ->
                    combine(
                        dashboardDao.observeLeader(seasonId).distinctUntilChanged(),
                        dashboardDao.observeNextRound(seasonId).distinctUntilChanged(),
                        dashboardDao.observeRecentResults(seasonId).distinctUntilChanged(),
                    ) { leader, round, results -> SeasonData(leader, round, results) }
                } ?: flowOf(SeasonData())

                val clubBalances = userId?.let(dashboardDao::observePresidentClubBalances)?.distinctUntilChanged()
                    ?: flowOf(emptyList())

                combine(organization, activity, seasonData, clubBalances) { org, events, season, balances ->
                    val started = SystemClock.elapsedRealtime()
                    DashboardSummary(
                        leagueId = context.leagueId,
                        leagueName = context.leagueName,
                        competitionId = context.competitionId,
                        competitionName = context.competitionName,
                        seasonId = context.seasonId,
                        seasonName = context.seasonName,
                        competitionCount = org.first,
                        clubCount = org.second,
                        totalCr = org.third,
                        transferCount = events.transferCount,
                        newsCount = events.newsCount,
                        leader = season.leader?.let {
                            DashboardLeader(it.clubId, it.clubName, it.crestUri, it.points, it.played)
                        },
                        nextRound = season.round?.let {
                            DashboardRound(it.seasonId, it.roundId, it.roundNumber, it.roundName, it.pendingMatches, it.totalMatches)
                        },
                        recentResults = season.results.map {
                            DashboardResult(it.matchId, it.roundNumber, it.homeClubName, it.awayClubName, it.homeScore, it.awayScore)
                        },
                        latestTransfers = events.transfers.map {
                            DashboardTransfer(it.transferId, it.playerName, it.originClubName, it.destinationClubName, it.valueCr, it.createdAt)
                        },
                        latestNews = events.news.map {
                            DashboardNews(it.newsId, it.title, it.category, it.publishedAt)
                        },
                        presidentClubBalances = balances.map {
                            DashboardClubBalance(it.clubId, it.clubName, it.balanceCr)
                        },
                    ).also {
                        performanceMonitor.record("dashboard.compose_summary", SystemClock.elapsedRealtime() - started)
                    }
                }
            }
            .distinctUntilChanged()
            .conflate()
            .catch { throwable ->
                errorReporter.report("Dashboard", throwable, "Não foi possível carregar o resumo da liga.")
                emit(DashboardSummary())
            }

    private data class ActivityData(
        val transferCount: Int,
        val newsCount: Int,
        val transfers: List<com.example.legacymasterliga.core.database.model.DashboardTransferRow>,
        val news: List<com.example.legacymasterliga.core.database.model.DashboardNewsRow>,
    )

    private data class SeasonData(
        val leader: com.example.legacymasterliga.core.database.model.DashboardLeaderRow? = null,
        val round: com.example.legacymasterliga.core.database.model.DashboardRoundRow? = null,
        val results: List<com.example.legacymasterliga.core.database.model.DashboardResultRow> = emptyList(),
    )
}
