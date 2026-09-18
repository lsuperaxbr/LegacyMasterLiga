package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.DensityPreference
import com.example.legacymasterliga.core.model.ThemePreference

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val themePreference: ThemePreference = ThemePreference.DARK,
    val densityPreference: DensityPreference = DensityPreference.COMFORTABLE,
    val animationsEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object { const val SINGLETON_ID = 1 }
}
