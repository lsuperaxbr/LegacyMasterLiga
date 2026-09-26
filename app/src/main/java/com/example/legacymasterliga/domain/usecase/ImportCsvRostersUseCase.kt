package com.example.legacymasterliga.domain.usecase

import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.domain.model.CsvImportProgress
import com.example.legacymasterliga.domain.model.CsvImportSummary
import com.example.legacymasterliga.domain.model.CsvRawPlayer
import com.example.legacymasterliga.domain.model.CsvTeamMapping
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
        mappings: List<CsvTeamMapping>,
        players: List<CsvRawPlayer>,
        parseErrors: List<String>,
        onProgress: (CsvImportProgress) -> Unit = {}
    ): CsvImportSummary = withContext(Dispatchers.IO) {
        val mappingMap = mappings.associateBy { it.csvTeam }
        val ignoredTeams = mappings.filter { it.ignore || (it.targetClubId == null && !it.isBank) }.map { it.csvTeam }

        val validPlayersToImport = players.filter { raw ->
            val m = mappingMap[raw.csvTeam]
            m != null && !m.ignore && (m.targetClubId != null || m.isBank)
        }

        val totalPlayers = validPlayersToImport.size
        val chunkSize = 500
        val chunks = validPlayersToImport.chunked(chunkSize)
        val totalChunks = chunks.size.coerceAtLeast(1)

        val importedCountByClub = mutableMapOf<String, Int>()
        var skippedDuplicates = 0
        var processedSoFar = 0

        val bankClub = clubDao.findBankByLeague(leagueId)

        chunks.forEachIndexed { chunkIdx, chunk ->
            val now = System.currentTimeMillis()

            database.withTransaction {
                chunk.forEach { rawPlayer ->
                    val m = mappingMap[rawPlayer.csvTeam] ?: return@forEach
                    val targetClubId = if (m.isBank) bankClub?.id else m.targetClubId
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
            importedCountByClub = importedCountByClub,
            ignoredTeams = ignoredTeams,
            skippedDuplicates = skippedDuplicates,
            lineErrors = parseErrors
        )
    }
}
