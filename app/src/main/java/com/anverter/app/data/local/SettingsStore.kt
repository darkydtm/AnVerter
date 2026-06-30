package com.anverter.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anverter.app.ui.NavBarStyle
import com.anverter.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(context: Context) {
    private val store = context.applicationContext.settingsDataStore

    val themeMode: Flow<ThemeMode> = store.data.map { prefs ->
        prefs[KEY_THEME_MODE]
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[KEY_THEME_MODE] = mode.name }
    }

    val navBarStyle: Flow<NavBarStyle> = store.data.map { prefs ->
        prefs[KEY_NAV_BAR_STYLE]
            ?.let { runCatching { NavBarStyle.valueOf(it) }.getOrNull() }
            ?: NavBarStyle.TABS
    }

    suspend fun setNavBarStyle(style: NavBarStyle) {
        store.edit { it[KEY_NAV_BAR_STYLE] = style.name }
    }

    val favoriteCurrencies: Flow<Set<String>> = store.data.map { prefs ->
        prefs[KEY_FAVORITES] ?: emptySet()
    }

    suspend fun toggleFavorite(code: String) {
        store.edit { prefs ->
            val current = prefs[KEY_FAVORITES] ?: emptySet()
            prefs[KEY_FAVORITES] = if (code in current) current - code else current + code
        }
    }

    val recentConversions: Flow<List<RecentConversion>> = store.data.map { prefs ->
        prefs[KEY_RECENTS]?.let {
            runCatching { Json.decodeFromString<List<RecentConversion>>(it) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun addRecentConversion(conversion: RecentConversion) {
        store.edit { prefs ->
            val current = prefs[KEY_RECENTS]
                ?.let { runCatching { Json.decodeFromString<List<RecentConversion>>(it) }.getOrNull() }
                ?: emptyList()
            prefs[KEY_RECENTS] = Json.encodeToString(current.withMostRecent(conversion))
        }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_NAV_BAR_STYLE = stringPreferencesKey("nav_bar_style")
        val KEY_FAVORITES = stringSetPreferencesKey("favorite_currencies")
        val KEY_RECENTS = stringPreferencesKey("recent_conversions")
    }
}
