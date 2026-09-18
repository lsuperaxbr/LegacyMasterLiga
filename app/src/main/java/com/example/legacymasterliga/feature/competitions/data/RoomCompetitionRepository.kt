package com.example.legacymasterliga.feature.competitions.data

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import com.example.legacymasterliga.core.database.entity.CompetitionParticipantEntity
import com.example.legacymasterliga.core.database.entity.SeasonEntity
import com.example.legacymasterliga.core.model.CompetitionStatus
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionSummary
import com.example.legacymasterliga.feature.competitions.domain.CreateCompetitionRequest
import com.example.legacymasterliga.feature.competitions.domain.SeasonSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class RoomCompetitionRepository @Inject constructor(
    private val database: AppDatabase,
    private val competitionDao: CompetitionDao,
    private val seasonDao: SeasonDao,
    private val resultsRepository: com.example.legacymasterliga.feature.results.domain.ResultsRepository,
    private val auditLogger: AuditLogger,
) : CompetitionRepository {
    override fun observeByLeague(leagueId: Long): Flow<List<CompetitionSummary>> =
        combine(
            competitionDao.observeByLeague(leagueId),
            seasonDao.observeByLeague(leagueId),
        ) { competitions, seasons ->
            competitions.map { competition ->
                CompetitionSummary(
                    id = competition.id,
                    leagueId = competition.leagueId,
                    name = competition.name,
                    type = competition.type,
                    format = competition.format,
                    status = competition.status,
                    seasons = seasons
                        .filter { it.competitionId == competition.id }
                        .map { season ->
                            SeasonSummary(
                                id = season.id,
                                competitionId = season.competitionId,
                                number = season.number,
                                name = season.name,
                                status = season.status,
                            )
                        },
                )
            }
        }

    override suspend fun createCompetition(request: CreateCompetitionRequest): Long {
        val cleanName = request.name.trim()
        val cleanSeasonName = request.firstSeasonName.trim()
        require(request.leagueId > 0) { "Selecione uma liga válida." }
        require(cleanName.length >= 3) { "O nome da competição deve ter pelo menos 3 caracteres." }
        require(cleanSeasonName.isNotBlank()) { "Informe o nome da primeira temporada." }
        val effectiveFormat = if (request.type == CompetitionType.CUP) CompetitionFormat.KNOCKOUT else request.format
        val effectiveGroupCount = if (request.type == CompetitionType.CUP) null else request.groupCount
        val effectiveQualifiedPerGroup = if (request.type == CompetitionType.CUP) null else request.qualifiedPerGroup

        return database.withTransaction {
            check(competitionDao.findByLeagueAndName(request.leagueId, cleanName) == null) {
                "Já existe uma competição com esse nome nesta liga."
            }
            val competitionId = competitionDao.insert(
                CompetitionEntity(
                    leagueId = request.leagueId,
                    name = cleanName,
                    type = request.type,
                    format = effectiveFormat,
                    status = CompetitionStatus.ACTIVE,
                    groupCount = effectiveGroupCount,
                    qualifiedPerGroup = effectiveQualifiedPerGroup,
                    knockoutLegs = request.knockoutLegs,
                ),
            )
            val seasonId = seasonDao.insert(
                SeasonEntity(
                    competitionId = competitionId,
                    number = 1,
                    name = cleanSeasonName,
                    status = SeasonStatus.ACTIVE,
                    startedAt = System.currentTimeMillis(),
                ),
            )
            
            if (request.initialParticipantIds.isNotEmpty()) {
                val participants = request.initialParticipantIds.mapIndexed { index, clubId ->
                    CompetitionParticipantEntity(
                        seasonId = seasonId,
                        clubId = clubId,
                        seed = index + 1,
                        isActive = true,
                    )
                }
                database.competitionParticipantDao().insertAll(participants)
                resultsRepository.rebuildStandings(seasonId)
            }

            auditLogger.log("COMPETITIONS", "COMPETITION_CREATED", "COMPETITION", competitionId, request.leagueId,
                "Competição $cleanName criada", "Primeira temporada: $cleanSeasonName (#$seasonId); participantes: ${request.initialParticipantIds.size}; formato: $effectiveFormat.")
            competitionId
        }
    }

    override suspend fun createNextSeason(competitionId: Long, name: String, participantIds: List<Long>): Long {
        require(competitionId > 0) { "Competição inválida." }
        return database.withTransaction {
            val competition = checkNotNull(competitionDao.findById(competitionId)) {
                "Competição não encontrada."
            }
            check(competition.status != CompetitionStatus.ARCHIVED) {
                "Não é possível criar temporada em uma competição arquivada."
            }
            check(competition.type != CompetitionType.CUP || competition.format == CompetitionFormat.KNOCKOUT) {
                "Copas antigas com fase de grupos são preservadas apenas no histórico. Crie uma nova Copa mata-mata."
            }
            check(seasonDao.findByStatus(competitionId, SeasonStatus.ACTIVE) == null) {
                "Encerre oficialmente a temporada ativa antes de criar a próxima."
            }
            val nextNumber = (seasonDao.findHighestNumber(competitionId) ?: 0) + 1
            val seasonId = seasonDao.insert(
                SeasonEntity(
                    competitionId = competitionId,
                    number = nextNumber,
                    name = name.trim().ifBlank { "Temporada $nextNumber" },
                    status = SeasonStatus.ACTIVE,
                    startedAt = System.currentTimeMillis(),
                ),
            )
            
            if (participantIds.isNotEmpty()) {
                val participants = participantIds.mapIndexed { index, clubId ->
                    CompetitionParticipantEntity(
                        seasonId = seasonId,
                        clubId = clubId,
                        seed = index + 1,
                        isActive = true,
                    )
                }
                database.competitionParticipantDao().insertAll(participants)
                resultsRepository.rebuildStandings(seasonId)
            }

            auditLogger.log("COMPETITIONS", "SEASON_CREATED", "SEASON", seasonId, competition.leagueId,
                "Temporada $nextNumber criada em ${competition.name}", "Participantes: ${participantIds.size}")
            seasonId
        }
    }

    override suspend fun delete(competitionId: Long) {
        competitionDao.deleteById(competitionId)
    }
}
