package com.example.domain.bot

/**
 * La voix du coach : directe, sans filtre, mais toujours avec la raison et une habitude à prendre.
 * Le but n'est pas de vexer : c'est de te forcer à réfléchir avant de jouer.
 */
object CoachVoice {

    private fun pieceName(key: String?, isEn: Boolean): String = when (key) {
        "N" -> if (isEn) "knight" else "cavalier"
        "B" -> if (isEn) "bishop" else "fou"
        "R" -> if (isEn) "rook" else "tour"
        "Q" -> if (isEn) "queen" else "dame"
        "K" -> if (isEn) "king" else "roi"
        else -> if (isEn) "pawn" else "pion"
    }

    private fun <T> pick(list: List<T>, seed: Int): T = list[Math.floorMod(seed, list.size)]

    fun gradeLabel(g: Grade, isEn: Boolean): String = when (g) {
        Grade.BEST -> if (isEn) "Best" else "Excellent"
        Grade.GOOD -> if (isEn) "Good" else "Correct"
        Grade.INACCURACY -> if (isEn) "Inaccuracy" else "Imprécis"
        Grade.MISTAKE -> if (isEn) "Mistake" else "Erreur"
        Grade.BLUNDER -> if (isEn) "Blunder" else "Gaffe"
    }

    fun line(n: MoveNote, isEn: Boolean): String {
        val p = pieceName(n.pieceKey, isEn)
        val v = pieceName(n.victimKey, isEn)
        val sq = n.square ?: ""
        val reply = n.replySan ?: "?"
        val best = n.bestSan ?: "?"
        val s = n.ply

        return when (n.cause) {
            Cause.NONE -> when (n.grade) {
                Grade.BEST -> pick(
                    if (isEn) listOf("Clean.", "That's the move.", "Good. Keep that discipline.")
                    else listOf("Propre.", "C'est le bon coup.", "Bien. Garde cette discipline."), s
                )
                else -> pick(
                    if (isEn) listOf("Fine.", "Acceptable.") else listOf("Correct.", "Acceptable."), s
                )
            }

            Cause.HUNG_PIECE -> pick(
                if (isEn) listOf(
                    "Your $p on $sq is hanging and you walk away from it. $reply and it's gone. Before EVERY move: what can my opponent capture?",
                    "You just left your $p for free. $reply. Check every unprotected piece before you touch anything.",
                    "Wake up: $reply wins your $p. An unprotected piece is a lost piece."
                ) else listOf(
                    "Ta $p en $sq est en prise et tu la laisses là. $reply et elle disparaît. Avant CHAQUE coup : qu'est-ce que l'adversaire peut prendre ?",
                    "Tu viens d'offrir ta $p. $reply. Vérifie toutes tes pièces non protégées avant de toucher quoi que ce soit.",
                    "Réveille-toi : $reply gagne ta $p. Une pièce non protégée est une pièce perdue."
                ), s
            )

            Cause.MOVED_INTO_CAPTURE -> pick(
                if (isEn) listOf(
                    "You send your $p to $sq... where it gets taken ($reply). Did you even look at who controls that square?",
                    "$sq is covered. Your $p dies there ($reply). Count the attackers before you move."
                ) else listOf(
                    "Tu envoies ta $p en $sq... où elle se fait prendre ($reply). Tu as regardé qui contrôle cette case ?",
                    "$sq est contrôlée. Ta $p y meurt ($reply). Compte les attaquants avant de bouger."
                ), s
            )

            Cause.BAD_TRADE -> if (isEn)
                "You take with your $p but it gets recaptured ($reply): $p for a $v. Count attackers and defenders BEFORE capturing."
            else
                "Tu prends avec ta $p mais elle est reprise ($reply) : une $p contre un(e) $v. Compte attaquants et défenseurs AVANT de capturer."

            Cause.ALLOWED_MATE -> if (isEn)
                "Mate in ${n.mateIn} for your opponent ($reply). Your king was exposed and you saw nothing. Every move: checks, captures, threats, in that order."
            else
                "Mat en ${n.mateIn} pour l'adversaire ($reply). Ton roi était à découvert et tu n'as rien vu. À chaque coup : échecs, captures, menaces, dans cet ordre."

            Cause.MISSED_MATE -> if (isEn)
                "You had mate in ${n.mateIn} with $best and walked past it. Always look at checks first."
            else
                "Tu avais mat en ${n.mateIn} avec $best et tu es passé à côté. Regarde TOUJOURS les échecs en premier."

            Cause.MISSED_FREE_PIECE -> if (isEn)
                "A free $v on $sq with $best and you ignored it. Scan captures before you play a quiet move."
            else
                "Un(e) $v gratuit(e) en $sq avec $best et tu l'ignores. Passe en revue les captures avant de jouer un coup tranquille."

            Cause.EARLY_QUEEN -> if (isEn)
                "Queen out already? She'll get chased and you'll lose tempo. Develop knights and bishops first."
            else
                "La dame déjà sortie ? Elle va se faire chasser et tu perds des temps. Développe cavaliers et fous d'abord."

            Cause.EARLY_KING -> if (isEn)
                "Why move your king this early? Castle. That's it. Best was $best."
            else
                "Pourquoi bouger ton roi si tôt ? Roque, point. Le bon coup était $best."

            Cause.THROWN_ADVANTAGE -> if (isEn)
                "You were winning and you just threw it away. When you're ahead: protect, trade, no heroics. Best was $best."
            else
                "Tu étais gagnant et tu viens de tout balancer. Quand tu es devant : protège, échange, pas d'exploit. Le bon coup était $best."

            Cause.GENERIC -> when (n.grade) {
                Grade.INACCURACY -> if (isEn) "Imprecise. $best was sharper. Why?" else "Imprécis. $best était plus précis. Pourquoi ?"
                Grade.MISTAKE -> if (isEn)
                    "Mistake. You let the advantage slip. The engine wanted $best. Ask yourself why before reading on."
                else
                    "Erreur. Tu laisses filer l'avantage. Le moteur voulait $best. Demande-toi pourquoi avant de continuer."
                else -> if (isEn)
                    "Blunder. $best was necessary. You're playing too fast: slow down."
                else
                    "Gaffe. $best était nécessaire. Tu joues trop vite : ralentis."
            }
        }
    }

    /** Question à se poser, associée à la cause : l'habitude à installer. */
    fun habit(cause: Cause, isEn: Boolean): String = when (cause) {
        Cause.HUNG_PIECE, Cause.MOVED_INTO_CAPTURE, Cause.BAD_TRADE ->
            if (isEn) "Before every move, say out loud: \"what can he take?\"" else "Avant chaque coup, dis à voix haute : « qu'est-ce qu'il peut prendre ? »"
        Cause.ALLOWED_MATE ->
            if (isEn) "Look at your king's safety every 3 moves: open files, diagonals, missing pawns." else "Regarde la sécurité de ton roi tous les 3 coups : colonnes ouvertes, diagonales, pions manquants."
        Cause.MISSED_MATE, Cause.MISSED_FREE_PIECE ->
            if (isEn) "Checks, captures, threats. In that order. Every single move." else "Échecs, captures, menaces. Dans cet ordre. À chaque coup."
        Cause.EARLY_QUEEN, Cause.EARLY_KING ->
            if (isEn) "Opening: develop, castle, then think." else "Ouverture : développe, roque, puis réfléchis."
        Cause.THROWN_ADVANTAGE ->
            if (isEn) "Ahead? Simplify and protect. Stop looking for the knockout." else "Devant ? Simplifie et protège. Arrête de chercher le KO."
        else ->
            if (isEn) "Slow down: find two candidate moves before you decide." else "Ralentis : trouve deux coups candidats avant de décider."
    }
}
