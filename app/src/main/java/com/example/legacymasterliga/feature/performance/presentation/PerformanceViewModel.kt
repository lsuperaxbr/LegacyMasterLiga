package com.example.legacymasterliga.feature.performance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.cache.AppMemoryCache
import com.example.legacymasterliga.core.error.AppError
import com.example.legacymasterliga.core.error.AppErrorReporter
import com.example.legacymasterliga.core.performance.PerformanceMetric
import com.example.legacymasterliga.core.performance.PerformanceMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PerformanceUiState(
    val metrics: List<PerformanceMetric> = emptyList(),
    val errors: List<AppError> = emptyList(),
    val cacheEntries: Int = 0,
)

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val monitor: PerformanceMonitor,
    private val errorReporter: AppErrorReporter,
    private val memoryCache: AppMemoryCache,
) : ViewModel() {
    private val cacheSize = MutableStateFlow(0)

    val state: StateFlow<PerformanceUiState> = combine(
        monitor.metrics,
        errorReporter.errors,
        cacheSize,
    ) { metrics, errors, entries ->
        PerformanceUiState(metrics = metrics, errors = errors, cacheEntries = entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerformanceUiState())

    init { refreshCacheSize() }

    fun refreshCacheSize() {
        viewModelScope.launch { cacheSize.value = memoryCache.size() }
    }

    fun clearMetrics() = monitor.clear()
    fun clearErrors() = errorReporter.clear()
    fun clearCache() {
        viewModelScope.launch {
            memoryCache.invalidate()
            cacheSize.value = 0
        }
    }
}
