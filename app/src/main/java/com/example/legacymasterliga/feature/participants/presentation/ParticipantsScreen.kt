package com.example.legacymasterliga.feature.participants.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.ui.components.ClubCrest

@Composable
fun ParticipantsRoute(onBack: () -> Unit, viewModel: ParticipantsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ParticipantsScreen(state, onBack, viewModel::selectLeague, viewModel::selectSeason, viewModel::toggleClub, viewModel::clearMessage)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsScreen(
    state: ParticipantsUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
    onToggleClub: (com.example.legacymasterliga.feature.participants.domain.ParticipantClub) -> Unit,
    onMessageConsumed: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); onMessageConsumed() }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inscrições e participantes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Liga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.leagues.forEach { league ->
                        AssistChip(onClick = { onSelectLeague(league.id) }, label = { Text(league.name) })
                    }
                }
            }
            item {
                Text("Competição e temporada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.seasons.forEach { season ->
                        AssistChip(
                            onClick = { onSelectSeason(season.id) },
                            label = { Text("${season.competitionName} • ${season.seasonName}") },
                        )
                    }
                    if (state.seasons.isEmpty()) Text("Crie uma competição e uma temporada antes de inscrever clubes.")
                }
            }
            item {
                Text("Clubes disponíveis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Selecione os clubes que disputarão a temporada. Duplicidades são bloqueadas pelo banco.")
            }
            items(state.clubs, key = { it.id }) { club ->
                Card(onClick = { onToggleClub(club) }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ClubCrest(club.name, club.crestUri, modifier = Modifier.size(32.dp))
                            Text(club.name, fontWeight = FontWeight.SemiBold)
                        }
                        Checkbox(checked = club.selected, onCheckedChange = { onToggleClub(club) })
                    }
                }
            }
            if (state.selectedSeasonId != null) {
                item { Text("${state.clubs.count { it.selected }} clube(s) inscrito(s).") }
            }
        }
    }
}
