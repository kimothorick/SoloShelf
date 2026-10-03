package com.kimothorick.soloshelf.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithEbookDetails(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId"
    )
    val ebookDetails: EbookDetails?,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId"
    )
    val progress: ReadingProgress?
)
