package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.bot.BotPersona
import com.example.domain.bot.BotPlayer
import com.example.domain.bot.BotProfiles
import com.example.domain.bot.CoachVoice
import com.example.domain.bot.Grade
import com.example.domain.bot.MoveCoach
import com.example.domain.bot.MoveNote
import com.example.domain.bot.RatingChange
import com.example.domain.bot.Searcher
import com.example.domain.chess.ChessEngine
import com.example.domain.chess.PgnParser
import com.example.domain.chess.rememberChessEngine
import com.example.domain.model.GameOutcome
import com.example.ui.feedback.LocalGameFeedback
import com.example.ui.feedback.Sfx
import com.example.domain.model.PieceColor
import com.example.domain.model.Square
import com.example.ui.GameReviewData
import com.example.ui.components.BotMonogram
import com.example.ui.components.InkButton
import com.example.ui.components.KChessBoard
import com.example.ui.components.LineButton
import com.example.ui.components.SectionLabel
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import com.github.bhlangonijr.chesslib.Board
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun GameScreen(
    persona: BotPersona,
    botElo: Int,
    playerElo: Int,
    colorChoice: Int,
    minutes: Int,
    showCoordinates: Boolean,
    onComplete: suspend (score: Double, rated: Boolean) -> RatingChange,
    onSaveGame: (String, String, String, String, Int, Boolean, Int) -> Unit,
    onOpenReview: (GameReviewData) -> Unit,
    onRematch: () -> Unit,
    onExit: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val scope = rememberCoroutineScope()
    val botName = if (isEn) persona.nameEn else persona.nameFr
    val userLabel = tr("Joueur", "Player")

    val feedback = LocalGameFeedback.current
    val engine = rememberChessEngine()
    val engineState by engine.state.collectAsStateWithLifecycle()
    val board = engineState.board
    val n = engineState.moveHistoryUci.size

    val userWhite by rememberSaveable {
        mutableStateOf(
            when (colorChoice) {
                0 -> true
                1 -> false
                else -> Random.nextBoolean()
            }
        )
    }
    val userColor = if (userWhite) PieceColor.WHITE else PieceColor.BLACK

    var result by remember { mutableStateOf<String?>(null) }
    var ratingChange by remember { mutableStateOf<RatingChange?>(null) }
    var botThinking by remember { mutableStateOf(false) }
    var hintArrow by remember { mutableStateOf<Pair<Square, Square>?>(null) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var takebacks by remember { mutableIntStateOf(0) }
    var showResign by remember { mutableStateOf(false) }
    var whiteClock by remember { mutableIntStateOf(minutes * 60) }
    var blackClock by remember { mutableIntStateOf(minutes * 60) }

    // positions[i] = FEN avant le coup i ; positions[n] = position actuelle
    val positions = remember { mutableStateListOf<String>() }
    val notes = remember { mutableStateMapOf<Int, MoveNote>() }
    var analyzing by remember { mutableStateOf(false) }

    val rated = hintsUsed == 0 && takebacks == 0

    fun finish(res: String) {
        if (result != null) return
        result = res
        val score = when (res) {
            "1-0" -> if (userWhite) 1.0 else 0.0
            "0-1" -> if (userWhite) 0.0 else 1.0
            else -> 0.5
        }
        feedback?.let { fb ->
            scope.launch {
                kotlinx.coroutines.delay(450) // laisse finir le son du dernier coup
                fb.onGameOver(if (score > 0.75) Sfx.WIN else if (score < 0.25) Sfx.LOSE else Sfx.DRAW)
            }
        }
        val san = engine.state.value.moveHistorySan
        val userName = userLabel
        val whiteName = if (userWhite) userName else "$botName ($botElo)"
        val blackName = if (userWhite) "$botName ($botElo)" else userName
        val pgn = PgnParser.exportPgn(san, whiteName, blackName, res)
        onSaveGame(pgn, res, whiteName, blackName, botElo, userWhite, (san.size + 1) / 2)
        scope.launch { ratingChange = onComplete(score, hintsUsed == 0 && takebacks == 0) }
    }

    // Suivi des positions + analyse du coach après chaque coup du joueur
    LaunchedEffect(n) {
        while (positions.size > n) positions.removeAt(positions.size - 1)
        val before = positions.getOrNull(n - 1)
        positions.add(engineState.fen)
        notes.keys.filter { it > n }.forEach { notes.remove(it) }

        val userJustMoved = n > 0 && ((n % 2 == 1) == userWhite)
        if (userJustMoved && before != null && !notes.containsKey(n)) {
            val uci = engineState.moveHistoryUci[n - 1]
            analyzing = true
            val note = withContext(Dispatchers.Default) { MoveCoach.analyzeMove(before, uci, n) }
            if (note != null && engine.state.value.moveHistoryUci.size >= n) notes[n] = note
            analyzing = false
        }
    }

    // Horloges
    LaunchedEffect(result, board.turn, minutes) {
        if (minutes <= 0 || result != null) return@LaunchedEffect
        while (result == null) {
            delay(1000L)
            if (board.turn == PieceColor.WHITE) {
                if (whiteClock > 0) whiteClock--
                if (whiteClock <= 0) finish("0-1")
            } else {
                if (blackClock > 0) blackClock--
                if (blackClock <= 0) finish("1-0")
            }
        }
    }

    // Fin de partie par les règles + tour du bot
    LaunchedEffect(engineState.fen, result) {
        if (result != null) return@LaunchedEffect
        val out = engineState.outcome
        if (out != GameOutcome.ONGOING) {
            finish(
                when (out) {
                    GameOutcome.CHECKMATE_WHITE_WINS -> "1-0"
                    GameOutcome.CHECKMATE_BLACK_WINS -> "0-1"
                    else -> "1/2-1/2"
                }
            )
            return@LaunchedEffect
        }
        if (board.turn != userColor) {
            botThinking = true
            delay(300)
            val fen = engineState.fen
            val uci = withContext(Dispatchers.Default) {
                BotPlayer.chooseMove(fen, BotProfiles.forElo(botElo, persona.aggression))?.let { MoveCoach.uciOf(it) }
            }
            if (uci != null && result == null) {
                hintArrow = null
                engine.playUci(uci)
            }
            botThinking = false
        }
    }

    val latestUserPly = (n downTo 1).firstOrNull { (it % 2 == 1) == userWhite && notes.containsKey(it) }
    val latestNote = latestUserPly?.let { notes[it] }
    val userToMove = board.turn == userColor && result == null && !botThinking

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Barre supérieure
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { if (result == null) showResign = true else onExit() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Retour", "Back"), tint = palette.textPrimary)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (rated) tr("PARTIE CLASSÉE", "RATED GAME") else tr("NON CLASSÉE", "UNRATED"),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp,
                    color = palette.textSecondary
                )
            }

            PlayerStrip(
                name = botName,
                elo = botElo,
                persona = persona,
                status = if (botThinking) tr("réfléchit…", "thinking…") else null,
                clock = if (minutes > 0) (if (userWhite) blackClock else whiteClock) else null,
                active = board.turn != userColor && result == null
            )

            KChessBoard(
                engine = engine,
                flipped = !userWhite,
                showCoordinates = showCoordinates,
                interactive = userToMove,
                hintArrow = hintArrow,
                showStatusHeader = false,
                onMovePlayed = { _, _ -> hintArrow = null }
            )

            PlayerStrip(
                name = tr("Toi", "You"),
                elo = playerElo,
                persona = null,
                status = if (userToMove) tr("à toi", "your move") else null,
                clock = if (minutes > 0) (if (userWhite) whiteClock else blackClock) else null,
                active = userToMove
            )

            if (result == null) {
                CoachPanel(
                    note = latestNote,
                    analyzing = analyzing,
                    moveCount = n,
                    canRetake = latestNote != null && latestNote.grade >= Grade.MISTAKE && !botThinking && n >= 1,
                    onRetake = {
                        // Reprendre le coup : annule la réponse du bot (si elle a eu lieu) et le coup du joueur
                        val toUndo = if ((n % 2 == 1) == userWhite) 1 else 2
                        repeat(toUndo) { engine.undoMove() }
                        takebacks++
                        hintArrow = null
                    }
                )
                MoveStrip(engineState.moveHistorySan)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LineButton(
                        text = tr("Indice", "Hint"),
                        onClick = {
                            scope.launch {
                                val fen = engine.currentFen()
                                val best = withContext(Dispatchers.Default) {
                                    val b = Board()
                                    b.loadFromFen(fen)
                                    Searcher(true).search(b, 3, 700).best
                                }
                                if (best != null) {
                                    val u = MoveCoach.uciOf(best)
                                    val from = Square.fromAlgebraic(u.substring(0, 2))
                                    val to = Square.fromAlgebraic(u.substring(2, 4))
                                    if (from != null && to != null) {
                                        hintArrow = from to to
                                        hintsUsed++
                                    }
                                }
                            }
                        },
                        enabled = userToMove,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.width(18.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    LineButton(
                        text = tr("Abandon", "Resign"),
                        onClick = { showResign = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.width(18.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                }
            } else {
                ResultPanel(
                    result = result!!,
                    userWhite = userWhite,
                    ratingChange = ratingChange,
                    botName = botName,
                    rated = rated,
                    onReview = {
                        onOpenReview(
                            GameReviewData(
                                botId = persona.id,
                                botNameFr = persona.nameFr,
                                botNameEn = persona.nameEn,
                                botElo = botElo,
                                userIsWhite = userWhite,
                                result = result!!,
                                movesUci = engine.state.value.moveHistoryUci,
                                notes = notes.toMap(),
                                ratingChange = ratingChange,
                                rated = rated
                            )
                        )
                    },
                    onRematch = onRematch,
                    onExit = onExit
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showResign) {
        AlertDialog(
            onDismissRequest = { showResign = false },
            containerColor = palette.glassSurface,
            title = {
                Text(
                    if (result == null) tr("Abandonner ?", "Resign?") else tr("Quitter ?", "Leave?"),
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    tr("Une défaite comptera dans ton Elo.", "A loss will count toward your rating."),
                    color = palette.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResign = false
                    finish(if (userWhite) "0-1" else "1-0")
                }) { Text(tr("Abandonner", "Resign"), color = palette.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResign = false }) {
                    Text(tr("Continuer", "Keep playing"), color = palette.textPrimary)
                }
            }
        )
    }
}

private fun formatClock(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

@Composable
private fun PlayerStrip(
    name: String,
    elo: Int,
    persona: BotPersona?,
    status: String?,
    clock: Int?,
    active: Boolean
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) palette.glassSurfaceElevated else palette.glassSurface)
            .border(1.dp, if (active) palette.textPrimary.copy(alpha = 0.55f) else palette.paperTaupe.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (persona != null) {
            BotMonogram(persona, size = 34.dp)
        } else {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(palette.paperTaupe.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) { Text("●", color = palette.textPrimary, fontSize = 12.sp) }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Text(
                text = "Elo $elo" + (status?.let { " · $it" } ?: ""),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = palette.textSecondary
            )
        }
        if (clock != null) {
            Text(
                text = formatClock(clock),
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (clock < 30) palette.error else palette.textPrimary
            )
        }
    }
}

@Composable
private fun CoachPanel(
    note: MoveNote?,
    analyzing: Boolean,
    moveCount: Int,
    canRetake: Boolean,
    onRetake: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val harsh = note != null && note.grade >= Grade.MISTAKE
    val accent = when (note?.grade) {
        Grade.BLUNDER -> palette.error
        Grade.MISTAKE -> palette.error.copy(alpha = 0.85f)
        Grade.INACCURACY -> palette.warning
        else -> palette.textSecondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.glassSurface)
            .border(1.dp, if (harsh) accent else palette.paperTaupe.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(tr("Le coach", "The coach"))
            Spacer(Modifier.weight(1f))
            if (note != null) {
                Text(
                    text = "${note.san} · ${CoachVoice.gradeLabel(note.grade, isEn)}".uppercase(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        val text = when {
            note != null -> CoachVoice.line(note, isEn)
            analyzing -> tr("Je regarde ton coup…", "Checking your move…")
            moveCount == 0 -> tr(
                "Je commente chacun de tes coups. Ne compte pas sur moi pour être gentil : compte sur moi pour que tu progresses.",
                "I comment on every move you play. Don't expect gentle: expect progress."
            )
            else -> tr("Continue. Réfléchis avant de jouer.", "Keep going. Think before you move.")
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textPrimary
        )
        if (note != null && analyzing) {
            Spacer(Modifier.height(4.dp))
        }
        if (canRetake) {
            Spacer(Modifier.height(10.dp))
            LineButton(
                text = tr("Reprendre mon coup", "Take my move back"),
                onClick = onRetake,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.width(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = tr("La partie ne sera plus classée.", "The game will no longer be rated."),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MoveStrip(san: List<String>) {
    val palette = LocalGlassPalette.current
    if (san.isEmpty()) return
    val sb = StringBuilder()
    val start = (san.size - 8).coerceAtLeast(0).let { if (it % 2 == 1) it - 1 else it }
    for (i in start until san.size) {
        if (i % 2 == 0) sb.append("${i / 2 + 1}. ")
        sb.append(san[i]).append(' ')
    }
    Text(
        text = (if (start > 0) "… " else "") + sb.toString().trim(),
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 12.sp,
        color = palette.textSecondary
    )
}

@Composable
private fun ResultPanel(
    result: String,
    userWhite: Boolean,
    ratingChange: RatingChange?,
    botName: String,
    rated: Boolean,
    onReview: () -> Unit,
    onRematch: () -> Unit,
    onExit: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val outcome = when (result) {
        "1-0" -> if (userWhite) 1 else -1
        "0-1" -> if (userWhite) -1 else 1
        else -> 0
    }
    val title = when (outcome) {
        1 -> tr("Victoire", "Victory")
        -1 -> tr("Défaite", "Defeat")
        else -> tr("Nulle", "Draw")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.glassSurfaceElevated)
            .border(1.dp, palette.glassBorderStart, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionLabel(tr("Partie terminée · $result", "Game over · $result"))
        Text(
            text = title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        val rc = ratingChange
        when {
            rc == null -> Text(tr("Calcul de ton Elo…", "Updating your rating…"), color = palette.textSecondary)
            !rated || !rc.rated -> Text(
                tr("Non classée : ton Elo ne bouge pas.", "Unrated: your rating doesn't change."),
                color = palette.textSecondary
            )
            else -> {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${rc.before} → ${rc.after}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = (if (rc.delta >= 0) "+" else "") + rc.delta,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rc.delta >= 0) palette.success else palette.error
                    )
                }
                if (rc.botBefore != rc.botAfter) {
                    Text(
                        text = "$botName : ${rc.botBefore} → ${rc.botAfter}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        InkButton(text = tr("Bilan de la partie", "Game review"), onClick = onReview, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            LineButton(text = tr("Revanche", "Rematch"), onClick = onRematch, modifier = Modifier.weight(1f))
            LineButton(text = tr("Quitter", "Leave"), onClick = onExit, modifier = Modifier.weight(1f))
        }
    }
}
