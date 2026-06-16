package com.markalon.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.markalon.app.data.Accents
import com.markalon.app.data.Settings
import com.markalon.app.data.ThemeMode

/**
 * Applies Markalon's design tokens. The custom palette is exposed through
 * [LocalMarkalonColors] / [LocalAccent]; a matching Material 3 [MaterialTheme]
 * is provided so standard M3 components (ripples, text selection) inherit it.
 */
@Composable
fun MarkalonTheme(
    settings: Settings,
    content: @Composable () -> Unit,
) {
    val dark = when (settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkColors else LightColors
    val accent = runCatching { hexColor(settings.accent) }
        .getOrDefault(hexColor(Accents.DEFAULT.hex))

    val m3 = if (dark) {
        darkColorScheme(
            primary = accent,
            background = colors.bg,
            surface = colors.surface,
            onBackground = colors.fg1,
            onSurface = colors.fg1,
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = colors.bg,
            surface = colors.surface,
            onBackground = colors.fg1,
            onSurface = colors.fg1,
        )
    }

    CompositionLocalProvider(
        LocalMarkalonColors provides colors,
        LocalAccent provides accent,
    ) {
        MaterialTheme(
            colorScheme = m3,
            typography = MarkalonTypography,
            content = content,
        )
    }
}
