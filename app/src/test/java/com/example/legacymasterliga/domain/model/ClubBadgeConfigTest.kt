package com.example.legacymasterliga.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ClubBadgeConfigTest {

    @Test
    fun `default ClubBadgeConfig contains required fallback properties`() {
        val default = ClubBadgeConfig.DEFAULT
        assertEquals("shield", default.shape)
        assertEquals("solid", default.pattern)
        assertEquals("#808080", default.primaryColor)
        assertEquals("#FFFFFF", default.secondaryColor)
        assertEquals("#000000", default.accentColor)
        assertEquals("initial", default.symbol)
    }

    @Test
    fun `extractInitial extracts first letter in uppercase`() {
        assertEquals("A", ClubBadgeConfig.extractInitial("Athletico Paranaense"))
        assertEquals("M", ClubBadgeConfig.extractInitial("Manchester United"))
        assertEquals("S", ClubBadgeConfig.extractInitial("São Paulo"))
        assertEquals("C", ClubBadgeConfig.extractInitial("   "))
    }

    @Test
    fun `toJson and fromJson serialize and deserialize correctly`() {
        val config = ClubBadgeConfig(
            shape = "hexagon",
            pattern = "vertical_stripes",
            primaryColor = "#FF0000",
            secondaryColor = "#00FF00",
            accentColor = "#0000FF",
            symbol = "star"
        )

        val json = ClubBadgeConfig.toJson(config)
        val deserialized = ClubBadgeConfig.fromJson(json)

        assertNotNull(deserialized)
        assertEquals("hexagon", deserialized.shape)
        assertEquals("vertical_stripes", deserialized.pattern)
        assertEquals("#FF0000", deserialized.primaryColor)
        assertEquals("#00FF00", deserialized.secondaryColor)
        assertEquals("#0000FF", deserialized.accentColor)
        assertEquals("star", deserialized.symbol)
    }

    @Test
    fun `fromJson with null or blank string returns default fallback`() {
        val fallbackNull = ClubBadgeConfig.fromJson(null)
        assertEquals(ClubBadgeConfig.DEFAULT, fallbackNull)

        val fallbackBlank = ClubBadgeConfig.fromJson("")
        assertEquals(ClubBadgeConfig.DEFAULT, fallbackBlank)
    }
}
