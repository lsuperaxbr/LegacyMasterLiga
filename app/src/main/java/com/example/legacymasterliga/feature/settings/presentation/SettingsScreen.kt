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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.DensityPreference
import com.example.legacymasterliga.core.model.ThemePreference
import com.example.legacymasterliga.core.model.TieBreakCriterion
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.usecase.ExportPes6Summary

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onNavigateToDiagnostic: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exportSummary by viewModel.exportSummary.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectCompetition = viewModel::selectCompetition,
        onSaveLeagueName = viewModel::saveLeagueName,
        onSavePreferences = viewModel::savePreferences,
        onSaveRules = viewModel::saveRules,
        onInjectBankBalance = viewModel::injectBankBalance,
        onResetLeague = viewModel::resetLeague,
        onDeleteLeague = viewModel::deleteLeague,
        onPurgeGhostLeague = viewModel::purgeGhostLeague,
        exportSummary = exportSummary,
        isExporting = isExporting,
        onExportPlvr = viewModel::exportPlvr,
        onDismissExportSummary = viewModel::dismissExportSummary,
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
    onResetLeague: () -> Unit = {},
    onDeleteLeague: (Long) -> Unit = {},
    onPurgeGhostLeague: () -> Unit = {},
    exportSummary: ExportPes6Summary? = null,
    isExporting: Boolean = false,
    onExportPlvr: (Boolean) -> Unit = {},
    onDismissExportSummary: () -> Unit = {},
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
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteLeagueDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var cleanExportInput by remember { mutableStateOf(false) }
    var resetConfirmInput by remember { mutableStateOf("") }

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

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Elencos (Pastas e TXT)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Esta ação criará uma estrutura de pastas por clube em 'Download/LegacyMasterLiga/' " +
                        "com arquivos .txt individuais para cada jogador no padrão PLVR do PES 6 Editor.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = cleanExportInput,
                            onCheckedChange = { cleanExportInput = it }
                        )
                        Text(
                            "Recriar pastas do zero (Limpeza Total)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        onExportPlvr(cleanExportInput)
                    }
                ) {
                    Text("Iniciar Exportação")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (isExporting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Exportando Elencos...") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Gerando pastas e arquivos .txt por jogador...")
                }
            },
            confirmButton = {}
        )
    }

    if (exportSummary != null) {
        AlertDialog(
            onDismissRequest = onDismissExportSummary,
            title = { Text("Exportação Concluída") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Clubes exportados: ${exportSummary.totalClubsExported}", style = MaterialTheme.typography.bodyMedium)
                    Text("• Atletas exportados: ${exportSummary.totalPlayersExported}", style = MaterialTheme.typography.bodyMedium)
                    if (exportSummary.isCleanedFirst) {
                        Text("• Pastas anteriores recriadas do zero.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Text("Caminho: ${exportSummary.exportPath}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = onDismissExportSummary) {
                    Text("Concluir")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
                resetConfirmInput = ""
            },
            title = { Text("Zona de Perigo — Reset da Liga") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Atenção: Esta ação apagará todos os jogadores da liga, zerará os elencos, removerá todas as partidas/temporadas/tabelas e restaurará o saldo dos clubes para 500 CR.\n\n" +
                        "Um backup de segurança do estado atual do app será gerado automaticamente antes do reset.\n\n" +
                        "Digite 'RESET' abaixo para confirmar:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = resetConfirmInput,
                        onValueChange = { resetConfirmInput = it },
                        label = { Text("Digite RESET para confirmar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        resetConfirmInput = ""
                        onResetLeague()
                    },
                    enabled = resetConfirmInput.trim() == "RESET",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Executar Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        resetConfirmInput = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteLeagueDialog) {
        DeleteLeagueDialog(
            leagues = state.leagues,
            activeLeagueId = state.selectedLeagueId,
            onDismiss = { showDeleteLeagueDialog = false },
            onConfirmDelete = { targetLeagueId ->
                showDeleteLeagueDialog = false
                onDeleteLeague(targetLeagueId)
            }
        )
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
                    colors = ButtonDefaults.buttonColors(
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

                SettingsCard("Exportação de Elencos PLVR (PES 6)") {
                    Text("Geração de Pastas e TXT por Jogador", fontWeight = FontWeight.SemiBold)
                    Text("Gera automaticamente a estrutura de pastas por clube em 'Download/LegacyMasterLiga/' com arquivos .txt individuais por atleta compatíveis com o PES 6 Editor.", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.selectedLeagueId != null
                    ) {
                        Text("Exportar Elencos (Pastas e TXT)")
                    }
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

                SettingsCard("Zona de Perigo") {
                    Text("Reset de Temporada/Liga", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    Text("Apaga jogadores e histórico de partidas/temporadas, restaurando o saldo inicial dos clubes. Preserva clubes, escudos, presidentes e usuários.", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Resetar Liga")
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    Text("Exclusão Total de Liga", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    Text("Remove completamente uma liga e TODOS os seus clubes, partidas e dados locais e na nuvem. Ação irreversível.", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { showDeleteLeagueDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Excluir Liga")
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    Text("Limpeza de Resíduo Fantasma (Remoção Única)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    Text("Remove resíduos da liga 'Liga M L Amigos' dos perfis no Firestore e deleta o documento e subcoleções remotas se existirem.", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { onPurgeGhostLeague() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Purga de Resíduo Fantasma na Nuvem")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteLeagueDialog(
    leagues: List<League>,
    activeLeagueId: Long?,
    onDismiss: () -> Unit,
    onConfirmDelete: (Long) -> Unit,
) {
    var selectedTargetLeagueId by remember(leagues, activeLeagueId) {
        mutableStateOf(leagues.firstOrNull { it.id != activeLeagueId }?.id ?: leagues.firstOrNull()?.id)
    }
    var confirmInput by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val targetLeague = leagues.firstOrNull { it.id == selectedTargetLeagueId }
    val isActiveLeague = targetLeague?.id == activeLeagueId
    val isOnlyRemainingLeague = leagues.size == 1
    val isBlocked = isActiveLeague && !isOnlyRemainingLeague
    val expectedName = targetLeague?.name.orEmpty()
    val isNameMatched = targetLeague != null && confirmInput.trim() == expectedName.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Liga — Ação Irreversível") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Exclusão irreversível. A liga e TODOS os dados dela (clubes, jogadores, partidas, finanças e histórico) serão apagados localmente e na nuvem.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                if (isOnlyRemainingLeague) {
                    Text(
                        "Atenção: Esta é a ÚNICA liga cadastrada no aplicativo. Ao excluí-la, o aplicativo ficará sem ligas e retornará à tela de criação para que você possa começar do zero.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text("Selecione a liga a ser excluída:", fontWeight = FontWeight.SemiBold)

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = targetLeague?.name ?: "Selecione uma liga",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Liga Alvo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        leagues.forEach { league ->
                            DropdownMenuItem(
                                text = {
                                    val isCurrentActive = league.id == activeLeagueId
                                    Text("${league.name}${if (isCurrentActive) " (Ativa no momento)" else ""}")
                                },
                                onClick = {
                                    selectedTargetLeagueId = league.id
                                    confirmInput = ""
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (isBlocked) {
                    Text(
                        "Atenção: Não é possível excluir a liga atualmente ativa enquanto existirem outras ligas. Selecione outra liga no topo da tela primeiro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (targetLeague != null) {
                    Text(
                        "Para confirmar a exclusão, digite o NOME EXATO da liga abaixo:\n'$expectedName'",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = confirmInput,
                        onValueChange = { confirmInput = it },
                        label = { Text("Nome exato da liga") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (targetLeague != null) {
                        onConfirmDelete(targetLeague.id)
                    }
                },
                enabled = isNameMatched && !isBlocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Excluir Liga Definitivamente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
