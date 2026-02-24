package com.example.bullet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary                = Ink,
    onPrimary              = Cream,
    primaryContainer       = Ink,
    onPrimaryContainer     = Cream,
    secondary              = Ink,
    onSecondary            = Cream,
    secondaryContainer     = CreamVariant,
    onSecondaryContainer   = Ink,
    background             = Cream,
    onBackground           = Ink,
    surface                = CreamSurface,
    onSurface              = Ink,
    surfaceVariant         = CreamVariant,
    onSurfaceVariant       = InkSoft,
    surfaceContainer       = CreamVariant,
    surfaceContainerHigh   = Color(0xFFE5DFD6),
    surfaceContainerLow    = CreamSurface,
    outline                = InkBorder,
    outlineVariant         = InkDivider,
    error                  = Color(0xFF8B2020),
    onError                = Cream,
    errorContainer         = Color(0xFFF5D5D5),
    onErrorContainer       = Color(0xFF5C1010),
)

private val DarkColorScheme = darkColorScheme(
    primary                = Paper,
    onPrimary              = Charcoal,
    primaryContainer       = Paper,
    onPrimaryContainer     = Charcoal,
    secondary              = Paper,
    onSecondary            = Charcoal,
    secondaryContainer     = CharcoalVariant,
    onSecondaryContainer   = Paper,
    background             = Charcoal,
    onBackground           = Paper,
    surface                = CharcoalSurface,
    onSurface              = Paper,
    surfaceVariant         = CharcoalVariant,
    onSurfaceVariant       = PaperSoft,
    surfaceContainer       = CharcoalVariant,
    surfaceContainerHigh   = Color(0xFF303030),
    surfaceContainerLow    = CharcoalSurface,
    outline                = PaperBorder,
    outlineVariant         = PaperDivider,
    error                  = Color(0xFFCF8080),
    onError                = Charcoal,
    errorContainer         = Color(0xFF3D1515),
    onErrorContainer       = Color(0xFFEDB8B8),
)

@Composable
fun NibTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
