package com.example.legacymasterliga.core.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

interface PasswordHasher {
    fun generateSalt(): String
    fun hash(password: CharArray, encodedSalt: String): String
    fun verify(password: CharArray, encodedSalt: String, expectedHash: String): Boolean
}

@Singleton
class Pbkdf2PasswordHasher @Inject constructor() : PasswordHasher {
    override fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    override fun hash(password: CharArray, encodedSalt: String): String {
        val salt = Base64.decode(encodedSalt, Base64.NO_WRAP)
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val encoded = secretKeyFactory().generateSecret(spec).encoded
            Base64.encodeToString(encoded, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
            password.fill('\u0000')
        }
    }

    override fun verify(
        password: CharArray,
        encodedSalt: String,
        expectedHash: String,
    ): Boolean = constantTimeEquals(
        first = hash(password, encodedSalt),
        second = expectedHash,
    )

    private fun secretKeyFactory(): SecretKeyFactory = runCatching {
        SecretKeyFactory.getInstance(PRIMARY_ALGORITHM)
    }.getOrElse {
        SecretKeyFactory.getInstance(FALLBACK_ALGORITHM)
    }

    private fun constantTimeEquals(first: String, second: String): Boolean {
        if (first.length != second.length) return false
        var difference = 0
        for (index in first.indices) {
            difference = difference or (first[index].code xor second[index].code)
        }
        return difference == 0
    }

    private companion object {
        const val PRIMARY_ALGORITHM = "PBKDF2WithHmacSHA256"
        const val FALLBACK_ALGORITHM = "PBKDF2WithHmacSHA1"
        const val ITERATIONS = 120_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_LENGTH_BYTES = 16
    }
}
