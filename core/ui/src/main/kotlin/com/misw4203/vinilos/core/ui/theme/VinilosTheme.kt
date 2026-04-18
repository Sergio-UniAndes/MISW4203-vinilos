package com.misw4203.vinilos.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = VinilosTertiary,
    primaryContainer = VinilosPrimaryContainer,
    secondary = VinilosPrimaryStrong,
    secondaryContainer = VinilosSecondaryContainer,
    tertiary = VinilosSecondary,
    background = VinilosBackground,
    surface = VinilosSurface,
    surfaceVariant = VinilosSurfaceContainerHigh,
    surfaceContainerLow = VinilosSurfaceContainerLow,
    surfaceContainer = VinilosSurfaceContainer,
    surfaceContainerHigh = VinilosSurfaceContainerHigh,
    surfaceContainerHighest = VinilosSurfaceContainerHighest,
    surfaceBright = VinilosSurfaceBright,
    outlineVariant = VinilosOutlineVariant,
)

private val DarkColors = darkColorScheme(
    primary = VinilosPrimary,
    primaryContainer = VinilosPrimaryContainer,
    secondary = VinilosPrimaryStrong,
    secondaryContainer = VinilosSecondaryContainer,
    tertiary = VinilosTertiary,
    background = VinilosBackground,
    surface = VinilosSurface,
    surfaceVariant = VinilosSurfaceContainerHigh,
    surfaceContainerLow = VinilosSurfaceContainerLow,
    surfaceContainer = VinilosSurfaceContainer,
    surfaceContainerHigh = VinilosSurfaceContainerHigh,
    surfaceContainerHighest = VinilosSurfaceContainerHighest,
    surfaceBright = VinilosSurfaceBright,
    outlineVariant = VinilosOutlineVariant,
    onBackground = VinilosOnDark,
    onSurface = VinilosOnDark,
    onSurfaceVariant = VinilosOnDarkMuted,
)

@Composable
fun VinilosTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VinilosTypography,
        content = content,
    )
}
