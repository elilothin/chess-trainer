package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.ChessPiece
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType

/**
 * High-precision Staunton-inspired vector chess pieces drawn on Compose Canvas.
 * Replaces text glyphs for crisp 60fps rendering across all DPIs and themes.
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

fun DrawScope.drawChessPiece(piece: ChessPiece) {
    val w = size.width
    val h = size.height
    val isWhite = piece.color == PieceColor.WHITE
    val fillColor = if (isWhite) Color(0xFFFAF7EE) else Color(0xFF24221F)
    val strokeColor = if (isWhite) Color(0xFF111111) else Color(0xFF111111)
    val detailColor = if (isWhite) Color(0xFF59564E) else Color(0xFFE6E1D7)
    val strokeWidth = (w * 0.052f).coerceAtLeast(2.0f)

    // Subtle crisp ink ground shadow
    drawOval(
        color = Color(0xFF111111).copy(alpha = 0.16f),
        topLeft = Offset(w * 0.19f, h * 0.845f),
        size = androidx.compose.ui.geometry.Size(w * 0.62f, h * 0.095f)
    )

    when (piece.type) {
        PieceType.PAWN -> drawPawn(w, h, fillColor, strokeColor, detailColor, strokeWidth)
        PieceType.ROOK -> drawRook(w, h, fillColor, strokeColor, detailColor, strokeWidth)
        PieceType.KNIGHT -> drawKnight(w, h, fillColor, strokeColor, detailColor, strokeWidth)
        PieceType.BISHOP -> drawBishop(w, h, fillColor, strokeColor, detailColor, strokeWidth)
        PieceType.QUEEN -> drawQueen(w, h, fillColor, strokeColor, detailColor, strokeWidth)
        PieceType.KING -> drawKing(w, h, fillColor, strokeColor, detailColor, strokeWidth)
    }
}

private fun DrawScope.drawBase(w: Float, h: Float, fill: Color, stroke: Color, sw: Float) {
    val base = Path().apply {
        moveTo(w * 0.22f, h * 0.86f)
        lineTo(w * 0.78f, h * 0.86f)
        lineTo(w * 0.74f, h * 0.77f)
        lineTo(w * 0.26f, h * 0.77f)
        close()
    }
    drawPath(base, color = fill, style = Fill)
    drawPath(base, color = stroke, style = Stroke(width = sw, join = StrokeJoin.Round))
}

private fun DrawScope.drawPawn(w: Float, h: Float, fill: Color, stroke: Color, detail: Color, sw: Float) {
    val body = Path().apply {
        moveTo(w * 0.30f, h * 0.77f)
        quadraticTo(w * 0.40f, h * 0.56f, w * 0.41f, h * 0.45f)
        lineTo(w * 0.59f, h * 0.45f)
        quadraticTo(w * 0.60f, h * 0.56f, w * 0.70f, h * 0.77f)
        close()
    }
    drawPath(body, color = fill, style = Fill)
    drawPath(body, color = stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Collar
    drawLine(
        color = stroke,
        start = Offset(w * 0.36f, h * 0.45f),
        end = Offset(w * 0.64f, h * 0.45f),
        strokeWidth = sw * 1.1f,
        cap = StrokeCap.Round
    )
    // Head
    drawCircle(color = fill, radius = w * 0.145f, center = Offset(w * 0.50f, h * 0.31f))
    drawCircle(color = stroke, radius = w * 0.145f, center = Offset(w * 0.50f, h * 0.31f), style = Stroke(width = sw))
    drawCircle(color = detail.copy(alpha = 0.35f), radius = w * 0.04f, center = Offset(w * 0.45f, h * 0.27f))
    drawBase(w, h, fill, stroke, sw)
}

private fun DrawScope.drawRook(w: Float, h: Float, fill: Color, stroke: Color, detail: Color, sw: Float) {
    val tower = Path().apply {
        moveTo(w * 0.29f, h * 0.77f)
        lineTo(w * 0.33f, h * 0.38f)
        lineTo(w * 0.27f, h * 0.33f)
        lineTo(w * 0.27f, h * 0.20f)
        lineTo(w * 0.37f, h * 0.20f)
        lineTo(w * 0.37f, h * 0.27f)
        lineTo(w * 0.45f, h * 0.27f)
        lineTo(w * 0.45f, h * 0.20f)
        lineTo(w * 0.55f, h * 0.20f)
        lineTo(w * 0.55f, h * 0.27f)
        lineTo(w * 0.63f, h * 0.27f)
        lineTo(w * 0.63f, h * 0.20f)
        lineTo(w * 0.73f, h * 0.20f)
        lineTo(w * 0.73f, h * 0.33f)
        lineTo(w * 0.67f, h * 0.38f)
        lineTo(w * 0.71f, h * 0.77f)
        close()
    }
    drawPath(tower, color = fill, style = Fill)
    drawPath(tower, color = stroke, style = Stroke(width = sw, join = StrokeJoin.Round))
    drawLine(detail, Offset(w * 0.35f, h * 0.38f), Offset(w * 0.65f, h * 0.38f), strokeWidth = sw * 0.8f)
    drawBase(w, h, fill, stroke, sw)
}

private fun DrawScope.drawKnight(w: Float, h: Float, fill: Color, stroke: Color, detail: Color, sw: Float) {
    val knight = Path().apply {
        moveTo(w * 0.30f, h * 0.77f)
        quadraticTo(w * 0.38f, h * 0.60f, w * 0.44f, h * 0.50f)
        quadraticTo(w * 0.30f, h * 0.54f, w * 0.22f, h * 0.47f)
        quadraticTo(w * 0.20f, h * 0.41f, w * 0.28f, h * 0.34f)
        lineTo(w * 0.39f, h * 0.25f)
        lineTo(w * 0.43f, h * 0.16f)
        lineTo(w * 0.50f, h * 0.21f)
        quadraticTo(w * 0.68f, h * 0.22f, w * 0.74f, h * 0.42f)
        quadraticTo(w * 0.78f, h * 0.60f, w * 0.69f, h * 0.77f)
        close()
    }
    drawPath(knight, color = fill, style = Fill)
    drawPath(knight, color = stroke, style = Stroke(width = sw, join = StrokeJoin.Round))
    // Eye & mane accent
    drawCircle(color = detail, radius = w * 0.032f, center = Offset(w * 0.42f, h * 0.33f))
    drawBase(w, h, fill, stroke, sw)
}

private fun DrawScope.drawBishop(w: Float, h: Float, fill: Color, stroke: Color, detail: Color, sw: Float) {
    val bishop = Path().apply {
        moveTo(w * 0.31f, h * 0.77f)
        quadraticTo(w * 0.36f, h * 0.58f, w * 0.38f, h * 0.46f)
        quadraticTo(w * 0.33f, h * 0.35f, w * 0.50f, h * 0.22f)
        quadraticTo(w * 0.67f, h * 0.35f, w * 0.62f, h * 0.46f)
        quadraticTo(w * 0.64f, h * 0.58f, w * 0.69f, h * 0.77f)
        close()
    }
    drawPath(bishop, color = fill, style = Fill)
    drawPath(bishop, color = stroke, style = Stroke(width = sw, join = StrokeJoin.Round))
    // Mitre finial + slash
    drawCircle(color = fill, radius = w * 0.055f, center = Offset(w * 0.50f, h * 0.17f))
    drawCircle(color = stroke, radius = w * 0.055f, center = Offset(w * 0.50f, h * 0.17f), style = Stroke(width = sw))
    drawLine(detail, Offset(w * 0.44f, h * 0.33f), Offset(w * 0.56f, h * 0.42f), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(detail, Offset(w * 0.39f, h * 0.50f), Offset(w * 0.61f, h * 0.50f), strokeWidth = sw * 0.8f)
    drawBase(w, h, fill, stroke, sw)
}

private fun DrawScope.drawQueen(w: Float, h: Float, fill: Color, stroke: Color, detail: Color, sw: Float) {
    val crown = Path().apply {
        moveTo(w * 0.30f, h * 0.77f)
        lineTo(w * 0.20f, h * 0.33f)
        lineTo(w * 0.35f, h * 0.48f)
        lineTo(w * 0.40f, h * 0.24f)
        lineTo(w * 0.50f, h * 0.46f)
        lineTo(w * 0.60f, h * 0.24f)
        lineTo(w * 0.65f, h * 0.48f)
        lineTo(w * 0.80f, h * 0.33f)
        lineTo(w * 0.70f, h * 0.77f)
        close()
    }
    drawPath(crown, color = fill, style = Fill)
    drawPath(crown, color = stroke, style = Stroke(width = sw, join = StrokeJoin.Round))
    // Crown pearls
    val points = listOf(
        Offset(w * 0.20f, h * 0.30f),
        Offset(w * 0.40f, h * 0.21f),
        Offset(w * 0.60f, h * 0.21f),
        Offset(w * 0.80f, h * 0.30f)
    )
    for (pt in points) {
        drawCircle(color = fill, radius = w * 0.042f, center = pt)
        drawCircle(color = stroke, radius = w * 0.042f, center = pt, style = Stroke(width = sw * 0.8f))
    }
    drawLine(detail, Offset(w * 0.33f, h * 0.64f), Offset(w * 0.67f, h * 0.64f), strokeWidth = sw * 0.85f)
    drawBase(w, h, fill, stroke, sw)
}

private fun DrawScope.drawKing(w: Float, h: Float, fill: Color, stroke: Color, detail: Color, sw: Float) {
    // Cross on top
    drawLine(stroke, Offset(w * 0.50f, h * 0.12f), Offset(w * 0.50f, h * 0.28f), strokeWidth = sw * 1.25f, cap = StrokeCap.Round)
    drawLine(stroke, Offset(w * 0.42f, h * 0.18f), Offset(w * 0.58f, h * 0.18f), strokeWidth = sw * 1.25f, cap = StrokeCap.Round)

    val king = Path().apply {
        moveTo(w * 0.30f, h * 0.77f)
        quadraticTo(w * 0.20f, h * 0.48f, w * 0.28f, h * 0.33f)
        quadraticTo(w * 0.38f, h * 0.26f, w * 0.50f, h * 0.34f)
        quadraticTo(w * 0.62f, h * 0.26f, w * 0.72f, h * 0.33f)
        quadraticTo(w * 0.80f, h * 0.48f, w * 0.70f, h * 0.77f)
        close()
    }
    drawPath(king, color = fill, style = Fill)
    drawPath(king, color = stroke, style = Stroke(width = sw, join = StrokeJoin.Round))
    drawArc(
        color = detail,
        startAngle = 190f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Rect(w * 0.33f, h * 0.54f, w * 0.67f, h * 0.72f).topLeft,
        size = Rect(w * 0.33f, h * 0.54f, w * 0.67f, h * 0.72f).size,
        style = Stroke(width = sw * 0.85f)
    )
    drawBase(w, h, fill, stroke, sw)
}
