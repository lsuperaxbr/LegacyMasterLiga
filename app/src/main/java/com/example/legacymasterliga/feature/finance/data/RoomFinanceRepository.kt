package com.example.legacymasterliga.feature.finance.data

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.dao.TransferDao
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.entity.TransferEntity
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.Player
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.PlayerRepository
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.finance.domain.ClubBalance
import com.example.legacymasterliga.feature.finance.domain.FinanceRepository
import com.example.legacymasterliga.feature.finance.domain.MarketTransfer
import com.example.legacymasterliga.feature.finance.domain.StatementEntry
import com.example.legacymasterliga.feature.news.domain.NewsEventPublisher
import com.example.legacymasterliga.feature.news.domain.SwapCompletedEvent
import com.example.legacymasterliga.feature.news.domain.TransferCompletedEvent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomFinanceRepository @Inject constructor(
    private val database: AppDatabase,
    private val clubDao: ClubDao,
    private val financialDao: FinancialDao,
    private val transferDao: TransferDao,
    private val playerDao: com.example.legacymasterliga.core.database.dao.PlayerDao,
    private val playerRepository: PlayerRepository,
    private val newsPublisher: NewsEventPublisher,
    private val auditLogger: AuditLogger,
    private val competitionDao: CompetitionDao,
    private val seasonDao: SeasonDao,
) : FinanceRepository {
    override fun observeBalances(leagueId: Long): Flow<List<ClubBalance>> =
        financialDao.observeClubBalances(leagueId).map { rows -> rows.map { ClubBalance(it.id, it.name, it.isBank, it.balanceCr) } }

    override fun observeStatement(leagueId: Long, clubId: Long?): Flow<List<StatementEntry>> =
        financialDao.observeStatement(leagueId, clubId).map { rows ->
            rows.map { StatementEntry(it.id, it.clubId, it.clubName, it.amountCr, it.description, it.type, it.counterpartyName, it.transferId, it.createdAt) }
        }

    override fun observeTransfers(leagueId: Long): Flow<List<MarketTransfer>> =
        transferDao.observeByLeague(leagueId).map { rows ->
            rows.map { MarketTransfer(it.id, it.playerName, it.originClubId, it.originClubName, it.destinationClubId, it.destinationClubName, it.valueCr, it.type, it.swapId, it.note, it.createdAt) }
        }

    override suspend fun adjustBalance(actor: User, clubId: Long, amountCr: Long, description: String) {
        require(actor.role == UserRole.ADMINISTRATOR) { "Somente administradores podem movimentar o Banco da Liga." }
        require(amountCr != 0L) { "Informe um valor diferente de zero." }
        val note = description.trim()
        require(note.length >= 3) { "Informe uma descrição para o lançamento." }
        database.withTransaction {
            val club = clubDao.findById(clubId) ?: error("Clube não encontrado.")
            require(!club.isBank && club.isActive) { "Selecione um clube ativo." }
            val bank = clubDao.findBankByLeague(club.leagueId) ?: error("Banco da Liga não encontrado.")
            if (amountCr < 0) require(financialDao.balanceOf(club.id) >= -amountCr) { "Saldo insuficiente para este débito." }
            val now = System.currentTimeMillis()
            financialDao.insert(FinancialTransactionEntity(clubId = club.id, amountCr = amountCr, description = note, type = "BANK_ADJUSTMENT", counterpartyClubId = bank.id, createdAt = now))
            financialDao.insert(FinancialTransactionEntity(clubId = bank.id, amountCr = -amountCr, description = note, type = "BANK_ADJUSTMENT", counterpartyClubId = club.id, createdAt = now))
            auditLogger.log("FINANCE", "BALANCE_ADJUSTED", "CLUB", club.id, club.leagueId,
                "Ajuste financeiro de ${amountCr} CR em ${club.name}", note)
        }
    }

    override suspend fun registerTransfer(
        actor: User,
        leagueId: Long,
        playerName: String,
        originClubId: Long,
        destinationClubId: Long,
        valueCr: Long,
        note: String?,
        type: String,
    ) {
        val player = playerName.trim()
        require(player.length >= 2) { "Informe o nome do jogador." }
        require(valueCr >= 0L) { "O valor não pode ser negativo." }
        require(originClubId != destinationClubId) { "Origem e destino devem ser diferentes." }
        database.withTransaction {
            val origin = clubDao.findById(originClubId) ?: error("Clube de origem não encontrado.")
            val destination = clubDao.findById(destinationClubId) ?: error("Clube de destino não encontrado.")
            require(origin.leagueId == leagueId && destination.leagueId == leagueId) { "Os clubes devem pertencer à mesma liga." }
            
            if (!origin.isBank) require(origin.isActive) { "O clube de origem deve estar ativo." }
            if (!destination.isBank) require(destination.isActive) { "O clube de destino deve estar ativo." }

            if (actor.role == UserRole.PRESIDENT) {
                val isBuyer = destination.presidentUserId == actor.id
                val isSeller = origin.presidentUserId == actor.id
                require(isBuyer || isSeller) { "Presidentes só podem operar transferências envolvendo o próprio clube." }
            }
            require(actor.role == UserRole.ADMINISTRATOR || actor.role == UserRole.PRESIDENT) { "Usuário sem permissão para transferências." }

            // Se o destino NÃO for o banco, o destino paga. Validar saldo.
            if (!destination.isBank && valueCr > 0) {
                require(financialDao.balanceOf(destination.id) >= valueCr) { "Saldo insuficiente: ${destination.name} não possui $valueCr CR." }
            }

            // FEATURE-RC1-007: Multa de Liberação (Clube -> Banco)
            val isRelease = destination.isBank && !origin.isBank
            val releaseFeeCr = 5L
            if (isRelease) {
                val currentBalance = financialDao.balanceOf(origin.id)
                require(currentBalance >= releaseFeeCr) { "Saldo insuficiente para pagar a multa de liberação (5 CR). Disponível: $currentBalance CR." }
            }

            val now = System.currentTimeMillis()
            val seasonId = findActiveLeagueSeasonId(leagueId)
            val transferId = transferDao.insert(
                TransferEntity(
                    leagueId = leagueId,
                    playerName = player,
                    originClubId = origin.id,
                    destinationClubId = destination.id,
                    valueCr = valueCr,
                    type = type,
                    seasonId = seasonId,
                    note = note,
                    createdAt = now
                )
            )
            
            val baseDesc = "Transferência de $player: ${origin.name} → ${destination.name}"
            val finalDesc = if (note.isNullOrBlank()) baseDesc else "$baseDesc ($note)"

            // Somente realizar movimentação se o destino não for o banco e valor > 0
            if (!destination.isBank && valueCr > 0) {
                financialDao.insert(FinancialTransactionEntity(clubId = destination.id, amountCr = -valueCr, description = finalDesc, type = "TRANSFER", counterpartyClubId = origin.id, transferId = transferId, createdAt = now))
                financialDao.insert(FinancialTransactionEntity(clubId = origin.id, amountCr = valueCr, description = finalDesc, type = "TRANSFER", counterpartyClubId = destination.id, transferId = transferId, createdAt = now))
            }

            // Aplicar Multa de Liberação se for o caso
            if (isRelease) {
                val bank = destination // Já sabemos que é o banco
                val feeDesc = "Multa de liberação: $player (${origin.name} → Banco)"
                financialDao.insert(FinancialTransactionEntity(clubId = origin.id, amountCr = -releaseFeeCr, description = feeDesc, type = "TRANSFER_FEE", counterpartyClubId = bank.id, transferId = transferId, createdAt = now))
                financialDao.insert(FinancialTransactionEntity(clubId = bank.id, amountCr = releaseFeeCr, description = feeDesc, type = "TRANSFER_FEE", counterpartyClubId = origin.id, transferId = transferId, createdAt = now))
            }

            auditLogger.log("MARKET", "TRANSFER_COMPLETED", "TRANSFER", transferId, leagueId,
                "Transferência de $player concluída${if (isRelease) " com multa de $releaseFeeCr CR" else ""}",
                "${origin.name} → ${destination.name}")

            newsPublisher.publish(
                TransferCompletedEvent(
                    leagueId = leagueId,
                    sourceId = transferId,
                    playerName = player,
                    originClubName = origin.name,
                    destinationClubName = destination.name,
                    valueCr = valueCr,
                    occurredAt = now,
                    type = type
                ),
            )
        }
    }

    override suspend fun transferPlayerAtomic(
        actor: User,
        leagueId: Long,
        player: Player,
        destinationClubId: Long,
        valueCr: Long,
        note: String?
    ) = database.withTransaction {
        registerTransfer(actor, leagueId, player.name, player.clubId, destinationClubId, valueCr, note, "TRANSFER")
        playerRepository.savePlayer(
            player.copy(
                clubId = destinationClubId,
                marketStatus = MarketStatus.NOT_LISTED,
                askingPriceCr = null,
            )
        )
        Unit
    }

    override suspend fun registerSwap(
        actor: User,
        leagueId: Long,
        playerA: String,
        clubAId: Long,
        playerB: String,
        clubBId: Long,
        compensationCr: Long,
        payerClubId: Long?,
        note: String?
    ) {
        require(playerA.trim().length >= 2 && playerB.trim().length >= 2) { "Informe o nome dos dois jogadores." }
        require(clubAId != clubBId) { "Clubes devem ser diferentes." }
        require(compensationCr >= 0) { "Compensação não pode ser negativa." }
        if (compensationCr > 0) require(payerClubId != null) { "Informe o clube pagador da compensação." }

        database.withTransaction {
            val clubA = clubDao.findById(clubAId) ?: error("Clube A não encontrado.")
            val clubB = clubDao.findById(clubBId) ?: error("Clube B não encontrado.")
            require(clubA.leagueId == leagueId && clubB.leagueId == leagueId) { "Os clubes devem pertencer à mesma liga." }
            
            if (actor.role == UserRole.PRESIDENT) {
                require(clubA.presidentUserId == actor.id || clubB.presidentUserId == actor.id) { "Presidentes só podem operar trocas envolvendo o próprio clube." }
            }

            if (compensationCr > 0 && payerClubId != null) {
                require(financialDao.balanceOf(payerClubId) >= compensationCr) { "Saldo insuficiente para compensação." }
            }

            val now = System.currentTimeMillis()
            val seasonId = findActiveLeagueSeasonId(leagueId)
            val swapId = UUID.randomUUID().toString()

            // Transferência A -> B
            val t1Id = transferDao.insert(
                TransferEntity(
                    leagueId = leagueId,
                    playerName = playerA.trim(),
                    originClubId = clubAId,
                    destinationClubId = clubBId,
                    valueCr = 0,
                    type = "SWAP",
                    swapId = swapId,
                    seasonId = seasonId,
                    note = note,
                    createdAt = now
                )
            )
            // Transferência B -> A
            transferDao.insert(
                TransferEntity(
                    leagueId = leagueId,
                    playerName = playerB.trim(),
                    originClubId = clubBId,
                    destinationClubId = clubAId,
                    valueCr = 0,
                    type = "SWAP",
                    swapId = swapId,
                    seasonId = seasonId,
                    note = note,
                    createdAt = now
                )
            )

            if (compensationCr > 0 && payerClubId != null) {
                val receiverClubId = if (payerClubId == clubAId) clubBId else clubAId
                val payerName = if (payerClubId == clubAId) clubA.name else clubB.name
                val receiverName = if (receiverClubId == clubAId) clubA.name else clubB.name
                
                val desc = "Compensação em troca: $payerName ($playerA) <-> $receiverName ($playerB)"
                financialDao.insert(
                    FinancialTransactionEntity(
                        clubId = payerClubId,
                        amountCr = -compensationCr,
                        description = desc,
                        type = "TRANSFER",
                        counterpartyClubId = receiverClubId,
                        transferId = t1Id,
                        createdAt = now
                    )
                )
                financialDao.insert(
                    FinancialTransactionEntity(
                        clubId = receiverClubId,
                        amountCr = compensationCr,
                        description = desc,
                        type = "TRANSFER",
                        counterpartyClubId = payerClubId,
                        transferId = t1Id,
                        createdAt = now
                    )
                )
            }

            auditLogger.log("MARKET", "SWAP_COMPLETED", "TRANSFER", t1Id, leagueId,
                "Troca entre ${clubA.name} e ${clubB.name} concluída",
                "$playerA <-> $playerB | Compensação: $compensationCr CR")

            newsPublisher.publish(
                SwapCompletedEvent(
                    leagueId = leagueId,
                    sourceId = t1Id,
                    playerA = playerA.trim(),
                    clubAName = clubA.name,
                    playerB = playerB.trim(),
                    clubBName = clubB.name,
                    compensationCr = compensationCr,
                    payerClubName = payerClubId?.let { if (it == clubAId) clubA.name else clubB.name },
                    occurredAt = now
                )
            )
        }
    }

    override suspend fun injectInitialBalance(leagueId: Long, amount: Long) {
        require(amount > 0) { "O valor de injeção deve ser maior que zero." }
        database.withTransaction {
            val bank = clubDao.findBankByLeague(leagueId) ?: error("Banco da Liga não encontrado.")
            val now = System.currentTimeMillis()
            financialDao.insert(
                FinancialTransactionEntity(
                    clubId = bank.id,
                    amountCr = amount,
                    description = "Injeção de capital inicial do sistema",
                    type = "SYSTEM_LOAD",
                    createdAt = now,
                ),
            )
            auditLogger.log("FINANCE", "BANK_INJECTION", "CLUB", bank.id, leagueId, "Injeção de $amount CR no Banco da Liga", null)
        }
    }

    override suspend fun revertTransfer(transferId: Long): com.example.legacymasterliga.feature.finance.domain.RevertResult = database.withTransaction {
        val transfer = transferDao.findById(transferId) ?: error("Transferência não encontrada.")
        if (transfer.note?.startsWith("[REVERTIDA]") == true) {
            error("Esta transferência já foi revertida.")
        }
        val now = System.currentTimeMillis()

        // 1. Reverter o dinheiro: Crédito para o Comprador original, Débito para o Vendedor original
        financialDao.insert(FinancialTransactionEntity(
            clubId = transfer.originClubId, amountCr = -transfer.valueCr,
            description = "Estorno de venda #${transfer.id} (${transfer.playerName})",
            type = "REVERSAO", counterpartyClubId = transfer.destinationClubId, createdAt = now,
        ))
        financialDao.insert(FinancialTransactionEntity(
            clubId = transfer.destinationClubId, amountCr = transfer.valueCr,
            description = "Estorno de compra #${transfer.id} (${transfer.playerName})",
            type = "REVERSAO", counterpartyClubId = transfer.originClubId, createdAt = now,
        ))

        // 1.1 Estornar Multa de Liberação (se houver)
        if (transfer.type == "RELEASE") {
            val releaseFeeCr = 5L
            financialDao.insert(FinancialTransactionEntity(
                clubId = transfer.originClubId, amountCr = releaseFeeCr,
                description = "Estorno de multa de liberação #${transfer.id}",
                type = "REVERSAO", counterpartyClubId = transfer.destinationClubId, createdAt = now,
            ))
            financialDao.insert(FinancialTransactionEntity(
                clubId = transfer.destinationClubId, amountCr = -releaseFeeCr,
                description = "Estorno de multa de liberação #${transfer.id}",
                type = "REVERSAO", counterpartyClubId = transfer.originClubId, createdAt = now,
            ))
        }

        // 2. Tentar mover o jogador de volta (só se houver correspondência única e segura)
        val candidates = playerDao.findAllByNameAndClub(transfer.destinationClubId, transfer.playerName)
        var playerMoved = false
        var warning: String? = null
        when {
            candidates.isEmpty() -> warning = "Jogador '${transfer.playerName}' não encontrado no clube de destino. Corrija manualmente se necessário."
            candidates.size > 1 -> warning = "Mais de um jogador chamado '${transfer.playerName}' no clube de destino. Corrija manualmente para evitar mover o jogador errado."
            else -> {
                val player = candidates.first()
                playerDao.upsert(player.copy(clubId = transfer.originClubId))
                playerMoved = true
            }
        }

        // 3. Marcar a transferência como revertida (sem apagar o histórico)
        transferDao.update(transfer.copy(note = "[REVERTIDA] ${transfer.note.orEmpty()}".trim()))

        com.example.legacymasterliga.feature.finance.domain.RevertResult(moneyReverted = true, playerMoved = playerMoved, warning = warning)
    }

    override suspend fun dispensePlayer(
        actor: User,
        leagueId: Long,
        player: Player,
        dispensalFeeCr: Long
    ) = database.withTransaction {
        val bankClub = clubDao.findBankByLeague(leagueId) ?: error("Banco da Liga não encontrado.")
        val originClub = clubDao.findById(player.clubId) ?: error("Clube de origem não encontrado.")
        
        // Validação de saldo
        require(financialDao.balanceOf(player.clubId) >= dispensalFeeCr) {
            "${originClub.name} não tem saldo suficiente para pagar a taxa de dispensa (${dispensalFeeCr} CR)."
        }
        
        val now = System.currentTimeMillis()
        val seasonId = findActiveLeagueSeasonId(leagueId)

        // 1. Cobra a taxa do clube
        financialDao.insert(
            FinancialTransactionEntity(
                clubId = player.clubId,
                amountCr = -dispensalFeeCr,
                description = "Taxa de dispensa: ${player.name}",
                type = "RELEASE",
                counterpartyClubId = bankClub.id,
                createdAt = now,
            )
        )
        
        // Também registra a entrada no Banco
        financialDao.insert(
            FinancialTransactionEntity(
                clubId = bankClub.id,
                amountCr = dispensalFeeCr,
                description = "Recebimento taxa de dispensa: ${player.name} (${originClub.name})",
                type = "RELEASE",
                counterpartyClubId = originClub.id,
                createdAt = now,
            )
        )

        // 2. Registra a transferência para o histórico (Negociações)
        transferDao.insert(
            TransferEntity(
                leagueId = leagueId,
                playerName = player.name,
                originClubId = player.clubId,
                destinationClubId = bankClub.id,
                valueCr = dispensalFeeCr,
                type = "RELEASE",
                seasonId = seasonId,
                createdAt = now,
            )
        )

        // 3. Move o jogador para o Banco, marcado como Dispensado (FOR_SALE, 5 CR)
        playerRepository.savePlayer(
            player.copy(
                clubId = bankClub.id,
                marketStatus = MarketStatus.FOR_SALE,
                askingPriceCr = 5L,
            )
        )
        
        auditLogger.log("MARKET", "PLAYER_DISPENSED", "PLAYER", player.id, leagueId,
            "Jogador ${player.name} dispensado por ${originClub.name}", "Taxa: $dispensalFeeCr CR")
    }

    private suspend fun findActiveLeagueSeasonId(leagueId: Long): Long? {
        val leagueCompetition = competitionDao.findByLeagueAndType(leagueId, com.example.legacymasterliga.core.model.CompetitionType.LEAGUE) ?: return null
        return seasonDao.findByStatus(leagueCompetition.id, com.example.legacymasterliga.core.model.SeasonStatus.ACTIVE)?.id
    }
}
