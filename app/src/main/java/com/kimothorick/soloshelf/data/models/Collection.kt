package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val collectionId: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "collection_book_cross_ref",
    primaryKeys = ["collectionId", "bookId"],
    indices = [Index("bookId")],
)
data class CollectionBookCrossRef(
    val collectionId: Long,
    val bookId: Long,
)
