package com.anverter.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anverter.app.ui.NavBarStyle
import com.anverter.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_NAV_BAR_STYLE = stringPreferencesKey("nav_bar_style")
    }
}
