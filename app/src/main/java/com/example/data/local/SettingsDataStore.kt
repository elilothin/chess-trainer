package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.bot.BotPersona
import com.example.domain.bot.PlayProfile
import com.example.domain.bot.RatingChange
import com.example.domain.bot.RatingEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode
import com.example.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "echecs_coach_settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val SHOW_COORDINATES = booleanPreferencesKey("show_coordinates")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val PIECE_STYLE = stringPreferencesKey("piece_style")
        val SOUND_PACK = stringPreferencesKey("sound_pack")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val ONLINE_DB_URL = stringPreferencesKey("online_db_url")
        val PLAY_PROFILE = stringPreferencesKey("play_profile")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: AppThemeMode.LIGHT.name
        val langStr = prefs[Keys.LANGUAGE] ?: AppLanguage.FR.name
        UserSettings(
            themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.LIGHT),
            language = runCatching { AppLanguage.valueOf(langStr) }.getOrDefault(AppLanguage.FR),
            textScale = prefs[Keys.TEXT_SCALE] ?: 1.0f,
            showCoordinates = prefs[Keys.SHOW_COORDINATES] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            boardTheme = prefs[Keys.BOARD_THEME] ?: "classic",
            pieceStyle = prefs[Keys.PIECE_STYLE] ?: "relief",
            soundPack = prefs[Keys.SOUND_PACK] ?: "wood",
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.85f
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setTextScale(scale: Float) {
        context.dataStore.edit { it[Keys.TEXT_SCALE] = scale }
    }

    suspend fun setShowCoordinates(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_COORDINATES] = show }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setBoardTheme(id: String) { context.dataStore.edit { it[Keys.BOARD_THEME] = id } }
    suspend fun setPieceStyle(id: String) { context.dataStore.edit { it[Keys.PIECE_STYLE] = id } }
    suspend fun setSoundPack(id: String) { context.dataStore.edit { it[Keys.SOUND_PACK] = id } }
    suspend fun setSoundVolume(v: Float) { context.dataStore.edit { it[Keys.SOUND_VOLUME] = v } }

    val onlineDbUrlFlow: Flow<String> = context.dataStore.data.map { it[Keys.ONLINE_DB_URL].orEmpty() }

    suspend fun setOnlineDbUrl(url: String) {
        context.dataStore.edit { it[Keys.ONLINE_DB_URL] = url }
    }

    val playProfileFlow: Flow<PlayProfile> = context.dataStore.data.map { prefs ->
        PlayProfile.decode(prefs[Keys.PLAY_PROFILE])
    }

    /** Applique le résultat d'une partie dans une seule transaction et renvoie la variation d'Elo. */
    suspend fun applyGameResult(
        persona: BotPersona,
        botEloAtStart: Int,
        playerScore: Double,
        rated: Boolean
    ): RatingChange {
        var change: RatingChange? = null
        context.dataStore.edit { prefs ->
            val current = PlayProfile.decode(prefs[Keys.PLAY_PROFILE])
            val (updated, c) = RatingEngine.apply(current, persona, botEloAtStart, playerScore, rated)
            prefs[Keys.PLAY_PROFILE] = updated.encode()
            change = c
        }
        return change ?: RatingChange(0, 0, botEloAtStart, botEloAtStart, false)
    }

    suspend fun setStartingElo(elo: Int) {
        context.dataStore.edit { prefs ->
            val current = PlayProfile.decode(prefs[Keys.PLAY_PROFILE])
            prefs[Keys.PLAY_PROFILE] = current.copy(playerElo = elo, peakElo = maxOf(current.peakElo, elo)).encode()
        }
    }
}
