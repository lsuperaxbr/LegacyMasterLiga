package com.example.legacymasterliga.feature.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerValidationTest {

    private fun isFootValid(foot: String?): Boolean {
        if (foot.isNullOrBlank()) return true
        val upper = foot.trim().uppercase()
        return upper == "R" || upper == "L"
    }

    private fun isHeightValid(heightCm: Int?): Boolean {
        if (heightCm == null) return true
        return heightCm in 100..250
    }

    @Test
    fun `validate Foot accepts R, L, empty, and null`() {
        assertTrue(isFootValid("R"))
        assertTrue(isFootValid("r"))
        assertTrue(isFootValid("L"))
        assertTrue(isFootValid("l"))
        assertTrue(isFootValid(""))
        assertTrue(isFootValid(null))

        assertFalse(isFootValid("X"))
        assertFalse(isFootValid("Right"))
    }

    @Test
    fun `validate Height accepts integers between 100 and 250 or null`() {
        assertTrue(isHeightValid(180))
        assertTrue(isHeightValid(100))
        assertTrue(isHeightValid(250))
        assertTrue(isHeightValid(null))

        assertFalse(isHeightValid(99))
        assertFalse(isHeightValid(251))
        assertFalse(isHeightValid(0))
        assertFalse(isHeightValid(-180))
    }
}
