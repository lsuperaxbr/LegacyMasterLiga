package com.example.legacymasterliga.feature.cup.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CupPrizeConfigRoute(
    onBack: () -> Unit,
    viewModel: CupPrizeConfigViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CupPrizeConfigScreen(
        state = state,
        onBack = onBack,
        onSave = viewModel::savePrizes,
        onFeedbackConsumed = viewModel::clearFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupPrizeConfigScreen(
    state: CupPrizeConfigUiState,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onFeedbackConsumed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    var champ by remember(state.isLoading) { mutableStateOf(state.championCr) }
    var vice by remember(state.isLoading) { mutableStateOf(state.runnerUpCr) }
    var part by remember(state.isLoading) { mutableStateOf(state.participationCr) }

    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            snackbarHostState.showSnackbar(it)
            onFeedbackConsumed()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Premiação da Copa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Configure os valores em CR para esta competição. Os prêmios são pagos automaticamente ao encerrar a Final.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = champ,
                onValueChange = { champ = it.filter { c -> c.isDigit() } },
                label = { Text("Prêmio do Campeão (CR)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = vice,
                onValueChange = { vice = it.filter { c -> c.isDigit() } },
                label = { Text("Prêmio do Vice-Campeão (CR)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = part,
                onValueChange = { part = it.filter { c -> c.isDigit() } },
                label = { Text("Prêmio de Participação (CR por clube)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onSave(champ, vice, part) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("SALVAR PREMIAÇÃO")
            }
        }
    }
}
