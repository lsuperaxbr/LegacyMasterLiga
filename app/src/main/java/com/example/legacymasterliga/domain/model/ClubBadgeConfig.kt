package com.example.legacymasterliga.domain.model

data class ClubBadgeConfig(
    val shape: String = "shield",
    val pattern: String = "solid",
    val primaryColor: String = "#808080",
    val secondaryColor: String = "#FFFFFF",
    val accentColor: String = "#000000",
    val symbol: String = "initial"
) {
    companion object {
        val DEFAULT = ClubBadgeConfig(
            shape = "shield",
            pattern = "solid",
            primaryColor = "#808080",
            secondaryColor = "#FFFFFF",
            accentColor = "#000000",
            symbol = "initial"
        )

        val AVAILABLE_SHAPES = listOf("shield", "circle", "diamond", "hexagon", "crest")
        val AVAILABLE_PATTERNS = listOf(
            "solid", "vertical_stripes", "horizontal_stripes",
            "diagonal_stripes", "half", "cross", "central_circle"
        )
        val AVAILABLE_SYMBOLS = listOf("initial", "star", "crown", "lightning", "ball", "tree", "none")

        val PALETTE_COLORS = listOf(
            "#808080" to "Cinza",
            "#FF0000" to "Vermelho",
            "#00E6C8" to "Ciano",
            "#FFD700" to "Dourado",
            "#2E7D32" to "Verde",
            "#1976D2" to "Azul",
            "#7B1FA2" to "Roxo",
            "#F57C00" to "Laranja",
            "#FFFFFF" to "Branco",
            "#000000" to "Preto"
        )

        fun extractInitial(clubName: String): String {
            val clean = clubName.trim()
            if (clean.isBlank()) return "C"
            return clean.first().uppercase()
        }

        fun toJson(config: ClubBadgeConfig): String {
            return """{"shape":"${config.shape}","pattern":"${config.pattern}","primaryColor":"${config.primaryColor}","secondaryColor":"${config.secondaryColor}","accentColor":"${config.accentColor}","symbol":"${config.symbol}"}"""
        }

        fun fromJson(json: String?, fallbackRawCrest: String? = null, clubName: String = ""): ClubBadgeConfig {
            if (!json.isNullOrBlank()) {
                val parsed = runCatching {
                    val shape = extractJsonValue(json, "shape") ?: "shield"
                    val pattern = extractJsonValue(json, "pattern") ?: "solid"
                    val primaryColor = extractJsonValue(json, "primaryColor") ?: "#808080"
                    val secondaryColor = extractJsonValue(json, "secondaryColor") ?: "#FFFFFF"
                    val accentColor = extractJsonValue(json, "accentColor") ?: "#000000"
                    val symbol = extractJsonValue(json, "symbol") ?: "initial"
                    ClubBadgeConfig(shape, pattern, primaryColor, secondaryColor, accentColor, symbol)
                }.getOrNull()
                if (parsed != null) return parsed
            }

            if (!fallbackRawCrest.isNullOrBlank()) {
                val legacy = runCatching {
                    val parts = fallbackRawCrest.split(";")
                    if (parts.size >= 3) {
                        val shape = when (parts[0]) {
                            "CIRCLE" -> "circle"
                            "DIAMOND" -> "diamond"
                            "OVAL" -> "circle"
                            "SHIELD_THIN" -> "crest"
                            else -> "shield"
                        }
                        val pattern = when (parts.getOrNull(3)) {
                            "STRIPES_VERTICAL" -> "vertical_stripes"
                            "STRIPES_HORIZONTAL" -> "horizontal_stripes"
                            "DIAGONAL" -> "diagonal_stripes"
                            else -> "solid"
                        }
                        ClubBadgeConfig(
                            shape = shape,
                            pattern = pattern,
                            primaryColor = parts.getOrNull(1) ?: "#808080",
                            secondaryColor = parts.getOrNull(2) ?: "#FFFFFF",
                            accentColor = parts.getOrNull(4) ?: "#000000",
                            symbol = "initial"
                        )
                    } else null
                }.getOrNull()
                if (legacy != null) return legacy
            }

            return DEFAULT
        }

        private fun extractJsonValue(json: String, key: String): String? {
            val pattern = Regex(""""$key"\s*:\s*"([^"]+)"""")
            return pattern.find(json)?.groupValues?.get(1)
        }
    }
}
