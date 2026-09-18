package com.example.legacymasterliga.core.database

import com.example.legacymasterliga.core.di.DatabaseModule
import org.junit.Assert.assertEquals
import org.junit.Test

class MigrationRegistryTest {
    @Test
    fun `registered migrations form an explicit chain to current version`() {
        val migrations = DatabaseModule.migrations.sortedBy { it.startVersion }

        assertEquals(DatabaseContract.OLDEST_SUPPORTED_VERSION, migrations.first().startVersion)
        migrations.zipWithNext().forEach { (current, next) ->
            assertEquals(current.endVersion, next.startVersion)
        }
        assertEquals(DatabaseContract.VERSION, migrations.last().endVersion)
        assertEquals(DatabaseContract.VERSION - DatabaseContract.OLDEST_SUPPORTED_VERSION, migrations.size)
    }
}
