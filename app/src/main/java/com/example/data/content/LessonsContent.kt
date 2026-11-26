package com.example.data.content

import com.example.domain.model.Lesson
import com.example.domain.model.LessonSection
import com.example.domain.model.LocalizedText

object LessonsContent {

    val lessons: List<Lesson> = listOf(
        // 1. Déplacement des pièces
        Lesson(
            id = "lesson_pawn",
            section = LessonSection.PIECE_MOVEMENT,
            title = LocalizedText("Le Pion : avance et capture", "The Pawn: Advance & Capture"),
            summary = LocalizedText(
                "Avance tout droit, mais capture en diagonale.",
                "Moves straight forward, captures diagonally."
            ),
            explanation = LocalizedText(
                "Le pion avance d'une case vers l'avant (ou de deux cases depuis sa case de départ). Il ne recule jamais et capture uniquement en diagonale d'une case vers l'avant.",
                "Pawns move one square forward (or two squares from their starting rank). They never move backward and capture only one square diagonally forward."
            ),
            initialFen = "4k3/8/8/8/4p3/3P4/8/4K3 w - - 0 1",
            objective = LocalizedText(
                "Capture le pion noir en e4 avec ton pion d3.",
                "Capture the black pawn on e4 with your d3 pawn."
            ),
            expectedMovesUci = listOf("d3e4"),
            highlightSquares = listOf("d3", "e4"),
            hintText = LocalizedText("Touche le pion en d3 puis la case e4.", "Tap the d3 pawn, then tap e4.")
        ),
        Lesson(
            id = "lesson_rook",
            section = LessonSection.PIECE_MOVEMENT,
            title = LocalizedText("La Tour : lignes et colonnes", "The Rook: Ranks & Files"),
            summary = LocalizedText(
                "Glisse horizontalement et verticalement sans sauter.",
                "Slides horizontally and vertically without jumping."
            ),
            explanation = LocalizedText(
                "La Tour vaut 5 points. Elle se déplace d'autant de cases libres qu'elle le souhaite en ligne droite (horizontalement ou verticalement). Elle est redoutable sur les colonnes ouvertes.",
                "The Rook is worth 5 points. It moves any number of unobstructed squares horizontally or vertically. It excels on open files."
            ),
            initialFen = "4k3/8/8/8/3R3r/8/8/4K3 w - - 0 1",
            objective = LocalizedText(
                "Traverse l'échiquier pour capturer la tour noire en h4.",
                "Slide across the rank to capture the black rook on h4."
            ),
            expectedMovesUci = listOf("d4h4"),
            highlightSquares = listOf("d4", "h4"),
            hintText = LocalizedText("Déplace la tour blanche de d4 jusqu'en h4.", "Move the white rook from d4 to h4.")
        ),
        Lesson(
            id = "lesson_knight",
            section = LessonSection.PIECE_MOVEMENT,
            title = LocalizedText("Le Cavalier : le saut en L", "The Knight: The L-Shape Jump"),
            summary = LocalizedText(
                "Seule pièce capable de sauter par-dessus les autres.",
                "The only piece able to jump over other pieces."
            ),
            explanation = LocalizedText(
                "Le Cavalier se déplace en forme de « L » : 2 cases dans une direction puis 1 case à angle droit. Il change toujours de couleur de case à chaque coup.",
                "The Knight moves in an L-shape: two squares in one direction and one square perpendicular. It always lands on a square of the opposite color."
            ),
            initialFen = "4k3/8/8/4q3/2PP4/3N4/8/4K3 w - - 0 1",
            objective = LocalizedText(
                "Saute par-dessus tes pions pour capturer la dame noire en e5.",
                "Jump over your pawns to capture the black queen on e5."
            ),
            expectedMovesUci = listOf("d3e5"),
            highlightSquares = listOf("d3", "e5"),
            hintText = LocalizedText("De d3, saute de 2 rangées vers le haut et 1 colonne à droite (e5).", "From d3, jump 2 ranks up and 1 file right (e5).")
        ),
        Lesson(
            id = "lesson_bishop",
            section = LessonSection.PIECE_MOVEMENT,
            title = LocalizedText("Le Fou : maître des diagonales", "The Bishop: Master of Diagonals"),
            summary = LocalizedText(
                "Glisse uniquement sur les diagonales de sa couleur.",
                "Slides along diagonals of a single square color."
            ),
            explanation = LocalizedText(
                "Chaque joueur commence avec un fou de cases blanches et un fou de cases noires. Deux fous ensemble forment la « paire de fous », très puissante en position ouverte.",
                "Each player starts with a light-squared and a dark-squared bishop. Together they form the bishop pair, a major asset in open positions."
            ),
            initialFen = "4k3/8/6b1/8/8/3B4/8/4K3 w - - 0 1",
            objective = LocalizedText(
                "Capture le fou noir en g6 en suivant la grande diagonale.",
                "Capture the black bishop on g6 along the diagonal."
            ),
            expectedMovesUci = listOf("d3g6"),
            highlightSquares = listOf("d3", "g6"),
            hintText = LocalizedText("Déplace ton fou de d3 à g6.", "Move your bishop from d3 to g6.")
        ),
        Lesson(
            id = "lesson_queen",
            section = LessonSection.PIECE_MOVEMENT,
            title = LocalizedText("La Dame : puissance maximale", "The Queen: Ultimate Power"),
            summary = LocalizedText(
                "Combine la puissance de la Tour et du Fou.",
                "Combines the movement of a Rook and a Bishop."
            ),
            explanation = LocalizedText(
                "Avec une valeur de 9 points, la Dame est la pièce la plus forte du jeu. Évite cependant de la sortir trop tôt dans l'ouverture, car elle devient une cible facile.",
                "Worth 9 points, the Queen is the strongest piece. Avoid bringing her out too early in the opening, where minor pieces can chase her."
            ),
            initialFen = "4k3/8/8/7r/8/8/4Q3/4K3 w - - 0 1",
            objective = LocalizedText(
                "Utilise la diagonale de la Dame pour capturer la tour en h5.",
                "Use the Queen's diagonal to capture the rook on h5."
            ),
            expectedMovesUci = listOf("e2h5"),
            highlightSquares = listOf("e2", "h5"),
            hintText = LocalizedText("De e2, glisse en diagonale vers h5.", "From e2, slide diagonally to h5.")
        ),
        Lesson(
            id = "lesson_king",
            section = LessonSection.PIECE_MOVEMENT,
            title = LocalizedText("Le Roi : cœur de la partie", "The King: Heart of the Game"),
            summary = LocalizedText(
                "Avance d'une seule case, jamais sur une case attaquée.",
                "Moves one square in any direction, never into check."
            ),
            explanation = LocalizedText(
                "Le Roi ne peut jamais être capturé ni se mettre en échec. En début de partie on le met à l'abri, mais en finale le Roi devient une pièce d'attaque essentielle !",
                "The King can never move into check. Keep him safe early on, but activate him as an attacking piece during the endgame!"
            ),
            initialFen = "8/8/8/4k3/8/4p3/4K3/8 w - - 0 1",
            objective = LocalizedText(
                "Capture le pion noir isolé en e3 avec ton Roi.",
                "Capture the undefended black pawn on e3 with your King."
            ),
            expectedMovesUci = listOf("e2e3"),
            highlightSquares = listOf("e2", "e3"),
            hintText = LocalizedText("Le pion en e3 n'est pas défendu : avance le Roi en e3.", "The e3 pawn is undefended: step the King to e3.")
        ),

        // 2. Règles spéciales
        Lesson(
            id = "lesson_castling",
            section = LessonSection.SPECIAL_RULES,
            title = LocalizedText("Le Roque : sécurité et développement", "Castling: Safety & Development"),
            summary = LocalizedText(
                "Seul coup où deux pièces bougent en même temps.",
                "The only move where two pieces move at once."
            ),
            explanation = LocalizedText(
                "Le roque met le Roi à l'abri et active la Tour. Conditions : ni le Roi ni la Tour n'ont encore bougé, aucune pièce entre eux, et le Roi n'est pas en échec ni ne traverse une case attaquée.",
                "Castling tucks the King to safety and activates a Rook. Requirements: neither King nor Rook has moved, squares between them are empty, and the King doesn't start, pass through, or land in check."
            ),
            initialFen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1",
            objective = LocalizedText(
                "Effectue le petit roque (O-O) en déplaçant le Roi de e1 à g1.",
                "Perform kingside castling (O-O) by moving the King from e1 to g1."
            ),
            expectedMovesUci = listOf("e1g1"),
            highlightSquares = listOf("e1", "g1", "h1"),
            hintText = LocalizedText("Touche le Roi en e1, puis touche la case g1.", "Tap the King on e1, then tap g1.")
        ),
        Lesson(
            id = "lesson_en_passant",
            section = LessonSection.SPECIAL_RULES,
            title = LocalizedText("La Prise en Passant", "En Passant Capture"),
            summary = LocalizedText(
                "Capture spéciale d'un pion qui avance de deux cases.",
                "Special pawn capture after a two-square pawn advance."
            ),
            explanation = LocalizedText(
                "Quand un pion ennemi avance de 2 cases depuis sa position initiale et se retrouve juste à côté du vôtre (sur la 5e rangée pour les Blancs), vous pouvez le capturer en diagonale sur la case qu'il vient de traverser — uniquement au coup suivant !",
                "When an enemy pawn advances two squares from its start square and lands next to yours, you may capture it diagonally on the square it skipped — on the very next turn only!"
            ),
            initialFen = "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1",
            objective = LocalizedText(
                "Les Noirs viennent de jouer d7-d5. Prends en passant en d6 !",
                "Black just played d7-d5. Capture en passant on d6!"
            ),
            expectedMovesUci = listOf("e5d6"),
            highlightSquares = listOf("e5", "d6"),
            hintText = LocalizedText("Déplace ton pion de e5 sur la case vide d6.", "Move your e5 pawn to the empty square d6.")
        ),
        Lesson(
            id = "lesson_promotion",
            section = LessonSection.SPECIAL_RULES,
            title = LocalizedText("La Promotion du Pion", "Pawn Promotion"),
            summary = LocalizedText(
                "Un pion qui atteint la 8e rangée se transforme.",
                "A pawn reaching the 8th rank transforms into a new piece."
            ),
            explanation = LocalizedText(
                "Lorsqu'un pion atteint la dernière rangée, il se transforme immédiatement en Dame, Tour, Fou ou Cavalier. La Dame est choisie dans 95 % des cas, mais parfois le Cavalier évite un pat ou donne un échec immédiat.",
                "When a pawn reaches the far edge of the board, it must promote into a Queen, Rook, Bishop, or Knight. Usually a Queen is best, though underpromotion can avoid stalemate."
            ),
            initialFen = "4k3/P7/8/8/8/8/8/4K3 w - - 0 1",
            objective = LocalizedText(
                "Avance le pion a7 en a8 et choisis la Dame pour le promouvoir.",
                "Push the a7 pawn to a8 and promote it to a Queen."
            ),
            expectedMovesUci = listOf("a7a8q"),
            highlightSquares = listOf("a7", "a8"),
            hintText = LocalizedText("Pousse le pion de a7 à a8 puis sélectionne la Dame.", "Move the pawn from a7 to a8 and pick the Queen.")
        ),

        // 3. Notions de base
        Lesson(
            id = "lesson_checkmate",
            section = LessonSection.BASIC_CONCEPTS,
            title = LocalizedText("Échec, Mat et Mat du Couloir", "Check, Checkmate & Back-Rank Mate"),
            summary = LocalizedText(
                "Le Roi est attaqué et n'a aucune fuite légale.",
                "The King is in check with no legal escape."
            ),
            explanation = LocalizedText(
                "Un joueur est en échec et mat quand son Roi est attaqué et qu'il ne peut ni fuir, ni capturer l'attaquant, ni interposer une pièce. La partie s'arrête immédiatement.",
                "Checkmate occurs when the King is attacked and cannot escape, capture the attacker, or block the check. The game ends immediately."
            ),
            initialFen = "6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1",
            objective = LocalizedText(
                "Délivre le mat du couloir en envoyant la Tour sur la 8e rangée.",
                "Deliver a back-rank checkmate by moving your Rook to the 8th rank."
            ),
            expectedMovesUci = listOf("a1a8"),
            highlightSquares = listOf("a1", "a8"),
            hintText = LocalizedText("Joue Ta8# (a1 vers a8).", "Play Ra8# (a1 to a8).")
        ),
        Lesson(
            id = "lesson_stalemate",
            section = LessonSection.BASIC_CONCEPTS,
            title = LocalizedText("Éviter le Pat (Stalemate)", "Avoiding Stalemate"),
            summary = LocalizedText(
                "Pas d'échec + aucun coup légal = partie nulle !",
                "Not in check + no legal moves = immediate draw!"
            ),
            explanation = LocalizedText(
                "Attention : si votre adversaire n'est PAS en échec mais n'a plus aucun coup légal, c'est PAT (match nul) ! Ici, jouer Df7 priverait le Roi noir de toute case sans le mettre en échec. Trouvez plutôt le mat !",
                "Careful: if your opponent is NOT in check and has no legal moves, it is STALEMATE (a draw). Here, Qf7 would trap the King with no check. Find the checkmate instead!"
            ),
            initialFen = "7k/8/6K1/8/8/8/8/5Q2 w - - 0 1",
            objective = LocalizedText(
                "Évite le pat (Df7) et joue le coup gagnant qui met échec et mat !",
                "Avoid stalemate (Qf7) and play the checkmating move!"
            ),
            expectedMovesUci = listOf("f1f8"),
            highlightSquares = listOf("f1", "f8"),
            hintText = LocalizedText("Descends la Dame sur la 8e rangée : Df8# (f1 vers f8).", "Bring the Queen to the 8th rank: Qf8# (f1 to f8).")
        ),

        // 4. Notation algébrique
        Lesson(
            id = "lesson_notation",
            section = LessonSection.ALGEBRAIC_NOTATION,
            title = LocalizedText("Lire la notation algébrique", "Reading Algebraic Notation"),
            summary = LocalizedText(
                "Colonnes a-h, rangées 1-8, initiales des pièces.",
                "Files a-h, ranks 1-8, and piece initials."
            ),
            explanation = LocalizedText(
                "Chaque case a un nom unique : colonne (a à h) + rangée (1 à 8). Pour noter un coup : R=Roi (K), D=Dame (Q), T=Tour (R), F=Fou (B), C=Cavalier (N), rien pour le pion. « x » indique une capture, « + » un échec, « # » un mat.",
                "Every square has a coordinate: file (a-h) + rank (1-8). Piece letters in English: K=King, Q=Queen, R=Rook, B=Bishop, N=Knight, none for pawns. 'x' is capture, '+' is check, '#' is checkmate."
            ),
            initialFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1",
            objective = LocalizedText(
                "Après 1.e4 e5, on joue souvent Cf3 (Cavalier en f3). Joue g1 → f3 !",
                "Move the Knight to f3 (Nf3) by playing g1 → f3!"
            ),
            expectedMovesUci = listOf("g1f3"),
            highlightSquares = listOf("g1", "f3"),
            hintText = LocalizedText("Touche le cavalier en g1 puis la case f3.", "Tap the Knight on g1, then tap f3.")
        ),

        // 5. Premiers motifs tactiques (pour faire le pont vers les puzzles)
        Lesson(
            id = "lesson_fork",
            section = LessonSection.BASIC_CONCEPTS,
            title = LocalizedText("La Fourchette Royale du Cavalier", "The Royal Knight Fork"),
            summary = LocalizedText(
                "Attaquer simultanément le Roi et la Tour.",
                "Attacking the King and Rook at the exact same time."
            ),
            explanation = LocalizedText(
                "Une fourchette menace deux cibles en un seul coup. Quand l'une d'elles est le Roi, l'adversaire est obligé de parer l'échec et perd l'autre pièce !",
                "A fork attacks two targets in a single move. When one target is the King, the opponent must answer the check and loses the second piece!"
            ),
            initialFen = "r3k3/8/8/3N4/8/8/8/4K3 w - - 0 1",
            objective = LocalizedText(
                "Saute en c7 pour faire échec au Roi et attaquer la Tour a8 en même temps !",
                "Jump to c7 to check the King and fork the a8 Rook simultaneously!"
            ),
            expectedMovesUci = listOf("d5c7"),
            highlightSquares = listOf("d5", "c7", "a8", "e8"),
            hintText = LocalizedText("Déplace le Cavalier de d5 vers c7.", "Move the Knight from d5 to c7.")
        ),
        Lesson(
            id = "lesson_pin",
            section = LessonSection.BASIC_CONCEPTS,
            title = LocalizedText("Le Clouage Absolu", "The Absolute Pin"),
            summary = LocalizedText(
                "Immobiliser une pièce devant son propre Roi.",
                "Freezing a piece in front of its own King."
            ),
            explanation = LocalizedText(
                "Une pièce est « clouée de façon absolue » lorsqu'elle ne peut légalement pas bouger car cela découvrirait son propre Roi à un échec.",
                "A piece is 'absolutely pinned' when moving it would expose its own King to check, making any move by that piece illegal."
            ),
            initialFen = "4k3/4n3/8/8/8/8/8/3R2K1 w - - 0 1",
            objective = LocalizedText(
                "Place ta Tour sur la colonne e (d1 → e1) pour clouer le Cavalier e7 devant son Roi !",
                "Slide your Rook to the e-file (d1 → e1) to pin the e7 Knight to its King!"
            ),
            expectedMovesUci = listOf("d1e1"),
            highlightSquares = listOf("d1", "e1", "e7", "e8"),
            hintText = LocalizedText("Joue Te1 (d1 vers e1).", "Play Re1 (d1 to e1).")
        ),
        Lesson(
            id = "lesson_discovered",
            section = LessonSection.BASIC_CONCEPTS,
            title = LocalizedText("L'Échec à la Découverte", "The Discovered Check"),
            summary = LocalizedText(
                "Une pièce s'écarte pour libérer l'attaque d'une Tour ou d'un Fou.",
                "One piece steps aside to unleash a hidden attack behind it."
            ),
            explanation = LocalizedText(
                "En déplaçant votre Fou de la colonne e, vous libérez l'échec de votre Tour contre le Roi noir tout en attaquant une autre cible gratuitement !",
                "By moving your Bishop off the e-file, you unleash your Rook's check while attacking another target for free!"
            ),
            initialFen = "rn2k3/8/8/8/4B3/8/4R3/6K1 w - - 0 1",
            objective = LocalizedText(
                "Écarte ton Fou vers b7 pour découvrir l'échec de la Tour et menacer la Tour a8 !",
                "Move your Bishop to b7 to unleash a discovered check and attack the a8 Rook!"
            ),
            expectedMovesUci = listOf("e4b7"),
            highlightSquares = listOf("e4", "b7", "a8", "e8"),
            hintText = LocalizedText("Joue Fb7+ (e4 vers b7).", "Play Bb7+ (e4 to b7).")
        )
    )
}
