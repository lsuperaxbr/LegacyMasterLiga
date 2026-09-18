package com.example.legacymasterliga.feature.backup.data

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.DatabaseContract
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.di.DatabaseModule
import com.example.legacymasterliga.core.storage.CrestStorage
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.backup.domain.RestoreResult
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class BackupRestoreIntegrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var database: AppDatabase
    private lateinit var repository: RoomBackupRepository
    private val auditLogger = object : AuditLogger {
        override suspend fun log(
            category: String,
            action: String,
            entityType: String,
            entityId: Long?,
            leagueId: Long?,
            summary: String,
            details: String?,
        ) = Unit
    }

    @Before
    fun setUp() {
        cleanFiles()
        database = openDatabase()
        repository = RoomBackupRepository(context, database, auditLogger)
    }

    @After
    fun tearDown() {
        if (::database.isInitialized && database.isOpen) database.close()
        cleanFiles()
    }

    @Test
    fun `backup header uses official version and restore reopens clean data`() = runBlocking {
        database.leagueDao().insert(LeagueEntity(name = "Liga preservada"))
        val backup = repository.createManualBackup().getOrThrow()
        assertEquals(DatabaseContract.VERSION, backup.databaseVersion)

        ZipFile(backup.absolutePath).use { zip ->
            val manifest = zip.getInputStream(zip.getEntry(BackupManifest.MANIFEST_ENTRY))
                .bufferedReader().use { BackupManifest.fromJson(it.readText()) }
            assertEquals(DatabaseContract.VERSION, manifest.databaseVersion)
        }

        database.leagueDao().insert(LeagueEntity(name = "Liga posterior"))
        val result = repository.restore(Uri.fromFile(File(backup.absolutePath)))
        assertTrue(result is RestoreResult.Success)
        assertFalse(database.isOpen)
        assertTrue(
            context.getSharedPreferences("legacy_backup_preferences", Context.MODE_PRIVATE)
                .getBoolean(RoomBackupRepository.KEY_RESTORE_PENDING_RESTART, false),
        )

        database = openDatabase()
        assertTrue(database.leagueDao().findByName("Liga preservada") != null)
        assertTrue(database.leagueDao().findByName("Liga posterior") == null)
    }

    private fun openDatabase(): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    ).addMigrations(*DatabaseModule.migrations).allowMainThreadQueries().build()

    private fun cleanFiles() {
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        File(context.filesDir, "backups").deleteRecursively()
        File(context.filesDir, "crests").deleteRecursively()
        context.getSharedPreferences("legacy_backup_preferences", Context.MODE_PRIVATE).edit().clear().commit()
    }
}
