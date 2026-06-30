package com.anverter.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anverter.app.data.local.SettingsStore
import com.anverter.app.ui.NavBarStyle
import com.anverter.app.ui.SoundFeedback
import com.anverter.app.ui.UiStyle
import com.anverter.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val store: SettingsStore,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = store.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.SYSTEM,
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }

    val navBarStyle: StateFlow<NavBarStyle> = store.navBarStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NavBarStyle.TABS,
    )

    fun setNavBarStyle(style: NavBarStyle) {
        viewModelScope.launch { store.setNavBarStyle(style) }
    }

    val uiStyle: StateFlow<UiStyle> = store.uiStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UiStyle.MIUIX,
    )

    fun setUiStyle(style: UiStyle) {
        viewModelScope.launch { store.setUiStyle(style) }
    }

    val soundFeedback: StateFlow<SoundFeedback> = store.soundFeedback.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SoundFeedback.ON,
    )

    fun setSoundFeedback(sound: SoundFeedback) {
        viewModelScope.launch { store.setSoundFeedback(sound) }
    }

    val calculatorExtendedMode: StateFlow<Boolean> = store.calculatorExtendedMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )

    fun setCalculatorExtendedMode(enabled: Boolean) {
        viewModelScope.launch { store.setCalculatorExtendedMode(enabled) }
    }
}
