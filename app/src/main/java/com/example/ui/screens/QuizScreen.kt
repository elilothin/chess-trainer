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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.domain.model.LionMood
import com.example.domain.model.QuizCategory
import com.example.domain.model.QuizQuestion
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.LionCoachBanner
import com.example.ui.components.tr
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

@Composable
fun QuizScreen(
    questions: List<QuizQuestion>,
    showCoordinates: Boolean,
    onQuizCompleted: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current

    var selectedCat by remember { mutableStateOf(QuizCategory.ALL) }
    val filtered = remember(questions, selectedCat) {
        if (selectedCat == QuizCategory.ALL) questions else questions.filter { it.category == selectedCat }
    }

    var currentIdx by remember(filtered) { mutableIntStateOf(0) }
    var selectedOption by remember(filtered, currentIdx) { mutableStateOf<Int?>(null) }
    var score by remember(filtered) { mutableIntStateOf(0) }
    var finished by remember(filtered) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = tr("Quiz Échecs", "Chess Quiz"),
            subtitle = tr("Règles, tactique, ouvertures & finales", "Rules, tactics, openings & endgames"),
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuizCategory.entries.forEach { cat ->
                GlassFilterChip(
                    label = tr(cat.fr, cat.en),
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat }
                )
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (finished) {
                val acc = if (filtered.isEmpty()) 0 else (score * 100) / filtered.size
                GlassCard(elevated = true, accentBorderColor = palette.success) {
                    Text(
                        text = tr("Quiz terminé !", "Quiz Completed!"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.success
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${tr("Score final", "Final Score")}: $score / ${filtered.size} ($acc%)",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            currentIdx = 0
                            score = 0
                            selectedOption = null
                            finished = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                    ) {
                        Text(tr("Recommencer le Quiz", "Restart Quiz"), fontWeight = FontWeight.Bold)
                    }
                }
            } else if (filtered.isNotEmpty()) {
                val q = filtered[currentIdx]
                val quizMood = when {
                    selectedOption == null -> LionMood.THINKING
                    selectedOption == q.correctIndex -> LionMood.CHEERING
                    else -> LionMood.SHOCKED
                }
                val quizMsg = when {
                    selectedOption == null -> tr("Réfléchis bien ! Léon attend ta réponse.", "Think carefully! Leo is waiting for your answer.")
                    selectedOption == q.correctIndex -> tr("Bravo ! Excellente culture échiquéenne !", "Bravo! Great chess knowledge!")
                    else -> tr("Raté pour celle-ci — lis l'astuce ci-dessous pour la retenir !", "Missed that one—check the tip below to lock it in!")
                }
                LionCoachBanner(mood = quizMood, message = quizMsg)

                LinearProgressIndicator(
                    progress = { (currentIdx + 1).toFloat() / filtered.size },
                    modifier = Modifier.fillMaxWidth(),
                    color = palette.lionMane,
                    trackColor = palette.textSecondary.copy(alpha = 0.2f)
                )

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassBadge(text = "${currentIdx + 1}/${filtered.size}", color = palette.accentCyan)
                        GlassBadge(text = tr(q.category.fr, q.category.en), color = palette.accentMagenta)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = q.question.get(isEn),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                }

                if (q.boardFen != null) {
                    ChessBoardView(
                        board = remember(q.boardFen) { ChessEngine.fromFen(q.boardFen) },
                        showCoordinates = showCoordinates
                    )
                }

                q.options.forEachIndexed { optIdx, opt ->
                    val answered = selectedOption != null
                    val isSelected = selectedOption == optIdx
                    val isCorrect = optIdx == q.correctIndex
                    val borderColor = when {
                        !answered -> null
                        isCorrect -> palette.success
                        isSelected -> palette.error
                        else -> null
                    }
                    GlassCard(
                        onClick = {
                            if (selectedOption == null) {
                                selectedOption = optIdx
                                if (optIdx == q.correctIndex) score++
                            }
                        },
                        accentBorderColor = borderColor,
                        elevated = isSelected,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = opt.get(isEn),
                            style = MaterialTheme.typography.bodyLarge,
                            color = when {
                                answered && isCorrect -> palette.success
                                answered && isSelected -> palette.error
                                else -> palette.textPrimary
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = selectedOption != null) {
                    GlassCard(accentBorderColor = palette.accentCyan) {
                        Text(
                            text = q.explanation.get(isEn),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (currentIdx + 1 < filtered.size) {
                                    currentIdx++
                                    selectedOption = null
                                } else {
                                    val acc = (score * 100) / filtered.size
                                    onQuizCompleted(score, acc)
                                    finished = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accentCyan, contentColor = Color(0xFF04101B)),
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = if (currentIdx + 1 < filtered.size) tr("Question suivante →", "Next Question →") else tr("Voir le résultat", "See Results"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
