package com.kimothorick.soloshelf.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithNotes(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId",
    )
    val notes: List<Note>,
)
