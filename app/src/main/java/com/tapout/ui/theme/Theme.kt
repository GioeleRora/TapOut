package com.tapout.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = CyberBlack,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = NeonPinkBright,
    secondary = ElectricPurple,
    onSecondary = TextWhite,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = NeonPurpleBright,
    tertiary = NeonMint,
    onTertiary = CyberBlack,
    background = CyberBlack,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = SurfaceBorder,
    error = DangerRed,
    onError = TextWhite
)

@Composable
fun TapOutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
