package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailyQuestProgress
import com.example.domain.model.UserGamificationStats
import com.example.ui.components.BauhausGaugeBar
import com.example.ui.components.EditorialKeywordPill
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.LionHeroStage
import com.example.ui.components.LionStateEvolutionStrip
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

data class ModuleTileItem(
    val route: String,
    val titleFr: String,
    val titleEn: String,
    val subtitleFr: String,
    val subtitleEn: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badgeText: String? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    completedLessonsCount: Int,
    totalLessonsCount: Int,
    solvedPuzzlesCount: Int,
    totalPuzzlesCount: Int,
    stats: UserGamificationStats,
    onClaimQuest: (DailyQuestProgress) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current

    val modules = listOf(
        ModuleTileItem(
            "lessons",
            "Apprendre les Bases", "Learn the Basics",
            "Pièces, clouage, fourchette", "Pieces, pins, forks",
            Icons.Default.School, palette.lionMane,
            "$completedLessonsCount/$totalLessonsCount"
        ),
        ModuleTileItem(
            "puzzles",
            "Puzzles Tactiques", "Tactical Puzzles",
            "Sprint 3m, Streak, 30 défis", "Sprint 3m, Streak, 30 drills",
            Icons.Default.Extension, palette.textPrimary,
            "$solvedPuzzlesCount/$totalPuzzlesCount"
        ),
        ModuleTileItem(
            "openings",
            "Ouvertures", "Openings",
            "Arbre 21 nœuds & SM-2", "21-node tree & SM-2",
            Icons.Default.Explore, palette.success
        ),
        ModuleTileItem(
            "middlegame",
            "Milieu de Jeu", "Middlegame",
            "Avant-postes, 7e, plans", "Outposts, 7th rank, plans",
            Icons.Default.AutoAwesome, palette.warning
        ),
        ModuleTileItem(
            "endgames",
            "Finales", "Endgames",
            "Carré, Lucena, IA live", "Square, Lucena, live AI",
            Icons.Default.Flag, palette.lionMane
        ),
        ModuleTileItem(
            "analysis",
            "Analyse PGN", "PGN Analysis",
            "Éval centipions & courbe", "Centipawn eval & chart",
            Icons.Default.Analytics, palette.textPrimary
        ),
        ModuleTileItem(
            "historical",
            "Parties Historiques", "Historical Games",
            "Guess the Move (5 légendes)", "Guess the Move (5 classics)",
            Icons.Default.HistoryEdu, palette.success
        ),
        ModuleTileItem(
            "coordinates",
            "Repérage Cases", "Coordinate Trainer",
            "Chrono 30s & mode inverse", "30s timer & reverse mode",
            Icons.Default.GridOn, palette.warning
        ),
        ModuleTileItem(
            "quiz",
            "Quiz Échecs", "Chess Quiz",
            "20 questions illustrées", "20 diagram questions",
            Icons.Default.Quiz, palette.lionMane
        ),
        ModuleTileItem(
            "play_ai",
            "Jouer contre l'IA", "Play vs AI",
            "Elo 400 à 2500 hors ligne", "Offline Elo 400–2500",
            Icons.Default.PlayArrow, palette.textPrimary
        ),
        ModuleTileItem(
            "progress",
            "Progression", "Progression",
            "Radar, XP & Trophées", "Radar, XP & Badges",
            Icons.AutoMirrored.Filled.TrendingUp, palette.success
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Editorial Masthead (matching むちねこ / CONCEPT DESIGN header)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("Échecs Coach", "Chess Coach"),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = tr(
                                "C O N C E P T  •  100% HORS LIGNE",
                                "C O N C E P T  •  100% OFFLINE"
                            ),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp,
                            color = palette.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(palette.glassSurfaceElevated)
                            .border(1.4.dp, palette.glassBorderStart, CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = tr("Paramètres", "Settings"),
                            tint = palette.textPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditorialKeywordPill("Elo ~${stats.estimatedTacticalElo}", highlighted = true)
                    EditorialKeywordPill(tr("Gratuit", "Free"))
                    EditorialKeywordPill("Open Source")
                    EditorialKeywordPill("Bauhaus")
                }
            }
        }

        // Gamified Vertical Lion Hero Viewport (inspired by reference phone frame!)
        item(span = { GridItemSpan(maxLineSpan) }) {
            LionHeroStage(
                xp = stats.totalXp,
                goldenCrowns = stats.goldenCrowns,
                streakDays = stats.currentStreakDays,
                estimatedElo = stats.estimatedTacticalElo,
                rank = stats.rank,
                levelProgressFraction = stats.levelProgressFraction,
                levelProgressPercent = stats.levelProgressPercent
            )
        }

        // Quick Primary Actions Strip
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate("puzzles") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.lionMane,
                        contentColor = Color(0xFF111111)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .border(1.5.dp, palette.glassBorderStart, RoundedCornerShape(14.dp))
                        .testTag("quick_puzzles_btn")
                ) {
                    Text(tr("Puzzles Tactiques", "Tactical Puzzles"), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onNavigate("play_ai") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.textPrimary,
                        contentColor = palette.bgTop
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("quick_play_ai_btn")
                ) {
                    Text(tr("Défier l'IA", "Play vs AI"), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Daily Quests Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            GlassCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("MISSIONS DU JOUR DE LÉON", "LEO'S DAILY MISSIONS"),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = palette.textSecondary
                    )
                    GlassBadge(
                        text = "${stats.dailyQuests.count { it.claimed || it.current >= it.target }}/${stats.dailyQuests.size}",
                        color = palette.lionMane
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    stats.dailyQuests.forEach { quest ->
                        val frac = if (quest.target <= 0) 1f else quest.current.toFloat() / quest.target
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quest.title.get(isEn),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                BauhausGaugeBar(
                                    progress = frac,
                                    fillColor = if (quest.claimed) palette.success else palette.lionMane,
                                    height = 10.dp
                                )
                            }
                            when {
                                quest.claimed -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = tr("Validée", "Claimed"),
                                        tint = palette.success
                                    )
                                }
                                quest.isReadyToClaim -> {
                                    Button(
                                        onClick = { onClaimQuest(quest) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.success,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text("+${quest.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    GlassBadge(
                                        text = "${quest.current}/${quest.target}",
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lion Evolution Strip (echoing CAT STATE from reference design!)
        item(span = { GridItemSpan(maxLineSpan) }) {
            LionStateEvolutionStrip(currentRank = stats.rank)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("TOUS LES MODULES (11)", "ALL MODULES (11)"),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = tr("Accès direct 1 tap", "1-Tap Direct Access"),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
            }
        }

        itemsIndexed(modules, key = { _, item -> item.route }) { index, item ->
            val num = (index + 1).toString().padStart(2, '0')
            GlassCard(
                onClick = { onNavigate(item.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("module_card_${item.route}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.glassSurfaceElevated)
                            .border(1.2.dp, palette.glassBorderStart, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = palette.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (item.badgeText != null) {
                        GlassBadge(text = item.badgeText, color = item.accentColor)
                    } else {
                        Text(
                            text = num,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = tr(item.titleFr, item.titleEn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tr(item.subtitleFr, item.subtitleEn),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LearnHubScreen(
    completedLessonsCount: Int,
    totalLessonsCount: Int,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val learningModules = listOf(
        ModuleTileItem(
            "lessons",
            "3.1 Apprendre les Bases", "3.1 Learn the Basics",
            "Déplacement des pièces, roque, prise en passant, fourchette, clouage, découverte.",
            "Piece movements, castling, en passant, forks, pins, and discovered checks.",
            Icons.Default.School, palette.lionMane,
            "$completedLessonsCount/$totalLessonsCount"
        ),
        ModuleTileItem(
            "openings",
            "3.3 Ouvertures & Répertoire", "3.3 Openings & Repertoire",
            "Explorateur 21 nœuds, 10 classiques et répétition espacée SM-2.",
            "21-node explorer, 10 classics, and SM-2 spaced repetition.",
            Icons.Default.Explore, palette.success
        ),
        ModuleTileItem(
            "middlegame",
            "3.4 Milieu de Jeu", "3.4 Middlegame Strategy",
            "Centre, avant-postes, 7e rangée, attaque de minorité et prophylaxie.",
            "Center, outposts, 7th rank, minority attack, and prophylaxis.",
            Icons.Default.AutoAwesome, palette.warning
        ),
        ModuleTileItem(
            "endgames",
            "3.5 Finales Essentielles", "3.5 Essential Endgames",
            "Roi+Dame, Roi+Tour, Règle du Carré, Lucena, Philidor & entraîneur IA.",
            "K+Q, K+R, Rule of the Square, Lucena, Philidor & live AI trainer.",
            Icons.Default.Flag, palette.lionMane
        ),
        ModuleTileItem(
            "historical",
            "3.7 Parties Historiques", "3.7 Historical Games",
            "Morphy, Capablanca, Fischer, Kasparov, Carlsen + Guess the Move.",
            "Morphy, Capablanca, Fischer, Kasparov, Carlsen + Guess the Move.",
            Icons.Default.HistoryEdu, palette.textPrimary
        ),
        ModuleTileItem(
            "quiz",
            "3.9 Quiz Thématiques", "3.9 Thematic Quizzes",
            "20 questions illustrées : règles, tactique, ouvertures et finales.",
            "20 illustrated questions: rules, tactics, openings, and endgames.",
            Icons.Default.Quiz, palette.success
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.glassSurfaceElevated)
                            .border(1.4.dp, palette.glassBorderStart, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = palette.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = tr("Parcours d'Apprentissage", "Learning Path"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = tr("Progressez étape par étape avec Léon", "Step-by-step mastery with Leo"),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = tr("Paramètres", "Settings"),
                        tint = palette.textPrimary
                    )
                }
            }
        }

        items(learningModules.size) { index ->
            val mod = learningModules[index]
            GlassCard(
                onClick = { onNavigate(mod.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("learn_card_${mod.route}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.glassSurfaceElevated)
                            .border(1.2.dp, palette.glassBorderStart, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = mod.icon,
                            contentDescription = null,
                            tint = palette.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tr(mod.titleFr, mod.titleEn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (mod.badgeText != null) {
                                GlassBadge(text = mod.badgeText, color = mod.accentColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tr(mod.subtitleFr, mod.subtitleEn),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}
