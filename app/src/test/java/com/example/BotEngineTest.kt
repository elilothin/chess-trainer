package com.example

import com.example.domain.bot.BotPlayer
import com.example.domain.bot.BotProfiles
import com.example.domain.bot.BotRoster
import com.example.domain.bot.Cause
import com.example.domain.bot.Grade
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
        val fen = "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
        val note = MoveCoach.analyzeMove(fen, "d1g4", ply = 1)
        assertNotNull(note)
        println("note: ${note!!.grade} ${note.cause} ${note.san} best=${note.bestSan}")
        assertTrue(note.grade == Grade.BLUNDER)
    }

    @Test
    fun coachFlagsMovedIntoCapture() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/7n/PPPPPPPP/RNBQKB1R w KQkq - 0 1"
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

class OnlineRoomTest {
    private class Fake : com.example.domain.online.RoomTransport {
        val store = HashMap<String, String>()
        override suspend fun get(path: String): String? {
            // reconstruit l'objet de la salle à partir des champs écrits séparément
            val prefix = "$path/"
            val whole = store[path]
            val fields = com.example.domain.online.FlatJson.decode(whole)?.toMutableMap() ?: LinkedHashMap()
            store.filterKeys { it.startsWith(prefix) }.forEach { (k, v) ->
                fields[k.removePrefix(prefix)] = com.example.domain.online.FlatJson.decode("{\"x\":$v}")!!["x"]!!
            }
            return if (whole == null && fields.isEmpty()) "null" else com.example.domain.online.FlatJson.encode(fields)
        }
        override suspend fun put(path: String, json: String) { store[path] = json }
    }

    @org.junit.Test
    fun fullRoomFlow() = kotlinx.coroutines.runBlocking {
        val room = com.example.domain.online.OnlineRoom(Fake())
        org.junit.Assert.assertEquals(com.example.domain.online.JoinResult.NOT_FOUND, room.join("ABCDE", "bob"))
        room.create("ABCDE", "alice")
        org.junit.Assert.assertEquals("waiting", room.poll("ABCDE")!!.status)
        org.junit.Assert.assertEquals(com.example.domain.online.JoinResult.OK, room.join("ABCDE", "bob"))
        org.junit.Assert.assertEquals(com.example.domain.online.JoinResult.FULL, room.join("ABCDE", "carol"))
        room.pushMoves("ABCDE", listOf("e2e4", "e7e5"))
        val snap = room.poll("ABCDE")!!
        org.junit.Assert.assertEquals(listOf("e2e4", "e7e5"), snap.moves)
        org.junit.Assert.assertEquals("playing", snap.status)
        org.junit.Assert.assertEquals("bob", snap.black)
        room.finish("ABCDE", "1-0")
        val end = room.poll("ABCDE")!!
        org.junit.Assert.assertEquals("over", end.status)
        org.junit.Assert.assertEquals("1-0", end.result)
        println("note3: online flow ok")
    }

    @org.junit.Test
    fun helpers() {
        org.junit.Assert.assertEquals("ABCDE", com.example.domain.online.OnlineRoom.normalizeCode(" abcde "))
        org.junit.Assert.assertEquals(5, com.example.domain.online.OnlineRoom.newCode().length)
        org.junit.Assert.assertEquals("https://x.firebaseio.com", com.example.domain.online.OnlineRoom.normalizeDbUrl("https://x.firebaseio.com/"))
        org.junit.Assert.assertNull(com.example.domain.online.OnlineRoom.normalizeDbUrl("http://x"))
        val m = com.example.domain.online.FlatJson.decode("{\"a\":\"x y\",\"b\":12,\"c\":null}")!!
        org.junit.Assert.assertEquals("x y", m["a"]); org.junit.Assert.assertEquals("12", m["b"]); org.junit.Assert.assertEquals("", m["c"])
    }
}

class HistoricalGamesTest {
    @org.junit.Test
    fun extraGamesAreLegal() {
        val expectedPlies = mapOf(
            "hist_fischer_spassky_g6" to 81, "hist_anderssen_kieseritzky" to 45,
            "hist_anand_carlsen_2013_g4" to 127, "hist_carlsen_caruana_2018_g11" to 110,
            "hist_karjakin_carlsen_2016_g2" to 65
        )
        for (g in com.example.data.content.ExtraHistoricalGames.games) {
            val parsed = com.example.domain.chess.PgnParser.parse(g.pgn)
            org.junit.Assert.assertEquals("plies of ${g.id}", expectedPlies[g.id], parsed.moves.size)
            g.commentsByPly.keys.forEach { org.junit.Assert.assertTrue("comment ply ${g.id}:$it", it in 1..parsed.moves.size) }
            println("note4: ${g.id} ok ${parsed.moves.size}")
        }
    }
}
