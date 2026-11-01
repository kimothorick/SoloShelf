package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ebook_details",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["bookId"])],
)
data class EbookDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val pageCount: Int = 0,
    val hasImages: Boolean = false,
)
