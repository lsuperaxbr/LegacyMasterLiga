package com.example.legacymasterliga.feature.player.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.domain.PlayerAttributesParser
import com.example.legacymasterliga.domain.model.Player

@Composable
fun PlayerDetailRoute(
    onBack: () -> Unit,
    viewModel: PlayerDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerDetailScreen(
        state = state,
        onBack = onBack,
        onUpdateMarketStatus = viewModel::updateMarketStatus,
        onUpdatePlayerDetails = viewModel::updatePlayerDetails,
        onUpdateAttributesFromText = viewModel::updateAttributesFromText,
        onDeletePlayer = viewModel::deletePlayer,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    state: PlayerDetailUiState,
    onBack: () -> Unit,
    onUpdateMarketStatus: (MarketStatus, Long?) -> Unit,
    onUpdatePlayerDetails: (String, String?, Int?) -> Unit,
    onUpdateAttributesFromText: (String) -> Unit,
    onDeletePlayer: (() -> Unit) -> Unit,
) {
    var showMarketDialog by remember { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showAttributesPasteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ficha do Jogador") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (state.canManage) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Excluir")
                        }
                        IconButton(onClick = { showMarketDialog = true }) {
                            Icon(Icons.Outlined.Storefront, contentDescription = "Mercado")
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val player = state.player
        if (player == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(state.errorMessage ?: "Erro desconhecido.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(player.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${player.position ?: "Sem posição"} • ${player.clubName ?: "Sem clube"}", style = MaterialTheme.typography.titleMedium)
                    
                    Spacer(Modifier.height(8.dp))
                    
                    MarketBadge(player)
                }
            }

            // Native Attributes Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Habilidades (PES6)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (state.canManage) {
                    TextButton(onClick = { showAttributesPasteDialog = true }) {
                        Icon(if (player.attributesRaw == null) Icons.Outlined.ContentPaste else Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (player.attributesRaw == null) "Colar atributos" else "Editar habilidades")
                    }
                }
            }
            
            if (player.attributesRaw != null) {
                AttributesCard(
                    player = player,
                    goalsScored = state.goalsScored,
                    onEdit = { showAttributesPasteDialog = true },
                    canManage = state.canManage
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Analytics, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Nenhum atributo nativo cadastrado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (state.canManage) {
                                Button(onClick = { showAttributesPasteDialog = true }) {
                                    Text("Colar atributos do Bloco de Notas")
                                }
                            }
                        }
                    }
                }
            }

            // Notes
            Text("Observações", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth()) {
                Text(
                    text = player.notes?.takeIf { it.isNotBlank() } ?: "Nenhuma observação registrada.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Footer Info
            Text(
                "ID PES: ${player.externalPlayerId ?: "Não vinculado"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showMarketDialog && state.player != null) {
        MarketStatusDialog(
            player = state.player,
            onDismiss = { showMarketDialog = false },
            onConfirm = { status, price ->
                onUpdateMarketStatus(status, price)
                showMarketDialog = false
            }
        )
    }

    if (showEditDialog && state.player != null) {
        EditPlayerDialog(
            player = state.player,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, position, overall ->
                onUpdatePlayerDetails(name, position, overall)
                showEditDialog = false
            }
        )
    }

    if (showAttributesPasteDialog) {
        AttributesPasteDialog(
            onDismiss = { showAttributesPasteDialog = false },
            onConfirm = { 
                onUpdateAttributesFromText(it)
                showAttributesPasteDialog = false
            },
            errorMessage = state.errorMessage
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Jogador") },
            text = { Text("Tem certeza que deseja remover este jogador do elenco? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = { onDeletePlayer { onBack() } },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlayerDialog(
    player: Player,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Int?) -> Unit
) {
    var name by remember { mutableStateOf(player.name) }
    var position by remember { mutableStateOf(player.position ?: "") }
    var overallText by remember { mutableStateOf(player.overall?.toString() ?: "") }
    val positions = listOf("GOL", "ZAG", "LD", "LE", "VOL", "MC", "MEI", "MD", "ME", "PD", "PE", "SA", "CA")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Jogador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = position,
                            onValueChange = { position = it },
                            label = { Text("Posição") },
                            readOnly = false,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            positions.forEach { pos ->
                                DropdownMenuItem(
                                    text = { Text(pos) },
                                    onClick = {
                                        position = pos
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = overallText,
                        onValueChange = { overallText = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Overall (0-99)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, position.ifBlank { null }, overallText.toIntOrNull()) },
                enabled = name.isNotBlank()
            ) {
                Text("Salvar")
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
private fun AttributesCard(player: Player, goalsScored: Int, onEdit: () -> Unit, canManage: Boolean) {
    val attrs = PlayerAttributesParser.toDisplayList(player.attributesRaw)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1428), RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(player.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, maxLines = 1, modifier = Modifier.weight(1f))
            if (player.overall != null) {
                Text("${player.overall}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 26.sp)
            }
        }
        
        if (goalsScored > 0) {
            Text(
                text = "⚽ Gols marcados: $goalsScored",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(Modifier.height(4.dp))
        if (!player.position.isNullOrBlank()) {
            Text(player.position, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        val half = (attrs.size + 1) / 2
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(attrs.take(half), attrs.drop(half)).forEach { column ->
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    column.forEach { (label, value) ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(label, color = Color(0xFF8CA0B4), fontSize = 11.sp)
                            Text("$value", color = PlayerAttributesParser.colorFor(value), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (canManage) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onEdit) {
                Text("Editar atributos")
            }
        }
    }
}

@Composable
private fun AttributesPasteDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit, errorMessage: String? = null) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Colar Atributos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cole o bloco de atributos do Bloco de Notas (ex: Attack: 80, Defence: 70...).", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    placeholder = { Text("ATTACK: 80\nDEFENCE: 75...") },
                    minLines = 8
                )
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Processar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun MarketBadge(player: Player) {
    val status = player.marketStatus
    val text = when (status) {
        MarketStatus.FOR_SALE -> "À VENDA: ${player.askingPriceCr ?: 0} CR"
        MarketStatus.NEGOTIABLE -> "NEGOCIÁVEL"
        MarketStatus.NOT_FOR_SALE -> "INEGOCIÁVEL"
        MarketStatus.NOT_LISTED -> "FORA DO MERCADO"
    }
    
    val color = when (status) {
        MarketStatus.FOR_SALE -> MaterialTheme.colorScheme.primary
        MarketStatus.NEGOTIABLE -> Color(0xFFF9A825)
        MarketStatus.NOT_FOR_SALE -> MaterialTheme.colorScheme.error
        MarketStatus.NOT_LISTED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
private fun MarketStatusDialog(
    player: Player,
    onDismiss: () -> Unit,
    onConfirm: (MarketStatus, Long?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(player.marketStatus) }
    var priceText by remember { mutableStateOf(player.askingPriceCr?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anunciar Jogador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(player.name, fontWeight = FontWeight.Bold)
                
                Text("Status de Mercado", style = MaterialTheme.typography.labelMedium)
                Column {
                    MarketOption("Não anunciado", selectedStatus == MarketStatus.NOT_LISTED) { selectedStatus = MarketStatus.NOT_LISTED }
                    MarketOption("Negociável", selectedStatus == MarketStatus.NEGOTIABLE) { selectedStatus = MarketStatus.NEGOTIABLE }
                    MarketOption("À Venda (com preço)", selectedStatus == MarketStatus.FOR_SALE) { selectedStatus = MarketStatus.FOR_SALE }
                    MarketOption("Inegociável", selectedStatus == MarketStatus.NOT_FOR_SALE) { selectedStatus = MarketStatus.NOT_FOR_SALE }
                }
                
                if (selectedStatus == MarketStatus.FOR_SALE) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Preço pedido (CR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val price = if (selectedStatus == MarketStatus.FOR_SALE) priceText.toLongOrNull() ?: 0L else null
                    onConfirm(selectedStatus, price) 
                },
                enabled = selectedStatus != MarketStatus.FOR_SALE || priceText.isNotBlank()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun MarketOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
