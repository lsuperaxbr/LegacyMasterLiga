package com.example.legacymasterliga.domain.usecase

import android.os.Environment
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExportPes6Summary(
    val exportPath: String,
    val totalClubsExported: Int,
    val totalPlayersExported: Int,
    val isCleanedFirst: Boolean
)

@Singleton
class ExportPes6PlvrUseCase @Inject constructor(
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
) {
    suspend operator fun invoke(
        leagueId: Long,
        cleanExport: Boolean = false
    ): Result<ExportPes6Summary> = withContext(Dispatchers.IO) {
        runCatching {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val rootDir = File(downloadsDir, "LegacyMasterLiga")

            if (cleanExport && rootDir.exists()) {
                rootDir.deleteRecursively()
            }

            if (!rootDir.exists()) {
                rootDir.mkdirs()
            }

            val clubs = clubDao.findAllByLeague(leagueId)
            var totalPlayersCount = 0

            clubs.forEach { club ->
                val folderName = if (club.isBank) {
                    "Banco_da_Liga"
                } else {
                    sanitizeName(club.name)
                }

                val clubDir = File(rootDir, folderName)
                if (!clubDir.exists()) {
                    clubDir.mkdirs()
                }

                val players = playerDao.findActiveByClub(club.id)
                players.forEach { player ->
                    val fileName = "${sanitizeName(player.name)}.txt"
                    val playerFile = File(clubDir, fileName)
                    val content = buildPlayerTxtContent(player)
                    playerFile.writeText(content, StandardCharsets.UTF_8)
                    totalPlayersCount++
                }
            }

            ExportPes6Summary(
                exportPath = rootDir.absolutePath,
                totalClubsExported = clubs.size,
                totalPlayersExported = totalPlayersCount,
                isCleanedFirst = cleanExport
            )
        }
    }

    fun sanitizeName(name: String): String {
        val clean = name.trim()
        val nfd = Normalizer.normalize(clean, Normalizer.Form.NFD)
        val noAccents = nfd.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        val sanitized = noAccents.replace(Regex("[^a-zA-Z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return if (sanitized.isBlank()) "Sem_Nome" else sanitized
    }

    fun buildPlayerTxtContent(player: PlayerEntity): String {
        val ovr = player.overall ?: 70
        val pos = player.position?.takeIf { it.isNotBlank() } ?: "MC"
        val heightStr = player.heightCm?.toString().orEmpty()
        val footStr = player.preferredFoot?.takeIf { it.isNotBlank() }?.let {
            if (it.startsWith("L", ignoreCase = true) || it.contains("E", ignoreCase = true)) "L" else "R"
        }.orEmpty()

        val attrs = parseAttributesRaw(player.attributesRaw, ovr)

        val sb = StringBuilder()
        sb.append("Name: ").append(player.name).append("\n")
        sb.append("Position: ").append(pos).append("\n")
        sb.append("OVR: ").append(ovr).append("\n")
        sb.append("Age: 25\n")
        sb.append("Foot: ").append(footStr).append("\n")
        sb.append("Height: ").append(heightStr).append("\n")

        val attrKeys = listOf(
            "Attack", "Defence", "Balance", "Stamina", "Speed", "Acceleration",
            "Response", "Agility", "Dribble_Accuracy", "Dribble_Speed",
            "Short_Pass_Accuracy", "Short_Pass_Speed", "Long_Pass_Accuracy", "Long_Pass_Speed",
            "Shot_Accuracy", "Shot_Power", "Shot_Technique", "Free_Kick_Accuracy",
            "Swerve", "Heading", "Jump", "Team_Work", "Technique", "Aggression",
            "Mentality", "GK_Skills"
        )

        attrKeys.forEachIndexed { idx, key ->
            val valNum = attrs.getOrElse(idx) { ovr }
            sb.append(key).append(": ").append(valNum).append("\n")
        }

        return sb.toString()
    }

    private fun parseAttributesRaw(raw: String?, fallbackOvr: Int): List<Int> {
        if (raw.isNullOrBlank()) return List(26) { fallbackOvr }
        val parts = raw.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (parts.size >= 26) return parts.take(26)

        val result = parts.toMutableList()
        while (result.size < 26) {
            result.add(fallbackOvr)
        }
        return result
    }
}
