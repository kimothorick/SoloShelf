package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_progress",
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
data class AudioProgress(
    @PrimaryKey
    val bookId: Long,
    val currentTrackId: Long? = null,
    val currentOffsetMs: Long = 0,
    val lastAccessed: Long = System.currentTimeMillis()
)
