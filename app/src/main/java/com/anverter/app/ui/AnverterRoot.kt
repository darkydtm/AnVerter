package com.anverter.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anverter.app.R
import com.anverter.app.di.AppContainer
import com.anverter.app.feature.calculator.CalculatorScreen
import com.anverter.app.feature.calculator.CalculatorViewModel
import com.anverter.app.feature.converter.ConverterScreen
import com.anverter.app.feature.converter.ConverterViewModel
import com.anverter.app.feature.settings.SettingsScreen
import com.anverter.app.feature.settings.SettingsViewModel
import com.anverter.app.ui.theme.AnverterTheme
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold

private data class Tab(val labelRes: Int, val icon: ImageVector)

@Composable
fun AnverterRoot(container: AppContainer) {
    val factory = remember(container) { anverterViewModelFactory(container) }
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    AnverterTheme(themeMode = themeMode) {
        AnverterApp(
            converterViewModel = viewModel(factory = factory),
            calculatorViewModel = viewModel(factory = factory),
            settingsViewModel = settingsViewModel,
        )
    }
}

@Composable
private fun AnverterApp(
    converterViewModel: ConverterViewModel,
    calculatorViewModel: CalculatorViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val tabs = listOf(
        Tab(R.string.tab_converter, Icons.Filled.CurrencyExchange),
        Tab(R.string.tab_calculator, Icons.Filled.Calculate),
        Tab(R.string.tab_settings, Icons.Filled.Settings),
    )
    var selected by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = tab.icon,
                        label = stringResource(tab.labelRes),
                    )
                }
            }
        },
    ) { padding ->
        val bottomPadding = padding.calculateBottomPadding()
        Box(modifier = Modifier.fillMaxSize()) {
            when (selected) {
                0 -> ConverterScreen(converterViewModel, bottomPadding = bottomPadding)
                1 -> CalculatorScreen(calculatorViewModel, bottomPadding = bottomPadding)
                else -> SettingsScreen(settingsViewModel, bottomPadding = bottomPadding)
            }
        }
    }
}
