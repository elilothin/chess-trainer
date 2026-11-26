package com.example.domain.chess

import com.example.domain.model.BoardState
import com.example.domain.model.ChessMove

data class ParsedPgnMove(
    val plyIndex: Int,
    val san: String,
    val move: ChessMove,
    val boardBefore: BoardState,
    val boardAfter: BoardState
)

data class ParsedPgnGame(
    val headers: Map<String, String>,
    val initialBoard: BoardState,
    val moves: List<ParsedPgnMove>,
    val result: String
)

object PgnParser {

    fun parse(pgnText: String): ParsedPgnGame {
        val headers = mutableMapOf<String, String>()
        val headerRegex = Regex("""\[([A-Za-z0-9_]+)\s+"([^"]*)"\]""")
        for (match in headerRegex.findAll(pgnText)) {
            headers[match.groupValues[1]] = match.groupValues[2]
        }

        // Remove header lines
        val body = pgnText.lines()
            .filterNot { it.trimStart().startsWith("[") }
            .joinToString(" ")

        // Strip {...} comments and (...) variations cleanly
        val cleaned = stripCommentsAndVariations(body)

        val initialFen = headers["FEN"]
        var board = if (!initialFen.isNullOrBlank()) ChessEngine.fromFen(initialFen) else ChessEngine.initialBoard()
        val initialBoard = board

        val tokens = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        val parsedMoves = mutableListOf<ParsedPgnMove>()
        var result = headers["Result"] ?: "*"
        var ply = 1

        for (rawToken in tokens) {
            val token = rawToken.trim()
            if (token in listOf("1-0", "0-1", "1/2-1/2", "*")) {
                result = token
                break
            }
            // Strip leading move numbers like "1.", "12...", "1.e4"
            val afterNum = token.replace(Regex("""^\d+\.+"""), "")
            if (afterNum.isEmpty() || afterNum.startsWith("$")) continue

            val move = ChessEngine.parseSanMove(board, afterNum) ?: break
            val nextBoard = ChessEngine.applyMove(board, move)
            val canonicalSan = ChessEngine.toStandardSan(board, move)
            parsedMoves.add(
                ParsedPgnMove(
                    plyIndex = ply,
                    san = canonicalSan,
                    move = move,
                    boardBefore = board,
                    boardAfter = nextBoard
                )
            )
            board = nextBoard
            ply++
        }

        return ParsedPgnGame(
            headers = headers,
            initialBoard = initialBoard,
            moves = parsedMoves,
            result = result
        )
    }

    fun exportPgn(
        movesSan: List<String>,
        whiteName: String = "Joueur",
        blackName: String = "IA",
        result: String = "*",
        event: String = "Échecs Coach"
    ): String {
        val sb = StringBuilder()
        sb.appendLine("""[Event "$event"]""")
        sb.appendLine("""[White "$whiteName"]""")
        sb.appendLine("""[Black "$blackName"]""")
        sb.appendLine("""[Result "$result"]""")
        sb.appendLine()
        for (i in movesSan.indices) {
            if (i % 2 == 0) {
                sb.append("${i / 2 + 1}. ")
            }
            sb.append(movesSan[i]).append(' ')
        }
        sb.append(result)
        return sb.toString().trim()
    }

    private fun stripCommentsAndVariations(input: String): String {
        val out = StringBuilder()
        var inBrace = false
        var parenDepth = 0
        var inLineComment = false
        for (ch in input) {
            if (inLineComment) {
                if (ch == '\n') inLineComment = false
                continue
            }
            if (inBrace) {
                if (ch == '}') inBrace = false
                continue
            }
            if (ch == ';') {
                inLineComment = true
                continue
            }
            if (ch == '{') {
                inBrace = true
                out.append(' ')
                continue
            }
            if (ch == '(') {
                parenDepth++
                continue
            }
            if (ch == ')') {
                if (parenDepth > 0) parenDepth--
                continue
            }
            if (parenDepth == 0) {
                out.append(ch)
            }
        }
        return out.toString()
    }
}
