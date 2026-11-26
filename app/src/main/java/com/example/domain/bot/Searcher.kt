package com.example.domain.bot

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move

const val MATE_SCORE = 100_000
const val MATE_THRESHOLD = MATE_SCORE - 500

data class RootScore(val move: Move, val score: Int)

data class SearchResult(
    val best: Move?,
    /** Score du point de vue du camp au trait, en centipions. */
    val score: Int,
    val depth: Int,
    val nodes: Long,
    /** Rempli seulement quand tous les coups racine ont été évalués à fenêtre complète. */
    val rootScores: List<RootScore> = emptyList()
)

/**
 * Recherche alpha-bêta (negamax) avec approfondissement itératif, table de transposition,
 * tri MVV-LVA, coups "killer", extension d'échec et recherche de quiescence.
 * Une instance n'est pas thread-safe : en créer une par recherche.
 */
class Searcher(
    private val useQuiescence: Boolean = true,
    private val botSide: Side? = null,
    private val aggression: Int = 0
) {
    private val tableSize = 1 shl 16
    private val tableMask = tableSize - 1
    private val ttKeys = LongArray(tableSize)
    private val ttScore = IntArray(tableSize)
    private val ttDepth = IntArray(tableSize) { -1 }
    private val ttFlag = IntArray(tableSize)
    private val ttMove = arrayOfNulls<Move>(tableSize)
    private val killers = Array(64) { arrayOfNulls<Move>(2) }

    private var nodes = 0L
    private var deadline = Long.MAX_VALUE
    private var aborted = false

    private fun evalStm(b: Board): Int {
        val white = Evaluator.evaluate(b, botSide, aggression)
        // petit bonus de trait
        return (if (b.sideToMove == Side.WHITE) white else -white) + 8
    }

    private fun timeUp(): Boolean {
        if ((nodes and 1023L) == 0L && System.currentTimeMillis() >= deadline) aborted = true
        return aborted
    }

    private fun pieceIdx(p: Piece): Int = when (p.pieceType) {
        PieceType.PAWN -> 0
        PieceType.KNIGHT -> 1
        PieceType.BISHOP -> 2
        PieceType.ROOK -> 3
        PieceType.QUEEN -> 4
        else -> 5
    }

    private fun isCapture(b: Board, m: Move): Boolean = b.getPiece(m.to) != Piece.NONE

    private fun orderMoves(b: Board, moves: List<Move>, ttBest: Move?, ply: Int): List<Move> {
        val k1 = if (ply < 64) killers[ply][0] else null
        val k2 = if (ply < 64) killers[ply][1] else null
        return moves.sortedByDescending { m ->
            when {
                ttBest != null && m == ttBest -> 1_000_000
                else -> {
                    var s = 0
                    val victim = b.getPiece(m.to)
                    if (victim != Piece.NONE) {
                        s += 10_000 + Evaluator.VALUE[pieceIdx(victim)] * 10 - Evaluator.VALUE[pieceIdx(b.getPiece(m.from))] / 10
                    } else if (m == k1) s += 9_000
                    else if (m == k2) s += 8_000
                    if (m.promotion != Piece.NONE) s += 9_500
                    s
                }
            }
        }
    }

    private fun quiesce(b: Board, alpha0: Int, beta: Int, ply: Int, qply: Int): Int {
        nodes++
        if (timeUp()) return 0
        var alpha = alpha0
        val standPat = evalStm(b)
        if (standPat >= beta) return standPat
        if (standPat > alpha) alpha = standPat
        if (qply >= 6) return alpha

        val captures = b.legalMoves().filter { isCapture(b, it) || it.promotion != Piece.NONE }
        if (captures.isEmpty()) return alpha
        for (m in orderMoves(b, captures, null, ply)) {
            // élagage delta
            val victim = b.getPiece(m.to)
            val gain = if (victim != Piece.NONE) Evaluator.VALUE[pieceIdx(victim)] else 0
            if (standPat + gain + 200 < alpha && m.promotion == Piece.NONE) continue
            b.doMove(m, false)
            val score = -quiesce(b, -beta, -alpha, ply + 1, qply + 1)
            b.undoMove()
            if (aborted) return 0
            if (score >= beta) return score
            if (score > alpha) alpha = score
        }
        return alpha
    }

    private fun negamax(b: Board, depth: Int, alpha0: Int, beta0: Int, ply: Int): Int {
        nodes++
        if (timeUp()) return 0
        if (ply > 0 && (b.halfMoveCounter >= 100 || b.isInsufficientMaterial || b.isRepetition(2))) return 0

        val inCheck = b.isKingAttacked
        val d = if (inCheck && ply < 40) depth + 1 else depth
        if (d <= 0) return if (useQuiescence) quiesce(b, alpha0, beta0, ply, 0) else evalStm(b)

        var alpha = alpha0
        var beta = beta0
        val key = b.zobristKey
        val slot = ((key xor (key ushr 29)).toInt()) and tableMask
        var ttBest: Move? = null
        if (ttKeys[slot] == key && ttDepth[slot] >= 0) {
            ttBest = ttMove[slot]
            if (ttDepth[slot] >= d) {
                val s = ttScore[slot]
                when (ttFlag[slot]) {
                    0 -> return s
                    1 -> if (s > alpha) alpha = s
                    else -> if (s < beta) beta = s
                }
                if (alpha >= beta) return s
            }
        }

        val moves = b.legalMoves()
        if (moves.isEmpty()) return if (inCheck) -(MATE_SCORE - ply) else 0

        var best = -MATE_SCORE - 1
        var bestMove: Move? = null
        val origAlpha = alpha
        for (m in orderMoves(b, moves, ttBest, ply)) {
            b.doMove(m, false)
            val score = -negamax(b, d - 1, -beta, -alpha, ply + 1)
            b.undoMove()
            if (aborted) return 0
            if (score > best) {
                best = score
                bestMove = m
            }
            if (score > alpha) alpha = score
            if (alpha >= beta) {
                if (!isCapture(b, m) && ply < 64) {
                    if (killers[ply][0] != m) {
                        killers[ply][1] = killers[ply][0]
                        killers[ply][0] = m
                    }
                }
                break
            }
        }

        ttKeys[slot] = key
        ttScore[slot] = best
        ttDepth[slot] = d
        ttMove[slot] = bestMove
        ttFlag[slot] = when {
            best <= origAlpha -> 2 // borne supérieure
            best >= beta -> 1      // borne inférieure
            else -> 0
        }
        return best
    }

    /** Recherche classique avec approfondissement itératif. */
    fun search(board: Board, maxDepth: Int, timeMs: Long): SearchResult {
        nodes = 0
        aborted = false
        deadline = System.currentTimeMillis() + timeMs
        val rootMoves = board.legalMoves()
        if (rootMoves.isEmpty()) {
            val s = if (board.isKingAttacked) -MATE_SCORE else 0
            return SearchResult(null, s, 0, 0)
        }
        if (rootMoves.size == 1) {
            return SearchResult(rootMoves[0], 0, 0, 0)
        }

        var bestMove: Move = rootMoves[0]
        var bestScore = 0
        var doneDepth = 0
        var ordered: List<Move> = orderMoves(board, rootMoves, null, 0)

        for (depth in 1..maxDepth) {
            var alpha = -MATE_SCORE - 1
            val beta = MATE_SCORE + 1
            var iterBest: Move? = null
            var iterScore = -MATE_SCORE - 1
            for (m in ordered) {
                board.doMove(m, false)
                val score = -negamax(board, depth - 1, -beta, -alpha, 1)
                board.undoMove()
                if (aborted) break
                if (score > iterScore) {
                    iterScore = score
                    iterBest = m
                }
                if (score > alpha) alpha = score
            }
            if (aborted) break
            if (iterBest != null) {
                bestMove = iterBest
                bestScore = iterScore
                doneDepth = depth
                ordered = listOf(iterBest) + ordered.filter { it != iterBest }
            }
            if (bestScore >= MATE_THRESHOLD || bestScore <= -MATE_THRESHOLD) break
            if (System.currentTimeMillis() >= deadline) break
        }
        return SearchResult(bestMove, bestScore, doneDepth, nodes)
    }

    /**
     * Évalue tous les coups racine à fenêtre complète (scores exacts) à une profondeur fixe.
     * Utilisé pour faire jouer des coups "humains" (bruit, erreurs) aux bots de niveau faible/moyen.
     */
    fun scoreAllRootMoves(board: Board, depth: Int, timeMs: Long): SearchResult {
        nodes = 0
        aborted = false
        deadline = System.currentTimeMillis() + timeMs
        val rootMoves = board.legalMoves()
        if (rootMoves.isEmpty()) return SearchResult(null, if (board.isKingAttacked) -MATE_SCORE else 0, 0, 0)

        val scored = ArrayList<RootScore>(rootMoves.size)
        for (m in orderMoves(board, rootMoves, null, 0)) {
            board.doMove(m, false)
            val s = -negamax(board, (depth - 1).coerceAtLeast(0), -MATE_SCORE - 1, MATE_SCORE + 1, 1)
            board.undoMove()
            if (aborted) break
            scored.add(RootScore(m, s))
        }
        if (scored.isEmpty()) {
            // temps écoulé avant le premier coup : repli sur le premier coup
            return SearchResult(rootMoves[0], 0, 0, nodes)
        }
        val top = scored.maxByOrNull { it.score }!!
        return SearchResult(top.move, top.score, depth, nodes, scored)
    }

    /** Score exact (point de vue du camp au trait dans [board]) à profondeur fixe, sans limite de temps. */
    fun scorePosition(board: Board, depth: Int, timeMs: Long): Int {
        nodes = 0
        aborted = false
        deadline = System.currentTimeMillis() + timeMs
        return negamax(board, depth, -MATE_SCORE - 1, MATE_SCORE + 1, 0)
    }
}
