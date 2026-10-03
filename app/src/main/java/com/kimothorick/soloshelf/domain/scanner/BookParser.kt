package com.kimothorick.soloshelf.domain.scanner

import android.net.Uri

class BookParser(
    private val coverScanner: CoverScanner,
) {

    fun parseBook(
        rootUri: Uri,
        chapters: List<ChapterRecord>,
    ): BookContent? {
        if (chapters.isEmpty()) return null

        val bookId = BookId(rootUri.toString())
        val firstChapter = chapters.first()
        val meta = firstChapter.metadata

        val bookTitle = meta.album
            ?.takeIf { it.isNotBlank() }
            ?: (if (chapters.size == 1) meta.title?.takeIf { it.isNotBlank() } else null)
            ?: cleanNameFromUri(rootUri, firstChapter.name)

        val author = meta.artist
            ?.takeIf { it.isNotBlank() }
            ?: "Unknown Author"

        val coverPath = coverScanner.scanCover(rootUri, chapters)
        val fileSynopsis = coverScanner.scanSynopsis(rootUri)
        val synopsis = fileSynopsis ?: meta.synopsis

        return BookContent(
            id = bookId,
            title = bookTitle,
            author = author,
            coverPath = coverPath,
            synopsis = synopsis,
            chapters = chapters,
            currentChapterId = firstChapter.id,
            positionInChapterMs = 0L,
            isActive = true,
        )
    }

    private fun cleanNameFromUri(uri: Uri, fallbackName: String): String {
        val segment = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringAfterLast(':')
            ?.substringBeforeLast('.')
            ?.replace('_', ' ')
            ?.replace('-', ' ')
            ?.trim()

        return if (!segment.isNullOrBlank() && segment != "primary") {
            segment
        } else {
            fallbackName.substringBeforeLast('.')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
        }
    }
}
