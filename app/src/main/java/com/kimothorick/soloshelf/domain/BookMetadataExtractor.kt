package com.kimothorick.soloshelf.domain

import android.util.Log
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.util.Constants
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import javax.inject.Inject

/**
 * Service to extract and unify metadata from various book formats using Readium.
 * Delegates to specific parsers based on book format.
 */
class BookMetadataExtractor @Inject constructor(
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever,
) {
    private val epubParser = EpubMetadataParser()
    private val pdfParser = PdfMetadataParser()
    private val audiobookParser = AudiobookMetadataParser()

    suspend fun extractMetadata(book: Book): Book {
        return try {
            val url = AbsoluteUrl(book.filePath) ?: return book
            val asset = assetRetriever.retrieve(url).getOrElse {
                Log.e("BookMetadataExtractor", "Failed to retrieve asset: $it")
                return book
            }

            publicationRetriever.retrieve(asset).getOrElse {
                Log.e("BookMetadataExtractor", "Failed to retrieve publication: $it")
                return book
            }.let { publication ->
                val parser = when (book.format) {
                    in Constants.EPUB_EXTENSIONS -> epubParser
                    in Constants.PDF_EXTENSIONS -> pdfParser
                    in Constants.AUDIO_EXTENSIONS -> audiobookParser
                    else -> {
                        Log.w("BookMetadataExtractor", "Unsupported format: ${book.format}. Skipping metadata extraction.")
                        return book
                    }
                }
                parser.parse(publication, book)
            }
        } catch (e: Exception) {
            Log.e("BookMetadataExtractor", "Error extracting metadata", e)
            book
        }
    }
}
