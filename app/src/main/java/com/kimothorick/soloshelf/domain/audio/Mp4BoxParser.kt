package com.kimothorick.soloshelf.domain.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Mp4BoxParser(
    private val context: Context,
) {
    fun extractChapters(
        uri: Uri,
    ): List<MarkData> {
        val chapters = mutableListOf<MarkData>()
        try {
            chapters.addAll(extractChaptersWithMediaExtractor(uri))

            if (chapters.isEmpty()) {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    pfd.use { parcelFileDescriptor ->
                        FileInputStream(parcelFileDescriptor.fileDescriptor).use { fis ->
                            BufferedInputStream(fis).use { stream ->
                                chapters.addAll(parseChplStream(stream))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Mp4BoxParser", "Error extracting chapters from MP4/M4B $uri", e)
        }
        return chapters
    }

    private fun extractChaptersWithMediaExtractor(
        uri: Uri,
    ): List<MarkData> {
        val marks = mutableListOf<MarkData>()
        val extractor = android.media.MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            var chapterTrackIndex = -1

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(android.media.MediaFormat.KEY_MIME) ?: ""
                if (mime.contains("text") || mime.contains("subtitle") || mime.contains("timed") || mime.contains("chap") || format.containsKey("is-chapter")) {
                    chapterTrackIndex = i
                    break
                }
            }

            if (chapterTrackIndex == -1) {
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    if (format.containsKey("chapter-list") || format.containsKey("chapters")) {
                        chapterTrackIndex = i
                        break
                    }
                }
            }

            if (chapterTrackIndex != -1) {
                extractor.selectTrack(chapterTrackIndex)
                val buffer = ByteBuffer.allocate(8192)
                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break
                    val presentationTimeUs = extractor.sampleTime
                    val chunk = ByteArray(sampleSize)
                    buffer.get(chunk)
                    buffer.clear()

                    val title = if (sampleSize > 2) {
                        val len = ((chunk[0].toInt() and 0xFF) shl 8) or (chunk[1].toInt() and 0xFF)
                        if (len in 1..(sampleSize - 2)) {
                            String(chunk, 2, len, Charsets.UTF_8)
                        } else {
                            String(chunk, Charsets.UTF_8)
                        }
                    } else {
                        String(chunk, Charsets.UTF_8)
                    }

                    val cleanTitle = title.trim().replace(Regex("[\u0000-\u001F]"), "").ifBlank { "Chapter ${marks.size + 1}" }
                    marks.add(MarkData(name = cleanTitle, startMs = presentationTimeUs / 1000L))
                    extractor.advance()
                }
            }
        } catch (e: Exception) {
            Log.e("Mp4BoxParser", "MediaExtractor chapter extraction error", e)
        } finally {
            try {
                extractor.release()
            } catch (_: Exception) {
            }
        }
        return marks
    }

    fun extractEmbeddedSynopsis(
        uri: Uri,
    ): String? {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                pfd.use { parcelFileDescriptor ->
                    FileInputStream(parcelFileDescriptor.fileDescriptor).use { fis ->
                        BufferedInputStream(fis).use { stream ->
                            // Read at most 1MB of the file header/metadata to prevent OOM
                            val bytes = ByteArray(1024 * 1024)
                            val read = stream.read(bytes)
                            if (read > 0) {
                                val actualBytes = if (read < bytes.size) bytes.copyOf(read) else bytes
                                dumpMp4Metadata(actualBytes)
                                return scanForSynopsisBytes(actualBytes, 0, actualBytes.size)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Mp4BoxParser", "Error extracting embedded synopsis", e)
        }
        return null
    }

    private fun dumpMp4Metadata(
        data: ByteArray,
    ) {
        try {
            Log.d("Mp4MetadataDumper", "=== DUMPING ALL MP4 ATOMS (Header 1MB) ===")
            walkAndDumpBoxes(data, 0, data.size, 0)
            Log.d("Mp4MetadataDumper", "=========================================")
        } catch (e: Exception) {
            Log.e("Mp4BoxParser", "Error dumping mp4 metadata", e)
        }
    }

    private fun walkAndDumpBoxes(
        data: ByteArray,
        offset: Int,
        end: Int,
        depth: Int,
    ) {
        var pos = offset
        val indent = "  ".repeat(depth)
        while (pos + 8 <= end) {
            val size32 = ByteBuffer.wrap(data, pos, 4).order(ByteOrder.BIG_ENDIAN).int
            val type = String(data, pos + 4, 4, Charsets.US_ASCII)
            var boxSize = size32.toLong() and 0xFFFFFFFFL
            var headerSize = 8L

            if (boxSize == 1L) {
                if (pos + 16 <= end) {
                    boxSize = ByteBuffer.wrap(data, pos + 8, 8).order(ByteOrder.BIG_ENDIAN).long
                    headerSize = 16L
                } else {
                    break
                }
            } else if (boxSize == 0L) {
                boxSize = (end - pos).toLong()
            }

            if (boxSize < headerSize || pos + boxSize > end) {
                break
            }

            val payloadOffset = (pos + headerSize).toInt()
            val payloadLen = (boxSize - headerSize).toInt()

            if (payloadLen > 0 && payloadOffset + payloadLen <= data.size) {
                val payload = data.copyOfRange(payloadOffset, payloadOffset + payloadLen)
                if (type in setOf("desc", "ldes", "©cmt", "cmt ", "©nam", "©ART", "©alb", "©too", "data", "mean", "name")) {
                    val text = try {
                        String(payload, Charsets.UTF_8).trim { it <= ' ' }
                    } catch (_: Exception) {
                        ""
                    }
                    Log.d("Mp4MetadataDumper", "$indent[$type] text: $text (size: $payloadLen)")
                } else {
                    Log.d("Mp4MetadataDumper", "$indent[$type] size: $boxSize")
                }

                if (type in setOf("moov", "udta", "trak", "tref", "edts", "mdia", "minf", "stbl", "meta", "ilst", "free", "skip")) {
                    val innerOffset = if (type == "meta" && payloadLen >= 4) 4 else 0
                    if (payloadLen > innerOffset) {
                        walkAndDumpBoxes(data, payloadOffset + innerOffset, payloadOffset + payloadLen, depth + 1)
                    }
                }
            }
            pos += boxSize.toInt()
        }
    }

    private fun scanForSynopsisBytes(
        data: ByteArray,
        offset: Int,
        end: Int,
    ): String? {
        var pos = offset
        while (pos + 8 <= end) {
            val size32 = ByteBuffer.wrap(data, pos, 4).order(ByteOrder.BIG_ENDIAN).int
            val type = String(data, pos + 4, 4, Charsets.US_ASCII)
            var boxSize = size32.toLong() and 0xFFFFFFFFL
            var headerSize = 8L

            if (boxSize == 1L) {
                if (pos + 16 <= end) {
                    boxSize = ByteBuffer.wrap(data, pos + 8, 8).order(ByteOrder.BIG_ENDIAN).long
                    headerSize = 16L
                } else {
                    break
                }
            } else if (boxSize == 0L) {
                boxSize = (end - pos).toLong()
            }

            if (boxSize < headerSize || pos + boxSize > end) {
                break
            }

            val payloadOffset = (pos + headerSize).toInt()
            val payloadLen = (boxSize - headerSize).toInt()

            if (payloadLen > 0 && payloadOffset + payloadLen <= data.size) {
                val payload = data.copyOfRange(payloadOffset, payloadOffset + payloadLen)
                if (type in setOf("desc", "ldes", "©cmt", "cmt ")) {
                    val text = extractTextFromPayload(payload)
                    if (!text.isNullOrBlank() && text.length > 3) {
                        return text
                    }
                }

                if (type in setOf("moov", "udta", "trak", "tref", "edts", "mdia", "minf", "stbl", "meta", "ilst")) {
                    val innerOffset = if (type == "meta" && payloadLen >= 4) 4 else 0
                    if (payloadLen > innerOffset) {
                        val sub = scanForSynopsisBytes(data, payloadOffset + innerOffset, payloadOffset + payloadLen)
                        if (!sub.isNullOrBlank()) return sub
                    }
                }
            }
            pos += boxSize.toInt()
        }
        return null
    }

    private fun extractTextFromPayload(
        payload: ByteArray,
    ): String? {
        var i = 0
        while (i < payload.size - 4) {
            if (payload[i] == 'd'.code.toByte() &&
                payload[i + 1] == 'a'.code.toByte() &&
                payload[i + 2] == 't'.code.toByte() &&
                payload[i + 3] == 'a'.code.toByte()
            ) {
                val textStart = i + 8
                val textLen = payload.size - textStart
                if (textStart >= 0 && textLen > 2) {
                    return String(payload, textStart, textLen, Charsets.UTF_8).trim()
                }
            }
            i++
        }
        return if (payload.size > 8) String(payload, 8, payload.size - 8, Charsets.UTF_8).trim() else null
    }

    private fun parseChplStream(
        buffered: BufferedInputStream,
    ): List<MarkData> {
        val chunk = ByteArray(65536)
        var prevTail = ByteArray(0)

        while (true) {
            val read = buffered.read(chunk)
            if (read <= 0) break
            val combined = prevTail + chunk.copyOfRange(0, read)
            for (i in 0 until combined.size - 4) {
                if (combined[i] == 'c'.code.toByte() &&
                    combined[i + 1] == 'h'.code.toByte() &&
                    combined[i + 2] == 'p'.code.toByte() &&
                    combined[i + 3] == 'l'.code.toByte()
                ) {
                    val atomStart = i - 4
                    if (atomStart >= 0 && atomStart + 8 <= combined.size) {
                        val size = ByteBuffer
                            .wrap(combined, atomStart, 4)
                            .order(ByteOrder.BIG_ENDIAN)
                            .int
                            .toLong() and 0xFFFFFFFFL
                        val payloadOffset = atomStart + 8
                        val payloadLen = if (size > 8 && size < 100_000L) (size - 8).toInt() else (combined.size - payloadOffset)
                        if (payloadOffset + payloadLen <= combined.size && payloadLen > 0) {
                            val payload = combined.copyOfRange(payloadOffset, payloadOffset + payloadLen)
                            return parseChplPayload(payload)
                        }
                    }
                }
            }
            prevTail = combined.takeLast(8).toByteArray()
        }
        return emptyList()
    }

    private fun parseChplPayload(
        payload: ByteArray,
    ): List<MarkData> {
        val marks = mutableListOf<MarkData>()
        try {
            val bb = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
            if (bb.remaining() >= 5) {
                bb.position(4) // version & flags
                val count = bb.get().toInt() and 0xFF
                for (c in 0 until count) {
                    if (bb.remaining() < 9) break
                    val start100ns = bb.long
                    val nameLen = bb.get().toInt() and 0xFF
                    if (bb.remaining() < nameLen) break
                    val nameBytes = ByteArray(nameLen)
                    bb.get(nameBytes)
                    val name = String(nameBytes, Charsets.UTF_8).ifBlank { "Chapter ${c + 1}" }
                    marks.add(MarkData(name = name, startMs = start100ns / 10_000L))
                }
            }
        } catch (e: Exception) {
            Log.e("Mp4BoxParser", "Error parsing chpl payload", e)
        }
        return marks
    }
}
