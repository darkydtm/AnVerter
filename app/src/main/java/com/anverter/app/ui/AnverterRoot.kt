package com.anverter.app.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold

private data class Tab(val labelRes: Int, val icon: ImageVector)

internal fun tabIndexForOffset(x: Float, width: Int, tabCount: Int): Int {
    if (width <= 0 || tabCount <= 0) return 0
    val clampedX = x.coerceIn(0f, width.toFloat())
    return ((clampedX / width.toFloat()) * tabCount)
        .toInt()
        .coerceIn(0, tabCount - 1)
}

/** Dragging across the floating bar selects the tab under the finger. */
private fun swipeTabsModifier(
    tabCount: Int,
    selectTab: (Int) -> Unit,
): Modifier = Modifier.pointerInput(tabCount, selectTab) {
    var lastSelected = -1

    fun selectAt(x: Float) {
        val index = tabIndexForOffset(x, size.width, tabCount)
        if (index != lastSelected) {
            lastSelected = index
            selectTab(index)
        }
    }

    detectHorizontalDragGestures(
        onDragStart = { offset -> selectAt(offset.x) },
        onDragEnd = { lastSelected = -1 },
        onDragCancel = { lastSelected = -1 },
    ) { change, _ ->
        selectAt(change.position.x)
        change.consume()
    }
}

@Composable
fun AnverterRoot(container: AppContainer) {
    val factory = remember(container) { anverterViewModelFactory(container) }
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val navBarStyle by settingsViewModel.navBarStyle.collectAsStateWithLifecycle()

    AnverterTheme(themeMode = themeMode) {
        AnverterApp(
            converterViewModel = viewModel(factory = factory),
            calculatorViewModel = viewModel(factory = factory),
            settingsViewModel = settingsViewModel,
            navBarStyle = navBarStyle,
        )
    }
}

@Composable
private fun AnverterApp(
    converterViewModel: ConverterViewModel,
    calculatorViewModel: CalculatorViewModel,
    settingsViewModel: SettingsViewModel,
    navBarStyle: NavBarStyle,
) {
    val tabs = listOf(
        Tab(R.string.tab_converter, Icons.Filled.CurrencyExchange),
        Tab(R.string.tab_calculator, Icons.Filled.Calculate),
        Tab(R.string.tab_settings, Icons.Filled.Settings),
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val selected = pagerState.targetPage
    val goTo: (Int) -> Unit = { index ->
        scope.launch { pagerState.animateScrollToPage(index.coerceIn(0, tabs.lastIndex)) }
    }
    val dragTo: (Int) -> Unit = { index ->
        val page = index.coerceIn(0, tabs.lastIndex)
        if (pagerState.targetPage != page) {
            scope.launch { pagerState.scrollToPage(page) }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            when (navBarStyle) {
                NavBarStyle.SLIDER -> Box(modifier = swipeTabsModifier(tabs.size, dragTo)) {
                    FloatingNavigationBar {
                        tabs.forEachIndexed { index, tab ->
                            FloatingNavigationBarItem(
                                selected = selected == index,
                                onClick = { goTo(index) },
                                icon = tab.icon,
                                label = stringResource(tab.labelRes),
                            )
                        }
                    }
                }

                NavBarStyle.TABS -> NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selected == index,
                            onClick = { goTo(index) },
                            icon = tab.icon,
                            label = stringResource(tab.labelRes),
                        )
                    }
                }
            }
        },
    ) { padding ->
        val bottomPadding = padding.calculateBottomPadding()
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            when (page) {
                0 -> ConverterScreen(converterViewModel, bottomPadding = bottomPadding)
                1 -> CalculatorScreen(calculatorViewModel, bottomPadding = bottomPadding)
                else -> SettingsScreen(settingsViewModel, bottomPadding = bottomPadding)
            }
        }
    }
}
