package com.example.legacymasterliga.core.error

import android.util.Log
import java.util.ArrayDeque
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppError(
    val source: String,
    val message: String,
    val occurredAt: Long = System.currentTimeMillis(),
)

/** Registro local de erros recuperáveis. Nenhum dado é enviado para fora do aparelho. */
@Singleton
class AppErrorReporter @Inject constructor() {
    private val recent = ArrayDeque<AppError>()
    private val _errors = MutableStateFlow<List<AppError>>(emptyList())
    val errors: StateFlow<List<AppError>> = _errors.asStateFlow()

    @Synchronized
    fun report(source: String, throwable: Throwable, fallbackMessage: String = "Ocorreu um erro inesperado.") {
        Log.e(TAG, "[$source] ${throwable.message}", throwable)
        recent.addFirst(AppError(source, throwable.message?.takeIf(String::isNotBlank) ?: fallbackMessage))
        while (recent.size > MAX_ERRORS) recent.removeLast()
        _errors.value = recent.toList()
    }

    @Synchronized
    fun clear() {
        recent.clear()
        _errors.value = emptyList()
    }

    private companion object {
        const val TAG = "LegacyErrorReporter"
        const val MAX_ERRORS = 20
    }
}
