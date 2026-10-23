package com.whatspent.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AcidLimeAccent,
    onPrimary = OnAcidLimeInk,
    primaryContainer = CharcoalBentoSurface,
    onPrimaryContainer = AcidLimeAccent,
    secondary = PeriwinkleBarHighlight,
    onSecondary = OnAcidLimeInk,
    secondaryContainer = ElevatedCharcoalSurface,
    onSecondaryContainer = PeriwinkleBarHighlight,
    tertiary = PeriwinkleBarHighlight,
    onTertiary = OnAcidLimeInk,
    tertiaryContainer = ElevatedCharcoalSurface,
    onTertiaryContainer = PeriwinkleBarHighlight,
    background = ObsidianBackground,
    onBackground = PrimaryWhiteText,
    surface = CharcoalBentoSurface,
    onSurface = PrimaryWhiteText,
    surfaceVariant = CharcoalBentoSurface,
    onSurfaceVariant = SubtleAxisLabelText,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = ObsidianBackground,
    surfaceContainer = CharcoalBentoSurface,
    surfaceContainerHigh = ElevatedCharcoalSurface,
    surfaceContainerHighest = ElevatedCharcoalSurface,
    outline = SubtleDarkBorder,
    outlineVariant = SubtleDarkBorder,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = OnEmeraldPrimaryLight,
    primaryContainer = EmeraldPrimaryContainerLight,
    onPrimaryContainer = OnEmeraldPrimaryContainerLight,
    secondary = PeriwinkleBarHighlight,
    onSecondary = OnAcidLimeInk,
    secondaryContainer = ElevatedCharcoalSurface,
    onSecondaryContainer = PeriwinkleBarHighlight,
    tertiary = AcidLimeAccent,
    onTertiary = OnAcidLimeInk,
    tertiaryContainer = CyanTertiaryContainerLight,
    onTertiaryContainer = OnCyanTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun WhatSpentTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ObsidianBackground.toArgb()
                window.navigationBarColor = ObsidianBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
