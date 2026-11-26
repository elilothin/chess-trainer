package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val completed: Boolean = true,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "puzzle_progress")
data class PuzzleProgressEntity(
    @PrimaryKey val puzzleId: String,
    val solved: Boolean,
    val attempts: Int = 1,
    val lastAttemptAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "repertoire_lines")
data class RepertoireLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorWhite: Boolean = true,
    val movesUci: String, // Space-separated UCI moves
    val notes: String = "",
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "played_games")
data class PlayedGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pgn: String,
    val result: String,
    val whiteName: String,
    val blackName: String,
    val aiElo: Int,
    val userPlaysWhite: Boolean,
    val moveCount: Int,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_sessions")
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moduleType: String, // "PUZZLE_SPRINT", "PUZZLE_STREAK", "PUZZLE", "COORDINATES", "QUIZ", "LESSON", "GAME_AI", "MIDDLEGAME", "ENDGAME", "OPENING"
    val score: Int,
    val accuracyPercent: Int,
    val durationSeconds: Int,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_quest_claims")
data class DailyQuestClaimEntity(
    @PrimaryKey val questKey: String, // e.g. "2026-11-25_quest_puzzles_3"
    val xpAwarded: Int,
    val crownsAwarded: Int,
    val claimedAt: Long = System.currentTimeMillis()
)

@Dao
interface TrainingDao {
    @Query("SELECT * FROM lesson_progress")
    fun observeLessonProgress(): Flow<List<LessonProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonProgress(entity: LessonProgressEntity)

    @Query("SELECT * FROM puzzle_progress")
    fun observePuzzleProgress(): Flow<List<PuzzleProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPuzzleProgress(entity: PuzzleProgressEntity)

    @Query("SELECT * FROM puzzle_progress WHERE puzzleId = :id LIMIT 1")
    suspend fun getPuzzleProgress(id: String): PuzzleProgressEntity?

    @Query("SELECT * FROM repertoire_lines ORDER BY nextReviewAt ASC")
    fun observeRepertoireLines(): Flow<List<RepertoireLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRepertoireLine(line: RepertoireLineEntity): Long

    @Query("DELETE FROM repertoire_lines WHERE id = :id")
    suspend fun deleteRepertoireLine(id: Long)

    @Query("SELECT * FROM played_games ORDER BY playedAt DESC")
    fun observePlayedGames(): Flow<List<PlayedGameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayedGame(game: PlayedGameEntity): Long

    @Query("DELETE FROM played_games WHERE id = :id")
    suspend fun deletePlayedGame(id: Long)

    @Query("SELECT * FROM activity_sessions ORDER BY timestampMillis ASC")
    fun observeActivitySessions(): Flow<List<ActivitySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivitySession(session: ActivitySessionEntity)

    @Query("SELECT * FROM daily_quest_claims")
    fun observeDailyClaims(): Flow<List<DailyQuestClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyClaim(claim: DailyQuestClaimEntity)
}

@Database(
    entities = [
        LessonProgressEntity::class,
        PuzzleProgressEntity::class,
        RepertoireLineEntity::class,
        PlayedGameEntity::class,
        ActivitySessionEntity::class,
        DailyQuestClaimEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao

    companion object {
        @Volatile
        private var INSTANCE: TrainingDatabase? = null

        fun getInstance(context: Context): TrainingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrainingDatabase::class.java,
                    "echecs_coach_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
