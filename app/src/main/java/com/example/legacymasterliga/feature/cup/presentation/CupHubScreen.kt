package com.example.legacymasterliga.feature.cup.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.legacymasterliga.core.navigation.LegacyDestination
import com.example.legacymasterliga.core.navigation.LegacyMenuIcon
import com.example.legacymasterliga.core.navigation.LegacyMenuItem
import com.example.legacymasterliga.core.ui.components.LegacyNavigationGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupHubScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit
) {
    val items = listOf(
        LegacyMenuItem("Nova Copa", "Criar torneio com sorteio", LegacyMenuIcon.CUP, LegacyDestination.CupSetup.route, highlighted = true),
        LegacyMenuItem("Chaveamento", "Ver bracket e placares", LegacyMenuIcon.ROUNDS, LegacyDestination.CupHub.route), // Will be handled in CupList
        LegacyMenuItem("Premiação", "Configurar prêmios em CR", LegacyMenuIcon.CLOSURE, LegacyDestination.CupHub.route), // Will be handled in CupList
        LegacyMenuItem("Histórico", "Ver copas passadas", LegacyMenuIcon.HISTORY, LegacyDestination.CupHub.route) // Will be handled in CupList
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Central da Copa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Gerencie as competições de mata-mata da sua liga. Sorteie confrontos e defina premiações automáticas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LegacyNavigationGrid(
                items = items,
                onOpen = { route ->
                    // For the list items, we navigate to CupHub which Task 3 will point to CupList
                    // But here we can be more specific if we want. 
                    // To follow the "simplify" hint: all 3 point to CupList and we add gear there.
                    if (route == LegacyDestination.CupSetup.route) {
                        onOpen(route)
                    } else {
                        onOpen("cup_list")
                    }
                }
            )
        }
    }
}
