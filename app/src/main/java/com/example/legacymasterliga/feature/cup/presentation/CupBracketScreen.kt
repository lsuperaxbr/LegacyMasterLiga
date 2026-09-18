package com.example.legacymasterliga.feature.cup.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.ui.components.ClubCrest
import com.example.legacymasterliga.feature.schedule.domain.ScheduleMatch
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRound
import com.example.legacymasterliga.feature.schedule.domain.CupEngine

@Composable
fun CupBracketRoute(
    onBack: () -> Unit,
    viewModel: CupBracketViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CupBracketScreen(
        state = state,
        onBack = onBack,
        onSaveResult = viewModel::saveResult,
        onFeedbackConsumed = viewModel::clearFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupBracketScreen(
    state: CupBracketUiState,
    onBack: () -> Unit,
    onSaveResult: (Long, Int, Int, Long?) -> Unit,
    onFeedbackConsumed: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var editingMatch by remember { mutableStateOf<ScheduleMatch?>(null) }
    val horizontalScroll = rememberScrollState()

    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            snackbarHostState.showSnackbar(it)
            onFeedbackConsumed()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.competitionName.uppercase(), fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // --- Lógica de Processamento do Chaveamento ---
        val phaseOrder = listOf(
            CupEngine.PRELIMINARY,
            CupEngine.ROUND_OF_32,
            CupEngine.ROUND_OF_16,
            CupEngine.QUARTER_FINALS,
            CupEngine.SEMI_FINALS,
            CupEngine.FINAL,
        )

        // Agrupar rodadas por rótulo de fase
        val roundsByPhase = state.rounds.groupBy { it.stageLabel ?: "Mata-mata" }

        // Processar as fases na ordem correta
        val processedPhases = phaseOrder.mapNotNull { label ->
            val phaseRounds = roundsByPhase[label] ?: return@mapNotNull null
            val allMatches = phaseRounds.flatMap { it.matches }
            
            // Agrupar por par de clubes (mesmos clubes em ida e volta)
            val pairings = allMatches.groupBy { match ->
                val ids = listOf(match.homeClubId, match.awayClubId).sorted()
                "P-${ids[0]}-${ids[1]}"
            }.values.toList()

            label to pairings
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                processedPhases.forEachIndexed { index, (label, pairings) ->
                    BracketColumn(
                        phaseLabel = label,
                        pairings = pairings,
                        isLastPhase = index == processedPhases.lastIndex,
                        canManage = state.canManage,
                        onEditMatch = { editingMatch = it }
                    )
                }

                // Pódio do Campeão
                val finalPairing = processedPhases.lastOrNull { it.first == CupEngine.FINAL }?.second?.firstOrNull()
                if (finalPairing != null && finalPairing.all { it.status == com.example.legacymasterliga.core.model.MatchStatus.FINISHED }) {
                    val winnerId = calcularVencedor(finalPairing)
                    if (winnerId != null) {
                        ChampionPodium(
                            winnerId = winnerId,
                            match = finalPairing.first()
                        )
                    }
                }
            }
        }
    }

    editingMatch?.let { match ->
        CupResultDialog(
            match = match,
            onDismiss = { editingMatch = null },
            onConfirm = { h, a, winner ->
                onSaveResult(match.id, h, a, winner)
                editingMatch = null
            }
        )
    }
}

@Composable
private fun BracketColumn(
    phaseLabel: String,
    pairings: List<List<ScheduleMatch>>,
    isLastPhase: Boolean,
    canManage: Boolean,
    onEditMatch: (ScheduleMatch) -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título da Fase
        Text(
            text = phaseLabel.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Lista de confrontos centralizada
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            pairings.forEach { matchGroup ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BracketPairCard(
                        matches = matchGroup,
                        canManage = canManage,
                        onEditMatch = onEditMatch
                    )
                    
                    if (!isLastPhase) {
                        // Linha conectora simples
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun BracketPairCard(
    matches: List<ScheduleMatch>,
    canManage: Boolean,
    onEditMatch: (ScheduleMatch) -> Unit
) {
    val winnerId = calcularVencedor(matches)
    
    Card(
        modifier = Modifier.width(190.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            matches.sortedBy { it.leg }.forEach { match ->
                MatchRow(
                    match = match,
                    confrontationWinnerId = winnerId,
                    canManage = canManage,
                    onEditMatch = onEditMatch
                )
            }
            
            if (matches.size > 1 && matches.all { it.status == com.example.legacymasterliga.core.model.MatchStatus.FINISHED }) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                val winnerName = if (winnerId == matches.first().homeClubId) matches.first().homeClubName else matches.first().awayClubName
                Text(
                    text = "➜ $winnerName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun MatchRow(
    match: ScheduleMatch,
    confrontationWinnerId: Long?,
    canManage: Boolean,
    onEditMatch: (ScheduleMatch) -> Unit,
) {
    val isFinished = match.status == com.example.legacymasterliga.core.model.MatchStatus.FINISHED
    val isClickable = canManage && !isFinished

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (isClickable) it.clickable { onEditMatch(match) } else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = match.homeClubName,
            style = MaterialTheme.typography.labelSmall,
            color = if (match.homeClubId == confrontationWinnerId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (match.homeClubId == confrontationWinnerId) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (isFinished) "${match.homeScore ?: 0} x ${match.awayScore ?: 0}" else "? x ?",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        Text(
            text = match.awayClubName,
            style = MaterialTheme.typography.labelSmall,
            color = if (match.awayClubId == confrontationWinnerId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (match.awayClubId == confrontationWinnerId) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

@Composable
private fun ChampionPodium(winnerId: Long, match: ScheduleMatch) {
    val winnerName = if (winnerId == match.homeClubId) match.homeClubName else match.awayClubName
    val winnerShield = if (winnerId == match.homeClubId) match.homeShieldUri else match.awayShieldUri

    Column(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "CAMPEÃO",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD700)
        )
        Spacer(Modifier.height(12.dp))
        ClubCrest(winnerName, winnerShield, Modifier.size(100.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            winnerName.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CupResultDialog(
    match: ScheduleMatch,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Long?) -> Unit
) {
    var homeText by remember { mutableStateOf("") }
    var awayText by remember { mutableStateOf("") }
    var manualWinnerId by remember { mutableStateOf<Long?>(null) }
    
    val h = homeText.toIntOrNull()
    val a = awayText.toIntOrNull()
    
    val isReturnLeg = match.leg == 2
    val isFinished = match.status == com.example.legacymasterliga.core.model.MatchStatus.FINISHED
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lançar Resultado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isFinished) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))) {
                        Text(
                            text = "Atenção: Este jogo já foi concluído. Editar o placar não recalcula fases seguintes já geradas.",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text("${match.homeClubName} x ${match.awayClubName}", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = homeText,
                        onValueChange = { homeText = it.filter { c -> c.isDigit() } },
                        label = { Text(match.homeClubName.take(5)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = awayText,
                        onValueChange = { awayText = it.filter { c -> c.isDigit() } },
                        label = { Text(match.awayClubName.take(5)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                if (isReturnLeg) {
                    Text("Em caso de empate no agregado, selecione quem avança:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = manualWinnerId == match.homeClubId,
                            onClick = { manualWinnerId = match.homeClubId },
                            label = { Text(match.homeClubName) }
                        )
                        FilterChip(
                            selected = manualWinnerId == match.awayClubId,
                            onClick = { manualWinnerId = match.awayClubId },
                            label = { Text(match.awayClubName) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(h!!, a!!, manualWinnerId) },
                enabled = h != null && a != null
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun calcularVencedor(matches: List<ScheduleMatch>): Long? {
    if (matches.isEmpty()) return null
    if (matches.size == 1) return matches[0].winnerClubId

    val ida = matches.firstOrNull { it.leg == 1 } ?: return null
    val volta = matches.firstOrNull { it.leg == 2 } ?: return null
    
    if (ida.status != com.example.legacymasterliga.core.model.MatchStatus.FINISHED || 
        volta.status != com.example.legacymasterliga.core.model.MatchStatus.FINISHED) return null

    val golsHome = (ida.homeScore ?: 0) + (volta.awayScore ?: 0)
    val golsAway = (ida.awayScore ?: 0) + (volta.homeScore ?: 0)
    
    return when {
        golsHome > golsAway -> ida.homeClubId
        golsAway > golsHome -> ida.awayClubId
        else -> volta.winnerClubId ?: ida.winnerClubId
    }
}
