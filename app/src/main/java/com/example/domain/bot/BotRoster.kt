package com.example.domain.bot

/** Un adversaire de l'échelle. L'Elo réel d'un bot dérive avec les résultats (voir [PlayProfile]). */
data class BotPersona(
    val id: String,
    val nameFr: String,
    val nameEn: String,
    val baseElo: Int,
    val styleFr: String,
    val styleEn: String,
    val bioFr: String,
    val bioEn: String,
    val aggression: Int
)

object BotRoster {
    const val SPARRING_ID = "sparring"

    val ladder: List<BotPersona> = listOf(
        BotPersona("stagiaire", "Le Stagiaire", "The Intern", 400, "Distrait", "Distracted",
            "Il bouge des pièces. Parfois il en perd. Parfois il en offre.", "He moves pieces. Sometimes he loses them. Sometimes he gives them away.", 0),
        BotPersona("ama", "Tantine Ama", "Auntie Ama", 600, "Prudente", "Cautious",
            "Elle ne prend aucun risque, mais regarde mal les menaces.", "Takes no risks, but misses threats.", 0),
        BotPersona("kodjo", "Kodjo le Rapide", "Quick Kodjo", 800, "Impulsif", "Impulsive",
            "Il joue vite, il attaque vite, il se fait punir vite.", "Plays fast, attacks fast, gets punished fast.", 6),
        BotPersona("yao", "Capitaine Yao", "Captain Yao", 1000, "Solide", "Solid",
            "Il connaît les bases. Si tu laisses une pièce, il la prend.", "Knows the basics. Leave a piece and he takes it.", 0),
        BotPersona("mensah", "Dr. Mensah", "Dr. Mensah", 1200, "Positionnel", "Positional",
            "Calme, méthodique. Il gagne en te laissant te piéger tout seul.", "Calm, methodical. He wins by letting you trap yourself.", 0),
        BotPersona("adjoa", "La Reine Adjoa", "Queen Adjoa", 1400, "Agressive", "Aggressive",
            "Elle va droit sur ton roi. Mal défendu, tu es fini.", "Goes straight for your king. Poorly defended, you're done.", 8),
        BotPersona("ren", "Sensei Ren", "Sensei Ren", 1600, "Tactique", "Tactical",
            "Il voit les fourchettes avant toi. Beaucoup avant toi.", "Sees forks before you do. Way before.", 3),
        BotPersona("koffi", "Commandant Koffi", "Commander Koffi", 1800, "Impitoyable", "Ruthless",
            "Pas d'erreur gratuite. Chaque faiblesse est punie.", "No free mistakes. Every weakness gets punished.", 2),
        BotPersona("zero", "Maître Zéro", "Master Zero", 2000, "Froid", "Cold",
            "Calcul précis. Il attend ta faute, et elle vient toujours.", "Precise calculation. He waits for your slip, and it always comes.", 0)
    )

    val sparring = BotPersona(
        SPARRING_ID, "Sparring", "Sparring", 800, "Adaptatif", "Adaptive",
        "Toujours un peu au-dessus de ton niveau réel. Il monte avec toi.",
        "Always slightly above your real level. He climbs with you.", 2
    )

    fun byId(id: String): BotPersona? = if (id == SPARRING_ID) sparring else ladder.firstOrNull { it.id == id }
}
