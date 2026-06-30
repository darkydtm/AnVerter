package com.anverter.app.feature.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anverter.app.BuildConfig
import com.anverter.app.R
import com.anverter.app.ui.NavBarStyle
import com.anverter.app.ui.SoundFeedback
import com.anverter.app.ui.UiStyle
import com.anverter.app.ui.adaptive.AppCard
import com.anverter.app.ui.adaptive.AppDropdownPreference
import com.anverter.app.ui.adaptive.AppPreferenceRow
import com.anverter.app.ui.adaptive.AppSmallTitle
import com.anverter.app.ui.adaptive.AppTopBar
import com.anverter.app.ui.theme.ThemeMode

private val THEME_ORDER = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
private val UI_STYLE_ORDER = listOf(UiStyle.MIUIX, UiStyle.MATERIAL3)
private val NAV_BAR_ORDER = listOf(NavBarStyle.TABS, NavBarStyle.SLIDER)
private val SOUND_ORDER = listOf(SoundFeedback.ON, SoundFeedback.OFF)
private val CALCULATOR_ORDER = listOf(false, true)
private const val GPL_URL = "https://www.gnu.org/licenses/gpl-3.0.html"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    bottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val uiStyle by viewModel.uiStyle.collectAsStateWithLifecycle()
    val navBarStyle by viewModel.navBarStyle.collectAsStateWithLifecycle()
    val soundFeedback by viewModel.soundFeedback.collectAsStateWithLifecycle()
    val calculatorExtendedMode by viewModel.calculatorExtendedMode.collectAsStateWithLifecycle()

    val themeLabels = listOf(
        stringResource(R.string.settings_theme_system),
        stringResource(R.string.settings_theme_light),
        stringResource(R.string.settings_theme_dark),
    )

    val navBarLabels = listOf(
        stringResource(R.string.settings_navbar_tabs),
        stringResource(R.string.settings_navbar_slider),
    )

    val uiStyleLabels = listOf(
        stringResource(R.string.settings_design_miuix),
        stringResource(R.string.settings_design_material3),
    )

    val soundLabels = listOf(
        stringResource(R.string.settings_sound_on),
        stringResource(R.string.settings_sound_off),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        AppTopBar(title = stringResource(R.string.settings_title))

        AppSmallTitle(text = stringResource(R.string.settings_theme))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppDropdownPreference(
                title = stringResource(R.string.settings_theme),
                items = themeLabels,
                selectedIndex = THEME_ORDER.indexOf(themeMode).coerceAtLeast(0),
                onSelectedIndexChange = { index -> viewModel.setThemeMode(THEME_ORDER[index]) },
                icon = Icons.Filled.Palette,
            )
        }

        AppSmallTitle(text = stringResource(R.string.settings_design_style))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppDropdownPreference(
                title = stringResource(R.string.settings_design_style),
                items = uiStyleLabels,
                selectedIndex = UI_STYLE_ORDER.indexOf(uiStyle).coerceAtLeast(0),
                onSelectedIndexChange = { index -> viewModel.setUiStyle(UI_STYLE_ORDER[index]) },
                icon = Icons.Filled.Style,
            )
        }

        AppSmallTitle(text = stringResource(R.string.settings_navbar))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppDropdownPreference(
                title = stringResource(R.string.settings_navbar),
                items = navBarLabels,
                selectedIndex = NAV_BAR_ORDER.indexOf(navBarStyle).coerceAtLeast(0),
                onSelectedIndexChange = { index -> viewModel.setNavBarStyle(NAV_BAR_ORDER[index]) },
                icon = Icons.Filled.Navigation,
            )
        }

        AppSmallTitle(text = stringResource(R.string.settings_sound_feedback))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppDropdownPreference(
                title = stringResource(R.string.settings_sound_feedback),
                items = soundLabels,
                selectedIndex = SOUND_ORDER.indexOf(soundFeedback).coerceAtLeast(0),
                onSelectedIndexChange = { index -> viewModel.setSoundFeedback(SOUND_ORDER[index]) },
                icon = Icons.Filled.VolumeUp,
            )
        }

        AppSmallTitle(text = stringResource(R.string.settings_calculator))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppDropdownPreference(
                title = stringResource(R.string.settings_calculator),
                items = listOf(
                    stringResource(R.string.settings_calculator_basic),
                    stringResource(R.string.settings_calculator_extended),
                ),
                selectedIndex = CALCULATOR_ORDER.indexOf(calculatorExtendedMode).coerceAtLeast(0),
                onSelectedIndexChange = { index -> viewModel.setCalculatorExtendedMode(CALCULATOR_ORDER[index]) },
                icon = Icons.Filled.Style,
            )
        }

        AppSmallTitle(text = stringResource(R.string.settings_about))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppPreferenceRow(
                title = stringResource(R.string.settings_source),
                summary = stringResource(R.string.settings_source_value),
                icon = Icons.Filled.Cloud,
            )
            AppPreferenceRow(
                title = stringResource(R.string.settings_license),
                summary = stringResource(R.string.settings_license_value),
                icon = Icons.Filled.Gavel,
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, GPL_URL.toUri()))
                },
            )
            AppPreferenceRow(
                title = stringResource(R.string.settings_version),
                summary = BuildConfig.VERSION_NAME,
                icon = Icons.Filled.Info,
            )
        }

        Spacer(Modifier.height(bottomPadding + 16.dp))
    }
}
