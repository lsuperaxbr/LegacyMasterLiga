package com.example.legacymasterliga.domain.parser

import android.util.Log
import com.example.legacymasterliga.domain.model.CsvParseResult
import com.example.legacymasterliga.domain.model.CsvRawPlayer
import com.example.legacymasterliga.domain.model.InitialDataDefaults
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.text.Normalizer

object CsvRosterParser {

    private const val TAG = "CsvRosterParser"

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
        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) {
            return CsvParseResult(emptyList(), listOf("O arquivo CSV está vazio."))
        }

        // 1. Rejeitar arquivos XLSX renomeados (magic bytes "PK\x03\x04")
        if (bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) {
            return CsvParseResult(
                players = emptyList(),
                errors = listOf("Arquivo inválido. Envie um CSV de texto, não uma planilha Excel renomeada.")
            )
        }

        // 2. Decodificação UTF-8 com fallback para Windows-1252
        val rawText = try {
            val decoder = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (e: Exception) {
            String(bytes, Charset.forName("Windows-1252"))
        }

        val text = rawText.removePrefix("\uFEFF").removePrefix("\uFFFE")
        val lines = text.lines().map { it.trimEnd('\r', '\n') }
        if (lines.isEmpty() || lines.all { cleanToken(it).isBlank() }) {
            return CsvParseResult(emptyList(), listOf("O arquivo CSV está vazio."))
        }

        val firstLine = lines.firstOrNull().orEmpty()
        val firstRawLineFormatted = formatRawLineForDisplay(firstLine)

        Log.d(TAG, "DIAGNOSTICO CSV: Total linhas=${lines.size}, Primeira linha: $firstRawLineFormatted")

        // 3. Detecção real de delimitador na primeira linha não-vazia
        val firstNonEmptyLine = lines.firstOrNull { cleanToken(it).isNotBlank() }
            ?.let { cleanToken(it) }.orEmpty()

        val counts = mapOf(
            ',' to firstNonEmptyLine.count { it == ',' },
            ';' to firstNonEmptyLine.count { it == ';' },
            '\t' to firstNonEmptyLine.count { it == '\t' },
            '|' to firstNonEmptyLine.count { it == '|' }
        )

        val maxCount = counts.values.maxOrNull() ?: 0
        val delimiter = if (maxCount == 0) ',' else {
            listOf(',', ';', '\t', '|').first { counts[it] == maxCount }
        }

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

        // 4. Verificar se a primeira linha é cabeçalho com normalização NFD
        val firstTokens = parseTokens(lines[0], delimiter)
        val isHeader = firstTokens.any { token ->
            val norm = normalize(token)
            norm in setOf("nome", "name", "player", "jogador", "time", "team", "club", "clube", "equipe", "pos", "posicao", "position", "ovr", "overall", "geral")
        }

        if (isHeader) {
            startIndex = 1
            firstTokens.forEachIndexed { idx, token ->
                val norm = normalize(token)
                when {
                    norm in setOf("team", "time", "club", "clube", "equipe") -> teamCol = idx
                    norm in setOf("name", "nome", "player", "jogador") -> nameCol = idx
                    norm in setOf("pos", "posicao", "position") -> posCol = idx
                    norm in setOf("ovr", "overall", "geral") -> ovrCol = idx
                    norm in setOf("altura", "height", "alt") -> heightCol = idx
                    norm in setOf("pe", "foot", "preferred foot") -> footCol = idx
                    norm in setOf("nacionalidade", "nation", "nac", "nationality") -> natCol = idx
                    norm in setOf("camisa", "shirt", "number", "no") -> shirtCol = idx
                    norm in setOf("attack", "att") -> attrStartCol = idx
                }
            }
        }

        // 5. Processamento e validação de cada linha
        for (i in startIndex until lines.size) {
            val rawLine = lines[i]
            if (rawLine.isBlank()) continue
            val lineNum = i + 1

            val tokens = parseTokens(rawLine, delimiter)
            if (tokens.size < 2) {
                errors.add("Linha $lineNum: Delimitador não reconhecido ou colunas insuficientes (apenas 1 coluna encontrada).")
                continue
            }

            val name = tokens.getOrNull(nameCol)?.let { cleanToken(it) }.orEmpty()
            val team = tokens.getOrNull(teamCol)?.let { cleanToken(it) }.orEmpty()

            if (name.isBlank()) {
                errors.add("Linha $lineNum: Dados incompletos (nome em branco).")
                continue
            }

            val assignedTeam = if (team.isBlank()) InitialDataDefaults.LEAGUE_BANK_NAME else team

            val rawPos = tokens.getOrNull(posCol)?.let { cleanToken(it) }.orEmpty()
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
                preferredFoot = tokens.getOrNull(footCol)?.let { cleanToken(it) }?.takeIf { it.isNotBlank() }
                nationality = tokens.getOrNull(natCol)?.let { cleanToken(it) }?.takeIf { it.isNotBlank() }
                shirtNumber = tokens.getOrNull(shirtCol)?.toIntOrNull()

                // Se houver colunas de atributos PES6 (26 atributos)
                if (tokens.size >= attrStartCol + 26) {
                    val attrValues = (0 until 26).map { offset ->
                        val valStr = tokens.getOrNull(attrStartCol + offset)?.let { cleanToken(it) }.orEmpty()
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
                    csvTeam = assignedTeam,
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

        val teamCounts = players.groupingBy { it.csvTeam }.eachCount()
        Log.d(TAG, "PARSE CONCLUIDO: Atletas extraídos=${players.size}, Times identificados=${teamCounts.size}, Erros=${errors.size}")

        return CsvParseResult(
            players = players,
            errors = errors,
            teamPlayerCounts = teamCounts,
            firstRawLine = firstRawLineFormatted
        )
    }

    private fun cleanToken(token: String): String {
        return token.removePrefix("\uFEFF").removePrefix("\uFFFE").trim().removeSurrounding("\"")
    }

    private fun normalize(str: String): String {
        val clean = cleanToken(str)
        val nfd = Normalizer.normalize(clean, Normalizer.Form.NFD)
        return nfd.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase().trim()
    }

    private fun formatRawLineForDisplay(line: String): String {
        return line.take(150)
            .replace(";", "[PV]")
            .replace(",", "[VG]")
            .replace("\t", "[TAB]")
            .replace("\r", "[CR]")
            .replace("\n", "[LF]")
    }

    private fun parsePosition(rawPos: String): String? {
        if (rawPos.isBlank()) return null
        val index = rawPos.toIntOrNull()
        if (index != null && index in POSITION_INDEX_MAP) {
            return POSITION_INDEX_MAP[index]
        }
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
                    tokens.add(cleanToken(sb.toString()))
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        tokens.add(cleanToken(sb.toString()))
        return tokens
    }
}
