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
import com.example.domain.model.ChessMove
import com.example.domain.model.MiddlegameExercise
import com.example.domain.model.MiddlegameType
import com.example.domain.model.Square
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.tr
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

@Composable
fun MiddlegameScreen(
    exercises: List<MiddlegameExercise>,
    showCoordinates: Boolean,
    onCompletedExercise: () -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current

    var selectedType by remember { mutableStateOf<MiddlegameType?>(null) }
    val filtered = remember(exercises, selectedType) {
        if (selectedType == null) exercises else exercises.filter { it.type == selectedType }
    }
    var idx by remember(filtered) { mutableIntStateOf(0) }
    val ex = filtered[idx.coerceIn(0, filtered.lastIndex)]

    var board by remember(ex.id) { mutableStateOf(ChessEngine.fromFen(ex.initialFen)) }
    var selectedSq by remember(ex.id) { mutableStateOf<Square?>(null) }
    var legalTargets by remember(ex.id) { mutableStateOf<Set<Square>>(emptySet()) }
    var lastMove by remember(ex.id) { mutableStateOf<ChessMove?>(null) }
    var solved by remember(ex.id) { mutableStateOf(false) }
    var showHint by remember(ex.id) { mutableStateOf(false) }
    var wrongNotice by remember(ex.id) { mutableStateOf(false) }

    val highlightSet = remember(ex.id) {
        ex.highlightSquares.mapNotNull { Square.fromAlgebraic(it) }.toSet()
    }
    val hintArrow = remember(ex.id, showHint) {
        if (!showHint) null
        else ex.acceptedMovesUci.firstOrNull()?.let { uci ->
            val f = Square.fromAlgebraic(uci.take(2))
            val t = Square.fromAlgebraic(uci.drop(2).take(2))
            if (f != null && t != null) f to t else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = tr("Milieu de Jeu", "Middlegame"),
            subtitle = tr("Stratégie, plans et amélioration des pièces", "Strategy, plans & piece improvement"),
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassFilterChip(
                label = tr("Tous", "All"),
                selected = selectedType == null,
                onClick = { selectedType = null }
            )
            MiddlegameType.entries.forEach { t ->
                GlassFilterChip(
                    label = tr(t.fr, t.en),
                    selected = selectedType == t,
                    onClick = { selectedType = t }
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
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ex.title.get(isEn),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    GlassBadge(text = tr(ex.type.fr, ex.type.en), color = palette.warning)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = ex.description.get(isEn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
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
                    if (solved) return@ChessBoardView
                    val sel = selectedSq
                    if (sel != null && sq in legalTargets) {
                        val move = ChessEngine.legalMovesFrom(board, sel).firstOrNull { it.to == sq }
                        if (move != null) {
                            if (move.toUci() in ex.acceptedMovesUci) {
                                board = ChessEngine.applyMove(board, move)
                                lastMove = move
                                selectedSq = null
                                legalTargets = emptySet()
                                solved = true
                                wrongNotice = false
                                onCompletedExercise()
                            } else {
                                wrongNotice = true
                                selectedSq = null
                                legalTargets = emptySet()
                            }
                        }
                    } else {
                        val p = board.pieceAt(sq)
                        if (p != null && p.color == board.turn) {
                            selectedSq = sq
                            legalTargets = ChessEngine.legalMovesFrom(board, sq).map { it.to }.toSet()
                            wrongNotice = false
                        } else {
                            selectedSq = null
                            legalTargets = emptySet()
                        }
                    }
                }
            )

            AnimatedVisibility(visible = solved) {
                GlassCard(accentBorderColor = palette.success, elevated = true) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = palette.success)
                        Text(tr("Excellent plan !", "Great plan!"), style = MaterialTheme.typography.titleMedium, color = palette.success)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = ex.explanation.get(isEn), style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                }
            }

            AnimatedVisibility(visible = wrongNotice) {
                GlassCard(accentBorderColor = palette.warning) {
                    Text(
                        text = tr("Ce coup ne répond pas au principe stratégique ici. Essayez un autre coup ou demandez l'indice !", "Not the strongest strategic idea here. Try again or tap Hint!"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.warning
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { showHint = true },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = palette.accentCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Indice", "Hint"), color = palette.textPrimary)
                }
                OutlinedButton(
                    onClick = {
                        board = ChessEngine.fromFen(ex.initialFen)
                        selectedSq = null
                        legalTargets = emptySet()
                        lastMove = null
                        solved = false
                        wrongNotice = false
                    },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = palette.textPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Reset", "Reset"), color = palette.textPrimary)
                }
                Button(
                    onClick = { idx = (idx + 1) % filtered.size },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Text(tr("Suivant", "Next"), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
