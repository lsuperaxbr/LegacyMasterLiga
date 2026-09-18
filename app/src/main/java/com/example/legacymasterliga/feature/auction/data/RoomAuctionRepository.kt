package com.example.legacymasterliga.feature.auction.data

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.AuctionDao
import com.example.legacymasterliga.core.database.dao.AuctionItemRow
import com.example.legacymasterliga.core.database.dao.AuctionLotSummary
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.dao.TransferDao
import com.example.legacymasterliga.core.database.entity.AuctionBidEntity
import com.example.legacymasterliga.core.database.entity.AuctionItemEntity
import com.example.legacymasterliga.core.database.entity.AuctionLotEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.entity.TransferEntity
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.domain.repository.PlayerRepository
import com.example.legacymasterliga.feature.auction.domain.AuctionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomAuctionRepository @Inject constructor(
    private val database: AppDatabase,
    private val auctionDao: AuctionDao,
    private val financialDao: FinancialDao,
    private val clubDao: ClubDao,
    private val transferDao: TransferDao,
    private val seasonDao: SeasonDao,
    private val competitionDao: CompetitionDao,
    private val playerRepository: PlayerRepository,
) : AuctionRepository {

    override fun observeLots(leagueId: Long): Flow<List<AuctionLotSummary>> =
        auctionDao.observeLotsByLeague(leagueId)

    override fun observeItems(lotId: Long): Flow<List<AuctionItemRow>> =
        auctionDao.observeItemsByLot(lotId)

    override suspend fun createLot(
        leagueId: Long,
        name: String,
        startAt: Long,
        endAt: Long,
        playerIds: List<Long>,
        createdByUserId: Long
    ) {
        require(endAt > startAt) { "O horário de fim precisa ser depois do início." }
        database.withTransaction {
            val now = System.currentTimeMillis()
            val lotId = auctionDao.insertLot(
                AuctionLotEntity(
                    leagueId = leagueId,
                    name = name,
                    startAt = startAt,
                    endAt = endAt,
                    status = "SCHEDULED",
                    createdByUserId = createdByUserId,
                    createdAt = now,
                )
            )
            playerIds.forEach { pid ->
                auctionDao.insertItem(
                    AuctionItemEntity(
                        lotId = lotId,
                        playerId = pid,
                        startingPriceCr = 5,
                        status = "OPEN",
                        createdAt = now,
                    )
                )
            }
        }
    }

    override suspend fun placeBid(
        itemId: Long,
        clubId: Long,
        amountCr: Long,
        bidByUserId: Long
    ): Result<Unit> = runCatching {
        database.withTransaction {
            val item = auctionDao.findItemById(itemId) ?: error("Item não encontrado.")
            val lot = auctionDao.findLotById(item.lotId) ?: error("Leilão não encontrado.")
            val now = System.currentTimeMillis()

            require(now in lot.startAt..lot.endAt) { "Este leilão não está aberto no momento." }
            require(item.status == "OPEN") { "Este jogador não está mais disponível no leilão." }

            val currentBid = item.currentBidCr ?: (item.startingPriceCr - 5)
            val minRequired = currentBid + 5
            require(amountCr >= minRequired) { "O lance mínimo é ${minRequired} CR (pelo menos 5 CR a mais que o atual)." }
            require(item.leadingClubId != clubId) { "Seu clube já está na frente deste item." }

            val totalCommittedByClub = auctionDao.sumActiveBidsByClub(clubId)
            val balance = financialDao.balanceOf(clubId)
            require(balance - totalCommittedByClub >= amountCr) {
                "Saldo insuficiente. Você já tem ${totalCommittedByClub} CR travados em outros lances deste leilão."
            }

            // Devolve o lance anterior (se houver)
            val previousBid = auctionDao.findActiveBid(itemId)
            if (previousBid != null) {
                auctionDao.updateBid(previousBid.copy(status = "OUTBID"))
                financialDao.insert(
                    FinancialTransactionEntity(
                        clubId = previousBid.clubId,
                        amountCr = previousBid.amountCr,
                        description = "Lance superado no leilão (devolução): ${lot.name}",
                        type = "AUCTION",
                        counterpartyClubId = clubId,
                        createdAt = now,
                    )
                )
            }

            // Trava o novo lance (débito imediato)
            financialDao.insert(
                FinancialTransactionEntity(
                    clubId = clubId,
                    amountCr = -amountCr,
                    description = "Lance no leilão: ${lot.name}",
                    type = "AUCTION",
                    counterpartyClubId = null,
                    createdAt = now,
                )
            )
            auctionDao.insertBid(
                AuctionBidEntity(
                    itemId = itemId,
                    clubId = clubId,
                    amountCr = amountCr,
                    bidByUserId = bidByUserId,
                    status = "ACTIVE",
                    createdAt = now,
                )
            )
            auctionDao.updateItem(item.copy(currentBidCr = amountCr, leadingClubId = clubId))
        }
    }

    override suspend fun closeExpiredLots(leagueId: Long) {
        val now = System.currentTimeMillis()
        val expiredLots = auctionDao.findExpiredLots(leagueId, now)
        
        expiredLots.forEach { lot ->
            runCatching {
                database.withTransaction {
                    // Recarrega para checar se ainda está elegível (trava otimista)
                    val currentLot = auctionDao.findLotById(lot.id)
                    if (currentLot == null || currentLot.status == "CLOSED") return@withTransaction

                    // 1. Marca o lote como fechado imediatamente
                    auctionDao.updateLot(currentLot.copy(status = "CLOSED", closedAt = now))

                    // 2. Processa cada item
                    val items = auctionDao.findItemsByLot(lot.id)
                    val activeSeasonId = findActiveLeagueSeasonId(leagueId)
                    
                    items.forEach { item ->
                        val winningBid = auctionDao.findActiveBid(item.id)
                        if (winningBid != null) {
                            // VENDIDO
                            auctionDao.updateItem(item.copy(status = "SOLD"))
                            auctionDao.updateBid(winningBid.copy(status = "WON"))
                            
                            // Move o jogador
                            val player = playerRepository.findById(item.playerId)
                            if (player != null) {
                                playerRepository.savePlayer(
                                    player.copy(
                                        clubId = winningBid.clubId,
                                        marketStatus = MarketStatus.NOT_LISTED
                                    )
                                )
                                
                                // Registra no histórico
                                transferDao.insert(
                                    TransferEntity(
                                        leagueId = leagueId,
                                        playerName = player.name,
                                        originClubId = player.clubId, // Banco da Liga
                                        destinationClubId = winningBid.clubId,
                                        valueCr = winningBid.amountCr,
                                        type = "AUCTION",
                                        seasonId = activeSeasonId,
                                        createdAt = now
                                    )
                                )
                            }
                        } else {
                            // NÃO VENDIDO
                            auctionDao.updateItem(item.copy(status = "UNSOLD"))
                        }
                    }
                }
            }
        }
    }

    private suspend fun findActiveLeagueSeasonId(leagueId: Long): Long? {
        val leagueCompetition = competitionDao.findByLeagueAndType(leagueId, CompetitionType.LEAGUE) ?: return null
        return seasonDao.findByStatus(leagueCompetition.id, SeasonStatus.ACTIVE)?.id
    }
}
