package com.example.legacymasterliga.feature.results.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.ui.components.ClubCrest
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.feature.results.domain.Standing
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import java.util.Locale

@Composable
fun StandingsRoute(
    onBack: () -> Unit,
    onOpenClub: (Long, Long) -> Unit,
    onOpenTopScorers: () -> Unit,
    viewModel: StandingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StandingsScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onSelectSeason = viewModel::selectSeason,
        onOpenClub = { clubId -> state.selectedSeasonId?.let { onOpenClub(clubId, it) } },
        onOpenTopScorers = onOpenTopScorers,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StandingsScreen(
    state: StandingsUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
    onOpenClub: (Long) -> Unit,
    onOpenTopScorers: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Classificação") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTopScorers) {
                        Icon(Icons.Outlined.EmojiEvents, contentDescription = "Artilharia")
                    }
                }
            )
        },
    ) { innerPadding ->
        val horizontalScrollState = rememberScrollState()
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                Column(Modifier.padding(16.dp)) {
                    SelectorSection(
                        leagues = state.leagues,
                        selectedLeagueId = state.selectedLeagueId,
                        seasons = state.seasons,
                        selectedSeasonId = state.selectedSeasonId,
                        onSelectLeague = onSelectLeague,
                        onSelectSeason = onSelectSeason,
                    )
                }
            }
            if (state.standings.isEmpty()) {
                item {
                    Text(
                        "A classificação aparecerá após o primeiro resultado lançado.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                val hasGroups = state.standings.any { it.groupIndex != null }
                if (hasGroups) {
                    val groups = state.standings.groupBy { it.groupIndex }
                    groups.forEach { (groupIndex, groupStandings) ->
                        if (groupIndex != null) {
                            stickyHeader {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        "Grupo ${(groupIndex + 'A'.code).toChar()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        stickyHeader {
                            TableHeader(horizontalScrollState)
                        }
                        items(groupStandings, key = { "st_${it.clubId}-${it.groupIndex}" }) { standing ->
                            StandingRow(standing, state.highlightLeader, horizontalScrollState, onOpenClub)
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                } else {
                    stickyHeader {
                        TableHeader(horizontalScrollState)
                    }
                    items(state.standings, key = { "st_${it.clubId}" }) { standing ->
                        StandingRow(standing, state.highlightLeader, horizontalScrollState, onOpenClub)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorSection(
    leagues: List<League>,
    selectedLeagueId: Long?,
    seasons: List<ScheduleSeasonOption>,
    selectedSeasonId: Long?,
    onSelectLeague: (Long) -> Unit,
    onSelectSeason: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Liga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        leagues.forEach { league ->
            SelectButton(league.id == selectedLeagueId, league.name) { onSelectLeague(league.id) }
        }
        Text("Competição e temporada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        seasons.forEach { season ->
            SelectButton(
                season.seasonId == selectedSeasonId,
                "${season.competitionName} • ${season.seasonName}",
            ) { onSelectSeason(season.seasonId) }
        }
    }
}

@Composable
private fun SelectButton(selected: Boolean, text: String, onClick: () -> Unit) {
    if (selected) Button(onClick, Modifier.fillMaxWidth()) { Text(text) }
    else OutlinedButton(onClick, Modifier.fillMaxWidth()) { Text(text) }
}

@Composable
private fun TableHeader(scrollState: androidx.compose.foundation.ScrollState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Parte Fixa: POS | Clube | PTS | J
            Row(Modifier.padding(start = 10.dp)) {
                Cell("#", 28)
                Cell("Clube", 140)
                Cell("PTS", 38, bold = true)
                Cell("J", 30)
            }
            // Parte com Rolagem: V | E | D | GP | GC | SG | %
            Row(
                Modifier
                    .horizontalScroll(scrollState)
                    .padding(end = 10.dp)
            ) {
                Cell("V", 30)
                Cell("E", 30)
                Cell("D", 30)
                Cell("GP", 36)
                Cell("GC", 36)
                Cell("SG", 38)
                Cell("APR", 54)
            }
        }
    }
}

@Composable
private fun StandingRow(
    standing: Standing,
    highlightLeader: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    onOpenClub: (Long) -> Unit
) {
    val leaderColor = Color(0xFFFFD54F)
    val isHighlightedLeader = standing.position == 1 && highlightLeader
    val background = if (isHighlightedLeader) leaderColor.copy(alpha = 0.28f) else Color.Transparent
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenClub(standing.clubId) },
        color = background
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Parte Fixa: POS | Clube | PTS | J
            Row(Modifier.padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Cell(if (isHighlightedLeader) "👑1" else standing.position.toString(), 28, isHighlightedLeader)
                Row(Modifier.width(140.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ClubCrest(standing.clubName, standing.shieldUri, Modifier.size(24.dp))
                    Text(
                        standing.clubName,
                        fontWeight = if (isHighlightedLeader) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Cell(standing.points.toString(), 38, true)
                Cell(standing.played.toString(), 30)
            }
            // Parte com Rolagem: V | E | D | GP | GC | SG | %
            Row(
                Modifier
                    .horizontalScroll(scrollState)
                    .padding(end = 10.dp)
            ) {
                Cell(standing.wins.toString(), 30)
                Cell(standing.draws.toString(), 30)
                Cell(standing.losses.toString(), 30)
                Cell(standing.goalsFor.toString(), 36)
                Cell(standing.goalsAgainst.toString(), 36)
                Cell(if (standing.goalDifference > 0) "+${standing.goalDifference}" else standing.goalDifference.toString(), 38)
                Cell(String.format(Locale("pt", "BR"), "%.0f%%", standing.performancePercentage), 54)
            }
        }
    }
}

@Composable
private fun Cell(text: String, width: Int, bold: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.width(width.dp),
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        maxLines = 1,
        style = MaterialTheme.typography.bodyMedium
    )
}
