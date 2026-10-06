package com.kimothorick.soloshelf.ui.tooling

import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.domain.models.LibraryItem
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.util.Url

object PreviewData {
    val sampleLink = Link(
        href = Url("chapter1")!!,
        title = "Chapter 1: The Beginning"
    )

    val sampleLinkWithChildren = Link(
        href = Url("chapter2")!!,
        title = "Chapter 2: The Middle",
        children = listOf(
            Link(href = Url("chapter2.1")!!, title = "Section 2.1"),
            Link(href = Url("chapter2.2")!!, title = "Section 2.2")
        )
    )

    val sampleToc = listOf(
        sampleLink,
        sampleLinkWithChildren,
        Link(href = Url("chapter3")!!, title = "Chapter 3: The End")
    )

    val sampleBook = Book(
        bookId = 1,
        title = "The Great Gatsby",
        author = "F. Scott Fitzgerald",
        format = "epub",
        publisher = "Scribner",
        language = "en",
        publicationDate = "1925",
        isbn = "978-0743273565",
        synopsis = "The Great Gatsby is a 1925 novel by American writer F. Scott Fitzgerald. Set in the Jazz Age on Long Island, near New York City, the novel depicts first-person narrator Nick Carraway's interactions with mysterious millionaire Jay Gatsby and Gatsby's obsession to reunite with his former lover, Daisy Buchanan.",
        genres = listOf("Fiction", "Classic", "Literature"),
        filePath = "/storage/emulated/0/Books/The Great Gatsby.epub",
    )

    val sampleBooks = listOf(
        Book(title = "The Fellowship of the Ring", author = "J.R.R Tolkien", format = "EPUB", filePath = ""),
        Book(title = "The Two Towers", author = "J.R.R Tolkien", format = "EPUB", filePath = ""),
        Book(title = "The Return of the King", author = "J.R.R Tolkien", format = "EPUB", filePath = ""),
    )

    val sampleCollections = listOf(
        CollectionWithBooks(Collection(name = "Fantasy"), sampleBooks),
        CollectionWithBooks(Collection(name = "Sci-Fi"), sampleBooks.take(2)),
        CollectionWithBooks(Collection(name = "Classics"), sampleBooks.take(1)),
        CollectionWithBooks(Collection(name = "To Read"), emptyList()),
        CollectionWithBooks(Collection(name = "Completed"), sampleBooks),
        CollectionWithBooks(Collection(name = "Favorites"), sampleBooks),
    )

    val sampleBookAndProgress = BookAndProgress(
        book = sampleBook,
        progress = ReadingProgress(
            bookId = sampleBook.bookId,
            lastPosition = "10",
            readPercentage = 25f,
            lastAccessed = System.currentTimeMillis()
        ),
        ebookDetails = null,
    )

    val sampleBooksAndProgress = sampleBooks.mapIndexed { index, book ->
        BookAndProgress(
            book = book.copy(bookId = index.toLong() + 2),
            progress = ReadingProgress(
                bookId = index.toLong() + 2,
                lastPosition = "0",
                readPercentage = 10f * (index + 1),
                lastAccessed = System.currentTimeMillis()
            ),
            ebookDetails = null,
        )
    }

    val sampleLibraryEbooks = sampleBooksAndProgress.map {
        LibraryItem.Ebook(
            id = it.book.bookId,
            title = it.book.title,
            author = it.book.author,
            coverPath = it.book.coverPath,
            filePath = it.book.filePath,
            mediaType = null,
            lastReadLocatorJson = it.progress?.lastPosition,
            lastAccessed = it.progress?.lastAccessed,
            progressPercentage = it.progress?.readPercentage ?: 0f,
            pageCount = 350,
        )
    }
}
