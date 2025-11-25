package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val noteId: Long = 0,
    val bookId: Long,
    val location: String, // CFI or Milliseconds
    val noteText: String,
)
