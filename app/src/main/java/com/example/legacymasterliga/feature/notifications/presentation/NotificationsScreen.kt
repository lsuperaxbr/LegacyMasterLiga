package com.example.legacymasterliga.feature.notifications.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.feature.notifications.domain.InternalNotification
import com.example.legacymasterliga.feature.notifications.domain.NotificationReadFilter
import java.text.DateFormat
import java.util.Date

@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    onOpenDestination: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationsScreen(
        state = state,
        onBack = onBack,
        onOpenDestination = onOpenDestination,
        onRefresh = viewModel::refresh,
        onMarkAllRead = viewModel::markAllRead,
        onLeague = viewModel::setLeague,
        onCategory = viewModel::setCategory,
        onReadFilter = viewModel::setReadFilter,
        onToggleRead = viewModel::toggleRead,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onBack: () -> Unit,
    onOpenDestination: (String) -> Unit,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit,
    onLeague: (Long?) -> Unit,
    onCategory: (String?) -> Unit,
    onReadFilter: (NotificationReadFilter) -> Unit,
    onToggleRead: (InternalNotification) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notificações (${state.unreadCount})") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Voltar") } },
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, "Atualizar") }
                    IconButton(onClick = onMarkAllRead, enabled = state.unreadCount > 0) { Icon(Icons.Outlined.DoneAll, "Marcar todas como lidas") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilterDropdown("Liga", state.leagues.map { it.id to it.name }, state.query.leagueId, onLeague)
            FilterDropdown("Categoria", state.categories.map { it to it }, state.query.category, onCategory)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationReadFilter.entries.forEach { filter ->
                    AssistChip(
                        onClick = { onReadFilter(filter) },
                        label = { Text(filter.label()) },
                        colors = if (state.query.readFilter == filter) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else AssistChipDefaults.assistChipColors(),
                    )
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.notifications.isEmpty()) {
                    item { Text("Nenhuma notificação para os filtros selecionados.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                items(state.notifications, key = { it.id }) { notification ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (!notification.isRead) onToggleRead(notification)
                            notification.destinationRoute?.let(onOpenDestination)
                        },
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(notification.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(if (notification.isRead) "Lida" else "Nova", color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                            Text("${notification.category} • ${notification.priority}", style = MaterialTheme.typography.labelMedium)
                            notification.leagueName?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                            Text(DateFormat.getDateTimeInstance().format(Date(notification.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            AssistChip(onClick = { onToggleRead(notification) }, label = { Text(if (notification.isRead) "Marcar como não lida" else "Marcar como lida") })
                        }
                    }
                }
            }
        }
    }
}

private fun NotificationReadFilter.label(): String = when (this) {
    NotificationReadFilter.ALL -> "Todas"
    NotificationReadFilter.UNREAD -> "Não lidas"
    NotificationReadFilter.READ -> "Lidas"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterDropdown(label: String, options: List<Pair<T, String>>, selected: T?, onSelected: (T?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: "Todos"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = selectedLabel, onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Todos") }, onClick = { onSelected(null); expanded = false })
            options.forEach { (value, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onSelected(value); expanded = false }) }
        }
    }
}
