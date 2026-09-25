package com.example.legacymasterliga.domain.model

data class CsvRawPlayer(
    val lineNum: Int,
    val name: String,
    val csvTeam: String,
    val position: String? = null,
    val overall: Int? = null,
)

data class CsvParseResult(
    val players: List<CsvRawPlayer>,
    val errors: List<String>,
)

data class CsvTeamMapping(
    val csvTeam: String,
    val targetClubId: Long? = null, // null significa "Ignorar este time" se isBank e ignore forem false
    val isBank: Boolean = false,
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
    val importedCountByClub: Map<String, Int>,
    val ignoredTeams: List<String>,
    val skippedDuplicates: Int,
    val lineErrors: List<String>,
)
