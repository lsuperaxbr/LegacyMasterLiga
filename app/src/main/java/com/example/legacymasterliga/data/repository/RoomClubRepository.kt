package com.example.legacymasterliga.data.repository

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.ClubPresidencyDao
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.ClubPresidencyEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.data.mapper.toDomain
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.repository.ClubRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomClubRepository @Inject constructor(
    private val database: AppDatabase,
    private val clubDao: ClubDao,
    private val clubPresidencyDao: ClubPresidencyDao,
    private val financialDao: FinancialDao,
    private val auditLogger: AuditLogger,
) : ClubRepository {
    override fun observeActiveByLeague(leagueId: Long): Flow<List<Club>> =
        clubDao.observeActiveByLeague(leagueId).map { clubs -> clubs.map { it.toDomain() } }

    override fun observeManagedByLeague(leagueId: Long): Flow<List<Club>> =
        clubDao.observeManagedByLeague(leagueId).map { clubs -> clubs.map { it.toDomain() } }

    override suspend fun findById(clubId: Long): Club? = clubDao.findById(clubId)?.toDomain()

    override suspend fun findLeagueBank(leagueId: Long): Club? =
        clubDao.findBankByLeague(leagueId)?.toDomain()

    override suspend fun createLeagueBank(leagueId: Long, name: String): Long =
        clubDao.insert(
            ClubEntity(
                leagueId = leagueId,
                name = name.trim(),
                isBank = true,
                isActive = true,
            ),
        )

    override suspend fun create(
        leagueId: Long,
        name: String,
        crestUri: String?,
        presidentUserId: Long?,
        initialBalance: Long,
    ): Long = database.withTransaction {
        val id = clubDao.insert(
            ClubEntity(
                leagueId = leagueId,
                name = name.trim(),
                crestUri = crestUri,
                presidentUserId = presidentUserId,
                isBank = false,
                isActive = true,
            ),
        )
        if (initialBalance > 0) {
            val bank = clubDao.findBankByLeague(leagueId) ?: error("Banco da Liga não encontrado.")
            val now = System.currentTimeMillis()
            financialDao.insert(
                FinancialTransactionEntity(
                    clubId = id,
                    amountCr = initialBalance,
                    description = "Saldo inicial (Injeção)",
                    type = "INITIAL_BALANCE",
                    counterpartyClubId = bank.id,
                    createdAt = now,
                )
            )
            financialDao.insert(
                FinancialTransactionEntity(
                    clubId = bank.id,
                    amountCr = -initialBalance,
                    description = "Carga de saldo inicial: ${name.trim()}",
                    type = "INITIAL_BALANCE",
                    counterpartyClubId = id,
                    createdAt = now,
                )
            )
        }
        auditLogger.log("CLUBS", "CLUB_CREATED", "CLUB", id, leagueId, "Clube ${name.trim()} criado", "Saldo inicial: $initialBalance CR")
        id
    }

    override suspend fun update(
        clubId: Long,
        leagueId: Long,
        name: String,
        crestUri: String?,
        presidentUserId: Long?,
    ) {
        val current = requireNotNull(clubDao.findById(clubId)) { "Clube não encontrado." }
        require(!current.isBank) { "O Banco da Liga não pode ser editado por esta tela." }

        if (presidentUserId != current.presidentUserId) {
            val now = System.currentTimeMillis()
            // Fecha o período do presidente anterior, se houver
            clubPresidencyDao.findOpenByClub(clubId)?.let { open ->
                clubPresidencyDao.update(open.copy(endAt = now))
            }
            // Abre um novo período para o novo presidente, se houver um
            if (presidentUserId != null) {
                clubPresidencyDao.insert(ClubPresidencyEntity(
                    clubId = clubId, userId = presidentUserId, startAt = now, endAt = null,
                ))
            }
        }

        clubDao.update(
            current.copy(
                leagueId = leagueId,
                name = name.trim(),
                crestUri = crestUri,
                presidentUserId = presidentUserId,
                updatedAt = System.currentTimeMillis(),
            ),
        )

        auditLogger.log("CLUBS", "CLUB_UPDATED", "CLUB", clubId, leagueId, "Clube ${name.trim()} atualizado", null)
    }

    override suspend fun delete(clubId: Long) {
        val club = requireNotNull(clubDao.findById(clubId)) { "Clube não encontrado." }
        require(!club.isBank) { "O Banco da Liga não pode ser excluído." }
        
        if (clubDao.isUsedInCompetitions(clubId)) {
            throw Exception("Este clube possui participações em competições e não pode ser excluído.")
        }
        
        if (clubDao.hasFinancialHistory(clubId)) {
            throw Exception("Este clube possui histórico financeiro e não pode ser excluído. Desative-o em vez disso.")
        }

        clubDao.deleteById(clubId)
        auditLogger.log("CLUBS", "CLUB_DELETED", "CLUB", clubId, club.leagueId, "Clube ${club.name} excluído", null)
    }

    override suspend fun assignPresident(userId: Long, clubId: Long?) = database.withTransaction {
        val now = System.currentTimeMillis()
        clubDao.clearPresidentAssignments(userId, now)
        if (clubId != null) {
            check(clubDao.assignPresident(clubId, userId, now) == 1) { "Clube não encontrado." }
            val club = clubDao.findById(clubId)
            auditLogger.log("CLUBS", "PRESIDENT_ASSIGNED", "CLUB", clubId, club?.leagueId, "Usuário $userId assumiu o clube ${club?.name}", null)
        } else {
            auditLogger.log("CLUBS", "PRESIDENT_REMOVED", "USER", userId, null, "Usuário $userId deixou seu clube", null)
        }
    }

    override suspend fun setActive(clubId: Long, isActive: Boolean) {
        val club = requireNotNull(clubDao.findById(clubId)) { "Clube não encontrado." }
        check(clubDao.setActive(clubId, isActive) == 1) { "Não foi possível atualizar o clube." }
        auditLogger.log("CLUBS", if (isActive) "CLUB_ACTIVATED" else "CLUB_DEACTIVATED", "CLUB", clubId, club.leagueId,
            "Clube ${club.name} ${if (isActive) "ativado" else "desativado"}", null)
    }
}
