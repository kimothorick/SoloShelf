package com.kimothorick.soloshelf.data

import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress

object TestData {
    val BOOK_1 = Book(bookId = 1, filePath = "/path/to/book.epub", title = "The Great Gatsby", author = "F. Scott Fitzgerald", format = "EPUB")
    val BOOK_2 = Book(bookId = 2, filePath = "/path/to/book2.epub", title = "To Kill a Mockingbird", author = "Harper Lee", format = "EPUB")
    val BOOK_3 = Book(bookId = 3, filePath = "/path/to/book3.pdf", title = "1984", author = "George Orwell", format = "PDF")
    val BOOK_4 = Book(bookId = 4, filePath = "/path/to/book4.m4b", title = "The Hobbit", author = "J.R.R. Tolkien", format = "M4B")

    val ALL_BOOKS = listOf(BOOK_2, BOOK_3, BOOK_4)

    val HIGHLIGHT_1 = Highlight(highlightId = 1, bookId = 1, startLocation = "start", endLocation = "end", color = "yellow")
    val NOTE_1 = Note(noteId = 1, bookId = 1, location = "location", noteText = "This is a note.")
    val BOOKMARK_1 = Bookmark(bookmarkId = 1, bookId = 1, location = "location", title = "Chapter 1")

    val PROGRESS_1 = ReadingProgress(bookId = 1, lastPosition = "pos1", readPercentage = 0.5f, lastAccessed = 100L)
    val PROGRESS_2 = ReadingProgress(bookId = 2, lastPosition = "pos2", readPercentage = 0.25f, lastAccessed = 200L)
}
