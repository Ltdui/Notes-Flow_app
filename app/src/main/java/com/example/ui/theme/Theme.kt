package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = PixelBluePrimary,
    onPrimary = PixelBlueOnPrimary,
    primaryContainer = PixelBluePrimaryContainer,
    onPrimaryContainer = PixelBlueOnPrimaryContainer,
    secondary = PixelSecondary,
    onSecondary = PixelOnSecondary,
    secondaryContainer = PixelSecondaryContainer,
    onSecondaryContainer = PixelOnSecondaryContainer,
    tertiary = PixelTertiary,
    onTertiary = PixelOnTertiary,
    tertiaryContainer = PixelTertiaryContainer,
    onTertiaryContainer = PixelOnTertiaryContainer,
    background = PixelBackgroundLight,
    onBackground = PixelOnBackgroundLight,
    surface = PixelSurfaceLight,
    onSurface = PixelOnSurfaceLight,
    surfaceVariant = PixelSurfaceVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PixelBluePrimaryDark,
    onPrimary = PixelBlueOnPrimaryDark,
    primaryContainer = PixelBluePrimaryContainerDark,
    onPrimaryContainer = PixelBlueOnPrimaryContainerDark,
    secondary = PixelSecondaryDark,
    onSecondary = PixelOnSecondaryDark,
    secondaryContainer = PixelSecondaryContainerDark,
    onSecondaryContainer = PixelOnSecondaryContainerDark,
    background = PixelBackgroundDark,
    onBackground = PixelOnBackgroundDark,
    surface = PixelSurfaceDark,
    onSurface = PixelOnSurfaceDark,
    surfaceVariant = PixelSurfaceVariantDark
)

@Composable
fun NoteFlowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    NoteFlowTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        dynamicColor = dynamicColor,
        content = content
    )
}
