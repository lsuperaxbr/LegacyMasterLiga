package com.example.legacymasterliga.feature.competitions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.legacymasterliga.domain.model.Club
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.feature.competitions.domain.CompetitionSummary

@Composable
fun CompetitionsRoute(
    onBack: () -> Unit,
    viewModel: CompetitionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CompetitionsScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onCreateLeague = viewModel::createLeague,
        onCreateCompetition = viewModel::createCompetition,
        onCreateNextSeason = viewModel::createNextSeason,
        onFeedbackConsumed = viewModel::clearFeedback,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionsScreen(
    state: CompetitionsUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onCreateLeague: (String) -> Unit,
    onCreateCompetition: (String, CompetitionType, CompetitionFormat, String, List<Long>, Int?, Int?, Int) -> Unit,
    onCreateNextSeason: (Long, String, List<Long>) -> Unit,
    onFeedbackConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLeagueDialog by rememberSaveable { mutableStateOf(false) }
    var showCompetitionDialog by rememberSaveable { mutableStateOf(false) }
    var competitionForNextSeason by remember { mutableStateOf<CompetitionSummary?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.feedback) {
        val feedback = state.feedback ?: return@LaunchedEffect
        val message = when (feedback) {
            is CompetitionFeedback.Success -> feedback.message
            is CompetitionFeedback.Error -> feedback.message
        }
        snackbarHostState.showSnackbar(message)
        onFeedbackConsumed()
    }

    val isAdmin = state.role == UserRole.ADMINISTRATOR

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Competições") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { showLeagueDialog = true }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Criar liga")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showCompetitionDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = "Criar competição")
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            item {
                Text("Liga selecionada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LeagueSelector(
                    leagues = state.leagues,
                    selectedLeagueId = state.selectedLeagueId,
                    onSelect = onSelectLeague,
                    onCreate = { showLeagueDialog = true },
                )
            }

            item {
                Text(
                    text = "Ligas, copas e temporadas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Cada competição mantém seus placares e temporadas separados.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.competitions.isEmpty()) {
                item {
                    EmptyCompetitions(onCreate = { showCompetitionDialog = true })
                }
            } else {
                items(state.competitions, key = { it.id }) { competition ->
                    CompetitionCard(
                        competition = competition,
                        canManage = isAdmin,
                        onCreateNextSeason = { competitionForNextSeason = competition },
                    )
                }
            }
        }
    }

    if (showLeagueDialog) {
        CreateLeagueDialog(
            onDismiss = { showLeagueDialog = false },
            onConfirm = {
                showLeagueDialog = false
                onCreateLeague(it)
            },
        )
    }

    if (showCompetitionDialog) {
        CreateCompetitionDialog(
            enabled = state.selectedLeagueId != null,
            availableClubs = state.availableClubs,
            onDismiss = { showCompetitionDialog = false },
            onConfirm = { name, type, format, season, participants, groups, qualified, knockoutLegs ->
                showCompetitionDialog = false
                onCreateCompetition(name, type, format, season, participants, groups, qualified, knockoutLegs)
            },
        )
    }

    if (competitionForNextSeason != null) {
        CreateNextSeasonDialog(
            competitionName = competitionForNextSeason!!.name,
            availableClubs = state.availableClubs,
            onDismiss = { competitionForNextSeason = null },
            onConfirm = { name, participants ->
                onCreateNextSeason(competitionForNextSeason!!.id, name, participants)
                competitionForNextSeason = null
            }
        )
    }
}

@Composable
private fun LeagueSelector(
    leagues: List<League>,
    selectedLeagueId: Long?,
    onSelect: (Long) -> Unit,
    onCreate: () -> Unit,
) {
    if (leagues.isEmpty()) {
        OutlinedButton(onClick = onCreate) { Text("Criar primeira liga") }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        leagues.forEach { league ->
            val selected = league.id == selectedLeagueId
            if (selected) {
                Button(onClick = { onSelect(league.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("${league.name} • ${league.currencyCode}")
                }
            } else {
                OutlinedButton(onClick = { onSelect(league.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("${league.name} • ${league.currencyCode}")
                }
            }
        }
    }
}

@Composable
private fun EmptyCompetitions(onCreate: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nenhuma competição cadastrada", fontWeight = FontWeight.Bold)
            Text("Crie uma liga ou copa e escolha se os jogos terão ida ou ida e volta.")
            Button(onClick = onCreate) { Text("Criar competição") }
        }
    }
}

@Composable
private fun CompetitionCard(
    competition: CompetitionSummary,
    canManage: Boolean,
    onCreateNextSeason: () -> Unit,
) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(competition.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${competition.type.label()} • ${competition.format.label()}")
                }
                AssistChip(onClick = {}, label = { Text(competition.status.label()) })
            }
            Text("Temporadas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (competition.seasons.isEmpty()) {
                Text("Nenhuma temporada cadastrada.")
            } else {
                competition.seasons.forEach { season ->
                    Text("• ${season.name} — ${season.status.label()}")
                }
            }
            if (canManage) {
                OutlinedButton(onClick = onCreateNextSeason) {
                    Text(if (competition.seasons.any { it.status == SeasonStatus.ACTIVE }) "Encerrar atual e criar próxima" else "Criar próxima temporada")
                }
            }
        }
    }
}

@Composable
private fun CreateLeagueDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova liga") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da liga") },
                    singleLine = true,
                )
                Text("A moeda será CR.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.trim().length >= 3) { Text("Criar") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateCompetitionDialog(
    enabled: Boolean,
    availableClubs: List<Club>,
    onDismiss: () -> Unit,
    onConfirm: (String, CompetitionType, CompetitionFormat, String, List<Long>, Int?, Int?, Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var seasonName by rememberSaveable { mutableStateOf("Temporada 1") }
    var format by rememberSaveable { mutableStateOf(CompetitionFormat.HOME_AND_AWAY) }
    var selectedClubIds by remember { mutableStateOf(emptySet<Long>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova competição") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { OutlinedTextField(name, { name = it }, label = { Text("Nome") }, singleLine = true) }
                item { OutlinedTextField(seasonName, { seasonName = it }, label = { Text("Primeira temporada") }, singleLine = true) }
                item {
                    Text("Formato", fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceButton("Somente ida", format == CompetitionFormat.SINGLE_ROUND) { format = CompetitionFormat.SINGLE_ROUND }
                        ChoiceButton("Ida e volta", format == CompetitionFormat.HOME_AND_AWAY) { format = CompetitionFormat.HOME_AND_AWAY }
                    }
                }

                item {
                    Text("Participantes (${selectedClubIds.size})", fontWeight = FontWeight.Bold)
                    Text("Selecione de 3 a 32 clubes.", style = MaterialTheme.typography.bodySmall)
                    if (availableClubs.isEmpty()) {
                        Text("Cadastre clubes antes de criar competições.", color = MaterialTheme.colorScheme.error)
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableClubs.forEach { club ->
                                FilterChip(
                                    selected = club.id in selectedClubIds,
                                    onClick = {
                                        selectedClubIds = if (club.id in selectedClubIds) selectedClubIds - club.id else selectedClubIds + club.id
                                    },
                                    label = { Text(club.name) }
                                )
                            }
                        }
                    }
                }
                if (!enabled) item { Text("Crie ou selecione uma liga primeiro.", color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(
                        name, CompetitionType.LEAGUE, format, seasonName, selectedClubIds.toList(),
                        null, null, 1
                    ) 
                },
                enabled = enabled && name.trim().length >= 3 && seasonName.isNotBlank() && selectedClubIds.size >= 3,
            ) { Text("Criar") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateNextSeasonDialog(
    competitionName: String,
    availableClubs: List<Club>,
    onDismiss: () -> Unit,
    onConfirm: (String, List<Long>) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var selectedClubIds by remember { mutableStateOf(emptySet<Long>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Temporada: $competitionName") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { OutlinedTextField(name, { name = it }, label = { Text("Nome da temporada (ex: 2026)") }, singleLine = true) }
                item {
                    Text("Participantes (${selectedClubIds.size})", fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableClubs.forEach { club ->
                            FilterChip(
                                selected = club.id in selectedClubIds,
                                onClick = {
                                    selectedClubIds = if (club.id in selectedClubIds) selectedClubIds - club.id else selectedClubIds + club.id
                                },
                                label = { Text(club.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, selectedClubIds.toList()) }, enabled = name.isNotBlank() && selectedClubIds.size >= 2) {
                Text("Criar")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ChoiceButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick) { Text(label) }
    else OutlinedButton(onClick = onClick) { Text(label) }
}

private fun CompetitionType.label(): String = when (this) {
    CompetitionType.LEAGUE -> "Liga"
    CompetitionType.CUP -> "Copa"
    CompetitionType.SUPER_CUP -> "Supercopa"
}

private fun CompetitionFormat.label(): String = when (this) {
    CompetitionFormat.SINGLE_ROUND -> "Somente ida"
    CompetitionFormat.HOME_AND_AWAY -> "Ida e volta"
    CompetitionFormat.SINGLE_MATCH -> "Jogo único"
    CompetitionFormat.KNOCKOUT -> "Mata-mata"
    CompetitionFormat.GROUPS_AND_KNOCKOUT -> "Grupos + Mata-mata"
}

private fun com.example.legacymasterliga.core.model.CompetitionStatus.label(): String = when (this) {
    com.example.legacymasterliga.core.model.CompetitionStatus.DRAFT -> "Rascunho"
    com.example.legacymasterliga.core.model.CompetitionStatus.ACTIVE -> "Ativa"
    com.example.legacymasterliga.core.model.CompetitionStatus.FINISHED -> "Encerrada"
    com.example.legacymasterliga.core.model.CompetitionStatus.ARCHIVED -> "Arquivada"
}

private fun SeasonStatus.label(): String = when (this) {
    SeasonStatus.DRAFT -> "Rascunho"
    SeasonStatus.ACTIVE -> "Ativa"
    SeasonStatus.FINISHED -> "Encerrada"
    SeasonStatus.ARCHIVED -> "Arquivada"
}
