package com.kimothorick.soloshelf.data.preferences

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kimothorick.soloshelf.data.models.LibraryFolder
import com.kimothorick.soloshelf.util.Constants
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

enum class AppTheme {
    AUTO,
    LIGHT,
    DARK,
}

enum class ThemeMode {
    MATERIAL_YOU,
    CUSTOM,
}

enum class ColorTheme {
    MONO,
    CYAN_TEAL,
    TERRACOTTA,
    IRIS,
    OATMEAL,
    CRIMSON,
    MINT,
    AMBER,
    PLUM,
    WALNUT,
    CUSTOM,
}

enum class SortOrder {
    TITLE_ASC,
    TITLE_DESC,
    AUTHOR_ASC,
    AUTHOR_DESC,
    DATE_ADDED_ASC,
    DATE_ADDED_DESC,
    LAST_READ_ASC,
    LAST_READ_DESC,
}

class UserPreferencesRepository(
    private val context: Context,
) {
    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    private val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val COLOR_THEME = stringPreferencesKey("color_theme")
    private val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
    private val AUTO_SCAN_ON_STARTUP = booleanPreferencesKey("auto_scan_on_startup")
    private val CUSTOM_SEED_COLOR = intPreferencesKey("custom_seed_color")
    private val PALETTE_STYLE = stringPreferencesKey("palette_style")
    private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    private val SORT_ORDER = stringPreferencesKey("sort_order")
    private val LIBRARY_FOLDERS = stringPreferencesKey("library_folders")

    private val READER_THEME = stringPreferencesKey("reader_theme")
    private val READER_FONT_FAMILY = stringPreferencesKey("reader_font_family")
    private val READER_FONT_SIZE = intPreferencesKey("reader_font_size")
    private val READER_LINE_HEIGHT = intPreferencesKey("reader_line_height")
    private val READER_PAGE_MARGINS = intPreferencesKey("reader_page_margins")
    private val READER_BRIGHTNESS = intPreferencesKey("reader_brightness")

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

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        val modeName = it[THEME_MODE] ?: ThemeMode.MATERIAL_YOU.name
        try {
            ThemeMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            Log.e("UserPreferences", "Invalid theme mode: $modeName", e)
            ThemeMode.MATERIAL_YOU
        }
    }

    val colorTheme: Flow<ColorTheme> = context.dataStore.data.map {
        val themeName = it[COLOR_THEME] ?: ColorTheme.MONO.name
        try {
            ColorTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            Log.e("UserPreferences", "Invalid color theme: $themeName", e)
            ColorTheme.MONO
        }
    }

    val amoledBlack: Flow<Boolean> = context.dataStore.data.map {
        it[AMOLED_BLACK] ?: false
    }

    val autoScanOnStartup: Flow<Boolean> = context.dataStore.data.map {
        it[AUTO_SCAN_ON_STARTUP] ?: true
    }

    val libraryFolders: Flow<List<LibraryFolder>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[LIBRARY_FOLDERS]
        if (jsonString.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Json.decodeFromString<List<LibraryFolder>>(jsonString)
            } catch (e: Exception) {
                Log.e("UserPreferences", "Failed to parse library folders", e)
                emptyList()
            }
        }
    }

    val customSeedColor: Flow<Color> = context.dataStore.data.map {
        val colorInt = it[CUSTOM_SEED_COLOR] ?: Color.Blue.toArgb()
        Color(colorInt)
    }

    val paletteStyle: Flow<PaletteStyle> = context.dataStore.data.map {
        val styleName = it[PALETTE_STYLE] ?: PaletteStyle.TonalSpot.name
        try {
            PaletteStyle.valueOf(styleName)
        } catch (e: IllegalArgumentException) {
            Log.e("UserPreferences", "Invalid palette style: $styleName", e)
            PaletteStyle.TonalSpot
        }
    }

    @Deprecated("Use themeMode instead", ReplaceWith("themeMode"))
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

    val readerTheme: Flow<String> = context.dataStore.data.map { it[READER_THEME] ?: "light" }
    val readerFontFamily: Flow<String?> = context.dataStore.data.map { it[READER_FONT_FAMILY] }
    val readerFontSize: Flow<Int> = context.dataStore.data.map { it[READER_FONT_SIZE] ?: 100 }
    val readerLineHeight: Flow<Int> = context.dataStore.data.map { it[READER_LINE_HEIGHT] ?: 100 }
    val readerPageMargins: Flow<Int> = context.dataStore.data.map { it[READER_PAGE_MARGINS] ?: 100 }
    val readerBrightness: Flow<Int?> = context.dataStore.data.map { it[READER_BRIGHTNESS] }

    suspend fun updateFirstLaunch() {
        context.dataStore.edit {
            it[IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun updateThemePreference(
        theme: AppTheme,
    ) {
        context.dataStore.edit {
            it[THEME_PREFERENCE] = theme.name
        }
    }

    suspend fun updateThemeMode(
        mode: ThemeMode,
    ) {
        context.dataStore.edit {
            it[THEME_MODE] = mode.name
        }
    }

    suspend fun updateColorTheme(
        theme: ColorTheme,
    ) {
        context.dataStore.edit {
            it[COLOR_THEME] = theme.name
        }
    }

    suspend fun updateAmoledBlack(
        enabled: Boolean,
    ) {
        context.dataStore.edit {
            it[AMOLED_BLACK] = enabled
        }
    }

    suspend fun updateAutoScanOnStartup(
        enabled: Boolean,
    ) {
        context.dataStore.edit {
            it[AUTO_SCAN_ON_STARTUP] = enabled
        }
    }

    suspend fun updateLibraryFolders(
        folders: List<LibraryFolder>,
    ) {
        context.dataStore.edit { preferences ->
            preferences[LIBRARY_FOLDERS] = Json.encodeToString(folders)
        }
    }

    suspend fun updateCustomSeedColor(
        color: Color,
    ) {
        context.dataStore.edit {
            it[CUSTOM_SEED_COLOR] = color.toArgb()
        }
    }

    suspend fun updatePaletteStyle(
        style: PaletteStyle,
    ) {
        context.dataStore.edit {
            it[PALETTE_STYLE] = style.name
        }
    }

    @Deprecated("Use updateThemeMode instead", ReplaceWith("updateThemeMode"))
    suspend fun updateDynamicColor(
        enabled: Boolean,
    ) {
        context.dataStore.edit {
            it[DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateSortOrder(
        sortOrder: SortOrder,
    ) {
        context.dataStore.edit {
            it[SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateReaderTheme(
        theme: String,
    ) {
        context.dataStore.edit { it[READER_THEME] = theme }
    }

    suspend fun updateReaderFontFamily(
        fontFamily: String?,
    ) {
        context.dataStore.edit {
            if (fontFamily == null) {
                it.remove(READER_FONT_FAMILY)
            } else {
                it[READER_FONT_FAMILY] = fontFamily
            }
        }
    }

    suspend fun updateReaderFontSize(
        size: Int,
    ) {
        context.dataStore.edit { it[READER_FONT_SIZE] = size }
    }

    suspend fun updateReaderLineHeight(
        height: Int,
    ) {
        context.dataStore.edit { it[READER_LINE_HEIGHT] = height }
    }

    suspend fun updateReaderPageMargins(
        margins: Int,
    ) {
        context.dataStore.edit { it[READER_PAGE_MARGINS] = margins }
    }

    suspend fun updateReaderBrightness(
        brightness: Int?,
    ) {
        context.dataStore.edit {
            if (brightness == null) {
                it.remove(READER_BRIGHTNESS)
            } else {
                it[READER_BRIGHTNESS] = brightness
            }
        }
    }
}
