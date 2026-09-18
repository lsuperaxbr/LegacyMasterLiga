package com.example.legacymasterliga.feature.settings.domain

import com.example.legacymasterliga.core.model.DensityPreference
import com.example.legacymasterliga.core.model.ThemePreference
import com.example.legacymasterliga.core.model.TieBreakCriterion

data class AppPreferences(
    val themePreference: ThemePreference = ThemePreference.DARK,
    val densityPreference: DensityPreference = DensityPreference.COMFORTABLE,
    val animationsEnabled: Boolean = true,
)

data class CompetitionRules(
    val competitionId: Long,
    val pointsForWin: Int = 3,
    val pointsForDraw: Int = 1,
    val pointsForLoss: Int = 0,
    val yellowCardFineCr: Long = 0,
    val redCardFineCr: Long = 0,
    val tieBreakCriteria: List<TieBreakCriterion> = listOf(
        TieBreakCriterion.POINTS,
        TieBreakCriterion.WINS,
        TieBreakCriterion.GOAL_DIFFERENCE,
        TieBreakCriterion.GOALS_FOR,
    ),
    val highlightLeader: Boolean = true,
)


data class CompetitionOption(
    val id: Long,
    val leagueId: Long,
    val name: String,
)
