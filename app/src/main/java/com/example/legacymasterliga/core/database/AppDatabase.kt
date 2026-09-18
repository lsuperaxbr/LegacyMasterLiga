package com.example.legacymasterliga.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.legacymasterliga.core.database.converter.LegacyTypeConverters
import com.example.legacymasterliga.core.database.dao.AuditLogDao
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.DashboardDao
import com.example.legacymasterliga.core.database.dao.CompetitionParticipantDao
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.dao.HistoryDao
import com.example.legacymasterliga.core.database.dao.HallOfFameDao
import com.example.legacymasterliga.core.database.dao.MatchDao
import com.example.legacymasterliga.core.database.dao.PresidentProfileDao
import com.example.legacymasterliga.core.database.dao.RoundDao
import com.example.legacymasterliga.core.database.dao.NewsDao
import com.example.legacymasterliga.core.database.dao.NotificationDao
import com.example.legacymasterliga.core.database.dao.OnlineSyncDao
import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.dao.PrizeDao
import com.example.legacymasterliga.core.database.dao.ReportDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.dao.SeasonClosureDao
import com.example.legacymasterliga.core.database.dao.SessionDao
import com.example.legacymasterliga.core.database.dao.StandingDao
import com.example.legacymasterliga.core.database.dao.StatisticsDao
import com.example.legacymasterliga.core.database.dao.TransferDao
import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.dao.SettingsDao
import com.example.legacymasterliga.core.database.entity.AuditLogEntity
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import com.example.legacymasterliga.core.database.entity.CompetitionParticipantEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.database.entity.MatchEntity
import com.example.legacymasterliga.core.database.entity.RoundEntity
import com.example.legacymasterliga.core.database.entity.NewsEntity
import com.example.legacymasterliga.core.database.entity.NotificationEntity
import com.example.legacymasterliga.core.database.entity.NotificationReadEntity
import com.example.legacymasterliga.core.database.entity.OnlineSyncQueueEntity
import com.example.legacymasterliga.core.database.entity.OnlineSyncRecordEntity
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.database.entity.SeasonEntity
import com.example.legacymasterliga.core.database.entity.SeasonClosureEntity
import com.example.legacymasterliga.core.database.entity.FinalStandingEntity
import com.example.legacymasterliga.core.database.entity.SessionEntity
import com.example.legacymasterliga.core.database.entity.StandingEntity
import com.example.legacymasterliga.core.database.entity.TransferEntity
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.database.entity.AppSettingsEntity
import com.example.legacymasterliga.core.database.entity.CompetitionSettingsEntity
import com.example.legacymasterliga.core.database.entity.CompetitionPrizeEntity
import com.example.legacymasterliga.core.database.entity.PrizeHistoryEntity
import com.example.legacymasterliga.core.database.entity.GoalEventEntity

/** Single source of truth for the on-device database contract. */
object DatabaseContract {
    const val VERSION = 27
    const val NAME = "legacy_master_liga.db"
    const val OLDEST_SUPPORTED_VERSION = 1
}

@Database(
    entities = [
        UserEntity::class,
        SessionEntity::class,
        LeagueEntity::class,
        ClubEntity::class,
        CompetitionEntity::class,
        CompetitionParticipantEntity::class,
        SeasonEntity::class,
        RoundEntity::class,
        MatchEntity::class,
        StandingEntity::class,
        FinancialTransactionEntity::class,
        TransferEntity::class,
        NewsEntity::class,
        AuditLogEntity::class,
        AppSettingsEntity::class,
        CompetitionSettingsEntity::class,
        SeasonClosureEntity::class,
        FinalStandingEntity::class,
        CompetitionPrizeEntity::class,
        PrizeHistoryEntity::class,
        NotificationEntity::class,
        NotificationReadEntity::class,
        OnlineSyncRecordEntity::class,
        OnlineSyncQueueEntity::class,
        PlayerEntity::class,
        com.example.legacymasterliga.core.database.entity.ArenaDuelEntity::class,
        GoalEventEntity::class,
        com.example.legacymasterliga.core.database.entity.AuctionLotEntity::class,
        com.example.legacymasterliga.core.database.entity.AuctionItemEntity::class,
        com.example.legacymasterliga.core.database.entity.AuctionBidEntity::class,
    ],
    version = DatabaseContract.VERSION,
    exportSchema = true,
)
@TypeConverters(LegacyTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun leagueDao(): LeagueDao
    abstract fun clubDao(): ClubDao
    abstract fun competitionDao(): CompetitionDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun competitionParticipantDao(): CompetitionParticipantDao
    abstract fun seasonDao(): SeasonDao
    abstract fun roundDao(): RoundDao
    abstract fun matchDao(): MatchDao
    abstract fun standingDao(): StandingDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun financialDao(): FinancialDao
    abstract fun transferDao(): TransferDao
    abstract fun newsDao(): NewsDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun settingsDao(): SettingsDao
    abstract fun playerDao(): PlayerDao
    abstract fun historyDao(): HistoryDao
    abstract fun hallOfFameDao(): HallOfFameDao
    abstract fun seasonClosureDao(): SeasonClosureDao
    abstract fun prizeDao(): PrizeDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reportDao(): ReportDao
    abstract fun onlineSyncDao(): OnlineSyncDao
    abstract fun presidentProfileDao(): PresidentProfileDao
    abstract fun arenaDao(): com.example.legacymasterliga.core.database.dao.ArenaDao
    abstract fun goalEventDao(): com.example.legacymasterliga.core.database.dao.GoalEventDao
    abstract fun auctionDao(): com.example.legacymasterliga.core.database.dao.AuctionDao

    companion object {
        const val DATABASE_NAME = DatabaseContract.NAME
    }
}
