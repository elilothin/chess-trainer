package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PlayedGameEntity
import com.example.domain.chess.ChessAi
import com.example.domain.chess.ChessEngine
import com.example.domain.chess.PgnParser
import com.example.domain.chess.rememberChessEngine
import com.example.domain.model.GameOutcome
import com.example.domain.model.LionMood
import com.example.domain.model.PieceColor
import com.example.domain.model.Square
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.KChessBoard
import com.example.ui.components.LionCoachBanner
import com.example.ui.components.loc
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun PlayAiScreen(
    playedGames: List<PlayedGameEntity>,
    showCoordinates: Boolean,
    showBackButton: Boolean,
    onSaveGame: (String, String, String, String, Int, Boolean, Int) -> Unit,
    onDeleteGame: (Long) -> Unit,
    onAnalyzeGame: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val scope = rememberCoroutineScope()

    var eloSlider by remember { mutableFloatStateOf(1000f) }
    var colorChoice by remember { mutableIntStateOf(0) } // 0 = White, 1 = Black, 2 = Random
    var timeControlMins by remember { mutableIntStateOf(0) } // 0 = Unlimited, 5, 10

    val engine = rememberChessEngine()
    val engineState by engine.state.collectAsStateWithLifecycle()
    val board = engineState.board

    var gameActive by remember { mutableStateOf(false) }
    var userPlaysWhite by remember { mutableStateOf(true) }
    var hintArrow by remember { mutableStateOf<Pair<Square, Square>?>(null) }
    var gameResultStr by remember { mutableStateOf<String?>(null) }
    var isAiThinking by remember { mutableStateOf(false) }

    var whiteClockSec by remember { mutableIntStateOf(0) }
    var blackClockSec by remember { mutableIntStateOf(0) }

    val aiElo = eloSlider.roundToInt()
    val isEn = LocalIsEnglish.current
    val aiPersonaName = when {
        aiElo < 650 -> loc(isEn, "Léo Endormi", "Sleepy Leo")
        aiElo < 1000 -> loc(isEn, "Petit Lionceau", "Little Cub")
        aiElo < 1400 -> loc(isEn, "Lynx Rusé", "Clever Lynx")
        aiElo < 1800 -> loc(isEn, "Capitaine Puma", "Captain Puma")
        aiElo < 2200 -> loc(isEn, "Tigre Stratège", "Strategist Tiger")
        else -> loc(isEn, "Roi Léon", "King Leo")
    }

    fun finishGame(result: String) {
        if (gameResultStr != null) return
        gameResultStr = result
        val sanMoves = engine.state.value.moveHistorySan
        val pgn = PgnParser.exportPgn(
            movesSan = sanMoves,
            whiteName = if (userPlaysWhite) "Joueur" else "IA ($aiElo)",
            blackName = if (userPlaysWhite) "IA ($aiElo)" else "Joueur",
            result = result
        )
        onSaveGame(
            pgn,
            result,
            if (userPlaysWhite) "Joueur" else "IA ($aiElo)",
            if (userPlaysWhite) "IA ($aiElo)" else "Joueur",
            aiElo,
            userPlaysWhite,
            (sanMoves.size + 1) / 2
        )
    }

    fun startMatch() {
        val playsWhite = when (colorChoice) {
            0 -> true
            1 -> false
            else -> Random.nextBoolean()
        }
        userPlaysWhite = playsWhite
        engine.reset()
        hintArrow = null
        gameResultStr = null
        whiteClockSec = timeControlMins * 60
        blackClockSec = timeControlMins * 60
        gameActive = true
    }

    // Clock countdown if timeControlMins > 0
    LaunchedEffect(gameActive, board.turn, gameResultStr) {
        if (gameActive && timeControlMins > 0 && gameResultStr == null) {
            while (gameActive && gameResultStr == null) {
                delay(1000L)
                if (board.turn == PieceColor.WHITE) {
                    if (whiteClockSec > 0) whiteClockSec--
                    if (whiteClockSec <= 0) finishGame("0-1")
                } else {
                    if (blackClockSec > 0) blackClockSec--
                    if (blackClockSec <= 0) finishGame("1-0")
                }
            }
        }
    }

    // AI Turn & Rule Outcome trigger powered by ChessEngine
    LaunchedEffect(gameActive, engineState.fen, gameResultStr) {
        if (!gameActive || gameResultStr != null) return@LaunchedEffect
        val out = engineState.outcome
        if (out != GameOutcome.ONGOING) {
            val res = when (out) {
                GameOutcome.CHECKMATE_WHITE_WINS -> "1-0"
                GameOutcome.CHECKMATE_BLACK_WINS -> "0-1"
                else -> "1/2-1/2"
            }
            finishGame(res)
            return@LaunchedEffect
        }

        val userColor = if (userPlaysWhite) PieceColor.WHITE else PieceColor.BLACK
        if (board.turn != userColor) {
            isAiThinking = true
            delay(250)
            val (move, eval) = withContext(Dispatchers.Default) {
                val chosen = ChessAi.chooseMoveForElo(board, aiElo)
                val cp = engine.evaluatePositionCp(depth = 1, updateUiState = false)
                chosen to cp
            }
            if (move != null) {
                engine.playMove(move)
                hintArrow = null
            }
            withContext(Dispatchers.Default) {
                engine.evaluatePositionCp(depth = 1, updateUiState = true)
            }
            val _evalLogged = eval
            isAiThinking = false
        } else if (engineState.moveHistoryUci.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                engine.evaluatePositionCp(depth = 1, updateUiState = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (!showBackButton) Modifier.statusBarsPadding() else Modifier)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        if (showBackButton) {
            GlassTopBar(
                title = tr("Jouer contre l'IA", "Play vs AI"),
                subtitle = "Elo $aiElo • ${tr("Moteur local hors ligne", "Offline local engine")}",
                onBack = onBack
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(tr("Jouer contre l'IA", "Play vs AI"), style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
                Text("Elo $aiElo • ${tr("100% Hors ligne", "100% Offline")}", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!gameActive) {
                GlassCard(elevated = true) {
                    Text(
                        text = "${tr("Niveau de l'IA", "AI Strength")}: Elo $aiElo",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.accentCyan
                    )
                    Slider(
                        value = eloSlider,
                        onValueChange = { eloSlider = (it / 50f).roundToInt() * 50f },
                        valueRange = 400f..2500f,
                        colors = SliderDefaults.colors(thumbColor = palette.accentCyan, activeTrackColor = palette.accentCyan)
                    )
                    Text(
                        text = tr("Votre couleur :", "Your color:"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassFilterChip(tr("Blancs", "White"), colorChoice == 0, { colorChoice = 0 })
                        GlassFilterChip(tr("Noirs", "Black"), colorChoice == 1, { colorChoice = 1 })
                        GlassFilterChip(tr("Aléatoire", "Random"), colorChoice == 2, { colorChoice = 2 })
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr("Cadence :", "Time control:"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassFilterChip(tr("Illimité", "Unlimited"), timeControlMins == 0, { timeControlMins = 0 })
                        GlassFilterChip("5 min", timeControlMins == 5, { timeControlMins = 5 })
                        GlassFilterChip("10 min", timeControlMins == 10, { timeControlMins = 10 })
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { startMatch() },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("start_ai_game_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Lancer la partie", "Start Match"), fontWeight = FontWeight.Bold)
                    }
                }

                // Match History
                Text(
                    text = tr("Historique des parties (${playedGames.size})", "Match History (${playedGames.size})"),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                if (playedGames.isEmpty()) {
                    GlassCard {
                        Text(
                            text = tr("Aucune partie enregistrée pour l'instant.", "No saved games yet."),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                } else {
                    playedGames.take(10).forEach { g ->
                        GlassCard(contentPadding = 12.dp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${g.whiteName} vs ${g.blackName}", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                    Text(
                                        text = "${g.result} • ${g.moveCount} ${tr("coups", "moves")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.accentCyan
                                    )
                                }
                                IconButton(onClick = { onAnalyzeGame(g.pgn) }) {
                                    Icon(Icons.Default.Analytics, contentDescription = tr("Analyser", "Analyze"), tint = palette.accentCyan)
                                }
                                IconButton(onClick = { onDeleteGame(g.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = tr("Supprimer", "Delete"), tint = palette.error)
                                }
                            }
                        }
                    }
                }
            } else {
                // Active Match UI
                val userColor = if (userPlaysWhite) PieceColor.WHITE else PieceColor.BLACK
                val evalCp = engineState.evaluationCp

                // Eval bar + live centipawn badge powered by ChessEngine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, palette.glassBorderStart.copy(alpha = 0.55f), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(engineState.whiteWinAdvantageFraction)
                                .fillMaxHeight()
                                .background(Color(0xFFF6F4EE))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f - engineState.whiteWinAdvantageFraction)
                                .fillMaxHeight()
                                .background(Color(0xFF1E1C18))
                        )
                    }
                    GlassBadge(
                        text = engineState.evaluationLabel,
                        color = if (evalCp >= 0) palette.accentCyan else palette.textPrimary
                    )
                }

                val matchMood = when {
                    gameResultStr != null -> LionMood.CROWNED
                    isAiThinking -> LionMood.THINKING
                    evalCp > 250 && userPlaysWhite -> LionMood.CHEERING
                    evalCp < -250 && !userPlaysWhite -> LionMood.CHEERING
                    evalCp < -250 && userPlaysWhite -> LionMood.SHOCKED
                    evalCp > 250 && !userPlaysWhite -> LionMood.SHOCKED
                    else -> LionMood.IDLE_HAPPY
                }
                val matchCoachMsg = when {
                    gameResultStr != null -> tr("Partie terminée ($gameResultStr) ! Analyse-la en PGN pour progresser.", "Match finished ($gameResultStr)! Analyze it in PGN to level up.")
                    isAiThinking -> tr("$aiPersonaName réfléchit à sa réponse...", "$aiPersonaName is calculating a reply...")
                    (evalCp > 250 && userPlaysWhite) || (evalCp < -250 && !userPlaysWhite) -> tr("Super avantage ! Convertis-le en gardant ton Roi en sécurité.", "Great advantage! Convert cleanly while keeping your King safe.")
                    (evalCp < -250 && userPlaysWhite) || (evalCp > 250 && !userPlaysWhite) -> tr("Position tendue ! Cherche du contre-jeu ou un échec.", "Sharp position! Look for active counterplay.")
                    else -> tr("Affrontement équilibré contre $aiPersonaName ($aiElo Elo).", "Balanced battle against $aiPersonaName ($aiElo Elo).")
                }
                LionCoachBanner(mood = matchMood, message = matchCoachMsg)

                // Opponent & Clocks header
                GlassCard(contentPadding = 10.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAiThinking) "$aiPersonaName ($aiElo)..." else "$aiPersonaName ($aiElo Elo)",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        if (timeControlMins > 0) {
                            val aiSec = if (userPlaysWhite) blackClockSec else whiteClockSec
                            val userSec = if (userPlaysWhite) whiteClockSec else blackClockSec
                            Text(
                                text = "IA %02d:%02d | ${tr("Vous", "You")} %02d:%02d".format(aiSec / 60, aiSec % 60, userSec / 60, userSec % 60),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.accentCyan
                            )
                        } else {
                            GlassBadge(
                                text = if (board.turn == userColor) tr("À vous", "Your turn") else tr("Tour IA", "AI turn"),
                                color = palette.accentCyan
                            )
                        }
                    }
                }

                KChessBoard(
                    engine = engine,
                    flipped = !userPlaysWhite,
                    showCoordinates = showCoordinates,
                    interactive = gameResultStr == null && !isAiThinking && board.turn == userColor,
                    hintArrow = hintArrow,
                    showStatusHeader = false,
                    onMovePlayed = { _, _ -> hintArrow = null }
                )

                if (gameResultStr != null) {
                    GlassCard(elevated = true, accentBorderColor = palette.success) {
                        Text(
                            text = "${tr("Partie terminée", "Game Over")} : $gameResultStr",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.success
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val pgn = PgnParser.exportPgn(
                                        engineState.moveHistorySan,
                                        if (userPlaysWhite) "Joueur" else "IA ($aiElo)",
                                        if (userPlaysWhite) "IA ($aiElo)" else "Joueur",
                                        gameResultStr!!
                                    )
                                    onAnalyzeGame(pgn)
                                },
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text(tr("Analyser (PGN)", "Analyze (PGN)"), color = palette.accentCyan)
                            }
                            Button(
                                onClick = { gameActive = false },
                                colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text(tr("Nouvelle partie", "New Match"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val hint = withContext(Dispatchers.Default) {
                                        ChessAi.bestMoveWithEval(board, 2).first
                                    }
                                    if (hint != null) hintArrow = hint.from to hint.to
                                }
                            },
                            enabled = !isAiThinking && board.turn == userColor,
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = palette.accentCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Indice", "Hint"), color = palette.textPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                // Undo last full turn (2 plies if AI already moved)
                                if (engineState.moveHistoryUci.size >= 2 && !isAiThinking) {
                                    engine.undoMove()
                                    engine.undoMove()
                                    hintArrow = null
                                }
                            },
                            enabled = engineState.moveHistoryUci.size >= 2 && !isAiThinking,
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, tint = palette.textPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Annuler", "Undo"), color = palette.textPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                finishGame(if (userPlaysWhite) "0-1" else "1-0")
                            },
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = palette.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Abandon", "Resign"), color = palette.error)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
