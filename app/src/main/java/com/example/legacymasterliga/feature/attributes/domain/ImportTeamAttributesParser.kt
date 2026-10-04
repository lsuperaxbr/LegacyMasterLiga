package com.example.legacymasterliga.feature.attributes.domain

data class ParsedPlayerAttributeBlock(
    val name: String,
    val position: String? = null,
    val overall: Int? = null,
    val age: Int? = null,
    val foot: String? = null,
    val height: Int? = null,
    val hasFootKey: Boolean = false,
    val hasHeightKey: Boolean = false,
    val attributesByIndex: Map<Int, Int> = emptyMap()
)

object ImportTeamAttributesParser {

    private val ATTRIBUTE_KEY_MAP = mapOf(
        "attack" to 0, "att" to 0,
        "defence" to 1, "def" to 1,
        "balance" to 2,
        "stamina" to 3,
        "speed" to 4, "topspeed" to 4,
        "acceleration" to 5, "acc" to 5,
        "response" to 6,
        "agility" to 7,
        "dribbleaccuracy" to 8, "dribble" to 8,
        "dribblespeed" to 9,
        "shortpassaccuracy" to 10,
        "shortpassspeed" to 11,
        "longpassaccuracy" to 12,
        "longpassspeed" to 13,
        "shotaccuracy" to 14,
        "shotpower" to 15,
        "shottechnique" to 16,
        "freekickaccuracy" to 17,
        "swerve" to 18,
        "heading" to 19, "header" to 19,
        "jump" to 20,
        "teamwork" to 21,
        "technique" to 22,
        "aggression" to 23,
        "mentality" to 24,
        "gkskills" to 25
    )

    fun parse(rawText: String): List<ParsedPlayerAttributeBlock> {
        if (rawText.isBlank()) return emptyList()

        val blocks = rawText.split(Regex("(?m)^[ \\t]*$\\r?\\n?"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val parsedPlayers = mutableListOf<ParsedPlayerAttributeBlock>()

        for (blockText in blocks) {
            var name: String? = null
            var position: String? = null
            var overall: Int? = null
            var age: Int? = null
            var foot: String? = null
            var height: Int? = null
            var hasFootKey = false
            var hasHeightKey = false
            val attributesMap = mutableMapOf<Int, Int>()

            val lines = blockText.lines().map { it.trim() }.filter { it.isNotBlank() }
            for (line in lines) {
                val colonIdx = line.indexOf(':')
                if (colonIdx == -1) continue

                val rawKey = line.substring(0, colonIdx).trim()
                val rawVal = line.substring(colonIdx + 1).trim()
                val normKey = normalizeKey(rawKey)

                when (normKey) {
                    "name", "nome", "player", "jogador" -> {
                        if (rawVal.isNotBlank()) name = rawVal
                    }
                    "position", "pos", "posicao" -> {
                        if (rawVal.isNotBlank()) position = rawVal
                    }
                    "ovr", "overall", "geral" -> {
                        overall = rawVal.toIntOrNull()
                    }
                    "age", "idade" -> {
                        age = rawVal.toIntOrNull()
                    }
                    "foot", "pe", "preferredfoot" -> {
                        hasFootKey = true
                        foot = if (rawVal.isBlank()) "" else rawVal
                    }
                    "height", "altura", "heightcm" -> {
                        hasHeightKey = true
                        height = rawVal.toIntOrNull()
                    }
                    else -> {
                        val attrIdx = ATTRIBUTE_KEY_MAP[normKey]
                        if (attrIdx != null) {
                            val numVal = rawVal.toIntOrNull()
                            if (numVal != null && numVal in 0..99) {
                                attributesMap[attrIdx] = numVal
                            }
                        }
                        // Chaves desconhecidas sao ignoradas silenciosamente
                    }
                }
            }

            if (!name.isNullOrBlank()) {
                parsedPlayers.add(
                    ParsedPlayerAttributeBlock(
                        name = name,
                        position = position,
                        overall = overall,
                        age = age,
                        foot = foot,
                        height = height,
                        hasFootKey = hasFootKey,
                        hasHeightKey = hasHeightKey,
                        attributesByIndex = attributesMap
                    )
                )
            }
        }

        return parsedPlayers
    }

    private fun normalizeKey(key: String): String {
        return key.replace("_", "").replace(" ", "").lowercase()
    }
}
