package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.chess.ChessEngine
import com.example.domain.chess.PgnParser
import com.example.domain.model.HistoricalGame
import com.example.domain.model.Square
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.loc
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import kotlinx.coroutines.delay

@Composable
fun HistoricalGamesScreen(
    games: List<HistoricalGame>,
    showCoordinates: Boolean,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current

    var selectedGame by remember { mutableStateOf(games.first()) }
    val parsed = remember(selectedGame.id) { PgnParser.parse(selectedGame.pgn) }
    var currentPly by remember(selectedGame.id) { mutableIntStateOf(0) } // 0 = initial board, 1..N = after ply
    var autoPlaying by remember(selectedGame.id) { mutableStateOf(false) }
    var guessMode by remember(selectedGame.id) { mutableStateOf(false) }
    var guessScore by remember(selectedGame.id) { mutableIntStateOf(0) }
    var guessAttempts by remember(selectedGame.id) { mutableIntStateOf(0) }
    var guessFeedback by remember(selectedGame.id, currentPly) { mutableStateOf<String?>(null) }

    var selectedSq by remember(selectedGame.id, currentPly) { mutableStateOf<Square?>(null) }
    var legalTargets by remember(selectedGame.id, currentPly) { mutableStateOf<Set<Square>>(emptySet()) }

    LaunchedEffect(autoPlaying, currentPly, selectedGame.id) {
        if (autoPlaying && !guessMode) {
            if (currentPly < parsed.moves.size) {
                delay(1100)
                currentPly++
            } else {
                autoPlaying = false
            }
        }
    }

    val currentBoard = if (currentPly == 0) parsed.initialBoard else parsed.moves[currentPly - 1].boardAfter
    val lastMove = if (currentPly == 0) null else parsed.moves[currentPly - 1].move
    val currentComment = selectedGame.commentsByPly[currentPly]?.get(isEn)
    val nextExpectedMove = parsed.moves.getOrNull(currentPly)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = tr("Parties Historiques", "Historical Games"),
            subtitle = "${selectedGame.whitePlayer} vs ${selectedGame.blackPlayer} (${selectedGame.year})",
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            games.forEach { g ->
                GlassFilterChip(
                    label = "${g.whitePlayer.substringAfterLast(" ")} (${g.year})",
                    selected = selectedGame.id == g.id,
                    onClick = { selectedGame = g }
                )
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassCard(contentPadding = 12.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = selectedGame.title.get(isEn), style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Text(text = selectedGame.event, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    GlassBadge(text = selectedGame.result, color = palette.accentCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = selectedGame.summary.get(isEn), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = palette.accentMagenta)
                        Text(tr("Mode « Guess the Move »", "Guess the Move Mode"), style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                    }
                    Switch(
                        checked = guessMode,
                        onCheckedChange = {
                            guessMode = it
                            autoPlaying = false
                            guessFeedback = null
                        }
                    )
                }
                if (guessMode) {
                    Text(
                        text = "${tr("Score", "Score")}: $guessScore / $guessAttempts  •  ${tr("Devinez le coup du maître sur l'échiquier !", "Guess the master's move on the board!")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.accentMagenta
                    )
                }
            }

            ChessBoardView(
                board = currentBoard,
                selectedSquare = selectedSq,
                legalTargets = legalTargets,
                lastMove = lastMove,
                showCoordinates = showCoordinates,
                onSquareClick = { sq ->
                    if (!guessMode || nextExpectedMove == null) return@ChessBoardView
                    val sel = selectedSq
                    if (sel != null && sq in legalTargets) {
                        val played = ChessEngine.legalMovesFrom(currentBoard, sel).firstOrNull { it.to == sq }
                        if (played != null) {
                            guessAttempts++
                            if (played.toUci() == nextExpectedMove.move.toUci()) {
                                guessScore++
                                guessFeedback = loc(isEn, "✓ Exact ! Vous avez trouvé ${nextExpectedMove.san} !", "✓ Spot on! You found ${nextExpectedMove.san}!")
                                currentPly++
                            } else {
                                guessFeedback = loc(
                                    isEn,
                                    "✗ Le maître a joué ${nextExpectedMove.san}. Essayez ou avancez !",
                                    "✗ The master played ${nextExpectedMove.san}. Try again or advance!"
                                )
                            }
                            selectedSq = null
                            legalTargets = emptySet()
                        }
                    } else {
                        val p = currentBoard.pieceAt(sq)
                        if (p != null && p.color == currentBoard.turn) {
                            selectedSq = sq
                            legalTargets = ChessEngine.legalMovesFrom(currentBoard, sq).map { it.to }.toSet()
                        } else {
                            selectedSq = null
                            legalTargets = emptySet()
                        }
                    }
                }
            )

            AnimatedVisibility(visible = guessFeedback != null) {
                GlassCard(accentBorderColor = palette.accentMagenta, contentPadding = 12.dp) {
                    Text(text = guessFeedback.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                }
            }

            if (currentComment != null) {
                GlassCard(accentBorderColor = palette.accentCyan, elevated = true) {
                    Text(
                        text = "${tr("Commentaire (coup", "Commentary (move")} ${(currentPly + 1) / 2}):",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.accentCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = currentComment, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                }
            }

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { autoPlaying = false; currentPly = 0 },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Text("|◀", color = palette.textPrimary)
                }
                OutlinedButton(
                    onClick = { autoPlaying = false; if (currentPly > 0) currentPly-- },
                    enabled = currentPly > 0,
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = tr("Précédent", "Previous"), tint = palette.textPrimary)
                }
                Button(
                    onClick = { if (!guessMode) autoPlaying = !autoPlaying },
                    enabled = !guessMode,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                    modifier = Modifier.weight(1.2f).minimumInteractiveComponentSize()
                ) {
                    Icon(if (autoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                }
                OutlinedButton(
                    onClick = { autoPlaying = false; if (currentPly < parsed.moves.size) currentPly++ },
                    enabled = currentPly < parsed.moves.size,
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = tr("Suivant", "Next"), tint = palette.textPrimary)
                }
            }

            if (!guessMode) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsed.moves.forEachIndexed { idx, pm ->
                        val label = if (idx % 2 == 0) "${idx / 2 + 1}.${pm.san}" else pm.san
                        GlassFilterChip(
                            label = label,
                            selected = currentPly == idx + 1,
                            onClick = { autoPlaying = false; currentPly = idx + 1 }
                        )
                    }
                }
            } else {
                Text(
                    text = "${tr("Coup actuel", "Current ply")}: $currentPly / ${parsed.moves.size}",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
