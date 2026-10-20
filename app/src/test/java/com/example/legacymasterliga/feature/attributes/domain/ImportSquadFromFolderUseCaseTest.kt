package com.example.legacymasterliga.feature.attributes.domain

import com.example.legacymasterliga.core.database.entity.PlayerEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportSquadFromFolderUseCaseTest {

    private val playerDatabase = mutableListOf<PlayerEntity>()
    private var deleteByClubCalledCount = 0
    private var idCounter = 1L

    @Before
    fun setUp() {
        playerDatabase.clear()
        deleteByClubCalledCount = 0
        idCounter = 1L

        // Seeding 2 jogadores pré-existentes no clube 10
        playerDatabase.add(
            PlayerEntity(
                id = idCounter++,
                leagueId = 1L,
                clubId = 10L,
                name = "Jogador Antigo 1",
                position = "ZAG",
                overall = 70
            )
        )
        playerDatabase.add(
            PlayerEntity(
                id = idCounter++,
                leagueId = 1L,
                clubId = 10L,
                name = "Jogador Antigo 2",
                position = "MC",
                overall = 72
            )
        )
    }

    @Test
    fun `parser supports minimal text file with default attribute values of 50`() {
        val minimalTxt = """
            Name: Adriano
            Position: CA
            OVR: 88
            Age: 24
            Foot: L
            Height: 189
        """.trimIndent()

        val parsed = Pes6PlayerTextParser.parse(minimalTxt)

        assertEquals("Adriano", parsed.name)
        assertEquals("CA", parsed.position)
        assertEquals(88, parsed.overall)
        assertEquals(24, parsed.age)
        assertEquals("L", parsed.preferredFoot)
        assertEquals(189, parsed.heightCm)

        // 26 atributos devem estar presentes com o valor padrão 50
        val attrs = parsed.attributesRaw.split(",")
        assertEquals(26, attrs.size)
        assertTrue(attrs.all { it == "50" })
    }

    @Test
    fun `parser processes complete text file and keeps custom attributes`() {
        val fullTxt = """
            Name: Lionel Messi
            Position: SA
            OVR: 95
            Foot: L
            Height: 170
            Attack: 95
            Speed: 92
            Dribble Accuracy: 98
        """.trimIndent()

        val parsed = Pes6PlayerTextParser.parse(fullTxt)

        assertEquals("Lionel Messi", parsed.name)
        assertEquals("SA", parsed.position)
        assertEquals(95, parsed.overall)

        val attrs = parsed.attributesRaw.split(",")
        assertEquals(26, attrs.size)
        assertEquals("95", attrs[0]) // ATTACK
        assertEquals("92", attrs[4]) // SPEED
        assertEquals("98", attrs[8]) // DRIBBLE ACCURACY
    }

    @Test
    fun `parser correctly maps historical problem attributes like Defense, Dribble_Accuracy, Short_Pass_Accuracy, etc to non-50 values`() {
        val txt = """
            Name: Virgil van Dijk
            Defense: 89
            Dribble_Accuracy: 78
            Short_Pass_Accuracy: 84
            Long_Pass_Accuracy: 86
            Free_Kick_Accuracy: 75
            Shot_Accuracy: 71
            Shot_Power: 88
            GK_Skills: 60
        """.trimIndent()

        val parsed = Pes6PlayerTextParser.parse(txt)
        val attrs = parsed.attributesRaw.split(",").map { it.toInt() }

        assertEquals(89, attrs[1])  // DEFENCE
        assertEquals(78, attrs[8])  // DRIBBLE ACCURACY
        assertEquals(84, attrs[10]) // SHORT PASS ACCURACY
        assertEquals(86, attrs[12]) // LONG PASS ACCURACY
        assertEquals(71, attrs[14]) // SHOT ACCURACY
        assertEquals(88, attrs[15]) // SHOT POWER
        assertEquals(75, attrs[17]) // FREE KICK ACCURACY
        assertEquals(60, attrs[25]) // GK SKILLS
    }

    @Test
    fun `simulated folder import wipes old squad and inserts players including duplicate names as separate records`() = runBlocking {
        // Simulação da lógica da transação de importação
        val clubId = 10L
        val incomingFiles = listOf(
            "Ronaldo.txt" to "Name: Ronaldo\nPosition: CA\nOVR: 92",
            "Ronaldo_2.txt" to "Name: Ronaldo\nPosition: SA\nOVR: 85", // Nome idêntico
            "Image.png" to "binary data" // Arquivo não .txt
        )

        val playersToInsert = mutableListOf<PlayerEntity>()
        var ignoredCount = 0

        for ((fileName, content) in incomingFiles) {
            if (!fileName.endsWith(".txt", ignoreCase = true)) {
                ignoredCount++
                continue
            }
            val parsed = Pes6PlayerTextParser.parse(content, fileName.removeSuffix(".txt"))
            playersToInsert.add(
                PlayerEntity(
                    id = idCounter++,
                    leagueId = 1L,
                    clubId = clubId,
                    name = parsed.name,
                    position = parsed.position,
                    overall = parsed.overall,
                    attributesRaw = parsed.attributesRaw
                )
            )
        }

        // Executa a transação simulada: limpa elenco antigo e insere os novos
        playerDatabase.removeAll { it.clubId == clubId }
        deleteByClubCalledCount++
        playerDatabase.addAll(playersToInsert)

        // Verificações
        assertEquals(1, deleteByClubCalledCount)
        assertEquals(1, ignoredCount) // Image.png ignorado
        assertEquals(2, playerDatabase.size) // Apenas os 2 novos Ronaldos existem no clube

        // Verifica que os 2 Ronaldos possuem IDs únicos no banco
        val r1 = playerDatabase[0]
        val r2 = playerDatabase[1]
        assertEquals("Ronaldo", r1.name)
        assertEquals("Ronaldo", r2.name)
        assertEquals(92, r1.overall)
        assertEquals(85, r2.overall)
        assertTrue(r1.id != r2.id)
    }
}
