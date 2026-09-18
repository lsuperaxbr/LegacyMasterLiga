package com.example.legacymasterliga.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material.icons.outlined.FlagCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.legacymasterliga.core.navigation.LegacyMenuIcon
import com.example.legacymasterliga.core.navigation.LegacyMenuItem
import com.example.legacymasterliga.ui.theme.LegacySpacing

@Composable
fun LegacyNavigationGrid(
    items: List<LegacyMenuItem>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LegacySpacing.md),
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LegacySpacing.md),
            ) {
                rowItems.forEach { item ->
                    if (item.icon == LegacyMenuIcon.ARENA) {
                        LegacyArcadePlate(
                            title = item.title,
                            subtitle = item.subtitle,
                            icon = { com.example.legacymasterliga.feature.arena.presentation.ArenaIcon() },
                            onClick = { onOpen(item.route) },
                            modifier = Modifier.weight(1f),
                            highlighted = item.highlighted,
                        )
                    } else {
                        LegacyArcadePlate(
                            title = item.title,
                            subtitle = item.subtitle,
                            icon = item.icon.imageVector(),
                            onClick = { onOpen(item.route) },
                            modifier = Modifier.weight(1f),
                            highlighted = item.highlighted,
                        )
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun LegacyMenuIcon.imageVector(): ImageVector = when (this) {
    LegacyMenuIcon.LEAGUE -> Icons.Outlined.SportsSoccer
    LegacyMenuIcon.CUP -> Icons.Outlined.WorkspacePremium
    LegacyMenuIcon.MARKET -> Icons.Outlined.SwapHoriz
    LegacyMenuIcon.CLUBS -> Icons.Outlined.Groups
    LegacyMenuIcon.FINANCE -> Icons.Outlined.AccountBalanceWallet
    LegacyMenuIcon.CENTRAL -> Icons.Outlined.Campaign
    LegacyMenuIcon.HISTORY -> Icons.Outlined.History
    LegacyMenuIcon.MORE -> Icons.Outlined.MoreHoriz
    LegacyMenuIcon.COMPETITION -> Icons.Outlined.EmojiEvents
    LegacyMenuIcon.ROUNDS -> Icons.Outlined.CalendarMonth
    LegacyMenuIcon.STANDINGS -> Icons.Outlined.Leaderboard
    LegacyMenuIcon.STATISTICS -> Icons.Outlined.Insights
    LegacyMenuIcon.CLOSURE -> Icons.Outlined.FlagCircle
    LegacyMenuIcon.NEWS -> Icons.Outlined.Article
    LegacyMenuIcon.NOTIFICATIONS -> Icons.Outlined.Notifications
    LegacyMenuIcon.USERS -> Icons.Outlined.ManageAccounts
    LegacyMenuIcon.SETTINGS -> Icons.Outlined.Settings
    LegacyMenuIcon.BACKUP -> Icons.Outlined.Backup
    LegacyMenuIcon.AUDIT -> Icons.Outlined.FactCheck
    LegacyMenuIcon.PERFORMANCE -> Icons.Outlined.Speed
    LegacyMenuIcon.EXPORT -> Icons.Outlined.FilePresent
    LegacyMenuIcon.BETA -> Icons.Outlined.Checklist
    LegacyMenuIcon.ARENA -> Icons.Outlined.SportsSoccer
    LegacyMenuIcon.SPORTS_SOCCER -> Icons.Outlined.SportsSoccer
}
