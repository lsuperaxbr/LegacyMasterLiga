package com.example.legacymasterliga.feature.attributes.domain

data class ParsedPes6Player(
    val name: String,
    val position: String?,
    val overall: Int?,
    val age: Int?,
    val preferredFoot: String?,
    val heightCm: Int?,
    val attributesRaw: String
)

object Pes6PlayerTextParser {

    val ATTRIBUTES_ORDER = listOf(
        "ATTACK", "DEFENCE", "BALANCE", "STAMINA", "SPEED", "ACCELERATION",
        "RESPONSE", "AGILITY", "DRIBBLE ACCURACY", "DRIBBLE SPEED",
        "SHORT PASS ACCURACY", "SHORT PASS SPEED", "LONG PASS ACCURACY", "LONG PASS SPEED",
        "SHOT ACCURACY", "SHOT POWER", "SHOT TECHNIQUE", "FREE KICK ACCURACY",
        "SWERVE", "HEADING", "JUMP", "TEAM WORK", "TECHNIQUE",
        "AGGRESSION", "MENTALITY", "GK SKILLS"
    )

    private fun normalizeKey(key: String): String {
        return key.lowercase().replace("_", "").replace(" ", "").trim()
    }

    fun parse(text: String, defaultName: String = "Jogador Sem Nome"): ParsedPes6Player {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val keyValueMap = mutableMapOf<String, String>()

        for (line in lines) {
            val colonIndex = line.indexOf(':')
            val dashIndex = line.indexOf('-')
            val splitIdx = when {
                colonIndex != -1 -> colonIndex
                dashIndex != -1 -> dashIndex
                else -> -1
            }

            if (splitIdx != -1) {
                val key = line.substring(0, splitIdx)
                val value = line.substring(splitIdx + 1)
                keyValueMap[normalizeKey(key)] = value.trim()
            }
        }

        // Header info
        val parsedName = keyValueMap["name"]?.takeIf { it.isNotBlank() } ?: defaultName
        val position = keyValueMap["position"]?.ifBlank { null } ?: keyValueMap["pos"]?.ifBlank { null }
        val overall = keyValueMap["ovr"]?.toIntOrNull() ?: keyValueMap["overall"]?.toIntOrNull()
        val age = keyValueMap["age"]?.toIntOrNull() ?: keyValueMap["idade"]?.toIntOrNull()

        // Foot validation
        val rawFoot = keyValueMap["foot"] ?: keyValueMap["pe"] ?: keyValueMap["pedominante"]
        val preferredFoot = when {
            rawFoot == null || rawFoot.isBlank() -> null
            rawFoot.equals("R", ignoreCase = true) || rawFoot.startsWith("D", ignoreCase = true) || rawFoot.contains("Right", ignoreCase = true) -> "R"
            rawFoot.equals("L", ignoreCase = true) || rawFoot.startsWith("C", ignoreCase = true) || rawFoot.startsWith("E", ignoreCase = true) || rawFoot.contains("Left", ignoreCase = true) -> "L"
            else -> null
        }

        // Height validation
        val rawHeight = keyValueMap["height"] ?: keyValueMap["altura"] ?: keyValueMap["alturacm"]
        val parsedHeight = rawHeight?.filter { it.isDigit() }?.toIntOrNull()
        val heightCm = if (parsedHeight != null && parsedHeight in 100..250) parsedHeight else null

        // 26 PES6 Attributes: missing attributes default to 50
        val attributeValues = ATTRIBUTES_ORDER.map { attr ->
            val normAttr = normalizeKey(attr)
            val valStr = keyValueMap[normAttr]
            val parsedVal = valStr?.filter { it.isDigit() }?.toIntOrNull()
            if (parsedVal != null && parsedVal in 1..99) parsedVal else 50
        }

        val calculatedOvr = overall ?: (attributeValues.average().toInt().coerceIn(1, 99))
        val attributesRaw = attributeValues.joinToString(",")

        return ParsedPes6Player(
            name = parsedName,
            position = position,
            overall = calculatedOvr,
            age = age,
            preferredFoot = preferredFoot,
            heightCm = heightCm,
            attributesRaw = attributesRaw
        )
    }
}
