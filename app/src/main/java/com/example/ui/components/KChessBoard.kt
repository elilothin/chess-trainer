package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.chess.ChessEngine
import com.example.domain.chess.ChessEngineUiState
import com.example.domain.chess.rememberChessEngine
import com.example.domain.model.ChessMove
import com.example.domain.model.GameOutcome
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType
import com.example.domain.model.Square
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.github.bhlangonijr.chesslib.Board as LibBoard
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType
import com.github.bhlangonijr.chesslib.Side as LibSide
import com.github.bhlangonijr.chesslib.Square as LibSquare
import com.github.bhlangonijr.chesslib.move.Move as LibMove

/**
 * Composant UI Compose autonome affichant un échiquier 8x8 interactif
 * piloté par la logique `kchesslib` via [ChessEngine] pour suivre les pièces,
 * surligner les coups légaux, gérer le roque, la prise en passant, l'échec
 * et afficher le sélecteur de promotion.
 */
@Composable
fun KChessBoard(
    engine: ChessEngine = rememberChessEngine(),
    modifier: Modifier = Modifier,
    flipped: Boolean = false,
    showCoordinates: Boolean = true,
    interactive: Boolean = true,
    hintArrow: Pair<Square, Square>? = null,
    highlightedSquares: Set<Square> = emptySet(),
    showStatusHeader: Boolean = true,
    onMovePlayed: (ChessMove, ChessEngineUiState) -> Unit = { _, _ -> }
) {
    val palette = LocalGlassPalette.current
    val state by engine.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kchess_board"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showStatusHeader) {
            KChessBoardStatusStrip(state = state)
        }

        ChessBoardView(
            board = state.board,
            selectedSquare = state.selectedSquare,
            legalTargets = state.legalTargets,
            lastMove = state.lastMove,
            hintArrow = hintArrow,
            highlightedSquares = highlightedSquares,
            flipped = flipped,
            showCoordinates = showCoordinates,
            onSquareClick = { sq ->
                if (!interactive) return@ChessBoardView
                val previousMoveCount = state.moveHistoryUci.size
                if (engine.onSquareTapped(sq)) {
                    val updated = engine.state.value
                    val played = updated.lastMove
                    if (played != null && updated.moveHistoryUci.size > previousMoveCount) {
                        onMovePlayed(played, updated)
                    }
                }
            }
        )
    }

    state.pendingPromotion?.let {
        PromotionPickerDialog(
            color = state.turn,
            onSelect = { promoType ->
                val previousMoveCount = state.moveHistoryUci.size
                if (engine.selectPromotion(promoType)) {
                    val updated = engine.state.value
                    val played = updated.lastMove
                    if (played != null && updated.moveHistoryUci.size > previousMoveCount) {
                        onMovePlayed(played, updated)
                    }
                }
            },
            onDismiss = { engine.dismissPromotion() }
        )
    }
}

/**
 * Surcharge directe acceptant une instance brute `com.github.bhlangonijr.chesslib.Board`
 * de KChessLib : suit automatiquement la position des pièces et les coups légaux
 * générés par `libBoard.legalMoves()`.
 */
@Composable
fun KChessBoard(
    libBoard: LibBoard,
    modifier: Modifier = Modifier,
    flipped: Boolean = false,
    showCoordinates: Boolean = true,
    interactive: Boolean = true,
    hintArrow: Pair<Square, Square>? = null,
    highlightedSquares: Set<Square> = emptySet(),
    showStatusHeader: Boolean = false,
    onBoardChanged: (LibBoard, LibMove) -> Unit = { _, _ -> }
) {
    var boardVersion by remember(libBoard) { mutableIntStateOf(0) }
    var selectedSquare by remember(libBoard) { mutableStateOf<Square?>(null) }
    var lastMove by remember(libBoard) { mutableStateOf<ChessMove?>(null) }
    var pendingPromotion by remember(libBoard) { mutableStateOf<Pair<Square, Square>?>(null) }

    val boardState = remember(libBoard, boardVersion) {
        ChessEngine.fromFen(libBoard.fen)
    }
    val legalLibMoves = remember(libBoard, boardVersion) {
        libBoard.legalMoves()
    }
    val legalTargets = remember(selectedSquare, legalLibMoves) {
        val sel = selectedSquare ?: return@remember emptySet()
        legalLibMoves
            .filter { it.from.name.equals(sel.toAlgebraic(), ignoreCase = true) }
            .mapNotNull { Square.fromAlgebraic(it.to.name.lowercase()) }
            .toSet()
    }

    fun executeLibMove(libMove: LibMove) {
        val from = Square.fromAlgebraic(libMove.from.name.lowercase())
        val to = Square.fromAlgebraic(libMove.to.name.lowercase())
        val promo = libMove.promotion.takeIf { it != LibPiece.NONE }?.pieceType?.let { pt ->
            when (pt) {
                LibPieceType.QUEEN -> PieceType.QUEEN
                LibPieceType.ROOK -> PieceType.ROOK
                LibPieceType.BISHOP -> PieceType.BISHOP
                LibPieceType.KNIGHT -> PieceType.KNIGHT
                else -> null
            }
        }
        if (libBoard.doMove(libMove, true)) {
            if (from != null && to != null) {
                lastMove = ChessMove(from = from, to = to, promotion = promo)
            }
            selectedSquare = null
            pendingPromotion = null
            boardVersion++
            onBoardChanged(libBoard, libMove)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kchess_board_direct"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showStatusHeader) {
            val outcome = ChessEngine.evaluateOutcome(boardState)
            val turnColor = if (libBoard.sideToMove == LibSide.WHITE) PieceColor.WHITE else PieceColor.BLACK
            KChessBoardStatusStrip(
                state = ChessEngineUiState(
                    board = boardState,
                    fen = libBoard.fen,
                    turn = turnColor,
                    selectedSquare = selectedSquare,
                    legalTargets = legalTargets,
                    lastMove = lastMove,
                    outcome = outcome,
                    isCheck = libBoard.isKingAttacked,
                    isGameOver = outcome != GameOutcome.ONGOING
                )
            )
        }

        ChessBoardView(
            board = boardState,
            selectedSquare = selectedSquare,
            legalTargets = legalTargets,
            lastMove = lastMove,
            hintArrow = hintArrow,
            highlightedSquares = highlightedSquares,
            flipped = flipped,
            showCoordinates = showCoordinates,
            onSquareClick = { sq ->
                if (!interactive || libBoard.isMated || libBoard.isDraw) return@ChessBoardView
                val sel = selectedSquare
                if (sel != null) {
                    val candidates = legalLibMoves.filter {
                        it.from.name.equals(sel.toAlgebraic(), ignoreCase = true) &&
                            it.to.name.equals(sq.toAlgebraic(), ignoreCase = true)
                    }
                    if (candidates.isNotEmpty()) {
                        if (candidates.any { it.promotion != LibPiece.NONE }) {
                            pendingPromotion = sel to sq
                        } else {
                            executeLibMove(candidates.first())
                        }
                        return@ChessBoardView
                    }
                }

                val tappedLibSq = LibSquare.valueOf(sq.toAlgebraic().uppercase())
                val tappedPiece = libBoard.getPiece(tappedLibSq)
                if (tappedPiece != LibPiece.NONE && tappedPiece.pieceSide == libBoard.sideToMove) {
                    selectedSquare = sq
                } else {
                    selectedSquare = null
                }
            }
        )
    }

    pendingPromotion?.let { (from, to) ->
        val turnColor = if (libBoard.sideToMove == LibSide.WHITE) PieceColor.WHITE else PieceColor.BLACK
        PromotionPickerDialog(
            color = turnColor,
            onSelect = { promoType ->
                val libType = when (promoType) {
                    PieceType.QUEEN -> LibPieceType.QUEEN
                    PieceType.ROOK -> LibPieceType.ROOK
                    PieceType.BISHOP -> LibPieceType.BISHOP
                    PieceType.KNIGHT -> LibPieceType.KNIGHT
                    PieceType.PAWN -> LibPieceType.PAWN
                    PieceType.KING -> LibPieceType.KING
                }
                val promoPiece = LibPiece.make(libBoard.sideToMove, libType)
                val targetMove = legalLibMoves.firstOrNull {
                    it.from.name.equals(from.toAlgebraic(), ignoreCase = true) &&
                        it.to.name.equals(to.toAlgebraic(), ignoreCase = true) &&
                        it.promotion == promoPiece
                }
                if (targetMove != null) {
                    executeLibMove(targetMove)
                } else {
                    pendingPromotion = null
                }
            },
            onDismiss = { pendingPromotion = null }
        )
    }
}

@Composable
private fun KChessBoardStatusStrip(state: ChessEngineUiState) {
    val palette = LocalGlassPalette.current
    val statusLabel = when (state.outcome) {
        GameOutcome.CHECKMATE_WHITE_WINS -> tr("Échec et mat • Blancs gagnent", "Checkmate • White wins")
        GameOutcome.CHECKMATE_BLACK_WINS -> tr("Échec et mat • Noirs gagnent", "Checkmate • Black wins")
        GameOutcome.STALEMATE -> tr("Pat • Partie nulle", "Stalemate • Draw")
        GameOutcome.DRAW_INSUFFICIENT_MATERIAL -> tr("Matériel insuffisant • Nulle", "Insufficient material • Draw")
        GameOutcome.DRAW_FIFTY_MOVE -> tr("Règle des 50 coups • Nulle", "50-move rule • Draw")
        GameOutcome.DRAW_REPETITION -> tr("Triple répétition • Nulle", "Threefold repetition • Draw")
        GameOutcome.ONGOING -> when {
            state.isCheck -> tr("Échec !", "Check!")
            state.turn == PieceColor.WHITE -> tr("Trait aux Blancs", "White to move")
            else -> tr("Trait aux Noirs", "Black to move")
        }
    }
    val statusColor = when {
        state.isGameOver -> palette.success
        state.isCheck -> palette.error
        else -> palette.accentCyan
    }

    GlassCard(contentPadding = 10.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassBadge(text = statusLabel, color = statusColor)
                if (!state.isGameOver && state.legalMoves.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${state.legalMoves.size} ${tr("coups", "moves")}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                }
            }
            Text(
                text = state.evaluationLabel,
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
        }
    }
}
