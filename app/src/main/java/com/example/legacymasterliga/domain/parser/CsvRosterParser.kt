package com.example.legacymasterliga.domain.parser

import com.example.legacymasterliga.domain.model.CsvParseResult
import com.example.legacymasterliga.domain.model.CsvRawPlayer
import java.io.InputStream

object CsvRosterParser {

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

        var nameCol = -1
        var teamCol = -1
        var posCol = -1
        var ovrCol = -1

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
                    clean.contains("pos") -> posCol = idx
                    clean.contains("ovr") || clean.contains("overall") || clean.contains("geral") -> ovrCol = idx
                }
            }
        }

        // Fallbacks se não for possível inferir pelo cabeçalho
        if (nameCol == -1 || teamCol == -1) {
            // Tenta heurística padrão: Coluna 0 Name, Coluna 1 Team (ou vice-versa)
            if (firstTokens.size >= 2) {
                if (teamCol == -1) teamCol = if (nameCol == 0) 1 else 0
                if (nameCol == -1) nameCol = if (teamCol == 0) 1 else 0
            } else {
                nameCol = 0
                teamCol = 1
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

            val pos = if (posCol in tokens.indices) tokens[posCol].takeIf { it.isNotBlank() } else null
            val ovr = if (ovrCol in tokens.indices) tokens[ovrCol].toIntOrNull() else null

            players.add(
                CsvRawPlayer(
                    lineNum = lineNum,
                    name = name,
                    csvTeam = team,
                    position = pos,
                    overall = ovr
                )
            )
        }

        return CsvParseResult(players = players, errors = errors)
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
