package com.example.legacymasterliga.feature.backup.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.feature.backup.domain.AutomaticBackupFrequency
import com.example.legacymasterliga.feature.backup.domain.BackupInfo
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import com.example.legacymasterliga.feature.backup.domain.BackupScheduler
import com.example.legacymasterliga.feature.backup.domain.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupUiState(
    val backups: List<BackupInfo> = emptyList(),
    val frequency: AutomaticBackupFrequency = AutomaticBackupFrequency.OFF,
    val isBusy: Boolean = false,
    val message: String? = null,
    val pendingExport: BackupInfo? = null,
    val requiresRestart: Boolean = false,
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repository: BackupRepository,
    private val scheduler: BackupScheduler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState(frequency = repository.getAutomaticFrequency()))
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _uiState.update { it.copy(backups = repository.listBackups()) }
    }

    fun createBackup() = launchBusy {
        repository.createManualBackup().fold(
            onSuccess = { info ->
                _uiState.update { it.copy(message = "Backup criado: ${info.fileName}") }
                refresh()
            },
            onFailure = { error -> _uiState.update { it.copy(message = error.message ?: "Falha ao criar backup.") } },
        )
    }

    fun requestExport(backup: BackupInfo) {
        _uiState.update { it.copy(pendingExport = backup) }
    }

    fun exportTo(uri: Uri) = launchBusy {
        val backup = _uiState.value.pendingExport ?: return@launchBusy
        repository.exportBackup(backup, uri).fold(
            onSuccess = { _uiState.update { it.copy(message = "Backup exportado com sucesso.", pendingExport = null) } },
            onFailure = { error -> _uiState.update { it.copy(message = error.message ?: "Falha ao exportar.", pendingExport = null) } },
        )
    }

    fun importBackup(uri: Uri) = launchBusy {
        repository.importBackup(uri).fold(
            onSuccess = { info ->
                _uiState.update { it.copy(message = "Backup importado e validado: ${info.fileName}") }
                refresh()
            },
            onFailure = { error -> _uiState.update { it.copy(message = error.message ?: "Arquivo inválido.") } },
        )
    }

    fun restore(uri: Uri) = launchBusy {
        when (val result = repository.restore(uri)) {
            is RestoreResult.Success -> _uiState.update {
                it.copy(
                    message = "Restauração concluída. Uma cópia de segurança foi criada: ${result.safetyBackup.fileName}",
                    requiresRestart = true,
                )
            }
            is RestoreResult.Failure -> _uiState.update { it.copy(message = result.message) }
        }
    }

    fun setFrequency(frequency: AutomaticBackupFrequency) {
        repository.setAutomaticFrequency(frequency)
        scheduler.apply(frequency)
        _uiState.update { it.copy(frequency = frequency, message = "Backup automático atualizado.") }
    }

    fun clearMessage() { _uiState.update { it.copy(message = null) } }

    private fun launchBusy(block: suspend () -> Unit) = viewModelScope.launch {
        _uiState.update { it.copy(isBusy = true) }
        try { block() } finally { _uiState.update { it.copy(isBusy = false) } }
    }
}
