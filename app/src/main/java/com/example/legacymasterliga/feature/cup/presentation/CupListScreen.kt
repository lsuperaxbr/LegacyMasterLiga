package com.example.legacymasterliga.feature.cup.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.feature.competitions.domain.CompetitionSummary

@Composable
fun CupListRoute(
    onBack: () -> Unit,
    onOpenCup: (Long) -> Unit,
    onOpenPrizeConfig: (Long) -> Unit,
    onNavigateToSetup: () -> Unit,
    viewModel: CupListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()

    CupListScreen(
        state = state,
        feedback = feedback,
        onBack = onBack,
        onOpenCup = onOpenCup,
        onOpenPrizeConfig = onOpenPrizeConfig,
        onNavigateToSetup = onNavigateToSetup,
        onDeleteCup = viewModel::deleteCup,
        onFeedbackConsumed = viewModel::clearFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupListScreen(
    state: CupListUiState,
    feedback: String?,
    onBack: () -> Unit,
    onOpenCup: (Long) -> Unit,
    onOpenPrizeConfig: (Long) -> Unit,
    onNavigateToSetup: () -> Unit,
    onDeleteCup: (Long) -> Unit,
    onFeedbackConsumed: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var cupToDelete by remember { mutableStateOf<CompetitionSummary?>(null) }

    LaunchedEffect(feedback) {
        feedback?.let {
            snackbarHostState.showSnackbar(it)
            onFeedbackConsumed()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Copas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.isAdmin) {
                FloatingActionButton(onClick = onNavigateToSetup) {
                    Icon(Icons.Outlined.Add, contentDescription = "Nova Copa")
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.cups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("Nenhuma copa ativa.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                    if (state.isAdmin) {
                        Button(onClick = onNavigateToSetup, modifier = Modifier.padding(top = 16.dp)) {
                            Text("CRIAR PRIMEIRA COPA")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.cups, key = { it.id }) { cup ->
                    val lastSeason = cup.seasons.lastOrNull()
                    Card(
                        onClick = { lastSeason?.let { onOpenCup(it.id) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cup.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (lastSeason != null) "Temporada ${lastSeason.number}" else "Sem temporadas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.isAdmin) {
                                IconButton(onClick = { lastSeason?.let { onOpenPrizeConfig(it.competitionId) } }) {
                                    Icon(Icons.Outlined.Settings, contentDescription = "Configurar Prêmios", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { cupToDelete = cup }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    cupToDelete?.let { cup ->
        AlertDialog(
            onDismissRequest = { cupToDelete = null },
            title = { Text("Excluir Copa") },
            text = { Text("Excluir esta Copa? Todos os confrontos e resultados serão apagados permanentemente. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCup(cup.id)
                        cupToDelete = null
                    }
                ) { 
                    Text("Excluir", color = MaterialTheme.colorScheme.error) 
                }
            },
            dismissButton = { TextButton(onClick = { cupToDelete = null }) { Text("Cancelar") } }
        )
    }
}
