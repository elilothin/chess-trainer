package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.sin
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

/** Remplissage « verre » translucide : plus lumineux en haut, plus léger en bas. */
@Composable
fun glassFillBrush(strong: Boolean = false): Brush {
    val dark = LocalGlassPalette.current.isDark
    val top = if (dark) (if (strong) 0.13f else 0.085f) else (if (strong) 0.78f else 0.58f)
    val bottom = if (dark) (if (strong) 0.07f else 0.04f) else (if (strong) 0.50f else 0.34f)
    return Brush.verticalGradient(listOf(Color.White.copy(alpha = top), Color.White.copy(alpha = bottom)))
}

/** Liseré de verre : bord éclairé en haut à gauche, presque éteint au milieu, reflet en bas à droite. */
@Composable
fun glassBorderBrush(strong: Boolean = false, tint: Color? = null): Brush {
    val dark = LocalGlassPalette.current.isDark
    val hi = if (dark) (if (strong) 0.42f else 0.28f) else 0.95f
    val lo = if (dark) 0.14f else 0.55f
    val edge = tint ?: Color.White
    return Brush.linearGradient(
        colors = listOf(
            edge.copy(alpha = if (tint != null) 0.85f else hi),
            Color.White.copy(alpha = if (dark) 0.04f else 0.18f),
            edge.copy(alpha = if (tint != null) 0.45f else lo)
        )
    )
}

/**
 * Fond ambiant du verre liquide : trois halos lents (champagne, pierre, ivoire) qui dérivent derrière
 * les surfaces translucides. Très discret pour rester sobre.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = LocalGlassPalette.current
    val transition = rememberInfiniteTransition(label = "ambient")
    val t1 by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart), label = "o1"
    )
    val t2 by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(34000, easing = LinearEasing), RepeatMode.Restart), label = "o2"
    )
    val t3 by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(41000, easing = LinearEasing), RepeatMode.Restart), label = "o3"
    )
    val twoPi = (2 * Math.PI).toFloat()
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
            val w = size.width
            val h = size.height
            val warm = if (palette.isDark) Color(0xFFC9A361).copy(alpha = 0.20f) else Color(0xFFD9B25A).copy(alpha = 0.34f)
            val stone = if (palette.isDark) Color(0xFF8E897D).copy(alpha = 0.16f) else Color(0xFFA38F70).copy(alpha = 0.22f)
            val ivory = if (palette.isDark) Color(0xFFEBE4D5).copy(alpha = 0.07f) else Color(0xFFFFFFFF).copy(alpha = 0.70f)
            fun orb(color: Color, cx: Float, cy: Float, r: Float) {
                drawCircle(
                    brush = Brush.radialGradient(listOf(color, Color.Transparent), center = Offset(cx, cy), radius = r),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
            orb(warm, w * (0.78f + 0.10f * sin(t1 * twoPi)), h * (0.16f + 0.05f * cos(t1 * twoPi)), w * 0.75f)
            orb(stone, w * (0.15f + 0.12f * cos(t2 * twoPi)), h * (0.52f + 0.07f * sin(t2 * twoPi)), w * 0.85f)
            orb(ivory, w * (0.60f + 0.14f * sin(t3 * twoPi)), h * (0.88f + 0.04f * cos(t3 * twoPi)), w * 0.80f)
        }
        content()
    }
}

/**
 * Carte de verre liquide : fond translucide dégradé, liseré lumineux, reflet spéculaire en haut
 * et ombre douce. [elevated] renforce l'opacité et la profondeur.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevated: Boolean = false,
    accentBorderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val fill = glassFillBrush(strong = elevated)
    val border = glassBorderBrush(strong = elevated, tint = accentBorderColor)
    val shadowColor = palette.inkShadow
    val sheen = Color.White.copy(alpha = if (palette.isDark) 0.22f else 0.9f)

    val base = modifier
        .drawBehind {
            // ombre douce : plusieurs couches décalées, de plus en plus larges et transparentes
            val layers = if (elevated) 5 else 3
            for (i in 1..layers) {
                val grow = i * 2.5.dp.toPx()
                drawRoundRect(
                    color = shadowColor.copy(alpha = shadowColor.alpha * (0.55f / i)),
                    topLeft = Offset(-grow * 0.4f, grow * 0.55f),
                    size = Size(size.width + grow * 0.8f, size.height + grow * 0.6f),
                    cornerRadius = CornerRadius(22.dp.toPx() + grow, 22.dp.toPx() + grow)
                )
            }
        }
        .clip(shape)
        .background(fill)
        .border(BorderStroke(1.dp, border), shape)
        .drawWithContent {
            drawContent()
            // reflet spéculaire en haut de la carte
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, sheen, Color.Transparent),
                    startX = size.width * 0.08f,
                    endX = size.width * 0.92f
                ),
                start = Offset(size.width * 0.08f, 1.dp.toPx()),
                end = Offset(size.width * 0.92f, 1.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }
        .let { m -> if (onClick != null) m.pressScale().clickable(onClick = onClick) else m }

    Column(
        modifier = base.padding(contentPadding),
        content = content
    )
}

/** Léger effet « ressort » à l'appui : la surface se comprime puis revient. */
fun Modifier.pressScale(pressedScale: Float = 0.97f): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val k by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "press"
    )
    this
        .graphicsLayer { scaleX = k; scaleY = k }
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val press = PressInteraction.Press(down.position)
                interaction.tryEmit(press)
                val up = waitForUpOrCancellation()
                interaction.tryEmit(if (up != null) PressInteraction.Release(press) else PressInteraction.Cancel(press))
            }
        }
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
                        .background(glassFillBrush(strong = true))
                        .border(1.dp, glassBorderBrush(strong = true), CircleShape)
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
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, palette.paperTaupe.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
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
        targetValue = if (selected) palette.textPrimary else Color.Transparent,
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) palette.bgTop else palette.textPrimary,
        label = "chip_fg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) palette.textPrimary else Color.Transparent,
        label = "chip_border"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.minimumInteractiveComponentSize().pressScale(0.94f),
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = null
    ) {
      Box(
        modifier = Modifier
            .background(if (selected) Brush.verticalGradient(listOf(bgColor, bgColor)) else glassFillBrush(), RoundedCornerShape(50))
            .border(1.dp, if (selected) Brush.verticalGradient(listOf(borderColor, borderColor)) else glassBorderBrush(), RoundedCornerShape(50))
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

/** Repères d'angle façon écran de visée (HUD) pour les cartes importantes. */
fun Modifier.hudCorners(color: Color, length: Dp = 14.dp, inset: Dp = 6.dp): Modifier = this.drawWithContent {
    drawContent()
    val l = length.toPx()
    val i = inset.toPx()
    val w = 1.6.dp.toPx()
    val c = color.copy(alpha = 0.85f)
    fun corner(x: Float, y: Float, dx: Float, dy: Float) {
        drawLine(c, Offset(x, y), Offset(x + dx * l, y), strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(c, Offset(x, y), Offset(x, y + dy * l), strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
    corner(i, i, 1f, 1f)
    corner(size.width - i, i, -1f, 1f)
    corner(i, size.height - i, 1f, -1f)
    corner(size.width - i, size.height - i, -1f, -1f)
}
