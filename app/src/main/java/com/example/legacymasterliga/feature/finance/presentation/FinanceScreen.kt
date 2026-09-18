package com.example.legacymasterliga.feature.finance.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.feature.finance.domain.ClubBalance
import java.text.DateFormat
import java.util.Date

@Composable
fun FinanceRoute(onBack: () -> Unit, viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FinanceScreen(state, onBack, viewModel::selectLeague, viewModel::selectClub, viewModel::adjust, viewModel::clearFeedback)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(state: FinanceUiState, onBack: () -> Unit, onLeague: (Long) -> Unit, onClub: (Long?) -> Unit,
                  onAdjust: (Long, Long, String) -> Unit, onFeedback: () -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.feedback) { state.feedback?.let { snackbar.showSnackbar(it); onFeedback() } }
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Financeiro • CR") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Voltar") } }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = { if (state.isAdmin) FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Outlined.Add, "Novo lançamento") } },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 12.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Saldos dos clubes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (state.isAdmin) item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.leagues.forEach { AssistChip(onClick = { onLeague(it.id) }, label = { Text(it.name) }, leadingIcon = if (it.id == state.selectedLeagueId) ({ Icon(Icons.Outlined.AccountBalance, null) }) else null) } } }
            items(state.balances, key = { "bal_${it.clubId}" }) { balance -> BalanceCard(balance, state.selectedClubId == balance.clubId) { onClub(if (state.selectedClubId == balance.clubId) null else balance.clubId) } }
            item { Spacer(Modifier.height(8.dp)); Text("Extrato completo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(if (state.selectedClubId == null) "Todos os lançamentos da liga" else "Filtrado pelo clube selecionado", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (state.statement.isEmpty()) item { Text("Nenhum lançamento registrado.", modifier = Modifier.padding(vertical = 24.dp)) }
            items(state.statement, key = { "stmt_${it.id}" }) { entry ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(entry.clubName, fontWeight = FontWeight.Bold); Text(formatCr(entry.amountCr), color = if (entry.amountCr >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
                    Text(entry.description)
                    Text(listOfNotNull(entry.typeLabel(), entry.counterpartyName?.let { "Contraparte: $it" }, entry.transferId?.let { "Transferência #$it" }).joinToString(" • "), style = MaterialTheme.typography.labelMedium)
                    Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(entry.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } }
            }
        }
    }
    if (showDialog) AdjustmentDialog(state.balances.filterNot { it.isBank }, { showDialog = false }) { club, amount, note -> onAdjust(club, amount, note); showDialog = false }
}

@Composable private fun BalanceCard(item: ClubBalance, selected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (item.isBank) "Banco da Liga" else item.clubName, fontWeight = FontWeight.Bold); Text(formatCr(item.balanceCr), fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun AdjustmentDialog(clubs: List<ClubBalance>, onDismiss: () -> Unit, onConfirm: (Long, Long, String) -> Unit) {
    var clubId by remember { mutableStateOf(clubs.firstOrNull()?.clubId) }; var value by rememberSaveable { mutableStateOf("") }; var note by rememberSaveable { mutableStateOf("") }; var credit by rememberSaveable { mutableStateOf(true) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Banco da Liga") }, text = {
        Column(
            modifier = Modifier
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Clube"); clubs.forEach { FilterChip(selected = clubId == it.clubId, onClick = { clubId = it.clubId }, label = { Text("${it.clubName} (${formatCr(it.balanceCr)})") }) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(credit, { credit = true }, { Text("Crédito") }); FilterChip(!credit, { credit = false }, { Text("Débito") }) }
            OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Valor em CR") }, singleLine = true)
            OutlinedTextField(note, { note = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
        }
    }, confirmButton = { Button(onClick = { clubId?.let { onConfirm(it, (value.toLongOrNull() ?: 0L) * if (credit) 1L else -1L, note) } }, enabled = clubId != null && (value.toLongOrNull() ?: 0L) > 0L && note.trim().length >= 3) { Text("Lançar") } }, dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } })
}

private fun formatCr(value: Long) = (if (value >= 0) "+" else "−") + kotlin.math.abs(value) + " CR"
private fun com.example.legacymasterliga.feature.finance.domain.StatementEntry.typeLabel() = if (type == "TRANSFER") "Mercado" else "Banco da Liga"
