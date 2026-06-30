package com.anverter.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anverter.app.di.AppContainer
import com.anverter.app.feature.calculator.CalculatorViewModel
import com.anverter.app.feature.converter.ConverterViewModel
import com.anverter.app.feature.settings.SettingsViewModel

/** Builds every screen view model from the [AppContainer]. */
fun anverterViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        ConverterViewModel(
            container.ratesRepository,
            container.connectivityObserver,
            container.settingsStore,
        )
    }
    initializer { CalculatorViewModel() }
    initializer { SettingsViewModel(container.settingsStore) }
}
