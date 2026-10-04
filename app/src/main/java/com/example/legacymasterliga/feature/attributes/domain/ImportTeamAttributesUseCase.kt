package com.example.legacymasterliga.feature.attributes.domain

import com.example.legacymasterliga.core.database.dao.PlayerDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImportTeamAttributesResult(
    val totalParsed: Int = 0,
    val totalUpdated: Int = 0,
    val unmappedPlayerNames: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

@Singleton
class ImportTeamAttributesUseCase @Inject constructor(
    private val playerDao: PlayerDao
) {
    suspend operator fun invoke(
        clubId: Long,
        rawText: String
    ): ImportTeamAttributesResult = withContext(Dispatchers.IO) {
        val blocks = ImportTeamAttributesParser.parse(rawText)
        if (blocks.isEmpty()) {
            return@withContext ImportTeamAttributesResult(
                errors = listOf("Nenhum atleta válido pôde ser extraído do texto informado.")
            )
        }

        var updatedCount = 0
        val unmappedNames = mutableListOf<String>()
        val errorsList = mutableListOf<String>()

        blocks.forEach { block ->
            val player = playerDao.findByNameAndClub(clubId, block.name)
            if (player == null) {
                unmappedNames.add(block.name)
                errorsList.add("Jogador '${block.name}' não encontrado no clube.")
            } else {
                val updatedPos = block.position ?: player.position
                val updatedOvr = block.overall ?: player.overall

                val updatedHeight = if (block.hasHeightKey) block.height else player.heightCm
                val updatedFoot = if (block.hasFootKey) block.foot else player.preferredFoot

                val updatedAttributesRaw = buildUpdatedAttributesRaw(
                    existingRaw = player.attributesRaw,
                    fallbackOvr = updatedOvr ?: 70,
                    newAttrMap = block.attributesByIndex
                )

                val updatedPlayer = player.copy(
                    position = updatedPos,
                    overall = updatedOvr,
                    heightCm = updatedHeight,
                    preferredFoot = updatedFoot,
                    attributesRaw = updatedAttributesRaw,
                    updatedAt = System.currentTimeMillis()
                )

                playerDao.update(updatedPlayer)
                updatedCount++
            }
        }

        ImportTeamAttributesResult(
            totalParsed = blocks.size,
            totalUpdated = updatedCount,
            unmappedPlayerNames = unmappedNames,
            errors = errorsList
        )
    }

    private fun buildUpdatedAttributesRaw(
        existingRaw: String?,
        fallbackOvr: Int,
        newAttrMap: Map<Int, Int>
    ): String {
        val existingAttrs = if (!existingRaw.isNullOrBlank()) {
            existingRaw.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else emptyList()

        val finalAttrs = (0 until 26).map { idx ->
            newAttrMap[idx] ?: existingAttrs.getOrElse(idx) { fallbackOvr }
        }

        return finalAttrs.joinToString(",")
    }
}
