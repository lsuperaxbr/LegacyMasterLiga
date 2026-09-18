package com.example.legacymasterliga.feature.backup

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.legacymasterliga.feature.backup.data.BackupManifest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupManifestTest {

    @Test
    fun manifest_serialization_and_deserialization_is_consistent() {
        val original = BackupManifest(
            formatVersion = 1,
            appVersion = "1.0.0-rc01",
            databaseVersion = 12,
            createdAt = 1626350000000L,
            databaseSha256 = "d4e5f6...",
            safetyCopy = false
        )
        
        val json = original.toJson()
        val restored = BackupManifest.fromJson(json)
        
        assertEquals(original, restored)
    }

    @Test
    fun manifest_handles_optional_safety_copy_flag() {
        val jsonWithoutSafety = """
            {
                "formatVersion": 1,
                "appVersion": "1.0.0",
                "databaseVersion": 10,
                "createdAt": 123456789,
                "databaseSha256": "abc"
            }
        """.trimIndent()
        
        val manifest = BackupManifest.fromJson(jsonWithoutSafety)
        assertEquals(false, manifest.safetyCopy)
    }
}
