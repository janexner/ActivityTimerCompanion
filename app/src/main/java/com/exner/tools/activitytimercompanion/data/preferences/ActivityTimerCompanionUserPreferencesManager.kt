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
            Theme.valueOf(preferences[KEY_THEME] ?: Theme.Auto.name)
        }
    }

    suspend fun setTheme(newTheme: Theme) {
        userDataStorePreferences.edit { preferences ->
            preferences[KEY_THEME] = newTheme.name
        }
    }

    fun chainToSameCategoryOnly(): Flow<Boolean> {
        return userDataStorePreferences.data.catch {
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[KEY_CHAIN_TO_SAME_CATEGORY_ONLY] == true
        }
    }

    suspend fun setChainToSameCategoryOnly(newChainToSameOnly: Boolean) {
        userDataStorePreferences.edit { preferences ->
            preferences[KEY_CHAIN_TO_SAME_CATEGORY_ONLY] = newChainToSameOnly
        }
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey(name = "preference_theme")
        val KEY_CHAIN_TO_SAME_CATEGORY_ONLY =
            booleanPreferencesKey(name = "chain_to_same_category_only")
    }
}