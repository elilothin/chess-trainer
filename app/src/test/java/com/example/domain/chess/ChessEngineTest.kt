package com.example.domain.chess

import com.example.data.content.EndgamesHistoryQuizContent
import com.example.data.content.LessonsContent
import com.example.data.content.OpeningsAndMiddlegameContent
import com.example.data.content.PuzzlesContent
import com.example.domain.model.GameOutcome
import com.example.domain.model.PieceColor
import com.example.domain.model.PieceType
import com.example.domain.model.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessEngineTest {

    @Test
    fun initialPosition_has20LegalMoves() {
        val board = ChessEngine.initialBoard()
        val moves = ChessEngine.legalMoves(board)
        assertEquals(20, moves.size)
        assertEquals(GameOutcome.ONGOING, ChessEngine.evaluateOutcome(board))
    }

    @Test
    fun foolsMate_detectsCheckmateBlackWins() {
        var board = ChessEngine.initialBoard()
        val sequence = listOf("f2f3", "e7e5", "g2g4", "d8h4")
        for (uci in sequence) {
            val move = ChessEngine.moveFromUci(board, uci)
            assertNotNull("Move $uci should be legal", move)
            board = ChessEngine.applyMove(board, move!!)
        }
        assertTrue(ChessEngine.isInCheck(board, PieceColor.WHITE))
        assertEquals(GameOutcome.CHECKMATE_BLACK_WINS, ChessEngine.evaluateOutcome(board))
    }

    @Test
    fun castling_and_enPassant_areSupported() {
        val castleFen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        val board = ChessEngine.fromFen(castleFen)
        val kingMoves = ChessEngine.legalMovesFrom(board, Square.fromAlgebraic("e1")!!)
        assertTrue(kingMoves.any { it.isCastleKingSide && it.to == Square.fromAlgebraic("g1") })
        assertTrue(kingMoves.any { it.isCastleQueenSide && it.to == Square.fromAlgebraic("c1") })

        val epFen = "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
        val epBoard = ChessEngine.fromFen(epFen)
        val epMoves = ChessEngine.legalMovesFrom(epBoard, Square.fromAlgebraic("e5")!!)
        assertTrue(epMoves.any { it.isEnPassant && it.to == Square.fromAlgebraic("d6") })
    }

    @Test
    fun pawnPromotion_generatesFourPromotionChoices() {
        val promoFen = "4k3/P7/8/8/8/8/8/4K3 w - - 0 1"
        val board = ChessEngine.fromFen(promoFen)
        val moves = ChessEngine.legalMovesFrom(board, Square.fromAlgebraic("a7")!!)
            .filter { it.to == Square.fromAlgebraic("a8") }
        assertEquals(4, moves.size)
        assertEquals(
            setOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT),
            moves.mapNotNull { it.promotion }.toSet()
        )
    }

    @Test
    fun allBundledLessons_haveLegalExpectedMoves() {
        for (lesson in LessonsContent.lessons) {
            val board = ChessEngine.fromFen(lesson.initialFen)
            for (uci in lesson.expectedMovesUci) {
                val move = ChessEngine.moveFromUci(board, uci)
                assertNotNull("Lesson ${lesson.id} expected move $uci must be legal", move)
            }
        }
    }

    @Test
    fun allBundledPuzzles_haveLegalSolutionSequences() {
        for (puzzle in PuzzlesContent.puzzles) {
            var board = ChessEngine.fromFen(puzzle.initialFen)
            for (uci in puzzle.solutionUci) {
                val move = ChessEngine.moveFromUci(board, uci)
                assertNotNull("Puzzle ${puzzle.id} solution move $uci must be legal", move)
                board = ChessEngine.applyMove(board, move!!)
            }
            if (puzzle.theme == com.example.domain.model.PuzzleTheme.MATE_IN_1 ||
                puzzle.theme == com.example.domain.model.PuzzleTheme.MATE_IN_2
            ) {
                val outcome = ChessEngine.evaluateOutcome(board)
                assertTrue(
                    "Puzzle ${puzzle.id} (${puzzle.theme}) must end in checkmate, got $outcome",
                    outcome == GameOutcome.CHECKMATE_WHITE_WINS || outcome == GameOutcome.CHECKMATE_BLACK_WINS
                )
            }
        }
    }

    @Test
    fun allMiddlegameExercises_and_openings_areLegal() {
        for (ex in OpeningsAndMiddlegameContent.middlegameExercises) {
            val board = ChessEngine.fromFen(ex.initialFen)
            for (uci in ex.acceptedMovesUci) {
                assertNotNull("Middlegame ${ex.id} move $uci must be legal", ChessEngine.moveFromUci(board, uci))
            }
        }
        for (op in OpeningsAndMiddlegameContent.classicOpenings) {
            var board = ChessEngine.initialBoard()
            for (uci in op.movesUci) {
                val m = ChessEngine.moveFromUci(board, uci)
                assertNotNull("Opening ${op.id} move $uci must be legal", m)
                board = ChessEngine.applyMove(board, m!!)
            }
        }
    }

    @Test
    fun allHistoricalGames_parseCompletely() {
        for (game in EndgamesHistoryQuizContent.historicalGames) {
            val parsed = PgnParser.parse(game.pgn)
            assertTrue("Historical game ${game.id} should parse >= 30 plies", parsed.moves.size >= 30)
        }
    }

    @Test
    fun statefulChessEngine_handlesTapMovesUndoAndEvaluation() {
        val engine = ChessEngine()
        assertEquals(PieceColor.WHITE, engine.state.value.turn)
        assertEquals(20, engine.state.value.legalMoves.size)

        // Tap e2 -> highlights e3, e4
        val e2 = Square.fromAlgebraic("e2")!!
        val e4 = Square.fromAlgebraic("e4")!!
        assertEquals(false, engine.onSquareTapped(e2))
        assertEquals(setOf(Square.fromAlgebraic("e3")!!, e4), engine.state.value.legalTargets)

        // Tap e4 -> plays 1. e4
        assertTrue(engine.onSquareTapped(e4))
        assertEquals(PieceColor.BLACK, engine.state.value.turn)
        assertEquals(listOf("e4"), engine.state.value.moveHistorySan)
        assertTrue(engine.state.value.canUndo)

        // Undo reverts to White's turn
        assertTrue(engine.undoMove())
        assertEquals(PieceColor.WHITE, engine.state.value.turn)
        assertTrue(engine.state.value.moveHistorySan.isEmpty())

        // Material advantage evaluation checks
        engine.loadFen("4k3/8/8/8/8/8/4Q3/4K3 w - - 0 1")
        val whiteWinningCp = engine.evaluatePositionCp(depth = 1)
        assertTrue("White Queen up should evaluate > +800 cp, got $whiteWinningCp", whiteWinningCp > 800)
        assertNotNull(engine.bestMove(depth = 1))
    }

    @Test
    fun statefulChessEngine_detectsThreefoldRepetition() {
        val engine = ChessEngine()
        val sequence = listOf(
            "g1f3", "g8f6", "f3g1", "f6g8",
            "g1f3", "g8f6", "f3g1", "f6g8"
        )
        for (uci in sequence) {
            assertTrue("Move $uci must succeed", engine.playUci(uci))
        }
        assertTrue(engine.isRepetition(3))
        assertEquals(GameOutcome.DRAW_REPETITION, engine.state.value.outcome)
        assertTrue(engine.state.value.isGameOver)
    }
}
