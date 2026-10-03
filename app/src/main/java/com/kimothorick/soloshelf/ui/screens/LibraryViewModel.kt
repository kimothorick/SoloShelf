package com.kimothorick.soloshelf.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookWithAudioDetails
import com.kimothorick.soloshelf.data.models.BookWithEbookDetails
import com.kimothorick.soloshelf.data.models.CollectionBookCrossRef
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.models.LibraryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { Ebooks, Audiobooks }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: BookRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.Ebooks)
    val selectedTab = _selectedTab.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val ebooks: Flow<PagingData<LibraryItem.Ebook>> = userPreferencesRepository.sortOrder
        .flatMapLatest { sortOrder ->
            repository.getEbooksWithProgress(sortOrder).map { pagingData ->
                pagingData.map { bookWithEbookDetails ->
                    val book = bookWithEbookDetails.book
                    LibraryItem.Ebook(
                        id = book.bookId,
                        title = book.title,
                        author = book.author,
                        coverPath = book.coverPath,
                        filePath = book.filePath,
                        mediaType = book.mediaType,
                        lastReadLocatorJson = bookWithEbookDetails.progress?.lastPosition,
                        lastAccessed = bookWithEbookDetails.progress?.lastAccessed,
                        progressPercentage = bookWithEbookDetails.progress?.readPercentage ?: 0f,
                        pageCount = bookWithEbookDetails.ebookDetails?.pageCount ?: 0
                    )
                }
            }
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val audiobooks: Flow<PagingData<LibraryItem.Audiobook>> = userPreferencesRepository.sortOrder
        .flatMapLatest { sortOrder ->
            repository.getAudiobooksWithProgress(sortOrder).map { pagingData ->
                pagingData.map { bookWithAudioDetails ->
                    val book = bookWithAudioDetails.book
                    val totalDuration = bookWithAudioDetails.audioDetails?.totalDurationMs ?: 0L
                    val listenPercentage = calculateListenPercentage(bookWithAudioDetails)

                    LibraryItem.Audiobook(
                        id = book.bookId,
                        title = book.title,
                        author = book.author,
                        coverPath = book.coverPath,
                        filePath = book.filePath,
                        mediaType = book.mediaType,
                        lastReadLocatorJson = null,
                        lastAccessed = bookWithAudioDetails.audioProgress?.lastAccessed,
                        progressPercentage = listenPercentage,
                        totalDurationMs = totalDuration,
                        narrator = bookWithAudioDetails.audioDetails?.narrator
                    )
                }
            }
        }.cachedIn(viewModelScope)

    val recentEbooks: Flow<PagingData<LibraryItem.Ebook>> = repository.getRecentEbooks().map { pagingData ->
        pagingData.map { bookWithEbookDetails ->
            val book = bookWithEbookDetails.book
            LibraryItem.Ebook(
                id = book.bookId,
                title = book.title,
                author = book.author,
                coverPath = book.coverPath,
                filePath = book.filePath,
                mediaType = book.mediaType,
                lastReadLocatorJson = bookWithEbookDetails.progress?.lastPosition,
                lastAccessed = bookWithEbookDetails.progress?.lastAccessed,
                progressPercentage = bookWithEbookDetails.progress?.readPercentage ?: 0f,
                pageCount = bookWithEbookDetails.ebookDetails?.pageCount ?: 0
            )
        }
    }.cachedIn(viewModelScope)

    val recentAudiobooks: Flow<PagingData<LibraryItem.Audiobook>> = repository.getRecentAudiobooks().map { pagingData ->
        pagingData.map { bookWithAudioDetails ->
            val book = bookWithAudioDetails.book
            val totalDuration = bookWithAudioDetails.audioDetails?.totalDurationMs ?: 0L
            val listenPercentage = calculateListenPercentage(bookWithAudioDetails)
            
            LibraryItem.Audiobook(
                id = book.bookId,
                title = book.title,
                author = book.author,
                coverPath = book.coverPath,
                filePath = book.filePath,
                mediaType = book.mediaType,
                lastReadLocatorJson = null,
                lastAccessed = bookWithAudioDetails.audioProgress?.lastAccessed,
                progressPercentage = listenPercentage,
                totalDurationMs = totalDuration,
                narrator = bookWithAudioDetails.audioDetails?.narrator
            )
        }
    }.cachedIn(viewModelScope)

    private fun calculateListenPercentage(bookWithAudioDetails: BookWithAudioDetails): Float {
        val tracks = bookWithAudioDetails.tracks.sortedBy { it.trackNumber }
        val totalDuration = bookWithAudioDetails.audioDetails?.totalDurationMs ?: 0L
        val currentTrackId = bookWithAudioDetails.audioProgress?.currentTrackId
        val currentOffset = bookWithAudioDetails.audioProgress?.currentOffsetMs ?: 0L

        var playedMs = 0L
        var found = false
        for (track in tracks) {
            if (track.trackId == currentTrackId) {
                playedMs += currentOffset
                found = true
                break
            } else {
                playedMs += (track.duration * 1000L)
            }
        }
        if (!found && tracks.isNotEmpty() && currentOffset > 0) {
            playedMs = currentOffset
        }

        return if (totalDuration > 0) {
            (playedMs.toFloat() / totalDuration.toFloat()) * 100f
        } else {
            0f
        }
    }

    val collections: StateFlow<List<CollectionWithBooks>> = repository.getCollectionsWithBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            repository.getBookByIdOnce(bookId)?.let {
                repository.deleteBook(it)
            }
        }
    }

    fun addBookToCollection(bookId: Long, collectionId: Long) {
        viewModelScope.launch {
            repository.insertCollectionBookCrossRef(
                CollectionBookCrossRef(collectionId = collectionId, bookId = bookId)
            )
        }
    }
}
