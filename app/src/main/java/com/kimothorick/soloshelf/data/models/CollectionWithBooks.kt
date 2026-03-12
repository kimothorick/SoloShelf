package com.kimothorick.soloshelf.data.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithBooks(
    @Embedded val collection: Collection,
    @Relation(
        parentColumn = "collectionId",
        entityColumn = "bookId",
        associateBy = Junction(CollectionBookCrossRef::class)
    )
    val books: List<Book>
)
