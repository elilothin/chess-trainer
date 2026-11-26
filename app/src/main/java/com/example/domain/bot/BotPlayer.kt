package com.example.domain.bot

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/** Réglages de force pour un Elo donné. */
data class BotProfile(
    val elo: Int,
    val depth: Int,
    val timeMs: Long,
    val quiescence: Boolean,
    /** Écart-type (cp) du bruit ajouté aux scores des coups : simule l'imprécision humaine. */
    val noiseCp: Int,
    /** Probabilité de jouer un coup "au hasard" parmi les meilleurs 60 %. */
    val blunderChance: Double,
    val aggression: Int = 0
)

object BotProfiles {
    private data class Anchor(
        val elo: Int, val depth: Int, val timeMs: Long, val q: Boolean, val noise: Int, val blunder: Double
    )

    private val anchors = listOf(
        Anchor(400, 1, 60, false, 230, 0.30),
        Anchor(600, 1, 80, false, 170, 0.18),
        Anchor(800, 2, 150, false, 125, 0.10),
        Anchor(1000, 2, 220, true, 90, 0.06),
        Anchor(1200, 3, 350, true, 60, 0.03),
        Anchor(1400, 3, 500, true, 40, 0.012),
        Anchor(1600, 4, 800, true, 25, 0.0),
        Anchor(1800, 4, 1100, true, 12, 0.0),
        Anchor(2000, 5, 1600, true, 5, 0.0),
        Anchor(2200, 6, 2200, true, 0, 0.0)
    )

    fun forElo(elo: Int, aggression: Int = 0): BotProfile {
        val e = elo.coerceIn(anchors.first().elo, anchors.last().elo)
        val upperIdx = anchors.indexOfFirst { it.elo >= e }.let { if (it < 0) anchors.lastIndex else it }
        val lower = anchors[(upperIdx - 1).coerceAtLeast(0)]
        val upper = anchors[upperIdx]
        val t = if (upper.elo == lower.elo) 0.0 else (e - lower.elo).toDouble() / (upper.elo - lower.elo)
        val base = if (t < 0.5) lower else upper
        return BotProfile(
            elo = elo,
            depth = base.depth,
            timeMs = base.timeMs,
            quiescence = base.q,
            noiseCp = (lower.noise + (upper.noise - lower.noise) * t).toInt(),
            blunderChance = lower.blunder + (upper.blunder - lower.blunder) * t,
            aggression = aggression
        )
    }
}

object BotPlayer {

    private fun gaussian(rnd: Random): Double {
        val u1 = rnd.nextDouble().coerceAtLeast(1e-9)
        val u2 = rnd.nextDouble()
        return sqrt(-2.0 * ln(u1)) * cos(2.0 * Math.PI * u2)
    }

    /**
     * Choisit un coup pour le camp au trait de [fen] selon [profile].
     * Bas niveau : tous les coups sont notés puis bruités (comme un humain qui "voit" mal).
     * Haut niveau : recherche à temps limité, avec un peu de variété dans l'ouverture.
     */
    fun chooseMove(fen: String, profile: BotProfile, rnd: Random = Random.Default): Move? {
        val board = Board()
        board.loadFromFen(fen)
        val legal = board.legalMoves()
        if (legal.isEmpty()) return null
        if (legal.size == 1) return legal[0]

        val side: Side = board.sideToMove
        val searcher = Searcher(profile.quiescence, side, profile.aggression)
        val opening = board.moveCounter <= 6

        val needsFullScoring = profile.elo < 1500 || (opening && profile.elo < 2100)
        if (needsFullScoring) {
            val depth = if (opening && profile.elo >= 1500) minOf(profile.depth, 3) else profile.depth
            val noise = if (opening && profile.elo >= 1500) maxOf(profile.noiseCp, 18) else profile.noiseCp
            val res = searcher.scoreAllRootMoves(board, depth, profile.timeMs + 400)
            val scored = res.rootScores
            if (scored.isEmpty()) return res.best ?: legal.random(rnd)

            // Un gain de mat ne doit jamais être raté par bruit.
            val mate = scored.filter { it.score >= MATE_THRESHOLD }.maxByOrNull { it.score }
            if (mate != null) return mate.move

            if (profile.blunderChance > 0.0 && rnd.nextDouble() < profile.blunderChance) {
                val sorted = scored.sortedByDescending { it.score }
                val pool = sorted.take(maxOf(2, (sorted.size * 0.6).toInt()))
                return pool.random(rnd).move
            }
            if (noise <= 0) return res.best
            return scored.maxByOrNull { it.score + gaussian(rnd) * noise }?.move ?: res.best
        }

        val result = searcher.search(board, profile.depth, profile.timeMs)
        return result.best ?: legal.random(rnd)
    }
}
