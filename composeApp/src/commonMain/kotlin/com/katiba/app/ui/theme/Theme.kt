package com.katiba.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = KatibaColors.KenyaGreen,
    onPrimary = KatibaColors.KenyaWhite,
    primaryContainer = KatibaColors.LightGreen,
    onPrimaryContainer = KatibaColors.DarkGreen,
    
    secondary = KatibaColors.KenyaRed,
    onSecondary = KatibaColors.KenyaWhite,
    secondaryContainer = KatibaColors.LightRed,
    onSecondaryContainer = KatibaColors.DarkRed,
    
    tertiary = KatibaColors.BeadGold,
    onTertiary = KatibaColors.KenyaBlack,
    tertiaryContainer = KatibaColors.BeadOrange,
    onTertiaryContainer = KatibaColors.BeadBrown,
    
    background = KatibaColors.Background,
    onBackground = KatibaColors.OnSurface,
    
    surface = KatibaColors.Surface,
    onSurface = KatibaColors.OnSurface,
    surfaceVariant = KatibaColors.SurfaceVariant,
    onSurfaceVariant = KatibaColors.OnSurfaceVariant,
    
    error = KatibaColors.DarkRed,
    onError = KatibaColors.KenyaWhite,
    
    outline = KatibaColors.OnSurfaceVariant,
    outlineVariant = KatibaColors.SurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = KatibaColors.LightGreen,
    onPrimary = KatibaColors.DarkGreen,
    primaryContainer = KatibaColors.KenyaGreen,
    onPrimaryContainer = KatibaColors.LightGreen,
    
    secondary = KatibaColors.LightRed,
    onSecondary = KatibaColors.DarkRed,
    secondaryContainer = KatibaColors.KenyaRed,
    onSecondaryContainer = KatibaColors.LightRed,
    
    tertiary = KatibaColors.BeadGold,
    onTertiary = KatibaColors.KenyaBlack,
    tertiaryContainer = KatibaColors.BeadBrown,
    onTertiaryContainer = KatibaColors.BeadGold,
    
    background = KatibaColors.DarkBackground,
    onBackground = KatibaColors.OnDarkSurface,
    
    surface = KatibaColors.DarkSurface,
    onSurface = KatibaColors.OnDarkSurface,
    surfaceVariant = KatibaColors.DarkSurfaceVariant,
    onSurfaceVariant = KatibaColors.OnDarkSurfaceVariant,
    
    error = KatibaColors.LightRed,
    onError = KatibaColors.DarkRed,
    
    outline = KatibaColors.OnDarkSurfaceVariant,
    outlineVariant = KatibaColors.DarkSurfaceVariant
)

@Composable
fun KatibaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KatibaTypography,
        content = content
    )
}
