package com.example.domain.bot

data class GameSummary(
    val accuracy: Int,
    val counts: Map<Grade, Int>,
    /** Pires coups, du plus coûteux au moins coûteux (au plus 5). */
    val keyMoments: List<MoveNote>,
    /** Causes d'erreur les plus fréquentes. */
    val habits: List<Cause>
)

object ReviewBuilder {

    fun summarize(notes: List<MoveNote>): GameSummary {
        if (notes.isEmpty()) return GameSummary(0, emptyMap(), emptyList(), emptyList())
        val acc = notes.map { MoveCoach.moveAccuracy(it.winLossPct) }.average().toInt()
        val counts = notes.groupingBy { it.grade }.eachCount()
        val key = notes.filter { it.grade >= Grade.MISTAKE }.sortedByDescending { it.winLossPct }.take(5)
        val habits = notes
            .filter { it.grade >= Grade.INACCURACY && it.cause != Cause.NONE && it.cause != Cause.GENERIC }
            .groupingBy { it.cause }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { it.key }
            .take(3)
        return GameSummary(acc, counts, key, habits)
    }

    /** [outcome] : 1 victoire, 0 nulle, -1 défaite (du point de vue du joueur). */
    fun verdict(accuracy: Int, outcome: Int, isEn: Boolean): String {
        val base = when {
            accuracy >= 90 -> if (isEn) "Clean game. You play like someone who thinks. Keep it up."
            else "Partie propre. Tu joues comme quelqu'un qui réfléchit. Continue."
            accuracy >= 75 -> if (isEn) "Not bad, but you had lapses. That's exactly where you lose points."
            else "Pas mal, mais tu as eu des moments d'inattention. C'est exactement là que tu perds des points."
            accuracy >= 55 -> if (isEn) "Too many avoidable errors. You're playing on instinct. Slow down."
            else "Trop d'erreurs évitables. Tu joues à l'instinct. Ralentis."
            accuracy >= 35 -> if (isEn) "You're playing without looking. The level is there, the discipline isn't."
            else "Tu joues sans regarder. Le niveau est là, la discipline non."
            else -> if (isEn) "Disaster. It's not a talent problem, it's an attention problem. One move, one check. That's all we ask."
            else "Catastrophe. Ce n'est pas un problème de talent, c'est un problème d'attention. Un coup, une vérification. C'est tout ce qu'on te demande."
        }
        val tail = when (outcome) {
            1 -> if (isEn) " You won, but don't fool yourself about how." else " Tu as gagné, mais ne te mens pas sur la manière."
            -1 -> if (isEn) " The loss isn't the problem. Repeating the same mistakes is." else " La défaite n'est pas le problème. Refaire les mêmes erreurs, si."
            else -> ""
        }
        return base + tail
    }
}
