package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.chess.ChessEngine
import com.example.domain.model.ChessMove
import com.example.domain.model.Lesson
import com.example.domain.model.LessonSection
import com.example.domain.model.LionMood
import com.example.domain.model.PieceType
import com.example.domain.model.Square
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.LionCoachBanner
import com.example.ui.components.PromotionPickerDialog
import com.example.ui.components.tr
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

@Composable
fun LessonsScreen(
    lessons: List<Lesson>,
    completedLessonIds: Set<String>,
    showCoordinates: Boolean,
    onMarkCompleted: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    var selectedSection by remember { mutableStateOf<LessonSection?>(null) }
    var activeLesson by remember { mutableStateOf<Lesson?>(null) }

    if (activeLesson != null) {
        val currentLesson = activeLesson!!
        val currentIdx = lessons.indexOfFirst { it.id == currentLesson.id }
        val nextLesson = lessons.getOrNull(currentIdx + 1)

        LessonInteractiveDetail(
            lesson = currentLesson,
            isCompleted = currentLesson.id in completedLessonIds,
            showCoordinates = showCoordinates,
            onCompleted = { onMarkCompleted(currentLesson.id) },
            onNext = nextLesson?.let { nxt -> { activeLesson = nxt } },
            onBack = { activeLesson = null }
        )
        return
    }

    val filtered = if (selectedSection == null) lessons else lessons.filter { it.section == selectedSection }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassTopBar(
            title = tr("Apprendre les Bases", "Learn the Basics"),
            subtitle = tr(
                "${completedLessonIds.size}/${lessons.size} leçons terminées",
                "${completedLessonIds.size}/${lessons.size} lessons completed"
            ),
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassFilterChip(
                label = tr("Toutes", "All"),
                selected = selectedSection == null,
                onClick = { selectedSection = null }
            )
            GlassFilterChip(
                label = tr("Pièces", "Pieces"),
                selected = selectedSection == LessonSection.PIECE_MOVEMENT,
                onClick = { selectedSection = LessonSection.PIECE_MOVEMENT }
            )
            GlassFilterChip(
                label = tr("Règles spéciales", "Special Rules"),
                selected = selectedSection == LessonSection.SPECIAL_RULES,
                onClick = { selectedSection = LessonSection.SPECIAL_RULES }
            )
            GlassFilterChip(
                label = tr("Notions", "Concepts"),
                selected = selectedSection == LessonSection.BASIC_CONCEPTS,
                onClick = { selectedSection = LessonSection.BASIC_CONCEPTS }
            )
            GlassFilterChip(
                label = tr("Notation", "Notation"),
                selected = selectedSection == LessonSection.ALGEBRAIC_NOTATION,
                onClick = { selectedSection = LessonSection.ALGEBRAIC_NOTATION }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { lesson ->
                val done = lesson.id in completedLessonIds
                GlassCard(
                    onClick = { activeLesson = lesson },
                    accentBorderColor = if (done) palette.success else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lesson_item_${lesson.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lesson.title.get(isEn),
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lesson.summary.get(isEn),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (done) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = tr("Terminée", "Completed"),
                                tint = palette.success
                            )
                        } else {
                            GlassBadge(text = tr("Ouvrir", "Start"), color = palette.accentCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonInteractiveDetail(
    lesson: Lesson,
    isCompleted: Boolean,
    showCoordinates: Boolean,
    onCompleted: () -> Unit,
    onNext: (() -> Unit)?,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current

    var board by remember(lesson.id) { mutableStateOf(ChessEngine.fromFen(lesson.initialFen)) }
    var selectedSq by remember(lesson.id) { mutableStateOf<Square?>(null) }
    var legalTargets by remember(lesson.id) { mutableStateOf<Set<Square>>(emptySet()) }
    var lastMove by remember(lesson.id) { mutableStateOf<ChessMove?>(null) }
    var showHint by remember(lesson.id) { mutableStateOf(false) }
    var solvedNow by remember(lesson.id) { mutableStateOf(false) }
    var errorMsg by remember(lesson.id) { mutableStateOf<String?>(null) }
    var pendingPromotion by remember(lesson.id) { mutableStateOf<Pair<Square, Square>?>(null) }

    val highlightSet = remember(lesson.id) {
        lesson.highlightSquares.mapNotNull { Square.fromAlgebraic(it) }.toSet()
    }

    val hintArrow = remember(lesson.id, showHint) {
        if (!showHint) null
        else lesson.expectedMovesUci.firstOrNull()?.let { uci ->
            val from = Square.fromAlgebraic(uci.take(2))
            val to = Square.fromAlgebraic(uci.drop(2).take(2))
            if (from != null && to != null) from to to else null
        }
    }

    fun tryExecuteMove(move: ChessMove) {
        val uci = move.toUci()
        if (uci in lesson.expectedMovesUci || lesson.expectedMovesUci.any { uci.startsWith(it.take(4)) && it.length == 4 }) {
            board = ChessEngine.applyMove(board, move)
            lastMove = move
            selectedSq = null
            legalTargets = emptySet()
            errorMsg = null
            solvedNow = true
            onCompleted()
        } else {
            selectedSq = null
            legalTargets = emptySet()
            errorMsg = if (isEn) {
                "Not quite the exercise target move. Try again or use the hint!"
            } else {
                "Ce n'est pas le coup attendu pour cet exercice. Réessayez ou utilisez l'indice !"
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
            title = lesson.title.get(isEn),
            subtitle = if (isCompleted || solvedNow) tr("Terminée ✓", "Completed ✓") else tr("Exercice interactif", "Interactive Exercise"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val mood = when {
                solvedNow -> LionMood.CHEERING
                errorMsg != null -> LionMood.SHOCKED
                showHint -> LionMood.IDLE_HAPPY
                else -> LionMood.THINKING
            }
            val coachMsg = when {
                solvedNow -> tr("Super ! +50 XP débloqués pour cette leçon !", "Awesome! +50 XP earned for this lesson!")
                errorMsg != null -> errorMsg!!
                showHint -> lesson.hintText.get(isEn)
                else -> lesson.objective.get(isEn)
            }
            LionCoachBanner(mood = mood, message = coachMsg)

            GlassCard {
                Text(
                    text = lesson.explanation.get(isEn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlassBadge(
                    text = "${tr("Objectif", "Goal")}: ${lesson.objective.get(isEn)}",
                    color = palette.accentCyan
                )
            }

            ChessBoardView(
                board = board,
                selectedSquare = selectedSq,
                legalTargets = legalTargets,
                lastMove = lastMove,
                hintArrow = hintArrow,
                highlightedSquares = highlightSet,
                showCoordinates = showCoordinates,
                onSquareClick = { sq ->
                    if (solvedNow) return@ChessBoardView
                    if (lesson.targetSquareToTap != null) {
                        if (sq.toAlgebraic() == lesson.targetSquareToTap) {
                            solvedNow = true
                            errorMsg = null
                            onCompleted()
                        } else {
                            errorMsg = if (isEn) "You tapped ${sq.toAlgebraic()}. Try again!" else "Vous avez touché ${sq.toAlgebraic()}. Réessayez !"
                        }
                        return@ChessBoardView
                    }

                    val currentSel = selectedSq
                    if (currentSel != null && sq in legalTargets) {
                        val moves = ChessEngine.legalMovesFrom(board, currentSel).filter { it.to == sq }
                        if (moves.any { it.promotion != null }) {
                            pendingPromotion = currentSel to sq
                        } else if (moves.isNotEmpty()) {
                            tryExecuteMove(moves.first())
                        }
                    } else {
                        val piece = board.pieceAt(sq)
                        if (piece != null && piece.color == board.turn) {
                            selectedSq = sq
                            legalTargets = ChessEngine.legalMovesFrom(board, sq).map { it.to }.toSet()
                            errorMsg = null
                        } else {
                            selectedSq = null
                            legalTargets = emptySet()
                        }
                    }
                }
            )

            AnimatedVisibility(visible = solvedNow) {
                GlassCard(accentBorderColor = palette.success, elevated = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = palette.success)
                        Text(
                            text = tr("Bravo ! Leçon validée et sauvegardée.", "Well done! Lesson completed and saved."),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.success
                        )
                    }
                }
            }

            AnimatedVisibility(visible = errorMsg != null) {
                GlassCard(accentBorderColor = palette.warning) {
                    Text(
                        text = errorMsg.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.warning
                    )
                }
            }

            AnimatedVisibility(visible = showHint) {
                GlassCard(accentBorderColor = palette.accentCyan) {
                    Text(
                        text = lesson.hintText.get(isEn),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.accentCyan
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showHint = !showHint },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = palette.accentCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Indice", "Hint"), color = palette.textPrimary)
                }
                OutlinedButton(
                    onClick = {
                        board = ChessEngine.fromFen(lesson.initialFen)
                        selectedSq = null
                        legalTargets = emptySet()
                        lastMove = null
                        solvedNow = false
                        errorMsg = null
                    },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = palette.textPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Rejouer", "Reset"), color = palette.textPrimary)
                }
            }

            if (onNext != null) {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accentCyan,
                        contentColor = Color(0xFF04101B)
                    ),
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                ) {
                    Text(tr("Leçon suivante →", "Next Lesson →"), fontWeight = FontWeight.Bold)
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
                if (move != null) tryExecuteMove(move)
            },
            onDismiss = { pendingPromotion = null }
        )
    }
}
