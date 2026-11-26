package com.example.domain.chess

import com.example.domain.model.AnalyzedPly
import com.example.domain.model.BoardState
import com.example.domain.model.ChessMove
import com.example.domain.model.GameAnalysisResult
import com.example.domain.model.MoveQuality
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

object ChessAi {

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

    fun chooseMoveForElo(board: BoardState, elo: Int): ChessMove? {
        val (depth, randomness) = when {
            elo <= 600 -> 1 to 0.55
            elo <= 900 -> 1 to 0.30
            elo <= 1200 -> 2 to 0.22
            elo <= 1500 -> 2 to 0.10
            elo <= 1850 -> 2 to 0.0
            elo <= 2200 -> 3 to 0.04
            else -> 3 to 0.0
        }
        return chooseMove(board, depth, randomness)
    }

    fun chooseMove(board: BoardState, depth: Int, randomness: Double): ChessMove? {
        val legal = ChessEngine.legalMoves(board)
        if (legal.isEmpty()) return null

        val ordered = orderMoves(legal)
        if (randomness > 0.0 && Random.nextDouble() < randomness) {
            val topSlice = ordered.take(minOf(4, ordered.size))
            return topSlice.random()
        }
        return bestMoveWithEval(board, depth).first ?: ordered.first()
    }

    fun bestMoveWithEval(board: BoardState, depth: Int = 2): Pair<ChessMove?, Int> {
        val legal = ChessEngine.legalMoves(board)
        if (legal.isEmpty()) {
            val score = if (ChessEngine.isInCheck(board, board.turn)) {
                if (board.turn == PieceColor.WHITE) -100_000 else 100_000
            } else 0
            return null to score
        }

        val ordered = orderMoves(legal)
        val maximizing = board.turn == PieceColor.WHITE
        var bestScore = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE
        val bestMoves = mutableListOf<ChessMove>()

        for (move in ordered) {
            val next = ChessEngine.applyMove(board, move)
            val score = minimax(next, (depth - 1).coerceAtLeast(0), Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, !maximizing)
            if (maximizing) {
                if (score > bestScore) {
                    bestScore = score
                    bestMoves.clear()
                    bestMoves.add(move)
                } else if (score == bestScore) {
                    bestMoves.add(move)
                }
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMoves.clear()
                    bestMoves.add(move)
                } else if (score == bestScore) {
                    bestMoves.add(move)
                }
            }
        }
        return (bestMoves.randomOrNull() ?: ordered.first()) to bestScore
    }

    fun evaluatePositionCp(board: BoardState, depth: Int = 2): Int {
        return bestMoveWithEval(board, depth).second
    }

    fun analyzeGame(parsed: ParsedPgnGame, depth: Int = 2): GameAnalysisResult {
        val analyzedPlies = mutableListOf<AnalyzedPly>()
        var whiteLossTotal = 0
        var whiteMoves = 0
        var blackLossTotal = 0
        var blackMoves = 0

        for (pm in parsed.moves) {
            val isWhiteTurn = pm.boardBefore.turn == PieceColor.WHITE
            val (bestMove, bestEval) = bestMoveWithEval(pm.boardBefore, depth)
            val actualEval = evaluatePositionCp(pm.boardAfter, (depth - 1).coerceAtLeast(1))

            // Centipawn loss from the mover's perspective
            val rawLoss = if (isWhiteTurn) {
                (bestEval - actualEval).coerceAtLeast(0)
            } else {
                (actualEval - bestEval).coerceAtLeast(0)
            }
            val cappedLoss = rawLoss.coerceAtMost(1000)

            val isBest = bestMove != null && bestMove.toUci() == pm.move.toUci()
            val isSacrificeGain = pm.move.capturedPiece == null &&
                pm.boardBefore.pieceAt(pm.move.from)?.type in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT) &&
                isBest && abs(actualEval) > 250

            val quality = when {
                isSacrificeGain -> MoveQuality.BRILLIANT
                isBest || cappedLoss <= 15 -> MoveQuality.BEST
                cappedLoss <= 45 -> MoveQuality.GOOD
                cappedLoss <= 110 -> MoveQuality.INACCURACY
                cappedLoss <= 250 -> MoveQuality.MISTAKE
                else -> MoveQuality.BLUNDER
            }

            if (isWhiteTurn) {
                whiteLossTotal += cappedLoss
                whiteMoves++
            } else {
                blackLossTotal += cappedLoss
                blackMoves++
            }

            val bestSan = bestMove?.let { ChessEngine.toStandardSan(pm.boardBefore, it) }
            analyzedPlies.add(
                AnalyzedPly(
                    plyIndex = pm.plyIndex,
                    san = pm.san,
                    uci = pm.move.toUci(),
                    fenBefore = ChessEngine.toFen(pm.boardBefore),
                    fenAfter = ChessEngine.toFen(pm.boardAfter),
                    evalCpAfter = actualEval.coerceIn(-1500, 1500),
                    bestMoveUci = bestMove?.toUci(),
                    bestMoveSan = bestSan,
                    quality = quality,
                    cpLoss = cappedLoss
                )
            )
        }

        val wAcc = if (whiteMoves == 0) 100 else (100 - (whiteLossTotal.toDouble() / whiteMoves) * 0.25).roundToInt().coerceIn(20, 100)
        val bAcc = if (blackMoves == 0) 100 else (100 - (blackLossTotal.toDouble() / blackMoves) * 0.25).roundToInt().coerceIn(20, 100)

        return GameAnalysisResult(
            whitePlayer = parsed.headers["White"] ?: "Blancs",
            blackPlayer = parsed.headers["Black"] ?: "Noirs",
            event = parsed.headers["Event"] ?: "Partie importée",
            result = parsed.result,
            plies = analyzedPlies,
            whiteAccuracy = wAcc,
            blackAccuracy = bAcc
        )
    }

    private fun minimax(board: BoardState, depth: Int, alphaInit: Int, betaInit: Int, maximizing: Boolean): Int {
        val moves = ChessEngine.legalMoves(board)
        if (moves.isEmpty()) {
            return if (ChessEngine.isInCheck(board, board.turn)) {
                if (board.turn == PieceColor.WHITE) -100_000 - depth else 100_000 + depth
            } else {
                0
            }
        }
        if (depth <= 0) {
            return evaluateBoard(board)
        }

        var alpha = alphaInit
        var beta = betaInit
        val ordered = orderMoves(moves)

        if (maximizing) {
            var maxEval = Int.MIN_VALUE + 1
            for (move in ordered) {
                val score = minimax(ChessEngine.applyMove(board, move), depth - 1, alpha, beta, false)
                if (score > maxEval) maxEval = score
                if (score > alpha) alpha = score
                if (beta <= alpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE - 1
            for (move in ordered) {
                val score = minimax(ChessEngine.applyMove(board, move), depth - 1, alpha, beta, true)
                if (score < minEval) minEval = score
                if (score < beta) beta = score
                if (beta <= alpha) break
            }
            return minEval
        }
    }

    private fun orderMoves(moves: List<ChessMove>): List<ChessMove> {
        return moves.sortedByDescending { move ->
            var score = 0
            if (move.capturedPiece != null) score += 1000 + move.capturedPiece.type.value
            if (move.promotion != null) score += 800 + move.promotion.value
            if (move.isCastleKingSide || move.isCastleQueenSide) score += 120
            score
        }
    }

    private fun evaluateBoard(board: BoardState): Int {
        var score = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board.grid[r][c] ?: continue
                val tableRow = if (p.color == PieceColor.WHITE) r else 7 - r
                val posBonus = when (p.type) {
                    PieceType.PAWN -> pawnTable[tableRow][c]
                    PieceType.KNIGHT -> knightTable[tableRow][c]
                    PieceType.BISHOP -> bishopTable[tableRow][c]
                    PieceType.ROOK -> if (tableRow == 1) 15 else 0
                    PieceType.QUEEN -> if (r in 2..5 && c in 2..5) 10 else 0
                    PieceType.KING -> if (c in listOf(0, 1, 2, 6, 7)) 15 else -10
                }
                val total = p.type.value + posBonus
                if (p.color == PieceColor.WHITE) score += total else score -= total
            }
        }
        return score
    }
}
