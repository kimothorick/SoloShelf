package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val bookmarkId: Long = 0,
    val bookId: Long,
    val location: String,
    val title: String?,
)
