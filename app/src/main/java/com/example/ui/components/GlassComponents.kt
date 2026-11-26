package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

fun loc(isEn: Boolean, fr: String, en: String): String = if (isEn) en else fr

@Composable
fun tr(fr: String, en: String): String = if (LocalIsEnglish.current) en else fr

/**
 * Editorial Japanese-Bauhaus warm paper canvas background with subtle drafting lines.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = LocalGlassPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(palette.bgTop, palette.bgMid, palette.bgBottom)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = palette.paperTaupe.copy(alpha = if (palette.isDark) 0.08f else 0.18f)
            val step = 64.dp.toPx()
            var x = step
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = step
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
            // Subtle warm Bauhaus sun glow behind top hero area
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.orbCyan, Color.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.14f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.82f, size.height * 0.14f)
            )
        }
        content()
    }
}

/**
 * Crisp Japanese-Bauhaus editorial paper card with ink outline and subtle tactile offset shadow when elevated.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevated: Boolean = false,
    accentBorderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val fill = if (elevated) palette.glassSurfaceElevated else palette.glassSurface
    val borderColor = when {
        elevated -> palette.glassBorderStart
        accentBorderColor != null -> accentBorderColor.copy(alpha = 0.72f)
        else -> palette.paperTaupe.copy(alpha = 0.85f)
    }
    val borderWidth = if (elevated) 1.5.dp else 1.dp

    val shadowMod = if (elevated) {
        modifier.drawBehind {
            val offsetPx = 3.dp.toPx()
            val radiusPx = 18.dp.toPx()
            drawRoundRect(
                color = palette.inkShadow,
                topLeft = Offset(offsetPx, offsetPx),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }
    } else {
        modifier
    }

    val baseModifier = shadowMod
        .clip(shape)
        .background(fill)
        .border(BorderStroke(borderWidth, borderColor), shape)
        .let { m ->
            if (onClick != null) m.clickable(onClick = onClick) else m
        }

    Column(
        modifier = baseModifier.padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backTestTag: String = "back_button",
    actions: @Composable RowScope.() -> Unit = {}
) {
    val palette = LocalGlassPalette.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(palette.glassSurfaceElevated)
                        .border(1.2.dp, palette.glassBorderStart, CircleShape)
                        .testTag(backTestTag)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = tr("Retour", "Back"),
                        tint = palette.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(palette.paperTaupe.copy(alpha = 0.55f))
        )
    }
}

@Composable
fun GlassFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val palette = LocalGlassPalette.current
    val bgColor by animateColorAsState(
        targetValue = if (selected) palette.textPrimary else palette.glassSurface,
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) palette.bgTop else palette.textPrimary,
        label = "chip_fg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) palette.textPrimary else palette.paperTaupe,
        label = "chip_border"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun GlassBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = if (palette.isDark) 0.22f else 0.14f))
            .border(1.dp, color.copy(alpha = 0.72f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (palette.isDark) color else palette.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Capsule gauge inspired by the reference CONCEPT DESIGN weight gauge (0% -> 50% -> 100%).
 */
@Composable
fun BauhausGaugeBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color? = null,
    labelLeft: String? = null,
    labelRight: String? = null,
    height: Dp = 14.dp
) {
    val palette = LocalGlassPalette.current
    val clamped = progress.coerceIn(0f, 1f)
    val barColor = fillColor ?: palette.textPrimary

    Column(modifier = modifier.fillMaxWidth()) {
        if (labelLeft != null || labelRight != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = labelLeft ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = labelRight ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(palette.bgTop)
                .border(1.5.dp, palette.glassBorderStart, RoundedCornerShape(50))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = clamped.coerceAtLeast(0.03f))
                    .height(height - 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun EditorialKeywordPill(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val palette = LocalGlassPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (highlighted) palette.accentCyan.copy(alpha = 0.22f) else palette.glassSurface)
            .border(
                width = 1.dp,
                color = if (highlighted) palette.accentCyan else palette.paperTaupe,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textPrimary
        )
    }
}
