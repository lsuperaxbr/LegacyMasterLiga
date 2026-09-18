package com.example.legacymasterliga.feature.reports.data

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.legacymasterliga.BuildConfig
import com.example.legacymasterliga.core.cache.AppMemoryCache
import com.example.legacymasterliga.core.error.AppErrorReporter
import com.example.legacymasterliga.core.performance.PerformanceMonitor
import com.example.legacymasterliga.core.database.dao.ReportDao
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.reports.domain.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomReportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ReportDao,
    private val auditLogger: AuditLogger,
    private val memoryCache: AppMemoryCache,
    private val performanceMonitor: PerformanceMonitor,
    private val errorReporter: AppErrorReporter,
) : ReportRepository {
    override suspend fun leagues(): List<ReportOption> = memoryCache.getOrPut("reports:leagues") {
        performanceMonitor.measureSuspend("reports.load_leagues") {
            dao.leagues().map { ReportOption(it.id, it.name) }
        }
    }

    override suspend fun competitions(leagueId: Long): List<ReportOption> =
        memoryCache.getOrPut("reports:competitions:$leagueId") {
            performanceMonitor.measureSuspend("reports.load_competitions") {
                dao.competitions(leagueId).map { ReportOption(it.id, it.name) }
            }
        }

    override suspend fun seasons(competitionId: Long): List<ReportOption> =
        memoryCache.getOrPut("reports:seasons:$competitionId") {
            performanceMonitor.measureSuspend("reports.load_seasons") {
                dao.seasons(competitionId).map { ReportOption(it.id, it.name) }
            }
        }

    override suspend fun generate(
        filters: ReportFilters,
        format: ReportFormat,
    ): GeneratedReport = withContext(Dispatchers.IO) {
        try {
            performanceMonitor.measureSuspend("reports.generate_${format.name.lowercase()}") {
                val league = dao.leagues().first { it.id == filters.leagueId }
                val competitionName = filters.competitionId?.let { id ->
                    dao.competitions(filters.leagueId).firstOrNull { it.id == id }?.name
                }
                val seasonName = filters.competitionId?.let { competitionId ->
                    filters.seasonId?.let { seasonId ->
                        dao.seasons(competitionId).firstOrNull { it.id == seasonId }?.name
                    }
                }
                val title = listOfNotNull(league.name, competitionName, seasonName).joinToString(" • ")
                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val safe = league.name.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val file = File(
                    context.cacheDir,
                    "reports/${safe}_relatorio_$stamp.${format.extension}",
                ).apply { parentFile?.mkdirs() }
                val sections = buildSections(filters)
                if (format == ReportFormat.CSV) writeCsv(file, title, sections) else writePdf(file, title, sections)
                auditLogger.log(
                    "REPORTS",
                    "EXPORT",
                    "REPORT",
                    leagueId = filters.leagueId,
                    summary = "Relatório ${format.name} gerado",
                    details = title,
                )
                GeneratedReport(
                    FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.files", file),
                    file.name,
                    format.mimeType,
                )
            }
        } catch (throwable: Throwable) {
            errorReporter.report("Relatórios", throwable, "Não foi possível gerar o relatório.")
            throw throwable
        }
    }

    override suspend fun copyTo(
        uri: android.net.Uri,
        source: GeneratedReport,
    ) = withContext(Dispatchers.IO) {
        try {
            performanceMonitor.measureSuspend("reports.copy_file") {
                context.contentResolver.openInputStream(source.uri).use { input ->
                    requireNotNull(input) { "Não foi possível abrir o relatório." }
                    context.contentResolver.openOutputStream(uri).use { output ->
                        requireNotNull(output) { "Não foi possível salvar o arquivo." }
                        input.copyTo(output)
                    }
                }
            }
            Unit
        } catch (throwable: Throwable) {
            errorReporter.report("Relatórios", throwable, "Não foi possível salvar o relatório.")
            throw throwable
        }
    }

    private suspend fun buildSections(f: ReportFilters): List<Pair<String, List<List<String>>>> {
        val standings = f.seasonId?.let { dao.standings(it) }.orEmpty().mapIndexed { i, r -> listOf((i+1).toString(), r.clubName, r.played.toString(), r.wins.toString(), r.draws.toString(), r.losses.toString(), r.goalsFor.toString(), r.goalsAgainst.toString(), r.goalDifference.toString(), r.points.toString()) }
        val finance = dao.finance(f.leagueId).map { listOf(date(it.createdAt), it.clubName, it.type, it.description, it.amountCr.toString()) }
        val market = dao.transfers(f.leagueId).map { listOf(date(it.createdAt), it.playerName, it.originClubName, it.destinationClubName, it.valueCr.toString()) }
        val matches = dao.matches(f.leagueId, f.competitionId, f.seasonId).map { listOf(it.roundNumber.toString(), it.homeClubName, it.homeScore.toString(), it.awayScore.toString(), it.awayClubName) }
        val history = dao.history(f.leagueId, f.competitionId, f.seasonId).map { listOf(it.competitionName, it.seasonName, it.championName ?: "-", it.runnerUpName ?: "-", it.completedMatches.toString(), it.totalGoals.toString()) }
        val s = dao.statistics(f.leagueId, f.competitionId, f.seasonId)
        return listOf(
            "CLASSIFICAÇÃO" to (listOf(listOf("Pos","Clube","J","V","E","D","GP","GC","SG","PTS")) + standings),
            "FINANCEIRO (CR)" to (listOf(listOf("Data","Clube","Tipo","Descrição","Valor CR")) + finance),
            "MERCADO" to (listOf(listOf("Data","Jogador","Origem","Destino","Valor CR")) + market),
            "RESULTADOS" to (listOf(listOf("Rodada","Mandante","GM","GV","Visitante")) + matches),
            "HISTÓRICO" to (listOf(listOf("Competição","Temporada","Campeão","Vice","Jogos","Gols")) + history),
            "ESTATÍSTICAS" to listOf(listOf("Partidas","Gols","Média gols","CR movimentados","Clubes ativos"), listOf(s.finishedMatches.toString(), s.totalGoals.toString(), "%.2f".format(Locale.US,s.averageGoals), s.totalCrMoved.toString(), s.activeClubs.toString())),
        )
    }

    private fun writeCsv(file: File, title: String, sections: List<Pair<String, List<List<String>>>>) {
        file.bufferedWriter(Charsets.UTF_8).use { out ->
            out.appendLine("Legacy Master Liga;$title")
            sections.forEach { (name, rows) ->
                out.appendLine(); out.appendLine(name)
                rows.forEach { row -> out.appendLine(row.joinToString(";") { csv(it) }) }
            }
        }
    }

    private fun writePdf(file: File, title: String, sections: List<Pair<String, List<List<String>>>>) {
        val document = PdfDocument(); val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }
        var pageNo = 0; var page: PdfDocument.Page? = null; var y = 0f
        fun newPage() { page?.let(document::finishPage); pageNo++; page = document.startPage(PdfDocument.PageInfo.Builder(595,842,pageNo).create()); y=42f; paint.isFakeBoldText=true; paint.textSize=16f; page!!.canvas.drawText("Legacy Master Liga",32f,y,paint); y+=22f; paint.textSize=11f; page!!.canvas.drawText(title,32f,y,paint); y+=28f }
        fun line(text: String, bold: Boolean=false) { if (page==null || y>805f) newPage(); paint.isFakeBoldText=bold; paint.textSize=if(bold)12f else 9f; page!!.canvas.drawText(text.take(105),32f,y,paint); y+=15f }
        newPage(); sections.forEach { (name, rows) -> line(name,true); rows.forEach { line(it.joinToString(" | ")) }; y+=8f }
        page?.let(document::finishPage); file.outputStream().use(document::writeTo); document.close()
    }
    private fun csv(value: String) = if (value.any { it == ';' || it == '"' || it == '\n' }) "\"${value.replace("\"","\"\"")}\"" else value
    private fun date(value: Long) = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(value))
}
