package com.kimothorick.soloshelf.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.AudiobookDetails
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.EbookDetails
import com.kimothorick.soloshelf.data.models.Track
import com.kimothorick.soloshelf.data.preferences.SortOrder
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.kimothorick.soloshelf.domain.BookMetadataExtractor
import com.kimothorick.soloshelf.domain.PublicationRetriever
import com.kimothorick.soloshelf.domain.narratorName
import com.kimothorick.soloshelf.domain.scanner.MediaScanner
import com.kimothorick.soloshelf.util.Constants
import com.kimothorick.soloshelf.util.UiText
import com.kimothorick.soloshelf.util.isAudiobook
import com.kimothorick.soloshelf.util.isEbook
import com.kimothorick.soloshelf.util.toUserError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.flatMap
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.toUri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
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
    private val mediaScanner = MediaScanner(context, bookRepository)

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
            uris.forEach {
                try {
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to take persistable URI permission", e)
                }
            }

            val audiobookFolders = com.kimothorick.soloshelf.domain.scanner
                .AudiobookFolders(context)
            val allBookUris = mutableListOf<Uri>()

            uris.forEach { uri ->
                val resolvedUris = audiobookFolders.resolveBookFolderUris(uri)
                allBookUris.addAll(resolvedUris)
            }

            val hasAudio = allBookUris.any { uri ->
                val name = uri.toString().lowercase()
                com.kimothorick.soloshelf.domain.scanner.CachedDocumentFile
                    .isAudioExtension(name.substringAfterLast('.', "")) ||
                    com.kimothorick.soloshelf.domain.scanner.CachedDocumentFile
                        .listFiles(context, uri)
                        .any {
                            com.kimothorick.soloshelf.domain.scanner.CachedDocumentFile
                                .isAudioExtension(it.extension)
                        }
            }

            if (hasAudio || uris.size > 1) {
                Log.d("MainViewModel", "Importing audiobooks via MediaScanner pipeline")
                val scanned = mediaScanner.scanAndPersist(allBookUris.ifEmpty { uris })
                if (scanned.isNotEmpty()) {
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.PluralResource(R.plurals.books_added_to_library, scanned.size)))
                } else {
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.error_adding_book)))
                }
            } else {
                if (uris.size == 1) {
                    val handled = addSingleBook(uris[0])
                    if (!handled) {
                        _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.error_adding_book)))
                    }
                }
            }
        }
    }

    private suspend fun addSingleBook(
        uri: Uri,
    ): Boolean {
        val sourceUrlString = uri.toString()
        Log.d("MainViewModel", "addSingleBook: $sourceUrlString")

        // Check for duplicates before starting the import process
        if (bookRepository.getBookByFilePath(sourceUrlString) != null) {
            Log.i("MainViewModel", "Book already exists in database: $sourceUrlString")
            _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.import_error_already_exists)))
            return true // Mark as handled so we don't try fallback
        }

        val sourceUrl = AbsoluteUrl(sourceUrlString)
        if (sourceUrl == null) return false

        return assetRetriever
            .retrieve(sourceUrl)
            .flatMap { asset ->
                Log.d("MainViewModel", "Asset retrieved: ${asset.format.mediaType}, Extension: ${asset.format.fileExtension.value}")
                publicationRetriever.retrieve(asset).map { pub -> asset to pub }
            }.onSuccess { (asset, publication) ->
                Log.d("MainViewModel", "Publication opened successfully: ${publication.metadata.title}")
                if (asset.format.mediaType.isBitmap) {
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.unsupported_format)))
                    return@onSuccess
                }

                val isAudiobook = asset.format.isAudiobook || publication.conformsTo(org.readium.r2.shared.publication.Publication.Profile.AUDIOBOOK)
                val format = if (isAudiobook) {
                    "AUDIOBOOK"
                } else {
                    asset.format.fileExtension.value
                        .uppercase()
                }

                // Improve initial title extraction from URI to avoid "primary:..." paths
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
                    isAudiobook = isAudiobook,
                )

                val enrichedBook = bookMetadataExtractor.extractMetadata(initialBook)

                val id = bookRepository.insertBook(enrichedBook)

                if (id != -1L) {
                    if (isAudiobook) {
                        var durationMs = (publication.metadata.duration ?: 0.0).toLong() * 1000
                        val narrator = publication.metadata.narratorName.takeIf { it.isNotBlank() }

                        if (durationMs == 0L) {
                            val mmr = android.media.MediaMetadataRetriever()
                            try {
                                mmr.setDataSource(context, uri)
                                val durationStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                                durationMs = durationStr?.toLongOrNull() ?: 0L
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Failed to extract duration via MMR", e)
                            } finally {
                                try {
                                    mmr.release()
                                } catch (ex: Exception) {
                                }
                            }
                        }

                        bookRepository.insertAudiobookDetails(
                            AudiobookDetails(
                                bookId = id,
                                totalDurationMs = durationMs,
                                narrator = narrator,
                            ),
                        )

                        val extractedTracks = publication.readingOrder.mapIndexed { index, link ->
                            val href = link.href.toString()
                            val playablePath = if (href.startsWith("content://") || href.startsWith("file://") || href.startsWith("http://") || href.startsWith("https://")) {
                                href
                            } else {
                                sourceUrlString
                            }
                            Track(
                                bookId = id,
                                filePath = playablePath,
                                title = link.title ?: "Track ${index + 1}",
                                duration = (link.duration ?: 0.0).toInt(),
                                trackNumber = index + 1,
                            )
                        }
                        val tracks = if (extractedTracks.isNotEmpty()) {
                            extractedTracks
                        } else {
                            listOf(
                                Track(
                                    bookId = id,
                                    filePath = sourceUrlString,
                                    title = enrichedBook.title,
                                    duration = (durationMs / 1000).toInt(),
                                    trackNumber = 1,
                                ),
                            )
                        }
                        bookRepository.insertTracks(tracks)
                    } else {
                        val pageCount = publication.metadata.numberOfPages ?: 0
                        bookRepository.insertEbookDetails(
                            EbookDetails(bookId = id, pageCount = pageCount),
                        )
                    }
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.PluralResource(R.plurals.books_added_to_library, 1)))
                } else {
                    _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.error_adding_book)))
                }
            }.onFailure { error ->
                Log.d("MainViewModel", "Readium failed to open publication: $error")
            }.isSuccess
    }

    private suspend fun addMultiFileAudiobook(
        uris: List<Uri>,
        originalZipUri: Uri? = null,
    ) {
        Log.d("MainViewModel", "addMultiFileAudiobook: uris=${uris.size}, originalZipUri=$originalZipUri")

        val audioUris = mutableListOf<Uri>()
        val imageUris = mutableListOf<Uri>()
        val otherUris = mutableListOf<Uri>()

        uris.forEach { uri ->
            val type = context.contentResolver.getType(uri) ?: ""
            val name = uri.lastPathSegment?.lowercase() ?: ""

            val isAudio = type.startsWith("audio/") || name.endsWith(".mp3") || name.endsWith(".m4b") ||
                name.endsWith(".m4a") || name.endsWith(".wav") || name.endsWith(".flac") ||
                name.endsWith(".ogg") || name.endsWith(".aac")

            val isImage = type.startsWith("image/") || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".webp")

            val isManifest = type == "application/audiobook+json" || name.contains("manifest.json")

            if (isAudio) {
                audioUris.add(uri)
            } else if (isImage) {
                imageUris.add(uri)
            } else if (isManifest || type.contains("json") || name.endsWith(".json")) {
                otherUris.add(uri)
            }
        }

        val targetUris = audioUris.ifEmpty { uris }
        val scannedBooks = mediaScanner.scanAndPersist(targetUris)
        if (scannedBooks.isNotEmpty()) {
            _uiEvent.send(MainUiEvent.ShowToast(UiText.PluralResource(R.plurals.books_added_to_library, scannedBooks.size)))
            return
        }

        val lookupPath = originalZipUri?.toString() ?: audioUris.firstOrNull()?.toString() ?: targetUris.first().toString()

        if (bookRepository.getBookByFilePath(lookupPath) != null) {
            _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.import_error_already_exists)))
            return
        }

        val initialTitle = originalZipUri
            ?.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringAfterLast(':')
            ?.substringBeforeLast('.')
            ?: targetUris
                .firstOrNull()
                ?.lastPathSegment
                ?.substringAfterLast('/')
                ?.substringAfterLast(':')
                ?.substringBeforeLast('.')
            ?: "New Audiobook"

        val initialBook = Book(
            filePath = lookupPath,
            title = initialTitle,
            author = "Unknown Author",
            format = "AUDIOBOOK",
            isAudiobook = true,
        )

        val (metadataBook, details, tracks) = bookMetadataExtractor.extractAudiobookMetadata(audioUris, imageUris, otherUris, initialBook)
        Log.d("MainViewModel", "Audiobook metadata extracted: ${metadataBook.title} by ${metadataBook.author}. Tracks: ${tracks.size}")

        val bookId = bookRepository.insertBook(metadataBook)
        Log.d("MainViewModel", "Book inserted with ID: $bookId")
        if (bookId != -1L) {
            val tracksWithId = tracks.map { it.copy(bookId = bookId) }
            bookRepository.insertTracks(tracksWithId)
            bookRepository.insertAudiobookDetails(details.copy(bookId = bookId))
            _uiEvent.send(MainUiEvent.ShowToast(UiText.PluralResource(R.plurals.books_added_to_library, 1)))
        } else {
            _uiEvent.send(MainUiEvent.ShowToast(UiText.StringResource(R.string.error_adding_book)))
        }
    }

    fun createCollection(
        name: String,
    ) {
        viewModelScope.launch {
            bookRepository.insertCollection(Collection(name = name))
        }
    }

    private suspend fun handleZippedAudiobook(
        uri: Uri,
    ): List<Uri>? =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            Log.d("MainViewModel", "Checking if ZIP is a loose audiobook: $uri")
            try {
                val audioEntries = mutableListOf<String>()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.util.zip.ZipInputStream(inputStream).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val name = entry.name.lowercase()
                                if (name.contains("meta-inf/container.xml") || name.endsWith(".epub") || name.endsWith(".pdf")) {
                                    Log.d("MainViewModel", "Zip contains Readium manifest ($name), letting Readium handle it.")
                                    return@withContext null
                                }
                                if (name.endsWith(".mp3") || name.endsWith(".m4b") || name.endsWith(".m4a") ||
                                    name.endsWith(".wav") || name.endsWith(".flac") || name.endsWith(".ogg") || name.endsWith(".aac") ||
                                    name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp") ||
                                    name.endsWith(".json")
                                ) {
                                    audioEntries.add(entry.name)
                                }
                            }
                            entry = zis.nextEntry
                        }
                    }
                }

                if (audioEntries.isEmpty()) return@withContext null
                audioEntries.sort()

                val outputDir = File(context.filesDir, "extracted_audiobooks/${UUID.randomUUID()}")
                if (!outputDir.exists()) outputDir.mkdirs()

                val extractedUris = mutableListOf<Uri>()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.util.zip.ZipInputStream(inputStream).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory && entry.name in audioEntries) {
                                Log.d("MainViewModel", "Extracting entry: ${entry.name}")
                                val targetFile = File(outputDir, File(entry.name).name)
                                FileOutputStream(targetFile).use { fos ->
                                    zis.copyTo(fos)
                                }
                                extractedUris.add(Uri.fromFile(targetFile))
                            }
                            entry = zis.nextEntry
                        }
                    }
                }
                Log.d("MainViewModel", "Finished extraction, total URIs: ${extractedUris.size}")
                return@withContext extractedUris
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error processing zipped audiobook", e)
                return@withContext null
            }
        }
}
