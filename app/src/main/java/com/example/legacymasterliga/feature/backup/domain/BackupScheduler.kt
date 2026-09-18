package com.example.legacymasterliga.feature.backup.domain

interface BackupScheduler {
    fun apply(frequency: AutomaticBackupFrequency)
}
