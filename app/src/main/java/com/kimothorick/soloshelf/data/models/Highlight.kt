package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Highlight(
    @PrimaryKey(autoGenerate = true)
    val highlightId: Long = 0,
    val bookId: Long,
    val startLocation: String, // CFI or Milliseconds
    val endLocation: String, // CFI or Milliseconds
    val color: String, // e.g., "#FFEB3B" or "yellow"
)
