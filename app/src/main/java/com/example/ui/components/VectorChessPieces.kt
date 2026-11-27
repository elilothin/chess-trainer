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

/**
 * Pièces à relief : silhouettes Staunton (chemins SVG, voir [PieceShapes]) avec dégradé cylindrique,
 * reflet, ombre portée et liseré clair sur les noires pour rester lisibles sur toutes les cases.
 */
@Composable
fun VectorChessPiece(
    piece: ChessPiece,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawChessPiece(piece)
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

fun DrawScope.drawChessPiece(piece: ChessPiece) {
    val key = piece.type.shapeKey()
    val geometry = geometryCache[key] ?: return
    val isWhite = piece.color == PieceColor.WHITE
    val ink = if (isWhite) WhiteInk else BlackInk
    val detail = if (isWhite) WhiteDetail else BlackDetail

    val s = minOf(size.width, size.height) / 100f
    val offsetX = (size.width - 100f * s) / 2f
    val offsetY = (size.height - 100f * s) / 2f

    // Ombre portée douce sous la pièce
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.42f), Color.Transparent),
            center = Offset(offsetX + 50f * s, offsetY + 90f * s),
            radius = 34f * s
        ),
        topLeft = Offset(offsetX + 14f * s, offsetY + 82f * s),
        size = androidx.compose.ui.geometry.Size(72f * s, 16f * s)
    )

    translate(offsetX, offsetY) {
        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
            if (!isWhite) {
                // liseré clair autour des noires : lisibles même sur les cases sombres
                drawPath(
                    path = geometry.outline,
                    color = BlackHalo.copy(alpha = 0.55f),
                    style = Stroke(width = 8.6f, join = StrokeJoin.Round, cap = StrokeCap.Round)
                )
            }
            drawPath(
                path = geometry.outline,
                color = ink,
                style = Stroke(width = 6.0f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )
            // volume : dégradé horizontal (lumière venant de la gauche)
            drawPath(
                path = geometry.outline,
                brush = Brush.horizontalGradient(
                    colorStops = (if (isWhite) whiteBody else blackBody).toTypedArray(),
                    startX = 12f,
                    endX = 88f
                ),
                style = Fill
            )
            clipPath(geometry.outline) {
                // ombre/lumière verticale : haut éclairé, pied plus sombre
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = if (isWhite) 0.22f else 0.14f),
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = if (isWhite) 0.26f else 0.34f)
                        ),
                        startY = 0f,
                        endY = 100f
                    ),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(100f, 100f)
                )
                // reflet spéculaire vertical
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.5f to Color.White.copy(alpha = if (isWhite) 0.55f else 0.30f),
                            1.0f to Color.Transparent
                        ),
                        startX = 30f,
                        endX = 44f
                    ),
                    topLeft = Offset(30f, 8f),
                    size = androidx.compose.ui.geometry.Size(14f, 78f)
                )
            }
            geometry.details.forEach { p ->
                drawPath(
                    path = p,
                    color = detail,
                    style = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            PieceShapes.eye[key]?.let { (ex, ey) ->
                drawCircle(color = detail, radius = 2.6f, center = Offset(ex, ey))
            }
        }
    }
}
