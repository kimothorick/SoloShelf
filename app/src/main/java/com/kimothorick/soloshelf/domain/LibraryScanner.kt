package com.kimothorick.soloshelf.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.EbookDetails
import com.kimothorick.soloshelf.util.UiText
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryScanner @Inject constructor(
    private val bookRepository: BookRepository,
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever,
    private val bookMetadataExtractor: BookMetadataExtractor,
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val importQueue = Channel<Uri>(capacity = Channel.UNLIMITED)

    private val _uiEvent = Channel<LibraryScannerEvent>()
    val uiEvent: Flow<LibraryScannerEvent> = _uiEvent.receiveAsFlow()

    private val _importState = MutableStateFlow(ImportState())
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun clearImportState() {
        _importState.value = ImportState()
    }

    init {
        startQueueWorker()
    }

    private fun startQueueWorker() {
        scope.launch {
            for (uri in importQueue) {
                processQueueItem(uri)
            }
        }
    }

    private suspend fun processQueueItem(
        uri: Uri,
    ) {
        val uriString = uri.toString()
        updateTaskStatus(uriString, ImportStatus.IN_PROGRESS, progress = 0.5f, subtitle = "Processing...")

        val status = try {
            importSingleBook(uri)
        } catch (e: Exception) {
            Log.e("LibraryScanner", "Exception importing queued book $uri: ${e.message}", e)
            ImportStatus.FAILED
        }

        val subtitle = when (status) {
            ImportStatus.ADDED -> "Added"
            ImportStatus.ALREADY_EXISTS -> "Already exists"
            ImportStatus.FAILED -> "Failed to import"
            else -> "Done"
        }

        updateTaskStatus(uriString, status, progress = 1f, subtitle = subtitle)

        _importState.update { state ->
            val newCompleted = state.completedCount + 1
            val isStillImporting = !importQueue.isEmpty || newCompleted < state.totalCount
            if (!isStillImporting) {
                scope.launch {
                    kotlinx.coroutines.delay(1000)
                    _importState.update { it.copy(isImporting = false) }
                }
            }
            state.copy(completedCount = newCompleted, isImporting = isStillImporting)
        }
    }

    suspend fun scanAndImportFolder(
        treeUri: Uri,
        showProgress: Boolean = true,
    ) {
        Log.d("LibraryScanner", "scanAndImportFolder started for: $treeUri, showProgress: $showProgress")
        try {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        } catch (e: Exception) {
            Log.w("LibraryScanner", "Failed to take persistable permission for $treeUri", e)
        }

        val bookUris = mutableListOf<Uri>()
        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val authority = treeUri.authority ?: "com.android.externalstorage.documents"
            Log.d("LibraryScanner", "Scanning with authority: $authority, rootDocId: $rootDocId")

            fun traverseDoc(docId: String) {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    ),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val childDocId = if (idCol != -1) cursor.getString(idCol) else continue
                        val mimeType = if (mimeCol != -1) cursor.getString(mimeCol) else null
                        val displayName = if (nameCol != -1) cursor.getString(nameCol) else ""

                        val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId)

                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                            traverseDoc(childDocId)
                        } else {
                            val name = displayName.lowercase()
                            Log.d("LibraryScanner", "Found file: $displayName with URI: $childUri")
                            if (name.endsWith(".epub") || name.endsWith(".pdf") || name.endsWith(".m4b") || name.endsWith(".zip")) {
                                bookUris.add(childUri)
                            }
                        }
                    }
                }
            }

            traverseDoc(rootDocId)
        } catch (e: Exception) {
            Log.e("LibraryScanner", "Error querying tree URI $treeUri with DocumentsContract, falling back to DocumentFile", e)
            val docFile = DocumentFile.fromTreeUri(context, treeUri)
            if (docFile != null) {
                fun traverse(file: DocumentFile) {
                    if (file.isDirectory) {
                        file.listFiles().forEach { traverse(it) }
                    } else if (file.isFile) {
                        val name = file.name?.lowercase() ?: ""
                        Log.d("LibraryScanner", "Fallback found file: ${file.name} with URI: ${file.uri}")
                        if (name.endsWith(".epub") || name.endsWith(".pdf") || name.endsWith(".m4b") || name.endsWith(".zip")) {
                            bookUris.add(file.uri)
                        }
                    }
                }
                traverse(docFile)
            }
        }

        Log.d("LibraryScanner", "Total book files found in folder: ${bookUris.size}")
        if (bookUris.isNotEmpty()) {
            importBooks(bookUris, showProgress = showProgress)
        } else {
            Log.i("LibraryScanner", "No book files found in folder: $treeUri")
        }
    }

    fun importBooks(
        uris: List<Uri>,
        showProgress: Boolean = true,
    ) {
        Log.d("LibraryScanner", "importBooks called with ${uris.size} URIs, showProgress: $showProgress")
        if (uris.isEmpty()) return

        // For direct file picker URIs (OpenDocument), take persistable permission
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: Exception) {}
        }

        enqueueBooks(uris, showProgress = showProgress)
    }

    private fun enqueueBooks(
        uris: List<Uri>,
        showProgress: Boolean = true,
    ) {
        val newTasks = uris.map { uri ->
            val fileName = uri.lastPathSegment
                ?.substringAfterLast('/')
                ?.substringAfterLast(':')
                ?.substringBeforeLast('.')
                ?: "Book"
            ImportTaskItem(
                id = uri.toString(),
                fileName = fileName,
                fileSize = "Ready",
                status = ImportStatus.QUEUED,
                subtitle = "Queued",
            )
        }

        if (showProgress) {
            _importState.update { currentState ->
                val combinedItems = currentState.items + newTasks
                currentState.copy(
                    isImporting = true,
                    totalCount = combinedItems.size,
                    items = combinedItems,
                )
            }
        }

        scope.launch {
            uris.forEach { uri ->
                importQueue.send(uri)
            }
        }
    }

    private fun updateTaskStatus(
        id: String,
        status: ImportStatus,
        progress: Float,
        subtitle: String,
    ) {
        _importState.update { state ->
            val newItems = state.items.map { item ->
                if (item.id == id) {
                    item.copy(status = status, progress = progress, subtitle = subtitle)
                } else {
                    item
                }
            }
            state.copy(items = newItems)
        }
    }

    private suspend fun doesBookExist(
        uri: Uri,
    ): Boolean {
        val sourceUrlString = uri.toString()
        if (bookRepository.getBookByFilePath(sourceUrlString) != null) {
            return true
        }

        val isDoc = try {
            DocumentsContract.isDocumentUri(context, uri)
        } catch (_: Exception) {
            false
        }

        if (isDoc) {
            val docId = try {
                DocumentsContract.getDocumentId(uri)
            } catch (_: Exception) {
                null
            }

            if (docId != null) {
                val allBooks = bookRepository.getAllBooksOnce()
                for (book in allBooks) {
                    val existingUri = Uri.parse(book.filePath)
                    val existingIsDoc = try {
                        DocumentsContract.isDocumentUri(context, existingUri)
                    } catch (_: Exception) {
                        false
                    }
                    if (existingIsDoc) {
                        val existingDocId = try {
                            DocumentsContract.getDocumentId(existingUri)
                        } catch (_: Exception) {
                            null
                        }
                        if (existingDocId == docId) {
                            Log.i("LibraryScanner", "Book already exists via document ID match: $docId")
                            return true
                        }
                    }
                }
            }
        }

        val path = uri.path
        if (path != null) {
            val allBooks = bookRepository.getAllBooksOnce()
            for (book in allBooks) {
                val existingUri = Uri.parse(book.filePath)
                if (existingUri.path == path) {
                    Log.i("LibraryScanner", "Book already exists via path match: $path")
                    return true
                }
            }
        }

        return false
    }

    private suspend fun importSingleBook(
        uri: Uri,
    ): ImportStatus {
        val sourceUrlString = uri.toString()
        Log.d("LibraryScanner", "Importing single book from URI: $sourceUrlString")

        if (doesBookExist(uri)) {
            Log.i("LibraryScanner", "Book already exists in database: $sourceUrlString")
            return ImportStatus.ALREADY_EXISTS
        }

        val sourceUrl = AbsoluteUrl(sourceUrlString)
        if (sourceUrl == null) {
            Log.e("LibraryScanner", "Failed to parse AbsoluteUrl from: $sourceUrlString")
            return ImportStatus.FAILED
        }

        val assetResult = assetRetriever.retrieve(sourceUrl)
        if (assetResult.isFailure) {
            val err = assetResult.failureOrNull()
            Log.e("LibraryScanner", "AssetRetriever failed for $sourceUrlString. Reason: $err, message: ${err?.message}, cause: ${err?.cause}")
            return ImportStatus.FAILED
        }

        val asset = assetResult.getOrNull()
        if (asset == null || asset.format.mediaType.isBitmap) {
            Log.e("LibraryScanner", "Invalid asset or bitmap format for $sourceUrlString (asset: $asset)")
            return ImportStatus.FAILED
        }

        val pubResult = publicationRetriever.retrieve(asset)
        if (pubResult.isFailure) {
            val err = pubResult.failureOrNull()
            Log.e("LibraryScanner", "PublicationRetriever failed for $sourceUrlString. Reason: $err, message: ${err?.message}, cause: ${err?.cause}")
            return ImportStatus.FAILED
        }

        val publication = pubResult.getOrNull()
        if (publication == null) {
            Log.e("LibraryScanner", "Publication is null for $sourceUrlString")
            return ImportStatus.FAILED
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
            val pageCount = publication.metadata.numberOfPages ?: 0
            bookRepository.insertEbookDetails(
                EbookDetails(bookId = id, pageCount = pageCount),
            )
            Log.d("LibraryScanner", "Successfully inserted book ID $id: $initialTitle")
            return ImportStatus.ADDED
        } else {
            Log.e("LibraryScanner", "Failed to insert book into database: $initialTitle")
            return ImportStatus.FAILED
        }
    }
}

sealed class LibraryScannerEvent {
    data class ShowToast(
        val message: UiText,
    ) : LibraryScannerEvent()
}
