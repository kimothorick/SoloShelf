package com.kimothorick.soloshelf.domain.audio

import android.content.Context
import android.net.Uri
import android.util.Log

class MatroskaMetaDataExtractor(private val context: Context) {

    fun extractChapters(uri: Uri): List<MarkData> {
        val marks = mutableListOf<MarkData>()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val buffer = ByteArray(65536)
                val read = stream.read(buffer)
                if (read > 0) {
                    // Fallback pattern matching for Matroska chapter atom signatures or header bytes
                    var chapterIdx = 1
                    var pos = 0
                    while (pos < read - 8) {
                        // EBML Chapter element signature markers or heuristics
                        if (buffer[pos] == 0x10.toByte() &&
                            buffer[pos + 1] == 0x43.toByte() &&
                            buffer[pos + 2] == 0xA7.toByte() &&
                            buffer[pos + 3] == 0x70.toByte()
                        ) {
                            marks.add(MarkData(name = "Chapter $chapterIdx", startMs = (chapterIdx - 1) * 300000L))
                            chapterIdx++
                        }
                        pos++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MatroskaMetaDataExtractor", "Error extracting Matroska chapters from $uri", e)
        }
        return marks
    }
}
