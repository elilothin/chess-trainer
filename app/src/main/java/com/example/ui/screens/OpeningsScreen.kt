package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.RepertoireLineEntity
import com.example.domain.chess.ChessEngine
import com.example.domain.model.ChessMove
import com.example.domain.model.OpeningInfo
import com.example.domain.model.OpeningStatMove
import com.example.domain.model.PieceColor
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
import kotlinx.coroutines.launch

enum class OpeningsTab(val fr: String, val en: String) {
    EXPLORER("Explorateur", "Explorer"),
    LESSONS("Leçons", "Lessons"),
    REPERTOIRE("Répertoire", "Repertoire")
}

@Composable
fun OpeningsScreen(
    openingBook: Map<String, Pair<String, List<OpeningStatMove>>>,
    classicOpenings: List<OpeningInfo>,
    repertoireLines: List<RepertoireLineEntity>,
    showCoordinates: Boolean,
    onSaveRepertoireLine: (String, Boolean, String, String) -> Unit,
    onReviewRepertoireLine: (RepertoireLineEntity, Boolean) -> Unit,
    onDeleteRepertoireLine: (Long) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    var activeTab by remember { mutableStateOf(OpeningsTab.EXPLORER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassTopBar(
            title = tr("Ouvertures", "Openings"),
            subtitle = tr("Explorateur, Leçons & Répétition espacée", "Explorer, Lessons & Spaced Repetition"),
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OpeningsTab.entries.forEach { tab ->
                GlassFilterChip(
                    label = tr(tab.fr, tab.en),
                    selected = activeTab == tab,
                    onClick = { activeTab = tab }
                )
            }
        }

        when (activeTab) {
            OpeningsTab.EXPLORER -> OpeningExplorerContent(
                openingBook = openingBook,
                showCoordinates = showCoordinates
            )
            OpeningsTab.LESSONS -> ClassicOpeningsContent(
                openings = classicOpenings,
                showCoordinates = showCoordinates
            )
            OpeningsTab.REPERTOIRE -> PersonalRepertoireContent(
                lines = repertoireLines,
                showCoordinates = showCoordinates,
                onSaveLine = onSaveRepertoireLine,
                onReviewLine = onReviewRepertoireLine,
                onDeleteLine = onDeleteRepertoireLine
            )
        }
    }
}

@Composable
private fun OpeningExplorerContent(
    openingBook: Map<String, Pair<String, List<OpeningStatMove>>>,
    showCoordinates: Boolean
) {
    val palette = LocalGlassPalette.current
    val moveHistoryUci = remember { mutableStateListOf<String>() }
    val moveHistorySan = remember { mutableStateListOf<String>() }
    var board by remember { mutableStateOf(ChessEngine.initialBoard()) }
    var selectedSq by remember { mutableStateOf<Square?>(null) }
    var legalTargets by remember { mutableStateOf<Set<Square>>(emptySet()) }
    var lastMove by remember { mutableStateOf<ChessMove?>(null) }

    val key = moveHistoryUci.joinToString(" ")
    val bookEntry = openingBook[key]
    val openingName = bookEntry?.first ?: tr("Hors livre / Ligne personnelle", "Out of book / Custom line")
    val candidateMoves = bookEntry?.second.orEmpty()

    fun playMove(move: ChessMove) {
        val san = ChessEngine.toStandardSan(board, move)
        board = ChessEngine.applyMove(board, move)
        moveHistoryUci.add(move.toUci())
        moveHistorySan.add(san)
        lastMove = move
        selectedSq = null
        legalTargets = emptySet()
    }

    fun stepBack() {
        if (moveHistoryUci.isEmpty()) return
        moveHistoryUci.removeAt(moveHistoryUci.lastIndex)
        moveHistorySan.removeAt(moveHistorySan.lastIndex)
        var b = ChessEngine.initialBoard()
        var lm: ChessMove? = null
        for (uci in moveHistoryUci) {
            val m = ChessEngine.moveFromUci(b, uci) ?: break
            b = ChessEngine.applyMove(b, m)
            lm = m
        }
        board = b
        lastMove = lm
        selectedSq = null
        legalTargets = emptySet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassCard(contentPadding = 12.dp) {
                Text(text = openingName, style = MaterialTheme.typography.titleMedium, color = palette.accentCyan)
                if (moveHistorySan.isNotEmpty()) {
                    Text(
                        text = moveHistorySan.chunked(2).mapIndexed { i, pair ->
                            "${i + 1}. ${pair.joinToString(" ")}"
                        }.joinToString("  "),
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            }

            ChessBoardView(
                board = board,
                selectedSquare = selectedSq,
                legalTargets = legalTargets,
                lastMove = lastMove,
                showCoordinates = showCoordinates,
                onSquareClick = { sq ->
                    val sel = selectedSq
                    if (sel != null && sq in legalTargets) {
                        val m = ChessEngine.legalMovesFrom(board, sel).firstOrNull { it.to == sq }
                        if (m != null) playMove(m)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { stepBack() },
                    enabled = moveHistoryUci.isNotEmpty(),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = palette.textPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Reculer", "Undo"), color = palette.textPrimary)
                }
                OutlinedButton(
                    onClick = {
                        moveHistoryUci.clear()
                        moveHistorySan.clear()
                        board = ChessEngine.initialBoard()
                        lastMove = null
                        selectedSq = null
                        legalTargets = emptySet()
                    },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Text(tr("Réinitialiser", "Reset"), color = palette.textPrimary)
                }
            }

            Text(
                text = tr("Coups les plus joués (Statistiques Maîtres)", "Most Played Moves (Master Stats)"),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary
            )

            if (candidateMoves.isEmpty()) {
                GlassCard {
                    Text(
                        text = tr(
                            "Fin de l'arbre d'ouverture intégré pour cette variante. Revenez en arrière ou explorez une autre ligne.",
                            "End of built-in opening tree for this line. Step back to explore another branch."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            } else {
                candidateMoves.forEach { stat ->
                    GlassCard(
                        onClick = {
                            ChessEngine.moveFromUci(board, stat.uci)?.let { playMove(it) }
                        },
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.san,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.accentCyan
                            )
                            Text(
                                text = "${stat.gamesCount / 1000}k ${tr("parties", "games")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // W / D / B bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .border(1.dp, palette.glassBorderStart.copy(alpha = 0.5f), RoundedCornerShape(50))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(stat.whiteWinPct.toFloat().coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(Color(0xFFF1F5F9))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(stat.drawPct.toFloat().coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(Color(0xFF94A3B8))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(stat.blackWinPct.toFloat().coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(Color(0xFF1E293B))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tr("Blancs", "White")} ${stat.whiteWinPct}%  •  ${tr("Nul", "Draw")} ${stat.drawPct}%  •  ${tr("Noirs", "Black")} ${stat.blackWinPct}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ClassicOpeningsContent(
    openings: List<OpeningInfo>,
    showCoordinates: Boolean
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    var selectedOpening by remember { mutableStateOf(openings.first()) }
    var plyIdx by remember(selectedOpening.id) { mutableIntStateOf(selectedOpening.movesUci.size) }

    val boardAndLast = remember(selectedOpening.id, plyIdx) {
        var b = ChessEngine.initialBoard()
        var lm: ChessMove? = null
        for (i in 0 until plyIdx.coerceAtMost(selectedOpening.movesUci.size)) {
            val m = ChessEngine.moveFromUci(b, selectedOpening.movesUci[i]) ?: break
            b = ChessEngine.applyMove(b, m)
            lm = m
        }
        b to lm
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                openings.forEach { op ->
                    GlassFilterChip(
                        label = op.name.get(isEn),
                        selected = selectedOpening.id == op.id,
                        onClick = { selectedOpening = op }
                    )
                }
            }

            GlassCard(contentPadding = 14.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedOpening.name.get(isEn),
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    GlassBadge(text = selectedOpening.eco, color = palette.accentCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = selectedOpening.keyIdeas.get(isEn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedOpening.trapOrTip.get(isEn),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.accentMagenta
                )
            }

            ChessBoardView(
                board = boardAndLast.first,
                lastMove = boardAndLast.second,
                showCoordinates = showCoordinates
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { if (plyIdx > 0) plyIdx-- },
                    enabled = plyIdx > 0,
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Text("← ${tr("Précédent", "Prev")}", color = palette.textPrimary)
                }
                Button(
                    onClick = { if (plyIdx < selectedOpening.movesUci.size) plyIdx++ },
                    enabled = plyIdx < selectedOpening.movesUci.size,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Text("${tr("Coup suivant", "Next Move")} →", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PersonalRepertoireContent(
    lines: List<RepertoireLineEntity>,
    showCoordinates: Boolean,
    onSaveLine: (String, Boolean, String, String) -> Unit,
    onReviewLine: (RepertoireLineEntity, Boolean) -> Unit,
    onDeleteLine: (Long) -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val scope = rememberCoroutineScope()

    var creatingNew by remember { mutableStateOf(false) }
    var drillingLine by remember { mutableStateOf<RepertoireLineEntity?>(null) }

    // Builder state
    var lineName by remember { mutableStateOf("") }
    var lineNotes by remember { mutableStateOf("") }
    var colorWhite by remember { mutableStateOf(true) }
    val recordedMoves = remember { mutableStateListOf<String>() }
    var builderBoard by remember { mutableStateOf(ChessEngine.initialBoard()) }
    var selSq by remember { mutableStateOf<Square?>(null) }
    var legals by remember { mutableStateOf<Set<Square>>(emptySet()) }

    BackHandler(enabled = drillingLine != null || creatingNew) {
        if (drillingLine != null) drillingLine = null else creatingNew = false
    }

    if (drillingLine != null) {
        val line = drillingLine!!
        val uciSeq = remember(line.id) { line.movesUci.split(" ").filter { it.isNotBlank() } }
        var step by remember(line.id) { mutableIntStateOf(0) }
        var drillBoard by remember(line.id) { mutableStateOf(ChessEngine.initialBoard()) }
        var drillSel by remember(line.id) { mutableStateOf<Square?>(null) }
        var drillLegals by remember(line.id) { mutableStateOf<Set<Square>>(emptySet()) }
        var drillLast by remember(line.id) { mutableStateOf<ChessMove?>(null) }
        var drillError by remember(line.id) { mutableStateOf<String?>(null) }
        var hadError by remember(line.id) { mutableStateOf(false) }
        var completed by remember(line.id) { mutableStateOf(false) }

        val userColor = if (line.colorWhite) PieceColor.WHITE else PieceColor.BLACK

        // Auto-play opponent move if it's opponent's turn
        androidx.compose.runtime.LaunchedEffect(line.id, step) {
            if (!completed && step < uciSeq.size && drillBoard.turn != userColor) {
                delay(350)
                val m = ChessEngine.moveFromUci(drillBoard, uciSeq[step])
                if (m != null) {
                    drillBoard = ChessEngine.applyMove(drillBoard, m)
                    drillLast = m
                    step++
                    if (step >= uciSeq.size) {
                        completed = true
                        onReviewLine(line, !hadError)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(contentPadding = 12.dp) {
                    Text(text = line.name, style = MaterialTheme.typography.titleMedium, color = palette.accentCyan)
                    Text(
                        text = if (completed) {
                            tr("✓ Révision terminée ! Intervalle mis à jour.", "✓ Review complete! Spaced interval updated.")
                        } else {
                            tr("Jouez les coups de votre répertoire (${step}/${uciSeq.size})", "Play your repertoire moves (${step}/${uciSeq.size})")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (completed) palette.success else palette.textSecondary
                    )
                }

                ChessBoardView(
                    board = drillBoard,
                    selectedSquare = drillSel,
                    legalTargets = drillLegals,
                    lastMove = drillLast,
                    flipped = !line.colorWhite,
                    showCoordinates = showCoordinates,
                    onSquareClick = { sq ->
                        if (completed || drillBoard.turn != userColor) return@ChessBoardView
                        val sel = drillSel
                        if (sel != null && sq in drillLegals) {
                            val move = ChessEngine.legalMovesFrom(drillBoard, sel).firstOrNull { it.to == sq }
                            val expected = uciSeq.getOrNull(step)
                            if (move != null && move.toUci() == expected) {
                                drillBoard = ChessEngine.applyMove(drillBoard, move)
                                drillLast = move
                                drillSel = null
                                drillLegals = emptySet()
                                drillError = null
                                step++
                                if (step >= uciSeq.size) {
                                    completed = true
                                    onReviewLine(line, !hadError)
                                }
                            } else {
                                hadError = true
                                drillError = loc(isEn, "Coup attendu différent ! Indice : ${expected ?: ""}", "Different move expected! Hint: ${expected ?: ""}")
                                drillSel = null
                                drillLegals = emptySet()
                            }
                        } else {
                            val p = drillBoard.pieceAt(sq)
                            if (p != null && p.color == drillBoard.turn) {
                                drillSel = sq
                                drillLegals = ChessEngine.legalMovesFrom(drillBoard, sq).map { it.to }.toSet()
                            } else {
                                drillSel = null
                                drillLegals = emptySet()
                            }
                        }
                    }
                )

                if (drillError != null) {
                    GlassCard(accentBorderColor = palette.warning) {
                        Text(text = drillError!!, color = palette.warning, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedButton(
                    onClick = { drillingLine = null },
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                ) {
                    Text(tr("Retour au répertoire", "Back to Repertoire"), color = palette.textPrimary)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!creatingNew) {
            Button(
                onClick = {
                    creatingNew = true
                    recordedMoves.clear()
                    builderBoard = ChessEngine.initialBoard()
                    lineName = ""
                    lineNotes = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(tr("Ajouter une ligne au répertoire", "Add Repertoire Line"), fontWeight = FontWeight.Bold)
            }

            lines.forEach { line ->
                GlassCard(contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = line.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text(
                                text = "${if (line.colorWhite) tr("Blancs", "White") else tr("Noirs", "Black")} • ${tr("Intervalle", "Interval")}: ${line.intervalDays}j",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.accentCyan
                            )
                            if (line.notes.isNotBlank()) {
                                Text(text = line.notes, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                            }
                        }
                        IconButton(onClick = { onDeleteLine(line.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = tr("Supprimer", "Delete"), tint = palette.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { drillingLine = line },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.textPrimary,
                            contentColor = if (palette.isDark) Color(0xFF111111) else Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Réviser (Répétition espacée)", "Review (Spaced Repetition)"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            GlassCard {
                Text(
                    text = tr("Jouez les coups sur l'échiquier puis enregistrez :", "Play moves on the board, then save:"),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lineName,
                    onValueChange = { lineName = it },
                    label = { Text(tr("Nom de l'ouverture", "Opening Name")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lineNotes,
                    onValueChange = { lineNotes = it },
                    label = { Text(tr("Notes personnelles", "Personal Notes")) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(tr("Jouée avec les Blancs ?", "Play as White?"), color = palette.textPrimary)
                    Switch(checked = colorWhite, onCheckedChange = { colorWhite = it })
                }
            }

            ChessBoardView(
                board = builderBoard,
                selectedSquare = selSq,
                legalTargets = legals,
                flipped = !colorWhite,
                showCoordinates = showCoordinates,
                onSquareClick = { sq ->
                    val sel = selSq
                    if (sel != null && sq in legals) {
                        val m = ChessEngine.legalMovesFrom(builderBoard, sel).firstOrNull { it.to == sq }
                        if (m != null) {
                            builderBoard = ChessEngine.applyMove(builderBoard, m)
                            recordedMoves.add(m.toUci())
                            selSq = null
                            legals = emptySet()
                        }
                    } else {
                        val p = builderBoard.pieceAt(sq)
                        if (p != null && p.color == builderBoard.turn) {
                            selSq = sq
                            legals = ChessEngine.legalMovesFrom(builderBoard, sq).map { it.to }.toSet()
                        } else {
                            selSq = null
                            legals = emptySet()
                        }
                    }
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { creatingNew = false },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Text(tr("Annuler", "Cancel"), color = palette.textPrimary)
                }
                Button(
                    onClick = {
                        if (recordedMoves.isNotEmpty()) {
                            onSaveLine(lineName, colorWhite, recordedMoves.joinToString(" "), lineNotes)
                            creatingNew = false
                        }
                    },
                    enabled = recordedMoves.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.success, contentColor = Color.White),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Text(tr("Enregistrer (${recordedMoves.size} coups)", "Save (${recordedMoves.size} moves)"), fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
