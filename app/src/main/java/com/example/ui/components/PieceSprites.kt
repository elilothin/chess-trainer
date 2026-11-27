package com.example.ui.components

import com.example.domain.model.BoardState
import com.example.domain.model.ChessPiece
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType
import com.example.domain.model.Square
import com.example.ui.feedback.MoveKind
import kotlin.math.abs

/** Une pièce affichée, avec une identité stable pour pouvoir l'animer d'une case à l'autre. */
data class Sprite(val id: Long, val piece: ChessPiece, val square: Square)

/**
 * Suit les pièces d'une position à l'autre : une pièce qui bouge garde son identifiant (donc glisse),
 * une pièce capturée devient un « fantôme » qui s'efface, un grand changement de position
 * (nouvel exercice, par exemple) remplace tout sans animation.
 */
class SpriteTracker {
    data class Frame(val sprites: List<Sprite>, val ghosts: List<Sprite>, val animated: Boolean)

    private var nextId = 1L
    private var current: List<Sprite> = emptyList()
    private var first = true

    fun advance(board: BoardState): Frame {
        val target = ArrayList<Pair<Square, ChessPiece>>()
        for (r in 0..7) for (c in 0..7) board.grid[r][c]?.let { target.add(Square(r, c) to it) }

        if (first) {
            first = false
            current = target.map { (sq, p) -> Sprite(nextId++, p, sq) }
            return Frame(current, emptyList(), false)
        }

        val keptIds = HashSet<Long>()
        val matchedTargets = HashSet<Square>()
        val kept = ArrayList<Sprite>()
        for (s in current) {
            if (board.pieceAt(s.square) == s.piece) {
                keptIds.add(s.id); matchedTargets.add(s.square); kept.add(s)
            }
        }
        val oldLeft = current.filter { it.id !in keptIds }.toMutableList()
        val newLeft = target.filter { it.first !in matchedTargets }

        if (oldLeft.size + newLeft.size > 6) {
            current = target.map { (sq, p) -> Sprite(nextId++, p, sq) }
            return Frame(current, emptyList(), false)
        }

        val result = ArrayList<Sprite>(kept)
        val unmatchedNew = ArrayList<Pair<Square, ChessPiece>>()
        // 1) même pièce qui se déplace : on prend la plus proche
        for ((sq, p) in newLeft) {
            val candidate = oldLeft.filter { it.piece == p }.minByOrNull { dist(it.square, sq) }
            if (candidate != null) {
                oldLeft.remove(candidate)
                result.add(Sprite(candidate.id, p, sq))
            } else unmatchedNew.add(sq to p)
        }
        // 2) promotion : un pion disparaît, une autre pièce du même camp apparaît
        val stillNew = ArrayList<Pair<Square, ChessPiece>>()
        for ((sq, p) in unmatchedNew) {
            val pawn = if (p.type != PieceType.PAWN && p.type != PieceType.KING) {
                oldLeft.filter { it.piece.type == PieceType.PAWN && it.piece.color == p.color }
                    .minByOrNull { dist(it.square, sq) }
            } else null
            if (pawn != null) {
                oldLeft.remove(pawn)
                result.add(Sprite(pawn.id, p, sq))
            } else stillNew.add(sq to p)
        }
        stillNew.forEach { (sq, p) -> result.add(Sprite(nextId++, p, sq)) }

        val changed = oldLeft.isNotEmpty() || unmatchedNew.isNotEmpty() || result.any { r -> current.none { it.id == r.id && it.square == r.square } }
        current = result
        return Frame(result, oldLeft.toList(), changed)
    }

    private fun dist(a: Square, b: Square) = abs(a.row - b.row) + abs(a.col - b.col)
}

/** Déduit la nature du coup entre deux positions, ou null si le changement n'est pas un coup simple. */
object MoveClassifier {
    fun classify(old: BoardState, new: BoardState, newIsCheck: Boolean): MoveKind? {
        var diff = 0
        var oldCount = 0
        var newCount = 0
        val kingMoved = HashSet<PieceColor>()
        for (r in 0..7) for (c in 0..7) {
            val a = old.grid[r][c]
            val b = new.grid[r][c]
            if (a != null) oldCount++
            if (b != null) newCount++
            if (a != b) diff++
            if (a != null && a.type == PieceType.KING && b != a) kingMoved.add(a.color)
        }
        if (diff == 0 || diff > 4) return null
        if (oldCount - newCount !in 0..1) return null
        val promoted = countType(old, PieceType.PAWN) > countType(new, PieceType.PAWN) &&
            (countType(new, PieceType.QUEEN) + countType(new, PieceType.ROOK) + countType(new, PieceType.BISHOP) + countType(new, PieceType.KNIGHT) >
                countType(old, PieceType.QUEEN) + countType(old, PieceType.ROOK) + countType(old, PieceType.BISHOP) + countType(old, PieceType.KNIGHT))
        return when {
            newIsCheck -> MoveKind.CHECK
            promoted -> MoveKind.PROMOTION
            diff == 4 && kingMoved.isNotEmpty() && oldCount == newCount -> MoveKind.CASTLE
            oldCount > newCount -> MoveKind.CAPTURE
            else -> MoveKind.QUIET
        }
    }

    private fun countType(b: BoardState, t: PieceType): Int {
        var n = 0
        for (r in 0..7) for (c in 0..7) if (b.grid[r][c]?.type == t) n++
        return n
    }
}
