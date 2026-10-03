package com.kimothorick.soloshelf.data

import android.util.Log
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.data.models.AudioProgress
import com.kimothorick.soloshelf.data.models.AudiobookDetails
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.BookWithAudioDetails
import com.kimothorick.soloshelf.data.models.BookWithBookmarks
import com.kimothorick.soloshelf.data.models.BookWithEbookDetails
import com.kimothorick.soloshelf.data.models.BookWithHighlights
import com.kimothorick.soloshelf.data.models.BookWithNotes
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.CollectionBookCrossRef
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.data.models.EbookDetails
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.data.models.Track
import com.kimothorick.soloshelf.data.preferences.SortOrder
import com.kimothorick.soloshelf.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.asset.AssetRetriever
import java.io.File

class BookRepository(
    private val bookDao: BookDao,
) {
    init {
        // One-time fix for audiobook flags after migration
        CoroutineScope(Dispatchers.IO).launch {
            bookDao.fixAudiobookFlags(Constants.AUDIO_EXTENSIONS)
        }
    }

    fun getAudiobooksWithProgress(
        sortOrder: SortOrder,
    ): Flow<PagingData<BookWithAudioDetails>> {
        val orderBy = getOrderBy(sortOrder)
        val query = SimpleSQLiteQuery("SELECT books.* FROM books WHERE isAudiobook = 1 ORDER BY $orderBy")

        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { bookDao.getAudiobooksWithProgress(query) },
        ).flow
    }

    fun getEbooksWithProgress(
        sortOrder: SortOrder,
    ): Flow<PagingData<BookWithEbookDetails>> {
        val orderBy = getOrderBy(sortOrder)
        val query = SimpleSQLiteQuery("SELECT books.* FROM books WHERE isAudiobook = 0 ORDER BY $orderBy")

        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { bookDao.getEbooksWithProgress(query) },
        ).flow
    }

    private fun getOrderBy(
        sortOrder: SortOrder,
    ): String =
        when (sortOrder) {
            SortOrder.TITLE_ASC -> "title ASC"

            SortOrder.TITLE_DESC -> "title DESC"

            SortOrder.AUTHOR_ASC -> "author ASC"

            SortOrder.AUTHOR_DESC -> "author DESC"

            SortOrder.DATE_ADDED_ASC -> "dateAdded ASC"

            SortOrder.DATE_ADDED_DESC -> "dateAdded DESC"

            SortOrder.LAST_READ_ASC -> "dateAdded ASC"

            // Placeholder for lastAccessed
            SortOrder.LAST_READ_DESC -> "dateAdded DESC" // Placeholder
        }

    fun getRecentAudiobooks(): Flow<PagingData<BookWithAudioDetails>> =
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { bookDao.getRecentAudiobooks() },
        ).flow

    fun getRecentEbooks(): Flow<PagingData<BookWithEbookDetails>> =
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { bookDao.getRecentEbooks() },
        ).flow

    fun getBookWithBookmarks(
        id: Long,
    ): Flow<BookWithBookmarks?> = bookDao.getBookWithBookmarks(id)

    fun getBookWithHighlights(
        id: Long,
    ): Flow<BookWithHighlights?> = bookDao.getBookWithHighlights(id)

    fun getBookWithNotes(
        id: Long,
    ): Flow<BookWithNotes?> = bookDao.getBookWithNotes(id)

    suspend fun getBookByFilePath(
        filePath: String,
    ): Book? = bookDao.getBookByFilePath(filePath)

    fun getBookById(
        id: Long,
    ): Flow<Book?> = bookDao.getBookById(id)

    suspend fun getBookByIdOnce(
        id: Long,
    ): Book? = bookDao.getBookByIdOnce(id)

    fun getBookAndProgressById(
        id: Long,
    ): Flow<BookAndProgress?> = bookDao.getBookAndProgressById(id)

    fun getBookWithAudioDetails(
        id: Long,
    ): Flow<BookWithAudioDetails?> = bookDao.getBookWithAudioDetails(id)

    suspend fun getBookWithAudioDetailsOnce(
        id: Long,
    ): BookWithAudioDetails? = bookDao.getBookWithAudioDetailsOnce(id)

    suspend fun getTracksForBook(
        bookId: Long,
    ): List<Track> = bookDao.getTracksForBook(bookId)

    suspend fun getBookAndProgressByIdOnce(
        id: Long,
    ): BookAndProgress? = bookDao.getBookAndProgressByIdOnce(id)

    suspend fun insertBook(
        book: Book,
    ): Long = bookDao.insertBook(book)

    suspend fun insertTracks(
        tracks: List<Track>,
    ) {
        bookDao.insertTracks(tracks)
    }

    suspend fun insertAudiobookDetails(
        details: AudiobookDetails,
    ) {
        bookDao.insertAudiobookDetails(details)
    }

    suspend fun deleteTracksForBook(bookId: Long) {
        bookDao.deleteTracksForBook(bookId)
    }

    suspend fun deleteAudiobookDetailsForBook(bookId: Long) {
        bookDao.deleteAudiobookDetailsForBook(bookId)
    }

    suspend fun insertAudioProgress(
        progress: AudioProgress,
    ) {
        bookDao.insertAudioProgress(progress)
    }

    suspend fun insertEbookDetails(
        details: EbookDetails,
    ) {
        bookDao.insertEbookDetails(details)
    }

    suspend fun updateBook(
        book: Book,
    ) {
        Log.d("BookRepository", "Updating book ID: ${book.bookId}, title: ${book.title}, coverPath: ${book.coverPath}")
        bookDao.insertBook(book) // Room @Insert(onConflict = REPLACE) handles updates
    }

    suspend fun insertBookmark(
        bookmark: Bookmark,
    ) {
        bookDao.insertBookmark(bookmark)
    }

    suspend fun insertReadingProgress(
        progress: ReadingProgress,
    ) {
        bookDao.insertReadingProgress(progress)
    }

    suspend fun insertHighlight(
        highlight: Highlight,
    ) {
        bookDao.insertHighlight(highlight)
    }

    suspend fun insertNote(
        note: Note,
    ) {
        bookDao.insertNote(note)
    }

    suspend fun deleteBook(
        book: Book,
    ) {
        // Delete only internal files (like the cover image)
        try {
            book.coverPath?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
        } catch (e: Exception) {
            // Log or ignore file deletion errors
        }

        // Then remove the reference from DB
        bookDao.deleteBook(book)
    }

    // Collections
    fun getCollectionsWithBooks(): Flow<List<CollectionWithBooks>> = bookDao.getCollectionsWithBooks()

    suspend fun insertCollection(
        collection: Collection,
    ): Long = bookDao.insertCollection(collection)

    suspend fun insertCollectionBookCrossRef(
        crossRef: CollectionBookCrossRef,
    ) {
        bookDao.insertCollectionBookCrossRef(crossRef)
    }

    suspend fun deleteCollection(
        collection: Collection,
    ) {
        bookDao.deleteCollection(collection)
    }
}
