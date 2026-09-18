package com.example.legacymasterliga.feature.results.data

import android.util.Log
import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.CompetitionParticipantDao
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.MatchDao
import com.example.legacymasterliga.core.database.dao.RoundDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.dao.StandingDao
import com.example.legacymasterliga.core.database.entity.StandingEntity
import com.example.legacymasterliga.core.model.RoundStatus
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.closure.domain.SeasonClosureRepository
import com.example.legacymasterliga.feature.results.domain.ResultsRepository
import com.example.legacymasterliga.feature.results.domain.Standing
import com.example.legacymasterliga.feature.schedule.domain.CupEngine
import com.example.legacymasterliga.feature.news.domain.MatchFinishedEvent
import com.example.legacymasterliga.feature.news.domain.NewsEventPublisher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomResultsRepository @Inject constructor(
    private val database: AppDatabase,
    private val matchDao: MatchDao,
    private val roundDao: RoundDao,
    private val standingDao: StandingDao,
    private val seasonDao: SeasonDao,
    private val competitionDao: CompetitionDao,
    private val participantDao: CompetitionParticipantDao,
    private val clubDao: ClubDao,
    private val newsPublisher: NewsEventPublisher,
    private val auditLogger: AuditLogger,
    private val closureRepository: SeasonClosureRepository,
    private val goalEventDao: com.example.legacymasterliga.core.database.dao.GoalEventDao,
) : ResultsRepository {
    override fun observeStandings(seasonId: Long): Flow<List<Standing>> =
        standingDao.observeTable(seasonId).map { rows ->
            var currentGroup: Int? = -2 // Placeholder
            var positionInGroup = 0
            
            rows.map { row ->
                if (row.groupIndex != currentGroup) {
                    currentGroup = row.groupIndex
                    positionInGroup = 1
                } else {
                    positionInGroup++
                }
                
                Standing(
                    position = positionInGroup,
                    clubId = row.clubId,
                    clubName = row.clubName,
                    shieldUri = row.shieldUri,
                    groupIndex = row.groupIndex,
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

    override fun observePodium(seasonId: Long): Flow<com.example.legacymasterliga.feature.results.domain.PodiumSummary?> =
        matchDao.observeSchedule(seasonId).map { rows ->
            val finishedMatches = rows.filter { it.matchStatus == com.example.legacymasterliga.core.model.MatchStatus.FINISHED }
            val finalMatches = finishedMatches.filter { it.stageLabel == "Final" }
            
            if (finalMatches.isEmpty()) return@map null
            
            val winnerId = calculateAggWinnerIdFromRows(finalMatches) ?: return@map null
            
            val f1 = finalMatches.first()
            val runnerUpId = if (winnerId == f1.homeClubId) f1.awayClubId else f1.homeClubId
            
            val championName = if (winnerId == f1.homeClubId) f1.homeClubName else f1.awayClubName
            val championShield = if (winnerId == f1.homeClubId) f1.homeShieldUri else f1.awayShieldUri
            val runnerUpName = if (winnerId == f1.homeClubId) f1.awayClubName else f1.homeClubName
            val runnerUpShield = if (winnerId == f1.homeClubId) f1.awayShieldUri else f1.homeShieldUri

            val semis = finishedMatches.filter { it.stageLabel == "Semifinais" || it.stageLabel == "Semifinal" }
            val semiNames = semis.flatMap { listOf(it.homeClubName, it.awayClubName) }
                .distinct()
                .filter { it != championName && it != runnerUpName }

            com.example.legacymasterliga.feature.results.domain.PodiumSummary(
                championName = championName,
                championShield = championShield,
                runnerUpName = runnerUpName,
                runnerUpShield = runnerUpShield,
                semifinalists = semiNames
            )
        }

    private fun calculateAggWinnerIdFromRows(mList: List<com.example.legacymasterliga.core.database.model.ScheduledMatchRow>): Long? {
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

    override suspend fun saveResult(
        matchId: Long, 
        homeScore: Int, 
        awayScore: Int,
        penaltiesHome: Int?,
        penaltiesAway: Int?,
        winnerClubId: Long?,
        goals: List<com.example.legacymasterliga.feature.results.domain.GoalRecord>,
        homeYellowCards: Int,
        homeRedCards: Int,
        awayYellowCards: Int,
        awayRedCards: Int
    ) {
        require(homeScore >= 0 && awayScore >= 0) { "O placar não pode ser negativo." }
        require(homeScore <= 99 && awayScore <= 99) { "O placar deve ser menor que 100." }

        var completedCupSeasonId: Long? = null
        database.withTransaction {
            val match = requireNotNull(matchDao.findById(matchId)) { "Partida não encontrada." }
            val season = requireNotNull(seasonDao.findById(match.seasonId)) { "Temporada não encontrada." }
            check(season.status == SeasonStatus.ACTIVE) { "A temporada está encerrada." }
            val competition = requireNotNull(competitionDao.findById(season.competitionId)) { "Competição não encontrada." }

            // Rotina de limpeza de duplicatas (BUG-RC1-003)
            val duplicates = matchDao.findDuplicateMatchIds(match.seasonId)
            if (duplicates.isNotEmpty()) {
                matchDao.deleteByIds(duplicates)
                auditLogger.log("SYSTEM", "CLEANUP", "SEASON", season.id, competition.leagueId, 
                    "${duplicates.size} partidas duplicadas removidas.", null)
            }

            val isKnockout = match.stage == "KNOCKOUT"
            
            // Determinar vencedor (Simplificado BUG-RC1-005: Ida vs Volta)
            var finalWinnerId = winnerClubId
            var reason: String? = null

            if (isKnockout) {
                val hasReturnLeg = competition.knockoutLegs == 2
                val isLeg1 = hasReturnLeg && match.leg == 1
                val isLeg2 = hasReturnLeg && match.leg == 2
                
                if (isLeg1) {
                    finalWinnerId = null // Ida nunca define vencedor, mesmo em empate
                } else if (isLeg2) {
                    val hTotal: Int
                    val aTotal: Int
                    val otherMatch = matchDao.findBySeason(match.seasonId).find { it.pairingKey == match.pairingKey && it.leg == 1 && it.id != match.id }
                    hTotal = homeScore + (otherMatch?.awayScore ?: 0)
                    aTotal = awayScore + (otherMatch?.homeScore ?: 0)

                    if (hTotal > aTotal) {
                        finalWinnerId = match.homeClubId
                    } else if (aTotal > hTotal) {
                        finalWinnerId = match.awayClubId
                    } else {
                        // Empate no agregado ou unico. winnerClubId DEVE vir da UI (MANUAL_TIEBREAK)
                        if (winnerClubId != null) {
                            finalWinnerId = winnerClubId
                            reason = "MANUAL_TIEBREAK"
                        } else {
                            throw Exception("Empate detectado. Selecione o clube que avança.")
                        }
                    }
                } else {
                    // Jogo único também usa leg=1; ele deve definir o vencedor imediatamente.
                    finalWinnerId = when {
                        homeScore > awayScore -> match.homeClubId
                        awayScore > homeScore -> match.awayClubId
                        winnerClubId != null -> winnerClubId.also { reason = "MANUAL_TIEBREAK" }
                        else -> throw Exception("Empate detectado. Selecione o clube que avança.")
                    }
                }
            } else {
                finalWinnerId = null // Ligas nao usam vencedor manual
            }

            // Atualizar partida (Pênaltis desativados nesta versão)
            matchDao.updateMatchFull(matchId, homeScore, awayScore, null, null, finalWinnerId, reason, com.example.legacymasterliga.core.model.MatchStatus.FINISHED, System.currentTimeMillis())
            matchDao.updateCards(matchId, homeYellowCards, homeRedCards, awayYellowCards, awayRedCards)

            // --- Gerenciar Goleadores ---
            goalEventDao.deleteByMatch(matchId)
            if (goals.isNotEmpty()) {
                val now = System.currentTimeMillis()
                goalEventDao.insertAll(goals.map { g ->
                    com.example.legacymasterliga.core.database.entity.GoalEventEntity(
                        matchId = matchId,
                        playerId = g.playerId,
                        scoringClubId = g.scoringClubId,
                        isOwnGoal = g.isOwnGoal,
                        createdAt = now
                    )
                })
            }

            val homeClub = requireNotNull(clubDao.findById(match.homeClubId))
            val awayClub = requireNotNull(clubDao.findById(match.awayClubId))
            val now = System.currentTimeMillis()
            
            newsPublisher.publish(
                MatchFinishedEvent(
                    leagueId = competition.leagueId,
                    competitionId = competition.id,
                    seasonId = season.id,
                    sourceId = match.id,
                    competitionName = competition.name,
                    seasonName = season.name,
                    homeClubName = homeClub.name,
                    awayClubName = awayClub.name,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    occurredAt = now,
                ),
            )
            
            auditLogger.log("RESULTS", "RESULT_SAVED", "MATCH", match.id, competition.leagueId, 
                "Placar: ${homeClub.name} $homeScore x $awayScore ${awayClub.name}${if (finalWinnerId != null) " (Vencedor: ${if (finalWinnerId == match.homeClubId) homeClub.name else awayClub.name}${if (reason != null) " [$reason]" else ""})" else ""}", null)

            if (isKnockout) {
                val cupFinished = checkAndAdvanceKnockoutInternal(season.id, competition, match.roundId)
                if (cupFinished && competition.type == com.example.legacymasterliga.core.model.CompetitionType.CUP) {
                    completedCupSeasonId = season.id
                }
            }
            
            // Reconstrução obrigatória
            rebuildStandings(season.id)
        }
        completedCupSeasonId?.let { closureRepository.closeCupAutomatically(it) }
    }

    private suspend fun checkAndAdvanceKnockoutInternal(
        seasonId: Long,
        competition: com.example.legacymasterliga.core.database.entity.CompetitionEntity,
        triggeringRoundId: Long,
    ): Boolean {
        val triggeringRound = roundDao.findById(triggeringRoundId) ?: return false
        val currentPhase = CupEngine.canonicalPhase(triggeringRound.stageLabel, triggeringRound.name)
        val allMatches = matchDao.findBySeason(seasonId)
        val roundsById = mutableMapOf<Long, com.example.legacymasterliga.core.database.entity.RoundEntity>()
        allMatches.map { it.roundId }.distinct().forEach { roundId ->
            roundDao.findById(roundId)?.let { roundsById[roundId] = it }
        }
        val phaseMatches = allMatches.filter { candidate ->
            candidate.stage == CupEngine.STAGE && roundsById[candidate.roundId]?.let { round ->
                CupEngine.canonicalPhase(round.stageLabel, round.name) == currentPhase
            } == true
        }
        if (phaseMatches.isEmpty() || phaseMatches.any { it.status != com.example.legacymasterliga.core.model.MatchStatus.FINISHED }) {
            return false
        }

        return advanceKnockoutInternal(
            seasonId = seasonId,
            competition = competition,
            currentRound = triggeringRound,
            phaseMatches = phaseMatches,
            allMatches = allMatches,
            roundsById = roundsById,
        )
    }

    private suspend fun advanceKnockoutInternal(
        seasonId: Long,
        competition: com.example.legacymasterliga.core.database.entity.CompetitionEntity,
        currentRound: com.example.legacymasterliga.core.database.entity.RoundEntity,
        phaseMatches: List<com.example.legacymasterliga.core.database.entity.MatchEntity>,
        allMatches: List<com.example.legacymasterliga.core.database.entity.MatchEntity>,
        roundsById: Map<Long, com.example.legacymasterliga.core.database.entity.RoundEntity>,
    ): Boolean {
        val pairings = phaseMatches.groupBy { it.pairingKey }
            .values
            .sortedBy { pairing -> pairing.minOfOrNull { it.bracketPosition ?: Int.MAX_VALUE } }
        val winners = pairings.mapNotNull(::calculateWinnerInternal)
        if (winners.size != pairings.size) return false

        if (CupEngine.isFinal(currentRound.stageLabel, currentRound.name)) {
            return winners.size == 1
        }

        val allParticipants = participantDao.findActiveBySeason(seasonId).map { it.clubId }
        val playersInPhase = phaseMatches.flatMap { listOf(it.homeClubId, it.awayClubId) }.toSet()
        val knockoutRoundNumbers = allMatches.filter { it.stage == CupEngine.STAGE }
            .mapNotNull { roundsById[it.roundId]?.number }
        val phaseFirstRoundNumber = phaseMatches.mapNotNull { roundsById[it.roundId]?.number }.minOrNull()
        val isOpeningPhase = phaseFirstRoundNumber != null && phaseFirstRoundNumber == knockoutRoundNumbers.minOrNull()
        val openingByes = if (isOpeningPhase) allParticipants.filter { it !in playersInPhase } else emptyList()
        val nextPhaseClubs = (openingByes + winners).distinct()
        check(nextPhaseClubs.size >= 2 && nextPhaseClubs.size % 2 == 0) {
            "A fase $currentRound não produziu um número válido de classificados."
        }

        val nextLabel = CupEngine.phaseLabel(nextPhaseClubs.size)
        val created = generateKnockoutStageInternal(seasonId, competition, nextPhaseClubs, nextLabel)
        if (created) {
            auditLogger.log(
                "COMPETITIONS",
                "STAGE_ADVANCED",
                "SEASON",
                seasonId,
                competition.leagueId,
                "$nextLabel gerada.",
                null,
            )
        }
        return false
    }

    private fun calculateWinnerInternal(
        matches: List<com.example.legacymasterliga.core.database.entity.MatchEntity>,
    ): Long? = CupEngine.winner(matches.map { match ->
        CupEngine.LegResult(
            leg = match.leg,
            homeClubId = match.homeClubId,
            awayClubId = match.awayClubId,
            homeScore = match.homeScore,
            awayScore = match.awayScore,
            winnerClubId = match.winnerClubId,
        )
    })

    private suspend fun generateKnockoutStageInternal(
        seasonId: Long,
        competition: com.example.legacymasterliga.core.database.entity.CompetitionEntity,
        entrants: List<Long>,
        label: String,
    ): Boolean {
        val legs = competition.knockoutLegs.coerceIn(1, 2)
        val firstRound = getOrCreateKnockoutRound(seasonId, label, leg = 1, legs = legs)
        val secondRound = if (legs == 2) getOrCreateKnockoutRound(seasonId, label, leg = 2, legs = legs) else null
        val nextMatches = mutableListOf<com.example.legacymasterliga.core.database.entity.MatchEntity>()

        entrants.chunked(2).forEachIndexed { index, pair ->
            check(pair.size == 2) { "A fase $label possui um confronto incompleto." }
            val pairingKey = "CUP:${label.uppercase()}:$index"
            val existingFirstLeg = matchDao.findByRoundBracketAndLeg(firstRound.id, index, 1)
                ?: matchDao.findByPairingAndLeg(seasonId, pairingKey, 1)
            if (existingFirstLeg == null) {
                nextMatches += com.example.legacymasterliga.core.database.entity.MatchEntity(
                    seasonId = seasonId,
                    roundId = firstRound.id,
                    homeClubId = pair[0],
                    awayClubId = pair[1],
                    pairingKey = pairingKey,
                    leg = 1,
                    stage = CupEngine.STAGE,
                    bracketPosition = index,
                )
            }

            if (secondRound != null) {
                val existingSecondLeg = matchDao.findByRoundBracketAndLeg(secondRound.id, index, 2)
                    ?: matchDao.findByPairingAndLeg(seasonId, pairingKey, 2)
                if (existingSecondLeg == null) {
                    nextMatches += com.example.legacymasterliga.core.database.entity.MatchEntity(
                        seasonId = seasonId,
                        roundId = secondRound.id,
                        homeClubId = pair[1],
                        awayClubId = pair[0],
                        pairingKey = pairingKey,
                        leg = 2,
                        stage = CupEngine.STAGE,
                        bracketPosition = index,
                    )
                }
            }
        }
        if (nextMatches.isNotEmpty()) matchDao.insertAll(nextMatches)
        return nextMatches.isNotEmpty()
    }

    private suspend fun getOrCreateKnockoutRound(
        seasonId: Long,
        stageLabel: String,
        leg: Int,
        legs: Int,
    ): com.example.legacymasterliga.core.database.entity.RoundEntity {
        val name = CupEngine.roundName(stageLabel, leg, legs)
        roundDao.findBySeasonAndName(seasonId, name)?.let { return it }
        roundDao.findAllBySeasonAndLabel(seasonId, stageLabel).getOrNull(leg - 1)?.let { return it }

        repeat(5) {
            val number = (roundDao.findMaxNumber(seasonId) ?: 0) + 1
            val candidate = com.example.legacymasterliga.core.database.entity.RoundEntity(
                seasonId = seasonId,
                number = number,
                name = name,
                stage = CupEngine.STAGE,
                stageLabel = stageLabel,
            )
            val id = roundDao.insertIfAbsent(candidate)
            if (id != -1L) return candidate.copy(id = id)
            roundDao.findBySeasonAndName(seasonId, name)?.let { return it }
            roundDao.findAllBySeasonAndLabel(seasonId, stageLabel).getOrNull(leg - 1)?.let { return it }
        }

        error("Não foi possível reservar uma rodada única para $name.")
    }

    override suspend fun rebuildStandings(seasonId: Long) {
        val season = requireNotNull(seasonDao.findById(seasonId)) { "Temporada não encontrada." }
        val competition = requireNotNull(competitionDao.findById(season.competitionId)) { "Competição não encontrada." }
        
        Log.d("StandingsAudit", "--- INICIANDO REBUILD STANDINGS ---")
        Log.d("StandingsAudit", "Competição: ${competition.name} (#${competition.id}) | Tipo: ${competition.type}")
        Log.d("StandingsAudit", "Temporada: ${season.name} (#$seasonId)")

        val accumulators = mutableMapOf<Pair<Long, Int?>, MutableStanding>()

        // 1. Fonte primária: Participantes inscritos (Garante que clubes apareçam mesmo com 0 jogos)
        val participants = participantDao.findActiveBySeason(seasonId)
        Log.d("StandingsAudit", "Participantes encontrados: ${participants.size}")
        
        // 2. Tentar descobrir grupos via partidas existentes (para Copas)
        val allMatches = matchDao.findBySeason(seasonId)
        val clubGroupMap = allMatches.associate { it.homeClubId to it.groupIndex } + 
                          allMatches.associate { it.awayClubId to it.groupIndex }

        participants.forEach { p ->
            // Se for LIGA, clubGroupMap retornará null (correto). 
            // Se for COPA com grupos, pegará o index do grupo.
            val gIdx = if (competition.type == com.example.legacymasterliga.core.model.CompetitionType.LEAGUE) null else clubGroupMap[p.clubId]
            accumulators[Pair(p.clubId, gIdx)] = MutableStanding()
            Log.d("StandingsAudit", "Clube inicializado: ID ${p.clubId} | Grupo: $gIdx")
        }

        // 3. Processar partidas finalizadas para atualizar estatísticas
        val finishedMatches = allMatches.filter { it.status == com.example.legacymasterliga.core.model.MatchStatus.FINISHED }
        Log.d("StandingsAudit", "Partidas finalizadas para processar: ${finishedMatches.size}")

        finishedMatches.forEach { match ->
            // Regra de Classificação: Apenas partidas de pontos corridos (REGULAR) ou Grupos (GROUPS) somam na tabela.
            // Partidas de Mata-Mata (KNOCKOUT) são ignoradas no cálculo de pontuação.
            if (match.stage == "KNOCKOUT" || match.stage == "PLACEHOLDER") return@forEach

            val hScore = match.homeScore ?: 0
            val aScore = match.awayScore ?: 0
            
            // Para Ligas, ignoramos o groupIndex da partida e usamos null como chave
            val homeKey = if (competition.type == com.example.legacymasterliga.core.model.CompetitionType.LEAGUE) Pair(match.homeClubId, null) else Pair(match.homeClubId, match.groupIndex)
            val awayKey = if (competition.type == com.example.legacymasterliga.core.model.CompetitionType.LEAGUE) Pair(match.awayClubId, null) else Pair(match.awayClubId, match.groupIndex)
            
            Log.d("StandingsAudit", "Processando: [${match.stage}] ${match.homeClubId} $hScore x $aScore ${match.awayClubId} | Grupo da partida: ${match.groupIndex} | Chave H: $homeKey")

            val home = accumulators[homeKey]
            val away = accumulators[awayKey]

            if (home == null || away == null) {
                Log.e("StandingsAudit", "ERRO: Clube da partida não encontrado nos acumuladores! H: $homeKey, A: $awayKey")
                return@forEach
            }

            home.played++
            away.played++
            home.goalsFor += hScore
            home.goalsAgainst += aScore
            away.goalsFor += aScore
            away.goalsAgainst += hScore

            when {
                hScore > aScore -> {
                    home.wins++
                    away.losses++
                    home.points += competition.pointsForWin
                    away.points += competition.pointsForLoss
                }
                hScore < aScore -> {
                    away.wins++
                    home.losses++
                    away.points += competition.pointsForWin
                    home.points += competition.pointsForLoss
                }
                else -> {
                    home.draws++
                    away.draws++
                    home.points += competition.pointsForDraw
                    away.points += competition.pointsForDraw
                }
            }
        }

        database.withTransaction {
            standingDao.deleteBySeason(seasonId)
            val entities = accumulators.map { (key, value) ->
                Log.d("StandingsAudit", "Gravando Clube ${key.first}: PTS=${value.points}, J=${value.played}, V=${value.wins}, E=${value.draws}, D=${value.losses}, GP=${value.goalsFor}, GC=${value.goalsAgainst}, SG=${value.goalsFor - value.goalsAgainst}")
                StandingEntity(
                    seasonId = seasonId,
                    clubId = key.first,
                    groupIndex = key.second,
                    played = value.played,
                    wins = value.wins,
                    draws = value.draws,
                    losses = value.losses,
                    goalsFor = value.goalsFor,
                    goalsAgainst = value.goalsAgainst,
                    goalDifference = value.goalsFor - value.goalsAgainst,
                    points = value.points,
                )
            }
            standingDao.upsertAll(entities)
            Log.d("StandingsAudit", "Total de linhas gravadas: ${entities.size}")
        }
        Log.d("StandingsAudit", "--- REBUILD STANDINGS CONCLUÍDO ---")
    }

    private data class MutableStanding(
        var played: Int = 0,
        var wins: Int = 0,
        var draws: Int = 0,
        var losses: Int = 0,
        var goalsFor: Int = 0,
        var goalsAgainst: Int = 0,
        var points: Int = 0,
    )
}
