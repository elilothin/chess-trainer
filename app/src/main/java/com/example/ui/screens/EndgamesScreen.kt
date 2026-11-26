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
import com.example.domain.chess.ChessAi
import com.example.domain.chess.ChessEngine
import com.example.domain.model.ChessMove
import com.example.domain.model.EndgameCategory
import com.example.domain.model.EndgameModuleItem
import com.example.domain.model.GameOutcome
import com.example.domain.model.LionMood
import com.example.domain.model.PieceColor
import com.example.domain.model.Square
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.LionCoachBanner
import com.example.ui.components.PromotionPickerDialog
import com.example.ui.components.loc
import com.example.ui.components.tr
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun EndgamesScreen(
    items: List<EndgameModuleItem>,
    showCoordinates: Boolean,
    onCompletedEndgame: () -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current

    var selectedCat by remember { mutableStateOf(EndgameCategory.LESSON) }
    val filtered = remember(items, selectedCat) { items.filter { it.category == selectedCat } }
    var idx by remember(filtered) { mutableIntStateOf(0) }
    val item = filtered[idx.coerceIn(0, filtered.lastIndex)]

    var board by remember(item.id) { mutableStateOf(ChessEngine.fromFen(item.initialFen)) }
    val playerColor = remember(item.id) { ChessEngine.fromFen(item.initialFen).turn }
    var selectedSq by remember(item.id) { mutableStateOf<Square?>(null) }
    var legalTargets by remember(item.id) { mutableStateOf<Set<Square>>(emptySet()) }
    var lastMove by remember(item.id) { mutableStateOf<ChessMove?>(null) }
    var showTip by remember(item.id) { mutableStateOf(false) }
    var statusText by remember(item.id) { mutableStateOf<String?>(null) }
    var isSuccess by remember(item.id) { mutableStateOf(false) }
    var isAiThinking by remember(item.id) { mutableStateOf(false) }
    var pendingPromo by remember(item.id) { mutableStateOf<Pair<Square, Square>?>(null) }

    // Live Trainer AI reply when it's AI's turn
    LaunchedEffect(item.id, board) {
        if (item.category == EndgameCategory.TRAINER && !isSuccess) {
            val outcome = ChessEngine.evaluateOutcome(board)
            if (outcome != GameOutcome.ONGOING) {
                val won = (outcome == GameOutcome.CHECKMATE_WHITE_WINS && playerColor == PieceColor.WHITE) ||
                    (outcome == GameOutcome.CHECKMATE_BLACK_WINS && playerColor == PieceColor.BLACK)
                isSuccess = won
                statusText = when {
                    won -> loc(isEn, "✓ Échec et mat ! Finale réussie contre l'IA.", "✓ Checkmate! Endgame converted vs AI.")
                    outcome == GameOutcome.STALEMATE -> loc(isEn, "Pat ! Attention à laisser une case de fuite avant le mat.", "Stalemate! Watch out not to trap the King without check.")
                    else -> loc(isEn, "Partie nulle (${outcome.name}). Réessayez !", "Draw (${outcome.name}). Try again!")
                }
                if (won) onCompletedEndgame()
            } else if (board.turn != playerColor) {
                isAiThinking = true
                delay(280)
                val aiMove = withContext(Dispatchers.Default) {
                    ChessAi.chooseMoveForElo(board, 1600)
                }
                if (aiMove != null) {
                    board = ChessEngine.applyMove(board, aiMove)
                    lastMove = aiMove
                }
                isAiThinking = false
            }
        }
    }

    fun executeMove(move: ChessMove) {
        if (item.category == EndgameCategory.LESSON) {
            if (move.toUci() in item.expectedMovesUci) {
                board = ChessEngine.applyMove(board, move)
                lastMove = move
                selectedSq = null
                legalTargets = emptySet()
                isSuccess = true
                statusText = loc(isEn, "✓ Parfait ! Technique de finale validée.", "✓ Spot on! Endgame technique validated.")
                onCompletedEndgame()
            } else {
                selectedSq = null
                legalTargets = emptySet()
                statusText = loc(isEn, "Pas tout à fait : consultez l'indice ci-dessous.", "Not quite: check the tip below.")
            }
        } else {
            board = ChessEngine.applyMove(board, move)
            lastMove = move
            selectedSq = null
            legalTargets = emptySet()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = tr("Finales Essentielles", "Essential Endgames"),
            subtitle = tr("Leçons (Lucena, Philidor) & Entraîneur IA", "Lessons (Lucena, Philidor) & Live Trainer"),
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EndgameCategory.entries.forEach { cat ->
                GlassFilterChip(
                    label = tr(cat.fr, cat.en),
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filtered.forEachIndexed { index, it ->
                GlassFilterChip(
                    label = it.title.get(isEn),
                    selected = idx == index,
                    onClick = { idx = index }
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
            val endgameMood = when {
                isSuccess -> LionMood.CHEERING
                statusText != null -> LionMood.SHOCKED
                else -> LionMood.THINKING
            }
            val endgameMsg = when {
                isSuccess -> tr("Finale maîtrisée ! En finale, le Roi devient une pièce d'attaque majeure.", "Endgame mastered! In the endgame, the King becomes a major attacking piece.")
                statusText != null -> tr("Attention à l'opposition et au pat — utilise l'indice si tu veux vérifier le plan.", "Watch opposition and stalemate — tap Tip if you want to verify the plan.")
                else -> tr("Calcule avec précision : chaque tempo de Roi compte en finale.", "Calculate precisely: every King tempo matters in the endgame.")
            }
            LionCoachBanner(mood = endgameMood, message = endgameMsg)

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.title.get(isEn), style = MaterialTheme.typography.titleMedium, color = palette.textPrimary, modifier = Modifier.weight(1f))
                    GlassBadge(
                        text = if (item.category == EndgameCategory.TRAINER) tr("VS IA", "VS AI") else tr("LEÇON", "LESSON"),
                        color = palette.accentCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = item.concept.get(isEn), style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
            }

            ChessBoardView(
                board = board,
                selectedSquare = selectedSq,
                legalTargets = legalTargets,
                lastMove = lastMove,
                flipped = (playerColor == PieceColor.BLACK),
                showCoordinates = showCoordinates,
                onSquareClick = { sq ->
                    if (isSuccess || isAiThinking || board.turn != playerColor) return@ChessBoardView
                    val sel = selectedSq
                    if (sel != null && sq in legalTargets) {
                        val moves = ChessEngine.legalMovesFrom(board, sel).filter { it.to == sq }
                        if (moves.any { it.promotion != null }) {
                            pendingPromo = sel to sq
                        } else if (moves.isNotEmpty()) {
                            executeMove(moves.first())
                        }
                    } else {
                        val p = board.pieceAt(sq)
                        if (p != null && p.color == board.turn) {
                            selectedSq = sq
                            legalTargets = ChessEngine.legalMovesFrom(board, sq).map { it.to }.toSet()
                        } else {
                            selectedSq = null
                            legalTargets = emptySet()
                        }
                    }
                }
            )

            AnimatedVisibility(visible = statusText != null) {
                GlassCard(accentBorderColor = if (isSuccess) palette.success else palette.warning, elevated = true) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isSuccess) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = palette.success)
                        Text(
                            text = statusText.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSuccess) palette.success else palette.warning
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showTip) {
                GlassCard(accentBorderColor = palette.accentCyan) {
                    Text(text = item.tip.get(isEn), style = MaterialTheme.typography.bodyMedium, color = palette.accentCyan)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { showTip = !showTip },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = palette.accentCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Conseil", "Tip"), color = palette.textPrimary)
                }
                Button(
                    onClick = {
                        board = ChessEngine.fromFen(item.initialFen)
                        selectedSq = null
                        legalTargets = emptySet()
                        lastMove = null
                        statusText = null
                        isSuccess = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Recommencer", "Restart"), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    pendingPromo?.let { (from, to) ->
        PromotionPickerDialog(
            color = board.turn,
            onSelect = { promoType ->
                val move = ChessEngine.legalMovesFrom(board, from).firstOrNull { it.to == to && it.promotion == promoType }
                pendingPromo = null
                if (move != null) executeMove(move)
            },
            onDismiss = { pendingPromo = null }
        )
    }
}
