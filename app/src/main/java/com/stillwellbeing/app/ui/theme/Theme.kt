package com.stillwellbeing.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.stillwellbeing.app.domain.ThemeChoice

/**
 * Resolves [ThemeChoice] against the OS setting, then provides both a
 * Material3 [MaterialTheme] (for standard components — buttons, switches,
 * dialogs) and the full [StillPalette] via [LocalStillPalette] (for the
 * tinted surfaces standard Material slots don't cover).
 */
@Composable
fun StillTheme(
    themeChoice: ThemeChoice,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeChoice) {
        ThemeChoice.SYSTEM -> isSystemInDarkTheme()
        ThemeChoice.LIGHT -> false
        ThemeChoice.DARK -> true
    }
    val palette = if (darkTheme) DarkStillPalette else LightStillPalette

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.paper,
            background = palette.paper,
            onBackground = palette.ink,
            surface = palette.card,
            onSurface = palette.ink,
            surfaceVariant = palette.cardAlt,
            onSurfaceVariant = palette.muted,
            outline = palette.line
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.paper,
            background = palette.paper,
            onBackground = palette.ink,
            surface = palette.card,
            onSurface = palette.ink,
            surfaceVariant = palette.cardAlt,
            onSurfaceVariant = palette.muted,
            outline = palette.line
        )
    }

    CompositionLocalProvider(LocalStillPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StillTypography,
            content = content
        )
    }
}
