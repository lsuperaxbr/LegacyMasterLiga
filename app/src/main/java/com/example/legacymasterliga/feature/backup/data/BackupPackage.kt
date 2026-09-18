package com.example.legacymasterliga.feature.backup.data

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import org.json.JSONObject

internal data class BackupManifest(
    val formatVersion: Int,
    val appVersion: String,
    val databaseVersion: Int,
    val createdAt: Long,
    val databaseSha256: String,
    val safetyCopy: Boolean,
) {
    fun toJson(): String = JSONObject()
        .put("formatVersion", formatVersion)
        .put("appVersion", appVersion)
        .put("databaseVersion", databaseVersion)
        .put("createdAt", createdAt)
        .put("databaseSha256", databaseSha256)
        .put("safetyCopy", safetyCopy)
        .toString(2)

    companion object {
        const val CURRENT_FORMAT = 1
        const val MANIFEST_ENTRY = "manifest.json"
        const val DATABASE_ENTRY = "database.sqlite"

        fun fromJson(text: String): BackupManifest {
            val json = JSONObject(text)
            return BackupManifest(
                formatVersion = json.getInt("formatVersion"),
                appVersion = json.getString("appVersion"),
                databaseVersion = json.getInt("databaseVersion"),
                createdAt = json.getLong("createdAt"),
                databaseSha256 = json.getString("databaseSha256"),
                safetyCopy = json.optBoolean("safetyCopy", false),
            )
        }
    }
}

internal fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    FileInputStream(file).use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
