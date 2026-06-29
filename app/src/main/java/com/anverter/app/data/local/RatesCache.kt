package com.anverter.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anverter.app.data.remote.RatesParser
import com.anverter.app.domain.model.RatesSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rates")

/**
 * Persists the last successful `/exchange_rates` body so the converter works offline.
 * Stores the raw JSON to avoid a second serialization model; re-parsed on read.
 */
class RatesCache(context: Context) {
    private val store = context.applicationContext.dataStore

    val snapshot: Flow<RatesSnapshot?> = store.data.map { prefs ->
        val raw = prefs[KEY_RAW] ?: return@map null
        val updatedAt = prefs[KEY_UPDATED_AT] ?: return@map null
        runCatching { RatesParser.parse(raw) }
            .getOrNull()
            ?.let { RatesSnapshot(rates = it, updatedAtEpochMs = updatedAt) }
    }

    suspend fun save(rawBody: String, updatedAtEpochMs: Long) {
        store.edit { prefs ->
            prefs[KEY_RAW] = rawBody
            prefs[KEY_UPDATED_AT] = updatedAtEpochMs
        }
    }

    private companion object {
        val KEY_RAW = stringPreferencesKey("rates_raw")
        val KEY_UPDATED_AT = longPreferencesKey("rates_updated_at")
    }
}
