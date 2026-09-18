package com.example.legacymasterliga.feature.backup.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.legacymasterliga.feature.backup.domain.AutomaticBackupFrequency
import com.example.legacymasterliga.feature.backup.domain.BackupScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerBackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : BackupScheduler {
    override fun apply(frequency: AutomaticBackupFrequency) {
        val manager = WorkManager.getInstance(context)
        if (frequency == AutomaticBackupFrequency.OFF) {
            manager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }
        val intervalDays = if (frequency == AutomaticBackupFrequency.DAILY) 1L else 7L
        val request = PeriodicWorkRequestBuilder<BackupWorker>(intervalDays, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build(),
            )
            .build()
        manager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "legacy_automatic_backup"
    }
}
