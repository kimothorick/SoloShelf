package com.kimothorick.soloshelf.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Shelf
import com.kimothorick.soloshelf.data.models.ShelfBookCrossRef
import com.kimothorick.soloshelf.data.models.ShelfWithBooks
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.models.LibraryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: BookRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    init {
        viewModelScope.launch {
            try {
                repository.getAllBooksOnce()
            } catch (_: Exception) {
                // Ignore
            } finally {
                _isInitialLoadComplete.value = true
            }
        }
    }

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
                        pageCount = bookWithEbookDetails.ebookDetails?.pageCount ?: 0,
                    )
                }
            }
        }.cachedIn(viewModelScope)

    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete: StateFlow<Boolean> = _isInitialLoadComplete.asStateFlow()

    val recentEbooks: StateFlow<ImmutableList<LibraryItem.Ebook>> = repository
        .getRecentEbooks()
        .map { books ->
            _isInitialLoadComplete.value = true
            books
                .map { bookWithEbookDetails ->
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
                        pageCount = bookWithEbookDetails.ebookDetails?.pageCount ?: 0,
                    )
                }.toImmutableList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf(),
        )

    val shelves: StateFlow<List<ShelfWithBooks>> = repository
        .getShelvesWithBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun deleteBook(
        bookId: Long,
    ) {
        viewModelScope.launch {
            repository.getBookByIdOnce(bookId)?.let {
                repository.deleteBook(it)
            }
        }
    }

    fun addBookToShelf(
        bookId: Long,
        shelfId: Long,
    ) {
        viewModelScope.launch {
            repository.insertShelfBookCrossRef(
                ShelfBookCrossRef(shelfId = shelfId, bookId = bookId),
            )
        }
    }

    fun removeBookFromShelf(
        bookId: Long,
        shelfId: Long,
    ) {
        viewModelScope.launch {
            repository.deleteShelfBookCrossRef(
                ShelfBookCrossRef(shelfId = shelfId, bookId = bookId),
            )
        }
    }

    fun createShelf(
        name: String,
    ) {
        viewModelScope.launch {
            repository.insertShelf(Shelf(name = name))
        }
    }

    fun toggleFavorite(
        bookId: Long,
    ) {
        viewModelScope.launch {
            repository.toggleFavorite(bookId)
        }
    }

    fun resetProgress(
        bookId: Long,
    ) {
        viewModelScope.launch {
            repository.resetProgress(bookId)
        }
    }

    fun markFinished(
        bookId: Long,
    ) {
        viewModelScope.launch {
            repository.markFinished(bookId)
        }
    }
}
