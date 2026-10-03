package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class Track(
    @PrimaryKey(autoGenerate = true)
    val trackId: Long = 0,
    val bookId: Long,
    val filePath: String,
    val title: String,
    val duration: Int = 0,
    val trackNumber: Int = 0,
    val startMs: Long = 0,
    val endMs: Long = 0,
)
