package com.example.legacymasterliga.domain.usecase

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.security.PasswordHasher
import com.example.legacymasterliga.domain.model.InitialDataDefaults
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminLeagueSetupUseCase @Inject constructor(
    private val database: AppDatabase,
    private val passwordHasher: PasswordHasher,
) {
    suspend operator fun invoke() = database.withTransaction {
        val league = database.leagueDao().findByName(InitialDataDefaults.FIRST_LEAGUE_NAME)
            ?: return@withTransaction

        SETUP_DATA.forEach { setup ->
            // 1. Garantir Usuário e Resetar Senha
            val salt = passwordHasher.generateSalt()
            val hash = passwordHasher.hash(DEFAULT_PASSWORD.toCharArray(), salt)
            
            val existingUser = database.userDao().findByUsername(setup.username)
            val userId = if (existingUser == null) {
                database.userDao().insert(
                    UserEntity(
                        username = setup.username,
                        displayName = setup.displayName,
                        passwordHash = hash,
                        passwordSalt = salt,
                        role = UserRole.PRESIDENT,
                        status = AccountStatus.ACTIVE,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                // Se o usuário já estiver vinculado ao Firebase, NÃO alteramos perfil nem senha locais
                // para evitar conflitos de sincronização ou deslogues inesperados.
                if (existingUser.firebaseUid == null) {
                    database.userDao().updateProfile(existingUser.id, setup.displayName, UserRole.PRESIDENT, AccountStatus.ACTIVE)
                    database.userDao().updatePassword(existingUser.id, hash, salt)
                }
                existingUser.id
            }

            // 2. Vincular Clubes
            setup.clubs.forEach { clubName ->
                val existingClub = database.clubDao().findByLeagueAndName(league.id, clubName)
                if (existingClub == null) {
                    database.clubDao().insert(
                        ClubEntity(
                            leagueId = league.id,
                            name = clubName,
                            presidentUserId = userId,
                            isBank = false,
                            isActive = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    database.clubDao().assignPresident(existingClub.id, userId, System.currentTimeMillis())
                }
            }
        }
    }

    private data class UserSetup(
        val username: String,
        val displayName: String,
        val clubs: List<String>
    )

    private companion object {
        const val DEFAULT_PASSWORD = "123456"
        
        val SETUP_DATA = listOf(
            UserSetup("richemont", "Rafael", listOf("Pisa", "RB Salzburg")),
            UserSetup("tomascote", "Anderson", listOf("LDU", "Atlethico PR")),
            UserSetup("pipocacr7", "Marcus", listOf("Vasco", "Levante")),
            UserSetup("lsuperax", "Luiz", listOf("Chapecoense", "ST. Pauli"))
        )
    }
}
