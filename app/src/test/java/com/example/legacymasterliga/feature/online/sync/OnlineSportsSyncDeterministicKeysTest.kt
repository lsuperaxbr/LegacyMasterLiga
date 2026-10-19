package com.example.legacymasterliga.feature.online.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class OnlineSportsSyncDeterministicKeysTest {

    private fun sanitizeKey(name: String): String {
        return name.trim().lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "item" }
    }

    private fun generateDeterministicPairingKey(homeName: String, awayName: String): String {
        val sortedNames = listOf(sanitizeKey(homeName), sanitizeKey(awayName)).sorted()
        return "${sortedNames[0]}_vs_${sortedNames[1]}"
    }

    @Test
    fun `generateDeterministicPairingKey is symmetric regardless of home or away order`() {
        val key1 = generateDeterministicPairingKey("Palmeiras", "Corinthians")
        val key2 = generateDeterministicPairingKey("Corinthians", "Palmeiras")

        assertEquals("corinthians_vs_palmeiras", key1)
        assertEquals("corinthians_vs_palmeiras", key2)
        assertEquals(key1, key2)
    }

    @Test
    fun `deterministic club cloud ID is formatted predictably`() {
        val leagueCloudId = "cloud_league_123"
        val clubName = "  São Paulo FC  "
        val expected = "cloud_league_123_clube_sao_paulo_fc"

        val actual = "${leagueCloudId}_clube_${sanitizeKey(clubName)}"
        assertEquals(expected, actual)
    }

    @Test
    fun `deterministic player cloud ID includes club cloud ID and shirt number or local ID`() {
        val clubCloudId = "cloud_league_123_clube_flamengo"
        val playerName = "Adriano Imperador"
        val shirtNumber = 10

        val expected = "cloud_league_123_clube_flamengo_player_adriano_imperador_10"
        val actual = "${clubCloudId}_player_${sanitizeKey(playerName)}_$shirtNumber"

        assertEquals(expected, actual)
    }
}
