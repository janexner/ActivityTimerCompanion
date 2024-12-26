package com.exner.tools.activitytimercompanion.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.exner.tools.activitytimercompanion.data.preferences.dataStore
import com.exner.tools.activitytimercompanion.ui.theme.Theme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("preferences")

@Singleton
class ActivityTimerCompanionUserPreferencesManager @Inject constructor(
    @ApplicationContext appContext: Context
) {

    private val userDataStorePreferences = appContext.dataStore

    fun theme(): Flow<Theme> {
        return userDataStorePreferences.data.catch {
            emit(emptyPreferences())
        }.map { preferences ->
            val wasDark = preferences[KEY_NIGHT_MODE] == true
            val default = if (wasDark) Theme.Dark.name else Theme.Auto.name
            Theme.valueOf(preferences[KEY_THEME] ?: default)
        }
    }

    suspend fun setTheme(newTheme: Theme) {
        userDataStorePreferences.edit { preferences ->
            preferences[KEY_THEME] = newTheme.name
        }
    }

    fun showSimpleDisplay(): Flow<Boolean> {
        return userDataStorePreferences.data.catch {
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[KEY_SIMPLE_DISPLAY] != false
        }
    }

    suspend fun setShowSimpleDisplay(newShowSimpleDisplay: Boolean) {
        userDataStorePreferences.edit { preferences ->
            preferences[KEY_SIMPLE_DISPLAY] = newShowSimpleDisplay
        }
    }

    private companion object {

        val KEY_NIGHT_MODE = booleanPreferencesKey(name = "preference_night_mode")
        val KEY_THEME = stringPreferencesKey(name = "preference_theme")
        val KEY_SIMPLE_DISPLAY = booleanPreferencesKey(name = "simple_display")
    }
}