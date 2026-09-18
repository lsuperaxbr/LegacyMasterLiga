package com.example.legacymasterliga.feature.halloffame.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Scoreboard
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.ui.components.ClubCrest
import com.example.legacymasterliga.feature.halloffame.domain.BiggestWinRecord
import com.example.legacymasterliga.feature.halloffame.domain.ClubRecord
import com.example.legacymasterliga.feature.halloffame.domain.UnbeatenStreakRecord
import androidx.compose.ui.graphics.Color

@Composable
fun HallOfFameRoute(
    onBack: () -> Unit,
    onOpenClub: (Long, Long?) -> Unit,
    viewModel: HallOfFameViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HallOfFameScreen(
        state = state,
        onBack = onBack,
        onSelectLeague = viewModel::selectLeague,
        onOpenClub = onOpenClub,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HallOfFameScreen(
    state: HallOfFameUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onOpenClub: (Long, Long?) -> Unit,
) {
    val records = state.records
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hall da Fama") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
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
                Text("Recordes históricos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Os maiores feitos registrados em todas as temporadas da liga.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.leagues, key = { it.id }) { league ->
                        AssistChip(
                            onClick = { onSelectLeague(league.id) },
                            label = { Text(league.name) },
                            leadingIcon = if (league.id == state.selectedLeagueId) {
                                { Icon(Icons.Outlined.WorkspacePremium, contentDescription = null) }
                            } else null,
                        )
                    }
                }
            }

            item {
                ClubRecordCard(
                    title = "Maior campeão",
                    icon = Icons.Outlined.EmojiEvents,
                    record = records.topChampion,
                    emptyMessage = "Nenhuma temporada encerrada.",
                    onOpenClub = onOpenClub,
                )
            }
            item {
                ClubRecordCard(
                    title = "Mais vitórias",
                    icon = Icons.Outlined.MilitaryTech,
                    record = records.mostWins,
                    emptyMessage = "Ainda não há vitórias registradas.",
                    onOpenClub = onOpenClub,
                )
            }
            item {
                ClubRecordCard(
                    title = "Melhor ataque",
                    icon = Icons.Outlined.SportsSoccer,
                    record = records.bestAttack,
                    emptyMessage = "Ainda não há gols registrados.",
                    onOpenClub = onOpenClub,
                )
            }
            item {
                ClubRecordCard(
                    title = "Melhor defesa",
                    icon = Icons.Outlined.GppGood,
                    record = records.bestDefense,
                    emptyMessage = "Conclua uma temporada para definir o recorde.",
                    onOpenClub = onOpenClub,
                )
            }
            item {
                BiggestWinCard(records.biggestWin, onOpenClub)
            }
            item {
                UnbeatenCard(records.longestUnbeaten, onOpenClub)
            }
            item {
                ClubRecordCard(
                    title = "Maior movimentação em CR",
                    icon = Icons.Outlined.Paid,
                    record = records.mostCrMovement,
                    emptyMessage = "Ainda não há transferências registradas.",
                    onOpenClub = onOpenClub,
                )
            }
        }
    }
}

@Composable
private fun ClubRecordCard(
    title: String,
    icon: ImageVector,
    record: ClubRecord?,
    emptyMessage: String,
    onOpenClub: (Long, Long?) -> Unit,
) {
    Card(
        onClick = { record?.let { onOpenClub(it.clubId, it.seasonId) } },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (record == null) {
                    Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClubCrest(record.clubName, record.crestUri, Modifier.size(24.dp))
                        Text(record.clubName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text(record.detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun BiggestWinCard(
    record: BiggestWinRecord?,
    onOpenClub: (Long, Long?) -> Unit,
) {
    Card(
        onClick = { record?.let { onOpenClub(it.winnerClubId, it.seasonId) } },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Outlined.Scoreboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Maior goleada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (record == null) {
                    Text("Ainda não há resultados registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(record.score, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Diferença de ${record.goalDifference} gols • ${record.competitionName} • ${record.seasonName} • Rodada ${record.roundNumber}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun UnbeatenCard(
    record: UnbeatenStreakRecord?,
    onOpenClub: (Long, Long?) -> Unit,
) {
    Card(
        onClick = { record?.let { onOpenClub(it.clubId, it.seasonId) } },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Maior sequência invicta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (record == null) {
                    Text("Ainda não há sequência registrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClubCrest(record.clubName, record.crestUri, Modifier.size(24.dp))
                        Text(record.clubName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "${record.matches} jogos • ${record.competitionName} • ${record.seasonName}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
