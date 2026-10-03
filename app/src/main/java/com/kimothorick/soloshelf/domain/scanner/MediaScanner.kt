package com.kimothorick.soloshelf.domain.scanner

import android.content.Context
import android.net.Uri
import android.util.Log
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.AudioProgress
import com.kimothorick.soloshelf.data.models.AudiobookDetails
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Track
import org.readium.r2.shared.util.mediatype.MediaType

class MediaScanner(
    private val context: Context,
    private val bookRepository: BookRepository,
    private val chapterParser: ChapterParser = ChapterParser(
        context = context,
        mediaAnalyzer = MediaAnalyzer(context),
    ),
    private val bookParser: BookParser = BookParser(
        coverScanner = CoverScanner(context),
    ),
) {
    suspend fun scanAndPersist(
        targetUris: List<Uri>,
    ): List<Book> {
        val scannedBooks = mutableListOf<Book>()

        for (uri in targetUris) {
            try {
                val chapters = chapterParser.parseDirectory(uri)
                val bookContent = bookParser.parseBook(uri, chapters)
                if (bookContent != null) {
                    val book = persistBookContent(bookContent)
                    if (book != null) {
                        scannedBooks.add(book)
                    }
                }
            } catch (e: Exception) {
                Log.e("MediaScanner", "Error scanning URI $uri", e)
            }
        }

        return scannedBooks
    }

    private suspend fun persistBookContent(
        content: BookContent,
    ): Book? {
        val existingBook = bookRepository.getBookByFilePath(content.id.uriString)

        val totalDurationMs = content.chapters.sumOf { it.metadata.durationMs }

        val rawMediaType = content.chapters
            .firstOrNull()
            ?.metadata
            ?.mediaType
        val cleanFormat = rawMediaType?.substringAfter('/')?.uppercase()
            ?: content.id.uriString
                .substringAfterLast('.')
                .uppercase()
                .takeIf { it.isNotBlank() }
            ?: "AUDIOBOOK"

        val mediaTypeObj = rawMediaType?.let { MediaType(it) } ?: MediaType(cleanFormat.lowercase())

        val rawGenre = content.chapters
            .firstOrNull()
            ?.metadata
            ?.genre
        val genresList = rawGenre
            ?.split(',', ';', '/')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val bookEntity = Book(
            bookId = existingBook?.bookId ?: 0,
            filePath = content.id.uriString,
            title = content.title,
            author = content.author,
            format = cleanFormat,
            mediaType = mediaTypeObj,
            genres = genresList.ifEmpty { existingBook?.genres ?: emptyList() },
            publicationDate = content.chapters
                .firstOrNull()
                ?.metadata
                ?.year ?: existingBook?.publicationDate,
            coverPath = content.coverPath ?: existingBook?.coverPath,
            synopsis = content.synopsis ?: existingBook?.synopsis,
            isAudiobook = true,
        )

        val insertedId = bookRepository.insertBook(bookEntity)
        val finalBookId = if (existingBook != null) existingBook.bookId else insertedId

        if (finalBookId != -1L) {
            if (existingBook != null) {
                bookRepository.deleteTracksForBook(finalBookId)
                bookRepository.deleteAudiobookDetailsForBook(finalBookId)
            }

            val meta = content.chapters.firstOrNull()?.metadata
            val details = AudiobookDetails(
                bookId = finalBookId,
                totalDurationMs = totalDurationMs,
                narrator = meta?.narrator,
                bitrate = meta?.bitrate,
                samplingRate = meta?.samplingRate,
            )
            bookRepository.insertAudiobookDetails(details)

            val tracks = mutableListOf<Track>()
            var trackNum = 1
            for (chapter in content.chapters) {
                val marks = chapter.metadata.chapters
                if (marks.size > 1) {
                    for (i in marks.indices) {
                        val mark = marks[i]
                        val nextMarkStart = if (i + 1 < marks.size) marks[i + 1].startMs else chapter.metadata.durationMs
                        val durationMs = if (nextMarkStart > mark.startMs) nextMarkStart - mark.startMs else chapter.metadata.durationMs - mark.startMs
                        tracks.add(
                            Track(
                                bookId = finalBookId,
                                filePath = chapter.uri.toString(),
                                title = mark.name.ifBlank { "Chapter $trackNum" },
                                duration = (durationMs / 1000).toInt(),
                                trackNumber = trackNum++,
                                startMs = mark.startMs,
                                endMs = nextMarkStart,
                            ),
                        )
                    }
                } else {
                    val fallbackName = chapter.name
                        .substringBeforeLast('.')
                        .replace('_', ' ')
                        .replace('-', ' ')
                        .trim()
                    val title = marks.firstOrNull()?.name?.takeIf { it.isNotBlank() } ?: fallbackName
                    tracks.add(
                        Track(
                            bookId = finalBookId,
                            filePath = chapter.uri.toString(),
                            title = title,
                            duration = (chapter.metadata.durationMs / 1000).toInt(),
                            trackNumber = trackNum++,
                            startMs = 0L,
                            endMs = chapter.metadata.durationMs,
                        ),
                    )
                }
            }
            bookRepository.insertTracks(tracks)

            val initialTrack = tracks.firstOrNull()
            if (initialTrack != null && existingBook == null) {
                val progress = AudioProgress(
                    bookId = finalBookId,
                    currentTrackId = initialTrack.trackId,
                    currentOffsetMs = content.positionInChapterMs,
                )
                bookRepository.insertAudioProgress(progress)
            }

            return bookRepository.getBookByIdOnce(finalBookId)
        }
        return null
    }
}
