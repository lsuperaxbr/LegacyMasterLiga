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
import androidx.compose.material3.OutlinedButton
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

    // Inicializar lista dos times identificados no CSV
    val csvTeams = remember(parseResult) {
        parseResult.players.map { it.csvTeam }.distinct().sorted()
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
                title = { Text("Importação Direta de Elencos PES 6") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (parseResult.players.isEmpty()) {
                            Text(
                                "Nenhum time ou atleta válido pôde ser extraído do arquivo selecionado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )

                            if (!parseResult.firstRawLine.isNullOrBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Primeira linha lida no arquivo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Text(parseResult.firstRawLine, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            if (parseResult.errors.isNotEmpty()) {
                                val displayedErrors = parseResult.errors.take(20)
                                val remainingCount = parseResult.errors.size - displayedErrors.size

                                Text("Motivos do erro / alertas (${parseResult.errors.size} linhas afetadas):", fontWeight = FontWeight.SemiBold)
                                displayedErrors.forEach { err ->
                                    Text("• $err", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                if (remainingCount > 0) {
                                    Text("• ... e mais $remainingCount outras linhas com erro.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        } else {
                            val bankCount = parseResult.teamPlayerCounts["Banco da Liga"] ?: 0
                            val clubTeams = csvTeams.filterNot { it.equals("Banco da Liga", ignoreCase = true) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Resumo do Processamento do Arquivo:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("• Total de Atletas Extraídos: ${parseResult.players.size}", style = MaterialTheme.typography.bodyMedium)
                                    Text("• Clubes Identificados (${clubTeams.size}): ${clubTeams.take(8).joinToString(", ")}${if (clubTeams.size > 8) "..." else ""}", style = MaterialTheme.typography.bodyMedium)
                                    if (bankCount > 0) {
                                        Text("• Atletas sem time (Banco da Liga): $bankCount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Text(
                                "O sistema criará/associará automaticamente todos os clubes identificados e importará os atletas de forma direta, sem necessidade de intervenção manual.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            step = CsvImportStep.PROGRESS
                            onConfirmImport(emptyList())
                        },
                        enabled = parseResult.players.isNotEmpty()
                    ) {
                        Text("Confirmar Importação de ${parseResult.players.size} Atletas")
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
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (summary != null) {
                            Text("Resumo Geral da Operação:", fontWeight = FontWeight.Bold)
                            Text("• Clubes novos criados: ${summary.createdClubsCount}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Atletas vinculados a clubes novos: ${summary.newClubPlayersCount}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Atletas colocados no Banco da Liga: ${summary.bankPlayersCount}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Atletas ignorados: ${summary.ignoredPlayersCount}", style = MaterialTheme.typography.bodyMedium)
                            if (summary.skippedDuplicates > 0) {
                                Text("• Duplicatas ignoradas: ${summary.skippedDuplicates}", style = MaterialTheme.typography.bodyMedium)
                            }

                            HorizontalDivider(Modifier.padding(vertical = 4.dp))

                            Text("Atletas Importados por Clube:", fontWeight = FontWeight.Bold)
                            summary.importedCountByClub.forEach { (clubName, count) ->
                                Text("• $clubName: $count atletas", style = MaterialTheme.typography.bodyMedium)
                            }

                            if (summary.ignoredTeams.isNotEmpty()) {
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                Text("Times Ignorados (${summary.ignoredTeams.size}):", fontWeight = FontWeight.Bold)
                                Text(summary.ignoredTeams.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                            }

                            if (summary.lineErrors.isNotEmpty()) {
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                val displayedSummaryErrors = summary.lineErrors.take(20)
                                val remainingSummaryErrorsCount = summary.lineErrors.size - displayedSummaryErrors.size

                                Text("Linhas com Alerta/Erro (${summary.lineErrors.size}):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                displayedSummaryErrors.forEach { err ->
                                    Text("• $err", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                if (remainingSummaryErrorsCount > 0) {
                                    Text("• ... e mais $remainingSummaryErrorsCount outras linhas com erro.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
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
