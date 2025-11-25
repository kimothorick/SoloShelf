package com.kimothorick.soloshelf.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.BookWithBookmarks
import com.kimothorick.soloshelf.data.models.BookWithHighlights
import com.kimothorick.soloshelf.data.models.BookWithNotes
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao,
) {
    fun getBooks(): Flow<PagingData<Book>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { bookDao.getBooks() },
        ).flow

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

    suspend fun insertBook(
        book: Book,
    ) {
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
        bookDao.deleteBook(book)
    }
}
