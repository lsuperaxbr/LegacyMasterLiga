package com.example.legacymasterliga.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.legacymasterliga.core.database.AppDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS financial_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    clubId INTEGER NOT NULL,
                    amountCr INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(clubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_financial_transactions_clubId ON financial_transactions(clubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_financial_transactions_createdAt ON financial_transactions(createdAt)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS transfers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER NOT NULL,
                    playerName TEXT NOT NULL,
                    originClubId INTEGER NOT NULL,
                    destinationClubId INTEGER NOT NULL,
                    valueCr INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(leagueId) REFERENCES leagues(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(originClubId) REFERENCES clubs(id) ON UPDATE NO ACTION ON DELETE RESTRICT,
                    FOREIGN KEY(destinationClubId) REFERENCES clubs(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transfers_leagueId ON transfers(leagueId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transfers_originClubId ON transfers(originClubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transfers_destinationClubId ON transfers(destinationClubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transfers_createdAt ON transfers(createdAt)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS news (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    category TEXT NOT NULL,
                    publishedAt INTEGER NOT NULL,
                    FOREIGN KEY(leagueId) REFERENCES leagues(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_news_leagueId ON news(leagueId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_news_publishedAt ON news(publishedAt)")
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS competition_participants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    seasonId INTEGER NOT NULL,
                    clubId INTEGER NOT NULL,
                    seed INTEGER,
                    isActive INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(clubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_competition_participants_seasonId ON competition_participants(seasonId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_competition_participants_clubId ON competition_participants(clubId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_competition_participants_seasonId_clubId ON competition_participants(seasonId, clubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_competition_participants_isActive ON competition_participants(isActive)")
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS rounds (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    seasonId INTEGER NOT NULL,
                    number INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    status TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rounds_seasonId ON rounds(seasonId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_rounds_seasonId_number ON rounds(seasonId, number)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rounds_status ON rounds(status)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS matches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    seasonId INTEGER NOT NULL,
                    roundId INTEGER NOT NULL,
                    homeClubId INTEGER NOT NULL,
                    awayClubId INTEGER NOT NULL,
                    pairingKey TEXT NOT NULL,
                    leg INTEGER NOT NULL,
                    homeScore INTEGER,
                    awayScore INTEGER,
                    status TEXT NOT NULL,
                    scheduledAt INTEGER,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(roundId) REFERENCES rounds(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(homeClubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
                    FOREIGN KEY(awayClubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_matches_seasonId ON matches(seasonId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_matches_roundId ON matches(roundId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_matches_homeClubId ON matches(homeClubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_matches_awayClubId ON matches(awayClubId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_matches_seasonId_pairingKey_leg ON matches(seasonId, pairingKey, leg)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_matches_status ON matches(status)")
        }
    }


    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS standings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    seasonId INTEGER NOT NULL,
                    clubId INTEGER NOT NULL,
                    played INTEGER NOT NULL,
                    wins INTEGER NOT NULL,
                    draws INTEGER NOT NULL,
                    losses INTEGER NOT NULL,
                    goalsFor INTEGER NOT NULL,
                    goalsAgainst INTEGER NOT NULL,
                    goalDifference INTEGER NOT NULL,
                    points INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(clubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_standings_seasonId ON standings(seasonId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_standings_clubId ON standings(clubId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_standings_seasonId_clubId ON standings(seasonId, clubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_standings_seasonId_points ON standings(seasonId, points)")
        }
    }

    private val migration5To6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE financial_transactions ADD COLUMN type TEXT NOT NULL DEFAULT 'ADJUSTMENT'")
            db.execSQL("ALTER TABLE financial_transactions ADD COLUMN counterpartyClubId INTEGER")
            db.execSQL("ALTER TABLE financial_transactions ADD COLUMN transferId INTEGER")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_financial_transactions_transferId ON financial_transactions(transferId)")
        }
    }

    private val migration6To7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE news_v7 (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER NOT NULL,
                    competitionId INTEGER,
                    seasonId INTEGER,
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    category TEXT NOT NULL,
                    eventType TEXT NOT NULL,
                    sourceId INTEGER,
                    dedupKey TEXT NOT NULL,
                    publishedAt INTEGER NOT NULL,
                    FOREIGN KEY(leagueId) REFERENCES leagues(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(competitionId) REFERENCES competitions(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                    FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO news_v7 (id, leagueId, competitionId, seasonId, title, body, category, eventType, sourceId, dedupKey, publishedAt)
                SELECT id, leagueId, NULL, NULL, title, body, category, 'LEGACY', id, 'LEGACY:' || id, publishedAt
                FROM news
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE news")
            db.execSQL("ALTER TABLE news_v7 RENAME TO news")
            db.execSQL("CREATE INDEX index_news_leagueId ON news(leagueId)")
            db.execSQL("CREATE INDEX index_news_competitionId ON news(competitionId)")
            db.execSQL("CREATE INDEX index_news_seasonId ON news(seasonId)")
            db.execSQL("CREATE INDEX index_news_publishedAt ON news(publishedAt)")
            db.execSQL("CREATE UNIQUE INDEX index_news_dedupKey ON news(dedupKey)")
        }
    }

    private val migration7To8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER,
                    actorUserId INTEGER,
                    actorName TEXT NOT NULL,
                    actorRole TEXT NOT NULL,
                    category TEXT NOT NULL,
                    action TEXT NOT NULL,
                    entityType TEXT NOT NULL,
                    entityId INTEGER,
                    summary TEXT NOT NULL,
                    details TEXT,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_leagueId ON audit_logs(leagueId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_actorUserId ON audit_logs(actorUserId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_category ON audit_logs(category)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_action ON audit_logs(action)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_entityType ON audit_logs(entityType)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_entityId ON audit_logs(entityId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_createdAt ON audit_logs(createdAt)")
        }
    }

    private val migration8To9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_settings (
                    id INTEGER NOT NULL PRIMARY KEY,
                    themePreference TEXT NOT NULL,
                    densityPreference TEXT NOT NULL,
                    animationsEnabled INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO app_settings
                    (id, themePreference, densityPreference, animationsEnabled, updatedAt)
                VALUES (1, 'DARK', 'COMFORTABLE', 1, strftime('%s','now') * 1000)
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS competition_settings (
                    competitionId INTEGER NOT NULL PRIMARY KEY,
                    tieBreakCriteriaCsv TEXT NOT NULL,
                    highlightLeader INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(competitionId) REFERENCES competitions(id)
                        ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_competition_settings_competitionId ON competition_settings(competitionId)",
            )
        }
    }

    private val migration9To10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS season_closures (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, seasonId INTEGER NOT NULL, championClubId INTEGER NOT NULL, runnerUpClubId INTEGER, championPrizeCr INTEGER NOT NULL, runnerUpPrizeCr INTEGER NOT NULL, closedAt INTEGER NOT NULL, FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE CASCADE ON DELETE CASCADE, FOREIGN KEY(championClubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT, FOREIGN KEY(runnerUpClubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE SET NULL)""")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_season_closures_seasonId ON season_closures(seasonId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_season_closures_championClubId ON season_closures(championClubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_season_closures_runnerUpClubId ON season_closures(runnerUpClubId)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS final_standings (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, seasonId INTEGER NOT NULL, clubId INTEGER NOT NULL, position INTEGER NOT NULL, played INTEGER NOT NULL, wins INTEGER NOT NULL, draws INTEGER NOT NULL, losses INTEGER NOT NULL, goalsFor INTEGER NOT NULL, goalsAgainst INTEGER NOT NULL, goalDifference INTEGER NOT NULL, points INTEGER NOT NULL, archivedAt INTEGER NOT NULL, FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE CASCADE ON DELETE CASCADE, FOREIGN KEY(clubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_final_standings_seasonId ON final_standings(seasonId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_final_standings_clubId ON final_standings(clubId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_final_standings_seasonId_clubId ON final_standings(seasonId, clubId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_final_standings_seasonId_position ON final_standings(seasonId, position)")
        }
    }


    private val migration10To11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE season_closures ADD COLUMN participationPrizeCr INTEGER NOT NULL DEFAULT 0")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS competition_prizes (
                    competitionId INTEGER NOT NULL PRIMARY KEY,
                    championPrizeCr INTEGER NOT NULL,
                    runnerUpPrizeCr INTEGER NOT NULL,
                    participationPrizeCr INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(competitionId) REFERENCES competitions(id)
                        ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_competition_prizes_competitionId ON competition_prizes(competitionId)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS prize_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER NOT NULL,
                    competitionId INTEGER NOT NULL,
                    seasonId INTEGER NOT NULL,
                    clubId INTEGER NOT NULL,
                    prizeType TEXT NOT NULL,
                    amountCr INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    awardedAt INTEGER NOT NULL,
                    FOREIGN KEY(leagueId) REFERENCES leagues(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(competitionId) REFERENCES competitions(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(seasonId) REFERENCES seasons(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(clubId) REFERENCES clubs(id) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_prize_history_leagueId ON prize_history(leagueId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_prize_history_competitionId ON prize_history(competitionId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_prize_history_seasonId ON prize_history(seasonId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_prize_history_clubId ON prize_history(clubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_prize_history_awardedAt ON prize_history(awardedAt)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_prize_history_seasonId_clubId_prizeType ON prize_history(seasonId, clubId, prizeType)")
        }
    }


    private val migration11To12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER,
                    category TEXT NOT NULL,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    audience TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    sourceKey TEXT NOT NULL,
                    destinationRoute TEXT,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(leagueId) REFERENCES leagues(id) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_leagueId ON notifications(leagueId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_category ON notifications(category)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_audience ON notifications(audience)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_createdAt ON notifications(createdAt)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_notifications_sourceKey ON notifications(sourceKey)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notification_reads (
                    notificationId INTEGER NOT NULL,
                    userId INTEGER NOT NULL,
                    readAt INTEGER NOT NULL,
                    PRIMARY KEY(notificationId, userId),
                    FOREIGN KEY(notificationId) REFERENCES notifications(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_reads_userId ON notification_reads(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_reads_readAt ON notification_reads(readAt)")
        }
    }

    val migration12To13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE transfers ADD COLUMN type TEXT NOT NULL DEFAULT 'TRANSFER'")
            db.execSQL("ALTER TABLE transfers ADD COLUMN swapId TEXT")
            db.execSQL("ALTER TABLE transfers ADD COLUMN note TEXT")
        }
    }

    private val migration13To14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // CompetitionEntity
            db.execSQL("ALTER TABLE competitions ADD COLUMN groupCount INTEGER")
            db.execSQL("ALTER TABLE competitions ADD COLUMN qualifiedPerGroup INTEGER")
            db.execSQL("ALTER TABLE competitions ADD COLUMN knockoutLegs INTEGER NOT NULL DEFAULT 1")

            // RoundEntity
            db.execSQL("ALTER TABLE rounds ADD COLUMN stage TEXT NOT NULL DEFAULT 'REGULAR'")
            db.execSQL("ALTER TABLE rounds ADD COLUMN groupIndex INTEGER")

            // MatchEntity
            db.execSQL("ALTER TABLE matches ADD COLUMN stage TEXT NOT NULL DEFAULT 'REGULAR'")
            db.execSQL("ALTER TABLE matches ADD COLUMN groupIndex INTEGER")

            // StandingEntity
            db.execSQL("ALTER TABLE standings ADD COLUMN groupIndex INTEGER")
        }
    }

    private val migration14To15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // MatchEntity
            db.execSQL("ALTER TABLE matches ADD COLUMN bracketPosition INTEGER")
            db.execSQL("ALTER TABLE matches ADD COLUMN penaltiesHome INTEGER")
            db.execSQL("ALTER TABLE matches ADD COLUMN penaltiesAway INTEGER")
            db.execSQL("ALTER TABLE matches ADD COLUMN winnerClubId INTEGER")
            db.execSQL("ALTER TABLE matches ADD COLUMN advanceReason TEXT")

            // RoundEntity
            db.execSQL("ALTER TABLE rounds ADD COLUMN stageLabel TEXT")
        }
    }

    private val migration15_16 = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE users ADD COLUMN firebaseUid TEXT")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_firebaseUid ON users(firebaseUid)")
        }
    }

    private val migration16_17 = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sessions ADD COLUMN loginSource TEXT NOT NULL DEFAULT 'LOCAL'")
        }
    }

    private val migration17_18 = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE leagues ADD COLUMN cloudLeagueId TEXT")
            db.execSQL("ALTER TABLE leagues ADD COLUMN isOnline INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_leagues_cloudLeagueId ON leagues(cloudLeagueId)")
        }
    }

    val migration18_19 = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS online_sync_records (
                    entityType TEXT NOT NULL,
                    localId INTEGER NOT NULL,
                    cloudLeagueId TEXT NOT NULL,
                    cloudId TEXT NOT NULL,
                    revision INTEGER NOT NULL DEFAULT 0,
                    lastLocalUpdatedAt INTEGER NOT NULL DEFAULT 0,
                    lastRemoteUpdatedAt INTEGER NOT NULL DEFAULT 0,
                    status TEXT NOT NULL DEFAULT 'SYNCED',
                    conflictPayload TEXT,
                    PRIMARY KEY(entityType, localId)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_online_sync_records_cloudLeagueId_entityType_cloudId ON online_sync_records(cloudLeagueId, entityType, cloudId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_online_sync_records_status ON online_sync_records(status)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS online_sync_queue (
                    operationId TEXT NOT NULL PRIMARY KEY,
                    entityType TEXT NOT NULL,
                    localId INTEGER NOT NULL,
                    cloudLeagueId TEXT NOT NULL,
                    baseRevision INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    attempts INTEGER NOT NULL DEFAULT 0,
                    lastError TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_online_sync_queue_entityType_localId_status ON online_sync_queue(entityType, localId, status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_online_sync_queue_cloudLeagueId_status_createdAt ON online_sync_queue(cloudLeagueId, status, createdAt)")
        }
    }

    val migration19To20 = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS players (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER NOT NULL,
                    clubId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    position TEXT,
                    marketStatus TEXT NOT NULL,
                    askingPriceCr INTEGER,
                    skillImageUri TEXT,
                    notes TEXT,
                    externalPlayerId TEXT,
                    isActive INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(leagueId) REFERENCES leagues(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(clubId) REFERENCES clubs(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_players_leagueId ON players(leagueId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_players_clubId ON players(clubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_players_name ON players(name)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_players_marketStatus ON players(marketStatus)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_players_externalPlayerId ON players(externalPlayerId)")

            db.execSQL("ALTER TABLE transfers ADD COLUMN playerId INTEGER")
        }
    }

    val migration20To21 = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE news ADD COLUMN imageUri TEXT")
            db.execSQL("ALTER TABLE news ADD COLUMN authorUserId INTEGER")
        }
    }

    val migration21To22 = object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS arena_duels (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER NOT NULL,
                    clubAId INTEGER NOT NULL,
                    clubBId INTEGER NOT NULL,
                    stakeCr INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    resultType TEXT,
                    note TEXT,
                    createdByUserId INTEGER,
                    createdAt INTEGER NOT NULL,
                    resolvedAt INTEGER
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_arena_duels_leagueId ON arena_duels(leagueId)")
        }
    }

    val migration22To23 = object : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE players ADD COLUMN attributesRaw TEXT")
            db.execSQL("ALTER TABLE players ADD COLUMN overall INTEGER")
        }
    }

    val migration23To24 = object : Migration(23, 24) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE season_closures ADD COLUMN championPresidentUserId INTEGER")
            db.execSQL("ALTER TABLE transfers ADD COLUMN seasonId INTEGER")
        }
    }

    val migration24To25 = object : Migration(24, 25) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS goal_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    matchId INTEGER NOT NULL,
                    playerId INTEGER NOT NULL,
                    scoringClubId INTEGER NOT NULL,
                    isOwnGoal INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(matchId) REFERENCES matches(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(playerId) REFERENCES players(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(scoringClubId) REFERENCES clubs(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_events_matchId ON goal_events(matchId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_events_playerId ON goal_events(playerId)")
        }
    }

    val migration25To26 = object : Migration(25, 26) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS auction_lots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    leagueId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    startAt INTEGER NOT NULL,
                    endAt INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    closedAt INTEGER,
                    createdByUserId INTEGER,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_auction_lots_leagueId ON auction_lots(leagueId)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS auction_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    lotId INTEGER NOT NULL,
                    playerId INTEGER NOT NULL,
                    startingPriceCr INTEGER NOT NULL DEFAULT 5,
                    currentBidCr INTEGER,
                    leadingClubId INTEGER,
                    status TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_auction_items_lotId ON auction_items(lotId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_auction_items_playerId ON auction_items(playerId)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS auction_bids (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    itemId INTEGER NOT NULL,
                    clubId INTEGER NOT NULL,
                    amountCr INTEGER NOT NULL,
                    bidByUserId INTEGER,
                    status TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_auction_bids_itemId ON auction_bids(itemId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_auction_bids_clubId ON auction_bids(clubId)")
        }
    }

    private val migration26To27 = object : Migration(26, 27) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE matches ADD COLUMN homeYellowCards INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE matches ADD COLUMN homeRedCards INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE matches ADD COLUMN awayYellowCards INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE matches ADD COLUMN awayRedCards INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE competitions ADD COLUMN yellowCardFineCr INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE competitions ADD COLUMN redCardFineCr INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val migration27To28 = object : Migration(27, 28) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS club_presidencies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    clubId INTEGER NOT NULL,
                    userId INTEGER NOT NULL,
                    startAt INTEGER NOT NULL,
                    endAt INTEGER
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_club_presidencies_clubId ON club_presidencies(clubId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_club_presidencies_userId ON club_presidencies(userId)")

            // Semear um registro aberto para cada clube que já tem presidente hoje
            db.execSQL("""
                INSERT INTO club_presidencies (clubId, userId, startAt, endAt)
                SELECT id, presidentUserId, ${System.currentTimeMillis()}, NULL
                FROM clubs
                WHERE presidentUserId IS NOT NULL AND isBank = 0
            """)
        }
    }

    /** Complete, ordered migration chain. Keep new migrations appended and covered by tests. */
    val migrations: Array<Migration> = arrayOf(
        migration1To2,
        migration2To3,
        migration3To4,
        migration4To5,
        migration5To6,
        migration6To7,
        migration7To8,
        migration8To9,
        migration9To10,
        migration10To11,
        migration11To12,
        migration12To13,
        migration13To14,
        migration14To15,
        migration15_16,
        migration16_17,
        migration17_18,
        migration18_19,
        migration19To20,
        migration20To21,
        migration21To22,
        migration22To23,
        migration23To24,
        migration24To25,
        migration25To26,
        migration26To27,
        migration27To28,
    )

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    ).addMigrations(*migrations).build()

    @Provides fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
    @Provides fun provideSessionDao(database: AppDatabase): SessionDao = database.sessionDao()
    @Provides fun provideLeagueDao(database: AppDatabase): LeagueDao = database.leagueDao()
    @Provides fun provideClubDao(database: AppDatabase): ClubDao = database.clubDao()
    @Provides fun provideCompetitionDao(database: AppDatabase): CompetitionDao = database.competitionDao()
    @Provides fun provideCompetitionParticipantDao(database: AppDatabase): CompetitionParticipantDao = database.competitionParticipantDao()
    @Provides fun provideSeasonDao(database: AppDatabase): SeasonDao = database.seasonDao()
    @Provides fun provideRoundDao(database: AppDatabase): RoundDao = database.roundDao()
    @Provides fun provideMatchDao(database: AppDatabase): MatchDao = database.matchDao()
    @Provides fun provideStandingDao(database: AppDatabase): StandingDao = database.standingDao()
    @Provides fun provideFinancialDao(database: AppDatabase): FinancialDao = database.financialDao()
    @Provides fun provideTransferDao(database: AppDatabase): TransferDao = database.transferDao()
    @Provides fun provideNewsDao(database: AppDatabase): NewsDao = database.newsDao()
    @Provides fun provideNotificationDao(database: AppDatabase): NotificationDao = database.notificationDao()
    @Provides fun provideReportDao(database: AppDatabase): ReportDao = database.reportDao()
    @Provides fun providePlayerDao(database: AppDatabase): PlayerDao = database.playerDao()
    @Provides fun provideOnlineSyncDao(database: AppDatabase): OnlineSyncDao = database.onlineSyncDao()
    @Provides fun provideAuditLogDao(database: AppDatabase): AuditLogDao = database.auditLogDao()
    @Provides fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()
    @Provides fun provideHistoryDao(database: AppDatabase): HistoryDao = database.historyDao()
    @Provides fun provideSeasonClosureDao(database: AppDatabase): SeasonClosureDao = database.seasonClosureDao()
    @Provides fun providePrizeDao(database: AppDatabase): PrizeDao = database.prizeDao()
    @Provides fun providePresidentProfileDao(database: AppDatabase): com.example.legacymasterliga.core.database.dao.PresidentProfileDao = database.presidentProfileDao()
    @Provides fun provideClubPresidencyDao(database: AppDatabase): com.example.legacymasterliga.core.database.dao.ClubPresidencyDao = database.clubPresidencyDao()

    @Provides
    fun provideHallOfFameDao(database: AppDatabase): HallOfFameDao = database.hallOfFameDao()
    @Provides
    fun provideStatisticsDao(database: AppDatabase): StatisticsDao = database.statisticsDao()


    @Provides
    fun provideDashboardDao(database: AppDatabase): DashboardDao = database.dashboardDao()

    @Provides
    fun provideArenaDao(database: AppDatabase): com.example.legacymasterliga.core.database.dao.ArenaDao = database.arenaDao()

    @Provides
    fun provideGoalEventDao(database: AppDatabase): com.example.legacymasterliga.core.database.dao.GoalEventDao = database.goalEventDao()

    @Provides
    fun provideAuctionDao(database: AppDatabase): com.example.legacymasterliga.core.database.dao.AuctionDao = database.auctionDao()
}
