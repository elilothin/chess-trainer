package com.example.data.content

import com.example.domain.model.LocalizedText
import com.example.domain.model.PuzzleTheme
import com.example.domain.model.TacticalPuzzle

object PuzzlesContent {

    val puzzles: List<TacticalPuzzle> = listOf(
        // --- MAT EN 1 (Elo 450 - 900) ---
        TacticalPuzzle(
            id = "puz_m1_1",
            initialFen = "6k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1",
            solutionUci = listOf("d1d8"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 480,
            title = LocalizedText("Mat du couloir", "Back-Rank Checkmate"),
            explanation = LocalizedText(
                "La 8e rangée est sans défense et les pions noirs bloquent la fuite de leur propre Roi. Td8# est mat immédiat !",
                "The 8th rank is undefended and Black's own pawns block the King's escape. Rd8# is immediate checkmate!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m1_2",
            initialFen = "r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 4 4",
            solutionUci = listOf("h5f7"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 520,
            title = LocalizedText("Le Coup du Berger", "Scholar's Mate"),
            explanation = LocalizedText(
                "Le Fou en c4 protège la Dame qui capture en f7 : Dxf7# ! Le point f7 n'est défendu que par le Roi en début de partie.",
                "The Bishop on c4 supports the Queen capturing on f7: Qxf7#! f7 is the weakest square in Black's opening camp."
            )
        ),
        TacticalPuzzle(
            id = "puz_m1_3",
            initialFen = "5rk1/5ppp/8/8/3Q4/8/1B3PPP/6K1 w - - 0 1",
            solutionUci = listOf("d4g7"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 620,
            title = LocalizedText("Batterie Fou + Dame", "Bishop & Queen Battery"),
            explanation = LocalizedText(
                "Le Fou en b2 vise g7 et soutient la Dame en d4 : Dxg7# est échec et mat imparable !",
                "The Bishop on b2 aims at g7 and supports the Queen on d4: Qxg7# is an unstoppable checkmate!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m1_4",
            initialFen = "6rk/6pp/8/6N1/8/8/8/6K1 w - - 0 1",
            solutionUci = listOf("g5f7"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 820,
            title = LocalizedText("Le Mat Étouffé", "Smothered Mate"),
            explanation = LocalizedText(
                "Le Roi noir en h8 est encerclé par ses propres pièces (Tg8, g7, h7). Cf7# donne un mat étouffé classique !",
                "Black's King on h8 is boxed in by its own pieces (Rg8, g7, h7). Nf7# delivers a classic smothered mate!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m1_5",
            initialFen = "7k/6R1/5N2/8/8/8/8/6K1 w - - 0 1",
            solutionUci = listOf("g7h7"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 750,
            title = LocalizedText("Mat Arabe", "Arabian Mate"),
            explanation = LocalizedText(
                "Le Cavalier en f6 protège h7 et coupe la case g8. La Tour en h7 donne le célèbre Mat Arabe (Th7#) !",
                "The Knight on f6 guards h7 and covers g8. Rook to h7 delivers the famous Arabian Mate (Rh7#)!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m1_6",
            initialFen = "4r1k1/5ppp/8/8/8/8/5PPP/R3R1K1 w - - 0 1",
            solutionUci = listOf("e1e8"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 560,
            title = LocalizedText("Batterie de Tours", "Rook Battery Mate"),
            explanation = LocalizedText(
                "Votre Tour en a1 soutient... non, ici e1 et a1 ne sont pas alignées ! Ah si : 4r1k1 et Tour en e1 prend e8 -> 1.Txe8# !",
                "Capture the defending Rook on e8 for an instant back-rank checkmate (Rxe8#)!"
            )
        ),

        // --- FOURCHETTES (FORK, Elo 650 - 1350) ---
        TacticalPuzzle(
            id = "puz_fork_1",
            initialFen = "r3k3/8/8/3N4/8/8/8/4K3 w - - 0 1",
            solutionUci = listOf("d5c7"),
            theme = PuzzleTheme.FORK,
            elo = 720,
            title = LocalizedText("Fourchette royale du Cavalier", "Royal Knight Fork"),
            explanation = LocalizedText(
                "Cc7+ attaque simultanément le Roi en e8 et la Tour en a8. Le Roi doit bouger, et la Tour tombe au coup suivant !",
                "Nc7+ forks the King on e8 and the Rook on a8. The King must move, winning the Rook!"
            )
        ),
        TacticalPuzzle(
            id = "puz_fork_2",
            initialFen = "4k3/8/8/2r1q3/8/3P4/8/6K1 w - - 0 1",
            solutionUci = listOf("d3d4"),
            theme = PuzzleTheme.FORK,
            elo = 680,
            title = LocalizedText("Fourchette de Pion", "Pawn Fork"),
            explanation = LocalizedText(
                "Même un humble pion peut gagner une pièce majeure ! d4 attaque à la fois la Tour en c5 et la Dame en e5.",
                "Even a humble pawn can win major material! Pushing d4 forks both the Rook on c5 and the Queen on e5."
            )
        ),
        TacticalPuzzle(
            id = "puz_fork_3",
            initialFen = "6k1/5pp1/7p/8/r7/8/4QPPP/6K1 w - - 0 1",
            solutionUci = listOf("e2e8", "g8h7", "e8a4"),
            theme = PuzzleTheme.FORK,
            elo = 920,
            title = LocalizedText("Double attaque de Dame", "Queen Check & Win"),
            explanation = LocalizedText(
                "De8+ donne échec au Roi en g8 tout en attaquant la Tour isolée en a4 ! Après Rh7, vous capturez la Tour gratuitement (Dxa4).",
                "Qe8+ checks the King on g8 while lining up the undefended Rook on a4! After Kh7, capture the Rook with Qxa4."
            )
        ),
        TacticalPuzzle(
            id = "puz_fork_4",
            initialFen = "2r3k1/8/8/3N4/8/8/8/4K3 w - - 0 1",
            solutionUci = listOf("d5e7", "g8f7", "e7c8"),
            theme = PuzzleTheme.FORK,
            elo = 1020,
            title = LocalizedText("Fourchette familiale du Cavalier", "Knight Fork on c8 & g8"),
            explanation = LocalizedText(
                "Ce7+! attaque simultanément le Roi en g8 et la Tour en c8. Après la fuite du Roi, Cxc8 gagne la Tour.",
                "Ne7+! simultaneously attacks the King on g8 and the Rook on c8. Once the King moves, Nxc8 wins the Rook."
            )
        ),

        // --- CLOUAGE (PIN, Elo 900 - 1900) ---
        TacticalPuzzle(
            id = "puz_pin_1",
            initialFen = "4k3/3q4/8/8/8/8/8/4KB2 w - - 0 1",
            solutionUci = listOf("f1b5"),
            theme = PuzzleTheme.PIN,
            elo = 960,
            title = LocalizedText("Clouage absolu de la Dame", "Absolute Pin on the Queen"),
            explanation = LocalizedText(
                "Fb5 cloue la Dame noire en d7 directement devant son Roi en e8 ! La Dame ne peut plus quitter la diagonale et sera capturée.",
                "Bb5 pins Black's Queen on d7 against the King on e8! The Queen cannot move away and is lost."
            )
        ),
        TacticalPuzzle(
            id = "puz_pin_2",
            initialFen = "3k4/3n4/8/2P5/8/8/3R4/4K3 w - - 0 1",
            solutionUci = listOf("c5c6"),
            theme = PuzzleTheme.PIN,
            elo = 1080,
            title = LocalizedText("Attaquer la pièce clouée", "Attack the Pinned Piece"),
            explanation = LocalizedText(
                "Le Cavalier en d7 est cloué par la Tour en d2 devant son Roi. Avec c6 ! vous attaquez la pièce clouée avec un pion — il ne peut pas fuir !",
                "The Knight on d7 is pinned to the King by the d2 Rook. Push c6! Always attack a pinned piece with a lower-value unit."
            )
        ),
        TacticalPuzzle(
            id = "puz_pin_3",
            initialFen = "6k1/5rpp/8/5N2/8/1Q6/8/6K1 w - - 0 1",
            solutionUci = listOf("f5e7", "g8h8", "b3f7"),
            theme = PuzzleTheme.PIN,
            elo = 1850,
            title = LocalizedText("Clouage diagonal mortel", "Deadly Diagonal Pin"),
            explanation = LocalizedText(
                "La Tour en f7 est clouée sur la diagonale a2-g8 par la Dame en b3 ! 1.Ce7+! (la Tour ne peut pas prendre !) force 1...Rh8, puis 2.Dxf7 gagne une Tour entière.",
                "Black's Rook on f7 is pinned along the a2-g8 diagonal by your Queen! 1.Ne7+! (the Rook cannot capture!) forces 1...Kh8, and 2.Qxf7 wins a full Rook."
            )
        ),

        // --- ENFILADE (SKEWER, Elo 1050 - 1500) ---
        TacticalPuzzle(
            id = "puz_skewer_1",
            initialFen = "4k2q/8/8/8/8/8/8/R3K3 w - - 0 1",
            solutionUci = listOf("a1a8", "e8d7", "a8h8"),
            theme = PuzzleTheme.SKEWER,
            elo = 1120,
            title = LocalizedText("L'Enfilade fatale", "The Fatal Skewer"),
            explanation = LocalizedText(
                "Ta8+ force le Roi noir à quitter la 8e rangée, exposant la Dame en h8 derrière lui ! Après Rd7, Txh8 gagne la Dame.",
                "Ra8+ forces the Black King off the 8th rank, exposing the Queen behind it on h8! After Kd7, Rxh8 wins the Queen."
            )
        ),
        TacticalPuzzle(
            id = "puz_skewer_2",
            initialFen = "6q1/5k2/8/8/8/8/4B3/7K w - - 0 1",
            solutionUci = listOf("e2c4", "f7g7", "c4g8"),
            theme = PuzzleTheme.SKEWER,
            elo = 1140,
            title = LocalizedText("Enfilade diagonale du Fou", "Diagonal Bishop Skewer"),
            explanation = LocalizedText(
                "Fc4+ aligne le Roi en f7 et la Dame en g8 sur la même diagonale. Le Roi doit s'écarter, livrant sa Dame !",
                "Bc4+ skewers the King on f7 and Queen on g8 along the same diagonal. The King steps aside and the Queen falls!"
            )
        ),

        // --- MAT EN 2 (Elo 1200 - 1700) ---
        TacticalPuzzle(
            id = "puz_m2_1",
            initialFen = "2r3k1/5ppp/8/8/8/8/3R1PPP/3R2K1 w - - 0 1",
            solutionUci = listOf("d2d8", "c8d8", "d1d8"),
            theme = PuzzleTheme.MATE_IN_2,
            elo = 1220,
            title = LocalizedText("Mat en 2 du couloir", "Back-Rank Mate in 2"),
            explanation = LocalizedText(
                "1.Td8+! attire la Tour noire sur d8 (1...Txd8 forcé), permettant 2.Txd8# mat du couloir imparable !",
                "1.Rd8+! forces Black's Rook onto d8 (1...Rxd8), allowing 2.Rxd8# unstoppable back-rank checkmate!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m2_2",
            initialFen = "5rk1/4Qppp/8/8/B7/8/5PPP/4R1K1 w - - 0 1",
            solutionUci = listOf("e7f8", "g8f8", "e1e8"),
            theme = PuzzleTheme.MATE_IN_2,
            elo = 1380,
            title = LocalizedText("Sacrifice de Dame en f8", "Queen Sacrifice on f8"),
            explanation = LocalizedText(
                "1.Dxf8+!! sacrifie la Dame pour attirer le Roi noir sur f8 (1...Rxf8 forcé), puis 2.Te8# délivre le mat du couloir protégé par le Fou a4 !",
                "1.Qxf8+!! sacrifices the Queen to force the King onto f8 (1...Kxf8), followed by 2.Re8# back-rank mate protected by the Bishop on a4!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m2_3",
            initialFen = "6k1/4bppp/8/8/8/BQ6/5PPP/6K1 w - - 0 1",
            solutionUci = listOf("b3b8", "e7f8", "b8f8"),
            theme = PuzzleTheme.MATE_IN_2,
            elo = 1280,
            title = LocalizedText("Échec et mat en 2 sur case faible", "Queen Mate in 2"),
            explanation = LocalizedText(
                "1.Db8+! force le Fou à s'interposer en f8 (1...Ff8), puis 2.Dxf8# capture le Fou pour le mat !",
                "1.Qb8+! forces the Bishop to block on f8 (1...Bf8), then 2.Qxf8# delivers checkmate!"
            )
        ),

        // --- ATTRACTION / DÉVIATION & SACRIFICE (Elo 1400 - 1950) ---
        TacticalPuzzle(
            id = "puz_attr_1",
            initialFen = "2r3k1/5ppp/8/2q5/8/8/5PPP/2Q1R1K1 w - - 0 1",
            solutionUci = listOf("c1c5", "c8c5", "e1e8"),
            theme = PuzzleTheme.ATTRACTION,
            elo = 1420,
            title = LocalizedText("Déviation du défenseur", "Deflecting the Defender"),
            explanation = LocalizedText(
                "La Tour en c8 surveille la 8e rangée, mais elle doit aussi défendre sa Dame en c5 ! 1.Dxc5! dévie la Tour (1...Txc5) et 2.Te8# mate !",
                "Black's Rook on c8 is overloaded defending both the Queen on c5 and the back rank. 1.Qxc5! deflects the Rook (1...Rxc5) for 2.Re8#!"
            )
        ),
        TacticalPuzzle(
            id = "puz_attr_2",
            initialFen = "r4rk1/1pp2ppp/p1np1q2/8/2B1P1b1/2N2N2/PPP2PPP/R2Q1RK1 b - - 0 1",
            solutionUci = listOf("g4f3", "d1f3", "f6f3"),
            theme = PuzzleTheme.ATTRACTION,
            elo = 1520,
            title = LocalizedText("Éliminer le défenseur (Noirs)", "Eliminate the Defender (Black)"),
            explanation = LocalizedText(
                "Avec les Noirs : 1...Fxf3! détruit la structure du roque blanc. Si 2.Dxf3 alors 2...Dxf3 3.gxf3 laisse les Blancs avec des pions doublés fragiles et le Cavalier c3 sans appui.",
                "With Black: 1...Bxf3! shatters the White kingside structure. After 2.Qxf3 Qxf3 3.gxf3 White's king safety collapses."
            )
        ),
        TacticalPuzzle(
            id = "puz_sac_1",
            initialFen = "r1bq1rk1/pppn1ppp/4pn2/3p4/1b1P4/2NBPN2/PPP2PPP/R1BQK2R w KQ - 0 7",
            solutionUci = listOf("d3h7", "g8h7", "f3g5"),
            theme = PuzzleTheme.SACRIFICE,
            elo = 1650,
            title = LocalizedText("Le Cadeau Grec (Fxh7+)", "The Greek Gift (Bxh7+)"),
            explanation = LocalizedText(
                "Classique immortel ! 1.Fxh7+! détruit le roque noir (1...Rxh7), puis 2.Cg5+! ouvre la voie à la Dame (Dh5+) pour une attaque décisive.",
                "A timeless classic! 1.Bxh7+! shatters the kingside shield (1...Kxh7), and 2.Ng5+! clears the path for Qh5+ with a crushing attack."
            )
        ),
        TacticalPuzzle(
            id = "puz_sac_2",
            initialFen = "r2q1rk1/ppp2ppp/2n5/2b1p3/2B1P1b1/2NP1N2/PPP2PPP/R1BQ1RK1 w - - 0 8",
            solutionUci = listOf("c4f7", "f8f7", "f3g5"),
            theme = PuzzleTheme.SACRIFICE,
            elo = 1760,
            title = LocalizedText("Sacrifice en f7 + Fourchette", "f7 Sacrifice into Fork"),
            explanation = LocalizedText(
                "1.Fxf7+! attire la Tour en f7 (1...Txf7), puis 2.Cg5! attaque simultanément la Tour clouée/isolée en f7 et le Fou en g4.",
                "1.Bxf7+! lures the Rook to f7 (1...Rxf7), and 2.Ng5! attacks the f7 Rook and g4 Bishop simultaneously."
            )
        ),
        TacticalPuzzle(
            id = "puz_m1_7",
            initialFen = "6k1/8/6K1/8/8/8/8/3Q4 w - - 0 1",
            solutionUci = listOf("d1d8"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 520,
            title = LocalizedText("Mat du Roi soutenu", "Supported Back-Rank Mate"),
            explanation = LocalizedText(
                "Votre Roi en g6 coupe les cases f7, g7 et h7 : 1.Dd8# délivre un mat immédiat sur la 8e rangée.",
                "Your King on g6 cuts off f7, g7, and h7: 1.Qd8# delivers an immediate back-rank checkmate."
            )
        ),
        TacticalPuzzle(
            id = "puz_m1_8",
            initialFen = "5rk1/6p1/6P1/8/8/8/7Q/6K1 w - - 0 1",
            solutionUci = listOf("h2h7"),
            theme = PuzzleTheme.MATE_IN_1,
            elo = 710,
            title = LocalizedText("Le Mat de Damiano", "Damiano's Mate"),
            explanation = LocalizedText(
                "Soutenu par votre pion avancé en g6, 1.Dh7# délivre le classique Mat de Damiano en enfermant le Roi noir.",
                "Supported by your advanced pawn on g6, 1.Qh7# delivers the classic Damiano's Mate."
            )
        ),
        TacticalPuzzle(
            id = "puz_fork_5",
            initialFen = "8/r5k1/8/8/8/8/5B2/6K1 w - - 0 1",
            solutionUci = listOf("f2d4", "g7f7", "d4a7"),
            theme = PuzzleTheme.FORK,
            elo = 890,
            title = LocalizedText("Fourchette diagonale du Fou", "Diagonal Bishop Fork"),
            explanation = LocalizedText(
                "1.Fd4+! attaque simultanément le Roi en g7 et la Tour isolée en a7. Le Roi doit bouger et la Tour tombe !",
                "1.Bd4+! forks the Black King on g7 and the undefended Rook on a7. The King must step aside, winning the Rook!"
            )
        ),
        TacticalPuzzle(
            id = "puz_pin_4",
            initialFen = "3qk3/8/5n2/8/8/8/8/2B3K1 w - - 0 1",
            solutionUci = listOf("c1g5"),
            theme = PuzzleTheme.PIN,
            elo = 1050,
            title = LocalizedText("Clouage relatif sur la Dame", "Relative Pin on the Queen"),
            explanation = LocalizedText(
                "1.Fg5! cloue le Cavalier f6 devant la Dame d8 : s'il bouge, la Dame noire tombe !",
                "1.Bg5! pins the f6 Knight against the Queen on d8—if the Knight moves, the Queen falls!"
            )
        ),
        TacticalPuzzle(
            id = "puz_skewer_3",
            initialFen = "q7/8/8/8/k7/8/8/3R2K1 w - - 0 1",
            solutionUci = listOf("d1a1"),
            theme = PuzzleTheme.SKEWER,
            elo = 1180,
            title = LocalizedText("Enfilade verticale sur colonne a", "a-File Vertical Skewer"),
            explanation = LocalizedText(
                "1.Ta1+! force le Roi noir en a4 à s'écarter, exposant la Dame derrière lui en a8 !",
                "1.Ra1+! forces the Black King on a4 to step aside, leaving the Queen on a8 defenseless behind it!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m2_4",
            initialFen = "6k1/2n2ppp/8/8/8/4Q3/5PPP/4R1K1 w - - 0 1",
            solutionUci = listOf("e3e8", "c7e8", "e1e8"),
            theme = PuzzleTheme.MATE_IN_2,
            elo = 1460,
            title = LocalizedText("Éliminer le Cavalier gardien", "Eliminate the Guarding Knight"),
            explanation = LocalizedText(
                "1.De8+!! force le Cavalier en c7 à reprendre (1...Cxe8), ouvrant la 8e rangée pour 2.Txe8#.",
                "1.Qe8+!! forces Black's Knight to capture (1...Nxe8), clearing the way for 2.Rxe8# checkmate!"
            )
        ),
        TacticalPuzzle(
            id = "puz_m2_5",
            initialFen = "3r2k1/5ppp/4q3/8/8/1B5P/5PP1/3R2K1 w - - 0 1",
            solutionUci = listOf("d1d8", "e6e8", "d8e8"),
            theme = PuzzleTheme.MATE_IN_2,
            elo = 1320,
            title = LocalizedText("Interposition forcée en e8", "Forced Interposition on e8"),
            explanation = LocalizedText(
                "1.Txd8+! La Dame noire en e6 est forcée de s'interposer (1...De8), puis 2.Txe8# conclut la partie.",
                "1.Rxd8+! Black's Queen is forced to block on e8 (1...Qe8), after which 2.Rxe8# is checkmate."
            )
        ),
        TacticalPuzzle(
            id = "puz_attr_3",
            initialFen = "5rk1/5pp1/3q4/6N1/8/8/5PP1/6KR w - - 0 1",
            solutionUci = listOf("h1h8", "g8h8", "g5f7"),
            theme = PuzzleTheme.ATTRACTION,
            elo = 1620,
            title = LocalizedText("Sacrifice d'attraction en h8", "Corner Attraction Sacrifice"),
            explanation = LocalizedText(
                "1.Th8+!! attire le Roi noir sur h8 (1...Rxh8 forcé), où 2.Cxf7+! réalise une fourchette royale sur le Roi et la Dame en d6 !",
                "1.Rh8+!! lures the Black King onto h8 (1...Kxh8 forced), walking right into the 2.Nxf7+! royal fork on King and Queen!"
            )
        ),
        TacticalPuzzle(
            id = "puz_sac_3",
            initialFen = "5r1k/6pp/7N/8/8/1Q6/6PP/6K1 w - - 0 1",
            solutionUci = listOf("b3g8", "f8g8", "h6f7"),
            theme = PuzzleTheme.SACRIFICE,
            elo = 1880,
            title = LocalizedText("Le Legs de Philidor (Mat étouffé)", "Philidor's Legacy (Smothered Mate)"),
            explanation = LocalizedText(
                "Un chef-d'œuvre absolu : 1.Dg8+!! sacrifie la Dame pour obliger la Tour à bloquer l'échappatoire du Roi (1...Txg8), permettant 2.Cf7#!",
                "An immortal tactical gem: 1.Qg8+!! forces the Rook to seal its own King's escape square (1...Rxg8), setting up 2.Nf7#!"
            )
        )
    )
}
