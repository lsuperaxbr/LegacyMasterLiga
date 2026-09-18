package com.example.legacymasterliga.feature.backup.data

import com.example.legacymasterliga.core.database.DatabaseContract

internal data class BackupVersionCheck(
    val isCompatible: Boolean,
    val message: String,
)

/** Validates both the manifest claim and the SQLite header before a database is replaced. */
internal object BackupVersionPolicy {
    fun validate(manifestVersion: Int, sqliteVersion: Int): BackupVersionCheck {
        if (manifestVersion != sqliteVersion) {
            return BackupVersionCheck(
                false,
                "A versão declarada no backup (v$manifestVersion) não corresponde ao banco SQLite (v$sqliteVersion).",
            )
        }
        if (sqliteVersion < DatabaseContract.OLDEST_SUPPORTED_VERSION) {
            return BackupVersionCheck(false, "Versão de banco inválida ou não suportada: v$sqliteVersion.")
        }
        if (sqliteVersion > DatabaseContract.VERSION) {
            return BackupVersionCheck(
                false,
                "Backup criado por uma versão mais nova do aplicativo (banco v$sqliteVersion; suportado até v${DatabaseContract.VERSION}).",
            )
        }
        return if (sqliteVersion == DatabaseContract.VERSION) {
            BackupVersionCheck(true, "Backup compatível com o banco v${DatabaseContract.VERSION}.")
        } else {
            BackupVersionCheck(
                true,
                "Backup compatível; o Room migrará o banco v$sqliteVersion para v${DatabaseContract.VERSION} após o reinício.",
            )
        }
    }
}
