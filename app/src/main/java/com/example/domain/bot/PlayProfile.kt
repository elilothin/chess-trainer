package com.example.domain.bot

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

data class BotRecord(
    val elo: Int,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0
) {
    val games: Int get() = wins + draws + losses
}

/**
 * Profil de progression du joueur : son Elo interne et l'état de chaque bot.
 * L'Elo du joueur et ceux des bots bougent ensemble : l'échelle se calibre sur toi.
 */
data class PlayProfile(
    val playerElo: Int = 800,
    val ratedGames: Int = 0,
    val bots: Map<String, BotRecord> = emptyMap(),
    val peakElo: Int = 800
) {
    val isProvisional: Boolean get() = ratedGames < 10

    fun botElo(persona: BotPersona): Int = when (persona.id) {
        BotRoster.SPARRING_ID -> (playerElo + 60).coerceIn(400, 2100)
        else -> bots[persona.id]?.elo ?: persona.baseElo
    }

    fun record(persona: BotPersona): BotRecord = bots[persona.id] ?: BotRecord(persona.baseElo)

    /** Un bot se débloque quand le précédent est battu, ou quand ton Elo est assez proche. */
    fun isUnlocked(persona: BotPersona): Boolean {
        val idx = BotRoster.ladder.indexOfFirst { it.id == persona.id }
        if (persona.id == BotRoster.SPARRING_ID || idx <= 0) return true
        val prev = BotRoster.ladder[idx - 1]
        return (bots[prev.id]?.wins ?: 0) >= 1 || playerElo >= persona.baseElo - 100
    }

    fun encode(): String {
        val botPart = bots.entries.joinToString("|") { (id, r) -> "$id:${r.elo}:${r.wins}:${r.draws}:${r.losses}" }
        return "pe=$playerElo;rg=$ratedGames;pk=$peakElo;b=$botPart"
    }

    companion object {
        fun decode(raw: String?): PlayProfile {
            if (raw.isNullOrBlank()) return PlayProfile()
            return runCatching {
                val map = raw.split(";").mapNotNull {
                    val i = it.indexOf('=')
                    if (i < 0) null else it.substring(0, i) to it.substring(i + 1)
                }.toMap()
                val bots = (map["b"] ?: "").split("|").filter { it.isNotBlank() }.mapNotNull { entry ->
                    val p = entry.split(":")
                    if (p.size != 5) null else p[0] to BotRecord(p[1].toInt(), p[2].toInt(), p[3].toInt(), p[4].toInt())
                }.toMap()
                val elo = map["pe"]?.toInt() ?: 800
                PlayProfile(
                    playerElo = elo,
                    ratedGames = map["rg"]?.toInt() ?: 0,
                    bots = bots,
                    peakElo = map["pk"]?.toInt() ?: elo
                )
            }.getOrDefault(PlayProfile())
        }
    }
}

data class RatingChange(
    val before: Int,
    val after: Int,
    val botBefore: Int,
    val botAfter: Int,
    val rated: Boolean
) {
    val delta: Int get() = after - before
}

object RatingEngine {
    fun expected(me: Int, opp: Int): Double = 1.0 / (1.0 + 10.0.pow((opp - me) / 400.0))

    /**
     * Applique le résultat d'une partie. [playerScore] : 1.0 victoire, 0.5 nulle, 0.0 défaite.
     * [botEloAtStart] : l'Elo du bot tel qu'il était pendant la partie.
     * Parties non classées (aide utilisée, coups repris) : seul l'historique W/D/L bouge.
     */
    fun apply(
        profile: PlayProfile,
        persona: BotPersona,
        botEloAtStart: Int,
        playerScore: Double,
        rated: Boolean
    ): Pair<PlayProfile, RatingChange> {
        val old = profile.record(persona)
        val record = old.copy(
            wins = old.wins + if (playerScore == 1.0) 1 else 0,
            draws = old.draws + if (playerScore == 0.5) 1 else 0,
            losses = old.losses + if (playerScore == 0.0) 1 else 0
        )
        if (!rated) {
            val updated = profile.copy(bots = profile.bots + (persona.id to record))
            return updated to RatingChange(profile.playerElo, profile.playerElo, botEloAtStart, record.elo, false)
        }

        val exp = expected(profile.playerElo, botEloAtStart)
        val k = when {
            profile.ratedGames < 10 -> 48.0
            profile.ratedGames < 30 -> 32.0
            else -> 20.0
        }
        val newPlayer = (profile.playerElo + k * (playerScore - exp)).roundToInt().coerceIn(100, 3000)

        // Le bot dérive lentement dans le sens inverse (jamais loin de son identité de départ).
        val newBot = if (persona.id == BotRoster.SPARRING_ID) {
            record.elo
        } else {
            val drift = 14.0 * ((1.0 - playerScore) - (1.0 - exp))
            (record.elo + drift).roundToInt().coerceIn(persona.baseElo - 250, persona.baseElo + 250)
        }
        val updatedRecord = record.copy(elo = newBot)
        val updated = profile.copy(
            playerElo = newPlayer,
            ratedGames = profile.ratedGames + 1,
            peakElo = maxOf(profile.peakElo, newPlayer),
            bots = profile.bots + (persona.id to updatedRecord)
        )
        return updated to RatingChange(profile.playerElo, newPlayer, botEloAtStart, updatedRecord.elo, true)
    }

    @Suppress("unused")
    fun gap(a: Int, b: Int): Int = abs(a - b)
}
