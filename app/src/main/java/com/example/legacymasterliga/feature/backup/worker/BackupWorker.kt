package com.example.legacymasterliga.feature.backup.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class BackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            BackupWorkerEntryPoint::class.java,
        )
        return entryPoint.backupRepository().createAutomaticBackup().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackupWorkerEntryPoint {
    fun backupRepository(): BackupRepository
}
