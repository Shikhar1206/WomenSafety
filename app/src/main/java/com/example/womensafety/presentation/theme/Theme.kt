package com.example.womensafety.presentation.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TrustPurpleLight,
    onPrimary = OnDark,
    primaryContainer = TrustPurpleDark,
    onPrimaryContainer = OnDark,
    secondary = WarmPinkLight,
    onSecondary = OnDark,
    secondaryContainer = WarmPink,
    onSecondaryContainer = OnDark,
    tertiary = SafeGreenLight,
    onTertiary = OnDark,
    background = Background,
    onBackground = OnDark,
    surface = Surface,
    onSurface = OnDark,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnDarkSecondary,
    error = SafetyRed,
    onError = OnDark,
    outline = Outline
)

private val LightColorScheme = lightColorScheme(
    primary = TrustPurple,
    onPrimary = OnDark,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = TrustPurpleDark,
    secondary = WarmPink,
    onSecondary = OnDark,
    background = BackgroundLight,
    onBackground = OnLight,
    surface = SurfaceLight,
    onSurface = OnLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnLightSecondary,
    error = SafetyRed,
    onError = OnDark
)

@Composable
fun WomenSafetyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
