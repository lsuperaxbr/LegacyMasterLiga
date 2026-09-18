package com.example.legacymasterliga.feature.hubs.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.navigation.LegacyMenuSection
import com.example.legacymasterliga.core.navigation.LegacyNavigationContext
import com.example.legacymasterliga.core.navigation.LegacyNavigationMenu
import com.example.legacymasterliga.core.ui.components.LegacyNavigationGrid
import com.example.legacymasterliga.ui.theme.LegacySpacing

@Composable
fun LeagueHubScreen(
    state: LeagueHubUiState,
    context: LegacyNavigationContext,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: LeagueHubViewModel = hiltViewModel(),
) {
    // Prefer resolved context from state (summary) over route parameters if IDs are zero
    val effectiveContext = if (context.leagueId > 0) context else state.context
    
    NavigationHubScreen(
        title = state.leagueName,
        subtitle = "Gerenciamento completo da sua Liga.",
        sections = LegacyNavigationMenu.league(state.role, effectiveContext, state.hasActiveSeason),
        onBack = onBack,
        onOpen = onOpen,
        actions = {
            IconButton(onClick = viewModel::forceRefresh) {
                Icon(Icons.Outlined.Sync, contentDescription = "Sincronizar")
            }
        }
    )
}

@Composable
fun CentralHubScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    NavigationHubScreen(
        title = "Central",
        subtitle = "As novidades da Master Liga sem repetir conteúdo no Início.",
        sections = LegacyNavigationMenu.central(),
        onBack = onBack,
        onOpen = onOpen,
    )
}

@Composable
fun HistoryHubScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    NavigationHubScreen(
        title = "História",
        subtitle = "Temporadas, campeões e recordes permanentes.",
        sections = LegacyNavigationMenu.history(),
        onBack = onBack,
        onOpen = onOpen,
    )
}

@Composable
fun MoreHubScreen(
    role: UserRole?,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) {
    NavigationHubScreen(
        title = "Mais",
        subtitle = "Gestão e ferramentas técnicas fora da experiência esportiva.",
        sections = LegacyNavigationMenu.more(role),
        onBack = onBack,
        onOpen = onOpen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationHubScreen(
    title: String,
    subtitle: String,
    sections: List<LegacyMenuSection>,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = actions,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(LegacySpacing.lg),
            verticalArrangement = Arrangement.spacedBy(LegacySpacing.lg),
        ) {
            item(key = "description") {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            sections.forEach { section ->
                item(key = "title-${section.title}") {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                item(key = "grid-${section.title}") {
                    LegacyNavigationGrid(
                        items = section.items,
                        onOpen = onOpen,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
