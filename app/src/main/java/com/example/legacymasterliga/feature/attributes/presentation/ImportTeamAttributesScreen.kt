package com.example.legacymasterliga.feature.attributes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ImportTeamAttributesRoute(
    onBack: () -> Unit,
    viewModel: ImportTeamAttributesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ImportTeamAttributesScreen(
        state = state,
        onBack = onBack,
        onSelectClub = viewModel::selectClub,
        onTextChange = viewModel::onTextChange,
        onImport = viewModel::importAttributes,
        onClearResult = viewModel::clearResult
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTeamAttributesScreen(
    state: ImportTeamAttributesUiState,
    onBack: () -> Unit,
    onSelectClub: (Long) -> Unit,
    onTextChange: (String) -> Unit,
    onImport: () -> Unit,
    onClearResult: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedClub = state.clubs.firstOrNull { it.id == state.selectedClubId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar Atributos (TXT)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.errorMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Atualização em Lote de Atributos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Selecione o clube de destino e cole no campo abaixo o conteúdo do arquivo .txt com os atletas e seus atributos (chave: valor).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Seletor de Clube
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedClub?.name ?: "Selecione um clube",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Clube Alvo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    state.clubs.forEach { club ->
                        DropdownMenuItem(
                            text = { Text(club.name) },
                            onClick = {
                                onSelectClub(club.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Campo de Texto Grande Multilinha
            OutlinedTextField(
                value = state.rawText,
                onValueChange = onTextChange,
                label = { Text("Conteúdo do arquivo .txt do time") },
                placeholder = { Text("Name: Bruno Fernandes\nPosition: MEI\nOVR: 86\nAttack: 84\nDefence: 68\n...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 320.dp),
                minLines = 8,
                maxLines = 14
            )

            // Botão de Ação
            Button(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isProcessing && state.selectedClubId != null && state.rawText.isNotBlank()
            ) {
                if (state.isProcessing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(end = 4.dp))
                        Text("Atualizando Atributos...")
                    }
                } else {
                    Text("Atualizar Atributos do Time")
                }
            }

            // Resultado da Importação
            val result = state.result
            if (result != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.unmappedPlayerNames.isEmpty())
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (result.unmappedPlayerNames.isEmpty()) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                                contentDescription = null
                            )
                            Text(
                                "Resultado da Atualização",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text("• Atletas no arquivo: ${result.totalParsed}")
                        Text("• Atletas atualizados com sucesso: ${result.totalUpdated}", fontWeight = FontWeight.Bold)

                        if (result.unmappedPlayerNames.isNotEmpty()) {
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Text(
                                "Atletas não encontrados no clube (${result.unmappedPlayerNames.size}):",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            result.unmappedPlayerNames.forEach { unmappedName ->
                                Text("• $unmappedName", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        OutlinedButton(
                            onClick = onClearResult,
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Limpar Resultado")
                        }
                    }
                }
            }
        }
    }
}
