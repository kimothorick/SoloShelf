package com.kimothorick.soloshelf.domain.scanner

import android.content.Context
import android.net.Uri

class MediaAnalyzer(context: Context) {

    private val audioEngine = com.kimothorick.soloshelf.domain.audio.MediaAnalyzer(context)

    fun analyze(uri: Uri, fileName: String): AnalyzedMetadata {
        val audioMeta = audioEngine.analyze(uri, fileName)
        val chapterMarks = audioMeta.chapters.map { mark ->
            ChapterMark(
                name = mark.name ?: "Chapter",
                startMs = mark.startMs,
                endMs = 0L,
            )
        }
        return AnalyzedMetadata(
            title = audioMeta.title,
            artist = audioMeta.artist,
            album = audioMeta.album,
            durationMs = audioMeta.duration.inWholeMilliseconds,
            genre = audioMeta.genre,
            narrator = audioMeta.narrator,
            series = audioMeta.series,
            part = audioMeta.part,
            synopsis = audioMeta.synopsis,
            year = audioMeta.year,
            mediaType = audioMeta.mediaType,
            bitrate = audioMeta.bitrate,
            samplingRate = audioMeta.samplingRate,
            chapters = chapterMarks,
        )
    }
}
