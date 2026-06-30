package com.anverter.app.feature.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.anverter.app.ui.theme.ThemeMode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference

private val THEME_ORDER = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
private val NAV_BAR_ORDER = listOf(NavBarStyle.TABS, NavBarStyle.SLIDER)
private const val GPL_URL = "https://www.gnu.org/licenses/gpl-3.0.html"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    bottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val navBarStyle by viewModel.navBarStyle.collectAsStateWithLifecycle()

    val themeLabels = listOf(
        stringResource(R.string.settings_theme_system),
        stringResource(R.string.settings_theme_light),
        stringResource(R.string.settings_theme_dark),
    )

    val navBarLabels = listOf(
        stringResource(R.string.settings_navbar_tabs),
        stringResource(R.string.settings_navbar_slider),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(title = stringResource(R.string.settings_title))

        SmallTitle(text = stringResource(R.string.settings_theme))
        Card(modifier = Modifier.fillMaxWidth()) {
            WindowDropdownPreference(
                title = stringResource(R.string.settings_theme),
                items = themeLabels,
                selectedIndex = THEME_ORDER.indexOf(themeMode).coerceAtLeast(0),
                onSelectedIndexChange = { index -> viewModel.setThemeMode(THEME_ORDER[index]) },
            )
        }

        SmallTitle(text = stringResource(R.string.settings_navbar))
        Card(modifier = Modifier.fillMaxWidth()) {
            WindowDropdownPreference(
                title = stringResource(R.string.settings_navbar),
                items = navBarLabels,
                selectedIndex = NAV_BAR_ORDER.indexOf(navBarStyle).coerceAtLeast(0),
                onSelectedIndexChange = { index -> viewModel.setNavBarStyle(NAV_BAR_ORDER[index]) },
            )
        }

        SmallTitle(text = stringResource(R.string.settings_about))
        Card(modifier = Modifier.fillMaxWidth()) {
            ArrowPreference(
                title = stringResource(R.string.settings_source),
                summary = stringResource(R.string.settings_source_value),
            )
            ArrowPreference(
                title = stringResource(R.string.settings_license),
                summary = stringResource(R.string.settings_license_value),
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, GPL_URL.toUri()))
                },
            )
            ArrowPreference(
                title = stringResource(R.string.settings_version),
                summary = BuildConfig.VERSION_NAME,
            )
        }

        Spacer(Modifier.height(bottomPadding + 16.dp))
    }
}
