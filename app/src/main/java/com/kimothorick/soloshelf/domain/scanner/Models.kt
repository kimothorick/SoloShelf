package com.kimothorick.soloshelf.domain.scanner

import android.net.Uri

@JvmInline
value class BookId(val uriString: String) {
    val uri: Uri get() = Uri.parse(uriString)
}

data class ChapterMark(
    val name: String,
    val startMs: Long,
    val endMs: Long,
)

data class AnalyzedMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0L,
    val genre: String? = null,
    val narrator: String? = null,
    val series: String? = null,
    val part: String? = null,
    val synopsis: String? = null,
    val year: String? = null,
    val mediaType: String? = null,
    val bitrate: Int? = null,
    val samplingRate: Int? = null,
    val chapters: List<ChapterMark> = emptyList(),
)

data class ChapterRecord(
    val id: String,
    val uri: Uri,
    val name: String,
    val fileLastModified: Long,
    val fileSize: Long,
    val metadata: AnalyzedMetadata,
)

data class BookContent(
    val id: BookId,
    val title: String,
    val author: String,
    val coverPath: String? = null,
    val synopsis: String? = null,
    val chapters: List<ChapterRecord>,
    val currentChapterId: String,
    val positionInChapterMs: Long = 0L,
    val isActive: Boolean = true,
)
