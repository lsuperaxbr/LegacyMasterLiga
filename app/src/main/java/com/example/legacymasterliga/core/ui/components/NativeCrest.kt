package com.example.legacymasterliga.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.legacymasterliga.core.ui.components.badge.ClubBadge
import com.example.legacymasterliga.domain.model.ClubBadgeConfig

@Composable
fun NativeCrest(
    clubName: String,
    crestRaw: String?,
    modifier: Modifier = Modifier,
    badgeConfigJson: String? = null
) {
    val config = ClubBadgeConfig.fromJson(badgeConfigJson, crestRaw, clubName)
    ClubBadge(config = config, clubName = clubName, modifier = modifier)
}
