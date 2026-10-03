package com.kimothorick.soloshelf.domain.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Mp4ChapterExtractor(private val context: Context) {

    fun extractChapters(uri: Uri): List<MarkData> {
        val chapters = mutableListOf<MarkData>()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                chapters.addAll(parseChplChapters(stream))
            }
        } catch (e: Exception) {
            Log.e("Mp4ChapterExtractor", "Error extracting MP4 chapters from $uri", e)
        }
        return chapters
    }

    private fun parseChplChapters(stream: InputStream): List<MarkData> {
        val marks = mutableListOf<MarkData>()
        val buffer = ByteArray(16384)
        var bytesRead = stream.read(buffer)

        while (bytesRead > 0) {
            for (i in 0 until bytesRead - 4) {
                // Look for 'chpl' atom header
                if (buffer[i] == 'c'.code.toByte() &&
                    buffer[i + 1] == 'h'.code.toByte() &&
                    buffer[i + 2] == 'p'.code.toByte() &&
                    buffer[i + 3] == 'l'.code.toByte()
                ) {
                    val payload = ByteArray(2048)
                    val readLen = stream.read(payload)
                    if (readLen > 8) {
                        marks.addAll(decodeChplAtom(payload, readLen))
                        if (marks.isNotEmpty()) return marks
                    }
                }
            }
            bytesRead = stream.read(buffer)
        }
        return marks
    }

    private fun decodeChplAtom(payload: ByteArray, length: Int): List<MarkData> {
        val marks = mutableListOf<MarkData>()
        val bb = ByteBuffer.wrap(payload, 0, length).order(ByteOrder.BIG_ENDIAN)

        try {
            bb.position(4) // Skip version/flags
            val chapterCount = bb.get().toInt() and 0xFF

            for (i in 0 until chapterCount) {
                if (bb.remaining() < 9) break
                val start100ns = bb.long
                val nameLen = bb.get().toInt() and 0xFF

                if (bb.remaining() < nameLen) break
                val nameBytes = ByteArray(nameLen)
                bb.get(nameBytes)
                val name = String(nameBytes, Charsets.UTF_8).ifBlank { "Chapter ${i + 1}" }

                val startMs = start100ns / 10_000L
                marks.add(MarkData(name = name, startMs = startMs))
            }
        } catch (e: Exception) {
            Log.e("Mp4ChapterExtractor", "Error decoding chpl payload", e)
        }
        return marks
    }
}
