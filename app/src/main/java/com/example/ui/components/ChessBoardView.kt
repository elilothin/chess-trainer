package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.chess.ChessEngine
import com.example.domain.model.BoardState
import com.example.domain.model.ChessMove
import com.example.domain.model.ChessPiece
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType
import com.example.domain.model.Square
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChessBoardView(
    board: BoardState,
    modifier: Modifier = Modifier,
    selectedSquare: Square? = null,
    legalTargets: Set<Square> = emptySet(),
    lastMove: ChessMove? = null,
    hintArrow: Pair<Square, Square>? = null,
    highlightedSquares: Set<Square> = emptySet(),
    flipped: Boolean = false,
    showCoordinates: Boolean = true,
    onSquareClick: (Square) -> Unit = {}
) {
    val palette = LocalGlassPalette.current
    val inCheckWhite = ChessEngine.isInCheck(board, PieceColor.WHITE)
    val inCheckBlack = ChessEngine.isInCheck(board, PieceColor.BLACK)
    val checkedKingSq = when {
        inCheckWhite && board.turn == PieceColor.WHITE -> ChessEngine.findKing(board, PieceColor.WHITE)
        inCheckBlack && board.turn == PieceColor.BLACK -> ChessEngine.findKing(board, PieceColor.BLACK)
        else -> null
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("chessboard_grid")
            .clip(RoundedCornerShape(18.dp))
            .background(palette.glassSurface)
            .border(1.5.dp, palette.boardBorder, RoundedCornerShape(18.dp))
            .padding(4.dp)
    ) {
        val boardPx = constraints.maxWidth.toFloat()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (displayRow in 0..7) {
                    val boardRow = if (flipped) 7 - displayRow else displayRow
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        for (displayCol in 0..7) {
                            val boardCol = if (flipped) 7 - displayCol else displayCol
                            val sq = Square(boardRow, boardCol)
                            val isLight = (boardRow + boardCol) % 2 == 0
                            val baseColor = if (isLight) palette.boardLightSquare else palette.boardDarkSquare

                            val isSelected = sq == selectedSquare
                            val isLastMove = lastMove != null && (sq == lastMove.from || sq == lastMove.to)
                            val isLegalTarget = sq in legalTargets
                            val isHighlighted = sq in highlightedSquares
                            val isCheckedKing = sq == checkedKingSq
                            val piece = board.pieceAt(sq)

                            val coordLabel = sq.toAlgebraic()
                            val rankLabel = if (displayCol == 0 && showCoordinates) "${8 - boardRow}" else null
                            val fileLabel = if (displayRow == 7 && showCoordinates) "${'a' + boardCol}" else null

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(baseColor)
                                    .let { m ->
                                        when {
                                            isSelected -> m.background(palette.highlightSelected)
                                            isHighlighted -> m.background(palette.highlightHint.copy(alpha = 0.45f))
                                            isLastMove -> m.background(palette.highlightLastMove)
                                            else -> m
                                        }
                                    }
                                    .testTag("chess_square_$coordLabel")
                                    .semantics {
                                        contentDescription = if (piece != null) {
                                            "${piece.type.frenchName} $coordLabel"
                                        } else {
                                            coordLabel
                                        }
                                    }
                                    .clickable { onSquareClick(sq) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCheckedKing) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawRect(
                                            brush = Brush.radialGradient(
                                                colors = listOf(Color(0xCCEF4444), Color.Transparent),
                                                radius = size.minDimension * 0.65f
                                            )
                                        )
                                    }
                                }

                                if (isHighlighted && piece == null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(2.dp, palette.success)
                                    )
                                }

                                if (piece != null) {
                                    VectorChessPiece(
                                        piece = piece,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(3.dp)
                                    )
                                }

                                if (isLegalTarget) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        if (piece == null) {
                                            drawCircle(
                                                color = palette.accentCyan.copy(alpha = 0.72f),
                                                radius = size.minDimension * 0.16f
                                            )
                                        } else {
                                            drawCircle(
                                                color = palette.accentMagenta.copy(alpha = 0.85f),
                                                radius = size.minDimension * 0.44f,
                                                style = Stroke(width = size.minDimension * 0.08f)
                                            )
                                        }
                                    }
                                }

                                val coordTextColor = if (isLight) {
                                    palette.boardDarkSquare.copy(alpha = 0.85f)
                                } else {
                                    palette.boardLightSquare.copy(alpha = 0.95f)
                                }

                                if (rankLabel != null) {
                                    Text(
                                        text = rankLabel,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = coordTextColor,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(start = 3.dp, top = 1.dp)
                                    )
                                }
                                if (fileLabel != null) {
                                    Text(
                                        text = fileLabel,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = coordTextColor,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 3.dp, bottom = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hint / Analysis Best Move Arrow Overlay
            if (hintArrow != null && boardPx > 0f) {
                val (fromSq, toSq) = hintArrow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / 8f
                    val cellH = size.height / 8f
                    fun centerOf(sq: Square): Offset {
                        val dc = if (flipped) 7 - sq.col else sq.col
                        val dr = if (flipped) 7 - sq.row else sq.row
                        return Offset((dc + 0.5f) * cellW, (dr + 0.5f) * cellH)
                    }
                    val start = centerOf(fromSq)
                    val end = centerOf(toSq)
                    val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
                    val headLen = cellW * 0.34f
                    val shortenedEnd = Offset(
                        (end.x - cos(angle) * headLen * 0.55f).toFloat(),
                        (end.y - sin(angle) * headLen * 0.55f).toFloat()
                    )
                    val arrowColor = palette.success.copy(alpha = 0.88f)
                    drawLine(
                        color = arrowColor,
                        start = start,
                        end = shortenedEnd,
                        strokeWidth = cellW * 0.16f,
                        cap = StrokeCap.Round
                    )
                    val headPath = Path().apply {
                        moveTo(end.x, end.y)
                        lineTo(
                            (end.x - headLen * cos(angle - Math.PI / 6)).toFloat(),
                            (end.y - headLen * sin(angle - Math.PI / 6)).toFloat()
                        )
                        lineTo(
                            (end.x - headLen * cos(angle + Math.PI / 6)).toFloat(),
                            (end.y - headLen * sin(angle + Math.PI / 6)).toFloat()
                        )
                        close()
                    }
                    drawPath(headPath, color = arrowColor)
                }
            }
        }
    }
}

@Composable
fun PromotionPickerDialog(
    color: PieceColor,
    onSelect: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val choices = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.bgMid,
        titleContentColor = palette.textPrimary,
        title = {
            Text(
                text = tr("Choisir une promotion", "Choose Promotion"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                choices.forEach { pieceType ->
                    GlassCard(
                        onClick = { onSelect(pieceType) },
                        contentPadding = 8.dp,
                        modifier = Modifier.size(64.dp)
                    ) {
                        VectorChessPiece(
                            piece = ChessPiece(pieceType, color),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Annuler", "Cancel"), color = palette.accentCyan)
            }
        }
    )
}
