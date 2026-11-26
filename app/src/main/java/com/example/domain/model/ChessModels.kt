package com.example.domain.model

enum class PieceColor {
    WHITE, BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
    fun fenChar(): Char = if (this == WHITE) 'w' else 'b'
}

enum class PieceType(val symbol: Char, val value: Int, val frenchName: String, val englishName: String) {
    PAWN('P', 100, "Pion", "Pawn"),
    KNIGHT('N', 320, "Cavalier", "Knight"),
    BISHOP('B', 330, "Fou", "Bishop"),
    ROOK('R', 500, "Tour", "Rook"),
    QUEEN('Q', 900, "Dame", "Queen"),
    KING('K', 20000, "Roi", "King");

    companion object {
        fun fromChar(c: Char): PieceType? = when (c.uppercaseChar()) {
            'P' -> PAWN
            'N' -> KNIGHT
            'B' -> BISHOP
            'R' -> ROOK
            'Q' -> QUEEN
            'K' -> KING
            else -> null
        }
    }
}

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor
) {
    fun fenChar(): Char = if (color == PieceColor.WHITE) type.symbol else type.symbol.lowercaseChar()
}

data class Square(val row: Int, val col: Int) {
    // row 0 = rank 8 (top), row 7 = rank 1 (bottom); col 0 = file 'a', col 7 = file 'h'
    fun isValid(): Boolean = row in 0..7 && col in 0..7

    fun toAlgebraic(): String {
        val file = ('a' + col)
        val rank = (8 - row)
        return "$file$rank"
    }

    companion object {
        fun fromAlgebraic(str: String): Square? {
            if (str.length < 2) return null
            val col = str[0] - 'a'
            val rank = str[1] - '0'
            val row = 8 - rank
            return Square(row, col).takeIf { it.isValid() }
        }
    }
}

data class ChessMove(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCastleKingSide: Boolean = false,
    val isCastleQueenSide: Boolean = false,
    val isEnPassant: Boolean = false,
    val capturedPiece: ChessPiece? = null
) {
    fun toUci(): String {
        val promoChar = promotion?.symbol?.lowercaseChar()?.toString() ?: ""
        return "${from.toAlgebraic()}${to.toAlgebraic()}$promoChar"
    }
}

enum class GameOutcome {
    ONGOING,
    CHECKMATE_WHITE_WINS,
    CHECKMATE_BLACK_WINS,
    STALEMATE,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_FIFTY_MOVE,
    DRAW_REPETITION
}

data class CastlingRights(
    val whiteKingSide: Boolean = true,
    val whiteQueenSide: Boolean = true,
    val blackKingSide: Boolean = true,
    val blackQueenSide: Boolean = true
) {
    fun toFen(): String {
        val sb = StringBuilder()
        if (whiteKingSide) sb.append('K')
        if (whiteQueenSide) sb.append('Q')
        if (blackKingSide) sb.append('k')
        if (blackQueenSide) sb.append('q')
        return if (sb.isEmpty()) "-" else sb.toString()
    }
}

data class BoardState(
    val grid: List<List<ChessPiece?>>, // 8x8
    val turn: PieceColor,
    val castling: CastlingRights,
    val enPassantTarget: Square?,
    val halfMoveClock: Int,
    val fullMoveNumber: Int
) {
    fun pieceAt(sq: Square): ChessPiece? = if (sq.isValid()) grid[sq.row][sq.col] else null
}
