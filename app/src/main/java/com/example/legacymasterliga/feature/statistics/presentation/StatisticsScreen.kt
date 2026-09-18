package com.example.legacymasterliga.feature.statistics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.ui.components.ClubCrest
import java.util.Locale

@Composable
fun StatisticsRoute(
    onBack: () -> Unit,
    onOpenClub: (Long, Long?) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsScreen(
        state = state,
        onBack = onBack,
        onOpenClub = onOpenClub,
        onSelectLeague = viewModel::selectLeague,
        onSelectCompetition = viewModel::selectCompetition,
        onSelectSeason = viewModel::selectSeason,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onBack: () -> Unit,
    onOpenClub: (Long, Long?) -> Unit,
    onSelectLeague: (Long?) -> Unit,
    onSelectCompetition: (Long?) -> Unit,
    onSelectSeason: (Long?) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Centro de Estatísticas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Liga", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.leagues.take(3).forEach { league ->
                            FilterChip(
                                selected = state.selectedLeagueId == league.id,
                                onClick = { onSelectLeague(league.id) },
                                label = { Text(league.name) },
                            )
                        }
                    }
                    Text("Competição", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.selectedCompetitionId == null,
                            onClick = { onSelectCompetition(null) },
                            label = { Text("Todas") },
                        )
                        state.competitions.take(3).forEach { competition ->
                            FilterChip(
                                selected = state.selectedCompetitionId == competition.id,
                                onClick = { onSelectCompetition(competition.id) },
                                label = { Text(competition.name) },
                            )
                        }
                    }
                    val selectedCompetition = state.competitions.firstOrNull { it.id == state.selectedCompetitionId }
                    if (selectedCompetition != null) {
                        Text("Temporada", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.selectedSeasonId == null,
                                onClick = { onSelectSeason(null) },
                                label = { Text("Todas") },
                            )
                            selectedCompetition.seasons.take(3).forEach { season ->
                                FilterChip(
                                    selected = state.selectedSeasonId == season.id,
                                    onClick = { onSelectSeason(season.id) },
                                    label = { Text(season.name) },
                                )
                            }
                        }
                    }
                }
            }

            item {
                val overview = state.snapshot.overview
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Resumo geral", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${overview.finishedMatches} partidas • ${overview.totalGoals} gols")
                        Text("Média de ${String.format(Locale.getDefault(), "%.2f", overview.averageGoals)} gols por jogo")
                        Text("${overview.totalCrMoved} CR movimentados")
                        Text("${overview.activeClubs} clubes ativos • ${overview.seasonsCount} temporadas")
                    }
                }
            }

            item {
                Text(
                    "Ranking histórico dos clubes",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            itemsIndexed(state.snapshot.clubs, key = { _, item -> item.clubId }) { index, club ->
                Card(
                    onClick = { onOpenClub(club.clubId, state.selectedSeasonId) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${index + 1}º", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            ClubCrest(club.clubName, club.crestUri, Modifier.size(24.dp))
                            Text(club.clubName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("${club.points} pts • ${club.wins} V • ${club.draws} E • ${club.losses} D")
                        Text("GP ${club.goalsFor} • GC ${club.goalsAgainst} • SG ${if (club.goalDifference >= 0) "+" else ""}${club.goalDifference}")
                        Text("Média ${String.format(Locale.getDefault(), "%.2f", club.averagePoints)} pts/jogo • ${club.crMoved} CR movimentados")
                    }
                }
            }
        }
    }
}
