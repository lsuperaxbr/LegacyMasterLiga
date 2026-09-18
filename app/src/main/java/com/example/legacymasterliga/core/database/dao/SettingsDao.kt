package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.legacymasterliga.core.database.entity.AppSettingsEntity
import com.example.legacymasterliga.core.database.entity.CompetitionSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeAppSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultAppSettings(settings: AppSettingsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM competition_settings WHERE competitionId = :competitionId LIMIT 1")
    fun observeCompetitionSettings(competitionId: Long): Flow<CompetitionSettingsEntity?>

    @Query("SELECT * FROM competition_settings WHERE competitionId = :competitionId LIMIT 1")
    suspend fun findCompetitionSettings(competitionId: Long): CompetitionSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompetitionSettings(settings: CompetitionSettingsEntity)
}
