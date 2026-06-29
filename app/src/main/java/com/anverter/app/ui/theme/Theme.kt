package com.anverter.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.platformDynamicColors

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Wraps content in [MiuixTheme] using Material You dynamic colors derived from the system
 * Monet palette (Android 12+). [themeMode] only decides light vs dark; the hue follows the
 * wallpaper-based system palette.
 */
@Composable
fun AnverterTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MiuixTheme(colors = platformDynamicColors(dark = dark), content = content)
}
