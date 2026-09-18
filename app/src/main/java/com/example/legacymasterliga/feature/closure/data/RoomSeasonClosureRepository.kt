package com.example.legacymasterliga.feature.closure.data

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.*
import com.example.legacymasterliga.core.database.entity.*
import com.example.legacymasterliga.core.model.CompetitionStatus
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.closure.domain.*
import com.example.legacymasterliga.feature.news.domain.ChampionCrownedEvent
import com.example.legacymasterliga.feature.news.domain.NewsEventPublisher
import com.example.legacymasterliga.feature.schedule.domain.CupEngine
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomSeasonClosureRepository @Inject constructor(
    private val database: AppDatabase,
    private val seasonDao: SeasonDao,
    private val competitionDao: CompetitionDao,
    private val participantDao: CompetitionParticipantDao,
    private val matchDao: MatchDao,
    private val standingDao: StandingDao,
    private val clubDao: ClubDao,
    private val financialDao: FinancialDao,
    private val closureDao: SeasonClosureDao,
    private val prizeDao: PrizeDao,
    private val newsPublisher: NewsEventPublisher,
    private val auditLogger: AuditLogger,
) : SeasonClosureRepository {
    override suspend fun preview(seasonId: Long): ClosurePreview {
        val table = standingDao.findTableSnapshot(seasonId)
        val season = seasonDao.findById(seasonId)
        val competition = season?.let { competitionDao.findById(it.competitionId) }
        val finalRows = if (competition?.type == com.example.legacymasterliga.core.model.CompetitionType.CUP) {
            matchDao.findScheduledRowsBySeason(seasonId).filter { it.stageLabel == "Final" }
        } else {
            emptyList()
        }
        val cupWinnerId = calculateKnockoutWinnerFromRows(finalRows)
        val champion = cupWinnerId?.let { winnerId -> table.find { it.clubId == winnerId } }
            ?: table.firstOrNull()
        val runnerUp = if (cupWinnerId != null && finalRows.isNotEmpty()) {
            val final = finalRows.first()
            val runnerUpId = if (cupWinnerId == final.homeClubId) final.awayClubId else final.homeClubId
            table.find { it.clubId == runnerUpId }
        } else {
            table.getOrNull(1)
        }
        val total = matchDao.countValidMatchesBySeason(seasonId)
        val finished = matchDao.countFinishedValidMatchesBySeason(seasonId)

        val cardCounts = matchDao.findCardCountsBySeason(seasonId)
        val totalFine = cardCounts.sumOf { 
            it.yellowCount * (competition?.yellowCardFineCr ?: 0L) + 
            it.redCount * (competition?.redCardFineCr ?: 0L) 
        }

        return ClosurePreview(
            championClubName = champion?.clubName ?: "-",
            runnerUpClubName = runnerUp?.clubName,
            championPoints = champion?.points ?: 0,
            totalMatches = total,
            finishedMatches = finished,
            totalDisciplinaryFineCr = totalFine
        )
    }

    override fun observePrizeConfiguration(competitionId: Long): Flow<PrizeConfiguration> =
        prizeDao.observeConfiguration(competitionId).map { entity ->
            PrizeConfiguration(
                competitionId = competitionId,
                championPrizeCr = entity?.championPrizeCr ?: 0,
                runnerUpPrizeCr = entity?.runnerUpPrizeCr ?: 0,
                participationPrizeCr = entity?.participationPrizeCr ?: 0,
            )
        }

    override fun observePrizeHistory(leagueId: Long): Flow<List<PrizeHistoryItem>> =
        prizeDao.observeHistoryByLeague(leagueId).map { rows ->
            rows.map { row ->
                PrizeHistoryItem(
                    id = row.id,
                    competitionName = row.competitionName,
                    seasonName = row.seasonName,
                    clubName = row.clubName,
                    prizeType = row.prizeType,
                    amountCr = row.amountCr,
                    description = row.description,
                    awardedAt = row.awardedAt,
                )
            }
        }

    override fun observeCompetitions(leagueId: Long): Flow<List<PrizeCompetitionOption>> =
        competitionDao.observeByLeague(leagueId).map { list ->
            list.filter { it.status == CompetitionStatus.FINISHED || it.status == CompetitionStatus.ACTIVE }
                .map { PrizeCompetitionOption(it.id, it.name) }
        }

    override fun observeSeasons(competitionId: Long): Flow<List<PrizeSeasonOption>> =
        seasonDao.observeByCompetition(competitionId).map { list ->
            list.map { PrizeSeasonOption(it.id, it.competitionId, it.name) }
        }

    override suspend fun findParticipants(seasonId: Long): List<PrizeClubOption> {
        val participants = participantDao.findActiveBySeason(seasonId)
        return participants.mapNotNull { p ->
            clubDao.findById(p.clubId)?.let { PrizeClubOption(it.id, it.name) }
        }.sortedBy { it.name }
    }

    override suspend fun savePrizeConfiguration(actor: User, configuration: PrizeConfiguration) {
        require(actor.role == UserRole.ADMINISTRATOR) { "Somente Administradores podem configurar premiações." }
        require(configuration.championPrizeCr >= 0 && configuration.runnerUpPrizeCr >= 0 && configuration.participationPrizeCr >= 0) {
            "As premiações não podem ser negativas."
        }
        val competition = requireNotNull(competitionDao.findById(configuration.competitionId)) { "Competição não encontrada." }
        prizeDao.upsertConfiguration(
            CompetitionPrizeEntity(
                competitionId = configuration.competitionId,
                championPrizeCr = configuration.championPrizeCr,
                runnerUpPrizeCr = configuration.runnerUpPrizeCr,
                participationPrizeCr = configuration.participationPrizeCr,
            ),
        )
        auditLogger.log(
            category = "FINANCE", action = "PRIZE_CONFIGURATION_UPDATED", entityType = "COMPETITION",
            entityId = competition.id, leagueId = competition.leagueId,
            summary = "Premiações atualizadas em ${competition.name}",
            details = "Campeão: ${configuration.championPrizeCr} CR; vice: ${configuration.runnerUpPrizeCr} CR; participação: ${configuration.participationPrizeCr} CR.",
        )
    }

    override suspend fun awardPrize(
        actor: User,
        leagueId: Long,
        competitionId: Long,
        seasonId: Long,
        clubId: Long,
        prizeType: String,
        amount: Long,
        description: String
    ) {
        require(actor.role == UserRole.ADMINISTRATOR) { "Somente Administradores podem distribuir premiações manualmente." }
        require(amount >= 0) { "O valor da premiação não pode ser negativo." }
        
        database.withTransaction {
            if (prizeDao.isPrizeAwarded(seasonId, clubId, prizeType)) {
                throw Exception("Premiação já distribuída para este clube nesta temporada.")
            }

            val competition = competitionDao.findById(competitionId) ?: error("Competição não encontrada.")
            val season = seasonDao.findById(seasonId) ?: error("Temporada não encontrada.")
            val club = clubDao.findById(clubId) ?: error("Clube não encontrado.")
            val bank = clubDao.findBankByLeague(leagueId) ?: error("Banco da Liga não encontrado.")

            val bankBalance = financialDao.balanceOf(bank.id)
            if (bankBalance < amount) {
                throw Exception("Saldo insuficiente no Banco da Liga (${bankBalance} CR) para esta premiação (${amount} CR).")
            }

            val now = System.currentTimeMillis()
            award(
                leagueId = leagueId,
                competitionId = competitionId,
                seasonId = seasonId,
                bankId = bank.id,
                clubId = clubId,
                amount = amount,
                prizeType = prizeType,
                description = description.trim().ifBlank { "Premiação: ${competition.name} - ${season.name}" },
                now = now
            )

            auditLogger.log(
                category = "FINANCE", action = "PRIZE_AWARDED_MANUAL", entityType = "CLUB",
                entityId = clubId, leagueId = leagueId,
                summary = "Premiação de $amount CR para ${club.name}",
                details = "Competição: ${competition.name}; temporada: ${season.name}; tipo: $prizeType; obs: $description"
            )
        }
    }

    override suspend fun close(actor: User, request: CloseSeasonRequest) {
        require(actor.role == UserRole.ADMINISTRATOR) { "Somente Administradores podem encerrar temporadas." }
        database.withTransaction {
            val season = requireNotNull(seasonDao.findById(request.seasonId)) { "Temporada não encontrada." }
            check(season.status == SeasonStatus.ACTIVE) { "A temporada precisa estar ativa." }
            check(closureDao.countBySeason(season.id) == 0) { "Esta temporada já foi encerrada oficialmente." }
            val competition = requireNotNull(competitionDao.findById(season.competitionId)) { "Competição não encontrada." }
            
            val totalValid = matchDao.countValidMatchesBySeason(season.id)
            val finishedValid = matchDao.countFinishedValidMatchesBySeason(season.id)
            check(totalValid > 0) { "A temporada não possui partidas válidas." }
            check(totalValid == finishedValid) { "Finalize todas as partidas ($finishedValid/$totalValid) antes de encerrar." }
            
            val table = standingDao.findTableSnapshot(season.id)
            check(table.isNotEmpty()) { "A classificação final está vazia." }
            
            // Determinar Campeao e Vice
            val champion: com.example.legacymasterliga.core.database.model.StandingRow
            val runnerUp: com.example.legacymasterliga.core.database.model.StandingRow?
            
            if (competition.type == com.example.legacymasterliga.core.model.CompetitionType.CUP) {
                val finalMatches = matchDao.findScheduledRowsBySeason(season.id).filter { it.stageLabel == "Final" }
                val winnerId = calculateKnockoutWinnerFromRows(finalMatches)
                
                if (winnerId == null) throw Exception("O campeão da Copa ainda não foi definido na partida Final.")

                champion = requireNotNull(table.find { it.clubId == winnerId }) { "Campeão não encontrado na tabela." }
                val finalMatch = finalMatches.first()
                val finalistId = if (winnerId == finalMatch.homeClubId) finalMatch.awayClubId else finalMatch.homeClubId
                runnerUp = table.find { it.clubId == finalistId }
            } else {
                champion = table.first()
                runnerUp = table.getOrNull(1)
            }

            val config = prizeDao.findConfiguration(competition.id) ?: CompetitionPrizeEntity(competitionId = competition.id)
            val participants = participantDao.findActiveBySeason(season.id)
            
            // Validar Saldo do Banco
            val totalPrizeCr = (config.participationPrizeCr * participants.size) + config.championPrizeCr + config.runnerUpPrizeCr
            val bank = clubDao.findBankByLeague(competition.leagueId) ?: error("Banco da Liga não encontrado.")
            val bankBalance = financialDao.balanceOf(bank.id)
            
            if (bankBalance < totalPrizeCr) {
                throw Exception("Saldo insuficiente no Banco da Liga para distribuir as premiações. Necessário: $totalPrizeCr CR, Disponível: $bankBalance CR.")
            }

            val now = System.currentTimeMillis()

            // In cups, the group table is not the final podium. Persist the
            // champion and runner-up first so history/report queries by position
            // agree with the official closure record.
            val finalTable = if (competition.type == com.example.legacymasterliga.core.model.CompetitionType.CUP) {
                listOfNotNull(champion, runnerUp) + table.filter { row ->
                    row.clubId != champion.clubId && row.clubId != runnerUp?.clubId
                }
            } else {
                table
            }

            // Salvar encerramento
            val championClub = clubDao.findById(champion.clubId)
            closureDao.insertFinalStandings(finalTable.mapIndexed { index, row ->
                FinalStandingEntity(
                    seasonId = season.id, clubId = row.clubId, position = index + 1,
                    played = row.played, wins = row.wins, draws = row.draws, losses = row.losses,
                    goalsFor = row.goalsFor, goalsAgainst = row.goalsAgainst,
                    goalDifference = row.goalDifference, points = row.points, archivedAt = now,
                )
            })
            closureDao.insertClosure(
                SeasonClosureEntity(
                    seasonId = season.id,
                    championClubId = champion.clubId,
                    runnerUpClubId = runnerUp?.clubId,
                    championPrizeCr = config.championPrizeCr,
                    runnerUpPrizeCr = config.runnerUpPrizeCr,
                    participationPrizeCr = config.participationPrizeCr,
                    championPresidentUserId = championClub?.presidentUserId,
                    closedAt = now,
                ),
            )

            // Distribuir Premiações
            participants.forEach { p ->
                award(competition.leagueId, competition.id, season.id, bank.id, p.clubId, config.participationPrizeCr, "PARTICIPATION", "Participação: ${competition.name} - ${season.name}", now)
            }
            award(competition.leagueId, competition.id, season.id, bank.id, champion.clubId, config.championPrizeCr, "CHAMPION", "Campeão: ${competition.name} - ${season.name}", now)
            if (runnerUp != null) {
                award(competition.leagueId, competition.id, season.id, bank.id, runnerUp.clubId, config.runnerUpPrizeCr, "RUNNER_UP", "Vice-campeão: ${competition.name} - ${season.name}", now)
            }

            // --- Multas Disciplinares ---
            val cardCounts = matchDao.findCardCountsBySeason(season.id)
            cardCounts.forEach { count ->
                val yellowFine = count.yellowCount * competition.yellowCardFineCr
                val redFine = count.redCount * competition.redCardFineCr
                val totalFine = yellowFine + redFine

                if (totalFine > 0) {
                    val desc = "Multa disciplinar: ${count.yellowCount} cartão(ões) amarelo(s), ${count.redCount} vermelho(s)"
                    financialDao.insert(
                        FinancialTransactionEntity(
                            clubId = count.clubId,
                            amountCr = -totalFine,
                            description = desc,
                            type = "DISCIPLINARY_FINE",
                            counterpartyClubId = bank.id,
                            createdAt = now
                        )
                    )
                    financialDao.insert(
                        FinancialTransactionEntity(
                            clubId = bank.id,
                            amountCr = totalFine,
                            description = desc,
                            type = "DISCIPLINARY_FINE",
                            counterpartyClubId = count.clubId,
                            createdAt = now
                        )
                    )
                }
            }

            seasonDao.update(season.copy(status = SeasonStatus.FINISHED, finishedAt = now, updatedAt = now))
            if (request.finishCompetition) competitionDao.updateStatus(competition.id, CompetitionStatus.FINISHED, now)

            newsPublisher.publish(
                ChampionCrownedEvent(
                    leagueId = competition.leagueId,
                    competitionId = competition.id,
                    seasonId = season.id,
                    competitionName = competition.name,
                    seasonName = season.name,
                    championClubName = champion.clubName,
                    points = champion.points,
                    occurredAt = now,
                ),
            )
            auditLogger.log(
                category = "COMPETITIONS", action = "SEASON_OFFICIALLY_CLOSED", entityType = "SEASON",
                entityId = season.id, leagueId = competition.leagueId,
                summary = "${season.name} encerrada: ${champion.clubName} campeão",
                details = "Prêmios distribuídos: $totalPrizeCr CR."
            )
        }
    }

    override suspend fun closeCupAutomatically(seasonId: Long) {
        if (closureDao.countBySeason(seasonId) > 0) return
        close(
            actor = User(
                id = 0L,
                username = "cup-engine",
                displayName = "Motor da Copa",
                role = UserRole.ADMINISTRATOR,
                status = AccountStatus.ACTIVE,
            ),
            request = CloseSeasonRequest(seasonId = seasonId, finishCompetition = true),
        )
    }

    private fun calculateKnockoutWinnerFromRows(mList: List<com.example.legacymasterliga.core.database.model.ScheduledMatchRow>): Long? {
        return CupEngine.winner(mList.map { row ->
            CupEngine.LegResult(
                leg = row.leg,
                homeClubId = row.homeClubId,
                awayClubId = row.awayClubId,
                homeScore = row.homeScore,
                awayScore = row.awayScore,
                winnerClubId = row.winnerClubId,
            )
        })
    }

    private suspend fun award(
        leagueId: Long,
        competitionId: Long,
        seasonId: Long,
        bankId: Long,
        clubId: Long,
        amount: Long,
        prizeType: String,
        description: String,
        now: Long,
    ) {
        if (amount <= 0) return
        financialDao.insert(FinancialTransactionEntity(clubId = clubId, amountCr = amount, description = description, type = "PRIZE_$prizeType", counterpartyClubId = bankId, createdAt = now))
        financialDao.insert(FinancialTransactionEntity(clubId = bankId, amountCr = -amount, description = description, type = "PRIZE_$prizeType", counterpartyClubId = clubId, createdAt = now))
        prizeDao.insertHistory(
            PrizeHistoryEntity(
                leagueId = leagueId,
                competitionId = competitionId,
                seasonId = seasonId,
                clubId = clubId,
                prizeType = prizeType,
                amountCr = amount,
                description = description,
                awardedAt = now,
            ),
        )
    }
}
