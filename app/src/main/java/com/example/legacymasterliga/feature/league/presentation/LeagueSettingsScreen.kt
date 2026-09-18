package com.example.legacymasterliga.feature.league.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.TieBreakCriterion

@Composable
fun LeagueSettingsRoute(
    onBack: () -> Unit,
    viewModel: LeagueSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LeagueSettingsScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectCompetition = viewModel::selectCompetition,
        onSaveRules = viewModel::saveRules,
        onClearMessage = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueSettingsScreen(
    state: LeagueSettingsUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectCompetition: (Long) -> Unit,
    onSaveRules: (Int, Int, Int, Long, Long, List<TieBreakCriterion>, Boolean) -> Unit,
    onClearMessage: () -> Unit,
) {
    var win by remember { mutableStateOf("3") }
    var draw by remember { mutableStateOf("1") }
    var loss by remember { mutableStateOf("0") }
    var yellowFine by remember { mutableStateOf("0") }
    var redFine by remember { mutableStateOf("0") }
    var criteria by remember { mutableStateOf(emptyList<TieBreakCriterion>()) }
    var highlightLeader by remember { mutableStateOf(true) }

    LaunchedEffect(state.rules) {
        state.rules?.let {
            win = it.pointsForWin.toString()
            draw = it.pointsForDraw.toString()
            loss = it.pointsForLoss.toString()
            yellowFine = it.yellowCardFineCr.toString()
            redFine = it.redCardFineCr.toString()
            criteria = it.tieBreakCriteria
            highlightLeader = it.highlightLeader
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações da Liga") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.message?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(it)
                        TextButton(onClick = onClearMessage) { Text("OK") }
                    }
                }
            }

            Text("Esta área reúne as regras esportivas da sua competição.", style = MaterialTheme.typography.bodyMedium)

            SettingsCard("Liga e Competição") {
                Text("Selecione a liga", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    state.leagues.forEach { league ->
                        FilterChip(
                            selected = league.id == state.selectedLeagueId,
                            onClick = { onSelectLeague(league.id) },
                            label = { Text(league.name) },
                        )
                    }
                }
                
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                
                Text("Competição", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    state.competitions.forEach { competition ->
                        FilterChip(
                            selected = competition.id == state.selectedCompetitionId,
                            onClick = { onSelectCompetition(competition.id) },
                            label = { Text(competition.name) },
                        )
                    }
                }
            }

            if (state.selectedCompetitionId != null) {
                SettingsCard("Regras de Pontuação") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreField("Vitória", win) { win = it }
                        ScoreField("Empate", draw) { draw = it }
                        ScoreField("Derrota", loss) { loss = it }
                    }
                }

                SettingsCard("Multas Disciplinares") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreField("Amarelo (CR)", yellowFine) { yellowFine = it }
                        ScoreField("Vermelho (CR)", redFine) { redFine = it }
                    }
                }

                SettingsCard("Critérios de Desempate") {
                    TieBreakCriterion.entries.forEach { criterion ->
                        FilterChip(
                            selected = criterion in criteria,
                            onClick = {
                                criteria = if (criterion in criteria) criteria - criterion else criteria + criterion
                            },
                            label = { Text(criterion.label()) },
                        )
                    }
                }

                SettingsCard("Visualização") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Destacar líder em amarelo")
                        Switch(checked = highlightLeader, onCheckedChange = { highlightLeader = it })
                    }
                }

                Button(
                    onClick = {
                        onSaveRules(
                            win.toIntOrNull() ?: 3,
                            draw.toIntOrNull() ?: 1,
                            loss.toIntOrNull() ?: 0,
                            yellowFine.toLongOrNull() ?: 0L,
                            redFine.toLongOrNull() ?: 0L,
                            criteria,
                            highlightLeader,
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Salvar Configurações") }
            } else {
                Text("Selecione uma competição para editar as regras.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun RowScope.ScoreField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onChange(text.filter { it.isDigit() }.take(2)) },
        label = { Text(label) },
        modifier = Modifier.weight(1f),
        singleLine = true,
    )
}

private fun TieBreakCriterion.label() = when (this) {
    TieBreakCriterion.POINTS -> "Pontos"
    TieBreakCriterion.WINS -> "Vitórias"
    TieBreakCriterion.GOAL_DIFFERENCE -> "Saldo de gols"
    TieBreakCriterion.GOALS_FOR -> "Gols pró"
    TieBreakCriterion.HEAD_TO_HEAD -> "Confronto direto"
}
