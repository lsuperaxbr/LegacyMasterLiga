package com.example.legacymasterliga

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.core.navigation.LegacyNavGraph
import com.example.legacymasterliga.ui.theme.AppThemeViewModel
import com.example.legacymasterliga.ui.theme.LegacyMasterLigaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: AppThemeViewModel by viewModels()

    @javax.inject.Inject
    lateinit var database: com.example.legacymasterliga.core.database.AppDatabase

    @javax.inject.Inject
    lateinit var bootstrap: com.example.legacymasterliga.domain.usecase.InitializeDefaultDataUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificacao de reinício pós-restauração
        val prefs = getSharedPreferences("legacy_backup_preferences", Context.MODE_PRIVATE)
        if (prefs.getBoolean("restore_pending_restart", false)) {
            prefs.edit().putBoolean("restore_pending_restart", false).apply()
            
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent?.component)
            startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
            return
        }

        enableEdgeToEdge()
        setContent {
            var showCrashReport by remember {
                val crashPrefs = getSharedPreferences("crash_reports", Context.MODE_PRIVATE)
                mutableStateOf(crashPrefs.contains("last_crash"))
            }

            if (showCrashReport) {
                com.example.legacymasterliga.feature.crashreport.presentation.CrashReportScreen(
                    onDismiss = { showCrashReport = false }
                )
            } else {
                val themePreference by themeViewModel.themePreference.collectAsStateWithLifecycle()
                LegacyMasterLigaTheme(themePreference = themePreference) {
                    LegacyNavGraph()
                }
            }
        }
    }
}
