package com.example.legacymasterliga.feature.backup.domain

import android.net.Uri

enum class AutomaticBackupFrequency {
    OFF,
    DAILY,
    WEEKLY,
}

data class BackupInfo(
    val fileName: String,
    val absolutePath: String,
    val createdAt: Long,
    val sizeBytes: Long,
    val appVersion: String,
    val databaseVersion: Int,
    val sha256: String,
    val isSafetyCopy: Boolean,
)

data class BackupValidation(
    val isValid: Boolean,
    val message: String,
    val appVersion: String? = null,
    val databaseVersion: Int? = null,
    val sha256: String? = null,
)

sealed interface RestoreResult {
    data class Success(val safetyBackup: BackupInfo) : RestoreResult
    data class Failure(val message: String) : RestoreResult
}

interface BackupRepository {
    suspend fun createManualBackup(): Result<BackupInfo>
    suspend fun createAutomaticBackup(): Result<BackupInfo>
    suspend fun exportBackup(backup: BackupInfo, destination: Uri): Result<Unit>
    suspend fun importBackup(source: Uri): Result<BackupInfo>
    suspend fun importAndValidate(source: Uri): Result<BackupValidation>
    suspend fun restore(source: Uri): RestoreResult
    suspend fun listBackups(): List<BackupInfo>
    fun getAutomaticFrequency(): AutomaticBackupFrequency
    fun setAutomaticFrequency(frequency: AutomaticBackupFrequency)
}
