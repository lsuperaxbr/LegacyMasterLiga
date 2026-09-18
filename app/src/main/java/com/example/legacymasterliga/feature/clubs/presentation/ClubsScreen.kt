package com.example.legacymasterliga.feature.clubs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.ui.components.ClubCrest
import com.example.legacymasterliga.core.ui.components.NativeCrest
import com.example.legacymasterliga.domain.ClubCrestParser

@Composable
fun ClubsRoute(
    onBack: () -> Unit,
    onOpenProfile: (Long) -> Unit,
    viewModel: ClubsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ClubsScreen(
        state = state,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        readOnly = state.user?.role == UserRole.VISITOR,
        currentUserId = state.currentUserId,
        isAdmin = state.isAdmin,
        onSelectLeague = viewModel::selectLeague,
        onSaveClub = viewModel::saveClub,
        onBulkImportClubs = viewModel::bulkCreateClubs,
        onDeleteClub = viewModel::deleteClub,
        onSetClubActive = viewModel::setClubActive,
        onFeedbackConsumed = viewModel::clearFeedback,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(
    state: ClubsUiState,
    onBack: () -> Unit,
    onOpenProfile: (Long) -> Unit,
    readOnly: Boolean,
    currentUserId: Long?,
    isAdmin: Boolean,
    onSelectLeague: (Long) -> Unit,
    onSaveClub: (Long?, String, String?, Long?, Long) -> Unit,
    onBulkImportClubs: (List<String>) -> Unit,
    onDeleteClub: (Long) -> Unit,
    onSetClubActive: (Long, Boolean) -> Unit,
    onFeedbackConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingClub by remember { mutableStateOf<Club?>(null) }
    var clubToDelete by remember { mutableStateOf<Club?>(null) }
    var showClubDialog by rememberSaveable { mutableStateOf(false) }
    var showBulkImportDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.feedback) {
        val item = state.feedback ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            when (item) {
                is ClubFeedback.Success -> item.message
                is ClubFeedback.Error -> item.message
            },
        )
        onFeedbackConsumed()
        if (item is ClubFeedback.Success) {
            showClubDialog = false
            showBulkImportDialog = false
            editingClub = null
            clubToDelete = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Clubes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { showBulkImportDialog = true }) {
                            Icon(Icons.Outlined.PlaylistAdd, contentDescription = "Importar vários times")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        editingClub = null
                        showClubDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Criar clube")
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Liga selecionada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LeagueSelector(
                    leagues = state.leagues,
                    selectedLeagueId = state.selectedLeagueId,
                    onSelect = onSelectLeague,
                )
            }

            if (state.selectedLeagueId == null) {
                item { EmptyState("Crie uma liga no módulo Competições antes de cadastrar clubes.") }
            } else if (state.clubs.isEmpty()) {
                item { EmptyState("Nenhum clube cadastrado nesta liga. Use o botão + para criar o primeiro.") }
            } else {
                items(state.clubs, key = { "cl_${it.id}" }) { club ->
                    val canEditThisClub = !readOnly && (isAdmin || club.presidentUserId == currentUserId)
                    ClubCard(
                        club = club,
                        presidentName = state.users.firstOrNull { it.id == club.presidentUserId }?.displayName,
                        onEdit = if (canEditThisClub) ({
                            editingClub = club
                            showClubDialog = true
                        }) else null,
                        onDelete = if (canEditThisClub) ({ clubToDelete = club }) else null,
                        onToggleActive = if (canEditThisClub) ({ onSetClubActive(club.id, !club.isActive) }) else null,
                        onOpenProfile = { onOpenProfile(club.id) },
                    )
                }
            }
        }
    }

    if (showClubDialog && !readOnly) {
        ClubEditorDialog(
            club = editingClub,
            users = state.users.filter { it.status == AccountStatus.ACTIVE },
            onDismiss = {
                showClubDialog = false
                editingClub = null
            },
            onConfirm = { name, crestUri, presidentUserId, initialBalance ->
                onSaveClub(editingClub?.id, name, crestUri, presidentUserId, initialBalance)
            },
        )
    }

    if (showBulkImportDialog && !readOnly) {
        BulkClubImportDialog(
            onDismiss = { showBulkImportDialog = false },
            onConfirm = { names -> onBulkImportClubs(names) },
        )
    }

    if (clubToDelete != null) {
        AlertDialog(
            onDismissRequest = { clubToDelete = null },
            title = { Text("Excluir clube") },
            text = { Text("Deseja excluir o clube ${clubToDelete?.name}? Esta ação não pode ser desfeita e falhará se houver histórico vinculado.") },
            confirmButton = {
                TextButton(onClick = { onDeleteClub(clubToDelete!!.id) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Excluir")
                }
            },
            dismissButton = { TextButton(onClick = { clubToDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun LeagueSelector(
    leagues: List<League>,
    selectedLeagueId: Long?,
    onSelect: (Long) -> Unit,
) {
    if (leagues.isEmpty()) {
        Text("Nenhuma liga disponível.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        leagues.forEach { league ->
            AssistChip(
                onClick = { onSelect(league.id) },
                label = { Text("${league.name} • ${league.currencyCode}") },
                leadingIcon = if (selectedLeagueId == league.id) {
                    { Icon(Icons.Outlined.Groups, contentDescription = null) }
                } else null,
            )
        }
    }
}

@Composable
private fun ClubCard(
    club: Club,
    presidentName: String?,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onToggleActive: (() -> Unit)?,
    onOpenProfile: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (club.isActive) 1f else 0.62f),
        colors = CardDefaults.cardColors(
            containerColor = if (club.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClubCrest(club.name, club.crestUri, modifier = Modifier.size(76.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(club.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    presidentName?.let { "Presidente: $it" } ?: "Sem presidente associado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(if (club.isActive) "Ativo" else "Desativado", style = MaterialTheme.typography.labelLarge)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onOpenProfile) {
                    Icon(Icons.Outlined.Info, contentDescription = "Ver perfil de ${club.name}")
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar ${club.name}")
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Excluir ${club.name}", tint = MaterialTheme.colorScheme.error)
                    }
                }
                if (onToggleActive != null) {
                    IconButton(onClick = onToggleActive) {
                        Icon(
                            if (club.isActive) Icons.Outlined.ToggleOn else Icons.Outlined.ToggleOff,
                            contentDescription = if (club.isActive) "Desativar ${club.name}" else "Ativar ${club.name}",
                            tint = if (club.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkClubImportDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val names = remember(text) { text.lines().map { it.trim() }.filter { it.isNotEmpty() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importar vários times") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Cole um nome de time por linha. Todos serão cadastrados de uma vez na liga selecionada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Um time por linha") },
                    placeholder = { Text("CHAPECOENSE\nVASCO\nCORITIBA...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 12,
                )
                if (names.isNotEmpty()) {
                    Text(
                        "${names.size} time(s) reconhecido(s) na lista.",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(names) },
                enabled = names.isNotEmpty(),
            ) { Text("Cadastrar todos") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubEditorDialog(
    club: Club?,
    users: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Long?, Long) -> Unit,
) {
    var name by rememberSaveable(club?.id) { mutableStateOf(club?.name.orEmpty()) }
    
    // Decodifica o estado inicial
    val initialData = remember(club?.id) { 
        ClubCrestParser.decode(club?.crestUri, name)
    }
    
    var shape by rememberSaveable(club?.id) { mutableStateOf(initialData.shape) }
    var colorFill by rememberSaveable(club?.id) { mutableStateOf(initialData.colorFill) }
    var colorBorder by rememberSaveable(club?.id) { mutableStateOf(initialData.colorBorder) }
    var pattern by rememberSaveable(club?.id) { mutableStateOf(initialData.pattern) }
    var colorPattern by rememberSaveable(club?.id) { mutableStateOf(initialData.colorPattern) }
    var showInitials by rememberSaveable(club?.id) { mutableStateOf(initialData.showInitials) }
    var initials by rememberSaveable(club?.id) { mutableStateOf(initialData.initials) }
    
    var selectedPresidentId by rememberSaveable(club?.id) { mutableStateOf(club?.presidentUserId) }
    var initialBalance by rememberSaveable(club?.id) { mutableStateOf("") }
    var presidentMenuExpanded by remember { mutableStateOf(false) }

    // Preview raw string
    val currentCrestRaw = remember(shape, colorFill, colorBorder, pattern, colorPattern, showInitials, initials) {
        ClubCrestParser.encode(shape, colorFill, colorBorder, pattern, colorPattern, showInitials, initials)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (club == null) "Novo clube" else "Editar clube") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    // Preview e Formato
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        NativeCrest(name.ifBlank { "CL" }, currentCrestRaw, modifier = Modifier.size(80.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Formato", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ClubCrestParser.CrestShape.values().forEach { s ->
                                    FilterChip(
                                        selected = shape == s,
                                        onClick = { shape = s },
                                        label = { 
                                            Text(when(s) {
                                                ClubCrestParser.CrestShape.CIRCLE -> "Círculo"
                                                ClubCrestParser.CrestShape.SHIELD -> "Escudo"
                                                ClubCrestParser.CrestShape.SHIELD_THIN -> "Escudo Fino"
                                                ClubCrestParser.CrestShape.DIAMOND -> "Losango"
                                                ClubCrestParser.CrestShape.OVAL -> "Oval"
                                            })
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Seletor de Padrão
                    Text("Padrão", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ClubCrestParser.CrestPattern.values().forEach { p ->
                            FilterChip(
                                selected = pattern == p,
                                onClick = { pattern = p },
                                label = { 
                                    Text(when(p) {
                                        ClubCrestParser.CrestPattern.NONE -> "Nenhum"
                                        ClubCrestParser.CrestPattern.STRIPES_VERTICAL -> "Listras V"
                                        ClubCrestParser.CrestPattern.STRIPES_HORIZONTAL -> "Listras H"
                                        ClubCrestParser.CrestPattern.DIAGONAL -> "Diagonal"
                                    })
                                }
                            )
                        }
                    }
                }

                item {
                    // Seletor Cor de Fundo
                    Text("Cor de Fundo", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClubCrestParser.AVAILABLE_COLORS.forEach { (hex, label) ->
                            FilterChip(
                                selected = colorFill == hex,
                                onClick = { colorFill = hex },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(android.graphics.Color.parseColor(hex)),
                                    selectedLabelColor = if (hex == "#FFD700" || hex == "#00E6C8" || hex == "#FFFFFF") Color.Black else Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    // Seletor Cor da Borda
                    Text("Cor da Borda", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClubCrestParser.AVAILABLE_COLORS.forEach { (hex, label) ->
                            FilterChip(
                                selected = colorBorder == hex,
                                onClick = { colorBorder = hex },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(android.graphics.Color.parseColor(hex)),
                                    selectedLabelColor = if (hex == "#FFD700" || hex == "#00E6C8" || hex == "#FFFFFF") Color.Black else Color.White
                                )
                            )
                        }
                    }
                }

                if (pattern != ClubCrestParser.CrestPattern.NONE) {
                    item {
                        // Seletor Cor do Padrão
                        Text("Cor do Padrão", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ClubCrestParser.AVAILABLE_COLORS.forEach { (hex, label) ->
                                FilterChip(
                                    selected = colorPattern == hex,
                                    onClick = { colorPattern = hex },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(android.graphics.Color.parseColor(hex)),
                                        selectedLabelColor = if (hex == "#FFD700" || hex == "#00E6C8" || hex == "#FFFFFF") Color.Black else Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    // Toggle Iniciais e Campo de Texto
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Mostrar iniciais", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = showInitials, onCheckedChange = { showInitials = it })
                    }
                }

                if (showInitials) {
                    item {
                        OutlinedTextField(
                            value = initials,
                            onValueChange = { if (it.length <= 3) initials = it.uppercase() },
                            label = { Text("Iniciais (Máx 3)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text(ClubCrestParser.defaultInitials(name)) }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do clube") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (club == null) {
                    item {
                        OutlinedTextField(
                            value = initialBalance,
                            onValueChange = { initialBalance = it.filter { c -> c.isDigit() } },
                            label = { Text("Saldo Inicial (CR)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = presidentMenuExpanded,
                        onExpandedChange = { presidentMenuExpanded = !presidentMenuExpanded },
                    ) {
                        val selectedName = users.firstOrNull { it.id == selectedPresidentId }?.displayName ?: "Sem presidente"
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Presidente") },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presidentMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = presidentMenuExpanded,
                            onDismissRequest = { presidentMenuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sem presidente") },
                                onClick = {
                                    selectedPresidentId = null
                                    presidentMenuExpanded = false
                                },
                            )
                            users.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text("${user.displayName} (@${user.username})") },
                                    onClick = {
                                        selectedPresidentId = user.id
                                        presidentMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val savedInitials = initials.ifBlank { ClubCrestParser.defaultInitials(name) }
                    val finalCrest = ClubCrestParser.encode(shape, colorFill, colorBorder, pattern, colorPattern, showInitials, savedInitials)
                    onConfirm(name, finalCrest, selectedPresidentId, initialBalance.toLongOrNull() ?: 0L) 
                }, 
                enabled = name.trim().length >= 2
            ) {
                Text("Salvar")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun EmptyState(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Groups, contentDescription = null, modifier = Modifier.size(42.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
