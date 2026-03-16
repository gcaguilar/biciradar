package com.gcaguilar.bizizaragoza.core.geo

import com.gcaguilar.bizizaragoza.core.GeoPoint
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Executes a geographic place search against datosbizi.com.
 *
 * Deduplication: if a search for the exact same (normalized) query is
 * already in-flight, the second caller waits and reuses the result.
 * This is enforced via a per-key Mutex (one slot — not a full cache).
 */
@Inject
class GeoSearchUseCase(
    private val geoApi: GeoApi,
) {
    private val inFlight = mutableMapOf<String, Mutex>()
    private val inFlightLock = Mutex()

    @kotlin.concurrent.Volatile
    private var lastKey: String? = null

    @kotlin.concurrent.Volatile
    private var lastResult: List<GeoResult>? = null

    /**
     * @param query Raw user input (will be trimmed + lowercased for dedup purposes).
     * @param bias Optional location to bias results.
     */
    suspend fun execute(query: String, bias: GeoPoint? = null): List<GeoResult> {
        val key = query.trim().lowercase()
        if (key.isBlank()) return emptyList()

        // Return cached result immediately if the key matches the last completed search
        if (key == lastKey) lastResult?.let { return it }

        val mutex = inFlightLock.withLock {
            inFlight.getOrPut(key) { Mutex() }
        }

        return mutex.withLock {
            // Another coroutine may have completed the same search while we waited
            if (key == lastKey) lastResult ?: fetchAndCache(key, bias)
            else fetchAndCache(key, bias)
        }.also {
            inFlightLock.withLock { inFlight.remove(key) }
        }
    }

    private suspend fun fetchAndCache(key: String, bias: GeoPoint?): List<GeoResult> {
        val result = geoApi.search(key, bias)
        lastKey = key
        lastResult = result
        return result
    }
}
