package com.example.legacymasterliga.feature.auction.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.database.dao.AuctionLotSummary
import java.text.DateFormat
import java.util.Date

@Composable
fun AuctionListRoute(
    onBack: () -> Unit,
    onCreateLot: () -> Unit,
    onOpenLot: (Long) -> Unit,
    viewModel: AuctionListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    AuctionListScreen(
        state = state,
        onBack = onBack,
        onCreateLot = onCreateLot,
        onOpenLot = onOpenLot
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionListScreen(
    state: AuctionListUiState,
    onBack: () -> Unit,
    onCreateLot: () -> Unit,
    onOpenLot: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Leilão") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isAdmin) {
                FloatingActionButton(onClick = onCreateLot) {
                    Icon(Icons.Outlined.Add, contentDescription = "Novo lote")
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

        if (state.lots.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Nenhum leilão disponível.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.lots, key = { it.id }) { lot ->
                LotCard(lot = lot, onClick = { onOpenLot(lot.id) })
            }
        }
    }
}

@Composable
private fun LotCard(lot: AuctionLotSummary, onClick: () -> Unit) {
    val now = System.currentTimeMillis()
    val statusText = when {
        lot.status == "CLOSED" -> "Encerrado"
        now < lot.startAt -> "Agendado"
        now > lot.endAt -> "Processando..."
        else -> "Aberto"
    }
    
    val statusColor = when (statusText) {
        "Aberto" -> Color(0xFF2E7D32)
        "Agendado" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(lot.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(color = statusColor.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                    Text(
                        statusText, 
                        color = statusColor, 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Gavel, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${lot.itemCount} jogadores", style = MaterialTheme.typography.bodyMedium)
            }
            
            Text(
                "Fim: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lot.endAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
