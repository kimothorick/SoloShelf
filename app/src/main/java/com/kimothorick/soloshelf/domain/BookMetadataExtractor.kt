package com.kimothorick.soloshelf.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.kimothorick.soloshelf.data.models.AudiobookDetails
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Track
import com.kimothorick.soloshelf.util.isAudiobook
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Manifest
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
 * Service to extract and unify metadata from various book formats using Readium.
 * Delegates to specific parsers based on book format.
 */
@UnstableApi
class BookMetadataExtractor @Inject constructor(
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever,
    @ApplicationContext private val context: Context,
) {
    private val epubParser = EpubMetadataParser()
    private val pdfParser = PdfMetadataParser()
    private val audiobookParser = AudiobookMetadataParser()

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

                Log.d("BookMetadataExtractor", "Format detected: ${asset.format.mediaType} (${asset.format.fileExtension.value})")

                publicationRetriever
                    .retrieve(asset)
                    .getOrElse {
                        Log.e("BookMetadataExtractor", "Failed to retrieve publication for ${book.filePath}: $it")
                        return@withContext book.copy(mediaType = asset.format.mediaType, isAudiobook = asset.format.isAudiobook)
                    }.let { publication ->
                        val isAudio = asset.format.isAudiobook || publication.conformsTo(Publication.Profile.AUDIOBOOK)
                        Log.d(
                            "BookMetadataExtractor",
                            "Readium Publication opened for ${book.filePath}\n" +
                                "  - Format: ${asset.format.fileExtension.value}\n" +
                                "  - MediaType: ${asset.format.mediaType}\n" +
                                "  - Title: ${publication.metadata.title}\n" +
                                "  - Author: ${publication.metadata.authorName}\n" +
                                "  - Identifier: ${publication.metadata.identifier}",
                        )

                        val parser = when {
                            asset.format.mediaType.matches(org.readium.r2.shared.util.mediatype.MediaType.EPUB) -> {
                                epubParser
                            }

                            asset.format.mediaType.matches(org.readium.r2.shared.util.mediatype.MediaType.PDF) -> {
                                pdfParser
                            }

                            isAudio -> {
                                audiobookParser
                            }

                            else -> {
                                Log.w(
                                    "BookMetadataExtractor",
                                    "Unsupported format: ${asset.format.mediaType}. Skipping metadata extraction.",
                                )
                                return@withContext book.copy(mediaType = asset.format.mediaType)
                            }
                        }

                        var enriched = parser.parse(
                            publication,
                            book.copy(isAudiobook = isAudio, mediaType = asset.format.mediaType),
                        )

                        if (isAudio) {
                            val mmr = MediaMetadataRetriever()
                            try {
                                mmr.setDataSource(context, book.filePath.toUri())
                                val mmrTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                val mmrArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                    ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                                val mmrAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

                                val finalTitle = mmrAlbum?.takeIf { it.isNotBlank() }
                                    ?: mmrTitle?.takeIf { it.isNotBlank() }
                                    ?: enriched.title

                                val finalAuthor = mmrArtist?.takeIf { it.isNotBlank() } ?: enriched.author

                                enriched = enriched.copy(
                                    title = if (finalTitle.isBlank() || finalTitle.contains("primary:") || finalTitle == "New Book" || finalTitle == "New Audiobook") enriched.title else finalTitle,
                                    author = finalAuthor.ifBlank { "Unknown Author" },
                                )

                                if (enriched.synopsis.isNullOrBlank()) {
                                    val mmrComment = mmr.extractMetadata(3)?.takeIf { it.isNotBlank() }
                                    if (!mmrComment.isNullOrBlank()) {
                                        enriched = enriched.copy(synopsis = mmrComment)
                                    }
                                }

                                if (enriched.coverPath.isNullOrBlank()) {
                                    mmr.embeddedPicture?.let { bytes ->
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { bmp ->
                                            enriched = enriched.copy(coverPath = saveCoverImage(bmp))
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("BookMetadataExtractor", "MediaMetadataRetriever failed for single audio asset", e)
                            } finally {
                                try {
                                    mmr.release()
                                } catch (ex: Exception) {
                                }
                            }
                        }

                        // Extract cover if missing using Readium's cover service
                        if (enriched.coverPath.isNullOrBlank()) {
                            publication.cover()?.let { bitmap ->
                                enriched = enriched.copy(coverPath = saveCoverImage(bitmap))
                            }
                        }

                        Log.d(
                            "BookMetadataExtractor",
                            "Enriched book: ${enriched.title}, coverPath: ${enriched.coverPath}",
                        )
                        enriched
                    }
            } catch (e: Exception) {
                Log.e("BookMetadataExtractor", "Error extracting metadata", e)
                book
            }
        }

    suspend fun extractAudiobookMetadata(
        audioUris: List<Uri>,
        imageUris: List<Uri>,
        otherUris: List<Uri> = emptyList(),
        book: Book,
    ): Triple<Book, AudiobookDetails, List<Track>> =
        withContext(Dispatchers.IO) {
            Log.d("BookMetadataExtractor", "Extracting audiobook metadata for ${book.title}, track count: ${audioUris.size}, other count: ${otherUris.size}")
            var totalDuration = 0L
            val tracks = mutableListOf<Track>()
            var mainBook = book
            var narrator: String? = null
            var coverPath: String? = null

            // 1. Try to find and parse manifest.json if present
            val manifest = otherUris
                .find { uri ->
                    val type = context.contentResolver.getType(uri) ?: ""
                    val name = uri.lastPathSegment?.lowercase() ?: uri.toString().lowercase()
                    type == "application/audiobook+json" || name.contains("manifest.json") || name.endsWith(".json")
                }?.let { manifestUri ->
                    try {
                        context.contentResolver.openInputStream(manifestUri)?.bufferedReader()?.use { it.readText() }?.let { jsonString ->
                            if (jsonString.isNotBlank()) {
                                val jsonObject = JSONObject(jsonString)
                                val m = Manifest.fromJSON(jsonObject)
                                if (m != null) {
                                    val pub = Publication(m)
                                    mainBook = audiobookParser.parse(pub, mainBook)
                                    narrator = m.metadata.narratorName.takeIf { it.isNotBlank() }

                                    // Map publication metadata if missing
                                    if (mainBook.synopsis.isNullOrBlank()) {
                                        mainBook = mainBook.copy(synopsis = m.metadata.description)
                                    }

                                    Log.d("BookMetadataExtractor", "Enriched from manifest.json: ${mainBook.title}, synopsis length: ${mainBook.synopsis?.length}")
                                    m
                                } else {
                                    // Fallback for non-Readium JSON metadata (e.g. Audiobookshelf)
                                    Log.d("BookMetadataExtractor", "Manifest.fromJSON returned null, trying fallback parsing")
                                    val title = jsonObject.optString("title").takeIf { it.isNotBlank() }
                                    val author = (jsonObject.optString("author") ?: jsonObject.optString("artist")).takeIf { it.isNotBlank() }
                                    val synopsis = (jsonObject.optString("description") ?: jsonObject.optString("summary") ?: jsonObject.optString("synopsis")).takeIf { it.isNotBlank() }
                                    val publisher = jsonObject.optString("publisher").takeIf { it.isNotBlank() }

                                    mainBook = mainBook.copy(
                                        title = title ?: mainBook.title,
                                        author = author ?: mainBook.author,
                                        synopsis = synopsis ?: mainBook.synopsis,
                                        publisher = publisher ?: mainBook.publisher,
                                    )
                                    narrator = jsonObject.optString("narrator").takeIf { it.isNotBlank() }
                                    null
                                }
                            } else {
                                null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BookMetadataExtractor", "Failed to parse manifest.json", e)
                        null
                    }
                }

            // 2. Try to find cover from provided images first, prioritizing common names
            val prioritizedImage = imageUris.find { uri ->
                val name = uri.lastPathSegment?.lowercase() ?: ""
                name.contains("cover") || name.contains("folder") || name.contains("front") || name.contains("album")
            } ?: imageUris.firstOrNull()

            prioritizedImage?.let { imageUri ->
                Log.d("BookMetadataExtractor", "Saving cover image from $imageUri")
                coverPath = saveImageFromUri(imageUri)
            }

            val processedUris = mutableSetOf<Uri>()

            // 3. Process tracks from manifest reading order first if available
            manifest?.readingOrder?.forEachIndexed { index, link ->
                val href = link.href.toString().lowercase()
                val matchingUri = audioUris.find { uri ->
                    val uriName = uri.lastPathSegment?.lowercase() ?: ""
                    href.endsWith(uriName) || uriName.endsWith(href) || href.contains(uriName) || uriName.contains(href)
                }

                if (matchingUri != null && matchingUri !in processedUris) {
                    Log.d("BookMetadataExtractor", "Processing manifest track: $matchingUri")
                    processedUris.add(matchingUri)

                    val (track, durationMs) = processAudioTrack(
                        context = context,
                        uri = matchingUri,
                        bookId = book.bookId,
                        trackNumber = tracks.size + 1,
                        preferredTitle = link.title,
                    )

                    totalDuration += durationMs
                    tracks.add(track)

                    // Extract additional metadata from the first track if needed
                    if (tracks.size == 1) {
                        // Use MMR for basic fields
                        val mmr = MediaMetadataRetriever()
                        try {
                            mmr.setDataSource(context, matchingUri)
                            val mmrArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                            val mmrAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

                            if (mainBook.author.isBlank() || mainBook.author == "Unknown Author") {
                                if (!mmrArtist.isNullOrBlank()) mainBook = mainBook.copy(author = mmrArtist)
                            }
                            if (mainBook.title == book.title || mainBook.title == "New Audiobook" || mainBook.title.contains("primary:")) {
                                if (!mmrAlbum.isNullOrBlank()) mainBook = mainBook.copy(title = mmrAlbum)
                            }
                        } catch (e: Exception) {
                            Log.e("BookMetadataExtractor", "MMR fallback failed", e)
                        } finally {
                            mmr.release()
                        }
                    }

                    if (coverPath == null) {
                        coverPath = extractCoverFromTrack(context, matchingUri)
                    }
                }
            }

            // 4. Process any remaining audio files not in the manifest
            audioUris.filter { it !in processedUris }.forEach { uri ->
                Log.d("BookMetadataExtractor", "Processing loose track: $uri")
                val (track, durationMs) = processAudioTrack(
                    context = context,
                    uri = uri,
                    bookId = book.bookId,
                    trackNumber = tracks.size + 1,
                )

                totalDuration += durationMs
                tracks.add(track)

                if (tracks.size == 1) {
                    // MMR fallback
                    val mmr = MediaMetadataRetriever()
                    try {
                        mmr.setDataSource(context, uri)
                        val mmrArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                            ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                        val mmrAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

                        if (mainBook.author.isBlank() || mainBook.author == "Unknown Author") {
                            if (!mmrArtist.isNullOrBlank()) mainBook = mainBook.copy(author = mmrArtist)
                        }
                        if (mainBook.title == book.title || mainBook.title == "New Audiobook" || mainBook.title.contains("primary:")) {
                            if (!mmrAlbum.isNullOrBlank()) mainBook = mainBook.copy(title = mmrAlbum)
                        }
                    } catch (e: Exception) {
                        Log.e("BookMetadataExtractor", "MMR fallback failed", e)
                    } finally {
                        mmr.release()
                    }
                }

                if (coverPath == null) {
                    coverPath = extractCoverFromTrack(context, uri)
                }
            }

            Log.d("BookMetadataExtractor", "Audiobook extraction complete: duration: $totalDuration, cover: $coverPath")

            // If it's a single file, and we only found one track, try to find internal chapters (Table of Contents)
            if (audioUris.size == 1 && tracks.size == 1) {
                try {
                    val url = AbsoluteUrl(audioUris[0].toString())
                    if (url != null) {
                        val asset = assetRetriever.retrieve(url).getOrNull()
                        if (asset != null) {
                            publicationRetriever.retrieve(asset).onSuccess { pub ->
                                if (pub.tableOfContents.isNotEmpty()) {
                                    Log.d("BookMetadataExtractor", "Found ${pub.tableOfContents.size} internal chapters in file")
                                    tracks.clear()
                                    pub.tableOfContents.forEachIndexed { index, link ->
                                        val href = link.href.toString()
                                        val playablePath = if (href.startsWith("content://") || href.startsWith("file://") || href.startsWith("http://") || href.startsWith("https://")) {
                                            href
                                        } else {
                                            audioUris[0].toString()
                                        }
                                        tracks.add(
                                            Track(
                                                bookId = book.bookId,
                                                filePath = playablePath,
                                                title = link.title ?: "Chapter ${index + 1}",
                                                duration = (link.duration ?: 0.0).toInt(),
                                                trackNumber = index + 1,
                                            ),
                                        )
                                    }
                                }
                                if (mainBook.synopsis.isNullOrBlank()) {
                                    mainBook = mainBook.copy(synopsis = pub.metadata.description)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BookMetadataExtractor", "Failed to probe for internal chapters", e)
                }
            }

            // Final title cleanup if it still looks like a path
            if (mainBook.title.contains("primary:") || mainBook.title.contains("/")) {
                mainBook.filePath.toUri().lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')?.substringBeforeLast('.')?.let { cleanTitle ->
                    mainBook = mainBook.copy(title = cleanTitle)
                }
            }

            if (mainBook.author.isBlank()) {
                mainBook = mainBook.copy(author = "Unknown Author")
            }

            val details = AudiobookDetails(
                bookId = book.bookId,
                narrator = narrator,
                totalDurationMs = totalDuration,
            )

            Triple(mainBook.copy(isAudiobook = true, coverPath = coverPath ?: mainBook.coverPath), details, tracks)
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

    private fun processAudioTrack(
        context: Context,
        uri: Uri,
        bookId: Long,
        trackNumber: Int,
        preferredTitle: String? = null,
    ): Pair<Track, Long> {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val mmrTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)

            val finalTitle = preferredTitle?.takeIf { it.isNotBlank() }
                ?: mmrTitle?.takeIf { it.isNotBlank() }
                ?: uri.lastPathSegment?.substringBeforeLast('.')
                ?: "Track $trackNumber"

            val track = Track(
                bookId = bookId,
                filePath = uri.toString(),
                title = finalTitle,
                duration = (durationMs / 1000).toInt(),
                trackNumber = trackNumber,
            )
            Pair(track, durationMs)
        } catch (e: Exception) {
            Log.e("BookMetadataExtractor", "Error processing audio track $uri", e)
            Pair(Track(bookId = bookId, filePath = uri.toString(), title = "Track $trackNumber", trackNumber = trackNumber), 0L)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
            }
        }
    }

    private fun extractCoverFromTrack(
        context: Context,
        uri: Uri,
    ): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture?.let { bytes ->
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { bmp ->
                    saveCoverImage(bmp)
                }
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
            }
        }
    }

    fun saveCoverImage(
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

    private fun saveImageFromUri(
        uri: Uri,
    ): String? =
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                saveCoverImage(bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("BookMetadataExtractor", "Error saving cover from URI", e)
            null
        }
}
