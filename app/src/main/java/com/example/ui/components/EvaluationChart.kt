package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalGlassPalette
import kotlin.math.roundToInt

/**
 * Interactive evaluation graph for Module 3.6 (PGN Analysis).
 * Values are in centipawns (positive favors White, negative favors Black).
 */
@Composable
fun GameEvaluationChart(
    evalsCp: List<Int>,
    selectedPlyIndex: Int,
    onSelectPly: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(evalsCp.size) {
                detectTapGestures { offset ->
                    if (evalsCp.isNotEmpty()) {
                        val idx = ((offset.x / size.width) * (evalsCp.size - 1))
                            .roundToInt()
                            .coerceIn(0, evalsCp.lastIndex)
                        onSelectPly(idx)
                    }
                }
            }
    ) {
        val midY = size.height / 2f
        // Center equality line (0.0)
        drawLine(
            color = palette.textSecondary.copy(alpha = 0.35f),
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )

        if (evalsCp.isEmpty()) return@Canvas

        val maxCp = 800f
        fun yForCp(cp: Int): Float {
            val clamped = cp.coerceIn(-800, 800).toFloat()
            return midY - (clamped / maxCp) * (size.height * 0.44f)
        }

        val stepX = if (evalsCp.size > 1) size.width / (evalsCp.size - 1) else size.width
        val linePath = Path()
        val areaPath = Path()

        evalsCp.forEachIndexed { idx, cp ->
            val x = idx * stepX
            val y = yForCp(cp)
            if (idx == 0) {
                linePath.moveTo(x, y)
                areaPath.moveTo(x, midY)
                areaPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                areaPath.lineTo(x, y)
            }
        }
        val lastX = (evalsCp.size - 1) * stepX
        areaPath.lineTo(lastX, midY)
        areaPath.close()

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette.accentCyan.copy(alpha = 0.32f),
                    palette.accentMagenta.copy(alpha = 0.20f)
                )
            )
        )
        drawPath(
            path = linePath,
            color = palette.accentCyan,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Selected ply indicator
        val selIdx = selectedPlyIndex.coerceIn(0, evalsCp.lastIndex)
        val selX = selIdx * stepX
        val selY = yForCp(evalsCp[selIdx])
        drawLine(
            color = palette.accentMagenta.copy(alpha = 0.7f),
            start = Offset(selX, 0f),
            end = Offset(selX, size.height),
            strokeWidth = 2.5f
        )
        drawCircle(
            color = palette.accentMagenta,
            radius = 8f,
            center = Offset(selX, selY)
        )
    }
}

/**
 * Progression trend line & area chart for Module 3.11 (Tableau de bord).
 */
@Composable
fun ProgressionTrendChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color? = null
) {
    val palette = LocalGlassPalette.current
    val strokeColor = lineColor ?: palette.accentCyan

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val pad = 12f
        val plotW = (size.width - pad * 2).coerceAtLeast(1f)
        val plotH = (size.height - pad * 2).coerceAtLeast(1f)

        // Horizontal guide lines
        for (i in 0..3) {
            val y = pad + (plotH / 3f) * i
            drawLine(
                color = palette.textSecondary.copy(alpha = 0.16f),
                start = Offset(pad, y),
                end = Offset(size.width - pad, y),
                strokeWidth = 1.5f
            )
        }

        if (values.isEmpty()) return@Canvas

        val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(10f)
        val minVal = 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val stepX = if (values.size > 1) plotW / (values.size - 1) else plotW

        val linePath = Path()
        val fillPath = Path()
        val points = mutableListOf<Offset>()

        values.forEachIndexed { idx, v ->
            val x = pad + idx * stepX
            val norm = ((v - minVal) / range).coerceIn(0f, 1f)
            val y = pad + plotH - norm * plotH
            points.add(Offset(x, y))
            if (idx == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, pad + plotH)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(points.last().x, pad + plotH)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(strokeColor.copy(alpha = 0.35f), Color.Transparent)
            )
        )
        drawPath(
            path = linePath,
            color = strokeColor,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
        points.forEach { pt ->
            drawCircle(color = palette.bgMid, radius = 7f, center = pt)
            drawCircle(color = strokeColor, radius = 5f, center = pt)
        }
    }
}

/**
 * 6-Axis Bauhaus Skill Radar Chart for the Progression Dashboard.
 */
@Composable
fun BauhausSkillRadarChart(
    axes: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        if (axes.size < 3) return@Canvas
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = (minOf(size.width, size.height) * 0.36f).coerceAtLeast(24f)
        val count = axes.size
        val angleStep = (2.0 * Math.PI) / count

        // Concentric hexagonal / polygonal rings
        for (ring in 1..3) {
            val r = maxR * (ring / 3f)
            val ringPath = Path()
            for (i in 0 until count) {
                val angle = -Math.PI / 2.0 + i * angleStep
                val px = cx + (r * kotlin.math.cos(angle)).toFloat()
                val py = cy + (r * kotlin.math.sin(angle)).toFloat()
                if (i == 0) ringPath.moveTo(px, py) else ringPath.lineTo(px, py)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = palette.paperTaupe.copy(alpha = 0.45f),
                style = Stroke(width = 1.5f)
            )
        }

        // Spokes
        for (i in 0 until count) {
            val angle = -Math.PI / 2.0 + i * angleStep
            val px = cx + (maxR * kotlin.math.cos(angle)).toFloat()
            val py = cy + (maxR * kotlin.math.sin(angle)).toFloat()
            drawLine(
                color = palette.paperTaupe.copy(alpha = 0.5f),
                start = Offset(cx, cy),
                end = Offset(px, py),
                strokeWidth = 1.5f
            )
        }

        // Player skill polygon
        val dataPath = Path()
        val pts = mutableListOf<Offset>()
        axes.forEachIndexed { i, (_, value) ->
            val clamped = value.coerceIn(0.12f, 1f)
            val angle = -Math.PI / 2.0 + i * angleStep
            val px = cx + (maxR * clamped * kotlin.math.cos(angle)).toFloat()
            val py = cy + (maxR * clamped * kotlin.math.sin(angle)).toFloat()
            pts.add(Offset(px, py))
            if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
        }
        dataPath.close()

        drawPath(
            path = dataPath,
            color = palette.accentCyan.copy(alpha = 0.30f)
        )
        drawPath(
            path = dataPath,
            color = palette.textPrimary,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
        pts.forEach { pt ->
            drawCircle(color = palette.accentCyan, radius = 7f, center = pt)
            drawCircle(color = palette.textPrimary, radius = 7f, center = pt, style = Stroke(width = 2.5f))
        }
    }
}

