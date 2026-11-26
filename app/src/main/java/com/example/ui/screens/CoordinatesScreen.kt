package com.example.ui.screens

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
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.chess.ChessEngine
import com.example.domain.model.LionMood
import com.example.domain.model.Square
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.LionCoachBanner
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import kotlinx.coroutines.delay
import kotlin.random.Random

private fun randomSquare(): Square = Square(Random.nextInt(0, 8), Random.nextInt(0, 8))

@Composable
fun CoordinatesScreen(
    bestScore: Int,
    onSaveSession: (Int, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current

    var reverseMode by remember { mutableStateOf(false) }
    var flipped by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(30) }
    var score by remember { mutableIntStateOf(0) }
    var attempts by remember { mutableIntStateOf(0) }
    var targetSq by remember { mutableStateOf(randomSquare()) }
    var feedbackColor by remember { mutableStateOf<Color?>(null) }

    // Inverse mode quick selector
    var selectedFile by remember { mutableStateOf<Char?>(null) }
    var selectedRank by remember { mutableStateOf<Char?>(null) }

    val board = remember { ChessEngine.initialBoard() }

    LaunchedEffect(running) {
        if (running) {
            while (secondsLeft > 0 && running) {
                delay(1000L)
                secondsLeft--
            }
            if (secondsLeft <= 0) {
                running = false
                val acc = if (attempts == 0) 0 else (score * 100) / attempts
                onSaveSession(score, acc, 30)
            }
        }
    }

    fun startRound() {
        score = 0
        attempts = 0
        secondsLeft = 30
        targetSq = randomSquare()
        selectedFile = null
        selectedRank = null
        feedbackColor = null
        running = true
    }

    fun checkInverseSelection(file: Char?, rank: Char?) {
        if (file != null && rank != null && running) {
            attempts++
            val chosen = "$file$rank"
            if (chosen == targetSq.toAlgebraic()) {
                score++
                feedbackColor = palette.success
                targetSq = randomSquare()
            } else {
                feedbackColor = palette.error
            }
            selectedFile = null
            selectedRank = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = tr("Repérage & Coordonnées", "Coordinate Trainer"),
            subtitle = "${tr("Record personnel", "Personal Best")}: $bestScore",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val lionMood = when {
                !running && score > 0 && score >= bestScore -> LionMood.CROWNED
                running && feedbackColor == palette.error -> LionMood.SHOCKED
                running -> LionMood.THINKING
                else -> LionMood.IDLE_HAPPY
            }
            val lionLine = when {
                !running && score > 0 && score >= bestScore ->
                    tr("Nouveau record ($score) ! Ta vision de l'échiquier devient foudroyante.", "New record ($score)! Your board vision is razor-sharp.")
                running ->
                    tr("Repère vite la case sans coordonnées — ton cerveau mémorise la géométrie !", "Spot the square without labels — your brain is memorizing the geometry!")
                else ->
                    tr("30 secondes chrono avec Léon : vise 15+ cases justes pour progresser vite.", "30-second sprint with Léon: aim for 15+ accurate squares to level up fast.")
            }
            LionCoachBanner(mood = lionMood, message = lionLine)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassFilterChip(
                    label = tr("Standard (Case → Toucher)", "Standard (Name → Tap)"),
                    selected = !reverseMode,
                    onClick = { reverseMode = false; running = false }
                )
                GlassFilterChip(
                    label = tr("Inverse (Case → Nom)", "Reverse (Square → Name)"),
                    selected = reverseMode,
                    onClick = { reverseMode = true; running = false }
                )
            }

            GlassCard(
                elevated = true,
                accentBorderColor = feedbackColor ?: palette.accentCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (running) "⏱ ${secondsLeft}s" else tr("Prêt (30s)", "Ready (30s)"),
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (secondsLeft <= 5 && running) palette.error else palette.accentCyan
                        )
                        Text(
                            text = "${tr("Score", "Score")}: $score  •  ${tr("Essais", "Tries")}: $attempts",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }

                    if (!reverseMode) {
                        Text(
                            text = if (running) targetSq.toAlgebraic() else "--",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.headlineLarge,
                            color = palette.textPrimary
                        )
                    } else {
                        Text(
                            text = "${selectedFile ?: "?"}${selectedRank ?: "?"}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.accentMagenta
                        )
                    }
                }
            }

            ChessBoardView(
                board = board,
                highlightedSquares = if (reverseMode && running) setOf(targetSq) else emptySet(),
                flipped = flipped,
                showCoordinates = !running, // Hide coordinates during timed round to train true board vision!
                onSquareClick = { sq ->
                    if (!running || reverseMode) return@ChessBoardView
                    attempts++
                    if (sq == targetSq) {
                        score++
                        feedbackColor = palette.success
                        targetSq = randomSquare()
                    } else {
                        feedbackColor = palette.error
                    }
                }
            )

            if (reverseMode && running) {
                GlassCard(contentPadding = 12.dp) {
                    Text(
                        text = tr("Choisissez la colonne puis la rangée :", "Pick file then rank:"),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ('a'..'h').forEach { f ->
                            GlassFilterChip(
                                label = f.toString(),
                                selected = selectedFile == f,
                                onClick = {
                                    selectedFile = f
                                    checkInverseSelection(f, selectedRank)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ('1'..'8').forEach { r ->
                            GlassFilterChip(
                                label = r.toString(),
                                selected = selectedRank == r,
                                onClick = {
                                    selectedRank = r
                                    checkInverseSelection(selectedFile, r)
                                }
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { flipped = !flipped },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = palette.textPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (flipped) tr("Vue Noirs", "Black View") else tr("Vue Blancs", "White View"), color = palette.textPrimary)
                }
                Button(
                    onClick = { startRound() },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("start_coord_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (running) tr("Relancer", "Restart") else tr("Démarrer 30s", "Start 30s"), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
