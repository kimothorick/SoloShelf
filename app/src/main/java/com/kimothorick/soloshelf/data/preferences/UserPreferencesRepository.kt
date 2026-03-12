package com.kimothorick.soloshelf.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kimothorick.soloshelf.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

enum class AppTheme {
    AUTO, LIGHT, DARK
}

enum class SortOrder {
    TITLE_ASC, TITLE_DESC,
    AUTHOR_ASC, AUTHOR_DESC,
    DATE_ADDED_ASC, DATE_ADDED_DESC,
    LAST_READ_ASC, LAST_READ_DESC
}

class UserPreferencesRepository(
    private val context: Context,
) {
    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    private val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    private val SORT_ORDER = stringPreferencesKey("sort_order")

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map {
        it[IS_FIRST_LAUNCH] ?: true
    }

    val themePreference: Flow<AppTheme> = context.dataStore.data.map {
        val themeName = it[THEME_PREFERENCE] ?: AppTheme.AUTO.name
        try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            Log.e("UserPreferences", "Invalid theme preference: $themeName", e)
            AppTheme.AUTO
        }
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map {
        it[DYNAMIC_COLOR] ?: true
    }

    val sortOrder: Flow<SortOrder> = context.dataStore.data.map {
        val sortOrderName = it[SORT_ORDER] ?: SortOrder.LAST_READ_DESC.name
        try {
            SortOrder.valueOf(sortOrderName)
        } catch (e: IllegalArgumentException) {
            Log.e("UserPreferences", "Invalid sort order: $sortOrderName", e)
            SortOrder.LAST_READ_DESC
        }
    }

    suspend fun updateFirstLaunch() {
        context.dataStore.edit {
            it[IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun updateThemePreference(theme: AppTheme) {
        context.dataStore.edit {
            it[THEME_PREFERENCE] = theme.name
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit {
            it[DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        context.dataStore.edit {
            it[SORT_ORDER] = sortOrder.name
        }
    }
}
