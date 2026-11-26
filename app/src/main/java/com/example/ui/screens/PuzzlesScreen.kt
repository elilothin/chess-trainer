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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PuzzleProgressEntity
import com.example.domain.chess.ChessEngine
import com.example.domain.model.ChessMove
import com.example.domain.model.LionMood
import com.example.domain.model.PieceColor
import com.example.domain.model.PuzzleDifficulty
import com.example.domain.model.PuzzleTheme
import com.example.domain.model.Square
import com.example.domain.model.TacticalPuzzle
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.LionCoachBanner
import com.example.ui.components.PromotionPickerDialog
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PuzzleMode(val fr: String, val en: String) {
    FREE("Libre", "Free"),
    SPRINT("Sprint 3m", "Sprint 3m"),
    STREAK("Streak", "Streak")
}

@Composable
fun PuzzlesScreen(
    puzzles: List<TacticalPuzzle>,
    progressMap: Map<String, PuzzleProgressEntity>,
    showCoordinates: Boolean,
    onRecordAttempt: (String, Boolean) -> Unit,
    onRecordSession: (String, Int, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(PuzzleMode.FREE) }
    var selectedTheme by remember { mutableStateOf<PuzzleTheme?>(null) }
    var selectedDiff by remember { mutableStateOf(PuzzleDifficulty.ALL) }

    val filteredPuzzles = remember(puzzles, selectedTheme, selectedDiff, mode) {
        val base = if (mode == PuzzleMode.FREE) {
            puzzles.filter { p ->
                (selectedTheme == null || p.theme == selectedTheme) &&
                    p.elo in selectedDiff.minElo..selectedDiff.maxElo
            }
        } else {
            puzzles.sortedBy { it.elo }
        }
        base.ifEmpty { puzzles }
    }

    var currentIdx by remember(filteredPuzzles) { mutableIntStateOf(0) }
    val puzzle = filteredPuzzles[currentIdx.coerceIn(0, filteredPuzzles.lastIndex)]

    var board by remember(puzzle.id, currentIdx) { mutableStateOf(ChessEngine.fromFen(puzzle.initialFen)) }
    var solutionStep by remember(puzzle.id, currentIdx) { mutableIntStateOf(0) }
    var selectedSq by remember(puzzle.id, currentIdx) { mutableStateOf<Square?>(null) }
    var legalTargets by remember(puzzle.id, currentIdx) { mutableStateOf<Set<Square>>(emptySet()) }
    var lastMove by remember(puzzle.id, currentIdx) { mutableStateOf<ChessMove?>(null) }
    var showHint by remember(puzzle.id, currentIdx) { mutableStateOf(false) }
    var status by remember(puzzle.id, currentIdx) { mutableStateOf<String?>(null) } // "SOLVED", "FAILED"
    var pendingPromotion by remember(puzzle.id, currentIdx) { mutableStateOf<Pair<Square, Square>?>(null) }

    // Sprint & Streak state
    var sprintSecondsLeft by remember { mutableIntStateOf(180) }
    var sprintActive by remember { mutableStateOf(false) }
    var sprintScore by remember { mutableIntStateOf(0) }
    var streakCount by remember { mutableIntStateOf(0) }
    var streakBroken by remember { mutableStateOf(false) }

    LaunchedEffect(mode, sprintActive) {
        if (mode == PuzzleMode.SPRINT && sprintActive) {
            while (sprintSecondsLeft > 0 && sprintActive) {
                delay(1000L)
                sprintSecondsLeft--
            }
            if (sprintSecondsLeft <= 0) {
                sprintActive = false
                onRecordSession("PUZZLE_SPRINT", sprintScore, 100, 180)
            }
        }
    }

    val playerColor = remember(puzzle.id) { ChessEngine.fromFen(puzzle.initialFen).turn }
    val hintArrow = remember(puzzle.id, solutionStep, showHint) {
        if (!showHint) null
        else puzzle.solutionUci.getOrNull(solutionStep)?.let { uci ->
            val from = Square.fromAlgebraic(uci.take(2))
            val to = Square.fromAlgebraic(uci.drop(2).take(2))
            if (from != null && to != null) from to to else null
        }
    }

    fun advancePuzzle() {
        currentIdx = (currentIdx + 1) % filteredPuzzles.size
    }

    fun handlePlayerMove(move: ChessMove) {
        val expected = puzzle.solutionUci.getOrNull(solutionStep) ?: return
        val uci = move.toUci()
        val isMatch = uci == expected || (expected.length == 4 && uci.startsWith(expected))
        if (isMatch) {
            board = ChessEngine.applyMove(board, move)
            lastMove = move
            selectedSq = null
            legalTargets = emptySet()
            showHint = false
            val nextStep = solutionStep + 1
            if (nextStep >= puzzle.solutionUci.size) {
                status = "SOLVED"
                onRecordAttempt(puzzle.id, true)
                if (mode == PuzzleMode.SPRINT && sprintActive) {
                    sprintScore++
                    scope.launch {
                        delay(550)
                        advancePuzzle()
                    }
                } else if (mode == PuzzleMode.STREAK && !streakBroken) {
                    streakCount++
                    onRecordSession("PUZZLE_STREAK", streakCount, 100, 15)
                }
            } else {
                // Play opponent's forced reply automatically
                solutionStep = nextStep
                val replyUci = puzzle.solutionUci[nextStep]
                scope.launch {
                    delay(320)
                    val replyMove = ChessEngine.moveFromUci(board, replyUci)
                    if (replyMove != null) {
                        board = ChessEngine.applyMove(board, replyMove)
                        lastMove = replyMove
                        solutionStep = nextStep + 1
                    }
                }
            }
        } else {
            selectedSq = null
            legalTargets = emptySet()
            status = "FAILED"
            onRecordAttempt(puzzle.id, false)
            if (mode == PuzzleMode.STREAK) {
                streakBroken = true
                streakCount = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = tr("Puzzles Tactiques", "Tactical Puzzles"),
            subtitle = tr(
                "Puzzle ${currentIdx + 1}/${filteredPuzzles.size} • Elo ${puzzle.elo}",
                "Puzzle ${currentIdx + 1}/${filteredPuzzles.size} • Elo ${puzzle.elo}"
            ),
            onBack = onBack
        )

        // Mode switcher: Free / Sprint 3m / Streak
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PuzzleMode.entries.forEach { m ->
                val icon = when (m) {
                    PuzzleMode.FREE -> Icons.Default.Bolt
                    PuzzleMode.SPRINT -> Icons.Default.Timer
                    PuzzleMode.STREAK -> Icons.Default.LocalFireDepartment
                }
                GlassFilterChip(
                    label = tr(m.fr, m.en),
                    selected = mode == m,
                    leadingIcon = icon,
                    onClick = {
                        mode = m
                        if (m == PuzzleMode.SPRINT) {
                            sprintSecondsLeft = 180
                            sprintScore = 0
                            sprintActive = true
                            currentIdx = 0
                        } else {
                            sprintActive = false
                        }
                        if (m == PuzzleMode.STREAK) {
                            streakCount = 0
                            streakBroken = false
                            currentIdx = 0
                        }
                    }
                )
            }
        }

        if (mode == PuzzleMode.FREE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassFilterChip(
                    label = tr("Tous thèmes", "All Themes"),
                    selected = selectedTheme == null,
                    onClick = { selectedTheme = null }
                )
                PuzzleTheme.entries.forEach { th ->
                    GlassFilterChip(
                        label = tr(th.fr, th.en),
                        selected = selectedTheme == th,
                        onClick = { selectedTheme = th }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PuzzleDifficulty.entries.forEach { diff ->
                    GlassFilterChip(
                        label = tr(diff.labelFr, diff.labelEn),
                        selected = selectedDiff == diff,
                        onClick = { selectedDiff = diff }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sprint / Streak banner
            if (mode == PuzzleMode.SPRINT) {
                GlassCard(accentBorderColor = palette.accentMagenta, contentPadding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mins = sprintSecondsLeft / 60
                        val secs = sprintSecondsLeft % 60
                        Text(
                            text = "⏱ %02d:%02d".format(mins, secs),
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (sprintSecondsLeft < 20) palette.error else palette.accentCyan
                        )
                        Text(
                            text = "${tr("Résolus", "Solved")}: $sprintScore",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                    }
                }
            } else if (mode == PuzzleMode.STREAK) {
                GlassCard(accentBorderColor = palette.warning, contentPadding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 ${tr("Série en cours", "Current Streak")}: $streakCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.warning
                        )
                        if (streakBroken) {
                            OutlinedButton(onClick = {
                                streakBroken = false
                                streakCount = 0
                                advancePuzzle()
                            }) {
                                Text(tr("Recommencer", "Restart"), color = palette.textPrimary)
                            }
                        }
                    }
                }
            }

            // Léon live tactical coach reaction
            val lionMood = when {
                status == "SOLVED" -> LionMood.CHEERING
                status == "FAILED" -> LionMood.SHOCKED
                showHint -> LionMood.IDLE_HAPPY
                else -> LionMood.THINKING
            }
            val lionMsg = when {
                status == "SOLVED" -> tr(
                    "Graou ! Coup brillant ! Tu gagnes de l'XP tactique.",
                    "Roar! Brilliant move! You earned tactical XP."
                )
                status == "FAILED" -> tr(
                    "Oups ! Pas celui-là. Regarde les pièces sans défense ou utilise l'indice !",
                    "Oops! Not that one. Check undefended pieces or tap Hint!"
                )
                showHint -> tr(
                    "Regarde la flèche verte : elle montre la pièce clé à bouger.",
                    "Follow the green arrow—it reveals the key tactical piece."
                )
                else -> tr(
                    "Cherche d'abord les échecs, puis les captures et les fourchettes !",
                    "Check for checks first, then captures and forks!"
                )
            }
            LionCoachBanner(mood = lionMood, message = lionMsg)

            // Puzzle header
            GlassCard(contentPadding = 12.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = puzzle.title.get(isEn),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (playerColor == PieceColor.WHITE) {
                                tr("Trait aux Blancs • Trouvez le meilleur coup", "White to move • Find the best move")
                            } else {
                                tr("Trait aux Noirs • Trouvez le meilleur coup", "Black to move • Find the best move")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.accentCyan
                        )
                    }
                    val solvedBefore = progressMap[puzzle.id]?.solved == true
                    GlassBadge(
                        text = if (solvedBefore) "✓ ${tr(puzzle.theme.fr, puzzle.theme.en)}" else tr(puzzle.theme.fr, puzzle.theme.en),
                        color = if (solvedBefore) palette.success else palette.accentMagenta
                    )
                }
            }

            ChessBoardView(
                board = board,
                selectedSquare = selectedSq,
                legalTargets = legalTargets,
                lastMove = lastMove,
                hintArrow = hintArrow,
                flipped = (playerColor == PieceColor.BLACK),
                showCoordinates = showCoordinates,
                onSquareClick = { sq ->
                    if (status == "SOLVED") return@ChessBoardView
                    if (mode == PuzzleMode.SPRINT && !sprintActive) return@ChessBoardView

                    val currentSel = selectedSq
                    if (currentSel != null && sq in legalTargets) {
                        val moves = ChessEngine.legalMovesFrom(board, currentSel).filter { it.to == sq }
                        if (moves.any { it.promotion != null }) {
                            pendingPromotion = currentSel to sq
                        } else if (moves.isNotEmpty()) {
                            handlePlayerMove(moves.first())
                        }
                    } else {
                        val piece = board.pieceAt(sq)
                        if (piece != null && piece.color == board.turn && board.turn == playerColor) {
                            selectedSq = sq
                            legalTargets = ChessEngine.legalMovesFrom(board, sq).map { it.to }.toSet()
                            if (status == "FAILED") status = null
                        } else {
                            selectedSq = null
                            legalTargets = emptySet()
                        }
                    }
                }
            )

            AnimatedVisibility(visible = status != null) {
                val solved = status == "SOLVED"
                GlassCard(
                    accentBorderColor = if (solved) palette.success else palette.error,
                    elevated = true
                ) {
                    Text(
                        text = if (solved) {
                            tr("✓ Puzzle résolu !", "✓ Puzzle Solved!")
                        } else {
                            tr("✗ Coup incorrect — Explication :", "✗ Incorrect Move — Explanation:")
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (solved) palette.success else palette.error
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = puzzle.explanation.get(isEn),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showHint = true },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("puzzle_hint_btn")
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = palette.accentCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Indice", "Hint"), color = palette.textPrimary)
                }
                OutlinedButton(
                    onClick = {
                        board = ChessEngine.fromFen(puzzle.initialFen)
                        solutionStep = 0
                        selectedSq = null
                        legalTargets = emptySet()
                        lastMove = null
                        status = null
                        showHint = false
                    },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = palette.textPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Réessayer", "Retry"), color = palette.textPrimary)
                }
                Button(
                    onClick = { advancePuzzle() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accentCyan,
                        contentColor = Color(0xFF04101B)
                    ),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("puzzle_next_btn")
                ) {
                    Text(tr("Suivant", "Next"), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    pendingPromotion?.let { (from, to) ->
        PromotionPickerDialog(
            color = board.turn,
            onSelect = { promoType ->
                val move = ChessEngine.legalMovesFrom(board, from)
                    .firstOrNull { it.to == to && it.promotion == promoType }
                pendingPromotion = null
                if (move != null) handlePlayerMove(move)
            },
            onDismiss = { pendingPromotion = null }
        )
    }
}
