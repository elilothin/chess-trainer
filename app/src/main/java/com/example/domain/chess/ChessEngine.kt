package com.example.domain.chess

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.domain.model.BoardState
import com.example.domain.model.CastlingRights
import com.example.domain.model.ChessMove
import com.example.domain.model.ChessPiece
import com.example.domain.model.GameOutcome
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType
import com.example.domain.model.Square
import com.github.bhlangonijr.chesslib.Board as LibBoard
import com.github.bhlangonijr.chesslib.CastleRight as LibCastleRight
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType
import com.github.bhlangonijr.chesslib.Side as LibSide
import com.github.bhlangonijr.chesslib.Square as LibSquare
import com.github.bhlangonijr.chesslib.move.Move as LibMove
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

@Immutable
data class ChessEngineUiState(
    val board: BoardState,
    val fen: String,
    val turn: PieceColor,
    val selectedSquare: Square? = null,
    val legalTargets: Set<Square> = emptySet(),
    val legalMoves: List<ChessMove> = emptyList(),
    val lastMove: ChessMove? = null,
    val outcome: GameOutcome = GameOutcome.ONGOING,
    val isCheck: Boolean = false,
    val isGameOver: Boolean = false,
    val evaluationCp: Int = 0,
    val evaluationLabel: String = "0.00",
    val whiteWinAdvantageFraction: Float = 0.5f,
    val moveHistorySan: List<String> = emptyList(),
    val moveHistoryUci: List<String> = emptyList(),
    val pendingPromotion: Pair<Square, Square>? = null,
    val canUndo: Boolean = false
)

/**
 * Moteur d'échecs orienté Compose basé sur la bibliothèque compatible `kchesslib` (`io.github.cvb941:kchesslib`).
 *
 * Gère :
 * - La génération et validation des coups légaux (roque, prise en passant, promotion, échec)
 * - Les règles officielles FIDE (mat, pat, répétition triple, règle des 50 coups, matériel insuffisant)
 * - L'évaluation des positions (centipions, tables pièce-case, paire de fous, sécurité du roi, alpha-bêta)
 * - L'état réactif prêt pour Jetpack Compose (`state: StateFlow` et `composeState: State`)
 */
class ChessEngine(initialFen: String = INITIAL_FEN) {

    private val libBoard: LibBoard = LibBoard().apply {
        loadFromFen(normalizeFen(initialFen))
    }

    private var selectedSquare: Square? = null
    private var pendingPromotion: Pair<Square, Square>? = null
    private val moveStack = mutableListOf<ChessMove>()
    private val sanHistory = mutableListOf<String>()
    private val uciHistory = mutableListOf<String>()
    private var lastEvalCp: Int = evaluateBoardStaticCp(libBoard)

    private val _state = MutableStateFlow(buildUiState())
    val state: StateFlow<ChessEngineUiState> = _state.asStateFlow()

    private val _composeState: MutableState<ChessEngineUiState> = mutableStateOf(_state.value)
    val composeState: State<ChessEngineUiState> = _composeState

    fun currentBoard(): BoardState = toBoardState(libBoard)

    fun currentFen(): String = libBoard.fen

    fun legalMoves(): List<ChessMove> = extractLegalMoves(libBoard)

    fun legalMovesFrom(square: Square): List<ChessMove> {
        val boardState = currentBoard()
        val piece = boardState.pieceAt(square) ?: return emptyList()
        if (piece.color != boardState.turn) return emptyList()
        return legalMoves().filter { it.from == square }
    }

    fun isLegalMove(from: Square, to: Square, promotion: PieceType? = null): Boolean {
        return legalMovesFrom(from).any { m ->
            m.to == to && (promotion == null || m.promotion == promotion)
        }
    }

    fun isInCheck(): Boolean = libBoard.isKingAttacked

    fun isCheckmate(): Boolean = libBoard.isMated

    fun isStalemate(): Boolean = libBoard.isStaleMate

    fun isRepetition(times: Int = 3): Boolean = libBoard.isRepetition(times)

    fun isInsufficientMaterial(): Boolean = libBoard.isInsufficientMaterial

    fun isFiftyMoveRule(): Boolean = libBoard.halfMoveCounter >= 100

    fun evaluateOutcome(): GameOutcome = extractOutcome(libBoard)

    /**
     * Gère directement le tap utilisateur sur une case depuis l'interface Compose :
     * sélection de pièce, surbrillance des coups légaux, déclenchement du dialogue de promotion
     * ou exécution immédiate du coup légal.
     *
     * @return `true` si un coup a été joué sur l'échiquier, `false` sinon.
     */
    fun onSquareTapped(square: Square): Boolean {
        val outcome = evaluateOutcome()
        if (outcome != GameOutcome.ONGOING) return false

        val currentTurn = libBoard.sideToMove.toModelColor()
        val sel = selectedSquare
        if (sel != null) {
            val candidates = legalMovesFrom(sel).filter { it.to == square }
            if (candidates.isNotEmpty()) {
                if (candidates.any { it.promotion != null }) {
                    pendingPromotion = sel to square
                    publishState()
                    return false
                }
                return playMove(candidates.first())
            }
        }

        val tappedPiece = currentBoard().pieceAt(square)
        if (tappedPiece != null && tappedPiece.color == currentTurn) {
            selectedSquare = square
            pendingPromotion = null
        } else {
            selectedSquare = null
            pendingPromotion = null
        }
        publishState()
        return false
    }

    fun selectPromotion(pieceType: PieceType): Boolean {
        val pending = pendingPromotion ?: return false
        val move = legalMovesFrom(pending.first)
            .firstOrNull { it.to == pending.second && it.promotion == pieceType }
            ?: return false
        pendingPromotion = null
        return playMove(move)
    }

    fun dismissPromotion() {
        pendingPromotion = null
        publishState()
    }

    fun playMove(move: ChessMove): Boolean {
        val beforeBoard = currentBoard()
        val libMove = move.toLibMove(libBoard.sideToMove)
        if (!libBoard.doMove(libMove, true)) return false

        val san = toStandardSan(beforeBoard, move)
        moveStack.add(move)
        sanHistory.add(san)
        uciHistory.add(move.toUci())
        selectedSquare = null
        pendingPromotion = null
        lastEvalCp = evaluateBoardStaticCp(libBoard)
        publishState()
        return true
    }

    fun playUci(uci: String): Boolean {
        val move = moveFromUci(currentBoard(), uci) ?: return false
        return playMove(move)
    }

    fun playSan(san: String): Boolean {
        val move = parseSanMove(currentBoard(), san) ?: return false
        return playMove(move)
    }

    fun undoMove(): Boolean {
        val undone = libBoard.undoMove() ?: return false
        if (moveStack.isNotEmpty()) moveStack.removeAt(moveStack.lastIndex)
        if (sanHistory.isNotEmpty()) sanHistory.removeAt(sanHistory.lastIndex)
        if (uciHistory.isNotEmpty()) uciHistory.removeAt(uciHistory.lastIndex)
        selectedSquare = null
        pendingPromotion = null
        lastEvalCp = evaluateBoardStaticCp(libBoard)
        publishState()
        return true
    }

    fun loadFen(fen: String) {
        libBoard.loadFromFen(normalizeFen(fen))
        moveStack.clear()
        sanHistory.clear()
        uciHistory.clear()
        selectedSquare = null
        pendingPromotion = null
        lastEvalCp = evaluateBoardStaticCp(libBoard)
        publishState()
    }

    fun reset() {
        loadFen(INITIAL_FEN)
    }

    fun evaluateStaticCp(): Int = evaluateBoardStaticCp(libBoard)

    fun evaluatePositionCp(depth: Int = 2, updateUiState: Boolean = true): Int {
        val clone = libBoard.clone()
        val cp = searchBestWithEval(clone, depth.coerceIn(0, 4)).second
        if (updateUiState) {
            lastEvalCp = cp
            publishState()
        }
        return cp
    }

    fun bestMove(depth: Int = 2): ChessMove? {
        val clone = libBoard.clone()
        return searchBestWithEval(clone, depth.coerceIn(1, 4)).first
    }

    private fun publishState() {
        val next = buildUiState()
        _state.value = next
        _composeState.value = next
    }

    private fun buildUiState(): ChessEngineUiState {
        val board = currentBoard()
        val legal = extractLegalMoves(libBoard)
        val targets = selectedSquare?.let { sel ->
            legal.filter { it.from == sel }.map { it.to }.toSet()
        } ?: emptySet()
        val outcome = extractOutcome(libBoard)
        val cp = when (outcome) {
            GameOutcome.CHECKMATE_WHITE_WINS -> 100_000
            GameOutcome.CHECKMATE_BLACK_WINS -> -100_000
            GameOutcome.STALEMATE,
            GameOutcome.DRAW_INSUFFICIENT_MATERIAL,
            GameOutcome.DRAW_FIFTY_MOVE,
            GameOutcome.DRAW_REPETITION -> 0
            GameOutcome.ONGOING -> lastEvalCp
        }
        val isMateOutcome = outcome == GameOutcome.CHECKMATE_WHITE_WINS ||
            outcome == GameOutcome.CHECKMATE_BLACK_WINS

        return ChessEngineUiState(
            board = board,
            fen = libBoard.fen,
            turn = board.turn,
            selectedSquare = selectedSquare,
            legalTargets = targets,
            legalMoves = legal,
            lastMove = moveStack.lastOrNull(),
            outcome = outcome,
            isCheck = libBoard.isKingAttacked,
            isGameOver = outcome != GameOutcome.ONGOING,
            evaluationCp = cp,
            evaluationLabel = formatEvaluation(cp, isMateOutcome),
            whiteWinAdvantageFraction = winProbabilityFraction(cp),
            moveHistorySan = sanHistory.toList(),
            moveHistoryUci = uciHistory.toList(),
            pendingPromotion = pendingPromotion,
            canUndo = moveStack.isNotEmpty()
        )
    }

    companion object {
        const val INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        private val pawnTable = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
            intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
            intArrayOf(5, 5, 10, 27, 27, 10, 5, 5),
            intArrayOf(0, 0, 0, 24, 24, 0, 0, 0),
            intArrayOf(5, -5, -10, 0, 0, -10, -5, 5),
            intArrayOf(5, 10, 10, -22, -22, 10, 10, 5),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        )

        private val knightTable = arrayOf(
            intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50),
            intArrayOf(-40, -20, 0, 0, 0, 0, -20, -40),
            intArrayOf(-30, 0, 10, 15, 15, 10, 0, -30),
            intArrayOf(-30, 5, 15, 20, 20, 15, 5, -30),
            intArrayOf(-30, 0, 15, 20, 20, 15, 0, -30),
            intArrayOf(-30, 5, 10, 15, 15, 10, 5, -30),
            intArrayOf(-40, -20, 0, 5, 5, 0, -20, -40),
            intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50)
        )

        private val bishopTable = arrayOf(
            intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20),
            intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
            intArrayOf(-10, 0, 5, 10, 10, 5, 0, -10),
            intArrayOf(-10, 5, 5, 10, 10, 5, 5, -10),
            intArrayOf(-10, 0, 10, 10, 10, 10, 0, -10),
            intArrayOf(-10, 10, 10, 10, 10, 10, 10, -10),
            intArrayOf(-10, 5, 0, 0, 0, 0, 5, -10),
            intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20)
        )

        fun initialBoard(): BoardState = fromFen(INITIAL_FEN)

        fun fromFen(fen: String): BoardState {
            val lib = LibBoard().apply { loadFromFen(normalizeFen(fen)) }
            return toBoardState(lib)
        }

        fun toFen(board: BoardState): String {
            val sb = StringBuilder()
            for (r in 0..7) {
                var empty = 0
                for (c in 0..7) {
                    val p = board.grid[r][c]
                    if (p == null) {
                        empty++
                    } else {
                        if (empty > 0) {
                            sb.append(empty)
                            empty = 0
                        }
                        sb.append(p.fenChar())
                    }
                }
                if (empty > 0) sb.append(empty)
                if (r < 7) sb.append('/')
            }
            sb.append(' ').append(board.turn.fenChar())
            sb.append(' ').append(board.castling.toFen())
            sb.append(' ').append(board.enPassantTarget?.toAlgebraic() ?: "-")
            sb.append(' ').append(board.halfMoveClock)
            sb.append(' ').append(board.fullMoveNumber)
            return sb.toString()
        }

        fun legalMoves(board: BoardState): List<ChessMove> {
            val lib = toLibBoard(board)
            return extractLegalMoves(lib)
        }

        fun legalMovesFrom(board: BoardState, from: Square): List<ChessMove> {
            val piece = board.pieceAt(from) ?: return emptyList()
            if (piece.color != board.turn) return emptyList()
            return legalMoves(board).filter { it.from == from }
        }

        fun applyMove(board: BoardState, move: ChessMove): BoardState {
            val lib = toLibBoard(board)
            val libMove = move.toLibMove(lib.sideToMove)
            lib.doMove(libMove, false)
            return toBoardState(lib)
        }

        fun isInCheck(board: BoardState, color: PieceColor): Boolean {
            if (board.turn == color) {
                return toLibBoard(board).isKingAttacked
            }
            val flipped = board.copy(turn = color, enPassantTarget = null)
            return toLibBoard(flipped).isKingAttacked
        }

        fun evaluateOutcome(board: BoardState): GameOutcome {
            val lib = toLibBoard(board)
            return extractOutcome(lib)
        }

        fun evaluatePositionCp(board: BoardState, depth: Int = 2): Int {
            val lib = toLibBoard(board)
            return searchBestWithEval(lib, depth.coerceIn(0, 4)).second
        }

        fun formatEvaluation(cp: Int, isMate: Boolean = false): String {
            if (isMate || abs(cp) >= 90_000) {
                return if (cp >= 0) "+M" else "-M"
            }
            val pawns = cp / 100.0
            return if (cp > 0) "+%.2f".format(pawns) else "%.2f".format(pawns)
        }

        fun winProbabilityFraction(cp: Int): Float {
            if (abs(cp) >= 90_000) return if (cp > 0) 0.96f else 0.04f
            val clamped = cp.coerceIn(-1200, 1200)
            val prob = 1.0 / (1.0 + exp(-clamped / 250.0))
            return prob.toFloat().coerceIn(0.05f, 0.95f)
        }

        fun findKing(board: BoardState, color: PieceColor): Square? {
            for (r in 0..7) {
                for (c in 0..7) {
                    val p = board.grid[r][c]
                    if (p != null && p.color == color && p.type == PieceType.KING) {
                        return Square(r, c)
                    }
                }
            }
            return null
        }

        fun formatMoveLabel(
            move: ChessMove,
            movingPiece: ChessPiece?,
            isCheck: Boolean,
            isCheckmate: Boolean
        ): String {
            if (move.isCastleKingSide) return if (isCheckmate) "O-O#" else if (isCheck) "O-O+" else "O-O"
            if (move.isCastleQueenSide) return if (isCheckmate) "O-O-O#" else if (isCheck) "O-O-O+" else "O-O-O"
            val pieceLetter = when (movingPiece?.type) {
                PieceType.PAWN -> if (move.capturedPiece != null) "${'a' + move.from.col}" else ""
                PieceType.KNIGHT -> "C"
                PieceType.BISHOP -> "F"
                PieceType.ROOK -> "T"
                PieceType.QUEEN -> "D"
                PieceType.KING -> "R"
                null -> ""
            }
            val capture = if (move.capturedPiece != null) "x" else ""
            val promo = move.promotion?.let { "=${it.symbol}" } ?: ""
            val suffix = if (isCheckmate) "#" else if (isCheck) "+" else ""
            return "$pieceLetter$capture${move.to.toAlgebraic()}$promo$suffix"
        }

        fun toStandardSan(board: BoardState, move: ChessMove): String {
            val next = applyMove(board, move)
            val inCheck = isInCheck(next, next.turn)
            val isMate = inCheck && legalMoves(next).isEmpty()
            val suffix = if (isMate) "#" else if (inCheck) "+" else ""
            if (move.isCastleKingSide) return "O-O$suffix"
            if (move.isCastleQueenSide) return "O-O-O$suffix"

            val piece = board.pieceAt(move.from) ?: return move.toUci()
            val capture = if (move.capturedPiece != null) "x" else ""
            val promo = move.promotion?.let { "=${it.symbol}" } ?: ""
            if (piece.type == PieceType.PAWN) {
                val prefix = if (move.capturedPiece != null) "${'a' + move.from.col}" else ""
                return "$prefix$capture${move.to.toAlgebraic()}$promo$suffix"
            }

            val peers = legalMoves(board).filter {
                it.to == move.to && it.from != move.from && board.pieceAt(it.from)?.type == piece.type
            }
            val disambig = when {
                peers.isEmpty() -> ""
                peers.none { it.from.col == move.from.col } -> "${'a' + move.from.col}"
                peers.none { it.from.row == move.from.row } -> "${8 - move.from.row}"
                else -> move.from.toAlgebraic()
            }
            return "${piece.type.symbol}$disambig$capture${move.to.toAlgebraic()}$promo$suffix"
        }

        fun moveFromUci(board: BoardState, uci: String): ChessMove? {
            val clean = uci.trim().lowercase()
            if (clean.length < 4) return null
            val from = Square.fromAlgebraic(clean.substring(0, 2)) ?: return null
            val to = Square.fromAlgebraic(clean.substring(2, 4)) ?: return null
            val promo = clean.getOrNull(4)?.let { PieceType.fromChar(it) }
            return legalMovesFrom(board, from).firstOrNull { move ->
                move.to == to && (promo == null || move.promotion == promo)
            }
        }

        fun parseSanMove(board: BoardState, rawSan: String): ChessMove? {
            var san = rawSan.trim()
                .replace("+", "")
                .replace("#", "")
                .replace("!", "")
                .replace("?", "")
            if (san.isEmpty()) return null
            if (san == "O-O" || san == "0-0") {
                return legalMoves(board).firstOrNull { it.isCastleKingSide }
            }
            if (san == "O-O-O" || san == "0-0-0") {
                return legalMoves(board).firstOrNull { it.isCastleQueenSide }
            }

            var promotion: PieceType? = null
            val eqIdx = san.indexOf('=')
            if (eqIdx != -1 && eqIdx + 1 < san.length) {
                promotion = PieceType.fromChar(san[eqIdx + 1])
                san = san.substring(0, eqIdx)
            } else if (san.length >= 3 && san.last().uppercaseChar() in listOf('Q', 'R', 'B', 'N') && san[san.length - 2].isDigit()) {
                promotion = PieceType.fromChar(san.last())
                san = san.dropLast(1)
            }

            val pieceType = if (san[0].isUpperCase() && san[0] in listOf('K', 'Q', 'R', 'B', 'N')) {
                val pt = PieceType.fromChar(san[0])!!
                san = san.substring(1)
                pt
            } else {
                PieceType.PAWN
            }

            san = san.replace("x", "")
            if (san.length < 2) return null
            val targetStr = san.takeLast(2)
            val targetSq = Square.fromAlgebraic(targetStr) ?: return null
            val disambig = san.dropLast(2)

            var reqCol: Int? = null
            var reqRow: Int? = null
            for (ch in disambig) {
                if (ch in 'a'..'h') reqCol = ch - 'a'
                else if (ch in '1'..'8') reqRow = 8 - (ch - '0')
            }

            val candidates = legalMoves(board).filter { move ->
                val p = board.pieceAt(move.from) ?: return@filter false
                p.type == pieceType &&
                    move.to == targetSq &&
                    (promotion == null || move.promotion == promotion) &&
                    (reqCol == null || move.from.col == reqCol) &&
                    (reqRow == null || move.from.row == reqRow)
            }
            return candidates.firstOrNull()
        }

        // --- Internal kchesslib adapters & evaluation engine ---

        private fun normalizeFen(fen: String): String {
            val parts = fen.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (parts.isEmpty()) return INITIAL_FEN
            val boardPart = parts[0]
            val turnPart = parts.getOrNull(1) ?: "w"
            val castlePart = parts.getOrNull(2) ?: "-"
            val epPart = parts.getOrNull(3) ?: "-"
            val halfPart = parts.getOrNull(4)?.toIntOrNull()?.toString() ?: "0"
            val fullPart = parts.getOrNull(5)?.toIntOrNull()?.coerceAtLeast(1)?.toString() ?: "1"
            return "$boardPart $turnPart $castlePart $epPart $halfPart $fullPart"
        }

        private fun toLibBoard(board: BoardState): LibBoard {
            return LibBoard().apply { loadFromFen(toFen(board)) }
        }

        private fun toBoardState(lib: LibBoard): BoardState {
            val grid = List(8) { r ->
                List(8) { c ->
                    val libSq = Square(r, c).toLibSquare()
                    lib.getPiece(libSq).toModelPiece()
                }
            }
            val wCastle = lib.getCastleRight(LibSide.WHITE)
            val bCastle = lib.getCastleRight(LibSide.BLACK)
            val castling = CastlingRights(
                whiteKingSide = wCastle == LibCastleRight.KING_SIDE || wCastle == LibCastleRight.KING_AND_QUEEN_SIDE,
                whiteQueenSide = wCastle == LibCastleRight.QUEEN_SIDE || wCastle == LibCastleRight.KING_AND_QUEEN_SIDE,
                blackKingSide = bCastle == LibCastleRight.KING_SIDE || bCastle == LibCastleRight.KING_AND_QUEEN_SIDE,
                blackQueenSide = bCastle == LibCastleRight.QUEEN_SIDE || bCastle == LibCastleRight.KING_AND_QUEEN_SIDE
            )
            val ep = lib.enPassant.takeIf { it != LibSquare.NONE }?.toModelSquare()
            return BoardState(
                grid = grid,
                turn = lib.sideToMove.toModelColor(),
                castling = castling,
                enPassantTarget = ep,
                halfMoveClock = lib.halfMoveCounter,
                fullMoveNumber = lib.moveCounter.coerceAtLeast(1)
            )
        }

        private fun extractLegalMoves(lib: LibBoard): List<ChessMove> {
            return lib.legalMoves().map { m -> libMoveToChessMove(lib, m) }
        }

        private fun libMoveToChessMove(lib: LibBoard, m: LibMove): ChessMove {
            val from = m.from.toModelSquare()
            val to = m.to.toModelSquare()
            val moving = lib.getPiece(m.from)
            val promo = m.promotion.takeIf { it != LibPiece.NONE }?.pieceType?.toModelPieceType()
            val isCastleKing = moving.pieceType == LibPieceType.KING && (to.col - from.col) == 2
            val isCastleQueen = moving.pieceType == LibPieceType.KING && (from.col - to.col) == 2
            val targetPiece = lib.getPiece(m.to)
            val isEnPassant = moving.pieceType == LibPieceType.PAWN &&
                from.col != to.col &&
                targetPiece == LibPiece.NONE
            val captured = if (isEnPassant) {
                val victimSq = Square(from.row, to.col).toLibSquare()
                lib.getPiece(victimSq).toModelPiece()
            } else {
                targetPiece.toModelPiece()
            }
            return ChessMove(
                from = from,
                to = to,
                promotion = promo,
                isCastleKingSide = isCastleKing,
                isCastleQueenSide = isCastleQueen,
                isEnPassant = isEnPassant,
                capturedPiece = captured
            )
        }

        private fun extractOutcome(lib: LibBoard): GameOutcome {
            if (lib.isMated) {
                return if (lib.sideToMove == LibSide.WHITE) {
                    GameOutcome.CHECKMATE_BLACK_WINS
                } else {
                    GameOutcome.CHECKMATE_WHITE_WINS
                }
            }
            if (lib.isStaleMate) return GameOutcome.STALEMATE
            if (lib.halfMoveCounter >= 100) return GameOutcome.DRAW_FIFTY_MOVE
            if (lib.isInsufficientMaterial) return GameOutcome.DRAW_INSUFFICIENT_MATERIAL
            if (lib.isRepetition(3)) return GameOutcome.DRAW_REPETITION
            return GameOutcome.ONGOING
        }

        private fun searchBestWithEval(lib: LibBoard, depth: Int): Pair<ChessMove?, Int> {
            val legal = lib.legalMoves()
            if (legal.isEmpty()) {
                val score = if (lib.isKingAttacked) {
                    if (lib.sideToMove == LibSide.WHITE) -100_000 else 100_000
                } else 0
                return null to score
            }
            if (depth <= 0) {
                return libMoveToChessMove(lib, legal.first()) to evaluateBoardStaticCp(lib)
            }

            val ordered = orderLibMoves(lib, legal)
            val maximizing = lib.sideToMove == LibSide.WHITE
            var bestScore = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE
            var bestLibMove = ordered.first()

            for (m in ordered) {
                val snapshotMove = libMoveToChessMove(lib, m)
                lib.doMove(m, false)
                val score = alphaBeta(lib, depth - 1, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, !maximizing)
                lib.undoMove()

                if (maximizing) {
                    if (score > bestScore) {
                        bestScore = score
                        bestLibMove = m
                        val _unused = snapshotMove
                    }
                } else {
                    if (score < bestScore) {
                        bestScore = score
                        bestLibMove = m
                    }
                }
            }
            return libMoveToChessMove(lib, bestLibMove) to bestScore
        }

        private fun alphaBeta(
            lib: LibBoard,
            depth: Int,
            alphaInit: Int,
            betaInit: Int,
            maximizing: Boolean
        ): Int {
            val legal = lib.legalMoves()
            if (legal.isEmpty()) {
                return if (lib.isKingAttacked) {
                    if (lib.sideToMove == LibSide.WHITE) -100_000 - depth else 100_000 + depth
                } else 0
            }
            if (lib.halfMoveCounter >= 100 || lib.isInsufficientMaterial || lib.isRepetition(3)) {
                return 0
            }
            if (depth <= 0) {
                return evaluateBoardStaticCp(lib)
            }

            var alpha = alphaInit
            var beta = betaInit
            val ordered = orderLibMoves(lib, legal)

            if (maximizing) {
                var maxEval = Int.MIN_VALUE + 1
                for (m in ordered) {
                    lib.doMove(m, false)
                    val score = alphaBeta(lib, depth - 1, alpha, beta, false)
                    lib.undoMove()
                    if (score > maxEval) maxEval = score
                    if (score > alpha) alpha = score
                    if (beta <= alpha) break
                }
                return maxEval
            } else {
                var minEval = Int.MAX_VALUE - 1
                for (m in ordered) {
                    lib.doMove(m, false)
                    val score = alphaBeta(lib, depth - 1, alpha, beta, true)
                    lib.undoMove()
                    if (score < minEval) minEval = score
                    if (score < beta) beta = score
                    if (beta <= alpha) break
                }
                return minEval
            }
        }

        private fun orderLibMoves(lib: LibBoard, moves: List<LibMove>): List<LibMove> {
            return moves.sortedByDescending { m ->
                var score = 0
                val victim = lib.getPiece(m.to)
                if (victim != LibPiece.NONE) {
                    score += 1000 + (victim.pieceType?.toModelPieceType()?.value ?: 0)
                }
                if (m.promotion != LibPiece.NONE) {
                    score += 800 + (m.promotion.pieceType?.toModelPieceType()?.value ?: 0)
                }
                val moving = lib.getPiece(m.from)
                if (moving.pieceType == LibPieceType.KING && abs(m.to.file.ordinal - m.from.file.ordinal) == 2) {
                    score += 120
                }
                score
            }
        }

        private fun evaluateBoardStaticCp(lib: LibBoard): Int {
            if (lib.isMated) {
                return if (lib.sideToMove == LibSide.WHITE) -100_000 else 100_000
            }
            if (lib.isStaleMate || lib.isInsufficientMaterial || lib.halfMoveCounter >= 100) {
                return 0
            }

            var score = 0
            var whiteBishops = 0
            var blackBishops = 0

            for (r in 0..7) {
                for (c in 0..7) {
                    val libSq = Square(r, c).toLibSquare()
                    val piece = lib.getPiece(libSq)
                    if (piece == LibPiece.NONE) continue
                    val type = piece.pieceType?.toModelPieceType() ?: continue
                    val isWhite = piece.pieceSide == LibSide.WHITE
                    val tableRow = if (isWhite) r else 7 - r

                    if (type == PieceType.BISHOP) {
                        if (isWhite) whiteBishops++ else blackBishops++
                    }

                    val posBonus = when (type) {
                        PieceType.PAWN -> pawnTable[tableRow][c]
                        PieceType.KNIGHT -> knightTable[tableRow][c]
                        PieceType.BISHOP -> bishopTable[tableRow][c]
                        PieceType.ROOK -> if (tableRow == 1) 18 else 0
                        PieceType.QUEEN -> if (r in 2..5 && c in 2..5) 12 else 0
                        PieceType.KING -> if (c in listOf(0, 1, 2, 6, 7) && (r == 0 || r == 7)) 18 else -8
                    }
                    val total = type.value + posBonus
                    if (isWhite) score += total else score -= total
                }
            }

            // Bonus paire de fous
            if (whiteBishops >= 2) score += 30
            if (blackBishops >= 2) score -= 30

            // Léger bonus de tempo / sécurité
            if (lib.isKingAttacked) {
                score += if (lib.sideToMove == LibSide.WHITE) -25 else 25
            }
            return score
        }

        private fun Square.toLibSquare(): LibSquare =
            LibSquare.valueOf(toAlgebraic().uppercase())

        private fun LibSquare.toModelSquare(): Square =
            Square.fromAlgebraic(name.lowercase()) ?: Square(0, 0)

        private fun LibSide.toModelColor(): PieceColor =
            if (this == LibSide.WHITE) PieceColor.WHITE else PieceColor.BLACK

        private fun LibPieceType.toModelPieceType(): PieceType? = when (this) {
            LibPieceType.PAWN -> PieceType.PAWN
            LibPieceType.KNIGHT -> PieceType.KNIGHT
            LibPieceType.BISHOP -> PieceType.BISHOP
            LibPieceType.ROOK -> PieceType.ROOK
            LibPieceType.QUEEN -> PieceType.QUEEN
            LibPieceType.KING -> PieceType.KING
            LibPieceType.NONE -> null
        }

        private fun PieceType.toLibPiece(side: LibSide): LibPiece {
            val libType = when (this) {
                PieceType.PAWN -> LibPieceType.PAWN
                PieceType.KNIGHT -> LibPieceType.KNIGHT
                PieceType.BISHOP -> LibPieceType.BISHOP
                PieceType.ROOK -> LibPieceType.ROOK
                PieceType.QUEEN -> LibPieceType.QUEEN
                PieceType.KING -> LibPieceType.KING
            }
            return LibPiece.make(side, libType)
        }

        private fun LibPiece.toModelPiece(): ChessPiece? {
            if (this == LibPiece.NONE) return null
            val type = pieceType?.toModelPieceType() ?: return null
            val side = pieceSide ?: return null
            return ChessPiece(type, side.toModelColor())
        }

        private fun ChessMove.toLibMove(side: LibSide): LibMove {
            val promoPiece = promotion?.toLibPiece(side) ?: LibPiece.NONE
            return LibMove(from.toLibSquare(), to.toLibSquare(), promoPiece)
        }
    }
}

@Composable
fun rememberChessEngine(initialFen: String = ChessEngine.INITIAL_FEN): ChessEngine {
    return remember(initialFen) { ChessEngine(initialFen) }
}
