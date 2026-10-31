package com.kimothorick.soloshelf.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.PublicationRetriever
import com.kimothorick.soloshelf.domain.ReaderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import javax.inject.Inject

sealed interface NavigationTarget {
    data class LinkTarget(val link: Link) : NavigationTarget
    data class LocatorTarget(val locator: Locator) : NavigationTarget
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever,
    private val readerRepository: ReaderRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _publication = MutableStateFlow<org.readium.r2.shared.publication.Publication?>(null)
    val publication: StateFlow<org.readium.r2.shared.publication.Publication?> = _publication.asStateFlow()

    private val _initialLocator = MutableStateFlow<Locator?>(null)
    val initialLocator: StateFlow<Locator?> = _initialLocator.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _readPercentage = MutableStateFlow(0f)
    val readPercentage: StateFlow<Float> = _readPercentage.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationTarget>()
    val navigationEvent: SharedFlow<NavigationTarget> = _navigationEvent.asSharedFlow()

    private var isInitialLoad = true

    private val _showBars = MutableStateFlow(true)
    val showBars: StateFlow<Boolean> = _showBars.asStateFlow()

    private val _showToc = MutableStateFlow(false)
    val showToc: StateFlow<Boolean> = _showToc.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    val readerPreferences: StateFlow<EpubPreferences> = combine(
        userPreferencesRepository.readerTheme,
        userPreferencesRepository.readerFontFamily,
        userPreferencesRepository.readerFontSize,
        userPreferencesRepository.readerLineHeight,
        userPreferencesRepository.readerPageMargins
    ) { theme, fontFamily, fontSize, lineHeight, pageMargins ->
        EpubPreferences(
            theme = when (theme) {
                "sepia" -> Theme.SEPIA
                "dark" -> Theme.DARK
                else -> Theme.LIGHT
            },
            fontFamily = fontFamily?.let { FontFamily(it) },
            fontSize = fontSize.toDouble() / 100.0,
            lineHeight = lineHeight.toDouble() / 100.0,
            pageMargins = pageMargins.toDouble() / 100.0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = EpubPreferences()
    )

    val readerBrightness = userPreferencesRepository.readerBrightness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateReaderTheme(theme: String) {
        viewModelScope.launch { userPreferencesRepository.updateReaderTheme(theme) }
    }

    fun updateReaderFontFamily(fontFamily: String?) {
        viewModelScope.launch { userPreferencesRepository.updateReaderFontFamily(fontFamily) }
    }

    fun updateReaderFontSize(size: Int) {
        viewModelScope.launch { userPreferencesRepository.updateReaderFontSize(size) }
    }

    fun updateReaderLineHeight(height: Int) {
        viewModelScope.launch { userPreferencesRepository.updateReaderLineHeight(height) }
    }

    fun updateReaderPageMargins(margins: Int) {
        viewModelScope.launch { userPreferencesRepository.updateReaderPageMargins(margins) }
    }

    fun updateReaderBrightness(brightness: Int?) {
        viewModelScope.launch { userPreferencesRepository.updateReaderBrightness(brightness) }
    }

    fun loadBook(bookId: Long) {
        // If we are already loading or have loaded this specific book, avoid redundant work.
        // This is crucial for configuration changes (like folding/unfolding) where
        // we want to preserve the publication and current position.
        if (_book.value?.bookId == bookId) return

        // Immediately reset state synchronously to prevent UI from showing stale data
        isInitialLoad = true
        _isLoading.value = true
        _book.value = null
        _publication.value = null
        _initialLocator.value = null
        readerRepository.clear()

        viewModelScope.launch {
            val dbBookAndProgress = bookRepository.getBookAndProgressByIdOnce(bookId)
            if (dbBookAndProgress != null) {
                _book.value = dbBookAndProgress.book
                dbBookAndProgress.progress?.let { progress ->
                    _readPercentage.value = progress.readPercentage
                    try {
                        val json = JSONObject(progress.lastPosition)
                        _initialLocator.value = Locator.fromJSON(json)
                    } catch (e: Exception) {
                        // Ignore invalid progress
                    }
                }
                openPublication(dbBookAndProgress.book)
            } else {
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            bookRepository.getBookWithBookmarks(bookId).collect {
                _bookmarks.value = it?.bookmarks ?: emptyList()
            }
        }

        viewModelScope.launch {
            bookRepository.getBookWithNotes(bookId).collect {
                _notes.value = it?.notes ?: emptyList()
            }
        }
    }

    private suspend fun openPublication(book: Book) {
        val url = AbsoluteUrl(book.filePath) ?: run {
            _isLoading.value = false
            return
        }
        val asset = assetRetriever.retrieve(url).getOrElse {
            _isLoading.value = false
            return
        }
        val publication = publicationRetriever.retrieve(asset).getOrElse {
            _isLoading.value = false
            return
        }
        readerRepository.currentPublication = publication
        _publication.value = publication
        _isLoading.value = false
    }

    fun toggleBars() {
        _showBars.value = !_showBars.value
    }

    fun toggleToc() {
        val willShow = !_showToc.value
        _showToc.value = willShow
        if (willShow) {
            _showSettings.value = false
        }
        _showBars.value = !willShow
    }

    fun toggleSettings() {
        val willShow = !_showSettings.value
        _showSettings.value = willShow
        if (willShow) {
            _showToc.value = false
        }
    }

    fun onTocItemSelected(link: Link) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationTarget.LinkTarget(link))
            _showToc.value = false
            _showBars.value = false // Hide bars to start reading
        }
    }

    fun onLocatorSelected(locator: Locator) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationTarget.LocatorTarget(locator))
            _showToc.value = false
            _showBars.value = false // Hide bars to start reading
        }
    }

    fun closeOverlays() {
        _showToc.value = false
        _showSettings.value = false
    }

    fun saveProgress(locator: Locator) {
        val currentBook = _book.value ?: return

        // Skip the very first emission if it's likely a default Page 1 emission
        // that happens before the navigator has applied our initialLocator.
        if (isInitialLoad) {
            val initial = _initialLocator.value
            // If we have an initial locator that isn't the start, and this is the start, skip it.
            if (initial != null && 
                (initial.locations.totalProgression ?: 0.0) > 0.01 && 
                (locator.locations.totalProgression ?: 0.0) < 0.01) {
                // Keep isInitialLoad = true to wait for the actual jump
                return
            }
            isInitialLoad = false
        }

        // Add a debounce check to avoid saving progress too frequently
        // which could lead to DB-induced UI jitters or performance issues.
        val lastProgress = _initialLocator.value
        if (lastProgress != null && lastProgress.locations.totalProgression == locator.locations.totalProgression) {
            return
        }
        _initialLocator.value = locator
        val percentage = (locator.locations.totalProgression?.times(100f))?.toFloat() ?: 0f
        _readPercentage.value = percentage

        viewModelScope.launch {
            val progress = ReadingProgress(
                bookId = currentBook.bookId,
                lastPosition = locator.toJSON().toString(),
                readPercentage = percentage,
                lastAccessed = System.currentTimeMillis()
            )
            bookRepository.insertReadingProgress(progress)
        }
    }

    override fun onCleared() {
        super.onCleared()
        readerRepository.clear()
    }
}
