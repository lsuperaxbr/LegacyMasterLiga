package com.example.legacymasterliga.feature.reports.domain

import android.net.Uri

enum class ReportFormat(val extension: String, val mimeType: String) { PDF("pdf", "application/pdf"), CSV("csv", "text/csv") }

data class ReportOption(val id: Long, val name: String)
data class ReportFilters(val leagueId: Long, val competitionId: Long? = null, val seasonId: Long? = null)
data class GeneratedReport(val uri: Uri, val fileName: String, val mimeType: String)
