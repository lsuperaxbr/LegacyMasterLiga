package com.example.legacymasterliga.feature.attributes.domain

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.domain.repository.PlayerRepository
import javax.inject.Inject

data class ImportSquadFolderResult(
    val totalFilesProcessed: Int = 0,
    val importedCount: Int = 0,
    val ignoredCount: Int = 0,
    val failedFiles: List<String> = emptyList(),
    val errorMessage: String? = null
)

class ImportSquadFromFolderUseCase @Inject constructor(
    private val database: AppDatabase,
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(
        clubId: Long,
        folderUri: Uri,
        context: Context
    ): ImportSquadFolderResult {
        val club = clubDao.findById(clubId)
            ?: return ImportSquadFolderResult(errorMessage = "Clube não encontrado.")

        val hasPlayedMatches = playerRepository.hasClubPlayedMatches(clubId)
        if (hasPlayedMatches) {
            return ImportSquadFolderResult(
                errorMessage = "Não é possível importar elenco porque o clube já possui partidas registradas."
            )
        }

        val treeFile = DocumentFile.fromTreeUri(context, folderUri)
            ?: return ImportSquadFolderResult(errorMessage = "Não foi possível acessar a pasta selecionada via SAF.")

        val files = treeFile.listFiles()
        if (files.isEmpty()) {
            return ImportSquadFolderResult(errorMessage = "A pasta selecionada está vazia.")
        }

        var ignoredCount = 0
        val failedFiles = mutableListOf<String>()
        val playersToInsert = mutableListOf<PlayerEntity>()

        for (file in files) {
            val fileName = file.name ?: "arquivo_desconhecido"
            if (!file.isFile || !fileName.endsWith(".txt", ignoreCase = true)) {
                ignoredCount++
                continue
            }

            try {
                val inputStream = context.contentResolver.openInputStream(file.uri)
                if (inputStream == null) {
                    failedFiles.add(fileName)
                    continue
                }
                val textContent = inputStream.bufferedReader().use { it.readText() }

                val parsed = Pes6PlayerTextParser.parse(
                    text = textContent,
                    defaultName = fileName.removeSuffix(".txt").removeSuffix(".TXT")
                )

                playersToInsert.add(
                    PlayerEntity(
                        leagueId = club.leagueId,
                        clubId = clubId,
                        name = parsed.name,
                        position = parsed.position,
                        overall = parsed.overall,
                        heightCm = parsed.heightCm,
                        preferredFoot = parsed.preferredFoot,
                        attributesRaw = parsed.attributesRaw,
                        isActive = true,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                failedFiles.add(fileName)
            }
        }

        if (playersToInsert.isEmpty() && failedFiles.isEmpty()) {
            return ImportSquadFolderResult(
                totalFilesProcessed = files.size,
                importedCount = 0,
                ignoredCount = ignoredCount,
                errorMessage = "Nenhum arquivo .txt válido encontrado na pasta selecionada."
            )
        }

        val importedCount: Int
        try {
            database.withTransaction {
                playerDao.deleteByClub(clubId)
                playersToInsert.forEach { player ->
                    playerDao.insert(player)
                }
            }
            importedCount = playersToInsert.size
        } catch (e: Exception) {
            return ImportSquadFolderResult(
                totalFilesProcessed = files.size,
                importedCount = 0,
                ignoredCount = ignoredCount,
                failedFiles = failedFiles,
                errorMessage = "Erro ao salvar no banco de dados: ${e.message}"
            )
        }

        return ImportSquadFolderResult(
            totalFilesProcessed = files.size,
            importedCount = importedCount,
            ignoredCount = ignoredCount,
            failedFiles = failedFiles
        )
    }
}
