package com.example.domain.model

data class LocalizedText(
    val fr: String,
    val en: String
) {
    fun get(isEnglish: Boolean): String = if (isEnglish) en else fr
}

enum class LessonSection {
    PIECE_MOVEMENT,
    SPECIAL_RULES,
    BASIC_CONCEPTS,
    ALGEBRAIC_NOTATION
}

data class Lesson(
    val id: String,
    val section: LessonSection,
    val title: LocalizedText,
    val summary: LocalizedText,
    val explanation: LocalizedText,
    val initialFen: String,
    val objective: LocalizedText,
    val expectedMovesUci: List<String>, // Empty if targetSquareExercise is used
    val targetSquareToTap: String? = null, // For algebraic notation tap exercises
    val highlightSquares: List<String> = emptyList(),
    val hintText: LocalizedText
)

enum class PuzzleTheme(val fr: String, val en: String) {
    MATE_IN_1("Mat en 1", "Mate in 1"),
    MATE_IN_2("Mat en 2", "Mate in 2"),
    FORK("Fourchette", "Fork"),
    PIN("Clouage", "Pin"),
    SKEWER("Enfilade", "Skewer"),
    ATTRACTION("Attraction / Déviation", "Attraction / Deflection"),
    SACRIFICE("Sacrifice", "Sacrifice")
}

enum class PuzzleDifficulty(val labelFr: String, val labelEn: String, val minElo: Int, val maxElo: Int) {
    ALL("Tous (400-2000)", "All (400-2000)", 0, 3000),
    BEGINNER("Débutant (400-800)", "Beginner (400-800)", 400, 800),
    INTERMEDIATE("Intermédiaire (801-1200)", "Intermediate (801-1200)", 801, 1200),
    ADVANCED("Avancé (1201-1600)", "Advanced (1201-1600)", 1201, 1600),
    MASTER("Expert (1601-2000)", "Expert (1601-2000)", 1601, 2000)
}

data class TacticalPuzzle(
    val id: String,
    val initialFen: String,
    val solutionUci: List<String>, // Alternating player / opponent / player moves
    val theme: PuzzleTheme,
    val elo: Int,
    val title: LocalizedText,
    val explanation: LocalizedText
)

data class OpeningStatMove(
    val san: String,
    val uci: String,
    val gamesCount: Int,
    val whiteWinPct: Int,
    val drawPct: Int,
    val blackWinPct: Int
)

data class OpeningInfo(
    val id: String,
    val eco: String,
    val name: LocalizedText,
    val movesSan: List<String>,
    val movesUci: List<String>,
    val keyIdeas: LocalizedText,
    val trapOrTip: LocalizedText
)

enum class MiddlegameType(val fr: String, val en: String) {
    PRINCIPLE("Principe", "Principle"),
    BEST_PLAN("Trouve le meilleur plan", "Find the best plan"),
    IMPROVE_PIECE("Améliore ta pièce", "Improve your piece"),
    TYPICAL_POSITION("Position type", "Typical position")
}

data class MiddlegameExercise(
    val id: String,
    val type: MiddlegameType,
    val title: LocalizedText,
    val description: LocalizedText,
    val initialFen: String,
    val acceptedMovesUci: List<String>,
    val highlightSquares: List<String> = emptyList(),
    val explanation: LocalizedText
)

enum class EndgameCategory(val fr: String, val en: String) {
    LESSON("Leçon essentielle", "Essential Lesson"),
    TRAINER("Entraîneur vs IA", "Trainer vs AI")
}

data class EndgameModuleItem(
    val id: String,
    val category: EndgameCategory,
    val title: LocalizedText,
    val concept: LocalizedText,
    val initialFen: String,
    val expectedMovesUci: List<String> = emptyList(), // For guided lessons
    val targetResult: String = "WIN", // "WIN" or "DRAW" for live trainer vs engine
    val tip: LocalizedText
)

data class HistoricalGame(
    val id: String,
    val title: LocalizedText,
    val whitePlayer: String,
    val blackPlayer: String,
    val year: Int,
    val event: String,
    val result: String,
    val pgn: String,
    val summary: LocalizedText,
    val commentsByPly: Map<Int, LocalizedText> // 1-indexed ply -> commentary
)

enum class QuizCategory(val fr: String, val en: String) {
    ALL("Mixte", "Mixed"),
    RULES("Règles", "Rules"),
    TACTICS("Tactique", "Tactics"),
    OPENINGS("Ouvertures", "Openings"),
    ENDGAMES("Finales", "Endgames")
}

data class QuizQuestion(
    val id: String,
    val category: QuizCategory,
    val question: LocalizedText,
    val options: List<LocalizedText>,
    val correctIndex: Int,
    val explanation: LocalizedText,
    val boardFen: String? = null
)

enum class MoveQuality(val fr: String, val en: String, val symbol: String) {
    BRILLIANT("Coup brillant", "Brilliant", "!!"),
    BEST("Meilleur coup", "Best move", "!"),
    GOOD("Bon coup", "Good", ""),
    INACCURACY("Imprécision", "Inaccuracy", "?!"),
    MISTAKE("Erreur", "Mistake", "?"),
    BLUNDER("Gaffe", "Blunder", "??")
}

data class AnalyzedPly(
    val plyIndex: Int,
    val san: String,
    val uci: String,
    val fenBefore: String,
    val fenAfter: String,
    val evalCpAfter: Int, // Positive favors White
    val bestMoveUci: String?,
    val bestMoveSan: String?,
    val quality: MoveQuality,
    val cpLoss: Int
)

data class GameAnalysisResult(
    val whitePlayer: String,
    val blackPlayer: String,
    val event: String,
    val result: String,
    val plies: List<AnalyzedPly>,
    val whiteAccuracy: Int,
    val blackAccuracy: Int
)

enum class AppThemeMode {
    DARK, LIGHT, SYSTEM
}

enum class AppLanguage {
    FR, EN
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val language: AppLanguage = AppLanguage.FR,
    val textScale: Float = 1.0f,
    val showCoordinates: Boolean = true,
    val hapticsEnabled: Boolean = true
)

enum class LionMood {
    IDLE_HAPPY,
    THINKING,
    CHEERING,
    SHOCKED,
    CROWNED,
    SLEEPY
}

enum class LionRank(
    val level: Int,
    val minXp: Int,
    val nextXp: Int,
    val titleFr: String,
    val titleEn: String,
    val tagFr: String,
    val tagEn: String
) {
    CUB(1, 0, 250, "Lionceau Curieux", "Curious Cub", "RANG I", "RANK I"),
    STRATEGIST(2, 250, 750, "Lion Stratège", "Strategist Lion", "RANG II", "RANK II"),
    ROYAL(3, 750, 1500, "Lion Royal", "Royal Lion", "RANG III", "RANK III"),
    GRANDMASTER(4, 1500, 3000, "Grand Maître Lion", "Grandmaster Lion", "RANG IV", "RANK IV");

    companion object {
        fun forXp(xp: Int): LionRank = entries.lastOrNull { xp >= it.minXp } ?: CUB
    }
}

data class DailyQuestProgress(
    val id: String,
    val title: LocalizedText,
    val current: Int,
    val target: Int,
    val xpReward: Int,
    val crownsReward: Int,
    val targetRoute: String,
    val claimed: Boolean
) {
    val isReadyToClaim: Boolean get() = current >= target && !claimed
}

data class LionAchievement(
    val id: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val unlocked: Boolean,
    val progressLabel: String
)

data class SkillRadarScores(
    val openings: Float,     // 0f..1f
    val tactics: Float,      // 0f..1f
    val strategy: Float,     // 0f..1f
    val endgames: Float,     // 0f..1f
    val calculation: Float,  // 0f..1f
    val theory: Float        // 0f..1f
)

data class UserGamificationStats(
    val totalXp: Int = 0,
    val goldenCrowns: Int = 0,
    val currentStreakDays: Int = 0,
    val estimatedTacticalElo: Int = 600,
    val rank: LionRank = LionRank.CUB,
    val levelProgressFraction: Float = 0f,
    val levelProgressPercent: Int = 0,
    val xpToNextRank: Int = 250,
    val dailyQuests: List<DailyQuestProgress> = emptyList(),
    val achievements: List<LionAchievement> = emptyList(),
    val radarScores: SkillRadarScores = SkillRadarScores(0.15f, 0.15f, 0.15f, 0.15f, 0.15f, 0.15f),
    val last7DaysActivity: List<Pair<String, Int>> = emptyList()
)

