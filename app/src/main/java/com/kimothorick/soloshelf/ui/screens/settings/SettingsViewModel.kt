package com.kimothorick.soloshelf.ui.screens.settings

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.LibraryFolder
import com.kimothorick.soloshelf.data.preferences.AppTheme
import com.kimothorick.soloshelf.data.preferences.ColorTheme
import com.kimothorick.soloshelf.data.preferences.ThemeMode
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.LibraryScanner
import com.materialkolor.PaletteStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookRepository: BookRepository,
    private val libraryScanner: LibraryScanner,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val themePreference: StateFlow<AppTheme> = userPreferencesRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.AUTO
        )

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.MATERIAL_YOU
        )

    val colorTheme: StateFlow<ColorTheme> = userPreferencesRepository.colorTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ColorTheme.MONO
        )

    val amoledBlack: StateFlow<Boolean> = userPreferencesRepository.amoledBlack
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val autoScanOnStartup: StateFlow<Boolean> = userPreferencesRepository.autoScanOnStartup
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val libraryFolders: StateFlow<List<LibraryFolder>> = userPreferencesRepository.libraryFolders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _cacheSize = MutableStateFlow("0kb")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    init {
        refreshCacheSize()
    }

    val customSeedColor: StateFlow<Color> = userPreferencesRepository.customSeedColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Color.Blue
        )

    val paletteStyle: StateFlow<PaletteStyle> = userPreferencesRepository.paletteStyle
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaletteStyle.TonalSpot
        )

    @Deprecated("Use themeMode instead")
    val dynamicColor: StateFlow<Boolean> = userPreferencesRepository.dynamicColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemePreference(theme)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemeMode(mode)
        }
    }

    fun setColorTheme(theme: ColorTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateColorTheme(theme)
        }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAmoledBlack(enabled)
        }
    }

    fun setAutoScanOnStartup(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoScanOnStartup(enabled)
        }
    }

    fun addLibraryFolder(uri: String, name: String, dateAdded: String) {
        viewModelScope.launch {
            val current = libraryFolders.value
            if (current.none { it.uri == uri }) {
                userPreferencesRepository.updateLibraryFolders(current + LibraryFolder(uri, name, dateAdded))
            }
            libraryScanner.scanAndImportFolder(Uri.parse(uri))
        }
    }

    fun removeLibraryFolder(uri: String) {
        viewModelScope.launch {
            val currentFolders = libraryFolders.value
            userPreferencesRepository.updateLibraryFolders(currentFolders.filter { it.uri != uri })

            try {
                val allBooks = bookRepository.getAllBooksOnce()
                allBooks.forEach { book ->
                    if (belongsToFolder(book.filePath, uri)) {
                        bookRepository.deleteBook(book)
                        Log.d("SettingsViewModel", "Deleted book ${book.title} because its folder was removed: $uri")
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to delete books for folder $uri", e)
            }
        }
    }

    private fun belongsToFolder(bookFilePath: String, folderUriString: String): Boolean {
        try {
            val bookUri = Uri.parse(bookFilePath)
            val folderUri = Uri.parse(folderUriString)

            val folderTreeId = try { DocumentsContract.getTreeDocumentId(folderUri) } catch (_: Exception) { "" }
            val bookDocId = try { DocumentsContract.getDocumentId(bookUri) } catch (_: Exception) { "" }

            if (folderTreeId.isNotEmpty() && bookDocId.isNotEmpty()) {
                if (bookDocId.startsWith(folderTreeId)) {
                    return true
                }
            }

            val decodedBook = Uri.decode(bookFilePath)
            val decodedFolder = Uri.decode(folderUriString)
            return decodedBook.contains(decodedFolder)
        } catch (_: Exception) {
            return bookFilePath.contains(folderUriString)
        }
    }

    fun refreshCacheSize() {
        viewModelScope.launch {
            _cacheSize.value = computeCacheSize()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                deleteRecursive(cacheDir)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to clear cache", e)
            }
            refreshCacheSize()
        }
    }

    private fun computeCacheSize(): String {
        val cacheDir = context.cacheDir ?: return "0kb"
        val bytes = getDirectorySize(cacheDir)
        return formatBytes(bytes)
    }

    private fun getDirectorySize(file: File): Long {
        if (file.isDirectory) {
            val files = file.listFiles() ?: return 0
            return files.sumOf { getDirectorySize(it) }
        } else {
            return file.length()
        }
    }

    private fun deleteRecursive(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursive(it) }
        }
        if (file != context.cacheDir) {
            file.delete()
        }
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb < 1.0) {
            val kb = bytes / 1024.0
            "${kb.toInt()}kb"
        } else {
            String.format(Locale.getDefault(), "%.1fmb", mb)
        }
    }

    fun setCustomSeedColor(color: Color) {
        viewModelScope.launch {
            userPreferencesRepository.updateCustomSeedColor(color)
        }
    }

    fun setPaletteStyle(style: PaletteStyle) {
        viewModelScope.launch {
            userPreferencesRepository.updatePaletteStyle(style)
        }
    }

    @Deprecated("Use setThemeMode instead")
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDynamicColor(enabled)
        }
    }
}
