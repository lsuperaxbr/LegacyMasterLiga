package com.example.legacymasterliga.domain.usecase

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.security.PasswordHasher
import com.example.legacymasterliga.domain.model.BootstrapResult
import com.example.legacymasterliga.domain.model.InitialDataDefaults
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.domain.repository.UserRepository
import javax.inject.Inject

class InitializeDefaultDataUseCase @Inject constructor(
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val leagueRepository: LeagueRepository,
    private val clubRepository: ClubRepository,
    private val passwordHasher: PasswordHasher,
) {
    suspend operator fun invoke(): BootstrapResult = database.withTransaction {
        val existingAdministrator = userRepository.findByUsername(
            InitialDataDefaults.ADMIN_USERNAME,
        )
        val administratorCreated = existingAdministrator == null
        val administratorId = existingAdministrator?.id ?: run {
            val salt = passwordHasher.generateSalt()
            val hash = passwordHasher.hash(
                InitialDataDefaults.TEMPORARY_ADMIN_PASSWORD.toCharArray(),
                salt,
            )
            userRepository.create(
                username = InitialDataDefaults.ADMIN_USERNAME,
                displayName = InitialDataDefaults.ADMIN_DISPLAY_NAME,
                passwordHash = hash,
                passwordSalt = salt,
                role = UserRole.ADMINISTRATOR,
            )
        }

        val existingLeague = leagueRepository.findByName(
            InitialDataDefaults.FIRST_LEAGUE_NAME,
        )
        val leagueId = existingLeague?.id ?: leagueRepository.create(
            name = InitialDataDefaults.FIRST_LEAGUE_NAME,
            currencyCode = InitialDataDefaults.CURRENCY_CODE,
        )

        val existingBank = clubRepository.findLeagueBank(leagueId)
        val leagueBankCreated = existingBank == null
        val finalBankId = if (leagueBankCreated) {
            clubRepository.createLeagueBank(
                leagueId = leagueId,
                name = InitialDataDefaults.LEAGUE_BANK_NAME,
            )
        } else {
            existingBank?.id ?: 0L
        }

        BootstrapResult(
            administratorCreated = administratorCreated,
            leagueCreated = existingLeague == null,
            leagueBankCreated = leagueBankCreated,
            administratorId = administratorId,
            leagueId = leagueId,
            leagueBankId = finalBankId,
        )
    }
}
