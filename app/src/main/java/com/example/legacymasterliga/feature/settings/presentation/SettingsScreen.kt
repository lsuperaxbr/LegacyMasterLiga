package com.example.legacymasterliga.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.DensityPreference
import com.example.legacymasterliga.core.model.ThemePreference
import com.example.legacymasterliga.core.model.TieBreakCriterion
import com.example.legacymasterliga.core.model.UserRole

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onNavigateToDiagnostic: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectCompetition = viewModel::selectCompetition,
        onSaveLeagueName = viewModel::saveLeagueName,
        onSavePreferences = viewModel::savePreferences,
        onSaveRules = viewModel::saveRules,
        onInjectBankBalance = viewModel::injectBankBalance,
        onOpenSyncDiagnostic = onNavigateToDiagnostic
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectCompetition: (Long) -> Unit,
    onSaveLeagueName: (String) -> Unit,
    onSavePreferences: (ThemePreference, DensityPreference, Boolean) -> Unit,
    onSaveRules: (Int, Int, Int, Long, Long, List<TieBreakCriterion>, Boolean) -> Unit,
    onInjectBankBalance: (Long) -> Unit,
    onOpenSyncDiagnostic: () -> Unit = {}
) {
    var leagueName by remember { mutableStateOf("") }
    var bankInjection by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf(state.preferences.themePreference) }
    var density by remember { mutableStateOf(state.preferences.densityPreference) }
    var animations by remember { mutableStateOf(state.preferences.animationsEnabled) }
    var win by remember { mutableStateOf("3") }
    var draw by remember { mutableStateOf("1") }
    var loss by remember { mutableStateOf("0") }
    var yellowFine by remember { mutableStateOf("0") }
    var redFine by remember { mutableStateOf("0") }
    var criteria by remember { mutableStateOf(emptyList<TieBreakCriterion>()) }
    var highlightLeader by remember { mutableStateOf(true) }
    var showOnlineDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }

    val selectedLeague = state.leagues.firstOrNull { it.id == state.selectedLeagueId }
    LaunchedEffect(selectedLeague?.id, selectedLeague?.name) { leagueName = selectedLeague?.name.orEmpty() }
    LaunchedEffect(state.preferences) {
        theme = state.preferences.themePreference
        density = state.preferences.densityPreference
        animations = state.preferences.animationsEnabled
    }
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
                title = { Text("Configurações avançadas") },
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
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            val isAdmin = state.user?.role == UserRole.ADMINISTRATOR

            SettingsCard("Identidade Online") {
                Text("Conecte sua conta ao Firebase para recursos de nuvem.", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { showOnlineDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gerenciar Conta Online")
                }
                
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                
                Text("Compartilhe sua liga com outros presidentes.", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { showCloudDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedLeagueId != null
                ) {
                    Text("Configurar Liga Online")
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                Text("Apoio técnico (Temporário)", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = onOpenSyncDiagnostic,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Diagnóstico de Sync")
                }
            }

            if (isAdmin) {
                SettingsCard("Liga") {
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
                    OutlinedTextField(
                        value = leagueName,
                        onValueChange = { leagueName = it },
                        label = { Text("Nome da liga") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Button(onClick = { onSaveLeagueName(leagueName) }, enabled = state.selectedLeagueId != null) {
                        Text("Salvar nome")
                    }
                    Text("Moeda oficial: CR", style = MaterialTheme.typography.bodySmall)
                }

                SettingsCard("Banco da Liga") {
                    Text("Injeção de capital (Saldo Inicial)", fontWeight = FontWeight.SemiBold)
                    Text("Defina o saldo disponível para premiações e operações do Banco.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = bankInjection,
                        onValueChange = { bankInjection = it.filter { c -> c.isDigit() } },
                        label = { Text("Valor em CR") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Button(
                        onClick = { 
                            onInjectBankBalance(bankInjection.toLongOrNull() ?: 0L)
                            bankInjection = ""
                        }, 
                        enabled = (bankInjection.toLongOrNull() ?: 0L) > 0L
                    ) {
                        Text("Injetar Saldo")
                    }
                }
            }

            SettingsCard("Preferências visuais") {
                Text("Tema", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePreference.entries.forEach { option ->
                        FilterChip(selected = theme == option, onClick = { theme = option }, label = { Text(option.label()) })
                    }
                }
                Text("Densidade", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DensityPreference.entries.forEach { option ->
                        FilterChip(selected = density == option, onClick = { density = option }, label = { Text(option.label()) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Animações")
                    Switch(checked = animations, onCheckedChange = { animations = it })
                }
                Button(onClick = { onSavePreferences(theme, density, animations) }) { Text("Salvar aparência") }
            }

            if (isAdmin) {
                SettingsCard("Regras por competição") {
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
                    if (state.selectedCompetitionId == null) {
                        Text("Crie ou selecione uma competição.")
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ScoreField("Vitória", win) { win = it }
                            ScoreField("Empate", draw) { draw = it }
                            ScoreField("Derrota", loss) { loss = it }
                        }
                        Text("Multas Disciplinares", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ScoreField("Amarelo (CR)", yellowFine) { yellowFine = it }
                            ScoreField("Vermelho (CR)", redFine) { redFine = it }
                        }
                        Text("Critérios de desempate", fontWeight = FontWeight.SemiBold)
                        TieBreakCriterion.entries.forEach { criterion ->
                            FilterChip(
                                selected = criterion in criteria,
                                onClick = {
                                    criteria = if (criterion in criteria) criteria - criterion else criteria + criterion
                                },
                                label = { Text(criterion.label()) },
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Destacar líder em amarelo")
                            Switch(checked = highlightLeader, onCheckedChange = { highlightLeader = it })
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
                        ) { Text("Salvar regras") }
                    }
                }
            }
        }
    }

    if (showOnlineDialog) {
        com.example.legacymasterliga.feature.online.presentation.OnlineIdentityDialog(
            onDismiss = { showOnlineDialog = false }
        )
    }

    if (showCloudDialog) {
        com.example.legacymasterliga.feature.online.presentation.CloudLeagueDialog(
            localLeagueId = state.selectedLeagueId,
            onDismiss = { showCloudDialog = false }
        )
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

private fun ThemePreference.label() = when (this) {
    ThemePreference.SYSTEM -> "Sistema"
    ThemePreference.DARK -> "Escuro"
    ThemePreference.LIGHT -> "Claro"
}

private fun DensityPreference.label() = when (this) {
    DensityPreference.COMFORTABLE -> "Confortável"
    DensityPreference.COMPACT -> "Compacto"
}

private fun TieBreakCriterion.label() = when (this) {
    TieBreakCriterion.POINTS -> "Pontos"
    TieBreakCriterion.WINS -> "Vitórias"
    TieBreakCriterion.GOAL_DIFFERENCE -> "Saldo de gols"
    TieBreakCriterion.GOALS_FOR -> "Gols pró"
    TieBreakCriterion.HEAD_TO_HEAD -> "Confronto direto"
}
