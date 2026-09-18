package com.example.legacymasterliga.domain

object ClubCrestParser {
    val AVAILABLE_COLORS = listOf(
        "#00E6C8" to "Ciano",
        "#FFD700" to "Dourado",
        "#DC2828" to "Vermelho",
        "#2E7D32" to "Verde",
        "#1976D2" to "Azul",
        "#7B1FA2" to "Roxo",
        "#F57C00" to "Laranja",
        "#455A64" to "Cinza",
        "#FFFFFF" to "Branco",
        "#000000" to "Preto",
    )

    enum class CrestShape { CIRCLE, SHIELD, SHIELD_THIN, DIAMOND, OVAL }
    enum class CrestPattern { NONE, STRIPES_VERTICAL, STRIPES_HORIZONTAL, DIAGONAL }

    data class CrestData(
        val shape: CrestShape,
        val colorFill: String,
        val colorBorder: String,
        val pattern: CrestPattern,
        val colorPattern: String,
        val showInitials: Boolean,
        val initials: String,
    )

    fun defaultInitials(clubName: String): String {
        val words = clubName.trim().split(" ").filter { it.isNotBlank() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            words.size == 1 && words[0].length >= 2 -> words[0].take(2).uppercase()
            else -> clubName.take(2).uppercase()
        }
    }

    private fun validColor(hex: String?, fallback: String): String =
        hex?.takeIf { runCatching { android.graphics.Color.parseColor(it) }.isSuccess } ?: fallback

    fun encode(shape: CrestShape, colorFill: String, colorBorder: String, pattern: CrestPattern, colorPattern: String, showInitials: Boolean, initials: String): String {
        val cleanInitials = initials.trim().uppercase().take(3)
        return "${shape.name};$colorFill;$colorBorder;${pattern.name};$colorPattern;$showInitials;$cleanInitials"
    }

    fun decode(raw: String?, clubName: String): CrestData {
        val defaultInitials = defaultInitials(clubName)
        val default = CrestData(CrestShape.CIRCLE, "#00E6C8", "#FFFFFF", CrestPattern.NONE, "#FFFFFF", true, defaultInitials)
        if (raw.isNullOrBlank()) return default
        
        val parts = raw.split(";")
        return when (parts.size) {
            7 -> CrestData(
                shape = runCatching { CrestShape.valueOf(parts[0]) }.getOrDefault(CrestShape.CIRCLE),
                colorFill = validColor(parts.getOrNull(1), "#00E6C8"),
                colorBorder = validColor(parts.getOrNull(2), "#FFFFFF"),
                pattern = runCatching { CrestPattern.valueOf(parts[3]) }.getOrDefault(CrestPattern.NONE),
                colorPattern = validColor(parts.getOrNull(4), "#FFFFFF"),
                showInitials = parts.getOrNull(5)?.toBooleanStrictOrNull() ?: true,
                initials = parts.getOrNull(6)?.takeIf { it.isNotBlank() } ?: defaultInitials,
            )
            5 -> CrestData( // formato anterior (forma, corFundo, corBorda, mostrarIniciais, iniciais)
                shape = runCatching { CrestShape.valueOf(parts[0]) }.getOrDefault(CrestShape.CIRCLE),
                colorFill = validColor(parts.getOrNull(1), "#00E6C8"),
                colorBorder = validColor(parts.getOrNull(2), "#FFFFFF"),
                pattern = CrestPattern.NONE,
                colorPattern = validColor(parts.getOrNull(2), "#FFFFFF"),
                showInitials = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: true,
                initials = parts.getOrNull(4)?.takeIf { it.isNotBlank() } ?: defaultInitials,
            )
            3 -> CrestData( // formato ainda mais antigo (corFundo, corBorda, iniciais)
                shape = CrestShape.CIRCLE,
                colorFill = validColor(parts.getOrNull(0), "#00E6C8"),
                colorBorder = validColor(parts.getOrNull(1), "#FFFFFF"),
                pattern = CrestPattern.NONE,
                colorPattern = validColor(parts.getOrNull(1), "#FFFFFF"),
                showInitials = true,
                initials = parts.getOrNull(2)?.takeIf { it.isNotBlank() } ?: defaultInitials,
            )
            else -> CrestData( // formato original (uma cor só)
                shape = CrestShape.CIRCLE,
                colorFill = validColor(raw, "#00E6C8"),
                colorBorder = "#FFFFFF",
                pattern = CrestPattern.NONE,
                colorPattern = "#FFFFFF",
                showInitials = true,
                initials = defaultInitials,
            )
        }
    }
}
