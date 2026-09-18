package com.example.legacymasterliga.feature.online.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnlineIdentityDialog(
    onDismiss: () -> Unit,
    viewModel: OnlineAuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var password by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conectar Conta Online") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.isOnline) {
                    Text("Conectado como:", fontWeight = FontWeight.Bold)
                    Text(state.profile?.username ?: "Usuário")
                    Text("UID: ${state.profile?.firebaseUid}", style = MaterialTheme.typography.labelSmall)
                    Text("Cargo Cloud: ${state.profile?.role}", color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("Vincule sua liga local ao Firebase para backup e sincronização em tempo real.")
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text("E-mail") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha Firebase") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (state.error != null) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            if (state.isOnline) {
                TextButton(onClick = { viewModel.disconnect() }) { Text("Desconectar") }
            } else {
                Button(
                    onClick = { viewModel.connect(password) },
                    enabled = !state.isConnecting && state.email.contains("@") && password.length >= 6
                ) {
                    Text(if (state.isConnecting) "Conectando..." else "Conectar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}
