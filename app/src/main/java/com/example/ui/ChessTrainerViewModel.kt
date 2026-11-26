package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ROUTE_COORDINATES
import com.example.ROUTE_OPENINGS
import com.example.ROUTE_PUZZLES
import com.example.data.local.ActivitySessionEntity
import com.example.data.local.DailyQuestClaimEntity
import com.example.data.local.PlayedGameEntity
import com.example.data.local.PuzzleProgressEntity
import com.example.data.local.RepertoireLineEntity
import com.example.data.repository.ChessTrainingRepository
import com.example.domain.chess.ChessAi
import com.example.domain.chess.PgnParser
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode
import com.example.domain.model.DailyQuestProgress
import com.example.domain.model.EndgameModuleItem
import com.example.domain.model.GameAnalysisResult
import com.example.domain.model.HistoricalGame
import com.example.domain.model.Lesson
import com.example.domain.model.LionAchievement
import com.example.domain.model.LionRank
import com.example.domain.model.LocalizedText
import com.example.domain.model.MiddlegameExercise
import com.example.domain.model.OpeningInfo
import com.example.domain.model.QuizQuestion
import com.example.domain.model.SkillRadarScores
import com.example.domain.model.TacticalPuzzle
import com.example.domain.model.UserGamificationStats
import com.example.domain.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class ChessTrainerViewModel(
    private val repository: ChessTrainingRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureStarterRepertoire()
        }
    }

    val settings: StateFlow<UserSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val completedLessonIds: StateFlow<Set<String>> = repository.lessonProgress
        .map { list -> list.filter { it.completed }.map { it.lessonId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val puzzleProgressMap: StateFlow<Map<String, PuzzleProgressEntity>> = repository.puzzleProgress
        .map { list -> list.associateBy { it.puzzleId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val repertoireLines: StateFlow<List<RepertoireLineEntity>> = repository.repertoireLines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playedGames: StateFlow<List<PlayedGameEntity>> = repository.playedGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activitySessions: StateFlow<List<ActivitySessionEntity>> = repository.activitySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dailyClaims: StateFlow<List<DailyQuestClaimEntity>> = repository.dailyClaims
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gamificationStats: StateFlow<UserGamificationStats> = combine(
        completedLessonIds,
        puzzleProgressMap,
        repertoireLines,
        playedGames,
        activitySessions,
        dailyClaims
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val lessonsDone = arr[0] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val puzzlesMap = arr[1] as Map<String, PuzzleProgressEntity>
        @Suppress("UNCHECKED_CAST")
        val lines = arr[2] as List<RepertoireLineEntity>
        @Suppress("UNCHECKED_CAST")
        val games = arr[3] as List<PlayedGameEntity>
        @Suppress("UNCHECKED_CAST")
        val sessions = arr[4] as List<ActivitySessionEntity>
        @Suppress("UNCHECKED_CAST")
        val claims = arr[5] as List<DailyQuestClaimEntity>

        computeGamificationStats(
            lessonsDone = lessonsDone,
            puzzlesMap = puzzlesMap,
            repertoireLines = lines,
            playedGames = games,
            sessions = sessions,
            claims = claims
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGamificationStats())

    private val _pendingAnalysisPgn = MutableStateFlow<String?>(null)
    val pendingAnalysisPgn: StateFlow<String?> = _pendingAnalysisPgn.asStateFlow()

    val lessons: List<Lesson> get() = repository.getLessons()
    val puzzles: List<TacticalPuzzle> get() = repository.getPuzzles()
    val openingBook get() = repository.getOpeningBook()
    val classicOpenings: List<OpeningInfo> get() = repository.getClassicOpenings()
    val middlegameExercises: List<MiddlegameExercise> get() = repository.getMiddlegameExercises()
    val endgameItems: List<EndgameModuleItem> get() = repository.getEndgameItems()
    val historicalGames: List<HistoricalGame> get() = repository.getHistoricalGames()
    val quizQuestions: List<QuizQuestion> get() = repository.getQuizQuestions()

    fun markLessonCompleted(lessonId: String) {
        viewModelScope.launch { repository.markLessonCompleted(lessonId) }
    }

    fun recordPuzzleAttempt(puzzleId: String, solved: Boolean) {
        viewModelScope.launch { repository.recordPuzzleAttempt(puzzleId, solved) }
    }

    fun recordSession(moduleType: String, score: Int, accuracyPercent: Int, durationSeconds: Int) {
        viewModelScope.launch {
            repository.recordSession(moduleType, score, accuracyPercent, durationSeconds)
        }
    }

    fun claimDailyQuest(quest: DailyQuestProgress) {
        if (!quest.isReadyToClaim) return
        val key = "${dayKey(System.currentTimeMillis())}_${quest.id}"
        viewModelScope.launch {
            repository.claimDailyQuest(key, quest.xpReward, quest.crownsReward)
        }
    }

    fun saveRepertoireLine(name: String, colorWhite: Boolean, movesUci: String, notes: String) {
        viewModelScope.launch {
            repository.saveRepertoireLine(name, colorWhite, movesUci, notes)
        }
    }

    fun reviewRepertoireLine(line: RepertoireLineEntity, success: Boolean) {
        viewModelScope.launch {
            repository.reviewRepertoireLine(line, success)
        }
    }

    fun deleteRepertoireLine(id: Long) {
        viewModelScope.launch { repository.deleteRepertoireLine(id) }
    }

    fun savePlayedGame(
        pgn: String,
        result: String,
        whiteName: String,
        blackName: String,
        aiElo: Int,
        userPlaysWhite: Boolean,
        moveCount: Int
    ) {
        viewModelScope.launch {
            repository.savePlayedGame(pgn, result, whiteName, blackName, aiElo, userPlaysWhite, moveCount)
        }
    }

    fun deletePlayedGame(id: Long) {
        viewModelScope.launch { repository.deletePlayedGame(id) }
    }

    fun queuePgnForAnalysis(pgn: String) {
        _pendingAnalysisPgn.value = pgn
    }

    fun clearPendingAnalysisPgn() {
        _pendingAnalysisPgn.value = null
    }

    suspend fun analyzePgn(pgnText: String): Result<GameAnalysisResult> = withContext(Dispatchers.Default) {
        runCatching {
            val parsed = PgnParser.parse(pgnText)
            require(parsed.moves.isNotEmpty()) { "Aucun coup valide trouvé dans le PGN." }
            ChessAi.analyzeGame(parsed, depth = 2)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setTextScale(scale: Float) {
        viewModelScope.launch { repository.setTextScale(scale) }
    }

    fun setShowCoordinates(show: Boolean) {
        viewModelScope.launch { repository.setShowCoordinates(show) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    private fun computeGamificationStats(
        lessonsDone: Set<String>,
        puzzlesMap: Map<String, PuzzleProgressEntity>,
        repertoireLines: List<RepertoireLineEntity>,
        playedGames: List<PlayedGameEntity>,
        sessions: List<ActivitySessionEntity>,
        claims: List<DailyQuestClaimEntity>
    ): UserGamificationStats {
        val puzzleLookup = puzzles.associateBy { it.id }
        val solvedPuzzles = puzzlesMap.values.filter { it.solved }
        val lessonXp = lessonsDone.size * 50
        val puzzleXp = solvedPuzzles.sumOf { p ->
            val elo = puzzleLookup[p.puzzleId]?.elo ?: 800
            if (elo >= 1400) 40 else 25
        }
        val sessionBonusXp = sessions.sumOf { s ->
            when (s.moduleType) {
                "COORDINATES" -> 20 + (s.score / 2)
                "QUIZ" -> 25 + s.score * 5
                "MIDDLEGAME" -> 35
                "ENDGAME" -> 45
                "OPENING" -> 20
                "PUZZLE_SPRINT" -> 30 + s.score * 10
                "PUZZLE_STREAK" -> 25 + s.score * 12
                else -> 0
            }
        }
        val aiWins = playedGames.count { g ->
            (g.userPlaysWhite && g.result == "1-0") || (!g.userPlaysWhite && g.result == "0-1")
        }
        val aiDraws = playedGames.count { it.result == "1/2-1/2" }
        val gameXp = aiWins * 80 + aiDraws * 35
        val claimedXp = claims.sumOf { it.xpAwarded }
        val totalXp = lessonXp + puzzleXp + sessionBonusXp + gameXp + claimedXp

        val goldenCrowns = lessonsDone.size +
            solvedPuzzles.size +
            (aiWins * 3) +
            claims.sumOf { it.crownsAwarded }

        val rank = LionRank.forXp(totalXp)
        val span = (rank.nextXp - rank.minXp).coerceAtLeast(1)
        val frac = ((totalXp - rank.minXp).toFloat() / span).coerceIn(0f, 1f)
        val pct = (frac * 100).toInt()
        val xpToNext = (rank.nextXp - totalXp).coerceAtLeast(0)

        // Estimated tactical Elo
        val avgSolvedElo = if (solvedPuzzles.isEmpty()) 600 else {
            solvedPuzzles.map { puzzleLookup[it.puzzleId]?.elo ?: 900 }.average().toInt()
        }
        val totalAttempts = puzzlesMap.values.sumOf { it.attempts }
        val accBonus = if (totalAttempts > 0) ((solvedPuzzles.size.toFloat() / totalAttempts) * 180).toInt() else 0
        val estimatedElo = (520 + (avgSolvedElo - 500) / 2 + solvedPuzzles.size * 18 + aiWins * 35 + accBonus)
            .coerceIn(550, 2450)

        // Streak & last 7 days
        val now = System.currentTimeMillis()
        val activeDays = sessions.map { dayKey(it.timestampMillis) }.toSet()
        var streak = 0
        val cal = Calendar.getInstance()
        if (dayKey(now) in activeDays) {
            while (dayKey(cal.timeInMillis) in activeDays) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            while (dayKey(cal.timeInMillis) in activeDays) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        val dayLabels = listOf("D", "L", "M", "M", "J", "V", "S")
        val last7 = (6 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val k = dayKey(c.timeInMillis)
            val label = dayLabels[(c.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]
            val count = sessions.count { dayKey(it.timestampMillis) == k }
            label to count
        }

        // Daily quests for today
        val today = dayKey(now)
        val todaySessions = sessions.filter { dayKey(it.timestampMillis) == today }
        val todayPuzzlesSolved = todaySessions.count { it.moduleType == "PUZZLE" && it.score > 0 } +
            todaySessions.filter { it.moduleType == "PUZZLE_SPRINT" || it.moduleType == "PUZZLE_STREAK" }.sumOf { it.score }
        val todayOpenings = todaySessions.count { it.moduleType == "OPENING" }
        val todayChallenges = todaySessions.count {
            it.moduleType in setOf("COORDINATES", "QUIZ", "ENDGAME", "GAME_AI", "MIDDLEGAME")
        }
        val claimedKeys = claims.map { it.questKey }.toSet()

        val quests = listOf(
            DailyQuestProgress(
                id = "quest_puzzles_3",
                title = LocalizedText("Résoudre 3 puzzles tactiques", "Solve 3 tactical puzzles"),
                current = todayPuzzlesSolved.coerceAtMost(3),
                target = 3,
                xpReward = 75,
                crownsReward = 2,
                targetRoute = ROUTE_PUZZLES,
                claimed = "${today}_quest_puzzles_3" in claimedKeys
            ),
            DailyQuestProgress(
                id = "quest_opening_1",
                title = LocalizedText("Réviser 1 ligne d'ouverture (SM-2)", "Review 1 opening line (SM-2)"),
                current = todayOpenings.coerceAtMost(1),
                target = 1,
                xpReward = 50,
                crownsReward = 1,
                targetRoute = ROUTE_OPENINGS,
                claimed = "${today}_quest_opening_1" in claimedKeys
            ),
            DailyQuestProgress(
                id = "quest_trainer_1",
                title = LocalizedText("Compléter 1 défi (Coord / Quiz / IA)", "Complete 1 drill (Coord / Quiz / AI)"),
                current = todayChallenges.coerceAtMost(1),
                target = 1,
                xpReward = 60,
                crownsReward = 2,
                targetRoute = ROUTE_COORDINATES,
                claimed = "${today}_quest_trainer_1" in claimedKeys
            )
        )

        // 8 Thoughtful Lion Achievements
        val bestCoord = sessions.filter { it.moduleType == "COORDINATES" }.maxOfOrNull { it.score } ?: 0
        val bestQuizAcc = sessions.filter { it.moduleType == "QUIZ" }.maxOfOrNull { it.accuracyPercent } ?: 0
        val reviewedOpenings = repertoireLines.sumOf { it.repetitions }
        val middlegameCount = sessions.count { it.moduleType == "MIDDLEGAME" }
        val endgameCount = sessions.count { it.moduleType == "ENDGAME" }

        val achievements = listOf(
            LionAchievement(
                id = "ach_first_lesson",
                title = LocalizedText("Premiers Pas du Lionceau", "Cub's First Steps"),
                description = LocalizedText("Terminer 3 leçons fondamentales", "Complete 3 core lessons"),
                unlocked = lessonsDone.size >= 3,
                progressLabel = "${lessonsDone.size.coerceAtMost(3)}/3"
            ),
            LionAchievement(
                id = "ach_tactician",
                title = LocalizedText("Griffe Tactique", "Tactical Claw"),
                description = LocalizedText("Résoudre 10 puzzles tactiques", "Solve 10 tactical puzzles"),
                unlocked = solvedPuzzles.size >= 10,
                progressLabel = "${solvedPuzzles.size.coerceAtMost(10)}/10"
            ),
            LionAchievement(
                id = "ach_opening_arch",
                title = LocalizedText("Mémoire d'Éléphant", "Opening Architect"),
                description = LocalizedText("Valider 3 révisions d'ouverture SM-2", "Complete 3 SM-2 opening reviews"),
                unlocked = reviewedOpenings >= 3,
                progressLabel = "${reviewedOpenings.coerceAtMost(3)}/3"
            ),
            LionAchievement(
                id = "ach_lynx_eye",
                title = LocalizedText("Œil de Lion", "Eagle-Eyed Lion"),
                description = LocalizedText("Atteindre 15+ en Repérage Cases", "Score 15+ in Coordinate Trainer"),
                unlocked = bestCoord >= 15,
                progressLabel = "${bestCoord.coerceAtMost(15)}/15"
            ),
            LionAchievement(
                id = "ach_ai_slayer",
                title = LocalizedText("Roi de la Savane", "King of the Savanna"),
                description = LocalizedText("Battre l'IA locale en partie", "Defeat the local AI in a match"),
                unlocked = aiWins >= 1,
                progressLabel = "${aiWins.coerceAtMost(1)}/1"
            ),
            LionAchievement(
                id = "ach_endgame_tech",
                title = LocalizedText("Chirurgien des Finales", "Endgame Surgeon"),
                description = LocalizedText("Compléter 2 finales guidées ou vs IA", "Complete 2 endgame lessons/drills"),
                unlocked = endgameCount >= 2,
                progressLabel = "${endgameCount.coerceAtMost(2)}/2"
            ),
            LionAchievement(
                id = "ach_quiz_scholar",
                title = LocalizedText("Érudit de Bauhaus", "Chess Scholar"),
                description = LocalizedText("Obtenir 80%+ sur un Quiz", "Score 80%+ on a Chess Quiz"),
                unlocked = bestQuizAcc >= 80,
                progressLabel = "${bestQuizAcc.coerceAtMost(80)}/80%"
            ),
            LionAchievement(
                id = "ach_streak_3",
                title = LocalizedText("Flamme Constante", "Steady Flame"),
                description = LocalizedText("Maintenir une série de 3 jours", "Keep a 3-day training streak"),
                unlocked = streak >= 3,
                progressLabel = "${streak.coerceAtMost(3)}/3"
            )
        )

        // 6-Axis Skill Radar
        val radar = SkillRadarScores(
            openings = (0.15f + (reviewedOpenings * 0.14f)).coerceIn(0.15f, 1f),
            tactics = (0.15f + (solvedPuzzles.size.toFloat() / puzzles.size.coerceAtLeast(1)) * 0.85f).coerceIn(0.15f, 1f),
            strategy = (0.15f + (middlegameCount * 0.12f) + (aiWins * 0.15f)).coerceIn(0.15f, 1f),
            endgames = (0.15f + (endgameCount * 0.16f)).coerceIn(0.15f, 1f),
            calculation = (0.15f + (bestCoord / 25f) * 0.75f).coerceIn(0.15f, 1f),
            theory = (0.15f + (lessonsDone.size.toFloat() / lessons.size.coerceAtLeast(1)) * 0.55f + (bestQuizAcc / 100f) * 0.30f).coerceIn(0.15f, 1f)
        )

        return UserGamificationStats(
            totalXp = totalXp,
            goldenCrowns = goldenCrowns,
            currentStreakDays = streak,
            estimatedTacticalElo = estimatedElo,
            rank = rank,
            levelProgressFraction = frac,
            levelProgressPercent = pct,
            xpToNextRank = xpToNext,
            dailyQuests = quests,
            achievements = achievements,
            radarScores = radar,
            last7DaysActivity = last7
        )
    }

    private fun dayKey(millis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        )
    }
}
