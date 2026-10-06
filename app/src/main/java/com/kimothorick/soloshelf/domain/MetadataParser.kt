package com.kimothorick.soloshelf.domain

import com.kimothorick.soloshelf.data.models.Book
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication

interface MetadataParser {
    suspend fun parse(publication: Publication, book: Book): Book
}

class EpubMetadataParser : MetadataParser {
    override suspend fun parse(publication: Publication, book: Book): Book {
        val metadata = publication.metadata
        
        return book.copy(
            title = metadata.title.takeIf { !it.isNullOrBlank() } ?: book.title,
            author = metadata.authorName.takeIf { it.isNotBlank() } ?: book.author,
            synopsis = metadata.description.takeIf { !it.isNullOrBlank() } ?: book.synopsis,
            language = metadata.languages.firstOrNull() ?: book.language,
            publicationDate = metadata.published?.toString() ?: book.publicationDate,
            genres = if (metadata.subjects.isNotEmpty()) metadata.subjects.map { it.name } else book.genres,
            publisher = metadata.publishers.firstOrNull()?.name ?: book.publisher,
            isbn = metadata.identifier ?: book.isbn
        )
    }
}

class PdfMetadataParser : MetadataParser {
    override suspend fun parse(publication: Publication, book: Book): Book {
        val metadata = publication.metadata
        return book.copy(
            title = metadata.title.takeIf { !it.isNullOrBlank() } ?: book.title,
            author = metadata.authorName.takeIf { it.isNotBlank() } ?: book.author,
            synopsis = metadata.description.takeIf { !it.isNullOrBlank() } ?: book.synopsis,
            language = metadata.languages.firstOrNull() ?: book.language,
            publicationDate = metadata.published?.toString() ?: book.publicationDate,
            genres = if (metadata.subjects.isNotEmpty()) metadata.subjects.map { it.name } else book.genres,
            publisher = metadata.publishers.firstOrNull()?.name ?: book.publisher,
            isbn = metadata.identifier ?: book.isbn
        )
    }
}

internal val Metadata.authorName: String
    get() {
        val names = (authors + artists).map { it.name }.distinct()
        return names.joinToString(", ").trim()
    }
