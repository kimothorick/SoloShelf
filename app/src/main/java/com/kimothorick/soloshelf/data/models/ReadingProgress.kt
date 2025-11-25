package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "progress",
    primaryKeys = ["bookId"],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ReadingProgress(
    val bookId: Long,
    val lastPosition: String,
    val readPercentage: Float,
    val lastAccessed: Long,
)
