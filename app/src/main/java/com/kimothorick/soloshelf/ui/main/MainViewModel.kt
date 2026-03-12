package com.kimothorick.soloshelf.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.preferences.SortOrder
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.PublicationRetriever
import com.kimothorick.soloshelf.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.publication.services.positions
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

sealed class MainUiEvent {
    data class ShowToast(
        val resId: Int,
        val quantity: Int? = null,
        val args: List<Any> = emptyList(),
    ) : MainUiEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookRepository: BookRepository,
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val sortOrder: StateFlow<SortOrder> = userPreferencesRepository.sortOrder
        .stateIn(
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

    fun addBook(
        uri: Uri,
    ) {
        viewModelScope.launch {
            val existingBook = withContext(Dispatchers.IO) {
                bookRepository.getBookByFilePath(uri.toString())
            }
            if (existingBook != null) {
                _uiEvent.send(MainUiEvent.ShowToast(R.string.book_already_in_library))
                return@launch
            }

            val book = withContext(Dispatchers.IO) {
                extractInitialMetadata(uri)
            }
            if (book != null) {
                if (book.format in Constants.SUPPORTED_EXTENSIONS) {
                    bookRepository.insertBook(book)
                    _uiEvent.send(MainUiEvent.ShowToast(resId = R.plurals.books_added_to_library, quantity = 1))
                } else {
                    _uiEvent.send(MainUiEvent.ShowToast(R.string.unsupported_format))
                }
            } else {
                _uiEvent.send(MainUiEvent.ShowToast(R.string.error_adding_book))
            }
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            bookRepository.insertCollection(Collection(name = name))
        }
    }

    private suspend fun extractInitialMetadata(
        uri: Uri,
    ): Book? = try {
        val url = AbsoluteUrl(uri.toString()) ?: throw Exception("Invalid URI")
        val asset = assetRetriever.retrieve(url).getOrElse { throw Exception(it.toString()) }
        
        publicationRetriever.retrieve(asset).getOrElse { throw Exception(it.toString()) }.let { publication ->
            val metadata = publication.metadata
            val fileName = uri.lastPathSegment ?: context.getString(R.string.unknown_book)
            val title = metadata.title ?: fileName
            val author = metadata.authors.firstOrNull()?.name ?: context.getString(R.string.unknown_author)

            val coverPath = publication.cover()?.let { bitmap ->
                saveCoverImage(bitmap)
            }

            // Attempt to get page count from positions service if not in metadata
            val pageCount = metadata.numberOfPages ?: publication.positions().size

            Book(
                title = title,
                author = author,
                filePath = uri.toString(),
                format = asset.format.fileExtension.value.uppercase(),
                coverPath = coverPath,
                totalPages = pageCount,
                synopsis = null,
                language = null,
                publicationDate = null,
                genres = emptyList(),
            )
        }
    } catch (e: Exception) {
        Log.e("MainViewModel", "Error extracting initial metadata", e)
        null
    }

    private fun saveCoverImage(bitmap: Bitmap): String? {
        return try {
            val filename = "cover_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, "covers").apply {
                if (!exists()) mkdirs()
            }.let { File(it, filename) }

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error saving cover image", e)
            null
        }
    }
}
