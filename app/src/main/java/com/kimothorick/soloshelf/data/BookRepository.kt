package com.kimothorick.soloshelf.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.BookWithBookmarks
import com.kimothorick.soloshelf.data.models.BookWithHighlights
import com.kimothorick.soloshelf.data.models.BookWithNotes
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.CollectionBookCrossRef
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.data.preferences.SortOrder
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao,
) {
    fun getBooksWithProgress(sortOrder: SortOrder): Flow<PagingData<BookAndProgress>> {
        val orderBy = when (sortOrder) {
            SortOrder.TITLE_ASC -> "title ASC"
            SortOrder.TITLE_DESC -> "title DESC"
            SortOrder.AUTHOR_ASC -> "author ASC"
            SortOrder.AUTHOR_DESC -> "author DESC"
            SortOrder.DATE_ADDED_ASC -> "dateAdded ASC"
            SortOrder.DATE_ADDED_DESC -> "dateAdded DESC"
            SortOrder.LAST_READ_ASC -> "lastAccessed ASC"
            SortOrder.LAST_READ_DESC -> "lastAccessed DESC"
        }

        val query = if (sortOrder == SortOrder.LAST_READ_ASC || sortOrder == SortOrder.LAST_READ_DESC) {
            SimpleSQLiteQuery("SELECT books.* FROM books LEFT JOIN progress ON books.bookId = progress.bookId ORDER BY $orderBy")
        } else {
            SimpleSQLiteQuery("SELECT * FROM books ORDER BY $orderBy")
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { bookDao.getBooksWithProgress(query) },
        ).flow
    }

    fun getRecentBooksWithProgress(): Flow<PagingData<BookAndProgress>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { bookDao.getRecentBooksWithProgress() },
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

    suspend fun getBookByFilePath(filePath: String): Book? = bookDao.getBookByFilePath(filePath)

    fun getBookById(id: Long): Flow<Book?> = bookDao.getBookById(id)

    suspend fun insertBook(
        book: Book,
    ) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(
        book: Book,
    ) {
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
        bookDao.deleteBook(book)
    }

    // Collections
    fun getCollectionsWithBooks(): Flow<List<CollectionWithBooks>> = bookDao.getCollectionsWithBooks()

    suspend fun insertCollection(collection: Collection): Long = bookDao.insertCollection(collection)

    suspend fun insertCollectionBookCrossRef(crossRef: CollectionBookCrossRef) {
        bookDao.insertCollectionBookCrossRef(crossRef)
    }

    suspend fun deleteCollection(collection: Collection) {
        bookDao.deleteCollection(collection)
    }
}
