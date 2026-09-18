package com.example.legacymasterliga.feature.clubprofile.presentation

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import com.example.legacymasterliga.core.ui.components.NativeCrest
import com.example.legacymasterliga.feature.clubprofile.domain.ClubMatchOutcome
import com.example.legacymasterliga.feature.clubprofile.domain.ClubRecentResult
import com.example.legacymasterliga.feature.clubprofile.domain.ClubSeasonHistory
import com.example.legacymasterliga.feature.results.domain.Standing
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import com.example.legacymasterliga.core.database.model.TransferHistoryRow
import com.example.legacymasterliga.core.database.dao.ClubTrophyRow
import java.util.Locale

@Composable
fun ClubProfileRoute(
    onBack: () -> Unit,
    onOpenPlayer: (Long) -> Unit,
    viewModel: ClubProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback) {
        feedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    ClubProfileScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSelectSeason = viewModel::selectSeason,
        onAddPlayer = viewModel::addPlayer,
        onAddPlayersBatch = viewModel::addPlayersBatch,
        onPrecifyAll = viewModel::precifyAllPlayers,
        onOpenPlayer = onOpenPlayer,
        onRevertTransfer = viewModel::revertTransfer,
        onDispensePlayer = viewModel::dispensePlayer,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubProfileScreen(
    state: ClubProfileUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSelectSeason: (Long) -> Unit,
    onAddPlayer: (String, String?) -> Unit,
    onAddPlayersBatch: (List<String>, Long?, Boolean) -> Unit,
    onPrecifyAll: (Long) -> Unit,
    onOpenPlayer: (Long) -> Unit,
    onRevertTransfer: (Long) -> Unit,
    onDispensePlayer: (com.example.legacymasterliga.domain.model.Player) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil do clube") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val header = state.header
        if (header == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Clube não encontrado.")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ClubHeader(
                name = header.clubName,
                crestUri = header.crestUri,
                presidentName = header.presidentName,
                isActive = header.isActive,
            )
            
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Resumo") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Elenco") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Histórico") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Negociações") })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Troféus") })
            }

            when (selectedTab) {
                0 -> SummaryTab(state, onSelectSeason)
                1 -> RosterTab(state, onAddPlayer, onAddPlayersBatch, onPrecifyAll, onOpenPlayer, onDispensePlayer)
                2 -> HistoryTab(state.history)
                3 -> NegociacoesTab(state.transfers, header.clubId, state.isAdmin, onRevertTransfer)
                4 -> TrophiesTab(state.trophies)
            }
        }
    }
}

@Composable
private fun SummaryTab(state: ClubProfileUiState, onSelectSeason: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SeasonSelector(
                seasons = state.seasons,
                selectedSeasonId = state.selectedSeasonId,
                onSelectSeason = onSelectSeason,
            )
        }
        item {
            SectionTitle(Icons.Outlined.EmojiEvents, "Estatísticas da temporada")
            val standing = state.standing
            if (standing == null) {
                EmptyCard("O clube ainda não possui classificação nesta temporada.")
            } else {
                CurrentStandingCard(standing)
            }
        }
        item {
            SectionTitle(Icons.Outlined.SportsSoccer, "Últimos resultados")
            if (state.recentResults.isEmpty()) {
                EmptyCard("Nenhum resultado lançado para este clube na temporada selecionada.")
            }
        }
        items(state.recentResults, key = { it.matchId }) { result ->
            RecentResultCard(result)
        }
    }
}

@Composable
private fun RosterTab(
    state: ClubProfileUiState,
    onAddPlayer: (String, String?) -> Unit,
    onAddPlayersBatch: (List<String>, Long?, Boolean) -> Unit,
    onPrecifyAll: (Long) -> Unit,
    onOpenPlayer: (Long) -> Unit,
    onDispensePlayer: (com.example.legacymasterliga.domain.model.Player) -> Unit,
) {
    val players = state.players
    var showAddDialog by remember { mutableStateOf(false) }
    var showBatchDialog by remember { mutableStateOf(false) }
    var showPrecifyDialog by remember { mutableStateOf(false) }
    var playerToDispense by remember { mutableStateOf<com.example.legacymasterliga.domain.model.Player?>(null) }
    
    val isBank = state.header?.isActive == true && state.header.clubName.contains("Banco", ignoreCase = true) 
    // Nota: Usamos a detecção de nome pois o domínio Club não tem isBank. 
    // Como Engenheiro Chefe, garanto que o Admin verá botões extras.

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Jogadores vinculados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isBank) {
                            IconButton(onClick = { showPrecifyDialog = true }) {
                                Icon(Icons.Outlined.Sell, contentDescription = "Precificar Tudo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        TextButton(onClick = { showBatchDialog = true }) {
                            Icon(Icons.Outlined.PlaylistAdd, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Vários")
                        }
                        TextButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Outlined.Add, null)
                            Text("Adicionar")
                        }
                    }
                }
            }
            
            if (players.isEmpty()) {
                item { EmptyCard("Nenhum jogador cadastrado neste elenco.") }
            } else {
                items(players, key = { it.id }) { player ->
                    PlayerListItem(
                        player = player, 
                        canManage = state.canManage,
                        onDispense = { playerToDispense = it },
                        onClick = { onOpenPlayer(player.id) }
                    )
                }
            }
        }
    }
    
    playerToDispense?.let { player ->
        AlertDialog(
            onDismissRequest = { playerToDispense = null },
            title = { Text("Dispensar ${player.name}?") },
            text = { Text("Isso vai custar 5 CR ao clube e o jogador será enviado ao Banco da Liga como Dispensado.") },
            confirmButton = {
                Button(
                    onClick = { 
                        onDispensePlayer(player)
                        playerToDispense = null 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Dispensar")
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToDispense = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    if (showAddDialog) {
        AddPlayerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, pos -> 
                onAddPlayer(name, pos)
                showAddDialog = false
            }
        )
    }

    if (showBatchDialog) {
        BatchPlayerImportDialog(
            onDismiss = { showBatchDialog = false },
            onConfirm = { names, price, forSale ->
                onAddPlayersBatch(names, price, forSale)
                showBatchDialog = false
            }
        )
    }

    if (showPrecifyDialog) {
        PrecifyAllDialog(
            onDismiss = { showPrecifyDialog = false },
            onConfirm = { price ->
                onPrecifyAll(price)
                showPrecifyDialog = false
            }
        )
    }
}

@Composable
private fun TrophiesTab(trophies: List<ClubTrophyRow>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (trophies.isEmpty()) {
            item { EmptyCard("Nenhum título conquistado ainda.") }
        } else {
            items(trophies, key = { it.seasonId }) { trophy ->
                TrophyCard(trophy)
            }
        }
    }
}

@Composable
private fun TrophyCard(trophy: ClubTrophyRow) {
    val isLeague = trophy.competitionType == "LEAGUE"
    val trophyColor = if (isLeague) Color(0xFFFFD700) else Color(0xFFC0C0C0)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, trophyColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = if (isLeague) Icons.Outlined.EmojiEvents else Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                tint = trophyColor,
                modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trophy.competitionName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = trophy.seasonName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Conquistado em ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR")).format(java.util.Date(trophy.closedAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NegociacoesTab(
    transfers: List<TransferHistoryRow>,
    currentClubId: Long,
    isAdmin: Boolean,
    onRevert: (Long) -> Unit,
) {
    var selectedSeasonFilter by rememberSaveable { mutableStateOf<Long?>(null) } // null = Todas, -1 = Sem temporada
    
    val seasonOptions = remember(transfers) {
        transfers.map { it.seasonId }.distinct().sortedByDescending { it }
    }

    val filteredTransfers = remember(transfers, selectedSeasonFilter) {
        when (selectedSeasonFilter) {
            null -> transfers
            -1L -> transfers.filter { it.seasonId == null }
            else -> transfers.filter { it.seasonId == selectedSeasonFilter }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (seasonOptions.size > 1 || (seasonOptions.size == 1 && seasonOptions.first() != null)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSeasonFilter == null,
                    onClick = { selectedSeasonFilter = null },
                    label = { Text("Todas") }
                )
                
                seasonOptions.forEach { sId ->
                    if (sId != null) {
                        FilterChip(
                            selected = selectedSeasonFilter == sId,
                            onClick = { selectedSeasonFilter = sId },
                            label = { Text("Temp. $sId") } // Simplificado pois não temos o nome aqui fácil
                        )
                    } else {
                        FilterChip(
                            selected = selectedSeasonFilter == -1L,
                            onClick = { selectedSeasonFilter = -1L },
                            label = { Text("Sem registro") }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (filteredTransfers.isEmpty()) {
                item { EmptyCard("Nenhuma negociação encontrada para este filtro.") }
            } else {
                items(filteredTransfers, key = { it.id }) { transfer ->
                    NegociacaoCard(transfer, currentClubId, isAdmin, onRevert)
                }
            }
        }
    }
}

@Composable
private fun NegociacaoCard(
    transfer: TransferHistoryRow,
    currentClubId: Long,
    isAdmin: Boolean,
    onRevert: (Long) -> Unit,
) {
    var showRevertDialog by remember { mutableStateOf(false) }
    val isEntrada = transfer.destinationClubId == currentClubId
    val color = if (isEntrada) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val isReverted = transfer.note?.startsWith("[REVERTIDA]") == true

    val (label, typeColor) = when (transfer.type) {
        "RELEASE"    -> "Dispensado" to MaterialTheme.colorScheme.error
        "FREE_AGENT" -> "Contratado (Livre)" to Color(0xFF2E7D32)
        "SWAP"       -> "Troca" to MaterialTheme.colorScheme.tertiary
        else         -> if (transfer.destinationClubId == transfer.originClubId) "Interno" to MaterialTheme.colorScheme.onSurfaceVariant
                        else "Transferência" to MaterialTheme.colorScheme.primary
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transfer.playerName, fontWeight = FontWeight.Bold)
                Text(
                    "${transfer.originClubName} → ${transfer.destinationClubName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!transfer.note.isNullOrBlank()) {
                    Text(transfer.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isAdmin) {
                        if (!isReverted) {
                            IconButton(onClick = { showRevertDialog = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.AutoMirrored.Outlined.Undo, "Reverter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall, color = typeColor)
                }
                if (transfer.valueCr > 0) {
                    Text("${transfer.valueCr} CR", fontWeight = FontWeight.Bold, color = color)
                }
                Text(
                    java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale("pt", "BR"))
                        .format(java.util.Date(transfer.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showRevertDialog) {
        AlertDialog(
            onDismissRequest = { showRevertDialog = false },
            title = { Text("Reverter transferência?") },
            text = {
                Text("A transferência de ${transfer.playerName} será desfeita. O valor de ${transfer.valueCr} CR será devolvido ao comprador e o jogador será movido de volta se possível.")
            },
            confirmButton = {
                Button(onClick = { onRevert(transfer.id); showRevertDialog = false }) {
                    Text("Reverter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevertDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BatchPlayerImportDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>, Long?, Boolean) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var putForSale by rememberSaveable { mutableStateOf(true) }
    
    val names = remember(text) { text.lines().map { it.trim() }.filter { it.isNotEmpty() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importar Elenco") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Cole a lista de jogadores (um por linha).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Lista de nomes") },
                    placeholder = { Text("ADRIANO\nIBRAHIMOVIC\nFIGO...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                )
                
                HorizontalDivider()
                
                Text("Configuração de Mercado", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = putForSale, onCheckedChange = { putForSale = it })
                    Text("Disponibilizar no Mercado", style = MaterialTheme.typography.bodyMedium)
                }

                if (putForSale) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Valor de Venda (CR)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }

                if (names.isNotEmpty()) {
                    Text("${names.size} jogador(es) detectado(s).", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(names, priceText.toLongOrNull(), putForSale) },
                enabled = names.isNotEmpty(),
            ) { Text("Adicionar Todos") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun PrecifyAllDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var priceText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Precificar Tudo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Defina o valor de venda para TODOS os jogadores deste clube.")
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor em CR") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(priceText.toLongOrNull() ?: 0L) },
                enabled = priceText.isNotBlank(),
            ) { Text("Aplicar em Todos") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun HistoryTab(history: List<ClubSeasonHistory>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionTitle(Icons.Outlined.History, "Histórico por temporada")
            if (history.isEmpty()) {
                EmptyCard("O histórico aparecerá após o primeiro resultado registrado.")
            }
        }
        items(history, key = { it.seasonId }) { item ->
            SeasonHistoryCard(item)
        }
    }
}

@Composable
private fun PlayerListItem(
    player: com.example.legacymasterliga.domain.model.Player,
    canManage: Boolean,
    onDispense: (com.example.legacymasterliga.domain.model.Player) -> Unit,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    if (player.overall != null) {
                        Text(
                            "${player.overall}",
                            color = com.example.legacymasterliga.domain.PlayerAttributesParser.colorFor(player.overall),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                        )
                    }
                }
                Text(player.position ?: "Posição não informada", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canManage) {
                    IconButton(
                        onClick = { onDispense(player) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonRemove,
                            contentDescription = "Dispensar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                MarketStatusTag(player)
            }
        }
    }
}

@Composable
private fun MarketStatusTag(player: com.example.legacymasterliga.domain.model.Player) {
    val status = player.marketStatus
    val text = when (status) {
        com.example.legacymasterliga.core.model.MarketStatus.FOR_SALE -> "${player.askingPriceCr ?: 0} CR"
        com.example.legacymasterliga.core.model.MarketStatus.NEGOTIABLE -> "NEGOCIÁVEL"
        com.example.legacymasterliga.core.model.MarketStatus.NOT_FOR_SALE -> "INEGOCIÁVEL"
        com.example.legacymasterliga.core.model.MarketStatus.NOT_LISTED -> "—"
    }
    
    val color = when (status) {
        com.example.legacymasterliga.core.model.MarketStatus.FOR_SALE -> MaterialTheme.colorScheme.primary
        com.example.legacymasterliga.core.model.MarketStatus.NEGOTIABLE -> Color(0xFFF9A825)
        com.example.legacymasterliga.core.model.MarketStatus.NOT_FOR_SALE -> MaterialTheme.colorScheme.error
        com.example.legacymasterliga.core.model.MarketStatus.NOT_LISTED -> MaterialTheme.colorScheme.onSurfaceVariant
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
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Jogador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nome do jogador") }, singleLine = true)
                OutlinedTextField(position, { position = it }, label = { Text("Posição (ex: CA, MAT, LD)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, position.takeIf { it.isNotBlank() }) }, enabled = name.isNotBlank()) {
                Text("Cadastrar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ClubHeader(
    name: String,
    crestUri: String?,
    presidentName: String?,
    isActive: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ClubCrest(name, crestUri, Modifier.size(92.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(presidentName ?: "Sem presidente associado")
                }
                Text(
                    if (isActive) "Clube ativo" else "Clube desativado",
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ClubCrest(name: String, crestUri: String?, modifier: Modifier = Modifier) {
    NativeCrest(clubName = name, crestRaw = crestUri, modifier = modifier)
}

@Composable
private fun SeasonSelector(
    seasons: List<ScheduleSeasonOption>,
    selectedSeasonId: Long?,
    onSelectSeason: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Competição e temporada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (seasons.isEmpty()) {
            Text("Nenhuma temporada disponível.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            seasons.forEach { season ->
                AssistChip(
                    onClick = { onSelectSeason(season.seasonId) },
                    label = { Text("${season.competitionName} • ${season.seasonName}") },
                    leadingIcon = if (season.seasonId == selectedSeasonId) {
                        { Icon(Icons.Outlined.EmojiEvents, contentDescription = null) }
                    } else null,
                )
            }
        }
    }
}

@Composable
private fun CurrentStandingCard(standing: Standing) {
    val leaderColor = Color(0xFFFFD54F)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (standing.position == 1) leaderColor.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (standing.position == 1) "👑 1º colocado" else "${standing.position}º colocado",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            StatGrid(
                listOf(
                    "Jogos" to standing.played.toString(),
                    "Vitórias" to standing.wins.toString(),
                    "Empates" to standing.draws.toString(),
                    "Derrotas" to standing.losses.toString(),
                    "Gols pró" to standing.goalsFor.toString(),
                    "Gols contra" to standing.goalsAgainst.toString(),
                    "Saldo" to standing.goalDifference.signed(),
                    "Pontos" to standing.points.toString(),
                    "Aproveitamento" to standing.performancePercentage.percent(),
                ),
            )
        }
    }
}

@Composable
private fun StatGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (label, value) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        }
                    }
                }
                repeat(3 - rowItems.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RecentResultCard(result: ClubRecentResult) {
    val color = when (result.outcome) {
        ClubMatchOutcome.WIN -> Color(0xFF2E7D32)
        ClubMatchOutcome.DRAW -> Color(0xFFF9A825)
        ClubMatchOutcome.LOSS -> MaterialTheme.colorScheme.error
    }
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                when (result.outcome) {
                    ClubMatchOutcome.WIN -> "V"
                    ClubMatchOutcome.DRAW -> "E"
                    ClubMatchOutcome.LOSS -> "D"
                },
                modifier = Modifier.background(color, CircleShape).padding(horizontal = 11.dp, vertical = 7.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Column(Modifier.weight(1f)) {
                Text("Rodada ${result.roundNumber}", style = MaterialTheme.typography.labelMedium)
                Text("contra ${result.opponentName}", fontWeight = FontWeight.SemiBold)
            }
            Text("${result.goalsFor} × ${result.goalsAgainst}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SeasonHistoryCard(history: ClubSeasonHistory) {
    Card {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(history.competitionName, fontWeight = FontWeight.Bold)
                    Text(history.seasonName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${history.position}º", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(
                "${history.points} pts • ${history.played} J • ${history.wins} V • ${history.draws} E • ${history.losses} D",
            )
            Text(
                "GP ${history.goalsFor} • GC ${history.goalsAgainst} • SG ${history.goalDifference.signed()} • APR ${history.performancePercentage.percent()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(message, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Int.signed(): String = if (this > 0) "+$this" else toString()
private fun Double.percent(): String = String.format(Locale("pt", "BR"), "%.1f%%", this)
