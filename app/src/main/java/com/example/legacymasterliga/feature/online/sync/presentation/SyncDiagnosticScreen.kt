package com.example.legacymasterliga.feature.online.sync.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.BuildConfig
import com.example.legacymasterliga.core.database.dao.OnlineSyncDao
import com.example.legacymasterliga.core.database.entity.OnlineSyncQueueEntity
import com.example.legacymasterliga.feature.online.sync.OnlineSportsSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SyncDiagnosticViewModel @Inject constructor(
    private val syncManager: OnlineSportsSyncManager,
    private val syncDao: OnlineSyncDao
) : ViewModel() {
    val pendingQueue = syncDao.observePendingQueue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getInfo() = syncManager.getDiagnosticInfo()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDiagnosticScreen(
    onBack: () -> Unit,
    viewModel: SyncDiagnosticViewModel = hiltViewModel()
) {
    val queue by viewModel.pendingQueue.collectAsStateWithLifecycle()
    var info by remember { mutableStateOf(viewModel.getInfo()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            info = viewModel.getInfo()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico de Sync") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DiagnosticCard("Versão") {
                    Text("Versão: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                }
            }

            item {
                DiagnosticCard("Sessão") {
                    Text("UID: ${info["uid"]}", style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                DiagnosticCard("Memberships Ativas") {
                    val memberships = info["memberships"] as? List<String> ?: emptyList()
                    if (memberships.isEmpty()) {
                        Text("Nenhuma liga encontrada no cache ou servidor.", color = MaterialTheme.colorScheme.error)
                    } else {
                        memberships.forEach { 
                            Text("• $it", fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                DiagnosticCard("Fila PENDING (${queue.size} itens)") {
                    if (queue.isEmpty()) {
                        Text("Fila vazia.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        queue.take(10).forEach { item ->
                            Text("${item.entityType} | ID: ${item.localId} | Liga: ${item.cloudLeagueId.take(5)}...", fontSize = 11.sp)
                        }
                        if (queue.size > 10) {
                            Text("... e mais ${queue.size - 10} itens", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("Últimos Logs (OnlineSportsSync)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
            }

            val logs = info["logs"] as? List<String> ?: emptyList()
            items(logs) { log ->
                Text(
                    text = log,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DiagnosticCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
            content()
        }
    }
}
