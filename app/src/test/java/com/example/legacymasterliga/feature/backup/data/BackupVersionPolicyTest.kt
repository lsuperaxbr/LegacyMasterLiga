package com.example.legacymasterliga.feature.backup.data

import com.example.legacymasterliga.core.database.DatabaseContract
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupVersionPolicyTest {
    @Test
    fun `current database version is accepted`() {
        assertTrue(BackupVersionPolicy.validate(DatabaseContract.VERSION, DatabaseContract.VERSION).isCompatible)
    }

    @Test
    fun `recent older database is accepted for Room migration`() {
        assertTrue(BackupVersionPolicy.validate(18, 18).isCompatible)
    }

    @Test
    fun `newer database is rejected clearly`() {
        val result = BackupVersionPolicy.validate(DatabaseContract.VERSION + 1, DatabaseContract.VERSION + 1)
        assertFalse(result.isCompatible)
        assertTrue(result.message.contains("mais nova"))
    }

    @Test
    fun `manifest and SQLite version mismatch is rejected`() {
        val result = BackupVersionPolicy.validate(DatabaseContract.VERSION, DatabaseContract.VERSION - 1)
        assertFalse(result.isCompatible)
        assertTrue(result.message.contains("não corresponde"))
    }
}
