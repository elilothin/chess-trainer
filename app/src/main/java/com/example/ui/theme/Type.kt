package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.poppins, FontWeight.SemiBold),
    Font(R.font.poppins, FontWeight.Bold)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

fun buildAppTypography(scale: Float = 1.0f): Typography {
    val s = scale.coerceIn(0.9f, 1.35f)
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * s).sp,
            lineHeight = (34 * s).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22 * s).sp,
            lineHeight = (28 * s).sp
        ),
        titleLarge = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * s).sp,
            lineHeight = (24 * s).sp
        ),
        titleMedium = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * s).sp,
            lineHeight = (22 * s).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * s).sp,
            lineHeight = (22 * s).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp
        ),
        bodySmall = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * s).sp,
            lineHeight = (17 * s).sp
        ),
        labelLarge = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp
        ),
        labelMedium = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * s).sp,
            lineHeight = (16 * s).sp
        ),
        labelSmall = TextStyle(
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * s).sp,
            lineHeight = (15 * s).sp
        )
    )
}
