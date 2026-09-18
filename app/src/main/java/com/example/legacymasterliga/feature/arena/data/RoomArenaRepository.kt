package com.example.legacymasterliga.feature.arena.data

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ArenaDao
import com.example.legacymasterliga.core.database.dao.ArenaDuelRow
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.entity.ArenaDuelEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.feature.arena.domain.ArenaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomArenaRepository @Inject constructor(
    private val database: AppDatabase,
    private val arenaDao: ArenaDao,
    private val clubDao: ClubDao,
    private val financialDao: FinancialDao,
) : ArenaRepository {

    override fun observeByLeague(leagueId: Long): Flow<List<ArenaDuelRow>> =
        arenaDao.observeByLeague(leagueId)

    override suspend fun createDuel(leagueId: Long, clubAId: Long, clubBId: Long, stakeCr: Long, note: String?, createdByUserId: Long) {
        require(stakeCr in 5..50) { "A aposta deve ser entre 5 e 50 CR." }
        require(clubAId != clubBId) { "Escolha dois clubes diferentes." }
        database.withTransaction {
            val clubA = clubDao.findById(clubAId) ?: error("Clube A não encontrado.")
            val clubB = clubDao.findById(clubBId) ?: error("Clube B não encontrado.")
            require(!clubA.isBank && !clubB.isBank) { "O Banco da Liga não pode participar da Arena." }
            require(financialDao.balanceOf(clubAId) >= stakeCr) { "${clubA.name} não tem saldo suficiente." }
            require(financialDao.balanceOf(clubBId) >= stakeCr) { "${clubB.name} não tem saldo suficiente." }

            val now = System.currentTimeMillis()
            arenaDao.insert(
                ArenaDuelEntity(
                    leagueId = leagueId, clubAId = clubAId, clubBId = clubBId,
                    stakeCr = stakeCr, status = "PENDING", note = note,
                    createdByUserId = createdByUserId, createdAt = now,
                )
            )
            val desc = "Arena: ${clubA.name} vs ${clubB.name} (${stakeCr} CR)"
            financialDao.insert(FinancialTransactionEntity(clubId = clubAId, amountCr = -stakeCr, description = desc, type = "ARENA", counterpartyClubId = clubBId, createdAt = now))
            financialDao.insert(FinancialTransactionEntity(clubId = clubBId, amountCr = -stakeCr, description = desc, type = "ARENA", counterpartyClubId = clubAId, createdAt = now))
        }
    }

    override suspend fun resolveDuel(duelId: Long, resultType: String) {
        database.withTransaction {
            val duel = arenaDao.findById(duelId) ?: error("Duelo não encontrado.")
            require(duel.status == "PENDING") { "Este duelo já foi resolvido." }
            val now = System.currentTimeMillis()
            val prize = duel.stakeCr * 2
            when (resultType) {
                "CLUB_A_WIN" -> financialDao.insert(FinancialTransactionEntity(clubId = duel.clubAId, amountCr = prize, description = "Arena: vitória (+${prize} CR)", type = "ARENA", counterpartyClubId = duel.clubBId, createdAt = now))
                "CLUB_B_WIN" -> financialDao.insert(FinancialTransactionEntity(clubId = duel.clubBId, amountCr = prize, description = "Arena: vitória (+${prize} CR)", type = "ARENA", counterpartyClubId = duel.clubAId, createdAt = now))
                "DRAW" -> {
                    financialDao.insert(FinancialTransactionEntity(clubId = duel.clubAId, amountCr = duel.stakeCr, description = "Arena: empate (devolução)", type = "ARENA", counterpartyClubId = duel.clubBId, createdAt = now))
                    financialDao.insert(FinancialTransactionEntity(clubId = duel.clubBId, amountCr = duel.stakeCr, description = "Arena: empate (devolução)", type = "ARENA", counterpartyClubId = duel.clubAId, createdAt = now))
                }
            }
            arenaDao.resolve(duelId, "RESOLVED", resultType, now)
        }
    }
}
