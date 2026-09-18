package com.example.legacymasterliga.feature.presidentprofile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.feature.presidentprofile.domain.PresidentProfileSnapshot

@Composable
fun PresidentProfileRoute(
    onBack: () -> Unit,
    viewModel: PresidentProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PresidentProfileScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresidentProfileScreen(
    state: PresidentProfileSnapshot,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meu Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Cabeçalho
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.displayName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "@${state.username}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (state.clubs.isEmpty()) "Sem clube no momento" else state.clubs.joinToString(" & "),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Seção Carreira
                item {
                    ProfileSection(title = "Carreira", icon = Icons.Outlined.EmojiEvents) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatItem("Temporadas", state.seasonsPlayed.toString())
                                StatItem("Títulos Liga", state.titles.toString(), highlight = state.titles > 0)
                                StatItem("Títulos Copa", state.cupTitles.toString(), highlight = state.cupTitles > 0)
                            }
                            if (state.titles > 0 || state.cupTitles > 0) {
                                val total = state.titles + state.cupTitles
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Outlined.WorkspacePremium, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Total de Conquistas: $total", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Seção Desempenho
                item {
                    ProfileSection(title = "Desempenho", icon = Icons.Outlined.Leaderboard) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatItem("Jogos", state.played.toString())
                                StatItem("Vitórias", state.wins.toString())
                                StatItem("Empates", state.draws.toString())
                                StatItem("Derrotas", state.losses.toString())
                            }
                            LinearProgressIndicator(
                                progress = { (state.winRate / 100.0).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Text(
                                text = "Aproveitamento: ${"%.1f".format(state.winRate)}%",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }

                // Seção Gols
                item {
                    ProfileSection(title = "Gols", icon = Icons.Outlined.SportsSoccer) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem("Marcados", state.goalsFor.toString())
                            StatItem("Sofridos", state.goalsAgainst.toString())
                            StatItem("Saldo", (state.goalsFor - state.goalsAgainst).toString())
                        }
                    }
                }

                // Seção Financeiro
                item {
                    ProfileSection(title = "Financeiro", icon = Icons.Outlined.MonetizationOn) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo em Caixa", style = MaterialTheme.typography.bodyMedium)
                                Text("${state.currentBalanceCr} CR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Movimentação de Mercado", style = MaterialTheme.typography.bodyMedium)
                                Text("${state.marketMovementCr} CR", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            content()
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
