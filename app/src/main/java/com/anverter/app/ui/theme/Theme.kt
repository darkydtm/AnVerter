package com.anverter.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.anverter.app.ui.UiStyle
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.platformDynamicColors

enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

val LocalAppDarkTheme = compositionLocalOf { false }
val LocalAppAmoledTheme = compositionLocalOf { false }

private val appLightColorScheme: ColorScheme = lightColorScheme(
	primary = Color(0xFF0E9F6E),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFFBAF7D0),
	onPrimaryContainer = Color(0xFF173124),
	secondaryContainer = Color(0xFFFFF2A8),
	onSecondaryContainer = Color(0xFF173124),
	tertiaryContainer = Color(0xFFFFC7B8),
	onTertiaryContainer = Color(0xFF4A2B20),
	background = Color(0xFFF8FAFF),
	onBackground = Color(0xFF182129),
	surface = Color(0xFFFFFFFF),
	onSurface = Color(0xFF182129),
	surfaceVariant = Color(0xFFFFC7B8),
	onSurfaceVariant = Color(0xFF5A6472),
	error = Color(0xFFBA1A1A),
)

private val appDarkColorScheme: ColorScheme = darkColorScheme(
	primary = Color(0xFF55D89A),
	onPrimary = Color(0xFF053122),
	primaryContainer = Color(0xFF1F5A3D),
	onPrimaryContainer = Color(0xFFD7F8E3),
	secondaryContainer = Color(0xFF5A4B0B),
	onSecondaryContainer = Color(0xFFD7F8E3),
	tertiaryContainer = Color(0xFF63392E),
	onTertiaryContainer = Color(0xFFFFDBD1),
	background = Color(0xFF101418),
	onBackground = Color(0xFFE3EAF2),
	surface = Color(0xFF1A222B),
	onSurface = Color(0xFFE3EAF2),
	surfaceVariant = Color(0xFF63392E),
	onSurfaceVariant = Color(0xFFB0BCC9),
	error = Color(0xFFFFB4AB),
)

private val appAmoledColorScheme: ColorScheme = darkColorScheme(
	primary = Color(0xFF55D89A),
	onPrimary = Color(0xFF053122),
	primaryContainer = Color(0xFF163D2B),
	onPrimaryContainer = Color(0xFFD7F8E3),
	secondaryContainer = Color(0xFF403500),
	onSecondaryContainer = Color(0xFFFFF2A8),
	tertiaryContainer = Color(0xFF4F261D),
	onTertiaryContainer = Color(0xFFFFDBD1),
	background = Color(0xFF000000),
	onBackground = Color(0xFFEAF1F8),
	surface = Color(0xFF000000),
	onSurface = Color(0xFFEAF1F8),
	surfaceVariant = Color(0xFF1C1C1C),
	onSurfaceVariant = Color(0xFFBEC7D2),
	error = Color(0xFFFFB4AB),
)

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
		ThemeMode.AMOLED -> true
	}
	val amoled = themeMode == ThemeMode.AMOLED
	CompositionLocalProvider(
		LocalAppDarkTheme provides dark,
		LocalAppAmoledTheme provides amoled,
	) {
		when (uiStyle) {
			UiStyle.MIUIX -> MiuixTheme(
				colors = platformDynamicColors(dark = dark),
				content = content,
			)
			UiStyle.MATERIAL3 -> MaterialTheme(
				colorScheme = when {
					amoled -> appAmoledColorScheme
					dark -> appDarkColorScheme
					else -> appLightColorScheme
				},
				content = content,
			)
		}
	}
}
