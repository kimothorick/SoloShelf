package com.kimothorick.soloshelf.domain.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.CommentFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import java.io.FileInputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MediaAnalyzer(
    private val context: Context,
) {
    private val mp4BoxParser = Mp4BoxParser(context)
    private val matroskaExtractor = MatroskaMetaDataExtractor(context)

    fun analyze(
        uri: Uri,
        fileName: String,
    ): Metadata {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val baseFileName = fileName
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()

        val chapters = mutableListOf<MarkData>()
        var embeddedSynopsis: String? = null

        try {
            if (extension in setOf("m4b", "m4a", "mp4", "aac")) {
                chapters.addAll(mp4BoxParser.extractChapters(uri))
                embeddedSynopsis = mp4BoxParser.extractEmbeddedSynopsis(uri) ?: extractMedia3Synopsis(uri)
            } else if (extension in setOf("mka", "mkv")) {
                chapters.addAll(matroskaExtractor.extractChapters(uri))
            }
        } catch (e: Exception) {
            Log.e("MediaAnalyzer", "Error parsing chapters/synopsis for $fileName", e)
        }

        val rawMeta = extractRawMetadata(uri)

        val finalChapters = if (chapters.isNotEmpty()) {
            chapters
        } else if (rawMeta.chapters.isNotEmpty()) {
            rawMeta.chapters
        } else {
            listOf(MarkData(name = baseFileName, startMs = 0L))
        }

        val rawBookTitle = rawMeta.album
            ?.takeIf { it.isNotBlank() }
            ?: rawMeta.title?.takeIf { it.isNotBlank() }
            ?: baseFileName

        val finalSynopsis = embeddedSynopsis ?: rawMeta.synopsis

        val meta = Metadata(
            title = rawBookTitle,
            artist = rawMeta.artist,
            album = rawMeta.album,
            duration = rawMeta.duration,
            genre = rawMeta.genre,
            narrator = rawMeta.narrator,
            series = rawMeta.series,
            part = rawMeta.part,
            synopsis = finalSynopsis,
            year = rawMeta.year,
            mediaType = rawMeta.mediaType,
            bitrate = rawMeta.bitrate,
            samplingRate = rawMeta.samplingRate,
            chapters = finalChapters,
        )

        Log.d(
            "MediaAnalyzer",
            "Analyzed Metadata for $fileName:\n" +
                "  - Title: ${meta.title}\n" +
                "  - Artist/Author: ${meta.artist}\n" +
                "  - Album: ${meta.album}\n" +
                "  - Duration: ${meta.duration}\n" +
                "  - Genre: ${meta.genre}\n" +
                "  - Narrator: ${meta.narrator}\n" +
                "  - Year: ${meta.year}\n" +
                "  - MediaType: ${meta.mediaType}\n" +
                "  - Synopsis: ${meta.synopsis?.take(100)}...\n" +
                "  - Chapters count: ${meta.chapters.size}",
        )

        return meta
    }

    @UnstableApi
    private fun extractMedia3Synopsis(
        uri: Uri,
    ): String? {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { fis ->
                    val buffer = ByteArray(65536)
                    val read = fis.read(buffer)
                    if (read > 10) {
                        if (buffer[0] == 'I'.code.toByte() && buffer[1] == 'D'.code.toByte() && buffer[2] == '3'.code.toByte()) {
                            try {
                                val id3Decoder = androidx.media3.extractor.metadata.id3
                                    .Id3Decoder()
                                val metadata = id3Decoder.decode(buffer, read)
                                if (metadata != null) {
                                    for (i in 0 until metadata.length()) {
                                        val entry = metadata.get(i)
                                        if (entry is CommentFrame && entry.text.isNotBlank()) {
                                            return entry.text
                                        }
                                        if (entry is TextInformationFrame &&
                                            (entry.id.equals("COMM", ignoreCase = true) || entry.id.equals("DES", ignoreCase = true) || entry.id.equals("TXXX", ignoreCase = true))
                                        ) {
                                            val valStr = entry.value
                                            if (valStr.isNotBlank()) return valStr
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MediaAnalyzer", "Error decoding ID3 metadata via Media3", e)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MediaAnalyzer", "Error in extractMedia3Synopsis", e)
        }
        return null
    }

    private fun extractRawMetadata(
        uri: Uri,
    ): Metadata {
        val retriever = MediaMetadataRetriever()
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var durationMs = 0L
        var genre: String? = null
        var narrator: String? = null
        var year: String? = null
        var mediaType: String? = null
        var bitrate: Int? = null
        var samplingRate: Int? = null
        var synopsis: String? = null

        try {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    retriever.setDataSource(pfd.fileDescriptor)
                } ?: retriever.setDataSource(context, uri)
            } catch (e: Exception) {
                retriever.setDataSource(context, uri)
            }

            dumpAllMetadataAsJson(retriever, uri)

            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)?.takeIf { it.isNotBlank() }
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }

            val mmrDurationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val extractorDurationMs = getAudioDurationMs(uri)
            durationMs = maxOf(mmrDurationMs, extractorDurationMs)

            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.takeIf { it.isNotBlank() }
            narrator = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)?.takeIf { it.isNotBlank() }
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.takeIf { it.isNotBlank() }
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)?.takeIf { it.isNotBlank() }
            mediaType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.takeIf { it.isNotBlank() }
            bitrate = retriever.extractMetadata(20)?.toIntOrNull()
            samplingRate = retriever.extractMetadata(38)?.toIntOrNull()

            synopsis = listOf(
                retriever.extractMetadata(13), // METADATA_KEY_COMMENT
                retriever.extractMetadata(11), // METADATA_KEY_WRITER
                retriever.extractMetadata(24),
                retriever.extractMetadata(30),
            ).firstOrNull { !it.isNullOrBlank() }
        } catch (e: Exception) {
            Log.e("MediaAnalyzer", "Retriever error for $uri", e)
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }

        if (durationMs <= 0L) {
            durationMs = getAudioDurationMs(uri)
        }

        return Metadata(
            title = title,
            artist = artist,
            album = album,
            duration = durationMs.milliseconds,
            genre = genre,
            narrator = narrator,
            series = null,
            part = null,
            synopsis = synopsis,
            year = year,
            mediaType = mediaType,
            bitrate = bitrate,
            samplingRate = samplingRate,
            chapters = emptyList(),
        )
    }

    private fun dumpAllMetadataAsJson(
        retriever: MediaMetadataRetriever,
        uri: Uri,
    ) {
        try {
            val json = org.json.JSONObject()
            for (key in 0..100) {
                try {
                    val value = retriever.extractMetadata(key)
                    if (!value.isNullOrBlank()) {
                        json.put("MMR_Key_$key", value)
                    }
                } catch (_: Exception) {
                }
            }
            Log.d("RawMetadataJSON", "=== RAW METADATA JSON FOR $uri ===\n${json.toString(2)}\n=======================================")
        } catch (e: Exception) {
            Log.e("MediaAnalyzer", "Error dumping raw metadata json", e)
        }
    }

    private fun getAudioDurationMs(
        uri: Uri,
    ): Long =
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val extractor = android.media.MediaExtractor()
                try {
                    extractor.setDataSource(pfd.fileDescriptor)
                    var maxDurationUs = 0L
                    for (i in 0 until extractor.trackCount) {
                        val format = extractor.getTrackFormat(i)
                        val mime = format.getString(android.media.MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("audio/")) {
                            extractor.selectTrack(i)
                            if (format.containsKey(android.media.MediaFormat.KEY_DURATION)) {
                                val durUs = format.getLong(android.media.MediaFormat.KEY_DURATION)
                                if (durUs > maxDurationUs) maxDurationUs = durUs
                            }
                            try {
                                extractor.seekTo(Long.MAX_VALUE, android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                                val lastSampleTimeUs = extractor.sampleTime
                                if (lastSampleTimeUs > maxDurationUs) {
                                    maxDurationUs = lastSampleTimeUs
                                }
                            } catch (_: Exception) {
                            }
                            break
                        }
                    }
                    maxDurationUs / 1000L
                } finally {
                    try {
                        extractor.release()
                    } catch (_: Exception) {
                    }
                }
            } ?: 0L
        } catch (e: Exception) {
            Log.e("MediaAnalyzer", "MediaExtractor duration error for $uri", e)
            0L
        }
}
