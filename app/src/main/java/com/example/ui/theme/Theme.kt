package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLightContainer,
    onPrimaryContainer = EmeraldDark,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldDarkText,
    tertiary = EmeraldSoft,
    onTertiary = Color.White,
    background = IvoryBackground,
    onBackground = CharcoalInk,
    surface = IvorySurface,
    onSurface = CharcoalInk,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = IvoryOutline,
    error = ErrorCrimson,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF56C29B),
    onPrimary = Color(0xFF003827),
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldLightContainer,
    secondary = GoldBright,
    onSecondary = Color(0xFF3E2B00),
    secondaryContainer = Color(0xFF5A4008),
    onSecondaryContainer = GoldContainer,
    tertiary = Color(0xFF8CD6B9),
    onTertiary = Color(0xFF003827),
    background = DarkSurfaceBg,
    onBackground = DarkIvoryText,
    surface = DarkCardSurface,
    onSurface = DarkIvoryText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB8C7C0),
    outline = Color(0xFF3B5248),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PaiImmersiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
