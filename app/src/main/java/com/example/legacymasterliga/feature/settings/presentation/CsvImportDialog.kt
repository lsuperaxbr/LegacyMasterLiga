package com.example.legacymasterliga.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.CsvImportProgress
import com.example.legacymasterliga.domain.model.CsvImportSummary
import com.example.legacymasterliga.domain.model.CsvParseResult
import com.example.legacymasterliga.domain.model.CsvTeamMapping

enum class CsvImportStep {
    WARNING,
    MAPPING,
    PROGRESS,
    SUMMARY
}

@Composable
fun CsvImportDialog(
    parseResult: CsvParseResult,
    existingPlayerCount: Int,
    clubs: List<Club>,
    progress: CsvImportProgress?,
    summary: CsvImportSummary?,
    onDismiss: () -> Unit,
    onConfirmImport: (List<CsvTeamMapping>) -> Unit,
    onRequestReset: () -> Unit,
) {
    var step by remember {
        mutableStateOf(
            if (existingPlayerCount > 0) CsvImportStep.WARNING else CsvImportStep.MAPPING
        )
    }

    // Inicializar mapeamentos dos times do CSV
    val csvTeams = remember(parseResult) {
        parseResult.players.map { it.csvTeam }.distinct().sorted()
    }

    val selectedMappings = remember(csvTeams, clubs) {
        mutableStateMapOf<String, CsvTeamMapping>().apply {
            csvTeams.forEach { csvTeam ->
                val exactMatch = clubs.firstOrNull { it.name.trim().equals(csvTeam.trim(), ignoreCase = true) }
                if (exactMatch != null) {
                    put(csvTeam, CsvTeamMapping(csvTeam = csvTeam, targetClubId = exactMatch.id, isConfirmed = true))
                } else {
                    // VÍNCULO SEGURO: Padrão "Ignorar este time" para evitar importação errada
                    put(csvTeam, CsvTeamMapping(csvTeam = csvTeam, ignore = true))
                }
            }
        }
    }

    when (step) {
        CsvImportStep.WARNING -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Aviso Pró-Importação") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Esta liga já possui $existingPlayerCount jogadores cadastrados.\n\n" +
                            "Recomendamos executar o Reset da Liga antes de importar para evitar elencos misturados ou duplicidades.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { step = CsvImportStep.MAPPING }) {
                        Text("Prosseguir Mesmo Assim")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onRequestReset()
                        }
                    ) {
                        Text("Executar Reset Primeiro")
                    }
                }
            )
        }

        CsvImportStep.MAPPING -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Mapeamento de Times do CSV (${csvTeams.size} identificados)") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Vincule cada time do CSV ao clube correspondente da liga. Times sem correspondência ficam como 'Ignorar este time' por padrão.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        csvTeams.forEach { csvTeam ->
                            val currentMapping = selectedMappings[csvTeam] ?: CsvTeamMapping(csvTeam = csvTeam, ignore = true)
                            val teamPlayerCount = parseResult.players.count { it.csvTeam == csvTeam }

                            TeamMappingRow(
                                csvTeam = csvTeam,
                                playerCount = teamPlayerCount,
                                currentMapping = currentMapping,
                                clubs = clubs,
                                onMappingChanged = { updated ->
                                    selectedMappings[csvTeam] = updated
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    val activeImportsCount = selectedMappings.values.filter { !it.ignore && (it.targetClubId != null || it.isBank) }.size
                    Button(
                        onClick = {
                            step = CsvImportStep.PROGRESS
                            onConfirmImport(selectedMappings.values.toList())
                        },
                        enabled = activeImportsCount > 0
                    ) {
                        Text("Importar $activeImportsCount Times")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                }
            )
        }

        CsvImportStep.PROGRESS -> {
            if (summary != null) {
                step = CsvImportStep.SUMMARY
            } else {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Importando Elencos...") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                if (progress != null) {
                                    "Processando bloco ${progress.currentChunk} de ${progress.totalChunks} " +
                                            "(${progress.processedPlayers}/${progress.totalPlayers} atletas)..."
                                } else {
                                    "Preparando lote de inserção..."
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {}
                )
            }
        }

        CsvImportStep.SUMMARY -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Resumo da Importação") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (summary != null) {
                            Text("Atletas Importados por Clube:", fontWeight = FontWeight.Bold)
                            summary.importedCountByClub.forEach { (clubName, count) ->
                                Text("• $clubName: $count atletas", style = MaterialTheme.typography.bodyMedium)
                            }

                            if (summary.ignoredTeams.isNotEmpty()) {
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                Text("Times Ignorados (${summary.ignoredTeams.size}):", fontWeight = FontWeight.Bold)
                                Text(summary.ignoredTeams.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                            }

                            if (summary.skippedDuplicates > 0) {
                                Text("• Duplicatas ignoradas: ${summary.skippedDuplicates}", style = MaterialTheme.typography.bodySmall)
                            }

                            if (summary.lineErrors.isNotEmpty()) {
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                Text("Linhas com Alerta/Erro (${summary.lineErrors.size}):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                summary.lineErrors.forEach { err ->
                                    Text("• $err", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = onDismiss) { Text("Concluir") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamMappingRow(
    csvTeam: String,
    playerCount: Int,
    currentMapping: CsvTeamMapping,
    clubs: List<Club>,
    onMappingChanged: (CsvTeamMapping) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when {
        currentMapping.ignore -> "Ignorar este time"
        currentMapping.isBank -> "Banco da Liga"
        currentMapping.targetClubId != null -> clubs.firstOrNull { it.id == currentMapping.targetClubId }?.name ?: "Clube Selecionado"
        else -> "Ignorar este time"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("$csvTeam ($playerCount atletas)", fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = displayText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Destino no App") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ignorar este time") },
                        onClick = {
                            onMappingChanged(CsvTeamMapping(csvTeam = csvTeam, ignore = true))
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Banco da Liga") },
                        onClick = {
                            onMappingChanged(CsvTeamMapping(csvTeam = csvTeam, isBank = true))
                            expanded = false
                        }
                    )
                    HorizontalDivider()
                    clubs.filter { !it.isBank }.forEach { club ->
                        DropdownMenuItem(
                            text = { Text(club.name) },
                            onClick = {
                                onMappingChanged(CsvTeamMapping(csvTeam = csvTeam, targetClubId = club.id))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
