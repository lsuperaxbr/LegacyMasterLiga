package com.example.legacymasterliga.feature.attributes.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.domain.model.Club

@Composable
fun ImportSquadFromFolderRoute(
    onBack: () -> Unit,
    viewModel: ImportSquadFromFolderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val folderName = uri.lastPathSegment?.split(":")?.lastOrNull() ?: uri.lastPathSegment
            viewModel.selectFolder(uri, folderName)
        }
    }

    ImportSquadFromFolderScreen(
        state = state,
        onBack = onBack,
        onSelectClub = viewModel::selectClub,
        onSelectFolderClick = { folderLauncher.launch(null) },
        onImportSquadClick = { viewModel.importSquad(context) },
        onDismissFeedback = viewModel::clearFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSquadFromFolderScreen(
    state: ImportSquadFromFolderUiState,
    onBack: () -> Unit,
    onSelectClub: (Long) -> Unit,
    onSelectFolderClick: () -> Unit,
    onImportSquadClick: () -> Unit,
    onDismissFeedback: () -> Unit
) {
    var clubDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Importar Elenco por Pasta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Importação do Elenco via SAF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Selecione o clube e a pasta no dispositivo (ex: /sdcard/Download/LegacyMasterLiga/ClubeName). Todos os arquivos .txt com fichas de atletas serão lidos e substituirão completamente o elenco antigo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Seletor de Clube
            Text("Clube de Destino", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ExposedDropdownMenuBox(
                expanded = clubDropdownExpanded,
                onExpandedChange = { clubDropdownExpanded = !clubDropdownExpanded }
            ) {
                val selectedClub = state.clubs.find { it.id == state.selectedClubId }
                OutlinedTextField(
                    value = selectedClub?.name ?: "Selecione um clube",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Clube") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clubDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = clubDropdownExpanded,
                    onDismissRequest = { clubDropdownExpanded = false }
                ) {
                    state.clubs.forEach { club ->
                        DropdownMenuItem(
                            text = { Text(club.name) },
                            onClick = {
                                onSelectClub(club.id)
                                clubDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Seletor de Pasta SAF
            Text("Pasta do Clube", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.folderName ?: "Nenhuma pasta selecionada",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (state.selectedFolderUri != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.selectedFolderUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.selectedFolderUri != null) {
                            Text(
                                text = "Acesso SAF concedido",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Button(
                        onClick = onSelectFolderClick,
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.selectedFolderUri == null) "Selecionar" else "Alterar")
                    }
                }
            }

            // Mensagem de Erro / Alerta
            if (state.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Botão Importar
            Button(
                onClick = onImportSquadClick,
                enabled = !state.isLoading && state.selectedClubId != null && state.selectedFolderUri != null,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(state.progressMessage ?: "Processando...")
                } else {
                    Icon(Icons.Outlined.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar Elenco Completo")
                }
            }

            // Resultado da Importação
            state.importResult?.let { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Importação Concluída!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("⚽ Jogadores importados com sucesso: ${res.importedCount}", fontWeight = FontWeight.Bold)
                        Text("📁 Arquivos ignorados (não .txt): ${res.ignoredCount}", style = MaterialTheme.typography.bodyMedium)
                        Text("📄 Total de arquivos varridos: ${res.totalFilesProcessed}", style = MaterialTheme.typography.bodySmall)

                        if (res.failedFiles.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "⚠️ Arquivos com falha no parsing (${res.failedFiles.size}):",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            res.failedFiles.forEach { f ->
                                Text("• $f", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
