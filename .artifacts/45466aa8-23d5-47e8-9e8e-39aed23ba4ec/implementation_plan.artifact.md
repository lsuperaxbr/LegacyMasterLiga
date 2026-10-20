# Plano de Implementação - Correção do Parser `Pes6PlayerTextParser.kt`

Diagnóstico e correção da normalização de chaves no parser de importação de elenco por pasta (`Pes6PlayerTextParser.kt`), garantindo mapeamento completo de variações em inglês britânico (`Defence`), inglês americano (`Defense`), variações em `camelCase`, `snake_case`, com espaços e aliases comuns para os 26 atributos numéricos do PES 6.

---

## Diagnóstico da Causa Raiz

No código anterior, a chave do arquivo `.txt` era normalizada removendo espaços e sublinhados (`normalizeKey("Defense")` $\rightarrow$ `"defense"`). Porém, a lista de atributos do PES 6 buscava pela grafia britânica `"defence"` (com `c`), fazendo com que o arquivo contendo `Defense: 70` (com `s`) não encontrasse a correspondência e assumisse o valor padrão de fallback `50`.

---

## Tabela de Mapeamento de Aliases dos 26 Atributos

| Atributo Canônico | Variações e Aliases Mapeados |
| :--- | :--- |
| **`ATTACK`** | `attack`, `attacking`, `att`, `attacking_awareness`, `attackingawareness` |
| **`DEFENCE`** | `defence`, `defense`, `defending`, `def`, `defensive_awareness`, `defensiveawareness` |
| **`BALANCE`** | `balance`, `body_balance`, `bodybalance`, `physicality`, `strength` |
| **`STAMINA`** | `stamina`, `endurance` |
| **`SPEED`** | `speed`, `top_speed`, `topspeed`, `sprint_speed`, `sprintspeed` |
| **`ACCELERATION`** | `acceleration`, `accel`, `acc` |
| **`RESPONSE`** | `response`, `reactions`, `reaction` |
| **`AGILITY`** | `agility`, `explosiveness` |
| **`DRIBBLE ACCURACY`** | `dribbleaccuracy`, `dribble_accuracy`, `dribbling`, `dribbleacc`, `dribble_acc` |
| **`DRIBBLE SPEED`** | `dribblespeed`, `dribble_speed`, `dribblingspeed`, `dribble_spd` |
| **`SHORT PASS ACCURACY`** | `shortpassaccuracy`, `short_pass_accuracy`, `shortpassacc`, `short_pass_acc`, `shortpass` |
| **`SHORT PASS SPEED`** | `shortpassspeed`, `short_pass_speed`, `shortpassspd`, `short_pass_spd` |
| **`LONG PASS ACCURACY`** | `longpassaccuracy`, `long_pass_accuracy`, `longpassacc`, `long_pass_acc`, `longpass` |
| **`LONG PASS SPEED`** | `longpassspeed`, `long_pass_speed`, `longpassspd`, `long_pass_spd` |
| **`SHOT ACCURACY`** | `shotaccuracy`, `shot_accuracy`, `shotacc`, `shot_acc`, `shooting_accuracy`, `finishing` |
| **`SHOT POWER`** | `shotpower`, `shot_power`, `shotpwr`, `shot_pwr`, `kicking_power` |
| **`SHOT TECHNIQUE`** | `shottechnique`, `shot_technique`, `shottech`, `shooting_technique` |
| **`FREE KICK ACCURACY`** | `freekickaccuracy`, `free_kick_accuracy`, `fk_accuracy`, `fkacc`, `fk_acc` |
| **`SWERVE`** | `swerve`, `curve`, `bending` |
| **`HEADING`** | `heading`, `head_accuracy`, `header` |
| **`JUMP`** | `jump`, `jumping`, `leaping` |
| **`TEAM WORK`** | `teamwork`, `team_work`, `teamwork_ability` |
| **`TECHNIQUE`** | `technique`, `ball_control`, `ballcontrol` |
| **`AGGRESSION`** | `aggression`, `aggressiveness` |
| **`MENTALITY`** | `mentality`, `composure`, `tenacity` |
| **`GK SKILLS`** | `gkskills`, `gk_skills`, `goalkeeping`, `gk_ability`, `gk` |

---

## Diffs Propostos para `Pes6PlayerTextParser.kt`

```kotlin
package com.example.legacymasterliga.feature.attributes.domain

import android.util.Log

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

    private const val TAG = "Pes6PlayerTextParser"

    val ATTRIBUTES_ORDER = listOf(
        "ATTACK", "DEFENCE", "BALANCE", "STAMINA", "SPEED", "ACCELERATION",
        "RESPONSE", "AGILITY", "DRIBBLE ACCURACY", "DRIBBLE SPEED",
        "SHORT PASS ACCURACY", "SHORT PASS SPEED", "LONG PASS ACCURACY", "LONG PASS SPEED",
        "SHOT ACCURACY", "SHOT POWER", "SHOT TECHNIQUE", "FREE KICK ACCURACY",
        "SWERVE", "HEADING", "JUMP", "TEAM WORK", "TECHNIQUE",
        "AGGRESSION", "MENTALITY", "GK SKILLS"
    )

    private val ALIAS_MAP = mapOf(
        // Header info
        "name" to "NAME", "playername" to "NAME",
        "position" to "POSITION", "pos" to "POSITION",
        "ovr" to "OVR", "overall" to "OVR",
        "age" to "AGE", "idade" to "AGE",
        "foot" to "FOOT", "pe" to "FOOT", "pedominante" to "FOOT", "preferredfoot" to "FOOT",
        "height" to "HEIGHT", "altura" to "HEIGHT", "alturacm" to "HEIGHT", "heightcm" to "HEIGHT",

        // Attributes
        "attack" to "ATTACK", "attacking" to "ATTACK", "att" to "ATTACK", "attackingawareness" to "ATTACK",
        "defence" to "DEFENCE", "defense" to "DEFENCE", "defending" to "DEFENCE", "def" to "DEFENCE", "defensiveawareness" to "DEFENCE",
        "balance" to "BALANCE", "bodybalance" to "BALANCE", "physicality" to "BALANCE", "strength" to "BALANCE",
        "stamina" to "STAMINA", "endurance" to "STAMINA",
        "speed" to "SPEED", "topspeed" to "SPEED", "sprintspeed" to "SPEED",
        "acceleration" to "ACCELERATION", "accel" to "ACCELERATION", "acc" to "ACCELERATION",
        "response" to "RESPONSE", "reactions" to "RESPONSE", "reaction" to "RESPONSE",
        "agility" to "AGILITY", "explosiveness" to "AGILITY",
        "dribbleaccuracy" to "DRIBBLE ACCURACY", "dribbling" to "DRIBBLE ACCURACY", "dribbleacc" to "DRIBBLE ACCURACY",
        "dribblespeed" to "DRIBBLE SPEED", "dribblingspeed" to "DRIBBLE SPEED", "dribblespd" to "DRIBBLE SPEED",
        "shortpassaccuracy" to "SHORT PASS ACCURACY", "shortpassacc" to "SHORT PASS ACCURACY", "shortpass" to "SHORT PASS ACCURACY",
        "shortpassspeed" to "SHORT PASS SPEED", "shortpassspd" to "SHORT PASS SPEED",
        "longpassaccuracy" to "LONG PASS ACCURACY", "longpassacc" to "LONG PASS ACCURACY", "longpass" to "LONG PASS ACCURACY",
        "longpassspeed" to "LONG PASS SPEED", "longpassspd" to "LONG PASS SPEED",
        "shotaccuracy" to "SHOT ACCURACY", "shotacc" to "SHOT ACCURACY", "shootingaccuracy" to "SHOT ACCURACY", "finishing" to "SHOT ACCURACY",
        "shotpower" to "SHOT POWER", "shotpwr" to "SHOT POWER", "kickingpower" to "SHOT POWER",
        "shottechnique" to "SHOT TECHNIQUE", "shottech" to "SHOT TECHNIQUE", "shootingtechnique" to "SHOT TECHNIQUE",
        "freekickaccuracy" to "FREE KICK ACCURACY", "fkaccuracy" to "FREE KICK ACCURACY", "fkacc" to "FREE KICK ACCURACY",
        "swerve" to "SWERVE", "curve" to "SWERVE", "bending" to "SWERVE",
        "heading" to "HEADING", "headaccuracy" to "HEADING", "header" to "HEADING",
        "jump" to "JUMP", "jumping" to "JUMP", "leaping" to "JUMP",
        "teamwork" to "TEAM WORK", "teamworkability" to "TEAM WORK",
        "technique" to "TECHNIQUE", "ballcontrol" to "TECHNIQUE",
        "aggression" to "AGGRESSION", "aggressiveness" to "AGGRESSION",
        "mentality" to "MENTALITY", "composure" to "MENTALITY", "tenacity" to "MENTALITY",
        "gkskills" to "GK SKILLS", "goalkeeping" to "GK SKILLS", "gkability" to "GK SKILLS", "gk" to "GK SKILLS"
    )

    private fun normalizeKey(key: String): String {
        return key.lowercase().replace("_", "").replace(" ", "").trim()
    }

    fun parse(text: String, defaultName: String = "Jogador Sem Nome"): ParsedPes6Player {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val parsedValues = mutableMapOf<String, String>()

        for (line in lines) {
            val colonIndex = line.indexOf(':')
            val dashIndex = line.indexOf('-')
            val splitIdx = when {
                colonIndex != -1 -> colonIndex
                dashIndex != -1 -> dashIndex
                else -> -1
            }

            if (splitIdx != -1) {
                val originalKey = line.substring(0, splitIdx).trim()
                val value = line.substring(splitIdx + 1).trim()
                val normalizedKey = normalizeKey(originalKey)
                val canonicalKey = ALIAS_MAP[normalizedKey]

                if (canonicalKey != null) {
                    parsedValues[canonicalKey] = value
                    runCatching { Log.d(TAG, "Lido: original='$originalKey', normalizada='$normalizedKey' -> canônica='$canonicalKey', valor='$value'") }
                } else {
                    runCatching { Log.w(TAG, "AVISO: Chave do arquivo não reconhecida: original='$originalKey', normalizada='$normalizedKey'") }
                }
            }
        }

        // Header info
        val parsedName = parsedValues["NAME"]?.takeIf { it.isNotBlank() } ?: defaultName
        val position = parsedValues["POSITION"]?.ifBlank { null }
        val overall = parsedValues["OVR"]?.toIntOrNull()
        val age = parsedValues["AGE"]?.toIntOrNull()

        // Foot validation
        val rawFoot = parsedValues["FOOT"]
        val preferredFoot = when {
            rawFoot == null || rawFoot.isBlank() -> null
            rawFoot.equals("R", ignoreCase = true) || rawFoot.startsWith("D", ignoreCase = true) || rawFoot.contains("Right", ignoreCase = true) -> "R"
            rawFoot.equals("L", ignoreCase = true) || rawFoot.startsWith("C", ignoreCase = true) || rawFoot.startsWith("E", ignoreCase = true) || rawFoot.contains("Left", ignoreCase = true) -> "L"
            else -> null
        }

        // Height validation
        val rawHeight = parsedValues["HEIGHT"]
        val parsedHeight = rawHeight?.filter { it.isDigit() }?.toIntOrNull()
        val heightCm = if (parsedHeight != null && parsedHeight in 100..250) parsedHeight else null

        // 26 PES6 Attributes: missing attributes default to 50
        val attributeValues = ATTRIBUTES_ORDER.map { attr ->
            val valStr = parsedValues[attr]
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
```

---

## Plano de Verificação e Testes

### Automated Tests
1. **`ImportSquadFromFolderUseCaseTest.kt`:**
   - Adicionar o teste específico:
     `@Test fun 'parser correctly maps Defense=70 to DEFENCE attribute as 70 instead of fallback 50'()`
   - Executar os testes via Gradle: `gradle_build("app:assembleDebug")`.

### Exemplo Demonstrativo Requisitado
```text
Arquivo de Entrada (.txt):
Name: Sergio Ramos
Defense: 70
Attack: 80

Resultado do Parsing:
ParsedPes6Player:
  name: "Sergio Ramos"
  attributesRaw[0] (ATTACK): 80
  attributesRaw[1] (DEFENCE): 70  <-- CORRETO (anteriormente 50)
```
