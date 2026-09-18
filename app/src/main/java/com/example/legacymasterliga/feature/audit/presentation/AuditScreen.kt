package com.example.legacymasterliga.feature.audit.presentation

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
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date

@Composable
fun AuditRoute(onBack: () -> Unit, viewModel: AuditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AuditScreen(state, onBack, viewModel::setText, viewModel::setLeague, viewModel::setCategory, viewModel::setAction, viewModel::clearFilters)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    state: AuditUiState,
    onBack: () -> Unit,
    onText: (String) -> Unit,
    onLeague: (Long?) -> Unit,
    onCategory: (String?) -> Unit,
    onAction: (String?) -> Unit,
    onClear: () -> Unit,
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Logs e Auditoria") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Voltar") } }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${state.logs.size} exibidos de ${state.totalCount}", fontWeight = FontWeight.Bold)
                IconButton(onClick = onClear) { Icon(Icons.Outlined.Clear, "Limpar filtros") }
            }
            OutlinedTextField(value = state.query.text, onValueChange = onText, label = { Text("Pesquisar ação, usuário ou detalhe") }, modifier = Modifier.fillMaxWidth())
            FilterDropdown("Liga", state.leagues.map { it.id to it.name }, state.query.leagueId, onLeague)
            FilterDropdown("Categoria", state.categories.map { it to it }, state.query.category, onCategory)
            FilterDropdown("Ação", state.actions.map { it to it }, state.query.action, onAction)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.logs, key = { it.id }) { log ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(log.summary, fontWeight = FontWeight.Bold)
                            Text("${log.category} • ${log.action} • ${log.actorName}", style = MaterialTheme.typography.labelMedium)
                            log.leagueName?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                            log.details?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            Text(DateFormat.getDateTimeInstance().format(Date(log.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterDropdown(label: String, options: List<Pair<T, String>>, selected: T?, onSelected: (T?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: "Todos"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = selectedLabel, onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Todos") }, onClick = { onSelected(null); expanded = false })
            options.forEach { (value, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onSelected(value); expanded = false }) }
        }
    }
}
