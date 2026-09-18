package com.example.legacymasterliga.feature.backup.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.core.content.edit
import com.example.legacymasterliga.BuildConfig
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.DatabaseContract
import com.example.legacymasterliga.feature.backup.domain.AutomaticBackupFrequency
import com.example.legacymasterliga.feature.backup.domain.BackupInfo
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import com.example.legacymasterliga.feature.backup.domain.BackupValidation
import com.example.legacymasterliga.feature.backup.domain.RestoreResult
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val auditLogger: AuditLogger,
) : BackupRepository {
    private val backupDirectory = File(context.filesDir, "backups").apply { mkdirs() }
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun createManualBackup(): Result<BackupInfo> = createBackup("manual", false).also { result ->
        result.onSuccess { info ->
            auditLogger.log("BACKUP", "CREATE_MANUAL", "BACKUP", summary = "Backup manual criado.", details = info.fileName)
        }
    }

    override suspend fun createAutomaticBackup(): Result<BackupInfo> = createBackup("automatic", false).also { result ->
        if (result.isSuccess) pruneAutomaticBackups()
        result.onSuccess { info ->
            auditLogger.log("BACKUP", "CREATE_AUTOMATIC", "BACKUP", summary = "Backup automático criado.", details = info.fileName)
        }
    }

    private suspend fun createSafetyBackup(): Result<BackupInfo> = createBackup("safety", true)

    private suspend fun createBackup(prefix: String, safety: Boolean): Result<BackupInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val timestamp = System.currentTimeMillis()
            val snapshot = File(context.cacheDir, "snapshot-${UUID.randomUUID()}.sqlite")
            val packageFile = File(backupDirectory, "legacy-$prefix-$timestamp.lmlbackup")
            try {
                createDatabaseSnapshot(snapshot)
                val hash = sha256(snapshot)
                val manifest = BackupManifest(
                    formatVersion = BackupManifest.CURRENT_FORMAT,
                    appVersion = BuildConfig.VERSION_NAME,
                    databaseVersion = DatabaseContract.VERSION,
                    createdAt = timestamp,
                    databaseSha256 = hash,
                    safetyCopy = safety,
                )
                ZipOutputStream(FileOutputStream(packageFile)).use { zip ->
                    zip.putNextEntry(ZipEntry(BackupManifest.MANIFEST_ENTRY))
                    zip.write(manifest.toJson().toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    zip.putNextEntry(ZipEntry(BackupManifest.DATABASE_ENTRY))
                    snapshot.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
                BackupInfo(
                    fileName = packageFile.name,
                    absolutePath = packageFile.absolutePath,
                    createdAt = timestamp,
                    sizeBytes = packageFile.length(),
                    appVersion = manifest.appVersion,
                    databaseVersion = manifest.databaseVersion,
                    sha256 = hash,
                    isSafetyCopy = safety,
                )
            } finally {
                snapshot.delete()
            }
        }
    }

    private fun createDatabaseSnapshot(destination: File) {
        destination.delete()
        val escapedPath = destination.absolutePath.replace("'", "''")
        val sqlite = database.openHelper.writableDatabase
        sqlite.query("PRAGMA wal_checkpoint(FULL)").close()
        runCatching { sqlite.execSQL("VACUUM INTO '$escapedPath'") }
            .onFailure {
                context.getDatabasePath(AppDatabase.DATABASE_NAME).copyTo(destination, overwrite = true)
            }
        check(destination.exists() && destination.length() > 0L) { "Não foi possível gerar a cópia do banco." }
    }

    override suspend fun exportBackup(backup: BackupInfo, destination: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val source = File(backup.absolutePath)
            check(source.exists()) { "O backup selecionado não existe mais." }
            val output = context.contentResolver.openOutputStream(destination)
                ?: error("Não foi possível abrir o destino.")
            output.use { out -> source.inputStream().use { it.copyTo(out) } }
            Unit
        }
    }

    override suspend fun importBackup(source: Uri): Result<BackupInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val imported = copyUriToCache(source)
            try {
                val (manifest, validation) = validatePackage(imported)
                check(validation.isValid) { validation.message }
                val destination = File(backupDirectory, "legacy-imported-${System.currentTimeMillis()}.lmlbackup")
                imported.copyTo(destination, overwrite = false)
                auditLogger.log("BACKUP", "IMPORT", "BACKUP", summary = "Backup importado e validado.", details = destination.name)
                BackupInfo(
                    fileName = destination.name,
                    absolutePath = destination.absolutePath,
                    createdAt = manifest.createdAt,
                    sizeBytes = destination.length(),
                    appVersion = manifest.appVersion,
                    databaseVersion = manifest.databaseVersion,
                    sha256 = manifest.databaseSha256,
                    isSafetyCopy = manifest.safetyCopy,
                )
            } finally {
                imported.delete()
            }
        }
    }

    override suspend fun importAndValidate(source: Uri): Result<BackupValidation> = withContext(Dispatchers.IO) {
        runCatching {
            val imported = copyUriToCache(source)
            try {
                validatePackage(imported).second
            } finally {
                imported.delete()
            }
        }
    }

    override suspend fun restore(source: Uri): RestoreResult = withContext(Dispatchers.IO) {
        val imported = runCatching { copyUriToCache(source) }.getOrElse {
            return@withContext RestoreResult.Failure(it.message ?: "Falha ao ler o arquivo.")
        }
        val extracted = File(context.cacheDir, "restore-${UUID.randomUUID()}.sqlite")
        try {
            val (manifest, validation) = validatePackage(imported, extracted)
            if (!validation.isValid) return@withContext RestoreResult.Failure(validation.message)
            auditLogger.log("BACKUP", "RESTORE_REQUESTED", "BACKUP", summary = "Restauração de backup iniciada.", details = "Versão ${manifest.appVersion}; banco v${manifest.databaseVersion}")
            val safety = createSafetyBackup().getOrElse {
                return@withContext RestoreResult.Failure("Não foi possível criar a cópia de segurança: ${it.message}")
            }
            database.close()
            check(!database.isOpen) { "Não foi possível encerrar o Room antes da restauração." }
            val target = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            target.parentFile?.mkdirs()
            File(target.absolutePath + "-wal").delete()
            File(target.absolutePath + "-shm").delete()
            extracted.copyTo(target, overwrite = true)

            preferences.edit(commit = true) { putBoolean(KEY_RESTORE_PENDING_RESTART, true) }
            RestoreResult.Success(safety)
        } catch (error: Throwable) {
            RestoreResult.Failure(error.message ?: "Falha desconhecida ao restaurar.")
        } finally {
            imported.delete()
            extracted.delete()
        }
    }

    override suspend fun listBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        backupDirectory.listFiles { file -> file.extension == "lmlbackup" }
            .orEmpty()
            .mapNotNull { file ->
                runCatching {
                    val (manifest, validation) = validatePackage(file)
                    if (!validation.isValid) return@runCatching null
                    BackupInfo(
                        fileName = file.name,
                        absolutePath = file.absolutePath,
                        createdAt = manifest.createdAt,
                        sizeBytes = file.length(),
                        appVersion = manifest.appVersion,
                        databaseVersion = manifest.databaseVersion,
                        sha256 = manifest.databaseSha256,
                        isSafetyCopy = manifest.safetyCopy,
                    )
                }.getOrNull()
            }
            .sortedByDescending { it.createdAt }
    }

    override fun getAutomaticFrequency(): AutomaticBackupFrequency = runCatching {
        AutomaticBackupFrequency.valueOf(preferences.getString(KEY_FREQUENCY, AutomaticBackupFrequency.OFF.name)!!)
    }.getOrDefault(AutomaticBackupFrequency.OFF)

    override fun setAutomaticFrequency(frequency: AutomaticBackupFrequency) {
        preferences.edit { putString(KEY_FREQUENCY, frequency.name) }
    }

    private fun copyUriToCache(uri: Uri): File {
        val target = File(context.cacheDir, "import-${UUID.randomUUID()}.lmlbackup")
        val input = context.contentResolver.openInputStream(uri) ?: error("Não foi possível abrir o arquivo.")
        input.use { source -> target.outputStream().use { source.copyTo(it) } }
        return target
    }

    private fun validatePackage(packageFile: File, extractedDatabase: File? = null): Pair<BackupManifest, BackupValidation> {
        ZipFile(packageFile).use { zip ->
            val manifestEntry = zip.getEntry(BackupManifest.MANIFEST_ENTRY)
                ?: error("Manifesto do backup não encontrado.")
            val databaseEntry = zip.getEntry(BackupManifest.DATABASE_ENTRY)
                ?: error("Banco de dados do backup não encontrado.")
            val manifest = zip.getInputStream(manifestEntry).bufferedReader().use { BackupManifest.fromJson(it.readText()) }
            if (manifest.formatVersion != BackupManifest.CURRENT_FORMAT) {
                return manifest to BackupValidation(
                    isValid = false,
                    message = "Formato de backup incompatível: ${manifest.formatVersion}.",
                    appVersion = manifest.appVersion,
                    databaseVersion = manifest.databaseVersion,
                )
            }
            val dbFile = extractedDatabase ?: File(context.cacheDir, "validate-${UUID.randomUUID()}.sqlite")
            try {
                zip.getInputStream(databaseEntry).use { input -> dbFile.outputStream().use { input.copyTo(it) } }
                val header = ByteArray(SQLITE_HEADER.length)
                dbFile.inputStream().use { it.read(header) }
                if (header.toString(Charsets.US_ASCII) != SQLITE_HEADER) {
                    return manifest to BackupValidation(
                        isValid = false,
                        message = "O arquivo não contém um banco SQLite válido.",
                        appVersion = manifest.appVersion,
                        databaseVersion = manifest.databaseVersion,
                    )
                }
                val actualHash = sha256(dbFile)
                if (!actualHash.equals(manifest.databaseSha256, ignoreCase = true)) {
                    return manifest to BackupValidation(
                        isValid = false,
                        message = "A soma de verificação não confere; o arquivo pode estar corrompido.",
                        appVersion = manifest.appVersion,
                        databaseVersion = manifest.databaseVersion,
                        sha256 = actualHash,
                    )
                }
                val sqliteVersion = readSqliteVersion(dbFile)
                val versionCheck = BackupVersionPolicy.validate(manifest.databaseVersion, sqliteVersion)
                return manifest to BackupValidation(
                    isValid = versionCheck.isCompatible,
                    message = versionCheck.message,
                    appVersion = manifest.appVersion,
                    databaseVersion = manifest.databaseVersion,
                    sha256 = actualHash,
                )
            } finally {
                if (extractedDatabase == null) dbFile.delete()
            }
        }
    }

    private fun readSqliteVersion(file: File): Int {
        val sqlite = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        return try {
            sqlite.rawQuery("PRAGMA user_version", null).use { cursor ->
                check(cursor.moveToFirst()) { "Não foi possível ler a versão do banco SQLite." }
                cursor.getInt(0)
            }
        } finally {
            sqlite.close()
        }
    }

    private fun pruneAutomaticBackups() {
        backupDirectory.listFiles { file -> file.name.startsWith("legacy-automatic-") }
            .orEmpty()
            .sortedByDescending { it.lastModified() }
            .drop(MAX_AUTOMATIC_BACKUPS)
            .forEach(File::delete)
    }

    companion object {
        private const val PREFS_NAME = "legacy_backup_preferences"
        private const val KEY_FREQUENCY = "automatic_frequency"
        const val KEY_RESTORE_PENDING_RESTART = "restore_pending_restart"
        private const val MAX_AUTOMATIC_BACKUPS = 10
        private const val SQLITE_HEADER = "SQLite format 3\u0000"
    }
}
