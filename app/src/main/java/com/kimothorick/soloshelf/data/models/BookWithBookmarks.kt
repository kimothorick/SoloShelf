package com.kimothorick.soloshelf.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithBookmarks(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId",
    )
    val bookmarks: List<Bookmark>,
)
