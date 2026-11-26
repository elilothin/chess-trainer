package com.example.data.content

import com.example.domain.model.LocalizedText
import com.example.domain.model.MiddlegameExercise
import com.example.domain.model.MiddlegameType
import com.example.domain.model.OpeningInfo
import com.example.domain.model.OpeningStatMove

object OpeningsAndMiddlegameContent {

    // Opening Explorer Tree: key = normalized UCI sequence joined by space ("" for root)
    val openingBook: Map<String, Pair<String, List<OpeningStatMove>>> = mapOf(
        "" to ("Position initiale" to listOf(
            OpeningStatMove("e4", "e2e4", 1_450_000, 38, 29, 33),
            OpeningStatMove("d4", "d2d4", 1_120_000, 40, 31, 29),
            OpeningStatMove("Nf3", "g1f3", 280_000, 39, 34, 27),
            OpeningStatMove("c4", "c2c4", 210_000, 40, 33, 27)
        )),
        "e2e4" to ("Début du Pion Roi (1.e4)" to listOf(
            OpeningStatMove("c5", "c7c5", 640_000, 35, 29, 36),
            OpeningStatMove("e5", "e7e5", 520_000, 36, 30, 34),
            OpeningStatMove("e6", "e7e6", 190_000, 38, 31, 31),
            OpeningStatMove("c6", "c7c6", 160_000, 37, 32, 31)
        )),
        "e2e4 e7e5" to ("Partie Ouverte (1.e4 e5)" to listOf(
            OpeningStatMove("Nf3", "g1f3", 460_000, 38, 31, 31),
            OpeningStatMove("Bc4", "f1c4", 45_000, 35, 28, 37),
            OpeningStatMove("Nc3", "b1c3", 32_000, 36, 29, 35)
        )),
        "e2e4 e7e5 g1f3" to ("Cavalier Roi (2.Cf3)" to listOf(
            OpeningStatMove("Nc6", "b8c6", 380_000, 37, 31, 32),
            OpeningStatMove("Nf6", "g8f6", 65_000, 34, 38, 28)
        )),
        "e2e4 e7e5 g1f3 b8c6" to ("Position classique (2...Cc6)" to listOf(
            OpeningStatMove("Bb5", "f1b5", 195_000, 39, 34, 27),
            OpeningStatMove("Bc4", "f1c4", 155_000, 38, 29, 33),
            OpeningStatMove("d4", "d2d4", 48_000, 37, 30, 33)
        )),
        "e2e4 e7e5 g1f3 b8c6 f1c4" to ("Ouverture Italienne (C50)" to listOf(
            OpeningStatMove("Bc5", "f8c5", 95_000, 37, 30, 33),
            OpeningStatMove("Nf6", "g8f6", 62_000, 39, 28, 33)
        )),
        "e2e4 e7e5 g1f3 b8c6 f1c4 f8c5" to ("Giuoco Piano (3...Fc5)" to listOf(
            OpeningStatMove("c3", "c2c3", 48_000, 39, 30, 31),
            OpeningStatMove("d3", "d2d3", 32_000, 37, 31, 32),
            OpeningStatMove("O-O", "e1g1", 15_000, 38, 30, 32)
        )),
        "e2e4 e7e5 g1f3 b8c6 f1b5" to ("Partie Espagnole / Ruy Lopez (C60)" to listOf(
            OpeningStatMove("a6", "a7a6", 130_000, 39, 34, 27),
            OpeningStatMove("Nf6", "g8f6", 42_000, 36, 37, 27)
        )),
        "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6" to ("Défense Morphy (3...a6)" to listOf(
            OpeningStatMove("Ba4", "b5a4", 112_000, 40, 34, 26),
            OpeningStatMove("Bxc6", "b5c6", 18_000, 35, 36, 29)
        )),
        "e2e4 c7c5" to ("Défense Sicilienne (B20)" to listOf(
            OpeningStatMove("Nf3", "g1f3", 480_000, 36, 29, 35),
            OpeningStatMove("c3", "c2c3", 68_000, 35, 31, 34),
            OpeningStatMove("Nc3", "b1c3", 55_000, 35, 29, 36)
        )),
        "e2e4 c7c5 g1f3" to ("Sicilienne Ouverte (2.Cf3)" to listOf(
            OpeningStatMove("d6", "d7d6", 240_000, 37, 29, 34),
            OpeningStatMove("Nc6", "b8c6", 135_000, 36, 28, 36),
            OpeningStatMove("e6", "e7e6", 85_000, 36, 30, 34)
        )),
        "e2e4 c7c5 g1f3 d7d6" to ("Sicilienne Najdorf / Classique (2...d6)" to listOf(
            OpeningStatMove("d4", "d2d4", 210_000, 38, 29, 33),
            OpeningStatMove("Bb5+", "f1b5", 22_000, 35, 34, 31)
        )),
        "e2e4 e7e6" to ("Défense Française (C00)" to listOf(
            OpeningStatMove("d4", "d2d4", 170_000, 39, 31, 30),
            OpeningStatMove("d3", "d2d3", 12_000, 34, 32, 34)
        )),
        "e2e4 e7e6 d2d4 d7d5" to ("Française Principale (2...d5)" to listOf(
            OpeningStatMove("Nc3", "b1c3", 78_000, 40, 31, 29),
            OpeningStatMove("Nd2", "b1d2", 49_000, 37, 34, 29),
            OpeningStatMove("e5", "e4e5", 32_000, 38, 29, 33)
        )),
        "e2e4 c7c6" to ("Défense Caro-Kann (B10)" to listOf(
            OpeningStatMove("d4", "d2d4", 138_000, 38, 32, 30),
            OpeningStatMove("Nc3", "b1c3", 16_000, 36, 31, 33)
        )),
        "d2d4" to ("Début du Pion Dame (1.d4)" to listOf(
            OpeningStatMove("Nf6", "g8f6", 510_000, 39, 32, 29),
            OpeningStatMove("d5", "d7d5", 490_000, 40, 32, 28),
            OpeningStatMove("e6", "e7e6", 85_000, 38, 31, 31)
        )),
        "d2d4 d7d5" to ("Jeu Fermé (1.d4 d5)" to listOf(
            OpeningStatMove("c4", "c2c4", 360_000, 41, 33, 26),
            OpeningStatMove("Bf4", "c1f4", 78_000, 38, 31, 31),
            OpeningStatMove("Nf3", "g1f3", 65_000, 38, 33, 29)
        )),
        "d2d4 d7d5 c2c4" to ("Gambit Dame (D06)" to listOf(
            OpeningStatMove("e6", "e7e6", 175_000, 40, 34, 26),
            OpeningStatMove("c6", "c7c6", 135_000, 39, 34, 27),
            OpeningStatMove("dxc4", "d5c4", 42_000, 42, 30, 28)
        )),
        "d2d4 g8f6" to ("Défense Indienne (1...Cf6)" to listOf(
            OpeningStatMove("c4", "c2c4", 420_000, 40, 32, 28),
            OpeningStatMove("Nf3", "g1f3", 64_000, 38, 34, 28)
        )),
        "d2d4 g8f6 c2c4 g7g6" to ("Est-Indienne / Grünfeld (2...g6)" to listOf(
            OpeningStatMove("Nc3", "b1c3", 285_000, 40, 31, 29),
            OpeningStatMove("Nf3", "g1f3", 82_000, 38, 34, 28)
        )),
        "c2c4" to ("Ouverture Anglaise (A10)" to listOf(
            OpeningStatMove("e5", "e7e5", 72_000, 38, 32, 30),
            OpeningStatMove("Nf6", "g8f6", 68_000, 40, 33, 27),
            OpeningStatMove("c5", "c7c5", 45_000, 38, 36, 26)
        ))
    )

    val classicOpenings: List<OpeningInfo> = listOf(
        OpeningInfo(
            id = "op_italian",
            eco = "C50",
            name = LocalizedText("Ouverture Italienne", "Italian Game"),
            movesSan = listOf("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5"),
            movesUci = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5"),
            keyIdeas = LocalizedText(
                "Développement rapide des pièces légères, contrôle du centre et pression directe sur le point sensible f7. Idéale pour progresser.",
                "Rapid minor-piece development, central control, and direct pressure on the weak f7 square. Ideal for steady improvement."
            ),
            trapOrTip = LocalizedText(
                "Astuce : après 3...Fc5, jouez c3 puis d4 pour bâtir un centre de pions solide, ou d3 pour une partie calme (Giuoco Pianissimo).",
                "Tip: after 3...Bc5, play c3 and d4 to build a strong pawn center, or d3 for a calm positional battle."
            )
        ),
        OpeningInfo(
            id = "op_spanish",
            eco = "C60",
            name = LocalizedText("Partie Espagnole (Ruy Lopez)", "Ruy Lopez (Spanish)"),
            movesSan = listOf("e4", "e5", "Nf3", "Nc6", "Bb5", "a6"),
            movesUci = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6"),
            keyIdeas = LocalizedText(
                "Le Fou en b5 met une pression indirecte sur le pion e5 en menaçant le cavalier défenseur c6. L'ouverture favorite des champions du monde.",
                "Bb5 pressures Black's e5 pawn indirectly by challenging its defender on c6. A lifelong favorite of world champions."
            ),
            trapOrTip = LocalizedText(
                "Attention : après 4.Fxc6 dxc6, 5.Cxe5? permet 5...Dd4! qui récupère le pion avec avantage noir.",
                "Watch out: after 4.Bxc6 dxc6, 5.Nxe5? allows 5...Qd4! winning the pawn back with a strong game."
            )
        ),
        OpeningInfo(
            id = "op_sicilian",
            eco = "B20",
            name = LocalizedText("Défense Sicilienne", "Sicilian Defense"),
            movesSan = listOf("e4", "c5", "Nf3", "d6", "d4", "cxd4"),
            movesUci = listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5d4"),
            keyIdeas = LocalizedText(
                "Les Noirs luttent immédiatement pour le centre de manière asymétrique et obtiennent la colonne semi-ouverte c pour leurs contre-attaques.",
                "Black fights for the center asymmetrically from move 1 and gains the semi-open c-file for dynamic counterplay."
            ),
            trapOrTip = LocalizedText(
                "Conseil : avec les Noirs, ne vous laissez pas distancer en développement ; terminez vite votre roque.",
                "Tip: with Black, keep pace in development and castle promptly before launching queenside play."
            )
        ),
        OpeningInfo(
            id = "op_french",
            eco = "C00",
            name = LocalizedText("Défense Française", "French Defense"),
            movesSan = listOf("e4", "e6", "d4", "d5", "Nc3", "Nf6"),
            movesUci = listOf("e2e4", "e7e6", "d2d4", "d7d5", "b1c3", "g8f6"),
            keyIdeas = LocalizedText(
                "Structure très solide. Les Noirs cèdent de l'espace au début mais contre-attaquent la chaîne de pions blanche avec ...c5 et ...f6.",
                "Rock-solid structure. Black concedes space early to strike White's pawn chain later with ...c5 and ...f6."
            ),
            trapOrTip = LocalizedText(
                "Attention au « Fou français » en c8 souvent bloqué par le pion e6 : cherchez à l'échanger ou à l'activer via b7/a6.",
                "Mind the 'French bishop' on c8 blocked behind e6—plan to trade it off or fianchetto it later."
            )
        ),
        OpeningInfo(
            id = "op_caro_kann",
            eco = "B12",
            name = LocalizedText("Défense Caro-Kann", "Caro-Kann Defense"),
            movesSan = listOf("e4", "c6", "d4", "d5", "Nc3", "dxe4"),
            movesUci = listOf("e2e4", "c7c6", "d2d4", "d7d5", "b1c3", "d5e4"),
            keyIdeas = LocalizedText(
                "Similaire à la Française mais le Fou c8 reste libre de sortir en f5 ou g4 avant de jouer ...e6 ! Structure de pions ultra-saine pour les finales.",
                "Like the French, except the c8 Bishop stays free to develop to f5 or g4 before ...e6! Rock-solid endgame pawn structure."
            ),
            trapOrTip = LocalizedText(
                "Idée clé : après 4.Cxe4 Ff5, reculez en g6 si attaqué par Cg3 — puis jouez ...Cd7 et ...Cgf6.",
                "Key idea: after 4.Nxe4 Bf5, tuck the bishop to g6 when challenged, then develop ...Nd7 and ...Ngf6."
            )
        ),
        OpeningInfo(
            id = "op_queens_gambit",
            eco = "D06",
            name = LocalizedText("Gambit Dame", "Queen's Gambit"),
            movesSan = listOf("d4", "d5", "c4", "e6", "Nc3", "Nf6"),
            movesUci = listOf("d2d4", "d7d5", "c2c4", "e7e6", "b1c3", "g8f6"),
            keyIdeas = LocalizedText(
                "Les Blancs proposent un pion latéral (c4) pour dévier le pion central d5 et prendre le contrôle du centre avec e4.",
                "White offers a wing pawn (c4) to deflect Black's d5 pawn and seize full central control with e4."
            ),
            trapOrTip = LocalizedText(
                "Ce n'est pas un vrai sacrifice : si les Noirs prennent (2...dxc4), ils ne peuvent pas garder le pion sans affaiblir leur aile dame.",
                "Not a true sacrifice: if Black takes (2...dxc4), trying to hold the pawn creates dangerous weaknesses."
            )
        ),
        OpeningInfo(
            id = "op_slav",
            eco = "D10",
            name = LocalizedText("Défense Slave", "Slav Defense"),
            movesSan = listOf("d4", "d5", "c4", "c6", "Nf3", "Nf6"),
            movesUci = listOf("d2d4", "d7d5", "c2c4", "c7c6", "g1f3", "g8f6"),
            keyIdeas = LocalizedText(
                "Les Noirs soutiennent d5 avec c6 sans enfermer leur Fou de cases blanches c8. Très populaire au plus haut niveau.",
                "Black reinforces d5 with c6 without locking in the light-squared c8 Bishop. A mainstay of world championship matches."
            ),
            trapOrTip = LocalizedText(
                "Attention : après 4.Cc3 dxc4 5.a4 Ff5!, vous contrôlez e4 et développez le Fou à l'extérieur de la chaîne.",
                "Note: after 4.Nc3 dxc4 5.a4 Bf5!, Black clamps down on e4 with active piece play."
            )
        ),
        OpeningInfo(
            id = "op_london",
            eco = "D02",
            name = LocalizedText("Système de Londres", "London System"),
            movesSan = listOf("d4", "d5", "Bf4", "Nf6", "e3", "e6"),
            movesUci = listOf("d2d4", "d7d5", "c1f4", "g8f6", "e2e3", "e7e6"),
            keyIdeas = LocalizedText(
                "Un système universel et robuste : le Fou sort en f4 avant e3, formant une pyramide de pions (c3-d4-e3) très difficile à percer.",
                "A universal, reliable system: the Bishop develops to f4 before e3, forming a solid c3-d4-e3 pawn pyramid."
            ),
            trapOrTip = LocalizedText(
                "Idéal pour éviter les pièges tactiques complexes tout en gardant des plans d'attaque sur l'aile roi (Ce5, Fd3).",
                "Great for avoiding sharp traps while keeping flexible kingside attacking plans (Ne5, Bd3)."
            )
        ),
        OpeningInfo(
            id = "op_kings_indian",
            eco = "E60",
            name = LocalizedText("Défense Est-Indienne", "King's Indian Defense"),
            movesSan = listOf("d4", "Nf6", "c4", "g6", "Nc3", "Bg7"),
            movesUci = listOf("d2d4", "g8f6", "c2c4", "g7g6", "b1c3", "f8g7"),
            keyIdeas = LocalizedText(
                "Système hypermoderne : les Noirs laissent les Blancs occuper le centre puis le dynamitent avec ...e5 ou ...c5 et une attaque féroce à l'aile roi.",
                "Hypermodern counterattack: Black lets White build a big center, then strikes back with ...e5 or ...c5 and a fierce kingside storm."
            ),
            trapOrTip = LocalizedText(
                "Quand le centre se bloque après e4-d5, lancez l'assaut de pions à l'aile roi avec ...f5 et ...f4 !",
                "Once the center locks with d5, launch your kingside pawn storm with ...f5 and ...f4!"
            )
        ),
        OpeningInfo(
            id = "op_english",
            eco = "A10",
            name = LocalizedText("Ouverture Anglaise", "English Opening"),
            movesSan = listOf("c4", "e5", "Nc3", "Nf6", "Nf3", "Nc6"),
            movesUci = listOf("c2c4", "e7e5", "b1c3", "g8f6", "g1f3", "b8c6"),
            keyIdeas = LocalizedText(
                "Une ouverture de flanc souple qui contrôle d5 à distance et prépare le fianchetto g3-Fg2 pour dominer la grande diagonale.",
                "A flexible flank opening controlling d5 from afar and often pairing with g3-Bg2 to rule the long diagonal."
            ),
            trapOrTip = LocalizedText(
                "Comme une Sicilienne inversée avec un temps d'avance ! Pensez au plan a3-b4 à l'aile dame.",
                "Like a reversed Sicilian with an extra tempo! Look for the queenside expansion plan a3-b4."
            )
        )
    )

    val middlegameExercises: List<MiddlegameExercise> = listOf(
        MiddlegameExercise(
            id = "mid_1",
            type = MiddlegameType.PRINCIPLE,
            title = LocalizedText("1. Contrôle du centre et développement", "1. Center Control & Development"),
            description = LocalizedText(
                "Avant d'attaquer, développez vos pièces légères (Cavaliers et Fous) vers le centre et mettez votre Roi à l'abri par le roque. Quel coup développe et met le Roi en sécurité ?",
                "Before launching an attack, develop minor pieces toward the center and castle early. Which move secures the King and activates the Rook?"
            ),
            initialFen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/2NP1N2/PPP2PPP/R1BQK2R w KQkq - 2 5",
            acceptedMovesUci = listOf("e1g1"),
            highlightSquares = listOf("e1", "g1"),
            explanation = LocalizedText(
                "Parfait ! Le petit roque (O-O) met le Roi en sécurité derrière ses pions et libère la Tour h1 pour la colonne e.",
                "Spot on! Kingside castling (O-O) shelters the King and brings the h1 Rook into play."
            )
        ),
        MiddlegameExercise(
            id = "mid_2",
            type = MiddlegameType.BEST_PLAN,
            title = LocalizedText("2. Trouve le meilleur plan : colonne ouverte", "2. Find the Best Plan: Open File"),
            description = LocalizedText(
                "Une colonne sans aucun pion est une autoroute pour vos Tours ! Dans cette position, placez votre Tour sur la seule colonne ouverte.",
                "A file with no pawns is a highway for your Rooks! Place your Rook on the only open file."
            ),
            initialFen = "2r2rk1/pp3ppp/2p5/4p3/4P3/2P5/PP3PPP/R4RK1 w - - 0 1",
            acceptedMovesUci = listOf("a1d1", "f1d1"),
            highlightSquares = listOf("d1"),
            explanation = LocalizedText(
                "Excellent ! La colonne d est totalement ouverte. En jouant Tad1 ou Tfd1, vous prenez le contrôle de la colonne et visez d7/d8.",
                "Excellent! The d-file is completely open. Moving a Rook to d1 seizes the open file and prepares an invasion."
            )
        ),
        MiddlegameExercise(
            id = "mid_3",
            type = MiddlegameType.IMPROVE_PIECE,
            title = LocalizedText("3. Améliore ta pièce la plus mal placée", "3. Improve Your Worst-Placed Piece"),
            description = LocalizedText(
                "Votre Cavalier en a3 est « au bord de l'échiquier » (un cavalier au bord est souvent hors jeu). Ramenez-le vers un avant-poste central fort !",
                "Your Knight on a3 is on the rim ('A knight on the rim is dim'). Reroute it toward a strong central square!"
            ),
            initialFen = "r1bq1rk1/ppp2ppp/2np1n2/4p3/2P1P3/N1P2N2/PP3PPP/R1BQ1RK1 w - - 0 1",
            acceptedMovesUci = listOf("a3c2"),
            highlightSquares = listOf("a3", "c2", "e3", "d5"),
            explanation = LocalizedText(
                "Bravo ! Cc2 prépare Ce3 puis Cd5 — un magnifique avant-poste inexpugnable au cœur du camp adverse.",
                "Great maneuver! Nc2 prepares Ne3 and eventually Nd5 — a dominant central outpost."
            )
        ),
        MiddlegameExercise(
            id = "mid_4",
            type = MiddlegameType.IMPROVE_PIECE,
            title = LocalizedText("4. Activer le Fou passif", "4. Activate the Passive Bishop"),
            description = LocalizedText(
                "Votre Fou en c1 est enfermé derrière vos pions. Placez ce Fou sur une grande diagonale active (b2 ou a3) !",
                "Your c1 Bishop is passive behind pawns. Place it on the active long diagonal b2 or a3!"
            ),
            initialFen = "r2q1rk1/ppp2ppp/2n1pn2/3p4/3P4/1P2PN2/P1P2PPP/RNBQ1RK1 w - - 0 1",
            acceptedMovesUci = listOf("c1b2", "c1a3"),
            highlightSquares = listOf("c1", "b2", "a3"),
            explanation = LocalizedText(
                "Très bien ! En b2 (fianchetto) ou a3 (sur la diagonale a3-f8), le Fou rayonne sur tout l'échiquier.",
                "Well played! On b2 or a3, the Bishop springs to life across a long open diagonal."
            )
        ),
        MiddlegameExercise(
            id = "mid_5",
            type = MiddlegameType.TYPICAL_POSITION,
            title = LocalizedText("5. Bon Fou vs Mauvais Fou", "5. Good Bishop vs Bad Bishop"),
            description = LocalizedText(
                "Un « mauvais fou » a ses propres pions bloqués sur des cases de sa couleur. Vos pions sont sur cases noires (d4, e3) et votre Fou est sur cases blanches (d3). Enfoncez le centre pour profiter de l'activité de votre bon Fou !",
                "A 'bad bishop' is blocked by its own pawns on the same color. Your pawns are on dark squares (d4, e3) leaving your light-squared bishop free! Push e4 to break open lines."
            ),
            initialFen = "2b2rk1/pp3ppp/2p1pn2/3p4/3P4/2PBPN2/PP3PPP/R4RK1 w - - 0 1",
            acceptedMovesUci = listOf("e3e4"),
            highlightSquares = listOf("e3", "e4", "d3"),
            explanation = LocalizedText(
                "Superbe compréhension positionnelle ! e4! libère votre jeu et active pleinement votre bon Fou de cases blanches contre le mauvais Fou noir en c8.",
                "Great positional sense! e4! opens the position in favor of your active light-squared bishop vs Black's passive c8 bishop."
            )
        ),
        MiddlegameExercise(
            id = "mid_6",
            type = MiddlegameType.BEST_PLAN,
            title = LocalizedText("6. Exploiter une case faible (Avant-poste)", "6. Exploit a Weak Square (Outpost)"),
            description = LocalizedText(
                "Les Noirs ont poussé c5 et e5, créant une case faible permanente en d5 qu'aucun pion noir ne peut attaquer. Installez-y votre Cavalier !",
                "Black pushed c5 and e5, leaving a permanent hole on d5 that no enemy pawn can guard. Plant your Knight on d5!"
            ),
            initialFen = "r1bq1rk1/pp3ppp/2np1n2/2p1p3/4P3/2NP1N2/PPP2PPP/R1BQ1RK1 w - - 0 1",
            acceptedMovesUci = listOf("c3d5"),
            highlightSquares = listOf("c3", "d5"),
            explanation = LocalizedText(
                "Parfait ! Un Cavalier en d5 domine tout le plateau : il ne peut pas être chassé par un pion et met une pression constante sur f6 et b6.",
                "Textbook! A Knight on d5 acts like a monster—no pawn can ever kick it away."
            )
        ),
        MiddlegameExercise(
            id = "mid_7",
            type = MiddlegameType.BEST_PLAN,
            title = LocalizedText("7. La Tour sur la 7e rangée", "7. Rook on the 7th Rank"),
            description = LocalizedText(
                "Placer une Tour sur la 7e rangée paralyse la défense adverse en attaquant la base des pions et en coupant le Roi. Envahissez la 7e rangée avec votre Tour !",
                "Placing a Rook on the 7th rank paralyzes the opponent by targeting pawns at their base and cutting off the King. Invade the 7th rank!"
            ),
            initialFen = "4r1k1/pp3ppp/8/8/8/7P/2R2PP1/6K1 w - - 0 1",
            acceptedMovesUci = listOf("c2c7"),
            highlightSquares = listOf("c2", "c7", "b7", "f7"),
            explanation = LocalizedText(
                "Exact ! Tc7! menace à la fois b7 et f7 tout en enfermant le Roi noir sur la 8e rangée.",
                "Right on target! Rc7! attacks both b7 and f7 while keeping the Black King trapped on the back rank."
            )
        ),
        MiddlegameExercise(
            id = "mid_8",
            type = MiddlegameType.TYPICAL_POSITION,
            title = LocalizedText("8. L'Attaque de Minorité", "8. The Minority Attack"),
            description = LocalizedText(
                "Dans la structure du Gambit Dame, vous avez moins de pions à l'aile dame mais ils avancent vite pour créer une faiblesse dans le camp noir. Poussez b5 pour attaquer c6 !",
                "In Queen's Gambit structures, advance your queenside minority to create structural weaknesses. Push b5 to undermine Black's c6 pawn!"
            ),
            initialFen = "4r1k1/1p3ppp/p1p5/8/1PP5/P6P/5PP1/4R1K1 w - - 0 1",
            acceptedMovesUci = listOf("b4b5"),
            highlightSquares = listOf("b4", "b5", "c6"),
            explanation = LocalizedText(
                "Excellent ! b5! force l'échange sur c6, laissant aux Noirs un pion isolé ou arriéré très vulnérable en finale.",
                "Spot on! b5! forces a structural fracture around c6, creating a permanent endgame target."
            )
        ),
        MiddlegameExercise(
            id = "mid_9",
            type = MiddlegameType.PRINCIPLE,
            title = LocalizedText("9. Prophylaxie : prévenir le plan adverse", "9. Prophylaxis: Stop Opponent's Idea"),
            description = LocalizedText(
                "Avant d'attaquer, demandez-vous toujours : « Que veut jouer mon adversaire ? » Ici les Noirs menacent ...Fg4 pour clouer votre Cavalier f3. Empêchez-le !",
                "Always ask 'What does my opponent want?' Here Black wants ...Bg4 to pin your f3 Knight. Prevent that move calmly!"
            ),
            initialFen = "r2q1rk1/1pp2ppp/p1np1n2/2b1p3/2B1P3/2NP1N2/PPP2PPP/R1BQ1RK1 w - - 0 1",
            acceptedMovesUci = listOf("h2h3"),
            highlightSquares = listOf("h2", "h3", "g4"),
            explanation = LocalizedText(
                "Brillant ! h3! est un coup prophylactique classique qui retire la case g4 au Fou noir et offre une case de fuite au Roi.",
                "Smart prophylaxis! h3! takes away g4 from Black's bishop and gives your King breathing room."
            )
        )
    )
}
