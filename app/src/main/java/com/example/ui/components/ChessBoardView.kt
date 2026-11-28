package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.example.ui.feedback.LocalGameFeedback
import com.example.ui.theme.LocalBoardTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
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
    val boardTheme = LocalBoardTheme.current
    val lightSquareColor = boardTheme.light ?: palette.boardLightSquare
    val darkSquareColor = boardTheme.dark ?: palette.boardDarkSquare
    val feedback = LocalGameFeedback.current
    val inCheckWhite = ChessEngine.isInCheck(board, PieceColor.WHITE)
    val inCheckBlack = ChessEngine.isInCheck(board, PieceColor.BLACK)
    val checkedKingSq = when {
        inCheckWhite && board.turn == PieceColor.WHITE -> ChessEngine.findKing(board, PieceColor.WHITE)
        inCheckBlack && board.turn == PieceColor.BLACK -> ChessEngine.findKing(board, PieceColor.BLACK)
        else -> null
    }

    // Suivi des pièces (pour les faire glisser) et retour sonore/haptique des coups
    val tracker = remember { SpriteTracker() }
    val frame = remember(board) { tracker.advance(board) }
    val previous = remember { arrayOfNulls<BoardState>(1) }
    LaunchedEffect(board) {
        val old = previous[0]
        previous[0] = board
        if (old != null && old != board) {
            val check = ChessEngine.isInCheck(board, board.turn)
            MoveClassifier.classify(old, board, check)?.let { feedback?.onMove(it) }
        }
    }

    val frameShape = RoundedCornerShape(20.dp)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("chessboard_grid")
            .clip(frameShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(palette.glassSurfaceElevated, palette.glassSurface)
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        palette.textPrimary.copy(alpha = if (palette.isDark) 0.35f else 0.55f),
                        palette.boardBorder.copy(alpha = 0.35f)
                    )
                ),
                frameShape
            )
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val density = LocalDensity.current
                val cellPx = with(density) { maxWidth.toPx() } / 8f
                val cellDp = maxWidth / 8

                // Couche 1 : cases, surbrillances, coordonnées (c'est elle qui reçoit les clics)
                Column(modifier = Modifier.fillMaxSize()) {
                    for (displayRow in 0..7) {
                        val boardRow = if (flipped) 7 - displayRow else displayRow
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (displayCol in 0..7) {
                                val boardCol = if (flipped) 7 - displayCol else displayCol
                                val sq = Square(boardRow, boardCol)
                                val isLight = (boardRow + boardCol) % 2 == 0
                                val baseColor = if (isLight) lightSquareColor else darkSquareColor

                                val isSelected = sq == selectedSquare
                                val isLastMove = lastMove != null && (sq == lastMove.from || sq == lastMove.to)
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
                                                    colors = listOf(Color(0xE6EF4444), Color.Transparent),
                                                    radius = size.minDimension * 0.7f
                                                )
                                            )
                                        }
                                    }
                                    if (isHighlighted && piece == null) {
                                        Box(modifier = Modifier.fillMaxSize().border(2.dp, palette.success))
                                    }

                                    val coordTextColor = if (isLight) {
                                        darkSquareColor.copy(alpha = 0.85f)
                                    } else {
                                        lightSquareColor.copy(alpha = 0.95f)
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

                // Reflet de verre diagonal sur les cases
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Black.copy(alpha = 0.08f)),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        )
                    )
                }

                // Couche 2 : pièces qui glissent (ne captent pas les clics)
                frame.ghosts.forEach { g ->
                    key("ghost_${g.id}") { GhostPieceView(g, flipped, cellPx, cellDp) }
                }
                frame.sprites.forEach { sp ->
                    key(sp.id) { PieceSpriteView(sp, flipped, cellPx, cellDp, frame.animated) }
                }

                // Couche 3 : repères de coups possibles (au-dessus des pièces)
                if (legalTargets.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        legalTargets.forEach { sq ->
                            val c = if (flipped) 7 - sq.col else sq.col
                            val r = if (flipped) 7 - sq.row else sq.row
                            val center = Offset((c + 0.5f) * cellPx, (r + 0.5f) * cellPx)
                            if (board.pieceAt(sq) == null) {
                                drawCircle(palette.accentCyan.copy(alpha = 0.78f), radius = cellPx * 0.16f, center = center)
                            } else {
                                drawCircle(
                                    color = palette.accentMagenta.copy(alpha = 0.85f),
                                    radius = cellPx * 0.45f,
                                    center = center,
                                    style = Stroke(width = cellPx * 0.07f)
                                )
                            }
                        }
                    }
                }

                // Flèche d'indice / meilleur coup
                if (hintArrow != null && cellPx > 0f) {
                    val (fromSq, toSq) = hintArrow
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        fun centerOf(sq: Square): Offset {
                            val dc = if (flipped) 7 - sq.col else sq.col
                            val dr = if (flipped) 7 - sq.row else sq.row
                            return Offset((dc + 0.5f) * cellPx, (dr + 0.5f) * cellPx)
                        }
                        val start = centerOf(fromSq)
                        val end = centerOf(toSq)
                        val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
                        val headLen = cellPx * 0.34f
                        val shortenedEnd = Offset(
                            (end.x - cos(angle) * headLen * 0.55f).toFloat(),
                            (end.y - sin(angle) * headLen * 0.55f).toFloat()
                        )
                        val arrowColor = palette.success.copy(alpha = 0.88f)
                        drawLine(arrowColor, start, shortenedEnd, strokeWidth = cellPx * 0.16f, cap = StrokeCap.Round)
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
}

/** Une pièce qui glisse vers sa case avec un petit soulèvement pendant le déplacement. */
@Composable
private fun PieceSpriteView(sprite: Sprite, flipped: Boolean, cellPx: Float, cellDp: Dp, animated: Boolean) {
    val tx = (if (flipped) 7 - sprite.square.col else sprite.square.col).toFloat()
    val ty = (if (flipped) 7 - sprite.square.row else sprite.square.row).toFloat()
    val target = Offset(tx, ty)
    val pos = remember { Animatable(target, Offset.VectorConverter) }
    val lift = remember { Animatable(0f) }

    LaunchedEffect(tx, ty) {
        if (pos.value != target) {
            if (animated) {
                launch {
                    lift.animateTo(1f, tween(120))
                    lift.animateTo(0f, tween(220))
                }
                pos.animateTo(target, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow))
            } else {
                pos.snapTo(target)
            }
        }
    }

    Box(
        modifier = Modifier
            .zIndex(if (pos.isRunning) 2f else 1f)
            .offset { IntOffset((pos.value.x * cellPx).roundToInt(), (pos.value.y * cellPx).roundToInt()) }
            .size(cellDp)
            .graphicsLayer {
                val k = 1f + 0.16f * lift.value
                scaleX = k
                scaleY = k
            }
            .padding(1.dp)
    ) {
        VectorChessPiece(piece = sprite.piece, modifier = Modifier.fillMaxSize())
    }
}

/** Pièce capturée : elle s'efface juste après l'arrivée de l'attaquante. */
@Composable
private fun GhostPieceView(sprite: Sprite, flipped: Boolean, cellPx: Float, cellDp: Dp) {
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(sprite.id) {
        delay(110)
        alpha.animateTo(0f, tween(170))
    }
    val c = if (flipped) 7 - sprite.square.col else sprite.square.col
    val r = if (flipped) 7 - sprite.square.row else sprite.square.row
    Box(
        modifier = Modifier
            .offset { IntOffset((c * cellPx).roundToInt(), (r * cellPx).roundToInt()) }
            .size(cellDp)
            .graphicsLayer {
                this.alpha = alpha.value
                val k = 0.85f + 0.15f * alpha.value
                scaleX = k
                scaleY = k
            }
            .padding(1.dp)
    ) {
        VectorChessPiece(piece = sprite.piece, modifier = Modifier.fillMaxSize())
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
