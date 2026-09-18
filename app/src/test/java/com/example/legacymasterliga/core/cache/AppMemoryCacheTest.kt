package com.example.legacymasterliga.core.cache

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppMemoryCacheTest {
    @Test fun stores_reads_and_invalidates_values() = runBlocking {
        val cache = AppMemoryCache()
        cache.put("league:1", "Liga M L Amigos")
        assertEquals("Liga M L Amigos", cache.get<String>("league:1"))
        cache.invalidate("league:")
        assertNull(cache.get<String>("league:1"))
    }

    @Test fun get_or_put_runs_loader_only_once() = runBlocking {
        val cache = AppMemoryCache()
        var calls = 0
        val first = cache.getOrPut("clubs") { calls++; 4 }
        val second = cache.getOrPut("clubs") { calls++; 8 }
        assertEquals(4, first)
        assertEquals(4, second)
        assertEquals(1, calls)
    }
}
