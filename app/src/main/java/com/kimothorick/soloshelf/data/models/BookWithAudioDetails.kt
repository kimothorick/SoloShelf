package com.kimothorick.soloshelf.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithAudioDetails(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId"
    )
    val audioDetails: AudiobookDetails?,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId"
    )
    val tracks: List<Track>,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId"
    )
    val audioProgress: AudioProgress?
)
