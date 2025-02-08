package com.duuolf.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightCard,
    surfaceVariant = LightCardColorOptimized,
    onBackground = LightForeground,
    onSurface = LightForeground,
    primary = LightForeground,
    primaryContainer = LightCardBorder,
    outline = LightCardBorderOptimized // 适中的边框颜色
)

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkCard,
    surfaceVariant = DarkCardColorOptimized,
    onBackground = DarkForeground,
    onSurface = DarkForeground,
    primary = DarkForeground,
    primaryContainer = DarkCardBorder,
    outline = DarkCardBorderOptimized // 适中的边框颜色
)

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
