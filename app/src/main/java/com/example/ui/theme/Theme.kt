package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode

val LocalGlassPalette = staticCompositionLocalOf { LightGlassPalette }
val LocalIsEnglish = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = LionOchre,
    onPrimary = InkBlack,
    secondary = InkCharcoal,
    onSecondary = PaperIvory,
    tertiary = SageSuccess,
    background = PaperIvory,
    onBackground = InkBlack,
    surface = Color(0xFFFBF9F5),
    onSurface = InkBlack,
    surfaceVariant = PaperSand,
    onSurfaceVariant = Color(0xFF59564E),
    outline = InkBlack,
    error = TerracottaAlert,
    onError = PaperIvory
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkLionGold,
    onPrimary = InkBlack,
    secondary = PaperTaupe,
    onSecondary = InkBlack,
    tertiary = Color(0xFF56A879),
    background = DarkPaperTop,
    onBackground = PaperIvory,
    surface = Color(0xFF1E1C18),
    onSurface = PaperIvory,
    surfaceVariant = Color(0xFF28251F),
    onSurfaceVariant = PaperTaupe,
    outline = PaperTaupe,
    error = Color(0xFFE06D5D),
    onError = InkBlack
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun EchecsCoachTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    language: AppLanguage = AppLanguage.FR,
    textScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme
    val glassPalette = if (useDark) DarkGlassPalette else LightGlassPalette
    val typography = buildAppTypography(textScale)

    CompositionLocalProvider(
        LocalGlassPalette provides glassPalette,
        LocalIsEnglish provides (language == AppLanguage.EN)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = AppShapes,
            content = content
        )
    }
}
