package com.example.domain.bot

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Évaluation statique rapide (en centipions, du point de vue des Blancs).
 * Matériel + tables pièce-case + structure de pions + colonnes ouvertes + sécurité du roi.
 */
internal object Evaluator {

    val VALUE = intArrayOf(100, 320, 330, 500, 900, 0) // PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING

    private val PAWN = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5, 5, 10, 25, 25, 10, 5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, -5, -10, 0, 0, -10, -5, 5,
        5, 10, 10, -20, -20, 10, 10, 5,
        0, 0, 0, 0, 0, 0, 0, 0
    )
    private val KNIGHT = intArrayOf(
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    )
    private val BISHOP = intArrayOf(
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 10, 10, 5, 0, -10,
        -10, 5, 5, 10, 10, 5, 5, -10,
        -10, 0, 10, 10, 10, 10, 0, -10,
        -10, 10, 10, 10, 10, 10, 10, -10,
        -10, 5, 0, 0, 0, 0, 5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    )
    private val ROOK = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        5, 10, 10, 10, 10, 10, 10, 5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        0, 0, 0, 5, 5, 0, 0, 0
    )
    private val QUEEN = intArrayOf(
        -20, -10, -10, -5, -5, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 5, 5, 5, 0, -10,
        -5, 0, 5, 5, 5, 5, 0, -5,
        0, 0, 5, 5, 5, 5, 0, -5,
        -10, 5, 5, 5, 5, 5, 0, -10,
        -10, 0, 5, 0, 0, 0, 0, -10,
        -20, -10, -10, -5, -5, -10, -10, -20
    )
    private val KING_MG = intArrayOf(
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        20, 20, 0, 0, 0, 0, 20, 20,
        20, 30, 10, 0, 0, 10, 30, 20
    )
    private val KING_EG = intArrayOf(
        -50, -40, -30, -20, -20, -30, -40, -50,
        -30, -20, -10, 0, 0, -10, -20, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -30, 0, 0, 0, 0, -30, -30,
        -50, -30, -30, -30, -30, -30, -30, -50
    )

    private val PASSED_BONUS = intArrayOf(0, 5, 10, 20, 35, 60, 100, 0)

    private val WHITE_PIECES = arrayOf(
        Piece.WHITE_PAWN, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP,
        Piece.WHITE_ROOK, Piece.WHITE_QUEEN, Piece.WHITE_KING
    )
    private val BLACK_PIECES = arrayOf(
        Piece.BLACK_PAWN, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP,
        Piece.BLACK_ROOK, Piece.BLACK_QUEEN, Piece.BLACK_KING
    )

    private fun table(type: Int, kingEnd: Boolean): IntArray = when (type) {
        0 -> PAWN
        1 -> KNIGHT
        2 -> BISHOP
        3 -> ROOK
        4 -> QUEEN
        else -> if (kingEnd) KING_EG else KING_MG
    }

    private fun fileMask(file: Int): Long {
        var m = 0L
        for (r in 0..7) m = m or (1L shl (r * 8 + file))
        return m
    }

    private val FILE_MASKS = LongArray(8) { fileMask(it) }

    /** Évaluation du point de vue des Blancs. [botSide] + [aggression] ajoutent un biais de style offensif. */
    fun evaluate(b: Board, botSide: Side? = null, aggression: Int = 0): Int {
        val bb = Array(2) { s -> LongArray(6) { t -> b.getBitboard(if (s == 0) WHITE_PIECES[t] else BLACK_PIECES[t]) } }

        // Phase de jeu : matériel non-pion total
        var nonPawn = 0
        for (s in 0..1) for (t in 1..4) nonPawn += java.lang.Long.bitCount(bb[s][t]) * VALUE[t]
        val endgame = 1f - min(1f, nonPawn / 5200f)

        var score = 0
        val kingSq = IntArray(2) { s -> java.lang.Long.numberOfTrailingZeros(bb[s][5]) }

        for (s in 0..1) {
            val sign = if (s == 0) 1 else -1
            var side = 0
            for (t in 0..5) {
                var bits = bb[s][t]
                while (bits != 0L) {
                    val sq = java.lang.Long.numberOfTrailingZeros(bits)
                    bits = bits and (bits - 1)
                    val rank = sq shr 3
                    val file = sq and 7
                    val idx = if (s == 0) (7 - rank) * 8 + file else sq
                    side += VALUE[t]
                    if (t == 5) {
                        val mg = KING_MG[idx]
                        val eg = KING_EG[idx]
                        side += (mg * (1f - endgame) + eg * endgame).toInt()
                    } else {
                        side += table(t, false)[idx]
                    }
                }
            }

            // Paire de fous
            if (java.lang.Long.bitCount(bb[s][2]) >= 2) side += 35

            // Structure de pions
            val ownPawns = bb[s][0]
            val oppPawns = bb[1 - s][0]
            for (f in 0..7) {
                val onFile = ownPawns and FILE_MASKS[f]
                val count = java.lang.Long.bitCount(onFile)
                if (count > 1) side -= 12 * (count - 1)
                if (count > 0) {
                    val left = if (f > 0) ownPawns and FILE_MASKS[f - 1] else 0L
                    val right = if (f < 7) ownPawns and FILE_MASKS[f + 1] else 0L
                    if (left == 0L && right == 0L) side -= 10 * count
                }
            }
            var pawns = ownPawns
            while (pawns != 0L) {
                val sq = java.lang.Long.numberOfTrailingZeros(pawns)
                pawns = pawns and (pawns - 1)
                val rank = sq shr 3
                val file = sq and 7
                // pion passé : aucun pion adverse devant sur les colonnes adjacentes
                var blocked = false
                for (df in -1..1) {
                    val ff = file + df
                    if (ff < 0 || ff > 7) continue
                    var bits = oppPawns and FILE_MASKS[ff]
                    while (bits != 0L) {
                        val osq = java.lang.Long.numberOfTrailingZeros(bits)
                        bits = bits and (bits - 1)
                        val orank = osq shr 3
                        if ((s == 0 && orank > rank) || (s == 1 && orank < rank)) blocked = true
                    }
                }
                if (!blocked) {
                    val relRank = if (s == 0) rank else 7 - rank
                    side += PASSED_BONUS[relRank] + (PASSED_BONUS[relRank] * endgame).toInt() / 2
                }
            }

            // Tours sur colonnes ouvertes / semi-ouvertes
            var rooks = bb[s][3]
            while (rooks != 0L) {
                val sq = java.lang.Long.numberOfTrailingZeros(rooks)
                rooks = rooks and (rooks - 1)
                val f = sq and 7
                val own = ownPawns and FILE_MASKS[f]
                val opp = oppPawns and FILE_MASKS[f]
                if (own == 0L && opp == 0L) side += 18 else if (own == 0L) side += 8
            }

            // Bouclier de pions devant le roi (milieu de jeu)
            val ksq = kingSq[s]
            if (ksq in 0..63) {
                val kr = ksq shr 3
                val kf = ksq and 7
                val onBackSide = if (s == 0) kr <= 1 else kr >= 6
                if (onBackSide) {
                    var shield = 0
                    for (df in -1..1) {
                        val ff = kf + df
                        if (ff < 0 || ff > 7) continue
                        val r1 = if (s == 0) kr + 1 else kr - 1
                        val r2 = if (s == 0) kr + 2 else kr - 2
                        if (r1 in 0..7 && (ownPawns and (1L shl (r1 * 8 + ff))) != 0L) shield += 2
                        else if (r2 in 0..7 && (ownPawns and (1L shl (r2 * 8 + ff))) != 0L) shield += 1
                    }
                    side += ((shield * 7) * (1f - endgame)).toInt()
                }
            }

            // Biais de style : pièces actives près du roi adverse
            if (aggression > 0 && botSide != null && ((s == 0) == (botSide == Side.WHITE))) {
                val enemyKing = kingSq[1 - s]
                if (enemyKing in 0..63) {
                    val ekr = enemyKing shr 3
                    val ekf = enemyKing and 7
                    for (t in 1..4) {
                        var bits = bb[s][t]
                        while (bits != 0L) {
                            val sq = java.lang.Long.numberOfTrailingZeros(bits)
                            bits = bits and (bits - 1)
                            val dist = max(abs((sq shr 3) - ekr), abs((sq and 7) - ekf))
                            if (dist <= 3) side += (4 - dist) * aggression
                        }
                    }
                }
            }

            score += sign * side
        }
        return score
    }
}
