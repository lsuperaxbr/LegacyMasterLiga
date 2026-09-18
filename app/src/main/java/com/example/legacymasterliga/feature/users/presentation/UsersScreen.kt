package com.example.legacymasterliga.feature.users.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.User

@Composable
fun UsersRoute(onBack: () -> Unit, viewModel: UsersViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    UsersScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSave = viewModel::saveUser,
        onFeedbackConsumed = viewModel::clearFeedback,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    state: UsersUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSave: (Long?, String, String, String, UserRole, AccountStatus, Long?) -> Unit,
    onFeedbackConsumed: () -> Unit,
) {
    var editing by remember { mutableStateOf<User?>(null) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.feedback) {
        val item = state.feedback ?: return@LaunchedEffect
        snackbar.showSnackbar(when (item) {
            is UserFeedback.Success -> item.message
            is UserFeedback.Error -> item.message
        })
        if (item is UserFeedback.Success) { showDialog = false; editing = null }
        onFeedbackConsumed()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Usuários e permissões") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Voltar") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                Icon(Icons.Outlined.Add, "Criar usuário")
            }
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, inner.calculateTopPadding() + 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Liga para associação de Presidentes", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.leagues.forEach { league ->
                        AssistChip(onClick = { onSelectLeague(league.id) }, label = { Text(league.name) })
                    }
                }
            }
            items(state.users, key = { it.id }) { user ->
                val clubName = state.clubs.firstOrNull { it.presidentUserId == user.id }?.name
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Outlined.ManageAccounts, null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${user.displayName} (@${user.username})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(roleLabel(user.role))
                            Text("${statusLabel(user.status)}${clubName?.let { " • $it" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { editing = user; showDialog = true }) {
                            Icon(Icons.Outlined.Edit, "Editar ${user.displayName}")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        UserEditorDialog(
            user = editing,
            clubs = state.clubs,
            currentClubId = state.clubs.firstOrNull { it.presidentUserId == editing?.id }?.id,
            onDismiss = { showDialog = false; editing = null },
            onSave = onSave,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserEditorDialog(
    user: User?,
    clubs: List<Club>,
    currentClubId: Long?,
    onDismiss: () -> Unit,
    onSave: (Long?, String, String, String, UserRole, AccountStatus, Long?) -> Unit,
) {
    var username by rememberSaveable(user?.id) { mutableStateOf(user?.username.orEmpty()) }
    var displayName by rememberSaveable(user?.id) { mutableStateOf(user?.displayName.orEmpty()) }
    var password by rememberSaveable(user?.id) { mutableStateOf("") }
    var role by rememberSaveable(user?.id) { mutableStateOf(user?.role ?: UserRole.VISITOR) }
    var status by rememberSaveable(user?.id) { mutableStateOf(user?.status ?: AccountStatus.ACTIVE) }
    var clubId by rememberSaveable(user?.id) { mutableStateOf(currentClubId) }
    var roleOpen by remember { mutableStateOf(false) }
    var statusOpen by remember { mutableStateOf(false) }
    var clubOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Novo usuário" else "Editar usuário") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(username, { username = it }, label = { Text("Usuário") }, enabled = user == null, singleLine = true)
                OutlinedTextField(displayName, { displayName = it }, label = { Text("Nome de exibição") }, singleLine = true)
                OutlinedTextField(password, { password = it }, label = { Text(if (user == null) "Senha inicial" else "Nova senha (opcional)") }, singleLine = true)
                EnumSelector("Perfil", roleLabel(role), roleOpen, { roleOpen = it }) {
                    UserRole.entries.forEach { item -> DropdownMenuItem(text = { Text(roleLabel(item)) }, onClick = { role = item; roleOpen = false }) }
                }
                EnumSelector("Status", statusLabel(status), statusOpen, { statusOpen = it }) {
                    AccountStatus.entries.forEach { item -> DropdownMenuItem(text = { Text(statusLabel(item)) }, onClick = { status = item; statusOpen = false }) }
                }
                EnumSelector("Clube", clubs.firstOrNull { it.id == clubId }?.name ?: "Sem clube", clubOpen, { clubOpen = it }) {
                    DropdownMenuItem(text = { Text("Sem clube") }, onClick = { clubId = null; clubOpen = false })
                    clubs.filter { it.isActive }.forEach { club ->
                        DropdownMenuItem(text = { Text(club.name) }, onClick = { clubId = club.id; clubOpen = false })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(user?.id, username, displayName, password, role, status, clubId) }) { Text("Salvar") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumSelector(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) { content() }
    }
}

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.ADMINISTRATOR -> "Administrador"
    UserRole.PRESIDENT -> "Presidente"
    UserRole.VISITOR -> "Visitante"
}

private fun statusLabel(status: AccountStatus): String = when (status) {
    AccountStatus.ACTIVE -> "Ativo"
    AccountStatus.DISABLED -> "Desativado"
    AccountStatus.BLOCKED -> "Bloqueado"
}
