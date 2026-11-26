package com.example.data.repository

import com.example.data.content.EndgamesHistoryQuizContent
import com.example.data.content.LessonsContent
import com.example.data.content.OpeningsAndMiddlegameContent
import com.example.data.content.PuzzlesContent
import com.example.data.local.ActivitySessionEntity
import com.example.data.local.DailyQuestClaimEntity
import com.example.data.local.LessonProgressEntity
import com.example.data.local.PlayedGameEntity
import com.example.data.local.PuzzleProgressEntity
import com.example.data.local.RepertoireLineEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.TrainingDao
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode
import com.example.domain.model.EndgameModuleItem
import com.example.domain.model.HistoricalGame
import com.example.domain.model.Lesson
import com.example.domain.model.MiddlegameExercise
import com.example.domain.model.OpeningInfo
import com.example.domain.model.QuizQuestion
import com.example.domain.model.TacticalPuzzle
import com.example.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class ChessTrainingRepository(
    private val dao: TrainingDao,
    private val settingsStore: SettingsDataStore
) {
    val settings: Flow<UserSettings> = settingsStore.settingsFlow
    val lessonProgress: Flow<List<LessonProgressEntity>> = dao.observeLessonProgress()
    val puzzleProgress: Flow<List<PuzzleProgressEntity>> = dao.observePuzzleProgress()
    val repertoireLines: Flow<List<RepertoireLineEntity>> = dao.observeRepertoireLines()
    val playedGames: Flow<List<PlayedGameEntity>> = dao.observePlayedGames()
    val activitySessions: Flow<List<ActivitySessionEntity>> = dao.observeActivitySessions()
    val dailyClaims: Flow<List<DailyQuestClaimEntity>> = dao.observeDailyClaims()

    fun getLessons(): List<Lesson> = LessonsContent.lessons
    fun getPuzzles(): List<TacticalPuzzle> = PuzzlesContent.puzzles
    fun getOpeningBook() = OpeningsAndMiddlegameContent.openingBook
    fun getClassicOpenings(): List<OpeningInfo> = OpeningsAndMiddlegameContent.classicOpenings
    fun getMiddlegameExercises(): List<MiddlegameExercise> = OpeningsAndMiddlegameContent.middlegameExercises
    fun getEndgameItems(): List<EndgameModuleItem> = EndgamesHistoryQuizContent.endgameItems
    fun getHistoricalGames(): List<HistoricalGame> = EndgamesHistoryQuizContent.historicalGames
    fun getQuizQuestions(): List<QuizQuestion> = EndgamesHistoryQuizContent.quizQuestions

    suspend fun ensureStarterRepertoire() {
        val current = dao.observeRepertoireLines().first()
        if (current.isEmpty()) {
            dao.upsertRepertoireLine(
                RepertoireLineEntity(
                    name = "Italienne (Giuoco Piano)",
                    colorWhite = true,
                    movesUci = "e2e4 e7e5 g1f3 b8c6 f1c4 f8c5",
                    notes = "Pression sur f7 et développement rapide des pièces légères."
                )
            )
            dao.upsertRepertoireLine(
                RepertoireLineEntity(
                    name = "Gambit Dame Refusé (Noirs)",
                    colorWhite = false,
                    movesUci = "d2d4 d7d5 c2c4 e7e6",
                    notes = "Défense classique solide avec ...e6 avant ...Cf6."
                )
            )
            dao.upsertRepertoireLine(
                RepertoireLineEntity(
                    name = "Système de Londres (Blancs)",
                    colorWhite = true,
                    movesUci = "d2d4 d7d5 c1f4 g8f6 e2e3",
                    notes = "Pyramide solide c3-d4-e3 et contrôle de e5."
                )
            )
        }
    }

    suspend fun markLessonCompleted(lessonId: String) {
        dao.upsertLessonProgress(LessonProgressEntity(lessonId = lessonId, completed = true))
        dao.insertActivitySession(
            ActivitySessionEntity(
                moduleType = "LESSON",
                score = 1,
                accuracyPercent = 100,
                durationSeconds = 30
            )
        )
    }

    suspend fun recordPuzzleAttempt(puzzleId: String, solved: Boolean) {
        val existing = dao.getPuzzleProgress(puzzleId)
        val attempts = (existing?.attempts ?: 0) + 1
        val finalSolved = (existing?.solved == true) || solved
        dao.upsertPuzzleProgress(
            PuzzleProgressEntity(
                puzzleId = puzzleId,
                solved = finalSolved,
                attempts = attempts
            )
        )
        dao.insertActivitySession(
            ActivitySessionEntity(
                moduleType = "PUZZLE",
                score = if (solved) 1 else 0,
                accuracyPercent = if (solved) 100 else 0,
                durationSeconds = 15
            )
        )
    }

    suspend fun recordSession(moduleType: String, score: Int, accuracyPercent: Int, durationSeconds: Int) {
        dao.insertActivitySession(
            ActivitySessionEntity(
                moduleType = moduleType,
                score = score,
                accuracyPercent = accuracyPercent.coerceIn(0, 100),
                durationSeconds = durationSeconds
            )
        )
    }

    suspend fun saveRepertoireLine(name: String, colorWhite: Boolean, movesUci: String, notes: String) {
        dao.upsertRepertoireLine(
            RepertoireLineEntity(
                name = name.ifBlank { "Ligne personnalisée" },
                colorWhite = colorWhite,
                movesUci = movesUci,
                notes = notes
            )
        )
    }

    suspend fun reviewRepertoireLine(line: RepertoireLineEntity, success: Boolean) {
        // SM-2 inspired spaced repetition interval update
        val nextReps = if (success) line.repetitions + 1 else 0
        val nextEase = (line.easeFactor + if (success) 0.1f else -0.25f).coerceIn(1.3f, 2.8f)
        val nextInterval = when {
            !success -> 1
            nextReps == 1 -> 1
            nextReps == 2 -> 3
            else -> (line.intervalDays * nextEase).roundToInt().coerceAtLeast(3)
        }
        val nextReviewAt = System.currentTimeMillis() + nextInterval * 24L * 60L * 60L * 1000L
        dao.upsertRepertoireLine(
            line.copy(
                repetitions = nextReps,
                easeFactor = nextEase,
                intervalDays = nextInterval,
                nextReviewAt = nextReviewAt
            )
        )
        dao.insertActivitySession(
            ActivitySessionEntity(
                moduleType = "OPENING",
                score = if (success) 1 else 0,
                accuracyPercent = if (success) 100 else 40,
                durationSeconds = 25
            )
        )
    }

    suspend fun claimDailyQuest(questKey: String, xpAwarded: Int, crownsAwarded: Int) {
        dao.insertDailyClaim(
            DailyQuestClaimEntity(
                questKey = questKey,
                xpAwarded = xpAwarded,
                crownsAwarded = crownsAwarded
            )
        )
    }

    suspend fun deleteRepertoireLine(id: Long) = dao.deleteRepertoireLine(id)

    suspend fun savePlayedGame(
        pgn: String,
        result: String,
        whiteName: String,
        blackName: String,
        aiElo: Int,
        userPlaysWhite: Boolean,
        moveCount: Int
    ): Long {
        val id = dao.insertPlayedGame(
            PlayedGameEntity(
                pgn = pgn,
                result = result,
                whiteName = whiteName,
                blackName = blackName,
                aiElo = aiElo,
                userPlaysWhite = userPlaysWhite,
                moveCount = moveCount
            )
        )
        val won = (userPlaysWhite && result == "1-0") || (!userPlaysWhite && result == "0-1")
        val score = if (won) 100 else if (result == "1/2-1/2") 50 else 10
        dao.insertActivitySession(
            ActivitySessionEntity(
                moduleType = "GAME_AI",
                score = score,
                accuracyPercent = if (won) 100 else if (result == "1/2-1/2") 60 else 30,
                durationSeconds = moveCount * 5
            )
        )
        return id
    }

    suspend fun deletePlayedGame(id: Long) = dao.deletePlayedGame(id)

    suspend fun setThemeMode(mode: AppThemeMode) = settingsStore.setThemeMode(mode)
    suspend fun setLanguage(language: AppLanguage) = settingsStore.setLanguage(language)
    suspend fun setTextScale(scale: Float) = settingsStore.setTextScale(scale)
    suspend fun setShowCoordinates(show: Boolean) = settingsStore.setShowCoordinates(show)
    suspend fun setHapticsEnabled(enabled: Boolean) = settingsStore.setHapticsEnabled(enabled)
}
