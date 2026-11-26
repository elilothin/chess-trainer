package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: AppThemeMode.LIGHT.name
        val langStr = prefs[Keys.LANGUAGE] ?: AppLanguage.FR.name
        UserSettings(
            themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.LIGHT),
            language = runCatching { AppLanguage.valueOf(langStr) }.getOrDefault(AppLanguage.FR),
            textScale = prefs[Keys.TEXT_SCALE] ?: 1.0f,
            showCoordinates = prefs[Keys.SHOW_COORDINATES] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true
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
}
