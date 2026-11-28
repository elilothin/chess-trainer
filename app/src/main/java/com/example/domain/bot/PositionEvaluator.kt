package com.example.domain.bot

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side

/** Évaluation affichée par la jauge d'avantage. */
data class Advantage(
    /** Part de chances de gain des Blancs, entre 0 (Noirs gagnent) et 1 (Blancs gagnent). */
    val whiteShare: Float,
    /** Texte court : "+0.8", "-1.3", "M3"… du point de vue des Blancs. */
    val label: String,
    val centipawns: Int,
    val mateFor: Side? = null
)

object PositionEvaluator {
    fun evaluate(fen: String, depth: Int = 3, timeMs: Long = 450): Advantage {
        val board = Board()
        board.loadFromFen(fen)
        val white = board.sideToMove == Side.WHITE
        if (board.legalMoves().isEmpty()) {
            return if (board.isKingAttacked) {
                val winner = if (white) Side.BLACK else Side.WHITE
                Advantage(if (winner == Side.WHITE) 1f else 0f, "#", if (winner == Side.WHITE) MATE_SCORE else -MATE_SCORE, winner)
            } else Advantage(0.5f, "0.0", 0)
        }
        val res = Searcher(true).search(board, depth, timeMs)
        val cpWhite = if (white) res.score else -res.score
        return fromWhiteCp(cpWhite)
    }

    fun fromWhiteCp(cpWhite: Int): Advantage {
        if (cpWhite >= MATE_THRESHOLD) {
            val moves = (MATE_SCORE - cpWhite + 1) / 2
            return Advantage(1f, "M$moves", cpWhite, Side.WHITE)
        }
        if (cpWhite <= -MATE_THRESHOLD) {
            val moves = (MATE_SCORE + cpWhite + 1) / 2
            return Advantage(0f, "-M$moves", cpWhite, Side.BLACK)
        }
        val pct = (MoveCoach.winPct(cpWhite) / 100.0).toFloat()
        val pawns = cpWhite / 100.0
        val label = (if (pawns >= 0) "+" else "") + String.format(java.util.Locale.US, "%.1f", pawns)
        return Advantage(pct, label, cpWhite)
    }
}
