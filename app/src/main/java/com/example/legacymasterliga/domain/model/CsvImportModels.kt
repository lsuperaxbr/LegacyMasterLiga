package com.example.legacymasterliga.domain.model

data class CsvRawPlayer(
    val lineNum: Int,
    val name: String,
    val csvTeam: String,
    val position: String? = null,
    val overall: Int? = null,
    val heightCm: Int? = null,
    val preferredFoot: String? = null,
    val nationality: String? = null,
    val shirtNumber: Int? = null,
    val attributesRaw: String? = null,
)

data class CsvParseResult(
    val players: List<CsvRawPlayer>,
    val errors: List<String>,
    val teamPlayerCounts: Map<String, Int> = emptyMap(),
    val firstRawLine: String? = null,
)

data class CsvTeamMapping(
    val csvTeam: String,
    val targetClubId: Long? = null,
    val isBank: Boolean = false,
    val createNewClub: Boolean = false,
    val ignore: Boolean = false,
    val isConfirmed: Boolean = false,
)

data class CsvImportProgress(
    val currentChunk: Int,
    val totalChunks: Int,
    val processedPlayers: Int,
    val totalPlayers: Int,
)

data class CsvImportSummary(
    val createdClubsCount: Int = 0,
    val importedCountByClub: Map<String, Int> = emptyMap(),
    val bankPlayersCount: Int = 0,
    val newClubPlayersCount: Int = 0,
    val ignoredTeams: List<String> = emptyList(),
    val ignoredPlayersCount: Int = 0,
    val skippedDuplicates: Int = 0,
    val lineErrors: List<String> = emptyList(),
)
