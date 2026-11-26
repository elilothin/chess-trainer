package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.chess.ChessEngine
import com.example.domain.model.GameAnalysisResult
import com.example.domain.model.MoveQuality
import com.example.domain.model.Square
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GameEvaluationChart
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.loc
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish
import kotlinx.coroutines.launch

private const val SAMPLE_PGN = """[Event "Exemple Tactique"]
[White "Élève"]
[Black "Adversaire"]
[Result "1-0"]

1. e4 e5 2. Nf3 Nc6 3. Bc4 Nf6 4. Ng5 d5 5. exd5 Nxd5 6. Nxf7 Kxf7 7. Qf3+ Ke6 8. Nc3 1-0"""

@Composable
fun AnalysisScreen(
    pendingPgn: String?,
    showCoordinates: Boolean,
    onConsumePendingPgn: () -> Unit,
    onAnalyzePgn: suspend (String) -> Result<GameAnalysisResult>,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pgnInput by remember { mutableStateOf(pendingPgn ?: SAMPLE_PGN) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var analysis by remember { mutableStateOf<GameAnalysisResult?>(null) }
    var selectedPlyIdx by remember { mutableIntStateOf(0) }

    fun runAnalysis(text: String) {
        scope.launch {
            isAnalyzing = true
            errorMessage = null
            val res = onAnalyzePgn(text)
            isAnalyzing = false
            res.onSuccess {
                analysis = it
                selectedPlyIdx = 0
            }.onFailure { err ->
                errorMessage = err.message ?: loc(isEn, "PGN invalide", "Invalid PGN")
            }
        }
    }

    LaunchedEffect(pendingPgn) {
        if (!pendingPgn.isNullOrBlank()) {
            pgnInput = pendingPgn
            onConsumePendingPgn()
            runAnalysis(pendingPgn)
        }
    }

    val openPgnFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.onSuccess { text ->
                if (!text.isNullOrBlank()) {
                    pgnInput = text
                    runAnalysis(text)
                }
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
            title = tr("Analyse de Parties (PGN)", "Game Analysis (PGN)"),
            subtitle = tr("Évaluation centipions, erreurs & graphique", "Centipawn eval, blunders & graph"),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard {
                Text(
                    text = tr("Importer un PGN (copier-coller ou fichier)", "Import PGN (paste or pick file)"),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pgnInput,
                    onValueChange = { pgnInput = it },
                    label = { Text("PGN") },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    textStyle = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { openPgnFile.launch(arrayOf("text/*", "application/*")) },
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, tint = palette.accentCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("Fichier", "File"), color = palette.textPrimary)
                    }
                    Button(
                        onClick = { runAnalysis(pgnInput) },
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("analyze_pgn_btn")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Analyser", "Analyze"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                GlassCard(accentBorderColor = palette.error) {
                    Text(text = errorMessage!!, color = palette.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            analysis?.let { result ->
                val activePly = result.plies.getOrNull(selectedPlyIdx)
                val board = if (activePly != null) {
                    ChessEngine.fromFen(activePly.fenAfter)
                } else {
                    ChessEngine.initialBoard()
                }
                val bestArrow = activePly?.bestMoveUci?.let { uci ->
                    val f = Square.fromAlgebraic(uci.take(2))
                    val t = Square.fromAlgebraic(uci.drop(2).take(2))
                    if (f != null && t != null) f to t else null
                }

                // Accuracy summary
                GlassCard(elevated = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "${tr("Blancs", "White")}: ${result.whitePlayer}", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                            Text(text = "${result.whiteAccuracy}% ${tr("précision", "accuracy")}", style = MaterialTheme.typography.titleMedium, color = palette.accentCyan)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${tr("Noirs", "Black")}: ${result.blackPlayer}", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                            Text(text = "${result.blackAccuracy}% ${tr("précision", "accuracy")}", style = MaterialTheme.typography.titleMedium, color = palette.accentMagenta)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    GameEvaluationChart(
                        evalsCp = result.plies.map { it.evalCpAfter },
                        selectedPlyIndex = selectedPlyIdx,
                        onSelectPly = { selectedPlyIdx = it }
                    )
                }

                ChessBoardView(
                    board = board,
                    hintArrow = bestArrow,
                    showCoordinates = showCoordinates
                )

                // Active ply feedback
                if (activePly != null) {
                    val qualityColor = when (activePly.quality) {
                        MoveQuality.BRILLIANT -> palette.accentCyan
                        MoveQuality.BEST, MoveQuality.GOOD -> palette.success
                        MoveQuality.INACCURACY -> palette.warning
                        MoveQuality.MISTAKE, MoveQuality.BLUNDER -> palette.error
                    }
                    val evalPawns = activePly.evalCpAfter / 100.0
                    GlassCard(accentBorderColor = qualityColor) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(activePly.plyIndex + 1) / 2}. ${activePly.san} ${activePly.quality.symbol}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            GlassBadge(
                                text = "${if (evalPawns >= 0) "+" else ""}%.2f".format(evalPawns),
                                color = qualityColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEn) activePly.quality.en else activePly.quality.fr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = qualityColor
                        )
                        if (activePly.bestMoveSan != null && activePly.bestMoveUci != activePly.uci) {
                            Text(
                                text = "${tr("Meilleur coup suggéré", "Best suggested move")}: ${activePly.bestMoveSan} (${tr("flèche verte", "green arrow")})",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                    }
                }

                // Move navigation chips
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    result.plies.forEachIndexed { idx, ply ->
                        val moveNum = if (idx % 2 == 0) "${idx / 2 + 1}." else ""
                        GlassFilterChip(
                            label = "$moveNum${ply.san}${ply.quality.symbol}",
                            selected = selectedPlyIdx == idx,
                            onClick = { selectedPlyIdx = idx }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { if (selectedPlyIdx > 0) selectedPlyIdx-- },
                        enabled = selectedPlyIdx > 0,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                    ) {
                        Text("← ${tr("Précédent", "Prev")}", color = palette.textPrimary)
                    }
                    OutlinedButton(
                        onClick = { if (selectedPlyIdx < result.plies.lastIndex) selectedPlyIdx++ },
                        enabled = selectedPlyIdx < result.plies.lastIndex,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                    ) {
                        Text("${tr("Suivant", "Next")} →", color = palette.textPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
