package com.example.legacymasterliga.feature.cup.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CupSetupRoute(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: CupSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.createdSeasonId) {
        state.createdSeasonId?.let { onCreated(it) }
    }

    CupSetupScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSetName = viewModel::setCupName,
        onSetLegs = viewModel::setLegs,
        onToggleClub = viewModel::toggleClub,
        onSelectAll = viewModel::selectAll,
        onToggleManualOrder = viewModel::toggleManualOrder,
        onMoveUp = viewModel::moveClubUp,
        onMoveDown = viewModel::moveClubDown,
        onConfirm = viewModel::createCup,
        onFeedbackConsumed = viewModel::clearFeedback,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupSetupScreen(
    state: CupSetupUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSetName: (String) -> Unit,
    onSetLegs: (Int) -> Unit,
    onToggleClub: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onToggleManualOrder: () -> Unit,
    onMoveUp: (Long) -> Unit,
    onMoveDown: (Long) -> Unit,
    onConfirm: () -> Unit,
    onFeedbackConsumed: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            snackbarHostState.showSnackbar(it)
            onFeedbackConsumed()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nova Copa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = onConfirm,
                    enabled = state.selectedClubIds.size >= 2 && !state.isCreating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("SORTEAR E CRIAR COPA")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Configuração básica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.cupName,
                    onValueChange = onSetName,
                    label = { Text("Nome da Competição") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text("Formato dos confrontos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = state.legs == 1,
                        onClick = { onSetLegs(1) },
                        label = { Text("Jogo Único") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = state.legs == 2,
                        onClick = { onSetLegs(2) },
                        label = { Text("Ida e Volta") }
                    )
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ordem Manual", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = state.manualOrderMode, onCheckedChange = { onToggleManualOrder() })
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Participantes (${state.selectedClubIds.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (!state.manualOrderMode) {
                        TextButton(onClick = onSelectAll) {
                            Text("Selecionar todos")
                        }
                    }
                }
                if (state.availableClubs.isEmpty()) {
                    Text("Nenhum clube disponível nesta liga.", color = MaterialTheme.colorScheme.error)
                }
            }

            if (state.manualOrderMode) {
                val selectedClubs = state.orderedSelectedClubs.mapNotNull { id -> state.availableClubs.find { it.id == id } }
                items(selectedClubs, key = { "ord_${it.id}" }) { club ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${selectedClubs.indexOf(club) + 1}. ${club.name}", modifier = Modifier.weight(1f))
                            IconButton(onClick = { onMoveUp(club.id) }) { Icon(Icons.Outlined.ArrowUpward, null) }
                            IconButton(onClick = { onMoveDown(club.id) }) { Icon(Icons.Outlined.ArrowDownward, null) }
                        }
                    }
                }
                if (selectedClubs.size >= 2) {
                    item {
                        Text("Preview dos confrontos:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        selectedClubs.chunked(2).forEachIndexed { i, pair ->
                            if (pair.size == 2) {
                                Text("Confronto ${i + 1}: ${pair[0].name} x ${pair[1].name}", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Aguardando adversário para ${pair[0].name}...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                items(state.availableClubs, key = { it.id }) { club ->
                    val selected = state.selectedClubIds.contains(club.id)
                    Surface(
                        onClick = { onToggleClub(club.id) },
                        shape = MaterialTheme.shapes.medium,
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = selected, onCheckedChange = { onToggleClub(club.id) })
                            Spacer(Modifier.width(8.dp))
                            Text(club.name, fontWeight = FontWeight.Medium)
                            if (selected) {
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
