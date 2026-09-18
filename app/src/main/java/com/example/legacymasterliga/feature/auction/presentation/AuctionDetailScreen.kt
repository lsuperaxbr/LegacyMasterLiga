package com.example.legacymasterliga.feature.auction.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.database.dao.AuctionItemRow
import com.example.legacymasterliga.core.database.dao.AuctionLotSummary
import java.text.DateFormat
import java.util.Date

@Composable
fun AuctionDetailRoute(
    onBack: () -> Unit,
    onOpenPlayer: (Long) -> Unit,
    viewModel: AuctionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    AuctionDetailScreen(
        state = state,
        onBack = onBack,
        onBid = viewModel::placeBid,
        onSelectClub = viewModel::selectClub,
        onOpenPlayer = onOpenPlayer,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailScreen(
    state: AuctionDetailUiState,
    onBack: () -> Unit,
    onBid: (Long, Long) -> Unit,
    onSelectClub: (Long) -> Unit,
    onOpenPlayer: (Long) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.lot?.name ?: "Detalhes do Leilão") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val lot = state.lot
        if (lot == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Leilão não encontrado.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LotInfoCard(lot)
            }

            if (state.managedClubs.size > 1) {
                item {
                    ClubSelector(
                        clubs = state.managedClubs,
                        selectedClubId = state.selectedClubId,
                        onSelectClub = onSelectClub
                    )
                }
            }
            
            item {
                Text("Jogadores em Leilão", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            items(state.items, key = { it.id }) { item ->
                AuctionItemCard(
                    item = item,
                    lot = lot,
                    userClubId = state.selectedClubId,
                    onBid = { amount -> onBid(item.id, amount) },
                    onOpenPlayer = onOpenPlayer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubSelector(
    clubs: List<com.example.legacymasterliga.domain.model.Club>,
    selectedClubId: Long?,
    onSelectClub: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Escolha o clube para o lance:", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            clubs.forEach { club ->
                FilterChip(
                    selected = club.id == selectedClubId,
                    onClick = { onSelectClub(club.id) },
                    label = { Text(club.name) },
                    leadingIcon = if (club.id == selectedClubId) {
                        { Icon(Icons.Outlined.Gavel, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun LotInfoCard(lot: AuctionLotSummary) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Período do Leilão", style = MaterialTheme.typography.labelMedium)
            Text(
                "${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lot.startAt))} até " +
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lot.endAt)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            if (lot.status == "CLOSED") {
                Text("Leilão encerrado em ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lot.closedAt ?: 0L))}", 
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuctionItemCard(
    item: AuctionItemRow,
    lot: AuctionLotSummary,
    userClubId: Long?,
    onBid: (Long) -> Unit,
    onOpenPlayer: (Long) -> Unit
) {
    var bidText by remember { mutableStateOf("") }
    val now = System.currentTimeMillis()
    val isOpen = lot.status != "CLOSED" && now in lot.startAt..lot.endAt && item.status == "OPEN"
    
    val leadingClubText = if (item.leadingClubId == null) "Sem lances" 
                         else if (item.leadingClubId == userClubId) "Seu clube lidera" 
                         else "Líder: ${item.leadingClubName}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onOpenPlayer(item.playerId) }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        item.playerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("${item.playerPosition ?: "---"} • OVR ${item.playerOverall ?: "--"}", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Lance Atual", style = MaterialTheme.typography.labelSmall)
                    Text("${item.currentBidCr ?: item.startingPriceCr} CR", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(leadingClubText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (item.status != "OPEN") {
                    Text(
                        if (item.status == "SOLD") "VENDIDO" else "NÃO VENDIDO",
                        color = if (item.status == "SOLD") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            if (isOpen) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bidText,
                        onValueChange = { bidText = it.filter { c -> c.isDigit() } },
                        label = { Text("Valor do Lance") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        prefix = { Text("CR ") }
                    )
                    Button(
                        onClick = { onBid(bidText.toLongOrNull() ?: 0L); bidText = "" },
                        enabled = bidText.isNotBlank(),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Outlined.Gavel, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Dar Lance")
                    }
                }
            }
        }
    }
}
