package com.anverter.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.anverter.app.ui.UiStyle
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.platformDynamicColors

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Wraps content in the selected UI style and derives light/dark colors from [themeMode].
 * Miuix and Material 3 both use dynamic system colors on Android 12+.
 */
@Composable
fun AnverterTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    uiStyle: UiStyle = UiStyle.MIUIX,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    when (uiStyle) {
        UiStyle.MIUIX -> MiuixTheme(colors = platformDynamicColors(dark = dark), content = content)
        UiStyle.MATERIAL3 -> MaterialTheme(
            colorScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (dark) darkColorScheme() else lightColorScheme()
            },
            content = content,
        )
    }
}
