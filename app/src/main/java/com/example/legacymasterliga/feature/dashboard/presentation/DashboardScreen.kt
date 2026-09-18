package com.example.legacymasterliga.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.navigation.LegacyDestination
import com.example.legacymasterliga.core.navigation.LegacyNavigationMenu
import com.example.legacymasterliga.core.ui.components.LegacyHudPanel
import com.example.legacymasterliga.core.ui.components.LegacyNavigationGrid
import com.example.legacymasterliga.feature.news.domain.NewsArticle
import com.example.legacymasterliga.ui.theme.LegacyBlue
import java.text.DateFormat
import java.util.Date
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border

@Composable
fun DashboardRoute(
    onOpen: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val latestNews by viewModel.latestNews.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotifications.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        latestNews = latestNews,
        unreadCount = unreadCount,
        onOpen = onOpen,
        onLogout = viewModel::logout,
        onOpenNotifications = { onOpen(LegacyDestination.Notifications.route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    latestNews: List<NewsArticle>,
    unreadCount: Int,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val role = state.user?.role
    val modules = remember(role) { LegacyNavigationMenu.dashboard(role) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF08152B),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF08152B))) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "LEGACY MASTER LIGA",
                        style = MaterialTheme.typography.labelSmall,
                        color = LegacyBlue,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(16.dp),
                    )
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text(unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notificações",
                                tint = Color.White.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
                LegacyHudPanel(
                    leagueName = state.summary.leagueName,
                    userName = state.user?.displayName ?: "JOGADOR",
                    roleName = state.user?.role?.name ?: "GERAL",
                    isOnline = state.user?.isOnlineSession == true,
                )
                if (state.summary.presidentClubBalances.isNotEmpty()) {
                    PresidentBalancesPanel(
                        balances = state.summary.presidentClubBalances,
                        onOpenClub = { clubId -> onOpen(LegacyDestination.ClubProfile.createRoute(clubId)) }
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                TextButton(onClick = onLogout) {
                    Icon(
                        Icons.Outlined.Logout,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text(
                        "Sair",
                        color = Color.White.copy(alpha = 0.35f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "quick-shortcuts") {
                QuickShortcuts(
                    onOpenStandings = { onOpen(LegacyDestination.Standings.route) },
                    onOpenSchedule = { onOpen(LegacyDestination.Schedule.route) },
                    onOpenTopScorers = { onOpen(LegacyDestination.TopScorers.route) }
                )
            }
            item(key = "news-title") {
                SectionTitle("ÚLTIMAS NOTÍCIAS")
            }
            item(key = "news-feed") {
                NewsFeedPreview(
                    news = latestNews,
                    onSeeAll = { onOpen(LegacyDestination.News.route) },
                )
            }
            item(key = "menu-title") {
                SectionTitle("CENTRAL DE JOGO")
            }
            item(key = "game-modules") {
                LegacyNavigationGrid(
                    items = modules,
                    onOpen = onOpen,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NewsFeedPreview(
    news: List<NewsArticle>,
    onSeeAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F3C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (news.isEmpty()) {
                Text(
                    "Nenhuma notícia ainda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                news.forEach { article ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(article.publishedAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                TextButton(
                    onClick = onSeeAll,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Ver todas →", style = MaterialTheme.typography.labelMedium, color = LegacyBlue)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = LegacyBlue,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun QuickShortcuts(
    onOpenStandings: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenTopScorers: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShortcutCard(
            title = "TABELA",
            icon = Icons.Outlined.EmojiEvents,
            color = Color(0xFFFFD700), // Gold
            onClick = onOpenStandings,
            modifier = Modifier.weight(1f)
        )
        ShortcutCard(
            title = "RODADAS",
            icon = Icons.Outlined.CalendarMonth,
            color = Color(0xFF00D4FF), // Neon Cyan
            onClick = onOpenSchedule,
            modifier = Modifier.weight(1f)
        )
        ShortcutCard(
            title = "GOLS",
            icon = Icons.Outlined.SportsSoccer,
            color = Color(0xFF2E7D32), // Green
            onClick = onOpenTopScorers,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F3C)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PresidentBalancesPanel(
    balances: List<com.example.legacymasterliga.feature.dashboard.domain.DashboardClubBalance>,
    onOpenClub: (Long) -> Unit,
) {
    val neonCyan = Color(0xFF00D4FF)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(4.dp)),
        color = Color(0xFF001226),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            balances.forEach { balance ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenClub(balance.clubId) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = balance.clubName.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${balance.balanceCr} CR",
                        style = MaterialTheme.typography.titleMedium,
                        color = neonCyan,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
