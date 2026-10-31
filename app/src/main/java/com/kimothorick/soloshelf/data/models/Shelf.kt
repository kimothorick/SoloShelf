package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shelves")
data class Shelf(
    @PrimaryKey(autoGenerate = true)
    val shelfId: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "shelf_book_cross_ref",
    primaryKeys = ["shelfId", "bookId"],
    indices = [Index("bookId")],
)
data class ShelfBookCrossRef(
    val shelfId: Long,
    val bookId: Long,
)
