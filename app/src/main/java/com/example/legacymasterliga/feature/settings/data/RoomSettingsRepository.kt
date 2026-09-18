package com.example.legacymasterliga.feature.settings.data

import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.dao.SettingsDao
import com.example.legacymasterliga.core.database.entity.AppSettingsEntity
import com.example.legacymasterliga.core.database.entity.CompetitionSettingsEntity
import com.example.legacymasterliga.core.model.TieBreakCriterion
import com.example.legacymasterliga.data.mapper.toDomain
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.settings.domain.AppPreferences
import com.example.legacymasterliga.feature.settings.domain.CompetitionRules
import com.example.legacymasterliga.feature.settings.domain.CompetitionOption
import com.example.legacymasterliga.feature.settings.domain.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class RoomSettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val leagueDao: LeagueDao,
    private val competitionDao: CompetitionDao,
    private val auditLogger: AuditLogger,
) : SettingsRepository {
    override fun observeAppPreferences(): Flow<AppPreferences> =
        settingsDao.observeAppSettings()
            .onStart { settingsDao.insertDefaultAppSettings(AppSettingsEntity()) }
            .map { entity ->
                entity?.let {
                    AppPreferences(it.themePreference, it.densityPreference, it.animationsEnabled)
                } ?: AppPreferences()
            }

    override fun observeLeagues() = leagueDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCompetitions(leagueId: Long): Flow<List<CompetitionOption>> =
        competitionDao.observeByLeague(leagueId).map { list ->
            list.map { competition ->
                CompetitionOption(competition.id, competition.leagueId, competition.name)
            }
        }

    override fun observeCompetitionRules(competitionId: Long): Flow<CompetitionRules> = combine(
        competitionDao.observeById(competitionId),
        settingsDao.observeCompetitionSettings(competitionId),
    ) { competition, settings ->
        val current = requireNotNull(competition) { "Competição não encontrada." }
        CompetitionRules(
            competitionId = competitionId,
            pointsForWin = current.pointsForWin,
            pointsForDraw = current.pointsForDraw,
            pointsForLoss = current.pointsForLoss,
            yellowCardFineCr = current.yellowCardFineCr,
            redCardFineCr = current.redCardFineCr,
            tieBreakCriteria = settings?.tieBreakCriteriaCsv
                ?.split(',')
                ?.mapNotNull { value -> runCatching { TieBreakCriterion.valueOf(value) }.getOrNull() }
                ?.takeIf { it.isNotEmpty() }
                ?: CompetitionRules(competitionId).tieBreakCriteria,
            highlightLeader = settings?.highlightLeader ?: true,
        )
    }

    override suspend fun updateAppPreferences(preferences: AppPreferences) {
        settingsDao.insertDefaultAppSettings(AppSettingsEntity())
        settingsDao.upsertAppSettings(
            AppSettingsEntity(
                themePreference = preferences.themePreference,
                densityPreference = preferences.densityPreference,
                animationsEnabled = preferences.animationsEnabled,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        auditLogger.log(
            category = "SETTINGS",
            action = "UPDATE_APP_PREFERENCES",
            entityType = "APP_SETTINGS",
            entityId = 1,
            summary = "Preferências visuais do aplicativo atualizadas.",
        )
    }

    override suspend fun renameLeague(leagueId: Long, name: String) {
        val cleanName = name.trim()
        require(cleanName.length >= 3) { "O nome da liga deve ter pelo menos 3 caracteres." }
        val current = leagueDao.findById(leagueId) ?: error("Liga não encontrada.")
        val duplicate = leagueDao.findByName(cleanName)
        require(duplicate == null || duplicate.id == leagueId) { "Já existe uma liga com esse nome." }
        leagueDao.update(current.copy(name = cleanName, updatedAt = System.currentTimeMillis()))
        auditLogger.log(
            category = "SETTINGS",
            action = "RENAME_LEAGUE",
            entityType = "LEAGUE",
            entityId = leagueId,
            leagueId = leagueId,
            summary = "Liga renomeada para $cleanName.",
        )
    }

    override suspend fun updateCompetitionRules(rules: CompetitionRules) {
        require(rules.pointsForWin in 0..10) { "Pontos por vitória inválidos." }
        require(rules.pointsForDraw in 0..10) { "Pontos por empate inválidos." }
        require(rules.pointsForLoss in 0..10) { "Pontos por derrota inválidos." }
        require(rules.tieBreakCriteria.isNotEmpty()) { "Defina ao menos um critério de desempate." }
        val competition = competitionDao.findById(rules.competitionId) ?: error("Competição não encontrada.")
        competitionDao.update(
            competition.copy(
                pointsForWin = rules.pointsForWin,
                pointsForDraw = rules.pointsForDraw,
                pointsForLoss = rules.pointsForLoss,
                yellowCardFineCr = rules.yellowCardFineCr,
                redCardFineCr = rules.redCardFineCr,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        settingsDao.upsertCompetitionSettings(
            CompetitionSettingsEntity(
                competitionId = rules.competitionId,
                tieBreakCriteriaCsv = rules.tieBreakCriteria.joinToString(",") { it.name },
                highlightLeader = rules.highlightLeader,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        auditLogger.log(
            category = "SETTINGS",
            action = "UPDATE_COMPETITION_RULES",
            entityType = "COMPETITION",
            entityId = competition.id,
            leagueId = competition.leagueId,
            summary = "Regras da competição ${competition.name} atualizadas.",
            details = "Vitória=${rules.pointsForWin}; Empate=${rules.pointsForDraw}; Derrota=${rules.pointsForLoss}",
        )
    }
}
