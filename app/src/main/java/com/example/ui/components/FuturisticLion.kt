package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import com.example.domain.model.LionMood
import com.example.domain.model.LionRank

/** Formes (boîte 100×100) de Léon, la mascotte futuriste : crinière géométrique, visière lumineuse. */
private object LionShapes {
    const val MANE = "M50.0 5.0 L57.8 18.9 L68.7 14.3 L71.8 25.6 L87.5 23.1 L81.5 37.8 L91.9 43.4 L85.0 53.0 L96.8 63.7 L81.5 68.2 L83.6 79.8 L71.8 80.4 L70.8 96.2 L57.8 87.1 L50.0 96.0 L42.2 87.1 L29.2 96.2 L28.2 80.4 L16.4 79.8 L18.5 68.2 L3.2 63.7 L15.0 53.0 L8.1 43.4 L18.5 37.8 L12.5 23.1 L28.2 25.6 L31.3 14.3 L42.2 18.9 Z"
    const val FACE = "M50 31 L68 37 L75 52 L68 72 L50 83 L32 72 L25 52 L32 37 Z"
    const val EARL = "M31 35 L27 21 L41 30 Z"
    const val EARR = "M69 35 L73 21 L59 30 Z"
    const val BROW = "M40 41 L50 36 L60 41"
    const val VISOR = "M32 49 L50 52 L68 49 L66 58 L50 59 L34 58 Z"
    const val EYEL = "M35 51.5 L47 53.5 L45.5 57 L36 56 Z"
    const val EYER = "M65 51.5 L53 53.5 L54.5 57 L64 56 Z"
    const val HAPPYL = "M35 57 Q41 49 47 57"
    const val HAPPYR = "M53 57 Q59 49 65 57"
    const val SLEEPL = "M35 55.5 L47 55.5"
    const val SLEEPR = "M53 55.5 L65 55.5"
    const val NOSE = "M45 62.5 L55 62.5 L50 68.5 Z"
    const val MOUTH = "M50 68.5 L50 72 M42.5 72.5 Q50 78 57.5 72.5"
    const val FANGS = "M45 74 L46.6 78.5 L48.2 75.3 M55 74 L53.4 78.5 L51.8 75.3"
    const val WHISKL = "M27 61 L12 58 M27 66 L14 70"
    const val WHISKR = "M73 61 L88 58 M73 66 L86 70"
    const val CROWN = "M36 20 L39 8 L45 15 L50 5 L55 15 L61 8 L64 20 Z"
    const val GEM = "M50 40 L53 44 L50 48 L47 44 Z"
}

private val lionPaths: Map<String, Path> by lazy {
    fun p(s: String) = PathParser().parsePathString(s).toPath()
    mapOf(
        "mane" to p(LionShapes.MANE), "face" to p(LionShapes.FACE), "earL" to p(LionShapes.EARL), "earR" to p(LionShapes.EARR),
        "brow" to p(LionShapes.BROW), "visor" to p(LionShapes.VISOR), "eyeL" to p(LionShapes.EYEL), "eyeR" to p(LionShapes.EYER),
        "happyL" to p(LionShapes.HAPPYL), "happyR" to p(LionShapes.HAPPYR), "sleepL" to p(LionShapes.SLEEPL), "sleepR" to p(LionShapes.SLEEPR),
        "nose" to p(LionShapes.NOSE), "mouth" to p(LionShapes.MOUTH), "fangs" to p(LionShapes.FANGS),
        "whiskL" to p(LionShapes.WHISKL), "whiskR" to p(LionShapes.WHISKR), "crown" to p(LionShapes.CROWN), "gem" to p(LionShapes.GEM)
    )
}

private val Champagne = Color(0xFFF1D589)
private val Cyan = Color(0xFF8FE9FF)
private val Ink = Color(0xFFD8CFBC)

/**
 * Léon, version futuriste : crinière à pointes dorées, visage de casque graphite, yeux lumineux
 * dont l'expression suit l'humeur, anneau HUD qui tourne lentement.
 */
@Composable
fun LionCharacter(
    mood: LionMood = LionMood.IDLE_HAPPY,
    rank: LionRank = LionRank.STRATEGIST,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    isDark: Boolean = false
) {
    val t = rememberInfiniteTransition(label = "lion_fx")
    val bob by t.animateFloat(
        0f, if (animated) 1f else 0f,
        infiniteRepeatable(tween(if (mood == LionMood.CHEERING) 550 else 1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val spin by t.animateFloat(
        0f, if (animated) 360f else 0f,
        infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin"
    )
    val pulse by t.animateFloat(
        if (animated) 0.6f else 1f, 1f,
        infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Canvas(modifier = modifier) {
        val side = minOf(size.width, size.height)
        val k = side / 100f
        val lift = when {
            !animated -> 0f
            mood == LionMood.CHEERING -> -5f * bob
            else -> -1.8f * bob
        }
        translate((size.width - side) / 2f, (size.height - side) / 2f) {
            scale(k, k, pivot = Offset.Zero) {
                drawFuturisticLion(mood, lift, spin, pulse, rank)
            }
        }
    }
}

private fun DrawScope.drawFuturisticLion(mood: LionMood, lift: Float, spin: Float, pulse: Float, rank: LionRank) {
    val paths = lionPaths
    // anneau HUD (ne bouge pas avec le flottement)
    rotate(spin, pivot = Offset(50f, 53f)) {
        drawCircle(
            color = Cyan.copy(alpha = 0.38f), radius = 49f, center = Offset(50f, 53f),
            style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 3f)))
        )
    }
    translate(0f, lift) {
        // crinière
        drawPath(
            paths.getValue("mane"),
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to Champagne, 0.6f to Color(0xFFB8903F), 1f to Color(0xFF6B511C)),
                center = Offset(50f, 48f), radius = 52f
            ),
            style = Fill
        )
        drawPath(paths.getValue("mane"), Color(0xFFF6E2A8).copy(alpha = 0.7f), style = Stroke(0.8f, join = StrokeJoin.Round))
        // oreilles
        for (e in listOf("earL", "earR")) {
            drawPath(paths.getValue(e), Color(0xFF2A2825), style = Fill)
            drawPath(paths.getValue(e), Ink.copy(alpha = 0.6f), style = Stroke(1f, join = StrokeJoin.Round))
        }
        // visage
        drawPath(
            paths.getValue("face"),
            brush = Brush.verticalGradient(listOf(Color(0xFF4A4743), Color(0xFF171614)), startY = 31f, endY = 83f),
            style = Fill
        )
        drawPath(paths.getValue("face"), Ink.copy(alpha = 0.75f), style = Stroke(1.2f, join = StrokeJoin.Round))
        drawPath(paths.getValue("brow"), Ink.copy(alpha = 0.7f), style = Stroke(1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(paths.getValue("gem"), Champagne, style = Fill)
        drawPath(paths.getValue("visor"), Color(0xFF0B0A09).copy(alpha = 0.88f), style = Fill)

        // yeux selon l'humeur
        val eye = if (mood == LionMood.CROWNED) Champagne else Cyan
        val a = if (mood == LionMood.SLEEPY) 0.45f else pulse
        fun glowStroke(path: Path, w: Float) {
            drawPath(path, eye.copy(alpha = 0.18f * a), style = Stroke(w + 4f, cap = StrokeCap.Round))
            drawPath(path, eye.copy(alpha = 0.35f * a), style = Stroke(w + 2f, cap = StrokeCap.Round))
            drawPath(path, eye.copy(alpha = a), style = Stroke(w, cap = StrokeCap.Round))
        }
        when (mood) {
            LionMood.CHEERING -> { glowStroke(paths.getValue("happyL"), 2.4f); glowStroke(paths.getValue("happyR"), 2.4f) }
            LionMood.SLEEPY -> { glowStroke(paths.getValue("sleepL"), 2.2f); glowStroke(paths.getValue("sleepR"), 2.2f) }
            LionMood.SHOCKED -> {
                for (cx in listOf(40f, 60f)) {
                    drawCircle(eye.copy(alpha = 0.25f * a), radius = 7f, center = Offset(cx, 54f))
                    drawCircle(eye, radius = 4.2f, center = Offset(cx, 54f))
                }
            }
            LionMood.THINKING -> {
                for (e in listOf("eyeL", "eyeR")) {
                    drawPath(paths.getValue(e), eye.copy(alpha = 0.25f * a), style = Stroke(4f, join = StrokeJoin.Round))
                    drawPath(paths.getValue(e), eye.copy(alpha = 0.55f + 0.45f * (1f - pulse)), style = Fill)
                }
            }
            else -> {
                for (e in listOf("eyeL", "eyeR")) {
                    drawPath(paths.getValue(e), eye.copy(alpha = 0.25f * a), style = Stroke(4f, join = StrokeJoin.Round))
                    drawPath(paths.getValue(e), eye, style = Fill)
                }
            }
        }
        // museau et bouche
        drawPath(paths.getValue("nose"), Champagne, style = Fill)
        drawPath(paths.getValue("nose"), Color(0xFF3A2A08), style = Stroke(0.8f, join = StrokeJoin.Round))
        drawPath(paths.getValue("mouth"), Ink, style = Stroke(1.4f, cap = StrokeCap.Round))
        if (mood != LionMood.SLEEPY) drawPath(paths.getValue("fangs"), Color(0xFFF3ECDD), style = Fill)
        // moustaches lumineuses
        drawPath(paths.getValue("whiskL"), Cyan.copy(alpha = 0.6f), style = Stroke(1f, cap = StrokeCap.Round))
        drawPath(paths.getValue("whiskR"), Cyan.copy(alpha = 0.6f), style = Stroke(1f, cap = StrokeCap.Round))
        // couronne
        if (mood == LionMood.CROWNED || mood == LionMood.CHEERING) {
            drawPath(
                paths.getValue("crown"),
                brush = Brush.verticalGradient(listOf(Color(0xFFFFE9A8), Color(0xFFB8903F)), startY = 5f, endY = 20f),
                style = Fill
            )
            drawPath(paths.getValue("crown"), Color(0xFF3A2A08), style = Stroke(1.2f, join = StrokeJoin.Round))
        }
    }
}
