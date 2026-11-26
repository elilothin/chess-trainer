package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.SettingsDataStore
import com.example.data.local.TrainingDatabase
import com.example.data.repository.ChessTrainingRepository
import com.example.ui.ChessTrainerViewModel
import com.example.ui.components.GlassBackground
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
import com.example.ui.screens.PlayAiScreen
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

            EchecsCoachTheme(
                themeMode = settings.themeMode,
                language = settings.language,
                textScale = settings.textScale
            ) {
                ChessTrainerApp(viewModel = viewModel)
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
                    NavigationBar(
                        containerColor = palette.bgTop,
                        windowInsets = WindowInsets.navigationBars,
                        tonalElevation = 0.dp,
                        modifier = Modifier.border(
                            width = 1.2.dp,
                            color = palette.glassBorderStart
                        )
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tr(tab.labelFr, tab.labelEn)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tr(tab.labelFr, tab.labelEn),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = palette.bgTop,
                                    selectedTextColor = palette.textPrimary,
                                    indicatorColor = palette.textPrimary,
                                    unselectedIconColor = palette.textSecondary,
                                    unselectedTextColor = palette.textSecondary
                                )
                            )
                        }
                    }
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
                    startDestination = ROUTE_HOME
                ) {
                    composable(ROUTE_HOME) {
                        HomeScreen(
                            completedLessonsCount = completedLessons.size,
                            totalLessonsCount = viewModel.lessons.size,
                            solvedPuzzlesCount = puzzleProgress.values.count { it.solved },
                            totalPuzzlesCount = viewModel.puzzles.size,
                            stats = gamificationStats,
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
                        PlayAiScreen(
                            playedGames = playedGames,
                            showCoordinates = settings.showCoordinates,
                            showBackButton = false,
                            onSaveGame = { pgn, res, w, b, elo, userWhite, moves ->
                                viewModel.savePlayedGame(pgn, res, w, b, elo, userWhite, moves)
                            },
                            onDeleteGame = { viewModel.deletePlayedGame(it) },
                            onAnalyzeGame = { pgn ->
                                viewModel.queuePgnForAnalysis(pgn)
                                navController.navigate(ROUTE_ANALYSIS)
                            },
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
                        PlayAiScreen(
                            playedGames = playedGames,
                            showCoordinates = settings.showCoordinates,
                            showBackButton = true,
                            onSaveGame = { pgn, res, w, b, elo, userWhite, moves ->
                                viewModel.savePlayedGame(pgn, res, w, b, elo, userWhite, moves)
                            },
                            onDeleteGame = { viewModel.deletePlayedGame(it) },
                            onAnalyzeGame = { pgn ->
                                viewModel.queuePgnForAnalysis(pgn)
                                navController.navigate(ROUTE_ANALYSIS)
                            },
                            onBack = { navController.popBackStack() }
                        )
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
