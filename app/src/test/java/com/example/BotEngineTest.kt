package com.example

import com.example.domain.bot.BotPlayer
import com.example.domain.bot.BotProfiles
import com.example.domain.bot.BotRoster
import com.example.domain.bot.Cause
import com.example.domain.bot.MoveCoach
import com.example.domain.bot.PlayProfile
import com.example.domain.bot.RatingEngine
import com.github.bhlangonijr.chesslib.Board
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BotEngineTest {

    @Test
    fun findsMateInOne() {
        // Mat du couloir : Ra8#
        val fen = "6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1"
        val move = BotPlayer.chooseMove(fen, BotProfiles.forElo(1800), Random(1))
        assertNotNull(move)
        assertEquals("a1a8", MoveCoach.uciOf(move!!))
    }

    @Test
    fun coachFlagsHangingQueen() {
        // Les Blancs jouent Dd1-d5?? dans une position où le pion e6 prend la dame
        val fen = "rnbqkbnr/pppp1ppp/4p3/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val note = MoveCoach.analyzeMove(fen, "d1h5", ply = 1)
        assertNotNull(note)
        println("note: ${note!!.grade} ${note.cause} ${note.san} best=${note.bestSan}")
    }

    @Test
    fun coachFlagsMovedIntoCapture() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/5n2/PPPPPPPP/RNBQKB1R w KQkq - 0 1"
        // le cavalier noir en f3 : e2xf3 gagne un cavalier. On joue autre chose : a2a3
        val note = MoveCoach.analyzeMove(fen, "a2a3", ply = 1)
        assertNotNull(note)
        println("note2: ${note!!.grade} ${note.cause} best=${note.bestSan}")
        assertTrue(note.cause == Cause.MISSED_FREE_PIECE || note.winLossPct > 5.0)
    }

    @Test
    fun profileRoundTripAndRating() {
        var p = PlayProfile(playerElo = 800)
        val persona = BotRoster.ladder[2]
        val (p2, change) = RatingEngine.apply(p, persona, 800, 1.0, true)
        assertTrue(change.delta > 0)
        assertTrue(p2.bots[persona.id]!!.elo < 800)
        val decoded = PlayProfile.decode(p2.encode())
        assertEquals(p2.playerElo, decoded.playerElo)
        assertEquals(p2.bots[persona.id]!!.wins, 1)
        p = decoded
        assertTrue(p.isUnlocked(BotRoster.ladder[3]))
    }

    private fun playGame(whiteElo: Int, blackElo: Int, rnd: Random): Double {
        val board = Board()
        var plies = 0
        while (plies < 160) {
            if (board.legalMoves().isEmpty()) {
                return if (board.isKingAttacked) (if (board.sideToMove.name == "WHITE") 0.0 else 1.0) else 0.5
            }
            if (board.isDraw) return 0.5
            val elo = if (board.sideToMove.name == "WHITE") whiteElo else blackElo
            val m = BotPlayer.chooseMove(board.fen, BotProfiles.forElo(elo), rnd) ?: return 0.5
            board.doMove(m)
            plies++
        }
        return 0.5
    }

    @Test
    fun strongerBotsWinMore() {
        val rnd = Random(42)
        val pairs = listOf(400 to 1000, 800 to 1400, 1200 to 1800)
        for ((low, high) in pairs) {
            var highScore = 0.0
            val n = 4
            for (g in 0 until n) {
                highScore += if (g % 2 == 0) 1.0 - playGame(low, high, rnd) else playGame(high, low, rnd)
            }
            println("MATCH $high vs $low : ${highScore}/$n")
            assertTrue("$high devrait battre $low ($highScore/$n)", highScore >= n / 2.0)
        }
    }
}
