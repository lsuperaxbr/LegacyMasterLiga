package com.example.legacymasterliga.feature.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.ui.components.ClubCrest
import com.example.legacymasterliga.feature.history.domain.HistoricalSeason
import com.example.legacymasterliga.feature.history.domain.HistoricalStanding
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryRoute(
    onBack: () -> Unit,
    onOpenClub: (Long, Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectCompetition = viewModel::selectCompetition,
        onSelectSeason = viewModel::selectSeason,
        onOpenClub = onOpenClub,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectCompetition: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
    onOpenClub: (Long, Long) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Histórico") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Voltar") } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                end = 16.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Temporadas e competições", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Consulte campeões, vices, classificação final e estatísticas históricas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.leagues, key = { it.id }) { league ->
                        AssistChip(
                            onClick = { onSelectLeague(league.id) },
                            label = { Text(league.name) },
                            leadingIcon = if (league.id == state.selectedLeagueId) ({ Icon(Icons.Outlined.History, null) }) else null,
                        )
                    }
                }
            }
            item { OverviewCard(state) }
            if (state.seasons.isNotEmpty()) {
                item {
                    val competitions = state.seasons.distinctBy { it.competitionId }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(competitions, key = { it.competitionId }) { season ->
                            FilterChip(
                                selected = season.competitionId == state.selectedCompetitionId,
                                onClick = { onSelectCompetition(season.competitionId) },
                                label = { Text(season.competitionName) },
                            )
                        }
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.visibleSeasons, key = { it.seasonId }) { season ->
                            FilterChip(
                                selected = season.seasonId == state.selectedSeasonId,
                                onClick = { onSelectSeason(season.seasonId) },
                                label = { Text(season.seasonName) },
                            )
                        }
                    }
                }
            }
            state.selectedSeason?.let { season ->
                item { SeasonSummaryCard(season) }
            }
            if (state.finalTable.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            if (state.selectedSeason == null) "Nenhuma temporada disponível." else "A classificação aparecerá após o lançamento dos resultados.",
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                item { Text("Classificação final", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(state.finalTable, key = { it.clubId }) { row ->
                    HistoricalStandingCard(row, state.selectedSeasonId ?: 0L, onOpenClub)
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(state: HistoryUiState) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo histórico", fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Temporadas: ${state.overview.seasonCount}")
                Text("Encerradas: ${state.overview.finishedSeasonCount}")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Competições: ${state.overview.competitionCount}")
                Text("Partidas: ${state.overview.completedMatchCount}")
            }
            Text("Gols registrados: ${state.overview.totalGoals}")
        }
    }
}

@Composable
private fun SeasonSummaryCard(season: HistoricalSeason) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${season.competitionName} • ${season.seasonName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Status: ${season.status.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("🏆 Campeão: ${season.championClubName ?: "Ainda não definido"}", fontWeight = FontWeight.SemiBold)
            Text("🥈 Vice: ${season.runnerUpClubName ?: "Ainda não definido"}")
            Text("${season.participantCount} clubes • ${season.completedMatchCount} partidas • ${season.totalGoals} gols")
            val range = listOfNotNull(season.startedAt?.let(::formatDate), season.finishedAt?.let(::formatDate)).joinToString(" — ")
            if (range.isNotBlank()) Text(range, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HistoricalStandingCard(
    row: HistoricalStanding,
    seasonId: Long,
    onOpenClub: (Long, Long) -> Unit,
) {
    val leaderColor = Color(0xFFFFD54F)
    Card(
        onClick = { onOpenClub(row.clubId, seasonId) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (row.position == 1) leaderColor.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${if (row.position == 1) "👑 " else ""}${row.position}º", fontWeight = FontWeight.Bold)
                    ClubCrest(row.clubName, row.shieldUri, Modifier.size(24.dp))
                    Text(row.clubName, fontWeight = FontWeight.Bold)
                }
                Text("${row.points} pts", fontWeight = FontWeight.Bold)
            }
            Text("J ${row.played}  V ${row.wins}  E ${row.draws}  D ${row.losses}")
            Text("GP ${row.goalsFor}  GC ${row.goalsAgainst}  SG ${signed(row.goalDifference)}  APR ${String.format(Locale.getDefault(), "%.1f%%", row.performance)}")
        }
    }
}

private fun signed(value: Int): String = if (value > 0) "+$value" else value.toString()
private fun formatDate(timestamp: Long): String = DateFormat.getDateInstance(DateFormat.SHORT).format(Date(timestamp))
