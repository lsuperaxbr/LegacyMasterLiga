package com.example.legacymasterliga

import android.app.Application
import android.util.Log
import com.example.legacymasterliga.domain.usecase.InitializeDefaultDataUseCase
import com.example.legacymasterliga.domain.usecase.InitializeDemoDataUseCase
import com.example.legacymasterliga.core.error.AppErrorReporter
import com.example.legacymasterliga.core.network.FirebaseConnectionChecker
import com.example.legacymasterliga.core.performance.PerformanceMonitor
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import com.example.legacymasterliga.feature.backup.domain.BackupScheduler
import com.example.legacymasterliga.feature.online.domain.OnlineLoginService
import com.example.legacymasterliga.feature.online.sync.OnlineSportsSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class LegacyMasterLigaApp : Application() {
    @Inject
    lateinit var initializeDefaultData: InitializeDefaultDataUseCase

    @Inject
    lateinit var initializeDemoData: InitializeDemoDataUseCase

    @Inject
    lateinit var adminLeagueSetup: com.example.legacymasterliga.domain.usecase.AdminLeagueSetupUseCase

    @Inject lateinit var backupRepository: BackupRepository
    @Inject lateinit var backupScheduler: BackupScheduler
    @Inject lateinit var errorReporter: AppErrorReporter
    @Inject lateinit var performanceMonitor: PerformanceMonitor
    @Inject lateinit var firebaseChecker: FirebaseConnectionChecker
    @Inject lateinit var onlineLoginService: OnlineLoginService
    @Inject lateinit var onlineSportsSyncManager: OnlineSportsSyncManager

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Falha ao inicializar os dados padrão.", throwable)
            errorReporter.report("Inicialização", throwable, "Não foi possível preparar os dados iniciais.")
        },
    )

    override fun onCreate() {
        // Captura de erros: PRIMEIRA linha, antes de qualquer outra coisa
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            // Se for um erro de rede do Firebase durante sync de fundo, ignorar crash fatal
            val isNetworkError = throwable.toString().contains("FirebaseFirestoreException") || 
                                 throwable.toString().contains("UnknownHostException")

            if (!isNetworkError) {
                try {
                    val stackTrace = Log.getStackTraceString(throwable)
                    val prefs = getSharedPreferences("crash_reports", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("last_crash", stackTrace)
                        .putLong("last_crash_time", System.currentTimeMillis())
                        .commit()

                    // Abre a tela de erro IMEDIATAMENTE para erros reais
                    val intent = android.content.Intent(this, CrashReportActivity::class.java).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                } catch (e: Exception) { }

                android.os.Process.killProcess(android.os.Process.myPid())
                kotlin.system.exitProcess(1)
            } else {
                Log.w(TAG, "Ignorado crash fatal por erro de rede: $throwable")
            }
        }

        super.onCreate()
        backupScheduler.apply(backupRepository.getAutomaticFrequency())
        
        firebaseChecker.checkConnection()
        applicationScope.launch {
            performanceMonitor.measureSuspend("app.initialize_default_data") { 
                initializeDefaultData()
            }
        }
        applicationScope.launch {
            onlineLoginService.restoreAuthenticatedSession()
                .onSuccess { profile ->
                    if (profile != null) onlineSportsSyncManager.startAuthenticatedSession(profile)
                }
                .onFailure { error ->
                    Log.w(TAG, "Não foi possível restaurar a sessão Firebase; a sessão local foi preservada.", error)
                }
        }
    }

    private companion object {
        const val TAG = "LegacyMasterLigaApp"
    }
}
