package com.example.legacymasterliga.feature.online.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CloudLeagueDialog(
    localLeagueId: Long?,
    onDismiss: () -> Unit,
    viewModel: CloudLeagueViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var inviteCodeInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    var showConnectDialog by remember { mutableStateOf(false) }

    if (showConnectDialog) {
        OnlineIdentityDialog(onDismiss = { showConnectDialog = false })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Groups, contentDescription = null)
                Text("Liga Online")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!state.isOnline) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Acesso Restrito", fontWeight = FontWeight.Bold)
                            Text("Você precisa conectar sua conta online para gerenciar ou participar de ligas na nuvem.")
                            Button(
                                onClick = { showConnectDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CONECTAR AGORA")
                            }
                        }
                    }
                } else {
                    if (state.pendingSyncCount > 0) {
                        Text("${state.pendingSyncCount} alteração(ões) aguardando conexão", color = MaterialTheme.colorScheme.tertiary)
                    }
                    if (state.conflictCount > 0) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${state.conflictCount} conflito(s) de placar", fontWeight = FontWeight.Bold)
                                Text("Nenhum placar foi sobrescrito. Confira os resultados e escolha a versão da nuvem para resolver com segurança.")
                                Button(onClick = viewModel::acceptRemoteConflicts) { Text("Usar placares da nuvem") }
                            }
                        }
                    }
                    if (state.cloudLeague == null) {
                        Text("Esta liga ainda é local. Promova-a para a nuvem para convidar outros presidentes.")
                        if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        
                        OutlinedTextField(
                            value = inviteCodeInput,
                            onValueChange = { inviteCodeInput = it.take(6) },
                            label = { Text("Código de Convite") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Outlined.VpnKey, contentDescription = null) }
                        )
                        Button(
                            onClick = { viewModel.join(inviteCodeInput) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = inviteCodeInput.length == 6 && !state.isLoading
                        ) {
                            if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Entrar em Liga Existente")
                        }
                        
                        HorizontalDivider()

                        Button(
                            onClick = { localLeagueId?.let { viewModel.promote(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = localLeagueId != null && !state.isLoading
                        ) {
                            Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Promover Liga Atual")
                        }
                    } else {
                        Text("Liga: ${state.cloudLeague!!.name}", fontWeight = FontWeight.Bold)
                        Text("ID Nuvem: ${state.cloudLeague!!.cloudLeagueId}", style = MaterialTheme.typography.labelSmall)
                        
                        HorizontalDivider()
                        
                        Text("Membros (${state.members.size})", fontWeight = FontWeight.Bold)
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(state.members) { member ->
                                ListItem(
                                    headlineContent = { Text(member.displayName) },
                                    overlineContent = { Text(member.role.name) }
                                )
                            }
                        }
                        
                        HorizontalDivider()
                        
                        if (state.activeInvite != null) {
                            val invite = state.activeInvite!!
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CONVITE ATIVO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = invite.inviteCode,
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Válido para cargos de PRESIDENTE",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        text = if (invite.expiresAt == null) "Validade: Permanente" 
                                               else "Expira em: " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(invite.expiresAt)),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Usos: ${invite.uses} / ${invite.maxUses}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = { clipboardManager.setText(AnnotatedString(invite.inviteCode)) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Copiar Código")
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.generateInvite(state.cloudLeague!!.cloudLeagueId) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isLoading
                            ) {
                                if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                else {
                                    Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Gerar Convite")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
