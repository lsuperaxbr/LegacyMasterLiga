package com.example.legacymasterliga.core.cache

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Cache LRU pequeno para dados derivados e consultas de apoio. */
@Singleton
class AppMemoryCache @Inject constructor() {
    private data class Entry(val value: Any, val expiresAt: Long)

    private val mutex = Mutex()
    private val values = object : LinkedHashMap<String, Entry>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>?): Boolean = size > MAX_ENTRIES
    }

    suspend fun <T : Any> get(key: String): T? = mutex.withLock {
        val entry = values[key] ?: return@withLock null
        if (entry.expiresAt < System.currentTimeMillis()) {
            values.remove(key)
            return@withLock null
        }
        @Suppress("UNCHECKED_CAST")
        entry.value as? T
    }

    suspend fun put(key: String, value: Any, ttlMs: Long = DEFAULT_TTL_MS) = mutex.withLock {
        values[key] = Entry(value, System.currentTimeMillis() + ttlMs.coerceAtLeast(1_000L))
    }

    suspend fun <T : Any> getOrPut(
        key: String,
        ttlMs: Long = DEFAULT_TTL_MS,
        loader: suspend () -> T,
    ): T {
        get<T>(key)?.let { return it }
        val loaded = loader()
        put(key, loaded, ttlMs)
        return loaded
    }

    suspend fun invalidate(prefix: String? = null) = mutex.withLock {
        if (prefix == null) values.clear() else values.keys.removeAll { it.startsWith(prefix) }
    }

    suspend fun size(): Int = mutex.withLock { values.size }

    private companion object {
        const val MAX_ENTRIES = 64
        const val DEFAULT_TTL_MS = 5 * 60 * 1_000L
    }
}
