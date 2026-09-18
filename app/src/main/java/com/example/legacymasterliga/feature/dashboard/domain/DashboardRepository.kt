package com.example.legacymasterliga.feature.dashboard.domain

import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeSummary(userId: Long?): Flow<DashboardSummary>
}
