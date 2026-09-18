package com.example.legacymasterliga.core.performance

import android.os.SystemClock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Snapshot agregado de uma operação monitorada. */
data class PerformanceMetric(
    val name: String,
    val count: Long = 0,
    val totalDurationMs: Long = 0,
    val lastDurationMs: Long = 0,
    val maxDurationMs: Long = 0,
    val lastUpdatedAt: Long = 0,
) {
    val averageDurationMs: Long
        get() = if (count == 0L) 0L else totalDurationMs / count
}

/**
 * Monitor leve e totalmente local. Não envia dados para serviços externos.
 * Pode ser usado em repositories, use cases e tarefas de manutenção.
 */
@Singleton
class PerformanceMonitor @Inject constructor() {
    private val values = ConcurrentHashMap<String, PerformanceMetric>()
    private val _metrics = MutableStateFlow<List<PerformanceMetric>>(emptyList())
    val metrics: StateFlow<List<PerformanceMetric>> = _metrics.asStateFlow()

    fun record(name: String, durationMs: Long) {
        values.compute(name) { _, previous ->
            val old = previous ?: PerformanceMetric(name = name)
            old.copy(
                count = old.count + 1,
                totalDurationMs = old.totalDurationMs + durationMs.coerceAtLeast(0),
                lastDurationMs = durationMs.coerceAtLeast(0),
                maxDurationMs = maxOf(old.maxDurationMs, durationMs.coerceAtLeast(0)),
                lastUpdatedAt = System.currentTimeMillis(),
            )
        }
        _metrics.update { values.values.sortedByDescending(PerformanceMetric::lastUpdatedAt) }
    }

    inline fun <T> measure(name: String, block: () -> T): T {
        val started = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            record(name, SystemClock.elapsedRealtime() - started)
        }
    }

    suspend inline fun <T> measureSuspend(name: String, crossinline block: suspend () -> T): T {
        val started = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            record(name, SystemClock.elapsedRealtime() - started)
        }
    }

    fun clear() {
        values.clear()
        _metrics.value = emptyList()
    }
}
