# Walkthrough - Correção do Parser `Pes6PlayerTextParser.kt`

Correção da normalização de chaves e adição do dicionário `ALIAS_MAP` no parser de importação de elencos (`Pes6PlayerTextParser.kt`), solucionando o problema onde o atributo `Defense` (grafia americana) ficava com o valor de fallback `50` em vez do valor lido no arquivo `.txt`.

---

## Causa Raiz do Bug Solucionada

O arquivo `.txt` continha a chave `Defense: 70` (com **s**, ortografia americana). A função anterior buscava estritamente pela chave normalizada `"defence"` (com **c**, ortografia britânica), gerando um desacerto e caindo no valor de fallback `50`.

---

## `ALIAS_MAP` Completo dos 26 Atributos PES 6 e Cabeçalhos

```kotlin
val ALIAS_MAP = mapOf(
    // Header Info
    "name" to "NAME", "playername" to "NAME",
    "position" to "POSITION", "pos" to "POSITION",
    "ovr" to "OVR", "overall" to "OVR",
    "age" to "AGE", "idade" to "AGE",
    "foot" to "FOOT", "pe" to "FOOT", "pedominante" to "FOOT", "preferredfoot" to "FOOT",
    "height" to "HEIGHT", "altura" to "HEIGHT", "alturacm" to "HEIGHT", "heightcm" to "HEIGHT",

    // 26 PES 6 Attributes
    "attack" to "ATTACK", "attacking" to "ATTACK", "att" to "ATTACK", "attackingawareness" to "ATTACK",
    "defence" to "DEFENCE", "defense" to "DEFENCE", "defending" to "DEFENCE", "def" to "DEFENCE", "defensiveawareness" to "DEFENCE",
    "balance" to "BALANCE", "bodybalance" to "BALANCE", "physicality" to "BALANCE", "strength" to "BALANCE",
    "stamina" to "STAMINA", "endurance" to "STAMINA",
    "speed" to "SPEED", "topspeed" to "SPEED", "sprintspeed" to "SPEED",
    "acceleration" to "ACCELERATION", "accel" to "ACCELERATION", "acc" to "ACCELERATION",
    "response" to "RESPONSE", "reactions" to "RESPONSE", "reaction" to "RESPONSE",
    "agility" to "AGILITY", "explosiveness" to "AGILITY",
    "dribbleaccuracy" to "DRIBBLE ACCURACY", "dribbling" to "DRIBBLE ACCURACY", "dribbleacc" to "DRIBBLE ACCURACY", "dribblingaccuracy" to "DRIBBLE ACCURACY",
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
```

---

## Exemplo de Arquivo PES 6 Editor e Resultado do Parsing

```text
Entrada (.txt PES 6 Editor):
Name: Virgil van Dijk
Defense: 89
Dribble_Accuracy: 78
Short_Pass_Accuracy: 84
Long_Pass_Accuracy: 86
Free_Kick_Accuracy: 75
Shot_Accuracy: 71
Shot_Power: 88
GK_Skills: 60

Resultado do Parsing:
ParsedPes6Player(
  name = "Virgil van Dijk",
  attributesRaw = "50,89,50,50,50,50,50,50,78,50,84,50,86,50,71,88,50,75,50,50,50,50,50,50,50,60"
)
```

---

## Resultados da Verificação e Build
- **Testes Unitários (`ImportSquadFromFolderUseCaseTest.kt`):** Adicionado o teste dedicado `parser correctly maps historical problem attributes like Defense, Dribble_Accuracy, Short_Pass_Accuracy, etc to non-50 values` e todos os testes passaram.
- **Build:** `clean app:assembleDebug` executado com sucesso (0 erros de compilação).
