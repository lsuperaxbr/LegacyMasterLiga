package com.example.legacymasterliga.core.di

import com.example.legacymasterliga.core.security.PasswordHasher
import com.example.legacymasterliga.core.security.Pbkdf2PasswordHasher
import com.example.legacymasterliga.core.session.RoomSessionManager
import com.example.legacymasterliga.core.session.SessionManager
import com.example.legacymasterliga.data.repository.RoomAuthRepository
import com.example.legacymasterliga.data.repository.RoomClubRepository
import com.example.legacymasterliga.data.repository.RoomLeagueRepository
import com.example.legacymasterliga.data.repository.RoomPlayerRepository
import com.example.legacymasterliga.data.repository.RoomUserRepository
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.domain.repository.PlayerRepository
import com.example.legacymasterliga.domain.repository.UserRepository
import com.example.legacymasterliga.feature.backup.data.RoomBackupRepository
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import com.example.legacymasterliga.feature.backup.domain.BackupScheduler
import com.example.legacymasterliga.feature.backup.worker.WorkManagerBackupScheduler
import com.example.legacymasterliga.feature.audit.data.RoomAuditRepository
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.audit.domain.AuditRepository
import com.example.legacymasterliga.feature.clubprofile.data.RoomClubProfileRepository
import com.example.legacymasterliga.feature.clubprofile.domain.ClubProfileRepository
import com.example.legacymasterliga.feature.competitions.data.RoomCompetitionRepository
import com.example.legacymasterliga.feature.competitions.domain.CompetitionRepository
import com.example.legacymasterliga.feature.dashboard.data.RoomDashboardRepository
import com.example.legacymasterliga.feature.dashboard.domain.DashboardRepository
import com.example.legacymasterliga.feature.participants.data.RoomParticipantRepository
import com.example.legacymasterliga.feature.participants.domain.ParticipantRepository
import com.example.legacymasterliga.feature.schedule.data.RoomScheduleRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.results.data.RoomResultsRepository
import com.example.legacymasterliga.feature.results.domain.ResultsRepository
import com.example.legacymasterliga.feature.finance.data.RoomFinanceRepository
import com.example.legacymasterliga.feature.finance.domain.FinanceRepository
import com.example.legacymasterliga.feature.news.data.RoomNewsEventPublisher
import com.example.legacymasterliga.feature.news.domain.NewsEventPublisher
import com.example.legacymasterliga.feature.news.data.RoomNewsRepository
import com.example.legacymasterliga.feature.news.domain.NewsRepository
import com.example.legacymasterliga.feature.notifications.data.RoomNotificationRepository
import com.example.legacymasterliga.feature.notifications.domain.NotificationRepository
import com.example.legacymasterliga.feature.settings.data.RoomSettingsRepository
import com.example.legacymasterliga.feature.settings.domain.SettingsRepository
import com.example.legacymasterliga.feature.history.data.RoomHistoryRepository
import com.example.legacymasterliga.feature.history.domain.HistoryRepository
import com.example.legacymasterliga.feature.halloffame.data.RoomHallOfFameRepository
import com.example.legacymasterliga.feature.halloffame.domain.HallOfFameRepository
import com.example.legacymasterliga.feature.closure.data.RoomSeasonClosureRepository
import com.example.legacymasterliga.feature.closure.domain.SeasonClosureRepository
import com.example.legacymasterliga.feature.statistics.data.RoomStatisticsRepository
import com.example.legacymasterliga.feature.statistics.domain.StatisticsRepository
import com.example.legacymasterliga.feature.reports.data.RoomReportRepository
import com.example.legacymasterliga.feature.reports.domain.ReportRepository
import com.example.legacymasterliga.feature.online.data.FirestoreCloudAuthRepository
import com.example.legacymasterliga.feature.online.data.FirestoreCloudLeagueRepository
import com.example.legacymasterliga.feature.online.data.RoomOnlineLocalSessionGateway
import com.example.legacymasterliga.feature.online.domain.CloudAuthRepository
import com.example.legacymasterliga.feature.online.domain.CloudLeagueRepository
import com.example.legacymasterliga.feature.online.domain.OnlineLocalSessionGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindUserRepository(implementation: RoomUserRepository): UserRepository
    @Binds @Singleton abstract fun bindLeagueRepository(implementation: RoomLeagueRepository): LeagueRepository
    @Binds @Singleton abstract fun bindPlayerRepository(implementation: RoomPlayerRepository): PlayerRepository
    @Binds @Singleton abstract fun bindCloudAuthRepository(implementation: FirestoreCloudAuthRepository): CloudAuthRepository
    @Binds @Singleton abstract fun bindOnlineLocalSessionGateway(implementation: RoomOnlineLocalSessionGateway): OnlineLocalSessionGateway
    @Binds @Singleton abstract fun bindCloudLeagueRepository(implementation: FirestoreCloudLeagueRepository): CloudLeagueRepository
    @Binds @Singleton abstract fun bindClubRepository(implementation: RoomClubRepository): ClubRepository
    @Binds @Singleton abstract fun bindAuthRepository(implementation: RoomAuthRepository): AuthRepository
    @Binds @Singleton abstract fun bindSessionManager(implementation: RoomSessionManager): SessionManager
    @Binds @Singleton abstract fun bindDashboardRepository(implementation: RoomDashboardRepository): DashboardRepository
    @Binds @Singleton abstract fun bindClubProfileRepository(implementation: RoomClubProfileRepository): ClubProfileRepository
    @Binds @Singleton abstract fun bindCompetitionRepository(implementation: RoomCompetitionRepository): CompetitionRepository
    @Binds @Singleton abstract fun bindParticipantRepository(implementation: RoomParticipantRepository): ParticipantRepository
    @Binds @Singleton abstract fun bindScheduleRepository(implementation: RoomScheduleRepository): ScheduleRepository
    @Binds @Singleton abstract fun bindResultsRepository(implementation: RoomResultsRepository): ResultsRepository
    @Binds @Singleton abstract fun bindFinanceRepository(implementation: RoomFinanceRepository): FinanceRepository
    @Binds @Singleton abstract fun bindNewsEventPublisher(implementation: RoomNewsEventPublisher): NewsEventPublisher
    @Binds @Singleton abstract fun bindNewsRepository(implementation: RoomNewsRepository): NewsRepository
    @Binds @Singleton abstract fun bindNotificationRepository(implementation: RoomNotificationRepository): NotificationRepository
    @Binds @Singleton abstract fun bindAuditRepository(implementation: RoomAuditRepository): AuditRepository
    @Binds @Singleton abstract fun bindAuditLogger(implementation: RoomAuditRepository): AuditLogger
    @Binds @Singleton abstract fun bindBackupRepository(implementation: RoomBackupRepository): BackupRepository
    @Binds @Singleton abstract fun bindSettingsRepository(implementation: RoomSettingsRepository): SettingsRepository
    @Binds @Singleton abstract fun bindHistoryRepository(implementation: RoomHistoryRepository): HistoryRepository
    @Binds @Singleton abstract fun bindHallOfFameRepository(implementation: RoomHallOfFameRepository): HallOfFameRepository
    @Binds @Singleton abstract fun bindSeasonClosureRepository(implementation: RoomSeasonClosureRepository): SeasonClosureRepository
    @Binds @Singleton abstract fun bindStatisticsRepository(implementation: RoomStatisticsRepository): StatisticsRepository
    @Binds @Singleton abstract fun bindReportRepository(implementation: RoomReportRepository): ReportRepository
    @Binds @Singleton abstract fun bindBackupScheduler(implementation: WorkManagerBackupScheduler): BackupScheduler
    @Binds @Singleton abstract fun bindPasswordHasher(implementation: Pbkdf2PasswordHasher): PasswordHasher
    @Binds @Singleton abstract fun bindArenaRepository(implementation: com.example.legacymasterliga.feature.arena.data.RoomArenaRepository): com.example.legacymasterliga.feature.arena.domain.ArenaRepository
    @Binds @Singleton abstract fun bindAuctionRepository(implementation: com.example.legacymasterliga.feature.auction.data.RoomAuctionRepository): com.example.legacymasterliga.feature.auction.domain.AuctionRepository
}
