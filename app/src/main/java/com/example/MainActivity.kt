package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.glassBorderBrush
import com.example.ui.components.glassFillBrush
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.domain.bot.BotRoster
import com.example.data.local.SettingsDataStore
import com.example.data.local.TrainingDatabase
import com.example.data.repository.ChessTrainingRepository
import com.example.ui.ChessTrainerViewModel
import com.example.ui.components.GlassBackground
import com.example.ui.feedback.GameFeedback
import com.example.ui.feedback.LocalGameFeedback
import com.example.ui.components.tr
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.CoordinatesScreen
import com.example.ui.screens.EndgamesScreen
import com.example.ui.screens.HistoricalGamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnHubScreen
import com.example.ui.screens.LessonsScreen
import com.example.ui.screens.MiddlegameScreen
import com.example.ui.screens.OpeningsScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.PlayHubScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.PuzzlesScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EchecsCoachTheme
import com.example.ui.theme.LocalGlassPalette

const val ROUTE_HOME = "home"
const val ROUTE_LEARN = "learn"
const val ROUTE_PLAY_TAB = "play_tab"
const val ROUTE_PROGRESS_TAB = "progress_tab"

const val ROUTE_LESSONS = "lessons"
const val ROUTE_PUZZLES = "puzzles"
const val ROUTE_OPENINGS = "openings"
const val ROUTE_MIDDLEGAME = "middlegame"
const val ROUTE_ENDGAMES = "endgames"
const val ROUTE_ANALYSIS = "analysis"
const val ROUTE_HISTORICAL = "historical"
const val ROUTE_COORDINATES = "coordinates"
const val ROUTE_QUIZ = "quiz"
const val ROUTE_PLAY_AI = "play_ai"
const val ROUTE_GAME = "game/{botId}/{color}/{minutes}"
const val ROUTE_REVIEW = "review"
const val ROUTE_PROGRESS = "progress"
const val ROUTE_SETTINGS = "settings"

private data class BottomTab(
    val route: String,
    val labelFr: String,
    val labelEn: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ChessTrainerViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val db = TrainingDatabase.getInstance(app)
                        val store = SettingsDataStore(app)
                        ChessTrainerViewModel(ChessTrainingRepository(db.trainingDao(), store))
                    }
                }
            )
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            val feedback = remember { GameFeedback(applicationContext) }
            DisposableEffect(feedback) { onDispose { feedback.release() } }
            feedback.soundEnabled = settings.soundEnabled
            feedback.hapticsEnabled = settings.hapticsEnabled

            EchecsCoachTheme(
                themeMode = settings.themeMode,
                language = settings.language,
                textScale = settings.textScale
            ) {
                CompositionLocalProvider(LocalGameFeedback provides feedback) {
                    ChessTrainerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ChessTrainerApp(viewModel: ChessTrainerViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val completedLessons by viewModel.completedLessonIds.collectAsStateWithLifecycle()
    val puzzleProgress by viewModel.puzzleProgressMap.collectAsStateWithLifecycle()
    val repertoireLines by viewModel.repertoireLines.collectAsStateWithLifecycle()
    val playedGames by viewModel.playedGames.collectAsStateWithLifecycle()
    val sessions by viewModel.activitySessions.collectAsStateWithLifecycle()
    val pendingPgn by viewModel.pendingAnalysisPgn.collectAsStateWithLifecycle()
    val gamificationStats by viewModel.gamificationStats.collectAsStateWithLifecycle()
    val playProfile by viewModel.playProfile.collectAsStateWithLifecycle()
    val reviewData by viewModel.review.collectAsStateWithLifecycle()

    val palette = LocalGlassPalette.current
    val tabs = listOf(
        BottomTab(ROUTE_HOME, "Accueil", "Home", Icons.Default.Home),
        BottomTab(ROUTE_LEARN, "Apprendre", "Learn", Icons.AutoMirrored.Filled.MenuBook),
        BottomTab(ROUTE_PLAY_TAB, "Jouer", "Play", Icons.Default.PlayArrow),
        BottomTab(ROUTE_PROGRESS_TAB, "Progrès", "Progress", Icons.AutoMirrored.Filled.TrendingUp)
    )
    val showBottomBar = currentRoute in tabs.map { it.route }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    GlassTabBar(
                        tabs = tabs,
                        currentRoute = currentRoute,
                        onSelect = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                    enterTransition = {
                        fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) { it / 9 } +
                            scaleIn(tween(420, easing = FastOutSlowInEasing), initialScale = 0.96f)
                    },
                    exitTransition = {
                        fadeOut(tween(220)) +
                            slideOutHorizontally(tween(420, easing = FastOutSlowInEasing)) { -it / 12 } +
                            scaleOut(tween(420, easing = FastOutSlowInEasing), targetScale = 0.97f)
                    },
                    popEnterTransition = {
                        fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) { -it / 9 } +
                            scaleIn(tween(420, easing = FastOutSlowInEasing), initialScale = 0.96f)
                    },
                    popExitTransition = {
                        fadeOut(tween(220)) +
                            slideOutHorizontally(tween(420, easing = FastOutSlowInEasing)) { it / 12 } +
                            scaleOut(tween(420, easing = FastOutSlowInEasing), targetScale = 0.97f)
                    }
                ) {
                    composable(ROUTE_HOME) {
                        HomeScreen(
                            completedLessonsCount = completedLessons.size,
                            totalLessonsCount = viewModel.lessons.size,
                            solvedPuzzlesCount = puzzleProgress.values.count { it.solved },
                            totalPuzzlesCount = viewModel.puzzles.size,
                            stats = gamificationStats,
                            playProfile = playProfile,
                            onClaimQuest = { viewModel.claimDailyQuest(it) },
                            onNavigate = { route -> navController.navigate(route) },
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                        )
                    }
                    composable(ROUTE_LEARN) {
                        LearnHubScreen(
                            completedLessonsCount = completedLessons.size,
                            totalLessonsCount = viewModel.lessons.size,
                            onNavigate = { route -> navController.navigate(route) },
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                        )
                    }
                    composable(ROUTE_PLAY_TAB) {
                        PlayHubScreen(
                            profile = playProfile,
                            playedGames = playedGames,
                            showBackButton = false,
                            onStartGame = { botId, color, minutes ->
                                navController.navigate("game/$botId/$color/$minutes")
                            },
                            onSetStartingElo = { viewModel.setStartingElo(it) },
                            onAnalyzeGame = { pgn ->
                                viewModel.queuePgnForAnalysis(pgn)
                                navController.navigate(ROUTE_ANALYSIS)
                            },
                            onDeleteGame = { viewModel.deletePlayedGame(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_PROGRESS_TAB) {
                        ProgressScreen(
                            completedLessons = completedLessons.size,
                            totalLessons = viewModel.lessons.size,
                            puzzleProgressMap = puzzleProgress,
                            totalPuzzles = viewModel.puzzles.size,
                            sessions = sessions,
                            playedGames = playedGames,
                            stats = gamificationStats,
                            showBackButton = false,
                            onBack = { navController.popBackStack() },
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                        )
                    }
                    composable(ROUTE_LESSONS) {
                        LessonsScreen(
                            lessons = viewModel.lessons,
                            completedLessonIds = completedLessons,
                            showCoordinates = settings.showCoordinates,
                            onMarkCompleted = { viewModel.markLessonCompleted(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_PUZZLES) {
                        PuzzlesScreen(
                            puzzles = viewModel.puzzles,
                            progressMap = puzzleProgress,
                            showCoordinates = settings.showCoordinates,
                            onRecordAttempt = { id, solved -> viewModel.recordPuzzleAttempt(id, solved) },
                            onRecordSession = { type, score, acc, dur ->
                                viewModel.recordSession(type, score, acc, dur)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_OPENINGS) {
                        OpeningsScreen(
                            openingBook = viewModel.openingBook,
                            classicOpenings = viewModel.classicOpenings,
                            repertoireLines = repertoireLines,
                            showCoordinates = settings.showCoordinates,
                            onSaveRepertoireLine = { name, isWhite, moves, notes ->
                                viewModel.saveRepertoireLine(name, isWhite, moves, notes)
                            },
                            onReviewRepertoireLine = { line, success ->
                                viewModel.reviewRepertoireLine(line, success)
                            },
                            onDeleteRepertoireLine = { viewModel.deleteRepertoireLine(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_MIDDLEGAME) {
                        MiddlegameScreen(
                            exercises = viewModel.middlegameExercises,
                            showCoordinates = settings.showCoordinates,
                            onCompletedExercise = {
                                viewModel.recordSession("MIDDLEGAME", 1, 100, 20)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_ENDGAMES) {
                        EndgamesScreen(
                            items = viewModel.endgameItems,
                            showCoordinates = settings.showCoordinates,
                            onCompletedEndgame = {
                                viewModel.recordSession("ENDGAME", 1, 100, 30)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_ANALYSIS) {
                        AnalysisScreen(
                            pendingPgn = pendingPgn,
                            showCoordinates = settings.showCoordinates,
                            onConsumePendingPgn = { viewModel.clearPendingAnalysisPgn() },
                            onAnalyzePgn = { viewModel.analyzePgn(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_HISTORICAL) {
                        HistoricalGamesScreen(
                            games = viewModel.historicalGames,
                            showCoordinates = settings.showCoordinates,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_COORDINATES) {
                        val best = sessions.filter { it.moduleType == "COORDINATES" }.maxOfOrNull { it.score } ?: 0
                        CoordinatesScreen(
                            bestScore = best,
                            onSaveSession = { score, acc, dur ->
                                viewModel.recordSession("COORDINATES", score, acc, dur)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_QUIZ) {
                        QuizScreen(
                            questions = viewModel.quizQuestions,
                            showCoordinates = settings.showCoordinates,
                            onQuizCompleted = { score, acc ->
                                viewModel.recordSession("QUIZ", score, acc, 60)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_PLAY_AI) {
                        PlayHubScreen(
                            profile = playProfile,
                            playedGames = playedGames,
                            showBackButton = true,
                            onStartGame = { botId, color, minutes ->
                                navController.navigate("game/$botId/$color/$minutes")
                            },
                            onSetStartingElo = { viewModel.setStartingElo(it) },
                            onAnalyzeGame = { pgn ->
                                viewModel.queuePgnForAnalysis(pgn)
                                navController.navigate(ROUTE_ANALYSIS)
                            },
                            onDeleteGame = { viewModel.deletePlayedGame(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = ROUTE_GAME,
                        arguments = listOf(
                            navArgument("botId") { type = NavType.StringType },
                            navArgument("color") { type = NavType.IntType },
                            navArgument("minutes") { type = NavType.IntType }
                        )
                    ) { entry ->
                        val botId = entry.arguments?.getString("botId") ?: BotRoster.SPARRING_ID
                        val color = entry.arguments?.getInt("color") ?: 0
                        val minutes = entry.arguments?.getInt("minutes") ?: 0
                        val persona = BotRoster.byId(botId) ?: BotRoster.sparring
                        val startElo = androidx.compose.runtime.remember { playProfile.botElo(persona) }
                        val startPlayerElo = androidx.compose.runtime.remember { playProfile.playerElo }
                        GameScreen(
                            persona = persona,
                            botElo = startElo,
                            playerElo = startPlayerElo,
                            colorChoice = color,
                            minutes = minutes,
                            showCoordinates = settings.showCoordinates,
                            onComplete = { score, rated -> viewModel.completeGame(persona, startElo, score, rated) },
                            onSaveGame = { pgn, res, w, b, elo, userWhite, moves ->
                                viewModel.savePlayedGame(pgn, res, w, b, elo, userWhite, moves)
                            },
                            onOpenReview = { data ->
                                viewModel.openReview(data)
                                navController.navigate(ROUTE_REVIEW)
                            },
                            onRematch = {
                                navController.popBackStack()
                                navController.navigate("game/$botId/$color/$minutes")
                            },
                            onExit = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_REVIEW) {
                        val data = reviewData
                        if (data != null) {
                            ReviewScreen(
                                data = data,
                                onAnalyzePgn = { pgn ->
                                    viewModel.queuePgnForAnalysis(pgn)
                                    navController.navigate(ROUTE_ANALYSIS)
                                },
                                onRematch = {
                                    navController.popBackStack(ROUTE_REVIEW, inclusive = true)
                                    navController.popBackStack()
                                    navController.navigate("game/${data.botId}/${if (data.userIsWhite) 1 else 0}/0")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(ROUTE_PROGRESS) {
                        ProgressScreen(
                            completedLessons = completedLessons.size,
                            totalLessons = viewModel.lessons.size,
                            puzzleProgressMap = puzzleProgress,
                            totalPuzzles = viewModel.puzzles.size,
                            sessions = sessions,
                            playedGames = playedGames,
                            stats = gamificationStats,
                            showBackButton = true,
                            onBack = { navController.popBackStack() },
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                        )
                    }
                    composable(ROUTE_SETTINGS) {
                        SettingsScreen(
                            settings = settings,
                            onSetThemeMode = { viewModel.setThemeMode(it) },
                            onSetLanguage = { viewModel.setLanguage(it) },
                            onSetTextScale = { viewModel.setTextScale(it) },
                            onSetShowCoordinates = { viewModel.setShowCoordinates(it) },
                            onSetHaptics = { viewModel.setHapticsEnabled(it) },
                            onSetSound = { viewModel.setSoundEnabled(it) },
                            onImportPgnPack = { pgn ->
                                viewModel.queuePgnForAnalysis(pgn)
                                navController.navigate(ROUTE_ANALYSIS)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

/** Barre d'onglets flottante en verre : l'onglet actif a une pastille qui se gonfle en ressort. */
@Composable
private fun GlassTabBar(
    tabs: List<BottomTab>,
    currentRoute: String,
    onSelect: (String) -> Unit
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    for (i in 1..4) {
                        val grow = i * 3.dp.toPx()
                        drawRoundRect(
                            color = palette.inkShadow.copy(alpha = palette.inkShadow.alpha * (0.6f / i)),
                            topLeft = Offset(-grow * 0.3f, grow * 0.5f),
                            size = androidx.compose.ui.geometry.Size(size.width + grow * 0.6f, size.height + grow * 0.5f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30.dp.toPx() + grow)
                        )
                    }
                }
                .clip(shape)
                .background(palette.bgTop.copy(alpha = if (palette.isDark) 0.72f else 0.62f))
                .background(glassFillBrush(strong = true))
                .border(1.dp, glassBorderBrush(strong = true), shape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val weight by animateFloatAsState(
                    targetValue = if (selected) 1.55f else 1f,
                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
                    label = "tab_w"
                )
                val pill by animateColorAsState(
                    targetValue = if (selected) palette.textPrimary else Color.Transparent,
                    animationSpec = tween(260),
                    label = "tab_pill"
                )
                val fg by animateColorAsState(
                    targetValue = if (selected) palette.bgTop else palette.textSecondary,
                    animationSpec = tween(260),
                    label = "tab_fg"
                )
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(pill)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onSelect(tab.route) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tr(tab.labelFr, tab.labelEn),
                            tint = fg,
                            modifier = Modifier.size(22.dp)
                        )
                        AnimatedVisibility(visible = selected) {
                            Text(
                                text = tr(tab.labelFr, tab.labelEn),
                                style = MaterialTheme.typography.labelMedium,
                                color = fg,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
