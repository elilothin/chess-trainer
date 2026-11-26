package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import com.example.domain.model.ChessPiece
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType

/**
 * Jeu de pièces sobre : silhouettes pleines à contour net, dessinées à partir de chemins SVG
 * (voir [PieceShapes]). Une seule teinte par camp, aucune dégradation de lisibilité sur
 * les cases claires ou sombres.
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

private val WhiteFill = Color(0xFFF5F0E6)
private val WhiteInk = Color(0xFF15130F)
private val WhiteDetail = Color(0xFF7D776A)
private val BlackFill = Color(0xFF2B2824)
private val BlackInk = Color(0xFF0A0908)
private val BlackDetail = Color(0xFFCFC6B4)

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
    val fill = if (isWhite) WhiteFill else BlackFill
    val ink = if (isWhite) WhiteInk else BlackInk
    val detail = if (isWhite) WhiteDetail else BlackDetail

    val s = minOf(size.width, size.height) / 100f
    val offsetX = (size.width - 100f * s) / 2f
    val offsetY = (size.height - 100f * s) / 2f

    // Ombre portée très discrète
    drawOval(
        color = Color.Black.copy(alpha = 0.14f),
        topLeft = Offset(offsetX + 22f * s, offsetY + 88f * s),
        size = androidx.compose.ui.geometry.Size(56f * s, 7f * s)
    )

    translate(offsetX, offsetY) {
        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
            // 1) contour épais derrière, 2) remplissage par-dessus : seul le contour extérieur reste visible
            drawPath(
                path = geometry.outline,
                color = ink,
                style = Stroke(width = 6.4f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )
            drawPath(path = geometry.outline, color = fill, style = Fill)
            geometry.details.forEach { p ->
                drawPath(
                    path = p,
                    color = detail,
                    style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            PieceShapes.eye[key]?.let { (ex, ey) ->
                drawCircle(color = detail, radius = 2.6f, center = Offset(ex, ey))
            }
        }
    }
}
