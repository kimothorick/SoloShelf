package com.kimothorick.soloshelf.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.LibraryFolder
import com.kimothorick.soloshelf.data.models.Shelf
import com.kimothorick.soloshelf.data.preferences.SortOrder
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.ImportState
import com.kimothorick.soloshelf.domain.LibraryScanner
import com.kimothorick.soloshelf.domain.LibraryScannerEvent
import com.kimothorick.soloshelf.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class MainUiEvent {
    data class ShowToast(
        val message: UiText,
    ) : MainUiEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookRepository: BookRepository,
    private val libraryScanner: LibraryScanner,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val sortOrder: StateFlow<SortOrder> = userPreferencesRepository.sortOrder.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SortOrder.LAST_READ_DESC,
    )

    val importState: StateFlow<ImportState> = libraryScanner.importState

    init {
        checkAndAutoScan()
        observeLibraryScannerEvents()
    }

    private var hasScannedOnStartup = false

    private fun observeLibraryScannerEvents() {
        viewModelScope.launch {
            libraryScanner.uiEvent.collect { event ->
                when (event) {
                    is LibraryScannerEvent.ShowToast -> {
                        _uiEvent.send(MainUiEvent.ShowToast(event.message))
                    }
                }
            }
        }
    }

    private fun checkAndAutoScan() {
        if (hasScannedOnStartup) return
        hasScannedOnStartup = true

        viewModelScope.launch {
            try {
                val autoScan = userPreferencesRepository.autoScanOnStartup.first()
                if (autoScan) {
                    val folders = userPreferencesRepository.libraryFolders.first()
                    folders.forEach { folder ->
                        try {
                            val uri = Uri.parse(folder.uri)
                            libraryScanner.scanAndImportFolder(uri, showProgress = false)
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "Failed to auto-scan folder ${folder.uri}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error during startup auto-scan", e)
            }
        }
    }

    private val _uiEvent = Channel<MainUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun updateSortOrder(
        sortOrder: SortOrder,
    ) {
        viewModelScope.launch {
            userPreferencesRepository.updateSortOrder(sortOrder)
        }
    }

    fun addBooks(
        uris: List<Uri>,
    ) {
        viewModelScope.launch {
            libraryScanner.importBooks(uris)
        }
    }

    fun createShelf(
        name: String,
    ) {
        viewModelScope.launch {
            bookRepository.insertShelf(Shelf(name = name))
        }
    }

    fun addLibraryFolder(
        uri: Uri,
    ) {
        viewModelScope.launch {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (_: Exception) {
            }

            val docFile = DocumentFile.fromTreeUri(context, uri)
            val name = docFile?.name ?: "Library Folder"
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            val current = userPreferencesRepository.libraryFolders.first()
            if (current.none { it.uri == uri.toString() }) {
                userPreferencesRepository.updateLibraryFolders(current + LibraryFolder(uri.toString(), name, dateStr))
                _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.folder_added)))
            }
            libraryScanner.scanAndImportFolder(uri)
        }
    }

    fun clearImportState() {
        libraryScanner.clearImportState()
    }
}
