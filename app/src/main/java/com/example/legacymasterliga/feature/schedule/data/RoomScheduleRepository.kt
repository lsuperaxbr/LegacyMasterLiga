package com.example.legacymasterliga.feature.schedule.data

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.CompetitionParticipantDao
import com.example.legacymasterliga.core.database.dao.MatchDao
import com.example.legacymasterliga.core.database.dao.RoundDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.entity.MatchEntity
import com.example.legacymasterliga.core.database.entity.RoundEntity
import com.example.legacymasterliga.feature.schedule.domain.ScheduleGenerationResult
import com.example.legacymasterliga.feature.schedule.domain.ScheduleGenerator
import com.example.legacymasterliga.feature.schedule.domain.ScheduleMatch
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRound
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class RoomScheduleRepository @Inject constructor(
    private val database: AppDatabase,
    private val competitionDao: CompetitionDao,
    private val seasonDao: SeasonDao,
    private val participantDao: CompetitionParticipantDao,
    private val roundDao: RoundDao,
    private val matchDao: MatchDao,
    private val resultsRepository: com.example.legacymasterliga.feature.results.domain.ResultsRepository,
) : ScheduleRepository {
    override fun observeSeasonOptions(leagueId: Long): Flow<List<ScheduleSeasonOption>> = combine(
        competitionDao.observeByLeague(leagueId),
        seasonDao.observeByLeague(leagueId),
    ) { competitions, seasons ->
        val competitionById = competitions.associateBy { it.id }
        seasons.mapNotNull { season ->
            competitionById[season.competitionId]?.let { competition ->
                ScheduleSeasonOption(
                    seasonId = season.id,
                    competitionId = competition.id,
                    competitionName = competition.name,
                    seasonName = season.name,
                    format = competition.format,
                    type = competition.type,
                )
            }
        }
    }

    override fun observeSchedule(seasonId: Long): Flow<List<ScheduleRound>> =
        matchDao.observeSchedule(seasonId).map { rows ->
            rows.groupBy { it.roundId }.values.map { roundRows ->
                val first = roundRows.first()
                ScheduleRound(
                    id = first.roundId,
                    number = first.roundNumber,
                    name = first.roundName,
                    stage = first.stage,
                    stageLabel = first.stageLabel,
                    matches = roundRows.map { row ->
                        ScheduleMatch(
                            id = row.matchId,
                            homeClubId = row.homeClubId,
                            homeClubName = row.homeClubName,
                            homeShieldUri = row.homeShieldUri,
                            awayClubId = row.awayClubId,
                            awayClubName = row.awayClubName,
                            awayShieldUri = row.awayShieldUri,
                            leg = row.leg,
                            stage = row.stage,
                            homeScore = row.homeScore,
                            awayScore = row.awayScore,
                            status = row.matchStatus,
                            penaltiesHome = row.penaltiesHome,
                            penaltiesAway = row.penaltiesAway,
                            winnerClubId = row.winnerClubId,
                        )
                    },
                )
            }.sortedBy { it.number }
        }

    override suspend fun generateSchedule(seasonId: Long): ScheduleGenerationResult = database.withTransaction {
        require(seasonId > 0) { "Temporada inválida." }
        val season = requireNotNull(seasonDao.findById(seasonId)) { "Temporada não encontrada." }
        val competition = requireNotNull(competitionDao.findById(season.competitionId)) { "Competição não encontrada." }
        if (matchDao.countBySeason(seasonId) > 0) {
            return@withTransaction ScheduleGenerationResult(roundsCreated = 0, matchesCreated = 0)
        }

        val clubIds = participantDao.findActiveBySeason(seasonId).map { it.clubId }
        val fixtures = ScheduleGenerator.generate(
            clubIds = clubIds,
            format = competition.format,
            groupCount = competition.groupCount,
            qualifiedPerGroup = competition.qualifiedPerGroup,
            knockoutLegs = competition.knockoutLegs
        )
        check(fixtures.isNotEmpty()) { "Não foi possível formar confrontos com os clubes inscritos." }

        val roundNumbers = fixtures.map { it.roundNumber }.distinct().sorted()
        var roundsCreated = 0
        val roundIdByNumber = roundNumbers.associateWith { number ->
            val first = fixtures.first { it.roundNumber == number }
            roundDao.findBySeasonAndNumber(seasonId, number)?.let { existing ->
                val recovered = existing.copy(
                    name = first.roundName ?: existing.name,
                    stage = first.stage,
                    stageLabel = first.stageLabel,
                    groupIndex = first.groupIndex,
                )
                if (recovered != existing) roundDao.update(recovered)
                existing.id
            } ?: run {
                val candidate = RoundEntity(
                    seasonId = seasonId,
                    number = number,
                    name = first.roundName ?: "Rodada $number",
                    stage = first.stage,
                    stageLabel = first.stageLabel,
                    groupIndex = first.groupIndex,
                )
                val insertedId = roundDao.insertIfAbsent(candidate)
                if (insertedId != -1L) {
                    roundsCreated++
                    insertedId
                } else {
                    requireNotNull(roundDao.findBySeasonAndNumber(seasonId, number)).id
                }
            }
        }

        val matches = fixtures.map { fixture ->
            val low = minOf(fixture.homeClubId, fixture.awayClubId)
            val high = maxOf(fixture.homeClubId, fixture.awayClubId)
            MatchEntity(
                seasonId = seasonId,
                roundId = requireNotNull(roundIdByNumber[fixture.roundNumber]),
                homeClubId = fixture.homeClubId,
                awayClubId = fixture.awayClubId,
                pairingKey = "$low-$high",
                leg = fixture.leg,
                stage = fixture.stage,
                groupIndex = fixture.groupIndex,
                bracketPosition = fixture.bracketPosition
            )
        }
        if (matches.isNotEmpty()) matchDao.insertAll(matches)
        resultsRepository.rebuildStandings(seasonId)
        ScheduleGenerationResult(roundsCreated, matches.size)
    }
}
