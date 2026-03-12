package com.kimothorick.soloshelf.domain

import com.kimothorick.soloshelf.data.models.Book
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.positions

interface MetadataParser {
    suspend fun parse(publication: Publication, book: Book): Book
}

class EpubMetadataParser : MetadataParser {
    override suspend fun parse(publication: Publication, book: Book): Book {
        val metadata = publication.metadata
        val positions = try { publication.positions() } catch (e: Exception) { emptyList() }
        
        return book.copy(
            title = metadata.title ?: book.title,
            author = metadata.authors.firstOrNull()?.name ?: book.author,
            synopsis = metadata.description ?: book.synopsis,
            language = metadata.languages.firstOrNull() ?: book.language,
            publicationDate = metadata.published?.toString() ?: book.publicationDate,
            genres = if (metadata.subjects.isNotEmpty()) metadata.subjects.map { it.name } else book.genres,
            // Fallback to positions if numberOfPages is missing (standard for EPUB 2.x)
            totalPages = if (book.totalPages == 0) (metadata.numberOfPages ?: positions.size) else book.totalPages
        )
    }
}

class PdfMetadataParser : MetadataParser {
    override suspend fun parse(publication: Publication, book: Book): Book {
        val metadata = publication.metadata
        return book.copy(
            title = metadata.title ?: book.title,
            author = metadata.authors.firstOrNull()?.name ?: book.author,
            synopsis = metadata.description ?: book.synopsis,
            language = metadata.languages.firstOrNull() ?: book.language,
            publicationDate = metadata.published?.toString() ?: book.publicationDate,
            genres = if (metadata.subjects.isNotEmpty()) metadata.subjects.map { it.name } else book.genres,
            totalPages = if (book.totalPages == 0) (metadata.numberOfPages ?: 0) else book.totalPages
        )
    }
}

class AudiobookMetadataParser : MetadataParser {
    override suspend fun parse(publication: Publication, book: Book): Book {
        val metadata = publication.metadata
        return book.copy(
            title = metadata.title ?: book.title,
            author = metadata.authors.firstOrNull()?.name ?: book.author,
            synopsis = metadata.description ?: book.synopsis,
            language = metadata.languages.firstOrNull() ?: book.language,
            publicationDate = metadata.published?.toString() ?: book.publicationDate,
            genres = if (metadata.subjects.isNotEmpty()) metadata.subjects.map { it.name } else book.genres,
            // For audiobooks, we map duration (seconds) to the totalPages field
            totalPages = if (book.totalPages == 0) (metadata.duration?.toInt() ?: 0) else book.totalPages
        )
    }
}
