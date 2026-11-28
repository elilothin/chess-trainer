package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import com.example.domain.model.ChessPiece
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType
import com.example.ui.theme.LocalPieceStyle
import com.example.ui.theme.PieceStyle

/**
 * Pièces à relief : silhouettes Staunton (chemins SVG, voir [PieceShapes]) avec dégradé cylindrique,
 * reflet, ombre portée et liseré clair sur les noires pour rester lisibles sur toutes les cases.
 */
@Composable
fun VectorChessPiece(
    piece: ChessPiece,
    modifier: Modifier = Modifier
) {
    val style = LocalPieceStyle.current
    Canvas(modifier = modifier.fillMaxSize()) {
        drawChessPiece(piece, style)
    }
}

private class PieceGeometry(
    val outline: Path,
    val details: List<Path>
)

private val geometryCache: Map<Char, PieceGeometry> by lazy {
    PieceShapes.outline.mapValues { (key, d) ->
        PieceGeometry(
            outline = PathParser().parsePathString(d).toPath(),
            details = (PieceShapes.details[key] ?: emptyList()).map { PathParser().parsePathString(it).toPath() }
        )
    }
}

private val WhiteInk = Color(0xFF1A1712)
private val WhiteDetail = Color(0xFF8A7F68)
private val BlackInk = Color(0xFF050404)
private val BlackDetail = Color(0xFFB9AE98)
private val BlackHalo = Color(0xFFD8CFBC)

private val whiteBody = listOf(
    0.00f to Color(0xFFEFE6D0),
    0.30f to Color(0xFFFFFCF2),
    0.62f to Color(0xFFE2D5B8),
    1.00f to Color(0xFFA9976F)
)
private val blackBody = listOf(
    0.00f to Color(0xFF4A453F),
    0.30f to Color(0xFF6A645B),
    0.60f to Color(0xFF292622),
    1.00f to Color(0xFF0B0A09)
)

private fun PieceType.shapeKey(): Char = when (this) {
    PieceType.PAWN -> 'P'
    PieceType.ROOK -> 'R'
    PieceType.KNIGHT -> 'N'
    PieceType.BISHOP -> 'B'
    PieceType.QUEEN -> 'Q'
    PieceType.KING -> 'K'
}

private class Look(
    val body: List<Pair<Float, Color>>,
    val ink: Color,
    val detail: Color,
    val halo: Color? = null,
    val vShade: Boolean = true,
    val specular: Float = 0.5f,
    val neon: Boolean = false,
    val neonColor: Color = Color.Unspecified,
    val groundShadow: Float = 0.42f
)

private fun lookFor(style: PieceStyle, white: Boolean): Look = when (style) {
    PieceStyle.RELIEF -> if (white) Look(whiteBody, WhiteInk, WhiteDetail, null, true, 0.55f)
    else Look(blackBody, BlackInk, BlackDetail, BlackHalo.copy(alpha = 0.55f), true, 0.30f)
    PieceStyle.FLAT -> if (white) Look(listOf(0f to Color(0xFFF3ECDD), 1f to Color(0xFFF3ECDD)), WhiteInk, WhiteDetail, null, false, 0f, groundShadow = 0.22f)
    else Look(listOf(0f to Color(0xFF2B2824), 1f to Color(0xFF2B2824)), BlackInk, BlackDetail, BlackHalo.copy(alpha = 0.55f), false, 0f, groundShadow = 0.22f)
    PieceStyle.METAL -> if (white) Look(
        listOf(0f to Color(0xFFD9DEE4), 0.28f to Color(0xFFFFFFFF), 0.55f to Color(0xFFB4BCC6), 0.8f to Color(0xFF8D97A4), 1f to Color(0xFF6F7885)),
        Color(0xFF232A33), Color(0xFF59626E), null, true, 0.75f
    ) else Look(
        listOf(0f to Color(0xFFB9923F), 0.28f to Color(0xFFF1D589), 0.55f to Color(0xFF9C7A2E), 0.8f to Color(0xFF6B511C), 1f to Color(0xFF3F2F0F)),
        Color(0xFF1E1606), Color(0xFF3A2B0C), Color(0xFFFFE9A8).copy(alpha = 0.45f), true, 0.7f
    )
    PieceStyle.GLASS -> if (white) Look(
        listOf(0f to Color(0xCCE9F6FF), 0.35f to Color(0xE6FFFFFF), 0.7f to Color(0xA6B9DDF2), 1f to Color(0x8C8DB8D4)),
        Color(0xFF3A5873), Color(0xFF4F7390), Color(0xFFFFFFFF).copy(alpha = 0.55f), true, 0.8f, groundShadow = 0.30f
    ) else Look(
        listOf(0f to Color(0xD9758AA3), 0.35f to Color(0xE0495A72), 0.7f to Color(0xEB222B3A), 1f to Color(0xF20E121A)),
        Color(0xFF05070B), Color(0xFFB8CCE0), Color(0xFFBEE3FF).copy(alpha = 0.55f), true, 0.65f, groundShadow = 0.30f
    )
    PieceStyle.NEON -> {
        val c = if (white) Color(0xFF8FE9FF) else Color(0xFFFFB454)
        Look(listOf(0f to c.copy(alpha = 0.20f), 1f to c.copy(alpha = 0.08f)), c, c, null, false, 0f, true, c, 0.15f)
    }
}

fun DrawScope.drawChessPiece(piece: ChessPiece, style: PieceStyle = PieceStyle.RELIEF) {
    val key = piece.type.shapeKey()
    val geometry = geometryCache[key] ?: return
    val look = lookFor(style, piece.color == PieceColor.WHITE)

    val s = minOf(size.width, size.height) / 100f
    val offsetX = (size.width - 100f * s) / 2f
    val offsetY = (size.height - 100f * s) / 2f

    // Ombre portée douce sous la pièce
    if (look.groundShadow > 0f) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = look.groundShadow), Color.Transparent),
                center = Offset(offsetX + 50f * s, offsetY + 90f * s),
                radius = 34f * s
            ),
            topLeft = Offset(offsetX + 14f * s, offsetY + 82f * s),
            size = androidx.compose.ui.geometry.Size(72f * s, 16f * s)
        )
    }

    translate(offsetX, offsetY) {
        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
            if (look.neon) {
                // néon : halo diffus, remplissage translucide, trait net
                drawPath(geometry.outline, look.neonColor.copy(alpha = 0.14f), style = Stroke(width = 12f, join = StrokeJoin.Round, cap = StrokeCap.Round))
                drawPath(geometry.outline, look.neonColor.copy(alpha = 0.28f), style = Stroke(width = 7.5f, join = StrokeJoin.Round, cap = StrokeCap.Round))
                drawPath(geometry.outline, brush = Brush.horizontalGradient(colorStops = look.body.toTypedArray(), startX = 12f, endX = 88f), style = Fill)
                drawPath(geometry.outline, look.neonColor, style = Stroke(width = 3.0f, join = StrokeJoin.Round, cap = StrokeCap.Round))
                geometry.details.forEach { p ->
                    drawPath(p, look.neonColor.copy(alpha = 0.75f), style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                PieceShapes.eye[key]?.let { (ex, ey) -> drawCircle(look.neonColor, radius = 2.4f, center = Offset(ex, ey)) }
                return@scale
            }
            look.halo?.let { halo ->
                drawPath(geometry.outline, halo, style = Stroke(width = 8.6f, join = StrokeJoin.Round, cap = StrokeCap.Round))
            }
            drawPath(geometry.outline, look.ink, style = Stroke(width = 6.0f, join = StrokeJoin.Round, cap = StrokeCap.Round))
            drawPath(
                path = geometry.outline,
                brush = Brush.horizontalGradient(colorStops = look.body.toTypedArray(), startX = 12f, endX = 88f),
                style = Fill
            )
            if (look.vShade || look.specular > 0f) {
                clipPath(geometry.outline) {
                    if (look.vShade) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.White.copy(alpha = 0.20f),
                                    0.5f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.30f)
                                ),
                                startY = 0f, endY = 100f
                            ),
                            topLeft = Offset.Zero,
                            size = androidx.compose.ui.geometry.Size(100f, 100f)
                        )
                    }
                    if (look.specular > 0f) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.5f to Color.White.copy(alpha = look.specular),
                                    1.0f to Color.Transparent
                                ),
                                startX = 30f, endX = 44f
                            ),
                            topLeft = Offset(30f, 8f),
                            size = androidx.compose.ui.geometry.Size(14f, 78f)
                        )
                    }
                }
            }
            geometry.details.forEach { p ->
                drawPath(p, look.detail, style = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            PieceShapes.eye[key]?.let { (ex, ey) ->
                drawCircle(color = look.detail, radius = 2.6f, center = Offset(ex, ey))
            }
        }
    }
}
