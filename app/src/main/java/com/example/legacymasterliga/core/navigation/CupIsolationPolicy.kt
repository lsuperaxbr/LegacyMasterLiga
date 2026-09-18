package com.example.legacymasterliga.core.navigation

import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption

/** UI-only policy used while the Cup module is unavailable. */
object CupIsolationPolicy {
    fun leagueSeasons(options: List<ScheduleSeasonOption>): List<ScheduleSeasonOption> =
        options.filter { it.type == CompetitionType.LEAGUE }

    fun blocksLegacySchedule(
        requestedCompetitionId: Long?,
        options: List<ScheduleSeasonOption>,
    ): Boolean = requestedCompetitionId != null && options.any { option ->
        option.competitionId == requestedCompetitionId && option.type == CompetitionType.CUP
    }
}
