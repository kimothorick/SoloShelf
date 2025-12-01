package com.kimothorick.soloshelf.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.kimothorick.soloshelf.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

class UserPreferencesRepository(private val context: Context) {

    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map {
            it[IS_FIRST_LAUNCH] ?: true
        }

    suspend fun updateFirstLaunch() {
        context.dataStore.edit {
            it[IS_FIRST_LAUNCH] = false
        }
    }
}
