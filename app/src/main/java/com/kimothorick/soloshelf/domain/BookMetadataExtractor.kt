package com.kimothorick.soloshelf.domain

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.kimothorick.soloshelf.data.models.Book
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

/**
 * Service to extract and unify metadata from EPUB and PDF books using Readium.
 */
class BookMetadataExtractor @Inject constructor(
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever,
    @ApplicationContext private val context: Context,
) {
    private val epubParser = EpubMetadataParser()
    private val pdfParser = PdfMetadataParser()

    suspend fun extractMetadata(
        book: Book,
    ): Book =
        withContext(Dispatchers.IO) {
            Log.d("BookMetadataExtractor", "extractMetadata: starting for ${book.filePath}")
            try {
                val url = AbsoluteUrl(book.filePath) ?: return@withContext book
                val asset = assetRetriever.retrieve(url).getOrElse {
                    Log.e("BookMetadataExtractor", "Failed to retrieve asset: $it")
                    return@withContext book
                }

                publicationRetriever
                    .retrieve(asset)
                    .getOrElse {
                        Log.e("BookMetadataExtractor", "Failed to retrieve publication for ${book.filePath}: $it")
                        return@withContext book.copy(mediaType = asset.format.mediaType)
                    }.let { publication ->
                        val parser = when {
                            asset.format.mediaType.matches(org.readium.r2.shared.util.mediatype.MediaType.EPUB) -> epubParser
                            asset.format.mediaType.matches(org.readium.r2.shared.util.mediatype.MediaType.PDF) -> pdfParser
                            else -> {
                                Log.w("BookMetadataExtractor", "Unsupported format: ${asset.format.mediaType}.")
                                return@withContext book.copy(mediaType = asset.format.mediaType)
                            }
                        }

                        var enriched = parser.parse(
                            publication,
                            book.copy(mediaType = asset.format.mediaType),
                        )

                        // Extract cover if missing using Readium's cover service
                        if (enriched.coverPath.isNullOrBlank()) {
                            publication.cover()?.let { bitmap ->
                                enriched = enriched.copy(coverPath = saveCoverImage(bitmap))
                            }
                        }

                        enriched
                    }
            } catch (e: Exception) {
                Log.e("BookMetadataExtractor", "Error extracting metadata", e)
                book
            }
        }

    suspend fun getTableOfContents(
        book: Book,
    ): List<Link> {
        return try {
            val url = AbsoluteUrl(book.filePath) ?: return emptyList()
            val asset = assetRetriever.retrieve(url).getOrElse { return emptyList() }
            publicationRetriever.retrieve(asset).getOrElse { return emptyList() }.tableOfContents
        } catch (e: Exception) {
            Log.e("BookMetadataExtractor", "Error getting table of contents", e)
            emptyList()
        }
    }

    private fun saveCoverImage(
        bitmap: Bitmap,
    ): String? =
        try {
            val filename = "cover_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
            val coverFile = File(file, filename)

            FileOutputStream(coverFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            coverFile.absolutePath
        } catch (e: Exception) {
            Log.e("BookMetadataExtractor", "Error saving cover bitmap", e)
            null
        }
}
