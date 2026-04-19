package com.rishi.remindly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary        = TeamsPrimary,
    onPrimary      = Color.White,
    background     = TeamsBg,
    onBackground   = TeamsText,
    surface        = TeamsSurface,
    onSurface      = TeamsText,
    secondary      = TeamsPrimarySoft,
    onSecondary    = Color.White,
    tertiary       = TeamsSuccess,
    onTertiary     = Color.White,
    error          = TeamsError,
    onError        = Color.White,
    surfaceVariant = TeamsSurfaceAlt,
)

private val DarkColors = darkColorScheme(
    primary        = TeamsPrimarySoft,
    onPrimary      = Color(0xFF0D1B2A),
    background     = Color(0xFF111827),
    onBackground   = Color(0xFFF8FAFC),
    surface        = Color(0xFF1F2937),
    onSurface      = Color(0xFFF8FAFC),
    secondary      = TeamsPrimary,
    onSecondary    = Color.White,
    tertiary       = TeamsSuccess,
    surfaceVariant = Color(0xFF253345),
)

@Composable
fun RemindlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        content     = content
    )
}
