package com.example.data.content

import com.example.domain.model.EndgameCategory
import com.example.domain.model.EndgameModuleItem
import com.example.domain.model.HistoricalGame
import com.example.domain.model.LocalizedText
import com.example.domain.model.QuizCategory
import com.example.domain.model.QuizQuestion

object EndgamesHistoryQuizContent {

    val endgameItems: List<EndgameModuleItem> = listOf(
        EndgameModuleItem(
            id = "end_lesson_kq",
            category = EndgameCategory.LESSON,
            title = LocalizedText("Mat Roi + Dame vs Roi", "King + Queen vs King Mate"),
            concept = LocalizedText(
                "Technique : poussez le Roi adverse au bord grâce à la Dame (à distance de cavalier pour éviter le pat !), puis amenez votre Roi en face pour mater.",
                "Technique: shepherd the enemy King to the edge with the Queen (at a knight's distance to avoid stalemate), then bring your King close to deliver mate."
            ),
            initialFen = "7k/5Q2/5K2/8/8/8/8/8 w - - 0 1",
            expectedMovesUci = listOf("f7g7"),
            targetResult = "WIN",
            tip = LocalizedText("Votre Roi en f6 soutient la case g7 : jouez Dg7# !", "Your King on f6 guards g7: play Qg7#!")
        ),
        EndgameModuleItem(
            id = "end_lesson_kr",
            category = EndgameCategory.LESSON,
            title = LocalizedText("Mat Roi + Tour vs Roi", "King + Rook vs King Mate"),
            concept = LocalizedText(
                "Technique de la boîte : la Tour coupe le Roi adverse rangée par rangée tandis que votre Roi prend l'opposition. Attention à ne pas laisser la Tour sans protection.",
                "Box technique: the Rook cuts off the enemy King rank by rank while your King takes the opposition to force mate on the edge."
            ),
            initialFen = "6k1/8/6K1/8/8/8/8/R7 w - - 0 1",
            expectedMovesUci = listOf("a1a8"),
            targetResult = "WIN",
            tip = LocalizedText("Les deux Rois sont en opposition (g6 vs g8) : Ta8# donne le mat !", "The Kings are in opposition (g6 vs g8): Ra8# is checkmate!")
        ),
        EndgameModuleItem(
            id = "end_lesson_kp",
            category = EndgameCategory.LESSON,
            title = LocalizedText("Finale Roi + Pion vs Roi : l'Opposition", "King + Pawn vs King: Opposition"),
            concept = LocalizedText(
                "Quand votre Roi est DEVANT le pion et prend l'opposition (face à face avec une case d'écart), le Roi adverse doit céder le passage pour la promotion.",
                "When your King stands IN FRONT of your pawn and takes the opposition, the defending King must step aside to let the pawn promote."
            ),
            initialFen = "4k3/8/4K3/4P3/8/8/8/8 w - - 0 1",
            expectedMovesUci = listOf("e6d6", "e6f6"),
            targetResult = "WIN",
            tip = LocalizedText("Ne poussez pas encore e6 (ça ferait pat après ...Rf8) ! Contournons avec Rd6 ou Rf6 pour contrôler e7.", "Don't push e6 yet! Step to d6 or f6 to outflank and control e7.")
        ),
        EndgameModuleItem(
            id = "end_lesson_lucena",
            category = EndgameCategory.LESSON,
            title = LocalizedText("Position de Lucena (Construire le Pont)", "Lucena Position (Building the Bridge)"),
            concept = LocalizedText(
                "Votre pion est en 7e rangée et votre Roi bloque devant lui (e8). Pour gagner, sortez la Tour en 4e rangée (Tf4) afin de parer les échecs verticaux (« construire le pont »).",
                "Your pawn is on the 7th rank with your King in front of it. Lift the Rook to the 4th rank (Rf4) to shield your King from checks—building the bridge!"
            ),
            initialFen = "4K3/4P1k1/8/8/8/8/5R2/3r4 w - - 0 1",
            expectedMovesUci = listOf("f2f4"),
            targetResult = "WIN",
            tip = LocalizedText("Placez la Tour blanche sur la 4e rangée : Tf4! puis le Roi pourra sortir en d7/c6 et parer les échecs avec Td4.", "Lift the Rook to the 4th rank with Rf4! This creates a shield for your King.")
        ),
        EndgameModuleItem(
            id = "end_lesson_philidor",
            category = EndgameCategory.LESSON,
            title = LocalizedText("Position de Philidor (Défense passive → active)", "Philidor Position (3rd Rank Defense)"),
            concept = LocalizedText(
                "En infériorité d'un pion, gardez la Tour sur votre 3e rangée (6e rangée pour les Noirs) pour empêcher le Roi ennemi de passer. Dès que le pion avance sur cette rangée, passez derrière pour enchaîner les échecs !",
                "Defending down a pawn: keep your Rook on the 3rd rank (6th for Black) to block the enemy King. Once the pawn advances onto that rank, drop behind for perpetual checks!"
            ),
            initialFen = "4k3/8/3PK3/8/8/8/8/r4R2 b - - 0 1",
            expectedMovesUci = listOf("a1a6"),
            targetResult = "DRAW",
            tip = LocalizedText("Avec les Noirs, jouez Ta6! sur la 6e rangée pour couper le Roi blanc.", "With Black, play Ra6! on the 6th rank to stop the White King from advancing.")
        ),
        EndgameModuleItem(
            id = "end_lesson_square",
            category = EndgameCategory.LESSON,
            title = LocalizedText("Règle du Carré (Course de Pions)", "Rule of the Square (Pawn Race)"),
            concept = LocalizedText(
                "Tracez un carré imaginaire de la case du pion jusqu'à sa case de promotion. Si le Roi adverse ne peut pas entrer dans ce carré, votre pion file à Dame tout seul !",
                "Draw an imaginary square from the pawn to its promotion rank. If the enemy King cannot step inside that square, the pawn promotes unassisted!"
            ),
            initialFen = "8/8/8/3P4/8/8/6k1/4K3 w - - 0 1",
            expectedMovesUci = listOf("d5d6"),
            targetResult = "WIN",
            tip = LocalizedText("Avancez en d6! Le Roi noir en g2 reste hors du carré d6-d8-f8-f6.", "Push d6! Black's King on g2 stays outside the d6-d8-f8-f6 square.")
        ),
        EndgameModuleItem(
            id = "end_lesson_diag_opp",
            category = EndgameCategory.LESSON,
            title = LocalizedText("L'Opposition Diagonale", "Diagonal Opposition"),
            concept = LocalizedText(
                "L'opposition fonctionne aussi en diagonale (2 cases d'écart en diagonale). Elle permet de contourner le Roi défenseur pour prendre l'opposition directe.",
                "Opposition also works diagonally (one empty diagonal square between the Kings). Use it to outflank the defender!"
            ),
            initialFen = "8/4k3/8/8/2K5/3P4/8/8 w - - 0 1",
            expectedMovesUci = listOf("c4c5"),
            targetResult = "WIN",
            tip = LocalizedText("Jouez Rc5! pour placer les deux Rois en opposition diagonale (c5 vs e7).", "Play Kc5! to place both Kings in diagonal opposition (c5 vs e7).")
        ),

        // Live Trainer vs Engine
        EndgameModuleItem(
            id = "end_train_kq",
            category = EndgameCategory.TRAINER,
            title = LocalizedText("Entraîneur : Mater avec Roi + Dame", "Trainer: Mate with K+Q vs K"),
            concept = LocalizedText(
                "Position gagnante à convertir contre l'IA ! Attention au pat quand le Roi noir n'a presque plus de cases.",
                "Winning position to convert against the AI! Watch out for stalemate near the corner."
            ),
            initialFen = "8/8/3k4/8/8/8/4Q3/4K3 w - - 0 1",
            targetResult = "WIN",
            tip = LocalizedText("Réduisez l'espace du Roi noir avec la Dame puis approchez votre Roi.", "Shrink the Black King's box with your Queen, then walk your King in.")
        ),
        EndgameModuleItem(
            id = "end_train_kr",
            category = EndgameCategory.TRAINER,
            title = LocalizedText("Entraîneur : Mater avec Roi + Tour", "Trainer: Mate with K+R vs K"),
            concept = LocalizedText(
                "Exercez-vous à forcer le Roi adverse sur le bord et à livrer le mat en moins de 30 coups.",
                "Practice driving the lone King to the edge and delivering checkmate."
            ),
            initialFen = "8/8/4k3/8/8/8/R7/4K3 w - - 0 1",
            targetResult = "WIN",
            tip = LocalizedText("Utilisez des coups d'attente de Tour pour prendre l'opposition avec votre Roi.", "Use Rook waiting moves to seize the opposition with your King.")
        ),
        EndgameModuleItem(
            id = "end_train_kp",
            category = EndgameCategory.TRAINER,
            title = LocalizedText("Entraîneur : Promouvoir Roi + Pion", "Trainer: Promote King + Pawn"),
            concept = LocalizedText(
                "Votre Roi est devant votre pion : gardez l'opposition et menez le pion à Dame !",
                "Your King leads the pawn: keep the opposition and promote to win!"
            ),
            initialFen = "8/4k3/8/4K3/4P3/8/8/8 w - - 0 1",
            targetResult = "WIN",
            tip = LocalizedText("Avancez le Roi avant le pion autant que possible !", "Lead with your King in front of the pawn!")
        )
    )

    val historicalGames: List<HistoricalGame> = listOf(
        HistoricalGame(
            id = "hist_morphy_opera",
            title = LocalizedText("La Partie de l'Opéra", "The Opera Game"),
            whitePlayer = "Paul Morphy",
            blackPlayer = "Duc de Brunswick & Comte Isouard",
            year = 1858,
            event = "Opéra de Paris",
            result = "1-0",
            pgn = """
                [Event "Opéra de Paris"]
                [White "Paul Morphy"]
                [Black "Duc de Brunswick & Comte Isouard"]
                [Result "1-0"]

                1. e4 e5 2. Nf3 d6 3. d4 Bg4 4. dxe5 Bxf3 5. Qxf3 dxe5 6. Bc4 Nf6
                7. Qb3 Qe7 8. Nc3 c6 9. Bg5 b5 10. Nxb5 cxb5 11. Bxb5+ Nbd7
                12. O-O-O Rd8 13. Rxd7 Rxd7 14. Rd1 Qe6 15. Bxd7+ Nxd7 16. Qb8+ Nxb8 17. Rd8# 1-0
            """.trimIndent(),
            summary = LocalizedText(
                "Le modèle absolu de développement rapide et de sacrifice pour ouvrir les lignes vers le Roi adverse.",
                "The ultimate blueprint for rapid development and sacrificing material to open lines against an uncastled King."
            ),
            commentsByPly = mapOf(
                1 to LocalizedText("Morphy ouvre classiquement par 1.e4.", "Morphy opens classically with 1.e4."),
                6 to LocalizedText("3...Fg4?! donne un temps et permet aux Blancs de prendre l'initiative.", "3...Bg4?! loses time and lets White seize the initiative."),
                11 to LocalizedText("6.Fc4 menace déjà mat en f7 !", "6.Bc4 already threatens mate on f7!"),
                19 to LocalizedText("10.Cxb5!! Morphy sacrifie un cavalier pour garder le Roi noir bloqué au centre.", "10.Nxb5!! Morphy sacrifices a knight to keep Black's King trapped in the center."),
                23 to LocalizedText("12.O-O-O! Chaque pièce blanche attaque ; les Noirs ne peuvent pas roquer.", "12.O-O-O! Every White piece joins the attack; Black cannot castle."),
                25 to LocalizedText("13.Txd7!! Sacrifice de qualité pour maintenir le clouage mortel.", "13.Rxd7!! An exchange sacrifice maintaining the deadly pin."),
                31 to LocalizedText("16.Db8+!! Le clou d'or : sacrifice de Dame qui attire le Cavalier loin de d8.", "16.Qb8+!! The crowning jewel: a Queen sacrifice deflecting the Knight."),
                33 to LocalizedText("17.Td8# ! Échec et mat avec Tour + Fou. Immortel !", "17.Rd8#! Checkmate with Rook and Bishop. Timeless!")
            )
        ),
        HistoricalGame(
            id = "hist_capablanca_tartakover",
            title = LocalizedText("Chef-d'œuvre de finale (Capablanca)", "Capablanca's Endgame Masterpiece"),
            whitePlayer = "José Raúl Capablanca",
            blackPlayer = "Savielly Tartakower",
            year = 1924,
            event = "New York 1924",
            result = "1-0",
            pgn = """
                [Event "New York 1924"]
                [White "José Raúl Capablanca"]
                [Black "Savielly Tartakower"]
                [Result "1-0"]

                1. d4 f5 2. Nf3 e6 3. c4 Nf6 4. Bg5 Be7 5. Nc3 O-O 6. e3 b6 7. Bd3 Bb7
                8. O-O Qe8 9. Qe2 Ne4 10. Bxe7 Nxc3 11. bxc3 Qxe7 12. a4 Bxf3 13. Qxf3 Nc6
                14. Rfb1 Rae8 15. Qh3 Rf6 16. f4 Na5 17. Qf3 d6 18. Re1 Qd7 19. e4 fxe4
                20. Qxe4 g6 21. g3 Kf8 22. Kg2 Rf7 23. h4 d5 24. cxd5 exd5 25. Qxe8+ Qxe8
                26. Rxe8+ Kxe8 27. h5 Rf6 28. hxg6 hxg6 29. Rh1 Kf8 30. Rh7 Rc6 31. g4 Nc4
                32. g5 Ne3+ 33. Kf3 Nf5 34. Bxf5 gxf5 35. Kg3 Rxc3+ 36. Kh4 Rf3 37. g6 Rxf4+
                38. Kg5 Re4 39. Kf6 Kg8 40. Rg7+ Kh8 41. Rxc7 Re8 42. Kxf5 1-0
            """.trimIndent(),
            summary = LocalizedText(
                "Capablanca sacrifie deux pions en finale de tours uniquement pour activer son Roi — une leçon magistrale sur la valeur de l'activité du Roi.",
                "Capablanca sacrifices two pawns in a rook endgame purely to activate his King—an unforgettable lesson in King activity."
            ),
            commentsByPly = mapOf(
                19 to LocalizedText("10.Fxe7 simplifie au bon moment pour éviter les complications tactiques.", "10.Bxe7 simplifies cleanly to secure a pleasant structural edge."),
                59 to LocalizedText("30.Th7! La Tour envahit la 7e rangée : règle d'or des finales de tours.", "30.Rh7! The Rook invades the 7th rank—golden rule of rook endgames."),
                69 to LocalizedText("35.Rg3!! Capablanca laisse tomber deux pions pour lancer son Roi en attaque !", "35.Kg3!! Capablanca gives up two pawns just to march his King into Black's camp!"),
                77 to LocalizedText("39.Rf6! Le Roi blanc crée des menaces de mat imparables avec le pion g6.", "39.Kf6! White's King and g6-pawn weave unstoppable mating nets.")
            )
        ),
        HistoricalGame(
            id = "hist_fischer_byrne",
            title = LocalizedText("La Partie du Siècle (Fischer, 13 ans)", "Game of the Century (13yo Fischer)"),
            whitePlayer = "Donald Byrne",
            blackPlayer = "Bobby Fischer",
            year = 1956,
            event = "Rosenwald Memorial, New York",
            result = "0-1",
            pgn = """
                [Event "Rosenwald Memorial"]
                [White "Donald Byrne"]
                [Black "Bobby Fischer"]
                [Result "0-1"]

                1. Nf3 Nf6 2. c4 g6 3. Nc3 Bg7 4. d4 O-O 5. Bf4 d5 6. Qb3 dxc4
                7. Qxc4 c6 8. e4 Nbd7 9. Rd1 Nb6 10. Qc5 Bg4 11. Bg5 Na4 12. Qa3 Nxc3
                13. bxc3 Nxe4 14. Bxe7 Qb6 15. Bc4 Nxc3 16. Bc5 Rfe8+ 17. Kf1 Be6
                18. Bxb6 Bxc4+ 19. Kg1 Ne2+ 20. Kf1 Nxd4+ 21. Kg1 Ne2+ 22. Kf1 Nc3+
                23. Kg1 axb6 24. Qb4 Ra4 25. Qxb6 Nxd1 26. h3 Rxa2 27. Kh2 Nxf2
                28. Re1 Rxe1 29. Qd8+ Bf8 30. Nxe1 Bd5 31. Nf3 Ne4 32. Qb8 b5
                33. h4 h5 34. Ne5 Kg7 35. Kg1 Bc5+ 36. Kf1 Ng3+ 37. Ke1 Bb4+
                38. Kd1 Bb3+ 39. Kc1 Ne2+ 40. Kb1 Nc3+ 41. Kc1 Rc2# 0-1
            """.trimIndent(),
            summary = LocalizedText(
                "À seulement 13 ans, Bobby Fischer joue l'un des plus célèbres sacrifices de Dame de l'histoire (17...Fe6!!).",
                "At just 13 years old, Bobby Fischer unleashes one of history's most famous Queen sacrifices (17...Be6!!)."
            ),
            commentsByPly = mapOf(
                22 to LocalizedText("11...Ca4!! Fischer exploite le Roi blanc resté au centre.", "11...Na4!! Fischer immediately targets White's uncastled King."),
                34 to LocalizedText("17...Fe6!! LE coup du siècle : Fischer abandonne sa Dame pour un moulinet dévastateur.", "17...Be6!! The move of the century: Fischer gives up his Queen for a devastating windmill!"),
                40 to LocalizedText("20...Cxd4+! Le moulinet commence : échec par découverte après échec par découverte.", "20...Nxd4+! The windmill spins—discoveries collect material with tempo."),
                82 to LocalizedText("41...Tc2# ! Un mat parfaitement orchestré par toutes les pièces noires.", "41...Rc2#! A picture-perfect checkmate by every remaining Black piece.")
            )
        ),
        HistoricalGame(
            id = "hist_kasparov_topalov",
            title = LocalizedText("L'Immortelle de Kasparov", "Kasparov's Immortal"),
            whitePlayer = "Garry Kasparov",
            blackPlayer = "Veselin Topalov",
            year = 1999,
            event = "Wijk aan Zee 1999",
            result = "1-0",
            pgn = """
                [Event "Wijk aan Zee"]
                [White "Garry Kasparov"]
                [Black "Veselin Topalov"]
                [Result "1-0"]

                1. e4 d6 2. d4 Nf6 3. Nc3 g6 4. Be3 Bg7 5. Qd2 c6 6. f3 b5
                7. Nge2 Nbd7 8. Bh6 Bxh6 9. Qxh6 Bb7 10. a3 e5 11. O-O-O Qe7
                12. Kb1 a6 13. Nc1 O-O-O 14. Nb3 exd4 15. Rxd4 c5 16. Rd1 Nb6
                17. g3 Kb8 18. Na5 Ba8 19. Bh3 d5 20. Qf4+ Ka7 21. Rhe1 d4
                22. Nd5 Nbxd5 23. exd5 Qd6 24. Rxd4 cxd4 25. Re7+ Kb6
                26. Qxd4+ Kxa5 27. b4+ Ka4 28. Qc3 Qxd5 29. Ra7 Bb7 30. Rxb7 Qc4
                31. Qxf6 Kxa3 32. Qxa6+ Kxb4 33. c3+ Kxc3 34. Qa1+ Kd2 35. Qb2+ Kd1
                36. Bf1 Rd2 37. Rd7 Rxd7 38. Bxc4 bxc4 39. Qxh8 Rd3 40. Qa8 c3
                41. Qa4+ Ke1 42. f4 f5 43. Kc1 Rd2 44. Qa7 1-0
            """.trimIndent(),
            summary = LocalizedText(
                "Une combinaison stupéfiante de 15 coups où Kasparov sacrifie ses deux Tours pour traquer le Roi noir à travers tout l'échiquier.",
                "A jaw-dropping 15-move combination where Kasparov sacrifices both Rooks to hunt Black's King across the board."
            ),
            commentsByPly = mapOf(
                47 to LocalizedText("24.Txd4!! Le début d'un feu d'artifice tactique inoubliable !", "24.Rxd4!! The start of an unforgettable tactical firework display!"),
                49 to LocalizedText("25.Te7+!! Deuxième Tour offerte pour attirer le Roi hors de son abri.", "25.Re7+!! A second Rook offered to drag the King into the open."),
                53 to LocalizedText("27.b4+! Le Roi noir est chassé jusqu'en a4 au milieu de l'échiquier !", "27.b4+! Black's King is driven all the way to a4!"),
                71 to LocalizedText("36.Ff1!! Coup silencieux génial : la Dame noire est sans défense utile.", "36.Bf1!! A brilliant quiet move sealing Black's fate.")
            )
        ),
        HistoricalGame(
            id = "hist_carlsen_anand",
            title = LocalizedText("Précision de Carlsen (Championnat du Monde)", "Carlsen's Precision (World Championship)"),
            whitePlayer = "Viswanathan Anand",
            blackPlayer = "Magnus Carlsen",
            year = 2013,
            event = "Championnat du Monde, Chennai (Partie 5)",
            result = "0-1",
            pgn = """
                [Event "World Chess Championship"]
                [White "Viswanathan Anand"]
                [Black "Magnus Carlsen"]
                [Result "0-1"]

                1. c4 e6 2. d4 d5 3. Nc3 c6 4. e4 dxe4 5. Nxe4 Bb4+ 6. Nc3 c5
                7. a3 Ba5 8. Nf3 Nf6 9. Be3 Nc6 10. Qd3 cxd4 11. Nxd4 Ng4
                12. O-O-O Nxe3 13. fxe3 Bc7 14. Nxc6 bxc6 15. Qxd8+ Bxd8
                16. Be2 Ke7 17. Bf3 Bd7 18. Ne4 Bb6 19. c5 f5 20. cxb6 fxe4
                21. b7 Rab8 22. Bxe4 Rxb7 23. Rhf1 Rb5 24. Rf4 g5 25. Rf2 h5
                26. Rdf1 Be8 27. Bc2 Rc5 28. Rf6 h4 29. e4 a5 30. Kd2 Rb5
                31. b3 Bh5 32. R1f2 Rd8+ 33. Kc3 Rc5+ 34. Kb2 Rd4 35. Rh6 Bd1
                36. Bb1 Rb5 37. Kc3 c5 38. Rb2 e5 39. Rg6 a4 40. Rxg5 Rxb3+
                41. Rxb3 Bxb3 42. Rxe5+ Kd6 43. Rh5 Rd1 44. e5+ Kd5 45. Bh7 Rc1+
                46. Kb2 Rg1 47. Bg8+ Kd4 48. Rxh4+ Kxe5 49. Bxb3 axb3 50. Kxb3 Rxg2 0-1
            """.trimIndent(),
            summary = LocalizedText(
                "Illustration parfaite du style moderne de Magnus Carlsen : presser une minuscule supériorité en finale jusqu'à provoquer la faute adverse.",
                "A textbook example of Magnus Carlsen's modern style: grinding a tiny endgame edge until the opponent cracks."
            ),
            commentsByPly = mapOf(
                30 to LocalizedText("15...Fxd8 : Les Dames s'échangent et Carlsen accueille la finale avec la paire de fous.", "15...Bxd8: Queens come off and Carlsen welcomes an endgame with the bishop pair."),
                64 to LocalizedText("32...Td8+! Activation maximale des Tours noires sur les colonnes centrales.", "32...Rd8+! Maximum Rook activity down the central files."),
                80 to LocalizedText("40...Txb3+! Rupture décisive sur l'aile dame qui disloque la défense blanche.", "40...Rxb3+! The decisive queenside breakthrough.")
            )
        )
    )

    val quizQuestions: List<QuizQuestion> = listOf(
        // RÈGLES
        QuizQuestion(
            id = "q_rules_1",
            category = QuizCategory.RULES,
            question = LocalizedText(
                "Quelle est la seule pièce qui ne peut jamais reculer ?",
                "Which piece can never move backward?"
            ),
            options = listOf(
                LocalizedText("Le Cavalier", "The Knight"),
                LocalizedText("Le Pion", "The Pawn"),
                LocalizedText("Le Fou", "The Bishop"),
                LocalizedText("La Tour", "The Rook")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "Le Pion avance toujours vers le camp adverse et ne recule jamais.",
                "Pawns always advance toward the opponent's side and never retreat."
            )
        ),
        QuizQuestion(
            id = "q_rules_2",
            category = QuizCategory.RULES,
            question = LocalizedText(
                "Dans quel cas le Roque est-il INTERDIT ?",
                "When is Castling ILLEGAL?"
            ),
            options = listOf(
                LocalizedText("Si la Tour cible est attaquée", "If the castling Rook is attacked"),
                LocalizedText("Si le Roi est actuellement en échec", "If the King is currently in check"),
                LocalizedText("Si le Roi adversaire a déjà roqué", "If the opponent's King has already castled"),
                LocalizedText("Après le 15e coup", "After move 15")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "On ne peut jamais roquer quand le Roi est en échec, ni s'il traverse ou arrive sur une case attaquée. (Une Tour attaquée n'empêche pas le roque !)",
                "You cannot castle while in check, nor through or into check. (An attacked Rook does NOT prevent castling!)"
            )
        ),
        QuizQuestion(
            id = "q_rules_3",
            category = QuizCategory.RULES,
            question = LocalizedText(
                "Que se passe-t-il si le joueur au trait n'est PAS en échec mais n'a aucun coup légal ?",
                "What happens if the player to move is NOT in check and has no legal moves?"
            ),
            options = listOf(
                LocalizedText("Il perd la partie", "They lose the game"),
                LocalizedText("Il passe son tour", "They skip their turn"),
                LocalizedText("C'est Pat (partie nulle)", "It is Stalemate (a draw)"),
                LocalizedText("On remet le Roi au centre", "The King is moved to the center")
            ),
            correctIndex = 2,
            explanation = LocalizedText(
                "C'est la règle du Pat : la partie se termine immédiatement sur un match nul (1/2 - 1/2).",
                "That is Stalemate: the game ends immediately in a draw (1/2 - 1/2)."
            ),
            boardFen = "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1"
        ),
        QuizQuestion(
            id = "q_rules_4",
            category = QuizCategory.RULES,
            question = LocalizedText(
                "Combien de points vaut généralement une Tour par rapport aux Pions ?",
                "How many points is a Rook generally worth in pawns?"
            ),
            options = listOf(
                LocalizedText("3 points", "3 points"),
                LocalizedText("5 points", "5 points"),
                LocalizedText("7 points", "7 points"),
                LocalizedText("9 points", "9 points")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "Barème classique : Pion=1, Cavalier=3, Fou=3, Tour=5, Dame=9.",
                "Classic piece values: Pawn=1, Knight=3, Bishop=3, Rook=5, Queen=9."
            )
        ),
        QuizQuestion(
            id = "q_rules_5",
            category = QuizCategory.RULES,
            question = LocalizedText(
                "Quand peut-on exécuter une prise en passant ?",
                "When can you perform an en passant capture?"
            ),
            options = listOf(
                LocalizedText("À n'importe quel moment de la partie", "At any point during the game"),
                LocalizedText("Uniquement sur un Cavalier", "Only against a Knight"),
                LocalizedText("Uniquement au coup suivant l'avance de 2 cases du pion adverse", "Only on the move right after the enemy pawn advances 2 squares"),
                LocalizedText("Uniquement en finale", "Only in the endgame")
            ),
            correctIndex = 2,
            explanation = LocalizedText(
                "Le droit de prise en passant expire immédiatement si vous jouez un autre coup.",
                "The en passant right expires immediately if you don't play it on the very next move."
            )
        ),

        // TACTIQUE
        QuizQuestion(
            id = "q_tac_1",
            category = QuizCategory.TACTICS,
            question = LocalizedText(
                "Qu'est-ce qu'une « Fourchette » aux échecs ?",
                "What is a 'Fork' in chess?"
            ),
            options = listOf(
                LocalizedText("Un échange de deux Dames", "A trade of two Queens"),
                LocalizedText("Une seule pièce qui attaque deux cibles (ou plus) en même temps", "A single piece attacking two or more targets at once"),
                LocalizedText("Un mat avec deux Tours", "A checkmate with two Rooks"),
                LocalizedText("Bloquer un pion avec le Roi", "Blockading a pawn with the King")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "La fourchette (souvent faite par le Cavalier ou un Pion) attaque au moins deux pièces adverses simultanément.",
                "A fork (commonly by a Knight or Pawn) attacks two or more enemy targets at the same time."
            ),
            boardFen = "r3k3/8/2N5/8/8/8/8/4K3 w - - 0 1"
        ),
        QuizQuestion(
            id = "q_tac_2",
            category = QuizCategory.TACTICS,
            question = LocalizedText(
                "Quelle est la différence entre un Clouage et une Enfilade ?",
                "What is the difference between a Pin and a Skewer?"
            ),
            options = listOf(
                LocalizedText("Dans l'enfilade, la pièce la PLUS forte est devant et forcée de bouger", "In a skewer, the MORE valuable piece is in front and forced to move"),
                LocalizedText("Il n'y a aucune différence", "There is no difference"),
                LocalizedText("Le clouage ne concerne que les pions", "A pin only applies to pawns"),
                LocalizedText("L'enfilade est réalisée par un Cavalier", "A skewer is done by a Knight")
            ),
            correctIndex = 0,
            explanation = LocalizedText(
                "Dans un clouage, une pièce moindre protège une pièce forte derrière. Dans une enfilade, la pièce forte (ex : Roi) est devant et doit fuir !",
                "In a pin, a lesser piece shields a stronger one. In a skewer, the higher-value piece is in front and must move away!"
            )
        ),
        QuizQuestion(
            id = "q_tac_3",
            category = QuizCategory.TACTICS,
            question = LocalizedText(
                "Quelle case est naturellement la plus faible du camp adverse au début de la partie ?",
                "Which square is naturally weakest in the opponent's camp at the start of the game?"
            ),
            options = listOf(
                LocalizedText("La case d4 / d5", "d4 / d5"),
                LocalizedText("La case f7 (pour les Noirs) et f2 (pour les Blancs)", "f7 (for Black) and f2 (for White)"),
                LocalizedText("La case a1 / h8", "a1 / h8"),
                LocalizedText("La case c3 / c6", "c3 / c6")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "f7 et f2 ne sont défendues que par le Roi en position initiale — cible du Coup du Berger et de nombreux sacrifices !",
                "f7 and f2 are guarded only by the King initially—the classic target of early tactical strikes!"
            )
        ),

        // OUVERTURES
        QuizQuestion(
            id = "q_open_1",
            category = QuizCategory.OPENINGS,
            question = LocalizedText(
                "Quels sont les 3 grands principes d'or de l'ouverture ?",
                "What are the 3 golden principles of the opening?"
            ),
            options = listOf(
                LocalizedText("Sortir la Dame, pousser les pions a/h, roquer tard", "Bring the Queen out, push flank pawns, castle late"),
                LocalizedText("Contrôler le centre, développer Cavaliers/Fous, mettre le Roi en sécurité (roque)", "Control the center, develop Knights/Bishops, castle the King to safety"),
                LocalizedText("Échanger toutes les pièces le plus vite possible", "Trade every piece as quickly as possible"),
                LocalizedText("Ne jamais bouger les pions centraux", "Never move center pawns")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "Centre, développement des pièces légères et roque rapide sont la base d'une ouverture saine.",
                "Central control, rapid minor-piece development, and early castling form the foundation of sound openings."
            )
        ),
        QuizQuestion(
            id = "q_open_2",
            category = QuizCategory.OPENINGS,
            question = LocalizedText(
                "Quelle suite caractérise l'Ouverture Italienne ?",
                "Which moves define the Italian Game?"
            ),
            options = listOf(
                LocalizedText("1.d4 d5 2.c4", "1.d4 d5 2.c4"),
                LocalizedText("1.e4 c5 2.Cf3", "1.e4 c5 2.Nf3"),
                LocalizedText("1.e4 e5 2.Cf3 Cc6 3.Fc4", "1.e4 e5 2.Nf3 Nc6 3.Bc4"),
                LocalizedText("1.e4 e6 2.d4 d5", "1.e4 e6 2.d4 d5")
            ),
            correctIndex = 2,
            explanation = LocalizedText(
                "3.Fc4 définit l'Ouverture Italienne, tandis que 3.Fb5 définit la Partie Espagnole (Ruy Lopez).",
                "3.Bc4 defines the Italian Game, whereas 3.Bb5 defines the Ruy Lopez (Spanish)."
            )
        ),
        QuizQuestion(
            id = "q_open_3",
            category = QuizCategory.OPENINGS,
            question = LocalizedText(
                "Que cherche à faire la Défense Sicilienne (1.e4 c5) ?",
                "What is the main goal of the Sicilian Defense (1.e4 c5)?"
            ),
            options = listOf(
                LocalizedText("Lutter de manière asymétrique pour le centre et ouvrir la colonne c", "Fight asymmetrically for the center and open the c-file"),
                LocalizedText("Forcer une nulle rapide par répétition", "Force a quick draw by repetition"),
                LocalizedText("Bloquer le jeu sur l'aile roi", "Lock up the kingside immediately")
            ),
            correctIndex = 0,
            explanation = LocalizedText(
                "En échangeant le pion c contre le pion d blanc, les Noirs obtiennent deux pions centraux contre un et une colonne c ouverte.",
                "By trading their c-pawn for White's d-pawn, Black gains a central pawn majority and an open c-file."
            )
        ),

        // FINALES
        QuizQuestion(
            id = "q_end_1",
            category = QuizCategory.ENDGAMES,
            question = LocalizedText(
                "En finale, quel rôle doit jouer votre Roi ?",
                "What role should your King play in the endgame?"
            ),
            options = listOf(
                LocalizedText("Rester caché dans un coin jusqu'à la fin", "Stay hidden in the corner until the end"),
                LocalizedText("Devenir une pièce active qui attaque au centre", "Become an active fighting piece in the center"),
                LocalizedText("Ne jamais dépasser la 2e rangée", "Never cross the 2nd rank")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "Avec peu de pièces sur l'échiquier, le Roi risque peu le mat et vaut autant qu'une pièce légère en attaque !",
                "With fewer pieces on the board, the King is safe from mate and acts as a powerful fighting piece!"
            )
        ),
        QuizQuestion(
            id = "q_end_2",
            category = QuizCategory.ENDGAMES,
            question = LocalizedText(
                "Quel matériel minimal suffit pour mater un Roi seul ?",
                "Which minimal material is sufficient to force checkmate against a lone King?"
            ),
            options = listOf(
                LocalizedText("Roi + Cavalier", "King + Knight"),
                LocalizedText("Roi + Fou", "King + Bishop"),
                LocalizedText("Roi + Tour", "King + Rook"),
                LocalizedText("Roi + deux Cavaliers (contre Roi seul)", "King + two Knights (vs lone King)")
            ),
            correctIndex = 2,
            explanation = LocalizedText(
                "Roi + Tour (et Roi + Dame) matent facilement. Roi + Fou ou Roi + Cavalier est nulle par matériel insuffisant !",
                "King + Rook (and King + Queen) force checkmate. King + single Bishop or Knight is an automatic draw."
            )
        ),
        QuizQuestion(
            id = "q_end_3",
            category = QuizCategory.ENDGAMES,
            question = LocalizedText(
                "Dans une finale de Tours, que signifie « la Tour derrière le pion passé » (règle de Tarrasch) ?",
                "In Rook endgames, what is Tarrasch's rule about Rook placement?"
            ),
            options = listOf(
                LocalizedText("Mettre la Tour devant le pion pour le bloquer", "Put the Rook in front of the pawn"),
                LocalizedText("Placer la Tour DERRIÈRE le pion passé (le vôtre ou celui de l'adversaire)", "Place the Rook BEHIND the passed pawn (yours or the opponent's)"),
                LocalizedText("Échanger les Tours immédiatement", "Trade Rooks immediately")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "Derrière son propre pion, la Tour le pousse tout en gagnant de l'espace ; derrière le pion adverse, elle freine son avance.",
                "Behind your own passed pawn, the Rook gains scope as the pawn advances; behind the enemy pawn, it restricts it from afar."
            )
        ),
        QuizQuestion(
            id = "q_end_4",
            category = QuizCategory.ENDGAMES,
            question = LocalizedText(
                "Qu'est-ce que le « Zugzwang » aux échecs ?",
                "What is 'Zugzwang' in chess?"
            ),
            options = listOf(
                LocalizedText("Une obligation de jouer qui détériore la position du joueur au trait", "A situation where having to move worsens your own position"),
                LocalizedText("Un échec double avec la Dame et la Tour", "A double check with Queen and Rook"),
                LocalizedText("Une règle permettant de promouvoir deux pions à la fois", "A rule allowing double pawn promotion")
            ),
            correctIndex = 0,
            explanation = LocalizedText(
                "En Zugzwang, tout coup légal affaiblit la position — si le joueur pouvait passer son tour, il tiendrait la partie !",
                "In Zugzwang, any legal move ruins your defense—if you could skip your turn, you'd be fine!"
            ),
            boardFen = "4k3/4P3/4K3/8/8/8/8/8 w - - 0 1"
        ),
        QuizQuestion(
            id = "q_end_5",
            category = QuizCategory.ENDGAMES,
            question = LocalizedText(
                "Dans cette position, les Blancs jouent d6! Le Roi noir en g2 peut-il rattraper le pion ?",
                "White plays d6! Can the Black King on g2 catch the pawn?"
            ),
            options = listOf(
                LocalizedText("Oui, grâce au tempo de diagonale", "Yes, via a diagonal sprint"),
                LocalizedText("Non, il est hors du carré du pion (Règle du Carré)", "No, it is outside the pawn's square (Rule of the Square)"),
                LocalizedText("Seulement s'il fait pat", "Only if it forces stalemate")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "Après d6, le carré du pion va de d6 à d8 et f8 à f6 : le Roi noir en g2 (colonne g) est trop loin d'une colonne !",
                "After d6, the pawn's square spans d6-d8 and f8-f6: Black's King on g2 is one file too far away!"
            ),
            boardFen = "8/8/3P4/8/8/8/6k1/4K3 b - - 0 1"
        ),
        QuizQuestion(
            id = "q_tac_4",
            category = QuizCategory.TACTICS,
            question = LocalizedText(
                "Qu'est-ce qu'un « coup intermédiaire » (Zwischenzug) ?",
                "What is an 'in-between move' (Zwischenzug)?"
            ),
            options = listOf(
                LocalizedText("Jouer un coup surprenant (souvent un échec ou une menace) avant d'effectuer la reprise attendue", "Inserting an unexpected threat or check before playing the expected recapture"),
                LocalizedText("Faire roquer la Tour sans bouger le Roi", "Castling the Rook without moving the King"),
                LocalizedText("Abandonner avant le 10e coup", "Resigning before move 10")
            ),
            correctIndex = 0,
            explanation = LocalizedText(
                "Le Zwischenzug renverse le calcul adverse en changeant l'ordre naturel des échanges avec un coup fort.",
                "A Zwischenzug disrupts the opponent's calculation by inserting a forcing move before recapturing."
            )
        ),
        QuizQuestion(
            id = "q_tac_5",
            category = QuizCategory.TACTICS,
            question = LocalizedText(
                "Dans quel type de position la « paire de Fous » est-elle la plus redoutable ?",
                "In what type of position is the 'Bishop Pair' strongest?"
            ),
            options = listOf(
                LocalizedText("Position complètement fermée avec chaînes de pions verrouillées", "Completely closed positions with locked pawn chains"),
                LocalizedText("Position ouverte avec diagonales libres et pions sur les deux ailes", "Open positions with clear diagonals and pawns on both wings"),
                LocalizedText("Finale avec uniquement des Rois", "King-only endgames")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "Dans les positions ouvertes, deux Fous balayent les deux couleurs de cases et dominent les Cavaliers à longue portée.",
                "In open positions, two Bishops control both color complexes across long diagonals."
            )
        ),
        QuizQuestion(
            id = "q_open_4",
            category = QuizCategory.OPENINGS,
            question = LocalizedText(
                "Quelle est l'idée principale derrière un Fianchetto (ex : g3 + Fg2) ?",
                "What is the main purpose of a Fianchetto (e.g., g3 + Bg2)?"
            ),
            options = listOf(
                LocalizedText("Contrôler la grande diagonale centrale depuis une case sûre et protéger le roque", "Control the long central diagonal safely while shielding the castled King"),
                LocalizedText("Enfermer la Tour h1 pour toute la partie", "Trap the h1 Rook for the entire game"),
                LocalizedText("Sacrifier le pion g3 dès le début", "Sacrifice the g3 pawn early")
            ),
            correctIndex = 0,
            explanation = LocalizedText(
                "Le Fou en g2 rayonne jusqu'en a8 à travers le centre tout en formant un bouclier solide devant le Roi roqué.",
                "A fianchettoed Bishop on g2 fires down the a8-h1 diagonal while reinforcing the castled King's fortress."
            )
        ),
        QuizQuestion(
            id = "q_rules_6",
            category = QuizCategory.RULES,
            question = LocalizedText(
                "Que se passe-t-il si exactement la même position se répète 3 fois (avec mêmes droits de roque/trait) ?",
                "What happens if the exact same position occurs 3 times (same turn & castling rights)?"
            ),
            options = listOf(
                LocalizedText("Les Blancs gagnent automatiquement", "White automatically wins"),
                LocalizedText("Nulle par triple répétition", "Draw by threefold repetition"),
                LocalizedText("On rejoue depuis le coup 1", "The game restarts from move 1")
            ),
            correctIndex = 1,
            explanation = LocalizedText(
                "La triple répétition permet à l'un des joueurs de réclamer la nulle — une ressource défensive précieuse (échec perpétuel) !",
                "Threefold repetition allows a player to claim a draw—a crucial defensive tool via perpetual check!"
            )
        )
    )
}
