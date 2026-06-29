package com.anverter.app.data

import com.anverter.app.data.local.RatesCache
import com.anverter.app.data.remote.CoinGeckoClient
import com.anverter.app.data.remote.RatesParser
import com.anverter.app.domain.model.RatesSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Network-first with cache fallback. [cachedRates] is the source of truth the UI observes;
 * [refresh] updates it. No background scheduling — callers invoke [refresh] only while the app
 * is in the foreground and online.
 */
class RatesRepository(
    private val client: CoinGeckoClient,
    private val cache: RatesCache,
    private val now: () -> Long = System::currentTimeMillis,
) {
    val cachedRates: Flow<RatesSnapshot?> = cache.snapshot

    suspend fun refresh(): Result<RatesSnapshot> = runCatching {
        val raw = client.fetchRaw()
        val rates = RatesParser.parse(raw)
        val timestamp = now()
        cache.save(raw, timestamp)
        RatesSnapshot(rates = rates, updatedAtEpochMs = timestamp)
    }
}
