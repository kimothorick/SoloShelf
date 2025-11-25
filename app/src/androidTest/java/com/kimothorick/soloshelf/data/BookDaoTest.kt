package com.kimothorick.soloshelf.data

import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kimothorick.soloshelf.data.TestData.ALL_BOOKS
import com.kimothorick.soloshelf.data.TestData.BOOKMARK_1
import com.kimothorick.soloshelf.data.TestData.BOOK_1
import com.kimothorick.soloshelf.data.TestData.BOOK_2
import com.kimothorick.soloshelf.data.TestData.HIGHLIGHT_1
import com.kimothorick.soloshelf.data.TestData.NOTE_1
import com.kimothorick.soloshelf.data.TestData.PROGRESS_1
import com.kimothorick.soloshelf.data.TestData.PROGRESS_2
import com.kimothorick.soloshelf.data.daos.BookDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BookDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var bookDao: BookDao

    @Inject
    lateinit var db: BookDatabase

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetBook() =
        runTest {
            bookDao.insertBook(BOOK_1)

            val pagingSource = bookDao.getBooks()
            val loadResult = pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 1,
                    placeholdersEnabled = false,
                ),
            )

            val data = (loadResult as PagingSource.LoadResult.Page).data
            println("Fetched Book: ${data.firstOrNull()}")
            assertEquals(1, data.size)
            assertEquals(BOOK_1, data[0])
        }

    @Test
    @Throws(Exception::class)
    fun insertMultipleAndLog() =
        runTest {
            ALL_BOOKS.forEach { bookDao.insertBook(it) }

            val pagingSource = bookDao.getBooks()
            val loadResult = pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 3,
                    placeholdersEnabled = false,
                ),
            )

            val data = (loadResult as PagingSource.LoadResult.Page).data
            println("Fetched Books:")
            data.forEach { println(it) }

            assertEquals(ALL_BOOKS.size, data.size)
        }

    @Test
    fun insertAndDeleteBook() =
        runTest {
            bookDao.insertBook(BOOK_1)
            bookDao.deleteBook(BOOK_1)

            val pagingSource = bookDao.getBooks()
            val loadResult = pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 1,
                    placeholdersEnabled = false,
                ),
            )
            val data = (loadResult as PagingSource.LoadResult.Page).data
            assertEquals(0, data.size)
        }

    @Test
    fun insertAndGetHighlight() =
        runTest {
            bookDao.insertBook(BOOK_1)
            bookDao.insertHighlight(HIGHLIGHT_1)

            val bookWithHighlights = bookDao.getBookWithHighlights(BOOK_1.bookId).first()

            assertNotNull(bookWithHighlights)
            assertEquals(BOOK_1, bookWithHighlights!!.book)
            assertEquals(1, bookWithHighlights.highlights.size)
            assertEquals(HIGHLIGHT_1, bookWithHighlights.highlights[0])
        }

    @Test
    fun insertAndGetNote() =
        runTest {
            bookDao.insertBook(BOOK_1)
            bookDao.insertNote(NOTE_1)

            val bookWithNotes = bookDao.getBookWithNotes(BOOK_1.bookId).first()

            assertNotNull(bookWithNotes)
            assertEquals(BOOK_1, bookWithNotes!!.book)
            assertEquals(1, bookWithNotes.notes.size)
            assertEquals(NOTE_1, bookWithNotes.notes[0])
        }

    @Test
    fun insertAndGetBookmark() =
        runTest {
            bookDao.insertBook(BOOK_1)
            bookDao.insertBookmark(BOOKMARK_1)

            val bookWithBookmarks = bookDao.getBookWithBookmarks(BOOK_1.bookId).first()

            assertNotNull(bookWithBookmarks)
            assertEquals(BOOK_1, bookWithBookmarks!!.book)
            assertEquals(1, bookWithBookmarks.bookmarks.size)
            assertEquals(BOOKMARK_1, bookWithBookmarks.bookmarks[0])
        }

    @Test
    fun insertAndGetRecentBookWithProgress() =
        runTest {
            bookDao.insertBook(BOOK_1)
            bookDao.insertReadingProgress(PROGRESS_1)
            bookDao.insertBook(BOOK_2)
            bookDao.insertReadingProgress(PROGRESS_2)

            val pagingSource = bookDao.getRecentBooksWithProgress()
            val loadResult = pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 2,
                    placeholdersEnabled = false,
                ),
            )

            val data = (loadResult as PagingSource.LoadResult.Page).data
            assertEquals(2, data.size)
            assertEquals(BOOK_2, data[0].book) // Should be the most recent
            assertEquals(PROGRESS_2, data[0].progress)
        }

    @Test
    fun deleteBook_deletesAssociatedData() =
        runTest {
            bookDao.insertBook(BOOK_1)
            bookDao.insertBookmark(BOOKMARK_1)
            bookDao.insertNote(NOTE_1)
            bookDao.insertHighlight(HIGHLIGHT_1)
            bookDao.insertReadingProgress(PROGRESS_1)

            bookDao.deleteBook(BOOK_1)

            assertNull(bookDao.getBookWithBookmarks(BOOK_1.bookId).firstOrNull())
            assertNull(bookDao.getBookWithNotes(BOOK_1.bookId).firstOrNull())
            assertNull(bookDao.getBookWithHighlights(BOOK_1.bookId).firstOrNull())

            val pagingSource = bookDao.getRecentBooksWithProgress()
            val loadResult = pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 1,
                    placeholdersEnabled = false,
                ),
            )
            val data = (loadResult as PagingSource.LoadResult.Page).data
            assertEquals(0, data.size)
        }
}
