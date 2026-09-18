package com.example.legacymasterliga.feature.schedule.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FilterListOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.ui.components.ClubCrest
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.feature.schedule.domain.ScheduleMatch
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRound
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import com.example.legacymasterliga.feature.cup.presentation.CupComingSoonScreen
import java.text.DateFormat
import java.util.Date

@Composable
fun ScheduleRoute(
    onBack: () -> Unit,
    onCupBlocked: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredRounds by viewModel.filteredRounds.collectAsStateWithLifecycle()
    val filterableClubs by viewModel.filterableClubs.collectAsStateWithLifecycle()
    val filterClubA by viewModel.filterClubA.collectAsStateWithLifecycle()
    val filterClubB by viewModel.filterClubB.collectAsStateWithLifecycle()

    if (state.isCupRouteBlocked) {
        LaunchedEffect(Unit) { onCupBlocked() }
        CupComingSoonScreen(onBack = onBack)
        return
    }
    ScheduleScreen(
        state = state,
        filteredRounds = filteredRounds,
        filterableClubs = filterableClubs,
        filterClubA = filterClubA,
        filterClubB = filterClubB,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectSeason = viewModel::selectSeason,
        onGenerate = viewModel::generate,
        onSaveResult = viewModel::saveResult,
        onSetFilterA = viewModel::setFilterClubA,
        onSetFilterB = viewModel::setFilterClubB,
        onClearFilter = viewModel::clearFilter,
        onFeedbackConsumed = viewModel::clearFeedback,
        onLoadRosters = viewModel::loadRosters,
        initialTab = viewModel.initialTabIndex,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    filteredRounds: List<ScheduleRound>,
    filterableClubs: List<Pair<Long, String>>,
    filterClubA: Long?,
    filterClubB: Long?,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
    onGenerate: () -> Unit,
    onSetFilterA: (Long?) -> Unit,
    onSetFilterB: (Long?) -> Unit,
    onClearFilter: () -> Unit,
    onSaveResult: (Long, Int, Int, Int?, Int?, Long?, List<com.example.legacymasterliga.feature.results.domain.GoalRecord>, Int, Int, Int, Int) -> Unit,
    onFeedbackConsumed: () -> Unit,
    onLoadRosters: (Long, Long) -> Unit,
    initialTab: Int = 0,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var editingMatch by remember { mutableStateOf<ScheduleMatch?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(initialTab.coerceIn(0, 1)) }
    val selectedSeason = state.seasons.firstOrNull { it.seasonId == state.selectedSeasonId }

    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            snackbarHostState.showSnackbar(it)
            onFeedbackConsumed()
        }
    }

    editingMatch?.let { match ->
        LaunchedEffect(match.id) {
            onLoadRosters(match.homeClubId, match.awayClubId)
        }
        ResultDialog(
            match = match,
            homeRoster = state.homeRoster,
            awayRoster = state.awayRoster,
            competitionFormat = selectedSeason?.format ?: CompetitionFormat.SINGLE_ROUND,
            onDismiss = { editingMatch = null },
            onConfirm = { h, a, ph, pa, winner, goals, hy, hr, ay, ar ->
                onSaveResult(match.id, h, a, ph, pa, winner, goals, hy, hr, ay, ar)
                editingMatch = null
            },
        )
    }

    val isAdmin = state.role == UserRole.ADMINISTRATOR

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rodadas e partidas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val selectedSeason = state.seasons.firstOrNull { it.seasonId == state.selectedSeasonId }
            val isCup = selectedSeason?.format == CompetitionFormat.KNOCKOUT || selectedSeason?.format == CompetitionFormat.GROUPS_AND_KNOCKOUT
            
            if (isCup && state.rounds.isNotEmpty()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Partidas") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Chaveamento") })
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (selectedTab == 0) {
                    item {
                        SelectorSection(
                            leagues = state.leagues,
                            selectedLeagueId = state.selectedLeagueId,
                            seasons = state.seasons,
                            selectedSeasonId = state.selectedSeasonId,
                            onSelectLeague = onSelectLeague,
                            onSelectSeason = onSelectSeason,
                        )
                    }
                    item(key = "club-filter") {
                        ClubFilterBar(
                            clubs = filterableClubs,
                            selectedA = filterClubA,
                            selectedB = filterClubB,
                            onSelectA = onSetFilterA,
                            onSelectB = onSetFilterB,
                            onClear = onClearFilter,
                        )
                    }
                    if (isAdmin || state.rounds.isNotEmpty()) {
                        item {
                            Card {
                                Column(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                                        Text("Geração automática", fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        selectedSeason?.format?.description()
                                            ?: "Selecione uma competição e uma temporada.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (isAdmin) {
                                        Button(
                                            onClick = onGenerate,
                                            enabled = selectedSeason != null && state.rounds.filter { it.stage == "REGULAR" || it.stage == "GROUPS" || it.stage == "KNOCKOUT" }.isEmpty() && !state.isGenerating,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(if (state.isGenerating) "Gerando..." else "Gerar fase inicial")
                                        }
                                    }
                                    if (state.rounds.isNotEmpty()) {
                                        Text("Toque em uma partida para lançar ou corrigir o placar.")
                                    }
                                }
                            }
                        }
                    }
                    if (state.rounds.isEmpty()) {
                        item {
                            Text(
                                "Nenhuma partida gerada para esta temporada.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(filteredRounds, key = { "round_${it.id}" }) { round ->
                            RoundCard(round = round, onEditMatch = { editingMatch = it })
                        }
                    }
                } else {
                    item { BracketsView(state.rounds) }
                }
            }
        }
    }
}

@Composable
private fun ClubFilterBar(
    clubs: List<Pair<Long, String>>,
    selectedA: Long?,
    selectedB: Long?,
    onSelectA: (Long?) -> Unit,
    onSelectB: (Long?) -> Unit,
    onClear: () -> Unit,
) {
    if (clubs.isEmpty()) return
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }
    val nomeA = clubs.find { it.first == selectedA }?.second ?: "Clube A"
    val nomeB = clubs.find { it.first == selectedB }?.second ?: "Clube B"
    val filtroAtivo = selectedA != null || selectedB != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Dropdown Clube A
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedA = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(nomeA, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(expanded = expandedA, onDismissRequest = { expandedA = false }) {
                    DropdownMenuItem(text = { Text("Todos") }, onClick = { onSelectA(null); expandedA = false })
                    clubs.forEach { (id, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { onSelectA(id); expandedA = false })
                    }
                }
            }
            // Dropdown Clube B
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedB = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(nomeB, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(expanded = expandedB, onDismissRequest = { expandedB = false }) {
                    DropdownMenuItem(text = { Text("Todos") }, onClick = { onSelectB(null); expandedB = false })
                    clubs.forEach { (id, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { onSelectB(id); expandedB = false })
                    }
                }
            }
        }
        // Botão limpar filtro — só aparece quando há filtro ativo
        if (filtroAtivo) {
            TextButton(
                onClick = onClear,
                modifier = Modifier.align(Alignment.End),
            ) {
                Icon(Icons.Outlined.FilterListOff, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Limpar filtro")
            }
        }
    }
}

@Composable
private fun SelectorSection(
    leagues: List<League>,
    selectedLeagueId: Long?,
    seasons: List<ScheduleSeasonOption>,
    selectedSeasonId: Long?,
    onSelectLeague: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Liga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        leagues.forEach { league ->
            SelectButton(
                selected = league.id == selectedLeagueId,
                text = league.name,
                onClick = { onSelectLeague(league.id) },
            )
        }
        Text("Competição e temporada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        seasons.forEach { season ->
            SelectButton(
                selected = season.seasonId == selectedSeasonId,
                text = "${season.competitionName} • ${season.seasonName} • ${season.format.shortLabel()}",
                onClick = { onSelectSeason(season.seasonId) },
            )
        }
    }
}

@Composable
private fun SelectButton(selected: Boolean, text: String, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
    }
}

@Composable
private fun RoundCard(round: ScheduleRound, onEditMatch: (ScheduleMatch) -> Unit) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val label = round.stageLabel ?: round.name
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            round.matches.forEach { match ->
                MatchLine(match, onClick = { onEditMatch(match) })
            }
        }
    }
}

@Composable
private fun MatchLine(match: ScheduleMatch, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mandante
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                val isWinner = match.winnerClubId == match.homeClubId
                Text(
                    match.homeClubName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = if (isWinner) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold) else MaterialTheme.typography.bodyMedium,
                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                ClubCrest(match.homeClubName, match.homeShieldUri, Modifier.size(28.dp))
            }

            // Placar Central
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.width(64.dp)
            ) {
                Text(
                    text = if (match.homeScore != null && match.awayScore != null) {
                        "${match.homeScore} × ${match.awayScore}"
                    } else {
                        "×"
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Visitante
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val isWinner = match.winnerClubId == match.awayClubId
                ClubCrest(match.awayClubName, match.awayShieldUri, Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    match.awayClubName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = if (isWinner) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold) else MaterialTheme.typography.bodyMedium,
                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    
    if (match.penaltiesHome != null && match.penaltiesAway != null) {
        Text(
            "Pênaltis: ${match.penaltiesHome} x ${match.penaltiesAway}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ResultDialog(
    match: ScheduleMatch,
    homeRoster: List<com.example.legacymasterliga.domain.model.Player>,
    awayRoster: List<com.example.legacymasterliga.domain.model.Player>,
    competitionFormat: CompetitionFormat,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int?, Int?, Long?, List<com.example.legacymasterliga.feature.results.domain.GoalRecord>, Int, Int, Int, Int) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) } // 0 = Placar, 1 = Cartões, 2 = Goleadores
    var homeText by remember(match.id) { mutableStateOf(match.homeScore?.toString().orEmpty()) }
    var awayText by remember(match.id) { mutableStateOf(match.awayScore?.toString().orEmpty()) }
    
    var homeYellow by remember(match.id) { mutableStateOf("") }
    var homeRed by remember(match.id) { mutableStateOf("") }
    var awayYellow by remember(match.id) { mutableStateOf("") }
    var awayRed by remember(match.id) { mutableStateOf("") }
    
    var selectedWinnerId by remember(match.id) { mutableStateOf(match.winnerClubId) }
    
    val home = homeText.toIntOrNull() ?: 0
    val away = awayText.toIntOrNull() ?: 0

    // Listas de goleadores (ID do jogador, -1 = Não selecionado, -2 = Gol Contra)
    var homeGoals by remember(match.id) { mutableStateOf<List<Long>>(emptyList()) }
    var awayGoals by remember(match.id) { mutableStateOf<List<Long>>(emptyList()) }

    LaunchedEffect(home) {
        homeGoals = List(home) { i -> homeGoals.getOrNull(i) ?: -1L }
    }
    LaunchedEffect(away) {
        awayGoals = List(away) { i -> awayGoals.getOrNull(i) ?: -1L }
    }
    
    val isCup = competitionFormat == CompetitionFormat.KNOCKOUT || competitionFormat == CompetitionFormat.GROUPS_AND_KNOCKOUT || competitionFormat == CompetitionFormat.SINGLE_MATCH
    val isDecisiveLeg = match.leg == 2 || (match.leg != 1 && match.leg != 2)
    val isTie = if (match.leg == 2) true else (homeText.isNotEmpty() && awayText.isNotEmpty() && home == away)
    val canShowTieBreak = isCup && match.stage == "KNOCKOUT" && isDecisiveLeg && isTie
    val validScore = homeText.isNotEmpty() && awayText.isNotEmpty() && (!canShowTieBreak || selectedWinnerId != null || (match.leg == 2))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(when(step) {
                0 -> "Lançar resultado"
                1 -> "Cartões Disciplinares"
                else -> "Marcar goleadores"
            })
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (step == 0) {
                    Text("${match.homeClubName} × ${match.awayClubName}", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = homeText,
                            onValueChange = { if (it.length <= 2) homeText = it.filter { c -> c.isDigit() } },
                            label = { Text(match.homeClubName) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = awayText,
                            onValueChange = { if (it.length <= 2) awayText = it.filter { c -> c.isDigit() } },
                            label = { Text(match.awayClubName) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    if (canShowTieBreak) {
                        HorizontalDivider()
                        Text("Desempate manual:", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selectedWinnerId == match.homeClubId, { selectedWinnerId = match.homeClubId }, { Text(match.homeClubName) })
                            FilterChip(selectedWinnerId == match.awayClubId, { selectedWinnerId = match.awayClubId }, { Text(match.awayClubName) })
                        }
                    }
                } else if (step == 1) {
                    Text("Contagem de cartões por clube", style = MaterialTheme.typography.labelSmall)
                    
                    Text(match.homeClubName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = homeYellow,
                            onValueChange = { homeYellow = it.filter { c -> c.isDigit() } },
                            label = { Text("Amarelos") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = homeRed,
                            onValueChange = { homeRed = it.filter { c -> c.isDigit() } },
                            label = { Text("Vermelhos") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(match.awayClubName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = awayYellow,
                            onValueChange = { awayYellow = it.filter { c -> c.isDigit() } },
                            label = { Text("Amarelos") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = awayRed,
                            onValueChange = { awayRed = it.filter { c -> c.isDigit() } },
                            label = { Text("Vermelhos") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                } else {
                    // Passo 2: Goleadores
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (home > 0) {
                            item { Text("Gols de ${match.homeClubName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                            items(home) { index ->
                                ScorerSelector(
                                    label = "Gol ${index + 1}",
                                    selectedId = homeGoals.getOrNull(index) ?: -1L,
                                    players = homeRoster,
                                    opponentPlayers = awayRoster,
                                    onSelect = { id -> 
                                        homeGoals = homeGoals.toMutableList().apply { set(index, id) }
                                    }
                                )
                            }
                        }
                        if (away > 0) {
                            item { Spacer(Modifier.height(8.dp)); Text("Gols de ${match.awayClubName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                            items(away) { index ->
                                ScorerSelector(
                                    label = "Gol ${index + 1}",
                                    selectedId = awayGoals.getOrNull(index) ?: -1L,
                                    players = awayRoster,
                                    opponentPlayers = homeRoster,
                                    onSelect = { id -> 
                                        awayGoals = awayGoals.toMutableList().apply { set(index, id) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step == 0) {
                Button(onClick = { step = 1 }, enabled = validScore) { Text("Próximo") }
            } else if (step == 1) {
                Button(onClick = { step = 2 }) { Text("Próximo") }
            } else {
                val goals = mutableListOf<com.example.legacymasterliga.feature.results.domain.GoalRecord>()
                homeGoals.forEach { id ->
                    if (id > 0) {
                        val isOwn = awayRoster.any { it.id == id }
                        goals.add(com.example.legacymasterliga.feature.results.domain.GoalRecord(id, match.homeClubId, isOwn))
                    }
                }
                awayGoals.forEach { id ->
                    if (id > 0) {
                        val isOwn = homeRoster.any { it.id == id }
                        goals.add(com.example.legacymasterliga.feature.results.domain.GoalRecord(id, match.awayClubId, isOwn))
                    }
                }
                
                Button(onClick = { 
                    onConfirm(
                        home, away, null, null, selectedWinnerId, goals,
                        homeYellow.toIntOrNull() ?: 0,
                        homeRed.toIntOrNull() ?: 0,
                        awayYellow.toIntOrNull() ?: 0,
                        awayRed.toIntOrNull() ?: 0
                    ) 
                }) { Text("Salvar") }
            }
        },
        dismissButton = {
            if (step > 0) {
                TextButton(onClick = { step-- }) { Text("Voltar") }
            } else {
                OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}

@Composable
private fun ScorerSelector(
    label: String,
    selectedId: Long,
    players: List<com.example.legacymasterliga.domain.model.Player>,
    opponentPlayers: List<com.example.legacymasterliga.domain.model.Player>,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isOwnGoal = opponentPlayers.any { it.id == selectedId }
    val selectedName = when {
        selectedId == -1L -> "Selecionar jogador"
        isOwnGoal -> "Contra: ${opponentPlayers.find { it.id == selectedId }?.name}"
        else -> players.find { it.id == selectedId }?.name ?: "Desconhecido"
    }

    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selectedName", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            DropdownMenuItem(text = { Text("Não marcar") }, onClick = { onSelect(-1L); expanded = false })
            
            if (players.isNotEmpty()) {
                itemHeader("Goleadores")
                players.forEach { p ->
                    DropdownMenuItem(text = { Text(p.name) }, onClick = { onSelect(p.id); expanded = false })
                }
            }
            
            if (opponentPlayers.isNotEmpty()) {
                HorizontalDivider()
                itemHeader("Gol Contra")
                opponentPlayers.forEach { p ->
                    DropdownMenuItem(
                        text = { Text("${p.name} (Contra)") }, 
                        onClick = { onSelect(p.id); expanded = false },
                        colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}

@Composable
private fun itemHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun BracketsView(rounds: List<ScheduleRound>) {
    val knockoutRounds = rounds.filter { it.stage == "KNOCKOUT" }.groupBy { it.stageLabel ?: "Mata-mata" }
    
    if (knockoutRounds.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chaveamento disponível após a fase inicial.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        knockoutRounds.forEach { (label, roundList) ->
            Column(
                modifier = Modifier.width(300.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Agrupar por pairingKey para consolidar ida e volta
                val allMatchesInStage = roundList.flatMap { it.matches }
                val pairings = allMatchesInStage.groupBy { 
                    val ids = listOf(it.homeClubId, it.awayClubId).sorted()
                    "P-${ids[0]}-${ids[1]}"
                }

                pairings.values.forEach { matchGroup ->
                    ConsolidatedBracketCard(matchGroup)
                }
            }

            // BUG-RC1-005: Seção CAMPEÃO
            val finalRound = rounds.find { it.stageLabel == "Final" }
            val finalMatches = finalRound?.matches ?: emptyList()
            if (finalMatches.isNotEmpty() && finalMatches.all { it.status == com.example.legacymasterliga.core.model.MatchStatus.FINISHED }) {
                val winnerId = if (finalMatches.size == 1) finalMatches.first().winnerClubId 
                               else calculateAggWinnerId(finalMatches)
                
                if (winnerId != null) {
                    val winnerName = if (winnerId == finalMatches.first().homeClubId) finalMatches.first().homeClubName else finalMatches.first().awayClubName
                    val winnerShield = if (winnerId == finalMatches.first().homeClubId) finalMatches.first().homeShieldUri else finalMatches.first().awayShieldUri
                    
                    Column(
                        modifier = Modifier.width(300.dp).padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🏆 CAMPEÃO", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        ClubCrest(winnerName, winnerShield, Modifier.size(120.dp))
                        Text(winnerName.uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        
                        Text("A temporada pode ser encerrada oficialmente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun calculateAggWinnerId(matches: List<ScheduleMatch>): Long? {
    val leg1 = matches.find { it.leg == 1 } ?: return null
    val leg2 = matches.find { it.leg == 2 } ?: return null
    if (leg2.winnerClubId != null) return leg2.winnerClubId
    
    val totalA = (leg1.homeScore ?: 0) + (leg2.awayScore ?: 0)
    val totalB = (leg1.awayScore ?: 0) + (leg2.homeScore ?: 0)
    return if (totalA > totalB) leg1.homeClubId else if (totalB > totalA) leg1.awayClubId else null
}

@Composable
private fun ConsolidatedBracketCard(matches: List<ScheduleMatch>) {
    val leg1 = matches.find { it.leg == 1 }
    val leg2 = matches.find { it.leg == 2 }
    val single = if (leg1 == null && leg2 == null) matches.firstOrNull() else null
    
    val clubAId = leg1?.homeClubId ?: single?.homeClubId ?: 0L
    val clubAName = leg1?.homeClubName ?: single?.homeClubName ?: "TBD"
    val clubAShield = leg1?.homeShieldUri ?: single?.homeShieldUri
    
    val clubBId = leg1?.awayClubId ?: single?.awayClubId ?: 0L
    val clubBName = leg1?.awayClubName ?: single?.awayClubName ?: "TBD"
    val clubBShield = leg1?.awayShieldUri ?: single?.awayShieldUri

    val scoreA1 = leg1?.homeScore
    val scoreB1 = leg1?.awayScore
    val scoreA2 = leg2?.awayScore // Mandos invertidos na volta
    val scoreB2 = leg2?.homeScore
    val scoreASingle = single?.homeScore
    val scoreBSingle = single?.awayScore

    val totalA = (scoreA1 ?: 0) + (scoreA2 ?: 0) + (scoreASingle ?: 0)
    val totalB = (scoreB1 ?: 0) + (scoreB2 ?: 0) + (scoreBSingle ?: 0)
    
    val winnerId = leg2?.winnerClubId ?: single?.winnerClubId ?: (if (totalA > totalB) clubAId else if (totalB > totalA) clubBId else null)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Time A
            BracketRow(clubAName, clubAShield, scoreA1, scoreA2, scoreASingle, totalA, winnerId == clubAId)
            
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            
            // Time B
            BracketRow(clubBName, clubBShield, scoreB1, scoreB2, scoreBSingle, totalB, winnerId == clubBId)

            if (winnerId != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "🏆 Classificado: ${if (winnerId == clubAId) clubAName.uppercase() else clubBName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BracketRow(name: String, shield: String?, s1: Int?, s2: Int?, sSingle: Int?, total: Int, isWinner: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ClubCrest(name, shield, Modifier.size(26.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = if (isWinner) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold) else MaterialTheme.typography.bodyMedium,
            color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        
        // Scores
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (sSingle != null) {
                ScoreBadge(sSingle.toString())
            } else {
                ScoreBadge(s1?.toString() ?: "-")
                ScoreBadge(s2?.toString() ?: "-")
                Text("=", style = MaterialTheme.typography.bodySmall)
                ScoreBadge(total.toString(), highlight = true)
            }
        }
    }
}

@Composable
private fun ScoreBadge(text: String, highlight: Boolean = false) {
    Surface(
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun CompetitionFormat.shortLabel(): String = when (this) {
    CompetitionFormat.SINGLE_ROUND -> "Somente ida"
    CompetitionFormat.HOME_AND_AWAY -> "Ida e volta"
    CompetitionFormat.SINGLE_MATCH -> "Jogo único"
    CompetitionFormat.KNOCKOUT -> "Mata-mata"
    CompetitionFormat.GROUPS_AND_KNOCKOUT -> "Grupos + Mata-mata"
}

private fun CompetitionFormat.description(): String = when (this) {
    CompetitionFormat.SINGLE_ROUND -> "Todos os clubes se enfrentam uma vez, com rodadas equilibradas."
    CompetitionFormat.HOME_AND_AWAY -> "Todos se enfrentam duas vezes; o segundo turno inverte os mandos."
    CompetitionFormat.SINGLE_MATCH -> "Cria a primeira fase eliminatória em jogo único, respeitando a ordem das inscrições."
    CompetitionFormat.KNOCKOUT -> "Gera confrontos eliminatórios de ida ou ida e volta entre os inscritos."
    CompetitionFormat.GROUPS_AND_KNOCKOUT -> "Divide os clubes em grupos e gera os confrontos da fase inicial."
}
