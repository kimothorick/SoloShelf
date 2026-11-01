package com.kimothorick.soloshelf.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.Shelf
import com.kimothorick.soloshelf.data.models.ShelfBookCrossRef
import com.kimothorick.soloshelf.data.models.ShelfWithBooks
import com.kimothorick.soloshelf.domain.BookMetadataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Link
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val metadataExtractor: BookMetadataExtractor,
) : ViewModel() {
    private val bookId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val book: StateFlow<Book?> = bookId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                bookRepository.getBookById(id)
            }
        }.flatMapLatest { dbBook ->
            flow {
                emit(dbBook)
                if (dbBook != null && dbBook.synopsis.isNullOrBlank()) {
                    val enriched = metadataExtractor.extractMetadata(dbBook)
                    if (enriched != dbBook) {
                        bookRepository.updateBook(enriched)
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookAndProgress: StateFlow<BookAndProgress?> = bookId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                bookRepository.getBookAndProgressById(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tableOfContents: StateFlow<List<Link>> = bookId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                bookRepository.getBookById(id)
            }
        }.flatMapLatest { book ->
            flow {
                if (book != null) {
                    emit(metadataExtractor.getTableOfContents(book))
                } else {
                    emit(emptyList())
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val shelves: StateFlow<List<ShelfWithBooks>> = bookRepository
        .getShelvesWithBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun setBookId(
        id: Long,
    ) {
        if (bookId.value != id) {
            bookId.value = id
        }
    }

    fun deleteBook(
        bookId: Long,
    ) {
        viewModelScope.launch {
            bookRepository.getBookByIdOnce(bookId)?.let {
                bookRepository.deleteBook(it)
            }
        }
    }

    fun addBookToShelf(
        bookId: Long,
        shelfId: Long,
    ) {
        viewModelScope.launch {
            bookRepository.insertShelfBookCrossRef(
                ShelfBookCrossRef(shelfId = shelfId, bookId = bookId),
            )
        }
    }

    fun removeBookFromShelf(
        bookId: Long,
        shelfId: Long,
    ) {
        viewModelScope.launch {
            bookRepository.deleteShelfBookCrossRef(
                ShelfBookCrossRef(shelfId = shelfId, bookId = bookId),
            )
        }
    }

    fun createShelf(
        name: String,
    ) {
        viewModelScope.launch {
            bookRepository.insertShelf(Shelf(name = name))
        }
    }

    fun toggleFavorite(
        bookId: Long,
    ) {
        viewModelScope.launch {
            bookRepository.toggleFavorite(bookId)
        }
    }

    fun resetProgress(
        bookId: Long,
    ) {
        viewModelScope.launch {
            bookRepository.resetProgress(bookId)
        }
    }

    fun markFinished(
        bookId: Long,
    ) {
        viewModelScope.launch {
            bookRepository.markFinished(bookId)
        }
    }
}
