package com.kimothorick.soloshelf.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.EbookDetails
import com.kimothorick.soloshelf.data.preferences.SortOrder
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.BookMetadataExtractor
import com.kimothorick.soloshelf.domain.PublicationRetriever
import com.kimothorick.soloshelf.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.flatMap
import javax.inject.Inject

@androidx.media3.common.util.UnstableApi
sealed class MainUiEvent {
    data class ShowToast(
        val message: UiText,
    ) : MainUiEvent()
}

@androidx.media3.common.util.UnstableApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookRepository: BookRepository,
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever,
    private val bookMetadataExtractor: BookMetadataExtractor,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val sortOrder: StateFlow<SortOrder> = userPreferencesRepository.sortOrder.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SortOrder.LAST_READ_DESC,
    )

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
        Log.d("MainViewModel", "addBooks called with ${uris.size} URIs")
        viewModelScope.launch {
            if (uris.isEmpty()) return@launch

            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    addSingleBook(uri)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to add book $uri", e)
                }
            }
        }
    }

    private suspend fun addSingleBook(
        uri: Uri,
    ): Boolean {
        val sourceUrlString = uri.toString()
        Log.d("MainViewModel", "addSingleBook: $sourceUrlString")

        if (bookRepository.getBookByFilePath(sourceUrlString) != null) {
            Log.i("MainViewModel", "Book already exists in database: $sourceUrlString")
            _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.import_error_already_exists)))
            return true
        }

        val sourceUrl = AbsoluteUrl(sourceUrlString)
        if (sourceUrl == null) return false

        return assetRetriever
            .retrieve(sourceUrl)
            .flatMap { asset ->
                publicationRetriever.retrieve(asset).map { pub -> asset to pub }
            }.onSuccess { (asset, publication) ->
                if (asset.format.mediaType.isBitmap) {
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.unsupported_format)))
                    return@onSuccess
                }

                val format = asset.format.fileExtension.value.uppercase()
                val initialTitle = uri.lastPathSegment
                    ?.substringAfterLast('/')
                    ?.substringAfterLast(':')
                    ?.substringBeforeLast('.')
                    ?: sourceUrl.filename?.substringBeforeLast('.')
                    ?: "New Book"

                val initialBook = Book(
                    filePath = sourceUrlString,
                    title = initialTitle,
                    author = "",
                    format = format,
                )

                val enrichedBook = bookMetadataExtractor.extractMetadata(initialBook)
                val id = bookRepository.insertBook(enrichedBook)

                if (id != -1L) {
                    try {
                        val metadataJson = publication.metadata.toJSON().toString(2)
                        Log.d("BookMetadataRaw", "Raw Metadata JSON:\n$metadataJson")
                    } catch (e: Exception) {
                        Log.e("BookMetadataRaw", "Failed to serialize metadata to JSON", e)
                    }

                    Log.d("BookMetadataRaw", "Publication details - Links: ${publication.links.size}, ReadingOrder: ${publication.readingOrder.size}, Resources: ${publication.resources.size}, OtherMetadata: ${publication.metadata.otherMetadata}")

                    val pageCount = publication.metadata.numberOfPages ?: 0
                    bookRepository.insertEbookDetails(
                        EbookDetails(bookId = id, pageCount = pageCount),
                    )
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.PluralResource(R.plurals.books_added_to_library, 1)))
                } else {
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.error_adding_book)))
                }
            }.onFailure { error ->
                Log.d("MainViewModel", "Readium failed to open publication: $error")
            }.isSuccess
    }

    fun createCollection(
        name: String,
    ) {
        viewModelScope.launch {
            bookRepository.insertCollection(Collection(name = name))
        }
    }
}
