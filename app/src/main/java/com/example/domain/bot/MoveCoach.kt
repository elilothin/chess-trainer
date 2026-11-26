package com.example.domain.bot

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.abs
import kotlin.math.exp

enum class Grade { BEST, GOOD, INACCURACY, MISTAKE, BLUNDER }

enum class Cause {
    NONE, ALLOWED_MATE, HUNG_PIECE, MOVED_INTO_CAPTURE, BAD_TRADE, MISSED_MATE,
    MISSED_FREE_PIECE, EARLY_QUEEN, EARLY_KING, THROWN_ADVANTAGE, GENERIC
}

/** Analyse d'un coup du joueur. Les textes sont produits à l'affichage par [CoachVoice]. */
data class MoveNote(
    /** Numéro du demi-coup (1 = premier coup des Blancs). */
    val ply: Int,
    val san: String,
    val uci: String,
    val fenBefore: String,
    val fenAfter: String,
    val bestUci: String?,
    val bestSan: String?,
    val replySan: String?,
    /** Score (cp) du meilleur coup, du point de vue du joueur. */
    val bestScore: Int,
    /** Score (cp) du coup joué, du point de vue du joueur. */
    val playedScore: Int,
    val winLossPct: Double,
    val grade: Grade,
    val cause: Cause,
    /** Pièce concernée par le message : P N B R Q K. */
    val pieceKey: String? = null,
    val victimKey: String? = null,
    val square: String? = null,
    val mateIn: Int = 0
) {
    val cpLoss: Int get() = (bestScore.coerceIn(-1500, 1500) - playedScore.coerceIn(-1500, 1500)).coerceAtLeast(0)
}

object MoveCoach {

    fun winPct(cp: Int): Double {
        val c = cp.coerceIn(-1500, 1500)
        return 50.0 + 50.0 * (2.0 / (1.0 + exp(-0.00368208 * c)) - 1.0)
    }

    /** Précision (0–100) d'un coup à partir de la perte en % de chances de gain (formule type Lichess). */
    fun moveAccuracy(winLossPct: Double): Double =
        (103.1668 * exp(-0.04354 * winLossPct) - 3.1669).coerceIn(0.0, 100.0)

    fun uciOf(m: Move): String {
        val promo = if (m.promotion != Piece.NONE) (m.promotion.pieceType?.sanSymbol ?: "").lowercase() else ""
        return m.from.value().lowercase() + m.to.value().lowercase() + promo
    }

    private fun keyOf(type: PieceType?): String = when (type) {
        PieceType.PAWN -> "P"
        PieceType.KNIGHT -> "N"
        PieceType.BISHOP -> "B"
        PieceType.ROOK -> "R"
        PieceType.QUEEN -> "Q"
        PieceType.KING -> "K"
        else -> "P"
    }

    private fun valueOf(piece: Piece): Int = when (piece.pieceType) {
        PieceType.PAWN -> 100
        PieceType.KNIGHT -> 320
        PieceType.BISHOP -> 330
        PieceType.ROOK -> 500
        PieceType.QUEEN -> 900
        else -> 0
    }

    /** Notation algébrique standard (SAN) d'un coup légal sur [board]. */
    fun sanOf(board: Board, move: Move): String {
        val piece = board.getPiece(move.from)
        val type = piece.pieceType ?: PieceType.NONE
        val target = board.getPiece(move.to)
        val isCapture = target != Piece.NONE || (type == PieceType.PAWN && move.from.file != move.to.file)
        val sb = StringBuilder()
        val fileDist = move.to.file.ordinal - move.from.file.ordinal
        if (type == PieceType.KING && abs(fileDist) == 2) {
            sb.append(if (fileDist > 0) "O-O" else "O-O-O")
        } else {
            if (type == PieceType.PAWN) {
                if (isCapture) sb.append(move.from.value().lowercase()[0])
            } else {
                sb.append(type.sanSymbol)
                val rivals = board.legalMoves().filter {
                    it != move && it.to == move.to && board.getPiece(it.from) == piece
                }
                if (rivals.isNotEmpty()) {
                    val sameFile = rivals.any { it.from.file == move.from.file }
                    val sameRank = rivals.any { it.from.rank == move.from.rank }
                    val f = move.from.value().lowercase()[0]
                    val r = move.from.value()[1]
                    when {
                        !sameFile -> sb.append(f)
                        !sameRank -> sb.append(r)
                        else -> sb.append(f).append(r)
                    }
                }
            }
            if (isCapture) sb.append('x')
            sb.append(move.to.value().lowercase())
            if (move.promotion != Piece.NONE) sb.append('=').append(move.promotion.pieceType?.sanSymbol ?: "")
        }
        board.doMove(move, false)
        if (board.isMated) sb.append('#') else if (board.isKingAttacked) sb.append('+')
        board.undoMove()
        return sb.toString()
    }

    private fun findMove(board: Board, uci: String): Move? = board.legalMoves().firstOrNull { uciOf(it) == uci }

    /**
     * Analyse le coup [uci] joué depuis [fenBefore] : meilleur coup, perte, classement et cause.
     * Coûteux (deux recherches) : à lancer hors du thread UI.
     */
    fun analyzeMove(fenBefore: String, uci: String, ply: Int, depth: Int = 3, timeMs: Long = 700): MoveNote? {
        val board = Board()
        board.loadFromFen(fenBefore)
        val played = findMove(board, uci) ?: return null
        val mover = board.sideToMove
        val movedPiece = board.getPiece(played.from)
        val capturedPiece = board.getPiece(played.to)

        val sanPlayed = sanOf(board, played)
        val best = Searcher(true).search(board, depth, timeMs)
        val bestMove = best.best
        val bestScore = best.score
        val bestSan = bestMove?.let { sanOf(board, it) }
        val bestUci = bestMove?.let { uciOf(it) }
        val bestIsCapture = bestMove != null && board.getPiece(bestMove.to) != Piece.NONE
        val bestVictim = bestMove?.let { board.getPiece(it.to) } ?: Piece.NONE

        val whiteBeforeSign = if (mover == Side.WHITE) 1 else -1
        val staticBefore = Evaluator.evaluate(board) * whiteBeforeSign

        // Position après le coup joué
        board.doMove(played, false)
        val fenAfter = board.fen
        var playedScore = bestScore
        var reply: Move? = null
        var replySan: String? = null
        var replyCapturedPiece = Piece.NONE
        if (bestMove == null || uciOf(bestMove) != uci) {
            val r = Searcher(true).search(board, (depth - 1).coerceAtLeast(1), timeMs)
            playedScore = if (board.legalMoves().isEmpty()) {
                if (board.isKingAttacked) MATE_SCORE else 0
            } else {
                -r.score
            }
            reply = r.best
            if (reply != null) {
                replySan = sanOf(board, reply)
                replyCapturedPiece = board.getPiece(reply.to)
            }
        }
        board.undoMove()

        val wl = (winPct(bestScore) - winPct(playedScore)).coerceAtLeast(0.0)
        val grade = when {
            wl <= 2.0 -> Grade.BEST
            wl <= 5.0 -> Grade.GOOD
            wl <= 10.0 -> Grade.INACCURACY
            wl <= 20.0 -> Grade.MISTAKE
            else -> Grade.BLUNDER
        }

        var cause = Cause.NONE
        var pieceKey: String? = null
        var victimKey: String? = null
        var square: String? = null
        var mateIn = 0

        if (grade >= Grade.INACCURACY) {
            val movedType = movedPiece.pieceType ?: PieceType.NONE
            val toSq = played.to.value().lowercase()
            when {
                playedScore <= -MATE_THRESHOLD -> {
                    cause = Cause.ALLOWED_MATE
                    mateIn = ((MATE_SCORE + playedScore) + 1) / 2
                }
                reply != null && replyCapturedPiece != Piece.NONE && valueOf(replyCapturedPiece) >= 300 -> {
                    val victimType = replyCapturedPiece.pieceType
                    pieceKey = keyOf(victimType)
                    square = reply.to.value().lowercase()
                    if (reply.to == played.to) {
                        if (capturedPiece != Piece.NONE) {
                            cause = Cause.BAD_TRADE
                            pieceKey = keyOf(movedType)
                            victimKey = keyOf(capturedPiece.pieceType)
                        } else {
                            cause = Cause.MOVED_INTO_CAPTURE
                            pieceKey = keyOf(movedType)
                        }
                    } else {
                        cause = Cause.HUNG_PIECE
                    }
                }
                bestScore >= MATE_THRESHOLD && playedScore < MATE_THRESHOLD -> {
                    cause = Cause.MISSED_MATE
                    mateIn = ((MATE_SCORE - bestScore) + 1) / 2
                }
                bestIsCapture && valueOf(bestVictim) >= 300 && bestScore - playedScore >= 150 -> {
                    cause = Cause.MISSED_FREE_PIECE
                    victimKey = keyOf(bestVictim.pieceType)
                    square = bestMove!!.to.value().lowercase()
                }
                movedType == PieceType.QUEEN && board.moveCounter <= 8 && capturedPiece == Piece.NONE -> {
                    cause = Cause.EARLY_QUEEN
                    square = toSq
                }
                movedType == PieceType.KING && abs(played.to.file.ordinal - played.from.file.ordinal) != 2 &&
                    board.moveCounter <= 12 && capturedPiece == Piece.NONE -> {
                    cause = Cause.EARLY_KING
                }
                staticBefore >= 250 && playedScore <= 60 -> cause = Cause.THROWN_ADVANTAGE
                else -> cause = Cause.GENERIC
            }
        }

        return MoveNote(
            ply = ply, san = sanPlayed, uci = uci, fenBefore = fenBefore, fenAfter = fenAfter,
            bestUci = bestUci, bestSan = bestSan, replySan = replySan,
            bestScore = bestScore, playedScore = playedScore, winLossPct = wl,
            grade = grade, cause = cause, pieceKey = pieceKey, victimKey = victimKey,
            square = square, mateIn = mateIn
        )
    }
}
