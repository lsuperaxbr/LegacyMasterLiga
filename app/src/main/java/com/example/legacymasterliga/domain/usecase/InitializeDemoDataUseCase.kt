package com.example.legacymasterliga.domain.usecase

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import com.example.legacymasterliga.core.database.entity.CompetitionParticipantEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.entity.SeasonEntity
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionStatus
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.security.PasswordHasher
import com.example.legacymasterliga.domain.model.InitialDataDefaults
import javax.inject.Inject

/**
 * Prepara dados seguros e idempotentes para os testes da variante Beta/Debug.
 * A variante Release não executa este caso de uso.
 */
class InitializeDemoDataUseCase @Inject constructor(
    private val database: AppDatabase,
    private val passwordHasher: PasswordHasher,
) {
    suspend operator fun invoke() = database.withTransaction {
        val league = database.leagueDao().findByName(InitialDataDefaults.FIRST_LEAGUE_NAME)
            ?: return@withTransaction

        val clubs = DEMO_CLUBS.mapIndexed { index, demo ->
            val userId = database.userDao().findByUsername(demo.username)?.id ?: run {
                val salt = passwordHasher.generateSalt()
                val hash = passwordHasher.hash(DEMO_PASSWORD.toCharArray(), salt)
                database.userDao().insert(
                    UserEntity(
                        username = demo.username,
                        displayName = demo.president,
                        passwordHash = hash,
                        passwordSalt = salt,
                        role = UserRole.PRESIDENT,
                        status = AccountStatus.ACTIVE,
                    ),
                )
            }

            val club = database.clubDao().findByLeagueAndName(league.id, demo.club)
            val clubId = club?.id ?: database.clubDao().insert(
                ClubEntity(
                    leagueId = league.id,
                    name = demo.club,
                    presidentUserId = userId,
                    isBank = false,
                    isActive = true,
                ),
            )

            val bank = requireNotNull(database.clubDao().findBankByLeague(league.id)) {
                "O Banco da Liga é obrigatório para inicializar os dados de demonstração."
            }

            if (database.financialDao().countByClubAndType(clubId, "INITIAL_BALANCE") == 0) {
                val now = System.currentTimeMillis()
                database.financialDao().insert(
                    FinancialTransactionEntity(
                        clubId = clubId,
                        amountCr = INITIAL_BALANCE_CR,
                        description = "Saldo inicial da demonstração Beta",
                        type = "INITIAL_BALANCE",
                        counterpartyClubId = bank.id,
                        createdAt = now,
                    ),
                )
                database.financialDao().insert(
                    FinancialTransactionEntity(
                        clubId = bank.id,
                        amountCr = -INITIAL_BALANCE_CR,
                        description = "Carga de saldo inicial: ${demo.club}",
                        type = "INITIAL_BALANCE",
                        counterpartyClubId = clubId,
                        createdAt = now,
                    ),
                )
            }
            DemoClubResult(clubId = clubId, seed = index + 1)
        }

        val competitionId = database.competitionDao()
            .findByLeagueAndName(league.id, DEMO_COMPETITION_NAME)?.id
            ?: database.competitionDao().insert(
                CompetitionEntity(
                    leagueId = league.id,
                    name = DEMO_COMPETITION_NAME,
                    type = CompetitionType.LEAGUE,
                    format = CompetitionFormat.HOME_AND_AWAY,
                    status = CompetitionStatus.ACTIVE,
                    pointsForWin = 3,
                    pointsForDraw = 1,
                    pointsForLoss = 0,
                ),
            )

        val seasonId = database.seasonDao().findByCompetitionAndNumber(competitionId, 1)?.id
            ?: database.seasonDao().insert(
                SeasonEntity(
                    competitionId = competitionId,
                    number = 1,
                    name = "Temporada 1",
                    status = SeasonStatus.ACTIVE,
                    startedAt = System.currentTimeMillis(),
                ),
            )

        database.competitionParticipantDao().insertAll(
            clubs.map {
                CompetitionParticipantEntity(
                    seasonId = seasonId,
                    clubId = it.clubId,
                    seed = it.seed,
                    isActive = true,
                )
            },
        )
    }

    private data class DemoClub(
        val club: String,
        val president: String,
        val username: String,
    )

    private data class DemoClubResult(val clubId: Long, val seed: Int)

    private companion object {
        const val DEMO_PASSWORD = "demo123"
        const val INITIAL_BALANCE_CR = 500L
        const val DEMO_COMPETITION_NAME = "Liga Principal"

        val DEMO_CLUBS = listOf(
            DemoClub("Torino", "Tomascote", "tomascote"),
            DemoClub("Fluminense", "Richemont", "richemont"),
            DemoClub("Bournemouth", "Pipocacr7", "pipocacr7"),
            DemoClub("Girona", "LSuperax", "lsuperax"),
        )
    }
}
