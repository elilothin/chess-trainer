package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivitySessionEntity
import com.example.data.local.PlayedGameEntity
import com.example.data.local.PuzzleProgressEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode
import com.example.domain.model.LionMood
import com.example.domain.model.UserGamificationStats
import com.example.domain.model.UserSettings
import com.example.ui.components.BauhausGaugeBar
import com.example.ui.components.BauhausSkillRadarChart
import com.example.ui.components.EditorialKeywordPill
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.LionCharacter
import com.example.ui.components.LionStateEvolutionStrip
import com.example.ui.components.ProgressionTrendChart
import com.example.ui.components.loc
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    completedLessons: Int,
    totalLessons: Int,
    puzzleProgressMap: Map<String, PuzzleProgressEntity>,
    totalPuzzles: Int,
    sessions: List<ActivitySessionEntity>,
    playedGames: List<PlayedGameEntity>,
    stats: UserGamificationStats,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current

    val solvedPuzzles = puzzleProgressMap.values.count { it.solved }
    val totalAttempts = puzzleProgressMap.values.sumOf { it.attempts }
    val puzzleAccuracy = if (totalAttempts == 0) 0 else ((solvedPuzzles * 100) / totalAttempts).coerceIn(0, 100)
    val bestCoord = sessions.filter { it.moduleType == "COORDINATES" }.maxOfOrNull { it.score } ?: 0
    val bestSprint = sessions.filter { it.moduleType == "PUZZLE_SPRINT" }.maxOfOrNull { it.score } ?: 0
    val bestStreak = sessions.filter { it.moduleType == "PUZZLE_STREAK" }.maxOfOrNull { it.score } ?: 0
    val avgQuizAcc = sessions.filter { it.moduleType == "QUIZ" }.map { it.accuracyPercent }.average()
        .let { if (it.isNaN()) 0 else it.toInt() }

    val chartPoints = remember(sessions) {
        if (sessions.isEmpty()) listOf(20f, 35f, 50f, 65f)
        else sessions.takeLast(14).map { it.accuracyPercent.toFloat().coerceAtLeast(10f) }
    }

    val radarAxes = remember(stats, isEn) {
        listOf(
            loc(isEn, "Ouvert.", "Openings") to stats.radarScores.openings,
            loc(isEn, "Tactique", "Tactics") to stats.radarScores.tactics,
            loc(isEn, "Stratégie", "Strategy") to stats.radarScores.strategy,
            loc(isEn, "Finales", "Endgames") to stats.radarScores.endgames,
            loc(isEn, "Calcul", "Vision") to stats.radarScores.calculation,
            loc(isEn, "Théorie", "Theory") to stats.radarScores.theory
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(if (!showBackButton) Modifier.statusBarsPadding() else Modifier)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (showBackButton) {
                GlassTopBar(
                    title = tr("Tableau de Progression", "Progress Dashboard"),
                    subtitle = tr("Elo, Radar, XP & Trophées de Léon", "Elo, Radar, XP & Leo Badges"),
                    onBack = onBack
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tr("Tableau de Progression", "Progress Dashboard"), style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
                        Text(tr("Données analytiques hors ligne", "Offline chess analytics"), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Default.Settings, contentDescription = tr("Paramètres", "Settings"), tint = palette.textPrimary)
                    }
                }
            }
        }

        // Léon Rank & XP Capsule Card
        item {
            GlassCard(elevated = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LionCharacter(
                        mood = LionMood.CROWNED,
                        rank = stats.rank,
                        isDark = palette.isDark,
                        modifier = Modifier.size(68.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = loc(isEn, stats.rank.titleFr, stats.rank.titleEn),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = tr(
                                "${stats.totalXp} XP • Elo estimé ~${stats.estimatedTacticalElo}",
                                "${stats.totalXp} XP • Est. Elo ~${stats.estimatedTacticalElo}"
                            ),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = palette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        BauhausGaugeBar(
                            progress = stats.levelProgressFraction,
                            fillColor = palette.lionMane,
                            labelLeft = "0%",
                            labelRight = "${stats.levelProgressPercent}% → ${stats.rank.nextXp} XP"
                        )
                    }
                }
            }
        }

        // KPI Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text(tr("Leçons", "Lessons"), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    Text("$completedLessons / $totalLessons", style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text(tr("Puzzles", "Puzzles"), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    Text("$solvedPuzzles / $totalPuzzles", style = MaterialTheme.typography.headlineMedium, color = palette.lionMane)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text(tr("Précision Puzzles", "Puzzle Accuracy"), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    Text("$puzzleAccuracy%", style = MaterialTheme.typography.titleLarge, color = palette.success)
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text(tr("Record Coordonnées", "Coord Best"), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    Text("$bestCoord", style = MaterialTheme.typography.titleLarge, color = palette.warning)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Sprint 3m / Streak", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    Text("$bestSprint / $bestStreak", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text(tr("Quiz / Parties IA", "Quiz / AI Games"), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    Text("$avgQuizAcc% / ${playedGames.size}", style = MaterialTheme.typography.titleLarge, color = palette.lionMane)
                }
            }
        }

        // 7-Day Activity Bar Chart
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Régularité des 7 derniers jours", "7-Day Training Consistency"),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    GlassBadge(
                        text = tr("${stats.currentStreakDays}j de série", "${stats.currentStreakDays}d streak"),
                        color = palette.lionMane
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                val maxDay = (stats.last7DaysActivity.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    stats.last7DaysActivity.forEach { (dayLabel, count) ->
                        val frac = if (count == 0) 0.10f else (count.toFloat() / maxDay).coerceIn(0.20f, 1f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$count",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = palette.textSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height((56 * frac).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (count > 0) palette.lionMane else palette.paperTaupe.copy(alpha = 0.45f))
                                    .border(1.dp, palette.glassBorderStart, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // 6-Axis Skill Radar Chart
        item {
            GlassCard(elevated = false) {
                Text(
                    text = tr("Profil Stratégique (Radar 6 Axes)", "6-Axis Chess Skill Radar"),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                BauhausSkillRadarChart(axes = radarAxes)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    radarAxes.forEach { (label, score) ->
                        EditorialKeywordPill("$label ${(score * 100).toInt()}%", highlighted = score >= 0.6f)
                    }
                }
            }
        }

        // Accuracy Trend Chart
        item {
            GlassCard {
                Text(
                    text = tr("Courbe de précision dans le temps (%)", "Accuracy Trend Over Time (%)"),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (sessions.isEmpty()) {
                        tr("Complétez des exercices pour alimenter votre graphique réel.", "Complete exercises to populate your live chart.")
                    } else {
                        tr("Basé sur vos ${sessions.size} dernières sessions", "Based on your last ${sessions.size} sessions")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProgressionTrendChart(values = chartPoints, lineColor = palette.lionMane)
            }
        }

        item {
            LionStateEvolutionStrip(currentRank = stats.rank)
        }

        // Achievements List
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Trophées de Léon", "Leo's Achievements"),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    GlassBadge(
                        text = "${stats.achievements.count { it.unlocked }}/${stats.achievements.size}",
                        color = palette.success
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stats.achievements.forEach { ach ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (ach.unlocked) palette.success.copy(alpha = 0.12f)
                                    else palette.glassSurfaceElevated
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (ach.unlocked) palette.success else palette.paperTaupe,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (ach.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (ach.unlocked) palette.lionMane else palette.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.title.get(isEn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = ach.description.get(isEn),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )
                            }
                            if (ach.unlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = palette.success
                                )
                            } else {
                                Text(
                                    text = ach.progressLabel,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetTextScale: (Float) -> Unit,
    onSetShowCoordinates: (Boolean) -> Unit,
    onSetHaptics: (Boolean) -> Unit,
    onImportPgnPack: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val context = LocalContext.current
    var packStatus by remember { mutableStateOf<String?>(null) }

    val openPack = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.onSuccess { content ->
                if (!content.isNullOrBlank()) {
                    packStatus = loc(isEn, "Pack PGN importé avec succès ! Ouvert dans l'Analyseur.", "PGN pack imported! Opened in Analyzer.")
                    onImportPgnPack(content)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassTopBar(
                title = tr("Paramètres", "Settings"),
                subtitle = tr("Thème papier/encre, langue & packs hors ligne", "Paper/Ink theme, language & offline packs"),
                onBack = onBack
            )
        }

        item {
            GlassCard {
                Text(tr("Apparence (Bauhaus Papier & Encre)", "Appearance (Bauhaus Paper & Ink)"), style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassFilterChip(tr("Papier Clair", "Warm Paper"), settings.themeMode == AppThemeMode.LIGHT, { onSetThemeMode(AppThemeMode.LIGHT) })
                    GlassFilterChip(tr("Encre Sombre", "Dark Ink"), settings.themeMode == AppThemeMode.DARK, { onSetThemeMode(AppThemeMode.DARK) })
                    GlassFilterChip(tr("Système", "System"), settings.themeMode == AppThemeMode.SYSTEM, { onSetThemeMode(AppThemeMode.SYSTEM) })
                }
            }
        }

        item {
            GlassCard {
                Text(tr("Langue", "Language"), style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassFilterChip("Français", settings.language == AppLanguage.FR, { onSetLanguage(AppLanguage.FR) })
                    GlassFilterChip("English", settings.language == AppLanguage.EN, { onSetLanguage(AppLanguage.EN) })
                }
            }
        }

        item {
            GlassCard {
                Text(tr("Accessibilité : Taille du texte", "Accessibility: Text Size"), style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassFilterChip("Normal", settings.textScale < 1.05f, { onSetTextScale(1.0f) })
                    GlassFilterChip(tr("Grand", "Large"), settings.textScale in 1.05f..1.18f, { onSetTextScale(1.12f) })
                    GlassFilterChip(tr("Très grand", "XL"), settings.textScale > 1.18f, { onSetTextScale(1.24f) })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr("Afficher les coordonnées (a-h, 1-8)", "Show board coordinates (a-h, 1-8)"), color = palette.textPrimary)
                    Switch(checked = settings.showCoordinates, onCheckedChange = onSetShowCoordinates)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr("Retour haptique", "Haptic feedback"), color = palette.textPrimary)
                    Switch(checked = settings.hapticsEnabled, onCheckedChange = onSetHaptics)
                }
            }
        }

        item {
            GlassCard(accentBorderColor = palette.lionMane) {
                Text(tr("Packs de contenu hors ligne", "Offline Content Packs"), style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tr(
                        "Base incluse : 15 leçons, 30 puzzles tactiques CC0, arbre d'ouvertures 21 nœuds, 10 classiques, 9 exercices milieu de jeu, 10 finales, 5 parties historiques, 20 questions Quiz.",
                        "Bundled core pack: 15 lessons, 30 CC0 puzzles, 21-node opening tree, 10 classics, 9 middlegame drills, 10 endgames, 5 historical games, 20 Quiz questions."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { openPack.launch(arrayOf("text/*", "application/*")) },
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = palette.textPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Importer un pack PGN local", "Import Local PGN Pack"), color = palette.textPrimary)
                }
                if (packStatus != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = packStatus!!, style = MaterialTheme.typography.bodySmall, color = palette.success)
                }
            }
        }

        item {
            GlassCard(accentBorderColor = palette.success) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = palette.success)
                    Text(tr("Vie Privée & Licence Open Source", "Privacy & Open Source License"), style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tr(
                        "Aucune publicité • Aucun tracker • Aucun compte requis • Toutes vos données restent stockées localement sur votre appareil.\n\nLicence Open Source : MIT License (Échecs Coach). Sources libres : Puzzles style Lichess (CC0), Parties historiques (Domaine public).",
                        "Zero ads • Zero trackers • No account required • All progress is stored locally on your device.\n\nOpen Source License: MIT License (Chess Coach). Free resources: Lichess-style Puzzles (CC0), Historical Games (Public Domain)."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassBadge("MIT LICENSE", palette.success)
                    GlassBadge("100% OFFLINE", palette.lionMane)
                }
            }
        }
    }
}
