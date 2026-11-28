package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.bot.Advantage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette

/**
 * Jauge d'avantage verticale : la part claire (Blancs) monte quand les Blancs dominent, descend sinon.
 * Le côté Blancs est en bas, sauf si l'échiquier est retourné.
 */
@Composable
fun EvalGauge(
    advantage: Advantage?,
    flipped: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    val target = advantage?.whiteShare ?: 0.5f
    val share by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "gauge"
    )
    val shape = RoundedCornerShape(10.dp)
    Canvas(
        modifier = modifier
            .clip(shape)
            .border(1.dp, glassBorderBrush(strong = true), shape)
    ) {
        val w = size.width
        val h = size.height
        // fond sombre = Noirs, partie claire = Blancs
        drawRect(Brush.verticalGradient(listOf(Color(0xFF2A2723), Color(0xFF0B0A09))))
        val lightH = h * share
        val lightTop = if (flipped) 0f else h - lightH
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFFFCF2), Color(0xFFD9CBAA)),
                startY = lightTop, endY = lightTop + lightH
            ),
            topLeft = Offset(0f, lightTop),
            size = Size(w, lightH)
        )
        // ligne lumineuse à la frontière
        val edgeY = if (flipped) lightH else h - lightH
        drawRect(
            color = palette.accentCyan.copy(alpha = 0.9f),
            topLeft = Offset(0f, edgeY - 1.dp.toPx()),
            size = Size(w, 2.dp.toPx())
        )
        // graduations
        for (i in 1..7) {
            val y = h * i / 8f
            drawRect(
                color = (if ((y > edgeY) != flipped) Color.Black else Color.White).copy(alpha = 0.18f),
                topLeft = Offset(w * 0.55f, y),
                size = Size(w * 0.45f, if (i == 4) 1.5.dp.toPx() else 0.8.dp.toPx())
            )
        }
    }
}

/** Échiquier + jauge à gauche, de même hauteur. */
@Composable
fun BoardWithGauge(
    advantage: Advantage?,
    flipped: Boolean,
    modifier: Modifier = Modifier,
    gaugeWidth: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = 8.dp
        val side = maxWidth - gaugeWidth - gap
        Row(verticalAlignment = Alignment.CenterVertically) {
            EvalGauge(advantage, flipped, Modifier.width(gaugeWidth).height(side))
            Spacer(Modifier.width(gap))
            androidx.compose.foundation.layout.Box(Modifier.width(side)) { content() }
        }
    }
}

/** Petit badge texte : « +0.8 » avec la couleur de l'avantage. */
@Composable
fun AdvantageBadge(advantage: Advantage?, modifier: Modifier = Modifier) {
    val palette = LocalGlassPalette.current
    val txt = advantage?.label ?: "…"
    val cp = advantage?.centipawns ?: 0
    val color = when {
        cp > 60 -> palette.success
        cp < -60 -> palette.error
        else -> palette.textSecondary
    }
    Text(
        text = txt,
        modifier = modifier,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = color
    )
}
