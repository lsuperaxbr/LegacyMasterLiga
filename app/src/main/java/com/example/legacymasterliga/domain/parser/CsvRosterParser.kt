package com.example.legacymasterliga.domain.parser

import com.example.legacymasterliga.domain.PlayerAttributesParser
import com.example.legacymasterliga.domain.model.CsvParseResult
import com.example.legacymasterliga.domain.model.CsvRawPlayer
import java.io.InputStream

object CsvRosterParser {

    private val POSITION_INDEX_MAP = mapOf(
        0 to "GOL",
        1 to "ZAG",
        2 to "LD",
        3 to "LE",
        4 to "VOL",
        5 to "MC",
        6 to "MEI",
        7 to "MD",
        8 to "ME",
        9 to "PD",
        10 to "PE",
        11 to "SA",
        12 to "CA"
    )

    fun parse(inputStream: InputStream): CsvParseResult {
        val lines = inputStream.bufferedReader(Charsets.UTF_8).readLines()
        if (lines.isEmpty()) {
            return CsvParseResult(emptyList(), listOf("O arquivo CSV está vazio."))
        }

        // Detectar delimitador predominantemente usado (, ou ;)
        val sampleText = lines.take(10).joinToString("\n")
        val semicolonCount = sampleText.count { it == ';' }
        val commaCount = sampleText.count { it == ',' }
        val delimiter = if (semicolonCount > commaCount) ';' else ','

        val players = mutableListOf<CsvRawPlayer>()
        val errors = mutableListOf<String>()

        var nameCol = 0
        var teamCol = 1
        var posCol = 2
        var ovrCol = 3
        var heightCol = 4
        var footCol = 5
        var natCol = 6
        var shirtCol = 7
        var attrStartCol = 8

        var startIndex = 0

        // Verificar se a primeira linha é cabeçalho
        val firstTokens = parseTokens(lines[0], delimiter)
        val isHeader = firstTokens.any { token ->
            val clean = token.lowercase()
            clean.contains("name") || clean.contains("nome") || clean.contains("player") ||
                    clean.contains("team") || clean.contains("time") || clean.contains("club") || clean.contains("equipe")
        }

        if (isHeader) {
            startIndex = 1
            firstTokens.forEachIndexed { idx, token ->
                val clean = token.lowercase()
                when {
                    clean.contains("team") || clean.contains("time") || clean.contains("club") || clean.contains("equipe") -> teamCol = idx
                    clean.contains("name") || clean.contains("nome") || clean.contains("player") || clean.contains("jogador") -> nameCol = idx
                    clean == "pos" || clean.contains("posição") || clean.contains("posicao") -> posCol = idx
                    clean.contains("ovr") || clean.contains("overall") || clean.contains("geral") -> ovrCol = idx
                    clean.contains("alt") || clean.contains("height") -> heightCol = idx
                    clean.contains("pé") || clean.contains("pe") || clean.contains("foot") -> footCol = idx
                    clean.contains("nac") || clean.contains("nation") -> natCol = idx
                    clean.contains("camisa") || clean.contains("shirt") || clean.contains("number") || clean == "no" -> shirtCol = idx
                    clean == "attack" || clean == "att" -> attrStartCol = idx
                }
            }
        }

        for (i in startIndex until lines.size) {
            val rawLine = lines[i]
            if (rawLine.isBlank()) continue
            val lineNum = i + 1

            val tokens = parseTokens(rawLine, delimiter)
            val name = tokens.getOrNull(nameCol)?.trim().orEmpty()
            val team = tokens.getOrNull(teamCol)?.trim().orEmpty()

            if (name.isBlank() || team.isBlank()) {
                errors.add("Linha $lineNum: Dados incompletos (nome ou time em branco).")
                continue
            }

            val rawPos = tokens.getOrNull(posCol)?.trim().orEmpty()
            val pos = parsePosition(rawPos)
            val ovr = tokens.getOrNull(ovrCol)?.toIntOrNull()

            var heightCm: Int? = null
            var preferredFoot: String? = null
            var nationality: String? = null
            var shirtNumber: Int? = null
            var attributesRaw: String? = null

            // Se a linha contiver mais de 4 colunas (formato estendido)
            if (tokens.size > 4) {
                heightCm = tokens.getOrNull(heightCol)?.toIntOrNull()
                preferredFoot = tokens.getOrNull(footCol)?.trim()?.takeIf { it.isNotBlank() }
                nationality = tokens.getOrNull(natCol)?.trim()?.takeIf { it.isNotBlank() }
                shirtNumber = tokens.getOrNull(shirtCol)?.toIntOrNull()

                // Se houver colunas de atributos PES6 (26 atributos)
                if (tokens.size >= attrStartCol + 26) {
                    val attrValues = (0 until 26).map { offset ->
                        val valStr = tokens.getOrNull(attrStartCol + offset)?.trim().orEmpty()
                        val num = valStr.toIntOrNull()
                        if (num != null && num in 0..99) num.toString() else ""
                    }
                    if (attrValues.any { it.isNotBlank() }) {
                        attributesRaw = attrValues.joinToString(",")
                    }
                }
            }

            players.add(
                CsvRawPlayer(
                    lineNum = lineNum,
                    name = name,
                    csvTeam = team,
                    position = pos,
                    overall = ovr,
                    heightCm = heightCm,
                    preferredFoot = preferredFoot,
                    nationality = nationality,
                    shirtNumber = shirtNumber,
                    attributesRaw = attributesRaw
                )
            )
        }

        return CsvParseResult(players = players, errors = errors)
    }

    private fun parsePosition(rawPos: String): String? {
        if (rawPos.isBlank()) return null
        // 1. Tentar parse como índice numérico 0-12
        val index = rawPos.toIntOrNull()
        if (index != null && index in POSITION_INDEX_MAP) {
            return POSITION_INDEX_MAP[index]
        }
        // 2. Tentar parse por texto padrão
        val clean = rawPos.uppercase()
        val validPositions = setOf("GOL", "GK", "ZAG", "CB", "LD", "RB", "LE", "LB", "VOL", "DMF", "MC", "CMF", "MEI", "AMF", "MD", "RMF", "ME", "LMF", "PD", "RWF", "PE", "LWF", "SA", "SS", "CA", "CF")
        if (clean in validPositions) {
            return when (clean) {
                "GK" -> "GOL"
                "CB" -> "ZAG"
                "RB" -> "LD"
                "LB" -> "LE"
                "DMF" -> "VOL"
                "CMF" -> "MC"
                "AMF" -> "MEI"
                "RMF" -> "MD"
                "LMF" -> "ME"
                "RWF" -> "PD"
                "LWF" -> "PE"
                "SS" -> "SA"
                "CF" -> "CA"
                else -> clean
            }
        }
        return null
    }

    private fun parseTokens(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == delimiter && !inQuotes -> {
                    tokens.add(sb.toString().trim().removeSurrounding("\""))
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        tokens.add(sb.toString().trim().removeSurrounding("\""))
        return tokens
    }
}
