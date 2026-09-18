package com.example.legacymasterliga.feature.halloffame.domain

import kotlinx.coroutines.flow.Flow

interface HallOfFameRepository {
    fun observeByLeague(leagueId: Long): Flow<HallOfFameSnapshot>
}
