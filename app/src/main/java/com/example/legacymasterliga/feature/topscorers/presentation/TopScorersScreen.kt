package com.example.legacymasterliga.feature.topscorers.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.database.dao.TopScorerRow

@Composable
fun TopScorersRoute(
    onBack: () -> Unit,
    viewModel: TopScorersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TopScorersScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectCompetition = viewModel::selectCompetition
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopScorersScreen(
    state: TopScorersUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectCompetition: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Artilharia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Liga", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.leagues.forEach { league ->
                            FilterChip(
                                selected = state.selectedLeagueId == league.id,
                                onClick = { onSelectLeague(league.id) },
                                label = { Text(league.name) }
                            )
                        }
                    }
                    Text("Competição", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.competitions.forEach { comp ->
                            FilterChip(
                                selected = state.selectedCompetitionId == comp.competitionId,
                                onClick = { onSelectCompetition(comp.competitionId) },
                                label = { Text(comp.competitionName) }
                            )
                        }
                    }
                }
            }

            if (state.topScorers.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum gol registrado para esta competição.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                itemsIndexed(state.topScorers, key = { _, row -> row.playerId }) { index, scorer ->
                    ScorerCard(index + 1, scorer)
                }
            }
        }
    }
}

@Composable
private fun ScorerCard(position: Int, scorer: TopScorerRow) {
    val isLeader = position == 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isLeader) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) 
                 else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                if (isLeader) {
                    Icon(Icons.Outlined.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                } else {
                    Text(text = "$position", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Column(Modifier.weight(1f)) {
                Text(scorer.playerName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                Text(scorer.clubName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${scorer.goals}",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = if (isLeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Icon(Icons.Outlined.SportsSoccer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
