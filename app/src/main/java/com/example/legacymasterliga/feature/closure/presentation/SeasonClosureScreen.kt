package com.example.legacymasterliga.feature.closure.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.SeasonStatus
import java.text.DateFormat
import java.util.Date

@Composable
fun SeasonClosureRoute(onBack: () -> Unit, viewModel: SeasonClosureViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SeasonClosureScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectSeason = viewModel::selectSeason,
        onSelectPrizeCompetition = viewModel::selectPrizeCompetition,
        onSelectPrizeSeason = viewModel::selectPrizeSeason,
        onSavePrizeConfiguration = viewModel::savePrizeConfiguration,
        onCloseSeason = viewModel::closeSeason,
        onAwardPrize = viewModel::awardPrize,
        onClearFeedback = viewModel::clearFeedback,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonClosureScreen(
    state: SeasonClosureUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
    onSelectPrizeCompetition: (Long) -> Unit,
    onSelectPrizeSeason: (Long) -> Unit,
    onSavePrizeConfiguration: (String, String, String) -> Unit,
    onCloseSeason: (Boolean) -> Unit,
    onAwardPrize: (Long, Long, Long, String, Long, String) -> Unit,
    onClearFeedback: () -> Unit,
) {
    var championPrize by rememberSaveable { mutableStateOf("0") }
    var runnerPrize by rememberSaveable { mutableStateOf("0") }
    var participationPrize by rememberSaveable { mutableStateOf("0") }
    var finishCompetition by rememberSaveable { mutableStateOf(false) }
    var leagueMenu by remember { mutableStateOf(false) }
    var showAwardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.selectedCompetitionId, state.prizeConfiguration) {
        val configuration = state.prizeConfiguration
        championPrize = (configuration?.championPrizeCr ?: 0).toString()
        runnerPrize = (configuration?.runnerUpPrizeCr ?: 0).toString()
        participationPrize = (configuration?.participationPrizeCr ?: 0).toString()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Encerramento e premiações") }, navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAwardDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Distribuir premiação")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ExposedDropdownMenuBox(expanded = leagueMenu, onExpandedChange = { leagueMenu = it }) {
                    OutlinedTextField(
                        value = state.leagues.firstOrNull { it.id == state.selectedLeagueId }?.name ?: "Selecione a liga",
                        onValueChange = {}, readOnly = true, label = { Text("Liga") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(leagueMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = leagueMenu, onDismissRequest = { leagueMenu = false }) {
                        state.leagues.forEach { league -> DropdownMenuItem(text = { Text(league.name) }, onClick = { leagueMenu = false; onSelectLeague(league.id) }) }
                    }
                }
            }
            state.competitions.forEach { competition ->
                item { Text(competition.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                competition.seasons.filter { it.status == SeasonStatus.ACTIVE }.forEach { season ->
                    item { FilterChip(selected = state.selectedSeasonId == season.id, onClick = { onSelectSeason(season.id) }, label = { Text(season.name) }) }
                }
            }
            item {
                state.preview?.let { p ->
                    Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Prévia oficial", fontWeight = FontWeight.Bold)
                        Text("Campeão: ${p.championClubName} (${p.championPoints} pts)")
                        Text("Vice: ${p.runnerUpClubName ?: "-"}")
                        Text("Partidas finalizadas: ${p.finishedMatches}/${p.totalMatches}")
                        if (p.totalDisciplinaryFineCr > 0) {
                            Text("Total em multas disciplinares: ${p.totalDisciplinaryFineCr} CR", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    } }
                }
            }
            item { Text("Premiações da competição", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { OutlinedTextField(championPrize, { championPrize = it.filter(Char::isDigit) }, label = { Text("Campeão (CR)") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(runnerPrize, { runnerPrize = it.filter(Char::isDigit) }, label = { Text("Vice-campeão (CR)") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(participationPrize, { participationPrize = it.filter(Char::isDigit) }, label = { Text("Participação por clube (CR)") }, modifier = Modifier.fillMaxWidth()) }
            item {
                Button(
                    onClick = { onSavePrizeConfiguration(championPrize, runnerPrize, participationPrize) },
                    enabled = state.selectedCompetitionId != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar premiações") }
            }
            item { Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(finishCompetition, { finishCompetition = it }); Text("Encerrar também a competição") } }
            item {
                Button(onClick = { onCloseSeason(finishCompetition) }, enabled = state.selectedSeasonId != null, modifier = Modifier.fillMaxWidth()) {
                    Text("Encerrar e aplicar premiações")
                }
            }
            item { Text("Histórico de premiações", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (state.prizeHistory.isEmpty()) {
                item { Text("Nenhuma premiação registrada nesta liga.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.prizeHistory, key = { it.id }) { prize ->
                    Card {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${prize.clubName} • ${prize.amountCr} CR", fontWeight = FontWeight.Bold)
                            Text("${prize.competitionName} — ${prize.seasonName}")
                            Text(prizeTypeLabel(prize.prizeType), color = MaterialTheme.colorScheme.primary)
                            Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(prize.awardedAt)), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
    state.feedback?.let { msg -> AlertDialog(onDismissRequest = onClearFeedback, confirmButton = { TextButton(onClick = onClearFeedback) { Text("OK") } }, title = { Text("Premiações") }, text = { Text(msg) }) }

    if (showAwardDialog) {
        AwardPrizeDialog(
            state = state,
            onSelectCompetition = onSelectPrizeCompetition,
            onSelectSeason = onSelectPrizeSeason,
            onDismiss = { showAwardDialog = false },
            onConfirm = { comp, season, club, t, valCr, d ->
                onAwardPrize(comp, season, club, t, valCr, d)
                showAwardDialog = false
            }
        )
    }
}

@Composable
private fun AwardPrizeDialog(
    state: SeasonClosureUiState,
    onSelectCompetition: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, Long, String, Long, String) -> Unit,
) {
    var competitionId by remember { mutableStateOf<Long?>(null) }
    var seasonId by remember { mutableStateOf<Long?>(null) }
    var clubId by remember { mutableStateOf<Long?>(null) }
    var type by remember { mutableStateOf("CHAMPION") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var compMenu by remember { mutableStateOf(false) }
    var seasonMenu by remember { mutableStateOf(false) }
    var clubMenu by remember { mutableStateOf(false) }
    var typeMenu by remember { mutableStateOf(false) }

    val prizeTypes = listOf("CHAMPION", "RUNNER_UP", "THIRD_PLACE", "PARTICIPATION")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Distribuir Premiação") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    DropdownSelector("Competição", state.prizeCompetitions.find { it.id == competitionId }?.name ?: "Selecione", compMenu, { compMenu = it }) {
                        state.prizeCompetitions.forEach { comp ->
                            DropdownMenuItem(text = { Text(comp.name) }, onClick = { competitionId = comp.id; onSelectCompetition(comp.id); compMenu = false })
                        }
                    }
                }
                item {
                    DropdownSelector("Temporada", state.prizeSeasons.find { it.id == seasonId }?.name ?: "Selecione", seasonMenu, { seasonMenu = it }, enabled = competitionId != null) {
                        state.prizeSeasons.forEach { s ->
                            DropdownMenuItem(text = { Text(s.name) }, onClick = { seasonId = s.id; onSelectSeason(s.id); seasonMenu = false })
                        }
                    }
                }
                item {
                    DropdownSelector("Clube", state.prizeParticipants.find { it.id == clubId }?.name ?: "Selecione", clubMenu, { clubMenu = it }, enabled = seasonId != null) {
                        state.prizeParticipants.forEach { club ->
                            DropdownMenuItem(text = { Text(club.name) }, onClick = { clubId = club.id; clubMenu = false })
                        }
                    }
                }
                item {
                    DropdownSelector("Tipo", prizeTypeLabel(type), typeMenu, { typeMenu = it }) {
                        prizeTypes.forEach { t ->
                            DropdownMenuItem(text = { Text(prizeTypeLabel(t)) }, onClick = { type = t; typeMenu = false })
                        }
                    }
                }
                item {
                    OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Valor (CR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(description, { description = it }, label = { Text("Descrição (opcional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(competitionId!!, seasonId!!, clubId!!, type, amount.toLongOrNull() ?: 0L, description) },
                enabled = competitionId != null && seasonId != null && clubId != null && (amount.toLongOrNull() ?: 0L) >= 0L
            ) { Text("Distribuir") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(label: String, value: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, enabled: Boolean = true, content: @Composable () -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) onExpandedChange(it) }) {
        OutlinedTextField(value = value, onValueChange = {}, readOnly = true, label = { Text(label) }, enabled = enabled, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { onExpandedChange(false) }) { content() }
    }
}

private fun prizeTypeLabel(type: String): String = when (type) {
    "CHAMPION" -> "Campeão"
    "RUNNER_UP" -> "Vice-campeão"
    "THIRD_PLACE" -> "3º Colocado"
    "PARTICIPATION" -> "Participação"
    else -> type
}
