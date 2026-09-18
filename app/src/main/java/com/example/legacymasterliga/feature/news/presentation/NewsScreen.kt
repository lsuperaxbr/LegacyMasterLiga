package com.example.legacymasterliga.feature.news.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.feature.news.domain.NewsArticle
import java.text.DateFormat
import java.util.Date

@Composable
fun NewsRoute(onBack: () -> Unit, viewModel: NewsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NewsScreen(state, onBack, viewModel::selectLeague, viewModel::selectCategory)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    state: NewsUiState,
    onBack: () -> Unit,
    onSelectLeague: (Long) -> Unit,
    onSelectCategory: (String?) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notícias da Liga") },
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
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Motor automático", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Notícias geradas por eventos reais, sem uso de IA.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.leagues, key = { it.id }) { league ->
                        AssistChip(
                            onClick = { onSelectLeague(league.id) },
                            label = { Text(league.name) },
                            leadingIcon = if (league.id == state.selectedLeagueId) ({ Icon(Icons.Outlined.Article, null) }) else null,
                        )
                    }
                }
            }
            if (state.categories.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = state.selectedCategory == null, onClick = { onSelectCategory(null) }, label = { Text("Todas") }) }
                        items(state.categories) { category ->
                            FilterChip(selected = state.selectedCategory == category, onClick = { onSelectCategory(category) }, label = { Text(category) })
                        }
                    }
                }
            }
            if (state.selectedLeagueId == null) {
                item { EmptyNews("Nenhuma liga disponível.") }
            } else if (state.articles.isEmpty()) {
                item { EmptyNews("As notícias aparecerão automaticamente após transferências e resultados.") }
            } else {
                items(state.articles, key = { it.id }) { article -> NewsCard(article) }
            }
        }
    }
}

@Composable
private fun NewsCard(article: NewsArticle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(article.category, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(article.publishedAt)), style = MaterialTheme.typography.labelSmall)
            }
            Text(article.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(article.body, style = MaterialTheme.typography.bodyLarge)
            val context = listOfNotNull(article.leagueName, article.competitionName, article.seasonName).joinToString(" • ")
            Text(context, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyNews(message: String) {
    Card(Modifier.fillMaxWidth()) { Text(message, modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
