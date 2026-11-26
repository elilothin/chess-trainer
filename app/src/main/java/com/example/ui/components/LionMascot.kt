package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LionMood
import com.example.domain.model.LionRank
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Léon le Lion — Chunky, endearing Bauhaus-ink vector mascot drawn cleanly on Compose Canvas.
 */
@Composable
fun LionCharacter(
    mood: LionMood = LionMood.IDLE_HAPPY,
    rank: LionRank = LionRank.STRATEGIST,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lion_idle")
    val bob by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (mood == LionMood.CHEERING) 550 else 1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lion_bob"
    )
    val tailSwing by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = if (animated) 14f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lion_tail"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val jumpOffset = when {
            !animated -> 0f
            mood == LionMood.CHEERING -> -h * 0.06f * bob
            else -> -h * 0.022f * bob
        }
        drawLion(
            w = w,
            h = h,
            yOffset = jumpOffset,
            tailAngle = tailSwing,
            mood = mood,
            rank = rank,
            isDark = isDark
        )
    }
}

private fun DrawScope.drawLion(
    w: Float,
    h: Float,
    yOffset: Float,
    tailAngle: Float,
    mood: LionMood,
    rank: LionRank,
    isDark: Boolean
) {
    val ink = if (isDark) Color(0xFF111111) else Color(0xFF111111)
    val maneFill = Color(0xFFD98A2B)
    val maneHighlight = Color(0xFFF0B44C)
    val furBody = Color(0xFFF4D18C)
    val creamBelly = Color(0xFFFAF5E6)
    val cheekPink = Color(0xFFE9967A)
    val sw = (minOf(w, h) * 0.036f).coerceIn(2.0f, 5.5f)

    // Ground shadow
    val shadowScale = 1f - (-yOffset / h).coerceIn(0f, 0.25f)
    drawOval(
        color = Color(0xFF111111).copy(alpha = 0.16f),
        topLeft = Offset(w * (0.5f - 0.28f * shadowScale), h * 0.87f),
        size = Size(w * 0.56f * shadowScale, h * 0.08f)
    )

    translate(top = yOffset) {
        // 1. Tail behind body
        val tailBase = Offset(w * 0.72f, h * 0.72f)
        rotate(degrees = tailAngle, pivot = tailBase) {
            val tailPath = Path().apply {
                moveTo(tailBase.x, tailBase.y)
                cubicTo(
                    w * 0.90f, h * 0.68f,
                    w * 0.92f, h * 0.50f,
                    w * 0.85f, h * 0.45f
                )
            }
            drawPath(tailPath, color = ink, style = Stroke(width = sw * 1.4f, cap = StrokeCap.Round))
            drawPath(tailPath, color = furBody, style = Stroke(width = sw * 0.75f, cap = StrokeCap.Round))
            // Tail tuft
            drawCircle(color = maneFill, radius = w * 0.065f, center = Offset(w * 0.85f, h * 0.44f))
            drawCircle(color = ink, radius = w * 0.065f, center = Offset(w * 0.85f, h * 0.44f), style = Stroke(width = sw * 0.9f))
        }

        // 2. Chunky round body
        val bodyRect = Rect(w * 0.24f, h * 0.50f, w * 0.76f, h * 0.88f)
        drawOval(color = furBody, topLeft = bodyRect.topLeft, size = bodyRect.size)
        drawOval(color = ink, topLeft = bodyRect.topLeft, size = bodyRect.size, style = Stroke(width = sw))

        // Cream belly patch
        val bellyRect = Rect(w * 0.34f, h * 0.58f, w * 0.66f, h * 0.85f)
        drawOval(color = creamBelly, topLeft = bellyRect.topLeft, size = bellyRect.size)

        // Royal ribbon / sash for ROYAL & GRANDMASTER
        if (rank == LionRank.ROYAL || rank == LionRank.GRANDMASTER) {
            val sash = Path().apply {
                moveTo(w * 0.33f, h * 0.63f)
                lineTo(w * 0.67f, h * 0.78f)
                lineTo(w * 0.64f, h * 0.83f)
                lineTo(w * 0.30f, h * 0.68f)
                close()
            }
            drawPath(sash, color = if (rank == LionRank.GRANDMASTER) maneHighlight else Color(0xFF3B7A57))
            drawPath(sash, color = ink, style = Stroke(width = sw * 0.7f, join = StrokeJoin.Round))
        }

        // 3. Little legs / paws at bottom
        val leftFoot = Offset(w * 0.36f, h * 0.85f)
        val rightFoot = Offset(w * 0.64f, h * 0.85f)
        for (foot in listOf(leftFoot, rightFoot)) {
            drawOval(
                color = creamBelly,
                topLeft = Offset(foot.x - w * 0.08f, foot.y - h * 0.045f),
                size = Size(w * 0.16f, h * 0.09f)
            )
            drawOval(
                color = ink,
                topLeft = Offset(foot.x - w * 0.08f, foot.y - h * 0.045f),
                size = Size(w * 0.16f, h * 0.09f),
                style = Stroke(width = sw * 0.9f)
            )
        }

        // 4. Arm paws (pose depends on mood!)
        when (mood) {
            LionMood.CHEERING, LionMood.CROWNED -> {
                // Raised happy paws!
                drawCircle(furBody, w * 0.07f, Offset(w * 0.21f, h * 0.53f))
                drawCircle(ink, w * 0.07f, Offset(w * 0.21f, h * 0.53f), style = Stroke(sw * 0.9f))
                drawCircle(furBody, w * 0.07f, Offset(w * 0.79f, h * 0.53f))
                drawCircle(ink, w * 0.07f, Offset(w * 0.79f, h * 0.53f), style = Stroke(sw * 0.9f))
            }
            LionMood.THINKING -> {
                // One paw on chin, other resting
                drawCircle(furBody, w * 0.07f, Offset(w * 0.28f, h * 0.70f))
                drawCircle(ink, w * 0.07f, Offset(w * 0.28f, h * 0.70f), style = Stroke(sw * 0.9f))
                drawCircle(furBody, w * 0.075f, Offset(w * 0.62f, h * 0.54f))
                drawCircle(ink, w * 0.075f, Offset(w * 0.62f, h * 0.54f), style = Stroke(sw * 0.9f))
            }
            else -> {
                // Cozy tucked paws
                drawCircle(furBody, w * 0.068f, Offset(w * 0.27f, h * 0.68f))
                drawCircle(ink, w * 0.068f, Offset(w * 0.27f, h * 0.68f), style = Stroke(sw * 0.9f))
                drawCircle(furBody, w * 0.068f, Offset(w * 0.73f, h * 0.68f))
                drawCircle(ink, w * 0.068f, Offset(w * 0.73f, h * 0.68f), style = Stroke(sw * 0.9f))
            }
        }

        // 5. Scalloped Lion Mane (size varies by Rank!)
        val headCenter = Offset(w * 0.50f, h * 0.37f)
        val maneRadius = when (rank) {
            LionRank.CUB -> w * 0.24f
            LionRank.STRATEGIST -> w * 0.29f
            LionRank.ROYAL -> w * 0.31f
            LionRank.GRANDMASTER -> w * 0.32f
        }

        if (rank == LionRank.CUB) {
            // Fluffy top tuft + lighter collar for Cub
            val tuft = Path().apply {
                moveTo(w * 0.43f, h * 0.17f)
                quadraticTo(w * 0.50f, h * 0.08f, w * 0.57f, h * 0.17f)
                quadraticTo(w * 0.50f, h * 0.21f, w * 0.43f, h * 0.17f)
                close()
            }
            drawPath(tuft, color = maneFill)
            drawPath(tuft, color = ink, style = Stroke(width = sw * 0.9f, join = StrokeJoin.Round))
            drawCircle(color = maneHighlight.copy(alpha = 0.7f), radius = maneRadius, center = headCenter)
            drawCircle(color = ink, radius = maneRadius, center = headCenter, style = Stroke(width = sw))
        } else {
            // 12-scallop flower mane
            val scallops = 12
            val manePath = Path()
            for (i in 0 until scallops) {
                val a1 = (2.0 * PI * i) / scallops - PI / 2.0
                val aMid = (2.0 * PI * (i + 0.5)) / scallops - PI / 2.0
                val a2 = (2.0 * PI * (i + 1)) / scallops - PI / 2.0
                val p1 = Offset(
                    headCenter.x + (maneRadius * 0.88f * cos(a1)).toFloat(),
                    headCenter.y + (maneRadius * 0.88f * sin(a1)).toFloat()
                )
                val ctrl = Offset(
                    headCenter.x + (maneRadius * 1.15f * cos(aMid)).toFloat(),
                    headCenter.y + (maneRadius * 1.15f * sin(aMid)).toFloat()
                )
                val p2 = Offset(
                    headCenter.x + (maneRadius * 0.88f * cos(a2)).toFloat(),
                    headCenter.y + (maneRadius * 0.88f * sin(a2)).toFloat()
                )
                if (i == 0) manePath.moveTo(p1.x, p1.y)
                manePath.quadraticTo(ctrl.x, ctrl.y, p2.x, p2.y)
            }
            manePath.close()
            drawPath(manePath, color = maneFill)
            drawPath(manePath, color = ink, style = Stroke(width = sw, join = StrokeJoin.Round))
        }

        // 6. Round Bear-Lion Ears
        for (sign in listOf(-1f, 1f)) {
            val earCenter = Offset(headCenter.x + sign * w * 0.19f, headCenter.y - h * 0.14f)
            drawCircle(color = furBody, radius = w * 0.068f, center = earCenter)
            drawCircle(color = ink, radius = w * 0.068f, center = earCenter, style = Stroke(width = sw * 0.9f))
            drawCircle(color = maneFill, radius = w * 0.032f, center = earCenter)
        }

        // 7. Face circle
        val faceRx = w * 0.21f
        val faceRy = h * 0.185f
        val faceRect = Rect(
            headCenter.x - faceRx,
            headCenter.y - faceRy,
            headCenter.x + faceRx,
            headCenter.y + faceRy
        )
        drawOval(color = furBody, topLeft = faceRect.topLeft, size = faceRect.size)
        drawOval(color = ink, topLeft = faceRect.topLeft, size = faceRect.size, style = Stroke(width = sw * 0.95f))

        // Cream muzzle pad
        drawOval(
            color = creamBelly,
            topLeft = Offset(headCenter.x - w * 0.12f, headCenter.y + h * 0.01f),
            size = Size(w * 0.24f, h * 0.135f)
        )

        // Rosy cheeks
        drawCircle(color = cheekPink.copy(alpha = 0.55f), radius = w * 0.036f, center = Offset(headCenter.x - w * 0.13f, headCenter.y + h * 0.035f))
        drawCircle(color = cheekPink.copy(alpha = 0.55f), radius = w * 0.036f, center = Offset(headCenter.x + w * 0.13f, headCenter.y + h * 0.035f))

        // 8. Eyes & Expression
        val leftEye = Offset(headCenter.x - w * 0.082f, headCenter.y - h * 0.025f)
        val rightEye = Offset(headCenter.x + w * 0.082f, headCenter.y - h * 0.025f)

        when (mood) {
            LionMood.CHEERING, LionMood.CROWNED -> {
                // Joyful ^ ^ arcs
                for (eye in listOf(leftEye, rightEye)) {
                    drawArc(
                        color = ink,
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(eye.x - w * 0.035f, eye.y - h * 0.025f),
                        size = Size(w * 0.07f, h * 0.05f),
                        style = Stroke(width = sw * 0.9f, cap = StrokeCap.Round)
                    )
                }
            }
            LionMood.SLEEPY -> {
                for (eye in listOf(leftEye, rightEye)) {
                    drawLine(
                        color = ink,
                        start = Offset(eye.x - w * 0.032f, eye.y),
                        end = Offset(eye.x + w * 0.032f, eye.y),
                        strokeWidth = sw * 0.85f,
                        cap = StrokeCap.Round
                    )
                }
            }
            LionMood.SHOCKED -> {
                for (eye in listOf(leftEye, rightEye)) {
                    drawCircle(color = Color.White, radius = w * 0.036f, center = eye)
                    drawCircle(color = ink, radius = w * 0.036f, center = eye, style = Stroke(sw * 0.8f))
                    drawCircle(color = ink, radius = w * 0.015f, center = eye)
                }
                // Comic sweat drop
                val drop = Path().apply {
                    moveTo(headCenter.x + w * 0.17f, headCenter.y - h * 0.09f)
                    quadraticTo(headCenter.x + w * 0.21f, headCenter.y - h * 0.02f, headCenter.x + w * 0.17f, headCenter.y - h * 0.01f)
                    quadraticTo(headCenter.x + w * 0.13f, headCenter.y - h * 0.02f, headCenter.x + w * 0.17f, headCenter.y - h * 0.09f)
                    close()
                }
                drawPath(drop, color = Color(0xFF9CD3EB))
                drawPath(drop, color = ink, style = Stroke(sw * 0.55f))
            }
            else -> {
                // Friendly round button eyes with sparkle highlight
                for (eye in listOf(leftEye, rightEye)) {
                    drawCircle(color = ink, radius = w * 0.028f, center = eye)
                    drawCircle(color = Color.White, radius = w * 0.010f, center = Offset(eye.x - w * 0.008f, eye.y - h * 0.008f))
                }
                if (mood == LionMood.THINKING) {
                    // Thoughtful raised brow
                    drawLine(
                        color = ink,
                        start = Offset(leftEye.x - w * 0.03f, leftEye.y - h * 0.045f),
                        end = Offset(leftEye.x + w * 0.03f, leftEye.y - h * 0.055f),
                        strokeWidth = sw * 0.7f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // 9. Cute triangular nose & ":3" smile
        val nose = Path().apply {
            moveTo(headCenter.x - w * 0.036f, headCenter.y + h * 0.025f)
            lineTo(headCenter.x + w * 0.036f, headCenter.y + h * 0.025f)
            lineTo(headCenter.x, headCenter.y + h * 0.058f)
            close()
        }
        drawPath(nose, color = Color(0xFF8C4A27))
        drawPath(nose, color = ink, style = Stroke(width = sw * 0.65f, join = StrokeJoin.Round))

        if (mood == LionMood.SHOCKED) {
            drawOval(
                color = Color(0xFF8C4A27),
                topLeft = Offset(headCenter.x - w * 0.025f, headCenter.y + h * 0.075f),
                size = Size(w * 0.05f, h * 0.038f)
            )
            drawOval(
                color = ink,
                topLeft = Offset(headCenter.x - w * 0.025f, headCenter.y + h * 0.075f),
                size = Size(w * 0.05f, h * 0.038f),
                style = Stroke(sw * 0.65f)
            )
        } else {
            // :3 cat-lion mouth
            drawArc(
                color = ink,
                startAngle = 15f,
                sweepAngle = 155f,
                useCenter = false,
                topLeft = Offset(headCenter.x - w * 0.055f, headCenter.y + h * 0.048f),
                size = Size(w * 0.055f, h * 0.042f),
                style = Stroke(width = sw * 0.72f, cap = StrokeCap.Round)
            )
            drawArc(
                color = ink,
                startAngle = 10f,
                sweepAngle = 155f,
                useCenter = false,
                topLeft = Offset(headCenter.x, headCenter.y + h * 0.048f),
                size = Size(w * 0.055f, h * 0.042f),
                style = Stroke(width = sw * 0.72f, cap = StrokeCap.Round)
            )
        }

        // 10. Whiskers
        for (sign in listOf(-1f, 1f)) {
            drawLine(
                color = ink,
                start = Offset(headCenter.x + sign * w * 0.14f, headCenter.y + h * 0.035f),
                end = Offset(headCenter.x + sign * w * 0.22f, headCenter.y + h * 0.022f),
                strokeWidth = sw * 0.55f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = ink,
                start = Offset(headCenter.x + sign * w * 0.14f, headCenter.y + h * 0.062f),
                end = Offset(headCenter.x + sign * w * 0.22f, headCenter.y + h * 0.068f),
                strokeWidth = sw * 0.55f,
                cap = StrokeCap.Round
            )
        }

        // 11. Golden King Crown for GRANDMASTER or CROWNED mood
        if (rank == LionRank.GRANDMASTER || mood == LionMood.CROWNED) {
            val crownPath = Path().apply {
                moveTo(headCenter.x - w * 0.12f, headCenter.y - h * 0.20f)
                lineTo(headCenter.x - w * 0.15f, headCenter.y - h * 0.31f)
                lineTo(headCenter.x - w * 0.06f, headCenter.y - h * 0.25f)
                lineTo(headCenter.x, headCenter.y - h * 0.34f)
                lineTo(headCenter.x + w * 0.06f, headCenter.y - h * 0.25f)
                lineTo(headCenter.x + w * 0.15f, headCenter.y - h * 0.31f)
                lineTo(headCenter.x + w * 0.12f, headCenter.y - h * 0.20f)
                close()
            }
            drawPath(crownPath, color = maneHighlight)
            drawPath(crownPath, color = ink, style = Stroke(width = sw * 0.85f, join = StrokeJoin.Round))
        }

        // Sparkles when cheering!
        if (mood == LionMood.CHEERING || mood == LionMood.CROWNED) {
            drawCircle(color = maneHighlight, radius = w * 0.025f, center = Offset(w * 0.14f, h * 0.24f))
            drawCircle(color = ink, radius = w * 0.025f, center = Offset(w * 0.14f, h * 0.24f), style = Stroke(sw * 0.5f))
            drawCircle(color = maneHighlight, radius = w * 0.022f, center = Offset(w * 0.86f, h * 0.22f))
            drawCircle(color = ink, radius = w * 0.022f, center = Offset(w * 0.86f, h * 0.22f), style = Stroke(sw * 0.5f))
        }
    }
}

/**
 * Vertical Japanese-Bauhaus Game Stage inspired by the reference screenshot:
 * Chess skyline silhouette, Léon bouncing over rooftops, XP gauge, and Golden Crowns counter.
 */
@Composable
fun LionHeroStage(
    xp: Int,
    goldenCrowns: Int,
    streakDays: Int,
    estimatedElo: Int,
    rank: LionRank,
    levelProgressFraction: Float,
    levelProgressPercent: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    var tipIndex by remember { mutableIntStateOf(0) }
    var tapMood by remember { mutableIntStateOf(0) }

    val tips = remember(isEn, streakDays, estimatedElo) {
        if (isEn) {
            listOf(
                "Roar! Solve 3 puzzles today to sharpen your tactical sight.",
                "Control the center, develop knights & bishops, castle early!",
                "Look for checks, captures, and threats on every single move.",
                "Your tactical estimate is $estimatedElo Elo—keep climbing!"
            )
        } else {
            listOf(
                "Graou ! Résous 3 puzzles aujourd'hui pour aiguiser ton œil tactique.",
                "Contrôle le centre, développe tes pièces légères et roque vite !",
                "Vérifie toujours : Échecs, Prises et Menaces avant de jouer.",
                "Ton Elo tactique estimé est de $estimatedElo — continuons l'ascension !"
            )
        }
    }

    val currentMood = when {
        tapMood % 2 == 1 -> LionMood.CHEERING
        rank == LionRank.GRANDMASTER -> LionMood.CROWNED
        else -> LionMood.IDLE_HAPPY
    }

    val progressAnim by animateFloatAsState(
        targetValue = levelProgressFraction.coerceIn(0.04f, 1f),
        animationSpec = spring(),
        label = "xp_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val offsetPx = 4.dp.toPx()
                val r = 22.dp.toPx()
                drawRoundRect(
                    color = palette.inkShadow,
                    topLeft = Offset(offsetPx, offsetPx),
                    size = size,
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .clip(RoundedCornerShape(22.dp))
            .background(palette.bgTop)
            .border(2.dp, palette.glassBorderStart, RoundedCornerShape(22.dp))
            .clickable {
                tipIndex = (tipIndex + 1) % tips.size
                tapMood++
            }
            .testTag("lion_hero_stage")
    ) {
        // Minimalist Monochrome Chess Skyline Background (matching reference screenshot!)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(215.dp)
        ) {
            val w = size.width
            val h = size.height
            val towerLight = palette.paperSand
            val towerMid = palette.paperTaupe
            val towerDark = if (palette.isDark) Color(0xFF3E3A33) else Color(0xFF333333)
            val inkLine = palette.glassBorderStart

            // Horizon buildings & chess-rook towers
            drawRect(towerLight, topLeft = Offset(w * 0.03f, h * 0.56f), size = Size(w * 0.16f, h * 0.44f))
            drawRect(towerMid, topLeft = Offset(w * 0.17f, h * 0.44f), size = Size(w * 0.15f, h * 0.56f))
            // Rook battlements on left tower
            drawRect(towerMid, topLeft = Offset(w * 0.17f, h * 0.40f), size = Size(w * 0.035f, h * 0.04f))
            drawRect(towerMid, topLeft = Offset(w * 0.225f, h * 0.40f), size = Size(w * 0.035f, h * 0.04f))
            drawRect(towerMid, topLeft = Offset(w * 0.28f, h * 0.40f), size = Size(w * 0.04f, h * 0.04f))

            drawRect(towerLight, topLeft = Offset(w * 0.38f, h * 0.62f), size = Size(w * 0.18f, h * 0.38f))
            drawRect(towerMid, topLeft = Offset(w * 0.58f, h * 0.47f), size = Size(w * 0.17f, h * 0.53f))
            drawRect(towerDark, topLeft = Offset(w * 0.77f, h * 0.38f), size = Size(w * 0.20f, h * 0.62f))
            // King spire on right dark tower
            drawLine(towerDark, Offset(w * 0.87f, h * 0.27f), Offset(w * 0.87f, h * 0.38f), strokeWidth = 5f)
            drawLine(towerDark, Offset(w * 0.83f, h * 0.32f), Offset(w * 0.91f, h * 0.32f), strokeWidth = 5f)

            // Crisp architectural windows on right tower
            val winColor = palette.bgTop
            for (row in 0..3) {
                for (col in 0..2) {
                    drawRect(
                        color = winColor.copy(alpha = 0.72f),
                        topLeft = Offset(w * (0.80f + col * 0.05f), h * (0.44f + row * 0.075f)),
                        size = Size(w * 0.032f, h * 0.042f)
                    )
                }
            }

            // Foreground rooftop silhouette
            drawRect(towerDark, topLeft = Offset(0f, h * 0.80f), size = Size(w * 0.44f, h * 0.20f))
            drawRect(towerDark, topLeft = Offset(w * 0.50f, h * 0.85f), size = Size(w * 0.50f, h * 0.15f))
            drawLine(inkLine, Offset(0f, h * 0.80f), Offset(w * 0.44f, h * 0.80f), strokeWidth = 2.5f)

            // Trail of mini golden chess treats / points leading from Léon to Goal
            val dots = listOf(
                Offset(w * 0.46f, h * 0.68f),
                Offset(w * 0.54f, h * 0.63f),
                Offset(w * 0.62f, h * 0.65f)
            )
            dots.forEach { d ->
                drawCircle(color = palette.lionMane, radius = 5.5.dp.toPx(), center = d)
                drawCircle(color = inkLine, radius = 5.5.dp.toPx(), center = d, style = Stroke(1.5.dp.toPx()))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top HUD row inside viewport (matching WEIGHT 42% + kibble count in screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loc(isEn, rank.tagFr, rank.tagEn) + " • " + loc(isEn, rank.titleFr, rank.titleEn).uppercase(),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(116.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(palette.bgTop)
                                .border(1.5.dp, palette.glassBorderStart, RoundedCornerShape(50))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressAnim)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(palette.textPrimary)
                            )
                        }
                        Text(
                            text = "$levelProgressPercent%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Streak pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.glassSurfaceElevated,
                        border = BorderStroke(1.2.dp, palette.glassBorderStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = palette.lionMane,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${streakDays}j",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }
                    }
                    // Golden Crowns pill (like × 18 in screenshot)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.glassSurfaceElevated,
                        border = BorderStroke(1.2.dp, palette.glassBorderStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = palette.lionMane,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "× $goldenCrowns",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Mascot Stage Row: Léon + Speech Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LionCharacter(
                    mood = currentMood,
                    rank = rank,
                    modifier = Modifier.size(116.dp),
                    animated = true,
                    isDark = palette.isDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = palette.bgTop.copy(alpha = 0.94f),
                    border = BorderStroke(1.5.dp, palette.glassBorderStart),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("LÉON LE COACH", "LEO THE COACH"),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.lionMane
                            )
                            Text(
                                text = "$xp XP",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedContent(targetState = tips[tipIndex], label = "lion_tip") { tip ->
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textPrimary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr("Touche Léon pour un conseil", "Tap Leo for another tip"),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal Evolution Strip echoing "CAT STATE" in the reference concept design.
 */
@Composable
fun LionStateEvolutionStrip(
    currentRank: LionRank,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    GlassCard(modifier = modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Text(
            text = tr("ÉVOLUTION DE LÉON • PALIERS XP", "LEO EVOLUTION • XP RANKS"),
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = palette.textSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LionRank.entries.forEachIndexed { idx, rank ->
                val isCurrent = rank == currentRank
                val isUnlocked = currentRank.level >= rank.level
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCurrent) palette.accentCyan.copy(alpha = 0.18f)
                            else Color.Transparent
                        )
                        .border(
                            width = if (isCurrent) 1.2.dp else 0.dp,
                            color = if (isCurrent) palette.glassBorderStart else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 6.dp, horizontal = 2.dp)
                ) {
                    LionCharacter(
                        mood = if (rank == LionRank.GRANDMASTER) LionMood.CROWNED else LionMood.IDLE_HAPPY,
                        rank = rank,
                        animated = isCurrent,
                        isDark = palette.isDark,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = loc(isEn, rank.titleFr, rank.titleEn).split(" ").first(),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isUnlocked) palette.textPrimary else palette.textSecondary.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (idx < LionRank.entries.lastIndex) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = palette.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact Mascot Reaction Banner for training modules (Lessons, Puzzles, Play AI, Quiz, Analysis).
 */
@Composable
fun LionCoachBanner(
    mood: LionMood,
    message: String,
    modifier: Modifier = Modifier,
    rank: LionRank = LionRank.STRATEGIST,
    accentColor: Color? = null
) {
    val palette = LocalGlassPalette.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        accentBorderColor = accentColor ?: palette.glassBorderStart,
        contentPadding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(palette.glassSurfaceElevated)
                    .border(1.2.dp, palette.glassBorderStart, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LionCharacter(
                    mood = mood,
                    rank = rank,
                    animated = true,
                    isDark = palette.isDark,
                    modifier = Modifier.size(44.dp)
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
