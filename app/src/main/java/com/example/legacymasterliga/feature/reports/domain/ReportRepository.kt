package com.example.legacymasterliga.feature.reports.domain

interface ReportRepository {
    suspend fun leagues(): List<ReportOption>
    suspend fun competitions(leagueId: Long): List<ReportOption>
    suspend fun seasons(competitionId: Long): List<ReportOption>
    suspend fun generate(filters: ReportFilters, format: ReportFormat): GeneratedReport
    suspend fun copyTo(uri: android.net.Uri, source: GeneratedReport)
}
