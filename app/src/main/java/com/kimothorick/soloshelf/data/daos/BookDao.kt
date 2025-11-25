package com.kimothorick.soloshelf.data.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(
        book: Book,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(
        bookmark: Bookmark,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingProgress(
        progress: ReadingProgress,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(
        highlight: Highlight,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(
        note: Note,
    )

    @Query("SELECT * FROM books")
    fun getBooks(): PagingSource<Int, Book>

    @Query("SELECT * FROM books INNER JOIN progress ON books.bookId = progress.bookId ORDER BY progress.lastAccessed DESC")
    fun getRecentBooksWithProgress(): PagingSource<Int, BookAndProgress>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookWithBookmarks(
        id: Long,
    ): Flow<BookWithBookmarks?>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookWithHighlights(
        id: Long,
    ): Flow<BookWithHighlights?>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookWithNotes(
        id: Long,
    ): Flow<BookWithNotes?>

    @Delete
    suspend fun deleteBook(
        book: Book,
    )
}
