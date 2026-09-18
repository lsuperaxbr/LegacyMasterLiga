package com.example.legacymasterliga.feature.arena.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.database.dao.ArenaDuelRow
import com.example.legacymasterliga.core.model.UserRole
import java.text.DateFormat
import java.util.Date

@Composable
fun ArenaRoute(
    onBack: () -> Unit,
    viewModel: ArenaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArenaScreen(
        state = state,
        onBack = onBack,
        onCreateDuel = viewModel::createDuel,
        onResolveDuel = viewModel::resolveDuel,
        onFeedbackConsumed = viewModel::clearFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaScreen(
    state: ArenaUiState,
    onBack: () -> Unit,
    onCreateDuel: (Long, Long, Long, String?) -> Unit,
    onResolveDuel: (Long, String) -> Unit,
    onFeedbackConsumed: () -> Unit
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ArenaIcon(Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("ARENA", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.currentUserRole == UserRole.ADMINISTRATOR || state.currentUserRole == UserRole.PRESIDENT) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Outlined.Add, "Novo Duelo")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && state.duels.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    InfoCard()
                }
                if (state.duels.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum duelo registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                items(state.duels, key = { it.id }) { duel ->
                    DuelCard(
                        duel = duel,
                        canResolve = state.currentUserRole == UserRole.ADMINISTRATOR || 
                                     state.ownClubIds.contains(duel.clubAId) || 
                                     state.ownClubIds.contains(duel.clubBId),
                        onResolve = { type -> onResolveDuel(duel.id, type) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDuelDialog(
            clubs = state.availableClubs,
            onDismiss = { showCreateDialog = false },
            onConfirm = { a, b, stake, note ->
                onCreateDuel(a, b, stake, note)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(12.dp))
            Text(
                "Duelos de aposta direta (5-50 CR). O vencedor leva tudo! Em caso de empate, o valor é devolvido.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DuelCard(
    duel: ArenaDuelRow,
    canResolve: Boolean,
    onResolve: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(duel.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = if (duel.status == "PENDING") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (duel.status == "PENDING") "PENDENTE" else "RESOLVIDO",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ClubDuelInfo(
                    name = duel.clubAName,
                    isWinner = duel.resultType == "CLUB_A_WIN",
                    modifier = Modifier.weight(1f),
                    alignEnd = false
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text("VS", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("${duel.stakeCr} CR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                ClubDuelInfo(
                    name = duel.clubBName,
                    isWinner = duel.resultType == "CLUB_B_WIN",
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            if (duel.note != null) {
                Text(
                    text = "Obs: ${duel.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (duel.status == "PENDING" && canResolve) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onResolve("CLUB_A_WIN") }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                        Text("A Venceu", fontSize = 10.sp)
                    }
                    OutlinedButton(onClick = { onResolve("DRAW") }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                        Text("Empate", fontSize = 10.sp)
                    }
                    Button(onClick = { onResolve("CLUB_B_WIN") }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                        Text("B Venceu", fontSize = 10.sp)
                    }
                }
            } else if (duel.status == "RESOLVED" && duel.resultType == "DRAW") {
                Text(
                    "RESULTADO: EMPATE",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ClubDuelInfo(name: String, isWinner: Boolean, modifier: Modifier, alignEnd: Boolean) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = name,
            fontWeight = if (isWinner) FontWeight.Black else FontWeight.Bold,
            color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        if (isWinner) {
            Text("VENCEDOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDuelDialog(
    clubs: List<com.example.legacymasterliga.domain.model.Club>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, Long, String?) -> Unit
) {
    var clubAId by remember { mutableStateOf<Long?>(null) }
    var clubBId by remember { mutableStateOf<Long?>(null) }
    var stakeText by remember { mutableStateOf("10") }
    var note by remember { mutableStateOf("") }
    
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    val stake = stakeText.toLongOrNull() ?: 0L
    val isValid = clubAId != null && clubBId != null && clubAId != clubBId && stake in 5..50

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Duelo Arena") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Clube A
                ExposedDropdownMenuBox(expanded = expandedA, onExpandedChange = { expandedA = it }) {
                    OutlinedTextField(
                        value = clubs.find { it.id == clubAId }?.name ?: "Selecionar Clube A",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Clube A") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedA) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedA, onDismissRequest = { expandedA = false }) {
                        clubs.forEach { club ->
                            DropdownMenuItem(
                                text = { Text(club.name) },
                                onClick = { clubAId = club.id; expandedA = false }
                            )
                        }
                    }
                }

                // Clube B
                ExposedDropdownMenuBox(expanded = expandedB, onExpandedChange = { expandedB = it }) {
                    OutlinedTextField(
                        value = clubs.find { it.id == clubBId }?.name ?: "Selecionar Clube B",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Clube B") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedB, onDismissRequest = { expandedB = false }) {
                        clubs.forEach { club ->
                            DropdownMenuItem(
                                text = { Text(club.name) },
                                onClick = { clubBId = club.id; expandedB = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = stakeText,
                    onValueChange = { stakeText = it.filter { c -> c.isDigit() } },
                    label = { Text("Aposta (5-50 CR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = stake != 0L && (stake < 5 || stake > 50),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Observação (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(clubAId!!, clubBId!!, stake, note.takeIf { it.isNotBlank() }) }, enabled = isValid) {
                Text("Criar Duelo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
