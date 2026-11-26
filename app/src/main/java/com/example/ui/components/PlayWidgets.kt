package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.bot.BotPersona
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

/** Petit label de section, en majuscules espacées (style éditorial). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val palette = LocalGlassPalette.current
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 11.sp,
        letterSpacing = 1.6.sp,
        fontWeight = FontWeight.Medium,
        color = palette.textSecondary
    )
}

/** Avatar typographique d'un bot : initiale sur carré encadré. */
@Composable
fun BotMonogram(persona: BotPersona, size: Dp = 44.dp, inverted: Boolean = false, modifier: Modifier = Modifier) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val name = if (isEn) persona.nameEn else persona.nameFr
    val letter = name.firstOrNull { it.isLetter() && it.isUpperCase() }?.toString() ?: name.take(1)
    val bg = if (inverted) palette.bgTop else palette.textPrimary
    val fg = if (inverted) palette.textPrimary else palette.bgTop
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4))
            .background(bg)
            .border(1.dp, palette.glassBorderStart, RoundedCornerShape(size / 4)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.46f).sp
        )
    }
}

/** Bouton principal : encre sur papier (noir qui ressort en clair, os en sombre). */
@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalGlassPalette.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.textPrimary,
            contentColor = palette.bgTop,
            disabledContainerColor = palette.paperTaupe.copy(alpha = 0.4f),
            disabledContentColor = palette.textSecondary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

/** Bouton secondaire à contour fin. */
@Composable
fun LineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable (RowScope.() -> Unit)? = null
) {
    val palette = LocalGlassPalette.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, palette.glassBorderStart),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.textPrimary),
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        if (content != null) content()
        Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
