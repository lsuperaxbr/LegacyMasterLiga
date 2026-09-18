package com.example.legacymasterliga.feature.backup.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupPackageTest {
    @Test fun manifest_round_trip_preserves_integrity_metadata() {
        val original = BackupManifest(1, "0.28.0-alpha28", 12, 1234L, "abc123", true)
        assertEquals(original, BackupManifest.fromJson(original.toJson()))
    }

    @Test fun sha256_changes_when_file_content_changes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "backup-hash-test.bin")
        file.writeText("primeira versão")
        val first = sha256(file)
        file.writeText("segunda versão")
        val second = sha256(file)
        assertNotEquals(first, second)
        file.delete()
    }
}
