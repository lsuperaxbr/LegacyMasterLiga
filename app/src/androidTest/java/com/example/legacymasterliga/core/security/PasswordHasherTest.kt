package com.example.legacymasterliga.core.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordHasherTest {
    private val hasher = Pbkdf2PasswordHasher()

    @Test fun correct_password_is_verified_and_wrong_password_is_rejected() {
        val salt = hasher.generateSalt()
        val hash = hasher.hash("senha-forte".toCharArray(), salt)
        assertTrue(hasher.verify("senha-forte".toCharArray(), salt, hash))
        assertFalse(hasher.verify("senha-errada".toCharArray(), salt, hash))
    }

    @Test fun salts_and_hashes_are_not_reused() {
        val firstSalt = hasher.generateSalt()
        val secondSalt = hasher.generateSalt()
        assertNotEquals(firstSalt, secondSalt)
        assertNotEquals(
            hasher.hash("mesma-senha".toCharArray(), firstSalt),
            hasher.hash("mesma-senha".toCharArray(), secondSalt),
        )
    }
}
