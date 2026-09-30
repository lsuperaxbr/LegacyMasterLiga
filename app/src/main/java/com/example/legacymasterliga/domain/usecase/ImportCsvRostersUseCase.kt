package com.example.legacymasterliga.domain.usecase

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.domain.model.CsvImportProgress
import com.example.legacymasterliga.domain.model.CsvImportSummary
import com.example.legacymasterliga.domain.model.CsvRawPlayer
import com.example.legacymasterliga.domain.model.CsvTeamMapping
import com.example.legacymasterliga.domain.model.InitialDataDefaults
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ImportCsvRostersUseCase @Inject constructor(
    private val database: AppDatabase,
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
) {
    suspend operator fun invoke(
        leagueId: Long,
        players: List<CsvRawPlayer>,
        parseErrors: List<String>,
        onProgress: (CsvImportProgress) -> Unit = {}
    ): CsvImportSummary = withContext(Dispatchers.IO) {
        val existingClubs = clubDao.findAllByLeague(leagueId)
        val distinctTeams = players.map { it.csvTeam }.distinct()

        val autoMappings = distinctTeams.map { team ->
            when {
                team.equals(InitialDataDefaults.LEAGUE_BANK_NAME, ignoreCase = true) || team.equals("Banco da Liga", ignoreCase = true) -> {
                    CsvTeamMapping(csvTeam = team, isBank = true)
                }
                else -> {
                    val existing = existingClubs.find { it.name.trim().equals(team.trim(), ignoreCase = true) }
                    if (existing != null) {
                        CsvTeamMapping(csvTeam = team, targetClubId = existing.id)
                    } else {
                        CsvTeamMapping(csvTeam = team, createNewClub = true)
                    }
                }
            }
        }

        return@withContext invoke(leagueId, autoMappings, players, parseErrors, onProgress)
    }

    suspend operator fun invoke(
        leagueId: Long,
        mappings: List<CsvTeamMapping>,
        players: List<CsvRawPlayer>,
        parseErrors: List<String>,
        onProgress: (CsvImportProgress) -> Unit = {}
    ): CsvImportSummary = withContext(Dispatchers.IO) {
        val mappingMap = mappings.associateBy { it.csvTeam }
        val ignoredTeams = mappings.filter { it.ignore || (!it.isBank && !it.createNewClub && it.targetClubId == null) }.map { it.csvTeam }

        val ignoredPlayersCount = players.count { raw ->
            val m = mappingMap[raw.csvTeam]
            m == null || m.ignore || (!m.isBank && !m.createNewClub && m.targetClubId == null)
        }

        val validPlayersToImport = players.filter { raw ->
            val m = mappingMap[raw.csvTeam]
            m != null && !m.ignore && (m.targetClubId != null || m.isBank || m.createNewClub)
        }

        val totalPlayers = validPlayersToImport.size
        val chunkSize = 500
        val chunks = validPlayersToImport.chunked(chunkSize)
        val totalChunks = chunks.size.coerceAtLeast(1)

        val importedCountByClub = mutableMapOf<String, Int>()
        val createdClubIds = mutableMapOf<String, Long>()
        var createdClubsCount = 0
        var bankPlayersCount = 0
        var newClubPlayersCount = 0
        var skippedDuplicates = 0
        var processedSoFar = 0

        val bankClub = clubDao.findBankByLeague(leagueId)

        // 1. Criar clubes novos on-the-fly dentro de transação atômica
        database.withTransaction {
            mappings.filter { it.createNewClub && !it.ignore }.forEach { m ->
                val existing = clubDao.findByLeagueAndName(leagueId, m.csvTeam)
                val clubId = if (existing != null) {
                    existing.id
                } else {
                    val newId = clubDao.insert(
                        ClubEntity(
                            leagueId = leagueId,
                            name = m.csvTeam,
                            crestUri = null, // Escudo nativo auto-gerado pelo NativeCrest
                            isBank = false,
                            isActive = true,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    createdClubsCount++
                    newId
                }
                createdClubIds[m.csvTeam] = clubId
            }
        }

        // 2. Inserir jogadores em blocos de ~500 com acompanhamento de progresso
        chunks.forEachIndexed { chunkIdx, chunk ->
            val now = System.currentTimeMillis()

            database.withTransaction {
                chunk.forEach { rawPlayer ->
                    val m = mappingMap[rawPlayer.csvTeam] ?: return@forEach
                    val targetClubId = when {
                        m.isBank -> bankClub?.id
                        m.createNewClub -> createdClubIds[m.csvTeam]
                        else -> m.targetClubId
                    }
                    if (targetClubId == null) return@forEach

                    val existing = playerDao.findByNameAndClub(targetClubId, rawPlayer.name)
                    if (existing != null) {
                        skippedDuplicates++
                    } else {
                        playerDao.insert(
                            PlayerEntity(
                                leagueId = leagueId,
                                clubId = targetClubId,
                                name = rawPlayer.name,
                                position = rawPlayer.position,
                                overall = rawPlayer.overall,
                                heightCm = rawPlayer.heightCm,
                                preferredFoot = rawPlayer.preferredFoot,
                                nationality = rawPlayer.nationality,
                                shirtNumber = rawPlayer.shirtNumber,
                                attributesRaw = rawPlayer.attributesRaw,
                                marketStatus = MarketStatus.NOT_LISTED,
                                createdAt = now,
                                updatedAt = now,
                            )
                        )

                        if (m.isBank) {
                            bankPlayersCount++
                        } else if (m.createNewClub) {
                            newClubPlayersCount++
                        }

                        val clubName = if (m.isBank) "Banco da Liga" else (clubDao.findById(targetClubId)?.name ?: "Clube #$targetClubId")
                        importedCountByClub[clubName] = (importedCountByClub[clubName] ?: 0) + 1
                    }
                }
            }

            processedSoFar += chunk.size
            onProgress(
                CsvImportProgress(
                    currentChunk = chunkIdx + 1,
                    totalChunks = totalChunks,
                    processedPlayers = processedSoFar,
                    totalPlayers = totalPlayers
                )
            )
        }

        CsvImportSummary(
            createdClubsCount = createdClubsCount,
            importedCountByClub = importedCountByClub,
            bankPlayersCount = bankPlayersCount,
            newClubPlayersCount = newClubPlayersCount,
            ignoredTeams = ignoredTeams,
            ignoredPlayersCount = ignoredPlayersCount,
            skippedDuplicates = skippedDuplicates,
            lineErrors = parseErrors
        )
    }
}
