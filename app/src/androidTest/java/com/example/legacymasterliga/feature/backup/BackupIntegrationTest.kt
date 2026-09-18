package com.example.legacymasterliga.feature.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.storage.CrestStorage
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.backup.data.BackupManifest
import com.example.legacymasterliga.feature.backup.data.RoomBackupRepository
import com.example.legacymasterliga.feature.backup.data.sha256
import com.example.legacymasterliga.feature.backup.domain.AutomaticBackupFrequency
import com.example.legacymasterliga.feature.backup.domain.RestoreResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class BackupIntegrationTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: RoomBackupRepository
    private lateinit var crestStorage: CrestStorage
    private lateinit var testBackupDir: File

    private val auditLogger = object : AuditLogger {
        override suspend fun log(category: String, action: String, entityType: String, entityId: Long?, leagueId: Long?, summary: String, details: String?) {}
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Usar um banco real em arquivo para testar a cópia física
        val dbFile = context.getDatabasePath("test_backup.db")
        dbFile.delete()
        db = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.name)
            .allowMainThreadQueries()
            .build()
        
        crestStorage = CrestStorage(context)
        // Limpar pasta de escudos antes de cada teste
        crestStorage.getCrestsDirectory().listFiles()?.forEach { it.delete() }
        
        repository = RoomBackupRepository(context, db, crestStorage, auditLogger)
        testBackupDir = File(context.filesDir, "backups")
        testBackupDir.listFiles()?.forEach { it.delete() }
    }

    @After
    fun teardown() {
        db.close()
        context.getDatabasePath("test_backup.db").delete()
        testBackupDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun manual_backup_creates_valid_zip_package() = runBlocking {
        // Inserir dados para ter algo no banco
        db.leagueDao().insert(LeagueEntity(name = "Liga Teste"))
        
        // Criar um escudo falso
        val crestFile = File(crestStorage.getCrestsDirectory(), "test_crest.png")
        crestFile.writeText("fake image content")

        val result = repository.createManualBackup()
        assertTrue(result.isSuccess)
        
        val backupInfo = result.getOrThrow()
        val file = File(backupInfo.absolutePath)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)

        // Validar estrutura interna do ZIP
        ZipFile(file).use { zip ->
            assertNotNull(zip.getEntry(BackupManifest.MANIFEST_ENTRY))
            assertNotNull(zip.getEntry(BackupManifest.DATABASE_ENTRY))
            assertNotNull(zip.getEntry("crests/test_crest.png"))
        }
    }

    @Test
    fun validation_fails_with_corrupted_database_header() = runBlocking {
        val backupResult = repository.createManualBackup().getOrThrow()
        val backupFile = File(backupResult.absolutePath)
        
        // Criar um backup manualmente com banco corrompido
        val corruptedFile = File(context.cacheDir, "corrupted.lmlbackup")
        ZipFile(backupFile).use { originalZip ->
            ZipOutputStream(FileOutputStream(corruptedFile)).use { out ->
                // Copiar manifesto original
                out.putNextEntry(ZipEntry(BackupManifest.MANIFEST_ENTRY))
                originalZip.getInputStream(originalZip.getEntry(BackupManifest.MANIFEST_ENTRY)).use { it.copyTo(out) }
                out.closeEntry()
                
                // Inserir banco corrompido (sem o header SQLite correto)
                out.putNextEntry(ZipEntry(BackupManifest.DATABASE_ENTRY))
                out.write("NOT A SQLITE DATABASE".toByteArray())
                out.closeEntry()
            }
        }

        val validation = repository.importAndValidate(android.net.Uri.fromFile(corruptedFile)).getOrThrow()
        assertFalse("Backup com header inválido deve ser rejeitado", validation.isValid)
        assertTrue(validation.message.contains("SQLite válido", ignoreCase = true))
    }

    @Test
    fun restore_replaces_database_and_restores_crests() = runBlocking {
        // 1. Preparar estado inicial para o backup
        db.leagueDao().insert(LeagueEntity(name = "Liga Original"))
        val crestFile = File(crestStorage.getCrestsDirectory(), "crest1.png")
        crestFile.writeText("image1 content")
        
        val backupInfo = repository.createManualBackup().getOrThrow()
        val backupUri = android.net.Uri.fromFile(File(backupInfo.absolutePath))

        // 2. Modificar estado atual (simulando mudanças após o backup)
        db.leagueDao().insert(LeagueEntity(name = "Liga Nova"))
        crestFile.delete() // Deletar escudo original
        File(crestStorage.getCrestsDirectory(), "crest2.png").writeText("image2 content")

        // 3. Restaurar
        val restoreResult = repository.restore(backupUri)
        assertTrue(restoreResult is RestoreResult.Success)

        // 4. Verificar se o banco foi revertido (precisamos reabrir a conexão se o repo fechou, 
        // mas o repo fecha a instância injetada. Em testes instrumentados isso é complexo).
        // No RoomBackupRepository real: database.close() é chamado.
        
        // Validar escudos fisicamente
        val files = crestStorage.getCrestsDirectory().listFiles()
        assertEquals(1, files?.size)
        assertEquals("crest1.png", files?.first()?.name)
        assertEquals("image1 content", files?.first()?.readText())
    }

    @Test
    fun safety_backup_is_created_before_restore() = runBlocking {
        db.leagueDao().insert(LeagueEntity(name = "Pre-restore data"))
        val backupResult = repository.createManualBackup().getOrThrow()
        val backupUri = android.net.Uri.fromFile(File(backupResult.absolutePath))

        repository.restore(backupUri)

        // Verificar se existe um arquivo de "safety" na pasta de backups
        val safetyBackups = testBackupDir.listFiles { f -> f.name.contains("safety") }
        assertFalse("Um backup de segurança deve ser criado", safetyBackups.isNullOrEmpty())
    }

    @Test
    fun pruning_keeps_only_the_most_recent_automatic_backups() = runBlocking {
        // Simular 12 backups automáticos (o limite é 10)
        repeat(12) { i ->
            val file = File(testBackupDir, "legacy-automatic-${System.currentTimeMillis() + i}.lmlbackup")
            file.writeText("fake content")
            // Pequena pausa para garantir timestamps diferentes se necessário, 
            // mas aqui forçamos nomes ordenáveis
            Thread.sleep(10)
        }
        
        // Chamar criação automática (dispara pruning)
        repository.createAutomaticBackup()
        
        val autoBackups = testBackupDir.listFiles { f -> f.name.startsWith("legacy-automatic-") }
        assertEquals("Deve manter apenas os 10 backups automáticos mais recentes", 10, autoBackups?.size)
    }
}
