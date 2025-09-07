package com.example.gamebooster.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF5E8C7), // Beige claro
    onPrimary = Color.Black,
    secondary = Color(0xFFDEBA9D), // Beige oscuro
    onSecondary = Color.Black,
    background = Color(0xFFF5E8C7),
    onBackground = Color.Black,
    surface = Color(0xFFF5E8C7),
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    secondary = Color(0xFF424242), // Gris oscuro
    onSecondary = Color.White,
    background = Color(0xFF212121), // Gris muy oscuro
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)

data class CustomColorScheme(
    val name: String,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color
)

val PredefinedThemes = listOf(
    CustomColorScheme(
        name = "Navy",
        primary = Color(0xFF64B5F6), // Azul claro para acentos
        onPrimary = Color.Black,
        secondary = Color(0xFF1E88E5), // Azul intermedio
        onSecondary = Color.White,
        background = Color(0xFF0B1224), // Azul marino muy oscuro
        onBackground = Color(0xFFE3F2FD), // Texto claro azulado
        surface = Color(0xFF0F172A),
        onSurface = Color(0xFFE3F2FD)
    ),
    CustomColorScheme(
        name = "Classic",
        primary = Color(0xFF4CAF50), // Verde
        onPrimary = Color.White,
        secondary = Color(0xFF388E3C),
        onSecondary = Color.White,
        background = Color(0xFFF5F5E6),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black
    ),
    CustomColorScheme(
        name = "Ocean",
        primary = Color(0xFF2196F3), // Azul
        onPrimary = Color.White,
        secondary = Color(0xFF1976D2),
        onSecondary = Color.White,
        background = Color(0xFFE3F2FD),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black
    ),
    CustomColorScheme(
        name = "Sunset",
        primary = Color(0xFFFF9800), // Naranja
        onPrimary = Color.White,
        secondary = Color(0xFFF44336),
        onSecondary = Color.White,
        background = Color(0xFFFFF3E0),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black
    ),
    CustomColorScheme(
        name = "Violet",
        primary = Color(0xFF9C27B0), // Morado
        onPrimary = Color.White,
        secondary = Color(0xFF673AB7),
        onSecondary = Color.White,
        background = Color(0xFFF3E5F5),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black
    ),
    CustomColorScheme(
        name = "Dark",
        primary = Color(0xFFFFEB3B), // Amarillo claro para elementos destacados
        onPrimary = Color.Black,
        secondary = Color(0xFFFFEB3B), // Amarillo claro para secundarios
        onSecondary = Color.Black,
        background = Color.Black, // Fondo negro absoluto
        onBackground = Color.White, // Texto principal blanco puro
        surface = Color.Black, // Fondo de cards negro absoluto
        onSurface = Color.White // Texto en cards blanco puro
    )
)

@Composable
fun GameBoosterTheme(
    darkTheme: Boolean = false,
    customThemeIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val colorScheme = with(PredefinedThemes[customThemeIndex]) {
        androidx.compose.material3.ColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primary,
            onPrimaryContainer = onPrimary,
            inversePrimary = primary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondary,
            onSecondaryContainer = onSecondary,
            tertiary = secondary,
            onTertiary = onSecondary,
            tertiaryContainer = secondary,
            onTertiaryContainer = onSecondary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surface,
            onSurfaceVariant = onSurface,
            surfaceTint = primary,
            inverseSurface = surface,
            inverseOnSurface = onSurface,
            error = Color(0xFFB00020),
            onError = Color.White,
            errorContainer = Color(0xFFB00020),
            onErrorContainer = Color.White,
            outline = Color.Gray,
            outlineVariant = Color.LightGray,
            scrim = Color.Black,
            surfaceBright = surface,
            surfaceDim = surface.copy(alpha = 0.87f),
            surfaceContainer = surface,
            surfaceContainerHigh = surface,
            surfaceContainerHighest = surface,
            surfaceContainerLow = surface,
            surfaceContainerLowest = surface
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}