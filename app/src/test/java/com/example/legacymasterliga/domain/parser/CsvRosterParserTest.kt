package com.example.legacymasterliga.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class CsvRosterParserTest {

    @Test
    fun `parse template csv with 6 players and 2 teams succeeds`() {
        val csvContent = """
            Nome;Time;Posição;Geral;Altura;Pé;Nacionalidade;Camisa;ATTACK;DEFENCE;BALANCE;STAMINA;SPEED;ACCELERATION;RESPONSE;AGILITY;DRIBBLE ACCURACY;DRIBBLE SPEED;SHORT PASS ACCURACY;SHORT PASS SPEED;LONG PASS ACCURACY;LONG PASS SPEED;SHOT ACCURACY;SHOT POWER;SHOT TECHNIQUE;FREE KICK ACCURACY;SWERVE;HEADING;JUMP;TEAM WORK;TECHNIQUE;AGGRESSION;MENTALITY;GK SKILLS
            "Lionel Messi";"Barcelona";"PE";93;170;"Esquerdo";"Argentina";10;95;45;82;84;88;94;85;96;97;94;88;82;85;80;92;86;90;88;89;70;72;80;95;90;82;50
            "Lamine Yamal";"Barcelona";"PE";81;178;"Esquerdo";"Espanha";19;82;35;70;80;86;88;78;88;89;87;80;78;76;74;80;78;80;72;75;60;65;72;85;80;74;50
            "Pedri";"Barcelona";"MC";86;174;"Direito";"Espanha";8;78;62;72;88;78;80;82;84;88;82;90;84;88;82;76;74;78;75;78;62;68;86;89;72;80;50
            "Lautaro Martínez";"Inter de Milão";"ATA";89;174;"Direito";"Argentina";10;89;48;84;85;82;84;88;82;84;82;78;76;74;72;88;86;87;72;70;82;80;80;85;88;84;50
            "Nicolò Barella";"Inter de Milão";"MC";87;172;"Direito";"Itália";23;78;72;78;92;82;84;85;84;82;80;86;82;84;80;76;82;76;72;74;68;74;88;82;84;88;50
            "Manuel Neuer";"Inter de Milão";0;89;193;"Direito";"Alemanha";1;30;85;90;78;68;72;88;65;55;50;62;68;60;65;40;88;45;35;40;62;75;84;60;50;82;94
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val result = CsvRosterParser.parse(inputStream)

        assertTrue(result.errors.isEmpty())
        assertEquals(6, result.players.size)

        val teams = result.players.map { it.csvTeam }.distinct().sorted()
        assertEquals(listOf("Barcelona", "Inter de Milão"), teams)
        assertEquals(3, result.teamPlayerCounts["Barcelona"])
        assertEquals(3, result.teamPlayerCounts["Inter de Milão"])

        // Verificar o jogador Lionel Messi
        val messi = result.players.first { it.name == "Lionel Messi" }
        assertEquals("Barcelona", messi.csvTeam)
        assertEquals("PE", messi.position)
        assertEquals(93, messi.overall)
        assertEquals(170, messi.heightCm)
        assertEquals("Esquerdo", messi.preferredFoot)
        assertEquals("Argentina", messi.nationality)
        assertEquals(10, messi.shirtNumber)
        assertNotNull(messi.attributesRaw)

        // Verificar Manuel Neuer (posição mapeada por índice 0 = GOL)
        val neuer = result.players.first { it.name == "Manuel Neuer" }
        assertEquals("Inter de Milão", neuer.csvTeam)
        assertEquals("GOL", neuer.position)
        assertEquals(89, neuer.overall)
        assertEquals(193, neuer.heightCm)
        assertEquals(1, neuer.shirtNumber)
    }

    @Test
    fun `parse original pes6 editor csv with comma delimiter and english headers succeeds`() {
        val pes6CsvContent = """
            Name,Team,Pos,OVR,Height,Foot,Nation,Shirt,ATTACK,DEFENCE,BALANCE,STAMINA,SPEED,ACCELERATION,RESPONSE,AGILITY,DRIBBLE ACCURACY,DRIBBLE SPEED,SHORT PASS ACCURACY,SHORT PASS SPEED,LONG PASS ACCURACY,LONG PASS SPEED,SHOT ACCURACY,SHOT POWER,SHOT TECHNIQUE,FREE KICK ACCURACY,SWERVE,HEADING,JUMP,TEAM WORK,TECHNIQUE,AGGRESSION,MENTALITY,GK SKILLS
            "Kylian Mbappé","Real Madrid","CF",91,178,"Right","France",9,92,38,80,88,97,97,88,94,92,95,82,80,78,76,89,88,86,74,78,72,78,82,90,88,82,50
            "Jude Bellingham","Real Madrid","AMF",90,186,"Right","England",5,86,75,86,92,84,82,88,84,88,84,89,86,88,84,84,82,82,78,80,82,84,90,88,86,88,50
            "Jogador Sem Clube","","CMF",75,180,"Left","Brazil",10,70,60,70,75,75,75,70,70,70,70,72,70,70,70,70,70,70,65,65,65,65,70,70,65,70,50
        """.trimIndent()

        val inputStream = ByteArrayInputStream(pes6CsvContent.toByteArray(Charsets.UTF_8))
        val result = CsvRosterParser.parse(inputStream)

        assertTrue(result.errors.isEmpty())
        assertEquals(3, result.players.size)

        // Verificar o cabeçalho 'Name' não virou jogador
        assertTrue(result.players.none { it.name.lowercase() == "name" })

        val mbappe = result.players.first { it.name == "Kylian Mbappé" }
        assertEquals("Real Madrid", mbappe.csvTeam)
        assertEquals("CA", mbappe.position) // AMF -> MEI / CF -> CA
        assertEquals(91, mbappe.overall)
        assertEquals(178, mbappe.heightCm)
        assertEquals("Right", mbappe.preferredFoot)
        assertEquals("France", mbappe.nationality)

        val freeAgent = result.players.first { it.name == "Jogador Sem Clube" }
        assertEquals("Banco da Liga", freeAgent.csvTeam)
    }

    @Test
    fun `header line is ignored and players are correctly assigned to team or Banco da Liga`() {
        val csvContent = """
            Nome;Time;Posição;Geral
            "Vinicius Jr";"Real Madrid";"PE";89
            "Agente Livre";"";"MC";80
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val result = CsvRosterParser.parse(inputStream)

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.players.size)

        // Confirmar que o cabeçalho "Nome" não virou um jogador
        assertTrue(result.players.none { it.name.lowercase() == "nome" })

        val vini = result.players.first { it.name == "Vinicius Jr" }
        assertEquals("Real Madrid", vini.csvTeam)

        val freeAgent = result.players.first { it.name == "Agente Livre" }
        assertEquals("Banco da Liga", freeAgent.csvTeam)
    }

    @Test
    fun `csv with blank team imports player into Banco da Liga`() {
        val csvContent = """
            Nome;Time;Posição;Geral
            "Agente Livre";"";"MC";80
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val result = CsvRosterParser.parse(inputStream)

        assertTrue(result.errors.isEmpty())
        assertEquals(1, result.players.size)
        assertEquals("Agente Livre", result.players[0].name)
        assertEquals("Banco da Liga", result.players[0].csvTeam)
    }

    @Test
    fun `csv with blank name discards row and records error`() {
        val csvContent = """
            Nome;Time;Posição;Geral
            "";"Barcelona";"PE";93
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val result = CsvRosterParser.parse(inputStream)

        assertEquals(1, result.errors.size)
        assertTrue(result.players.isEmpty())
        assertTrue(result.errors[0].contains("nome em branco"))
    }
}
