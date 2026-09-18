package com.example.legacymasterliga.feature.clubprofile.data

import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.MatchDao
import com.example.legacymasterliga.core.database.dao.StandingDao
import com.example.legacymasterliga.feature.clubprofile.domain.ClubProfileHeader
import com.example.legacymasterliga.feature.clubprofile.domain.ClubProfileRepository
import com.example.legacymasterliga.feature.clubprofile.domain.ClubRecentResult
import com.example.legacymasterliga.feature.clubprofile.domain.ClubSeasonHistory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomClubProfileRepository @Inject constructor(
    private val clubDao: ClubDao,
    private val matchDao: MatchDao,
    private val standingDao: StandingDao,
) : ClubProfileRepository {
    override fun observeHeader(clubId: Long): Flow<ClubProfileHeader?> =
        clubDao.observeProfileHeader(clubId).map { row ->
            row?.let {
                ClubProfileHeader(
                    clubId = it.clubId,
                    leagueId = it.leagueId,
                    clubName = it.clubName,
                    crestUri = it.crestUri,
                    presidentUserId = it.presidentUserId,
                    presidentName = it.presidentName,
                    isActive = it.isActive,
                )
            }
        }

    override fun observeRecentResults(
        clubId: Long,
        seasonId: Long,
        limit: Int,
    ): Flow<List<ClubRecentResult>> = matchDao.observeRecentByClub(seasonId, clubId, limit).map { rows ->
        rows.map { row ->
            val isHome = row.homeClubId == clubId
            ClubRecentResult(
                matchId = row.matchId,
                roundNumber = row.roundNumber,
                opponentName = if (isHome) row.awayClubName else row.homeClubName,
                goalsFor = if (isHome) row.homeScore else row.awayScore,
                goalsAgainst = if (isHome) row.awayScore else row.homeScore,
            )
        }
    }

    override fun observeHistory(clubId: Long): Flow<List<ClubSeasonHistory>> =
        standingDao.observeHistoryByClub(clubId).map { rows ->
            rows.map { row ->
                ClubSeasonHistory(
                    seasonId = row.seasonId,
                    seasonName = row.seasonName,
                    competitionName = row.competitionName,
                    position = row.position,
                    pointsForWin = row.pointsForWin,
                    played = row.played,
                    wins = row.wins,
                    draws = row.draws,
                    losses = row.losses,
                    goalsFor = row.goalsFor,
                    goalsAgainst = row.goalsAgainst,
                    goalDifference = row.goalDifference,
                    points = row.points,
                )
            }
        }
}
