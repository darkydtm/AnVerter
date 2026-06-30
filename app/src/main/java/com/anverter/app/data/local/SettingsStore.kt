package com.anverter.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anverter.app.ui.NavBarStyle
import com.anverter.app.ui.SoundFeedback
import com.anverter.app.ui.UiStyle
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

    val uiStyle: Flow<UiStyle> = store.data.map { prefs ->
        prefs[KEY_UI_STYLE]
            ?.let { runCatching { UiStyle.valueOf(it) }.getOrNull() }
            ?: UiStyle.MIUIX
    }

    suspend fun setUiStyle(style: UiStyle) {
        store.edit { it[KEY_UI_STYLE] = style.name }
    }

    val soundFeedback: Flow<SoundFeedback> = store.data.map { prefs ->
        prefs[KEY_SOUND_FEEDBACK]
            ?.let { runCatching { SoundFeedback.valueOf(it) }.getOrNull() }
            ?: SoundFeedback.ON
    }

    suspend fun setSoundFeedback(sound: SoundFeedback) {
        store.edit { it[KEY_SOUND_FEEDBACK] = sound.name }
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

    val calculatorHistory: Flow<List<CalculatorHistoryItem>> = store.data.map { prefs ->
        prefs[KEY_CALCULATOR_HISTORY]?.let {
            runCatching { Json.decodeFromString<List<CalculatorHistoryItem>>(it) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun addCalculatorHistory(item: CalculatorHistoryItem) {
        store.edit { prefs ->
            val current = prefs[KEY_CALCULATOR_HISTORY]
                ?.let { runCatching { Json.decodeFromString<List<CalculatorHistoryItem>>(it) }.getOrNull() }
                ?: emptyList()
            prefs[KEY_CALCULATOR_HISTORY] = Json.encodeToString((listOf(item) + current).take(MAX_CALCULATOR_HISTORY))
        }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_NAV_BAR_STYLE = stringPreferencesKey("nav_bar_style")
        val KEY_UI_STYLE = stringPreferencesKey("ui_style")
        val KEY_SOUND_FEEDBACK = stringPreferencesKey("sound_feedback")
        val KEY_FAVORITES = stringSetPreferencesKey("favorite_currencies")
        val KEY_RECENTS = stringPreferencesKey("recent_conversions")
        val KEY_CALCULATOR_HISTORY = stringPreferencesKey("calculator_history")
    }
}
