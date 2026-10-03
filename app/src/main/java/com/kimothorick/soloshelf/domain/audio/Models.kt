package com.kimothorick.soloshelf.domain.audio

import kotlin.time.Duration

data class MarkData(
    val name: String?,
    val startMs: Long,
)

data class Metadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: Duration,
    val genre: String?,
    val narrator: String?,
    val series: String?,
    val part: String?,
    val synopsis: String? = null,
    val year: String? = null,
    val mediaType: String? = null,
    val bitrate: Int? = null,
    val samplingRate: Int? = null,
    val chapters: List<MarkData> = emptyList(),
)
