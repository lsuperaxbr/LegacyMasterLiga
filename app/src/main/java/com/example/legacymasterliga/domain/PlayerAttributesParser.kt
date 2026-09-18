package com.example.legacymasterliga.domain

import androidx.compose.ui.graphics.Color

object PlayerAttributesParser {
    val ORDER = listOf(
        "ATTACK", "DEFENCE", "BALANCE", "STAMINA", "SPEED", "ACCELERATION",
        "RESPONSE", "AGILITY", "DRIBBLE ACCURACY", "DRIBBLE SPEED",
        "SHORT PASS ACCURACY", "SHORT PASS SPEED", "LONG PASS ACCURACY", "LONG PASS SPEED",
        "SHOT ACCURACY", "SHOT POWER", "SHOT TECHNIQUE", "FREE KICK ACCURACY",
        "SWERVE", "HEADING", "JUMP", "TEAM WORK", "TECHNIQUE",
        "AGGRESSION", "MENTALITY", "GK SKILLS",
    )

    // IMPORTANTE: regex procura pelos NOMES EXATOS conhecidos (mais longos primeiro),
    // não por "qualquer palavra antes do número" — isso evita confundir texto vizinho
    // (ex: posição do jogador colada sem espaço, tipo "AM Attack: 73") com o nome do atributo.
    private val sortedLabels = ORDER.sortedByDescending { it.length }
    private val pattern = sortedLabels.joinToString("|") { Regex.escape(it) }
    private val regex = Regex("""\b($pattern)\s*[:\-–]\s*(\d{1,3})""")

    data class ParseResult(val attributesRaw: String?, val foundCount: Int, val missing: List<String>)

    fun parse(text: String): ParseResult {
        val normalized = text.uppercase()
        val values = mutableMapOf<String, Int>()
        regex.findAll(normalized).forEach { match ->
            val label = match.groupValues[1]
            val value = match.groupValues[2].toIntOrNull() ?: return@forEach
            if (value in 0..99) values[label] = value
        }
        val missing = ORDER.filter { it !in values }
        val ordered = ORDER.map { values[it]?.toString() ?: "" }
        val raw = if (values.size >= 20) ordered.joinToString(",") else null
        return ParseResult(attributesRaw = raw, foundCount = values.size, missing = missing)
    }

    fun toDisplayList(attributesRaw: String?): List<Pair<String, Int>> {
        if (attributesRaw == null) return emptyList()
        val values = attributesRaw.split(",")
        return ORDER.zip(values).mapNotNull { (label, v) -> v.toIntOrNull()?.let { label to it } }
    }

    fun colorFor(value: Int): Color = when {
        value >= 95 -> Color(0xFFDC2828)
        value >= 90 -> Color(0xFFE68C28)
        value >= 80 -> Color(0xFFF0DC00)
        value >= 75 -> Color(0xFFF0F0A0)
        else -> Color(0xFFD2D2D2)
    }
}
