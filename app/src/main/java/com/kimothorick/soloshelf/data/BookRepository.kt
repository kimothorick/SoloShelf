package com.kimothorick.soloshelf.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.BookWithBookmarks
import com.kimothorick.soloshelf.data.models.BookWithEbookDetails
import com.kimothorick.soloshelf.data.models.BookWithHighlights
import com.kimothorick.soloshelf.data.models.BookWithNotes
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Shelf
import com.kimothorick.soloshelf.data.models.ShelfBookCrossRef
import com.kimothorick.soloshelf.data.models.ShelfWithBooks
import com.kimothorick.soloshelf.data.models.EbookDetails
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.data.preferences.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.File

class BookRepository(
    private val bookDao: BookDao,
) {
    fun getEbooksWithProgress(
        sortOrder: SortOrder,
    ): Flow<PagingData<BookWithEbookDetails>> {
        val orderBy = getOrderBy(sortOrder)
        val query = SimpleSQLiteQuery("SELECT books.* FROM books ORDER BY $orderBy")

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
            SortOrder.LAST_READ_DESC -> "dateAdded DESC"
        }

    fun getRecentEbooks(): Flow<List<BookWithEbookDetails>> = bookDao.getRecentEbooks()

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

    suspend fun getAllBooksOnce(): List<Book> = bookDao.getAllBooks()

    fun getBookById(
        id: Long,
    ): Flow<Book?> = bookDao.getBookById(id)

    suspend fun getBookByIdOnce(
        id: Long,
    ): Book? = bookDao.getBookByIdOnce(id)

    fun getBookAndProgressById(
        id: Long,
    ): Flow<BookAndProgress?> = bookDao.getBookAndProgressById(id)

    suspend fun getBookAndProgressByIdOnce(
        id: Long,
    ): BookAndProgress? = bookDao.getBookAndProgressByIdOnce(id)

    suspend fun insertBook(
        book: Book,
    ): Long = bookDao.insertBook(book)

    suspend fun insertEbookDetails(
        details: EbookDetails,
    ) {
        bookDao.insertEbookDetails(details)
    }

    suspend fun updateBook(
        book: Book,
    ) {
        Log.d("BookRepository", "Updating book ID: ${book.bookId}, title: ${book.title}, coverPath: ${book.coverPath}")
        bookDao.insertBook(book)
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
        try {
            book.coverPath?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
        } catch (e: Exception) {
        }
        bookDao.deleteBook(book)
    }

    fun getShelvesWithBooks(): Flow<List<ShelfWithBooks>> = bookDao.getShelvesWithBooks()

    suspend fun insertShelf(
        shelf: Shelf,
    ): Long = bookDao.insertShelf(shelf)

    suspend fun insertShelfBookCrossRef(
        crossRef: ShelfBookCrossRef,
    ) {
        bookDao.insertShelfBookCrossRef(crossRef)
    }

    suspend fun deleteShelfBookCrossRef(
        crossRef: ShelfBookCrossRef,
    ) {
        bookDao.deleteShelfBookCrossRef(crossRef)
    }

    suspend fun deleteShelf(
        shelf: Shelf,
    ) {
        bookDao.deleteShelf(shelf)
    }

    suspend fun getShelvesWithBooksOnce(): List<ShelfWithBooks> {
        return bookDao.getShelvesWithBooks().first()
    }

    suspend fun toggleFavorite(bookId: Long) {
        addOrToggleBookToShelfByName(bookId, "Favorites")
    }

    suspend fun resetProgress(bookId: Long) {
        updateBookProgress(bookId, 0f)
    }

    suspend fun markFinished(bookId: Long) {
        updateBookProgress(bookId, 100f)
        addBookToShelfByName(bookId, "Finished")
    }

    suspend fun addOrToggleBookToShelfByName(bookId: Long, shelfName: String) {
        val shelves = getShelvesWithBooksOnce()
        val targetShelf = shelves.find { it.shelf.name.equals(shelfName, ignoreCase = true) }
        val shelfId = if (targetShelf != null) {
            targetShelf.shelf.shelfId
        } else {
            bookDao.insertShelf(Shelf(name = shelfName))
        }

        val isBookInShelf = targetShelf?.books?.any { it.bookId == bookId } == true
        if (isBookInShelf) {
            bookDao.deleteShelfBookCrossRef(ShelfBookCrossRef(shelfId = shelfId, bookId = bookId))
        } else {
            bookDao.insertShelfBookCrossRef(ShelfBookCrossRef(shelfId = shelfId, bookId = bookId))
        }
    }

    suspend fun addBookToShelfByName(bookId: Long, shelfName: String) {
        val shelves = getShelvesWithBooksOnce()
        val targetShelf = shelves.find { it.shelf.name.equals(shelfName, ignoreCase = true) }
        val shelfId = if (targetShelf != null) {
            targetShelf.shelf.shelfId
        } else {
            bookDao.insertShelf(Shelf(name = shelfName))
        }

        val isBookInShelf = targetShelf?.books?.any { it.bookId == bookId } == true
        if (!isBookInShelf) {
            bookDao.insertShelfBookCrossRef(ShelfBookCrossRef(shelfId = shelfId, bookId = bookId))
        }
    }

    suspend fun updateBookProgress(bookId: Long, percentage: Float) {
        val existing = bookDao.getBookAndProgressByIdOnce(bookId)
        val rawLastPosition = existing?.progress?.lastPosition ?: "{}"
        val lastPosition = try {
            val json = JSONObject(rawLastPosition)
            val locations = json.optJSONObject("locations") ?: JSONObject().also { json.put("locations", it) }
            val targetProgression = (percentage / 100f).coerceIn(0f, 1f)
            locations.put("totalProgression", targetProgression.toDouble())
            locations.put("progression", targetProgression.toDouble())
            json.toString()
        } catch (e: Exception) {
            "{}"
        }

        val progress = ReadingProgress(
            bookId = bookId,
            lastPosition = lastPosition,
            readPercentage = percentage,
            lastAccessed = System.currentTimeMillis()
        )
        bookDao.insertReadingProgress(progress)
    }
}
