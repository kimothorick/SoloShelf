package com.kimothorick.soloshelf.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
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
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val metadataExtractor: BookMetadataExtractor,
) : ViewModel() {
    private val _bookId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val book: StateFlow<Book?> = _bookId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else bookRepository.getBookById(id)
        }
        .flatMapLatest { dbBook ->
            flow {
                // Emit current DB state immediately to avoid blank screen
                emit(dbBook)
                
                // If the book exists but is missing details, extract and save them
                if (dbBook != null && (dbBook.synopsis.isNullOrBlank() || dbBook.totalPages == 0)) {
                    val enriched = metadataExtractor.extractMetadata(dbBook)
                    if (enriched != dbBook) {
                        bookRepository.updateBook(enriched)
                        // Note: Room will automatically emit the updated book via getBookById
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    fun setBookId(id: Long) {
        if (_bookId.value != id) {
            _bookId.value = id
        }
    }
}
