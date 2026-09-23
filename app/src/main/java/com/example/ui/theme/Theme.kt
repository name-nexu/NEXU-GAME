package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BlokiCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D73),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = BlokiYellow,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5D4200),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = BlokiPink,
    onTertiary = Color.White,
    background = GameBackgroundDark,
    surface = GameSurfaceDark,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
)

private val LightColorScheme = lightColorScheme(
    primary = BlokiCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F5FE),
    onPrimaryContainer = Color(0xFF01579B),
    secondary = BlokiYellowDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFF8E1),
    onSecondaryContainer = Color(0xFFFF6F00),
    tertiary = BlokiPink,
    onTertiary = Color.White,
    background = GameBackgroundLight,
    surface = GameSurfaceLight,
    onBackground = Color(0xFF263238),
    onSurface = Color(0xFF263238),
)

@Composable
fun BlockyBuddiesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep colorful branded look by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
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
