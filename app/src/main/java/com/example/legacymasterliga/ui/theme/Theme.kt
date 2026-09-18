package com.example.legacymasterliga.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.legacymasterliga.core.model.ThemePreference

private val LegacyDarkColorScheme = darkColorScheme(
    primary = LegacyBlue,
    onPrimary = Color.Black,
    primaryContainer = LegacyBlueDark,
    secondary = LegacyGold,
    background = LegacyBackground,
    onBackground = Color.White,
    surface = LegacySurface,
    onSurface = Color.White,
    surfaceVariant = LegacySurfaceVariant,
    onSurfaceVariant = LegacyTextSecondary,
    error = LegacyError,
)

private val LegacyComponentShapes = Shapes(
    small = LegacyShapes.small,
    medium = LegacyShapes.medium,
    large = LegacyShapes.large,
)

private val LegacyLightColorScheme = lightColorScheme(
    primary = LegacyBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    secondary = Color(0xFF8A6500),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE8EDF5),
    onSurfaceVariant = Color(0xFF475569),
    error = LegacyError,
)

@Composable
fun LegacyMasterLigaTheme(
    themePreference: ThemePreference = ThemePreference.DARK,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (darkTheme) LegacyDarkColorScheme else LegacyLightColorScheme,
        typography = Typography,
        shapes = LegacyComponentShapes,
        content = content,
    )
}
