package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.bot.Cause
import com.example.domain.bot.CoachVoice
import com.example.domain.bot.Grade
import com.example.domain.bot.MoveCoach
import com.example.domain.bot.MoveNote
import com.example.domain.bot.ReviewBuilder
import com.example.domain.chess.ChessEngine
import com.example.domain.chess.PgnParser
import com.example.domain.model.Square
import com.example.ui.GameReviewData
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassTopBar
import com.example.ui.components.InkButton
import com.example.ui.components.LineButton
import com.example.ui.components.SectionLabel
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import com.github.bhlangonijr.chesslib.Board
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** FEN avant chaque coup (taille = nombre de coups + 1). */
private fun replayFens(movesUci: List<String>): List<String> {
    val board = Board()
    val fens = ArrayList<String>()
    fens.add(board.fen)
    for (uci in movesUci) {
        val m = board.legalMoves().firstOrNull { MoveCoach.uciOf(it) == uci } ?: break
        board.doMove(m, false)
        fens.add(board.fen)
    }
    return fens
}

@Composable
fun ReviewScreen(
    data: GameReviewData,
    onAnalyzePgn: (String) -> Unit,
    onRematch: () -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val notes = remember(data) { mutableStateMapOf<Int, MoveNote>().apply { putAll(data.notes) } }
    val userPlies = remember(data) {
        (1..data.movesUci.size).filter { (it % 2 == 1) == data.userIsWhite }
    }
    var done by remember(data) { mutableIntStateOf(userPlies.count { data.notes.containsKey(it) }) }

    LaunchedEffect(data) {
        val fens = withContext(Dispatchers.Default) { replayFens(data.movesUci) }
        for (ply in userPlies) {
            if (notes.containsKey(ply)) continue
            val before = fens.getOrNull(ply - 1) ?: continue
            val note = withContext(Dispatchers.Default) {
                MoveCoach.analyzeMove(before, data.movesUci[ply - 1], ply, depth = 3, timeMs = 500)
            }
            if (note != null) notes[ply] = note
            done = notes.size
        }
    }

    val ordered = notes.values.sortedBy { it.ply }
    val summary = ReviewBuilder.summarize(ordered)
    val outcome = when (data.result) {
        "1-0" -> if (data.userIsWhite) 1 else -1
        "0-1" -> if (data.userIsWhite) -1 else 1
        else -> 0
    }
    val botName = if (isEn) data.botNameEn else data.botNameFr
    val youLabel = tr("Joueur", "Player")

    Column(modifier = Modifier.fillMaxWidth()) {
        GlassTopBar(
            title = tr("Bilan", "Review"),
            subtitle = "vs $botName · ${data.botElo}",
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 620.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Verdict
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.textPrimary)
                        .padding(18.dp)
                ) {
                    Text(
                        text = tr("PRÉCISION", "ACCURACY"),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp,
                        color = palette.bgTop.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (ordered.isEmpty()) "–" else "${summary.accuracy}%",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 52.sp,
                        color = palette.bgTop
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (ordered.isEmpty()) tr("Analyse en cours…", "Analyzing…")
                        else ReviewBuilder.verdict(summary.accuracy, outcome, isEn),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.bgTop
                    )
                    if (done < userPlies.size) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = tr("Analyse : $done/${userPlies.size} coups", "Analysis: $done/${userPlies.size} moves"),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = palette.bgTop.copy(alpha = 0.7f)
                        )
                    }
                }

                // Compteurs
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile(tr("Excellents", "Best"), (summary.counts[Grade.BEST] ?: 0) + (summary.counts[Grade.GOOD] ?: 0), Modifier.weight(1f))
                    StatTile(tr("Imprécis", "Inaccurate"), summary.counts[Grade.INACCURACY] ?: 0, Modifier.weight(1f))
                    StatTile(tr("Erreurs", "Mistakes"), summary.counts[Grade.MISTAKE] ?: 0, Modifier.weight(1f))
                    StatTile(tr("Gaffes", "Blunders"), summary.counts[Grade.BLUNDER] ?: 0, Modifier.weight(1f))
                }

                // Courbe
                if (ordered.size >= 2) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.glassSurface)
                            .border(1.dp, palette.paperTaupe.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        SectionLabel(tr("Tes chances de gain, coup après coup", "Your winning chances, move by move"))
                        Spacer(Modifier.height(8.dp))
                        WinChart(ordered, Modifier.fillMaxWidth().height(90.dp))
                    }
                }

                // Moments clés
                if (summary.keyMoments.isNotEmpty()) {
                    SectionLabel(tr("Là où tu as perdu la partie", "Where you lost the game"))
                    summary.keyMoments.forEach { note ->
                        KeyMomentCard(note, data.userIsWhite, isEn)
                    }
                } else if (ordered.isNotEmpty()) {
                    SectionLabel(tr("Moments clés", "Key moments"))
                    Text(
                        tr("Aucune erreur grave. Reste exigeant : les inexactitudes comptent.", "No serious errors. Stay demanding: inaccuracies add up."),
                        color = palette.textSecondary
                    )
                }

                // Habitudes
                if (summary.habits.isNotEmpty()) {
                    SectionLabel(tr("À travailler maintenant", "Work on this now"))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.glassSurface)
                            .border(1.dp, palette.paperTaupe.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        summary.habits.forEachIndexed { i, cause ->
                            Row {
                                Text(
                                    text = "${i + 1}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    modifier = Modifier.width(22.dp)
                                )
                                Text(CoachVoice.habit(cause, isEn), color = palette.textPrimary)
                            }
                        }
                    }
                }

                InkButton(text = tr("Revanche", "Rematch"), onClick = onRematch, modifier = Modifier.fillMaxWidth())
                LineButton(
                    text = tr("Analyse approfondie (PGN)", "Deep analysis (PGN)"),
                    onClick = {
                        val sanBoard = Board()
                        val san = ArrayList<String>()
                        for (uci in data.movesUci) {
                            val m = sanBoard.legalMoves().firstOrNull { MoveCoach.uciOf(it) == uci } ?: break
                            san.add(MoveCoach.sanOf(sanBoard, m))
                            sanBoard.doMove(m, false)
                        }
                        val you = youLabel
                        val white = if (data.userIsWhite) you else botName
                        val black = if (data.userIsWhite) botName else you
                        onAnalyzePgn(PgnParser.exportPgn(san, white, black, data.result))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: Int, modifier: Modifier = Modifier) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.glassSurface)
            .border(1.dp, palette.paperTaupe.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = palette.textPrimary
        )
        Text(text = label, fontSize = 10.sp, color = palette.textSecondary, maxLines = 1)
    }
}

@Composable
private fun WinChart(notes: List<MoveNote>, modifier: Modifier = Modifier) {
    val palette = LocalGlassPalette.current
    val ink = palette.textPrimary
    val grid = palette.paperTaupe
    val bad = palette.error
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(grid, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1.5f)
        val pts = notes.map { MoveCoach.winPct(it.playedScore) }
        if (pts.size < 2) return@Canvas
        val path = Path()
        pts.forEachIndexed { i, p ->
            val x = w * i / (pts.size - 1)
            val y = h * (1f - (p / 100f).toFloat())
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, ink, style = Stroke(width = 3f, cap = StrokeCap.Round))
        notes.forEachIndexed { i, note ->
            if (note.grade >= Grade.MISTAKE) {
                val x = w * i / (pts.size - 1)
                val y = h * (1f - (pts[i] / 100f).toFloat())
                drawCircle(bad, radius = 6f, center = Offset(x, y))
            }
        }
    }
}

@Composable
private fun KeyMomentCard(note: MoveNote, userWhite: Boolean, isEn: Boolean) {
    val palette = LocalGlassPalette.current
    val moveNo = (note.ply + 1) / 2
    val arrow: Pair<Square, Square>? = note.bestUci?.let { u ->
        val a = Square.fromAlgebraic(u.take(2))
        val b = Square.fromAlgebraic(u.drop(2).take(2))
        if (a != null && b != null) a to b else null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.glassSurface)
            .border(1.dp, palette.error.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${tr("Coup", "Move")} $moveNo · ${note.san}",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = CoachVoice.gradeLabel(note.grade, isEn).uppercase(),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = palette.error
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(250.dp)) {
                ChessBoardView(
                    board = remember(note.fenBefore) { ChessEngine.fromFen(note.fenBefore) },
                    flipped = !userWhite,
                    showCoordinates = false,
                    hintArrow = arrow
                )
            }
        }
        Text(CoachVoice.line(note, isEn), color = palette.textPrimary, style = MaterialTheme.typography.bodyMedium)
        if (note.bestSan != null) {
            Text(
                text = tr("Le bon coup : ${note.bestSan}", "Best move: ${note.bestSan}"),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 12.sp,
                color = palette.textSecondary
            )
        }
        if (note.cause != Cause.NONE && note.cause != Cause.GENERIC) {
            Text(
                text = "→ " + CoachVoice.habit(note.cause, isEn),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary
            )
        }
    }
}
