package com.papertrader.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00C853),          // Bright green — gains
    onPrimary = Color(0xFF003910),
    secondary = Color(0xFF64B5F6),        // Blue — secondary actions
    onSecondary = Color(0xFF003059),
    tertiary = Color(0xFFFF5252),         // Red — losses
    onTertiary = Color(0xFF410002),
    background = Color(0xFF0D0F14),       // Deep dark background
    onBackground = Color(0xFFE3E7F0),
    surface = Color(0xFF161B26),          // Card surfaces
    onSurface = Color(0xFFE3E7F0),
    surfaceVariant = Color(0xFF1E2535),
    outline = Color(0xFF3A4255),
)

@Composable
fun PaperTraderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PaperTraderTypography,
        content = content
    )
}
