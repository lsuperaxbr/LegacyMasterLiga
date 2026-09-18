package com.example.legacymasterliga.feature.participants.data

import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.CompetitionParticipantDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.entity.CompetitionParticipantEntity
import com.example.legacymasterliga.feature.participants.domain.ParticipantClub
import com.example.legacymasterliga.feature.participants.domain.ParticipantRepository
import com.example.legacymasterliga.feature.participants.domain.SeasonOption
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class RoomParticipantRepository @Inject constructor(
    private val clubDao: ClubDao,
    private val competitionDao: CompetitionDao,
    private val seasonDao: SeasonDao,
    private val participantDao: CompetitionParticipantDao,
) : ParticipantRepository {
    override fun observeSeasonOptions(leagueId: Long): Flow<List<SeasonOption>> = combine(
        competitionDao.observeByLeague(leagueId),
        seasonDao.observeByLeague(leagueId),
    ) { competitions, seasons ->
        val names = competitions.associate { it.id to it.name }
        seasons.mapNotNull { season ->
            names[season.competitionId]?.let { competitionName ->
                SeasonOption(season.id, season.competitionId, competitionName, season.name)
            }
        }
    }

    override fun observeClubsForSeason(leagueId: Long, seasonId: Long): Flow<List<ParticipantClub>> = combine(
        clubDao.observeManagedByLeague(leagueId),
        participantDao.observeBySeason(seasonId),
    ) { clubs, participants ->
        val selectedIds = participants.filter { it.isActive }.mapTo(mutableSetOf()) { it.clubId }
        clubs.filter { it.isActive }.map { club ->
            ParticipantClub(club.id, club.name, club.crestUri, club.id in selectedIds)
        }
    }

    override suspend fun setSelected(seasonId: Long, clubId: Long, selected: Boolean) {
        require(seasonId > 0 && clubId > 0) { "Temporada ou clube inválido." }
        if (selected) {
            if (participantDao.find(seasonId, clubId) == null) {
                participantDao.insert(CompetitionParticipantEntity(seasonId = seasonId, clubId = clubId))
            }
        } else {
            participantDao.deleteBySeasonAndClub(seasonId, clubId)
        }
    }
}
