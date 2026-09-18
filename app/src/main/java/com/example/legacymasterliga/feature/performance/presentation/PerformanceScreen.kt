package com.example.legacymasterliga.feature.performance.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.ui.components.LegacyEmptyState

@Composable
fun PerformanceRoute(
    onBack: () -> Unit,
    viewModel: PerformanceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PerformanceScreen(
        state = state,
        onBack = onBack,
        onClearMetrics = viewModel::clearMetrics,
        onClearErrors = viewModel::clearErrors,
        onClearCache = viewModel::clearCache,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerformanceScreen(
    state: PerformanceUiState,
    onBack: () -> Unit,
    onClearMetrics: () -> Unit,
    onClearErrors: () -> Unit,
    onClearCache: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Desempenho") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cache local", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${state.cacheEntries} entradas em memória")
                        OutlinedButton(onClick = onClearCache) { Text("Limpar cache") }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Métricas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Button(onClick = onClearMetrics) { Text("Limpar") }
                }
            }
            if (state.metrics.isEmpty()) {
                item { LegacyEmptyState(title = "Sem métricas ainda", message = "Use o aplicativo para registrar tempos de operação.") }
            } else {
                items(state.metrics, key = { it.name }) { metric ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(metric.name, fontWeight = FontWeight.Bold)
                            Text("Média: ${metric.averageDurationMs} ms • Última: ${metric.lastDurationMs} ms")
                            Text("Máxima: ${metric.maxDurationMs} ms • Execuções: ${metric.count}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Erros recuperáveis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Button(onClick = onClearErrors) { Text("Limpar") }
                }
            }
            if (state.errors.isEmpty()) {
                item { LegacyEmptyState(title = "Nenhum erro registrado", message = "O aplicativo está operando normalmente.") }
            } else {
                items(state.errors, key = { "${it.source}-${it.occurredAt}" }) { error ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(error.source, fontWeight = FontWeight.Bold)
                            Text(error.message)
                        }
                    }
                }
            }
        }
    }
}
