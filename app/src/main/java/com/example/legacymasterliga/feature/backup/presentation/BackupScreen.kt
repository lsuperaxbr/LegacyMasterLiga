package com.example.legacymasterliga.feature.backup.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.feature.backup.domain.AutomaticBackupFrequency
import com.example.legacymasterliga.feature.backup.domain.BackupInfo
import java.text.DateFormat
import java.util.Date
import kotlin.system.exitProcess

@Composable
fun BackupRoute(onBack: () -> Unit, viewModel: BackupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> if (uri != null) viewModel.exportTo(uri) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) viewModel.importBackup(uri) }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) viewModel.restore(uri) }

    BackupScreen(
        state = state,
        onBack = onBack,
        onCreate = viewModel::createBackup,
        onExport = { backup ->
            viewModel.requestExport(backup)
            exportLauncher.launch(backup.fileName)
        },
        onImport = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
        onRestore = { restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
        onFrequency = viewModel::setFrequency,
        onRestart = { restartApplication(context) },
        onMessageShown = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupScreen(
    state: BackupUiState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onExport: (BackupInfo) -> Unit,
    onImport: () -> Unit,
    onRestore: () -> Unit,
    onFrequency: (AutomaticBackupFrequency) -> Unit,
    onRestart: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); onMessageShown() }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backup e restauração") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Voltar") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Proteção dos dados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Os backups incluem todo o banco da liga, manifesto de versão e SHA-256 para detectar corrupção.")
                        Button(onClick = onCreate, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Save, null); Text("  Criar backup agora")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onImport, enabled = !state.isBusy, modifier = Modifier.weight(1f)) { Text("Importar") }
                            OutlinedButton(onClick = onRestore, enabled = !state.isBusy, modifier = Modifier.weight(1f)) { Text("Restaurar") }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Backup automático", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        AutomaticBackupFrequency.entries.forEach { frequency ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = state.frequency == frequency, onClick = { onFrequency(frequency) })
                                Text(
                                    when (frequency) {
                                        AutomaticBackupFrequency.OFF -> "Desativado"
                                        AutomaticBackupFrequency.DAILY -> "Diário"
                                        AutomaticBackupFrequency.WEEKLY -> "Semanal"
                                    },
                                )
                            }
                        }
                    }
                }
            }
            item { Text("Backups neste aparelho", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(state.backups, key = { it.absolutePath }) { backup ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(backup.fileName, fontWeight = FontWeight.Bold)
                        Text(DateFormat.getDateTimeInstance().format(Date(backup.createdAt)))
                        Text("${backup.sizeBytes / 1024} KB • App ${backup.appVersion} • Banco v${backup.databaseVersion}")
                        if (backup.isSafetyCopy) Text("Cópia de segurança pré-restauração", color = MaterialTheme.colorScheme.primary)
                        OutlinedButton(onClick = { onExport(backup) }, enabled = !state.isBusy) {
                            Icon(Icons.Outlined.CloudUpload, null); Text("  Exportar")
                        }
                    }
                }
            }
            if (state.isBusy) item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() } }
        }
    }
    if (state.requiresRestart) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Reinício necessário") },
            text = { Text("O banco foi restaurado com segurança. Reinicie o aplicativo para carregar os dados recuperados.") },
            confirmButton = {
                Button(onClick = onRestart) { Icon(Icons.Outlined.RestartAlt, null); Text("  Reiniciar agora") }
            },
        )
    }
}

private fun restartApplication(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    exitProcess(0)
}
