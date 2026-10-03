package com.kimothorick.soloshelf.data.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.BookWithAudioDetails
import com.kimothorick.soloshelf.data.models.BookWithBookmarks
import com.kimothorick.soloshelf.data.models.BookWithEbookDetails
import com.kimothorick.soloshelf.data.models.BookWithHighlights
import com.kimothorick.soloshelf.data.models.BookWithNotes
import com.kimothorick.soloshelf.data.models.BookWithTracks
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.CollectionBookCrossRef
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.data.models.Track
import com.kimothorick.soloshelf.data.models.AudiobookDetails
import com.kimothorick.soloshelf.data.models.EbookDetails
import com.kimothorick.soloshelf.data.models.AudioProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(
        book: Book,
    ): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(
        track: Track,
    ): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(
        tracks: List<Track>,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudiobookDetails(details: AudiobookDetails): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEbookDetails(details: EbookDetails): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioProgress(progress: AudioProgress)

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

    @Query("SELECT * FROM books WHERE filePath = :filePath")
    suspend fun getBookByFilePath(filePath: String): Book?

    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookById(id: Long): Flow<Book?>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookAndProgressById(id: Long): Flow<BookAndProgress?>

    @Transaction
    @RawQuery(observedEntities = [Book::class, ReadingProgress::class, AudiobookDetails::class, EbookDetails::class, Track::class, AudioProgress::class])
    fun getAudiobooksWithProgress(query: SupportSQLiteQuery): PagingSource<Int, BookWithAudioDetails>

    @Transaction
    @RawQuery(observedEntities = [Book::class, ReadingProgress::class, EbookDetails::class])
    fun getEbooksWithProgress(query: SupportSQLiteQuery): PagingSource<Int, BookWithEbookDetails>

    @Transaction
    @RawQuery(observedEntities = [Book::class, ReadingProgress::class])
    fun getBooksWithProgress(query: SupportSQLiteQuery): PagingSource<Int, BookAndProgress>

    @Transaction
    @Query(
        """
        SELECT books.* FROM books 
        LEFT JOIN audio_progress ON books.bookId = audio_progress.bookId 
        WHERE isAudiobook = 1
        ORDER BY audio_progress.lastAccessed DESC
    """,
    )
    fun getRecentAudiobooks(): PagingSource<Int, BookWithAudioDetails>

    @Transaction
    @Query(
        """
        SELECT books.* FROM books 
        LEFT JOIN progress ON books.bookId = progress.bookId 
        WHERE isAudiobook = 0
        ORDER BY progress.lastAccessed DESC
    """,
    )
    fun getRecentEbooks(): PagingSource<Int, BookWithEbookDetails>

    @Query("SELECT * FROM books WHERE bookId = :id")
    suspend fun getBookByIdOnce(id: Long): Book?

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    suspend fun getBookAndProgressByIdOnce(id: Long): BookAndProgress?

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

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookWithTracks(
        id: Long,
    ): Flow<BookWithTracks?>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookWithAudioDetails(id: Long): Flow<BookWithAudioDetails?>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    suspend fun getBookWithAudioDetailsOnce(id: Long): BookWithAudioDetails?

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    fun getBookWithEbookDetails(id: Long): Flow<BookWithEbookDetails?>

    @Query("SELECT * FROM tracks WHERE bookId = :bookId ORDER BY trackNumber ASC")
    suspend fun getTracksForBook(bookId: Long): List<Track>

    @Query("DELETE FROM tracks WHERE bookId = :bookId")
    suspend fun deleteTracksForBook(bookId: Long)

    @Query("DELETE FROM audiobook_details WHERE bookId = :bookId")
    suspend fun deleteAudiobookDetailsForBook(bookId: Long)

    @Query("UPDATE books SET isAudiobook = 1 WHERE format IN (:audioExtensions)")
    suspend fun fixAudiobookFlags(audioExtensions: List<String>)

    @Delete
    suspend fun deleteBook(
        book: Book,
    )

    // Collections
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: Collection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionBookCrossRef(crossRef: CollectionBookCrossRef)

    @Transaction
    @Query("SELECT * FROM collections")
    fun getCollectionsWithBooks(): Flow<List<CollectionWithBooks>>

    @Delete
    suspend fun deleteCollection(collection: Collection)
}
