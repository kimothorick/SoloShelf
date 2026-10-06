package com.kimothorick.soloshelf.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.CollectionBookCrossRef
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.models.LibraryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val collections: StateFlow<List<CollectionWithBooks>> = repository.getCollectionsWithBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
