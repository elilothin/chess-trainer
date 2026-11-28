package com.example.data.content

import com.example.domain.model.HistoricalGame
import com.example.domain.model.LocalizedText

/** Parties supplémentaires. Les coups sont vérifiés par un test (légalité + nombre de demi-coups). */
object ExtraHistoricalGames {
    private fun t(fr: String, en: String) = LocalizedText(fr, en)

    val games: List<HistoricalGame> = listOf(
        HistoricalGame(
            id = "hist_fischer_spassky_g6",
            title = t("Fischer – Spassky, partie 6 (Gambit Dame refusé)", "Fischer – Spassky, Game 6 (Queen's Gambit Declined)"),
            whitePlayer = "Bobby Fischer",
            blackPlayer = "Boris Spassky",
            year = 1972,
            event = "Championnat du monde, Reykjavik",
            result = "1-0",
            pgn = """
                [Event "World Championship"]
                [White "Bobby Fischer"]
                [Black "Boris Spassky"]
                [Result "1-0"]

                1. c4 e6 2. Nf3 d5 3. d4 Nf6 4. Nc3 Be7 5. Bg5 O-O 6. e3 h6 7. Bh4 b6
                8. cxd5 Nxd5 9. Bxe7 Qxe7 10. Nxd5 exd5 11. Rc1 Be6 12. Qa4 c5 13. Qa3 Rc8
                14. Bb5 a6 15. dxc5 bxc5 16. O-O Ra7 17. Be2 Nd7 18. Nd4 Qf8 19. Nxe6 fxe6
                20. e4 d4 21. f4 Qe7 22. e5 Rb8 23. Bc4 Kh8 24. Qh3 Nf8 25. b3 a5 26. f5 exf5
                27. Rxf5 Nh7 28. Rcf1 Qd8 29. Qg3 Re7 30. h4 Rbb7 31. e6 Rbc7 32. Qe5 Qe8
                33. a4 Qd8 34. R1f2 Qe8 35. R2f3 Qd8 36. Bd3 Qe8 37. Qe4 Nf6 38. Rxf6 gxf6
                39. Rxf6 Kg8 40. Bc4 Kh8 41. Qf4 1-0
            """.trimIndent(),
            summary = t(
                "Fischer joue 1.c4 pour la première fois du match et livre une leçon d'ouverture, de plans et de précision. C'est le type de Gambit Dame qui a inspiré toute une génération.",
                "Fischer opens 1.c4 for the first time in the match and delivers a lesson in planning and precision, in the Queen's Gambit family that inspired a whole generation."
            ),
            commentsByPly = mapOf(
                1 to t("1.c4 : Fischer, fidèle à 1.e4, change d'ouverture et surprend Spassky.", "1.c4: lifelong 1.e4 player Fischer changes course and surprises Spassky."),
                27 to t("14.Fb5 : le fou vient clouer et gêner le camp noir.", "14.Bb5: the bishop pins and cramps Black's camp."),
                37 to t("19.Cxe6 : l'échange qui laisse les Noirs avec des pions faibles.", "19.Nxe6: the exchange leaves Black with weak pawns."),
                41 to t("21.f4 : les Blancs prennent tout l'espace au centre et à l'aile roi.", "21.f4: White grabs space in the center and on the kingside."),
                81 to t("Spassky abandonne et applaudit son adversaire.", "Spassky resigns and applauds his opponent.")
            )
        ),
        HistoricalGame(
            id = "hist_anderssen_kieseritzky",
            title = t("La Partie Immortelle", "The Immortal Game"),
            whitePlayer = "Adolf Anderssen",
            blackPlayer = "Lionel Kieseritzky",
            year = 1851,
            event = "Londres (partie amicale)",
            result = "1-0",
            pgn = """
                [Event "London"]
                [White "Adolf Anderssen"]
                [Black "Lionel Kieseritzky"]
                [Result "1-0"]

                1. e4 e5 2. f4 exf4 3. Bc4 Qh4+ 4. Kf1 b5 5. Bxb5 Nf6 6. Nf3 Qh6 7. d3 Nh5
                8. Nh4 Qg5 9. Nf5 c6 10. g4 Nf6 11. Rg1 cxb5 12. h4 Qg6 13. h5 Qg5 14. Qf3 Ng8
                15. Bxf4 Qf6 16. Nc3 Bc5 17. Nd5 Qxb2 18. Bd6 Bxg1 19. e5 Qxa1+ 20. Ke2 Na6
                21. Nxg7+ Kd8 22. Qf6+ Nxf6 23. Be7# 1-0
            """.trimIndent(),
            summary = t(
                "Anderssen sacrifie deux tours, un fou puis la dame pour mater avec trois pièces mineures. Le symbole du jeu romantique.",
                "Anderssen sacrifices both rooks, a bishop and then the queen to mate with minor pieces. The emblem of romantic chess."
            ),
            commentsByPly = mapOf(
                3 to t("2.f4 : le Gambit du Roi, l'arme préférée de l'époque.", "2.f4: the King's Gambit, the weapon of the era."),
                7 to t("4.Rf1 : Anderssen renonce au roque mais garde l'initiative.", "4.Kf1: Anderssen gives up castling but keeps the initiative."),
                33 to t("17.Cd5 : le piège se met en place, les Noirs prennent le pion b2.", "17.Nd5: the trap is set; Black grabs the b2 pawn."),
                35 to t("18.Fd6!! Anderssen offre ses deux tours pour une attaque décisive.", "18.Bd6!! Anderssen offers both rooks for a decisive attack."),
                41 to t("21.Cxg7+ : un cavalier ouvre la voie au roi noir.", "21.Nxg7+: a knight opens the way to the black king."),
                43 to t("22.Df6+!! Le sacrifice de dame final.", "22.Qf6+!! The final queen sacrifice."),
                45 to t("23.Fe7# : mat avec trois pièces mineures, sans dame ni tours.", "23.Be7#: mate with minor pieces only, no queen, no rooks.")
            )
        ),
        HistoricalGame(
            id = "hist_anand_carlsen_2013_g4",
            title = t("Anand – Carlsen, partie 4 (Berlinoise)", "Anand – Carlsen, Game 4 (Berlin)"),
            whitePlayer = "Viswanathan Anand",
            blackPlayer = "Magnus Carlsen",
            year = 2013,
            event = "Championnat du monde, Chennai",
            result = "1/2-1/2",
            pgn = """
                [Event "World Championship"]
                [White "Viswanathan Anand"]
                [Black "Magnus Carlsen"]
                [Result "1/2-1/2"]

                1. e4 e5 2. Nf3 Nc6 3. Bb5 Nf6 4. O-O Nxe4 5. d4 Nd6 6. Bxc6 dxc6 7. dxe5 Nf5
                8. Qxd8+ Kxd8 9. h3 Bd7 10. Rd1 Be7 11. Nc3 Kc8 12. Bg5 h6 13. Bxe7 Nxe7
                14. Rd2 c5 15. Rad1 Be6 16. Ne1 Ng6 17. Nd3 b6 18. Ne2 Bxa2 19. b3 c4
                20. Ndc1 cxb3 21. cxb3 Bb1 22. f4 Kb7 23. Nc3 Bf5 24. g4 Bc8 25. Nd3 h5
                26. f5 Ne7 27. Nb5 hxg4 28. hxg4 Rh4 29. Nf2 Nc6 30. Rc2 a5 31. Rc4 g6
                32. Rdc1 Bd7 33. e6 fxe6 34. fxe6 Be8 35. Ne4 Rxg4+ 36. Kf2 Rf4+ 37. Ke3 Rf8
                38. Nd4 Nxd4 39. Rxc7+ Ka6 40. Kxd4 Rd8+ 41. Kc3 Rf3+ 42. Kb2 Re3 43. Rc8 Rdd3
                44. Ra8+ Kb7 45. Rxe8 Rxe4 46. e7 Rg3 47. Rc3 Re2+ 48. Rc2 Ree3 49. Ka2 g5
                50. Rd2 Re5 51. Rd7+ Kc6 52. Red8 Rge3 53. Rd6+ Kb7 54. R8d7+ Ka6 55. Rd5 Re2+
                56. Ka3 Re6 57. Rd8 g4 58. Rg5 Rxe7 59. Ra8+ Kb7 60. Rag8 a4 61. Rxg4 axb3
                62. R8g7 Ka6 63. Rxe7 Rxe7 64. Kxb3 1/2-1/2
            """.trimIndent(),
            summary = t(
                "Une Berlinoise sans dames où Carlsen, le futur champion, défend un finale difficile avec un calme total. Un bon exemple de la résistance au plus haut niveau.",
                "A queenless Berlin where the future champion defends a difficult endgame with total calm. A fine example of top-level resilience."
            ),
            commentsByPly = mapOf(
                9 to t("5.d4 : la ligne ouverte contre le « Mur de Berlin ».", "5.d4: the open line against the Berlin Wall."),
                15 to t("8.Dxd8+ : les dames s'échangent très tôt, jeu technique en vue.", "8.Qxd8+: queens come off early, a technical game ahead."),
                65 to t("33.e6 : les Blancs ouvrent le jeu pour activer leurs tours.", "33.e6: White opens the position to activate the rooks."),
                127 to t("Nulle : la défense de Carlsen tient.", "Draw: Carlsen's defense holds.")
            )
        ),
        HistoricalGame(
            id = "hist_carlsen_caruana_2018_g11",
            title = t("Carlsen – Caruana, partie 11 (Petroff)", "Carlsen – Caruana, Game 11 (Petroff)"),
            whitePlayer = "Magnus Carlsen",
            blackPlayer = "Fabiano Caruana",
            year = 2018,
            event = "Championnat du monde, Londres",
            result = "1/2-1/2",
            pgn = """
                [Event "World Championship"]
                [White "Magnus Carlsen"]
                [Black "Fabiano Caruana"]
                [Result "1/2-1/2"]

                1. e4 e5 2. Nf3 Nf6 3. Nxe5 d6 4. Nf3 Nxe4 5. Nc3 Nxc3 6. dxc3 Be7 7. Be3 O-O
                8. Qd2 Nd7 9. O-O-O Nf6 10. Bd3 c5 11. Rhe1 Be6 12. Kb1 Qa5 13. c4 Qxd2
                14. Bxd2 h6 15. Nh4 Rfe8 16. Ng6 Ng4 17. Nxe7+ Rxe7 18. Re2 Ne5 19. Bf4 Nxd3
                20. Rxd3 Rd7 21. Rxd6 Rxd6 22. Bxd6 Rd8 23. Rd2 Bxc4 24. Kc1 b6 25. Bf4 Rxd2
                26. Kxd2 a6 27. a3 Kf8 28. Bc7 b5 29. Bd6+ Ke8 30. Bxc5 h5 31. Ke3 Kd7
                32. Kd4 g6 33. g3 Be2 34. Bf8 Kc6 35. b3 Bd1 36. Kd3 Bg4 37. c4 Be6
                38. Kd4 bxc4 39. bxc4 Bg4 40. c5 Be6 41. Bh6 Bd5 42. Be3 Be6 43. Ke5 Bd5
                44. Kf4 Be6 45. Kg5 Bd5 46. g4 hxg4 47. Kxg4 Ba2 48. Kg5 Bb3 49. Kf6 Ba2
                50. h4 Bb3 51. f4 Ba2 52. Ke7 Bb3 53. Kf6 Ba2 54. f5 Bb1 55. Bf2 Bc2 1/2-1/2
            """.trimIndent(),
            summary = t(
                "Un duel de préparation : Carlsen presse sans relâche dans un finale de fous de couleurs opposées, Caruana tient toutes les menaces. La nulle d'un match où tout se joue à un détail.",
                "A preparation duel: Carlsen presses relentlessly in a pure bishop endgame while Caruana meets every threat. A draw in a match decided by details."
            ),
            commentsByPly = mapOf(
                5 to t("3.Cxe5 : la ligne principale de la Petroff.", "3.Nxe5: the main line of the Petroff."),
                25 to t("13.c4 : Carlsen force l'échange des dames.", "13.c4: Carlsen forces the queen trade."),
                110 to t("Nulle : Caruana a résisté à toutes les tentatives.", "Draw: Caruana resisted every attempt.")
            )
        ),
        HistoricalGame(
            id = "hist_karjakin_carlsen_2016_g2",
            title = t("Karjakin – Carlsen, partie 2 (Espagnole)", "Karjakin – Carlsen, Game 2 (Ruy Lopez)"),
            whitePlayer = "Sergey Karjakin",
            blackPlayer = "Magnus Carlsen",
            year = 2016,
            event = "Championnat du monde, New York",
            result = "1/2-1/2",
            pgn = """
                [Event "World Championship"]
                [White "Sergey Karjakin"]
                [Black "Magnus Carlsen"]
                [Result "1/2-1/2"]

                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. d3 b5 7. Bb3 d6 8. a3 O-O
                9. Nc3 Na5 10. Ba2 Be6 11. d4 Bxa2 12. Rxa2 Re8 13. Ra1 Nc4 14. Re1 Rc8
                15. h3 h6 16. b3 Nb6 17. Bb2 Bf8 18. dxe5 dxe5 19. a4 c6 20. Qxd8 Rcxd8
                21. axb5 axb5 22. Ne2 Bb4 23. Bc3 Bxc3 24. Nxc3 Nbd7 25. Ra6 Rc8 26. b4 Re6
                27. Rb1 c5 28. Rxe6 fxe6 29. Nxb5 cxb4 30. Rxb4 Rxc2 31. Nd6 Rc1+ 32. Kh2 Rc2
                33. Kg1 1/2-1/2
            """.trimIndent(),
            summary = t(
                "Une Espagnole fermée jouée à toute vitesse jusqu'à un finale de tours précis : deux joueurs de très haut niveau qui ne laissent aucune faille.",
                "A fast-played Closed Ruy Lopez into a precise rook endgame: two top players leaving no cracks."
            ),
            commentsByPly = mapOf(
                9 to t("5.O-O : Karjakin choisit la voie classique.", "5.O-O: Karjakin takes the classical road."),
                39 to t("20.Dxd8 : échange de dames, le jeu devient technique.", "20.Qxd8: queens traded, the game turns technical."),
                65 to t("Nulle par répétition : l'équilibre est parfait.", "Draw by repetition: the balance is perfect.")
            )
        )
    )
}
