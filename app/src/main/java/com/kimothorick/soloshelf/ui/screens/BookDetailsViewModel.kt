package com.kimothorick.soloshelf.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.BookWithAudioDetails
import com.kimothorick.soloshelf.data.models.Track
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
import org.readium.r2.shared.publication.Href
import org.readium.r2.shared.publication.Link
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val metadataExtractor: BookMetadataExtractor,
) : ViewModel() {
    private val bookId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val book: StateFlow<Book?> = bookId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else bookRepository.getBookById(id)
        }
        .flatMapLatest { dbBook ->
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
            if (id == null) flowOf(null)
            else bookRepository.getBookAndProgressById(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tableOfContents: StateFlow<List<Link>> = bookId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else bookRepository.getBookWithAudioDetails(id)
        }
        .flatMapLatest { details ->
            flow {
                if (details != null) {
                    if (details.book.isAudiobook) {
                        val tracks = details.tracks.sortedBy { it.trackNumber }
                        val links = tracks.mapIndexed { index, track ->
                            Link(
                                href = Href("chapter_${index + 1}")!!,
                                title = track.title,
                                duration = track.duration.toDouble(),
                            )
                        }
                        emit(links)
                    } else {
                        emit(metadataExtractor.getTableOfContents(details.book))
                    }
                } else {
                    emit(emptyList())
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun setBookId(id: Long) {
        if (bookId.value != id) {
            bookId.value = id
        }
    }
}
