package com.gymsync.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Shapes = androidx.compose.material3.Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

val Emerald = Color(0xFF00E676)
val EmeraldDark = Color(0xFF00C853)
val EmeraldLight = Color(0xFF69F0AE)
val Amber = Color(0xFFFFB300)
val AmberLight = Color(0xFFFFD740)
val WarmCoral = Color(0xFFFF6B6B)
val SoftPurple = Color(0xFFB388FF)
val DeepNavy = Color(0xFF0A0E17)
val DarkSurface = Color(0xFF131A26)
val CardSurface = Color(0xFF1A2235)
val CardBorder = Color(0xFF2A3448)
val SubtleWhite = Color(0xFFE8EAED)
val MutedGray = Color(0xFF9AA0A6)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003920),
    onPrimaryContainer = EmeraldLight,
    secondary = Amber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3E2D00),
    onSecondaryContainer = AmberLight,
    tertiary = SoftPurple,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF2A1050),
    onTertiaryContainer = Color(0xFFE8D0FF),
    background = DeepNavy,
    onBackground = SubtleWhite,
    surface = DarkSurface,
    onSurface = SubtleWhite,
    surfaceVariant = CardSurface,
    onSurfaceVariant = MutedGray,
    surfaceTint = Emerald.copy(alpha = 0.1f),
    outline = CardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4FFE0),
    onPrimaryContainer = Color(0xFF00210E),
    secondary = Color(0xFFE65100),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3E0),
    onSecondaryContainer = Color(0xFF2C1500),
    tertiary = Color(0xFF7C4DFF),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0E0FF),
    onTertiaryContainer = Color(0xFF1C0050),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceTint = EmeraldDark.copy(alpha = 0.08f),
    outline = Color(0xFFE0E0E0)
)

@Composable
fun GymSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
