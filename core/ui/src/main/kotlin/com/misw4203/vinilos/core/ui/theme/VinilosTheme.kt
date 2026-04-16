package com.misw4203.vinilos.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = VinilosTertiary,
    secondary = VinilosPrimaryStrong,
    tertiary = VinilosSecondary,
)

private val DarkColors = darkColorScheme(
    primary = VinilosPrimary,
    secondary = VinilosPrimaryStrong,
    tertiary = VinilosTertiary,
    background = VinilosBackground,
    surface = VinilosSurface,
    surfaceVariant = VinilosSurfaceElevated,
    outline = VinilosOutline,
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
