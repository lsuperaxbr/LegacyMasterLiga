package com.example.legacymasterliga.feature.market.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.Player
import java.text.DateFormat
import java.util.Date

@Composable fun MarketRoute(onBack: () -> Unit, onOpenPlayer: (Long) -> Unit, viewModel: MarketViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MarketScreen(
        state = state,
        onBack = onBack,
        onLeague = viewModel::selectLeague,
        onTransfer = viewModel::transfer,
        onTransferRealPlayer = viewModel::transferRealPlayer,
        onDeleteTransfer = viewModel::deleteTransfer,
        onSwap = viewModel::swap,
        onFeedback = viewModel::clearFeedback,
        onQuery = viewModel::setPlayerQuery,
        onStatusFilter = viewModel::setPlayerStatusFilter,
        onClubFilter = viewModel::setPlayerClubFilter,
        onOpenPlayer = onOpenPlayer,
        onSetMarketStatus = viewModel::setPlayerMarketStatus,
        onAddFreeAgent = viewModel::addFreeAgent,
        onAddFreeAgentsBatch = viewModel::addFreeAgentsBatch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MarketScreen(
    state: MarketUiState,
    onBack: () -> Unit,
    onLeague: (Long) -> Unit,
    onTransfer: (String, Long, Long, Long, String?, String) -> Unit,
    onTransferRealPlayer: (Long, Long, Long, String?) -> Unit,
    onDeleteTransfer: (Long) -> Unit,
    onSwap: (String, Long, String, Long, Long, Long?, String?) -> Unit,
    onFeedback: () -> Unit,
    onQuery: (String?) -> Unit,
    onStatusFilter: (MarketStatus?) -> Unit,
    onClubFilter: (Long?) -> Unit,
    onOpenPlayer: (Long) -> Unit,
    onSetMarketStatus: (Player, MarketStatus, Long?) -> Unit,
    onAddFreeAgent: (String, Long) -> Unit,
    onAddFreeAgentsBatch: (List<String>, Long) -> Unit
) {
    var show by rememberSaveable { mutableStateOf(false) }
    var realTransferPlayer by remember { mutableStateOf<Player?>(null) }
    var showAddFreeAgent by rememberSaveable { mutableStateOf(false) }
    var showBatchFreeAgent by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    
    LaunchedEffect(state.feedback) { state.feedback?.let { snackbar.showSnackbar(it); onFeedback() } }
    
    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text("Mercado") }, 
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar") } }
            ) 
        }, 
        snackbarHost = { SnackbarHost(snackbar) }, 
        floatingActionButton = { 
            if (selectedTab == 1) {
                FloatingActionButton(onClick = { show = true }) { Icon(Icons.Outlined.Add, "Nova transferência") } 
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Jogadores") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Transferências") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Banco da Liga") })
            }
            
            when (selectedTab) {
                0 -> PlayersTab(state, onQuery, onStatusFilter, onClubFilter, onOpenPlayer, onSetMarketStatus, onTransfer = { realTransferPlayer = it })
                1 -> TransfersTab(state, onLeague, onDeleteTransfer)
                2 -> BankTab(state, onOpenPlayer, onSetMarketStatus, onTransfer = { show = true }, onTransferRealPlayer = { realTransferPlayer = it }, onAddFreeAgent = { showAddFreeAgent = true }, onAddFreeAgentsBatch = { showBatchFreeAgent = true })
            }
        }
    }
    
    if (show) TransferDialog(
        state = state,
        onDismiss = { show = false },
        onConfirmTransfer = { p, o, d, v, n, t -> onTransfer(p, o, d, v, n, t) },
        onConfirmSwap = { pA, cA, pB, cB, comp, payer, n -> onSwap(pA, cA, pB, cB, comp, payer, n) }
    )

    realTransferPlayer?.let { player ->
        RealTransferDialog(
            player = player,
            state = state,
            onDismiss = { realTransferPlayer = null },
            onConfirm = { dest, valCr, note ->
                onTransferRealPlayer(player.id, dest, valCr, note)
                realTransferPlayer = null
            }
        )
    }

    if (showAddFreeAgent) AddFreeAgentDialog(
        onDismiss = { showAddFreeAgent = false },
        onConfirm = { name, price -> onAddFreeAgent(name, price); showAddFreeAgent = false }
    )

    if (showBatchFreeAgent) BatchFreeAgentDialog(
        onDismiss = { showBatchFreeAgent = false },
        onConfirm = { names, price -> onAddFreeAgentsBatch(names, price); showBatchFreeAgent = false }
    )
}

@Composable
private fun BankTab(
    state: MarketUiState,
    onOpenPlayer: (Long) -> Unit,
    onSetMarketStatus: (Player, MarketStatus, Long?) -> Unit,
    onTransfer: () -> Unit,
    onTransferRealPlayer: (Player) -> Unit,
    onAddFreeAgent: () -> Unit,
    onAddFreeAgentsBatch: () -> Unit
) {
    val bankClub = state.clubs.find { it.isBank }
    val bankPlayers = state.players.filter { it.clubId == bankClub?.id }
    val reforcos = bankPlayers.filter { it.askingPriceCr != 5L || it.marketStatus != MarketStatus.FOR_SALE }
    val dispensados = bankPlayers.filter { it.marketStatus == MarketStatus.FOR_SALE && it.askingPriceCr == 5L }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.isAdmin) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onAddFreeAgentsBatch,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.PlaylistAdd, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Vários")
                    }
                    OutlinedButton(
                        onClick = onAddFreeAgent,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Um jogador")
                    }
                }
            }
        }

        // Seção Reforços Disponíveis
        item {
            Text(
                "REFORÇOS DISPONÍVEIS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        if (reforcos.isEmpty()) {
            item { Text("Nenhum reforço disponível.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(reforcos, key = { it.id }) { player ->
                PlayerMarketCard(player, state.isAdmin, state.ownClubIds, onClick = { onOpenPlayer(player.id) }, onSetStatus = onSetMarketStatus, onTransfer = onTransferRealPlayer)
            }
        }

        // Seção Dispensados
        item {
            Spacer(Modifier.height(12.dp))
            Text(
                "DISPENSADOS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        if (dispensados.isEmpty()) {
            item { Text("Nenhum jogador dispensado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(dispensados, key = { "d_${it.id}" }) { player ->
                PlayerMarketCard(player, state.isAdmin, state.ownClubIds, onClick = { onOpenPlayer(player.id) }, onSetStatus = onSetMarketStatus, onTransfer = onTransferRealPlayer)
            }
        }

        // Botão para registrar nova contratação do Banco
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onTransfer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.SwapHoriz, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar Contratação do Banco")
            }
        }
    }
}

@Composable
private fun BatchFreeAgentDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>, Long) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    val names = remember(text) {
        text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Reforços ao Banco") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Cole os nomes (um por linha). Todos entrarão como Reforços Disponíveis. Sem limite de quantidade.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Um jogador por linha") },
                    placeholder = { Text("RONALDINHO\nADRIANO\nFIGO...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 12,
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor em CR (mesmo para todos)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    ),
                )
                if (names.isNotEmpty()) {
                    Text(
                        "${names.size} jogador(es) detectado(s).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(names, priceText.toLongOrNull() ?: 0L) },
                enabled = names.isNotEmpty() && priceText.isNotBlank(),
            ) { Text("Adicionar Todos") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun AddFreeAgentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Reforço Disponível") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do jogador") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor em CR") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, priceText.toLongOrNull() ?: 0L) },
                enabled = name.isNotBlank() && priceText.isNotBlank()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun PlayersTab(
    state: MarketUiState,
    onQuery: (String?) -> Unit,
    onStatusFilter: (MarketStatus?) -> Unit,
    onClubFilter: (Long?) -> Unit,
    onOpenPlayer: (Long) -> Unit,
    onSetMarketStatus: (Player, MarketStatus, Long?) -> Unit,
    onTransfer: (Player) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SearchBar(state.query, onQuery)
        
        FilterBar(state, onStatusFilter, onClubFilter)
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.players.isEmpty()) {
                item { 
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum jogador encontrado com estes filtros.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.players, key = { it.id }) { player ->
                    PlayerMarketCard(
                        player = player, 
                        isAdmin = state.isAdmin,
                        ownClubIds = state.ownClubIds,
                        onClick = { onOpenPlayer(player.id) },
                        onSetStatus = onSetMarketStatus,
                        onTransfer = onTransfer
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String?, onQuery: (String?) -> Unit) {
    OutlinedTextField(
        value = query ?: "",
        onValueChange = { onQuery(it.takeIf { it.isNotBlank() }) },
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        placeholder = { Text("Buscar jogador por nome...") },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterBar(
    state: MarketUiState,
    onStatusFilter: (MarketStatus?) -> Unit,
    onClubFilter: (Long?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.statusFilter == null,
                onClick = { onStatusFilter(null) },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = state.statusFilter == MarketStatus.FOR_SALE,
                onClick = { onStatusFilter(MarketStatus.FOR_SALE) },
                label = { Text("À Venda") }
            )
            FilterChip(
                selected = state.statusFilter == MarketStatus.NEGOTIABLE,
                onClick = { onStatusFilter(MarketStatus.NEGOTIABLE) },
                label = { Text("Negociáveis") }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.clubFilter == null,
                onClick = { onClubFilter(null) },
                label = { Text("Todos os Clubes") }
            )
            state.clubs.forEach { club ->
                FilterChip(
                    selected = state.clubFilter == club.id,
                    onClick = { onClubFilter(club.id) },
                    label = { Text(club.name) }
                )
            }
        }
    }
}

@Composable
private fun PlayerMarketCard(
    player: Player, 
    isAdmin: Boolean,
    ownClubIds: List<Long>,
    onClick: () -> Unit,
    onSetStatus: (Player, MarketStatus, Long?) -> Unit,
    onTransfer: (Player) -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    val canTransfer = (player.marketStatus == MarketStatus.FOR_SALE || player.marketStatus == MarketStatus.NEGOTIABLE) &&
                      (isAdmin || !ownClubIds.contains(player.clubId))
    
    Card(onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        if (isAdmin || ownClubIds.contains(player.clubId)) {
                            IconButton(
                                onClick = { showEdit = true },
                                modifier = Modifier.size(32.dp).padding(start = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, "Editar status", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text("${player.clubName} • ${player.position ?: "?"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                MarketStatusTag(player)
            }

            if (canTransfer) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onTransfer(player) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Transferir", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showEdit) {
        MarketStatusEditDialog(
            player = player,
            onDismiss = { showEdit = false },
            onConfirm = { status, price ->
                onSetStatus(player, status, price)
                showEdit = false
            }
        )
    }
}

@Composable
private fun MarketStatusEditDialog(
    player: Player,
    onDismiss: () -> Unit,
    onConfirm: (MarketStatus, Long?) -> Unit
) {
    var status by remember { mutableStateOf(player.marketStatus) }
    var priceText by remember { mutableStateOf(player.askingPriceCr?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Status no Mercado: ${player.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Escolha como este jogador aparece no mercado:", style = MaterialTheme.typography.bodySmall)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MarketEditOption("À Venda", status == MarketStatus.FOR_SALE) { status = MarketStatus.FOR_SALE }
                    MarketEditOption("Negociável", status == MarketStatus.NEGOTIABLE) { status = MarketStatus.NEGOTIABLE }
                    MarketEditOption("Não Negociável", status == MarketStatus.NOT_FOR_SALE) { status = MarketStatus.NOT_FOR_SALE }
                }

                if (status == MarketStatus.FOR_SALE) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Preço em CR") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val price = if (status == MarketStatus.FOR_SALE) priceText.toLongOrNull() ?: 0L else null
                    onConfirm(status, price) 
                },
                enabled = status != MarketStatus.FOR_SALE || priceText.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun MarketEditOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
private fun MarketStatusTag(player: Player) {
    val status = player.marketStatus
    val text = when (status) {
        MarketStatus.FOR_SALE -> "${player.askingPriceCr ?: 0} CR"
        MarketStatus.NEGOTIABLE -> "PROPOSTA"
        MarketStatus.NOT_FOR_SALE -> "—"
        MarketStatus.NOT_LISTED -> "—"
    }
    
    val color = when (status) {
        MarketStatus.FOR_SALE -> MaterialTheme.colorScheme.primary
        MarketStatus.NEGOTIABLE -> Color(0xFFF9A825)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
private fun TransfersTab(state: MarketUiState, onLeague: (Long) -> Unit, onDelete: (Long) -> Unit) {
    var deleteId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.isAdmin) item { Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.leagues.forEach { AssistChip(onClick = { onLeague(it.id) }, label = { Text(it.name) }) } } }
        if (state.transfers.isEmpty()) item { Text("Nenhuma transferência registrada.", modifier = Modifier.padding(vertical = 24.dp)) }
        items(state.transfers, key = { "tx_${it.id}" }) { transfer -> 
            Card(Modifier.fillMaxWidth()) { 
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                        Text(transfer.playerName, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (transfer.valueCr == 0L) "Sem custo" else "${transfer.valueCr} CR", fontWeight = FontWeight.Bold)
                            if (state.isAdmin) {
                                val isReverted = transfer.note?.startsWith("[REVERTIDA]") == true
                                if (!isReverted) {
                                    IconButton(onClick = { deleteId = transfer.id }, modifier = Modifier.size(32.dp).padding(start = 8.dp)) {
                                        Icon(Icons.AutoMirrored.Outlined.Undo, "Reverter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    Text("${transfer.originClubName} → ${transfer.destinationClubName}")
                    Text(
                        text = listOfNotNull(
                            opTypeLabel(transfer.type),
                            transfer.note?.let { "Obs: $it" },
                            transfer.swapId?.let { "Vínculo de Troca" }
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(transfer.createdAt)), style = MaterialTheme.typography.labelSmall)
                } 
            } 
        }
    }

    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Reverter transferência?") },
            text = { Text("Isso vai devolver o valor da transferência e tentar mover o jogador de volta. O histórico será mantido, marcado como revertido.") },
            confirmButton = {
                Button(onClick = { onDelete(id); deleteId = null }) {
                    Text("Reverter")
                }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun TransferDialog(
    state: MarketUiState,
    onDismiss: () -> Unit,
    onConfirmTransfer: (String, Long, Long, Long, String?, String) -> Unit,
    onConfirmSwap: (String, Long, String, Long, Long, Long?, String?) -> Unit
) {
    var opType by rememberSaveable { mutableStateOf("PURCHASE") }
    var playerA by rememberSaveable { mutableStateOf("") }; var playerB by rememberSaveable { mutableStateOf("") }
    var origin by remember { mutableStateOf<Long?>(null) }; var destination by remember { mutableStateOf(if (state.isAdmin) state.ownClubIds.firstOrNull() else state.ownClubIds.firstOrNull()) }
    var value by rememberSaveable { mutableStateOf("") }; var note by rememberSaveable { mutableStateOf("") }
    var payerId by remember { mutableStateOf<Long?>(null) }

    val opTypes = listOf("PURCHASE" to "Compra", "FREE" to "Gratuita", "FREE_AGENT" to "Contratar Livre", "RELEASE" to "Liberar", "SWAP" to "Troca")
    
    val selectedDestination = state.clubs.find { it.id == destination }
    val selectedOrigin = state.clubs.find { it.id == origin }
    
    // Auto-setup based on type
    LaunchedEffect(opType) {
        val bank = state.clubs.find { it.isBank }
        when (opType) {
            "FREE_AGENT" -> { origin = bank?.id; value = "0" }
            "RELEASE" -> { destination = bank?.id; value = "0" }
            "FREE" -> { value = "0" }
        }
    }

    val available = (if (opType == "SWAP") payerId else if (opType == "RELEASE") origin else destination)?.let { id -> state.balances.find { it.clubId == id }?.balanceCr } ?: 0L
    val amount = value.toLongOrNull() ?: 0L
    val hasBalance = if (opType == "RELEASE") available >= 5L 
                    else (opType != "PURCHASE" && opType != "SWAP") || (selectedDestination?.isBank == true) || amount <= available

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Nova operação de mercado") }, text = { 
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Tipo de operação", fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    opTypes.forEach { (key, label) ->
                        FilterChip(opType == key, { opType = key }, { Text(label) })
                    }
                }
            }

            if (opType == "SWAP") {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Clube A", fontWeight = FontWeight.Bold)
                        ClubChoices("", state.clubs.filter { !it.isBank }, origin) { origin = it }
                        OutlinedTextField(playerA, { playerA = it }, label = { Text("Jogador do Clube A") }, modifier = Modifier.fillMaxWidth())
                        
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        
                        Text("Clube B", fontWeight = FontWeight.Bold)
                        ClubChoices("", state.clubs.filter { !it.isBank && it.id != origin }, destination) { destination = it }
                        OutlinedTextField(playerB, { playerB = it }, label = { Text("Jogador do Clube B") }, modifier = Modifier.fillMaxWidth())
                        
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        
                        Text("Compensação Financeira", fontWeight = FontWeight.Bold)
                        OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Valor em CR") }, modifier = Modifier.fillMaxWidth())
                        if (amount > 0) {
                            Text("Quem paga a compensação?", style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(payerId == origin, { payerId = origin }, { Text("Clube A") }, enabled = origin != null)
                                FilterChip(payerId == destination, { payerId = destination }, { Text("Clube B") }, enabled = destination != null)
                            }
                        }
                    }
                }
            } else {
                item { OutlinedTextField(playerA, { playerA = it }, label = { Text("Nome do jogador") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { 
                    ClubChoices(
                        label = "Saindo de (Origem)", 
                        clubs = if (opType == "FREE_AGENT") state.clubs.filter { it.isBank } else state.clubs.filter { it.id != destination }, 
                        selected = origin
                    ) { origin = it } 
                }
                item { 
                    ClubChoices(
                        label = "Indo para (Destino)", 
                        clubs = if (opType == "RELEASE") state.clubs.filter { it.isBank } else state.clubs.filter { it.id != origin && (state.isAdmin || state.ownClubIds.contains(it.id)) }, 
                        selected = destination
                    ) { destination = it } 
                }
                item { 
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Valor em CR") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = opType == "PURCHASE" || opType == "FREE_AGENT")
                        
                        if (opType == "RELEASE" && origin != null && selectedOrigin?.isBank == false) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Multa de liberação: 5 CR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    Text("O clube pagará 5 CR ao Banco da Liga para liberar o jogador.", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (destination != null && (opType == "PURCHASE" || opType == "FREE_AGENT")) {
                            if (selectedDestination?.isBank == true) {
                                Text("Pagamento: Isento (Retorno ao Banco)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Pagador: ${selectedDestination?.name}", style = MaterialTheme.typography.labelMedium)
                                Text("Saldo disponível: $available CR", color = if (hasBalance) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            item { OutlinedTextField(note, { note = it }, label = { Text("Observação (opcional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2) }
        }
    }, confirmButton = { 
        val canConfirm = if (opType == "SWAP") {
            playerA.trim().length >= 2 && playerB.trim().length >= 2 && origin != null && destination != null && (amount == 0L || (payerId != null && hasBalance))
        } else {
            val isRelease = opType == "RELEASE"
            playerA.trim().length >= 2 && origin != null && destination != null && hasBalance && (if (isRelease) available >= 5L else true)
        }
        
        Button(onClick = { 
            if (opType == "SWAP") {
                onConfirmSwap(playerA, origin!!, playerB, destination!!, amount, payerId, note.takeIf { it.isNotBlank() })
            } else {
                onConfirmTransfer(playerA, origin!!, destination!!, amount, note.takeIf { it.isNotBlank() }, opType)
            }
        }, enabled = canConfirm) { 
            Text("Confirmar") 
        } 
    }, dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } })
}

private fun opTypeLabel(type: String): String = when (type) {
    "PURCHASE" -> "Compra"
    "FREE" -> "Gratuita"
    "FREE_AGENT" -> "Contratação Livre"
    "RELEASE" -> "Liberação"
    "SWAP" -> "Troca"
    else -> "Transferência"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun ClubChoices(label: String, clubs: List<Club>, selected: Long?, onSelect: (Long) -> Unit) { 
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { 
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (clubs.isEmpty()) {
            Text("Nenhum clube disponível", style = MaterialTheme.typography.bodySmall)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { 
                clubs.forEach { club -> 
                    FilterChip(
                        selected = selected == club.id, 
                        onClick = { onSelect(club.id) }, 
                        label = { Text(if (club.isBank) "🏦 ${club.name}" else club.name) }
                    ) 
                } 
            } 
        }
    } 
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RealTransferDialog(
    player: Player,
    state: MarketUiState,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, String?) -> Unit
) {
    var destination by remember { mutableStateOf<Long?>(if (state.isAdmin) null else state.ownClubIds.firstOrNull()) }
    var valueText by remember { mutableStateOf(player.askingPriceCr?.toString() ?: "0") }
    var note by remember { mutableStateOf("") }

    val clubs = if (state.isAdmin) state.clubs.filter { it.id != player.clubId }
                else state.clubs.filter { state.ownClubIds.contains(it.id) && it.id != player.clubId }

    val selectedDest = state.clubs.find { it.id == destination }
    val available = destination?.let { id -> state.balances.find { it.clubId == id }?.balanceCr } ?: 0L
    val amount = valueText.toLongOrNull() ?: 0L
    val hasBalance = selectedDest?.isBank == true || amount <= available

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transferir ${player.name}") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Origem: ${player.clubName}", style = MaterialTheme.typography.bodyMedium)
                }
                item {
                    ClubChoices(
                        label = "Destino (Seu Clube)",
                        clubs = clubs,
                        selected = destination,
                        onSelect = { destination = it }
                    )
                }
                item {
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it.filter(Char::isDigit) },
                        label = { Text("Valor em CR") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                item {
                    if (destination != null) {
                        Text(
                            text = "Saldo disponível: $available CR",
                            color = if (hasBalance) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Observação (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(destination!!, amount, note.takeIf { it.isNotBlank() }) },
                enabled = destination != null && hasBalance
            ) {
                Text("Confirmar Transferência")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
