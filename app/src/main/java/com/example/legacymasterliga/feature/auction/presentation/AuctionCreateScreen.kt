package com.example.legacymasterliga.feature.auction.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.*

@Composable
fun AuctionCreateRoute(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: AuctionCreateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    AuctionCreateScreen(
        state = state,
        onBack = onBack,
        onConfirm = { name, start, end, players ->
            viewModel.createLot(name, start, end, players, onCreated)
        },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionCreateScreen(
    state: AuctionCreateUiState,
    onBack: () -> Unit,
    onConfirm: (String, Long, Long, List<Long>) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var name by rememberSaveable { mutableStateOf("") }
    var startAt by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var endAt by rememberSaveable { mutableLongStateOf(System.currentTimeMillis() + 3600000 * 24) }
    val selectedPlayers = remember { mutableStateListOf<Long>() }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Novo Leilão") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = { onConfirm(name, startAt, endAt, selectedPlayers.toList()) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = !state.isSaving && name.isNotBlank() && selectedPlayers.isNotEmpty()
                ) {
                    Text(if (state.isSaving) "Criando..." else "Criar Lote de Leilão")
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Lote") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateTimeSelector(
                        label = "Início",
                        timestamp = startAt,
                        onTimestampSelected = { startAt = it },
                        modifier = Modifier.weight(1f)
                    )
                    DateTimeSelector(
                        label = "Fim",
                        timestamp = endAt,
                        onTimestampSelected = { endAt = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Text("Selecionar Jogadores do Banco", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            if (state.bankPlayers.isEmpty()) {
                item {
                    Text("Nenhum jogador disponível no Banco da Liga.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                items(state.bankPlayers, key = { it.player.id }) { pwc ->
                    val player = pwc.player
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedPlayers.contains(player.id),
                            onCheckedChange = { 
                                if (it) selectedPlayers.add(player.id) else selectedPlayers.remove(player.id)
                            }
                        )
                        Column {
                            Text(player.name, fontWeight = FontWeight.Bold)
                            Text("${player.position ?: "---"} • OVR ${player.overall ?: "--"}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateTimeSelector(
    label: String,
    timestamp: Long,
    onTimestampSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember(timestamp) { Calendar.getInstance().apply { timeInMillis = timestamp } }
    
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedCard(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        calendar.set(y, m, d)
                        TimePickerDialog(
                            context,
                            { _, hh, mm ->
                                calendar.set(Calendar.HOUR_OF_DAY, hh)
                                calendar.set(Calendar.MINUTE, mm)
                                onTimestampSelected(calendar.timeInMillis)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(16.dp))
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
